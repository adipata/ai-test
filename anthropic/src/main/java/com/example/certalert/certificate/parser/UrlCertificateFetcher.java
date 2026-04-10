package com.example.certalert.certificate.parser;

import com.example.certalert.config.UrlFetchProperties;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

/**
 * Connects to a TLS endpoint and returns the server's leaf certificate.
 *
 * Uses a trust-all X509TrustManager intentionally — we want to record whatever
 * certificate the server actually presents, even if it is expired, self-signed,
 * or otherwise untrusted (those are precisely the cases worth alerting on).
 *
 * An SSRF guard rejects loopback / link-local / private ranges by default; disable
 * {@code cert-alert.url-fetch.block-private-networks} only in trusted environments.
 */
@Component
public class UrlCertificateFetcher {

    private final UrlFetchProperties properties;
    private final CertificateParser parser;

    public UrlCertificateFetcher(UrlFetchProperties properties, CertificateParser parser) {
        this.properties = properties;
        this.parser = parser;
    }

    public ParsedCertificate fetch(String rawInput) {
        HostPort target = parseInput(rawInput);
        if (properties.blockPrivateNetworks()) {
            assertPublicAddress(target.host());
        }
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, new TrustManager[]{TRUST_ALL}, null);
            SSLSocketFactory factory = ctx.getSocketFactory();
            try (SSLSocket socket = (SSLSocket) factory.createSocket()) {
                socket.connect(new InetSocketAddress(target.host(), target.port()), properties.connectTimeoutMs());
                socket.setSoTimeout(properties.readTimeoutMs());
                // SNI is enabled by default when connecting via a hostname — good.
                socket.startHandshake();
                Certificate[] chain = socket.getSession().getPeerCertificates();
                if (chain.length == 0 || !(chain[0] instanceof X509Certificate leaf)) {
                    throw new CertificateParseException("server did not present an X.509 certificate");
                }
                return parser.fromX509(leaf);
            }
        } catch (CertificateParseException e) {
            throw e;
        } catch (Exception e) {
            throw new CertificateParseException(
                    "failed to fetch certificate from " + target.host() + ":" + target.port() + " — " + e.getMessage(), e);
        }
    }

    HostPort parseInput(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new CertificateParseException("url must not be empty");
        }
        String input = raw.trim();
        try {
            // Try URI first for inputs like "https://www.google.com/path".
            if (input.contains("://")) {
                URI uri = URI.create(input);
                String host = uri.getHost();
                if (host == null) {
                    throw new CertificateParseException("could not extract host from '" + raw + "'");
                }
                int port = uri.getPort() > 0 ? uri.getPort() : 443;
                return new HostPort(host, port);
            }
            // Bare "host" or "host:port".
            String host = input;
            int port = 443;
            int colon = input.lastIndexOf(':');
            if (colon > 0 && input.indexOf(':') == colon /* not an IPv6 literal */) {
                host = input.substring(0, colon);
                port = Integer.parseInt(input.substring(colon + 1));
            }
            // Strip any trailing path the user might have pasted.
            int slash = host.indexOf('/');
            if (slash >= 0) host = host.substring(0, slash);
            if (host.isBlank()) {
                throw new CertificateParseException("could not extract host from '" + raw + "'");
            }
            return new HostPort(host, port);
        } catch (NumberFormatException e) {
            throw new CertificateParseException("invalid port in '" + raw + "'");
        }
    }

    private void assertPublicAddress(String host) {
        try {
            for (InetAddress addr : InetAddress.getAllByName(host)) {
                if (addr.isAnyLocalAddress() || addr.isLoopbackAddress()
                        || addr.isLinkLocalAddress() || addr.isSiteLocalAddress()
                        || addr.isMulticastAddress()) {
                    throw new CertificateParseException(
                            "refusing to fetch from private / loopback address: " + addr.getHostAddress());
                }
            }
        } catch (UnknownHostException e) {
            throw new CertificateParseException("unknown host: " + host);
        }
    }

    public record HostPort(String host, int port) {}

    private static final X509TrustManager TRUST_ALL = new X509TrustManager() {
        @Override public void checkClientTrusted(X509Certificate[] chain, String authType) {}
        @Override public void checkServerTrusted(X509Certificate[] chain, String authType) {}
        @Override public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
    };
}
