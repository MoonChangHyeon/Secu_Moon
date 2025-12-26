package com.example.vulnscanner.module.compliance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplianceMappingRepository extends JpaRepository<ComplianceMapping, Long> {
<<<<<<< HEAD:src/main/java/com/example/vulnscanner/module/compliance/ComplianceMappingRepository.java
}
=======
    @org.springframework.data.jpa.repository.Query("SELECT c.internalCategory FROM ComplianceMapping c WHERE c.internalCategory IS NOT NULL")
    java.util.List<String> findAllInternalCategories();
}
>>>>>>> 2512W04:src/main/java/com/example/vulnscanner/repository/ComplianceMappingRepository.java
