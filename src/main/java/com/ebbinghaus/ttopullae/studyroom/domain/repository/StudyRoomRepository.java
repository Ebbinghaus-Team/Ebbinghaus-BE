package com.ebbinghaus.ttopullae.studyroom.domain.repository;

import com.ebbinghaus.ttopullae.studyroom.domain.RoomType;
import com.ebbinghaus.ttopullae.studyroom.domain.StudyRoom;
import com.ebbinghaus.ttopullae.user.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyRoomRepository extends JpaRepository<StudyRoom, Long> {

    boolean existsByJoinCode(String joinCode);

    Optional<StudyRoom> findByJoinCode(String joinCode);

    List<StudyRoom> findAllByOwnerAndRoomType(User owner, RoomType roomType);
}
