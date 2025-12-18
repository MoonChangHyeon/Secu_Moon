# Config Module (설정)

## 개요
Spring Boot 애플리케이션의 핵심 설정을 담당하는 패키지입니다. 보안(Security), 데이터베이스(DataSource), 애플리케이션 초기화 설정 등을 포함합니다.

## 주요 클래스

### Security
- **[SecurityConfig](SecurityConfig.java)**
  - Spring SecurityFilterChain 설정.
  - 로그인/로그아웃 경로 및 권한 설정.
  - 정적 리소스(CSS, JS) 접근 허용 설정.

### Database (Multi-DataSource)
- **[PrimaryDataSourceConfig](PrimaryDataSourceConfig.java)**
  - `vulnscanner_db` (RW) 연결 설정.
  - `entityManagerFactoryPrimary`, `transactionManagerPrimary` 빈(Bean) 정의.
- **[MochaDataSourceConfig](MochaDataSourceConfig.java)**
  - `mocha_dev` (RO) 연결 설정.
  - `entityManagerFactoryMocha`, `transactionManagerMocha` 빈(Bean) 정의.

### Setup
- **[AdminUserSetup](AdminUserSetup.java)**
  - 애플리케이션 최초 구동 시 관리자 계정(`admin`)이 없으면 자동으로 생성하는 로직.
