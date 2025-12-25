package com.example.vulnscanner.module.compliance;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ComplianceMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String internalCategory; // Fortify Category
    private String externalCategoryName; // Mapping target (redundant but useful)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compliance_category_id")
    private ComplianceCategory category;
}