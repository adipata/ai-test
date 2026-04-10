package com.certalert.controller;

import com.certalert.model.AlertConfiguration;
import com.certalert.service.AlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping("/config")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<AlertConfiguration> getAlertConfiguration(@RequestParam("groupId") Long groupId) {
        AlertConfiguration config = alertService.getAlertConfiguration(groupId);
        return config != null ? ResponseEntity.ok(config) : ResponseEntity.notFound().build();
    }

    @PutMapping("/config")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<AlertConfiguration> updateAlertConfiguration(
            @RequestParam("groupId") Long groupId,
            @RequestParam("thresholdDays") int thresholdDays,
            @RequestParam("emailEnabled") boolean emailEnabled,
            @RequestParam("emailRecipients") String emailRecipients) {
        
        AlertConfiguration config = alertService.updateAlertConfiguration(
                groupId, thresholdDays, emailEnabled, emailRecipients);
        return ResponseEntity.ok(config);
    }
}