# 컨트롤러 (Controller)

이 디렉토리는 사용자의 HTTP 요청을 받아 비즈니스 로직을 호출하고, 결과를 뷰(View) 템플릿이나 JSON 형태로 응답하는 **MVC 컨트롤러** 클래스들을 포함합니다.

## 파일 목록 및 설명

### `AnalysisController.java`
- **역할**: 취약점 점검 분석(SAST) 및 결과 조회 관련 요청 처리.
- **주요 기능**:
  - 분석 요청 처리 (소스코드 업로드 및 분석 시작).
  - 대시보드 데이터 조회.
  - 분석 결과 목록 및 상세 내용 조회.
  - 각종 보고서(PDF, FPR, Log) 및 데이터 다운로드.
  - 분석 데이터 삭제 및 XML 재파싱 요청 처리.

### `SbomController.java`
- **역할**: SBOM(Software Bill of Materials) 분석 관련 요청 처리.
- **주요 기능**:
  - SBOM 분석 요청 (소스/파일 업로드 -> 외부 API 호출).
  - SBOM 분석 진행 상태(Status) 확인.
  - SBOM 분석 상세 결과 조회.

### `SettingsController.java`
- **역할**: 애플리케이션 및 시스템 설정 관리.
- **주요 기능**:
  - 일반 설정 페이지(경로, 메모리, API URL 등) 조회 및 업데이트.
  - 외부 API 연결 테스트 요청 처리.

### `UserManagementController.java`
- **역할**: 사용자 관리 기능 (관리자 전용).
- **주요 기능**:
  - 사용자 목록 조회.
  - 사용자 생성, 수정, 삭제.
  - 비밀번호 초기화 및 상태 변경(잠금 해제).

### `LoginController.java`
- **역할**: 로그인 페이지 요청 처리.
- **주요 기능**:
  - 커스텀 로그인 페이지(`/login`)를 반환합니다.

### `FortifyController.java`
- **역할**: Fortify 관련 특정 기능 처리(현재는 주로 AnalysisController에 통합됨, 레거시 또는 확장용).
- **주요 기능**:
  - (현재 기능 확인 필요, 주로 분석 도구 관련 상태 체크나 별도 동작 정의 시 사용)
