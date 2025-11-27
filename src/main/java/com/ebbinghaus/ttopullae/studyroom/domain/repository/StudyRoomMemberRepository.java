package com.ebbinghaus.ttopullae.studyroom.domain.repository;

import com.ebbinghaus.ttopullae.studyroom.domain.StudyRoom;
import com.ebbinghaus.ttopullae.studyroom.domain.StudyRoomMember;
import com.ebbinghaus.ttopullae.user.domain.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyRoomMemberRepository extends JpaRepository<StudyRoomMember, Long> {

    boolean existsByUserAndStudyRoomAndActive(User user, StudyRoom studyRoom, Boolean active);

    List<StudyRoomMember> findAllByUserAndActive(User user, Boolean active);

    int countByStudyRoomAndActive(StudyRoom studyRoom, Boolean active);
}
