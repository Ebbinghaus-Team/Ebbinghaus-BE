package com.ebbinghaus.ttopullae.problem.presentation.dto;

import com.ebbinghaus.ttopullae.problem.application.dto.ProblemDetailResult;
import com.ebbinghaus.ttopullae.problem.domain.ProblemType;
import com.ebbinghaus.ttopullae.problem.domain.ReviewGate;

import java.time.LocalDate;
import java.util.List;

public record ProblemDetailResponse(
        Long problemId,
        String question,
        ProblemType problemType,
        Long studyRoomId,
        List<String> choices,
        ReviewGate currentGate,
        LocalDate nextReviewDate,
        Integer reviewCount,
        Boolean includeInReview
) {
    public static ProblemDetailResponse from(ProblemDetailResult result) {
        return new ProblemDetailResponse(
                result.problemId(),
                result.question(),
                result.problemType(),
                result.studyRoomId(),
                result.choices(),
                result.currentGate(),
                result.nextReviewDate(),
                result.reviewCount(),
                result.includeInReview()
        );
    }
}
