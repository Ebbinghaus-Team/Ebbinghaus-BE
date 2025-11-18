package com.ebbinghaus.ttopullae.studyroom.domain.repository;

import com.ebbinghaus.ttopullae.studyroom.domain.StudyRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyRoomRepository extends JpaRepository<StudyRoom, Long> {

    boolean existsByJoinCode(String joinCode);
}
