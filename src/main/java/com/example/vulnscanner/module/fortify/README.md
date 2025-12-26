# Fortify Vulnerability Explorer Module

이 모듈은 Fortify SAST의 언어별 취약점 데이터를 탐색하고 관리하는 기능을 제공합니다.

---

## 🔥 주요 기능 (Features)

### 1. 데이터 업로드 및 관리
- `fortify_data_YYYYMMDD.zip` 형식의 ZIP 파일을 업로드하여 날짜별 데이터셋을 생성합니다.
- 업로드된 파일은 분류 후 `data/fortify/{YYYYMMDD}` 경로에 저장됩니다.

### 2. 언어별 취약점 탐색 (Vulnerability Explorer)
- **언어 선택**: Java/JSP, Python, C/C++ 등 분석된 언어별로 취약점을 조회합니다.
- **검색 (Search)**: ID 또는 취약점 제목으로 실시간 검색(Client-side filter)을 지원합니다.
- **요약 정보**: 선택한 언어의 총 취약점 수와 가장 많이 발견된 취약점 유형을 요약 표시합니다.

### 3. 버전 비교 (Diff Analysis)
- 서로 다른 두 날짜의 분석 결과를 비교하여 변경 사항을 추적합니다.
- **상태 구분**:
    - <span style="background-color:green; color:white; padding:2px 4px; border-radius:3px;">NEW</span>: 새로 추가된 취약점
    - <span style="background-color:red; color:white; padding:2px 4px; border-radius:3px;">REMOVED</span>: 삭제된(해결된) 취약점
    - <span style="background-color:orange; color:white; padding:2px 4px; border-radius:3px;">MODIFIED</span>: 속성이 변경된 취약점

### 4. 리포트 다운로드 (Export)
- 현재 조회 중인 언어/날짜의 취약점 목록을 다양한 포맷으로 다운로드할 수 있습니다.
- **지원 포맷**: JSON, XML, CSV.

### 5. Compliance 매핑 여부 확인
- 조회된 취약점이 내부 관리 중인 보안 규정(Compliance)에 매핑되어 있는지 시각적으로 표시합니다.
- `Mapped` 배지가 표시된 항목은 상세 규정 가이드를 참조할 수 있음을 의미합니다.

---

## 🛠 기술 명세 (Technical Specs)

### Classes
- **`FortifyModuleController`**:
    - `/fortify` (View): 메인 탐색 페이지.
    - `/fortify/export`: 데이터 내보내기 API.
    - `/fortify/upload`: 파일 업로드 처리.
- **`FortifyModuleService`**: ZIP 파싱, 날짜별 폴더 관리, Diff 로직 구현.
- **`WeaknessItem` (DTO)**: 취약점 데이터 모델 (ID, Title, Abstract 등).

### Frontend
- **`list.html`**:
    - Fetch API를 이용한 비동기 데이터 로딩.
    - Vanilla JS 기반의 Client-side Search & Pagination 구현.
    - Bootstrap 5 기반의 반응형 레이아웃.

---

## 📂 데이터 구조
- **Source**: `fortify_{Language}.json` 파일들.
- **Storage**: 서버 로컬 파일 시스템 `data/fortify/{YYYYMMDD}/` 디렉토리.
