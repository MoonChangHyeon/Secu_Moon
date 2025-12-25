package com.example.vulnscanner.module.sbom;

import com.example.vulnscanner.module.analysis.AnalysisOption;
import com.example.vulnscanner.module.user.User;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class SbomResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    private AnalysisOption analysisOption;

    private LocalDateTime scanDate;
    private String status; // SUCCESS, FAILED, RUNNING

    // Summary Counts
    private Integer vulnerabilitiesCount;
    private Integer licensesCount;
    private Integer componentsCount;

    private String jobId; // AI_SBOM API Job ID
    private String requester; // 요청자 (User ID/Name)

    @Lob
    @Column(columnDefinition = "TEXT")
    private String logs;

    private String resultJsonPath; // Path to the raw JSON result file
    private String sbomFilePath; // Uploaded or generated SBOM file path
    private String reportPath; // Path to the generated report

    @OneToMany(mappedBy = "sbomResult", cascade = CascadeType.ALL, orphanRemoval = true)
    @lombok.ToString.Exclude
    private List<SbomComponent> components;
}