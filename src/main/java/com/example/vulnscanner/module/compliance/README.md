# Compliance Module

보안 규정(Compliance) 룰팩을 관리하고, 표준(Standard)-카테고리(Category)-매핑(Mapping)의 계층적 구조를 제공하는 모듈입니다.

---

## 🔥 주요 기능 (Features)

### 1. 룰팩 뷰어 (Rulepack Viewer)
- **2단 레이아웃 구조**:
    - **Sidebar**: 카테고리 목록을 직관적으로 탐색.
    - **Main Content**: 선택된 카테고리의 세부 항목 리스트 표시.
- **실시간 검색**: 표준명, 설명 텍스트 기반의 즉시 필터링(Client-side).
- **데이터 다운로드**: 표준별 CSV/XML 데이터 내보내기 지원.
- **페이징**: 대용량 리스트도 효율적으로 볼 수 있는 페이지네이션.

### 2. 룰팩 버전 비교 (Version Diff)
- 두 룰팩 버전 간의 차이점(Added, Deleted, Modified)을 분석합니다.
- **그룹화 뷰**: 매핑된 카테고리를 `:` 구분자로 그룹화하여 변경 사항을 쉽게 파악할 수 잇습니다.
- **상세 팝업**: 변경된 표준의 세부 내역(카테고리/매핑 레벨) 확인 가능.

### 3. 비교 이력 관리 (History)
- 과거 비교 분석 이력을 저장하고 언제든 다시 조회할 수 있습니다.

---

## 🛠 기술 명세 (Technical Specs)

### Components
- **Controller**: `ComplianceController` (뷰 및 비교 API), `RulepackController` (업로드/파싱).
- **Service**: 
    - `ComplianceComparisonService`: 룰팩 간 Diff 로직 수행.
    - `RulepackService`: Excel 파싱 및 DB 저장.
- **Domain**: `PackInfo` (Root), `ComplianceStandard` (1:N), `ComplianceCategory` (1:N), `ComplianceMapping` (1:N).

### Frontend
- **`viewer.html`**: Thymeleaf 기반의 2-Column 레이아웃, JS 필터링.
- **`compare.html`**: 버전 비교 요약 및 상세 조회 UI.

---

## 📂 디렉토리 구조
- `ComplianceController.java`: 웹 요청 핸들러.
- `RulepackService.java`: 룰팩 파싱 및 관리.
- `ComplianceComparisonService.java`: 버전 비교 비즈니스 로직.
- `dto/`: 비교 결과(Diff) 전송을 위한 DTO 객체들.
