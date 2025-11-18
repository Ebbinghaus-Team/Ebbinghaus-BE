package com.ebbinghaus.ttopullae.studyroom.application;

import com.ebbinghaus.ttopullae.global.exception.ApplicationException;
import com.ebbinghaus.ttopullae.global.util.JoinCodeGenerator;
import com.ebbinghaus.ttopullae.studyroom.application.dto.StudyRoomCreateCommand;
import com.ebbinghaus.ttopullae.studyroom.application.dto.StudyRoomCreateResult;
import com.ebbinghaus.ttopullae.studyroom.domain.RoomType;
import com.ebbinghaus.ttopullae.studyroom.domain.StudyRoom;
import com.ebbinghaus.ttopullae.studyroom.domain.StudyRoomMember;
import com.ebbinghaus.ttopullae.studyroom.domain.repository.StudyRoomMemberRepository;
import com.ebbinghaus.ttopullae.studyroom.domain.repository.StudyRoomRepository;
import com.ebbinghaus.ttopullae.studyroom.exception.StudyRoomException;
import com.ebbinghaus.ttopullae.user.domain.User;
import com.ebbinghaus.ttopullae.user.domain.repository.UserRepository;
import com.ebbinghaus.ttopullae.user.exception.UserException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudyRoomService {

    private static final int MAX_JOIN_CODE_ATTEMPTS = 10;

    private final StudyRoomRepository studyRoomRepository;
    private final StudyRoomMemberRepository studyRoomMemberRepository;
    private final UserRepository userRepository;

    /**
     * 개인 공부방을 생성합니다.
     */
    @Transactional
    public StudyRoomCreateResult createPersonalRoom(StudyRoomCreateCommand command) {
        User owner = findUserById(command.userId());

        StudyRoom personalRoom = StudyRoom.builder()
                .owner(owner)
                .roomType(RoomType.PERSONAL)
                .name(command.name())
                .description(command.description())
                .category(command.category())
                .joinCode(null) // 개인방은 참여 코드가 없음
                .build();

        StudyRoom savedRoom = studyRoomRepository.save(personalRoom);

        return StudyRoomCreateResult.from(savedRoom);
    }

    /**
     * 그룹 스터디를 생성합니다.
     * 생성 시 고유한 참여 코드를 자동으로 발급합니다.
     */
    @Transactional
    public StudyRoomCreateResult createGroupRoom(StudyRoomCreateCommand command) {
        User owner = findUserById(command.userId());

        String uniqueJoinCode = generateUniqueJoinCode();

        StudyRoom groupRoom = StudyRoom.builder()
                .owner(owner)
                .roomType(RoomType.GROUP)
                .name(command.name())
                .description(command.description())
                .category(command.category())
                .joinCode(uniqueJoinCode)
                .build();

        StudyRoom savedRoom = studyRoomRepository.save(groupRoom);

        // 방장을 그룹 멤버로 자동 등록
        StudyRoomMember ownerMembership = StudyRoomMember.builder()
                .user(owner)
                .studyRoom(savedRoom)
                .active(true)
                .build();

        studyRoomMemberRepository.save(ownerMembership);

        return StudyRoomCreateResult.from(savedRoom);
    }

    /**
     * 고유한 참여 코드를 생성합니다.
     * 최대 10회까지 재시도하며, 모두 실패 시 예외를 발생시킵니다.
     */
    private String generateUniqueJoinCode() {
        for (int attempt = 0; attempt < MAX_JOIN_CODE_ATTEMPTS; attempt++) {
            String joinCode = JoinCodeGenerator.generateCode();
            if (!studyRoomRepository.existsByJoinCode(joinCode)) {
                return joinCode;
            }
        }
        throw new ApplicationException(StudyRoomException.JOIN_CODE_GENERATION_FAILED);
    }

    /**
     * 사용자 ID로 User 엔티티를 조회합니다.
     */
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(UserException.USER_NOT_FOUND));
    }
}
