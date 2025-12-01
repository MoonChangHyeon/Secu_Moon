package com.example.vulnscanner.service;

import com.example.vulnscanner.entity.AnalysisOption;
import com.example.vulnscanner.entity.AnalysisResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ReportService {

    public String generateReport(AnalysisResult result, AnalysisOption option, String format,
            java.util.function.Consumer<String> logConsumer) {
        log.info("Starting report generation for result ID: {}, format: {}", result.getId(), format);
        if (logConsumer != null)
            logConsumer.accept("Starting report generation (" + format + ")...");

        String fprPath = result.getFprPath();
        if (fprPath == null || !new File(fprPath).exists()) {
            String msg = "FPR file not found: " + fprPath;
            log.error(msg);
            if (logConsumer != null)
                logConsumer.accept(msg);
            return null;
        }

        String outputDir = Paths.get(fprPath).getParent().toString() + File.separator + "report";
        String reportFileName = result.getAnalysisOption().getBuildId() + "." + format;
        String reportPath = Paths.get(outputDir, reportFileName).toString();

        List<String> command = new ArrayList<>();
        command.add("ReportGenerator");
        command.add("-source");
        command.add(fprPath);
        command.add("-format");
        command.add(format);
        command.add("-f");
        command.add(reportPath);

        if (option.getReportTemplate() != null && !option.getReportTemplate().isEmpty()) {
            command.add("-template");
            command.add(option.getReportTemplate());
        }

        if (option.isShowSuppressed()) {
            command.add("-showSuppressed");
        }

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (logConsumer != null)
                        logConsumer.accept(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                log.info("Report generated successfully: {}", reportPath);
                if (logConsumer != null)
                    logConsumer.accept("Report generated successfully: " + reportPath);
                return reportPath;
            } else {
                String msg = "Report generation failed. Exit code: " + exitCode;
                log.error(msg);
                if (logConsumer != null)
                    logConsumer.accept(msg);
                return null;
            }

        } catch (Exception e) {
            log.error("Error generating report", e);
            if (logConsumer != null)
                logConsumer.accept("Error generating report: " + e.getMessage());
            return null;
        }
    }
}
