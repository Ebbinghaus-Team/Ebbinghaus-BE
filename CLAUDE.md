# CLAUDE.md

This file provides guida nce to Claude Code (claude.ai/code) when working with code in this repository.

## 1\. 프로젝트 개요

\*\*Ebbinghaus-BE (또뿔래, ttopullae)\*\*는 에빙하우스의 망각 곡선에 기반한 '간헐적 복습' 학습 시스템을 구현한 Spring Boot 3.5.7 백엔드 서비스입니다. 이 서비스는 사용자가 직접 학습 문제를 생성하고, **"Two-Strike" 모델**(1일 및 7일 복습 관문)을 통해 복습하도록 하여 '능동적 인출(Active Recall)'과 '장기 기억'을 촉진합니다.

자세한 프로젝트 내용은 @PRD.md 를 참고

## 2\. 기술 스택

- **언어**: Java 21
- **프레임워크**: Spring Boot 3.5.7
- **데이터**: Spring Data JPA (Hibernate 포함), MySQL 8.0 (운영), H2 (테스트)
- **빌드**: Gradle
- **기타**: Spring Validation, Lombok

## 3\. 아키텍처

백엔드 단일 리포지토리 구성입니다.

### 3.1. 백엔드 아키텍처

백엔드는 도메인 주도 설계(DDD)를 따르는 계층형 아키텍처를 사용하며, 각 도메인(Bounded Context)이 명확히 분리되어 있습니다.

```
com.ebbinghaus.ttopullae/
├── user/
│   ├── presentation/    # User API 컨트롤러
│   ├── application/     # User 비즈니스 로직 (서비스)
│   ├── domain/          # User 도메인 모델, 엔티티, 리포지토리
│   └── exception/       # User 관련 예외
├── studyroom/
│   ├── presentation/    # Studyroom API 컨트롤러
│   ├── application/     # Studyroom 비즈니스 로직
│   ├── domain/          # Studyroom 도메인 모델
│   └── exception/       # Studyroom 관련 예외
├── problem/
│   ├── presentation/    # Problem API 컨트롤러
│   ├── application/     # Problem 비즈니스 로직
│   ├── domain/          # Problem 도메인 모델
│   └── exception/       # Problem 관련 예외
└── global/
    ├── config/          # 전역 설정 (Security, JPA Auditing 등)
    ├── BaseTimeEntity/  # 공통 엔티티 (createdAt)
    ├── exception/       # 전역 예외 처리 핸들러
    └── util/            # 공통 유틸리티 클래스
```

**주요 아키텍처 패턴**:

- **도메인 주도 설계(DDD)**: `user`, `studyroom`, `problem` 등 명확한 Bounded Context로 분리.
- **계층형 아키텍처**: `presentation` -\> `application` -\> `domain` 의 명확한 의존성 방향.
- **JPA Auditing**: `BaseTimeEntity`를 통해 모든 엔티티의 `createdAt` 자동 추적.
- **지연 로딩(Lazy Loading)**: `FetchType.LAZY`를 기본으로 사용하여 N+1 쿼리 문제 방지.

### 3.2. 핵심 도메인 구조

1.  **User 도메인** (`com.ebbinghaus.ttopullae.user`)
    - 사용자 계정, 이메일 알림, 환경 설정 관리
2.  **Study Room 도메인** (`com.ebbinghaus.ttopullae.studyroom`)
    - **StudyRoom**: 개인 또는 그룹 스터디 공간
    - **RoomType**: `PERSONAL` 또는 `GROUP`
    - **StudyRoomMember**: 그룹 멤버십을 위한 조인 테이블
    - 개인방은 소유자 1명, 그룹방은 고유 참여 코드를 사용
3.  **Problem 도메인** (`com.ebbinghaus.ttopullae.problem`)
    - **Problem**: 사용자가 생성한 학습 문제
    - **ProblemType**: `MULTIPLE_CHOICE` (객관식), `TRUE_FALSE` (OX), `SHORT_ANSWER` (단답형), `ESSAY` (서술형)
    - **ProblemChoice**: 객관식 선택지
    - **ProblemKeyword**: AI 기반 서술형 채점을 위한 키워드
    - **ProblemAttempt**: 각 문제 풀이 시도 기록
    - **ProblemReviewState**: 문제별 개별 사용자의 간헐적 복습 상태 추적
