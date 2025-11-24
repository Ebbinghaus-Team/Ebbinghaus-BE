package com.ebbinghaus.ttopullae.problem.application.dto;

import com.ebbinghaus.ttopullae.problem.domain.AttemptStatus;
import com.ebbinghaus.ttopullae.problem.domain.Problem;
import com.ebbinghaus.ttopullae.problem.domain.ProblemAttempt;
import com.ebbinghaus.ttopullae.problem.domain.ProblemReviewState;
import com.ebbinghaus.ttopullae.problem.domain.ProblemType;
import com.ebbinghaus.ttopullae.problem.domain.ReviewGate;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record TodayReviewResult(
    DashboardInfo dashboard,
    List<TodayReviewProblemInfo> problems
) {
    public static TodayReviewResult of(List<ProblemReviewState> reviewStates, LocalDate today, List<ProblemAttempt> todaysAttempts) {
        // 오늘의 풀이 기록을 문제 ID로 매핑
        Map<Long, ProblemAttempt> attemptMap = todaysAttempts.stream()
            .collect(Collectors.toMap(
                pa -> pa.getProblem().getProblemId(),
                pa -> pa
            ));

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
            .map(rs -> {
                Long problemId = rs.getProblem().getProblemId();
                AttemptStatus attemptStatus = calculateAttemptStatus(attemptMap, problemId);
                return TodayReviewProblemInfo.from(rs, attemptStatus);
            })
            .toList();

        return new TodayReviewResult(dashboard, problems);
    }

    private static AttemptStatus calculateAttemptStatus(Map<Long, ProblemAttempt> attemptMap, Long problemId) {
        ProblemAttempt attempt = attemptMap.get(problemId);
        if (attempt == null) {
            return AttemptStatus.NOT_ATTEMPTED;
        }
        return attempt.getIsCorrect() ? AttemptStatus.CORRECT : AttemptStatus.INCORRECT;
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
        LocalDate nextReviewDate,
        AttemptStatus attemptStatus
    ) {
        public static TodayReviewProblemInfo from(ProblemReviewState reviewState, AttemptStatus attemptStatus) {
            Problem problem = reviewState.getProblem();
            return new TodayReviewProblemInfo(
                problem.getProblemId(),
                problem.getQuestion(),
                problem.getProblemType(),
                reviewState.getTodayReviewIncludedGate(),
                reviewState.getNextReviewDate(),
                attemptStatus
            );
        }
    }
}