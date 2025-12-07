# 서비스 (Service)

이 디렉토리는 애플리케이션의 핵심 **비즈니스 로직**을 담당하는 서비스 클래스들을 포함합니다. 컨트롤러와 리포지토리 사이에서 데이터 처리, 트랜잭션 관리, 외부 시스템 연동 등을 수행합니다.

## 파일 목록 및 설명

### `AnalysisService.java`
- **역할**: SAST(정적 분석) 프로세스 총괄.
- **주요 기능**:
  - 분석 작업 생성 및 실행(비동기).
  - Fortify 분석 도구 호출(`FortifyService` 위임).
  - 분석 완료 후 결과 XML 파싱 및 DB 저장(`ReportParserService` 위임).
  - 실시간 로그 스트리밍 및 파일 관리.

### `SbomService.java`
- **역할**: SBOM 분석 프로세스 총괄.
- **주요 기능**:
  - 소스코드/파일 업로드 처리.
  - 외부 AI_SBOM API 연동 (`WebClient` 사용) - 업로드, 상태 확인, 결과 조회.
  - 동적 API URL 설정 적용.

### `UserService.java`
- **역할**: 사용자 및 인증 관리 로직.
- **주요 기능**:
  - `UserDetailsService` 구현 (Spring Security 로그인 연동).
  - 사용자 생성(회원가입), 수정, 삭제.
  - 로그인 실패 잠금 로직 관리.

### `SettingsService.java`
- **역할**: 시스템 설정 관리.
- **주요 기능**:
  - DB 기반 동적 설정값(API URL, 업로드 제한 등) 로드 및 캐싱/초기화.
  - 외부 API 연결 테스트 로직.

### `FortifyService.java`
- **역할**: CLI 도구(sourceanalyzer) 실행 래퍼.
- **주요 기능**:
  - 실제 쉘 명령어를 실행하여 Fortify Clean, Translate, Scan, Report 과정을 수행.
  - 프로세스 출력 로그 캡처.

### `FileService.java`
- **역할**: 로컬 파일 시스템 제어.
- **주요 기능**:
  - 업로드된 파일 저장.
  - ZIP 압축 해제 처리.
  - 분석 결과 디렉토리 관리.

### `ReportService.java`
- **역할**: 보고서 생성 관련 유틸리티 (필요 시 확장).
- **주요 기능**: 보고서 관련 보조 로직 수행.

### `ReportParserService.java`
- **역할**: Fortify XML 리포트 파싱.
- **주요 기능**:
  - 생성된 XML 결과 파일을 읽어 `AnalysisResult` 및 `Vulnerability` 엔티티 객체로 변환.

### `StatsService.java`
- **역할**: 통계 데이터 계산.
- **주요 기능**:
  - 대시보드 및 통계 페이지에 필요한 각종 수치(취약점 추이, 유형별 분포, 성공률 등) 집계.
