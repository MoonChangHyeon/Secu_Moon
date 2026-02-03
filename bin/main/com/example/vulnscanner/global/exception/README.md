# Exception Package

이 패키지는 애플리케이션 전역에서 발생하는 예외를 처리하는 공통 로직을 포함합니다.

## 주요 클래스

- **`GlobalExceptionHandler.java`**: `@ControllerAdvice`를 사용하여 모든 컨트롤러에서 발생하는 예외를 가로채서 처리합니다. 특정 예외(예: `ResourceNotFoundException`, `AccessDeniedException`) 발생 시 사용자에게 적절한 에러 페이지나 JSON 응답을 반환하도록 구성됩니다.
