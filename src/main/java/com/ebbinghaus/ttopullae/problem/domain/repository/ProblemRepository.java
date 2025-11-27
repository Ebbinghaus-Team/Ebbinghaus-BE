package com.ebbinghaus.ttopullae.problem.domain.repository;

import com.ebbinghaus.ttopullae.problem.domain.Problem;
import com.ebbinghaus.ttopullae.problem.domain.ReviewGate;
import com.ebbinghaus.ttopullae.problem.domain.repository.dto.ProblemWithMyReviewDto;
import com.ebbinghaus.ttopullae.studyroom.domain.StudyRoom;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

    int countByStudyRoom(StudyRoom studyRoom);

    /**
     * 개인 공부방의 문제 목록을 복습 상태와 함께 조회합니다.
     * Fetch Join을 사용하여 단일 쿼리로 문제 + 복습 상태를 조회합니다.
     * 동적 필터링을 통해 ALL/GATE_1/GATE_2/GRADUATED 필터를 지원합니다.
     *
     * @param studyRoomId 스터디룸 ID
     * @param userId 사용자 ID
     * @param targetGate 필터링할 관문 (null이면 전체 조회)
     * @return 문제 목록 (reviewStates 포함)
     */
    @Query("""
        SELECT DISTINCT p FROM Problem p
        LEFT JOIN FETCH p.reviewStates rs
        WHERE p.studyRoom.studyRoomId = :studyRoomId
          AND rs.user.userId = :userId
          AND (:targetGate IS NULL OR rs.gate = :targetGate)
        ORDER BY p.createdAt DESC
        """)
    List<Problem> findPersonalRoomProblemsWithReviewState(
        @Param("studyRoomId") Long studyRoomId,
        @Param("userId") Long userId,
        @Param("targetGate") ReviewGate targetGate
    );

    /**
     * 그룹 공부방의 문제 목록을 복습 상태와 생성자 정보와 함께 조회합니다.
     * DTO 프로젝션을 사용하여 문제와 현재 사용자의 복습 상태만 조회합니다.
     * Boolean 플래그 방식을 사용하여 동적 필터링을 지원합니다.
     *
     * 필터 조합:
     * - ALL: includeAll=true, includeNotInReview=false, targetGate=null
     * - NOT_IN_REVIEW: includeAll=false, includeNotInReview=true, targetGate=null
     * - GATE_1/GATE_2/GRADUATED: includeAll=false, includeNotInReview=false, targetGate=해당관문
     *
     * @param studyRoomId 스터디룸 ID
     * @param userId 사용자 ID
     * @param includeAll 모든 문제 포함 여부 (ALL 필터)
     * @param includeNotInReview ReviewState 없는 문제 포함 여부 (NOT_IN_REVIEW 필터)
     * @param targetGate 특정 관문 필터 (GATE_1/GATE_2/GRADUATED)
     * @return 문제와 내 복습 상태 DTO 목록
     */
    @Query("""
        SELECT new com.ebbinghaus.ttopullae.problem.domain.repository.dto.ProblemWithMyReviewDto(p, rs)
        FROM Problem p
        LEFT JOIN FETCH p.creator
        LEFT JOIN p.reviewStates rs ON rs.user.userId = :userId
        WHERE p.studyRoom.studyRoomId = :studyRoomId
          AND (
            :includeAll = true
            OR (:includeNotInReview = true AND rs.stateId IS NULL)
            OR (:targetGate IS NOT NULL AND rs.gate = :targetGate)
          )
        ORDER BY p.createdAt DESC
        """)
    List<ProblemWithMyReviewDto> findGroupRoomProblemsWithReviewStateAndCreator(
        @Param("studyRoomId") Long studyRoomId,
        @Param("userId") Long userId,
        @Param("includeAll") boolean includeAll,
        @Param("includeNotInReview") boolean includeNotInReview,
        @Param("targetGate") ReviewGate targetGate
    );
}
