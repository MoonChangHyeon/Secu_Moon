package com.example.vulnscanner.module.compliance;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class PackInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String version;

    private String packId;
    private String name;
    private String locale;

    private LocalDateTime uploadDate;

    @OneToMany(mappedBy = "packInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ComplianceStandard> standards = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.uploadDate = LocalDateTime.now();
    }
}