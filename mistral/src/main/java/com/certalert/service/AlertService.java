package com.certalert.service;

import com.certalert.dto.CertificateDTO;
import com.certalert.model.AlertConfiguration;
import com.certalert.repository.AlertConfigurationRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AlertService {

    private final CertificateService certificateService;
    private final AlertConfigurationRepository alertConfigurationRepository;
    private final JavaMailSender mailSender;

    public AlertService(CertificateService certificateService, 
                       AlertConfigurationRepository alertConfigurationRepository, 
                       JavaMailSender mailSender) {
        this.certificateService = certificateService;
        this.alertConfigurationRepository = alertConfigurationRepository;
        this.mailSender = mailSender;
    }

    @Scheduled(cron = "0 0 9 * * ?") // Run every day at 9 AM
    public void checkExpiringCertificates() {
        List<CertificateDTO> expiringCertificates = certificateService.getExpiringCertificates(30);
        
        if (!expiringCertificates.isEmpty()) {
            sendAlertEmails(expiringCertificates);
        }
    }

    private void sendAlertEmails(List<CertificateDTO> certificates) {
        // Group certificates by group
        // For simplicity, we'll send one email per group
        // In a real implementation, we'd check alert configurations per group
        
        String subject = "Certificate Expiration Alert - " + certificates.size() + " certificates expiring soon";
        StringBuilder message = new StringBuilder();
        message.append("The following certificates are expiring soon:\n\n");
        
        for (CertificateDTO cert : certificates) {
            message.append("Certificate: ").append(cert.getName()).append("\n");
            message.append("Group: ").append(cert.getGroupName()).append("\n");
            message.append("Expiry Date: ").append(cert.getExpiryDate()).append("\n");
            message.append("Days until expiry: ").append(cert.getDaysUntilExpiry()).append("\n");
            message.append("\n");
        }
        
        // Send email to configured recipients
        // For now, we'll use a default recipient
        sendEmail("admin@example.com", subject, message.toString());
    }

    private void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        } catch (Exception e) {
            // Log error
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }

    public AlertConfiguration getAlertConfiguration(Long groupId) {
        return alertConfigurationRepository.findByGroupId(groupId)
                .orElse(null);
    }

    public AlertConfiguration updateAlertConfiguration(Long groupId, int thresholdDays, 
                                                       boolean emailEnabled, String emailRecipients) {
        AlertConfiguration config = alertConfigurationRepository.findByGroupId(groupId)
                .orElse(new AlertConfiguration());
        
        config.setGroup(new com.certalert.model.Group(groupId, null, null));
        config.setThresholdDays(thresholdDays);
        config.setEmailEnabled(emailEnabled);
        config.setEmailRecipients(emailRecipients);
        
        return alertConfigurationRepository.save(config);
    }
}