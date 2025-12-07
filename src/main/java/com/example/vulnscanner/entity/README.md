# 엔티티 (Entity)

이 디렉토리는 데이터베이스 테이블과 매핑되는 **JPA Entity** 클래스들을 포함합니다. 애플리케이션의 핵심 도메인 모델을 정의합니다.

## 파일 목록 및 설명

### `User.java`
- **역할**: 사용자 정보 엔티티.
- **매핑 테이블**: `users`
- **속성**: 사용자명(ID), 비밀번호, 이름, 이메일, 역할(Role), 팀, 계정 잠금 상태 등.

### `AnalysisResult.java`
- **역할**: SAST(정적 취약점) 분석 결과 엔티티.
- **매핑 테이블**: `analysis_results`
- **속성**: 빌드 ID, 분석 상태, 시작/종료 시간, 로그 파일 경로, 보고서(PDF/XML) 경로 등.
- **관계**: `ScanSummary`, `Vulnerability`와 연관 관계를 가짐.

### `AnalysisOption.java`
- **역할**: 분석 시 사용된 옵션 정보 (Embeddable).
- **속성**: 소스 경로, 빌드 ID, 메모리 설정, JDK 버전 등 분석 요청 시 입력된 파라미터.

### `ScanSummary.java`
- **역할**: 분석 결과 요약 정보 엔티티.
- **매핑 테이블**: `scan_summary`
- **속성**: 전체 파일 수, 코드 라인 수(LOC), 발견된 총 취약점 수, 심각도별(Critical/High/Medium/Low) 개수 등.

### `Vulnerability.java`
- **역할**: 발견된 개별 취약점 상세 정보 엔티티.
- **매핑 테이블**: `vulnerabilities`
- **속성**: 취약점 카테고리, 파일명, 라인 번호, 심각도 등 상세 내용.

### `SbomResult.java`
- **역할**: SBOM(오픈소스) 분석 결과 엔티티.
- **매핑 테이블**: `sbom_results`
- **속성**: 분석 상태, 외부 API Job ID, 결과 JSON 데이터(Logs에 저장), 생성된 SBOM 파일 경로 등.

### `SbomComponent.java`
- **역할**: SBOM 분석에서 식별된 오픈소스 컴포넌트 정보.
- **속성**: 컴포넌트 이름, 버전, 라이선스, PURL(Package URL) 등. (현재 로직에 따라 JSON 파싱 후 DB 저장 여부 결정)

### `SystemSetting.java`
- **역할**: 시스템 동적 설정 값 엔티티.
- **매핑 테이블**: `system_settings`
- **속성**: 설정 키(Key), 설정 값(Value), 설명. (예: 파일 업로드 제한, 외부 API URL 등 저장)
