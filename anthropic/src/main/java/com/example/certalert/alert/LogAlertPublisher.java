package com.example.certalert.alert;

import com.example.certalert.certificate.Certificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
public class LogAlertPublisher implements AlertPublisher {

    private static final Logger log = LoggerFactory.getLogger(LogAlertPublisher.class);

    @Override
    public void publish(List<Certificate> expiring, int thresholdDays) {
        if (expiring.isEmpty()) {
            log.info("cert scan: no certificates within {} days of expiry", thresholdDays);
            return;
        }
        log.warn("cert scan: {} certificate(s) within {} days of expiry", expiring.size(), thresholdDays);
        Instant now = Instant.now();
        for (Certificate c : expiring) {
            long days = Duration.between(now, c.getNotAfter()).toDays();
            log.warn("  [{}] group={} alias='{}' subject='{}' expires in {} day(s) (notAfter={})",
                    c.getId(), c.getOwnerGroup(), c.getAlias(), c.getSubject(), days, c.getNotAfter());
        }
    }
}
