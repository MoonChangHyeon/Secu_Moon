package com.example.vulnscanner.module.compliance.dto;

import lombok.Data;
import java.util.List;

@Data
public class ComplianceManagerDto {
    private Long categoryId;
    private String categoryName;
    private List<MappingInfo> mappings;
    private List<RuleInfo> allRules; // For Source column

    @Data
    public static class MappingInfo {
        private Long mappingId;
        private String ruleId; // Internal Category
        private boolean isCustom;
        private boolean isActive;
    }

    @Data
    public static class RuleInfo {
        private String ruleId;
        private String ruleName; // If we populate this
    }
}
