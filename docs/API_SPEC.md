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