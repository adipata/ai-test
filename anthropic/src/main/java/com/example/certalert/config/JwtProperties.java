package com.example.certalert.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cert-alert.jwt")
public record JwtProperties(String secret, long ttlMinutes, String issuer) {}
