# User Module

`com.example.vulnscanner.module.user` 패키지는 사용자 인증 및 계정 관리를 담당합니다.

## 🔑 주요 역할 (Key Responsibilities)

1.  **사용자 인증 (`LoginController`)**
    - Spring Security와 연동된 로그인 페이지 및 로직
    - 세션 관리 및 접근 제어
2.  **사용자 관리 (`UserManagementController`)**
    - 사용자 목록 조회, 생성, 수정, 삭제 (CRUD)
    - 역할(Role) 및 소속팀 관리
3.  **데이터 접근 (`UserService`)**
    - `User` 엔티티 관리 및 `UserDetailService` 구현

## 📄 주요 클래스 (Key Classes)

- **Controller**: `LoginController`, `UserManagementController`
- **Service**: `UserService`
- **Entity**: `User`
- **Repository**: `UserRepository`

## 🔗 연관 뷰 (Templates)
- `templates/login.html`: 로그인 페이지
- `templates/user/**`: 사용자 목록 및 관리
