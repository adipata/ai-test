package com.example.certalert.alert;

import jakarta.persistence.*;

/**
 * Single-row configuration entity holding the runtime-editable alert threshold (in days).
 * A row is lazily created on first read, seeded from {@code cert-alert.alert.default-threshold-days}.
 */
@Entity
@Table(name = "threshold_setting")
public class ThresholdSetting {

    public static final long SINGLETON_ID = 1L;

    @Id
    private Long id = SINGLETON_ID;

    @Column(name = "threshold_days", nullable = false)
    private int thresholdDays;

    protected ThresholdSetting() {}

    public ThresholdSetting(int thresholdDays) {
        this.id = SINGLETON_ID;
        this.thresholdDays = thresholdDays;
    }

    public int getThresholdDays() { return thresholdDays; }

    public void setThresholdDays(int thresholdDays) {
        if (thresholdDays < 1) {
            throw new IllegalArgumentException("threshold must be at least 1 day");
        }
        this.thresholdDays = thresholdDays;
    }
}
