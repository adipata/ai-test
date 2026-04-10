package com.example.certalert.alert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AlertScheduler {

    private static final Logger log = LoggerFactory.getLogger(AlertScheduler.class);

    private final AlertService alertService;

    public AlertScheduler(AlertService alertService) {
        this.alertService = alertService;
    }

    @Scheduled(cron = "${cert-alert.alert.scan-cron}")
    public void scheduledScan() {
        log.info("running scheduled certificate expiry scan");
        alertService.scanAndPublish();
    }
}
