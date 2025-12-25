# Compliance Module

이 모듈은 컴플라이언스 룰팩 업로드, 파싱, 조회 및 버전 간 비교 기능을 담당합니다.
사용자는 표준(Standard), 카테고리(Category), 매핑(Mapping) 정보를 계층적으로 관리하고 분석할 수 있습니다.

## 주요 기능 (Key Features)

### 1. 룰팩 뷰어 (Rulepack Viewer)
- 업로드된 룰팩의 상세 정보를 제공합니다.
- 표준별 CSV, XML, JSON 형식의 개별 데이터 다운로드를 지원합니다.
- HTML 뷰어를 통해 계층적인 구조(표준 > 카테고리 > 매핑)를 시각적으로 탐색할 수 있습니다.

### 2. 룰팩 버전 비교 (Version Comparison)
- 서로 다른 두 룰팩 버전을 선택하여 차이점을 분석합니다.
- **분석 유형**:
  - `ADDED`: 신규 표준, 카테고리 또는 매핑 추가
  - `DELETED`: 기존 항목 삭제
  - `MODIFIED`: 기존 항목의 내용 변경 (메타데이터 수정 등)
- **UI 구성**:
  - **요약 통계**: 추가, 삭제, 변경 건수 요약 표시
  - **필터링**: 상태별 필터링 기능 (Added/Deleted/Modified)
  - **상세 뷰**: 각 표준 항목 클릭 시 팝업(`compare_detail.html`)을 통해 상세 변경 내역(카테고리/매핑) 확인 가능
  - **그룹화**: 매핑된 카테고리를 `:` 구분자를 기준으로 그룹화하여 시각적 가독성 향상

### 3. 비교 이력 관리 (Comparison History)
- 과거에 수행한 비교 분석 결과를 저장하고 조회할 수 있습니다.
- 불필요한 비교 이력은 삭제 가능합니다.

## 컴포넌트 구성 (Components)

### Controller
- **`ComplianceController`**:
  - `/compliance`: 룰팩 목록 및 업로드 화면
  - `/compliance/viewer/{id}`: 룰팩 상세 뷰어
  - `/compliance/compare`: 버전 비교 페이지
  - `/compliance/compare/{baseId}/{targetId}/standard/{standardId}`: 표준별 비교 상세 팝업
  - `/api/compliance/compare`: 비교 분석 수행 API
  - `/api/compliance/history`: 이력 조회 및 삭제 API

### Service
- **`ComplianceComparisonService`**:
  - 두 룰팩(`PackInfo`) 간의 데이터를 비교하여 `ComplianceDiffDto`를 생성합니다.
  - 비교 결과를 `ComplianceComparisonResult` 엔티티로 저장하고 관리합니다.
  - **비교 로직**:
    1. Base 팩과 Target 팩의 표준 목록을 로드합니다.
    2. 표준 이름(`name`)을 기준으로 매칭합니다.
    3. 동일한 표준 내에서 카테고리 및 매핑 변화를 재귀적으로 분석합니다.
- **`RulepackService`**:
  - Excel 파일 파싱 및 DB 저장, 조회 로직을 처리합니다.

### DTO & Entities
- **DTO**:
  - `ComplianceDiffDto`: 비교 결과 데이터 전송 객체 (StandardDiff > CategoryDiff > MappingDiff 구조)
    - Helper 메서드(`getAddedCount`, `getDeletedCount` 등)를 포함하여 뷰 렌더링을 지원합니다.
- **Entities**:
  - `PackInfo`: 룰팩 메타데이터
  - `ComplianceStandard`, `ComplianceCategory`, `ComplianceMapping`: 계층적 컴플라이언스 데이터
  - `ComplianceComparisonResult`: 비교 결과/이력 저장 (JSON 포맷으로 결과 상세 저장)

## 디렉토리 구조
- `ComplianceController.java`: 웹 요청 처리
- `ComplianceComparisonService.java`: 비교 비즈니스 로직
- `RulepackService.java`: 룰팩 관리 로직
- `ComplianceDiffDto.java`: 계층적 비교 결과 모델
