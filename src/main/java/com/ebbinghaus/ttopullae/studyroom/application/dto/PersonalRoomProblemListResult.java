package com.ebbinghaus.ttopullae.studyroom.application.dto;

import com.ebbinghaus.ttopullae.problem.domain.Problem;
import com.ebbinghaus.ttopullae.problem.domain.ProblemAttempt;
import com.ebbinghaus.ttopullae.problem.domain.ProblemReviewState;
import com.ebbinghaus.ttopullae.problem.domain.ProblemType;
import com.ebbinghaus.ttopullae.problem.domain.ReviewGate;
import com.ebbinghaus.ttopullae.studyroom.domain.StudyRoom;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record PersonalRoomProblemListResult(
        DashboardInfo dashboard,
        Long studyRoomId,
        String studyRoomName,
        String studyRoomCategory,
        String studyRoomDescription,
        List<ProblemInfo> problems,
        int totalCount
) {

    /**
     * Problem 엔티티 목록을 PersonalRoomProblemListResult로 변환합니다.
     *
     * @param studyRoom  스터디룸
     * @param problems   문제 목록 (reviewStates fetch join 완료)
     * @param attemptMap 최근 시도 기록 맵 (problemId -> ProblemAttempt)
     * @param userId     사용자 ID
     */
    public static PersonalRoomProblemListResult of(
            StudyRoom studyRoom,
            List<Problem> problems,
            Map<Long, ProblemAttempt> attemptMap,
            Long userId
    ) {
        List<ProblemInfo> problemInfos = problems.stream()
                .map(problem -> ProblemInfo.from(problem, attemptMap, userId))
                .filter(Objects::nonNull)
                .toList();

        DashboardInfo dashboardInfo = DashboardInfo.from(problemInfos);

        return new PersonalRoomProblemListResult(
                dashboardInfo,
                studyRoom.getStudyRoomId(),
                studyRoom.getName(),
                studyRoom.getCategory(),
                studyRoom.getDescription(),
                problemInfos,
                problemInfos.size()
        );
    }

    public record ProblemInfo(
            Long problemId,
            String question,
            ProblemType problemType,
            ReviewGate reviewGate,
            LocalDateTime createdAt,
            LocalDateTime lastReviewedAt,
            int reviewCount
    ) {
        /**
         * Problem 엔티티를 ProblemInfo DTO로 변환합니다.
         *
         * @param problem    문제 엔티티 (reviewStates 포함)
         * @param attemptMap 최근 시도 기록 맵
         * @param userId     사용자 ID
         * @return ProblemInfo 또는 null (reviewState 없을 시)
         */
        public static ProblemInfo from(
                Problem problem,
                Map<Long, ProblemAttempt> attemptMap,
                Long userId
        ) {
            // reviewStates에서 현재 사용자의 복습 상태 찾기
            ProblemReviewState userReviewState = problem.getReviewStates().stream()
                    .filter(rs -> rs.getUser().getUserId().equals(userId))
                    .findFirst()
                    .orElse(null);

            if (userReviewState == null) {
                return null;
            }

            ProblemAttempt latestAttempt = attemptMap.get(problem.getProblemId());

            return new ProblemInfo(
                    problem.getProblemId(),
                    problem.getQuestion(),
                    problem.getProblemType(),
                    userReviewState.getGate(),
                    problem.getCreatedAt(),
                    latestAttempt != null ? latestAttempt.getCreatedAt() : null,
                    userReviewState.getReviewCount()
            );
        }

        public boolean isCompleted() {
            return this.reviewGate == ReviewGate.GRADUATED;
        }
    }


    public record DashboardInfo(
            int totalCount,
            int completedCount,
            int incompletedCount,
            double progressRate
    ) {
        public static DashboardInfo from(List<ProblemInfo> problemInfos) {
            int totalCount = problemInfos.size();

            int completedCount = (int) problemInfos.stream()
                    .filter(ProblemInfo::isCompleted)
                    .count();

            int incompletedCount = totalCount - completedCount;

            double progressRate = totalCount == 0 ? 0.0 :
                    Math.round(((double) completedCount / totalCount) * 1000.0) / 10.0;

            return new DashboardInfo(totalCount, completedCount, incompletedCount, progressRate);
        }
    }
}
