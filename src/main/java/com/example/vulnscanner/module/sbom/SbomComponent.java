package com.example.vulnscanner.module.sbom;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class SbomComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sbom_result_id")
    @lombok.ToString.Exclude
    private SbomResult sbomResult;

    private String name;
    private String version;
    private String type; // library, framework, application, etc.
    private String purl; // Package URL
    private String license;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "component", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<SbomVulnerability> vulnerabilities = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "component", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<SbomLicense> licenses = new java.util.ArrayList<>();
}