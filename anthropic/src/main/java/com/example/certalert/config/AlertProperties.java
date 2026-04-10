package com.example.certalert.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cert-alert.alert")
public record AlertProperties(int defaultThresholdDays, String scanCron) {}
