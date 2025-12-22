# Repository Package

이 패키지는 데이터베이스 접근 계층(Data Access Layer)을 담당합니다. Spring Data JPA를 사용하여 Entity 객체에 대한 CRUD 작업 및 데이터베이스 쿼리를 수행합니다.

## 주요 Repository 구조 및 역할

### 1. 분석 결과 Repository

#### **`AnalysisRepository.java`**
`AnalysisResult` 엔티티를 관리하는 리포지토리입니다.
- **역할**: SAST 분석 결과의 저장, 조회, 삭제를 담당합니다.
- **주요 기능**: 기본적인 JpaRepository 메소드를 사용합니다.

#### **`SbomRepository.java`**
`SbomResult` 엔티티를 관리하는 리포지토리입니다.
- **역할**: SBOM 분석 결과의 영속성을 관리합니다.
- **주요 쿼리 메소드**:
    - `findTop5ByOrderByScanDateDesc()`: 대시보드 표시 등을 위해 최근 분석된 SBOM 결과 5건을 스캔 날짜 역순으로 조회합니다.

### 2. 시스템 및 사용자 Repository

#### **`UserRepository.java`**
`User` 엔티티를 관리하는 리포지토리입니다.
- **역할**: 사용자 정보 조회 및 인증 시 데이터 접근을 지원합니다.
- **주요 쿼리 메소드**:
    - `findByUsername(String username)`: 로그인 ID(username)를 기반으로 사용자 정보를 조회합니다 (Optional 반환).

#### **`SystemSettingRepository.java`**
`SystemSetting` 엔티티를 관리하는 리포지토리입니다.
- **역할**: 키-값 쌍으로 저장된 시스템 설정 정보를 관리합니다.
