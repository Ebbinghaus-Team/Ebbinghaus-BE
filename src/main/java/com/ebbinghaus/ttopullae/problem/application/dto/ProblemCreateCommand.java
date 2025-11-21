package com.ebbinghaus.ttopullae.problem.application.dto;

import com.ebbinghaus.ttopullae.problem.domain.ProblemType;

import java.util.List;

public record ProblemCreateCommand(
        Long userId,
        Long studyRoomId,
        ProblemType problemType,
        String question,
        String explanation,

        List<String> choices,
        Integer correctChoiceIndex,

        Boolean answerBoolean,

        String answerText,

        String modelAnswerText,
        List<String> keywords
) {
}
