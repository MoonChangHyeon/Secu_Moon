package com.example.vulnscanner.service;

import com.example.vulnscanner.entity.AnalysisOption;
import com.example.vulnscanner.entity.AnalysisResult;
import com.example.vulnscanner.repository.AnalysisRepository;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;

@Service
@Slf4j
public class AnalysisService {

    private final AnalysisRepository analysisRepository;
    private final SettingsService settingsService;
    private final ReportService reportService;
    private final ReportParserService reportParserService;
    private final Map<Long, StringBuilder> activeLogs = new ConcurrentHashMap<>();

    public AnalysisService(AnalysisRepository analysisRepository, SettingsService settingsService,
            ReportService reportService, ReportParserService reportParserService) {
        this.analysisRepository = analysisRepository;
        this.settingsService = settingsService;
        this.reportService = reportService;
        this.reportParserService = reportParserService;
    }

    public List<AnalysisResult> getAllResults() {
        return analysisRepository.findAll();
    }

    public AnalysisResult getResultById(Long id) {
        return analysisRepository.findById(id).orElse(null);
    }

    public AnalysisResult saveResult(AnalysisResult result) {
        return analysisRepository.save(result);
    }

    public void deleteResult(Long id) {
        analysisRepository.findById(id).ifPresent(result -> {
            // Try to find the root directory of the analysis
            File analysisDir = null;

            // Strategy 1: Use FPR path parent (usually .../buildId)
            if (result.getFprPath() != null) {
                File fprFile = new File(result.getFprPath());
                if (fprFile.getParentFile() != null) {
                    analysisDir = fprFile.getParentFile();
                }
            }

            // Strategy 2: Use Log path parent's parent (log/.. -> buildId)
            if (analysisDir == null && result.getBuildLogFilePath() != null) {
                File logFile = new File(result.getBuildLogFilePath());
                if (logFile.getParentFile() != null && logFile.getParentFile().getParentFile() != null) {
                    analysisDir = logFile.getParentFile().getParentFile();
                }
            }

            if (analysisDir != null && analysisDir.exists()) {
                log.info("Deleting analysis directory: {}", analysisDir.getAbsolutePath());
                deleteDirectory(analysisDir);
            } else {
                log.warn("Could not determine analysis directory for deletion. ID: {}", id);
                // Fallback: Try to delete individual files if directory not found
                deleteFile(result.getBuildLogFilePath());
                deleteFile(result.getScanLogFilePath());
                deleteFile(result.getFprPath());
                deleteFile(result.getSourceFilePath());
                deleteFile(result.getReportPdfPath());
                deleteFile(result.getReportXmlPath());
            }

            analysisRepository.delete(result);
        });
    }

