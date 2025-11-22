package com.ebbinghaus.ttopullae.problem.application.dto;

import java.util.List;

/**
 * AI 채점 요청 DTO
 * OpenAI API에 서술형 답안 채점을 요청하기 위한 데이터 전송 객체
 */
public record AiGradingRequest(
    String topic,
    String question,
    String modelAnswer,
    List<String> keywords,
    String userAnswer
) {
}
