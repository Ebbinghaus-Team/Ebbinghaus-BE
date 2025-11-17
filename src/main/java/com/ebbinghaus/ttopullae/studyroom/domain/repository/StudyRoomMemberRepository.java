package com.ebbinghaus.ttopullae.studyroom.domain.repository;

import com.ebbinghaus.ttopullae.studyroom.domain.StudyRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyRoomMemberRepository extends JpaRepository<StudyRoomMember, Long> {
}
