# Services (Business Logic Layer)

이 패키지는 애플리케이션의 핵심 비즈니스 로직을 담당하는 서비스 클래스들을 포함합니다. 컨트롤러와 리포지토리 사이에서 데이터 처리, 트랜잭션 관리, 연산을 수행합니다.

## 📋 주요 서비스 (Key Services)

### 1. `RulepackService` <!-- New -->
- **역할**: Fortify 룰팩(`externalmetadata`) XML 파싱 및 저장.
- **기능**:
  - **XML 파싱**: `DocumentBuilderFactory`를 사용하여 대용량 XML 구조 파싱.
  - **유효성 검사**: `<ExternalMetadataPack>` 루트 요소 및 버전 중복 확인.
  - **엔티티 저장**: `PackInfo` -> `ComplianceStandard` -> `ComplianceCategory` -> `ComplianceMapping` 계층 구조로 데이터 저장.
  - **버전 이력**: 업로드된 룰팩의 버전 정보를 관리.

### 2. `ComplianceComparisonService` <!-- New -->
- **역할**: 룰팩 버전 간의 변경 사항 비교 (Diff Algorithm).
- **기능**:
  - **비교 로직**: Source(구버전)와 Target(신버전) 데이터를 로드하여 표준, 카테고리, 매핑 단위로 비교.
  - **상태 판별**:
    - `ADDED`: 신규 추가된 항목 (초록색).
    - `DELETED`: 삭제된 항목 (빨간색).
    - `MODIFIED`: 내용이 변경된 항목 (노랑색).
    - `UNCHANGED`: 변경 없는 항목.
  - **결과 반환**: `ComplianceDiffDto` 구조로 계층화된 비교 결과 생성.

### 3. `SbomAnalysisService`
- **역할**: SBOM 분석 실행 및 결과 데이터 처리.
- **기능**:
  - Syft/Grype 등의 도구를 실행하여 SBOM 생성.
  - 분석 결과 파싱 및 `SbomResult`, `SbomComponent` 저장.
  - `MochaService`와 연동하여 취약점 정보 보강(Enrichment).

### 4. `MochaService`
- **역할**: 보안 취약점 마스터 데이터(`mocha_dev`) 조회.
- **기능**:
  - CVE/GHSA ID 기반의 상세 취약점 정보(한글 설명 포함) 매핑.
  - 라이선스 정보 조회.

### 5. `VulnerabilityScanService`
- **역할**: SAST(정적 분석) 실행 관리.
- **기능**: Fortify SourceAnalyzer 실행 명령 생성 및 프로세스 제어.

### 6. `ResultParsingService`
- **역할**: 분석 결과 파일(XML/FPR) 파싱.
- **기능**: 정적 분석 결과 XML을 파싱하여 DB에 구조화된 데이터로 저장.

---
**기술적 특징**:
- **Transactional**: 주요 데이터 변경 작업은 `@Transactional`로 묶여 데이터 무결성을 보장합니다.
- **Parsing**: 대용량 XML/JSON 처리를 위한 효율적인 파싱 로직이 구현되어 있습니다.
