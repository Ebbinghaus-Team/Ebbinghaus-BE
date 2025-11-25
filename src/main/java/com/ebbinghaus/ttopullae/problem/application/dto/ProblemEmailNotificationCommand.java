package com.ebbinghaus.ttopullae.problem.application.dto;

public record ProblemEmailNotificationCommand(
        Long problemId,
        Boolean receiveEmailNotification
) {
}
