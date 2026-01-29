package com.example.vulnscanner.module.compliance;

import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CompliancePackDetailsDto {
    private Long id;
    private String name;
    private String version;
    private List<StandardDto> standards = new ArrayList<>();

    @Getter
    @Setter
    public static class StandardDto {
        private Long id;
        private String name;
        private String description;
        private String externalListId;
        private List<CategoryDto> categories = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class CategoryDto {
        private Long id;
        private String name;
        private String description;
        private List<MappingDto> mappings = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class MappingDto {
        private Long id;
        private String internalCategory;
    }
}