    private void deleteDirectory(File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        boolean deleted = file.delete();
                        if (deleted) {
                            log.info("Deleted file: {}", file.getAbsolutePath());
                        } else {
                            log.error("Failed to delete file: {}", file.getAbsolutePath());
                        }
                    }
                }
            }
            boolean deleted = dir.delete();
            if (deleted) {
                log.info("Deleted directory: {}", dir.getAbsolutePath());
            } else {
                log.error("Failed to delete directory: {}", dir.getAbsolutePath());
            }
        }
    }

    private void deleteFile(String path) {
        if (path != null) {
            File file = new File(path);
            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    log.info("Deleted file: {}", path);
                } else {
                    log.error("Failed to delete file: {}", path);
                }
            } else {
                log.warn("File not found for deletion: {}", path);
            }
        }
    }

    public String getLiveLogs(Long analysisId) {
        StringBuilder logs = activeLogs.get(analysisId);
        if (logs != null) {
            return logs.toString();
        }
        // Fallback to DB
        return analysisRepository.findById(analysisId)
                .map(AnalysisResult::getLogs)
                .orElse("");
    }

    @Async
    public CompletableFuture<Void> runAnalysis(AnalysisOption option, Long analysisId) {
        AnalysisResult result = analysisRepository.findById(analysisId).orElseThrow();
        StringBuilder logs = new StringBuilder();
        activeLogs.put(analysisId, logs);

        try {
            String buildId = option.getBuildId();
            String fortifyPath = settingsService.getSetting(SettingsService.KEY_FORTIFY_PATH);
            String executable = (fortifyPath != null && !fortifyPath.isEmpty()) ? fortifyPath : "sourceanalyzer";

            String defaultMemory = settingsService.getSetting(SettingsService.KEY_DEFAULT_MEMORY);
            String memoryOption = (defaultMemory != null && !defaultMemory.isEmpty()) ? "-Xmx" + defaultMemory
                    : "-Xmx4G";

            // Prepare Log Files
            String resultPath = settingsService.getSetting(SettingsService.KEY_RESULT_PATH);
            if (resultPath == null || resultPath.isEmpty()) {
                resultPath = "./results";
            }

            String datePath = java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd")
                    .format(java.time.LocalDate.now());
            String logDir = resultPath + File.separator + datePath + File.separator + buildId;
            new File(logDir).mkdirs();
            new File(logDir + File.separator + "source").mkdirs();
            new File(logDir + File.separator + "log").mkdirs();
            new File(logDir + File.separator + "report").mkdirs();

            String buildLogPath = new File(logDir + File.separator + "log", buildId + "_build.log").getAbsolutePath();
            String scanLogPath = new File(logDir + File.separator + "log", buildId + "_scan.log").getAbsolutePath();

            result.setBuildLogFilePath(buildLogPath);
            result.setScanLogFilePath(scanLogPath);
            analysisRepository.save(result);

            // 1. Clean
            if (option.isClean()) {
                updateStatus(result, "CLEANING");
                executeCommand(logs, executable, memoryOption, "-b", buildId, "-logfile", buildLogPath, "-clean");
            }

            // 2. Translate
            if (option.isTranslate()) {
                updateStatus(result, "TRANSLATING");
                List<String> translateCmd = new ArrayList<>();
                translateCmd.add(executable);
                translateCmd.add(memoryOption);
                translateCmd.add("-b");
                translateCmd.add(buildId);
                translateCmd.add("-logfile");
                translateCmd.add(buildLogPath);

                if (option.getClasspath() != null && !option.getClasspath().isEmpty()) {
                    translateCmd.add("-cp");
                    translateCmd.add(option.getClasspath());
                }

                String jdkVersion = option.getJdkVersion();
                if (jdkVersion == null || jdkVersion.isEmpty()) {
                    jdkVersion = settingsService.getSetting(SettingsService.KEY_DEFAULT_JDK);
                }

                if (jdkVersion != null && !jdkVersion.isEmpty()) {
                    translateCmd.add("-source");
                    translateCmd.add(jdkVersion);
                }

                String sourcePath = option.getSourcePath() != null ? option.getSourcePath() : ".";
                translateCmd.add(sourcePath);

                executeCommand(logs, translateCmd.toArray(new String[0]));
            }

            // 3. Scan
            if (option.isScan()) {
                updateStatus(result, "SCANNING");
                List<String> scanCmd = new ArrayList<>();
                scanCmd.add(executable);
                scanCmd.add(memoryOption);
                scanCmd.add("-b");
                scanCmd.add(buildId);
                scanCmd.add("-scan");
                scanCmd.add("-logfile");
                scanCmd.add(scanLogPath);

                String fprPath = logDir + File.separator + buildId + ".fpr";
                scanCmd.add("-f");
                scanCmd.add(fprPath);
                result.setFprPath(fprPath);

                if (option.isQuickScan()) {
                    scanCmd.add("-quick");
                }

                if (option.isDebugVerbose()) {
                    scanCmd.add("-debug-verbose");
                }

                executeCommand(logs, scanCmd.toArray(new String[0]));
            }

            // 4. Report
            if (option.isReport()) {
                updateStatus(result, "REPORTING");
                try {
                    // Generate PDF
                    String pdfPath = reportService.generateReport(result, option, "pdf", line -> {
                        logs.append(line).append("\n");
                    });
                    if (pdfPath != null) {
                        result.setReportPdfPath(pdfPath);
                        analysisRepository.save(result); // Save immediately
                        logs.append("PDF Report generated: ").append(pdfPath).append("\n");
                    } else {
                        logs.append("PDF Report generation failed.\n");
                    }

                    // Generate XML
                    String xmlPath = reportService.generateReport(result, option, "xml", line -> {
                        logs.append(line).append("\n");
                    });
                    if (xmlPath != null) {
                        result.setReportXmlPath(xmlPath);
                        analysisRepository.save(result); // Save immediately
                        logs.append("XML Report generated: ").append(xmlPath).append("\n");

                        // Parse XML and save to DB
                        try {
                            // Reload result to ensure it's attached to the persistence context
                            // AnalysisResult resultForParsing =
                            // analysisRepository.findById(analysisId).orElse(result);
                            // reportParserService.parseAndSave(new File(xmlPath), resultForParsing);
                            reportParserService.parseAndSave(new File(xmlPath), analysisId);
                            logs.append("XML Report parsed and saved to database.\n");
                        } catch (Exception e) {
                            logs.append("XML Parsing failed: ").append(e.getMessage()).append("\n");
                            log.error("XML Parsing failed", e);
                        }
                    } else {
                        logs.append("XML Report generation failed.\n");
                    }
                } catch (Exception e) {
                    logs.append("Report generation failed: ").append(e.getMessage()).append("\n");
                    log.error("Report generation failed", e);
                    // Don't fail the whole analysis if only reporting fails, or maybe set a warning
                    // status?
                    // For now, proceed to SUCCESS but log error.
                }
            }

            logs.append("Analysis completed successfully. Updating status to SUCCESS.\n");

            // Reload result to ensure we have the latest data (including what
            // ReportParserService might have saved)
            result = analysisRepository.findById(analysisId).orElse(result);
            updateStatus(result, "SUCCESS");
        } catch (Exception e) {
            String errorMsg = "Error: " + e.getMessage();
            logs.append(errorMsg).append("\n");
            System.err.println(errorMsg);
            e.printStackTrace();
            updateStatus(result, "FAILED");
        } finally {
            try {
                AnalysisResult finalResult = analysisRepository.findById(analysisId).orElse(null);
                if (finalResult != null) {
                    finalResult.setLogs(logs.toString());
                    analysisRepository.save(finalResult);
                }
            } catch (Exception e) {
                log.error("Failed to save logs", e);
            }
            activeLogs.remove(analysisId);
        }
        return CompletableFuture.completedFuture(null);
    }

    private void updateStatus(AnalysisResult result, String status) {
        result.setStatus(status);
        analysisRepository.save(result);
    }

    public AnalysisResult createAnalysis(AnalysisOption option) {
        AnalysisResult result = new AnalysisResult();
        result.setAnalysisOption(option);
        result.setScanDate(LocalDateTime.now());
        result.setStatus("RUNNING");
        return analysisRepository.save(result);
    }

    private void executeCommand(StringBuilder logs, String... command) throws Exception {
        String cmdStr = String.join(" ", command);
        logs.append("Executing: ").append(cmdStr).append("\n");
        System.out.println("Executing: " + cmdStr);

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(new File(".")); // Execute in current directory
        builder.redirectErrorStream(true);
        Process process = builder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logs.append(line).append("\n");
                System.out.println("[CMD] " + line);
            }
        }

        int exitCode = process.waitFor();
        logs.append("Exit Code: ").append(exitCode).append("\n\n");
        if (exitCode != 0) {
            throw new Exception("Command failed with exit code " + exitCode);
        }

    }

    public void reparseResult(Long id) throws Exception {
        AnalysisResult result = analysisRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid analysis ID: " + id));

        String xmlPath = result.getReportXmlPath();
        if (xmlPath == null || xmlPath.isEmpty()) {
            throw new IllegalStateException("No XML report path found for analysis ID: " + id);
        }

        File xmlFile = new File(xmlPath);
        if (!xmlFile.exists()) {
            throw new IllegalStateException("XML report file not found at: " + xmlPath);
        }

        // Clear existing data
        if (result.getScanSummary() != null) {
            result.setScanSummary(null);
        }

        if (result.getVulnerabilities() != null) {
            result.getVulnerabilities().clear();
        }

        analysisRepository.save(result);

        // Re-parse
        reportParserService.parseAndSave(xmlFile, id);
    }
}
