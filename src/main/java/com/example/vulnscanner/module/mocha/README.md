# Mocha Module

`com.example.vulnscanner.module.mocha` 패키지는 외부 취약점 데이터베이스("Mocha")와의 연동을 위한 엔티티 및 리포지토리를 정의합니다.

## 🔑 주요 역할 (Key Responsibilities)

1.  **외부 데이터 매핑**
    - 별도의 DataSource(`MochaDbConfig`)를 통해 외부 DB에 접근
    - CVE, GHSA 등 공신력 있는 취약점 정보 조회
2.  **데이터 참조**
    - `SbomService` 등에서 취약점 정보를 풍부하게(Enrich) 만들기 위해 참조됨

## 📄 주요 클래스 (Key Classes)

- **Entity**: `MochaCve`, `MochaGhsa`, `MochaSpdxLicense`
- **Repository**: `MochaCveRepository`, `MochaGhsaRepository`, `MochaSpdxLicenseRepository`
