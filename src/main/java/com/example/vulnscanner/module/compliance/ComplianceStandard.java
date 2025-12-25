package com.example.vulnscanner.module.compliance;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class ComplianceStandard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String externalListId;
    private String name;

    @Column(length = 4000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pack_info_id")
    private PackInfo packInfo;

    @OneToMany(mappedBy = "standard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ComplianceCategory> categories = new ArrayList<>();
}