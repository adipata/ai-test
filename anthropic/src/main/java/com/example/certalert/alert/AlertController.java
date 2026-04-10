package com.example.certalert.alert;

import com.example.certalert.certificate.dto.CertificateDto;
import jakarta.validation.constraints.Min;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Validated
public class AlertController {

    private final AlertService alertService;
    private final ThresholdService thresholdService;

    public AlertController(AlertService alertService, ThresholdService thresholdService) {
        this.alertService = alertService;
        this.thresholdService = thresholdService;
    }

    /** Certificates from the caller's group that are within the current threshold. */
    @GetMapping("/alerts")
    @PreAuthorize("hasAnyRole('VIEWER','MANAGER')")
    public AlertsResponse alerts() {
        int threshold = thresholdService.current();
        List<CertificateDto> expiring = alertService.expiringForCurrentUser().stream()
                .map(CertificateDto::from).toList();
        return new AlertsResponse(threshold, expiring);
    }

    @GetMapping("/config/threshold")
    @PreAuthorize("hasAnyRole('VIEWER','MANAGER')")
    public ThresholdResponse getThreshold() {
        return new ThresholdResponse(thresholdService.current());
    }

    @PutMapping("/config/threshold")
    @PreAuthorize("hasRole('MANAGER')")
    public ThresholdResponse updateThreshold(@RequestBody ThresholdUpdate body) {
        return new ThresholdResponse(thresholdService.update(body.thresholdDays()));
    }

    public record AlertsResponse(int thresholdDays, List<CertificateDto> expiring) {}
    public record ThresholdResponse(int thresholdDays) {}
    public record ThresholdUpdate(@Min(1) int thresholdDays) {}
}
