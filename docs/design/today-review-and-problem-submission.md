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

**정의**: 다음 조건 중 하나라도 만족하는 문제

```
nextReviewDate <= today  OR  todayReviewIncludedDate = today
```

- **`nextReviewDate <= today`**: 아직 풀지 않은 복습 대상 문제
- **`todayReviewIncludedDate = today`**: 오늘 이미 풀었지만 목록에 유지해야 하는 문제

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

#### 방안 1: 자정 배치 처리
- 매일 자정 스케줄러가 스냅샷 저장
- ❌ 오버엔지니어링, 인프라 의존성 높음

#### 방안 2: 쿼리 기반 해결 (상태 변화 지연)
- 문제 풀어도 자정까지 상태 업데이트 안 함
- ❌ 실시간 진행 상황 추적 불가

#### 방안 3: 세션 스냅샷 테이블
- `daily_review_sessions` 테이블 생성
- ⚠️ 장기적으로 유용하나 MVP에 과함

#### 방안 4: todayReviewIncludedDate 필드 추가 ✅
- ProblemReviewState에 필드 3개 추가
- ✅ **MVP 선택 방안**

#### 방안 5: 프론트엔드 상태 관리
- 클라이언트 캐싱
- ❌ 근본적 해결책 아님

---

## 5. MVP 결정사항

### 5.1. 선택: 필드 3개 추가 방식 ✅

**추가 필드**:
1. `todayReviewIncludedDate` (LocalDate): 오늘의 복습에 포함된 날짜
2. `todayReviewIncludedGate` (ReviewGate): 오늘의 복습 포함 시점의 관문 상태
3. `todayReviewFirstAttemptDate` (LocalDate): 오늘의 복습 첫 시도 처리한 날짜

### 5.2. 선택 이유
1. **빠른 구현**: 1일 내 구현 가능
2. **인프라 독립성**: 스케줄러 불필요, 장애 포인트 감소
3. **충분한 성능**: 현재 규모에서 최적화된 쿼리 성능
4. **낮은 리스크**: 기존 테이블 활용
5. **확장 가능성**: 향후 스냅샷 테이블로 전환 가능

### 5.3. 필드 역할

| 필드 | 역할 | 업데이트 시점 |
|------|------|--------------|
| `todayReviewIncludedDate` | 목록 일관성 유지 | 오늘의 복습 문제 첫 시도 시 |
| `todayReviewIncludedGate` | 필터 일관성 유지 | 오늘의 복습 문제 첫 시도 시 |
| `todayReviewFirstAttemptDate` | 재시도 판단 | 오늘의 복습 문제 첫 시도 시 |

---

## 6. 기술 구현 상세

### 6.1. 엔티티 구조

```java
@Entity
@Table(name = "problem_review_states")
public class ProblemReviewState extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewStateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    // ===== 기존 필드 =====

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReviewGate gate;

    @Column(nullable = false)
    private LocalDate nextReviewDate;

    @Column(nullable = false)
    private int reviewCount = 0;

    // ===== MVP: 오늘의 복습 일관성 필드 =====

    /**
     * 오늘의 복습 문제에 포함된 날짜
     * - 값이 오늘이면 → 오늘 복습 대상이었던 문제 (목록 유지)
     * - null이면 → 오늘 복습 대상 아님
     */
    @Column(name = "today_review_included_date")
    private LocalDate todayReviewIncludedDate;

    /**
     * 오늘의 복습 포함 시점의 관문 상태
     * - 문제를 풀어서 gate가 변경되어도 필터 일관성 유지
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "today_review_included_gate", length = 20)
    private ReviewGate todayReviewIncludedGate;

    /**
     * 오늘의 복습 첫 시도 처리한 날짜
     * - 값이 오늘이면 → 이미 첫 시도 완료 (재시도는 상태 불변)
     * - 다르거나 null이면 → 아직 첫 시도 안 함 (다음 시도가 상태 변화)
     */
    @Column(name = "today_review_first_attempt_date")
    private LocalDate todayReviewFirstAttemptDate;

    /**
     * 오늘의 복습 문제 첫 시도 여부 판단
     */
    public boolean isFirstAttemptToday(LocalDate today) {
        return todayReviewFirstAttemptDate == null || !todayReviewFirstAttemptDate.equals(today);
    }

    /**
     * 오늘의 복습 문제인지 확인
     */
    public boolean isTodayReviewProblem(LocalDate today) {
        return (nextReviewDate != null && !nextReviewDate.isAfter(today))
                || (todayReviewIncludedDate != null && todayReviewIncludedDate.equals(today));
    }

    /**
     * 문제 풀이 결과를 처리하고 복습 상태를 업데이트합니다.
     *
     * @param isCorrect 정답 여부
     * @param today 오늘 날짜
     * @param isTodayReview 오늘의 복습 문제인지 여부
     */
    public void processReviewResult(boolean isCorrect, LocalDate today, boolean isTodayReview) {
        // 1. 오늘의 복습 문제가 아니면 상태 변화 없음
        if (!isTodayReview) {
            return;
        }

        // 2. 이미 오늘 첫 시도를 처리했으면 상태 변화 없음 (재시도)
        if (!isFirstAttemptToday(today)) {
            return;
        }

        // 3. 첫 시도 처리: 원래 관문 상태 기록
        this.todayReviewIncludedDate = today;
        this.todayReviewIncludedGate = this.gate;  // 변경 전 gate 기록
        this.todayReviewFirstAttemptDate = today;  // 첫 시도 날짜 기록

        // 4. Gate 상태 전이
        if (isCorrect) {
            switch (this.gate) {
                case GATE_1 -> updateGateAndNextReview(ReviewGate.GATE_2, today.plusDays(7));
                case GATE_2 -> updateGateAndNextReview(ReviewGate.GRADUATED, null);
                case GRADUATED -> {} // 졸업 상태는 변화 없음
            }
        } else {
            // 오답 시 GATE_1로 강등
            updateGateAndNextReview(ReviewGate.GATE_1, today.plusDays(1));
        }

        // 5. 복습 횟수 증가
        increaseReviewCount();
    }

    /**
     * 그룹방 타인 문제 첫 풀이 시 ReviewState 생성
     */
    public static ProblemReviewState createForGroupProblem(User user, Problem problem, LocalDate today) {
        ProblemReviewState reviewState = new ProblemReviewState();
        reviewState.user = user;
        reviewState.problem = problem;
        reviewState.gate = ReviewGate.GATE_1;
        reviewState.nextReviewDate = today.plusDays(1);
        reviewState.reviewCount = 0;
        // 오늘의 복습 필드는 null로 유지 (내일부터 복습 대상)
        return reviewState;
    }

    private void updateGateAndNextReview(ReviewGate newGate, LocalDate newNextReviewDate) {
        this.gate = newGate;
        this.nextReviewDate = newNextReviewDate;
    }

    private void increaseReviewCount() {
        this.reviewCount++;
    }
}
```

