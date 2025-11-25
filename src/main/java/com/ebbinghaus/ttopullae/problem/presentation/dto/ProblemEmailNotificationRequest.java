package com.ebbinghaus.ttopullae.problem.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "문제 이메일 알림 설정 요청")
public record ProblemEmailNotificationRequest(

        @Schema(description = "이메일 알림 수신 여부", example = "true")
        @NotNull(message = "이메일 알림 수신 여부는 필수입니다")
        Boolean receiveEmailNotification
) {
}
