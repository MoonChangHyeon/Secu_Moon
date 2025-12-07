package com.example.vulnscanner.service;

import com.example.vulnscanner.entity.AnalysisOption;
import com.example.vulnscanner.entity.SbomResult;
import com.example.vulnscanner.repository.SbomRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SbomService {

    private final SbomRepository sbomRepository;
    private final SettingsService settingsService;
    private final WebClient webClient;

    @org.springframework.beans.factory.annotation.Autowired
    public SbomService(SbomRepository sbomRepository, SettingsService settingsService,
            WebClient.Builder webClientBuilder) {
        this.sbomRepository = sbomRepository;
        this.settingsService = settingsService;
        this.webClient = webClientBuilder.build();
    }

    @Transactional
    public SbomResult createSbomAnalysis(AnalysisOption option, MultipartFile sbomFile) throws IOException {
        SbomResult result = new SbomResult();
        result.setAnalysisOption(option);
        result.setScanDate(LocalDateTime.now());
        result.setStatus("RUNNING");

        // Save uploaded SBOM file
        if (sbomFile != null && !sbomFile.isEmpty()) {
            String resultPath = settingsService.getSetting(SettingsService.KEY_RESULT_PATH);
            Path uploadDir = Paths.get(resultPath, "sbom_uploads");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            String fileName = UUID.randomUUID() + "_" + sbomFile.getOriginalFilename();
            Path filePath = uploadDir.resolve(fileName);
            Files.copy(sbomFile.getInputStream(), filePath);
            result.setSbomFilePath(filePath.toString());

            // Call AI_SBOM API
            try {
                String jobId = requestAnalysis(filePath);
                result.setJobId(jobId);
            } catch (Exception e) {
                result.setStatus("FAILED");
                result.setLogs("Failed to start analysis: " + e.getMessage());
            }
        }

        return sbomRepository.save(result);
    }

    private String getApiBaseUrl() {
        String url = settingsService.getSetting(SettingsService.KEY_SBOM_API_URL);
        return (url != null && !url.isEmpty()) ? url : "http://localhost:5000";
    }

    private String requestAnalysis(Path filePath) {
        FileSystemResource resource = new FileSystemResource(filePath);

        // Multipart Body Builder
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", resource);
        String clientJobId = UUID.randomUUID().toString();
        builder.part("job_id", clientJobId);

        String baseUrl = getApiBaseUrl();

        Map<String, Object> response = webClient.post()
                .uri(baseUrl + "/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .block();

        if (response != null && response.containsKey("job_id")) {
            return (String) response.get("job_id");
        }
        throw new RuntimeException("Failed to get job_id from API response");
    }

    public void updateSbomStatus(Long id) {
        SbomResult result = sbomRepository.findById(id).orElse(null);
        if (result == null || result.getJobId() == null || "SUCCESS".equals(result.getStatus())
                || "FAILED".equals(result.getStatus())) {
            return;
        }

        try {
            String baseUrl = getApiBaseUrl();
            Map<String, Object> response = webClient.get()
                    .uri(baseUrl + "/status/" + result.getJobId())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    })
                    .block();

            if (response != null) {
                String status = (String) response.get("status"); // running, completed, failed
                // Map API status to App status
                if ("completed".equalsIgnoreCase(status)) {
                    result.setStatus("SUCCESS");
                    fetchAndSaveResults(result);
                } else if ("failed".equalsIgnoreCase(status)) {
                    result.setStatus("FAILED");
                    result.setLogs((String) response.get("message")); // Or detailed error
                } else {
                    result.setStatus("RUNNING");
                    // Update progress if needed
                }
                sbomRepository.save(result);
            }
        } catch (Exception e) {
            // Log error, maybe mark as FAILED if repeated errors
            System.err.println("Error updating status for SBOM " + id + ": " + e.getMessage());
        }
    }

    private void fetchAndSaveResults(SbomResult result) {
        try {
            String baseUrl = getApiBaseUrl();
            Map<String, Object> response = webClient.get()
                    .uri(baseUrl + "/api/sbom/results/" + result.getJobId())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    })
                    .block();

            if (response != null) {
                // Store full response in logs as JSON
                ObjectMapper mapper = new ObjectMapper();
                result.setLogs(mapper.writeValueAsString(response));

                // Parse components and save (Simplified for now)
                // List<Map> components = (List<Map>) response.get("components");
                // ... save components ...
            }
        } catch (Exception e) {
            result.setStatus("FAILED");
            result.setLogs("Failed to fetch results: " + e.getMessage());
        }
    }

    public List<SbomResult> getRecentScans() {
        return sbomRepository.findTop5ByOrderByScanDateDesc();
    }

    public List<SbomResult> getAllSbomResults() {
        return sbomRepository.findAll();
    }

    public SbomResult getSbomResult(Long id) {
        if (id == null)
            return null;
        return sbomRepository.findById(id).orElse(null);
    }
}
