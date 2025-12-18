# Controller Module (웹 계층)

## 개요
사용자의 HTTP 요청을 처리하고, 적절한 View(Thymeleaf Template)를 반환하거나 API 응답(JSON)을 제공하는 계층입니다.

## 주요 컨트롤러
- **[MainController](MainController.java)**
  - 대시보드 및 메인 페이지 라우팅.
  - 로그인 페이지 처리.
- **[AnalysisController](AnalysisController.java)**
  - 취약점 분석 요청 처리.
  - 분석 결과 리스트 조회 및 상세 페이지 이동.
  - 분석 결과 삭제 및 재분석 요청 핸들링.
- **[SbomController](SbomController.java)**
  - SBOM 분석 결과 조회 및 상세 페이지.
  - SBOM 데이터 보강(Enrichment) 요청 처리.
- **[UserController](UserController.java)**
  - 사용자 관리(목록 조회, 수정, 삭제).
  - 마이페이지(내 정보 수정).
- **[SettingsController](SettingsController.java)**
  - 시스템 환경 설정(DB 접속 정보, 외부 도구 경로 등) 관리.
