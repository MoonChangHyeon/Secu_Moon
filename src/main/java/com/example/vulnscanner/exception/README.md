# Exception Module (예외 처리)

## 개요
애플리케이션 실행 중 발생하는 예외를 전역적으로 처리하거나, 커스텀 예외를 정의하는 패키지입니다.

## 주요 클래스
- **[GlobalExceptionHandler](GlobalExceptionHandler.java)**
  - `@ControllerAdvice`를 사용하여 전역 예외 처리 로직 구현.
  - 특정 예외 발생 시 적절한 에러 페이지나 JSON 응답을 반환하도록 설정.
- **[CustomException](CustomException.java)** (예시)
  - 비즈니스 로직 상의 특정 오류 상황을 명시하기 위한 사용자 정의 예외 클래스.
