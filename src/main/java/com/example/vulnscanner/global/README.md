# Global Package

`com.example.vulnscanner.global` 패키지는 프로젝트 전반에서 공통적으로 사용되는 설정, 유틸리티, 예외 처리 로직을 포함합니다.

## 📦 구성 요소 (Components)

### 1. Config (`global.config`)
애플리케이션의 핵심 설정을 담당합니다.
- **`SecurityConfig`**: Spring Security 인증/인가 설정 (CSRF, 로그인/로그아웃, 페이지 권한)
- **`PrimaryDbConfig`**: 메인 애플리케이션 DB(`vulnscanner`) 연결 설정 (JPA, Transaction)
- **`MochaDbConfig`**: 외부 취약점 DB(`mocha`) 연결 설정 (Multi-Datasource)

### 2. Exception (`global.exception`)
전역적인 예외 처리를 담당합니다.
- **`GlobalExceptionHandler`**: `@ControllerAdvice`를 사용하여 발생하는 예외를 잡고, 적절한 에러 페이지나 JSON 응답을 반환합니다.

### 3. Util (`global.util`)
공통 유틸리티 클래스 모음입니다.
- 날짜 변환, 문자열 처리 등 다용도 헬퍼 클래스 포함

## 🔗 참조
이 패키지의 클래스들은 `module.*` 패키지의 비즈니스 로직에서 광범위하게 참조됩니다.
