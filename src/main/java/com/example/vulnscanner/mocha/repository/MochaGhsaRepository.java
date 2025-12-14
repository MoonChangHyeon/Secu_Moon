package com.example.vulnscanner.mocha.repository;

import com.example.vulnscanner.mocha.entity.MochaGhsa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MochaGhsaRepository extends JpaRepository<MochaGhsa, String> {
}
