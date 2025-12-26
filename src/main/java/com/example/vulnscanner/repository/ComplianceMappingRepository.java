package com.example.vulnscanner.repository;

import com.example.vulnscanner.entity.ComplianceMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplianceMappingRepository extends JpaRepository<ComplianceMapping, Long> {
    @org.springframework.data.jpa.repository.Query("SELECT c.internalCategory FROM ComplianceMapping c WHERE c.internalCategory IS NOT NULL")
    java.util.List<String> findAllInternalCategories();
}
