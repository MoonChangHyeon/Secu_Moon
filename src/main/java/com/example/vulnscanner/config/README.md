# 설정 (Config)

이 디렉토리는 애플리케이션의 전반적인 환경 설정, 특히 **보안(Spring Security)** 및 **인증(Authentication)**과 관련된 설정 파일들을 포함하고 있습니다.

## 파일 목록 및 설명

### `SecurityConfig.java`
- **역할**: Spring Security의 핵심설정 파일입니다.
- **주요 기능**:
  - HTTP 요청에 대한 접근 권한 설정 (URL별 권한 제어).
  - 로그인 및 로그아웃 프로세스 설정.
  - CSRF 보안 설정.
  - 패스워드 인코더(BCrypt) 빈 등록.

### `AuthenticationEventListener.java`
- **역할**: 인증 관련 이벤트 리스너입니다.
- **주요 기능**:
  - 로그인 성공/실패 이벤트를 감지하여 로그를 기록하거나 추가적인 보안 로직(예: 실패 횟수 집계 등)을 트리거할 수 있습니다.

### `CustomAuthenticationSuccessHandler.java`
- **역할**: 로그인 성공 시 실행되는 핸들러입니다.
- **주요 기능**:
  - 로그인 성공 후 사용자의 실패 횟수를 초기화합니다.
  - 사용자 역할(Role)에 따라 리다이렉트할 페이지를 결정합니다.

### `CustomAuthenticationFailureHandler.java`
- **역할**: 로그인 실패 시 실행되는 핸들러입니다.
- **주요 기능**:
  - 로그인 실패 횟수를 증가시킵니다.
  - 최대 허용 횟수 초과 시 계정을 잠급니다(Lock).
  - 실패 원인에 따라 적절한 에러 메시지를 생성하여 로그인 페이지로 전달합니다.

### `PasswordConfig.java`
- **역할**: 비밀번호 암호화 빈 설정입니다.
- **주요 기능**:
  - `PasswordEncoder`(BCryptPasswordEncoder)를 스프링 컨테이너에 등록하여, 안전한 비밀번호 저장을 지원합니다.
