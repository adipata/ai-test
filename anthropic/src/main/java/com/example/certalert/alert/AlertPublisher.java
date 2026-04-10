package com.example.certalert.alert;

import com.example.certalert.certificate.Certificate;

import java.util.List;

/**
 * SPI for delivering expiration alerts. The default implementation logs; plug in additional
 * implementations (email, Slack, PagerDuty) without touching the scan logic.
 */
public interface AlertPublisher {
    void publish(List<Certificate> expiring, int thresholdDays);
}
