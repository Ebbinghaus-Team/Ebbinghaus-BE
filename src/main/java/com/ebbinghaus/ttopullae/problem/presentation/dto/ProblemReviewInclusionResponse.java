package com.ebbinghaus.ttopullae.problem.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "문제 복습 루프 포함 설정 응답")
public record ProblemReviewInclusionResponse(

        @Schema(description = "설정된 복습 루프 포함 여부", example = "true")
        Boolean includeInReview,

        @Schema(description = "성공 메시지", example = "복습 루프 포함 설정이 완료되었습니다.")
        String message
) {
    public static ProblemReviewInclusionResponse of(Boolean includeInReview) {
        return new ProblemReviewInclusionResponse(
                includeInReview,
                "복습 루프 포함 설정이 완료되었습니다."
        );
    }
}
