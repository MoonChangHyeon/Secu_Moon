# Business Feature Modules

`com.example.vulnscanner.module` 패키지는 애플리케이션의 핵심 비즈니스 기능을 담당하는 모듈들의 집합입니다. 각 모듈은 관련된 Controller, Service, Repository, Entity, DTO 등을 응집도 있게 관리합니다.

## 📦 모듈 목록 (Modules Index)

각 모듈에 대한 상세 설명은 아래 링크를 참조하세요.

- **[Analysis (분석)](analysis/README.md)**
    - 소스 코드 취약점 분석 (SAST)
    - Fortify 연동 및 실행 제어
    - 분석 결과 조회 및 통계 대시보드
- **[SBOM (Software Bill of Materials)](sbom/README.md)**
    - SBOM 파일(CycloneDX 등) 업로드 및 파싱
    - 오픈소스 컴포넌트 및 라이선스 식별
    - 취약점(CVE/GHSA) 매핑 및 상세 조회
- **[Compliance (규정 준수)](compliance/README.md)**
    - 보안 규정(Rulepack) 업로드 및 관리
    - 표준(Standard) - 카테고리 - 매핑 데이터 구조화
    - **룰팩 버전 비교**: 추가/삭제/변경 항목 통계 및 상세 분석
    - **비교 이력 관리**: 과거 비교 결과 저장, 조회 및 삭제
- **[User (사용자)](user/README.md)**
    - Spring Security 기반 로그인/인증
    - 사용자 계정 관리 (CRUD)
- **[Settings (설정)](settings/README.md)**
    - 시스템 전역 설정 관리 (업로드 경로, 파일 크기 제한 등)
    - 외부 API 연결 테스트
- **[Mocha](mocha/README.md)**
    - 외부 취약점 데이터베이스 스키마 및 엔티티 정의
