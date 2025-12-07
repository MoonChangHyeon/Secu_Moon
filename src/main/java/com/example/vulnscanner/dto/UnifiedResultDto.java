package com.example.vulnscanner.dto;

import java.time.LocalDateTime;

public class UnifiedResultDto {
    private Long id;
    private String type; // "SAST" or "SBOM"
    private String buildId;
    private LocalDateTime scanDate;
    private String status;
    private String requester;

    private String logs;
    private String fprPath;
    private String reportPdfPath;
    private String reportXmlPath;

    public UnifiedResultDto(Long id, String type, String buildId, LocalDateTime scanDate, String status,
            String requester, String logs, String fprPath, String reportPdfPath, String reportXmlPath) {
        this.id = id;
        this.type = type;
        this.buildId = buildId;
        this.scanDate = scanDate;
        this.status = status;
        this.requester = requester;
        this.logs = logs;
        this.fprPath = fprPath;
        this.reportPdfPath = reportPdfPath;
        this.reportXmlPath = reportXmlPath;
    }

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getBuildId() {
        return buildId;
    }

    public LocalDateTime getScanDate() {
        return scanDate;
    }

    public String getStatus() {
        return status;
    }

    public String getRequester() {
        return requester;
    }

    public String getLogs() {
        return logs;
    }

    public String getFprPath() {
        return fprPath;
    }

    public String getReportPdfPath() {
        return reportPdfPath;
    }

    public String getReportXmlPath() {
        return reportXmlPath;
    }
}
