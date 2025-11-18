package com.ebbinghaus.ttopullae.studyroom.application;

import com.ebbinghaus.ttopullae.global.exception.ApplicationException;
import com.ebbinghaus.ttopullae.global.util.JoinCodeGenerator;
import com.ebbinghaus.ttopullae.problem.domain.ReviewGate;
import com.ebbinghaus.ttopullae.problem.domain.repository.ProblemRepository;
import com.ebbinghaus.ttopullae.problem.domain.repository.ProblemReviewStateRepository;
import com.ebbinghaus.ttopullae.studyroom.application.dto.GroupRoomJoinCommand;
import com.ebbinghaus.ttopullae.studyroom.application.dto.GroupRoomJoinResult;
import com.ebbinghaus.ttopullae.studyroom.application.dto.GroupRoomListResult;
import com.ebbinghaus.ttopullae.studyroom.application.dto.PersonalRoomListResult;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudyRoomServiceTest {

    @InjectMocks
    private StudyRoomService studyRoomService;

    @Mock
    private StudyRoomRepository studyRoomRepository;

    @Mock
    private StudyRoomMemberRepository studyRoomMemberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private ProblemReviewStateRepository problemReviewStateRepository;

    @Test
    @DisplayName("개인 공부방 생성 성공")
    void createPersonalRoom_Success() {
        // given
        Long userId = 1L;
        User mockUser = User.builder()
                .userId(userId)
                .email("test@example.com")
                .username("테스트유저")
                .receiveNotifications(true)
                .build();

        StudyRoomCreateCommand command = new StudyRoomCreateCommand(
                userId,
                "자바 스터디",
                "자바 개념 정리",
                "프로그래밍",
                RoomType.PERSONAL
        );

        StudyRoom savedRoom = StudyRoom.builder()
                .studyRoomId(1L)
                .owner(mockUser)
                .roomType(RoomType.PERSONAL)
                .name("자바 스터디")
                .description("자바 개념 정리")
                .category("프로그래밍")
                .joinCode(null)
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(studyRoomRepository.save(any(StudyRoom.class))).willReturn(savedRoom);

        // when
        StudyRoomCreateResult result = studyRoomService.createPersonalRoom(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.studyRoomId()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("자바 스터디");
        assertThat(result.description()).isEqualTo("자바 개념 정리");
        assertThat(result.category()).isEqualTo("프로그래밍");
        assertThat(result.joinCode()).isNull(); // 개인방은 참여 코드가 없음

        verify(userRepository, times(1)).findById(userId);
        verify(studyRoomRepository, times(1)).save(any(StudyRoom.class));
        verify(studyRoomMemberRepository, never()).save(any(StudyRoomMember.class)); // 개인방은 멤버 등록 안 함
    }

    @Test
    @DisplayName("개인 공부방 생성 실패 - 사용자를 찾을 수 없음")
    void createPersonalRoom_Fail_UserNotFound() {
        // given
        Long nonExistentUserId = 999L;
        StudyRoomCreateCommand command = new StudyRoomCreateCommand(
                nonExistentUserId,
                "자바 스터디",
                "자바 개념 정리",
                "프로그래밍",
                RoomType.PERSONAL
        );

        given(userRepository.findById(nonExistentUserId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> studyRoomService.createPersonalRoom(command))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", UserException.USER_NOT_FOUND);

        verify(userRepository, times(1)).findById(nonExistentUserId);
        verify(studyRoomRepository, never()).save(any(StudyRoom.class));
    }

    @Test
    @DisplayName("그룹 스터디 생성 성공")
    void createGroupRoom_Success() {
        // given
        Long userId = 1L;
        User mockUser = User.builder()
                .userId(userId)
                .email("test@example.com")
                .username("테스트유저")
                .receiveNotifications(true)
                .build();

        StudyRoomCreateCommand command = new StudyRoomCreateCommand(
                userId,
                "알고리즘 스터디",
                "매일 알고리즘 풀이",
                "알고리즘",
                RoomType.GROUP
        );

        String generatedJoinCode = "ABC12345";
        StudyRoom savedRoom = StudyRoom.builder()
                .studyRoomId(1L)
                .owner(mockUser)
                .roomType(RoomType.GROUP)
                .name("알고리즘 스터디")
                .description("매일 알고리즘 풀이")
                .category("알고리즘")
                .joinCode(generatedJoinCode)
                .build();

        StudyRoomMember savedMember = StudyRoomMember.builder()
                .user(mockUser)
                .studyRoom(savedRoom)
                .active(true)
                .build();

        try (MockedStatic<JoinCodeGenerator> mockedGenerator = mockStatic(JoinCodeGenerator.class)) {
            mockedGenerator.when(JoinCodeGenerator::generateCode).thenReturn(generatedJoinCode);
            given(studyRoomRepository.existsByJoinCode(generatedJoinCode)).willReturn(false);
            given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
            given(studyRoomRepository.save(any(StudyRoom.class))).willReturn(savedRoom);
            given(studyRoomMemberRepository.save(any(StudyRoomMember.class))).willReturn(savedMember);

            // when
            StudyRoomCreateResult result = studyRoomService.createGroupRoom(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.studyRoomId()).isEqualTo(1L);
            assertThat(result.name()).isEqualTo("알고리즘 스터디");
            assertThat(result.description()).isEqualTo("매일 알고리즘 풀이");
            assertThat(result.category()).isEqualTo("알고리즘");
            assertThat(result.joinCode()).isEqualTo(generatedJoinCode); // 그룹방은 참여 코드 필수

            verify(userRepository, times(1)).findById(userId);
            verify(studyRoomRepository, times(1)).save(any(StudyRoom.class));
            verify(studyRoomMemberRepository, times(1)).save(any(StudyRoomMember.class)); // 방장 자동 등록
        }
    }

    @Test
    @DisplayName("그룹 스터디 생성 성공 - 방장이 그룹 멤버로 자동 등록됨")
    void createGroupRoom_Success_OwnerAddedAsMember() {
        // given
        Long userId = 1L;
        User mockUser = User.builder()
                .userId(userId)
                .email("test@example.com")
                .username("테스트유저")
                .receiveNotifications(true)
                .build();

        StudyRoomCreateCommand command = new StudyRoomCreateCommand(
                userId,
                "스프링 스터디",
                "스프링 심화 학습",
                "프레임워크",
                RoomType.GROUP
        );

        String generatedJoinCode = "SPRING01";
        StudyRoom savedRoom = StudyRoom.builder()
                .studyRoomId(2L)
                .owner(mockUser)
                .roomType(RoomType.GROUP)
                .name("스프링 스터디")
                .description("스프링 심화 학습")
                .category("프레임워크")
                .joinCode(generatedJoinCode)
                .build();

        try (MockedStatic<JoinCodeGenerator> mockedGenerator = mockStatic(JoinCodeGenerator.class)) {
            mockedGenerator.when(JoinCodeGenerator::generateCode).thenReturn(generatedJoinCode);
            given(studyRoomRepository.existsByJoinCode(generatedJoinCode)).willReturn(false);
            given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
            given(studyRoomRepository.save(any(StudyRoom.class))).willReturn(savedRoom);

            // when
            studyRoomService.createGroupRoom(command);

            // then
            verify(studyRoomMemberRepository, times(1)).save(argThat(member ->
                    member.getUser().getUserId().equals(userId) &&
                            member.getStudyRoom().getStudyRoomId().equals(2L) &&
                            member.getActive()
            ));
        }
    }

    @Test
    @DisplayName("그룹 스터디 생성 실패 - 사용자를 찾을 수 없음")
    void createGroupRoom_Fail_UserNotFound() {
        // given
        Long nonExistentUserId = 999L;
        StudyRoomCreateCommand command = new StudyRoomCreateCommand(
                nonExistentUserId,
                "알고리즘 스터디",
                "매일 알고리즘 풀이",
                "알고리즘",
                RoomType.GROUP
        );

        given(userRepository.findById(nonExistentUserId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> studyRoomService.createGroupRoom(command))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", UserException.USER_NOT_FOUND);

        verify(userRepository, times(1)).findById(nonExistentUserId);
        verify(studyRoomRepository, never()).save(any(StudyRoom.class));
        verify(studyRoomMemberRepository, never()).save(any(StudyRoomMember.class));
    }

    @Test
    @DisplayName("그룹 스터디 생성 실패 - 참여 코드 생성 실패 (10회 재시도 후 실패)")
    void createGroupRoom_Fail_JoinCodeGenerationFailed() {
        // given
        Long userId = 1L;
        User mockUser = User.builder()
                .userId(userId)
                .email("test@example.com")
                .username("테스트유저")
                .receiveNotifications(true)
                .build();

        StudyRoomCreateCommand command = new StudyRoomCreateCommand(
                userId,
                "알고리즘 스터디",
                "매일 알고리즘 풀이",
                "알고리즘",
                RoomType.GROUP
        );

        try (MockedStatic<JoinCodeGenerator> mockedGenerator = mockStatic(JoinCodeGenerator.class)) {
            // 모든 생성된 코드가 이미 존재한다고 가정 (10회 재시도 모두 실패)
            mockedGenerator.when(JoinCodeGenerator::generateCode).thenReturn("DUPLICATE1");
            given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
            given(studyRoomRepository.existsByJoinCode(anyString())).willReturn(true); // 항상 중복

            // when & then
            assertThatThrownBy(() -> studyRoomService.createGroupRoom(command))
                    .isInstanceOf(ApplicationException.class)
                    .hasFieldOrPropertyWithValue("code", StudyRoomException.JOIN_CODE_GENERATION_FAILED);

            verify(userRepository, times(1)).findById(userId);
            verify(studyRoomRepository, times(10)).existsByJoinCode(anyString()); // 10회 재시도
            verify(studyRoomRepository, never()).save(any(StudyRoom.class));
            verify(studyRoomMemberRepository, never()).save(any(StudyRoomMember.class));
        }
    }

    @Test
    @DisplayName("그룹 스터디 생성 성공 - 참여 코드 중복 후 재생성 성공")
    void createGroupRoom_Success_JoinCodeRetrySuccess() {
        // given
        Long userId = 1L;
        User mockUser = User.builder()
                .userId(userId)
                .email("test@example.com")
                .username("테스트유저")
                .receiveNotifications(true)
                .build();

        StudyRoomCreateCommand command = new StudyRoomCreateCommand(
                userId,
                "데이터베이스 스터디",
                "DB 최적화 학습",
                "데이터베이스",
                RoomType.GROUP
        );

        String duplicateCode = "DUPLICATE";
        String uniqueCode = "UNIQUE123";

        StudyRoom savedRoom = StudyRoom.builder()
                .studyRoomId(3L)
                .owner(mockUser)
                .roomType(RoomType.GROUP)
                .name("데이터베이스 스터디")
                .description("DB 최적화 학습")
                .category("데이터베이스")
                .joinCode(uniqueCode)
                .build();

        try (MockedStatic<JoinCodeGenerator> mockedGenerator = mockStatic(JoinCodeGenerator.class)) {
            // 첫 번째 시도는 중복, 두 번째 시도는 성공
            mockedGenerator.when(JoinCodeGenerator::generateCode)
                    .thenReturn(duplicateCode)
                    .thenReturn(uniqueCode);

            given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
            given(studyRoomRepository.existsByJoinCode(duplicateCode)).willReturn(true); // 중복
            given(studyRoomRepository.existsByJoinCode(uniqueCode)).willReturn(false); // 고유
            given(studyRoomRepository.save(any(StudyRoom.class))).willReturn(savedRoom);

            // when
            StudyRoomCreateResult result = studyRoomService.createGroupRoom(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.joinCode()).isEqualTo(uniqueCode);

            verify(studyRoomRepository, times(2)).existsByJoinCode(anyString()); // 2회 시도
            verify(studyRoomRepository, times(1)).save(any(StudyRoom.class));
        }
    }

    // ===== 그룹 스터디 참여 API 테스트 =====

    @Test
    @DisplayName("그룹 스터디 참여 성공")
    void joinGroupRoom_Success() {
        // given
        Long userId = 2L;
        String joinCode = "ABC12345";

        User mockUser = User.builder()
                .userId(userId)
                .email("member@example.com")
                .username("멤버유저")
                .receiveNotifications(true)
                .build();

        User ownerUser = User.builder()
                .userId(1L)
                .email("owner@example.com")
                .username("방장유저")
                .receiveNotifications(true)
                .build();

        StudyRoom mockGroupRoom = StudyRoom.builder()
                .studyRoomId(10L)
                .owner(ownerUser)
                .roomType(RoomType.GROUP)
                .name("알고리즘 스터디")
                .description("매일 알고리즘 풀이")
                .category("알고리즘")
                .joinCode(joinCode)
                .build();

        StudyRoomMember savedMember = StudyRoomMember.builder()
                .memberId(1L)
                .user(mockUser)
                .studyRoom(mockGroupRoom)
                .active(true)
                .build();

        GroupRoomJoinCommand command = new GroupRoomJoinCommand(userId, joinCode);

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(studyRoomRepository.findByJoinCode(joinCode)).willReturn(Optional.of(mockGroupRoom));
        given(studyRoomMemberRepository.existsByUserAndStudyRoomAndActive(mockUser, mockGroupRoom, true))
                .willReturn(false);
        given(studyRoomMemberRepository.save(any(StudyRoomMember.class))).willReturn(savedMember);

        // when
        GroupRoomJoinResult result = studyRoomService.joinGroupRoom(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.studyRoomId()).isEqualTo(10L);
        assertThat(result.name()).isEqualTo("알고리즘 스터디");
        assertThat(result.category()).isEqualTo("알고리즘");
        assertThat(result.description()).isEqualTo("매일 알고리즘 풀이");

        verify(userRepository, times(1)).findById(userId);
        verify(studyRoomRepository, times(1)).findByJoinCode(joinCode);
        verify(studyRoomMemberRepository, times(1)).existsByUserAndStudyRoomAndActive(mockUser, mockGroupRoom, true);
        verify(studyRoomMemberRepository, times(1)).save(any(StudyRoomMember.class));
    }

    @Test
    @DisplayName("그룹 스터디 참여 실패 - 존재하지 않는 참여 코드")
    void joinGroupRoom_Fail_StudyRoomNotFound() {
        // given
        Long userId = 2L;
        String invalidJoinCode = "INVALID1";

        User mockUser = User.builder()
                .userId(userId)
                .email("member@example.com")
                .username("멤버유저")
                .receiveNotifications(true)
                .build();

        GroupRoomJoinCommand command = new GroupRoomJoinCommand(userId, invalidJoinCode);

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(studyRoomRepository.findByJoinCode(invalidJoinCode)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> studyRoomService.joinGroupRoom(command))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", StudyRoomException.STUDY_ROOM_NOT_FOUND);

        verify(userRepository, times(1)).findById(userId);
        verify(studyRoomRepository, times(1)).findByJoinCode(invalidJoinCode);
        verify(studyRoomMemberRepository, never()).save(any(StudyRoomMember.class));
    }

    @Test
    @DisplayName("그룹 스터디 참여 실패 - 개인 공부방의 참여 코드")
    void joinGroupRoom_Fail_NotGroupRoom() {
        // given
        Long userId = 2L;
        String joinCode = "PERSONAL";

        User mockUser = User.builder()
                .userId(userId)
                .email("member@example.com")
                .username("멤버유저")
                .receiveNotifications(true)
                .build();

        User ownerUser = User.builder()
                .userId(1L)
                .email("owner@example.com")
                .username("방장유저")
                .receiveNotifications(true)
                .build();

        StudyRoom personalRoom = StudyRoom.builder()
                .studyRoomId(5L)
                .owner(ownerUser)
                .roomType(RoomType.PERSONAL) // 개인방!
                .name("개인 공부방")
                .description("개인용")
                .category("기타")
                .joinCode(joinCode)
                .build();

        GroupRoomJoinCommand command = new GroupRoomJoinCommand(userId, joinCode);

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(studyRoomRepository.findByJoinCode(joinCode)).willReturn(Optional.of(personalRoom));

        // when & then
        assertThatThrownBy(() -> studyRoomService.joinGroupRoom(command))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", StudyRoomException.NOT_GROUP_ROOM);

        verify(userRepository, times(1)).findById(userId);
        verify(studyRoomRepository, times(1)).findByJoinCode(joinCode);
        verify(studyRoomMemberRepository, never()).existsByUserAndStudyRoomAndActive(any(), any(), any());
        verify(studyRoomMemberRepository, never()).save(any(StudyRoomMember.class));
    }

    @Test
    @DisplayName("그룹 스터디 참여 실패 - 이미 참여한 스터디룸")
    void joinGroupRoom_Fail_AlreadyJoined() {
        // given
        Long userId = 2L;
        String joinCode = "ABC12345";

        User mockUser = User.builder()
                .userId(userId)
                .email("member@example.com")
                .username("멤버유저")
                .receiveNotifications(true)
                .build();

        User ownerUser = User.builder()
                .userId(1L)
                .email("owner@example.com")
                .username("방장유저")
                .receiveNotifications(true)
                .build();

        StudyRoom mockGroupRoom = StudyRoom.builder()
                .studyRoomId(10L)
                .owner(ownerUser)
                .roomType(RoomType.GROUP)
                .name("알고리즘 스터디")
                .description("매일 알고리즘 풀이")
                .category("알고리즘")
                .joinCode(joinCode)
                .build();

        GroupRoomJoinCommand command = new GroupRoomJoinCommand(userId, joinCode);

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(studyRoomRepository.findByJoinCode(joinCode)).willReturn(Optional.of(mockGroupRoom));
        given(studyRoomMemberRepository.existsByUserAndStudyRoomAndActive(mockUser, mockGroupRoom, true))
                .willReturn(true); // 이미 참여함

        // when & then
        assertThatThrownBy(() -> studyRoomService.joinGroupRoom(command))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", StudyRoomException.ALREADY_JOINED);

        verify(userRepository, times(1)).findById(userId);
        verify(studyRoomRepository, times(1)).findByJoinCode(joinCode);
        verify(studyRoomMemberRepository, times(1)).existsByUserAndStudyRoomAndActive(mockUser, mockGroupRoom, true);
        verify(studyRoomMemberRepository, never()).save(any(StudyRoomMember.class));
    }

    // ===== 개인 공부방 목록 조회 API 테스트 =====

    @Test
    @DisplayName("개인 공부방 목록 조회 성공 - 빈 목록")
    void getPersonalRooms_Success_EmptyList() {
        // given
        Long userId = 1L;
        User mockUser = User.builder()
                .userId(userId)
                .email("test@example.com")
                .username("테스트유저")
                .receiveNotifications(true)
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(studyRoomRepository.findAllByOwnerAndRoomType(mockUser, RoomType.PERSONAL))
                .willReturn(Collections.emptyList());

        // when
        PersonalRoomListResult result = studyRoomService.getPersonalRooms(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.rooms()).isEmpty();

        verify(userRepository, times(1)).findById(userId);
        verify(studyRoomRepository, times(1)).findAllByOwnerAndRoomType(mockUser, RoomType.PERSONAL);
    }

    @Test
    @DisplayName("개인 공부방 목록 조회 성공 - 여러 개의 공부방")
    void getPersonalRooms_Success_MultipleRooms() {
        // given
        Long userId = 1L;
        User mockUser = User.builder()
                .userId(userId)
                .email("test@example.com")
                .username("테스트유저")
                .receiveNotifications(true)
                .build();

        StudyRoom room1 = StudyRoom.builder()
                .studyRoomId(1L)
                .owner(mockUser)
                .roomType(RoomType.PERSONAL)
                .name("자바 스터디")
                .description("자바 기초")
                .category("프로그래밍")
                .joinCode(null)
                .build();

        StudyRoom room2 = StudyRoom.builder()
                .studyRoomId(2L)
                .owner(mockUser)
                .roomType(RoomType.PERSONAL)
                .name("스프링 스터디")
                .description("스프링 심화")
                .category("프레임워크")
                .joinCode(null)
                .build();

        List<StudyRoom> personalRooms = Arrays.asList(room1, room2);

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(studyRoomRepository.findAllByOwnerAndRoomType(mockUser, RoomType.PERSONAL))
                .willReturn(personalRooms);
        given(problemRepository.countByStudyRoom(room1)).willReturn(10);
        given(problemRepository.countByStudyRoom(room2)).willReturn(15);
        given(problemReviewStateRepository.countByUserAndProblem_StudyRoomAndGate(mockUser, room1, ReviewGate.GRADUATED))
                .willReturn(5);
        given(problemReviewStateRepository.countByUserAndProblem_StudyRoomAndGate(mockUser, room2, ReviewGate.GRADUATED))
                .willReturn(8);

        // when
        PersonalRoomListResult result = studyRoomService.getPersonalRooms(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.rooms()).hasSize(2);

        PersonalRoomListResult.PersonalRoomInfo firstRoom = result.rooms().get(0);
        assertThat(firstRoom.studyRoomId()).isEqualTo(1L);
        assertThat(firstRoom.name()).isEqualTo("자바 스터디");
        assertThat(firstRoom.category()).isEqualTo("프로그래밍");
        assertThat(firstRoom.totalProblems()).isEqualTo(10);
        assertThat(firstRoom.graduatedProblems()).isEqualTo(5);

        PersonalRoomListResult.PersonalRoomInfo secondRoom = result.rooms().get(1);
        assertThat(secondRoom.studyRoomId()).isEqualTo(2L);
        assertThat(secondRoom.name()).isEqualTo("스프링 스터디");
        assertThat(secondRoom.category()).isEqualTo("프레임워크");
        assertThat(secondRoom.totalProblems()).isEqualTo(15);
        assertThat(secondRoom.graduatedProblems()).isEqualTo(8);

        verify(userRepository, times(1)).findById(userId);
        verify(studyRoomRepository, times(1)).findAllByOwnerAndRoomType(mockUser, RoomType.PERSONAL);
        verify(problemRepository, times(2)).countByStudyRoom(any(StudyRoom.class));
        verify(problemReviewStateRepository, times(2))
                .countByUserAndProblem_StudyRoomAndGate(any(User.class), any(StudyRoom.class), eq(ReviewGate.GRADUATED));
    }

    @Test
    @DisplayName("개인 공부방 목록 조회 실패 - 사용자를 찾을 수 없음")
    void getPersonalRooms_Fail_UserNotFound() {
        // given
        Long nonExistentUserId = 999L;

        given(userRepository.findById(nonExistentUserId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> studyRoomService.getPersonalRooms(nonExistentUserId))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", UserException.USER_NOT_FOUND);

        verify(userRepository, times(1)).findById(nonExistentUserId);
        verify(studyRoomRepository, never()).findAllByOwnerAndRoomType(any(), any());
    }

    // ===== 그룹 스터디 목록 조회 API 테스트 =====

    @Test
    @DisplayName("그룹 스터디 목록 조회 성공 - 빈 목록")
    void getGroupRooms_Success_EmptyList() {
        // given
        Long userId = 1L;
        User mockUser = User.builder()
                .userId(userId)
                .email("test@example.com")
                .username("테스트유저")
                .receiveNotifications(true)
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(studyRoomMemberRepository.findAllByUserAndActive(mockUser, true))
                .willReturn(Collections.emptyList());

        // when
        GroupRoomListResult result = studyRoomService.getGroupRooms(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.rooms()).isEmpty();

        verify(userRepository, times(1)).findById(userId);
        verify(studyRoomMemberRepository, times(1)).findAllByUserAndActive(mockUser, true);
    }

    @Test
    @DisplayName("그룹 스터디 목록 조회 성공 - 여러 개의 그룹")
    void getGroupRooms_Success_MultipleGroups() {
        // given
        Long userId = 2L;
        User mockUser = User.builder()
                .userId(userId)
                .email("member@example.com")
                .username("멤버유저")
                .receiveNotifications(true)
                .build();

        User owner1 = User.builder()
                .userId(1L)
                .email("owner1@example.com")
                .username("방장1")
                .receiveNotifications(true)
                .build();

        User owner2 = User.builder()
                .userId(3L)
                .email("owner2@example.com")
                .username("방장2")
                .receiveNotifications(true)
                .build();

        StudyRoom group1 = StudyRoom.builder()
                .studyRoomId(10L)
                .owner(owner1)
                .roomType(RoomType.GROUP)
                .name("알고리즘 스터디")
                .description("매일 알고리즘")
                .category("알고리즘")
                .joinCode("CODE001")
                .build();

        StudyRoom group2 = StudyRoom.builder()
                .studyRoomId(20L)
                .owner(owner2)
                .roomType(RoomType.GROUP)
                .name("CS 스터디")
                .description("CS 기초 학습")
                .category("CS")
                .joinCode("CODE002")
                .build();

        StudyRoomMember member1 = StudyRoomMember.builder()
                .memberId(1L)
                .user(mockUser)
                .studyRoom(group1)
                .active(true)
                .build();

        StudyRoomMember member2 = StudyRoomMember.builder()
                .memberId(2L)
                .user(mockUser)
                .studyRoom(group2)
                .active(true)
                .build();

        List<StudyRoomMember> memberships = Arrays.asList(member1, member2);

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(studyRoomMemberRepository.findAllByUserAndActive(mockUser, true))
                .willReturn(memberships);
        given(problemRepository.countByStudyRoom(group1)).willReturn(20);
        given(problemRepository.countByStudyRoom(group2)).willReturn(30);
        given(problemReviewStateRepository.countByUserAndProblem_StudyRoomAndGate(mockUser, group1, ReviewGate.GRADUATED))
                .willReturn(12);
        given(problemReviewStateRepository.countByUserAndProblem_StudyRoomAndGate(mockUser, group2, ReviewGate.GRADUATED))
                .willReturn(18);

        // when
        GroupRoomListResult result = studyRoomService.getGroupRooms(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.rooms()).hasSize(2);

        GroupRoomListResult.GroupRoomInfo firstGroup = result.rooms().get(0);
        assertThat(firstGroup.studyRoomId()).isEqualTo(10L);
        assertThat(firstGroup.name()).isEqualTo("알고리즘 스터디");
        assertThat(firstGroup.category()).isEqualTo("알고리즘");
        assertThat(firstGroup.joinCode()).isEqualTo("CODE001");
        assertThat(firstGroup.totalProblems()).isEqualTo(20);
        assertThat(firstGroup.graduatedProblems()).isEqualTo(12);

        GroupRoomListResult.GroupRoomInfo secondGroup = result.rooms().get(1);
        assertThat(secondGroup.studyRoomId()).isEqualTo(20L);
        assertThat(secondGroup.name()).isEqualTo("CS 스터디");
        assertThat(secondGroup.category()).isEqualTo("CS");
        assertThat(secondGroup.joinCode()).isEqualTo("CODE002");
        assertThat(secondGroup.totalProblems()).isEqualTo(30);
        assertThat(secondGroup.graduatedProblems()).isEqualTo(18);

        verify(userRepository, times(1)).findById(userId);
        verify(studyRoomMemberRepository, times(1)).findAllByUserAndActive(mockUser, true);
        verify(problemRepository, times(2)).countByStudyRoom(any(StudyRoom.class));
        verify(problemReviewStateRepository, times(2))
                .countByUserAndProblem_StudyRoomAndGate(any(User.class), any(StudyRoom.class), eq(ReviewGate.GRADUATED));
    }

    @Test
    @DisplayName("그룹 스터디 목록 조회 실패 - 사용자를 찾을 수 없음")
    void getGroupRooms_Fail_UserNotFound() {
        // given
        Long nonExistentUserId = 999L;

        given(userRepository.findById(nonExistentUserId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> studyRoomService.getGroupRooms(nonExistentUserId))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", UserException.USER_NOT_FOUND);

        verify(userRepository, times(1)).findById(nonExistentUserId);
        verify(studyRoomMemberRepository, never()).findAllByUserAndActive(any(), any());
    }
}