package com.example.vulnscanner.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ComplianceDiffDto {
    private String versionBase;
    private String versionTarget;
    private List<StandardDiff> standards = new ArrayList<>();

    @Getter
    @Setter
    public static class StandardDiff {
        private String name;
        private String externalListId;
        private DiffType diffType; // ADDED, DELETED, MODIFIED, SAME
        private List<CategoryDiff> categories = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class CategoryDiff {
        private String name;
        private DiffType diffType;
        private List<MappingDiff> mappings = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class MappingDiff {
        private String internalCategory;
        private DiffType diffType;
    }

    public enum DiffType {
        ADDED, DELETED, MODIFIED, SAME
    }
}
