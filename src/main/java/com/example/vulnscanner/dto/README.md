# DTO Module (데이터 전송 객체)

## 개요
DTO (Data Transfer Object)는 계층 간(Controller ↔ Service ↔ Repository)에 데이터를 전달하기 위해 사용되는 순수 데이터 객체입니다. 엔티티(Entity)를 직접 노출하지 않고 필요한 데이터만 선별하여 클라이언트에 전달하거나 입력을 받기 위해 사용합니다.

## 주요 DTO
- **[UserUpdateDto](UserUpdateDto.java)**: 사용자 정보 수정 요청 시 사용되는 DTO.
- **[PasswordChangeDto](PasswordChangeDto.java)**: 비밀번호 변경 요청 데이터.
- **[SettingsDto](SettingsDto.java)**: 시스템 설정 정보 전송용.
- **[SbomDetailDto](SbomDetailDto.java)**: SBOM 상세 결과 화면에 표시할 복합 데이터 객체.
