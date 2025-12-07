# 레포지토리 (Repository)

이 디렉토리는 데이터베이스 접근을 위한 **Spring Data JPA Repository** 인터페이스들을 포함합니다. 기본적인 CRUD 기능과 커스텀 쿼리 메소드를 제공합니다.

## 파일 목록 및 설명

### `UserRepository.java`
- **역할**: `User` 엔티티 접근 리포지토리.
- **주요 기능**: 사용자명(username)으로 사용자 조회, 이메일 중복 체크 등 사용자 인증 및 관리에 필요한 쿼리 제공.

### `AnalysisRepository.java`
- **역할**: `AnalysisResult` (SAST 결과) 엔티티 접근 리포지토리.
- **주요 기능**: 분석 결과 저장, 빌드 ID로 조회, 날짜순 정렬 조회 등 분석 데이터 관리.

### `SbomRepository.java`
- **역할**: `SbomResult` (SBOM 결과) 엔티티 접근 리포지토리.
- **주요 기능**: 최근 SBOM 분석 이력 조회(Top 5) 등 관련 쿼리 제공.

### `SystemSettingRepository.java`
- **역할**: `SystemSetting` 엔티티 접근 리포지토리.
- **주요 기능**: 키(Key) 값을 기반으로 시스템 설정값을 조회하거나 수정/저장하는 기능 제공.
