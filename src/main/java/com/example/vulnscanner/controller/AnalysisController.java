package com.example.vulnscanner.controller;

import com.example.vulnscanner.entity.AnalysisOption;
import com.example.vulnscanner.entity.AnalysisResult;
import com.example.vulnscanner.service.AnalysisService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class AnalysisController {

    private final AnalysisService analysisService;

    private final com.example.vulnscanner.service.FileService fileService;

    private final com.example.vulnscanner.service.SettingsService settingsService;
    private final com.example.vulnscanner.service.StatsService statsService;
    private final com.example.vulnscanner.service.UserService userService;
    private final com.example.vulnscanner.service.SbomService sbomService;

    public AnalysisController(AnalysisService analysisService,
            com.example.vulnscanner.service.FileService fileService,
            com.example.vulnscanner.service.SettingsService settingsService,
            com.example.vulnscanner.service.StatsService statsService,
            com.example.vulnscanner.service.UserService userService,
            com.example.vulnscanner.service.SbomService sbomService) {
        this.analysisService = analysisService;
        this.fileService = fileService;
        this.settingsService = settingsService;
        this.statsService = statsService;
        this.userService = userService;
        this.sbomService = sbomService;
    }

    @GetMapping("/")
    public String index(Model model) {
        List<AnalysisResult> allResults = analysisService.getAllResults();

        // 1. Summary Cards
        model.addAttribute("totalScans", allResults.size());

        long successCount = allResults.stream().filter(r -> "SUCCESS".equals(r.getStatus())).count();
        int successRate = allResults.isEmpty() ? 0 : (int) ((double) successCount / allResults.size() * 100);
        model.addAttribute("successRate", successRate);

        long recentCount = allResults.stream()
                .filter(r -> r.getScanDate().isAfter(java.time.LocalDateTime.now().minusHours(24)))
                .count();
        model.addAttribute("recentCount", recentCount);

        // Calculate Total LOC and Total Vulnerabilities
        long totalLoc = allResults.stream()
                .filter(r -> r.getScanSummary() != null && r.getScanSummary().getTotalLoc() != null)
                .mapToLong(r -> r.getScanSummary().getTotalLoc())
                .sum();
        model.addAttribute("totalLoc", totalLoc);

        long totalVulns = allResults.stream()
                .filter(r -> r.getVulnerabilities() != null)
                .mapToLong(r -> r.getVulnerabilities().size())
                .sum();
        model.addAttribute("totalVulns", totalVulns);

        // 2. Recent Scans (Top 5)
        List<AnalysisResult> recentScans = allResults.stream()
                .sorted((r1, r2) -> r2.getScanDate().compareTo(r1.getScanDate()))
                .limit(5)
                .collect(java.util.stream.Collectors.toList());
        model.addAttribute("recentScans", recentScans);

        // 3. Status Distribution (Pie Chart)
        Map<String, Long> statusDistribution = allResults.stream()
                .collect(java.util.stream.Collectors.groupingBy(AnalysisResult::getStatus,
                        java.util.stream.Collectors.counting()));
        model.addAttribute("statusLabels", statusDistribution.keySet());
        model.addAttribute("statusData", statusDistribution.values());

        // 4. Trends (Weekly & Monthly)
        java.time.LocalDate today = java.time.LocalDate.now();

        // Weekly (7 days)
        Map<String, Object> weeklyTrend = calculateTrend(allResults, today, 7);
        model.addAttribute("trendLabels", weeklyTrend.get("labels"));
        model.addAttribute("trendData", weeklyTrend.get("data"));

        // Monthly (30 days)
        Map<String, Object> monthlyTrend = calculateTrend(allResults, today, 30);
        model.addAttribute("monthlyTrendLabels", monthlyTrend.get("labels"));
        model.addAttribute("monthlyTrendLabels", monthlyTrend.get("labels"));
        model.addAttribute("monthlyTrendData", monthlyTrend.get("data"));

        // 5. Recent SBOM Scans
        model.addAttribute("recentSbomScans", sbomService.getRecentScans());

        return "dashboard";
    }

    private Map<String, Object> calculateTrend(List<AnalysisResult> results, java.time.LocalDate endDate, int days) {
        java.time.LocalDate startDate = endDate.minusDays(days - 1);

        Map<java.time.LocalDate, Long> trendMap = results.stream()
                .filter(r -> !r.getScanDate().toLocalDate().isBefore(startDate)
                        && !r.getScanDate().toLocalDate().isAfter(endDate))
                .collect(java.util.stream.Collectors.groupingBy(
                        r -> r.getScanDate().toLocalDate(),
                        java.util.stream.Collectors.counting()));

        List<String> labels = new java.util.ArrayList<>();
        List<Long> data = new java.util.ArrayList<>();

        for (int i = 0; i < days; i++) {
            java.time.LocalDate date = startDate.plusDays(i);
            labels.add(date.format(java.time.format.DateTimeFormatter.ofPattern("MM-dd")));
            data.add(trendMap.getOrDefault(date, 0L));
        }

        return Map.of("labels", labels, "data", data);
    }

    @GetMapping("/results")
    public String results(Model model) {
        List<AnalysisResult> sastResults = analysisService.getAllResults();
        List<com.example.vulnscanner.entity.SbomResult> sbomResults = sbomService.getAllSbomResults();

        List<com.example.vulnscanner.dto.UnifiedResultDto> unifiedResults = new java.util.ArrayList<>();

        // Convert SAST results
        for (AnalysisResult r : sastResults) {
            String requester = r.getRequester() != null ? r.getRequester() : "-";
            unifiedResults.add(new com.example.vulnscanner.dto.UnifiedResultDto(
                    r.getId(), "SAST", r.getAnalysisOption().getBuildId(), r.getScanDate(), r.getStatus(), requester,
                    r.getLogs(), r.getFprPath(), r.getReportPdfPath(), r.getReportXmlPath()));
        }

        // Convert SBOM results
        for (com.example.vulnscanner.entity.SbomResult r : sbomResults) {
            String requester = r.getRequester() != null ? r.getRequester() : "-";
            unifiedResults.add(new com.example.vulnscanner.dto.UnifiedResultDto(
                    r.getId(), "SBOM", r.getAnalysisOption().getBuildId(), r.getScanDate(), r.getStatus(), requester,
                    r.getLogs(), null, null, null));
        }

        // Sort by date descending
        unifiedResults.sort((r1, r2) -> r2.getScanDate().compareTo(r1.getScanDate()));

        model.addAttribute("results", unifiedResults);
        return "analysis/list";
    }

    @GetMapping("/results/{id}")
    public String resultDetail(@org.springframework.web.bind.annotation.PathVariable Long id, Model model) {
        AnalysisResult result = analysisService.getResultById(id);
        if (result == null) {
            return "redirect:/results";
        }
        model.addAttribute("result", result);
        return "analysis/detail";
    }

    @GetMapping("/analysis")
    public String analysisForm(Model model) {
        model.addAttribute("analysisOption", new AnalysisOption());
        return "analysis/request";
    }

    @PostMapping("/analysis")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> runAnalysis(@ModelAttribute AnalysisOption analysisOption,
            @org.springframework.web.bind.annotation.RequestParam(value = "file", required = false) org.springframework.web.multipart.MultipartFile file) {
        String sourceFilePath = null;
        try {
            if (file != null && !file.isEmpty()) {
                // Check File Size dynamically
                String maxSizeStr = settingsService
                        .getSetting(com.example.vulnscanner.service.SettingsService.KEY_MAX_UPLOAD_SIZE);
                long maxSizeBytes = 100 * 1024 * 1024; // Default 100MB
                if (maxSizeStr != null && !maxSizeStr.isEmpty()) {
                    try {
                        maxSizeBytes = Long.parseLong(maxSizeStr) * 1024 * 1024;
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }
                if (file.getSize() > maxSizeBytes) {
                    return org.springframework.http.ResponseEntity.badRequest()
                            .body(java.util.Map.of("error",
                                    "파일 크기가 너무 큽니다. (제한: " + (maxSizeBytes / 1024 / 1024) + "MB)"));
                }

                // Get result path from settings
                String resultPath = settingsService
                        .getSetting(com.example.vulnscanner.service.SettingsService.KEY_RESULT_PATH);
                if (resultPath == null || resultPath.isEmpty()) {
                    resultPath = "./results";
                }

                String filePath = fileService.storeFile(file, analysisOption.getBuildId(), resultPath);
                sourceFilePath = filePath; // 원본 파일 경로 저장
                if (filePath.endsWith(".zip")) {
                    String extractedPath = fileService.extractZip(filePath);
                    analysisOption.setSourcePath(extractedPath);
                } else {
                    analysisOption.setSourcePath(filePath);
                }
            }
        } catch (java.io.IOException e) {
            e.printStackTrace(); // Log to console
            return org.springframework.http.ResponseEntity.badRequest()
                    .body(java.util.Map.of("error",
                            "File upload failed: " + e.getClass().getName() + ": " + e.getMessage()));
        }

        AnalysisResult result = analysisService.createAnalysis(analysisOption);
        result.setSourceFilePath(sourceFilePath); // 소스 파일 경로 저장

        // Set Requester
        String currentUsername = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();

        String requesterName = currentUsername;
        try {
            com.example.vulnscanner.entity.User user = ((com.example.vulnscanner.service.UserService) userService)
                    .loadUserEntityByUsername(currentUsername);
            if (user != null && user.getName() != null && !user.getName().isEmpty()) {
                requesterName = user.getName();
            }
        } catch (Exception e) {
            System.out.println("Failed to fetch user details for requester name: " + e.getMessage());
        }

        System.out.println("Current User for Analysis: " + currentUsername + ", Requester Name: " + requesterName); // Debug
                                                                                                                    // Log

        if (result.getScanSummary() == null) {
            com.example.vulnscanner.entity.ScanSummary summary = new com.example.vulnscanner.entity.ScanSummary();
            summary.setAnalysisResult(result); // Set back-reference
            result.setScanSummary(summary);
        }
        result.setRequester(requesterName);
        result.getScanSummary().setRequester(requesterName);
        result.getScanSummary().setAnalysisResult(result); // Ensure it's set

        analysisService.saveResult(result);
        analysisService.runAnalysis(analysisOption, result.getId());
        return org.springframework.http.ResponseEntity.ok(java.util.Map.of("analysisId", result.getId()));
    }

    @GetMapping("/api/analysis/{id}/logs")
    @ResponseBody
    public String getAnalysisLogs(@org.springframework.web.bind.annotation.PathVariable Long id) {
        return analysisService.getLiveLogs(id);
    }

    @GetMapping("/api/analysis/{id}/download-log")
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> downloadLog(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "all") String type) {
        try {
            AnalysisResult result = analysisService.getAllResults().stream()
                    .filter(r -> r.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Analysis not found"));

            String buildId = result.getAnalysisOption().getBuildId();

            if ("build".equals(type)) {
                return downloadSingleFile(result.getBuildLogFilePath(), buildId + "_build.log");
            } else if ("scan".equals(type)) {
                return downloadSingleFile(result.getScanLogFilePath(), buildId + "_scan.log");
            } else {
                // "all" - Zip both
                return downloadZipLogs(result);
            }
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.internalServerError().build();
        }
    }

    private org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> downloadSingleFile(
            String pathStr, String filename) throws java.net.MalformedURLException {
        if (pathStr == null)
            return org.springframework.http.ResponseEntity.notFound().build();
        java.nio.file.Path path = java.nio.file.Paths.get(pathStr);
        org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(path.toUri());
        if (resource.exists() || resource.isReadable()) {
            // 파일 확장자에 따른 Content-Type 설정
            String contentType = "application/octet-stream"; // 기본값
            if (filename.endsWith(".fpr")) {
                contentType = "application/octet-stream";
            } else if (filename.endsWith(".zip")) {
                contentType = "application/zip";
            } else if (filename.endsWith(".log")) {
                contentType = "text/plain";
            }

            return org.springframework.http.ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"")
                    .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, contentType)
                    .body(resource);
        }
        return org.springframework.http.ResponseEntity.notFound().build();
    }

    private org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> downloadZipLogs(
            AnalysisResult result) throws java.io.IOException {
        String buildId = result.getAnalysisOption().getBuildId();
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(baos)) {
            addToZip(zos, result.getBuildLogFilePath());
            addToZip(zos, result.getScanLogFilePath());
        }

        org.springframework.core.io.ByteArrayResource resource = new org.springframework.core.io.ByteArrayResource(
                baos.toByteArray());
        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + buildId + "_logs.zip\"")
                .body(resource);
    }

    private void addToZip(java.util.zip.ZipOutputStream zos, String filePath) throws java.io.IOException {
        if (filePath == null)
            return;
        java.io.File file = new java.io.File(filePath);
        if (file.exists()) {
            try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
                java.util.zip.ZipEntry zipEntry = new java.util.zip.ZipEntry(file.getName());
                zos.putNextEntry(zipEntry);
                byte[] bytes = new byte[1024];
                int length;
            }
        }
    }

    // 단일 삭제
    @org.springframework.web.bind.annotation.DeleteMapping("/api/analysis/{id}")
    public org.springframework.http.ResponseEntity<String> deleteAnalysis(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "SAST") String type) {
        try {
            if ("SBOM".equalsIgnoreCase(type)) {
                sbomService.deleteSbomResult(id);
            } else {
                analysisService.deleteResult(id);
            }
            return org.springframework.http.ResponseEntity.ok("Deleted");
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.internalServerError().body("Error deleting analysis");
        }
    }

    public static class BatchDeleteRequest {
        public Long id;
        public String type;
    }

    // 그룹 삭제
    @org.springframework.web.bind.annotation.DeleteMapping("/api/analysis/batch")
    @ResponseBody
    public org.springframework.http.ResponseEntity<String> deleteBatch(
            @org.springframework.web.bind.annotation.RequestBody java.util.List<BatchDeleteRequest> items) {
        try {
            for (BatchDeleteRequest item : items) {
                if ("SBOM".equalsIgnoreCase(item.type)) {
                    sbomService.deleteSbomResult(item.id);
                } else {
                    analysisService.deleteResult(item.id);
                }
            }
            return org.springframework.http.ResponseEntity.ok("Deleted " + items.size() + " items");
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.internalServerError().body("Error deleting analyses");
        }
    }

    // FPR 다운로드
    @GetMapping("/api/analysis/{id}/download-fpr")
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> downloadFpr(
            @org.springframework.web.bind.annotation.PathVariable Long id) {
        try {
            AnalysisResult result = analysisService.getAllResults().stream()
                    .filter(r -> r.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Analysis not found"));

            String fprPath = result.getFprPath();
            if (fprPath == null) {
                return org.springframework.http.ResponseEntity.notFound().build();
            }

            String buildId = result.getAnalysisOption().getBuildId();
            return downloadSingleFile(fprPath, buildId + ".fpr");
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.internalServerError().build();
        }
    }

    // 소스 파일 다운로드
    @GetMapping("/api/analysis/{id}/download-source")
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> downloadSource(
            @org.springframework.web.bind.annotation.PathVariable Long id) {
        try {
            AnalysisResult result = analysisService.getAllResults().stream()
                    .filter(r -> r.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Analysis not found"));

            String sourcePath = result.getSourceFilePath();
            if (sourcePath == null) {
                return org.springframework.http.ResponseEntity.notFound().build();
            }

            java.io.File sourceFile = new java.io.File(sourcePath);
            String buildId = result.getAnalysisOption().getBuildId();

            // ZIP 파일이면 그대로 다운로드
            if (sourcePath.endsWith(".zip")) {
                return downloadSingleFile(sourcePath, buildId + "_source.zip");
            } else {
                // 단일 파일이면 파일명 유지
                return downloadSingleFile(sourcePath, sourceFile.getName());
            }
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.internalServerError().build();
        }
    }

    // 보고서 다운로드
    @GetMapping("/api/analysis/{id}/download-report")
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> downloadReport(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "pdf") String type) {
        try {
            AnalysisResult result = analysisService.getAllResults().stream()
                    .filter(r -> r.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Analysis not found"));

            String reportPath = null;
            if ("xml".equalsIgnoreCase(type)) {
                reportPath = result.getReportXmlPath();
            } else {
                reportPath = result.getReportPdfPath();
            }

            if (reportPath == null) {
                return org.springframework.http.ResponseEntity.notFound().build();
            }

            java.io.File reportFile = new java.io.File(reportPath);
            return downloadSingleFile(reportPath, reportFile.getName());
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/stats")
    public String stats(Model model) {
        // Overall Stats
        model.addAttribute("overallStats", statsService.getOverallStats());

        // Severity Distribution
        Map<String, Long> severityDist = statsService.getSeverityDistribution();
        model.addAttribute("severityLabels", severityDist.keySet());
        model.addAttribute("severityData", severityDist.values());

        // Trend (Last 30 days)
        Map<String, Object> trend = statsService.getTrendData(30);
        model.addAttribute("trendLabels", trend.get("labels"));
        model.addAttribute("trendData", trend.get("data"));

        // Top Categories (Top 10)
        Map<String, Long> topCategories = statsService.getTopCategories(10);
        model.addAttribute("categoryLabels", topCategories.keySet());
        model.addAttribute("categoryData", topCategories.values());

        // SBOM Stats
        model.addAttribute("sbomOverallStats", statsService.getSbomOverallStats());

        Map<String, Long> sbomComponents = statsService.getSbomComponentDistribution(10);
        model.addAttribute("sbomComponentLabels", sbomComponents.keySet());
        model.addAttribute("sbomComponentData", sbomComponents.values());

        Map<String, Long> sbomLicenses = statsService.getSbomLicenseDistribution();
        model.addAttribute("sbomLicenseLabels", sbomLicenses.keySet());
        model.addAttribute("sbomLicenseData", sbomLicenses.values());

        return "stats";
    }

    // Reparse XML
    @PostMapping("/api/analysis/{id}/reparse")
    @ResponseBody
    public org.springframework.http.ResponseEntity<String> reparseAnalysis(
            @org.springframework.web.bind.annotation.PathVariable Long id) {
        try {
            analysisService.reparseResult(id);
            return org.springframework.http.ResponseEntity.ok("Reparsed successfully");
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.internalServerError()
                    .body("Error reparsing analysis: " + e.getMessage());
        }
    }

    @GetMapping("/api/analysis/status")
    @ResponseBody
    public List<Map<String, Object>> getAnalysisStatuses() {
        List<Map<String, Object>> statuses = new java.util.ArrayList<>();

        // SAST Results
        analysisService.getAllResults().forEach(r -> {
            statuses.add(Map.of("id", r.getId(), "type", "SAST", "status", r.getStatus()));
        });

        // SBOM Results
        sbomService.getAllSbomResults().forEach(r -> {
            // Trigger status update if running
            if ("RUNNING".equals(r.getStatus())) {
                sbomService.updateSbomStatus(r.getId());
                // Refresh entity after update
                com.example.vulnscanner.entity.SbomResult updated = sbomService.getSbomResult(r.getId());
                if (updated != null) {
                    statuses.add(Map.of("id", updated.getId(), "type", "SBOM", "status", updated.getStatus()));
                }
            } else {
                statuses.add(Map.of("id", r.getId(), "type", "SBOM", "status", r.getStatus()));
            }
        });

        return statuses;
    }

}
