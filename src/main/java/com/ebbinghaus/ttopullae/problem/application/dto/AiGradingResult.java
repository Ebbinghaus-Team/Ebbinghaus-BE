package com.ebbinghaus.ttopullae.problem.application.dto;

import java.util.List;

/**
 * AI 채점 결과 DTO
 * OpenAI API로부터 받은 서술형 답안 채점 결과를 담는 데이터 전송 객체
 */
public record AiGradingResult(
    Boolean isCorrect,
    String feedback,
    List<String> missingKeywords,
    String scoringReason
) {
}
