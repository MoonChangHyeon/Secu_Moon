package com.example.vulnscanner.module.analysis;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class AnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    private AnalysisOption analysisOption;

    private String fprPath;
    private LocalDateTime scanDate;
    private String status; // SUCCESS, FAILED, RUNNING
    @Lob
    @Column(columnDefinition = "TEXT")
    private String logs;
    private String requester; // 분석 요청자
    private String buildLogFilePath;
    private String scanLogFilePath;
    private String sourceFilePath; // 업로드된 소스 파일 경로
    private String reportPdfPath; // PDF 보고서 파일 경로
    private String reportXmlPath; // XML 보고서 파일 경로

    @OneToOne(mappedBy = "analysisResult", cascade = CascadeType.ALL, orphanRemoval = true)
    private ScanSummary scanSummary;

    @OneToMany(mappedBy = "analysisResult", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Vulnerability> vulnerabilities;

    public long getCriticalCount() {
        if (vulnerabilities == null)
            return 0;
        return vulnerabilities.stream().filter(v -> "Critical".equalsIgnoreCase(v.getPriority())).count();
    }

    public long getHighCount() {
        if (vulnerabilities == null)
            return 0;
        return vulnerabilities.stream().filter(v -> "High".equalsIgnoreCase(v.getPriority())).count();
    }

    public long getMediumCount() {
        if (vulnerabilities == null)
            return 0;
        return vulnerabilities.stream().filter(v -> "Medium".equalsIgnoreCase(v.getPriority())).count();
    }

    public long getLowCount() {
        if (vulnerabilities == null)
            return 0;
        return vulnerabilities.stream().filter(v -> "Low".equalsIgnoreCase(v.getPriority())).count();
    }
}