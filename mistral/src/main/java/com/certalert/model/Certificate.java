package com.certalert.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Certificate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "issuer", nullable = false)
    private String issuer;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "serial_number", nullable = false)
    private String serialNumber;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @Column(name = "source_type")
    @Enumerated(EnumType.STRING)
    private SourceType sourceType;

    @Column(name = "source_url")
    private String sourceUrl;

    public enum SourceType {
        FILE_UPLOAD, URL_FETCH
    }

    public Certificate() {
    }

    public Certificate(Long id, String name, String content, LocalDate expiryDate, String issuer, 
                      String subject, String serialNumber, User createdBy, Group group, 
                      SourceType sourceType, String sourceUrl) {
        this.id = id;
        this.name = name;
        this.content = content;
        this.expiryDate = expiryDate;
        this.issuer = issuer;
        this.subject = subject;
        this.serialNumber = serialNumber;
        this.createdBy = createdBy;
        this.group = group;
        this.sourceType = sourceType;
        this.sourceUrl = sourceUrl;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public Group getGroup() { return group; }
    public void setGroup(Group group) { this.group = group; }
    public SourceType getSourceType() { return sourceType; }
    public void setSourceType(SourceType sourceType) { this.sourceType = sourceType; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
}