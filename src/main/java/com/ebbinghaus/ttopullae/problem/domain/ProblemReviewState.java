package com.ebbinghaus.ttopullae.problem.domain;

import com.ebbinghaus.ttopullae.global.BaseTimeEntity;
import com.ebbinghaus.ttopullae.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Builder
@Entity @Getter
@Table(
        name = "problem_review_states",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "problem_id"}),
        indexes = {
                @Index(name = "idx_user_next_review_date", columnList = "user_id, nextReviewDate"),
                @Index(name = "idx_user_today_included", columnList = "user_id, today_review_included_date")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProblemReviewState extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewGate gate;

    @Column(nullable = false)
    private LocalDate nextReviewDate;

    private int reviewCount;

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

    public void updateGate(ReviewGate gate, LocalDate nextDate) {
        this.gate = gate;
        this.nextReviewDate = nextDate;
    }

    public void increaseReviewCount() {
        // [수정] int 타입이므로 +1 연산이 항상 안전
        this.reviewCount++;
    }

    /**
     * 오늘의 복습 문제 첫 시도 여부 판단
     */
    public boolean isFirstAttemptToday(LocalDate today) {
        return todayReviewFirstAttemptDate == null
                || !todayReviewFirstAttemptDate.equals(today);
    }

    /**
     * 오늘의 복습 문제인지 확인
     */
    public boolean isTodayReviewProblem(LocalDate today) {
        return (nextReviewDate != null && !nextReviewDate.isAfter(today))
                || (todayReviewIncludedDate != null && todayReviewIncludedDate.equals(today));
    }
}
