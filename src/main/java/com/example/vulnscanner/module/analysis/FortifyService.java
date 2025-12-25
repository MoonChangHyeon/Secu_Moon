package com.example.vulnscanner.module.analysis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class FortifyService {

    @Value("${fortify.mock-mode:true}")
    private boolean mockMode;

    @Value("${fortify.executable-path:}")
    private String executablePath;

    private String getExecutable() {
        return (executablePath != null && !executablePath.isBlank()) ? executablePath : "sourceanalyzer";
    }

    public CompletableFuture<String> clean(String buildId) {
        return executeCommand(getExecutable(), "-b", buildId, "-clean");
    }

    public CompletableFuture<String> translate(String buildId, String targetPath) {
        // Simple translation: sourceanalyzer -b <buildId> -cp "lib/*.jar"
        // "src/**/*.java"
        // Assuming targetPath is the root of the project
        String fileSpec = targetPath + "/**/*.java";
        return executeCommand(getExecutable(), "-b", buildId, "-cp", targetPath + "/lib/*.jar", fileSpec);
    }

    public CompletableFuture<String> translateWithGradle(String buildId, String projectPath) {
        return executeCommand(getExecutable(), "-b", buildId, "gradle", "clean", "build", "-p", projectPath);
    }

    public CompletableFuture<String> scan(String buildId, String outputName) {
        return executeCommand(getExecutable(), "-b", buildId, "-scan", "-f", outputName + ".fpr");
    }

    private CompletableFuture<String> executeCommand(String... command) {
        return CompletableFuture.supplyAsync(() -> {
            StringBuilder output = new StringBuilder();
            String cmdString = String.join(" ", command);

            output.append("$ ").append(cmdString).append("\n");

            if (mockMode) {
                try {
                    Thread.sleep(1000); // Simulate work
                    output.append("[MOCK] Command executed successfully.\n");
                    output.append("[MOCK] Simulated output for: ").append(command[0]).append("\n");
                    return output.toString();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return "Interrupted";
                }
            }

            try {
                ProcessBuilder pb = new ProcessBuilder(command);
                pb.redirectErrorStream(true);
                Process process = pb.start();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                }

                int exitCode = process.waitFor();
                output.append("\nExit Code: ").append(exitCode);
            } catch (Exception e) {
                log.error("Error executing command", e);
                output.append("\nError: ").append(e.getMessage());
            }

            return output.toString();
        });
    }
}