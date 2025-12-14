# Service Module (비즈니스 로직)

## 목차 (Table of Contents)
- [주요 서비스 (Key Services)](#주요-서비스-key-services)
    - [1. SbomService (SBOM 분석 및 보강)](#1-sbomservice-sbom-분석-및-보강)
    - [2. AnalysisService (SAST 분석)](#2-analysisservice-sast-분석)
    - [3. 기타 서비스](#3-기타-서비스)

---

이 디렉토리는 애플리케이션의 핵심 비즈니스 로직을 처리하는 서비스 클래스들을 포함합니다.
컨트롤러로부터 요청을 받아 트랜잭션 단위로 작업을 수행하고, 리포지토리를 통해 데이터를 조작합니다.

## 주요 서비스 (Key Services)

### 1. SbomService (SBOM 분석 및 보강)
SBOM 분석의 전체 수명 주기를 관리합니다. 가장 중요한 변경 사항은 **데이터 보강(Enrichment)** 로직의 추가입니다.

- **분석 요청 (`processSbomAnalysis`)**:
  - 외부 파이썬 API(`sbom-api`)에 분석을 요청합니다.
  - 비동기로 실행되며 상태를 `RUNNING` -> `SUCCESS/FAILED`로 갱신합니다.

- **결과 파싱 및 보강 (`parseAndSaveAnalysisResult`)**:
  - 분석 결과 JSON을 파싱하여 `SbomComponent`를 생성합니다.
  - **Enrichment Step**:
    - 추출된 취약점/라이선스 ID를 사용하여 `Mocha*Repository`를 조회합니다.
    - 조회된 상세 정보(한글 설명, 위험도 등)를 `SbomVulnerability`, `SbomLicense` 엔티티에 채워 넣습니다.
    - 이렇게 보강된 데이터를 Primary DB에 영구 저장합니다.

### 2. AnalysisService (SAST 분석)
- Fortify(`sourceanalyzer`)를 사용하여 소스 코드 정적 분석을 수행합니다.
- `ProcessBuilder`를 통해 외부 명령어를 실행하고 결과를 `AnalysisResult` 엔티티로 변환합니다.

### 3. 기타 서비스
- **UserService**: 사용자 생성, 수정, 삭제 및 인증 관련 로직.
- **StatsService**: 취약점 통계 데이터 집계.
- **SettingsService**: 시스템 설정(파일 경로, API URL 등) 관리.
