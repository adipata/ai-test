package com.example.certalert.alert;

import com.example.certalert.config.AlertProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ThresholdService {

    private final ThresholdSettingRepository repository;
    private final AlertProperties properties;

    public ThresholdService(ThresholdSettingRepository repository, AlertProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    /** Returns the current threshold, lazily seeding from application properties on first access. */
    @Transactional
    public int current() {
        return repository.findById(ThresholdSetting.SINGLETON_ID)
                .orElseGet(() -> repository.save(new ThresholdSetting(properties.defaultThresholdDays())))
                .getThresholdDays();
    }

    @Transactional
    public int update(int newValue) {
        ThresholdSetting setting = repository.findById(ThresholdSetting.SINGLETON_ID)
                .orElseGet(() -> new ThresholdSetting(properties.defaultThresholdDays()));
        setting.setThresholdDays(newValue);
        return repository.save(setting).getThresholdDays();
    }
}
