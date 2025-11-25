package com.ebbinghaus.ttopullae.problem.application.dto;

public record ProblemReviewInclusionCommand(
        Long problemId,
        Boolean includeInReview
) {
}
