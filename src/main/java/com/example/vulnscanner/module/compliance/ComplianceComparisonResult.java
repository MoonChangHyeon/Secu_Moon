package com.example.vulnscanner.module.compliance;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "compliance_comparison_results")
@Getter
@Setter
@NoArgsConstructor
public class ComplianceComparisonResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long basePackId;
    private String basePackName;
    private String basePackVersion;

    private Long targetPackId;
    private String targetPackName;
    private String targetPackVersion;

    private LocalDateTime comparedAt;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String resultJson;

    @PrePersist
    public void prePersist() {
        this.comparedAt = LocalDateTime.now();
    }
}
