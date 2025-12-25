package com.example.vulnscanner.module.settings;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemSetting {

    @Id
    private String keyName;
    private String value;
    private String description;
}