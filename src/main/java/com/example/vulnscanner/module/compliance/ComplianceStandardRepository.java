package com.example.vulnscanner.module.compliance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplianceStandardRepository extends JpaRepository<ComplianceStandard, Long> {
    List<ComplianceStandard> findByPackInfoId(Long packInfoId);

    Page<ComplianceStandard> findByPackInfoId(Long packInfoId, Pageable pageable);
}