package com.ebbinghaus.ttopullae.problem.domain.repository;

import com.ebbinghaus.ttopullae.problem.domain.ProblemReviewState;
import com.ebbinghaus.ttopullae.problem.domain.ReviewGate;
import com.ebbinghaus.ttopullae.studyroom.domain.StudyRoom;
import com.ebbinghaus.ttopullae.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemReviewStateRepository extends JpaRepository<ProblemReviewState, Long> {

    int countByUserAndProblem_StudyRoomAndGate(User user, StudyRoom studyRoom, ReviewGate gate);
}