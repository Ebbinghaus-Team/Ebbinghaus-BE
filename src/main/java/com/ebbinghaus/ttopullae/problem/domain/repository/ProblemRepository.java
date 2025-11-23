package com.ebbinghaus.ttopullae.problem.domain.repository;

import com.ebbinghaus.ttopullae.problem.domain.Problem;
import com.ebbinghaus.ttopullae.problem.domain.ReviewGate;
import com.ebbinghaus.ttopullae.studyroom.domain.StudyRoom;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

    int countByStudyRoom(StudyRoom studyRoom);

    /**
     * 개인 공부방의 문제 목록을 복습 상태와 함께 조회합니다.
     * Fetch Join을 사용하여 단일 쿼리로 문제 + 생성자 + 복습 상태를 조회합니다.
     * 동적 필터링을 통해 ALL/GATE_1/GATE_2/GRADUATED 필터를 지원합니다.
     *
     * @param studyRoomId 스터디룸 ID
     * @param userId 사용자 ID
     * @param targetGate 필터링할 관문 (null이면 전체 조회)
     * @return 문제 목록 (reviewStates 포함)
     */
    @Query("""
        SELECT DISTINCT p FROM Problem p
        LEFT JOIN FETCH p.creator
        LEFT JOIN p.reviewStates rs
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
}