package com.ebbinghaus.ttopullae.problem.application.dto;

public record ProblemSubmitCommand(
        Long userId,
        Long problemId,
        String answer,
        Boolean receiveEmailNotification
) {
}
