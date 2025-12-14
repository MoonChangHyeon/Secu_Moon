package com.example.vulnscanner.controller;

import com.example.vulnscanner.entity.AnalysisOption;
import com.example.vulnscanner.entity.SbomResult;
import com.example.vulnscanner.service.SbomService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class SbomController {

    private final SbomService sbomService;
    private final com.example.vulnscanner.service.SettingsService settingsService;

    @PostMapping("/analysis/request/sbom")
    @ResponseBody
    public ResponseEntity<?> requestSbomAnalysis(
            @RequestParam("buildId") String buildId,
            @RequestParam(value = "sbomFile", required = false) MultipartFile sbomFile) {

        if (sbomFile != null && !sbomFile.isEmpty()) {
            String maxSizeStr = settingsService
                    .getSetting(com.example.vulnscanner.service.SettingsService.KEY_MAX_UPLOAD_SIZE);
            long maxSizeBytes = 100 * 1024 * 1024; // Default 100MB
            if (maxSizeStr != null && !maxSizeStr.isEmpty()) {
                try {
                    maxSizeBytes = Long.parseLong(maxSizeStr) * 1024 * 1024;
                } catch (NumberFormatException e) {
                    // ignore, use default
                }
            }

            if (sbomFile.getSize() > maxSizeBytes) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "파일 크기가 너무 큽니다. (제한: " + (maxSizeBytes / 1024 / 1024) + "MB)"));
            }
        }

        try {
            AnalysisOption option = new AnalysisOption();
            option.setBuildId(buildId);

            // Get Current User
            String requester = org.springframework.security.core.context.SecurityContextHolder.getContext()
                    .getAuthentication().getName();

            SbomResult result = sbomService.createSbomAnalysis(option, sbomFile, requester);
            return ResponseEntity.ok(Map.of("analysisId", result.getId()));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to process SBOM file: " + e.getMessage()));
        }
    }

    @GetMapping("/api/sbom/status/{id}")
    @ResponseBody
    public ResponseEntity<?> checkStatus(
            @PathVariable Long id) {
        sbomService.updateSbomStatus(id);
        SbomResult result = sbomService.getSbomResult(id);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of(
                "status", result.getStatus(),
                "logs", result.getLogs() != null ? result.getLogs() : ""));
    }

    @GetMapping("/sbom/results/{id}")
    public String getSbomDetail(@PathVariable Long id,
            Model model) {
        SbomResult result = sbomService.getSbomResult(id);
        if (result == null) {
            return "redirect:/";
        }

        try {
            // 1. Try to read from DB Entities first (Enriched Data)
            if (result.getComponents() != null && !result.getComponents().isEmpty()) {
                Map<String, Object> resultMap = convertEntitiesToMap(result.getComponents());
                model.addAttribute("sbomData", resultMap);
                model.addAttribute("result", result);
                return "sbom_result_detail";
            }

            String jsonContent = null;

            // 2. Fallback to file if DB entities are empty (Legacy or failed parsing)
            if (result.getResultJsonPath() != null && !result.getResultJsonPath().isEmpty()) {
                java.nio.file.Path jsonPath = java.nio.file.Paths.get(result.getResultJsonPath());
                if (java.nio.file.Files.exists(jsonPath)) {
                    jsonContent = java.nio.file.Files.readString(jsonPath);
                }
            }

            // 3. Fallback to logs column
            if ((jsonContent == null || jsonContent.isEmpty()) && result.getLogs() != null) {
                String logs = result.getLogs().trim();
                if (logs.startsWith("{") && logs.endsWith("}")) {
                    jsonContent = logs;
                }
            }

            // 3. Parse JSON
            if (jsonContent != null && !jsonContent.isEmpty()) {
                ObjectMapper mapper = new ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode rootNode = mapper.readTree(jsonContent);
                Map<String, Object> resultMap = new java.util.HashMap<>();

                if (rootNode.isArray()) {
                    // If root is array, assume it's the components list
                    List<Map<String, Object>> componentsList = mapper.convertValue(rootNode,
                            new TypeReference<List<Map<String, Object>>>() {
                            });
                    resultMap.put("components", componentsList);
                    // Calculate summary since it's missing in raw array
                    Map<String, Object> summary = new java.util.HashMap<>();
                    summary.put("components_count", componentsList.size());
                    summary.put("vulnerabilities_count", 0); // Cannot know from simple component list
                    summary.put("licenses_count", 0);
                    resultMap.put("summary", summary);
                } else if (rootNode.isObject()) {
                    resultMap = mapper.convertValue(rootNode, new TypeReference<Map<String, Object>>() {
                    });
                }

                model.addAttribute("sbomData", resultMap);
            }
        } catch (Exception e) {
            // Logs might be plain text error message, just ignore parsing
            System.out.println("Logs are not valid JSON or File Read Error: " + e.getMessage());
        }

        model.addAttribute("result", result);
        return "sbom_result_detail";
    }

    private Map<String, Object> convertEntitiesToMap(List<com.example.vulnscanner.entity.SbomComponent> components) {
        List<Map<String, Object>> componentsList = new java.util.ArrayList<>();
        int vulnCount = 0;
        int licenseCount = 0;

        for (com.example.vulnscanner.entity.SbomComponent comp : components) {
            Map<String, Object> compMap = new java.util.HashMap<>();
            compMap.put("name", comp.getName());
            compMap.put("version", comp.getVersion());
            compMap.put("type", comp.getType());
            compMap.put("purl", comp.getPurl());

            List<Map<String, Object>> vulnsList = new java.util.ArrayList<>();
            if (comp.getVulnerabilities() != null) {
                for (com.example.vulnscanner.entity.SbomVulnerability vuln : comp.getVulnerabilities()) {
                    Map<String, Object> vulnMap = new java.util.HashMap<>();
                    vulnMap.put("id", vuln.getVulnId());
                    vulnMap.put("source_name", vuln.getSource());
                    vulnMap.put("severity", vuln.getSeverity());
                    vulnMap.put("url", vuln.getUrl());
                    vulnMap.put("title", vuln.getTitle());
                    vulnMap.put("description", vuln.getDescription());
                    vulnsList.add(vulnMap);
                    vulnCount++;
                }
            }
            compMap.put("vulnerabilities", vulnsList);

            List<Map<String, Object>> licensesList = new java.util.ArrayList<>();
            if (comp.getLicenses() != null) {
                for (com.example.vulnscanner.entity.SbomLicense lic : comp.getLicenses()) {
                    Map<String, Object> licMap = new java.util.HashMap<>();
                    licMap.put("id", lic.getLicenseId());
                    licMap.put("name", lic.getName());
                    licMap.put("severity", lic.getSeverity());
                    licMap.put("url", lic.getUrl());
                    licensesList.add(licMap);
                    licenseCount++;
                }
            }
            compMap.put("licenses", licensesList);

            componentsList.add(compMap);
        }

        Map<String, Object> resultMap = new java.util.HashMap<>();
        resultMap.put("components", componentsList);

        Map<String, Object> summary = new java.util.HashMap<>();
        summary.put("components_count", componentsList.size());
        summary.put("vulnerabilities_count", vulnCount);
        summary.put("licenses_count", licenseCount);
        resultMap.put("summary", summary);

        return resultMap;
    }
}
