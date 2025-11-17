package com.ebbinghaus.ttopullae.studyroom.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyRoomRepository extends JpaRepository<StudyRoom, Long> {

    boolean existsByJoinCode(String joinCode);
}