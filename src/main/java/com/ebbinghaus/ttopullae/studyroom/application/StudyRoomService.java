package com.ebbinghaus.ttopullae.studyroom.application;

import com.ebbinghaus.ttopullae.global.util.JoinCodeGenerator;
import com.ebbinghaus.ttopullae.studyroom.application.dto.StudyRoomCreateCommand;
import com.ebbinghaus.ttopullae.studyroom.domain.RoomType;
import com.ebbinghaus.ttopullae.studyroom.domain.StudyRoom;
import com.ebbinghaus.ttopullae.studyroom.domain.StudyRoomMember;
import com.ebbinghaus.ttopullae.studyroom.domain.repository.StudyRoomMemberRepository;
import com.ebbinghaus.ttopullae.studyroom.domain.repository.StudyRoomRepository;
import com.ebbinghaus.ttopullae.studyroom.exception.JoinCodeGenerationException;
import com.ebbinghaus.ttopullae.user.domain.User;
import com.ebbinghaus.ttopullae.user.domain.repository.UserRepository;
import com.ebbinghaus.ttopullae.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 공부방 생성 및 관리 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyRoomService {

    private final StudyRoomRepository studyRoomRepository;
    private final StudyRoomMemberRepository studyRoomMemberRepository;
    private final UserRepository userRepository;

    private static final int MAX_JOIN_CODE_GENERATION_ATTEMPTS = 10;

    /**
     * 개인 공부방 생성
     *
     * @param command 공부방 생성 명령
     * @return 생성된 공부방
     */
    @Transactional
    public StudyRoom createPersonalRoom(StudyRoomCreateCommand command) {
        // 사용자 조회
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));

        // 개인 공부방 엔티티 생성
        StudyRoom studyRoom = StudyRoom.builder()
                .owner(user)
                .roomType(RoomType.PERSONAL)
                .name(command.name())
                .description(command.description())
                .category(command.category())
                .joinCode(null)  // 개인방은 참여 코드 없음
                .build();

        return studyRoomRepository.save(studyRoom);
    }

    /**
     * 그룹 스터디 생성
     * 참여 코드를 생성하고, 생성자를 첫 번째 멤버로 등록
     *
     * @param command 공부방 생성 명령
     * @return 생성된 공부방
     */
    @Transactional
    public StudyRoom createGroupRoom(StudyRoomCreateCommand command) {
        // 사용자 조회
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));

        // 고유한 참여 코드 생성
        String joinCode = generateUniqueJoinCode();

        // 그룹 스터디 엔티티 생성
        StudyRoom studyRoom = StudyRoom.builder()
                .owner(user)
                .roomType(RoomType.GROUP)
                .name(command.name())
                .description(command.description())
                .category(command.category())
                .joinCode(joinCode)
                .build();

        StudyRoom savedStudyRoom = studyRoomRepository.save(studyRoom);

        // 생성자를 첫 번째 멤버로 등록
        StudyRoomMember member = StudyRoomMember.builder()
                .user(user)
                .studyRoom(savedStudyRoom)
                .active(true)
                .build();

        studyRoomMemberRepository.save(member);

        return savedStudyRoom;
    }

    /**
     * 고유한 참여 코드 생성
     * 중복되지 않을 때까지 최대 10회 시도
     *
     * @return 고유한 참여 코드
     * @throws JoinCodeGenerationException 최대 시도 횟수 초과 시
     */
    private String generateUniqueJoinCode() {
        for (int attempt = 0; attempt < MAX_JOIN_CODE_GENERATION_ATTEMPTS; attempt++) {
            String code = JoinCodeGenerator.generate();

            // 중복 체크
            if (!studyRoomRepository.existsByJoinCode(code)) {
                return code;
            }
        }

        throw new JoinCodeGenerationException();
    }
}
