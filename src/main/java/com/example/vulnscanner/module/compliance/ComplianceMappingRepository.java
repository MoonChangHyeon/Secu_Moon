package com.example.vulnscanner.module.compliance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ComplianceMappingRepository extends JpaRepository<ComplianceMapping, Long> {
    @Query("SELECT c.internalCategory FROM ComplianceMapping c WHERE c.internalCategory IS NOT NULL")
    List<String> findAllInternalCategories();
}
