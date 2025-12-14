# Entity Module (도메인 모델)

## 목차 (Table of Contents)
- [패키지 구조 (Package Structure)](#패키지-구조-package-structure)
    - [1. Primary Entities](#1-primary-entities-comexamplevulnscannerentity)
    - [2. Mocha Entities](#2-mocha-entities-comexamplevulnscannermochaentity)

---

이 디렉토리는 데이터베이스 테이블과 매핑되는 JPA Entity 클래스들을 포함합니다.
**Dual DB 전략**에 따라 엔티티 패키지가 물리적으로 분리되어 있습니다.

## 패키지 구조 (Package Structure)

### 1. Primary Entities (`com.example.vulnscanner.entity`)
애플리케이션의 핵심 비즈니스 로직에서 생성되고 관리되는 데이터입니다. (`vulnscanner_db`)

- **분석 결과 관련**
  - **[SbomResult.java](SbomResult.java)**: SBOM 분석 요청 및 결과 헤더 정보.
  - **[SbomComponent.java](SbomComponent.java)**: 분석된 라이브러리/컴포넌트 정보.
  - **[SbomVulnerability.java](SbomVulnerability.java)**: (New) `mocha_dev`에서 보강된 취약점 상세 정보 저장.
  - **[SbomLicense.java](SbomLicense.java)**: (New) `mocha_dev`에서 보강된 라이선스 상세 정보 저장.
  - **AnalysisResult.java**: (Legacy) 소스 코드 분석(SAST) 결과.

- **사용자 및 설정**
  - **Users.java**: 사용자 계정 정보.
  - **SystemSetting.java**: 시스템 전역 설정값.

### 2. Mocha Entities (`com.example.vulnscanner.mocha.entity`)
외부 보안 취약점 마스터 데이터를 참조하기 위한 엔티티입니다. (`mocha_dev`)
이 엔티티들은 주로 **읽기 전용(Read-Only)**으로 사용되며, 분석 시점에 데이터를 조회하여 Primary Entity로 복사하는 데 사용됩니다.

- **[MochaCve.java](../mocha/entity/MochaCve.java)**
  - `cve` 테이블 매핑. 표준 CVE 정보 (Title, Description, Status 등).
- **[MochaGhsa.java](../mocha/entity/MochaGhsa.java)**
  - `ghsa_advisory` 테이블 매핑. GitHub Security Advisory 정보.
- **[MochaSpdxLicense.java](../mocha/entity/MochaSpdxLicense.java)**
  - `spdx_licenses` 테이블 매핑. 오픈소스 라이선스 상세 및 위험도 정보.
