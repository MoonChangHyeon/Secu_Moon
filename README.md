# 취약점 점검 시스템 (Vulnerability Scanner)

**취약점 점검 및 분석 자동화 플랫폼**입니다.
다양한 보안 도구(Fortify, SBOM 등)의 분석 결과를 통합 관리하고, 버전별 비교(Diff), 규정(Compliance) 매핑, 리포트 생성 기능을 제공합니다.

---

## 📚 목차 (Index)

- **[모듈별 상세 문서 (Modules)](src/main/java/com/example/vulnscanner/module/README.md)**
    - **[Fortify 모듈](src/main/java/com/example/vulnscanner/module/fortify/README.md)**: 언어별 취약점 탐색, 버전 비교, 리포트(JSON/XML/CSV) 다운로드.
    - **[Compliance 모듈](src/main/java/com/example/vulnscanner/module/compliance/README.md)**: 외부 보안 규정 매핑 및 뷰어 (Side-bar 레이아웃).
    - **[Analysis 모듈](src/main/java/com/example/vulnscanner/module/analysis/README.md)**: 취약점 분석 요청 및 결과 리포팅.
    - **[SBOM 모듈](src/main/java/com/example/vulnscanner/module/sbom/README.md)**: 오픈소스 라이브러리 및 라이선스 분석.
    - **[User & Settings](src/main/java/com/example/vulnscanner/module/user/README.md)**: 사용자 관리 및 시스템 설정.

---

## 🚀 주요 기능 (Key Features)

### 1. Fortify 취약점 탐색 (Exploration)
- **언어별 조회**: Java, Python, C++ 등 언어별 취약점 필터링.
- **버전 비교 (Diff)**: 날짜별 분석 결과 비교 (NEW, REMOVED, MODIFIED 상태 표시).
- **상세 정보**: 취약점 설명, 해결 방안, 외부 링크 제공.
- **리포트 다운로드**: JSON, XML, CSV 포맷으로 분석 결과 내보내기.

### 2. 규정 매핑 (Compliance Mapping)
- **표준 뷰어**: ISMS-P, ISO27001 등 보안 표준별 항목 조회.
- **취약점 연동**: 각 컴플라이언스 항목에 매핑된 Fortify 취약점 확인.

### 3. 통합 대시보드
- 분석 현황, 최근 이슈, 취약점 통계 시각화.

---

## 🛠 기술 스택 (Tech Stack)
- **Backend**: Java 17, Spring Boot 3.x, JPA/Hibernate
- **Database**: MariaDB 10.x
- **Frontend**: Thymeleaf, Bootstrap 5, Vanilla JS
- **Build**: Gradle (Kotlin DSL)

---

## 📂 프로젝트 구조 (Project Structure)

```
src/main/java/com/example/vulnscanner/
├── module/             # 비즈니스 기능 모듈
│   ├── fortify/        # [NEW] Fortify 취약점 탐색 및 리포팅
│   ├── compliance/     # [UPDATED] 규정 매핑 및 뷰어
│   ├── analysis/       # 분석 실행 및 결과 관리
│   ├── sbom/           # SBOM 관리
│   ├── user/           # 사용자 및 권한
│   ├── settings/       # 시스템 설정
│   └── mocha/          # 외부/레거시 DB 연동
└── global/             # 전역 공통 요소
    ├── config/         # Security, MVC, Batch 설정
    ├── util/           # 공통 유틸리티 (File, Date 등)
    └── exception/      # Global Exception Handler
```

---

## 🚀 시작하기 (Getting Started)

### 빌드 및 실행
```bash
# 프로젝트 빌드
./gradlew clean build

# 애플리케이션 실행
./gradlew bootRun
```
앱 실행 후 브라우저에서 `http://localhost:8080` 접속.