# Controller Module (컨트롤러)

## 목차 (Table of Contents)
- [주요 컨트롤러 (Key Controllers)](#주요-컨트롤러-key-controllers)
    - [1. SbomController (SBOM 분석 관리)](#1-sbomcontroller-sbom-분석-관리)
    - [2. AnalysisController (SAST 분석 관리)](#2-analysiscontroller-sast-분석-관리)
    - [3. MainController / UserController](#3-maincontroller--usercontroller)
    - [4. SettingsController](#4-settingscontroller)

---

이 디렉토리는 웹 요청(HTTP Request)을 처리하고 뷰(HTML Template) 또는 JSON 응답을 반환하는 클래스들을 포함합니다.

## 주요 컨트롤러 (Key Controllers)

### 1. SbomController (SBOM 분석 관리)
SBOM 분석 요청을 접수하고 결과를 사용자에게 보여줍니다.

- **`getSbomDetail` (상세 조회)**:
  - 기존에는 JSON 파일(`resultJsonPath`)을 직접 읽어 파싱 후 화면에 전달했습니다.
  - **개선함**: 이제는 DB에 저장된 보강된 엔티티(`SbomComponent`, `SbomVulnerability` 등)를 우선적으로 조회합니다.
  - 이를 통해 `Mocha` DB에서 가져온 한글 설명 및 위험도 정보가 화면에 정확히 표시됩니다.
  - DB 데이터가 없을 경우에만 레거시 방식(파일 읽기)으로 동작합니다 (`Fallback Mechanism`).

### 2. AnalysisController (SAST 분석 관리)
- 소스 코드 취약점 분석 요청 처리.
- 분석 결과 목록 및 상세 페이지 제공.
- 재분석 및 삭제 기능 제공.

### 3. MainController / UserController
- **MainController**: 대시보드 및 메인 페이지 라우팅.
- **UserController**: 로그인, 회원가입, 사용자 관리 페이지 라우팅.

### 4. SettingsController
- 시스템 설정 값을 UI에서 조회하고 수정하는 기능 제공.
