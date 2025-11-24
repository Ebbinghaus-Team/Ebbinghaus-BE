# 오늘의 복습 문제 및 문제 풀이 정책 설계 문서

## 문서 정보
- **작성일**: 2025-01-23
- **작성자**: 개발팀
- **버전**: 1.0
- **상태**: MVP 결정 완료

---

## 목차
1. [개요](#1-개요)
2. [오늘의 복습 문제 정의](#2-오늘의-복습-문제-정의)
3. [문제 풀이 정책](#3-문제-풀이-정책)
4. [상태 일관성 문제 및 해결 방안](#4-상태-일관성-문제-및-해결-방안)
5. [MVP 결정사항](#5-mvp-결정사항)
6. [기술 구현 상세](#6-기술-구현-상세)
7. [시나리오별 동작](#7-시나리오별-동작)
8. [향후 확장 경로](#8-향후-확장-경로)

---

## 1. 개요

### 1.1. 배경
에빙하우스 망각 곡선 기반 학습 시스템에서 사용자는 매일 복습해야 할 문제를 조회하고 풀이할 수 있습니다.
이 문서는 "오늘의 복습 문제" 조회 기능과 문제 풀이 시 상태 전이 로직의 설계를 다룹니다.

### 1.2. Two-Strike 복습 모델
- **GATE_1**: 1일차 복습 관문 (생성 또는 이전 시도 후 1일 뒤)
- **GATE_2**: 7일차 복습 관문 (GATE_1 통과 후 7일 뒤)
- **GRADUATED**: 복습 완료 (GATE_2 통과)
- **강등 규칙**: 오답 시 GATE_1로 강등

### 1.3. 핵심 과제
1. 문제를 풀면 상태(`gate`, `nextReviewDate`)가 변경되는데, 오늘 조회된 문제가 목록에서 사라지는 문제
2. 필터(GATE_1/GATE_2) 조건과 실제 상태가 불일치하는 문제
3. 오늘의 복습 문제와 비복습 문제를 구분하여 다르게 처리해야 하는 요구사항
4. 오늘의 복습 문제에 대해 재시도 시 첫 시도 결과를 기준으로 상태를 결정해야 하는 정책

---

## 2. 오늘의 복습 문제 정의

### 2.1. 복습 문제 셋 판단 기준

**정의**: 매일 자정에 스냅샷된 문제들

```
todayReviewIncludedDate = today
```

**스냅샷 대상** (매일 00:00:00 배치 실행 시):
- `nextReviewDate <= today` (오늘 또는 과거가 복습날인 문제)
- `gate != GRADUATED` (졸업하지 않은 문제)
- `todayReviewIncludedDate != today` (이미 오늘 스냅샷되지 않은 문제)

**자동 이월**:
- 어제 스냅샷되었으나 풀지 않은 문제는 오늘 배치에서 자동으로 재스냅샷됨

### 2.2. 필터링 옵션

| 필터 | 설명 | 조회 조건 |
|------|------|----------|
| `ALL` | 모든 복습 문제 | GRADUATED 제외 |
| `GATE_1` | 1일차 복습 문제만 | gate = GATE_1 OR todayReviewIncludedGate = GATE_1 |
| `GATE_2` | 7일차 복습 문제만 | gate = GATE_2 OR todayReviewIncludedGate = GATE_2 |

### 2.3. 정렬 순서
- **기본**: `nextReviewDate` 오름차순 (오래된 복습 문제가 먼저)

---

## 3. 문제 풀이 정책

### 3.1. 핵심 원칙

#### 원칙 1: 모든 문제는 언제든 풀 수 있음
- 사용자는 **오늘의 복습 문제가 아니어도** 공부방의 모든 문제를 풀 수 있습니다.
- 예시: 내일 복습 예정인 문제를 미리 풀기, 졸업한 문제 재학습

#### 원칙 2: 오늘의 복습 문제만 상태 변화 발생
- **오늘의 복습 셋에 포함된 문제**를 풀면 → `gate` 전이 발생
- **오늘의 복습 셋이 아닌 문제**를 풀면 → 채점만 제공, 시도 로그만 남김

#### 원칙 3: 첫 시도 기준 상태 변화
- 오늘의 복습 문제를 **첫 시도**했을 때의 결과만 상태 변화에 영향
- 이후 재시도는 채점만 제공 (상태 불변)

### 3.2. 문제 유형별 처리

| 문제 유형 | 조건 | 처리 내용 |
|----------|------|----------|
| **오늘의 복습 문제 (첫 시도)** | `nextReviewDate <= today` AND 오늘 첫 시도 | 채점 + **상태 전이** + 시도 로그 |
| **오늘의 복습 문제 (재시도)** | `todayReviewIncludedDate = today` AND 이미 첫 시도 완료 | 채점 + 시도 로그 (상태 불변) |
| **비복습 문제 (미래 문제)** | `nextReviewDate > today` | 채점 + 시도 로그 (상태 불변) |
| **비복습 문제 (졸업 문제)** | `gate = GRADUATED` | 채점 + 시도 로그 (상태 불변) |
| **그룹방 타인 문제 (첫 풀이)** | `ProblemReviewState` 없음 | 채점 + **ReviewState 생성** + 시도 로그 |

### 3.3. 상태 전이 규칙

#### 3.3.1. 첫 시도 정답 시
```
GATE_1 + 정답 → GATE_2 (nextReviewDate = today + 7일)
GATE_2 + 정답 → GRADUATED (nextReviewDate = null)
```

#### 3.3.2. 첫 시도 오답 시
```
GATE_1 + 오답 → GATE_1 강등 (nextReviewDate = today + 1일)
GATE_2 + 오답 → GATE_1 강등 (nextReviewDate = today + 1일)
```

#### 3.3.3. 재시도 (2회차 이상)
```
모든 경우 → 상태 불변 (채점만 제공)
```

**중요**: 첫 시도 결과가 최종 상태를 결정합니다.
- 첫 시도 정답 → 이후 틀려도 승급 유지
- 첫 시도 오답 → 이후 맞춰도 강등 유지

### 3.4. 그룹방 타인 문제 첫 풀이

그룹방에서 다른 멤버가 만든 문제를 처음 풀 때:
1. **ProblemReviewState 생성**:
   - gate = GATE_1
   - nextReviewDate = today + 1일
   - todayReviewIncludedDate = null
   - todayReviewIncludedGate = null
   - todayReviewFirstAttemptDate = null
2. **채점 제공 + 시도 로그 저장**
3. **상태 전이는 발생하지 않음** (아직 복습 셋에 미포함)

→ 다음 날부터 "오늘의 복습 문제"로 조회됨

---

## 4. 상태 일관성 문제 및 해결 방안

### 4.1. 핵심 문제 상황

#### 문제 1: nextReviewDate 변경으로 인한 문제 누락

**시나리오**:
```
09:00 - 사용자가 오늘의 복습 문제 조회
        Problem A: nextReviewDate = 2025-01-23 (오늘)

10:00 - Problem A를 정답으로 풀이
        Gate: GATE_1 → GATE_2
        nextReviewDate: 2025-01-23 → 2025-01-30 (+7일)

11:00 - 다시 오늘의 복습 문제 조회
        ❌ Problem A가 목록에서 사라짐 (nextReviewDate > today)
```

**문제점**:
- 사용자가 "오늘 풀었던 문제"를 다시 확인할 수 없음
- "오늘 몇 개 풀었는지" 추적 불가능
- UX 혼란

#### 문제 2: gate 상태 변경으로 인한 필터 불일치

**시나리오**:
```
09:00 - GATE_1 필터로 조회
        Problem A: gate = GATE_1
        Problem B: gate = GATE_1

10:00 - Problem A를 정답으로 풀이
        Gate: GATE_1 → GATE_2

11:00 - 다시 GATE_1 필터로 조회
        ❌ Problem A가 목록에서 사라짐 (gate != GATE_1)
```

**문제점**:
- 필터 기준이 실시간 상태를 기준으로 작동하여 일관성 없음
- "오늘 GATE_1 문제를 몇 개 풀었는지" 추적 불가능

### 4.2. 검토한 설계 방안 (5가지)

#### 방안 1: 자정 배치 처리 ✅
- 매일 자정 스케줄러가 스냅샷 저장
- ✅ **최종 선택 방안** (하이브리드 접근)

#### 방안 2: 쿼리 기반 해결 (상태 변화 지연)
- 문제 풀어도 자정까지 상태 업데이트 안 함
- ❌ 실시간 진행 상황 추적 불가

#### 방안 3: 세션 스냅샷 테이블
- `daily_review_sessions` 테이블 생성
- ⚠️ 장기적으로 유용하나 MVP에 과함

#### 방안 4: todayReviewIncludedDate 필드 추가
- ProblemReviewState에 필드 2개 추가 (Gate는 이미 존재)
- ⚠️ 초기화 시점 문제 발생 (조회 시마다 초기화 시 복잡도 증가)

#### 방안 5: 프론트엔드 상태 관리
- 클라이언트 캐싱
- ❌ 근본적 해결책 아님

---

## 5. MVP 결정사항

### 5.1. 선택: 스케줄러 기반 스냅샷 방식 ✅

**최종 구현 방식**:
- **매일 자정 00:00:00**에 Spring Scheduler가 오늘의 복습 문제 스냅샷을 생성
- **벌크 JPQL UPDATE** 쿼리로 성능 최적화
- **스냅샷 필드**에 현재 상태를 저장하여 하루 동안 일관성 유지

**추가 필드** (ProblemReviewState 엔티티):
1. `todayReviewIncludedDate` (LocalDate): 오늘의 복습에 포함된 날짜
2. `todayReviewIncludedGate` (ReviewGate): 오늘의 복습 포함 시점의 관문 상태 (불변)
3. `todayReviewFirstAttemptDate` (LocalDate): 오늘의 복습 첫 시도 처리한 날짜

### 5.2. 선택 이유
1. **명확한 책임 분리**: 스냅샷 생성(배치) vs 조회(서비스) 로직 분리
2. **쿼리 단순화**: 복잡한 조건문 제거, 단순 `todayReviewIncludedDate = today` 체크
3. **성능 최적화**: 벌크 업데이트로 대량 데이터 처리 효율적
4. **목록 일관성 자동 보장**: 조회 시점마다 초기화 불필요
5. **이월 로직 단순화**: 스냅샷 조건에 자동 포함, 별도 로직 불필요
6. **확장 가능성**: 향후 통계 테이블로 전환 시 스케줄러 재활용 가능

### 5.3. 필드 역할

| 필드 | 역할 | 업데이트 시점 | 특징 |
|------|------|--------------|------|
| `todayReviewIncludedDate` | 목록 일관성 유지 | 매일 자정 배치 | 재스냅샷 가능 (이월) |
| `todayReviewIncludedGate` | 필터 일관성 유지 | 매일 자정 배치 | **불변** (하루 동안 고정) |
| `todayReviewFirstAttemptDate` | 재시도 판단 | 문제 첫 풀이 시 | 문제 풀이 로직에서 관리 |

---

## 6. 기술 구현 상세

### 6.1. 스케줄러 구현 (ReviewScheduleService)

**파일**: `src/main/java/com/ebbinghaus/ttopullae/problem/application/ReviewScheduleService.java`

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewScheduleService {

    private final ProblemReviewStateRepository problemReviewStateRepository;

    @Scheduled(cron = "0 0 0 * * *")  // 매일 자정 00:00:00
    @Transactional
    public void createDailyReviewSnapshot() {
        LocalDate today = LocalDate.now();
        int snapshotCount = problemReviewStateRepository.snapshotTodayReviewProblems(today);
        log.info("오늘의 복습 문제 스냅샷 생성 완료: {} 건", snapshotCount);
    }
}
```

**주요 특징**:
- **실행 시간**: 매일 00:00:00 (KST)
- **트랜잭션**: `@Transactional`로 원자성 보장
- **로깅**: 스냅샷 생성 건수 기록
- **멱등성**: 이미 스냅샷된 문제는 제외 (`todayReviewIncludedDate != today`)

**활성화 방법**: `TtopullaeApplication`에 `@EnableScheduling` 추가 필요

---

### 6.2. 벌크 업데이트 쿼리 (ProblemReviewStateRepository)

**파일**: `src/main/java/com/ebbinghaus/ttopullae/problem/domain/repository/ProblemReviewStateRepository.java`

```java
@Modifying(clearAutomatically = true)
@Query("""
    UPDATE ProblemReviewState prs
    SET prs.todayReviewIncludedDate = :today,
        prs.todayReviewIncludedGate = prs.gate
    WHERE prs.nextReviewDate <= :today
      AND prs.gate <> 'GRADUATED'
      AND (prs.todayReviewIncludedDate IS NULL
           OR prs.todayReviewIncludedDate <> :today)
    """)
int snapshotTodayReviewProblems(@Param("today") LocalDate today);
```

**쿼리 상세**:

| 조건 | 설명 | 의도 |
|------|------|------|
| `nextReviewDate <= today` | 오늘 또는 과거가 복습날 | 오늘 복습할 문제 + 이월 문제 |
| `gate <> 'GRADUATED'` | 졸업하지 않은 문제 | 졸업 문제 제외 |
| `todayReviewIncludedDate IS NULL` | 첫 스냅샷 | 새로운 복습 문제 |
| `todayReviewIncludedDate <> today` | 중복 방지 | 이미 오늘 스냅샷된 문제 제외 |

**업데이트 내용**:
- `todayReviewIncludedDate = today` → 오늘 목록에 포함
- `todayReviewIncludedGate = prs.gate` → 현재 gate를 불변 스냅샷으로 저장

**성능**:
- 벌크 연산으로 모든 사용자의 문제를 한 번에 처리
- N+1 쿼리 문제 없음
- `clearAutomatically = true`로 JPA 캐시 자동 정리

---

### 6.3. 오늘의 복습 문제 조회 쿼리 (간소화됨)

**파일**: `src/main/java/com/ebbinghaus/ttopullae/problem/domain/repository/ProblemReviewStateRepository.java`

```java
@Query("""
    SELECT DISTINCT prs FROM ProblemReviewState prs
    LEFT JOIN FETCH prs.problem p
    WHERE prs.user.userId = :userId
      AND prs.todayReviewIncludedDate = :today
      AND (:targetGate IS NULL OR prs.todayReviewIncludedGate = :targetGate)
    ORDER BY prs.nextReviewDate ASC
    """)
List<ProblemReviewState> findTodayReviewProblems(
    @Param("userId") Long userId,
    @Param("today") LocalDate today,
    @Param("targetGate") ReviewGate targetGate
);
```

**쿼리 단순화**:
- **이전**: 8줄의 복잡한 조건 (nextReviewDate, gate 상태 체크 등)
- **이후**: 3줄의 단순한 조건 (`todayReviewIncludedDate = today`)

**성능 최적화**:
- `LEFT JOIN FETCH prs.problem`: N+1 쿼리 방지
- `todayReviewIncludedDate` 인덱스 활용
- 단순 동등 비교로 쿼리 플랜 최적화

---

### 6.4. 이벤트별 상태 변화 플로우

#### 이벤트 1: 문제 생성 (Problem Creation)

**트리거**: 사용자가 새 문제 생성

**상태 변화**:
```
ProblemReviewState 생성:
- user: 생성자
- problem: 생성된 문제
- gate: GATE_1
- nextReviewDate: 생성일 + 1일
- todayReviewIncludedDate: null
- todayReviewIncludedGate: null
- todayReviewFirstAttemptDate: null
- reviewCount: 0
```

**주요 로직**: `ProblemService.createProblem()`

---

#### 이벤트 2: 매일 자정 배치 실행 (Daily Batch)

**트리거**: 매일 00:00:00 스케줄러 실행

**Before**:
```
Problem A: nextReviewDate=2025-01-24, todayReviewIncludedDate=null, gate=GATE_1
Problem B: nextReviewDate=2025-01-23, todayReviewIncludedDate=2025-01-23, gate=GATE_2 (어제 스냅샷, 미풀이)
Problem C: nextReviewDate=2025-01-25, gate=GATE_1 (미래 문제)
```

**After** (2025-01-24 기준):
```
Problem A: todayReviewIncludedDate=2025-01-24, todayReviewIncludedGate=GATE_1 ← 새로 스냅샷
Problem B: todayReviewIncludedDate=2025-01-24, todayReviewIncludedGate=GATE_2 ← 재스냅샷 (이월)
Problem C: 변화 없음 (nextReviewDate > today)
```

**주요 로직**: `ReviewScheduleService.createDailyReviewSnapshot()`

---

#### 이벤트 3: 오늘의 복습 문제 조회 (Query Today's Review)

**트리거**: 사용자가 오늘의 복습 문제 조회 API 호출

**동작**: 읽기 전용 (`@Transactional(readOnly = true)`)

**반환**:
```java
TodayReviewResult {
    problemId: 123,
    question: "...",
    gate: todayReviewIncludedGate,  // 스냅샷 gate (불변)
    nextReviewDate: 2025-01-24
}
```

**중요**:
- `gate` 필드는 **스냅샷 gate** (`todayReviewIncludedGate`) 반환
- 현재 gate가 변경되어도 API 응답은 일관됨

**주요 로직**: `ProblemService.getTodayReviewProblems()`

---

#### 이벤트 4: 오늘의 복습 문제 첫 시도 (정답)

**트리거**: 오늘의 복습 문제를 처음 풀이 (정답)

**Before**:
```
ProblemReviewState:
- gate: GATE_1
- nextReviewDate: 2025-01-24 (오늘)
- todayReviewIncludedDate: 2025-01-24 (배치에서 스냅샷됨)
- todayReviewIncludedGate: GATE_1
- todayReviewFirstAttemptDate: null
- reviewCount: 0
```

**After**:
```
ProblemReviewState:
- gate: GATE_2 ← 승급
- nextReviewDate: 2025-01-31 (today + 7일)
- todayReviewIncludedDate: 2025-01-24 (유지)
- todayReviewIncludedGate: GATE_1 (유지, 불변)
- todayReviewFirstAttemptDate: 2025-01-24 ← 기록
- reviewCount: 1

ProblemAttempt 생성:
- user, problem, isCorrect=true, attemptDate=2025-01-24
```

**주요 로직**: `ProblemService.submitProblemAnswer()`

---

#### 이벤트 5: 오늘의 복습 문제 첫 시도 (오답)

**트리거**: 오늘의 복습 문제를 처음 풀이 (오답)

**Before**:
```
ProblemReviewState:
- gate: GATE_2
- nextReviewDate: 2025-01-24 (오늘)
- todayReviewIncludedDate: 2025-01-24
- todayReviewIncludedGate: GATE_2
- todayReviewFirstAttemptDate: null
```

**After**:
```
ProblemReviewState:
- gate: GATE_1 ← 강등
- nextReviewDate: 2025-01-25 (today + 1일)
- todayReviewIncludedDate: 2025-01-24 (유지)
- todayReviewIncludedGate: GATE_2 (유지, 불변)
- todayReviewFirstAttemptDate: 2025-01-24 ← 기록

ProblemAttempt 생성:
- user, problem, isCorrect=false, attemptDate=2025-01-24
```

---

#### 이벤트 6: 오늘의 복습 문제 재시도

**트리거**: 오늘 이미 풀었던 문제를 다시 풀이 (정답/오답 무관)

**Before**:
```
ProblemReviewState:
- gate: GATE_2
- todayReviewFirstAttemptDate: 2025-01-24 (오늘)
```

**After**:
```
ProblemReviewState: 변화 없음 (모든 필드 동일)

ProblemAttempt 생성:
- user, problem, isCorrect=true/false, attemptDate=2025-01-24
```

**핵심**:
- `todayReviewFirstAttemptDate`가 오늘이면 재시도로 판단
- 채점 결과만 제공, **상태 불변**

---

#### 이벤트 7: 비복습 문제 풀이 (미래/졸업 문제)

**트리거**: 오늘의 복습 셋이 아닌 문제 풀이

**Before**:
```
ProblemReviewState:
- gate: GATE_1
- nextReviewDate: 2025-01-26 (미래)
- todayReviewIncludedDate: null
```

**After**:
```
ProblemReviewState: 변화 없음

ProblemAttempt 생성:
- user, problem, isCorrect=true/false, attemptDate=2025-01-24
```

**핵심**: 채점만 제공, 상태 불변

---

### 6.5. 문제 풀이 API 구현 가이드라인

> **대상 독자**: 문제 풀이 API (`POST /api/problems/{problemId}/submit`)를 구현할 개발자

#### 6.5.1. 핵심 판단 로직

**단계 1: 오늘의 복습 문제 여부 판단**

```java
boolean isTodayReview = (reviewState.getTodayReviewIncludedDate() != null
                         && reviewState.getTodayReviewIncludedDate().equals(LocalDate.now()));
```

**단계 2: 첫 시도 여부 판단** (오늘의 복습 문제인 경우만)

```java
boolean isFirstAttemptToday = isTodayReview
    && (reviewState.getTodayReviewFirstAttemptDate() == null
        || !reviewState.getTodayReviewFirstAttemptDate().equals(LocalDate.now()));
```

**단계 3: 상태 전이 여부 결정**

```
if (!isTodayReview) {
    → 채점만 제공, 상태 불변
    → 시도 로그만 저장
} else if (!isFirstAttemptToday) {
    → 재시도 (채점만 제공)
    → 상태 불변, 시도 로그만 저장
} else {
    → 첫 시도 (상태 전이 발생)
    → 채점 + 상태 업데이트 + 시도 로그 저장
}
```

#### 6.5.2. 상태 업데이트 체크리스트

**첫 시도 시 필수 업데이트 필드**:

- [ ] `todayReviewFirstAttemptDate = LocalDate.now()`
- [ ] `reviewCount += 1`
- [ ] `gate` 전이 (정답/오답에 따라)
- [ ] `nextReviewDate` 갱신 (gate 전이 규칙 따름)

**주의사항**:
- ❌ `todayReviewIncludedDate` 업데이트 금지 (배치만 수정)
- ❌ `todayReviewIncludedGate` 업데이트 금지 (배치만 수정, 불변)

#### 6.5.3. 의사 결정 플로우차트

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

#### 6.5.4. 엣지 케이스 처리

**케이스 1: 졸업 문제를 다시 풀기**
- `gate = GRADUATED` → 비복습 문제로 처리
- 채점만 제공, 상태 불변

**케이스 2: 자정 직후 문제 풀이**
- 배치가 아직 실행 안 됨 → `todayReviewIncludedDate = 어제`
- 비복습 문제로 처리 (정상 동작)
- 배치 실행 후 스냅샷 생성됨

**케이스 3: 그룹방 타인 문제 첫 풀이**
- `ProblemReviewState` 없음 → 생성
- `nextReviewDate = today + 1일`로 설정
- 채점만 제공, 상태 불변
- 다음 날 배치에서 스냅샷됨

**케이스 4: 첫 시도 후 gate가 GRADUATED**
- `todayReviewFirstAttemptDate != null` → 재시도로 판단
- 채점만 제공, 상태 불변 (GRADUATED 유지)

#### 6.5.5. 안티패턴 (하지 말 것)

❌ **잘못된 예시 1**: 조회 시 스냅샷 필드 초기화
```java
// ❌ 절대 금지: 조회 로직에서 todayReviewIncludedDate 수정
if (reviewState.getTodayReviewIncludedDate() == null) {
    reviewState.setTodayReviewIncludedDate(LocalDate.now());
}
```
→ 배치만 스냅샷 필드를 수정해야 함

❌ **잘못된 예시 2**: 재시도 시 상태 업데이트
```java
// ❌ 재시도 시 gate 변경
if (isCorrect && reviewState.getGate() == ReviewGate.GATE_1) {
    reviewState.setGate(ReviewGate.GATE_2);  // 첫 시도 확인 없이 변경
}
```
→ 첫 시도만 상태 전이

❌ **잘못된 예시 3**: `todayReviewIncludedGate` 갱신
```java
// ❌ 문제 풀이 시 todayReviewIncludedGate 수정
reviewState.setTodayReviewIncludedGate(reviewState.getGate());
```
→ 이 필드는 배치에서만 설정, 하루 동안 불변

#### 6.5.6. 구현 참고 코드 위치

- **문제 풀이 서비스**: `ProblemService.submitProblemAnswer()`
- **상태 전이 로직**: `ProblemReviewState.processReviewResult()`
- **첫 시도 판단**: `ProblemReviewState.isFirstAttemptToday()`
- **오늘의 복습 판단**: `ProblemReviewState.isTodayReview()`

---

### 6.6. 필드 업데이트 시점 요약

| 필드 | 업데이트 주체 | 시점 | 빈도 |
|------|--------------|------|------|
| `gate` | 문제 풀이 로직 | 오늘의 복습 첫 시도 시 | 수시 |
| `nextReviewDate` | 문제 풀이 로직 | 오늘의 복습 첫 시도 시 | 수시 |
| `reviewCount` | 문제 풀이 로직 | 오늘의 복습 첫 시도 시 | 수시 |
| `todayReviewIncludedDate` | 배치 스케줄러 | 매일 00:00:00 | 1회/일 |
| `todayReviewIncludedGate` | 배치 스케줄러 | 매일 00:00:00 | 1회/일 |
| `todayReviewFirstAttemptDate` | 문제 풀이 로직 | 오늘의 복습 첫 시도 시 | 수시 |

---

## 7. 시나리오별 동작

### 7.1. 시나리오 1: 오늘의 복습 문제 첫 시도 (정답)

**초기 상태** (자정 배치 실행 완료 후):
```
ProblemReviewState:
- gate: GATE_1
- nextReviewDate: 2025-01-23 (오늘)
- todayReviewIncludedDate: 2025-01-23  ← 배치에서 스냅샷됨
- todayReviewIncludedGate: GATE_1      ← 배치에서 스냅샷됨
- todayReviewFirstAttemptDate: null
```

**사용자 동작** (10:00): Problem A를 정답으로 풀이

**처리 과정**:
1. ✅ 오늘의 복습 문제 확인: `todayReviewIncludedDate == today` → true
2. ✅ 첫 시도 확인: `todayReviewFirstAttemptDate == null` → true
3. 📝 시도 로그 저장: `ProblemAttempt(isCorrect=true)`
4. 🔄 상태 전이:
   ```
   todayReviewFirstAttemptDate = 2025-01-23  ← 첫 시도 기록
   gate: GATE_1 → GATE_2                     ← 승급
   nextReviewDate: 2025-01-23 → 2025-01-30 (+7일)
   reviewCount: 0 → 1

   (todayReviewIncludedDate, todayReviewIncludedGate는 유지)
   ```

**조회 결과** (11:00에 다시 조회):
- ✅ Problem A 여전히 표시됨
- 이유: `todayReviewIncludedDate = 2025-01-23` (배치에서 설정, 불변)

**GATE_1 필터 조회**:
- ✅ Problem A 여전히 표시됨
- 이유: `todayReviewIncludedGate = GATE_1` (배치에서 설정, 불변)

### 7.2. 시나리오 2: 오늘의 복습 문제 재시도

**초기 상태** (시나리오 1 이후):
```
ProblemReviewState:
- gate: GATE_2
- nextReviewDate: 2025-01-30
- todayReviewIncludedDate: 2025-01-23
- todayReviewIncludedGate: GATE_1
- todayReviewFirstAttemptDate: 2025-01-23
```

**사용자 동작**: Problem A를 다시 풀이 (오답)

**처리 과정**:
1. ✅ 오늘의 복습 문제 확인: `todayReviewIncludedDate = today` → true
2. ❌ 첫 시도 확인: `todayReviewFirstAttemptDate = today` → false (재시도)
3. 📝 시도 로그 저장: `ProblemAttempt(isCorrect=false)`
4. ⏹️ 상태 불변: `processReviewResult`에서 early return

**결과**:
- 채점 결과만 제공: "오답입니다"
- 상태 유지: gate는 여전히 GATE_2 (첫 시도 결과 유지)

### 7.3. 시나리오 3: 비복습 문제 풀이 (미래 문제)

**초기 상태**:
```
ProblemReviewState:
- gate: GATE_1
- nextReviewDate: 2025-01-25 (모레)
- todayReviewIncludedDate: null
- todayReviewIncludedGate: null
- todayReviewFirstAttemptDate: null
```

**사용자 동작**: Problem B를 미리 풀기 (정답)

**처리 과정**:
1. ❌ 오늘의 복습 문제 확인: `nextReviewDate > today` → false
2. 📝 시도 로그 저장: `ProblemAttempt(isCorrect=true)`
3. ⏹️ 상태 불변: `isTodayReview = false` → early return

**결과**:
- 채점 결과만 제공: "정답입니다"
- 상태 유지: gate는 여전히 GATE_1, nextReviewDate는 2025-01-25

### 7.4. 시나리오 4: 그룹방 타인 문제 첫 풀이

**초기 상태**: `ProblemReviewState` 없음

**사용자 동작**: 그룹방 멤버가 만든 Problem C를 풀이 (정답)

**처리 과정**:
1. ❌ ReviewState 조회 실패
2. ➕ ReviewState 생성:
   ```
   ProblemReviewState:
   - gate: GATE_1
   - nextReviewDate: 2025-01-24 (내일)
   - todayReviewIncludedDate: null
   - todayReviewIncludedGate: null
   - todayReviewFirstAttemptDate: null
   - reviewCount: 0
   ```
3. 📝 시도 로그 저장: `ProblemAttempt(isCorrect=true)`
4. ⏹️ 상태 불변: `isTodayReview = false` → early return

**결과**:
- 채점 결과만 제공: "정답입니다"
- 내일부터 "오늘의 복습 문제"로 조회됨

### 7.5. 시나리오 5: 첫 시도 오답 후 재시도 정답

**타임라인**:

**10:00 - 첫 시도 (오답)**:
```
초기: gate=GATE_2, nextReviewDate=2025-01-23
처리:
- todayReviewIncludedDate = 2025-01-23
- todayReviewIncludedGate = GATE_2
- todayReviewFirstAttemptDate = 2025-01-23
- gate: GATE_2 → GATE_1 (강등)
- nextReviewDate: 2025-01-23 → 2025-01-24
```

**11:00 - 재시도 (정답)**:
```
처리:
- 첫 시도 확인: todayReviewFirstAttemptDate = today → 재시도
- 채점만 제공: "정답입니다"
- 상태 불변: gate는 여전히 GATE_1 (강등 유지)
```

**교훈**: 첫 시도 결과가 최종 상태를 결정합니다.

---

## 8. 향후 확장 경로

### 8.1. 스냅샷 테이블 추가 시점

다음 요구사항 발생 시 `daily_review_snapshots` 테이블 추가를 검토합니다:

1. **장기 학습 분석**: "지난 3개월간 복습 패턴 분석"
2. **통계 대시보드**: "주간/월간 복습 완료율 추이"
3. **감사 요구사항**: "특정 날짜에 어떤 문제가 복습 대상이었는지 확인"
4. **A/B 테스트**: 복습 알고리즘 변경 효과 측정

### 8.2. 하이브리드 접근 (권장)

```
현재 (MVP):
- ProblemReviewState의 필드 3개로 당일 일관성 보장
- 조회 성능 우수

미래 (통계 기능 추가 시):
- daily_review_snapshots 테이블 추가
- 스케줄러로 매일 스냅샷 저장
- 실시간 조회는 여전히 현재 방식 사용
- 통계/분석만 스냅샷 테이블 활용
```

### 8.3. 재시도 정책 개선 (선택적)

현재는 무제한 재시도를 허용하지만, 향후 다음 정책을 고려할 수 있습니다:

- **1일 1회 제한**: 첫 시도 후 재시도 차단
- **오답 시만 재시도**: 정답 맞춘 문제는 재시도 불가
- **재시도 횟수 제한**: 하루 최대 3회까지

---

## 9. 결론

### 9.1. 요약

오늘의 복습 문제 조회 기능과 문제 풀이 정책을 설계하여 다음을 달성했습니다:

1. ✅ **목록 일관성**: 문제를 풀어도 오늘 하루 동안 목록에 유지
2. ✅ **필터 일관성**: gate 변경되어도 원래 필터 기준 유지
3. ✅ **유연한 학습**: 모든 문제를 언제든 풀 수 있음
4. ✅ **첫 시도 기준**: 재시도 정책으로 공정한 평가
5. ✅ **단순한 구현**: 필드 3개 추가로 빠른 MVP 구현

### 9.2. 핵심 결정 사항

- ✅ ProblemReviewState에 3개 필드 추가
- ✅ 오늘의 복습 문제만 상태 전이 발생
- ✅ 첫 시도 결과가 최종 상태 결정
- ✅ 비복습 문제는 채점만 제공
- ✅ 그룹방 타인 문제 첫 풀이 시 ReviewState 생성

### 9.3. 기대 효과

- 사용자는 오늘 풀었던 문제를 하루 종일 일관되게 확인 가능
- 필터 기능이 정확하게 동작하여 UX 개선
- 재시도 시에도 공정한 평가 (첫 시도 기준)
- 구현 시간 단축 (1일 이내 완료 가능)
- 인프라 의존성 없어 장애 리스크 감소

---

## 부록

### A. 참고 자료

- PRD 문서: `docs/specs/PRD.md`
- 테이블 설계: `docs/TABLE.md`
- API 스펙: `docs/specs/API_SPEC.md`

### B. 변경 이력

| 버전 | 날짜 | 작성자 | 변경 내용 |
|------|------|--------|----------|
| 1.0 | 2025-01-23 | 개발팀 | 초안 작성, MVP 방안 결정 |
| 2.0 | 2025-01-24 | 개발팀 | 스케줄러 기반 스냅샷 구현으로 변경, 기술 구현 상세 추가, 이벤트별 상태 변화 플로우 문서화, 문제 풀이 API 구현 가이드라인 추가 |

### C. 용어 정의

- **오늘의 복습 문제**: `nextReviewDate <= today OR todayReviewIncludedDate = today`
- **비복습 문제**: 오늘의 복습 문제가 아닌 모든 문제 (미래 문제, 졸업 문제 등)
- **첫 시도**: 오늘의 복습 문제를 오늘 처음으로 풀이하는 것
- **재시도**: 오늘의 복습 문제를 오늘 이미 풀었는데 다시 풀이하는 것
- **상태 전이**: gate 관문이 변경되는 것 (GATE_1 → GATE_2, GATE_2 → GRADUATED, 강등 등)
