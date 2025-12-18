# Entity Module (도메인 모델)

## 개요
관계형 데이터베이스(RDBMS)의 테이블과 매핑되는 자바 객체(Entity)들을 정의한 패키지입니다. 프로젝트의 핵심 도메인 모델을 담고 있습니다.

## 주요 엔티티
- **[Member](Member.java)**
  - 사용자 정보 (사용자명, 비밀번호, 팀, 역할 등).
- **[AnalysisResult](AnalysisResult.java)**
  - 소스 코드 분석(SAST) 결과 정보 (분석 ID, 상태, 탐지된 취약점 수 등).
- **[SbomResult](SbomResult.java)**
  - 오픈소스 분석(SBOM) 결과 정보 (프로젝트 메타데이터, 전체 통계).
- **[SbomVulnerability](SbomVulnerability.java)**
  - SBOM 분석에서 발견된 개별 취약점 정보 (CVE ID, 패키지명, 위험도 등).
- **[SbomLicense](SbomLicense.java)**
  - SBOM 분석에서 식별된 라이선스 정보 (라이선스명, 관련 패키지 등).

> **참고**: `mocha` 패키지의 엔티티는 별도의 `mocha` 패키지 README를 참고하십시오.
