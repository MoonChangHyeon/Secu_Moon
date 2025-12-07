package com.example.vulnscanner.repository;

import com.example.vulnscanner.entity.SbomResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SbomRepository extends JpaRepository<SbomResult, Long> {
    List<SbomResult> findTop5ByOrderByScanDateDesc();
}
