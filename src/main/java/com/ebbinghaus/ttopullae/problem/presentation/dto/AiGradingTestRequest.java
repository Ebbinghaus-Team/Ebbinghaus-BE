package com.ebbinghaus.ttopullae.problem.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * AI 채점 테스트 요청 DTO
 */
@Schema(description = "AI 채점 테스트 요청")
public record AiGradingTestRequest(

    @Schema(description = "주제", example = "Spring Framework")
    @NotBlank(message = "주제는 필수입니다")
    String topic,

    @Schema(description = "문제", example = "IoC(Inversion of Control)란 무엇인가?")
    @NotBlank(message = "문제는 필수입니다")
    String question,

    @Schema(description = "모범 답안", example = "제어의 역전으로, 객체의 생성과 관리를 개발자가 아닌 스프링 컨테이너가 담당하는 것을 의미합니다.")
    @NotBlank(message = "모범 답안은 필수입니다")
    String modelAnswer,

    @Schema(description = "핵심 키워드 리스트", example = "[\"제어의 역전\", \"컨테이너\"]")
    @NotEmpty(message = "최소 1개 이상의 키워드가 필요합니다")
    List<String> keywords,

    @Schema(description = "사용자 답안", example = "제어의 역전이며, 스프링 컨테이너가 객체를 관리합니다.")
    @NotBlank(message = "사용자 답안은 필수입니다")
    String userAnswer
) {
}
