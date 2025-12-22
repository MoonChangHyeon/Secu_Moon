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
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(50 * 1024 * 1024)) // Increased to 50MB
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
            System.out.println(
                    "SBOM Results Fetch Raw Response Length: " + (rawResponse != null ? rawResponse.length() : "null"));

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

                // DEBUG: Check database counts
                try {
                    long cveCount = mochaCveRepository.count();
                    long ghsaCount = mochaGhsaRepository.count();
                    System.out.println("DEBUG: Mocha DB Check - CVE Count: " + cveCount);
                    System.out.println("DEBUG: Mocha DB Check - GHSA Count: " + ghsaCount);
                } catch (Exception e) {
                    System.err.println("DEBUG: Failed to count Mocha DB records: " + e.getMessage());
                }

                try {
                    com.fasterxml.jackson.databind.JsonNode rootNode = mapper.readTree(rawResponse);

                    // Temp storage for 2-pass parsing
                    java.util.Map<String, com.example.vulnscanner.entity.SbomComponent> componentMap = new java.util.HashMap<>();
                    List<com.example.vulnscanner.entity.SbomComponent> newComponents = new java.util.ArrayList<>();

                    com.fasterxml.jackson.databind.JsonNode componentsNode = null;
                    com.fasterxml.jackson.databind.JsonNode vulnerabilitiesNode = null;
                    com.fasterxml.jackson.databind.JsonNode licensesNode = null;

                    if (rootNode.isArray()) {
                        // Find sections
                        for (com.fasterxml.jackson.databind.JsonNode section : rootNode) {
                            if (section.has("components"))
                                componentsNode = section.get("components");
                            if (section.has("vulnerabilities"))
                                vulnerabilitiesNode = section.get("vulnerabilities");
                            if (section.has("licenses"))
                                licensesNode = section.get("licenses");
                            if (section.has("summary")) {
                                com.fasterxml.jackson.databind.JsonNode summaryNode = section.get("summary");
                                if (summaryNode.has("vulnerabilities_count"))
                                    result.setVulnerabilitiesCount(summaryNode.get("vulnerabilities_count").asInt());
                                if (summaryNode.has("licenses_count"))
                                    result.setLicensesCount(summaryNode.get("licenses_count").asInt());
                                if (summaryNode.has("components_count"))
                                    result.setComponentsCount(summaryNode.get("components_count").asInt());
                            }
                        }
                    } else if (rootNode.isObject()) {
                        if (rootNode.has("components"))
                            componentsNode = rootNode.get("components");
                        if (rootNode.has("vulnerabilities"))
                            vulnerabilitiesNode = rootNode.get("vulnerabilities");
                        if (rootNode.has("licenses"))
                            licensesNode = rootNode.get("licenses");
                    }

                    // Pass 1: Parse Components
                    if (componentsNode != null && componentsNode.isArray()) {
                        System.out.println("Processing " + componentsNode.size() + " components...");
                        for (com.fasterxml.jackson.databind.JsonNode node : componentsNode) {
                            try {
                                com.example.vulnscanner.entity.SbomComponent comp = parseComponent(result, node);
                                newComponents.add(comp);
                                if (comp.getPurl() != null && !comp.getPurl().isEmpty()) {
                                    componentMap.put(comp.getPurl(), comp);
                                }
                            } catch (Exception e) {
                                System.err.println("Warning: Failed to parse component: " + e.getMessage());
                            }
                        }
                    } else {
                        System.out.println("No 'components' section found.");
                    }

                    // Pass 2: Parse and Link Vulnerabilities
                    if (vulnerabilitiesNode != null && vulnerabilitiesNode.isArray()) {
                        System.out.println("Processing " + vulnerabilitiesNode.size() + " vulnerabilities...");
                        for (com.fasterxml.jackson.databind.JsonNode node : vulnerabilitiesNode) {
                            try {
                                // DEBUG LOG
                                System.out.println("DEBUG: Raw Vulnerability Node: " + node.toString());

                                String pkgName = getText(node, "package_name");
                                com.example.vulnscanner.entity.SbomComponent comp = componentMap.get(pkgName);
                                if (comp != null) {
                                    com.example.vulnscanner.entity.SbomVulnerability vuln = parseAndEnrichVulnerability(
                                            node);
                                    vuln.setComponent(comp);
                                    comp.getVulnerabilities().add(vuln);
                                } else {
                                    System.out.println("Warning: Vulnerability found for unknown package: " + pkgName);
                                }
                            } catch (Exception e) {
                                System.err.println("Warning: Failed to parse vulnerability: " + e.getMessage());
                            }
                        }
                    }

                    // Pass 3: Parse Licenses
                    if (licensesNode != null && licensesNode.isArray()) {
                        System.out.println("Processing " + licensesNode.size() + " licenses...");
                        for (com.fasterxml.jackson.databind.JsonNode node : licensesNode) {
                            try {
                                String pkgName = getText(node, "package_name");
                                com.example.vulnscanner.entity.SbomComponent comp = componentMap.get(pkgName);

                                if (comp != null) {
                                    com.example.vulnscanner.entity.SbomLicense lic = new com.example.vulnscanner.entity.SbomLicense();
                                    lic.setComponent(comp);
                                    lic.setName(getText(node, "license_type"));
                                    lic.setLocation(getText(node, "location"));

                                    if (comp.getLicenses() == null) {
                                        comp.setLicenses(new java.util.ArrayList<>());
                                    }
                                    comp.getLicenses().add(lic);
                                }
                            } catch (Exception e) {
                                System.err.println("Warning: Failed to parse license: " + e.getMessage());
                            }
                        }
                    }

                    if (!newComponents.isEmpty()) {
                        if (result.getComponents() == null) {
                            result.setComponents(new java.util.ArrayList<>());
                        } else {
                            result.getComponents().clear();
                        }
                        result.getComponents().addAll(newComponents);
                        System.out.println("Successfully parsed " + newComponents.size() + " components.");
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

        String type = "";
        String name = "";
        String version = "";

        // Parse "package_name" which is a PURL: pkg:pypi/cookiecutter@1.7.3
        String purl = getText(node, "package_name");
        component.setPurl(purl);

        if (purl != null && purl.startsWith("pkg:")) {
            try {
                // Simple parser without dependency
                String remainder = purl.substring(4); // Remove pkg:

                int slashIndex = remainder.indexOf("/");
                if (slashIndex > 0) {
                    type = remainder.substring(0, slashIndex);
                    remainder = remainder.substring(slashIndex + 1);
                } else {
                    // unexpected format pkg:name@ver?
                    int atIndex = remainder.indexOf("@");
                    if (atIndex > 0) {
                        type = "generic"; // fallback
                        // continue parsing name/ver
                    }
                }

                // Now remainder is namespace/name@version or just name@version
                int atIndex = remainder.lastIndexOf("@");
                if (atIndex > 0) {
                    version = remainder.substring(atIndex + 1);
                    name = remainder.substring(0, atIndex); // includes namespace if present
                } else {
                    name = remainder;
                }

            } catch (Exception e) {
                System.err.println("Error parsing PURL: " + purl);
                name = purl; // fallback
            }
        } else {
            // Fallback try standard fields
            name = getText(node, "name");
            if (name.isEmpty())
                name = getText(node, "Name");
            version = getText(node, "version");
            type = getText(node, "type");
        }

        component.setName(name);
        component.setVersion(version);
        component.setType(type);

        // Location mapping?
        // component.setDescription(getText(node, "location"));

        return component;
    }

    private com.example.vulnscanner.entity.SbomVulnerability parseAndEnrichVulnerability(
            com.fasterxml.jackson.databind.JsonNode node) {
        com.example.vulnscanner.entity.SbomVulnerability vuln = new com.example.vulnscanner.entity.SbomVulnerability();

        String rawCve = getText(node, "cve");
        String rawVulnId = getText(node, "vulnerability_id");

        // Normalize IDs
        String cveId = normalizeId(rawCve);
        String vendorId = normalizeId(rawVulnId);

        // Determine Final ID (Prioritize CVE)
        String finalId = (cveId != null && !cveId.isEmpty()) ? cveId : vendorId;

        vuln.setVulnId(finalId);
        vuln.setVendorId(vendorId); // Keep vendor ID (GHSA, etc.)
        vuln.setSource((vendorId != null && vendorId.startsWith("GHSA")) ? "GHSA" : "NVD");

        // Enrichment from Mocha DB
        boolean enriched = false;
        if (finalId != null && !finalId.isEmpty()) {
            System.out.println("DEBUG: Attempting to enrich vulnerability. FinalID=[" + finalId + "], RawID=[" + rawCve
                    + "], VendorID=[" + rawVulnId + "]");

            if (finalId.startsWith("CVE")) {
                com.example.vulnscanner.mocha.entity.MochaCve cve = mochaCveRepository.findById(finalId).orElse(null);
                if (cve != null) {
                    System.out.println("DEBUG: >>> Found CVE in Mocha DB: " + finalId);
                    vuln.setTitle(cve.getTitle());
                    // Use KR description if available and not empty, otherwise EN
                    String descKr = cve.getDescriptionKr();
                    if (descKr != null && !descKr.trim().isEmpty()) {
                        // Store raw JSON string for frontend parsing
                        vuln.setDescription(descKr);
                    } else {
                        vuln.setDescription(cve.getDescriptionEn());
                    }
                    vuln.setSeverity(cve.getVulnStatus()); // Map status to severity for now
                    enriched = true;
                } else {
                    System.out.println("DEBUG: >>> CVE NOT found in Mocha DB: " + finalId);
                }
            } else if (finalId.startsWith("GHSA")) {
                com.example.vulnscanner.mocha.entity.MochaGhsa ghsa = mochaGhsaRepository.findById(finalId)
                        .orElse(null);
                if (ghsa != null) {
                    System.out.println("DEBUG: >>> Found GHSA in Mocha DB: " + finalId);
                    vuln.setTitle(ghsa.getTitle());
                    String sumKr = ghsa.getSummaryKr();
                    if (sumKr != null && !sumKr.trim().isEmpty()) {
                        vuln.setDescription(sumKr);
                    } else {
                        vuln.setDescription(ghsa.getSummaryEn());
                    }
                    vuln.setSeverity(ghsa.getSeverity());
                    enriched = true;
                } else {
                    System.out.println("DEBUG: >>> GHSA NOT found in Mocha DB: " + finalId);
                }
            }
        } else {
            System.out.println("DEBUG: No valid Vulnerability ID found to enrich.");
        }

        if (!enriched) {
            // Basic population if not found in DB
            vuln.setTitle(finalId != null ? finalId : "Unknown Vulnerability");
            String loc = getText(node, "location");
            vuln.setDescription("Details not found in local DB.\nLocation: " + (loc.isEmpty() ? "N/A" : loc));
        }

        // URL Generation
        if (vuln.getUrl() == null || vuln.getUrl().isEmpty()) {
            if (finalId != null) {
                if (finalId.startsWith("GHSA"))
                    vuln.setUrl("https://github.com/advisories/" + finalId);
                else if (finalId.startsWith("CVE"))
                    vuln.setUrl("https://nvd.nist.gov/vuln/detail/" + finalId);
            }
        }

        return vuln;
    }

    private String normalizeId(String id) {
        if (id == null)
            return null;
        String normalized = id.trim();
        // Handle array format ["CVE-XXX"]
        if (normalized.startsWith("[")) {
            normalized = normalized.replaceAll("[\"\\[\\]]", "");
            if (normalized.contains(",")) {
                normalized = normalized.split(",")[0].trim();
            }
        }
        if ("N/A".equalsIgnoreCase(normalized) || "-".equals(normalized) || normalized.isEmpty()) {
            return null;
        }
        return normalized;
    }

    // Keep getText and parseAndEnrichLicense (simplified)
    private com.example.vulnscanner.entity.SbomLicense parseAndEnrichLicense(
            com.fasterxml.jackson.databind.JsonNode node) {
        return null; // Impl if needed
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
