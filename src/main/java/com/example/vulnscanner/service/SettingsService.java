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
    public static final String KEY_LOGIN_MAX_ATTEMPTS = "security.login.max-attempts";
    public static final String KEY_LOGIN_LOCKOUT_DURATION = "security.login.lockout-duration";
    public static final String KEY_SESSION_TIMEOUT = "security.session.timeout";
    public static final String KEY_SBOM_API_URL = "sbom.api.url";
    public static final String KEY_MAX_UPLOAD_SIZE = "file.upload.max-size";

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
        if (!systemSettingRepository.existsById(KEY_SBOM_API_URL)) {
            systemSettingRepository
                    .save(new SystemSetting(KEY_SBOM_API_URL, "http://localhost:5000", "AI_SBOM API URL"));
        }
        if (!systemSettingRepository.existsById(KEY_LOGIN_MAX_ATTEMPTS)) {
            systemSettingRepository
                    .save(new SystemSetting(KEY_LOGIN_MAX_ATTEMPTS, "5", "Max login attempts before lockout"));
        }
        if (!systemSettingRepository.existsById(KEY_LOGIN_LOCKOUT_DURATION)) {
            systemSettingRepository
                    .save(new SystemSetting(KEY_LOGIN_LOCKOUT_DURATION, "15", "Account lockout duration in minutes"));
        }
        if (!systemSettingRepository.existsById(KEY_SESSION_TIMEOUT)) {
            systemSettingRepository.save(new SystemSetting(KEY_SESSION_TIMEOUT, "30", "Session timeout in minutes"));
        }
        if (!systemSettingRepository.existsById(KEY_MAX_UPLOAD_SIZE)) {
            systemSettingRepository.save(new SystemSetting(KEY_MAX_UPLOAD_SIZE, "100", "Max file upload size in MB"));
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

    public Map<String, Object> testApiConnection(String urlStr) {
        Map<String, Object> result = new HashMap<>();
        String requestInfo = "";
        try {
            java.net.URL url = new java.net.URL(urlStr);
            requestInfo = "Request: GET " + url + " (Timeout: 3000ms)";
            result.put("requestInfo", requestInfo);

            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);

            int code = connection.getResponseCode();
            result.put("statusCode", code);
            // Accept 404 and 403 as valid "connection" proofs (server is reachable)
            result.put("success", (code >= 200 && code < 300) || code == 404 || code == 403);
            result.put("message", "Status: " + code);
        } catch (Exception e) {
            result.put("statusCode", -1);
            result.put("requestInfo", requestInfo.isEmpty() ? "Request generation failed for: " + urlStr : requestInfo);
            result.put("success", false);
            result.put("message", e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return result;
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
