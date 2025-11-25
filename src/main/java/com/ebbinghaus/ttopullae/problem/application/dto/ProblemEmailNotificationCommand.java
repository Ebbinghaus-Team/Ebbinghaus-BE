package com.ebbinghaus.ttopullae.problem.application.dto;

public record ProblemEmailNotificationCommand(
        Long userId,
        Long problemId,
        Boolean receiveEmailNotification
) {
}
