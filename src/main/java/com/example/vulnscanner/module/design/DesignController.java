package com.example.vulnscanner.module.design;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DesignController {

    @GetMapping("/design/dashboard")
    public String dashboardDesign() {
        return "dashboard_stitch";
    }
}
