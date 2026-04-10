package com.certalert.repository;

import com.certalert.model.AlertConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AlertConfigurationRepository extends JpaRepository<AlertConfiguration, Long> {
    Optional<AlertConfiguration> findByGroupId(Long groupId);
}