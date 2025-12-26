# 취약점 점검 시스템 (Vulnerability Scanner)

- **[Fortify 모듈 (Exploration)](src/main/java/com/example/vulnscanner/module/fortify/README.md)**: 언어별 취약점 데이터 업로드/탐색, **데이터 버전 비교(Diff)** 및 Compliance 매핑 연동.

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