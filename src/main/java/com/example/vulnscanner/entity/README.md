# Entities (Domain Model)

이 패키지는 데이터베이스 테이블과 1:1로 매핑되는 JPA 엔티티 클래스들을 포함합니다. 애플리케이션의 핵심 데이터 구조를 정의합니다.

## 📋 주요 엔티티 (Key Entities)

### 1. Compliance (규정 준수) 모델 <!-- New -->
다층적인 계층 구조(Tree)를 가집니다.
- **`PackInfo`**: 룰팩의 최상위 단위 (버전, 로케일 정보).
  - `one-to-many` -> `ComplianceStandard`
- **`ComplianceStandard`**: 개별 규정 준수 표준 (예: NIST 800-53).
  - `one-to-many` -> `ComplianceCategory`
- **`ComplianceCategory`**: 표준 내의 통제 항목 또는 카테고리.
  - `one-to-many` -> `ComplianceMapping`
- **`ComplianceMapping`**: 외부 카테고리와 내부(Fortify) 취약점 간의 매핑.

### 2. Analysis (분석) 모델
- **`AnalysisResult`**: 분석 요청 및 전체 결과 요약.
- **`SbomResult`**: SBOM 분석 상세 결과.
- **`SbomComponent`**: 식별된 라이브러리 컴포넌트.
- **`SbomVulnerability`**: 컴포넌트 관련 취약점 (CVE/GHSA).
- **`SbomLicense`**: 컴포넌트 관련 라이선스.

### 3. User & Config (사용자/설정)
- **`User`**: 사용자 계정 정보 (ID, PW, Role, Team 등).
- **`CodeSettings`**: 시스템 전역 설정 (Key-Value 스토어).

---
**설계 특징**:
- **ERD 관계**: `@OneToMany`, `@ManyToOne` 어노테이션을 통해 엔티티 간의 관계가 정의되어 있습니다.
- **FetchType**: 성능 최적화를 위해 컬렉션 관계는 기본적으로 `Lazy Loading`을 사용합니다.
