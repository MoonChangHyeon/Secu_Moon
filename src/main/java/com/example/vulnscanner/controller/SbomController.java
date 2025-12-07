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
            SbomResult result = sbomService.createSbomAnalysis(option, sbomFile);
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
            if (result.getLogs() != null && !result.getLogs().isEmpty()) {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> resultMap = mapper.readValue(result.getLogs(),
                        new TypeReference<Map<String, Object>>() {
                        });
                model.addAttribute("sbomData", resultMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        model.addAttribute("result", result);
        return "sbom_result_detail";
    }
}
