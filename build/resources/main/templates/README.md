# Templates (Thymeleaf Views)

이 디렉토리는 Thymeleaf 템플릿 엔진을 사용하는 HTML 화면 파일들을 포함합니다. `src/main/resources/templates` 경로에 위치하며 컨트롤러에 의해 렌더링됩니다.

## 📂 디렉토리 구조 및 설명

<<<<<<< Updated upstream
### 1. `compliance/` (규정 준수 뷰어) <!-- New -->
- **`list.html`**:
  - 룰팩 XML 파일 업로드 폼.
  - 업로드된 룰팩 버전 목록 및 다운로드/비교/삭제 기능 제공.
- **`viewer.html`**:
  - 특정 버전의 상세 내용 조회 (Tree/Accordion UI).
  - 페이지네이션, 클립보드 복사, 데이터 내보내기(CSV/XML/JSON) 기능 포함.
- **`compare.html`**:
  - 두 버전 간의 Diff 결과 시각화.
  - 변경된 항목(추가/삭제/수정) 하이라이팅 및 필터링.
=======
- **`dashboard.html`**: 메인 대시보드 페이지 (요약 통계 등)
- **`layout.html`**: 공통 레이아웃 (헤더, 사이드바, 푸터 포함)
- **`analysis/`**: 분석 관련 뷰
    - `request.html`: 분석 요청 폼
    - `list.html`: 분석 결과 목록
    - `detail.html`: 분석 상세 결과
    - `stats.html`: 통계 페이지
- **`sbom/`**: SBOM 관련 뷰
    - `detail.html`: SBOM 상세 분석 결과
- **`compliance/`**: 규정 준수 관련 뷰
    - `viewer.html`: 룰팩 뷰어 (표준/카테고리/매핑 탐색)
    - `compare.html`: 룰팩 버전 비교 및 이력 관리
    - `compare_detail.html`: 표준별 비교 상세 결과 (팝업)
- **`user/`**: 사용자 관리 뷰
    - `list.html`: 사용자 목록 및 관리
- **`settings/`**: 설정 관련 뷰
    - `index.html`: 설정 관리 페이지
- **`login.html`**: 로그인 페이지
- **`error.html`**: 공통 에러 페이지
>>>>>>> Stashed changes

### 2. `dashboard/` (대시보드)
- **`dashboard.html`**: 통합 대시보드 화면. 차트 및 요약 테이블 제공.

### 3. `analysis/` (분석 결과)
- **`result_list.html`**: 분석 이력 전체 목록.
- **`result_detail.html`**: SAST(정적 분석) 상세 결과 리포트.
- **`sbom_result_detail.html`**: SBOM 분석 및 취약점/라이선스 상세 리포트.

### 4. `settings/` (설정)
- **`user_list.html`**: 사용자 계정 관리.
- **`code_editor.html`**: 시스템 설정 파일 편집기.

### 5. `layout/` (공통 레이아웃)
- **`layout.html`**: 헤더, 사이드바, 푸터를 포함한 마스터 레이아웃.
- 모든 페이지는 이 레이아웃을 상속받아 일관돤 디자인을 유지합니다.

---
**주요 UI 기술**:
- **Framework**: Bootstrap 5
- **Icons**: Bootstrap Icons (BI)
- **Script**: Vanilla JS, Fetch API (AJAX 요청용)
