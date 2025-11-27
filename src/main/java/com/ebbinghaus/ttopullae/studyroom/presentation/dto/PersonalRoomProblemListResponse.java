package com.ebbinghaus.ttopullae.studyroom.presentation.dto;

import com.ebbinghaus.ttopullae.problem.domain.ProblemType;
import com.ebbinghaus.ttopullae.problem.domain.ReviewGate;
import com.ebbinghaus.ttopullae.studyroom.application.dto.PersonalRoomProblemListResult;
import java.time.LocalDateTime;
import java.util.List;

public record PersonalRoomProblemListResponse(
    Long studyRoomId,
    String studyRoomName,
    String studyRoomCategory,
    String studyRoomDescription,
    DashboardDto dashboard,
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
        int reviewCount
    ) {
    }

    public static PersonalRoomProblemListResponse from(PersonalRoomProblemListResult result) {
        List<ProblemSummary> summaries = result.problems().stream()
                .map(info -> new ProblemSummary(
                        info.problemId(),
                        info.question(),
                        info.problemType(),
                        info.reviewGate(),
                        info.createdAt(),
                        info.lastReviewedAt(),
                        info.reviewCount()
                ))
                .toList();

        return new PersonalRoomProblemListResponse(
                result.studyRoomId(),
                result.studyRoomName(),
                result.studyRoomCategory(),
                result.studyRoomDescription(),
                DashboardDto.from(result),
                summaries,
                result.totalCount()
        );
    }

    public record DashboardDto(
            int totalCount,
            int completedCount,
            int incompletedCount,
            double progressRate
    ) {
        static DashboardDto from(PersonalRoomProblemListResult result) {
            return new DashboardDto(
                    result.dashboard().totalCount(),
                    result.dashboard().completedCount(),
                    result.dashboard().incompletedCount(),
                    result.dashboard().progressRate()
            );
        }
    }
}
