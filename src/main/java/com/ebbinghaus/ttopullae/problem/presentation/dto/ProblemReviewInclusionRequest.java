package com.ebbinghaus.ttopullae.problem.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "문제 복습 루프 포함 설정 요청")
public record ProblemReviewInclusionRequest(

        @Schema(description = "복습 루프 포함 여부", example = "true")
        @NotNull(message = "복습 루프 포함 여부는 필수입니다")
        Boolean includeInReview
) {
}
