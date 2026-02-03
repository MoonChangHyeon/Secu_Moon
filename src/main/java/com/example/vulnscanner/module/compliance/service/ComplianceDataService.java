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

    @Transactional(readOnly = true)
    public List<String> searchRules(String query) {
        if (query == null)
            query = "";
        return mappingRepository.findDistinctRuleIds(query);
    }

    @Transactional
    public void removeMappings(Long categoryId, List<String> ruleIds) {
        ComplianceCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Category ID"));

        for (String ruleId : ruleIds) {
            category.getMappings().stream()
                    .filter(m -> m.getInternalCategory().equals(ruleId) && m.isActive())
                    .findFirst()
                    .ifPresent(mapping -> {
                        if (mapping.isCustom()) {
                            // Hard delete custom mappings? Or soft delete?
                            // Design says: "is_custom=true이면 물리 삭제" (Physical Delete)
                            category.getMappings().remove(mapping);
                            mappingRepository.delete(mapping);
                        } else {
                            // Soft delete system mappings
                            mapping.setActive(false);
                            // Design says: "is_custom=false이면 is_active=false"
                        }
                    });
        }
        categoryRepository.save(category); // Might need to save category if we removed from list
    }
}
