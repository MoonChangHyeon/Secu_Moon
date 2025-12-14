# 템플릿 (Templates)

## 목차 (Table of Contents)
- [파일 목록 및 설명](#파일-목록-및-설명)
    - [layout.html](#layouthtml)
    - [index.html (또는 dashboard.html)](#indexhtml-또는-dashboardhtml)
    - [results.html](#resultshtml)
    - [result_detail.html](#result_detailhtml)
    - [sbom_result_detail.html](#sbom_result_detailhtml)
    - [stats.html](#statshtml)
    - [settings.html](#settingshtml)
    - [user_management.html](#user_managementhtml)
    - [login.html](#loginhtml)

---

이 디렉토리는 사용자 인터페이스(UI) 구성을 위한 **Thymeleaf HTML 템플릿** 파일들을 포함합니다. 서버 사이드 렌더링을 통해 동적인 웹 페이지를 생성합니다.

## 파일 목록 및 설명

### `layout.html`
- **역할**: 공통 레이아웃 템플릿.
- **내용**:
  - 네비게이션 바(헤더), 푸터, 공통 CSS/JS 포함.
  - **분석 요청 모달(New Scan Modal)**, 알림 모달, 글로벌 스크립트(에러 처리 등)가 포함되어 있어 모든 페이지에서 공통적으로 사용됩니다.

### `index.html` (또는 `dashboard.html`)
- **역할**: 메인 대시보드 페이지.
- **내용**: 전체 스캔 현황, 최근 검사 이력, 주요 통계 차트 등을 표시합니다.

### `results.html`
- **역할**: 통합 분석 결과 목록 페이지.
- **내용**: SAST 및 SBOM 분석 이력을 리스트 형태로 보여주며, 상태 확인, 보고서 다운로드, 상세 보기, 삭제 기능을 제공합니다.

### `result_detail.html`
- **역할**: SAST(정적 분석) 상세 결과 페이지.
- **내용**:
  - 분석 요약 정보(차트).
  - 발견된 취약점 목록 및 상세 정보(코드 라인, 심각도 등).
  - 로그 및 FPR 다운로드 버튼.

### `sbom_result_detail.html`
- **역할**: SBOM 분석 상세 결과 페이지.
- **내용**: 오픈소스 컴포넌트 목록, 라이선스 정보, 의존성 관계 등을 시각화하여 표시합니다.

### `stats.html`
- **역할**: 상세 통계 페이지.
- **내용**: 전체적인 취약점 추이, 심각도 분포, 상위 취약점 카테고리 등 심층적인 분석 데이터를 그래프로 제공합니다.

### `settings.html`
- **역할**: 설정 관리 페이지.
- **내용**:
  - 일반 설정 (경로, 메모리, API URL, 업로드 제한 등).
  - 보안 설정 (로그인 정책).
  - 시스템 정보 확인.

### `user_management.html`
- **역할**: 사용자 관리 페이지 (관리자용).
- **내용**: 사용자 목록 그리드, 계정 생성/수정/삭제/잠금해제 UI.

### `login.html`
- **역할**: 로그인 페이지.
- **내용**: 사용자 인증을 위한 ID/PW 입력 폼.
