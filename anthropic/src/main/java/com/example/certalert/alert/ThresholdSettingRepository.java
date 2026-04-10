package com.example.certalert.alert;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ThresholdSettingRepository extends JpaRepository<ThresholdSetting, Long> {
}
