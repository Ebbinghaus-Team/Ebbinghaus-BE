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
        // [추가] "오늘의 복습" 조회를 위한 핵심 인덱스
        indexes = @Index(name = "idx_user_next_review_date", columnList = "user_id, nextReviewDate")
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

    public void updateGate(ReviewGate gate, LocalDate nextDate) {
        this.gate = gate;
        this.nextReviewDate = nextDate;
    }

    public void increaseReviewCount() {
        // [수정] int 타입이므로 +1 연산이 항상 안전
        this.reviewCount++;
    }
}
