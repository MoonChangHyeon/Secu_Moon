package com.example.vulnscanner.module.compliance;

import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ComplianceDiffDto {
    private Long basePackId;
    private Long targetPackId;
    private String versionBase;
    private String versionTarget;
    private List<StandardDiff> standards = new ArrayList<>();

    @Getter
    @Setter
    public static class StandardDiff {
        private Long id; // Standard ID
        private String name;
        private String externalListId;
        private DiffType diffType; // ADDED, DELETED, MODIFIED, SAME
        private List<CategoryDiff> categories = new ArrayList<>();

        public long getAddedCount() {
            return categories.stream()
                    .filter(c -> c.getDiffType() == DiffType.ADDED).count() +
                    categories.stream()
                            .flatMap(c -> c.getMappings().stream())
                            .filter(m -> m.getDiffType() == DiffType.ADDED).count();
        }

        public long getDeletedCount() {
            return categories.stream()
                    .filter(c -> c.getDiffType() == DiffType.DELETED).count() +
                    categories.stream()
                            .flatMap(c -> c.getMappings().stream())
                            .filter(m -> m.getDiffType() == DiffType.DELETED).count();
        }

        public long getModifiedCount() {
            return categories.stream()
                    .filter(c -> c.getDiffType() == DiffType.MODIFIED).count() +
                    categories.stream()
                            .flatMap(c -> c.getMappings().stream())
                            .filter(m -> m.getDiffType() == DiffType.MODIFIED).count();
        }
    }

    @Getter
    @Setter
    public static class CategoryDiff {
        private String name;
        private DiffType diffType;
        private List<MappingDiff> mappings = new ArrayList<>();

        public java.util.Map<String, List<MappingDiff>> getGroupedMappings() {
            return mappings.stream().collect(java.util.stream.Collectors.groupingBy(m -> {
                String ic = m.getInternalCategory();
                if (ic == null)
                    return "Others";
                int idx = ic.indexOf(':');
                return (idx > 0) ? ic.substring(0, idx).trim() : "General";
            }, java.util.TreeMap::new, java.util.stream.Collectors.toList()));
        }
    }

    @Getter
    @Setter
    public static class MappingDiff {
        private String internalCategory;
        private DiffType diffType;

        // Helper to get only the sub-category name if grouped
        public String getSubCategoryName() {
            if (internalCategory == null)
                return "";
            int idx = internalCategory.indexOf(':');
            return (idx > 0) ? internalCategory.substring(idx + 1).trim() : internalCategory;
        }
    }

    public enum DiffType {
        ADDED, DELETED, MODIFIED, SAME
    }
}