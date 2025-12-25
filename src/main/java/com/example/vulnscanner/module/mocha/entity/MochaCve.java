package com.example.vulnscanner.module.mocha.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cve")
@Getter
@Setter
public class MochaCve {

    @Id
    @Column(name = "id", length = 30)
    private String id; // CVE ID (e.g., CVE-2023-1234)

    @Column(columnDefinition = "TEXT")
    private String title;

    @Column(name = "description_en", columnDefinition = "TEXT")
    private String descriptionEn;

    @Column(name = "description_kr", columnDefinition = "LONGTEXT")
    private String descriptionKr;

    @Column(name = "vuln_status", length = 30)
    private String vulnStatus;

    @Column(name = "published_date")
    private LocalDateTime publishedDate;

    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    @Column(name = "is_translated")
    private Boolean isTranslated;
}