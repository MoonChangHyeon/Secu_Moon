package com.example.vulnscanner.controller;

import com.example.vulnscanner.service.FortifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequiredArgsConstructor
public class FortifyController {

    private final FortifyService fortifyService;
    private final Map<String, StringBuilder> logs = new ConcurrentHashMap<>();

    @GetMapping("/fortify")
    public String index() {
        return "index";
    }

    @PostMapping("/api/clean")
    @ResponseBody
    public ResponseEntity<String> clean(@RequestParam String buildId) {
        logs.put(buildId, new StringBuilder("Starting Clean...\n"));
        fortifyService.clean(buildId).thenAccept(output -> appendLog(buildId, output));
        return ResponseEntity.ok("Clean started");
    }

    @PostMapping("/api/translate")
    @ResponseBody
    public ResponseEntity<String> translate(@RequestParam String buildId, @RequestParam String path) {
        appendLog(buildId, "\nStarting Translation for " + path + "...\n");
        fortifyService.translate(buildId, path).thenAccept(output -> appendLog(buildId, output));
        return ResponseEntity.ok("Translation started");
    }

    @PostMapping("/api/scan")
    @ResponseBody
    public ResponseEntity<String> scan(@RequestParam String buildId) {
        appendLog(buildId, "\nStarting Scan...\n");
        fortifyService.scan(buildId, buildId + "_result").thenAccept(output -> appendLog(buildId, output));
        return ResponseEntity.ok("Scan started");
    }

    @GetMapping("/api/logs")
    @ResponseBody
    public String getLogs(@RequestParam String buildId) {
        return logs.getOrDefault(buildId, new StringBuilder()).toString();
    }

    private void appendLog(String buildId, String text) {
        logs.computeIfAbsent(buildId, k -> new StringBuilder()).append(text);
    }
}
