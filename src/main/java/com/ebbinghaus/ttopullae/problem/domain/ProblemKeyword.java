package com.ebbinghaus.ttopullae.problem.domain;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Entity @Getter
@Table(name = "problem_keywords")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProblemKeyword { // BaseTimeEntity 상속 제거

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long keywordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(nullable = false)
    private String keyword;
}
