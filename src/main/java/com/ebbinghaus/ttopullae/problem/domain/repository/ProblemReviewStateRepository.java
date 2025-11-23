package com.ebbinghaus.ttopullae.problem.domain.repository;

import com.ebbinghaus.ttopullae.problem.domain.ProblemReviewState;
import com.ebbinghaus.ttopullae.problem.domain.ReviewGate;
import com.ebbinghaus.ttopullae.studyroom.domain.StudyRoom;
import com.ebbinghaus.ttopullae.user.domain.User;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProblemReviewStateRepository extends JpaRepository<ProblemReviewState, Long> {

    int countByUserAndProblem_StudyRoomAndGate(User user, StudyRoom studyRoom, ReviewGate gate);

    /**
     * 오늘의 복습 문제를 조회합니다.
     *
     * 조회 조건:
     * 1. 졸업하지 않았거나 오늘 졸업한 문제
     * 2. nextReviewDate <= today (아직 풀지 않은 문제) OR todayReviewIncludedDate = today (오늘 이미 푼 문제)
     * 3. targetGate 필터 (현재 gate 또는 원래 gate)
     *
     * @param userId 사용자 ID
     * @param today 오늘 날짜
     * @param targetGate 필터할 관문 (null이면 전체 조회)
     * @return 오늘의 복습 문제 목록
     */
    @Query("""
        SELECT DISTINCT prs FROM ProblemReviewState prs
        LEFT JOIN FETCH prs.problem p
        WHERE prs.user.userId = :userId
          AND (prs.gate <> 'GRADUATED'
               OR (prs.gate = 'GRADUATED' AND prs.todayReviewIncludedDate = :today))
          AND (prs.nextReviewDate <= :today OR prs.todayReviewIncludedDate = :today)
          AND (:targetGate IS NULL
               OR prs.gate = :targetGate
               OR (prs.todayReviewIncludedDate = :today AND prs.todayReviewIncludedGate = :targetGate))
        ORDER BY prs.nextReviewDate ASC
        """)
    List<ProblemReviewState> findTodaysReviewProblems(
        @Param("userId") Long userId,
        @Param("today") LocalDate today,
        @Param("targetGate") ReviewGate targetGate
    );

}
