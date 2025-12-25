package com.example.vulnscanner.module.compliance;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ComplianceComparisonResultRepository extends JpaRepository<ComplianceComparisonResult, Long> {
    List<ComplianceComparisonResult> findAllByOrderByComparedAtDesc();
}
