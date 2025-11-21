# API 명세서

> 작성일: 2025-01-18
> 버전: v2.0
> **변경사항**: JWT 쿠키 기반 인증 시스템 도입

## 목차

1. [인증 API](#1-인증-api)
   - [1.1. 회원가입 API](#11-회원가입-api)
   - [1.2. 로그인 API](#12-로그인-api)
   - [1.3. 로그아웃 API](#13-로그아웃-api)
2. [개인 공부방 생성 API](#2-개인-공부방-생성-api)
3. [그룹 스터디 생성 API](#3-그룹-스터디-생성-api)
4. [문제 생성 API](#4-문제-생성-api)

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
| accessToken | String | JWT 액세스 토큰 | "eyJhbGciOiJIUzI1..." |

**Set-Cookie Header**:
```
Set-Cookie: accessToken={JWT_TOKEN}; Path=/; HttpOnly; SameSite=None; Max-Age=86400
```

##### Response Example

```json
{
  "userId": 1,
  "email": "user@example.com",
  "username": "홍길동",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
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