package com.ebbinghaus.ttopullae.problem.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "문제 풀이 제출 요청")
public record ProblemSubmitRequest(

        @Schema(description = "제출 답안 (객관식: 선택지 인덱스, OX: true/false, 단답형/서술형: 텍스트 답안)", example = "Spring Container")
        @NotNull(message = "답안은 필수입니다")
        String answer,

        @Schema(description = "이메일 알림 수신 여부 (첫 시도 시에만 설정 가능, 본인 문제는 무조건 true)", example = "true")
        Boolean receiveEmailNotification
) {
}
