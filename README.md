# Secu_Moon

## 프로젝트 개요 (Project Overview)
**Secu_Moon**은 소스 코드 보안 취약점 분석(SAST) 및 오픈소스 관리(SBOM)를 위한 통합 취약점 점검 웹 애플리케이션입니다.
Java Spring Boot 기반의 백엔드와 Thymeleaf 템플릿 엔진을 사용한 프론트엔드로 구성되어 있으며, Fortify와 연동된 정적 분석 및 외부 API를 활용한 SBOM 분석 기능을 제공합니다.

## 문서 인덱스 (Documentation Index)
각 프로젝트 모듈 및 디렉토리별 역할과 상세 설명은 아래 링크된 README를 참고하세요.

### Backend (Core Logic)
- **[Config (설정 및 보안)](src/main/java/com/example/vulnscanner/config/README.md)**
  - Spring Security, 인증(Authentication), 초기 환경 설정 파일 모음.
- **[Controller (컨트롤러)](src/main/java/com/example/vulnscanner/controller/README.md)**
  - 웹 요청(Web Request) 처리 및 화면/API 응답 로직.
- **[Service (비즈니스 로직)](src/main/java/com/example/vulnscanner/service/README.md)**
  - 분석 수행, 파일 처리, 트랜잭션 관리 등 핵심 비즈니스 로직.
- **[Repository (데이터 접근)](src/main/java/com/example/vulnscanner/repository/README.md)**
  - JPA를 이용한 데이터베이스 CRUD 및 쿼리 인터페이스.
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
© 2024 Secu_Moon Project. All rights reserved.
