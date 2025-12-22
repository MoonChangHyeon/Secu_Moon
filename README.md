# Secu_Moon Project (VulnScanner)

## 프로젝트 개요 (Project Overview)
**Secu_Moon**은 기업 환경을 위한 통합 보안 취약점 점검 및 관리 플랫폼입니다.
Fortify SAST(정적 분석) 도구와 연동하여 소스 코드의 보안 약점을 분석하고, 별도의 SBOM 분석 엔진을 통해 오픈소스 라이브러리의 취약점(CVE) 및 라이선스 위험을 식별합니다. 또한, 보안 규정 준수(Compliance) 매핑 정보를 시각화하여 감사를 지원합니다.

---

## 🚀 주요 기능 (Key Features)

### 1. 🔍 SAST (Static Application Security Testing)
- **Fortify 연동**: Fortify SourceAnalyzer를 활용한 정적 분석 자동화.
- **취약점 스캔**: 소스 코드 내 보안 취약점 식별 및 상세 리포트 제공.
- **이력 관리**: 분석 이력 조회 및 결과 파일(FPR) 다운로드.

### 2. 📦 SBOM (Software Bill of Materials) 분석
- **오픈소스 식별**: 프로젝트 내 오픈소스 라이브러리 및 버전 탐지.
- **취약점 매핑 (Mocha 연동)**:
    - 발견된 컴포넌트의 취약점(CVE, GHSA) 정보를 **한글 설명(Korean Description)**과 함께 제공.
    - 보안 전문 데이터베이스(`mocha_dev`)와 연동하여 최신 취약점 데이터 반영.
- **라이선스 점검**: SPDX 라이선스 정보를 기반으로 위험도(High/Medium/Low) 식별.

### 3. ✅ 규정 준수 뷰어 (Compliance Mapping Viewer) <!-- New -->
- **Fortify 룰팩 관리**: 보안 룰팩(External Metadata) XML 파일을 업로드하고 버전별로 관리.
- **계층적 조회**: 표준(Standard) > 카테고리(Category) > 매핑(Mapping) 구조를 트리 및 아코디언 UI로 제공.
- **버전 비교 (Diff)**: 두 룰팩 버전 간의 변경 사항(추가/삭제/수정)을 시각적으로 비교.
- **데이터 활용**:
    - 표준별 데이터 내보내기 (**CSV, XML, JSON** 포맷 지원).
    - 표준 ID 및 카테고리명 클립보드 복사 기능.

### 4. 📊 대시보드 및 통계
- **통합 대시보드**: 최근 분석 현황, 취약점 통계, 서버 상태 모니터링.
- **직관적 차트**: Chart.js를 활용한 취약점 유형별, 심각도별 시각화.

---

## 🛠 기술 스택 (Tech Stack)

| Category | Technology |
|:---:|:---|
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.x, Spring Data JPA, Spring Security |
| **Frontend** | Thymeleaf, Bootstrap 5, Javascript (ES6+) |
| **Database** | H2 (In-Memory/File, Main), MySQL/MariaDB (Security Data) |
| **Build Tool** | Gradle |
| **Analysis** | Fortify SCA (Static Code Analyzer) |

---

## 🏗 시스템 아키텍처 (Architecture)

### Dual Database Strategy (이중 데이터베이스 전략)
데이터 무결성과 전문화된 정보 제공을 위해 두 개의 데이터베이스를 운용합니다.

1.  **Primary Database (`vulnscanner_db`)**
    - **역할**: 애플리케이션 메인 저장소 (사용자, 분석 이력, 결과 저장).
    - **특징**: H2 Database 사용, 빠른 트랜잭션 처리.

2.  **Secondary Database (`mocha_dev`)**
    - **역할**: 보안 취약점 마스터 데이터 참조 (Read-Only).
    - **내용**: CVE, GHSA, SPDX License 정보 (한글 설명 및 상세 위험도 포함).
    - **기능**: SBOM 분석 시 실시간 데이터 보강(Enrichment)에 사용.

---

## 📂 디렉토리 구조 및 문서 (Documentation Index)

각 모듈별 상세 설명은 아래 링크를 참고하세요.

- **[Config (설정)](src/main/java/com/example/vulnscanner/config/README.md)**: 보안, DB 설정.
- **[Controller (API)](src/main/java/com/example/vulnscanner/controller/README.md)**: 요청 처리 로직.
- **[Service (로직)](src/main/java/com/example/vulnscanner/service/README.md)**: 비즈니스 로직.
- **[Repository (DB)](src/main/java/com/example/vulnscanner/repository/README.md)**: 데이터 접근 계층.
- **[Entity (모델)](src/main/java/com/example/vulnscanner/entity/README.md)**: 도메인 엔티티.
- **[Mocha (보안 데이터)](src/main/java/com/example/vulnscanner/mocha/README.md)**: 외부 DB 연동 모델.
- **[Templates (화면)](src/main/resources/templates/README.md)**: Thymeleaf 뷰 템플릿.

---

## 🚀 설치 및 실행 방법 (Getting Started)

### 1. 요구 사항 (Prerequisites)
- **Java JDK 17** 이상
- **Fortify SCA** (SAST 분석 기능을 사용하기 위해 필요)
- **Gradle** (Wrapper 내장)

### 2. 프로젝트 클론 & 빌드
```bash
git clone [repository_url]
cd vulnscanner
./gradlew build
```

### 3. 애플리케이션 실행
```bash
./gradlew bootRun
```
- 실행 후 브라우저에서 `http://localhost:8080` 접속.

### 4. 설정 (Configuration)
`src/main/resources/application.properties` 파일에서 데이터베이스 경로 및 포트 설정을 변경할 수 있습니다.

---
© 2024 Secu_Moon Project. All rights reserved.