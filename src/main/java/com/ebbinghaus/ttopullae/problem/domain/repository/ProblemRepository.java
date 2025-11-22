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
     * 개인 공부방의 문제 목록을 조회합니다.
     * Fetch Join을 사용하여 N+1 문제를 방지합니다.
     *
     * @param studyRoomId 스터디룸 ID
     * @return 문제 목록
     */
    @Query("""
        SELECT DISTINCT p FROM Problem p
        LEFT JOIN FETCH p.creator
        WHERE p.studyRoom.studyRoomId = :studyRoomId
        ORDER BY p.createdAt DESC
        """)
    List<Problem> findByStudyRoomIdWithCreator(@Param("studyRoomId") Long studyRoomId);
}