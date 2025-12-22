# Entity Package

이 패키지는 애플리케이션의 데이터 모델(Entity)을 정의합니다. JPA(Java Persistence API)를 사용하여 데이터베이스 테이블과 매핑되며, 주요 도메인 객체의 상태와 관계를 관리합니다.

## 주요 Entity 구조 및 역할

### 1. 분석 결과 (Analysis Results)

#### **`AnalysisResult.java`**
SAST(정적 애플리케이션 보안 테스트) 분석 결과를 저장하는 엔티티입니다.
- **역할**: 소스 코드 분석의 메타데이터와 결과 요약, 취약점 목록을 관리합니다.
- **주요 필드**:
    - `analysisOption`: 분석에 사용된 옵션 정보 (1:1 관계).
    - `status`: 분석 상태 (`SUCCESS`, `FAILED`, `RUNNING`).
    - `vulnerabilities`: 발견된 취약점 리스트 (1:N 관계).
    - `scanSummary`: 분석 결과 요약 정보.
    - `logs`: 분석 관련 로그.
    - 파일 경로: `fprPath` (결과 파일), `reportPdfPath` (PDF 보고서), `sourceFilePath` (소스 코드) 등.
- **주요 로직**:
    - `getCriticalCount()`, `getHighCount()` 등: 포함된 취약점들의 심각도별 개수를 계산하여 반환하는 편의 메소드를 포함합니다.

#### **`SbomResult.java`**
SBOM(Software Bill of Materials) 분석 결과를 저장하는 엔티티입니다.
- **역할**: 오픈소스 컴포넌트 분석 결과와 라이선스 정보를 관리합니다.
- **주요 필드**:
    - `analysisOption`: 분석 옵션.
    - `status`: 분석 상태.
    - `components`: 분석된 컴포넌트 목록 (1:N 관계).
    - `vulnerabilitiesCount`, `licensesCount`, `componentsCount`: 결과 요약 카운트.
    - `jobId`: 외부 AI 분석 API와의 연동 ID.
    - `resultJsonPath`: 원본 JSON 결과 파일 경로.

### 2. 세부 데이터 모델

#### **`Vulnerability.java`**
`AnalysisResult`에 포함되는 개별 취약점 정보를 정의합니다.
- **필드**: `issueId`, `category`, `priority` (심각도), `filePath` (발견된 파일), `lineNumber`, `codeSnippet` (관련 코드).

#### **`SbomComponent.java`**
`SbomResult`에 포함되는 개별 컴포넌트 정보를 정의합니다.
- **필드**: `name`, `version`, `purl` (Package URL), `type` (라이브러리 유형).

#### **`AnalysisOption.java`**
분석 실행 시 설정된 옵션 정보를 저장합니다.
- **역할**: 분석 요청 시 사용자가 선택한 설정값을 보존하여 이력을 관리합니다.
- **필드**: `buildId`, `clean`, `translate`, `scan` 등 Fortify 및 스캔 세부 옵션.

### 3. 사용자 관리

#### **`User.java`**
시스템 사용자 정보를 관리하는 엔티티입니다.
- **역할**: 로그인, 권한 관리, 계정 잠금 기능 등을 지원합니다.
- **주요 필드**:
    - `username`, `password`: 인증 정보.
    - `role`: 사용자 권한 (`ROLE_USER`, `ROLE_ADMIN`).
    - `failedAttempts`, `lockTime`: 로그인 실패 시 계정 잠금 처리를 위한 필드.

### 4. 기타

- **`SystemSetting.java`**: 시스템 전역 설정값(예: API 키, 경로 설정 등)을 저장합니다.
- **`ScanSummary.java`**: SAST 스캔의 요약 통계(파일 수, 라인 수 등)를 저장합니다.
