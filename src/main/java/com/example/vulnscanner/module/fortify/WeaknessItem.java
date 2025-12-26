package com.example.vulnscanner.module.fortify;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeaknessItem {
    private String title;
    private String url;
    private String id;

    @JsonProperty("abstract")
    private String abstractContent;

    @JsonProperty("supported_languages")
    private List<String> supportedLanguages;

    private boolean isMapped;
    private String diffStatus; // NEW, REMOVED, MODIFIED, SAME
}
