# Service Package

이 패키지는 비즈니스 로직을 담당합니다. 컨트롤러의 요청을 받아 리포지토리를 통해 데이터를 처리하거나, 외부 시스템(Fortify, AI API 등)과 상호작용합니다.

## 주요 Service 구조 및 역할

### 1. 분석 실행 및 관리 (Analysis Core)

#### **`AnalysisService.java`**
SAST(정적 분석)의 전체 수명 주기를 관리하는 핵심 서비스입니다.
- **주요 기능**:
    - `runAnalysis`: 비동기적으로 분석을 실행합니다. `FortifyService`를 호출하여 Clean -> Translate -> Scan 단계를 수행합니다.
    - `createAnalysis`: 초기 분석 요청을 생성하고 데이터베이스에 기록합니다.
    - `deleteResult`: 분석 결과 및 관련 파일(소스, 로그, 보고서)을 삭제합니다.
    - `getLiveLogs`: 실행 중인 분석의 실시간 로그를 제공합니다.

#### **`FortifyService.java`**
Fortify SCA(Source Code Analyzer) CLI 도구를 래핑(Wrapping)한 서비스입니다.
- **주요 기능**:
    - `clean`, `translate`, `scan`: Fortify 명령어를 실행합니다.
    - `executeCommand`: `ProcessBuilder`를 사용하여 시스템 쉘 명령을 실행하고 출력을 캡처합니다.
    - **Mock Mode**: 개발 환경을 위해 실제 분석 없이 성공/실패를 시뮬레이션하는 모드를 지원합니다.

#### **`SbomService.java`**
SBOM(소프트웨어 자재 명세서) 분석을 수행하고 결과를 처리합니다.
- **주요 기능**:
    - `requestAnalysis`: AI 기반 SBOM 분석 API에 분석을 요청합니다.
    - `fetchAndSaveResults`: 비동기적으로 분석 상태를 폴링(Polling)하고, 완료 시 JSON 결과를 가져와 저장합니다.
    - `parseComponent`, `parseAndEnrichVulnerability`: JSON 결과에서 컴포넌트 및 취약점 정보를 파싱 하여 DB에 적재합니다.

### 2. 보고서 및 결과 파싱 (Reporting & Parsing)

#### **`ReportParserService.java`**
Fortify 스캔 결과(XML)를 파싱 하여 데이터베이스 구조로 변환합니다.
- **주요 기능**:
    - `parseAndSave`: XML 리포트 파일을 읽어 `Issue` 태그 등을 분석하고 `Vulnerability` 엔티티로 변환하여 저장합니다.
    - XPath를 사용하여 XML 노드에서 필요한 정보를 추출합니다.

#### **`ReportService.java`**
분석 결과를 기반으로 사용자용 리포트(PDF 등)를 생성합니다. (BIRT 또는 JasperReports 연동 추정)

### 3. 통계 및 대시보드 (Stats & Dashboard)

#### **`StatsService.java`**
대시보드에 표시될 통계 데이터를 계산합니다.
- **주요 기능**:
    - 기간별 분석 횟수, 취약점 유형별 분포, 최근 트렌드 등의 데이터를 집계하여 반환합니다.

### 4. 시스템 및 유틸리티 (System & Utils)

#### **`FileService.java`**
파일 업로드 및 관리를 담당합니다.
- **주요 기능**:
    - 업로드된 소스 코드 파일(ZIP 등)의 저장, 압축 해제 등을 처리합니다.

#### **`SettingsService.java`**
시스템 설정을 관리합니다.
- **주요 기능**:
    - 분석 옵션 기본값, API 키 설정 등 `SystemSetting` 테이블의 데이터를 읽고 씁니다.

#### **`UserService.java`**
사용자 인증 및 계정 관리를 수행합니다.
- **주요 기능**:
    - 회원가입, 로그인 검증, 비밀번호 암호화(BCrypt) 등을 처리합니다.
