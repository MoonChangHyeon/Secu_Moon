package com.example.vulnscanner.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Table(name = "sbom_license")
@Getter
@Setter
public class SbomLicense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_id")
    private SbomComponent component;

    @Column(name = "license_id")
    private String licenseId; // SPDX ID

    @Column(name = "name")
    private String name;

    @Column(name = "severity")
    private String severity; // Risk level from policy/master data

    @Column(name = "url")
    private String url;
}
