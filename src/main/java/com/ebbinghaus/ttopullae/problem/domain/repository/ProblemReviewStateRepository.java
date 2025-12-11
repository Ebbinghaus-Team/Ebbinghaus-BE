package com.ebbinghaus.ttopullae.problem.domain.repository;

import com.ebbinghaus.ttopullae.problem.domain.Problem;
import com.ebbinghaus.ttopullae.problem.domain.ProblemReviewState;
import com.ebbinghaus.ttopullae.problem.domain.ReviewGate;
import com.ebbinghaus.ttopullae.problem.domain.repository.dto.TodayMailProjection;
import com.ebbinghaus.ttopullae.studyroom.domain.StudyRoom;
import com.ebbinghaus.ttopullae.user.domain.User;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProblemReviewStateRepository extends JpaRepository<ProblemReviewState, Long> {

    int countByUserAndProblem_StudyRoomAndGate(User user, StudyRoom studyRoom, ReviewGate gate);

    Optional<ProblemReviewState> findByUserAndProblem(User user, Problem problem);

    /**
     * 오늘의 복습 문제 스냅샷을 생성합니다. (벌크 업데이트)
     *
     * nextReviewDate가 오늘 이하인 문제들의 todayReviewIncludedDate를 오늘로 설정하고,
     * todayReviewIncludedGate를 현재 gate로 보존하여 필터 일관성을 유지합니다.
     *
     * 처리 조건:
     * - nextReviewDate <= today: 오늘이 복습날인 문제 (신규 + 이월)
     * - gate <> 'GRADUATED': 졸업하지 않은 문제만
     * - todayReviewIncludedDate <> today: 중복 스냅샷 방지
     *
     * @param today 오늘 날짜
     * @return 스냅샷된 문제 개수
     */
    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE ProblemReviewState prs
        SET prs.todayReviewIncludedDate = :today,
            prs.todayReviewIncludedGate = prs.gate
        WHERE prs.nextReviewDate <= :today
          AND prs.gate <> 'GRADUATED'
          AND (prs.todayReviewIncludedDate IS NULL
               OR prs.todayReviewIncludedDate <> :today)
        """)
    int snapshotTodayReviewProblems(@Param("today") LocalDate today);

    /**
     * 오늘의 복습 문제를 조회합니다.
     *
     * 조회 조건:
     * 1. todayReviewIncludedDate = today (자정 배치에서 스냅샷됨)
     * 2. targetGate 필터 (todayReviewIncludedGate 기준)
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
          AND prs.todayReviewIncludedDate = :today
          AND (:targetGate IS NULL OR prs.todayReviewIncludedGate = :targetGate)
        ORDER BY prs.nextReviewDate ASC
        """)
    List<ProblemReviewState> findTodaysReviewProblems(
        @Param("userId") Long userId,
        @Param("today") LocalDate today,
        @Param("targetGate") ReviewGate targetGate
    );


    @Query("""
            SELECT
                u.userId as userId,
                u.email as email,
                u.username as username,
                p.question as question
            FROM ProblemReviewState prs
                 JOIN prs.user u
                 JOIN prs.problem p
            WHERE prs.todayReviewIncludedDate = :today
                 AND u.receiveNotifications = true
            """)
    List<TodayMailProjection> findAllTodayReviewProblemMails(@Param("today")LocalDate today);

}
