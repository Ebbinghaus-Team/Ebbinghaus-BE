package com.ebbinghaus.ttopullae.problem.presentation.dto;

import com.ebbinghaus.ttopullae.problem.application.dto.ProblemCreateCommand;
import com.ebbinghaus.ttopullae.problem.domain.ProblemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ProblemCreateRequest(
        @NotNull(message = "문제 유형은 필수입니다")
        ProblemType problemType,

        @NotBlank(message = "문제 내용은 필수입니다")
        String question,

        @NotBlank(message = "해설은 필수입니다")
        String explanation,

        List<String> choices,
        Integer correctChoiceIndex,

        Boolean answerBoolean,

        String answerText,

        String modelAnswerText,
        List<String> keywords
) {
    public ProblemCreateCommand toCommand(Long userId, Long studyRoomId) {
        return new ProblemCreateCommand(
                userId,
                studyRoomId,
                problemType,
                question,
                explanation,
                choices,
                correctChoiceIndex,
                answerBoolean,
                answerText,
                modelAnswerText,
                keywords
        );
    }
}
