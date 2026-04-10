package com.example.certalert.certificate.parser;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.*;

class CertificateParserTest {

    private final CertificateParser parser = new CertificateParser();

    @Test
    void parsesPemCertificate() throws Exception {
        byte[] bytes = new ClassPathResource("test-cert.pem").getInputStream().readAllBytes();
        ParsedCertificate parsed = parser.parse(bytes);

        assertTrue(parsed.subject().contains("CN=test.example.com"));
        assertTrue(parsed.issuer().contains("CN=test.example.com"));
        assertNotNull(parsed.serialNumber());
        assertNotNull(parsed.notBefore());
        assertNotNull(parsed.notAfter());
        assertTrue(parsed.notAfter().isAfter(parsed.notBefore()));
        assertTrue(parsed.signatureAlgorithm().toLowerCase().contains("rsa"));
        assertTrue(parsed.fingerprintSha256().matches("([0-9A-F]{2}:){31}[0-9A-F]{2}"),
                "fingerprint should be colon-separated SHA-256 hex");
        assertTrue(parsed.pem().startsWith("-----BEGIN CERTIFICATE-----"));
    }

    @Test
    void rejectsEmptyPayload() {
        assertThrows(CertificateParseException.class, () -> parser.parse(new byte[0]));
    }

    @Test
    void rejectsGarbage() {
        assertThrows(CertificateParseException.class,
                () -> parser.parse("not a certificate".getBytes()));
    }
}
