# Analysis Module

`com.example.vulnscanner.module.analysis` 패키지는 소스 코드 취약점 분석(SAST)의 핵심 기능을 담당합니다.

## 🔑 주요 역할 (Key Responsibilities)

1.  **분석 요청 처리 (`AnalysisController`)**
    - 소스 코드 업로드 및 압축 해제 (`FileService`)
    - 분석 옵션(빌드 ID, 언어 등) 설정 및 검증
    - 비동기 분석 작업 실행 요청
2.  **Fortify 연동 (`FortifyService`)**
    - 외부 도구인 Fortify SCA 명령 실행 (Translation, Scan, Report)
    - 프로세스 실행 및 로그 실시간 캡처
3.  **결과 및 로그 관리 (`AnalysisService`)**
    - 분석 상태(READY, RUNNING, SUCCESS, FAIL) 추적
    - 결과 리포트(PDF, XML, FPR) 생성 및 저장
    - 로그 파일 다운로드 및 실시간 스트리밍
4.  **통계 대시보드 (`StatsService`)**
    - 전체 분석 현황, 검출 취약점 수, 성공률 등 지표 계산
    - 주간/월간 트렌드 분석 데이터 제공

## 📄 주요 클래스 (Key Classes)

- **Controller**: `AnalysisController`, `FortifyController`
- **Service**: `AnalysisService`, `FortifyService`, `StatsService`, `FileService`
- **Entity**: `AnalysisResult`, `AnalysisOption`, `ScanSummary`
- **Repository**: `AnalysisResultRepository`

## 🔗 연관 뷰 (Templates)
- `templates/analysis/**`: 리스트, 상세 보기, 요청 폼, 통계 화면
