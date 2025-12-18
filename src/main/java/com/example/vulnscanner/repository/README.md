# Repository Module (데이터 접근)

## 개요
Database에 직접 접근하여 CRUD(Create, Read, Update, Delete) 작업을 수행하는 계층입니다. Spring Data JPA를 사용하여 인터페이스 선언만으로 쿼리를 실행할 수 있습니다.

## 주요 리포지토리
- **[MemberRepository](MemberRepository.java)**
  - `Member` 엔티티 관리 (사용자 조회, 저장 등).
- **[AnalysisResultRepository](AnalysisResultRepository.java)**
  - `AnalysisResult` (SAST 분석) 데이터 관리.
- **[SbomResultRepository](SbomResultRepository.java)**
  - `SbomResult` (SBOM 분석) 데이터 관리.
- **[SbomVulnerabilityRepository](SbomVulnerabilityRepository.java)**
  - SBOM 분석 결과 내의 취약점 상세 정보 저장 및 조회.
- **[SbomLicenseRepository](SbomLicenseRepository.java)**
  - SBOM 분석 결과 내의 라이선스 상세 정보 저장 및 조회.

> **참고**: `mocha` 패키지의 리포지토리는 `Mocha` DB 전용이며 별도의 `mocha` 패키지 README를 참고하십시오.
