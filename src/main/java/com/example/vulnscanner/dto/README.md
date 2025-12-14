# DTO (Data Transfer Object)

## 목차 (Table of Contents)
- [파일 목록 및 설명](#파일-목록-및-설명)
    - [UnifiedResultDto.java](#unifiedresultdtojava)

---

이 디렉토리는 계층 간 데이터 교환을 위해 사용되는 객체들을 포함합니다. 엔티티(Entity)와 달리 순수하게 데이터를 전달하는 목적을 가집니다.

## 파일 목록 및 설명

### `UnifiedResultDto.java`
- **역할**: 통합 분석 결과 DTO.
- **주요 기능**:
  - SAST(정적 분석) 결과와 SBOM(오픈소스) 분석 결과를 통합된 형태로 화면에 보여주기 위해 사용됩니다.
  - `AnalysisResult`(SAST)와 `SbomResult`(SBOM) 엔티티의 공통 필드를 추출하여 목록 페이지 등에서 일관된 포맷으로 데이터를 전달합니다.
