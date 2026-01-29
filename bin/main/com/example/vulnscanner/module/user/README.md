# User Module

사용자 계정, 권한, 인증을 관리하는 모듈입니다.

---

## 🔥 주요 기능 (Features)

### 1. 사용자 관리
- 사용자 생성, 수정, 삭제.
- 역할(Role) 기반 권한 관리 (ADMIN, USER 등).

### 2. 인증 및 인가
- 로그인/로그아웃 처리.
- 패스워드 암호화 및 검증.

---

## 🛠 기술 명세 (Technical Specs)

- **Controller**: `UserController` (사용자 CRUD 뷰/API).
- **Service**: `UserService` (비즈니스 로직).
- **Entity**: `User`, `Role`.
