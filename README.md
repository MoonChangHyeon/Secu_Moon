# Secu_Moon

## 문서 인덱스 (Documentation Index)
각 프로젝트 모듈 및 디렉토리별 역할과 상세 설명은 아래 링크된 README를 참고하세요.

### Backend (Core Logic)
- **[Config (설정 및 보안)](src/main/java/com/example/vulnscanner/config/README.md)**
  - Spring Security, 인증(Authentication), 초기 환경 설정, Multi-DataSource 설정.
- **[Controller (컨트롤러)](src/main/java/com/example/vulnscanner/controller/README.md)**
  - 웹 요청(Web Request) 처리 및 화면/API 응답 로직.
- **[Service (비즈니스 로직)](src/main/java/com/example/vulnscanner/service/README.md)**
  - 분석 수행, 파일 처리, 트랜잭션 관리, 데이터 보강(Enrichment).
- **[Repository (데이터 접근)](src/main/java/com/example/vulnscanner/repository/README.md)**
  - JPA를 이용한 데이터베이스 CRUD 및 쿼리 인터페이스 (Primary & Mocha).
- **[Entity (도메인 모델)](src/main/java/com/example/vulnscanner/entity/README.md)**
  - 데이터베이스 테이블과 매핑되는 핵심 데이터 모델.
- **[DTO (데이터 전송 객체)](src/main/java/com/example/vulnscanner/dto/README.md)**
  - 계층 간 데이터 교환을 위한 순수 데이터 객체.
- **[Exception (예외 처리)](src/main/java/com/example/vulnscanner/exception/README.md)**
  - 전역 예외 처리(Global Exception Handling) 로직.

### Frontend (View Layer)
- **[Templates (화면 템플릿)](src/main/resources/templates/README.md)**
  - Thymeleaf 기반의 HTML 화면 구성 및 UI 컴포넌트 설명.

---

## 프로젝트 개요 (Project Overview)
**Secu_Moon**은 소스 코드 보안 취약점 분석(SAST) 및 오픈소스 관리(SBOM)를 위한 통합 취약점 점검 웹 애플리케이션입니다.
Java Spring Boot 기반의 백엔드와 Thymeleaf 템플릿 엔진을 사용한 프론트엔드로 구성되어 있으며, Fortify와 연동된 정적 분석 및 외부 API를 활용한 SBOM 분석 기능을 제공합니다.

## 주요 시스템 아키텍처 (Key Architecture)
### Dual Database Strategy (이중 데이터베이스 전략)
본 프로젝트는 데이터의 무결성과 풍부한 정보 제공을 위해 두 개의 데이터베이스를 동시에 사용합니다.

1.  **Primary Database (`vulnscanner_db`)**
    - **역할**: 애플리케이션의 메인 저장소.
    - **데이터**: 사용자 정보, 분석 요청 이력, 분석 결과(SbomResult), 보강된 취약점/라이선스 데이터.
    - **특징**: CRUD(읽기/쓰기) 작업이 빈번하게 발생.

2.  **Secondary Database (`mocha_dev`)**
    - **역할**: 보안 취약점 마스터 데이터 참조용 (Read-Only).
    - **데이터**: CVE(Common Vulnerabilities and Exposures), GHSA(GitHub Security Advisories), SPDX License 목록.
    - **특징**: 방대한 양의 보안 데이터를 보유하며, 분석 시점에 조회하여 한글 설명(Korean Description) 및 정확한 위험도(Severity) 정보를 제공합니다.

### SBOM 데이터 보강 (Enrichment Process)
SBOM 분석이 수행될 때, 단순히 오픈소스 라이브러리 목록만 추출하는 것이 아니라 `mocha_dev` DB를 실시간으로 조회하여 데이터를 보강합니다.
- **CVE/GHSA**: 영문 설명뿐만 아니라 **한글 설명**을 우선적으로 매핑합니다.
- **License**: 라이선스의 세부적인 **위험도(Severity)** 및 상세 링크를 매핑합니다.
- **최적화**: 보강된 데이터는 `vulnscanner_db`의 `SbomVulnerability` 및 `SbomLicense` 테이블에 저장되어, 이후 조회 시에는 외부 조인 없이 빠른 속도로 상세 정보를 제공합니다.

---
© 2024 Secu_Moon Project. All rights reserved.