4.  **Global 도메인** (`com.ebbinghaus.ttopullae.global`)
    - **BaseTimeEntity**: 모든 엔티티에 `createdAt` 타임스탬프를 제공하는 추상 클래스

### 3.3. "Two-Strike" 간헐적 복습 모델

문제는 사용자의 성과에 따라 다음 복습 관문을 거칩니다.

1.  **GATE\_1** (1일차 복습): 생성 또는 이전 시도 후 1일 뒤에 문제 등장
2.  **GATE\_2** (7일차 복습): GATE\_1 통과 후 7일 뒤에 문제 등장
3.  **GRADUATED** (졸업): GATE\_2 통과 후 문제 완료

**강등 규칙**: 오답 시 문제를 이전 관문으로 강등시켜, 어려운 자료에 반복적으로 노출되도록 보장합니다.

### 3.4. 주요 디자인 패턴

- **엔티티 관계**:
    - 모든 엔티티는 `BaseTimeEntity`를 상속하여 `createdAt` 자동 추적
    - 메인 애플리케이션 클래스의 `@EnableJpaAuditing` 활성화
    - 성능 최적화를 위해 `@ManyToOne` 관계에 지연 로딩(Lazy fetching) 사용
    - '오늘의 복습' 쿼리 효율화를 위해 `ProblemReviewState`의 `(user_id, nextReviewDate)`에 인덱스 설정
- **문제 소유권**:
    - **개인방 문제**: 생성자 소유, 1일 후 복습 주기에 자동 등록
    - **그룹방 문제 (생성자)**: 개인방 문제와 동일하게 취급, 복습 주기에 자동 등록
    - **그룹방 문제 (타인)**: 첫 번째 풀이 시도 후에만 복습 주기에 등록됨

-----

## 4\. 핵심 비즈니스 규칙

1.  **복습 주기 개인화**: 그룹 스터디라도 모든 문제에 대해 사용자별 `ProblemReviewState`를 가집니다.
2.  **그룹 문제 활성화**: 다른 멤버의 그룹 문제를 (정답 여부와 관계없이) 풀면, 해당 문제가 개인 복습 주기에 등록됩니다.
3.  **복습 이월**: 완료하지 않은 '오늘의 복습' 문제는 다음 날로 자동 이월됩니다.
4.  **일일 제한 없음**: 사용자는 하루에 무제한으로 복습을 완료할 수 있습니다.
5.  **이메일 알림**: 복습할 문제가 있는 사용자에게 매일 오전 8시(KST)에 알림 메일이 발송됩니다.

-----

## 5\. 개발 규칙

### 5.1. 코딩 스타일

