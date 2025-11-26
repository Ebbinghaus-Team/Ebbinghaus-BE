package com.ebbinghaus.ttopullae.studyroom.presentation.dto;

import com.ebbinghaus.ttopullae.problem.domain.ProblemType;
import com.ebbinghaus.ttopullae.problem.domain.ReviewGate;
import com.ebbinghaus.ttopullae.studyroom.application.dto.GroupRoomProblemListResult;
import java.time.LocalDateTime;
import java.util.List;

public record GroupRoomProblemListResponse(
    Long studyRoomId,
    String studyRoomName,
    List<ProblemSummary> problems,
    int totalCount
) {

    public record ProblemSummary(
        Long problemId,
        String question,
        ProblemType problemType,
        ReviewGate reviewGate,
        LocalDateTime createdAt,
        LocalDateTime lastReviewedAt,
        int reviewCount,
        boolean isMyProblem,
        String creatorName
    ) {
    }

    public static GroupRoomProblemListResponse from(GroupRoomProblemListResult result) {
        List<ProblemSummary> summaries = result.problems().stream()
                .map(info -> new ProblemSummary(
                        info.problemId(),
                        info.question(),
                        info.problemType(),
                        info.reviewGate(),
                        info.createdAt(),
                        info.lastReviewedAt(),
                        info.reviewCount(),
                        info.isMyProblem(),
                        info.creatorName()
                ))
                .toList();

        return new GroupRoomProblemListResponse(
                result.studyRoomId(),
                result.studyRoomName(),
                summaries,
                result.totalCount()
        );
    }
}