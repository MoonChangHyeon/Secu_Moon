package com.example.vulnscanner.module.mocha.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Table(name = "spdx_licenses")
@Getter
@Setter
public class MochaSpdxLicense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "license_id", length = 100, nullable = false)
    private String licenseId; // SPDX ID

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "is_osi_approved", length = 20)
    private String isOsiApproved;

    @Column(name = "is_deprecated_license_id", length = 20)
    private String isDeprecatedLicenseId;

    @Column(name = "details_url", length = 512)
    private String detailsUrl;

    @Column(name = "summary_kr", columnDefinition = "LONGTEXT")
    private String summaryKr;

    @Column(name = "severity")
    private String severity; // LOW, MEDIUM, HIGH
}