## 🏛️ 최종 테이블 설계 (9 Tables)

### 1. `users` (사용자)

사용자 계정의 기본 정보와 알림 수신 여부를 관리합니다.

| **컬럼명** | **데이터 타입** | **제약조건** | **설명** |
| --- | --- | --- | --- |
| `user_id` | `BIGINT` | **PK**, Auto-increment | 사용자 고유 식별자 (JPA @Id) |
| `email` | `VARCHAR(255)` | **UNIQUE**, **NOT NULL** | 로그인 ID 및 알림 발송 주소 |
| `username` | `VARCHAR(100)` | **NOT NULL** | 사용자 닉네임 |
| `receive_notifications` | `BOOLEAN` | **NOT NULL**, DEFAULT `true` | 이메일 알림 수신 동의 여부 |
| `created_at` | `TIMESTAMP` | **NOT NULL**, DEFAULT `NOW()` | 계정 생성일 |

### 🧐 설계 근거

- **서비스의 주체:** PRD의 모든 기능(학습, 문제 생성, 알림)은 `user`를 기준으로 동작합니다.
- **알림 정책 (PRD 4.3):** `receive_notifications` 컬럼은 "기본 제공(Opt-out)" 원칙을 명시한 PRD 4.3을 직접적으로 구현합니다.
- **무결성:** `email`에 `UNIQUE` 제약을 설정하여 중복 계정을 방지합니다.

---

### 2. `study_rooms` (스터디 룸)

'개인 스터디'와 '그룹 스터디'의 공통 정보를 관리하는 컨테이너입니다.

| **컬럼명** | **데이터 타입** | **제약조건** | **설명** |
| --- | --- | --- | --- |
| `study_room_id` | `BIGINT` | **PK**, Auto-increment | 스터디 룸 고유 식별자 (JPA @Id) |
| `owner_id` | `BIGINT` | **FK** (`users.user_id`), **NOT NULL** | 방 생성자(방장) ID |
| `room_type` | `ENUM('PERSONAL', 'GROUP')` | **NOT NULL** | 스터디 T룸 유형 (개인/그룹) |
| `name` | `VARCHAR(255)` | **NOT NULL** | 스터디 룸 이름 |
| `description` | `TEXT` | `Nullable` | 스터디 룸 설명 |
| `category` | `VARCHAR(100)` | `Nullable` | 스터디 룸 카테고리 |
| `join_code` | `VARCHAR(50)` | **UNIQUE**, `Nullable` | 그룹 스터디 참여 코드 |
| `created_at` | `TIMESTAMP` | **NOT NULL**, DEFAULT `NOW()` | 스터디 룸 생성일 |

### 🧐 설계 근거

- **효율적인 통합 (PRD 2.1):** '개인'과 '그룹' 스터디는 본질적으로 문제를 담는 '방'이라는 점에서 동일합니다. `room_type`으로 구분하여 테이블을 통합, 중복 설계를 피합니다.
- **그룹 참여 (PRD 3.4.2):** `join_code`는 그룹 스터디 생성 시 발급되는 "고유한 '그룹 참여 코드'" 요구사항을 구현합니다. `UNIQUE` 제약으로 코드의 유일성을 보장합니다.

---

### 3. `study_room_members` (스터디 룸 멤버)

사용자(`users`)와 그룹 스터디 룸(`study_rooms`) 간의 다대다(N:M) 관계를 매핑합니다.

| **컬럼명** | **데이터 타입** | **제약조건** | **설명** |
| --- | --- | --- | --- |
| `member_id` | `BIGINT` | **PK**, Auto-increment | 멤버십 고유 식별자 (JPA @Id) |
| `user_id` | `BIGINT` | **FK** (`users.user_id`), **NOT NULL** | 멤버 ID |
| `study_room_id` | `BIGINT` | **FK** (`study_rooms.study_room_id`), **NOT NULL** | 참여한 스터디 룸 ID |
| `joined_at` | `TIMESTAMP` | **NOT NULL**, DEFAULT `NOW()` | 스터디 룸 참여일 |
|  |  | **UNIQUE (`user_id`, `study_room_id`)** | **(중요)** 중복 참여 방지 제약 |

### 🧐 설계 근거

- **N:M 관계 해소:** 한 명의 사용자가 여러 그룹에, 하나의 그룹이 여러 사용자를 가질 수 있는 관계(PRD 3.4)를 정규화합니다.
- **JPA 편의성:** `member_id`라는 대리 키(PK)를 두어 JPA에서 엔티티를 관리하기 용이하게 합니다.
- **데이터 무결성:** `UNIQUE(user_id, study_room_id)` 제약은 한 사용자가 같은 방에 두 번 참여하는 비즈니스 로직 오류를 DB 레벨에서 원천 차단합니다.

