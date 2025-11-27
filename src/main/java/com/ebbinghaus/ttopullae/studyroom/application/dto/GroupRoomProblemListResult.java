package com.ebbinghaus.ttopullae.studyroom.application.dto;

import com.ebbinghaus.ttopullae.problem.domain.Problem;
import com.ebbinghaus.ttopullae.problem.domain.ProblemAttempt;
import com.ebbinghaus.ttopullae.problem.domain.ProblemReviewState;
import com.ebbinghaus.ttopullae.problem.domain.ProblemType;
import com.ebbinghaus.ttopullae.problem.domain.ReviewGate;
import com.ebbinghaus.ttopullae.problem.domain.repository.dto.ProblemWithMyReviewDto;
import com.ebbinghaus.ttopullae.studyroom.domain.StudyRoom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record GroupRoomProblemListResult(
    Long studyRoomId,
    String studyRoomName,
    List<ProblemInfo> problems,
    int totalCount
) {

    /**
     * ProblemWithMyReviewDto 목록을 GroupRoomProblemListResult로 변환합니다.
     *
     * @param studyRoom 스터디룸
     * @param problemDtos 문제+내 복습 상태 DTO 목록
     * @param attemptMap 최근 시도 기록 맵 (problemId -> ProblemAttempt)
     * @param userId 현재 사용자 ID
     */
    public static GroupRoomProblemListResult of(
            StudyRoom studyRoom,
            List<ProblemWithMyReviewDto> problemDtos,
            Map<Long, ProblemAttempt> attemptMap,
            Long userId
    ) {
        List<ProblemInfo> problemInfos = problemDtos.stream()
                .map(dto -> ProblemInfo.from(dto, attemptMap, userId))
                .toList();

        return new GroupRoomProblemListResult(
                studyRoom.getStudyRoomId(),
                studyRoom.getName(),
                problemInfos,
                problemInfos.size()
        );
    }

    /**
     * 그룹 공부방 문제 정보 DTO
     * 개인 공부방과의 차이점: isMyProblem, creatorName 필드 추가
     */
    public record ProblemInfo(
        Long problemId,
        String question,
        ProblemType problemType,
        ReviewGate reviewGate,         // ReviewState 없으면 NOT_IN_REVIEW
        LocalDateTime createdAt,
        LocalDateTime lastReviewedAt,  // 시도 기록 없으면 null
        int reviewCount,               // ReviewState 없으면 0
        boolean isMyProblem,           // 내가 생성한 문제인지 여부
        String creatorName             // 문제 생성자 이름
    ) {
        /**
         * ProblemWithMyReviewDto를 ProblemInfo DTO로 변환합니다.
         *
         * @param dto 문제와 내 복습 상태 DTO
         * @param attemptMap 최근 시도 기록 맵
         * @param userId 현재 사용자 ID
         * @return ProblemInfo (ReviewState 없어도 항상 반환)
         */
        public static ProblemInfo from(
                ProblemWithMyReviewDto dto,
                Map<Long, ProblemAttempt> attemptMap,
                Long userId
        ) {
            Problem problem = dto.problem();
            ProblemReviewState myReviewState = dto.myReviewState();

            // 최근 시도 기록 조회
            ProblemAttempt latestAttempt = attemptMap.get(problem.getProblemId());

            // 내가 생성한 문제인지 확인
            boolean isMyProblem = problem.getCreator().getUserId().equals(userId);

            return new ProblemInfo(
                    problem.getProblemId(),
                    problem.getQuestion(),
                    problem.getProblemType(),
                    myReviewState != null ? myReviewState.getGate() : ReviewGate.NOT_IN_REVIEW,
                    problem.getCreatedAt(),
                    latestAttempt != null ? latestAttempt.getCreatedAt() : null,
                    myReviewState != null ? myReviewState.getReviewCount() : 0,
                    isMyProblem,
                    problem.getCreator().getUsername()
            );
        }
    }
}