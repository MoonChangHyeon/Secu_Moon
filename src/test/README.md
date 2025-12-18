# Test Module (테스트)

## 개요
애플리케이션의 안정성을 검증하기 위한 단위 테스트(Unit) 및 통합 테스트(Integration) 코드가 위치한 디렉토리입니다.

## 테스트 구조
- **Service Tests**: 비즈니스 로직 검증.
  - `UserServiceTest.java`: 사용자 생성/삭제 로직 테스트.
  - `PasswordValidatorTest.java` (예상): 비밀번호 복잡도 규칙 테스트.
- **Controller Tests**: HTTP 요청 및 응답 검증 (MockMvc 활용).
- **Repository Tests**: H2 데이터베이스 등을 활용한 쿼리 검증.

## 실행 방법 (Gradle)
```bash
./gradlew test
```
프로젝트 루트에서 위 명령어를 실행하여 전체 테스트를 수행할 수 있습니다.
