# Controllers (API & Web Layer)

이 패키지는 웹 요청(Web Request)을 처리하고, 적절한 뷰(View)를 반환하거나 RESTful API 응답을 제공하는 컨트롤러 클래스들을 포함합니다.

## 📋 주요 컨트롤러 (Key Controllers)

### 1. `MainController`
- **역할**: 애플리케이션의 진입점 및 메인 대시보드 관리.
- **기능**:
  - `/`: 메인 대시보드 페이지 (`dashboard.html`).
  - 최근 분석 이력, 통계 데이터 조회.

### 2. `AnalysisResultController`
- **역할**: SAST(정적 분석) 및 SBOM 분석 결과 관리.
- **기능**:
  - 분석 결과 목록 조회 및 삭제.
  - 상세 결과 조회 (`result_detail.html`, `sbom_result_detail.html`).
  - 재분석 요청 및 결과 파일 다운로드.

### 3. `ComplianceController` <!-- New -->
- **역할**: 보안 규정 준수(Compliance) 매핑 정보 관리.
- **기능**:
  - **룰팩 관리**: XML 룰팩 파일 업로드 (`/api/rulepacks/upload`) 및 목록 조회.
  - **뷰어 (Viewer)**: 표준-카테고리-매핑 트리 구조 조회 (`viewer.html`).
  - **버전 비교**: 두 룰팩 버전 간 차이점(Diff) 비교 (`compare.html`).
  - **데이터 내보내기**: `downloadStandard` 엔드포인트를 통해 CSV, XML, JSON 포맷 다운로드 지원.

### 4. `CodeEditorController`
- **역할**: `sourceanalyzer.json` 등 설정 파일 편집.
- **기능**: 웹상에서 분석 도구 설정 파일을 직접 수정하고 저장.

### 5. `ConfigManagementController`
- **역할**: 데이터베이스 및 시스템 설정 관리.
- **기능**: DB 연결 정보, 분석 옵션 등을 동적으로 설정.

### 6. `UserController`
- **역할**: 사용자 계정 및 권한 관리.
- **기능**: 사용자 목록 조회, 등록, 정보 수정, 비밀번호 변경.

---
**참고**: 모든 컨트롤러는 `templates` 폴더 내의 Thymeleaf HTML 파일과 연결되거나, JSON 데이터를 반환합니다.
