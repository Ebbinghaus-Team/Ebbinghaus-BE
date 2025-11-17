package com.ebbinghaus.ttopullae.problem.domain;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Entity @Getter
@Table(name = "problem_choices")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProblemChoice { // BaseTimeEntity 상속 제거

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long choiceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(nullable = false)
    private Integer choiceOrder;

    @Column(nullable = false)
    private String choiceText;
}
