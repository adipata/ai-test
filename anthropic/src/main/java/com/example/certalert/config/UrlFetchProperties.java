package com.example.certalert.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cert-alert.url-fetch")
public record UrlFetchProperties(boolean blockPrivateNetworks,
                                 int connectTimeoutMs,
                                 int readTimeoutMs) {}
