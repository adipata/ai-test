package com.example.certalert.certificate.dto;

import jakarta.validation.constraints.NotBlank;

public record UrlFetchRequest(@NotBlank String url, String alias) {}
