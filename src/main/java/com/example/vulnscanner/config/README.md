# Configuration Module (설정 모듈)

## 목차 (Table of Contents)
- [주요 클래스 (Key Classes)](#주요-클래스-key-classes)
    - [1. Database Configuration (Dual DB Setup)](#1-database-configuration-dual-db-setup)
    - [2. Security & Web](#2-security--web)
- [설정 파일 (Application Properties)](#설정-파일-application-properties)

---

이 디렉토리는 Spring Boot 애플리케이션의 핵심 설정을 담당하는 클래스들을 포함합니다. 보안(Security), 웹 MVC 설정, 그리고 멀티 데이터소스(Database) 설정이 포함되어 있습니다.

## 주요 클래스 (Key Classes)

### 1. Database Configuration (Dual DB Setup)
본 프로젝트는 **Dual DB 전략**을 사용하며, 이를 위해 두 개의 데이터소스 설정 클래스를 제공합니다.

- **[PrimaryDbConfig.java](PrimaryDbConfig.java)**
  - **대상 DB**: `vulnscanner_db` (Primary)
  - **역할**: 애플리케이션의 메인 데이터 저장소 (CRUD).
  - **설정**:
    - `DataSource`: `primaryDataSource` (Prefix: `spring.datasource.primary`)
    - `EntityManagerFactory`: `primaryEntityManagerFactory` (Entity 패키지: `com.example.vulnscanner.entity`)
    - `TransactionManager`: `primaryTransactionManager` (@Primary)

- **[MochaDbConfig.java](MochaDbConfig.java)**
  - **대상 DB**: `mocha_dev` (Secondary)
  - **역할**: 보안 취약점 마스터 데이터 참조 (Read-Only).
  - **설정**:
    - `DataSource`: `mochaDataSource` (Prefix: `spring.datasource.mocha`)
    - `EntityManagerFactory`: `mochaEntityManagerFactory` (Entity 패키지: `com.example.vulnscanner.mocha.entity`)
    - `TransactionManager`: `mochaTransactionManager`

### 2. Security & Web
- **[SecurityConfig.java](SecurityConfig.java)**
  - Spring Security 필터 체인 설정.
  - 로그인/로그아웃 로직, 권한 제어(Role-based Access Control), 정적 리소스 허용 등 보안 정책 정의.
  - `UserDetailsService` 빈 등록.

- **[WebConfig.java](WebConfig.java)**
  - CORS(Cross-Origin Resource Sharing) 설정.
  - 리소스 핸들러 등 웹 관련 전역 설정.

- **[PasswordEncoderConfig.java](PasswordEncoderConfig.java)**
  - 비밀번호 암호화를 위한 `BCryptPasswordEncoder` 빈 등록.

## 설정 파일 (Application Properties)
- `src/main/resources/application.properties` 파일에서 실제 접속 정보(URL, Username, Password)를 관리합니다.
- HikariCP를 사용하므로 URL 설정 시 **`jdbc-url`** 키를 사용해야 함에 유의하세요.
