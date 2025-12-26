# Fortify Vulnerability Explorer Module

이 모듈은 Fortify SAST의 언어별 취약점 데이터를 탐색하고 관리하는 기능을 제공합니다.

## 기능 (Features)

1.  **데이터 업로드 및 관리**
    - `fortify_data_YYYYMMDD.zip` 형식의 ZIP 파일을 업로드하여 날짜별로 데이터를 저장합니다.
    - 서버의 `data/fortify/{YYYYMMDD}` 경로에 압축이 해제되어 저장됩니다.

2.  **언어별 취약점 탐색**
    - 특정 날짜의 데이터셋을 선택하고, 지원되는 언어(예: Java/JSP, Python 등)를 선택하여 해당 언어의 취약점 목록을 조회합니다.
    - 각 취약점의 ID, 제목, 요약(Abstract), Fortify VulnCat 외부 링크를 제공합니다.

3.  **버전 비교 (Diff)**
    - 서로 다른 두 날짜의 데이터를 비교하여 **추가됨(NEW)**, **삭제됨(REMOVED)**, **변경됨(MODIFIED)** 상태의 취약점을 식별합니다.
    - 이를 통해 새로운 Fortify Rulepack 업데이트 시 변경 사항을 추적할 수 있습니다.

4.  **Compliance Rulepack 매핑 확인**
    - 조회된 취약점이 `ComplianceMapping` 테이블에 존재하는지 확인하여, 규정 준수(Compliance) 모듈과의 연동성을 시각적으로 표시합니다.

## 주요 클래스 (Classes)

- **FortifyController**: API 엔드포인트 및 View 반환.
- **FortifyService**: ZIP 파일 처리, JSON 파싱, 버전 비교 로직 담당.
- **WeaknessItem**: 개별 취약점 정보를 담는 DTO.
- **FortifyWeaknessResponse**: JSON 파일의 최상위 구조 DTO.

## 데이터 구조 (Data Structure)

- **Source**: JSON Files (`fortify_{Language}.json`)
- **Storage**: Local File System (`data/fortify/{Date}/`)
