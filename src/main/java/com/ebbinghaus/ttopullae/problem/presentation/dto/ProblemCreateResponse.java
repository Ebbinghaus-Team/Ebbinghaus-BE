package com.ebbinghaus.ttopullae.problem.presentation.dto;

import com.ebbinghaus.ttopullae.problem.application.dto.ProblemCreateResult;
import com.ebbinghaus.ttopullae.problem.domain.ProblemType;

public record ProblemCreateResponse(
        Long problemId,
        Long studyRoomId,
        ProblemType problemType,
        String question,
        String createdAt
) {
    public static ProblemCreateResponse from(ProblemCreateResult result) {
        return new ProblemCreateResponse(
                result.problemId(),
                result.studyRoomId(),
                result.problemType(),
                result.question(),
                result.createdAt().toString()
        );
    }
}
