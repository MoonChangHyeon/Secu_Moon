# Service Module (비즈니스 로직)

## 개요
애플리케이션의 핵심 비즈니스 로직을 수행하는 계층입니다. 트랜잭션(@Transactional) 안에서 데이터 처리, 외부 도구 연동, 파일 입출력 등을 담당합니다.

## 주요 서비스
- **[AnalysisService](AnalysisService.java)**
  - SAST(정적 분석) 도구(Fortify 등) 실행 및 결과 처리.
  - 분석 결과(XML) 파싱 및 DB 저장.
  - 분석 폴더 및 파일 관리.
- **[SbomService](SbomService.java)**
  - SBOM 분석 수행 및 결과 파싱.
  - `Mocha` DB와 연동하여 취약점 및 라이선스 정보 보강(Enrichment).
  - SBOM 통계 데이터 생성.
- **[UserService](UserService.java)**
  - 사용자 생성, 수정, 삭제 비즈니스 로직.
  - 비밀번호 변경 및 검증.
- **[SettingsService](SettingsService.java)**
  - 애플리케이션 설정 값(Properties) 조회 및 업데이트.
