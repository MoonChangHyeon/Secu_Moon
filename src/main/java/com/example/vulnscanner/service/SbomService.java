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
    private final com.example.vulnscanner.mocha.repository.MochaCveRepository mochaCveRepository;
    private final com.example.vulnscanner.mocha.repository.MochaGhsaRepository mochaGhsaRepository;
    private final com.example.vulnscanner.mocha.repository.MochaSpdxLicenseRepository mochaSpdxLicenseRepository;

    @org.springframework.beans.factory.annotation.Autowired
    public SbomService(SbomRepository sbomRepository, SettingsService settingsService,
            WebClient.Builder webClientBuilder,
            com.example.vulnscanner.mocha.repository.MochaCveRepository mochaCveRepository,
            com.example.vulnscanner.mocha.repository.MochaGhsaRepository mochaGhsaRepository,
            com.example.vulnscanner.mocha.repository.MochaSpdxLicenseRepository mochaSpdxLicenseRepository) {
        this.sbomRepository = sbomRepository;
        this.settingsService = settingsService;
        this.mochaCveRepository = mochaCveRepository;
        this.mochaGhsaRepository = mochaGhsaRepository;
        this.mochaSpdxLicenseRepository = mochaSpdxLicenseRepository;
        this.webClient = webClientBuilder
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10MB
                .build();
    }

    @Transactional
    public SbomResult createSbomAnalysis(AnalysisOption option, MultipartFile sbomFile, String requester)
            throws IOException {
        SbomResult result = new SbomResult();
        result.setAnalysisOption(option);
        result.setScanDate(LocalDateTime.now());
        result.setStatus("RUNNING");
        result.setRequester(requester);

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
                System.out.println("SBOM Analysis Status Check: JobID=" + result.getJobId() + ", Status=" + status);

                // Map API status to App status
                if ("completed".equalsIgnoreCase(status)) {
                    result.setStatus("SUCCESS");
                    fetchAndSaveResults(result);
                } else if ("failed".equalsIgnoreCase(status)) {
                    result.setStatus("FAILED");
                    result.setLogs((String) response.get("message"));
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

    @Transactional
    public void fetchAndSaveResults(SbomResult result) {
        try {
            String baseUrl = getApiBaseUrl();
            // Fetch as String to avoid TypeReference errors and allow debugging
            String rawResponse = webClient.get()
                    .uri(baseUrl + "/api/sbom/results/" + result.getJobId())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Log raw response for debugging
            System.out.println("SBOM Results Fetch Raw Response: " + rawResponse);

            if (rawResponse != null && !rawResponse.isEmpty()) {
                // 1. Save raw JSON to file
                String resultPath = settingsService.getSetting(SettingsService.KEY_RESULT_PATH);
                Path resultDir = Paths.get(resultPath, "sbom_results");
                if (!Files.exists(resultDir)) {
                    Files.createDirectories(resultDir);
                }
                String jsonFileName = result.getJobId() + "_result.json";
                Path jsonFilePath = resultDir.resolve(jsonFileName);
                Files.writeString(jsonFilePath, rawResponse);

                result.setResultJsonPath(jsonFilePath.toString());
                result.setLogs("Analysis completed successfully. Result saved to: " + jsonFileName);

                // 2. Parse JSON and save to DB
                ObjectMapper mapper = new ObjectMapper();
                try {
                    com.fasterxml.jackson.databind.JsonNode rootNode = mapper.readTree(rawResponse);
                    List<com.example.vulnscanner.entity.SbomComponent> newComponents = new java.util.ArrayList<>();

                    if (rootNode.isArray()) {
                        System.out.println("Result is a JSON Array. Parsing as component list.");
                        for (com.fasterxml.jackson.databind.JsonNode node : rootNode) {
                            newComponents.add(parseComponent(result, node));
                        }
                    } else if (rootNode.isObject()) {
                        System.out.println("Result is a JSON Object.");
                        if (rootNode.has("components")) {
                            com.fasterxml.jackson.databind.JsonNode componentsNode = rootNode.get("components");
                            if (componentsNode.isArray()) {
                                for (com.fasterxml.jackson.databind.JsonNode node : componentsNode) {
                                    newComponents.add(parseComponent(result, node));
                                }
                            }
                        }
                    } else {
                        System.err.println("Warning: fetched JSON is neither Array nor Object.");
                    }

                    if (!newComponents.isEmpty()) {
                        if (result.getComponents() == null) {
                            result.setComponents(new java.util.ArrayList<>());
                        } else {
                            result.getComponents().clear();
                        }
                        result.getComponents().addAll(newComponents);
                        System.out.println("Parsed " + newComponents.size() + " components.");
                    } else {
                        System.out.println("No components found to parse.");
                    }

                } catch (Exception e) {
                    System.err.println("Warning: Fetched SBOM result parsing failed: " + e.getMessage());
                    e.printStackTrace();
                    result.setLogs("Analysis completed, but parsing failed: " + e.getMessage());
                }

                result.setStatus("SUCCESS");
                sbomRepository.save(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setStatus("FAILED");
            result.setLogs("Failed to fetch results: " + e.getMessage());
            sbomRepository.save(result);
        }
    }

    private com.example.vulnscanner.entity.SbomComponent parseComponent(SbomResult result,
            com.fasterxml.jackson.databind.JsonNode node) {
        com.example.vulnscanner.entity.SbomComponent component = new com.example.vulnscanner.entity.SbomComponent();
        component.setSbomResult(result);
        component.setName(getText(node, "name"));
        component.setVersion(getText(node, "version"));
        component.setType(getText(node, "type"));
        component.setPurl(getText(node, "purl"));

        // Parse and Enrich Vulnerabilities
        if (node.has("vulnerabilities") && node.get("vulnerabilities").isArray()) {
            for (com.fasterxml.jackson.databind.JsonNode vulnNode : node.get("vulnerabilities")) {
                com.example.vulnscanner.entity.SbomVulnerability vuln = parseAndEnrichVulnerability(vulnNode);
                if (vuln != null) {
                    vuln.setComponent(component);
                    component.getVulnerabilities().add(vuln);
                }
            }
        }

        // Parse and Enrich Licenses
        if (node.has("licenses") && node.get("licenses").isArray()) {
            for (com.fasterxml.jackson.databind.JsonNode licenseNode : node.get("licenses")) {
                com.example.vulnscanner.entity.SbomLicense license = parseAndEnrichLicense(licenseNode);
                if (license != null) {
                    license.setComponent(component);
                    component.getLicenses().add(license);
                }
            }
        }

        return component;
    }

    private com.example.vulnscanner.entity.SbomVulnerability parseAndEnrichVulnerability(
            com.fasterxml.jackson.databind.JsonNode node) {
        com.example.vulnscanner.entity.SbomVulnerability vuln = new com.example.vulnscanner.entity.SbomVulnerability();
        String id = getText(node, "id"); // e.g., CVE-2023-1234
        String source = getText(node, "source_name"); // e.g., NVD, GHSA

        if (id == null || id.isEmpty()) {
            // Try to find ID in other fields if structure differs
            if (node.has("cve"))
                id = getText(node, "cve");
        }

        final String finalId = id;
        vuln.setVulnId(finalId);
        vuln.setSource(source);
        vuln.setUrl(getText(node, "url"));

        // Enrichment from Mocha DB
        if (finalId != null) {
            if (finalId.startsWith("CVE")) {
                mochaCveRepository.findById(finalId).ifPresent(cve -> {
                    vuln.setTitle(cve.getTitle());
                    vuln.setDescription(
                            cve.getDescriptionKr() != null ? cve.getDescriptionKr() : cve.getDescriptionEn());
                    vuln.setSeverity(cve.getVulnStatus()); // Mapping check needed, using status for now or need
                                                           // severity field in Cve
                    // Note: Schema analysis showed 'vuln_status' in CVE table, GHSA has 'severity'.
                    // If CVE table lacks severity, we might need NVD data or just leave it.
                    // Checking implementation_plan, MochaCve has vulnStatus. MochaGhsa has
                    // severity.
                });
            } else if (finalId.startsWith("GHSA")) {
                mochaGhsaRepository.findById(finalId).ifPresent(ghsa -> {
                    vuln.setTitle(ghsa.getTitle());
                    vuln.setDescription(ghsa.getSummaryKr() != null ? ghsa.getSummaryKr() : ghsa.getSummaryEn());
                    vuln.setSeverity(ghsa.getSeverity());
                    if (vuln.getUrl() == null || vuln.getUrl().isEmpty()) {
                        vuln.setUrl("https://github.com/advisories/" + finalId);
                    }
                });
            }
        }

        // Fallback: If no enrichment, use data from JSON if available
        if (vuln.getDescription() == null)
            vuln.setDescription(getText(node, "description"));
        if (vuln.getSeverity() == null) {
            // Try to find severity in JSON
            if (node.has("ratings") && node.get("ratings").isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode rating : node.get("ratings")) {
                    if (rating.has("severity")) {
                        vuln.setSeverity(getText(rating, "severity"));
                        break;
                    }
                }
            }
        }

        return vuln;
    }

    private com.example.vulnscanner.entity.SbomLicense parseAndEnrichLicense(
            com.fasterxml.jackson.databind.JsonNode node) {
        com.example.vulnscanner.entity.SbomLicense license = new com.example.vulnscanner.entity.SbomLicense();

        // Structure might be { "license": { "id": "Apache-2.0" } } or just { "id":
        // "Apache-2.0" }
        String licenseId = "";
        String name = "";

        if (node.has("license")) {
            com.fasterxml.jackson.databind.JsonNode inner = node.get("license");
            licenseId = getText(inner, "id");
            name = getText(inner, "name");
        } else {
            licenseId = getText(node, "id");
            name = getText(node, "name");
        }

        license.setLicenseId(licenseId);
        license.setName(name);

        // Enrichment
        if (licenseId != null && !licenseId.isEmpty()) {
            mochaSpdxLicenseRepository.findByLicenseId(licenseId).ifPresent(spdx -> {
                if (license.getName() == null || license.getName().isEmpty()) {
                    license.setName(spdx.getName());
                }
                license.setSeverity(spdx.getSeverity());
                license.setUrl(spdx.getDetailsUrl());
            });
        }

        return license;
    }

    private String getText(com.fasterxml.jackson.databind.JsonNode node, String fieldName) {
        return node.has(fieldName) ? node.get(fieldName).asText() : "";
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

    @Transactional
    public void deleteSbomResult(Long id) {
        sbomRepository.findById(id).ifPresent(result -> {
            // Delete uploaded SBOM file
            if (result.getSbomFilePath() != null) {
                try {
                    Files.deleteIfExists(Paths.get(result.getSbomFilePath()));
                } catch (IOException e) {
                    System.err.println("Failed to delete SBOM file: " + e.getMessage());
                }
            }
            // Delete report files if any (future proofing)
            if (result.getReportPath() != null) {
                try {
                    Files.deleteIfExists(Paths.get(result.getReportPath()));
                } catch (IOException e) {
                    System.err.println("Failed to delete SBOM report file: " + e.getMessage());
                }
            }

            sbomRepository.delete(result);
        });
    }
}
