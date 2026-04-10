package com.example.certalert.certificate.parser;

import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Parses an X.509 certificate from bytes (DER or PEM). The JDK's {@code CertificateFactory}
 * handles both encodings natively — no BouncyCastle needed.
 */
@Component
public class CertificateParser {

    private static final String BEGIN = "-----BEGIN CERTIFICATE-----";
    private static final String END = "-----END CERTIFICATE-----";

    public ParsedCertificate parse(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            throw new CertificateParseException("certificate payload is empty");
        }
        try {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            try (InputStream in = new ByteArrayInputStream(bytes)) {
                X509Certificate cert = (X509Certificate) factory.generateCertificate(in);
                if (cert == null) {
                    throw new CertificateParseException("no certificate found in payload");
                }
                return toParsed(cert);
            }
        } catch (CertificateException e) {
            throw new CertificateParseException("not a valid X.509 certificate: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new CertificateParseException("failed to parse certificate: " + e.getMessage(), e);
        }
    }

    public ParsedCertificate fromX509(X509Certificate cert) {
        return toParsed(cert);
    }

    private ParsedCertificate toParsed(X509Certificate cert) {
        try {
            byte[] der = cert.getEncoded();
            String pem = toPem(der);
            String fingerprint = sha256Colon(der);
            return new ParsedCertificate(
                    cert.getSubjectX500Principal().getName(),
                    cert.getIssuerX500Principal().getName(),
                    cert.getSerialNumber().toString(16).toUpperCase(),
                    cert.getNotBefore().toInstant(),
                    cert.getNotAfter().toInstant(),
                    cert.getSigAlgName(),
                    fingerprint,
                    pem
            );
        } catch (CertificateEncodingException e) {
            throw new CertificateParseException("failed to re-encode certificate", e);
        }
    }

    private static String toPem(byte[] der) {
        String b64 = Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(der);
        return BEGIN + "\n" + b64 + "\n" + END + "\n";
    }

    private static String sha256Colon(byte[] der) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(der);
            String hex = HexFormat.of().withUpperCase().formatHex(hash);
            StringBuilder sb = new StringBuilder(hex.length() + hex.length() / 2);
            for (int i = 0; i < hex.length(); i += 2) {
                if (i > 0) sb.append(':');
                sb.append(hex, i, i + 2);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
