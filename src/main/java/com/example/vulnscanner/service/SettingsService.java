package com.example.vulnscanner.service;

import com.example.vulnscanner.entity.SystemSetting;
import com.example.vulnscanner.repository.SystemSettingRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final SystemSettingRepository systemSettingRepository;

    @Value("${fortify.executable-path}")
    private String defaultFortifyPath;

    public static final String KEY_FORTIFY_PATH = "fortify.executable-path";
    public static final String KEY_DEFAULT_MEMORY = "scan.default.memory";
    public static final String KEY_DEFAULT_JDK = "scan.default.jdk";
    public static final String KEY_RESULT_PATH = "result.path";

    @PostConstruct
    public void init() {
        // Initialize default settings if not present
        if (!systemSettingRepository.existsById(KEY_FORTIFY_PATH)) {
            systemSettingRepository.save(new SystemSetting(KEY_FORTIFY_PATH, defaultFortifyPath,
                    "Path to Fortify sourceanalyzer executable"));
        }
        if (!systemSettingRepository.existsById(KEY_DEFAULT_MEMORY)) {
            systemSettingRepository
                    .save(new SystemSetting(KEY_DEFAULT_MEMORY, "4G", "Default memory for scan (e.g., 4G, 8G)"));
        }
        if (!systemSettingRepository.existsById(KEY_DEFAULT_JDK)) {
            systemSettingRepository
                    .save(new SystemSetting(KEY_DEFAULT_JDK, "17", "Default JDK version for translation"));
        }
        if (!systemSettingRepository.existsById(KEY_RESULT_PATH)) {
            systemSettingRepository
                    .save(new SystemSetting(KEY_RESULT_PATH, "./results", "Directory path for analysis results"));
        }
    }

    public String getSetting(String key) {
        return systemSettingRepository.findById(key)
                .map(SystemSetting::getValue)
                .orElse(null);
    }

    public void updateSetting(String key, String value) {
        Optional<SystemSetting> settingOpt = systemSettingRepository.findById(key);
        if (settingOpt.isPresent()) {
            SystemSetting setting = settingOpt.get();
            setting.setValue(value);
            systemSettingRepository.save(setting);
        } else {
            systemSettingRepository.save(new SystemSetting(key, value, ""));
        }
    }

    public Map<String, String> getAllSettings() {
        List<SystemSetting> settings = systemSettingRepository.findAll();
        Map<String, String> map = new HashMap<>();
        for (SystemSetting setting : settings) {
            map.put(setting.getKeyName(), setting.getValue());
        }
        return map;
    }
}
