package com.ebbinghaus.ttopullae.problem.presentation.dto;

import com.ebbinghaus.ttopullae.problem.application.dto.TodayReviewResult;

import java.util.List;

public record TodayReviewResponse(
    DashboardDto dashboard,
    List<TodayReviewProblemDto> problems
) {
    public static TodayReviewResponse from(TodayReviewResult result) {
        DashboardDto dashboard = new DashboardDto(
            result.dashboard().totalCount(),
            result.dashboard().completedCount(),
            result.dashboard().incompletedCount(),
            result.dashboard().progressRate()
        );

        List<TodayReviewProblemDto> problems = result.problems().stream()
            .map(TodayReviewProblemDto::from)
            .toList();

        return new TodayReviewResponse(dashboard, problems);
    }

    public record DashboardDto(
        int totalCount,
        int completedCount,
        int incompletedCount,
        double progressRate
    ) {
    }

    public record TodayReviewProblemDto(
        Long problemId,
        String question,
        String problemType,
        String gate,
        String nextReviewDate
    ) {
        public static TodayReviewProblemDto from(TodayReviewResult.TodayReviewProblemInfo info) {
            return new TodayReviewProblemDto(
                info.problemId(),
                info.question(),
                info.problemType().name(),
                info.gate().name(),
                info.nextReviewDate() != null ? info.nextReviewDate().toString() : null
            );
        }
    }
}