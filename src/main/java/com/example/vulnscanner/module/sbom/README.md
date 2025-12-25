# SBOM Module

`com.example.vulnscanner.module.sbom` 패키지는 소프트웨어 자재 명세서(SBOM)를 관리하고 분석하는 기능을 제공합니다.

## 🔑 주요 역할 (Key Responsibilities)

1.  **SBOM 업로드 및 파싱 (`SbomService`)**
    - CycloneDX 등의 형식으로 된 SBOM 파일 업로드
    - JSON 파싱을 통한 컴포넌트(라이브러리), 라이선스, 취약점 정보 추출
2.  **오픈소스 분석 (`SbomResult`)**
    - 프로젝트 내 사용된 오픈소스 현황 파악
    - 컴포넌트별 버전 및 PURL(Package URL) 식별
3.  **취약점 연동**
    - 외부 DB(Mocha)와 연계된 취약점 상세 정보(CVE, GHSA 등) 매핑
    - 취약점의 심각도(Severity) 및 해결 방안(Description) 제공

## 📄 주요 클래스 (Key Classes)

- **Controller**: `SbomController`
- **Service**: `SbomService`
- **Entity**: `SbomResult`, `SbomComponent`, `SbomLicense`, `SbomVulnerability`
- **Repository**: `SbomRepository`

## 🔗 연관 뷰 (Templates)
- `templates/sbom/**`: SBOM 결과 상세, 재파싱 기능