- [구글 자바 스타일 가이드](https://google.github.io/styleguide/javaguide.html) 를 컨벤션으로 한다.
- 축약어를 쓰지 않고, 네이밍을 보고도 어떤 역할을 하는지 쉽게 알 수 있어야 한다.
- 가독성이 좋은 코드를 작성한다.
- 하나의 메서드에 모든 로직을 몰아넣지 않는다.  
  메서드는 단일 책임을 가지도록 구성하고, 복잡해지기 시작하면 과감히 분리한다.
- 메서드는 이름(네이밍), 파라미터, 반환 타입만 보아도 어떤 역할을 수행하는지 이해할 수 있어야 한다.  
  즉, 시그니처 자체가 메서드의 의도를 명확하게 드러내야 한다.
- 복잡한 기능이 있다면 한글로 짧막한 주석을 추가한다.
- DTO는 `record` 를 사용한다.
- **DTO 네이밍 규칙**은 다음의 데이터 흐름을 따른다:
    - `Client -> Controller`: **`...Request`** (예: `ProblemCreateRequest`)
    - `Controller -> Service`: **`...Command`** (서비스 계층에 작업을 지시하는 객체, 예: `ProblemCreateCommand`)
    - `Service -> Controller`: **`...Result`** 또는 **Domain 객체** (서비스 처리 결과)
    - `Controller -> Client`: **`...Response`** (예: `ProblemDetailResponse`)

### 5.2. Git 규칙

- 깃 허브 저장소: `https://github.com/Ebbinghaus-Team/Ebbinghaus-BE`
- Main Branch: `main`
- 깃 컨벤션은 'Conventional Commits' 를 따른다.
- 커밋 메세지는 한글로 작성한다.
- 구현한 기능을 한번에 커밋하지 않고 단계별로 끊어서 커밋한다.
- 커밋이 특정 이슈를 해결하거나 관련된 작업을 포함하는 경우, 커밋 메시지 제목 끝에 해당 이슈 번호를 `[#[번호]]` 형식으로 명시한다.
  - 예: "feat: 로그인 인터셉터 구현 [#12]"
- PR을 작성할 떄는 템플릿(`.github/PULL_REQUEST_TEMPLATE.md`)을 활용한다.

### 5.2.1 브랜치 전략 (Branch Strategy)

본 프로젝트는 개발자별 작업 분리를 명확히 하고 브랜치 히스토리를 안정적으로 추적하기 위해  
**"GitHub 사용자명 / Conventional Prefix - 브랜치 번호 - 기능 설명(kebab-case)"** 형식을 표준 브랜치 네이밍 규칙으로 사용한다.

형식:
{github-username}/{prefix}-{branch-number}-{kebab-case-description}

#### 규칙 상세

1. **GitHub 사용자명을 접두사로 반드시 포함한다.**
    - 예: `hyerimh`, `chxghee` 등

2. **브랜치 Prefix는 Conventional Commits 규칙을 따른다.**
    - `feat`: 기능 개발
    - `fix`: 버그 수정
    - `refactor`: 리팩토링
    - `chore`: 설정, 빌드, CI 등 기타 작업
    - `docs`: 문서 작성 및 수정

3. **브랜치 번호(branch-number)는 개발자별로 독립적으로 증가하는 일련 번호이다.**
    - 개발자가 만든 브랜치의 순서에 따라 1부터 증가
    - 예: 첫 기능 개발 → `feat-1`, 두 번째 → `feat-2`

4. **기능 설명은 kebab-case로 작성하며, 간결하고 명확해야 한다.**
    - 예: `generate-entity`, `implement-review-logic`, `update-studyroom-api`

---

#### 예시

| 개발자     | 목적                 | 브랜치 이름 |
|------------|----------------------|-------------|
| hyerimh    | 엔티티 설계          | `hyerimh/feat-1-generate-entity` |
| chxghee  | 문제 생성 API 구현   | `chxghee/feat-3-create-problem-api` |
| chxghee    | 복습 로직 버그 수정 | `chxghee/fix-2-review-schedule-bug` |
| hyerimh    | DDL 스키마 수정      | `hyerimh/chore-4-update-schema` |
| hyerimh      | 서비스 레이어 리팩토링 | `hyerimh/refactor-5-service-layer` |

---

#### 브랜치 병합 규칙

- 모든 브랜치는 `main`에 직접 push할 수 없으며 **반드시 Pull Request를 통해 병합한다.**
- 리뷰어 **1명 이상 승인 필수**
- 강제 push 금지 (force push)
- 브랜치 삭제는 PR 머지 후 GitHub에서 자동 삭제 옵션 사용을 권장




### 5.3. 예외 처리 (Exception Handling)

프로젝트는 **도메인별 Enum 기반 예외 관리 시스템**을 사용합니다. 모든 예외는 `GlobalExceptionHandler`에서 전역적으로 처리되며, 클라이언트에게 일관된 형식의 `ErrorResponse`를 반환합니다.

#### 5.3.1. 예외 처리 아키텍처

```
global/exception/
├── ExceptionCode.java           # 예외 코드 인터페이스 (HttpStatus, title, detail)
├── CommonException.java         # 공통 예외 Enum (BAD_REQUEST, NOT_FOUND 등)
├── ApplicationException.java    # 비즈니스 로직 예외 클래스
├── InfrastructureException.java # 외부 시스템 연동 예외 클래스
├── ErrorResponse.java           # 클라이언트 응답 DTO
└── GlobalExceptionHandler.java  # @RestControllerAdvice 전역 예외 핸들러

각 도메인/exception/
└── {Domain}Exception.java       # 도메인별 예외 Enum (ExceptionCode 구현)
```

#### 5.3.2. 도메인별 예외 Enum 작성 규칙

각 도메인(user, studyroom, problem 등)의 `exception` 패키지에 `ExceptionCode` 인터페이스를 구현한 Enum을 작성합니다.

**작성 예시** (`user/exception/UserException.java`):

```java
package com.ebbinghaus.ttopullae.user.exception;

import com.ebbinghaus.ttopullae.global.exception.ExceptionCode;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum UserException implements ExceptionCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없음", "요청한 ID의 사용자가 존재하지 않습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이메일 중복", "이미 사용 중인 이메일입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "잘못된 비밀번호", "비밀번호 형식이 올바르지 않습니다.");

    private final HttpStatus httpStatus;
    private final String title;
    private final String detail;

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getDetail() {
        return detail;
    }
}
```

#### 5.3.3. 예외 발생 방법

**1. 비즈니스 로직 예외** (일반적인 도메인 예외)

서비스 계층에서 `ApplicationException`을 사용하여 예외를 발생시킵니다.

```java
// Service 계층
public User findUserById(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new ApplicationException(UserException.USER_NOT_FOUND));
}

public void createUser(String email) {
    if (userRepository.existsByEmail(email)) {
        throw new ApplicationException(UserException.DUPLICATE_EMAIL);
    }
    // ... 사용자 생성 로직
}
```

**2. 외부 시스템 연동 예외** (S3, 외부 API 등)

외부 인프라와 연동 시 발생하는 예외는 `InfrastructureException`을 사용합니다.

```java
// 외부 API 호출 실패 시
public void sendEmail(String to, String subject) {
    try {
        emailClient.send(to, subject);
    } catch (Exception e) {
        throw new InfrastructureException(EmailException.SEND_FAILED);
    }
}
```

#### 5.3.4. 예외 처리 흐름

1. **예외 발생**: Service 계층에서 `ApplicationException` 또는 `InfrastructureException` 발생
2. **예외 포착**: `GlobalExceptionHandler`가 `@ExceptionHandler`로 예외를 포착
3. **응답 변환**: `ErrorResponse` 형식으로 변환
4. **클라이언트 응답**: HTTP 상태 코드와 함께 JSON 응답 반환

**ErrorResponse 형식**:
```json
{
  "title": "사용자를 찾을 수 없음",
  "status": 404,
  "detail": "요청한 ID의 사용자가 존재하지 않습니다.",
  "instance": "/api/users/123"
}
```

#### 5.3.5. 예외 네이밍 및 메시지 작성 가이드

- **Enum 이름**: `{DOMAIN}_{ERROR_TYPE}` 형식 (예: `USER_NOT_FOUND`, `ROOM_ACCESS_DENIED`)
- **HttpStatus**: 적절한 HTTP 상태 코드 선택 (404, 400, 403, 409, 500 등)
- **title**: 간결한 한글 제목 (예: "사용자를 찾을 수 없음")
- **detail**: 구체적인 한글 설명 (예: "요청한 ID의 사용자가 존재하지 않습니다.")
- 클라이언트가 이해하기 쉽고 디버깅에 도움이 되는 명확한 메시지 작성

#### 5.3.6. 주의사항

- 도메인별 예외는 반드시 해당 도메인의 `exception` 패키지에 작성
- 공통으로 사용되는 예외는 `CommonException`에 정의 (이미 정의된 예외 재사용 권장)
- 민감한 정보(비밀번호, 토큰 등)는 예외 메시지에 포함하지 않음
- `ApplicationException`과 `InfrastructureException`을 상황에 맞게 구분하여 사용
- 예외 로깅은 `GlobalExceptionHandler`에서 자동으로 처리됨 (별도 로깅 불필요)

### 5.4. 테스트 작성 규칙

- 개발 구현 완료 되면 테스트 코드를 작성하여 구현한 기능이 제대로 동작하는지 확인한다.
- **단위 테스트**: 서비스 로직은 Mockito를 사용한 단위 테스트로 검증한다.
- **통합 테스트**: API는 `@SpringBootTest` + `MockMvc`를 사용한 통합 테스트로 검증한다.
- 외부 API에 의존하는 기능의 경우, 외부 API를 Mocking하여 테스트를 진행한다.
- 모든 테스트는 **Given/When/Then 패턴**을 적용한다.

### 5.5. API 문서화 (Swagger)

- API 스펙 문서화는 Swagger를 사용
- 각 Controller의 Swagger 문서는 xxxControllerDocs 인터페이스로 분리하여 관리한다.
- Swagger 작성 가이드라인은 `docs/SWAGGER.md` 참고
- API에 대한 구체적인 설명은 `docs/API_SPEC.md`에 작성

### 5.6. 엔티티 공통 규칙

- 모든 엔티티는 `BaseTimeEntity`를 상속하여 `createdAt` 자동 추적

-----

## 6\. 빌드 및 실행 명령어

### 6.1. 개발 환경

```bash
# 애플리케이션 실행
./gradlew bootRun

# 프로젝트 빌드
./gradlew build

# 테스트 스킵하고 빠른 빌드 (개발 중 빠른 확인용)
./gradlew build -x test

# 빌드 산출물 정리 후 새로 빌드
./gradlew clean build
```

### 6.2. 테스트 실행

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests com.ebbinghaus.ttopullae.studyroom.presentation.StudyRoomControllerTest

# 특정 테스트 메서드 실행
./gradlew test --tests com.ebbinghaus.ttopullae.studyroom.application.StudyRoomServiceTest.createPersonalRoom_Success

# 특정 패키지의 모든 테스트 실행
./gradlew test --tests "com.ebbinghaus.ttopullae.studyroom.*"

# 테스트 결과를 계속 확인하며 실행 (continuous mode)
./gradlew test --continuous
```

### 6.3. 도커 (Docker)

```bash
# MySQL 데이터베이스 시작 (개발용)
docker compose up -d

# MySQL 데이터베이스 중지
docker compose down

# MySQL 로그 보기
docker logs mysql-server
```

-----

## 7\. 데이터베이스 설정

- **데이터베이스**: MySQL 8.0 (Port: 3306)
- **데이터베이스명**: `main_db`
- **자격 증명**: root / asd1234 (개발용)
- **JPA DDL**: `create` 모드 (시작 시 스키마 재생성)
- **연결 설정**: `src/main/resources/application.yaml` 에 구성됨

⚠️ **주의**: 개발 환경은 `ddl-auto: create` 모드로 설정되어, 애플리케이션 시작 시마다 **스키마가 재생성되고 데이터가 삭제됩니다.** 운영 환경에서는 절대 사용하지 마십시오.

## 8\. 프로젝트 네이밍

- **루트 프로젝트명**: `ttopullae` (`settings.gradle`에 정의됨)
- **그룹 ID**: `com.ebbinghaus`
- **기본 패키지**: `com.ebbinghaus.ttopullae`

## 9\. 문서

- **PRD**: `docs/PRD.md` - 종합 제품 요구사항 문서 (v1.7)
- **TABLE**: `docs/TABLE.md` - 테이블 설계 문서
- **SWAGGER**: `docs/SWAGGER.md` - swagger API 문서화 요령을 담은 문서 

-----
## 10. 인증 / 인가

> 본 서비스는 쿠키 기반 JWT 인증 방식을 사용한다.
로그인 시 발급된 Access Token은 HttpOnly 쿠키에 저장되며, 모든 요청에서 자동으로 인증이 수행된다.

### 10.1 인증 방식 개요

1. 로그인 성공 → JWT Access Token 생성
2. Access Token을 HttpOnly 쿠키(accessToken)로 클라이언트에 전달
3. 이후 API 요청 시 브라우저가 쿠키를 자동 전송
3. 인터셉터가 토큰 검증
4. 검증된 userId를 컨트롤러에 자동 주입(@LoginUser 와 ArgumentResolver)

### 10.2 JWT Payload

[예시]
```json
{
  "userId": 1,
  "exp": 1700000000
}
```
- JWT 토큰의 페이로드에는 유저 아이디가 포함된다. 
