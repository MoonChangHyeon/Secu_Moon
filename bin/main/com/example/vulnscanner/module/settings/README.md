# Settings Module

`com.example.vulnscanner.module.settings` 패키지는 애플리케이션의 전역 설정을 관리합니다.

## 🔑 주요 역할 (Key Responsibilities)

1.  **설정 관리 (`SettingsService`)**
    - Key-Value 쌍으로 시스템 설정 저장 (DB 기반)
    - 주요 설정 항목:
        - `RESULT_PATH`: 분석 결과 저장 경로
        - `MAX_UPLOAD_SIZE`: 파일 업로드 최대 크기 제한
        - `API_URL`: 연동 시스템 API 주소
2.  **연결 테스트**
    - 외부 API와의 연결 상태 확인 기능 제공

## 📄 주요 클래스 (Key Classes)

- **Controller**: `SettingsController`
- **Service**: `SettingsService`
- **Entity**: `Setting`
- **Repository**: `SettingsRepository`

## 🔗 연관 뷰 (Templates)
- `templates/settings/**`: 설정 관리 대시보드
