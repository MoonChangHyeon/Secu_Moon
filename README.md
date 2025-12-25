# 취약점 점검 시스템 (Vulnerability Scanner)

**Vulnerability Scanner**는 애플리케이션 보안을 강화하기 위한 통합 관리 시스템입니다. 소스 코드 취약점 분석(SAST), 소프트웨어 구성 분석(SBOM), 그리고 보안 규정 준수(Compliance) 관리를 하나의 플랫폼에서 제공합니다.

## 📋 목차 (Index)

이 프로젝트는 기능(Feature) 단위의 모듈로 구성되어 있습니다. 각 링크를 통해 상세 문서를 확인할 수 있습니다.

### 📦 핵심 모듈 (Modules)
- **[분석 모듈 (Analysis)](src/main/java/com/example/vulnscanner/module/analysis/README.md)**: SAST 분석 요청, 결과 관리, Fortify 연동, 통계 대시보드.
- **[SBOM 모듈 (Software Bill of Materials)](src/main/java/com/example/vulnscanner/module/sbom/README.md)**: SBOM 파일 업로드, 파싱, 오픈소스 취약점 및 라이선스 관리.
- **[규정 준수 모듈 (Compliance)](src/main/java/com/example/vulnscanner/module/compliance/README.md)**: 보안 규정 관리, 표준/카테고리/매핑 데이터 시각화, **룰팩 버전 비교 및 상세 이력 분석**.
- **[사용자 모듈 (User)](src/main/java/com/example/vulnscanner/module/user/README.md)**: 로그인, 사용자 권한 및 계정 관리.
- **[설정 모듈 (Settings)](src/main/java/com/example/vulnscanner/module/settings/README.md)**: 시스템 환경 설정, 파일 경로 및 업로드 정책 관리.
- **[Mocha 모듈](src/main/java/com/example/vulnscanner/module/mocha/README.md)**: 외부 취약점 데이터베이스(Mocha) 연동 엔티티 및 리포지토리.

### ⚙️ 공통 및 설정 (Global)
- **[Global Components](src/main/java/com/example/vulnscanner/global/README.md)**: 전역 설정(Security, JPA), 유틸리티, 예외 처리 핸들러.

### 🖥️ Frontend
- **[Templates](src/main/resources/templates/README.md)**: Thymeleaf 뷰 템플릿 구조 및 화면 설명.

---

## 🚀 시작하기 (Getting Started)

### 사전 요구사항 (Prerequisites)
- Java 17+
- MariaDB 10.x+
- Gradle (Wrapper 포함)

### 빌드 및 실행 (Build & Run)
```bash
# 프로젝트 빌드
./gradlew clean build

# 애플리케이션 실행
./gradlew bootRun
```
애플리케이션이 시작되면 브라우저에서 `http://localhost:8080`으로 접속할 수 있습니다.

---

## 📂 프로젝트 구조 (Project Structure)

리팩토링된 프로젝트는 **Package-by-Feature** 전략을 따릅니다.

```
src/main/java/com/example/vulnscanner/
├── module/             # 비즈니스 기능 모듈
│   ├── analysis/       # 분석 기능
│   ├── compliance/     # 규정 기능
│   ├── sbom/           # SBOM 기능
│   ├── user/           # 사용자 기능
│   ├── settings/       # 설정 기능
│   └── mocha/          # 외부 DB 연동
└── global/             # 전역 공통 요소
    ├── config/         # Spring 설정
    ├── util/           # 유틸리티
    └── exception/      # 예외 처리
```