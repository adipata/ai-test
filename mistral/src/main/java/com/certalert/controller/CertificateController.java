package com.certalert.controller;

import com.certalert.dto.CertificateDTO;
import com.certalert.service.CertificateService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.List;

@RestController
@RequestMapping("/api/certificates")
public class CertificateController {

    private final CertificateService certificateService;

    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<CertificateDTO> uploadCertificate(
            @RequestParam("file") MultipartFile file,
            @RequestParam("groupId") Long groupId,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            CertificateDTO certificateDTO = certificateService.uploadCertificateFromFile(file, username, groupId);
            return ResponseEntity.ok(certificateDTO);
        } catch (IOException | CertificateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/fetch")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<CertificateDTO> fetchCertificateFromUrl(
            @RequestParam("url") String url,
            @RequestParam("groupId") Long groupId,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            CertificateDTO certificateDTO = certificateService.fetchCertificateFromUrl(url, username, groupId);
            return ResponseEntity.ok(certificateDTO);
        } catch (CertificateException | IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<List<CertificateDTO>> getCertificatesByGroup(
            @RequestParam("groupId") Long groupId) {
        List<CertificateDTO> certificates = certificateService.getCertificatesByGroup(groupId);
        return ResponseEntity.ok(certificates);
    }

    @GetMapping("/expiring")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<List<CertificateDTO>> getExpiringCertificates(
            @RequestParam(value = "threshold", defaultValue = "30") int thresholdDays) {
        List<CertificateDTO> certificates = certificateService.getExpiringCertificates(thresholdDays);
        return ResponseEntity.ok(certificates);
    }
}