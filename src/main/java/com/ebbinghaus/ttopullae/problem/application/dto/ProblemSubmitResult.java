package com.ebbinghaus.ttopullae.problem.application.dto;

import com.ebbinghaus.ttopullae.problem.domain.ReviewGate;

public record ProblemSubmitResult(
        boolean isCorrect,
        String explanation,
        String aiFeedback,
        ReviewGate currentGate,
        Integer reviewCount,
        String nextReviewDate,
        boolean isFirstAttempt,
        boolean isReviewStateChanged
) {
}
