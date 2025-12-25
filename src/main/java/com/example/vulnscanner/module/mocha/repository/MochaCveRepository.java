package com.example.vulnscanner.module.mocha.repository;

import com.example.vulnscanner.module.mocha.entity.MochaCve;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MochaCveRepository extends JpaRepository<MochaCve, String> {
}