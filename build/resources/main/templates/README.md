# Thymeleaf Templates

`src/main/resources/templates` 디렉토리는 서버 사이드 렌더링을 위한 HTML 뷰 파일들을 포함합니다. 리팩토링을 통해 각 기능 모듈별로 폴더가 구조화되었습니다.

## 📂 디렉토리 구조 (Directory Structure)

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

## 🎨 기술 스택
- **Thymeleaf**: 템플릿 엔진
- **Bootstrap 5**: UI 프레임워크
- **Chart.js** (일부): 통계 차트 시각화
