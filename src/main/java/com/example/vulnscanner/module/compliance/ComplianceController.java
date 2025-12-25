package com.example.vulnscanner.module.compliance;

import com.example.vulnscanner.module.analysis.RulepackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ComplianceController {

    private final RulepackService rulepackService;
    private final ComplianceComparisonService comparisonService;
    private final PackInfoRepository packInfoRepository;
    private final ComplianceStandardRepository standardRepository;

    // --- Pages ---

    @GetMapping("/compliance")
    public String listPage(Model model) {
        model.addAttribute("packs", rulepackService.getAllRulepacks());
        return "compliance/list";
    }

    @GetMapping("/compliance/viewer/{packId}")
    public String viewerPage(@PathVariable Long packId, Model model) {
        PackInfo pack = packInfoRepository.findById(packId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Pack ID: " + packId));

        // Fetch all standards for grouping
        List<ComplianceStandard> allStandards = standardRepository.findByPackInfoId(packId);

        // Group by Organization (First word of name)
        // Using TreeMap to sort keys (Group Names) alphabetically
        java.util.Map<String, List<ComplianceStandard>> groupedStandards = allStandards.stream()
                .collect(java.util.stream.Collectors.groupingBy(std -> {
                    String name = std.getName();
                    if (name == null || name.isEmpty())
                        return "Others";
                    String[] parts = name.split("\\s+");
                    return parts.length > 0 ? parts[0] : "Others";
                }, java.util.TreeMap::new, java.util.stream.Collectors.toList()));

        model.addAttribute("pack", pack);
        model.addAttribute("groupedStandards", groupedStandards);
        return "compliance/viewer";
    }

    @GetMapping("/compliance/viewer/{packId}/standard/{standardId}")
    public String standardDetailPage(@PathVariable Long packId,
            @PathVariable Long standardId,
            Model model) {
        PackInfo pack = packInfoRepository.findById(packId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Pack ID: " + packId));

        ComplianceStandard standard = standardRepository.findById(standardId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Standard ID: " + standardId));

        model.addAttribute("pack", pack);
        model.addAttribute("standard", standard);
        return "compliance/standard_detail";
    }

    @GetMapping("/compliance/compare")
    public String comparePage(Model model) {
        model.addAttribute("packs", rulepackService.getAllRulepacks());
        return "compliance/compare";
    }

    @GetMapping("/compliance/compare/{baseId}/{targetId}/standard/{standardId}")
    public String compareStandardDetail(@PathVariable Long baseId,
            @PathVariable Long targetId,
            @PathVariable Long standardId,
            Model model) {
        ComplianceDiffDto diff = comparisonService.compare(baseId, targetId);

        ComplianceDiffDto.StandardDiff targetStandard = diff.getStandards().stream()
                .filter(std -> std.getId() != null && std.getId().equals(standardId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Standard not found in comparison result"));

        model.addAttribute("diff", diff);
        model.addAttribute("standard", targetStandard);

        return "compliance/compare_detail";
    }

    @GetMapping("/api/compliance/export/{packId}")
    public ResponseEntity<?> downloadRulepack(@PathVariable Long packId, @RequestParam String type) {
        PackInfo pack = packInfoRepository.findById(packId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Pack ID: " + packId));
        List<ComplianceStandard> standards = standardRepository.findByPackInfoId(packId);

        if ("csv".equalsIgnoreCase(type)) {
            StringBuilder csv = new StringBuilder();
            csv.append("Standard,Category,Internal Category\n");
            for (ComplianceStandard std : standards) {
                for (com.example.vulnscanner.module.compliance.ComplianceCategory cat : std.getCategories()) {
                    if (cat.getMappings().isEmpty()) {
                        csv.append(String.format("\"%s\",\"%s\",\"\"\n",
                                escapeCsv(std.getName()), escapeCsv(cat.getName())));
                    } else {
                        for (com.example.vulnscanner.module.compliance.ComplianceMapping map : cat.getMappings()) {
                            csv.append(String.format("\"%s\",\"%s\",\"%s\"\n",
                                    escapeCsv(std.getName()), escapeCsv(cat.getName()),
                                    escapeCsv(map.getInternalCategory())));
                        }
                    }
                }
            }
            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + pack.getName() + ".csv\"")
                    .contentType(org.springframework.http.MediaType.parseMediaType("text/csv; charset=UTF-8"))
                    .body(csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } else if ("xml".equalsIgnoreCase(type)) {
            StringBuilder xml = new StringBuilder();
            xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            xml.append("<RulepackExport>\n");
            xml.append("  <PackName>").append(escapeXml(pack.getName())).append("</PackName>\n");
            xml.append("  <Version>").append(escapeXml(pack.getVersion())).append("</Version>\n");
            for (ComplianceStandard std : standards) {
                xml.append("  <Standard>\n");
                xml.append("    <Name>").append(escapeXml(std.getName())).append("</Name>\n");
                for (com.example.vulnscanner.module.compliance.ComplianceCategory cat : std.getCategories()) {
                    xml.append("    <Category>\n");
                    xml.append("      <Name>").append(escapeXml(cat.getName())).append("</Name>\n");
                    for (com.example.vulnscanner.module.compliance.ComplianceMapping map : cat.getMappings()) {
                        xml.append("      <Mapping>").append(escapeXml(map.getInternalCategory()))
                                .append("</Mapping>\n");
                    }
                    xml.append("    </Category>\n");
                }
                xml.append("  </Standard>\n");
            }
            xml.append("</RulepackExport>");
            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + pack.getName() + ".xml\"")
                    .contentType(org.springframework.http.MediaType.APPLICATION_XML)
                    .body(xml.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } else {
            return ResponseEntity.badRequest().body("Unsupported type");
        }
    }

    @GetMapping("/api/compliance/export/standard/{standardId}")
    public ResponseEntity<?> downloadStandard(@PathVariable Long standardId, @RequestParam String type) {
        ComplianceStandard standard = standardRepository.findById(standardId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Standard ID: " + standardId));

        if ("csv".equalsIgnoreCase(type)) {
            StringBuilder csv = new StringBuilder();
            csv.append("Standard,Category,Internal Category\n");
            for (com.example.vulnscanner.module.compliance.ComplianceCategory cat : standard.getCategories()) {
                if (cat.getMappings().isEmpty()) {
                    csv.append(String.format("\"%s\",\"%s\",\"\"\n",
                            escapeCsv(standard.getName()), escapeCsv(cat.getName())));
                } else {
                    for (com.example.vulnscanner.module.compliance.ComplianceMapping map : cat.getMappings()) {
                        csv.append(String.format("\"%s\",\"%s\",\"%s\"\n",
                                escapeCsv(standard.getName()), escapeCsv(cat.getName()),
                                escapeCsv(map.getInternalCategory())));
                    }
                }
            }
            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + standard.getName() + ".csv\"")
                    .contentType(org.springframework.http.MediaType.parseMediaType("text/csv; charset=UTF-8"))
                    .body(csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } else if ("xml".equalsIgnoreCase(type)) {
            StringBuilder xml = new StringBuilder();
            xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            xml.append("<StandardExport>\n");
            xml.append("  <StandardName>").append(escapeXml(standard.getName())).append("</StandardName>\n");
            for (com.example.vulnscanner.module.compliance.ComplianceCategory cat : standard.getCategories()) {
                xml.append("    <Category>\n");
                xml.append("      <Name>").append(escapeXml(cat.getName())).append("</Name>\n");
                for (com.example.vulnscanner.module.compliance.ComplianceMapping map : cat.getMappings()) {
                    xml.append("      <Mapping>").append(escapeXml(map.getInternalCategory())).append("</Mapping>\n");
                }
                xml.append("    </Category>\n");
            }
            xml.append("</StandardExport>");
            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + standard.getName() + ".xml\"")
                    .contentType(org.springframework.http.MediaType.APPLICATION_XML)
                    .body(xml.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } else if ("json".equalsIgnoreCase(type)) {
            // Simple Manual JSON construction to avoid adding Jackson dependencies if not
            // present, or use Jackson if available.
            // Given Spring Boot, Jackson is likely available. Let's use ObjectMapper if
            // possible, but manual is safer for single endpoint without autowiring.
            // Manual JSON for robustness here:
            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("  \"standard\": \"").append(escapeJson(standard.getName())).append("\",\n");
            json.append("  \"categories\": [\n");

            List<com.example.vulnscanner.module.compliance.ComplianceCategory> cats = standard.getCategories();
            for (int i = 0; i < cats.size(); i++) {
                com.example.vulnscanner.module.compliance.ComplianceCategory cat = cats.get(i);
                json.append("    {\n");
                json.append("      \"name\": \"").append(escapeJson(cat.getName())).append("\",\n");
                json.append("      \"mappings\": [");
                List<com.example.vulnscanner.module.compliance.ComplianceMapping> maps = cat.getMappings();
                for (int j = 0; j < maps.size(); j++) {
                    json.append("\"").append(escapeJson(maps.get(j).getInternalCategory())).append("\"");
                    if (j < maps.size() - 1)
                        json.append(", ");
                }
                json.append("]\n");
                json.append("    }");
                if (i < cats.size() - 1)
                    json.append(",");
                json.append("\n");
            }
            json.append("  ]\n");
            json.append("}");

            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + standard.getName() + ".json\"")
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(json.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } else {
            return ResponseEntity.badRequest().body("Unsupported type");
        }
    }

    private String escapeCsv(String val) {
        if (val == null)
            return "";
        return val.replace("\"", "\"\"");
    }

    private String escapeXml(String val) {
        if (val == null)
            return "";
        return val.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'",
                "&apos;");
    }

    private String escapeJson(String val) {
        if (val == null)
            return "";
        return val.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // --- API ---

    @PostMapping("/api/rulepacks/upload")
    public ResponseEntity<?> uploadRulepack(@RequestParam("file") MultipartFile file) {
        try {
            PackInfo packInfo = rulepackService.uploadRulepack(file);
            return ResponseEntity.ok(packInfo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/api/rulepacks/{id}")
    public ResponseEntity<?> deleteRulepack(@PathVariable Long id) {
        rulepackService.deleteRulepack(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/compliance/compare")
    @ResponseBody
    public ComplianceDiffDto comparePacks(@RequestParam Long baseId, @RequestParam Long targetId) {
        return comparisonService.compare(baseId, targetId);
    }

    @GetMapping("/api/compliance/history")
    @ResponseBody
    public List<ComplianceComparisonResult> getComparisonHistory() {
        return comparisonService.getAllHistory();
    }

    @DeleteMapping("/api/compliance/history/{id}")
    public ResponseEntity<?> deleteHistory(@PathVariable Long id) {
        comparisonService.deleteHistory(id);
        return ResponseEntity.ok().build();
    }
}