---

### 4. `problems` (문제 원본)

4가지 고정 유형의 문제 원본과 **단일 값 정답**을 저장합니다.

| **컬럼명** | **데이터 타입** | **제약조건** | **설명** |
| --- | --- | --- | --- |
| `problem_id` | `BIGINT` | **PK**, Auto-increment | 문제 고유 식별자 (JPA @Id) |
| `study_room_id` | `BIGINT` | **FK** (`study_rooms.study_room_id`), **NOT NULL** | 소속 스터디 룸 |
| `creator_id` | `BIGINT` | **FK** (`users.user_id`), **NOT NULL** | 문제 생성자 |
| `problem_type` | `ENUM('MCQ', 'OX', 'SHORT', 'SUBJECTIVE')` | **NOT NULL** | 4가지 문제 유형 (PRD 2.2) |
| `question` | `TEXT` | **NOT NULL** | 문제의 '질문' |
| `explanation` | `TEXT` | **NOT NULL** | 문제 '해설' |
| `answer_boolean` | `BOOLEAN` | `Nullable` | (OX 전용) 정답 (PRD 3.2.2) |
| `answer_text` | `TEXT` | `Nullable` | (단답형 전용) 정답 (PRD 3.2.2) |
| `model_answer_text` | `TEXT` | `Nullable` | (서술형 전용) 모범 답안 (PRD 3.2.2) |
| `correct_choice_index` | `INT` | `Nullable` | (객관식 전용) 정답 보기 순서(index) |
| `created_at` | `TIMESTAMP` | **NOT NULL**, DEFAULT `NOW()` | 문제 생성일 |

### 🧐 설계 근거

- **'4가지 유형 고정' 원칙:** 프로젝트 제약에 따라 "유연성"이 불필요하므로, PRD 3.2.2에 명시된 단일 값 정답(`answer(Boolean)`, `answer`, `modelAnswer`)을 `NULL` 허용 컬럼으로 직접 저장합니다. 이는 **DB 레벨에서 데이터 타입을 강제**(예: OX 정답은 반드시 Boolean)하여 무결성을 높입니다.
- **JPA 친화성:** JSON 대신 명시적인 컬럼을 사용하여, JPA에서 타입 안전하게 데이터를 다룰 수 있습니다.
- **생성자 귀속 (PRD 4.4):** `creator_id`는 그룹 문제를 만든 생성자 본인에게는 이 문제를 '개인 문제'처럼 취급(자동 1차 관문 배정)하는 핵심 로직의 기준이 됩니다.

---

### 5. `problem_choices` (객관식 보기)

객관식 문제(MCQ)의 `choices[]` (배열) 데이터를 정규화합니다.

| **컬럼명** | **데이터 타입** | **제약조건** | **설명** |
| --- | --- | --- | --- |
| `choice_id` | `BIGINT` | **PK**, Auto-increment | 보기 고유 식별자 (JPA @Id) |
| `problem_id` | `BIGINT` | **FK** (`problems.problem_id`), **NOT NULL** | 원본 문제 (1:N 관계) |
| `choice_order` | `INT` | **NOT NULL** | 보기 순서 (예: 1, 2, 3, 4) |
| `choice_text` | `TEXT` | **NOT NULL** | 보기 내용 |

### 🧐 설계 근거

- **정규화 (PRD 3.2.2):** PRD에 명시된 `choices (Array)`는 1:N 관계(하나의 `problem`이 여러 `choice`를 가짐)입니다. 이를 별도 테이블로 분리하는 것이 정규화의 기본 원칙입니다.
- **데이터 구조:** `choice_order`를 통해 보기의 순서를 명확히 보장하며, `problems.correct_choice_index`와 매칭됩니다.

---

### 6. `problem_keywords` (서술형 키워드)

서술형 문제(Subjective)의 `keywords[]` (배열) 데이터를 정규화합니다.

| **컬럼명** | **데이터 타입** | **제약조건** | **설명** |
| --- | --- | --- | --- |
| `keyword_id` | `BIGINT` | **PK**, Auto-increment | 키워드 고유 식별자 (JPA @Id) |
| `problem_id` | `BIGINT` | **FK** (`problems.problem_id`), **NOT NULL** | 원본 문제 (1:N 관계) |
| `keyword` | `VARCHAR(255)` | **NOT NULL** | AI 채점 기준 핵심 키워드 |

### 🧐 설계 근거

