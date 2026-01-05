package com.example.vulnscanner.module.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/user/templates")
@RequiredArgsConstructor
public class RoleTemplateController {

    private final RoleTemplateService roleTemplateService;

    @GetMapping
    public String listTemplates(Model model) {
        model.addAttribute("templates", roleTemplateService.getAllTemplates());
        return "user/template_list";
    }

    @GetMapping("/create")
    public String createTemplateForm(Model model) {
        model.addAttribute("privileges", roleTemplateService.getAllPrivileges());
        return "user/template_form";
    }

    @PostMapping("/create")
    public String createTemplate(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam(required = false) List<Long> privilegeIds) {

        roleTemplateService.createTemplate(name, description, privilegeIds);
        return "redirect:/user/templates";
    }

    @PostMapping("/{id}/delete")
    public String deleteTemplate(@PathVariable Long id) {
        roleTemplateService.deleteTemplate(id);
        return "redirect:/user/templates";
    }

    @GetMapping("/{id}")
    public String viewTemplate(@PathVariable Long id, Model model) {
        model.addAttribute("template", roleTemplateService.getTemplate(id));
        model.addAttribute("privileges", roleTemplateService.getAllPrivileges());
        return "user/template_form";
    }

    @PostMapping("/{id}/update")
    public String updateTemplate(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam(required = false) List<Long> privilegeIds) {
        roleTemplateService.updateTemplate(id, name, description, privilegeIds);
        return "redirect:/user/templates";
    }
}
