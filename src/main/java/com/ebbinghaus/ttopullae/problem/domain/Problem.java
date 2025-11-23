package com.ebbinghaus.ttopullae.problem.domain;

import com.ebbinghaus.ttopullae.global.BaseTimeEntity;
import com.ebbinghaus.ttopullae.studyroom.domain.StudyRoom;
import com.ebbinghaus.ttopullae.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@Entity @Getter
@Table(name = "problems")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Problem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long problemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_room_id", nullable = false)
    private StudyRoom studyRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProblemType problemType;

    @Column(nullable = false)
    private String question;

    @Column(nullable = false)
    private String explanation;

    // 유형별 해답 컬럼
    private Boolean answerBoolean;
    private String answerText;
    private String modelAnswerText;
    private Integer correctChoiceIndex;

    @OneToMany(mappedBy = "problem", fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProblemReviewState> reviewStates = new ArrayList<>();
}
