package com.example.vulnscanner.module.fortify;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/fortify")
@RequiredArgsConstructor
public class FortifyModuleController {

    private final FortifyModuleService fortifyService;

    @GetMapping("/view")
    public String view(Model model) {
        return "fortify/list";
    }

    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<String> uploadData(@RequestParam("file") MultipartFile file) {
        try {
            fortifyService.uploadData(file);
            return ResponseEntity.ok("Upload successful");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/dates")
    @ResponseBody
    public List<String> getAvailableDates() {
        return fortifyService.getAvailableDates();
    }

    @GetMapping("/languages")
    @ResponseBody
    public List<String> getAvailableLanguages(@RequestParam("date") String date) {
        return fortifyService.getAvailableLanguages(date);
    }

    @GetMapping("/weaknesses")
    @ResponseBody
    public List<WeaknessItem> getWeaknesses(@RequestParam("date") String date, @RequestParam("lang") String lang) {
        try {
            return fortifyService.getWeaknessesByLanguage(date, lang);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load data", e);
        }
    }

    @GetMapping("/compare")
    @ResponseBody
    public List<WeaknessItem> compareVersions(@RequestParam("date1") String date1,
            @RequestParam("date2") String date2,
            @RequestParam("lang") String lang) {
        try {
            return fortifyService.compareVersions(date1, date2, lang);
        } catch (IOException e) {
            throw new RuntimeException("Failed to compare data", e);
        }
    }

    @GetMapping("/export")
    public ResponseEntity<?> exportWeaknesses(
            @RequestParam String date,
            @RequestParam String lang,
            @RequestParam(defaultValue = "json") String format) {

        List<WeaknessItem> weaknesses;
        try {
            weaknesses = fortifyService.getWeaknessesByLanguage(date, lang);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Failed to load data: " + e.getMessage());
        }

        if ("json".equalsIgnoreCase(format)) {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(weaknesses);
        } else if ("xml".equalsIgnoreCase(format)) {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_XML)
                    .body(weaknesses);
        } else if ("csv".equalsIgnoreCase(format)) {
            String csvContent = generateCsv(weaknesses);
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"fortify_" + lang + "_" + date + ".csv\"")
                    .body(csvContent);
        } else {
            return ResponseEntity.badRequest().body("Unsupported format: " + format);
        }
    }

    private String generateCsv(List<WeaknessItem> items) {
        StringBuilder sb = new StringBuilder();
        // Header
        sb.append("ID,Title,Abstract,Mapped\n");

        for (WeaknessItem item : items) {
            sb.append(escapeCsv(item.getId())).append(",");
            sb.append(escapeCsv(item.getTitle())).append(",");
            sb.append(escapeCsv(item.getAbstractContent())).append(",");
            sb.append(item.isMapped() ? "Yes" : "No").append("\n");
        }
        return sb.toString();
    }

    private String escapeCsv(Object value) {
        if (value == null)
            return "";
        String str = value.toString();
        if (str.contains(",") || str.contains("\"") || str.contains("\n")) {
            return "\"" + str.replace("\"", "\"\"") + "\"";
        }
        return str;
    }
}
