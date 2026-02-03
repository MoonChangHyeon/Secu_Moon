package com.example.vulnscanner.module.compliance;

import com.example.vulnscanner.module.compliance.service.ComplianceDataService;
import com.example.vulnscanner.module.compliance.service.XmlExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ComplianceManagerController {

    private final ComplianceDataService dataService;
    private final com.example.vulnscanner.module.compliance.service.ComplianceLoaderService loaderService;
    private final XmlExportService exportService;
    private final ComplianceStandardRepository standardRepository;

    @GetMapping("/compliance/manager")
    public String managerPage(Model model) {
        // Load System Pack Info
        PackInfo systemPack = dataService.getSystemPackInfo();
        if (systemPack == null) {
            // Suggest Sync
            model.addAttribute("needSync", true);
        } else {
            // Load Standards for Sidebar (Assuming single standard "KISA-49" for now or
            // list)
            List<ComplianceStandard> standards = standardRepository.findByPackInfoId(systemPack.getId());
            model.addAttribute("standards", standards);
            model.addAttribute("packId", systemPack.getId());
        }
        return "compliance/manager";
    }

    @PostMapping("/api/compliance/sync")
    @ResponseBody
    public ResponseEntity<?> syncData() {
        try {
            dataService.syncSystemData();
            return ResponseEntity.ok("Sync Successful");
        } catch (Exception e) {
            log.error("Sync Failed", e);
            return ResponseEntity.internalServerError().body("Sync Failed: " + e.getMessage());
        }
    }

    @PostMapping("/api/compliance/upload")
    @ResponseBody
    public ResponseEntity<?> uploadXml(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        try {
            loaderService.loadKisaMetadata(file.getInputStream());
            return ResponseEntity.ok("Upload and Parse Successful");
        } catch (Exception e) {
            log.error("Upload Failed", e);
            return ResponseEntity.internalServerError().body("Upload Failed: " + e.getMessage());
        }
    }

    @GetMapping("/api/compliance/categories/{categoryId}/mappings")
    @ResponseBody
    public ResponseEntity<?> getMappings(@PathVariable Long categoryId) {
        return ResponseEntity.ok(dataService.getMappingDetails(categoryId));
    }

    @GetMapping("/api/compliance/rules")
    @ResponseBody
    public ResponseEntity<?> searchRules(@RequestParam(required = false, defaultValue = "") String query) {
        return ResponseEntity.ok(dataService.searchRules(query));
    }

    // Bulk Add
    @PostMapping("/api/compliance/categories/{categoryId}/mappings")
    @ResponseBody
    public ResponseEntity<?> addMappings(@PathVariable Long categoryId, @RequestBody List<String> ruleIds) {
        dataService.addMappings(categoryId, ruleIds);
        return ResponseEntity.ok().build();
    }

    // Bulk Remove
    @DeleteMapping("/api/compliance/categories/{categoryId}/mappings")
    @ResponseBody
    public ResponseEntity<?> removeMappings(@PathVariable Long categoryId, @RequestBody List<String> ruleIds) {
        dataService.removeMappings(categoryId, ruleIds);
        return ResponseEntity.ok().build();
    }

    // Export XML
    @GetMapping("/api/compliance/export/{packId}/xml")
    public void exportXml(@PathVariable Long packId, HttpServletResponse response) {
        try {
            response.setContentType("application/xml");
            response.setHeader("Content-Disposition", "attachment; filename=\"kisa_metadata_export.xml\"");
            exportService.exportToXml(packId, response.getOutputStream());
        } catch (Exception e) {
            log.error("Export Failed", e);
            response.setStatus(500);
        }
    }
}
