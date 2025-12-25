package com.example.vulnscanner.module.analysis;

import com.example.vulnscanner.module.sbom.SbomRepository;
import com.example.vulnscanner.module.sbom.SbomResult;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final com.example.vulnscanner.module.analysis.AnalysisRepository analysisRepository;
    private final com.example.vulnscanner.module.sbom.SbomRepository sbomRepository;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    public Map<String, Object> getOverallStats() {
        List<AnalysisResult> results = analysisRepository.findAll();
        long totalScans = results.size();
        long successCount = results.stream().filter(r -> "SUCCESS".equals(r.getStatus())).count();
        long totalVulns = results.stream()
                .filter(r -> r.getVulnerabilities() != null)
                .mapToLong(r -> r.getVulnerabilities().size())
                .sum();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalScans", totalScans);
        stats.put("successCount", successCount);
        stats.put("totalVulns", totalVulns);

        // Calculate average vulnerabilities per scan
        double avgVulns = successCount > 0 ? (double) totalVulns / successCount : 0;
        stats.put("avgVulns", String.format("%.1f", avgVulns));

        return stats;
    }

    public Map<String, Object> getSbomOverallStats() {
        List<com.example.vulnscanner.module.sbom.SbomResult> results = sbomRepository.findAll();
        long totalScans = results.size();
        long successCount = results.stream().filter(r -> "SUCCESS".equals(r.getStatus())).count();

        long totalComponents = 0;
        long totalVulns = 0;

        for (com.example.vulnscanner.module.sbom.SbomResult result : results) {
            if ("SUCCESS".equals(result.getStatus()) && result.getLogs() != null) {
                try {
                    Map<String, Object> data = objectMapper.readValue(result.getLogs(),
                            new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
                            });
                    if (data.containsKey("summary")) {
                        Map<String, Object> summary = (Map<String, Object>) data.get("summary");
                        totalComponents += ((Number) summary.getOrDefault("components_count", 0)).longValue();
                        totalVulns += ((Number) summary.getOrDefault("vulnerabilities_count", 0)).longValue();
                    }
                } catch (Exception e) {
                    // Ignore parsing errors for stats
                }
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalScans", totalScans);
        stats.put("successCount", successCount);
        stats.put("totalComponents", totalComponents);
        stats.put("totalVulns", totalVulns);

        return stats;
    }

    public Map<String, Long> getSeverityDistribution() {
        List<AnalysisResult> results = analysisRepository.findAll();
        // Use LinkedHashMap to preserve order: Critical -> High -> Medium -> Low
        Map<String, Long> distribution = new LinkedHashMap<>();
        distribution.put("Critical", 0L);
        distribution.put("High", 0L);
        distribution.put("Medium", 0L);
        distribution.put("Low", 0L);

        for (AnalysisResult result : results) {
            if (result.getVulnerabilities() != null) {
                for (Vulnerability v : result.getVulnerabilities()) {
                    String priority = v.getPriority();
                    if (priority != null) {
                        // Normalize priority string if needed (e.g. "Critical" vs "critical")
                        String key = capitalize(priority);
                        if (distribution.containsKey(key)) {
                            distribution.put(key, distribution.get(key) + 1);
                        }
                    }
                }
            }
        }
        return distribution;
    }

    public Map<String, Object> getTrendData(int days) {
        List<AnalysisResult> results = analysisRepository.findAll();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        Map<LocalDate, Long> trendMap = new TreeMap<>();
        // Initialize map with 0
        for (int i = 0; i < days; i++) {
            trendMap.put(startDate.plusDays(i), 0L);
        }

        for (AnalysisResult result : results) {
            if (result.getScanDate() != null) {
                LocalDate scanDate = result.getScanDate().toLocalDate();
                if (!scanDate.isBefore(startDate) && !scanDate.isAfter(endDate)) {
                    long vulnCount = result.getVulnerabilities() != null ? result.getVulnerabilities().size() : 0;
                    trendMap.put(scanDate, trendMap.get(scanDate) + vulnCount);
                }
            }
        }

        List<String> labels = new ArrayList<>();
        List<Long> data = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        for (Map.Entry<LocalDate, Long> entry : trendMap.entrySet()) {
            labels.add(entry.getKey().format(formatter));
            data.add(entry.getValue());
        }

        Map<String, Object> chartData = new HashMap<>();
        chartData.put("labels", labels);
        chartData.put("data", data);
        return chartData;
    }

    public Map<String, Long> getTopCategories(int limit) {
        List<AnalysisResult> results = analysisRepository.findAll();
        Map<String, Long> categoryCount = new HashMap<>();

        for (AnalysisResult result : results) {
            if (result.getVulnerabilities() != null) {
                for (Vulnerability v : result.getVulnerabilities()) {
                    String category = v.getCategory();
                    if (category != null) {
                        categoryCount.put(category, categoryCount.getOrDefault(category, 0L) + 1);
                    }
                }
            }
        }

        return categoryCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));
    }

    public Map<String, Long> getSbomComponentDistribution(int limit) {
        List<com.example.vulnscanner.module.sbom.SbomResult> results = sbomRepository.findAll();
        Map<String, Long> componentCount = new HashMap<>();

        for (com.example.vulnscanner.module.sbom.SbomResult result : results) {
            if ("SUCCESS".equals(result.getStatus()) && result.getLogs() != null) {
                try {
                    Map<String, Object> data = objectMapper.readValue(result.getLogs(),
                            new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
                            });
                    if (data.containsKey("components")) {
                        List<Map<String, Object>> components = (List<Map<String, Object>>) data.get("components");
                        for (Map<String, Object> comp : components) {
                            String name = (String) comp.get("name");
                            if (name != null) {
                                componentCount.put(name, componentCount.getOrDefault(name, 0L) + 1);
                            }
                        }
                    }
                } catch (Exception e) {
                    // Ignore
                }
            }
        }

        return componentCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));
    }

    public Map<String, Long> getSbomLicenseDistribution() {
        List<com.example.vulnscanner.module.sbom.SbomResult> results = sbomRepository.findAll();
        Map<String, Long> licenseCount = new HashMap<>();

        for (com.example.vulnscanner.module.sbom.SbomResult result : results) {
            if ("SUCCESS".equals(result.getStatus()) && result.getLogs() != null) {
                try {
                    Map<String, Object> data = objectMapper.readValue(result.getLogs(),
                            new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
                            });
                    if (data.containsKey("licenses")) {
                        List<Map<String, Object>> licenses = (List<Map<String, Object>>) data.get("licenses");
                        for (Map<String, Object> lic : licenses) {
                            String type = (String) lic.get("license_type");
                            if (type != null) {
                                licenseCount.put(type, licenseCount.getOrDefault(type, 0L) + 1);
                            }
                        }
                    }
                } catch (Exception e) {
                    // Ignore
                }
            }
        }

        // Sort by count desc
        return licenseCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10) // Top 10 licenses
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty())
            return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}