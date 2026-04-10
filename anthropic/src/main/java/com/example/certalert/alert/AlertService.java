package com.example.certalert.alert;

import com.example.certalert.certificate.Certificate;
import com.example.certalert.certificate.CertificateRepository;
import com.example.certalert.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class AlertService {

    private final CertificateRepository certificates;
    private final ThresholdService thresholdService;
    private final List<AlertPublisher> publishers;

    public AlertService(CertificateRepository certificates,
                        ThresholdService thresholdService,
                        List<AlertPublisher> publishers) {
        this.certificates = certificates;
        this.thresholdService = thresholdService;
        this.publishers = publishers;
    }

    /** Expiring certificates visible to the authenticated caller (group-scoped). */
    @Transactional(readOnly = true)
    public List<Certificate> expiringForCurrentUser() {
        String group = SecurityUtils.currentUser().group();
        int days = thresholdService.current();
        Instant cutoff = Instant.now().plus(days, ChronoUnit.DAYS);
        return certificates.findAllByOwnerGroupAndNotAfterBeforeOrderByNotAfterAsc(group, cutoff);
    }

    /** Runs across all groups and dispatches to every {@link AlertPublisher} bean. */
    @Transactional(readOnly = true)
    public List<Certificate> scanAndPublish() {
        int days = thresholdService.current();
        Instant cutoff = Instant.now().plus(days, ChronoUnit.DAYS);
        List<Certificate> expiring = certificates.findAllByNotAfterBeforeOrderByNotAfterAsc(cutoff);
        for (AlertPublisher publisher : publishers) {
            try {
                publisher.publish(expiring, days);
            } catch (Exception e) {
                // A bad publisher should not kill the scan — log and continue.
                org.slf4j.LoggerFactory.getLogger(AlertService.class)
                        .error("alert publisher {} failed: {}", publisher.getClass().getSimpleName(), e.getMessage(), e);
            }
        }
        return expiring;
    }
}
