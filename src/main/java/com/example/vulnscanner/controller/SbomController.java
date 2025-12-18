package com.example.vulnscanner.controller;

import com.example.vulnscanner.entity.AnalysisOption;
import com.example.vulnscanner.entity.SbomResult;
import com.example.vulnscanner.service.SbomService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import com.example.vulnscanner.repository.SbomRepository;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
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
    private final SbomRepository sbomRepository;

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
                Map<String, Object> resultMap = convertResultToMap(result);
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
                    // If root is array, assume it's the components list OR new format
                    // Check if it's new format with job_id, summary, vulnerabilities
                    boolean isNewFormat = false;
                    for (com.fasterxml.jackson.databind.JsonNode node : rootNode) {
                        if (node.has("summary") || node.has("vulnerabilities")) {
                            isNewFormat = true;
                            break;
                        }
                    }

                    if (isNewFormat) {
                        List<Map<String, Object>> componentsList = new java.util.ArrayList<>();
                        List<Map<String, Object>> vulnerabilitiesList = new java.util.ArrayList<>();
                        Map<String, Object> summaryMap = new java.util.HashMap<>();

                        for (com.fasterxml.jackson.databind.JsonNode node : rootNode) {
                            if (node.has("components")) {
                                componentsList.addAll(mapper.convertValue(node.get("components"),
                                        new TypeReference<List<Map<String, Object>>>() {
                                        }));
                            } else if (node.has("vulnerabilities")) {
                                vulnerabilitiesList.addAll(mapper.convertValue(node.get("vulnerabilities"),
                                        new TypeReference<List<Map<String, Object>>>() {
                                        }));
                            } else if (node.has("summary")) {
                                summaryMap = mapper.convertValue(node.get("summary"),
                                        new TypeReference<Map<String, Object>>() {
                                        });
                            }
                        }
                        resultMap.put("components", componentsList);
                        resultMap.put("vulnerabilities", vulnerabilitiesList);
                        resultMap.put("summary", summaryMap);
                    } else {
                        // Old format: just components array
                        List<Map<String, Object>> componentsList = mapper.convertValue(rootNode,
                                new TypeReference<List<Map<String, Object>>>() {
                                });
                        resultMap.put("components", componentsList);
                        resultMap.put("vulnerabilities", java.util.Collections.emptyList());
                        resultMap.put("licenses", java.util.Collections.emptyList());

                        // Calculate summary since it's missing in raw array
                        Map<String, Object> summary = new java.util.HashMap<>();
                        summary.put("components_count", componentsList.size());
                        summary.put("vulnerabilities_count", 0); // Cannot know from simple component list
                        summary.put("licenses_count", 0);
                        resultMap.put("summary", summary);
                    }
                } else if (rootNode.isObject()) {
                    resultMap = mapper.convertValue(rootNode, new TypeReference<Map<String, Object>>() {
                    });

                    // Ensure keys exist
                    resultMap.putIfAbsent("vulnerabilities", java.util.Collections.emptyList());
                    resultMap.putIfAbsent("licenses", java.util.Collections.emptyList());
                    resultMap.putIfAbsent("components", java.util.Collections.emptyList());
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

    private Map<String, Object> convertResultToMap(SbomResult result) {
        List<com.example.vulnscanner.entity.SbomComponent> components = result.getComponents();
        List<Map<String, Object>> componentsList = new java.util.ArrayList<>();
        List<Map<String, Object>> allVulnerabilities = new java.util.ArrayList<>();
        List<Map<String, Object>> allLicenses = new java.util.ArrayList<>();

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
                    // Map for component structure
                    Map<String, Object> vulnMap = new java.util.HashMap<>();
                    vulnMap.put("id", vuln.getVulnId());
                    vulnMap.put("vendor_id", vuln.getVendorId());
                    vulnMap.put("source_name", vuln.getSource());
                    vulnMap.put("severity", vuln.getSeverity());
                    vulnMap.put("url", vuln.getUrl());
                    vulnMap.put("title", vuln.getTitle());
                    vulnMap.put("description", vuln.getDescription());
                    vulnsList.add(vulnMap);

                    // Map for global flattened structure (template expects cve, vendor_id,
                    // package_name)
                    Map<String, Object> globalVulnMap = new java.util.HashMap<>();
                    String vulnId = vuln.getVulnId();
                    globalVulnMap.put("cve", vulnId != null ? vulnId : "N/A");
                    globalVulnMap.put("vendor_id", vuln.getVendorId() != null ? vuln.getVendorId() : "-");
                    globalVulnMap.put("ghsa_id", vuln.getVendorId() != null ? vuln.getVendorId() : "-"); // Previous key
                                                                                                         // compatibility
                    globalVulnMap.put("package_name", comp.getName());

                    // Add details for Modal
                    globalVulnMap.put("severity", vuln.getSeverity());
                    globalVulnMap.put("title", vuln.getTitle());
                    globalVulnMap.put("description", vuln.getDescription());
                    globalVulnMap.put("url", vuln.getUrl());

                    allVulnerabilities.add(globalVulnMap);

                    vulnCount++;
                }
            }
            compMap.put("vulnerabilities", vulnsList);

            List<Map<String, Object>> licensesList = new java.util.ArrayList<>();
            if (comp.getLicenses() != null) {
                for (com.example.vulnscanner.entity.SbomLicense lic : comp.getLicenses()) {
                    // Map for component structure
                    Map<String, Object> licMap = new java.util.HashMap<>();
                    licMap.put("id", lic.getLicenseId());
                    licMap.put("name", lic.getName());
                    licMap.put("location", lic.getLocation());
                    licensesList.add(licMap);

                    // Map for global flattened structure (template expects package_name,
                    // license_type)
                    Map<String, Object> globalLicMap = new java.util.HashMap<>();
                    globalLicMap.put("package_name", comp.getName());
                    globalLicMap.put("license_type", lic.getName());
                    globalLicMap.put("location", lic.getLocation());
                    allLicenses.add(globalLicMap);

                    licenseCount++;
                }
            }
            compMap.put("licenses", licensesList);

            componentsList.add(compMap);
        }

        Map<String, Object> resultMap = new java.util.HashMap<>();
        resultMap.put("components", componentsList);
        resultMap.put("vulnerabilities", allVulnerabilities);
        resultMap.put("licenses", allLicenses);

        Map<String, Object> summary = new java.util.HashMap<>();
        // Use count from Result entity if available, otherwise fallback to calculated
        // count
        summary.put("components_count",
                result.getComponentsCount() != null ? result.getComponentsCount() : componentsList.size());
        summary.put("vulnerabilities_count",
                result.getVulnerabilitiesCount() != null ? result.getVulnerabilitiesCount() : vulnCount);
        summary.put("licenses_count", result.getLicensesCount() != null ? result.getLicensesCount() : licenseCount);
        resultMap.put("summary", summary);

        if (!componentsList.isEmpty()) {
            System.out.println("DEBUG: First Component Entity: " + result.getComponents().get(0).toString());
        }

        System.out.println("DEBUG: converted components size = " + componentsList.size());
        System.out.println("DEBUG: converted global vulnerabilities size = " + allVulnerabilities.size());
        System.out.println("DEBUG: converted global licenses size = " + allLicenses.size());
        if (!allLicenses.isEmpty()) {
            System.out.println("DEBUG: Sample License MAP: " + allLicenses.get(0));
            // Find a license entity to log
            for (com.example.vulnscanner.entity.SbomComponent c : components) {
                if (c.getLicenses() != null && !c.getLicenses().isEmpty()) {
                    System.out.println("DEBUG: Sample License Entity: " + c.getLicenses().get(0).toString());
                    break;
                }
            }
        }

        return resultMap;
    }

    @PostMapping("/sboms/reparse/{id}")
    public String reparseSbom(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            java.util.Optional<SbomResult> optionalResult = sbomRepository.findById(id);
            if (optionalResult.isPresent()) {
                SbomResult result = optionalResult.get();
                // Re-fetch and parse (updates existing result)
                sbomService.fetchAndSaveResults(result);
                redirectAttributes.addFlashAttribute("message", "SBOM Re-parsing completed successfully.");
            } else {
                redirectAttributes.addFlashAttribute("error", "SBOM Result not found.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Re-parsing failed: " + e.getMessage());
        }
        return "redirect:/sbom/results/" + id;
    }
}
