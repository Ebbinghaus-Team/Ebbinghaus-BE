package com.ebbinghaus.ttopullae.problem.application.dto;

import com.ebbinghaus.ttopullae.problem.domain.Problem;
import com.ebbinghaus.ttopullae.problem.domain.ProblemReviewState;
import com.ebbinghaus.ttopullae.problem.domain.ProblemType;
import com.ebbinghaus.ttopullae.problem.domain.ReviewGate;

import java.time.LocalDate;
import java.util.List;

public record TodayReviewResult(
    DashboardInfo dashboard,
    List<TodayReviewProblemInfo> problems
) {
    public static TodayReviewResult of(List<ProblemReviewState> reviewStates, LocalDate today) {
        // 완료 여부 판단: todayReviewFirstAttemptDate = today
        long completedCount = reviewStates.stream()
            .filter(rs -> rs.getTodayReviewFirstAttemptDate() != null
                       && rs.getTodayReviewFirstAttemptDate().equals(today))
            .count();

        int totalCount = reviewStates.size();
        int incompletedCount = totalCount - (int) completedCount;
        double progressRate = totalCount > 0
            ? Math.round((double) completedCount / totalCount * 1000) / 10.0
            : 0.0;

        DashboardInfo dashboard = new DashboardInfo(
            totalCount,
            (int) completedCount,
            incompletedCount,
            progressRate
        );

        List<TodayReviewProblemInfo> problems = reviewStates.stream()
            .map(TodayReviewProblemInfo::from)
            .toList();

        return new TodayReviewResult(dashboard, problems);
    }

    public record DashboardInfo(
        int totalCount,
        int completedCount,
        int incompletedCount,
        double progressRate
    ) {
    }

    public record TodayReviewProblemInfo(
        Long problemId,
        String question,
        ProblemType problemType,
        ReviewGate gate,
        LocalDate nextReviewDate
    ) {
        public static TodayReviewProblemInfo from(ProblemReviewState reviewState) {
            Problem problem = reviewState.getProblem();
            return new TodayReviewProblemInfo(
                problem.getProblemId(),
                problem.getQuestion(),
                problem.getProblemType(),
                reviewState.getTodayReviewIncludedGate(),
                reviewState.getNextReviewDate()
            );
        }
    }
}