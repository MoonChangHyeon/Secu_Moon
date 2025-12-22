# DTO Package

이 패키지는 계층 간 데이터 교환을 위한 객체(Data Transfer Object)를 정의합니다. 엔티티와 달리 로직을 포함하지 않으며, 주로 View Layer에 데이터를 전달하거나 API 응답 구조를 정의하는 데 사용됩니다.

## 주요 클래스

- **`UnifiedResultDto.java`**: 서로 다른 분석 결과(SAST, SBOM)를 통일된 형식으로 클라이언트에 전달하기 위한 DTO입니다. 대시보드나 통합 리포트 등에서 활용될 수 있습니다.
