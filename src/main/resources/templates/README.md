# Template Module (화면)

## 개요
Thymeleaf 템플릿 엔진을 사용하여 서버 사이드 렌더링(SSR)을 수행하는 HTML 파일들이 위치한 디렉토리입니다.

## 주요 템플릿
- **[dashboard.html](dashboard.html)**
  - 애플리케이션의 메인 대시보드. 분석 현황판과 최근 활동 내역을 표시합니다.
- **[result_list.html](result_list.html)**
  - SAST (정적 분석) 결과 목록 페이지.
- **[result_detail.html](result_detail.html)**
  - SAST 분석 상세 결과 및 취약점 리포트.
- **[sbom_result_list.html](sbom_result_list.html)**
  - SBOM (오픈소스 분석) 결과 목록 페이지.
- **[sbom_result_detail.html](sbom_result_detail.html)**
  - SBOM 분석 상세 결과, 취약점(CVE/GHSA) 및 라이선스 정보.
- **[login.html](login.html)**
  - 사용자 로그인 화면.
- **[settings.html](settings.html)**
  - 시스템 및 분석 도구 설정 화면.
- **[users.html](users.html)**
  - 사용자 관리 (관리자 전용) 화면.

## 디렉토리 구조
- `/fragments`: 공통적으로 사용되는 헤더, 사이드바, 푸터 등의 조각(Fragment) 파일 모음.
