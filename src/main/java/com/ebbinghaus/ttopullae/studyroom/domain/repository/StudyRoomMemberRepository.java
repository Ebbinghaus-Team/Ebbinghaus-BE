package com.ebbinghaus.ttopullae.studyroom.domain.repository;

import com.ebbinghaus.ttopullae.studyroom.domain.StudyRoom;
import com.ebbinghaus.ttopullae.studyroom.domain.StudyRoomMember;
import com.ebbinghaus.ttopullae.user.domain.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudyRoomMemberRepository extends JpaRepository<StudyRoomMember, Long> {

    boolean existsByUserAndStudyRoomAndActive(User user, StudyRoom studyRoom, Boolean active);

    List<StudyRoomMember> findAllByUserAndActive(User user, Boolean active);

    int countByStudyRoomAndActive(StudyRoom studyRoom, Boolean active);

    @Query("SELECT m FROM StudyRoomMember m JOIN FETCH m.user WHERE m.studyRoom = :studyRoom AND m.active = :active ORDER BY m.createdAt ASC")
    List<StudyRoomMember> findAllByStudyRoomAndActiveWithUser(@Param("studyRoom") StudyRoom studyRoom, @Param("active") Boolean active);
}
