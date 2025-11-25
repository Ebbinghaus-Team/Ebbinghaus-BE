package com.ebbinghaus.ttopullae.problem.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "문제 이메일 알림 설정 응답")
public record ProblemEmailNotificationResponse(

        @Schema(description = "설정된 이메일 알림 수신 여부", example = "true")
        Boolean receiveEmailNotification,

        @Schema(description = "성공 메시지", example = "이메일 알림 설정이 완료되었습니다.")
        String message
) {
    public static ProblemEmailNotificationResponse of(Boolean receiveEmailNotification) {
        return new ProblemEmailNotificationResponse(
                receiveEmailNotification,
                "이메일 알림 설정이 완료되었습니다."
        );
    }
}
