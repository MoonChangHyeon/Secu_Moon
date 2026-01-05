# 사용자 및 권한 관리 모듈 (User & Role Management)

## 개요
이 모듈은 사용자 회원가입, 승인(Approval), 그리고 역할(Role) 기반의 접근 제어(RBAC) 기능을 담당합니다.
단순한 `User/Admin` 문자열 권한에서 벗어나, `Privilege`(세부 권한)를 그룹화한 `RoleTemplate`을 사용하여 보다 유연한 권한 관리가 가능합니다.

## 주요 기능

### 1. 회원가입 및 승인 (Sign-Up & Approval)
- **회원가입**: 사용자가 `/signup` 페이지를 통해 계정을 생성하면 `PENDING` 상태가 됩니다.
- **로그인 차단**: `PENDING` 상태의 사용자는 로그인이 차단됩니다.
- **관리자 승인**: 관리자(`ADMIN`)는 `/user/list` 페이지에서 대기 중인 사용자를 확인하고 승인(`Approve`)할 수 있습니다.
- **역할 할당**: 승인 시 사용자의 역할(Role Template)을 지정해야 합니다.

### 2. 권한 템플릿 (Role Templates)
- **Privilege**: 시스템의 최소 권한 단위입니다. (예: `READ_PRIVILEGE`, `WRITE_PRIVILEGE`)
- **RoleTemplate**: 여러 Privilege를 묶은 그룹입니다. (예: `Admin`, `User`, `Security Auditor`)
- **관리 페이지**: `/user/templates`에서 템플릿을 생성하고 수정할 수 있습니다.

### 3. 관리자 알림 (Admin Notifications)
- 신규 가입 요청 등 주요 이벤트 발생 시 관리자에게 알림이 전송됩니다.
- 로그인한 관리자의 화면 상단 네비게이션 바에 알림 배지(Badge)가 표시됩니다.

## 파일 구조

```
src/main/resources/templates/user/
├── list.html           # 사용자 목록 조회 및 승인/거절 UI
├── template_list.html  # 역할 템플릿 목록 조회
├── template_form.html  # 역할 템플릿 생성/수정 폼
└── README.md           # 모듈 설명 문서 (현재 파일)
```

## 초기 데이터 (Data Bootstrapping)
- `DataLoader.java`를 통해 애플리케이션 시작 시 기본 권한과 데이터가 생성됩니다.
    - **Privileges**: READ_PRIVILEGE, WRITE_PRIVILEGE, DELETE_PRIVILEGE, ADMIN_PRIVILEGE
    - **Role Templates**:
        - `Admin`: 모든 권한 보유
        - `User`: READ_PRIVILEGE 보유
