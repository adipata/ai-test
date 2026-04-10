package com.certalert;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CertificateAlertServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CertificateAlertServiceApplication.class, args);
    }
}