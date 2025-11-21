package com.ebbinghaus.ttopullae.problem.application.dto;

import com.ebbinghaus.ttopullae.problem.domain.Problem;
import com.ebbinghaus.ttopullae.problem.domain.ProblemType;

import java.time.LocalDateTime;

public record ProblemCreateResult(
        Long problemId,
        Long studyRoomId,
        ProblemType problemType,
        String question,
        LocalDateTime createdAt
) {
    public static ProblemCreateResult from(Problem problem) {
        return new ProblemCreateResult(
                problem.getProblemId(),
                problem.getStudyRoom().getStudyRoomId(),
                problem.getProblemType(),
                problem.getQuestion(),
                problem.getCreatedAt()
        );
    }
}
