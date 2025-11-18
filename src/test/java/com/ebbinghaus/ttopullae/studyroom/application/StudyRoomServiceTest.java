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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

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
}