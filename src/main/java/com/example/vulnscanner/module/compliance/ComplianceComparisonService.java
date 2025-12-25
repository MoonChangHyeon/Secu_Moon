package com.example.vulnscanner.module.compliance;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComplianceComparisonService {

    private final PackInfoRepository packInfoRepository;
    private final ComplianceComparisonResultRepository comparisonResultRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public ComplianceDiffDto compare(Long basePackId, Long targetPackId) {
        PackInfo basePack = packInfoRepository.findById(basePackId)
                .orElseThrow(() -> new IllegalArgumentException("Base pack not found"));
        PackInfo targetPack = packInfoRepository.findById(targetPackId)
                .orElseThrow(() -> new IllegalArgumentException("Target pack not found"));

        ComplianceDiffDto diff = new ComplianceDiffDto();
        diff.setBasePackId(basePack.getId());
        diff.setTargetPackId(targetPack.getId());
        diff.setVersionBase(basePack.getVersion());
        diff.setVersionTarget(targetPack.getVersion());

        // Map Standards by ExternalListID (or Name if ID missing) for easier lookup
        Map<String, ComplianceStandard> baseStandards = basePack.getStandards().stream()
                .collect(Collectors.toMap(this::getStandardKey, s -> s));
        Map<String, ComplianceStandard> targetStandards = targetPack.getStandards().stream()
                .collect(Collectors.toMap(this::getStandardKey, s -> s));

        // 1. Check TARGET standards against BASE
        for (Map.Entry<String, ComplianceStandard> entry : targetStandards.entrySet()) {
            String key = entry.getKey();
            ComplianceStandard targetStd = entry.getValue();

            ComplianceDiffDto.StandardDiff stdDiff = new ComplianceDiffDto.StandardDiff();
            stdDiff.setId(targetStd.getId());
            stdDiff.setName(targetStd.getName());
            stdDiff.setExternalListId(targetStd.getExternalListId());

            if (!baseStandards.containsKey(key)) {
                // ADDED in Target
                stdDiff.setDiffType(ComplianceDiffDto.DiffType.ADDED);
                // Also add all categories/mappings as ADDED
                fillCategories(stdDiff, targetStd, ComplianceDiffDto.DiffType.ADDED);
                diff.getStandards().add(stdDiff);
            } else {
                // Exists in BOTH -> Check for modifications in categories
                ComplianceStandard baseStd = baseStandards.get(key);
                boolean changed = compareCategories(stdDiff, baseStd, targetStd);
                if (changed) {
                    stdDiff.setDiffType(ComplianceDiffDto.DiffType.MODIFIED);
                    diff.getStandards().add(stdDiff);
                } else {
                    // SAME - optionally skip adding to diff to keep it clean, or add as SAME
                    // Let's skip valid 'SAME' to reduce noise, or add only if user wants full view.
                    // For now, let's only return diffs.
                }
            }
        }

        // 2. Check BASE standards against TARGET (to find DELETED)
        for (Map.Entry<String, ComplianceStandard> entry : baseStandards.entrySet()) {
            String key = entry.getKey();
            ComplianceStandard baseStd = entry.getValue();

            if (!targetStandards.containsKey(key)) {
                // DELETED in Target
                ComplianceDiffDto.StandardDiff stdDiff = new ComplianceDiffDto.StandardDiff();
                stdDiff.setId(baseStd.getId());
                stdDiff.setName(baseStd.getName());
                stdDiff.setExternalListId(baseStd.getExternalListId());
                stdDiff.setDiffType(ComplianceDiffDto.DiffType.DELETED);
                // Fill categories as DELETED?
                // Maybe just showing standard is deleted is enough, but detailed view might
                // need it.
                diff.getStandards().add(stdDiff);
            }
        }

        // Save History
        saveComparisonResult(diff, basePack, targetPack);

        return diff;
    }

    private void saveComparisonResult(ComplianceDiffDto diff, PackInfo base, PackInfo target) {
        try {
            ComplianceComparisonResult result = new ComplianceComparisonResult();
            result.setBasePackId(base.getId());
            result.setBasePackName(base.getName());
            result.setBasePackVersion(base.getVersion());
            result.setTargetPackId(target.getId());
            result.setTargetPackName(target.getName());
            result.setTargetPackVersion(target.getVersion());
            result.setResultJson(objectMapper.writeValueAsString(diff));
            comparisonResultRepository.save(result);
        } catch (Exception e) {
            // Log error but don't fail the comparison request
            e.printStackTrace();
        }
    }

    @Transactional(readOnly = true)
    public List<ComplianceComparisonResult> getAllHistory() {
        return comparisonResultRepository.findAllByOrderByComparedAtDesc();
    }

    @Transactional
    public void deleteHistory(Long id) {
        comparisonResultRepository.deleteById(id);
    }

    private boolean compareCategories(ComplianceDiffDto.StandardDiff stdDiff, ComplianceStandard baseStd,
            ComplianceStandard targetStd) {
        boolean anyChange = false;
        Map<String, ComplianceCategory> baseCats = baseStd.getCategories().stream()
                .collect(Collectors.toMap(ComplianceCategory::getName, c -> c));
        Map<String, ComplianceCategory> targetCats = targetStd.getCategories().stream()
                .collect(Collectors.toMap(ComplianceCategory::getName, c -> c));

        // Target vs Base
        for (Map.Entry<String, ComplianceCategory> entry : targetCats.entrySet()) {
            String name = entry.getKey();
            ComplianceCategory targetCat = entry.getValue();
            ComplianceDiffDto.CategoryDiff catDiff = new ComplianceDiffDto.CategoryDiff();
            catDiff.setName(name);

            if (!baseCats.containsKey(name)) {
                catDiff.setDiffType(ComplianceDiffDto.DiffType.ADDED);
                fillMappings(catDiff, targetCat, ComplianceDiffDto.DiffType.ADDED);
                stdDiff.getCategories().add(catDiff);
                anyChange = true;
            } else {
                boolean mapChanged = compareMappings(catDiff, baseCats.get(name), targetCat);
                if (mapChanged) {
                    catDiff.setDiffType(ComplianceDiffDto.DiffType.MODIFIED);
                    stdDiff.getCategories().add(catDiff);
                    anyChange = true;
                }
            }
        }

        // Base vs Target (Deleted)
        for (Map.Entry<String, ComplianceCategory> entry : baseCats.entrySet()) {
            String name = entry.getKey();
            if (!targetCats.containsKey(name)) {
                ComplianceDiffDto.CategoryDiff catDiff = new ComplianceDiffDto.CategoryDiff();
                catDiff.setName(name);
                catDiff.setDiffType(ComplianceDiffDto.DiffType.DELETED);
                stdDiff.getCategories().add(catDiff);
                anyChange = true;
            }
        }
        return anyChange;
    }

    private boolean compareMappings(ComplianceDiffDto.CategoryDiff catDiff, ComplianceCategory baseCat,
            ComplianceCategory targetCat) {
        boolean anyChange = false;
        Set<String> baseMappings = baseCat.getMappings().stream().map(ComplianceMapping::getInternalCategory)
                .collect(Collectors.toSet());
        Set<String> targetMappings = targetCat.getMappings().stream().map(ComplianceMapping::getInternalCategory)
                .collect(Collectors.toSet());

        // Added
        for (String m : targetMappings) {
            if (!baseMappings.contains(m)) {
                ComplianceDiffDto.MappingDiff md = new ComplianceDiffDto.MappingDiff();
                md.setInternalCategory(m);
                md.setDiffType(ComplianceDiffDto.DiffType.ADDED);
                catDiff.getMappings().add(md);
                anyChange = true;
            }
        }

        // Deleted
        for (String m : baseMappings) {
            if (!targetMappings.contains(m)) {
                ComplianceDiffDto.MappingDiff md = new ComplianceDiffDto.MappingDiff();
                md.setInternalCategory(m);
                md.setDiffType(ComplianceDiffDto.DiffType.DELETED);
                catDiff.getMappings().add(md);
                anyChange = true;
            }
        }
        return anyChange;
    }

    private void fillCategories(ComplianceDiffDto.StandardDiff stdDiff, ComplianceStandard standard,
            ComplianceDiffDto.DiffType type) {
        for (ComplianceCategory cat : standard.getCategories()) {
            ComplianceDiffDto.CategoryDiff catDiff = new ComplianceDiffDto.CategoryDiff();
            catDiff.setName(cat.getName());
            catDiff.setDiffType(type);
            fillMappings(catDiff, cat, type);
            stdDiff.getCategories().add(catDiff);
        }
    }

    private void fillMappings(ComplianceDiffDto.CategoryDiff catDiff, ComplianceCategory category,
            ComplianceDiffDto.DiffType type) {
        for (ComplianceMapping mapping : category.getMappings()) {
            ComplianceDiffDto.MappingDiff md = new ComplianceDiffDto.MappingDiff();
            md.setInternalCategory(mapping.getInternalCategory());
            md.setDiffType(type);
            catDiff.getMappings().add(md);
        }
    }

    private String getStandardKey(ComplianceStandard s) {
        return s.getExternalListId() != null ? s.getExternalListId() : s.getName();
    }
}