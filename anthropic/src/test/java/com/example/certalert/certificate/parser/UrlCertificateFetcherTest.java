package com.example.certalert.certificate.parser;

import com.example.certalert.config.UrlFetchProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Focuses on the host/port parsing and the SSRF guard — we do not actually open sockets here.
 */
class UrlCertificateFetcherTest {

    private final UrlCertificateFetcher fetcher =
            new UrlCertificateFetcher(new UrlFetchProperties(true, 1000, 1000), new CertificateParser());

    @Test
    void acceptsBareHost() {
        var hp = fetcher.parseInput("www.google.com");
        assertEquals("www.google.com", hp.host());
        assertEquals(443, hp.port());
    }

    @Test
    void acceptsHostWithPort() {
        var hp = fetcher.parseInput("example.com:8443");
        assertEquals("example.com", hp.host());
        assertEquals(8443, hp.port());
    }

    @Test
    void acceptsHttpsUrl() {
        var hp = fetcher.parseInput("https://www.example.com/some/path");
        assertEquals("www.example.com", hp.host());
        assertEquals(443, hp.port());
    }

    @Test
    void acceptsHttpsUrlWithPort() {
        var hp = fetcher.parseInput("https://example.com:9443/foo");
        assertEquals("example.com", hp.host());
        assertEquals(9443, hp.port());
    }

    @Test
    void stripsTrailingPathFromBareHost() {
        var hp = fetcher.parseInput("example.com/foo");
        assertEquals("example.com", hp.host());
    }

    @Test
    void rejectsBlank() {
        assertThrows(CertificateParseException.class, () -> fetcher.parseInput(""));
    }

    @Test
    void rejectsLoopbackWhenGuardEnabled() {
        assertThrows(CertificateParseException.class, () -> fetcher.fetch("127.0.0.1"));
    }

    @Test
    void rejectsLocalhostWhenGuardEnabled() {
        assertThrows(CertificateParseException.class, () -> fetcher.fetch("localhost"));
    }
}
