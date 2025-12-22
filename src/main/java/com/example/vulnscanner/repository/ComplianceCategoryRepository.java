package com.example.vulnscanner.repository;

import com.example.vulnscanner.entity.ComplianceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplianceCategoryRepository extends JpaRepository<ComplianceCategory, Long> {
}