### 6.2. 리포지토리 메서드

```java
@Repository
public interface ProblemReviewStateRepository extends JpaRepository<ProblemReviewState, Long> {

    /**
     * 오늘의 복습 문제를 조회합니다.
     *
     * 조회 조건:
     * 1. nextReviewDate <= today (아직 풀지 않은 문제)
     * 2. OR todayReviewIncludedDate = today (오늘 이미 푼 문제)
     * 3. gate != GRADUATED (졸업하지 않은 문제)
     * 4. targetGate 필터:
     *    - 현재 gate = targetGate (아직 풀지 않은 문제)
     *    - OR todayReviewIncludedGate = targetGate (오늘 푼 문제)
     *
     * @param userId 사용자 ID
     * @param today 오늘 날짜
     * @param targetGate 필터할 관문 (null이면 전체 조회)
     * @return 오늘의 복습 문제 목록
     */
    @Query("""
        SELECT DISTINCT prs FROM ProblemReviewState prs
        LEFT JOIN FETCH prs.problem p
        WHERE prs.user.userId = :userId
          AND prs.gate <> 'GRADUATED'
          AND (prs.nextReviewDate <= :today OR prs.todayReviewIncludedDate = :today)
          AND (:targetGate IS NULL
               OR prs.gate = :targetGate
               OR (prs.todayReviewIncludedDate = :today AND prs.todayReviewIncludedGate = :targetGate))
        ORDER BY prs.nextReviewDate ASC
        """)
    List<ProblemReviewState> findTodaysReviewProblems(
        @Param("userId") Long userId,
        @Param("today") LocalDate today,
        @Param("targetGate") ReviewGate targetGate
    );

    /**
     * 사용자의 특정 문제에 대한 복습 상태 조회
     */
    Optional<ProblemReviewState> findByUserIdAndProblemId(Long userId, Long problemId);
}
```

