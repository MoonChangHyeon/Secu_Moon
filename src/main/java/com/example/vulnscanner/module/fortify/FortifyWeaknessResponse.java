package com.example.vulnscanner.module.fortify;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class FortifyWeaknessResponse {
    private String codelang;

    @JsonProperty("total_count_text")
    private int totalCountText;

    @JsonProperty("fetched_count")
    private int fetchedCount;

    private List<WeaknessItem> weaknesses;
}
