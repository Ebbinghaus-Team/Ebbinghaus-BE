package com.ebbinghaus.ttopullae.problem.domain;

import com.ebbinghaus.ttopullae.global.BaseTimeEntity;
import com.ebbinghaus.ttopullae.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Builder
@Entity @Getter
@Table(name = "problem_attempts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProblemAttempt extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long attemptId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(columnDefinition = "TEXT")
    private String submittedAnswerText;

    private Integer submittedChoiceIndex;

    private Boolean submittedBoolean;

    @Column(nullable = false)
    private Boolean isCorrect;

    @Column(columnDefinition = "TEXT")
    private String aiFeedbackJson;

    public void updateAiFeedback(String json) {
        this.aiFeedbackJson = json;
    }
}
