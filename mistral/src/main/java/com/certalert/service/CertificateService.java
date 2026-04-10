package com.certalert.service;

import com.certalert.dto.CertificateDTO;
import com.certalert.model.Certificate;
import com.certalert.model.Group;
import com.certalert.model.User;
import com.certalert.repository.CertificateRepository;
import com.certalert.repository.GroupRepository;
import com.certalert.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    public CertificateService(CertificateRepository certificateRepository, 
                           UserRepository userRepository, 
                           GroupRepository groupRepository) {
        this.certificateRepository = certificateRepository;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
    }

    public CertificateDTO uploadCertificateFromFile(MultipartFile file, String username, Long groupId) 
            throws IOException, CertificateException {
        
        // Parse certificate from file
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) certFactory.generateCertificate(file.getInputStream());
        
        return saveCertificate(cert, username, groupId, Certificate.SourceType.FILE_UPLOAD, null);
    }

    public CertificateDTO fetchCertificateFromUrl(String url, String username, Long groupId) 
            throws CertificateException, IOException {
        // In a real implementation, this would fetch the certificate from the URL
        // For now, we'll simulate it
        // TODO: Implement actual URL fetching logic
        
        // This is a placeholder - actual implementation would use HttpClient or similar
        throw new UnsupportedOperationException("URL fetching not yet implemented");
    }

    private CertificateDTO saveCertificate(X509Certificate cert, String username, Long groupId, 
                                         Certificate.SourceType sourceType, String sourceUrl) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found: " + groupId));
        
        Certificate certificate = new Certificate();
        certificate.setName(cert.getSubjectX500Principal().getName());
        certificate.setContent(cert.toString());
        certificate.setExpiryDate(cert.getNotAfter().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        certificate.setIssuer(cert.getIssuerX500Principal().getName());
        certificate.setSubject(cert.getSubjectX500Principal().getName());
        certificate.setSerialNumber(cert.getSerialNumber().toString());
        certificate.setCreatedBy(user);
        certificate.setGroup(group);
        certificate.setSourceType(sourceType);
        certificate.setSourceUrl(sourceUrl);
        
        Certificate savedCertificate = certificateRepository.save(certificate);
        return convertToDTO(savedCertificate);
    }

    public List<CertificateDTO> getCertificatesByGroup(Long groupId) {
        List<Certificate> certificates = certificateRepository.findByGroupIdOrderByExpiryDateAsc(groupId);
        return certificates.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<CertificateDTO> getExpiringCertificates(int thresholdDays) {
        LocalDate thresholdDate = LocalDate.now().plusDays(thresholdDays);
        List<Certificate> certificates = certificateRepository.findByExpiryDateBeforeOrderByExpiryDateAsc(thresholdDate);
        return certificates.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private CertificateDTO convertToDTO(Certificate certificate) {
        CertificateDTO dto = new CertificateDTO();
        dto.setId(certificate.getId());
        dto.setName(certificate.getName());
        dto.setExpiryDate(certificate.getExpiryDate());
        dto.setIssuer(certificate.getIssuer());
        dto.setSubject(certificate.getSubject());
        dto.setSerialNumber(certificate.getSerialNumber());
        dto.setCreatedBy(certificate.getCreatedBy().getUsername());
        dto.setGroupName(certificate.getGroup().getName());
        dto.setSourceType(certificate.getSourceType().name());
        dto.setSourceUrl(certificate.getSourceUrl());
        
        // Calculate days until expiry
        int daysUntilExpiry = (int) java.time.temporal.ChronoUnit.DAYS.between(
            LocalDate.now(), certificate.getExpiryDate());
        dto.setDaysUntilExpiry(daysUntilExpiry);
        dto.setExpiringSoon(daysUntilExpiry <= 30); // Default threshold
        
        return dto;
    }
}