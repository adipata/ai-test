package com.certalert.model;

import jakarta.persistence.*;

@Entity
public class AlertConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @Column(name = "threshold_days", nullable = false)
    private int thresholdDays;

    @Column(name = "email_enabled", nullable = false)
    private boolean emailEnabled = true;

    @Column(name = "email_recipients")
    private String emailRecipients;

    public AlertConfiguration() {
    }

    public AlertConfiguration(Long id, Group group, int thresholdDays, boolean emailEnabled, String emailRecipients) {
        this.id = id;
        this.group = group;
        this.thresholdDays = thresholdDays;
        this.emailEnabled = emailEnabled;
        this.emailRecipients = emailRecipients;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Group getGroup() { return group; }
    public void setGroup(Group group) { this.group = group; }
    public int getThresholdDays() { return thresholdDays; }
    public void setThresholdDays(int thresholdDays) { this.thresholdDays = thresholdDays; }
    public boolean isEmailEnabled() { return emailEnabled; }
    public void setEmailEnabled(boolean emailEnabled) { this.emailEnabled = emailEnabled; }
    public String getEmailRecipients() { return emailRecipients; }
    public void setEmailRecipients(String emailRecipients) { this.emailRecipients = emailRecipients; }
}