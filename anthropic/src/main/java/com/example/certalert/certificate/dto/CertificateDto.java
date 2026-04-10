package com.example.certalert.certificate.dto;

import com.example.certalert.certificate.Certificate;
import com.example.certalert.certificate.CertificateSource;

import java.time.Duration;
import java.time.Instant;

public record CertificateDto(
        Long id,
        String alias,
        String subject,
        String issuer,
        String serialNumber,
        Instant notBefore,
        Instant notAfter,
        long daysUntilExpiry,
        String signatureAlgorithm,
        String fingerprintSha256,
        String ownerGroup,
        String uploadedBy,
        Instant uploadedAt,
        CertificateSource source,
        String sourceRef
) {
    public static CertificateDto from(Certificate c) {
        long days = Duration.between(Instant.now(), c.getNotAfter()).toDays();
        return new CertificateDto(
                c.getId(),
                c.getAlias(),
                c.getSubject(),
                c.getIssuer(),
                c.getSerialNumber(),
                c.getNotBefore(),
                c.getNotAfter(),
                days,
                c.getSignatureAlgorithm(),
                c.getFingerprintSha256(),
                c.getOwnerGroup(),
                c.getUploadedBy(),
                c.getUploadedAt(),
                c.getSource(),
                c.getSourceRef()
        );
    }
}
