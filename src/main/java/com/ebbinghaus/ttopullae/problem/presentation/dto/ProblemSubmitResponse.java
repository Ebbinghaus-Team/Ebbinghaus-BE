package com.ebbinghaus.ttopullae.problem.presentation.dto;

import com.ebbinghaus.ttopullae.problem.application.dto.ProblemSubmitResult;
import com.ebbinghaus.ttopullae.problem.domain.ReviewGate;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "문제 풀이 제출 응답")
public record ProblemSubmitResponse(

        @Schema(description = "채점 결과 (정답 여부)", example = "true")
        boolean isCorrect,

        @Schema(description = "문제 해설", example = "IoC는 제어의 역전을 의미하며...")
        String explanation,

        @Schema(description = "AI 피드백 (서술형 문제만)", example = "키워드를 모두 포함하여 정확하게 작성하셨습니다.")
        String aiFeedback,

        @Schema(description = "현재 복습 관문", example = "GATE_2")
        ReviewGate currentGate,

        @Schema(description = "복습 완료 횟수", example = "2")
        Integer reviewCount,

        @Schema(description = "다음 복습 예정일 (yyyy-MM-dd)", example = "2025-01-30")
        String nextReviewDate,

        @Schema(description = "오늘의 복습 첫 시도 여부", example = "true")
        boolean isFirstAttempt,

        @Schema(description = "복습 상태 변경 여부 (승급/강등)", example = "true")
        boolean isReviewStateChanged
) {
    public static ProblemSubmitResponse from(ProblemSubmitResult result) {
        return new ProblemSubmitResponse(
                result.isCorrect(),
                result.explanation(),
                result.aiFeedback(),
                result.currentGate(),
                result.reviewCount(),
                result.nextReviewDate(),
                result.isFirstAttempt(),
                result.isReviewStateChanged()
        );
    }
}
