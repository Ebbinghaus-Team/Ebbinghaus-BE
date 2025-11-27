package com.ebbinghaus.ttopullae.problem.application.dto;

import com.ebbinghaus.ttopullae.problem.domain.Problem;
import com.ebbinghaus.ttopullae.problem.domain.ProblemChoice;
import com.ebbinghaus.ttopullae.problem.domain.ProblemReviewState;
import com.ebbinghaus.ttopullae.problem.domain.ProblemType;
import com.ebbinghaus.ttopullae.problem.domain.ReviewGate;

import java.time.LocalDate;
import java.util.List;

public record ProblemDetailResult(
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
    public static ProblemDetailResult of(
            Problem problem,
            List<ProblemChoice> problemChoices,
            ProblemReviewState reviewState
    ) {
        // 객관식 선택지 (정답 제외)
        List<String> choices = null;
        if (problem.getProblemType() == ProblemType.MCQ && problemChoices != null) {
            choices = problemChoices.stream()
                    .map(ProblemChoice::getChoiceText)
                    .toList();
        }

        // ReviewState가 없으면 null 반환
        ReviewGate currentGate = reviewState != null ? reviewState.getGate() : null;
        LocalDate nextReviewDate = reviewState != null ? reviewState.getNextReviewDate() : null;
        Integer reviewCount = reviewState != null ? reviewState.getReviewCount() : null;
        Boolean includeInReview = reviewState != null ? reviewState.getIncludeInReview() : null;

        return new ProblemDetailResult(
                problem.getProblemId(),
                problem.getQuestion(),
                problem.getProblemType(),
                problem.getStudyRoom().getStudyRoomId(),
                choices,
                currentGate,
                nextReviewDate,
                reviewCount,
                includeInReview
        );
    }
}
