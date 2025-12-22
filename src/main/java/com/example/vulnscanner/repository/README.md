# Repositories (Data Access Layer)

이 패키지는 Spring Data JPA를 기반으로 데이터베이스 CRUD 작업을 수행하는 인터페이스들을 포함합니다.

## 📋 주요 리포지토리 (Key Repositories)

### 1. Compliance (규정 준수) 관련 <!-- New -->
- **`PackInfoRepository`**: 업로드된 룰팩 버전 정보(`PackInfo`) 관리.
- **`ComplianceStandardRepository`**:
  - 표준 정보(`ComplianceStandard`) 조회.
  - **Pagination**: `findByPackInfoId(Long packInfoId, Pageable pageable)` 지원.
- **`ComplianceCategoryRepository`**: 표준 하위의 카테고리 정보 관리.
- **`ComplianceMappingRepository`**: 카테고리와 내부 취약점 간의 매핑 정보 관리.

### 2. Analysis (분석 결과) 관련
- **`AnalysisResultRepository`**: 통합 분석 결과(SAST, SBOM) 저장.
- **`SbomResultRepository`**: SBOM 분석 메타데이터 저장.
- **`SbomComponentRepository`**: 식별된 오픈소스 컴포넌트 저장.
- **`SbomVulnerabilityRepository`**: 컴포넌트별 취약점(CVE) 정보 저장.
- **`SbomLicenseRepository`**: 라이선스 정보 저장.

### 3. User & System (사용자/시스템)
- **`UserRepository`**: 사용자 계정 조회 및 관리.
- **`CodeSettingsRepository`**: 시스템 설정 정보 관리.

### 4. Mocha (보안 DB - Secondary)
`src/main/java/com/example/vulnscanner/mocha` 패키지에 별도로 위치하지만, 역할상 리포지토리로 분류됩니다.
- **`MochaCveRepository`**: `mocha_dev` DB의 CVE 마스터 데이터 조회.
- **`MochaGhsaRepository`**: GHSA 마스터 데이터 조회.
- **`MochaLicenseRepository`**: 라이선스 마스터 데이터 조회.

---
**특징**:
- 대부분 `JpaRepository`를 상속받아 표준 CRUD 메서드를 제공합니다.
- 복잡한 조회 쿼리는 JPQL 또는 QueryDSL(필요시)을 사용하여 구현됩니다.
