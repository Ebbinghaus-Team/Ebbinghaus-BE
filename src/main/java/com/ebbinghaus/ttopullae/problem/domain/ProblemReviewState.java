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

    /**
     * 이메일 알림 수신 여부
     * - true: 복습 알림 메일 받기
     * - false: 복습 알림 메일 받지 않기 (기본값)
     * - 본인이 생성한 문제는 항상 true (필수)
     * - 그룹방 타인 문제는 첫 풀이 후 설정 가능
     */
    @Builder.Default
    @Column(nullable = false)
    private Boolean receiveEmailNotification = Boolean.FALSE;

    /**
     * 이메일 알림 설정 변경 여부
     * - true: 이미 설정을 변경함 (재변경 불가)
     * - false: 아직 설정 안 함 (변경 가능)
     * - 본인이 만든 문제는 처음부터 true (변경 불가)
     */
    @Builder.Default
    @Column(nullable = false)
    private Boolean emailNotificationConfigured = Boolean.FALSE;

    public void updateGate(ReviewGate gate, LocalDate nextDate) {
        this.gate = gate;
        this.nextReviewDate = nextDate;
    }

    public void increaseReviewCount() {
        // [수정] int 타입이므로 +1 연산이 항상 안전
        this.reviewCount++;
    }

    public void recordFirstAttemptToday(LocalDate today) {
        this.todayReviewFirstAttemptDate = today;
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

    /**
     * 이메일 알림 수신 설정 변경
     * @param receive 이메일 알림 수신 여부
     */
    public void configureEmailNotification(boolean receive) {
        this.receiveEmailNotification = receive;
        this.emailNotificationConfigured = true;
    }

    /**
     * 이메일 알림 설정이 가능한 상태인지 확인
     * @return 설정 가능 여부 (아직 설정 안 한 경우 true)
     */
    public boolean canConfigureEmailNotification() {
        return !this.emailNotificationConfigured;
    }
}
