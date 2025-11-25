# API 명세서

> 작성일: 2025-01-25
> 버전: v2.2
> **변경사항**:
> - 로그인 API 응답 수정 (accessToken 응답 바디에서 제거, 쿠키로만 전달)
> - 이메일 알림 설정 API 추가

## 목차

1. [인증 API](#1-인증-api)
   - [1.1. 회원가입 API](#11-회원가입-api)
   - [1.2. 로그인 API](#12-로그인-api)
   - [1.3. 로그아웃 API](#13-로그아웃-api)
2. [개인 공부방 생성 API](#2-개인-공부방-생성-api)
3. [그룹 스터디 생성 API](#3-그룹-스터디-생성-api)
4. [문제 생성 API](#4-문제-생성-api)
5. [AI 채점 테스트 API](#5-ai-채점-테스트-api)
6. [개인 공부방 문제 목록 조회 API](#6-개인-공부방-문제-목록-조회-api)
7. [오늘의 복습 문제 조회 API](#7-오늘의-복습-문제-조회-api)
8. [문제 풀이 제출 API](#8-문제-풀이-제출-api)
9. [이메일 알림 설정 API](#9-이메일-알림-설정-api)

---

## 1. 인증 API

### 1.1. 회원가입 API

#### 1.1.1. 기본 정보

- **Endpoint**: `POST /api/auth/signup`
- **설명**: 새로운 사용자를 등록합니다.
- **인증**: 불필요

#### 1.1.2. Request

##### Headers

```
Content-Type: application/json
```

##### Body

| 필드 | 타입 | 필수 | 설명 | 예시 |
|------|------|------|------|------|
| email | String | O | 사용자 이메일 (로그인 ID) | "user@example.com" |
| password | String | O | 비밀번호 (평문) | "password123" |
| username | String | O | 사용자 이름 | "홍길동" |
| receiveNotifications | Boolean | O | 알림 수신 여부 | true |

##### Request Example

```json
{
  "email": "user@example.com",
  "password": "password123",
  "username": "홍길동",
  "receiveNotifications": true
}
```

#### 1.1.3. Response

##### Success (201 Created)

| 필드 | 타입 | 설명 | 예시 |
|------|------|------|------|
| userId | Long | 생성된 사용자 ID | 1 |
| email | String | 사용자 이메일 | "user@example.com" |
| username | String | 사용자 이름 | "홍길동" |
| receiveNotifications | Boolean | 알림 수신 여부 | true |

##### Response Example

```json
{
  "userId": 1,
  "email": "user@example.com",
  "username": "홍길동",
  "receiveNotifications": true
}
```

#### 1.1.4. Error Responses

##### 400 Bad Request - Validation 실패

**이메일 형식 오류**
```json
{
  "title": "유효하지 않은 입력값",
  "status": 400,
  "detail": "올바른 이메일 형식이어야 합니다",
  "instance": "/api/auth/signup"
}
```

##### 409 Conflict - 이메일 중복

```json
{
  "title": "이메일 중복",
  "status": 409,
  "detail": "이미 사용 중인 이메일입니다.",
  "instance": "/api/auth/signup"
}
```

---

### 1.2. 로그인 API

#### 1.2.1. 기본 정보

- **Endpoint**: `POST /api/auth/login`
- **설명**: 이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다. 토큰은 HttpOnly 쿠키에 자동으로 저장됩니다.
- **인증**: 불필요

#### 1.2.2. Request

##### Headers

```
Content-Type: application/json
```

##### Body

| 필드 | 타입 | 필수 | 설명 | 예시 |
|------|------|------|------|------|
| email | String | O | 사용자 이메일 | "user@example.com" |
| password | String | O | 비밀번호 | "password123" |

##### Request Example

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

#### 1.2.3. Response

##### Success (200 OK)

| 필드 | 타입 | 설명 | 예시 |
|------|------|------|------|
| userId | Long | 사용자 ID | 1 |
| email | String | 사용자 이메일 | "user@example.com" |
| username | String | 사용자 이름 | "홍길동" |

**Set-Cookie Header**:
```
Set-Cookie: accessToken={JWT_TOKEN}; Path=/; HttpOnly; SameSite=None; Max-Age=86400
```

> **참고**: 액세스 토큰은 응답 바디에 포함되지 않고, HttpOnly 쿠키로만 전달됩니다. 브라우저가 자동으로 이후 요청에 쿠키를 포함시킵니다.

##### Response Example

```json
{
  "userId": 1,
  "email": "user@example.com",
  "username": "홍길동"
}
```

#### 1.2.4. Error Responses

##### 404 Not Found - 사용자를 찾을 수 없음

```json
{
  "title": "사용자를 찾을 수 없음",
  "status": 404,
  "detail": "요청한 ID의 사용자가 존재하지 않습니다.",
  "instance": "/api/auth/login"
}
```

##### 401 Unauthorized - 비밀번호 불일치

```json
{
  "title": "잘못된 비밀번호",
  "status": 401,
  "detail": "비밀번호가 일치하지 않습니다.",
  "instance": "/api/auth/login"
}
```

---

### 1.3. 로그아웃 API

#### 1.3.1. 기본 정보

- **Endpoint**: `POST /api/auth/logout`
- **설명**: 현재 로그인된 사용자를 로그아웃합니다. 쿠키에서 JWT 토큰을 삭제합니다.
- **인증**: 불필요

#### 1.3.2. Request

##### Headers

```
(없음)
```

#### 1.3.3. Response

##### Success (200 OK)

**Set-Cookie Header**:
```
Set-Cookie: accessToken=; Path=/; HttpOnly; SameSite=None; Max-Age=0
```

---

## 2. 개인 공부방 생성 API

### 2.1. 기본 정보

- **Endpoint**: `POST /api/study-rooms/personal`
- **설명**: 사용자가 개인 공부방을 생성합니다.
- **인증**: **필수** (JWT 쿠키 인증)

### 2.2. Request

#### Headers

```
Content-Type: application/json
Cookie: accessToken={JWT_TOKEN}
```

#### Body

| 필드 | 타입 | 필수 | 설명 | 예시 |
|------|------|------|------|------|
| name | String | O | 공부방 이름 | "자바 스터디" |
| description | String | X | 공부방 설명 | "자바 개념 정리" |
| category | String | X | 공부방 카테고리 | "프로그래밍" |

#### Request Example

```json
{
  "name": "자바 스터디",
  "description": "자바 개념 정리",
  "category": "프로그래밍"
}
```

### 2.3. Response

#### Success (201 Created)

| 필드 | 타입 | 설명 | 예시 |
|------|------|------|------|
| studyRoomId | Long | 생성된 공부방 ID | 1 |
| name | String | 공부방 이름 | "자바 스터디" |
| category | String | 공부방 카테고리 | "프로그래밍" |
| description | String | 공부방 설명 | "자바 개념 정리" |
| createdAt | String (ISO 8601) | 생성 일시 | "2025-01-18T10:30:00" |

#### Response Example

```json
{
  "studyRoomId": 1,
  "name": "자바 스터디",
  "category": "프로그래밍",
  "description": "자바 개념 정리",
  "createdAt": "2025-01-18T10:30:00"
}
```

### 2.4. Error Responses

#### 400 Bad Request - Validation 실패

**공부방 이름 누락**
```json
{
  "title": "유효하지 않은 입력값",
  "status": 400,
  "detail": "공부방 이름은 필수입니다",
  "instance": "/api/study-rooms/personal"
}
```

#### 401 Unauthorized - 인증 실패

**JWT 토큰 없음**
```json
{
  "title": "토큰을 찾을 수 없음",
  "status": 401,
  "detail": "인증 토큰이 제공되지 않았습니다.",
  "instance": "/api/study-rooms/personal"
}
```

**JWT 토큰 유효하지 않음**
```json
{
  "title": "유효하지 않은 토큰",
  "status": 401,
  "detail": "토큰이 유효하지 않습니다.",
  "instance": "/api/study-rooms/personal"
}
```

**JWT 토큰 만료**
```json
{
  "title": "만료된 토큰",
  "status": 401,
  "detail": "토큰이 만료되었습니다.",
  "instance": "/api/study-rooms/personal"
}
```

### 2.5. 비즈니스 로직

1. JWT 쿠키에서 사용자 ID 추출 (인터셉터에서 자동 처리)
2. `RoomType.PERSONAL`로 설정하여 개인 공부방 생성
3. `joinCode`는 `null`로 설정 (개인방은 참여 코드 불필요)
4. 생성된 공부방 정보를 `201 Created` 상태 코드와 함께 반환

---

## 3. 그룹 스터디 생성 API

### 3.1. 기본 정보

- **Endpoint**: `POST /api/study-rooms/group`
- **설명**: 사용자가 그룹 스터디를 생성하고, 고유한 참여 코드를 자동으로 발급받습니다.
- **인증**: **필수** (JWT 쿠키 인증)

### 3.2. Request

#### Headers

```
Content-Type: application/json
Cookie: accessToken={JWT_TOKEN}
```

#### Body

| 필드 | 타입 | 필수 | 설명 | 예시 |
|------|------|------|------|------|
| name | String | O | 그룹 스터디 이름 | "알고리즘 스터디" |
| description | String | X | 그룹 스터디 설명 | "매일 알고리즘 풀이" |
| category | String | X | 그룹 스터디 카테고리 | "알고리즘" |

#### Request Example

```json
{
  "name": "알고리즘 스터디",
  "description": "매일 알고리즘 풀이",
  "category": "알고리즘"
}
```

### 3.3. Response

#### Success (201 Created)

| 필드 | 타입 | 설명 | 예시 |
|------|------|------|------|
| studyRoomId | Long | 생성된 그룹 스터디 ID | 1 |
| name | String | 그룹 스터디 이름 | "알고리즘 스터디" |
| category | String | 그룹 스터디 카테고리 | "알고리즘" |
| description | String | 그룹 스터디 설명 | "매일 알고리즘 풀이" |
| joinCode | String | 참여 코드 (8자리 영숫자) | "A3K9XP2M" |
| createdAt | String (ISO 8601) | 생성 일시 | "2025-01-18T10:30:00" |

#### Response Example

```json
{
  "studyRoomId": 1,
  "name": "알고리즘 스터디",
  "category": "알고리즘",
  "description": "매일 알고리즘 풀이",
  "joinCode": "A3K9XP2M",
  "createdAt": "2025-01-18T10:30:00"
}
```

### 3.4. Error Responses

#### 400 Bad Request - Validation 실패

**그룹 스터디 이름 누락**
```json
{
  "title": "유효하지 않은 입력값",
  "status": 400,
  "detail": "그룹 스터디 이름은 필수입니다",
  "instance": "/api/study-rooms/group"
}
```

#### 401 Unauthorized - 인증 실패

**JWT 토큰 없음 또는 유효하지 않음** (개인 공부방 생성 API와 동일)

#### 500 Internal Server Error - 참여 코드 생성 실패

```json
{
  "title": "참여 코드 생성 실패",
  "status": 500,
  "detail": "고유한 참여 코드 생성에 실패했습니다. 잠시 후 다시 시도해주세요.",
  "instance": "/api/study-rooms/group"
}
```

### 3.5. 비즈니스 로직

1. JWT 쿠키에서 사용자 ID 추출 (인터셉터에서 자동 처리)
2. 8자리 영숫자 랜덤 참여 코드 생성 (중복 확인, 최대 10회 재시도)
3. 10회 재시도 후에도 고유 코드 생성 실패 시 `500 Internal Server Error` 반환
4. `RoomType.GROUP`로 설정하여 그룹 스터디 생성
5. 생성자를 그룹 멤버(`StudyRoomMember`)로 자동 등록
6. 생성된 그룹 스터디 정보 (참여 코드 포함)를 `201 Created` 상태 코드와 함께 반환

### 3.6. 참여 코드 규칙

- **형식**: 영문 대문자 + 숫자 조합
- **길이**: 8자리
- **예시**: `A3K9XP2M`, `7BX4KL9Q`
- **중복 방지**: 데이터베이스에서 중복 확인 후 고유한 코드만 사용

---

## 부록: 공통 사항

### 인증 (Authentication)

**현재 (v2.0)**:
- JWT 쿠키 기반 인증 시스템 사용
- HttpOnly 쿠키에 JWT 토큰 저장 (XSS 방지)
- 쿠키 이름: `accessToken`
- 토큰 만료 시간: 24시간 (86400초)
- `/api/**` 경로에 인증 인터셉터 적용
- 제외 경로: `/api/auth/signup`, `/api/auth/login`, `/api/auth/logout`, `/docs/**`, `/swagger-ui/**`

**JWT 토큰 구조**:
- Payload: `{ "sub": "{userId}", "iat": 1234567890, "exp": 1234654290 }`
- 알고리즘: HS256
- Secret Key: application.yaml에 설정

### 에러 응답 형식

모든 에러 응답은 RFC 7807 (Problem Details for HTTP APIs) 형식을 따릅니다.

```json
{
  "title": "에러 제목",
  "status": 400,
  "detail": "상세한 에러 설명",
  "instance": "/api/endpoint"
}
```

### 날짜/시간 형식

- 모든 날짜/시간 필드는 ISO 8601 형식을 사용합니다.
- 예시: `2025-01-18T10:30:00`

### 데이터베이스 스키마 관련

#### users 테이블

| 컬럼명 | 타입 | NULL | 키 | 설명 |
|--------|------|------|-----|------|
| user_id | BIGINT | NO | PK | 사용자 ID (자동 증가) |
| email | VARCHAR(255) | NO | UNIQUE | 사용자 이메일 (로그인 ID) |
| password | VARCHAR(255) | NO | | 비밀번호 (평문) |
| username | VARCHAR(100) | NO | | 사용자 이름 |
| receive_notifications | BOOLEAN | NO | | 알림 수신 여부 |
| created_at | TIMESTAMP | NO | | 생성 일시 |

#### study_rooms 테이블

| 컬럼명 | 타입 | NULL | 키 | 설명 |
|--------|------|------|-----|------|
| study_room_id | BIGINT | NO | PK | 공부방 ID (자동 증가) |
| owner_id | BIGINT | NO | FK | 방장 사용자 ID |
| room_type | VARCHAR(20) | NO | | PERSONAL 또는 GROUP |
| name | VARCHAR(255) | NO | | 공부방 이름 |
| description | TEXT | YES | | 공부방 설명 |
| category | VARCHAR(100) | YES | | 공부방 카테고리 |
| join_code | VARCHAR(8) | YES | UNIQUE | 참여 코드 (그룹방만) |
| created_at | TIMESTAMP | NO | | 생성 일시 |

#### study_room_members 테이블

| 컬럼명 | 타입 | NULL | 키 | 설명 |
|--------|------|------|-----|------|
| member_id | BIGINT | NO | PK | 멤버 ID (자동 증가) |
| user_id | BIGINT | NO | FK | 사용자 ID |
| study_room_id | BIGINT | NO | FK | 그룹 스터디 ID |
| active | BOOLEAN | NO | | 멤버십 활성화 상태 |
| created_at | TIMESTAMP | NO | | 가입 일시 |

**제약 조건**:
- `(user_id, study_room_id)` 복합 UNIQUE 제약 (중복 가입 방지)

---

## 4. 문제 생성 API

### 4.1. 기본 정보

- **Endpoint**: `POST /api/study-rooms/{studyRoomId}/problems`
- **설명**: 스터디룸에 새로운 학습 문제를 생성합니다. 4가지 유형의 문제를 지원합니다.
- **인증**: **필수** (JWT 쿠키 인증)
- **지원 문제 유형**:
  - `MCQ`: 객관식 (Multiple Choice Question)
  - `OX`: OX 문제 (True/False)
  - `SHORT`: 단답형 (Short Answer)
  - `SUBJECTIVE`: 서술형 (Essay/Subjective)

### 4.2. Request

#### Headers

```
Content-Type: application/json
Cookie: accessToken={JWT_TOKEN}
```

#### Path Parameters

| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|------|------|------|
| studyRoomId | Long | O | 문제를 생성할 스터디룸 ID | 1 |

#### Body - 공통 필드

| 필드 | 타입 | 필수 | 설명 | 예시 |
|------|------|------|------|------|
| problemType | String | O | 문제 유형 (MCQ, OX, SHORT, SUBJECTIVE) | "MCQ" |
| question | String | O | 문제 내용 | "자바의 접근 제어자가 아닌 것은?" |
| explanation | String | O | 해설 | "friend는 C++의 접근 제어자입니다." |

#### Body - 객관식(MCQ) 추가 필드

| 필드 | 타입 | 필수 | 설명 | 예시 |
|------|------|------|------|------|
| choices | List<String> | O | 선택지 목록 (2개 이상) | ["public", "private", "protected", "friend"] |
| correctChoiceIndex | Integer | O | 정답 인덱스 (0부터 시작) | 3 |

#### Body - OX 추가 필드

| 필드 | 타입 | 필수 | 설명 | 예시 |
|------|------|------|------|------|
| answerBoolean | Boolean | O | 정답 (true 또는 false) | true |

#### Body - 단답형(SHORT) 추가 필드

| 필드 | 타입 | 필수 | 설명 | 예시 |
|------|------|------|------|------|
| answerText | String | O | 정답 텍스트 | "String" |

#### Body - 서술형(SUBJECTIVE) 추가 필드

| 필드 | 타입 | 필수 | 설명 | 예시 |
|------|------|------|------|------|
| modelAnswerText | String | O | 모범 답안 | "DDD는 도메인을 중심으로..." |
| keywords | List<String> | O | 채점 키워드 목록 | ["도메인", "엔티티", "리포지토리"] |

#### Request Example - 객관식

```json
{
  "problemType": "MCQ",
  "question": "자바의 접근 제어자가 아닌 것은?",
  "explanation": "friend는 C++의 접근 제어자입니다.",
  "choices": ["public", "private", "protected", "friend"],
  "correctChoiceIndex": 3
}
```

#### Request Example - OX

```json
{
  "problemType": "OX",
  "question": "JVM은 Java Virtual Machine의 약자이다.",
  "explanation": "맞습니다.",
  "answerBoolean": true
}
```

#### Request Example - 단답형

```json
{
  "problemType": "SHORT",
  "question": "자바에서 문자열을 다루는 불변 클래스는?",
  "explanation": "String 클래스입니다.",
  "answerText": "String"
}
```

#### Request Example - 서술형

```json
{
  "problemType": "SUBJECTIVE",
  "question": "DDD의 핵심 개념에 대해 설명하시오.",
  "explanation": "DDD는 도메인 중심 설계입니다.",
  "modelAnswerText": "DDD는 도메인을 중심으로 소프트웨어를 설계하는 방법론입니다.",
  "keywords": ["도메인", "엔티티", "리포지토리"]
}
```

### 4.3. Response

#### Success (201 Created)

| 필드 | 타입 | 설명 | 예시 |
|------|------|------|------|
| problemId | Long | 생성된 문제 ID | 1 |
| studyRoomId | Long | 스터디룸 ID | 1 |
| problemType | String | 문제 유형 | "MCQ" |
| question | String | 문제 내용 | "자바의 접근 제어자가 아닌 것은?" |
| createdAt | String (ISO 8601) | 생성 일시 | "2025-01-21T10:30:00" |

#### Response Example

```json
{
  "problemId": 1,
  "studyRoomId": 1,
  "problemType": "MCQ",
  "question": "자바의 접근 제어자가 아닌 것은?",
  "createdAt": "2025-01-21T10:30:00"
}
```

### 4.4. Error Responses

#### 400 Bad Request - 필수 필드 누락

**문제 내용 누락**
```json
{
  "title": "유효하지 않은 입력값",
  "status": 400,
  "detail": "question: 문제 내용은 필수입니다",
  "instance": "/api/study-rooms/1/problems"
}
```

**객관식 데이터 오류**
```json
{
  "title": "객관식 데이터 오류",
  "status": 400,
  "detail": "객관식 문제는 선택지 목록과 정답 인덱스가 필요합니다.",
  "instance": "/api/study-rooms/1/problems"
}
```

**OX 데이터 오류**
```json
{
  "title": "OX 데이터 오류",
  "status": 400,
  "detail": "OX 문제는 정답(true/false)이 필요합니다.",
  "instance": "/api/study-rooms/1/problems"
}
```

**단답형 데이터 오류**
```json
{
  "title": "단답형 데이터 오류",
  "status": 400,
  "detail": "단답형 문제는 정답 텍스트가 필요합니다.",
  "instance": "/api/study-rooms/1/problems"
}
```

**서술형 데이터 오류**
```json
{
  "title": "서술형 데이터 오류",
  "status": 400,
  "detail": "서술형 문제는 모범 답안과 키워드 목록이 필요합니다.",
  "instance": "/api/study-rooms/1/problems"
}
```

#### 401 Unauthorized - 인증 실패

**JWT 토큰 없음**
```json
{
  "title": "인증되지 않은 요청",
  "status": 401,
  "detail": "인증 토큰이 제공되지 않았습니다.",
  "instance": "/api/study-rooms/1/problems"
}
```

#### 404 Not Found - 스터디룸을 찾을 수 없음

```json
{
  "title": "스터디룸을 찾을 수 없음",
  "status": 404,
  "detail": "요청한 ID의 스터디룸이 존재하지 않습니다.",
  "instance": "/api/study-rooms/999/problems"
}
```

### 4.5. 비즈니스 로직

#### 4.5.1. 문제 생성 플로우

1. JWT 쿠키에서 사용자 ID 추출 (인터셉터에서 자동 처리)
2. 사용자 존재 여부 확인
3. 스터디룸 존재 여부 확인
4. 문제 유형별 필수 데이터 검증
5. Problem 엔티티 생성 및 저장
6. 관련 엔티티 저장 (객관식 선택지 또는 서술형 키워드)
7. 복습 상태 초기화 (조건부)
8. 생성된 문제 정보 반환 (201 Created)

#### 4.5.2. 복습 상태 초기화 조건

문제 생성 시 다음 조건을 만족하면 `ProblemReviewState`가 자동으로 생성됩니다:

- **개인 공부방(PERSONAL)**: 모든 문제에 대해 복습 상태 생성
- **그룹 스터디(GROUP)**: 생성자가 만든 문제에 대해서만 복습 상태 생성

**초기 복습 상태**:
- `gate`: `GATE_1` (1일차 복습)
- `nextReviewDate`: 생성일 + 1일
- `reviewCount`: 0

**그룹 스터디의 타인 문제**: 첫 번째 풀이 시도 시에만 복습 주기에 등록됩니다.

#### 4.5.3. 관련 엔티티 생성 규칙

**객관식(MCQ) - ProblemChoice**:
- 선택지 목록(`choices`)을 순회하며 `ProblemChoice` 엔티티 생성
- `choiceOrder`는 1부터 시작 (0-based 아님)
- `correctChoiceIndex`는 Problem 엔티티의 필드로 저장

**서술형(SUBJECTIVE) - ProblemKeyword**:
- 키워드 목록(`keywords`)을 순회하며 `ProblemKeyword` 엔티티 생성
- AI 기반 채점 시 사용할 키워드로 활용

### 4.6. 데이터베이스 스키마

#### problems 테이블

| 컬럼명 | 타입 | NULL | 키 | 설명 |
|--------|------|------|-----|------|
| problem_id | BIGINT | NO | PK | 문제 ID (자동 증가) |
| study_room_id | BIGINT | NO | FK | 스터디룸 ID |
| creator_id | BIGINT | NO | FK | 생성자 사용자 ID |
| problem_type | VARCHAR(20) | NO | | 문제 유형 (MCQ, OX, SHORT, SUBJECTIVE) |
| question | TEXT | NO | | 문제 내용 |
| explanation | TEXT | NO | | 해설 |
| answer_boolean | BOOLEAN | YES | | OX 정답 (OX 문제만) |
| answer_text | VARCHAR(500) | YES | | 단답형 정답 (단답형만) |
| model_answer_text | TEXT | YES | | 모범 답안 (서술형만) |
| correct_choice_index | INTEGER | YES | | 정답 인덱스 (객관식만) |
| created_at | TIMESTAMP | NO | | 생성 일시 |

#### problem_choices 테이블 (객관식 선택지)

| 컬럼명 | 타입 | NULL | 키 | 설명 |
|--------|------|------|-----|------|
| choice_id | BIGINT | NO | PK | 선택지 ID (자동 증가) |
| problem_id | BIGINT | NO | FK | 문제 ID |
| choice_order | INTEGER | NO | | 선택지 순서 (1부터 시작) |
| choice_text | VARCHAR(500) | NO | | 선택지 내용 |
| created_at | TIMESTAMP | NO | | 생성 일시 |

#### problem_keywords 테이블 (서술형 키워드)

| 컬럼명 | 타입 | NULL | 키 | 설명 |
|--------|------|------|-----|------|
| keyword_id | BIGINT | NO | PK | 키워드 ID (자동 증가) |
| problem_id | BIGINT | NO | FK | 문제 ID |
| keyword | VARCHAR(100) | NO | | 키워드 |
| created_at | TIMESTAMP | NO | | 생성 일시 |

#### problem_review_states 테이블 (복습 상태)

| 컬럼명 | 타입 | NULL | 키 | 설명 |
|--------|------|------|-----|------|
| state_id | BIGINT | NO | PK | 상태 ID (자동 증가) |
| user_id | BIGINT | NO | FK | 사용자 ID |
| problem_id | BIGINT | NO | FK | 문제 ID |
| gate | VARCHAR(20) | NO | | 복습 관문 (GATE_1, GATE_2, GRADUATED) |
| next_review_date | DATE | NO | | 다음 복습 날짜 |
| review_count | INTEGER | NO | | 복습 횟수 |
| created_at | TIMESTAMP | NO | | 생성 일시 |

**제약 조건**:
- `(user_id, problem_id)` 복합 UNIQUE 제약 (중복 복습 상태 방지)
- `(user_id, next_review_date)` 복합 인덱스 (오늘의 복습 쿼리 최적화)

### 4.7. Sparse Column 패턴

본 API는 **Sparse Column 패턴**을 사용하여 4가지 문제 유형을 하나의 테이블로 관리합니다:

- 모든 유형별 필드(`answer_boolean`, `answer_text`, `model_answer_text`, `correct_choice_index`)를 nullable로 설정
- 서비스 계층에서 `problemType`에 따라 필수 필드를 검증
- 유형에 맞지 않는 필드는 `null`로 저장

**장점**:
- 테이블 구조 단순화 (단일 테이블)
- JOIN 없이 문제 조회 가능
- 새로운 문제 유형 추가 시 컬럼 추가만으로 확장 가능

**주의사항**:
- 필수 데이터 검증은 애플리케이션 레이어에서 수행
- 클라이언트는 문제 유형에 맞는 필드만 전송해야 함

---

## 5. AI 채점 테스트 API

### 5.1. 기본 정보

- **Endpoint**: `POST /api/grading/test`
- **설명**: 서술형 답안에 대한 AI 자동 채점 기능을 테스트합니다. OpenAI GPT-4o-mini 모델을 사용하여 사용자 답안을 모범 답안 및 핵심 키워드와 비교하여 채점하고 피드백을 제공합니다.
- **인증**: 필요 (JWT 쿠키)
- **용도**: 개발 및 테스트 목적

### 5.2. Request

#### 5.2.1. Headers

```
Content-Type: application/json
Cookie: accessToken={JWT_TOKEN}
```

#### 5.2.2. Body

| 필드 | 타입 | 필수 | 설명 | 예시 |
|------|------|------|------|------|
| topic | String | O | 주제 | "Spring Framework" |
| question | String | O | 문제 | "IoC(Inversion of Control)란 무엇인가?" |
| modelAnswer | String | O | 모범 답안 | "제어의 역전으로, 객체의 생성과 관리를 개발자가 아닌 스프링 컨테이너가 담당하는 것을 의미합니다." |
| keywords | List\<String\> | O | 핵심 키워드 리스트 (최소 1개) | ["제어의 역전", "컨테이너"] |
| userAnswer | String | O | 사용자 답안 | "제어의 역전이며, 스프링 컨테이너가 객체를 관리합니다." |

#### 5.2.3. Request Example

```json
{
  "topic": "Spring Framework",
  "question": "IoC(Inversion of Control)란 무엇인가?",
  "modelAnswer": "제어의 역전으로, 객체의 생성과 관리를 개발자가 아닌 스프링 컨테이너가 담당하는 것을 의미합니다.",
  "keywords": ["제어의 역전", "컨테이너"],
  "userAnswer": "제어의 역전이며, 스프링 컨테이너가 객체를 관리합니다."
}
```

### 5.3. Response

#### 5.3.1. Success Response (200 OK)

##### Response Body

| 필드 | 타입 | 설명 | 예시 |
|------|------|------|------|
| isCorrect | Boolean | 정답 여부 | true |
| feedback | String | 피드백 메시지 | "정답입니다. '제어의 역전'의 개념과 '컨테이너'의 역할이 정확하게 설명되었습니다." |
| missingKeywords | List\<String\> | 누락된 키워드 리스트 | [] |
| scoringReason | String | 채점 근거 | "모든 핵심 키워드가 의미적으로 포함되었으며, 모범 답안의 핵심 내용과 일치합니다." |

##### Response Example (정답)

```json
{
  "isCorrect": true,
  "feedback": "정답입니다. '제어의 역전'의 개념과 '컨테이너'의 역할이 정확하게 설명되었습니다.",
  "missingKeywords": [],
  "scoringReason": "모든 핵심 키워드가 의미적으로 포함되었으며, 모범 답안의 핵심 내용과 일치합니다."
}
```

##### Response Example (오답)

```json
{
  "isCorrect": false,
  "feedback": "핵심 개념인 '제어의 역전'에 대한 설명이 누락되었습니다. 현재 답안은 '컨테이너'의 역할에 초점을 맞추고 있지만, '누가 제어의 주체인지'가 바뀌는 점이 포함되어야 합니다.",
  "missingKeywords": ["제어의 역전"],
  "scoringReason": "'제어의 역전' 개념이 누락되었습니다. '컨테이너'의 역할은 올바르게 설명되었습니다."
}
```

#### 5.3.2. Error Responses

##### 400 Bad Request - 필수 입력값 누락

```json
{
  "title": "유효하지 않은 입력값",
  "status": 400,
  "detail": "question: 문제는 필수입니다",
  "instance": "/api/grading/test"
}
```

##### 400 Bad Request - 키워드 리스트 비어있음

```json
{
  "title": "유효하지 않은 입력값",
  "status": 400,
  "detail": "keywords: 최소 1개 이상의 키워드가 필요합니다",
  "instance": "/api/grading/test"
}
```

##### 401 Unauthorized - 인증 실패

```json
{
  "title": "토큰을 찾을 수 없음",
  "status": 401,
  "detail": "인증 토큰이 제공되지 않았습니다.",
  "instance": "/api/grading/test"
}
```

##### 500 Internal Server Error - AI 서비스 오류

```json
{
  "title": "서버 내부 오류",
  "status": 500,
  "detail": "AI 채점 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
  "instance": "/api/grading/test"
}
```

### 5.4. 비즈니스 로직

1. **인증 검증**
   - JWT 쿠키에서 사용자 ID 추출
   - 유효하지 않은 토큰인 경우 401 에러 반환

2. **입력값 검증**
   - 모든 필수 필드 존재 여부 확인
   - `keywords` 리스트가 비어있지 않은지 확인
   - 검증 실패 시 400 에러 반환

3. **프롬프트 생성**
   - `src/main/resources/prompts/grading_system_prompt.txt` 템플릿 로드
   - `{{TOPIC}}`, `{{QUESTION}}`, `{{MODEL_ANSWER}}`, `{{KEYWORDS_LIST}}`, `{{USER_ANSWER}}` 변수 치환

4. **AI 채점 요청**
   - OpenAI API (`gpt-4o-mini` 모델) 호출
   - JSON 모드(`response_format: { "type": "json_object" }`) 사용
   - 타임아웃 또는 API 오류 시 Graceful Degradation 적용

5. **응답 파싱**
   - AI가 반환한 JSON을 `AiGradingResult`로 변환
   - `isCorrect`, `feedback`, `missingKeywords`, `scoringReason` 필드 추출

6. **결과 반환**
   - 클라이언트에 채점 결과 전달

### 5.5. AI 채점 기준

AI는 다음 기준에 따라 답안을 채점합니다:

1. **정확성**: 사용자 답안이 모범 답안의 핵심 의미와 일치하며, 사실적 오류가 없어야 함
2. **키워드 포함**: 핵심 키워드 리스트의 모든 개념이 사용자 답안에 의미적으로 포함되어야 함
   - 단순히 단어가 존재하는지가 아니라, 해당 키워드의 개념과 맥락이 올바르게 설명되었는지 판단
3. **판정**: 1번과 2번 기준을 모두 충족할 경우에만 `isCorrect` 값이 `true`

### 5.6. 주의사항

- **개발/테스트 전용**: 이 API는 AI 채점 기능을 테스트하기 위한 용도로, 실제 문제 풀이 시나리오에서는 별도의 답안 제출 API를 통해 자동 채점이 이루어집니다.
- **API 키 필요**: 실제 OpenAI API를 호출하므로 `.env` 파일에 `OPENAI_API_KEY` 설정이 필요합니다.
- **비용 발생**: OpenAI API 호출 시 비용이 발생할 수 있습니다.
- **응답 시간**: AI 모델 호출로 인해 응답 시간이 일반 API보다 길 수 있습니다 (보통 2-5초).
- **외부 지식 금지**: AI는 오직 제공된 모범 답안과 핵심 키워드만을 근거로 채점하며, 외부 지식을 사용하지 않습니다.

---

## 6. 개인 공부방 문제 목록 조회 API

### 6.1. 기본 정보

- **Endpoint**: `GET /api/study-rooms/personal/{studyRoomId}/problems`
- **설명**: 개인 공부방에 등록된 문제 목록을 조회합니다. 복습 관문(GATE_1, GATE_2, GRADUATED)별로 필터링할 수 있습니다.
- **인증**: **필수** (JWT 쿠키 인증)

### 6.2. Request

#### 6.2.1. Headers

```
Cookie: accessToken={JWT_TOKEN}
```

#### 6.2.2. Path Parameters

| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|------|------|------|
| studyRoomId | Long | O | 조회할 개인 공부방 ID | 1 |

#### 6.2.3. Query Parameters

| 파라미터 | 타입 | 필수 | 설명 | 기본값 | 가능한 값 |
|---------|------|------|------|--------|----------|
| filter | String | X | 복습 관문 필터 | "ALL" | "ALL", "GATE_1", "GATE_2", "GRADUATED" |

**필터 설명**:
- `ALL`: 모든 문제 조회 (기본값)
- `GATE_1`: 1일차 복습 관문의 문제만 조회
- `GATE_2`: 7일차 복습 관문의 문제만 조회
- `GRADUATED`: 복습을 완료한 문제만 조회

#### 6.2.4. Request Example

```http
GET /api/study-rooms/personal/1/problems?filter=ALL
Cookie: accessToken=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

```http
GET /api/study-rooms/personal/1/problems?filter=GATE_1
Cookie: accessToken=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 6.3. Response

#### 6.3.1. Success Response (200 OK)

##### Response Body

| 필드 | 타입 | 설명 | 예시 |
|------|------|------|------|
| studyRoomId | Long | 스터디룸 ID | 1 |
| studyRoomName | String | 스터디룸 이름 | "자바 스터디" |
| problems | List\<ProblemSummary\> | 문제 목록 | [...] |
| totalCount | Integer | 전체 문제 수 | 3 |

##### ProblemSummary 필드

| 필드 | 타입 | 설명 | 예시 |
|------|------|------|------|
| problemId | Long | 문제 ID | 1 |
| question | String | 문제 내용 | "자바의 접근 제어자가 아닌 것은?" |
| problemType | String | 문제 유형 (MCQ, OX, SHORT, SUBJECTIVE) | "MCQ" |
| reviewGate | String | 현재 복습 관문 (GATE_1, GATE_2, GRADUATED) | "GATE_1" |
| createdAt | String (ISO 8601) | 문제 생성 일시 | "2025-01-21T10:30:00" |
| lastReviewedAt | String (ISO 8601) | 마지막 복습 일시 (null 가능) | "2025-01-22T14:20:00" |
| reviewCount | Integer | 복습 횟수 | 2 |

##### Response Example (문제 있음)

```json
{
  "studyRoomId": 1,
  "studyRoomName": "자바 스터디",
  "problems": [
    {
      "problemId": 1,
      "question": "자바의 접근 제어자가 아닌 것은?",
      "problemType": "MCQ",
      "reviewGate": "GATE_1",
      "createdAt": "2025-01-21T10:30:00",
      "lastReviewedAt": "2025-01-22T14:20:00",
      "reviewCount": 2
    },
    {
      "problemId": 2,
      "question": "JVM은 Java Virtual Machine의 약자이다.",
      "problemType": "OX",
      "reviewGate": "GATE_2",
      "createdAt": "2025-01-21T11:00:00",
      "lastReviewedAt": null,
      "reviewCount": 0
    },
    {
      "problemId": 3,
      "question": "DDD의 핵심 개념에 대해 설명하시오.",
      "problemType": "SUBJECTIVE",
      "reviewGate": "GRADUATED",
      "createdAt": "2025-01-21T11:30:00",
      "lastReviewedAt": "2025-01-23T09:10:00",
      "reviewCount": 5
    }
  ],
  "totalCount": 3
}
```

##### Response Example (빈 목록)

```json
{
  "studyRoomId": 1,
  "studyRoomName": "자바 스터디",
  "problems": [],
  "totalCount": 0
}
```

### 6.4. Error Responses

#### 6.4.1. 401 Unauthorized - 인증 실패

**JWT 토큰 없음**
```json
{
  "title": "인증되지 않은 요청",
  "status": 401,
  "detail": "인증 토큰이 제공되지 않았습니다.",
  "instance": "/api/study-rooms/personal/1/problems"
}
```

**JWT 토큰 유효하지 않음**
```json
{
  "title": "유효하지 않은 토큰",
  "status": 401,
  "detail": "토큰이 유효하지 않습니다.",
  "instance": "/api/study-rooms/personal/1/problems"
}
```

#### 6.4.2. 404 Not Found - 스터디룸을 찾을 수 없음

```json
{
  "title": "스터디룸을 찾을 수 없음",
  "status": 404,
  "detail": "요청한 참여 코드의 스터디룸이 존재하지 않습니다.",
  "instance": "/api/study-rooms/personal/999/problems"
}
```

#### 6.4.3. 400 Bad Request - 개인 공부방이 아님

```json
{
  "title": "개인 공부방이 아님",
  "status": 400,
  "detail": "해당 스터디룸은 개인 공부방이 아닙니다.",
  "instance": "/api/study-rooms/personal/1/problems"
}
```

#### 6.4.4. 403 Forbidden - 스터디룸 소유자가 아님

```json
{
  "title": "스터디룸 소유자가 아님",
  "status": 403,
  "detail": "해당 스터디룸의 소유자만 접근할 수 있습니다.",
  "instance": "/api/study-rooms/personal/1/problems"
}
```

### 6.5. 비즈니스 로직

1. **인증 검증**
   - JWT 쿠키에서 사용자 ID 추출 (인터셉터에서 자동 처리)
   - 유효하지 않은 토큰인 경우 401 에러 반환

2. **사용자 및 스터디룸 검증**
   - 사용자 존재 여부 확인
   - 스터디룸 존재 여부 확인
   - 스터디룸이 존재하지 않으면 404 에러 반환

3. **개인 공부방 및 소유권 검증**
   - 스터디룸이 개인 공부방(`RoomType.PERSONAL`)인지 확인
   - 개인 공부방이 아니면 400 에러 반환
   - 현재 사용자가 스터디룸의 소유자인지 확인
   - 소유자가 아니면 403 에러 반환

4. **필터 파싱 및 문제 조회**
   - `filter` 파라미터를 `ReviewGate` Enum으로 변환
   - `"ALL"`인 경우 모든 복습 관문의 문제 조회
   - 특정 관문 지정 시 해당 관문의 문제만 조회
   - 복습 상태(`ProblemReviewState`)가 있는 문제만 반환

5. **최근 시도 기록 조회**
   - 각 문제의 최근 풀이 시도(`ProblemAttempt`) 조회
   - `lastReviewedAt` 필드에 사용

6. **결과 변환 및 반환**
   - 조회된 문제 목록을 Response DTO로 변환
   - 200 OK 상태 코드와 함께 반환

### 6.6. 주의사항

- **개인 공부방 전용**: 이 API는 개인 공부방(`RoomType.PERSONAL`)만 지원합니다. 그룹 스터디의 문제 목록은 별도 API를 사용해야 합니다.
- **소유자만 조회 가능**: 개인 공부방의 소유자만 문제 목록을 조회할 수 있습니다.
- **복습 상태 기반**: 복습 상태(`ProblemReviewState`)가 등록되지 않은 문제는 목록에 포함되지 않습니다.
- **필터 기본값**: `filter` 파라미터를 생략하면 `"ALL"`이 기본값으로 적용됩니다.
- **lastReviewedAt null 가능**: 한 번도 풀지 않은 문제는 `lastReviewedAt` 필드가 `null`입니다.

---

## 7. 오늘의 복습 문제 조회 API

### 7.1. 기본 정보

- **Endpoint**: `GET /api/review/today`
- **설명**: 사용자가 오늘 복습해야 할 문제 목록과 대시보드 통계를 조회합니다. 에빙하우스 망각곡선 기반 간헐적 복습 시스템의 핵심 기능으로, 1일차 복습(GATE_1)과 7일차 복습(GATE_2) 문제를 관문별로 필터링할 수 있습니다.
- **인증**: **필수** (JWT 쿠키 인증)

### 7.2. Request

#### 7.2.1. Headers

```
Cookie: accessToken={JWT_TOKEN}
```

#### 7.2.2. Query Parameters

| 파라미터 | 타입 | 필수 | 설명 | 기본값 | 가능한 값 |
|---------|------|------|------|--------|----------|
| filter | String | X | 복습 관문 필터 | "ALL" | "ALL", "GATE_1", "GATE_2" |

**필터 설명**:
- `ALL`: 모든 관문의 복습 문제 조회 (GATE_1 + GATE_2, 기본값)
- `GATE_1`: 1일차 복습 관문의 문제만 조회
- `GATE_2`: 7일차 복습 관문의 문제만 조회

#### 7.2.3. Request Example

```http
GET /api/review/today?filter=ALL
Cookie: accessToken=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

```http
GET /api/review/today?filter=GATE_1
Cookie: accessToken=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 7.3. Response

#### 7.3.1. Success Response (200 OK)

##### Response Body

| 필드 | 타입 | 설명 | 예시 |
|------|------|------|------|
| dashboard | DashboardInfo | 대시보드 통계 정보 | {...} |
| problems | List\<TodayReviewProblemInfo\> | 오늘의 복습 문제 목록 | [...] |

##### DashboardInfo 필드

| 필드 | 타입 | 설명 | 예시 |
|------|------|------|------|
| totalCount | Integer | 오늘 복습 대상 문제 총 개수 | 5 |
| completedCount | Integer | 완료한 문제 개수 | 2 |
| incompletedCount | Integer | 미완료 문제 개수 | 3 |
| progressRate | Double | 진행률 (0.0 ~ 100.0) | 40.0 |

**완료 기준**: 오늘 날짜에 첫 시도(`todayReviewFirstAttemptDate`)를 완료한 문제

##### TodayReviewProblemInfo 필드

| 필드 | 타입 | 설명 | 예시 |
|------|------|------|------|
| problemId | Long | 문제 ID | 1 |
| question | String | 문제 내용 | "자바의 접근 제어자가 아닌 것은?" |
| problemType | String | 문제 유형 (MCQ, OX, SHORT, SUBJECTIVE) | "MCQ" |
| gate | String | 현재 복습 관문 (GATE_1, GATE_2, GRADUATED) | "GATE_1" |
| nextReviewDate | String (ISO Date) | 다음 복습 예정일 (YYYY-MM-DD) | "2025-01-24" |
| attemptStatus | String | 오늘 풀이 상태 (NOT_ATTEMPTED, CORRECT, INCORRECT) | "NOT_ATTEMPTED" |

**attemptStatus 설명**:
- `NOT_ATTEMPTED`: 아직 풀지 않은 문제
- `CORRECT`: 오늘 첫 시도에서 정답을 맞힌 문제
- `INCORRECT`: 오늘 첫 시도에서 오답을 제출한 문제

##### Response Example (문제 있음)

```json
{
  "dashboard": {
    "totalCount": 5,
    "completedCount": 2,
    "incompletedCount": 3,
    "progressRate": 40.0
  },
  "problems": [
    {
      "problemId": 1,
      "question": "자바의 접근 제어자가 아닌 것은?",
      "problemType": "MCQ",
      "gate": "GATE_1",
      "nextReviewDate": "2025-01-24",
      "attemptStatus": "NOT_ATTEMPTED"
    },
    {
      "problemId": 2,
      "question": "JVM은 Java Virtual Machine의 약자이다.",
      "problemType": "OX",
      "gate": "GATE_1",
      "nextReviewDate": "2025-01-24",
      "attemptStatus": "CORRECT"
    },
    {
      "problemId": 3,
      "question": "JPA의 영속성 컨텍스트란?",
      "problemType": "SHORT",
      "gate": "GATE_2",
      "nextReviewDate": "2025-01-24",
      "attemptStatus": "INCORRECT"
    },
    {
      "problemId": 4,
      "question": "DDD의 핵심 개념에 대해 설명하시오.",
      "problemType": "SUBJECTIVE",
      "gate": "GATE_2",
      "nextReviewDate": "2025-01-24",
      "attemptStatus": "NOT_ATTEMPTED"
    },
    {
      "problemId": 5,
      "question": "Spring IoC란 무엇인가?",
      "problemType": "SUBJECTIVE",
      "gate": "GRADUATED",
      "nextReviewDate": null,
      "attemptStatus": "CORRECT"
    }
  ]
}
```

##### Response Example (빈 목록)

```json
{
  "dashboard": {
    "totalCount": 0,
    "completedCount": 0,
    "incompletedCount": 0,
    "progressRate": 0.0
  },
  "problems": []
}
```

### 7.4. Error Responses

#### 7.4.1. 400 Bad Request - 잘못된 필터 값

```json
{
  "title": "잘못된 요청",
  "status": 400,
  "detail": "유효하지 않은 필터 값입니다.",
  "instance": "/api/review/today"
}
```

#### 7.4.2. 401 Unauthorized - 인증 실패

**JWT 토큰 없음**
```json
{
  "title": "토큰을 찾을 수 없음",
  "status": 401,
  "detail": "인증 토큰이 제공되지 않았습니다.",
  "instance": "/api/review/today"
}
```

**JWT 토큰 유효하지 않음**
```json
{
  "title": "유효하지 않은 토큰",
  "status": 401,
  "detail": "토큰이 유효하지 않습니다.",
  "instance": "/api/review/today"
}
```

### 7.5. 비즈니스 로직

#### 7.5.1. 복습 대상 선정 기준

오늘의 복습 문제는 다음 조건 중 하나라도 만족하는 문제입니다:

1. **일반 복습 대상**:
   - `nextReviewDate`가 오늘 날짜보다 작거나 같은 문제
   - GATE_1 또는 GATE_2 상태인 문제

2. **목록 일관성 유지 대상**:
   - 오늘 조회 시점에 목록에 포함된 적이 있는 문제 (`todayReviewIncludedDate = 오늘`)
   - 오늘 중에 관문이 변경되어도 목록에서 사라지지 않음
   - 오늘 졸업(GRADUATED)한 문제도 포함됨

#### 7.5.2. 필터링 로직

- **ALL**: GATE_1과 GATE_2 문제 모두 조회 (오늘 졸업한 문제 포함)
- **GATE_1**: GATE_1 상태이거나, 오늘 GATE_1 상태로 포함된 적이 있는 문제
- **GATE_2**: GATE_2 상태이거나, 오늘 GATE_2 상태로 포함된 적이 있는 문제

**필터 일관성 보장**:
- 사용자가 필터를 적용한 후 문제를 풀어서 관문이 변경되어도, 오늘 하루 동안은 해당 필터에서 문제가 사라지지 않습니다.
- `todayReviewIncludedGate` 필드를 사용하여 원래 속했던 필터에 계속 표시됩니다.

#### 7.5.3. 완료 여부 판단

문제가 "완료"로 간주되는 조건:
- `todayReviewFirstAttemptDate = 오늘 날짜`
- 즉, 오늘 첫 번째 시도를 완료한 문제
- 정답/오답 여부와 관계없이 첫 시도만 했다면 완료로 처리

#### 7.5.4. 처리 순서

1. **인증 검증**
   - JWT 쿠키에서 사용자 ID 추출 (인터셉터에서 자동 처리)
   - 유효하지 않은 토큰인 경우 401 에러 반환

2. **필터 파싱**
   - `filter` 파라미터를 `ReviewGate` Enum으로 변환
   - "ALL"인 경우 `null`로 처리 (모든 관문 조회)
   - 잘못된 필터 값인 경우 400 에러 반환

3. **복습 상태 조회**
   - `ProblemReviewStateRepository.findTodaysReviewProblems()` 호출
   - Problem 엔티티를 fetch join하여 N+1 쿼리 방지
   - 오늘 날짜와 필터 조건을 만족하는 복습 상태 목록 반환

4. **대시보드 통계 계산**
   - 총 문제 수: 조회된 복습 상태 개수
   - 완료 수: `todayReviewFirstAttemptDate = 오늘`인 문제 개수
   - 미완료 수: 총 문제 수 - 완료 수
   - 진행률: (완료 수 / 총 문제 수) × 100 (소수점 첫째 자리까지)

5. **결과 변환 및 반환**
   - 조회된 데이터를 Response DTO로 변환
   - 200 OK 상태 코드와 함께 반환

### 7.6. 주의사항

- **목록 일관성 보장**: 오늘 조회 시점에 목록에 포함된 문제는 하루 동안 사라지지 않습니다.
- **졸업 문제 포함**: 오늘 졸업한 문제도 목록에 표시되어 완료 통계에 반영됩니다.
- **복습 이월**: 미완료 복습 문제는 다음 날로 자동 이월됩니다.
- **일일 제한 없음**: 하루에 무제한으로 복습을 완료할 수 있습니다.
- **필터 기본값**: `filter` 파라미터를 생략하면 `"ALL"`이 기본값으로 적용됩니다.
- **개인/그룹 모두 지원**: 개인 공부방과 그룹 스터디의 문제를 모두 포함합니다.

### 7.7. 데이터베이스 스키마 보강

`problem_review_states` 테이블에 다음 필드가 추가되었습니다:

| 컬럼명 | 타입 | NULL | 키 | 설명 |
|--------|------|------|-----|------|
| today_review_included_date | DATE | YES | INDEX | 오늘의 복습 목록에 포함된 날짜 |
| today_review_included_gate | VARCHAR(20) | YES | | 목록 포함 시점의 관문 상태 (필터 일관성) |
| today_review_first_attempt_date | DATE | YES | | 오늘의 복습 첫 시도 완료 날짜 |

**인덱스**:
- `(user_id, today_review_included_date)`: 오늘의 복습 쿼리 최적화

---

## 8. 문제 풀이 제출 API

### 8.1. 기본 정보

- **Endpoint**: `POST /api/problems/{problemId}/submit`
- **설명**: 문제를 풀고 답안을 제출하여 채점 결과와 복습 상태를 반환받습니다.
- **인증**: 필수 (JWT 쿠키)

### 8.2. Request

#### Path Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| problemId | Long | O | 문제 ID |

#### Headers

```
Content-Type: application/json
Cookie: accessToken={JWT_TOKEN}
```

#### Body

| 필드 | 타입 | 필수 | 설명 | 예시 |
|------|------|------|------|------|
| answer | String | O | 제출 답안 | "3" (객관식), "true" (OX), "Garbage Collector" (단답형/서술형) |

**답안 형식:**
- **객관식 (MCQ)**: 선택지 인덱스 (0부터 시작, 문자열로 전달)
- **OX (TRUE_FALSE)**: "true" 또는 "false"
- **단답형 (SHORT_ANSWER)**: 답안 텍스트
- **서술형 (ESSAY)**: 답안 텍스트

#### Request Example

```json
{
  "answer": "3"
}
```

### 8.3. Response

#### Response Body

| 필드 | 타입 | 설명 | 예시 |
|------|------|------|------|
| isCorrect | Boolean | 채점 결과 (정답 여부) | true |
| explanation | String | 문제 해설 | "public은 모든 클래스에서 접근 가능합니다." |
| aiFeedback | String | AI 피드백 (서술형만, 나머지는 null) | "키워드를 모두 포함하여..." |
| currentGate | String | 현재 복습 관문 | "GATE_2", "GRADUATED" |
| reviewCount | Integer | 복습 완료 횟수 | 1 |
| nextReviewDate | String | 다음 복습 예정일 (yyyy-MM-dd, 졸업 시 null) | "2025-01-31" |
| isFirstAttempt | Boolean | 오늘의 복습 첫 시도 여부 | true |
| isReviewStateChanged | Boolean | 복습 상태 변경 여부 (승급/강등) | true |

#### 시나리오별 Response Examples

**8.3.1. 오늘의 복습 문제 첫 시도 - 정답 (GATE_1 → GATE_2 승급)**

```json
{
  "isCorrect": true,
  "explanation": "public은 모든 클래스에서 접근 가능합니다.",
  "aiFeedback": null,
  "currentGate": "GATE_2",
  "reviewCount": 1,
  "nextReviewDate": "2025-01-31",
  "isFirstAttempt": true,
  "isReviewStateChanged": true
}
```

**8.3.2. 오늘의 복습 문제 첫 시도 - 오답 (GATE_2 → GATE_1 강등)**

```json
{
  "isCorrect": false,
  "explanation": "Java는 인터페이스를 통한 다중 구현만 지원하며, 클래스 다중 상속은 지원하지 않습니다.",
  "aiFeedback": null,
  "currentGate": "GATE_1",
  "reviewCount": 2,
  "nextReviewDate": "2025-01-25",
  "isFirstAttempt": true,
  "isReviewStateChanged": true
}
```

**8.3.3. 오늘의 복습 문제 재시도 (상태 불변)**

```json
{
  "isCorrect": true,
  "explanation": "Garbage Collector가 Heap 영역의 사용하지 않는 객체를 자동으로 정리합니다.",
  "aiFeedback": null,
  "currentGate": "GATE_2",
  "reviewCount": 2,
  "nextReviewDate": "2025-01-24",
  "isFirstAttempt": false,
  "isReviewStateChanged": false
}
```

**8.3.4. 서술형 문제 AI 채점**

```json
{
  "isCorrect": true,
  "explanation": "IoC는 객체의 생성과 의존성 관리를 개발자가 아닌 프레임워크(Spring Container)가 담당하는 설계 원칙입니다.",
  "aiFeedback": "필수 키워드를 모두 포함하고 정확하게 설명하셨습니다. 제어의 역전, Spring Container, 객체 생성 개념이 명확히 드러나 있습니다.",
  "currentGate": "GATE_2",
  "reviewCount": 1,
  "nextReviewDate": "2025-01-31",
  "isFirstAttempt": true,
  "isReviewStateChanged": true
}
```

**8.3.5. 비복습 문제 풀이 (미래 문제 또는 졸업 문제)**

```json
{
  "isCorrect": false,
  "explanation": "200번대는 성공, 300번대는 리다이렉션, 400번대는 클라이언트 오류, 500번대는 서버 오류를 나타냅니다.",
  "aiFeedback": null,
  "currentGate": "GRADUATED",
  "reviewCount": 3,
  "nextReviewDate": null,
  "isFirstAttempt": false,
  "isReviewStateChanged": false
}
```

### 8.4. Error Responses

#### 400 Bad Request - 답안 누락

```json
{
  "title": "유효하지 않은 입력값",
  "status": 400,
  "detail": "answer: 답안은 필수입니다",
  "instance": "/api/problems/1/submit"
}
```

#### 401 Unauthorized - 인증 실패

```json
{
  "title": "토큰을 찾을 수 없음",
  "status": 401,
  "detail": "인증 토큰이 제공되지 않았습니다.",
  "instance": "/api/problems/1/submit"
}
```

#### 404 Not Found - 문제를 찾을 수 없음

```json
{
  "title": "문제를 찾을 수 없음",
  "status": 404,
  "detail": "요청한 ID의 문제가 존재하지 않습니다.",
  "instance": "/api/problems/999/submit"
}
```

### 8.5. 비즈니스 로직

#### 8.5.1. 동작 방식 개요

문제 풀이 제출 API는 설계 문서 `docs/design/today-review-and-problem-submission.md`의 의사 결정 플로우차트를 따릅니다.

```
[문제 풀이 요청]
     ↓
[ReviewState 조회]
     ↓
     ├─ 없음? → [ReviewState 생성] → [채점만 제공, 상태 불변]
     ↓
     └─ 있음
          ↓
    [todayReviewIncludedDate == today?]
          ↓
          ├─ NO (비복습 문제) → [채점만 제공, 상태 불변]
          ↓
          └─ YES (오늘의 복습 문제)
               ↓
         [todayReviewFirstAttemptDate == null OR != today?]
               ↓
               ├─ NO (재시도) → [채점만 제공, 상태 불변]
               ↓
               └─ YES (첫 시도)
                    ↓
              [채점 + 상태 전이 + todayReviewFirstAttemptDate 기록]
```

#### 8.5.2. 상태 전이 규칙

**첫 시도 정답:**
- `GATE_1 → GATE_2` (nextReviewDate = today + 7일)
- `GATE_2 → GRADUATED` (nextReviewDate = null)

**첫 시도 오답:**
- `GATE_1 → GATE_1` 유지 (nextReviewDate = today + 1일)
- `GATE_2 → GATE_1` 강등 (nextReviewDate = today + 1일)

**재시도 (2회차 이상):**
- 모든 경우 상태 불변 (채점만 제공)

#### 8.5.3. 그룹방 타인 문제 첫 풀이

그룹방에서 다른 멤버가 만든 문제를 처음 풀 때:
1. `ProblemReviewState` 자동 생성
2. gate = `GATE_1`, nextReviewDate = `today + 1일`
3. 채점만 제공 (상태 전이 없음)
4. 다음 날부터 "오늘의 복습 문제"로 조회됨

#### 8.5.4. 채점 로직

- **객관식 (MCQ)**: 제출한 선택지 인덱스와 정답 인덱스 비교
- **OX (TRUE_FALSE)**: boolean 값 비교
- **단답형 (SHORT_ANSWER)**: 대소문자 무시, 앞뒤 공백 제거 후 문자열 비교
- **서술형 (ESSAY)**: AI 채점 서비스 (`AiGradingService`) 호출하여 키워드 기반 채점

### 8.6. 주요 특징

1. **오늘의 복습 문제와 비복습 문제 구분**
   - 오늘의 복습 문제: 매일 자정 스냅샷된 문제 (`todayReviewIncludedDate == today`)
   - 비복습 문제: 미래에 복습할 문제 또는 졸업한 문제

2. **첫 시도 기준 상태 전이**
   - 오늘의 복습 문제를 처음 풀었을 때만 상태 변경
   - 이후 재시도는 채점만 제공하여 학습 진행 일관성 유지

3. **다양한 문제 유형 지원**
   - 객관식, OX, 단답형은 자동 채점
   - 서술형은 AI 채점 서비스 통합

4. **풀이 이력 기록**
   - 모든 시도는 `problem_attempts` 테이블에 기록
   - 정답/오답, 제출 답안, AI 피드백 저장

---

## 9. 이메일 알림 설정 API

### 9.1. 기본 정보

- **Endpoint**: `PATCH /api/problems/{problemId}/email-notification`
- **설명**: 그룹방 타인 문제에 대한 복습 이메일 알림 수신 여부를 설정합니다.
- **인증**: 필수 (JWT 쿠키)

### 9.2. Request

#### Path Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| problemId | Long | O | 문제 ID |

#### Headers

```
Content-Type: application/json
Cookie: accessToken={JWT_TOKEN}
```

#### Body

| 필드 | 타입 | 필수 | 설명 | 예시 |
|------|------|------|------|------|
| receiveEmailNotification | Boolean | O | 이메일 알림 수신 여부 | true |

#### Request Example

```json
{
  "receiveEmailNotification": true
}
```

### 9.3. Response

#### Success (200 OK)

| 필드 | 타입 | 설명 | 예시 |
|------|------|------|------|
| receiveEmailNotification | Boolean | 설정된 이메일 알림 수신 여부 | true |
| message | String | 성공 메시지 | "이메일 알림 설정이 완료되었습니다." |

#### Response Examples

**9.3.1. 알림 수신 설정**

```json
{
  "receiveEmailNotification": true,
  "message": "이메일 알림 설정이 완료되었습니다."
}
```

**9.3.2. 알림 거부 설정**

```json
{
  "receiveEmailNotification": false,
  "message": "이메일 알림 설정이 완료되었습니다."
}
```

### 9.4. Error Responses

#### 400 Bad Request - 문제를 풀지 않음

**문제를 아직 풀지 않은 경우**
```json
{
  "title": "문제를 풀지 않음",
  "status": 400,
  "detail": "아직 풀지 않은 문제입니다. 문제를 먼저 풀어주세요.",
  "instance": "/api/problems/6/email-notification"
}
```

#### 400 Bad Request - 본인 문제 설정 시도

**본인이 만든 문제의 알림 설정을 변경하려는 경우**
```json
{
  "title": "알림 설정 변경 불가",
  "status": 400,
  "detail": "본인이 만든 문제는 이메일 알림 설정을 변경할 수 없습니다.",
  "instance": "/api/problems/1/email-notification"
}
```

#### 400 Bad Request - 이미 설정 완료

**이미 알림 설정을 변경한 경우**
```json
{
  "title": "알림 설정 이미 완료",
  "status": 400,
  "detail": "이메일 알림 설정은 한 번만 변경할 수 있습니다.",
  "instance": "/api/problems/6/email-notification"
}
```

#### 401 Unauthorized - 인증 실패

**JWT 토큰이 없거나 유효하지 않은 경우**
```json
{
  "title": "토큰을 찾을 수 없음",
  "status": 401,
  "detail": "인증 토큰이 제공되지 않았습니다.",
  "instance": "/api/problems/6/email-notification"
}
```

#### 404 Not Found - 문제를 찾을 수 없음

**존재하지 않는 문제 ID로 요청한 경우**
```json
{
  "title": "문제를 찾을 수 없음",
  "status": 404,
  "detail": "요청한 ID의 문제가 존재하지 않습니다.",
  "instance": "/api/problems/999/email-notification"
}
```

### 9.5. 비즈니스 규칙

#### 9.5.1. 설정 가능 조건

이메일 알림 설정을 변경하려면 다음 조건을 모두 만족해야 합니다:

1. **문제를 최소 한 번 이상 풀었어야 함**
   - `ProblemReviewState`가 존재해야 함
   - 문제를 풀지 않은 상태에서는 설정 불가

2. **타인이 만든 문제여야 함**
   - 본인이 만든 문제는 항상 이메일 알림 필수 (true 고정)
   - 본인 문제의 알림 설정은 변경 불가

3. **아직 설정을 변경하지 않았어야 함**
   - 이메일 알림 설정은 문제당 **한 번만** 변경 가능
   - 이미 설정을 변경한 경우 재변경 불가

#### 9.5.2. 기본 동작

- **본인이 만든 문제**
  - 이메일 알림: `true` (필수, 변경 불가)
  - `emailNotificationConfigured`: `true` (처음부터 설정 완료 상태)

- **타인이 만든 문제 (그룹방)**
  - 기본값: `false` (알림 받지 않음)
  - 첫 풀이 후 한 번만 설정 가능
  - 설정 후 `emailNotificationConfigured`: `true`

#### 9.5.3. 사용 시나리오

**시나리오 1: 그룹방 타인 문제를 처음 풀고 알림 받기**
1. 그룹방의 다른 멤버가 만든 문제를 풀이 (`POST /api/problems/{problemId}/submit`)
2. `ProblemReviewState` 자동 생성 (`receiveEmailNotification: false`, `emailNotificationConfigured: false`)
3. 알림 수신 설정 (`PATCH /api/problems/{problemId}/email-notification`)
4. `receiveEmailNotification: true`, `emailNotificationConfigured: true`로 변경
5. 다음 날부터 해당 문제가 복습 알림 메일에 포함됨

**시나리오 2: 그룹방 타인 문제를 풀었지만 알림 거부**
1. 그룹방의 다른 멤버가 만든 문제를 풀이
2. `ProblemReviewState` 자동 생성 (`receiveEmailNotification: false`)
3. 알림 설정을 하지 않거나 `false`로 설정
4. 해당 문제는 복습 알림 메일에 포함되지 않음

**시나리오 3: 본인 문제 알림 설정 시도 (실패)**
1. 본인이 만든 문제에 대해 알림 설정 API 호출
2. `400 Bad Request` 응답 (본인 문제는 설정 변경 불가)

### 9.6. 주요 특징

1. **선택적 알림 수신**
   - 그룹방에서 타인이 만든 문제만 알림 수신 여부 선택 가능
   - 본인이 만든 문제는 항상 알림 필수

2. **1회 설정 제한**
   - 문제당 한 번만 설정 가능하여 의사 결정 명확성 보장
   - 무분별한 설정 변경 방지

3. **문제 풀이 후 설정**
   - 문제를 먼저 풀어본 후에 알림 여부 결정
   - 문제의 난이도와 중요도를 파악한 후 선택 가능

4. **기본값 false**
   - 타인 문제는 기본적으로 알림을 보내지 않음
   - 사용자가 명시적으로 선택한 문제만 알림 수신
