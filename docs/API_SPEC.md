# API 명세서

> 작성일: 2025-11-17
> 버전: v1.0

## 목차

1. [개인 공부방 생성 API](#1-개인-공부방-생성-api)
2. [그룹 스터디 생성 API](#2-그룹-스터디-생성-api)

---

## 1. 개인 공부방 생성 API

### 1.1. 기본 정보

- **Endpoint**: `POST /api/study-rooms/personal`
- **설명**: 사용자가 개인 공부방을 생성합니다.
- **인증**: MVP 단계에서는 Request Body에 `userId` 포함 (향후 JWT 토큰 인증으로 대체 예정)

### 1.2. Request

#### Headers

```
Content-Type: application/json
```

#### Body

| 필드 | 타입 | 필수 | 설명 | 예시 |
|------|------|------|------|------|
| userId | Long | O | 사용자 ID (임시, 인증 구현 후 제거) | 1 |
| name | String | O | 공부방 이름 | "자바 스터디" |
| description | String | X | 공부방 설명 | "자바 개념 정리" |
| category | String | X | 공부방 카테고리 | "프로그래밍" |

#### Request Example

```json
{
  "userId": 1,
  "name": "자바 스터디",
  "description": "자바 개념 정리",
  "category": "프로그래밍"
}
```

### 1.3. Response

#### Success (201 Created)

| 필드 | 타입 | 설명 | 예시 |
|------|------|------|------|
| studyRoomId | Long | 생성된 공부방 ID | 1 |
| name | String | 공부방 이름 | "자바 스터디" |
| category | String | 공부방 카테고리 | "프로그래밍" |
| description | String | 공부방 설명 | "자바 개념 정리" |
| createdAt | String (ISO 8601) | 생성 일시 | "2025-11-17T10:30:00" |

#### Response Example

```json
{
  "studyRoomId": 1,
  "name": "자바 스터디",
  "category": "프로그래밍",
  "description": "자바 개념 정리",
  "createdAt": "2025-11-17T10:30:00"
}
```

### 1.4. Error Responses

#### 400 Bad Request - Validation 실패

**사용자 ID 누락**
```json
{
  "title": "요청 데이터 검증 실패",
  "status": 400,
  "detail": "사용자 ID는 필수입니다",
  "instance": "/api/study-rooms/personal"
}
```

**공부방 이름 누락**
```json
{
  "title": "요청 데이터 검증 실패",
  "status": 400,
  "detail": "공부방 이름은 필수입니다",
  "instance": "/api/study-rooms/personal"
}
```

#### 404 Not Found - 사용자 미존재

```json
{
  "title": "사용자를 찾을 수 없음",
  "status": 404,
  "detail": "요청한 ID의 사용자가 존재하지 않습니다.",
  "instance": "/api/study-rooms/personal"
}
```

### 1.5. 비즈니스 로직

1. Request Body에서 `userId`를 추출하여 사용자 존재 여부 확인
2. 존재하지 않는 사용자일 경우 `404 Not Found` 에러 반환
3. `RoomType.PERSONAL`로 설정하여 개인 공부방 생성
4. `joinCode`는 `null`로 설정 (개인방은 참여 코드 불필요)
5. 생성된 공부방 정보를 `201 Created` 상태 코드와 함께 반환

---

## 2. 그룹 스터디 생성 API

### 2.1. 기본 정보

- **Endpoint**: `POST /api/study-rooms/group`
- **설명**: 사용자가 그룹 스터디를 생성하고, 고유한 참여 코드를 자동으로 발급받습니다.
- **인증**: MVP 단계에서는 Request Body에 `userId` 포함 (향후 JWT 토큰 인증으로 대체 예정)

### 2.2. Request

#### Headers

```
Content-Type: application/json
```

#### Body

| 필드 | 타입 | 필수 | 설명 | 예시 |
|------|------|------|------|------|
| userId | Long | O | 사용자 ID (임시, 인증 구현 후 제거) | 1 |
| name | String | O | 그룹 스터디 이름 | "알고리즘 스터디" |
| description | String | X | 그룹 스터디 설명 | "매일 알고리즘 풀이" |
| category | String | X | 그룹 스터디 카테고리 | "알고리즘" |

#### Request Example

```json
{
  "userId": 1,
  "name": "알고리즘 스터디",
  "description": "매일 알고리즘 풀이",
  "category": "알고리즘"
}
```

### 2.3. Response

#### Success (201 Created)

| 필드 | 타입 | 설명 | 예시 |
|------|------|------|------|
| studyRoomId | Long | 생성된 그룹 스터디 ID | 1 |
| name | String | 그룹 스터디 이름 | "알고리즘 스터디" |
| category | String | 그룹 스터디 카테고리 | "알고리즘" |
| description | String | 그룹 스터디 설명 | "매일 알고리즘 풀이" |
| joinCode | String | 참여 코드 (8자리 영숫자) | "A3K9XP2M" |
| createdAt | String (ISO 8601) | 생성 일시 | "2025-11-17T10:30:00" |

#### Response Example

```json
{
  "studyRoomId": 1,
  "name": "알고리즘 스터디",
  "category": "알고리즘",
  "description": "매일 알고리즘 풀이",
  "joinCode": "A3K9XP2M",
  "createdAt": "2025-11-17T10:30:00"
}
```

### 2.4. Error Responses

#### 400 Bad Request - Validation 실패

**사용자 ID 누락**
```json
{
  "title": "요청 데이터 검증 실패",
  "status": 400,
  "detail": "사용자 ID는 필수입니다",
  "instance": "/api/study-rooms/group"
}
```

**그룹 스터디 이름 누락**
```json
{
  "title": "요청 데이터 검증 실패",
  "status": 400,
  "detail": "그룹 스터디 이름은 필수입니다",
  "instance": "/api/study-rooms/group"
}
```

#### 404 Not Found - 사용자 미존재

```json
{
  "title": "사용자를 찾을 수 없음",
  "status": 404,
  "detail": "요청한 ID의 사용자가 존재하지 않습니다.",
  "instance": "/api/study-rooms/group"
}
```

#### 500 Internal Server Error - 참여 코드 생성 실패

```json
{
  "title": "참여 코드 생성 실패",
  "status": 500,
  "detail": "고유한 참여 코드 생성에 실패했습니다. 잠시 후 다시 시도해주세요.",
  "instance": "/api/study-rooms/group"
}
```

### 2.5. 비즈니스 로직

1. Request Body에서 `userId`를 추출하여 사용자 존재 여부 확인
2. 존재하지 않는 사용자일 경우 `404 Not Found` 에러 반환
3. 8자리 영숫자 랜덤 참여 코드 생성 (중복 확인, 최대 10회 재시도)
4. 10회 재시도 후에도 고유 코드 생성 실패 시 `500 Internal Server Error` 반환
5. `RoomType.GROUP`로 설정하여 그룹 스터디 생성
6. 생성자를 그룹 멤버(`StudyRoomMember`)로 자동 등록
7. 생성된 그룹 스터디 정보 (참여 코드 포함)를 `201 Created` 상태 코드와 함께 반환

### 2.6. 참여 코드 규칙

- **형식**: 영문 대문자 + 숫자 조합
- **길이**: 8자리
- **예시**: `A3K9XP2M`, `7BX4KL9Q`
- **중복 방지**: 데이터베이스에서 중복 확인 후 고유한 코드만 사용

---

## 부록: 공통 사항

### 인증 (Authentication)

**현재 (MVP 단계)**:
- Request Body에 `userId` 필드를 포함하여 사용자 식별

**향후 계획**:
- Spring Security + JWT 인증 구현
- `Authorization: Bearer {token}` 헤더 사용
- Request DTO에서 `userId` 필드 제거

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
- 예시: `2025-11-17T10:30:00`

### 데이터베이스 스키마 관련

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