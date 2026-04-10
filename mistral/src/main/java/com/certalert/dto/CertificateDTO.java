package com.certalert.dto;

import java.time.LocalDate;

public class CertificateDTO {
    private Long id;
    private String name;
    private LocalDate expiryDate;
    private String issuer;
    private String subject;
    private String serialNumber;
    private String createdBy;
    private String groupName;
    private String sourceType;
    private String sourceUrl;
    private int daysUntilExpiry;
    private boolean isExpiringSoon;

    public CertificateDTO() {
    }

    public CertificateDTO(Long id, String name, LocalDate expiryDate, String issuer, String subject, 
                         String serialNumber, String createdBy, String groupName, String sourceType, 
                         String sourceUrl, int daysUntilExpiry, boolean isExpiringSoon) {
        this.id = id;
        this.name = name;
        this.expiryDate = expiryDate;
        this.issuer = issuer;
        this.subject = subject;
        this.serialNumber = serialNumber;
        this.createdBy = createdBy;
        this.groupName = groupName;
        this.sourceType = sourceType;
        this.sourceUrl = sourceUrl;
        this.daysUntilExpiry = daysUntilExpiry;
        this.isExpiringSoon = isExpiringSoon;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
    public int getDaysUntilExpiry() { return daysUntilExpiry; }
    public void setDaysUntilExpiry(int daysUntilExpiry) { this.daysUntilExpiry = daysUntilExpiry; }
    public boolean isExpiringSoon() { return isExpiringSoon; }
    public void setExpiringSoon(boolean expiringSoon) { isExpiringSoon = expiringSoon; }
}