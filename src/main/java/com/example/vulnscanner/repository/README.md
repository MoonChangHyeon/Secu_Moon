# Repository Module (데이터 접근 계층)

## 목차 (Table of Contents)
- [리포지토리 분류 (Repository Classification)](#리포지토리-분류-repository-classification)
    - [1. Primary Repositories](#1-primary-repositories-comexamplevulnscannerrepository)
    - [2. Mocha Repositories](#2-mocha-repositories-comexamplevulnscannermocharepository)
- [설정 주의사항](#설정-주의사항)

---

이 디렉토리는 Spring Data JPA를 사용하여 데이터베이스와 상호작용하는 인터페이스들을 포함합니다.
**Dual DB 전략**에 따라 두 개의 패키지로 구분되어 리포지토리가 관리됩니다.

## 리포지토리 분류 (Repository Classification)

### 1. Primary Repositories (`com.example.vulnscanner.repository`)
`vulnscanner_db` (Primary)에 접근하여 CRUD를 수행합니다.
- **SbomResultRepository**: 분석 결과 메타데이터 조회/저장.
- **SbomComponentRepository**: 컴포넌트 정보 관리.
- **AnalysisResultRepository**: SAST 분석 결과 관리.
- **UsersRepository**: 사용자 계정 관리.
- **SystemSettingRepository**: 시스템 설정값 관리.

### 2. Mocha Repositories (`com.example.vulnscanner.mocha.repository`)
`mocha_dev` (Secondary)에 접근하여 마스터 데이터를 조회(Read-Only)합니다.
이 리포지토리들은 `SbomService`에서 데이터 보강(Enrichment) 목적으로만 호출됩니다.

- **[MochaCveRepository](../mocha/repository/MochaCveRepository.java)**
  - CVE ID(`CVE-xxxx-xxxx`)로 CVE 상세 정보(Title, Description) 조회.
- **[MochaGhsaRepository](../mocha/repository/MochaGhsaRepository.java)**
  - GHSA ID(`GHSA-xxxx-xxxx`)로 GitHub Advisory 정보 조회.
- **[MochaSpdxLicenseRepository](../mocha/repository/MochaSpdxLicenseRepository.java)**
  - 라이선스 ID(`MIT`, `Apache-2.0` 등)로 라이선스 상세 및 위험도 조회.

## 설정 주의사항
각 리포지토리 패키지는 `DataSourceConfig`에서 `@EnableJpaRepositories` 어노테이션을 통해 서로 다른 `EntityManagerFactory`와 연결되도록 정확히 설정되어야 합니다.
- **Primary Repo** -> `primaryEntityManagerFactory`
- **Mocha Repo** -> `mochaEntityManagerFactory`
