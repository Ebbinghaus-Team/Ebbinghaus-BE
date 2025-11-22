package com.ebbinghaus.ttopullae.studyroom.application.dto;

import com.ebbinghaus.ttopullae.problem.domain.ProblemType;
import com.ebbinghaus.ttopullae.problem.domain.ReviewGate;
import java.time.LocalDateTime;
import java.util.List;

public record PersonalRoomProblemListResult(
    Long studyRoomId,
    String studyRoomName,
    List<ProblemInfo> problems,
    int totalCount
) {

    public record ProblemInfo(
        Long problemId,
        String question,
        ProblemType problemType,
        ReviewGate reviewGate,
        LocalDateTime createdAt,
        LocalDateTime lastReviewedAt,
        int reviewCount
    ) {
    }
}