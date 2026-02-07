package com.example.vulnscanner.module.compliance.service;

import com.example.vulnscanner.module.compliance.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComplianceDataService {

    private final ComplianceLoaderService loaderService;
    private final ComplianceCategoryRepository categoryRepository;
    private final ComplianceMappingRepository mappingRepository;
    private final PackInfoRepository packInfoRepository;

    @Transactional
    public void syncSystemData() {
        // Define path to data/Client - ideally from config property
        String clientDataPath = System.getProperty("user.dir") + "/data/Client";
        loaderService.loadSystemData(clientDataPath);
    }

    public PackInfo getSystemPackInfo() {
        return packInfoRepository.findByName("KISA 49 (System)").orElse(null);
    }

    @Transactional(readOnly = true)
    public List<ComplianceCategory> getCategories(Long standardId) {
        // We might want to filter active ones if categories themselves can be
        // deactivated?
        // For now just return all associated with standard
        // Assuming we can get standard by ID, but repository method was simple
        // Let's rely on standard logic
        return null; // TODO: Implement if needed for UI tree
    }

    @Transactional
    public void addMappings(Long categoryId, List<String> ruleIds) {
        ComplianceCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Category ID"));

        for (String ruleId : ruleIds) {
            // Check existing
            ComplianceMapping existing = category.getMappings().stream()
                    .filter(m -> m.getInternalCategory().equals(ruleId))
                    .findFirst()
                    .orElse(null);

            if (existing != null) {
                if (!existing.isActive()) {
                    existing.setActive(true); // Reactivate soft-deleted
                }
                // If it was system (custom=false) or already custom, we ensure it's active.
                // If we want to mark it as customized override, we could set isCustom=true.
                // Design says: "Add" sets isCustom=true.
                // If it was a deleted system mapping, we are "Restoring" it.
                // If it's a completely new mapping, isCustom=true.
            } else {
                ComplianceMapping mapping = new ComplianceMapping();
                mapping.setCategory(category);
                mapping.setInternalCategory(ruleId);
                mapping.setExternalCategoryName(category.getName()); // Keep redundant field or deprecate
                mapping.setCustom(true);
                mapping.setActive(true);
                category.getMappings().add(mapping);
            }
        }
        categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public com.example.vulnscanner.module.compliance.dto.ComplianceManagerDto getMappingDetails(Long categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("Category ID cannot be null");
        }
        ComplianceCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Category ID"));

        com.example.vulnscanner.module.compliance.dto.ComplianceManagerDto dto = new com.example.vulnscanner.module.compliance.dto.ComplianceManagerDto();
        dto.setCategoryId(categoryId);
        dto.setCategoryName(category.getName());

        List<com.example.vulnscanner.module.compliance.dto.ComplianceManagerDto.MappingInfo> mappingInfos = category
                .getMappings().stream()
                .map(m -> {
                    com.example.vulnscanner.module.compliance.dto.ComplianceManagerDto.MappingInfo info = new com.example.vulnscanner.module.compliance.dto.ComplianceManagerDto.MappingInfo();
                    info.setMappingId(m.getId());
                    info.setRuleId(m.getInternalCategory());
                    info.setCustom(m.isCustom());
                    info.setActive(m.isActive());
                    return info;
                })
                .collect(Collectors.toList());

        dto.setMappings(mappingInfos);
        return dto;
    }

    private final com.example.vulnscanner.module.fortify.FortifyModuleService fortifyModuleService;

    @Transactional(readOnly = true)
    public List<String> searchRules(String query) {
        return searchRules(query, null, false, null);
    }

    @Transactional(readOnly = true)
    public List<String> searchRules(String query, String language, boolean unmappedOnly, Long categoryId) {
        if (query == null)
            query = "";

        // 1. Language FIlter
        List<String> languageRuleIds = null;
        if (language != null && !language.isEmpty()) {
            languageRuleIds = fortifyModuleService.getRuleIdsByLanguage(language);
            if (languageRuleIds.isEmpty()) {
                return java.util.Collections.emptyList(); // No rules for this language
            }
        }

        // 2. Dispatch Query
        if (languageRuleIds != null) {
            // Case A: Language + Unmapped
            if (unmappedOnly && categoryId != null) {
                return mappingRepository.findRulesByLanguageAndUnmapped(languageRuleIds, categoryId, query);
            }
            // Case B: Language only
            return mappingRepository.findRulesByLanguage(languageRuleIds, query);
        } else {
            // Case C: Unmapped only
            if (unmappedOnly && categoryId != null) {
                return mappingRepository.findUnmappedRules(categoryId, query);
            }
            // Case D: Default (Query only)
            return mappingRepository.findDistinctRuleIds(query);
        }
    }

    public List<String> getAvailableLanguages() {
        // Get languages from the latest Fortify data
        String latestDate = null;
        List<String> dates = fortifyModuleService.getAvailableDates();
        if (!dates.isEmpty())
            latestDate = dates.get(0);

        if (latestDate != null) {
            return fortifyModuleService.getAvailableLanguages(latestDate);
        }
        return java.util.Collections.emptyList();
    }

    @Transactional(readOnly = true)
    public List<String> getRecommendations(Long categoryId) {
        ComplianceCategory category = categoryRepository.findById(categoryId).orElse(null);
        if (category == null)
            return java.util.Collections.emptyList();

        // Simple Keyword Extraction Strategy
        // e.g. "01.01. SQL 삽입" -> "SQL", "Injection"
        // e.g. "Cross-Site Scripting" -> "Cross-Site", "Scripting", "XSS"

        String name = category.getName();
        String keyword = extractKeyword(name);

        if (keyword.isEmpty())
            return java.util.Collections.emptyList();

        return mappingRepository.findRecommendations(keyword);
    }

    private String extractKeyword(String categoryName) {
        if (categoryName == null)
            return "";
        String upperName = categoryName.toUpperCase();

        // Logic to extract meaningful keyword
        if (upperName.contains("SQL"))
            return "SQL";
        if (upperName.contains("XSS") || categoryName.contains("크로스사이트"))
            return "Cross-Site";
        if (upperName.contains("BUFFER"))
            return "Buffer";
        if (categoryName.contains("경로 조작"))
            return "Path";
        if (categoryName.contains("운영체제"))
            return "Command";

        return "";
    }

    @Transactional
    public void removeMappings(Long categoryId, List<String> ruleIds) {
        ComplianceCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Category ID"));

        // Use Iterator to safely remove from collection while iterating if needed
        List<ComplianceMapping> mappings = category.getMappings();

        for (String ruleId : ruleIds) {
            java.util.Iterator<ComplianceMapping> iterator = mappings.iterator();
            while (iterator.hasNext()) {
                ComplianceMapping mapping = iterator.next();
                if (mapping.getInternalCategory().equals(ruleId) && mapping.isActive()) {
                    if (mapping.isCustom()) {
                        // Hard delete custom mappings
                        iterator.remove(); // Removes from list
                        mappingRepository.delete(mapping); // Removes from DB
                    } else {
                        // Soft delete system mappings
                        mapping.setActive(false);
                    }
                    break; // Found and processed, move to next ruleId
                }
            }
        }
        categoryRepository.save(category);
    }
}
