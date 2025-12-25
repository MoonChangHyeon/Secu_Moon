package com.example.vulnscanner.module.mocha.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "ghsa_advisory")
@Getter
@Setter
public class MochaGhsa {

    @Id
    @Column(name = "ghsa_id", nullable = false, unique = true)
    private String ghsaId;

    @Column(name = "title")
    private String title;

    @Column(name = "summary_en", columnDefinition = "LONGTEXT")
    private String summaryEn;

    @Column(name = "summary_kr", columnDefinition = "LONGTEXT")
    private String summaryKr;

    @Column(name = "severity")
    private String severity;

    @Column(name = "cvss_vector")
    private String cvssVector;

    @Column(name = "cvss_type")
    private String cvssType;

    @Column(name = "cwe_ids")
    private String cweIds;

    @Column(name = "github_reviewed")
    private Boolean githubReviewed;

    @Column(name = "published")
    private LocalDateTime published;

    @Column(name = "modified")
    private LocalDateTime modified;
}