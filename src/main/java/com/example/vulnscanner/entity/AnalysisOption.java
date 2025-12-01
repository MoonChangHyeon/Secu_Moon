package com.example.vulnscanner.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class AnalysisOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String buildId;
    private boolean clean = true;
    private boolean translate = true;
    private boolean scan = true;
    private boolean report;

    // Translation Options
    private String classpath;
    private String jdkVersion;
    private String excludePattern;
    private String sourcePath;

    // Scan Options
    private String scanPolicy;
    private boolean quickScan;
    private String analyzers;

    // Report Options
    private String outputFormat = "pdf"; // Default to PDF
    private String reportTemplate;
    private boolean showSuppressed;
    private boolean disableSourceBundling;
    private boolean debugVerbose;
}
