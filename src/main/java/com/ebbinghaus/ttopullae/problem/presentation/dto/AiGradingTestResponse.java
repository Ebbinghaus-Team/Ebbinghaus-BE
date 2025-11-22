package com.ebbinghaus.ttopullae.problem.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * AI 채점 테스트 응답 DTO
 */
@Schema(description = "AI 채점 테스트 응답")
public record AiGradingTestResponse(

    @Schema(description = "정답 여부", example = "true")
    Boolean isCorrect,

    @Schema(description = "피드백 메시지", example = "정답입니다. '제어의 역전'의 개념과 '컨테이너'의 역할이 정확하게 설명되었습니다.")
    String feedback,

    @Schema(description = "누락된 키워드 리스트", example = "[]")
    List<String> missingKeywords,

    @Schema(description = "채점 근거", example = "모든 핵심 키워드가 의미적으로 포함되었으며, 모범 답안의 핵심 내용과 일치합니다.")
    String scoringReason
) {
}
