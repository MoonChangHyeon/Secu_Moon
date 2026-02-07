package com.example.vulnscanner.module.compliance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ComplianceMappingRepository extends JpaRepository<ComplianceMapping, Long> {
        @Query("SELECT DISTINCT m.internalCategory FROM ComplianceMapping m WHERE m.internalCategory LIKE %:query%")
        java.util.List<String> findDistinctRuleIds(String query);

        @Query("SELECT c.internalCategory FROM ComplianceMapping c WHERE c.internalCategory IS NOT NULL")
        List<String> findAllInternalCategories();

        // 1. Smart Recommendation (Keyword Search)
        @Query("SELECT DISTINCT m.internalCategory FROM ComplianceMapping m WHERE m.internalCategory LIKE %:keyword%")
        List<String> findRecommendations(String keyword);

        // 2. Unmapped Filter (Excludes rules mapped to specific category)
        @Query("SELECT DISTINCT m.internalCategory FROM ComplianceMapping m " +
                        "WHERE (:query IS NULL OR m.internalCategory LIKE %:query%) " +
                        "AND m.internalCategory NOT IN (" +
                        "  SELECT sub.internalCategory FROM ComplianceMapping sub " +
                        "  WHERE sub.category.id = :categoryId AND sub.isActive = true" +
                        ")")
        List<String> findUnmappedRules(Long categoryId, String query);

        // 3. Language Filter (In-Memory Join Support)
        @Query("SELECT DISTINCT m.internalCategory FROM ComplianceMapping m " +
                        "WHERE m.internalCategory IN :ruleIds " +
                        "AND (:query IS NULL OR m.internalCategory LIKE %:query%)")
        List<String> findRulesByLanguage(List<String> ruleIds, String query);

        // 4. Combined Filter (Language + Unmapped)
        @Query("SELECT DISTINCT m.internalCategory FROM ComplianceMapping m " +
                        "WHERE m.internalCategory IN :ruleIds " +
                        "AND (:query IS NULL OR m.internalCategory LIKE %:query%) " +
                        "AND m.internalCategory NOT IN (" +
                        "  SELECT sub.internalCategory FROM ComplianceMapping sub " +
                        "  WHERE sub.category.id = :categoryId AND sub.isActive = true" +
                        ")")
        List<String> findRulesByLanguageAndUnmapped(List<String> ruleIds, Long categoryId, String query);
}
