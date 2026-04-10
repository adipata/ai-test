package com.certalert.repository;

import com.certalert.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long>, JpaSpecificationExecutor<Certificate> {
    List<Certificate> findByGroupIdOrderByExpiryDateAsc(Long groupId);
    List<Certificate> findByExpiryDateBeforeOrderByExpiryDateAsc(LocalDate date);
    List<Certificate> findByGroupIdAndExpiryDateBeforeOrderByExpiryDateAsc(Long groupId, LocalDate date);
}