package com.example.vulnscanner.entity;

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
    private SbomResult sbomResult;

    private String name;
    private String version;
    private String type; // library, framework, application, etc.
    private String purl; // Package URL
    private String license;

    @Column(columnDefinition = "TEXT")
    private String description;
}
