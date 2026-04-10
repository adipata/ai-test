package com.example.certalert.certificate.parser;

import java.time.Instant;

/**
 * Immutable snapshot of the fields we care about from an {@link java.security.cert.X509Certificate},
 * plus the original PEM for storage.
 */
public record ParsedCertificate(
        String subject,
        String issuer,
        String serialNumber,
        Instant notBefore,
        Instant notAfter,
        String signatureAlgorithm,
        String fingerprintSha256,
        String pem
) {}
