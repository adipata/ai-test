package com.example.certalert.certificate;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * A stored X.509 certificate. Group scoping is enforced at the repository level: a user
 * may only query certificates whose {@code ownerGroup} matches their authenticated group.
 */
@Entity
@Table(name = "certificate", indexes = {
        @Index(name = "idx_certificate_group", columnList = "owner_group"),
        @Index(name = "idx_certificate_not_after", columnList = "not_after")
})
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Human-friendly label — filename or host for URL-sourced certs. */
    @Column(nullable = false, length = 255)
    private String alias;

    @Column(nullable = false, length = 512)
    private String subject;

    @Column(nullable = false, length = 512)
    private String issuer;

    @Column(name = "serial_number", nullable = false, length = 128)
    private String serialNumber;

    @Column(name = "not_before", nullable = false)
    private Instant notBefore;

    @Column(name = "not_after", nullable = false)
    private Instant notAfter;

    @Column(name = "signature_algorithm", length = 64)
    private String signatureAlgorithm;

    @Column(name = "fingerprint_sha256", nullable = false, length = 95)
    private String fingerprintSha256;

    @Column(name = "owner_group", nullable = false, length = 64)
    private String ownerGroup;

    @Column(name = "uploaded_by", nullable = false, length = 64)
    private String uploadedBy;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private CertificateSource source;

    /** Original file name (FILE source) or host:port (URL source). */
    @Column(name = "source_ref", nullable = false, length = 255)
    private String sourceRef;

    /** PEM-encoded certificate for download / re-parsing. */
    @Lob
    @Column(name = "pem", nullable = false)
    private String pem;

    protected Certificate() {}

    public Certificate(String alias, String subject, String issuer, String serialNumber,
                       Instant notBefore, Instant notAfter, String signatureAlgorithm,
                       String fingerprintSha256, String ownerGroup, String uploadedBy,
                       Instant uploadedAt, CertificateSource source, String sourceRef, String pem) {
        this.alias = alias;
        this.subject = subject;
        this.issuer = issuer;
        this.serialNumber = serialNumber;
        this.notBefore = notBefore;
        this.notAfter = notAfter;
        this.signatureAlgorithm = signatureAlgorithm;
        this.fingerprintSha256 = fingerprintSha256;
        this.ownerGroup = ownerGroup;
        this.uploadedBy = uploadedBy;
        this.uploadedAt = uploadedAt;
        this.source = source;
        this.sourceRef = sourceRef;
        this.pem = pem;
    }

    public Long getId() { return id; }
    public String getAlias() { return alias; }
    public String getSubject() { return subject; }
    public String getIssuer() { return issuer; }
    public String getSerialNumber() { return serialNumber; }
    public Instant getNotBefore() { return notBefore; }
    public Instant getNotAfter() { return notAfter; }
    public String getSignatureAlgorithm() { return signatureAlgorithm; }
    public String getFingerprintSha256() { return fingerprintSha256; }
    public String getOwnerGroup() { return ownerGroup; }
    public String getUploadedBy() { return uploadedBy; }
    public Instant getUploadedAt() { return uploadedAt; }
    public CertificateSource getSource() { return source; }
    public String getSourceRef() { return sourceRef; }
    public String getPem() { return pem; }
}
