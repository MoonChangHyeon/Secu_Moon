package com.example.vulnscanner.controller;

import com.example.vulnscanner.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping("/settings")
    public String settings(Model model) {
        Map<String, String> settings = settingsService.getAllSettings();
        model.addAttribute("settings", settings);
        return "settings/index";
    }

    @GetMapping("/settings/users")
    public String userManagement() {
        return "user/list";
    }

    @PostMapping("/settings/update")
    public String updateSettings(@RequestParam Map<String, String> allParams, RedirectAttributes redirectAttributes) {
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            // Filter out non-setting params if any (though @RequestParam Map captures all)
            // We assume all params posted are settings keys
            settingsService.updateSetting(entry.getKey(), entry.getValue());
        }
        redirectAttributes.addFlashAttribute("message", "Settings updated successfully.");
        return "redirect:/settings";
    }

    @PostMapping("/settings/test-connection")
    @org.springframework.web.bind.annotation.ResponseBody
    public java.util.Map<String, Object> testConnection(@RequestParam String url) {
        return settingsService.testApiConnection(url);
    }
}
