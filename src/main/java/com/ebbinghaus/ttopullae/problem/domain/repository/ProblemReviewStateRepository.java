package com.ebbinghaus.ttopullae.problem.domain.repository;

import com.ebbinghaus.ttopullae.problem.domain.ProblemReviewState;
import com.ebbinghaus.ttopullae.problem.domain.ReviewGate;
import com.ebbinghaus.ttopullae.studyroom.domain.StudyRoom;
import com.ebbinghaus.ttopullae.user.domain.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProblemReviewStateRepository extends JpaRepository<ProblemReviewState, Long> {

    int countByUserAndProblem_StudyRoomAndGate(User user, StudyRoom studyRoom, ReviewGate gate);

    /**
     * 특정 사용자의 여러 문제에 대한 복습 상태를 조회합니다.
     *
     * @param userId 사용자 ID
     * @param problemIds 문제 ID 목록
     * @return 복습 상태 목록
     */
    @Query("""
        SELECT prs FROM ProblemReviewState prs
        WHERE prs.user.userId = :userId
          AND prs.problem.problemId IN :problemIds
        """)
    List<ProblemReviewState> findByUserIdAndProblemIds(
        @Param("userId") Long userId,
        @Param("problemIds") List<Long> problemIds
    );
}