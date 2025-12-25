# Config Package

이 패키지는 Spring Boot 애플리케이션의 설정(Configuration) 클래스들을 포함합니다. 보안, 데이터베이스 연결, 암호화 설정 등을 담당합니다.

## 주요 설정 클래스

### 1. 보안 설정 (Security)
- **`SecurityConfig.java`**: Spring Security의 핵심 설정을 정의합니다. URL별 접근 권한, 로그인/로그아웃 페이지 설정, CSRF 보호 정책 등을 구성합니다.
- **`CustomAuthenticationSuccessHandler.java`**: 로그인 성공 시 동작을 정의합니다 (예: 실패 횟수 초기화, 마지막 로그인 시간 기록, 페이지 리다이렉트).
- **`CustomAuthenticationFailureHandler.java`**: 로그인 실패 시 동작을 정의합니다 (예: 실패 횟수 증가, 계정 잠금 처리, 에러 메시지 전달).
- **`AuthenticationEventListener.java`**: 인증 관련 이벤트를 리스닝하여 로깅하거나 추가 작업을 수행합니다.
- **`PasswordConfig.java`**: 비밀번호 암호화를 위한 `BCryptPasswordEncoder` Bean을 등록합니다.

### 2. 데이터베이스 설정 (Database)
이 애플리케이션은 다중 데이터베이스 연결을 지원하는 것으로 보입니다.
- **`PrimaryDbConfig.java`**: 주 데이터베이스(VulnScanner)에 대한 DataSource, EntityManager, TransactionManager 설정을 담당합니다. (`@Primary`)
- **`MochaDbConfig.java`**: 보조 데이터베이스(Mocha)에 대한 연결 설정을 담당합니다.