### 6.3. 서비스 로직

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProblemService {

    private final ProblemReviewStateRepository reviewStateRepository;
    private final ProblemAttemptRepository attemptRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;

    /**
     * 오늘의 복습 문제 목록을 조회합니다.
     */
    public TodayReviewResult getTodayReviewProblems(TodayReviewCommand command) {
        LocalDate today = LocalDate.now();

        // 필터 파라미터 변환
        ReviewGate targetGate = parseFilterToGate(command.filter());

        // 복습 상태 조회 (Problem 엔티티 fetch join)
        List<ProblemReviewState> reviewStates = reviewStateRepository
            .findTodaysReviewProblems(command.userId(), today, targetGate);

        // DTO 변환
        return TodayReviewResult.of(reviewStates, today);
    }

    /**
     * 문제 풀이 결과를 제출하고 복습 상태를 업데이트합니다.
     */
    @Transactional
    public ProblemSubmitResult submitProblem(ProblemSubmitCommand command) {
        LocalDate today = LocalDate.now();

        // 1. 문제 조회
        Problem problem = problemRepository.findById(command.problemId())
            .orElseThrow(() -> new ApplicationException(ProblemException.PROBLEM_NOT_FOUND));

        // 2. 정답 여부 판단
        boolean isCorrect = problem.checkAnswer(command.userAnswer());

        // 3. 시도 기록 저장 (모든 경우)
        ProblemAttempt attempt = ProblemAttempt.create(
            command.userId(),
            command.problemId(),
            command.userAnswer(),
            isCorrect
        );
        attemptRepository.save(attempt);

        // 4. 복습 상태 조회 또는 생성
        ProblemReviewState reviewState = reviewStateRepository
            .findByUserIdAndProblemId(command.userId(), command.problemId())
            .orElseGet(() -> {
                // 4-1. 그룹방 타인 문제 첫 풀이 → ReviewState 생성
                User user = userRepository.findById(command.userId())
                    .orElseThrow(() -> new ApplicationException(UserException.USER_NOT_FOUND));
                ProblemReviewState newState = ProblemReviewState.createForGroupProblem(user, problem, today);
                return reviewStateRepository.save(newState);
            });

        // 5. 오늘의 복습 문제인지 확인
        boolean isTodayReview = reviewState.isTodayReviewProblem(today);

        // 6. 복습 상태 업데이트 (오늘의 복습 문제 + 첫 시도만 상태 전이)
        reviewState.processReviewResult(isCorrect, today, isTodayReview);

        // 7. 결과 반환
        return ProblemSubmitResult.of(
            problem,
            isCorrect,
            reviewState.getGate(),
            isTodayReview,
            reviewState.isFirstAttemptToday(today)
        );
    }

    private ReviewGate parseFilterToGate(String filter) {
        return switch (filter) {
            case "GATE_1" -> ReviewGate.GATE_1;
            case "GATE_2" -> ReviewGate.GATE_2;
            case "ALL" -> null;
            default -> throw new ApplicationException(CommonException.BAD_REQUEST);
        };
    }
}
```

### 6.4. 데이터베이스 마이그레이션

```sql
-- ProblemReviewState 테이블에 필드 추가
ALTER TABLE problem_review_states
ADD COLUMN today_review_included_date DATE NULL
    COMMENT '오늘의 복습 포함 날짜',
ADD COLUMN today_review_included_gate VARCHAR(20) NULL
    COMMENT '오늘의 복습 포함 시점 관문',
ADD COLUMN today_review_first_attempt_date DATE NULL
    COMMENT '오늘의 복습 첫 시도 처리 날짜';

-- 조회 성능을 위한 인덱스 추가
CREATE INDEX idx_user_today_included
ON problem_review_states(user_id, today_review_included_date);

-- 복합 인덱스 (필터 조회 최적화)
CREATE INDEX idx_user_next_review_gate
ON problem_review_states(user_id, next_review_date, gate);
```

---

## 7. 시나리오별 동작

### 7.1. 시나리오 1: 오늘의 복습 문제 첫 시도 (정답)

**초기 상태**:
```
ProblemReviewState:
- gate: GATE_1
- nextReviewDate: 2025-01-23 (오늘)
- todayReviewIncludedDate: null
- todayReviewIncludedGate: null
- todayReviewFirstAttemptDate: null
```

**사용자 동작**: Problem A를 정답으로 풀이

**처리 과정**:
1. ✅ 오늘의 복습 문제 확인: `nextReviewDate <= today` → true
2. ✅ 첫 시도 확인: `todayReviewFirstAttemptDate == null` → true
3. 📝 시도 로그 저장: `ProblemAttempt(isCorrect=true)`
4. 🔄 상태 전이:
   ```
   todayReviewIncludedDate = 2025-01-23
   todayReviewIncludedGate = GATE_1 (원래 gate)
   todayReviewFirstAttemptDate = 2025-01-23
   gate: GATE_1 → GATE_2
   nextReviewDate: 2025-01-23 → 2025-01-30 (+7일)
   reviewCount: 0 → 1
   ```

**조회 결과** (11:00에 다시 조회):
- ✅ Problem A 여전히 표시됨
- 이유: `todayReviewIncludedDate = 2025-01-23`

**GATE_1 필터 조회**:
- ✅ Problem A 여전히 표시됨
- 이유: `todayReviewIncludedGate = GATE_1`

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

### C. 용어 정의

- **오늘의 복습 문제**: `nextReviewDate <= today OR todayReviewIncludedDate = today`
- **비복습 문제**: 오늘의 복습 문제가 아닌 모든 문제 (미래 문제, 졸업 문제 등)
- **첫 시도**: 오늘의 복습 문제를 오늘 처음으로 풀이하는 것
- **재시도**: 오늘의 복습 문제를 오늘 이미 풀었는데 다시 풀이하는 것
- **상태 전이**: gate 관문이 변경되는 것 (GATE_1 → GATE_2, GATE_2 → GRADUATED, 강등 등)