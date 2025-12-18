# Mocha Module (보안 데이터)

## 개요
`mocha` 패키지는 `mocha_dev` (Secondary Database)와 연동하여 글로벌 보안 취약점 데이터 및 라이선스 정보를 조회하는 역할을 담당합니다.
이 데이터는 읽기 전용(Read-Only)으로 사용되며, 메인 분석 결과(`SbomResult`)를 보강(Enrichment)하는 데 활용됩니다.

## 주요 엔티티 (Entity)
- **[MochaCve](entity/MochaCve.java)**: CVE (Common Vulnerabilities and Exposures) 마스터 데이터.
- **[MochaGhsa](entity/MochaGhsa.java)**: GHSA (GitHub Security Advisories) 마스터 데이터.
- **[MochaSpdxLicense](entity/MochaSpdxLicense.java)**: SPDX 표준 라이선스 정보 및 상세 설명.

## 리포지토리 (Repository)
- **[MochaCveRepository](repository/MochaCveRepository.java)**, **[MochaGhsaRepository](repository/MochaGhsaRepository.java)**, **[MochaSpdxLicenseRepository](repository/MochaSpdxLicenseRepository.java)**
  - JPA를 통해 `mocha_dev` 데이터베이스에서 보안 데이터를 조회하는 인터페이스입니다.