- **정규화 (PRD 3.2.2):** PRD에 명시된 `keywords (Array)`를 1:N 관계로 분리합니다.
- **AI 채점 근거 (PRD 2.4):** "AI가 '핵심 키워드' 기반으로 보완"한다는 요구사항의 데이터베이스 모델입니다.

---

### 7. `problem_review_states` (복습 상태)

사용자별, 문제별 "Two-Strike" 복습 모델의 **현재 상태(Status)**를 관리합니다.

| **컬럼명** | **데이터 타입** | **제약조건** | **설명** |
| --- | --- | --- | --- |
| `state_id` | `BIGINT` | **PK**, Auto-increment | 상태 고유 식별자 (JPA @Id) |
| `user_id` | `BIGINT` | **FK** (`users.user_id`), **NOT NULL** | 사용자 ID |
| `problem_id` | `BIGINT` | **FK** (`problems.problem_id`), **NOT NULL** | 문제 ID |
| `gate` | `ENUM('GATE_1', 'GATE_2', 'GRADUATED')` | **NOT NULL** | 현재 관문 (PRD 2.3) |
| `next_review_date` | `DATE` | **NOT NULL**, **INDEX** | **다음 복습 예정일** |
| `last_reviewed_at` | `TIMESTAMP` | `Nullable` | 마지막 복습 일시 (PRD 3.1.4) |
| `review_count` | `INT` | **NOT NULL**, DEFAULT `0` | 총 복습 횟수 (PRD 3.1.4) |
|  |  | **UNIQUE (`user_id`, `problem_id`)** | **(중요)** 중복 상태 방지 제약 |

### 🧐 설계 근거

- **핵심 성능 테이블:** PRD 3.3.1의 **'오늘의 복습'** 기능은 이 테이블을 직접 조회합니다. `INDEX(user_id, next_review_date)`는 `WHERE user_id = ? AND next_review_date <= CURRENT_DATE` 쿼리의 성능을 보장하는 **필수 인덱스**입니다.
- **개인화 (PRD 4.4):** "복습 주기는... 철저히 사용자별로 개인화"된다는 요구사항을 `(user_id, problem_id)` 단위로 구현합니다.
- **상태와 이력 분리:** 모든 풀이 이력(`problem_attempts`)을 매번 계산(Calculation)하지 않고, 현재 '상태(State)'만 관리하여 성능을 확보합니다.
- **무결성:** `UNIQUE(user_id, problem_id)` 제약은 한 사용자가 한 문제에 대해 오직 하나의 복습 상태만 갖도록 보장합니다.

---

### 8. `problem_attempts` (문제 풀이 이력)

사용자의 모든 문제 풀이 **시도 이력(History Log)**을 저장합니다.

| **컬럼명** | **데이터 타입** | **제약조건** | **설명** |
| --- | --- | --- | --- |
| `attempt_id` | `BIGINT` | **PK**, Auto-increment | 풀이 시도 고유 식별자 (JPA @Id) |
| `user_id` | `BIGINT` | **FK** (`users.user_id`), **NOT NULL** | 문제를 푼 사용자 |
| `problem_id` | `BIGINT` | **FK** (`problems.problem_id`), **NOT NULL** | 푼 문제 |
| `submitted_answer_text` | `TEXT` | `Nullable` | (단답형/서술형) 제출 답안 |
| `submitted_choice_index` | `INT` | `Nullable` | (객관식) 제출 보기 순서 |
| `submitted_boolean` | `BOOLEAN` | `Nullable` | (OX) 제출 답안 |
| `is_correct` | `BOOLEAN` | **NOT NULL** | 채점 결과 (정답/오답) |
| `ai_feedback_json` | `JSON` | `Nullable` | (서술형) AI 채점 피드백 |
| `attempted_at` | `TIMESTAMP` | **NOT NULL**, DEFAULT `NOW()` | 문제를 푼 시각 |

### 🧐 설계 근거

- **상태와 이력 분리:** `review_states`(현재 상태)와 별개로, 모든 시도 이력을 로그처럼 적재합니다.
- **최초 풀이 (PRD 4.4):** "타 스터디원"이 그룹 문제를 "최초 1회 풀이"했는지 여부를 판단하는 근거가 됩니다. (이 테이블에 `(user_id, problem_id)`로 `SELECT` 시도)
- **AI 피드백 (PRD 2.4):** "AI가 구조화된 피드백(JSON)을 반환"한다는 요구사항을 `ai_feedback_json` 컬럼으로 정확히 구현합니다. **이것이 PRD에 근거한 유일하고 올바른 JSON 사용처입니다.**
- **기술적 예외 처리 (PRD 4.2):** AI 채점 실패 시 "다시 채점하기"는 이 `attempt_id`를 기준으로 재시도를 요청할 수 있습니다.
