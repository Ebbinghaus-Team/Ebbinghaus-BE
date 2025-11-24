package com.ebbinghaus.ttopullae.problem.domain.repository;

import com.ebbinghaus.ttopullae.problem.domain.ProblemAttempt;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProblemAttemptRepository extends JpaRepository<ProblemAttempt, Long> {

    /**
     * 여러 문제에 대한 사용자의 가장 최근 풀이 기록을 조회합니다.
     * Batch 조회로 N+1 문제를 방지합니다.
     *
     * @param userId 사용자 ID
     * @param problemIds 문제 ID 목록
     * @return 각 문제의 최근 풀이 기록 목록
     */
    @Query("""
        SELECT pa FROM ProblemAttempt pa
        WHERE pa.user.userId = :userId
          AND pa.problem.problemId IN :problemIds
          AND pa.createdAt = (
              SELECT MAX(pa2.createdAt)
              FROM ProblemAttempt pa2
              WHERE pa2.user.userId = :userId
                AND pa2.problem.problemId = pa.problem.problemId
          )
        """)
    List<ProblemAttempt> findLatestAttemptsByUserAndProblems(
        @Param("userId") Long userId,
        @Param("problemIds") List<Long> problemIds
    );

    /**
     * 오늘 날짜에 풀이한 문제들의 첫 번째 시도를 조회합니다.
     * Batch 조회로 N+1 문제를 방지합니다.
     *
     * @param userId 사용자 ID
     * @param problemIds 문제 ID 목록
     * @param todayStart 오늘 시작 시간 (00:00:00)
     * @param tomorrowStart 내일 시작 시간 (00:00:00)
     * @return 각 문제의 오늘 첫 번째 풀이 기록 목록
     */
    @Query("""
        SELECT pa FROM ProblemAttempt pa
        WHERE pa.user.userId = :userId
          AND pa.problem.problemId IN :problemIds
          AND pa.createdAt >= :todayStart
          AND pa.createdAt < :tomorrowStart
          AND pa.createdAt = (
              SELECT MIN(pa2.createdAt)
              FROM ProblemAttempt pa2
              WHERE pa2.user.userId = :userId
                AND pa2.problem.problemId = pa.problem.problemId
                AND pa2.createdAt >= :todayStart
                AND pa2.createdAt < :tomorrowStart
          )
        """)
    List<ProblemAttempt> findTodaysFirstAttemptsByUserAndProblems(
        @Param("userId") Long userId,
        @Param("problemIds") List<Long> problemIds,
        @Param("todayStart") LocalDateTime todayStart,
        @Param("tomorrowStart") LocalDateTime tomorrowStart
    );
}