package com.example.vulnscanner.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ScanSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String scanDate;
    private String scanTime;
    private Integer totalLoc;
    private Integer fileCount;
    private String scaEngineVersion;
    private String machineName;
    private String requester;

    @OneToOne
    @JoinColumn(name = "analysis_result_id")
    private AnalysisResult analysisResult;
}
