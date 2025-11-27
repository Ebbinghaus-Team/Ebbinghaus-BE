package com.ebbinghaus.ttopullae.problem.application;

import com.ebbinghaus.ttopullae.global.exception.ApplicationException;
import com.ebbinghaus.ttopullae.problem.application.dto.ProblemCreateCommand;
import com.ebbinghaus.ttopullae.problem.application.dto.ProblemCreateResult;
import com.ebbinghaus.ttopullae.problem.domain.*;
import com.ebbinghaus.ttopullae.problem.domain.repository.*;
import com.ebbinghaus.ttopullae.problem.exception.ProblemException;
import com.ebbinghaus.ttopullae.studyroom.domain.RoomType;
import com.ebbinghaus.ttopullae.studyroom.domain.StudyRoom;
import com.ebbinghaus.ttopullae.studyroom.domain.StudyRoomMember;
import com.ebbinghaus.ttopullae.studyroom.domain.repository.StudyRoomMemberRepository;
import com.ebbinghaus.ttopullae.studyroom.domain.repository.StudyRoomRepository;
import com.ebbinghaus.ttopullae.user.domain.User;
import com.ebbinghaus.ttopullae.user.domain.repository.UserRepository;
import com.ebbinghaus.ttopullae.user.exception.UserException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProblemServiceTest {

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private ProblemChoiceRepository problemChoiceRepository;

    @Mock
    private ProblemKeywordRepository problemKeywordRepository;

    @Mock
    private ProblemReviewStateRepository problemReviewStateRepository;

    @Mock
    private ProblemAttemptRepository problemAttemptRepository;

    @Mock
    private StudyRoomRepository studyRoomRepository;

    @Mock
    private StudyRoomMemberRepository studyRoomMemberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AiGradingService aiGradingService;

    @InjectMocks
    private ProblemService problemService;

    @Test
    @DisplayName("객관식 문제 생성 성공")
    void createMcqProblem_Success() {
        // Given
        Long userId = 1L;
        Long studyRoomId = 1L;
        List<String> choices = List.of("public", "private", "protected", "friend");
        Integer correctChoiceIndex = 3;

        User mockUser = User.builder()
                .userId(userId)
                .email("test@example.com")
                .password("password")
                .username("테스터")
                .receiveNotifications(true)
                .build();

        StudyRoom mockStudyRoom = StudyRoom.builder()
                .studyRoomId(studyRoomId)
                .owner(mockUser)
                .roomType(RoomType.PERSONAL)
                .name("자바 스터디")
                .build();

        Problem mockProblem = Problem.builder()
                .problemId(1L)
                .studyRoom(mockStudyRoom)
                .creator(mockUser)
                .problemType(ProblemType.MCQ)
                .question("자바의 접근 제어자가 아닌 것은?")
                .explanation("friend는 C++의 접근 제어자입니다.")
                .correctChoiceIndex(correctChoiceIndex)
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(studyRoomRepository.findById(studyRoomId)).willReturn(Optional.of(mockStudyRoom));
        given(problemRepository.save(any(Problem.class))).willReturn(mockProblem);

        ProblemCreateCommand command = new ProblemCreateCommand(
                userId, studyRoomId, ProblemType.MCQ,
                "자바의 접근 제어자가 아닌 것은?",
                "friend는 C++의 접근 제어자입니다.",
                choices, correctChoiceIndex,
                null, null, null, null
        );

        // When
        ProblemCreateResult result = problemService.createProblem(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.problemId()).isEqualTo(1L);
        assertThat(result.problemType()).isEqualTo(ProblemType.MCQ);
        verify(problemChoiceRepository, times(1)).saveAll(anyList());
        verify(problemReviewStateRepository, times(1)).save(any(ProblemReviewState.class));
    }

    @Test
    @DisplayName("OX 문제 생성 성공")
    void createOxProblem_Success() {
        // Given
        Long userId = 1L;
        Long studyRoomId = 1L;

        User mockUser = User.builder()
                .userId(userId)
                .email("test@example.com")
                .password("password")
                .username("테스터")
                .receiveNotifications(true)
                .build();

        StudyRoom mockStudyRoom = StudyRoom.builder()
                .studyRoomId(studyRoomId)
                .owner(mockUser)
                .roomType(RoomType.PERSONAL)
                .name("자바 스터디")
                .build();

        Problem mockProblem = Problem.builder()
                .problemId(2L)
                .studyRoom(mockStudyRoom)
                .creator(mockUser)
                .problemType(ProblemType.OX)
                .question("JVM은 Java Virtual Machine의 약자이다.")
                .explanation("맞습니다.")
                .answerBoolean(true)
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(studyRoomRepository.findById(studyRoomId)).willReturn(Optional.of(mockStudyRoom));
        given(problemRepository.save(any(Problem.class))).willReturn(mockProblem);

        ProblemCreateCommand command = new ProblemCreateCommand(
                userId, studyRoomId, ProblemType.OX,
                "JVM은 Java Virtual Machine의 약자이다.",
                "맞습니다.",
                null, null,
                true,
                null, null, null
        );

        // When
        ProblemCreateResult result = problemService.createProblem(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.problemType()).isEqualTo(ProblemType.OX);
        verify(problemReviewStateRepository, times(1)).save(any(ProblemReviewState.class));
    }

    @Test
    @DisplayName("단답형 문제 생성 성공")
    void createShortProblem_Success() {
        // Given
        Long userId = 1L;
        Long studyRoomId = 1L;

        User mockUser = User.builder()
                .userId(userId)
                .email("test@example.com")
                .password("password")
                .username("테스터")
                .receiveNotifications(true)
                .build();

        StudyRoom mockStudyRoom = StudyRoom.builder()
                .studyRoomId(studyRoomId)
                .owner(mockUser)
                .roomType(RoomType.PERSONAL)
                .name("자바 스터디")
                .build();

        Problem mockProblem = Problem.builder()
                .problemId(3L)
                .studyRoom(mockStudyRoom)
                .creator(mockUser)
                .problemType(ProblemType.SHORT)
                .question("자바에서 문자열을 다루는 불변 클래스는?")
                .explanation("String 클래스입니다.")
                .answerText("String")
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(studyRoomRepository.findById(studyRoomId)).willReturn(Optional.of(mockStudyRoom));
        given(problemRepository.save(any(Problem.class))).willReturn(mockProblem);

        ProblemCreateCommand command = new ProblemCreateCommand(
                userId, studyRoomId, ProblemType.SHORT,
                "자바에서 문자열을 다루는 불변 클래스는?",
                "String 클래스입니다.",
                null, null, null,
                "String",
                null, null
        );

        // When
        ProblemCreateResult result = problemService.createProblem(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.problemType()).isEqualTo(ProblemType.SHORT);
    }

    @Test
    @DisplayName("서술형 문제 생성 성공")
    void createSubjectiveProblem_Success() {
        // Given
        Long userId = 1L;
        Long studyRoomId = 1L;
        List<String> keywords = List.of("도메인", "엔티티", "리포지토리");

        User mockUser = User.builder()
                .userId(userId)
                .email("test@example.com")
                .password("password")
                .username("테스터")
                .receiveNotifications(true)
                .build();

        StudyRoom mockStudyRoom = StudyRoom.builder()
                .studyRoomId(studyRoomId)
                .owner(mockUser)
                .roomType(RoomType.PERSONAL)
                .name("자바 스터디")
                .build();

        Problem mockProblem = Problem.builder()
                .problemId(4L)
                .studyRoom(mockStudyRoom)
                .creator(mockUser)
                .problemType(ProblemType.SUBJECTIVE)
                .question("DDD의 핵심 개념에 대해 설명하시오.")
                .explanation("DDD는 도메인 중심 설계입니다.")
                .modelAnswerText("DDD는 도메인을 중심으로 소프트웨어를 설계하는 방법론입니다.")
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(studyRoomRepository.findById(studyRoomId)).willReturn(Optional.of(mockStudyRoom));
        given(problemRepository.save(any(Problem.class))).willReturn(mockProblem);

        ProblemCreateCommand command = new ProblemCreateCommand(
                userId, studyRoomId, ProblemType.SUBJECTIVE,
                "DDD의 핵심 개념에 대해 설명하시오.",
                "DDD는 도메인 중심 설계입니다.",
                null, null, null, null,
                "DDD는 도메인을 중심으로 소프트웨어를 설계하는 방법론입니다.",
                keywords
        );

        // When
        ProblemCreateResult result = problemService.createProblem(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.problemType()).isEqualTo(ProblemType.SUBJECTIVE);
        verify(problemKeywordRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("사용자 없음 예외")
    void createProblem_UserNotFound() {
        // Given
        Long userId = 999L;
        Long studyRoomId = 1L;

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        ProblemCreateCommand command = new ProblemCreateCommand(
                userId, studyRoomId, ProblemType.OX,
                "테스트 문제", "해설",
                null, null, true, null, null, null
        );

        // When & Then
        assertThatThrownBy(() -> problemService.createProblem(command))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", UserException.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("스터디룸 없음 예외")
    void createProblem_StudyRoomNotFound() {
        // Given
        Long userId = 1L;
        Long studyRoomId = 999L;

        User mockUser = User.builder()
                .userId(userId)
                .email("test@example.com")
                .password("password")
                .username("테스터")
                .receiveNotifications(true)
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(studyRoomRepository.findById(studyRoomId)).willReturn(Optional.empty());

        ProblemCreateCommand command = new ProblemCreateCommand(
                userId, studyRoomId, ProblemType.OX,
                "테스트 문제", "해설",
                null, null, true, null, null, null
        );

        // When & Then
        assertThatThrownBy(() -> problemService.createProblem(command))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", ProblemException.STUDYROOM_NOT_FOUND);
    }

    @Test
    @DisplayName("객관식 데이터 누락 예외")
    void createProblem_InvalidMcqData() {
        // Given
        Long userId = 1L;
        Long studyRoomId = 1L;

        User mockUser = User.builder().userId(userId).email("test@example.com").password("password").username("테스터").receiveNotifications(true).build();
        StudyRoom mockStudyRoom = StudyRoom.builder().studyRoomId(studyRoomId).owner(mockUser).roomType(RoomType.PERSONAL).name("자바 스터디").build();

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(studyRoomRepository.findById(studyRoomId)).willReturn(Optional.of(mockStudyRoom));

        ProblemCreateCommand command = new ProblemCreateCommand(
                userId, studyRoomId, ProblemType.MCQ,
                "테스트 문제", "해설",
                null, null, null, null, null, null
        );

        // When & Then
        assertThatThrownBy(() -> problemService.createProblem(command))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", ProblemException.INVALID_MCQ_DATA);
    }

    @Test
    @DisplayName("OX 데이터 누락 예외")
    void createProblem_InvalidOxData() {
        // Given
        Long userId = 1L;
        Long studyRoomId = 1L;

        User mockUser = User.builder().userId(userId).email("test@example.com").password("password").username("테스터").receiveNotifications(true).build();
        StudyRoom mockStudyRoom = StudyRoom.builder().studyRoomId(studyRoomId).owner(mockUser).roomType(RoomType.PERSONAL).name("자바 스터디").build();

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(studyRoomRepository.findById(studyRoomId)).willReturn(Optional.of(mockStudyRoom));

        ProblemCreateCommand command = new ProblemCreateCommand(
                userId, studyRoomId, ProblemType.OX,
                "테스트 문제", "해설",
                null, null, null, null, null, null
        );

        // When & Then
        assertThatThrownBy(() -> problemService.createProblem(command))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", ProblemException.INVALID_OX_DATA);
    }

    @Test
    @DisplayName("단답형 데이터 누락 예외")
    void createProblem_InvalidShortData() {
        // Given
        Long userId = 1L;
        Long studyRoomId = 1L;

        User mockUser = User.builder().userId(userId).email("test@example.com").password("password").username("테스터").receiveNotifications(true).build();
        StudyRoom mockStudyRoom = StudyRoom.builder().studyRoomId(studyRoomId).owner(mockUser).roomType(RoomType.PERSONAL).name("자바 스터디").build();

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(studyRoomRepository.findById(studyRoomId)).willReturn(Optional.of(mockStudyRoom));

        ProblemCreateCommand command = new ProblemCreateCommand(
                userId, studyRoomId, ProblemType.SHORT,
                "테스트 문제", "해설",
                null, null, null, null, null, null
        );

        // When & Then
        assertThatThrownBy(() -> problemService.createProblem(command))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", ProblemException.INVALID_SHORT_DATA);
    }

    @Test
    @DisplayName("서술형 데이터 누락 예외")
    void createProblem_InvalidSubjectiveData() {
        // Given
        Long userId = 1L;
        Long studyRoomId = 1L;

        User mockUser = User.builder().userId(userId).email("test@example.com").password("password").username("테스터").receiveNotifications(true).build();
        StudyRoom mockStudyRoom = StudyRoom.builder().studyRoomId(studyRoomId).owner(mockUser).roomType(RoomType.PERSONAL).name("자바 스터디").build();

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(studyRoomRepository.findById(studyRoomId)).willReturn(Optional.of(mockStudyRoom));

        ProblemCreateCommand command = new ProblemCreateCommand(
                userId, studyRoomId, ProblemType.SUBJECTIVE,
                "테스트 문제", "해설",
                null, null, null, null, null, null
        );

        // When & Then
        assertThatThrownBy(() -> problemService.createProblem(command))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", ProblemException.INVALID_SUBJECTIVE_DATA);
    }

    // ===== 오늘의 복습 문제 조회 API 테스트 =====

    @Test
    @DisplayName("오늘의 복습 문제 조회 성공 - 필터 ALL")
    void getTodayReviewProblems_Success_FilterAll() {
        // Given
        Long userId = 1L;
        String filter = "ALL";
        LocalDate today = LocalDate.now();

        User mockUser = User.builder()
                .userId(userId)
                .email("test@example.com")
                .password("password")
                .username("테스터")
                .receiveNotifications(true)
                .build();

        StudyRoom mockStudyRoom = StudyRoom.builder()
                .studyRoomId(1L)
                .owner(mockUser)
                .roomType(RoomType.PERSONAL)
                .name("자바 스터디")
                .build();

        Problem problem1 = Problem.builder()
                .problemId(1L)
                .studyRoom(mockStudyRoom)
                .creator(mockUser)
                .problemType(ProblemType.MCQ)
                .question("자바의 접근 제어자가 아닌 것은?")
                .build();

        Problem problem2 = Problem.builder()
                .problemId(2L)
                .studyRoom(mockStudyRoom)
                .creator(mockUser)
                .problemType(ProblemType.SHORT)
                .question("JPA의 영속성 컨텍스트란?")
                .build();

        ProblemReviewState reviewState1 = ProblemReviewState.builder()
                .user(mockUser)
                .problem(problem1)
                .gate(ReviewGate.GATE_1)
                .nextReviewDate(today)
                .reviewCount(0)
                .todayReviewIncludedDate(today)
                .todayReviewIncludedGate(ReviewGate.GATE_1)
                .build();

        ProblemReviewState reviewState2 = ProblemReviewState.builder()
                .user(mockUser)
                .problem(problem2)
                .gate(ReviewGate.GATE_2)
                .nextReviewDate(today)
                .reviewCount(1)
                .todayReviewFirstAttemptDate(today)
                .todayReviewIncludedDate(today)
                .todayReviewIncludedGate(ReviewGate.GATE_2)
                .build();

        List<ProblemReviewState> reviewStates = List.of(reviewState1, reviewState2);

        given(problemReviewStateRepository.findTodaysReviewProblems(userId, today, null))
                .willReturn(reviewStates);
        given(problemAttemptRepository.findTodaysFirstAttemptsByUserAndProblems(
                any(Long.class), anyList(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(List.of());

        com.ebbinghaus.ttopullae.problem.application.dto.TodayReviewCommand command =
                new com.ebbinghaus.ttopullae.problem.application.dto.TodayReviewCommand(userId, filter);

        // When
        com.ebbinghaus.ttopullae.problem.application.dto.TodayReviewResult result =
                problemService.getTodayReviewProblems(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.dashboard().totalCount()).isEqualTo(2);
        assertThat(result.dashboard().completedCount()).isEqualTo(1); // problem2만 오늘 완료
        assertThat(result.dashboard().incompletedCount()).isEqualTo(1);
        assertThat(result.dashboard().progressRate()).isEqualTo(50.0);

        assertThat(result.problems()).hasSize(2);
        assertThat(result.problems().get(0).problemId()).isEqualTo(1L);
        assertThat(result.problems().get(0).gate()).isEqualTo(ReviewGate.GATE_1);
        assertThat(result.problems().get(1).problemId()).isEqualTo(2L);
        assertThat(result.problems().get(1).gate()).isEqualTo(ReviewGate.GATE_2);

        verify(problemReviewStateRepository, times(1)).findTodaysReviewProblems(userId, today, null);
    }

    @Test
    @DisplayName("오늘의 복습 문제 조회 성공 - 필터 GATE_1")
    void getTodayReviewProblems_Success_FilterGate1() {
        // Given
        Long userId = 1L;
        String filter = "GATE_1";
        LocalDate today = LocalDate.now();

        User mockUser = User.builder()
                .userId(userId)
                .email("test@example.com")
                .password("password")
                .username("테스터")
                .receiveNotifications(true)
                .build();

        StudyRoom mockStudyRoom = StudyRoom.builder()
                .studyRoomId(1L)
                .owner(mockUser)
                .roomType(RoomType.PERSONAL)
                .name("자바 스터디")
                .build();

        Problem problem1 = Problem.builder()
                .problemId(1L)
                .studyRoom(mockStudyRoom)
                .creator(mockUser)
                .problemType(ProblemType.MCQ)
                .question("자바의 접근 제어자가 아닌 것은?")
                .build();

        ProblemReviewState reviewState1 = ProblemReviewState.builder()
                .user(mockUser)
                .problem(problem1)
                .gate(ReviewGate.GATE_1)
                .nextReviewDate(today)
                .reviewCount(0)
                .todayReviewIncludedDate(today)
                .todayReviewIncludedGate(ReviewGate.GATE_1)
                .build();

        List<ProblemReviewState> reviewStates = List.of(reviewState1);

        given(problemReviewStateRepository.findTodaysReviewProblems(userId, today, ReviewGate.GATE_1))
                .willReturn(reviewStates);
        given(problemAttemptRepository.findTodaysFirstAttemptsByUserAndProblems(
                any(Long.class), anyList(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(List.of());

        com.ebbinghaus.ttopullae.problem.application.dto.TodayReviewCommand command =
                new com.ebbinghaus.ttopullae.problem.application.dto.TodayReviewCommand(userId, filter);

        // When
        com.ebbinghaus.ttopullae.problem.application.dto.TodayReviewResult result =
                problemService.getTodayReviewProblems(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.dashboard().totalCount()).isEqualTo(1);
        assertThat(result.problems()).hasSize(1);
        assertThat(result.problems().get(0).gate()).isEqualTo(ReviewGate.GATE_1);

        verify(problemReviewStateRepository, times(1))
                .findTodaysReviewProblems(userId, today, ReviewGate.GATE_1);
    }

    @Test
    @DisplayName("오늘의 복습 문제 조회 성공 - 필터 GATE_2")
    void getTodayReviewProblems_Success_FilterGate2() {
        // Given
        Long userId = 1L;
        String filter = "GATE_2";
        LocalDate today = LocalDate.now();

        User mockUser = User.builder()
                .userId(userId)
                .email("test@example.com")
                .password("password")
                .username("테스터")
                .receiveNotifications(true)
                .build();

        StudyRoom mockStudyRoom = StudyRoom.builder()
                .studyRoomId(1L)
                .owner(mockUser)
                .roomType(RoomType.PERSONAL)
                .name("자바 스터디")
                .build();

        Problem problem2 = Problem.builder()
                .problemId(2L)
                .studyRoom(mockStudyRoom)
                .creator(mockUser)
                .problemType(ProblemType.SHORT)
                .question("JPA의 영속성 컨텍스트란?")
                .build();

        ProblemReviewState reviewState2 = ProblemReviewState.builder()
                .user(mockUser)
                .problem(problem2)
                .gate(ReviewGate.GATE_2)
                .nextReviewDate(today)
                .reviewCount(1)
                .todayReviewIncludedDate(today)
                .todayReviewIncludedGate(ReviewGate.GATE_2)
                .build();

        List<ProblemReviewState> reviewStates = List.of(reviewState2);

        given(problemReviewStateRepository.findTodaysReviewProblems(userId, today, ReviewGate.GATE_2))
                .willReturn(reviewStates);
        given(problemAttemptRepository.findTodaysFirstAttemptsByUserAndProblems(
                any(Long.class), anyList(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(List.of());

        com.ebbinghaus.ttopullae.problem.application.dto.TodayReviewCommand command =
                new com.ebbinghaus.ttopullae.problem.application.dto.TodayReviewCommand(userId, filter);

        // When
        com.ebbinghaus.ttopullae.problem.application.dto.TodayReviewResult result =
                problemService.getTodayReviewProblems(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.dashboard().totalCount()).isEqualTo(1);
        assertThat(result.problems()).hasSize(1);
        assertThat(result.problems().get(0).gate()).isEqualTo(ReviewGate.GATE_2);

        verify(problemReviewStateRepository, times(1))
                .findTodaysReviewProblems(userId, today, ReviewGate.GATE_2);
    }

    @Test
    @DisplayName("오늘의 복습 문제 조회 성공 - 빈 목록")
    void getTodayReviewProblems_Success_EmptyList() {
        // Given
        Long userId = 1L;
        String filter = "ALL";
        LocalDate today = LocalDate.now();

        given(problemReviewStateRepository.findTodaysReviewProblems(userId, today, null))
                .willReturn(List.of());

        com.ebbinghaus.ttopullae.problem.application.dto.TodayReviewCommand command =
                new com.ebbinghaus.ttopullae.problem.application.dto.TodayReviewCommand(userId, filter);

        // When
        com.ebbinghaus.ttopullae.problem.application.dto.TodayReviewResult result =
                problemService.getTodayReviewProblems(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.dashboard().totalCount()).isEqualTo(0);
        assertThat(result.dashboard().completedCount()).isEqualTo(0);
        assertThat(result.dashboard().incompletedCount()).isEqualTo(0);
        assertThat(result.dashboard().progressRate()).isEqualTo(0.0);
        assertThat(result.problems()).isEmpty();

        verify(problemReviewStateRepository, times(1)).findTodaysReviewProblems(userId, today, null);
    }

    @Test
    @DisplayName("오늘의 복습 문제 조회 실패 - 잘못된 필터 값")
    void getTodayReviewProblems_Fail_InvalidFilter() {
        // Given
        Long userId = 1L;
        String invalidFilter = "INVALID_FILTER";

        com.ebbinghaus.ttopullae.problem.application.dto.TodayReviewCommand command =
                new com.ebbinghaus.ttopullae.problem.application.dto.TodayReviewCommand(userId, invalidFilter);

        // When & Then
        assertThatThrownBy(() -> problemService.getTodayReviewProblems(command))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", com.ebbinghaus.ttopullae.global.exception.CommonException.INVALID_QUERY_PARAMETER);

        verify(problemReviewStateRepository, never()).findTodaysReviewProblems(any(), any(), any());
    }

    // ===== 문제 풀이 제출 API 단위 테스트 =====

    @Test
    @DisplayName("문제 풀이 제출 - 객관식 정답 (오늘의 복습 첫 시도, GATE_1 → GATE_2 승급)")
    void submitProblemAnswer_McqCorrect_TodayReviewFirstAttempt_Promotion() {
        // Given
        Long userId = 1L;
        Long problemId = 1L;
        String answer = "3";
        LocalDate today = LocalDate.now();

        User mockUser = User.builder()
                .userId(userId)
                .email("test@example.com")
                .password("password")
                .username("테스터")
                .receiveNotifications(true)
                .build();

        StudyRoom mockStudyRoom = StudyRoom.builder()
                .studyRoomId(1L)
                .owner(mockUser)
                .roomType(RoomType.PERSONAL)
                .name("자바 스터디")
                .build();

        Problem mockProblem = Problem.builder()
                .problemId(problemId)
                .studyRoom(mockStudyRoom)
                .creator(mockUser)
                .problemType(ProblemType.MCQ)
                .question("자바의 접근 제어자가 아닌 것은?")
                .explanation("friend는 C++의 접근 제어자입니다.")
                .correctChoiceIndex(3)
                .build();

        ProblemReviewState mockReviewState = ProblemReviewState.builder()
                .user(mockUser)
                .problem(mockProblem)
                .gate(ReviewGate.GATE_1)
                .nextReviewDate(today)
                .reviewCount(0)
                .todayReviewIncludedDate(today)
                .todayReviewIncludedGate(ReviewGate.GATE_1)
                .todayReviewFirstAttemptDate(null)
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(problemRepository.findById(problemId)).willReturn(Optional.of(mockProblem));
        given(problemReviewStateRepository.findByUserAndProblem(mockUser, mockProblem))
                .willReturn(Optional.of(mockReviewState));

        com.ebbinghaus.ttopullae.problem.application.dto.ProblemSubmitCommand command =
                new com.ebbinghaus.ttopullae.problem.application.dto.ProblemSubmitCommand(userId, problemId, answer);

        // When
        com.ebbinghaus.ttopullae.problem.application.dto.ProblemSubmitResult result =
                problemService.submitProblemAnswer(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isCorrect()).isTrue();
        assertThat(result.explanation()).isEqualTo("friend는 C++의 접근 제어자입니다.");
        assertThat(result.currentGate()).isEqualTo(ReviewGate.GATE_2);
        assertThat(result.reviewCount()).isEqualTo(1);
        assertThat(result.isFirstAttempt()).isTrue();
        assertThat(result.isReviewStateChanged()).isTrue();

        verify(problemAttemptRepository, times(1)).save(any(ProblemAttempt.class));
    }

    @Test
    @DisplayName("문제 풀이 제출 - 객관식 오답 (오늘의 복습 첫 시도, GATE_2 → GATE_1 강등)")
    void submitProblemAnswer_McqWrong_TodayReviewFirstAttempt_Demotion() {
        // Given
        Long userId = 1L;
        Long problemId = 2L;
        String answer = "1";
        LocalDate today = LocalDate.now();

        User mockUser = User.builder()
                .userId(userId)
                .email("test@example.com")
                .password("password")
                .username("테스터")
                .receiveNotifications(true)
                .build();

        StudyRoom mockStudyRoom = StudyRoom.builder()
                .studyRoomId(1L)
                .owner(mockUser)
                .roomType(RoomType.PERSONAL)
                .name("자바 스터디")
                .build();

        Problem mockProblem = Problem.builder()
                .problemId(problemId)
                .studyRoom(mockStudyRoom)
                .creator(mockUser)
                .problemType(ProblemType.MCQ)
                .question("Java는 다중 상속을 지원하는가?")
                .explanation("Java는 인터페이스를 통한 다중 구현만 지원하며, 클래스 다중 상속은 지원하지 않습니다.")
                .correctChoiceIndex(0)
                .build();

        ProblemReviewState mockReviewState = ProblemReviewState.builder()
                .user(mockUser)
                .problem(mockProblem)
                .gate(ReviewGate.GATE_2)
                .nextReviewDate(today)
                .reviewCount(1)
                .todayReviewIncludedDate(today)
                .todayReviewIncludedGate(ReviewGate.GATE_2)
                .todayReviewFirstAttemptDate(null)
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(problemRepository.findById(problemId)).willReturn(Optional.of(mockProblem));
        given(problemReviewStateRepository.findByUserAndProblem(mockUser, mockProblem))
                .willReturn(Optional.of(mockReviewState));

        com.ebbinghaus.ttopullae.problem.application.dto.ProblemSubmitCommand command =
                new com.ebbinghaus.ttopullae.problem.application.dto.ProblemSubmitCommand(userId, problemId, answer);

        // When
        com.ebbinghaus.ttopullae.problem.application.dto.ProblemSubmitResult result =
                problemService.submitProblemAnswer(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isCorrect()).isFalse();
        assertThat(result.currentGate()).isEqualTo(ReviewGate.GATE_1);
        assertThat(result.reviewCount()).isEqualTo(2);
        assertThat(result.isFirstAttempt()).isTrue();
        assertThat(result.isReviewStateChanged()).isTrue();

        verify(problemAttemptRepository, times(1)).save(any(ProblemAttempt.class));
    }

    @Test
    @DisplayName("문제 풀이 제출 - 오늘의 복습 문제 재시도 (상태 불변)")
    void submitProblemAnswer_TodayReviewRetry_StateUnchanged() {
        // Given
        Long userId = 1L;
        Long problemId = 3L;
        String answer = "true";
        LocalDate today = LocalDate.now();

        User mockUser = User.builder()
                .userId(userId)
                .email("test@example.com")
                .password("password")
                .username("테스터")
                .receiveNotifications(true)
                .build();

        StudyRoom mockStudyRoom = StudyRoom.builder()
                .studyRoomId(1L)
                .owner(mockUser)
                .roomType(RoomType.PERSONAL)
                .name("자바 스터디")
                .build();

        Problem mockProblem = Problem.builder()
                .problemId(problemId)
                .studyRoom(mockStudyRoom)
                .creator(mockUser)
                .problemType(ProblemType.OX)
                .question("Garbage Collector는 Heap 영역의 메모리를 관리한다.")
                .explanation("Garbage Collector가 Heap 영역의 사용하지 않는 객체를 자동으로 정리합니다.")
                .answerBoolean(true)
                .build();

        // 이미 오늘 첫 시도를 완료한 상태
        ProblemReviewState mockReviewState = ProblemReviewState.builder()
                .user(mockUser)
                .problem(mockProblem)
                .gate(ReviewGate.GATE_2)
                .nextReviewDate(today)
                .reviewCount(2)
                .todayReviewIncludedDate(today)
                .todayReviewIncludedGate(ReviewGate.GATE_2)
                .todayReviewFirstAttemptDate(today)
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(problemRepository.findById(problemId)).willReturn(Optional.of(mockProblem));
        given(problemReviewStateRepository.findByUserAndProblem(mockUser, mockProblem))
                .willReturn(Optional.of(mockReviewState));

        com.ebbinghaus.ttopullae.problem.application.dto.ProblemSubmitCommand command =
                new com.ebbinghaus.ttopullae.problem.application.dto.ProblemSubmitCommand(userId, problemId, answer);

        // When
        com.ebbinghaus.ttopullae.problem.application.dto.ProblemSubmitResult result =
                problemService.submitProblemAnswer(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isCorrect()).isTrue();
        assertThat(result.currentGate()).isEqualTo(ReviewGate.GATE_2);
        assertThat(result.reviewCount()).isEqualTo(2); // 증가하지 않음
        assertThat(result.isFirstAttempt()).isFalse();
        assertThat(result.isReviewStateChanged()).isFalse();

        verify(problemAttemptRepository, times(1)).save(any(ProblemAttempt.class));
    }

    @Test
    @DisplayName("문제 풀이 제출 - 단답형 정답 (대소문자 무시)")
    void submitProblemAnswer_ShortAnswerCorrect_CaseInsensitive() {
        // Given
        Long userId = 1L;
        Long problemId = 4L;
        String answer = "string";
        LocalDate today = LocalDate.now();

        User mockUser = User.builder()
                .userId(userId)
                .email("test@example.com")
                .password("password")
                .username("테스터")
                .receiveNotifications(true)
                .build();

        StudyRoom mockStudyRoom = StudyRoom.builder()
                .studyRoomId(1L)
                .owner(mockUser)
                .roomType(RoomType.PERSONAL)
                .name("자바 스터디")
                .build();

        Problem mockProblem = Problem.builder()
                .problemId(problemId)
                .studyRoom(mockStudyRoom)
                .creator(mockUser)
                .problemType(ProblemType.SHORT)
                .question("자바에서 문자열을 다루는 불변 클래스는?")
                .explanation("String 클래스입니다.")
                .answerText("String")
                .build();

        ProblemReviewState mockReviewState = ProblemReviewState.builder()
                .user(mockUser)
                .problem(mockProblem)
                .gate(ReviewGate.GATE_1)
                .nextReviewDate(today)
                .reviewCount(0)
                .todayReviewIncludedDate(today)
                .todayReviewIncludedGate(ReviewGate.GATE_1)
                .todayReviewFirstAttemptDate(null)
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(problemRepository.findById(problemId)).willReturn(Optional.of(mockProblem));
        given(problemReviewStateRepository.findByUserAndProblem(mockUser, mockProblem))
                .willReturn(Optional.of(mockReviewState));

        com.ebbinghaus.ttopullae.problem.application.dto.ProblemSubmitCommand command =
                new com.ebbinghaus.ttopullae.problem.application.dto.ProblemSubmitCommand(userId, problemId, answer);

        // When
        com.ebbinghaus.ttopullae.problem.application.dto.ProblemSubmitResult result =
                problemService.submitProblemAnswer(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isCorrect()).isTrue();
        assertThat(result.currentGate()).isEqualTo(ReviewGate.GATE_2);
        assertThat(result.isFirstAttempt()).isTrue();
        assertThat(result.isReviewStateChanged()).isTrue();

        verify(problemAttemptRepository, times(1)).save(any(ProblemAttempt.class));
    }

    @Test
    @DisplayName("문제 풀이 제출 - 서술형 AI 채점 정답")
    void submitProblemAnswer_EssayCorrect_AiGrading() {
        // Given
        Long userId = 1L;
        Long problemId = 5L;
        String answer = "IoC는 제어의 역전으로, 객체의 생성과 의존성 관리를 Spring Container가 담당합니다.";
        LocalDate today = LocalDate.now();

        User mockUser = User.builder()
                .userId(userId)
                .email("test@example.com")
                .password("password")
                .username("테스터")
                .receiveNotifications(true)
                .build();

        StudyRoom mockStudyRoom = StudyRoom.builder()
                .studyRoomId(1L)
                .owner(mockUser)
                .roomType(RoomType.PERSONAL)
                .name("스프링 스터디")
                .build();

        Problem mockProblem = Problem.builder()
                .problemId(problemId)
                .studyRoom(mockStudyRoom)
                .creator(mockUser)
                .problemType(ProblemType.SUBJECTIVE)
                .question("Spring IoC에 대해 설명하시오.")
                .explanation("IoC는 객체의 생성과 의존성 관리를 개발자가 아닌 프레임워크(Spring Container)가 담당하는 설계 원칙입니다.")
                .modelAnswerText("IoC는 제어의 역전(Inversion of Control)으로, Spring Container가 객체의 생성과 의존성을 관리합니다.")
                .build();

        ProblemReviewState mockReviewState = ProblemReviewState.builder()
                .user(mockUser)
                .problem(mockProblem)
                .gate(ReviewGate.GATE_1)
                .nextReviewDate(today)
                .reviewCount(0)
                .todayReviewIncludedDate(today)
                .todayReviewIncludedGate(ReviewGate.GATE_1)
                .todayReviewFirstAttemptDate(null)
                .build();

        List<ProblemKeyword> mockKeywords = List.of(
                ProblemKeyword.builder().problem(mockProblem).keyword("제어의 역전").build(),
                ProblemKeyword.builder().problem(mockProblem).keyword("Spring Container").build(),
                ProblemKeyword.builder().problem(mockProblem).keyword("객체 생성").build()
        );

        com.ebbinghaus.ttopullae.problem.application.dto.AiGradingResult aiResult =
                new com.ebbinghaus.ttopullae.problem.application.dto.AiGradingResult(
                        true,
                        "필수 키워드를 모두 포함하고 정확하게 설명하셨습니다.",
                        List.of(),
                        "모든 키워드가 포함되어 있습니다."
                );

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(problemRepository.findById(problemId)).willReturn(Optional.of(mockProblem));
        given(problemReviewStateRepository.findByUserAndProblem(mockUser, mockProblem))
                .willReturn(Optional.of(mockReviewState));
        given(problemKeywordRepository.findByProblem(mockProblem)).willReturn(mockKeywords);
        given(aiGradingService.gradeSubjectiveAnswer(any())).willReturn(aiResult);

        com.ebbinghaus.ttopullae.problem.application.dto.ProblemSubmitCommand command =
                new com.ebbinghaus.ttopullae.problem.application.dto.ProblemSubmitCommand(userId, problemId, answer);

        // When
        com.ebbinghaus.ttopullae.problem.application.dto.ProblemSubmitResult result =
                problemService.submitProblemAnswer(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isCorrect()).isTrue();
        assertThat(result.aiFeedback()).isEqualTo("필수 키워드를 모두 포함하고 정확하게 설명하셨습니다.");
        assertThat(result.currentGate()).isEqualTo(ReviewGate.GATE_2);
        assertThat(result.isFirstAttempt()).isTrue();
        assertThat(result.isReviewStateChanged()).isTrue();

        verify(aiGradingService, times(2)).gradeSubjectiveAnswer(any());
        verify(problemAttemptRepository, times(1)).save(any(ProblemAttempt.class));
    }

    @Test
    @DisplayName("문제 풀이 제출 - 그룹방 타인 문제 첫 풀이 (ReviewState 생성 안 함)")
    void submitProblemAnswer_GroupProblemFirstAttempt_NoReviewState() {
        // Given
        Long userId = 2L;
        Long problemId = 6L;
        String answer = "2";

        User mockUser = User.builder()
                .userId(userId)
                .email("user2@example.com")
                .password("password")
                .username("사용자2")
                .receiveNotifications(true)
                .build();

        User mockOwner = User.builder()
                .userId(1L)
                .email("owner@example.com")
                .password("password")
                .username("방장")
                .receiveNotifications(true)
                .build();

        StudyRoom mockStudyRoom = StudyRoom.builder()
                .studyRoomId(1L)
                .owner(mockOwner)
                .roomType(RoomType.GROUP)
                .name("그룹 스터디")
                .joinCode("ABC123")
                .build();

        Problem mockProblem = Problem.builder()
                .problemId(problemId)
                .studyRoom(mockStudyRoom)
                .creator(mockOwner)
                .problemType(ProblemType.MCQ)
                .question("HTTP 상태 코드 200번대는 무엇을 의미하는가?")
                .explanation("200번대는 성공을 나타냅니다.")
                .correctChoiceIndex(2)
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(problemRepository.findById(problemId)).willReturn(Optional.of(mockProblem));
        given(problemReviewStateRepository.findByUserAndProblem(mockUser, mockProblem))
                .willReturn(Optional.empty());
        given(studyRoomMemberRepository.existsByUserAndStudyRoomAndActive(mockUser, mockStudyRoom, true))
                .willReturn(true);

        com.ebbinghaus.ttopullae.problem.application.dto.ProblemSubmitCommand command =
                new com.ebbinghaus.ttopullae.problem.application.dto.ProblemSubmitCommand(userId, problemId, answer);

        // When
        com.ebbinghaus.ttopullae.problem.application.dto.ProblemSubmitResult result =
                problemService.submitProblemAnswer(command);

        // Then: ReviewState 없이 채점만 수행
        assertThat(result).isNotNull();
        assertThat(result.isCorrect()).isTrue();
        assertThat(result.currentGate()).isNull(); // ReviewState 없음
        assertThat(result.reviewCount()).isNull(); // ReviewState 없음
        assertThat(result.nextReviewDate()).isNull(); // ReviewState 없음
        assertThat(result.isFirstAttempt()).isFalse(); // 오늘의 복습 아님
        assertThat(result.isReviewStateChanged()).isFalse(); // 상태 변화 없음

        // ReviewState는 생성되지 않고, ProblemAttempt만 저장됨
        verify(problemReviewStateRepository, never()).save(any(ProblemReviewState.class));
        verify(problemAttemptRepository, times(1)).save(any(ProblemAttempt.class));
    }

    @Test
    @DisplayName("문제 풀이 제출 - 졸업한 문제 풀이 (상태 불변)")
    void submitProblemAnswer_GraduatedProblem_StateUnchanged() {
        // Given
        Long userId = 1L;
        Long problemId = 7L;
        String answer = "1"; // 오답 (정답은 0)
        LocalDate today = LocalDate.now();

        User mockUser = User.builder()
                .userId(userId)
                .email("test@example.com")
                .password("password")
                .username("테스터")
                .receiveNotifications(true)
                .build();

        StudyRoom mockStudyRoom = StudyRoom.builder()
                .studyRoomId(1L)
                .owner(mockUser)
                .roomType(RoomType.PERSONAL)
                .name("자바 스터디")
                .build();

        Problem mockProblem = Problem.builder()
                .problemId(problemId)
                .studyRoom(mockStudyRoom)
                .creator(mockUser)
                .problemType(ProblemType.MCQ)
                .question("HTTP 응답 코드에서 200번대는 무엇을 의미하는가?")
                .explanation("200번대는 성공을 나타냅니다.")
                .correctChoiceIndex(0)
                .build();

        ProblemReviewState mockReviewState = ProblemReviewState.builder()
                .user(mockUser)
                .problem(mockProblem)
                .gate(ReviewGate.GRADUATED)
                .nextReviewDate(null)
                .reviewCount(3)
                .todayReviewIncludedDate(today.minusDays(10))
                .todayReviewIncludedGate(ReviewGate.GATE_2)
                .todayReviewFirstAttemptDate(today.minusDays(10))
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(problemRepository.findById(problemId)).willReturn(Optional.of(mockProblem));
        given(problemReviewStateRepository.findByUserAndProblem(mockUser, mockProblem))
                .willReturn(Optional.of(mockReviewState));

        com.ebbinghaus.ttopullae.problem.application.dto.ProblemSubmitCommand command =
                new com.ebbinghaus.ttopullae.problem.application.dto.ProblemSubmitCommand(userId, problemId, answer);

        // When
        com.ebbinghaus.ttopullae.problem.application.dto.ProblemSubmitResult result =
                problemService.submitProblemAnswer(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isCorrect()).isFalse(); // 오답이어도
        assertThat(result.currentGate()).isEqualTo(ReviewGate.GRADUATED); // GRADUATED 상태 유지
        assertThat(result.reviewCount()).isEqualTo(3);
        assertThat(result.nextReviewDate()).isNull();
        assertThat(result.isFirstAttempt()).isFalse();
        assertThat(result.isReviewStateChanged()).isFalse();

        verify(problemAttemptRepository, times(1)).save(any(ProblemAttempt.class));
    }

    @Test
    @DisplayName("문제 풀이 제출 - 사용자 없음 예외")
    void submitProblemAnswer_UserNotFound() {
        // Given
        Long userId = 999L;
        Long problemId = 1L;
        String answer = "0";

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        com.ebbinghaus.ttopullae.problem.application.dto.ProblemSubmitCommand command =
                new com.ebbinghaus.ttopullae.problem.application.dto.ProblemSubmitCommand(userId, problemId, answer);

        // When & Then
        assertThatThrownBy(() -> problemService.submitProblemAnswer(command))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", UserException.USER_NOT_FOUND);

        verify(problemRepository, never()).findById(any());
    }

    @Test
    @DisplayName("문제 풀이 제출 - 문제 없음 예외")
    void submitProblemAnswer_ProblemNotFound() {
        // Given
        Long userId = 1L;
        Long problemId = 999L;
        String answer = "0";

        User mockUser = User.builder()
                .userId(userId)
                .email("test@example.com")
                .password("password")
                .username("테스터")
                .receiveNotifications(true)
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(problemRepository.findById(problemId)).willReturn(Optional.empty());

        com.ebbinghaus.ttopullae.problem.application.dto.ProblemSubmitCommand command =
                new com.ebbinghaus.ttopullae.problem.application.dto.ProblemSubmitCommand(userId, problemId, answer);

        // When & Then
        assertThatThrownBy(() -> problemService.submitProblemAnswer(command))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", ProblemException.PROBLEM_NOT_FOUND);

        verify(problemReviewStateRepository, never()).findByUserAndProblem(any(), any());
    }

    @Test
    @DisplayName("문제 상세 조회 성공 - 객관식 문제, 복습 상태 있음")
    void getProblemDetail_Success_McqWithReviewState() {
        // Given
        Long userId = 1L;
        Long problemId = 1L;

        User mockUser = User.builder()
                .userId(userId)
                .email("test@example.com")
                .password("password")
                .username("테스터")
                .receiveNotifications(true)
                .build();

        StudyRoom mockStudyRoom = StudyRoom.builder()
                .studyRoomId(1L)
                .owner(mockUser)
                .roomType(RoomType.PERSONAL)
                .name("자바 스터디")
                .build();

        Problem mockProblem = Problem.builder()
                .problemId(problemId)
                .studyRoom(mockStudyRoom)
                .creator(mockUser)
                .problemType(ProblemType.MCQ)
                .question("자바의 접근 제어자가 아닌 것은?")
                .explanation("friend는 C++의 접근 제어자입니다.")
                .correctChoiceIndex(3)
                .build();

        List<ProblemChoice> choices = List.of(
                ProblemChoice.builder().choiceText("public").build(),
                ProblemChoice.builder().choiceText("private").build(),
                ProblemChoice.builder().choiceText("protected").build(),
                ProblemChoice.builder().choiceText("friend").build()
        );

        ProblemReviewState mockReviewState = ProblemReviewState.builder()
                .user(mockUser)
                .problem(mockProblem)
                .gate(ReviewGate.GATE_1)
                .nextReviewDate(LocalDate.now().plusDays(1))
                .reviewCount(0)
                .includeInReview(true)
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(problemRepository.findById(problemId)).willReturn(Optional.of(mockProblem));
        given(problemChoiceRepository.findByProblem(mockProblem)).willReturn(choices);
        given(problemReviewStateRepository.findByUserAndProblem(mockUser, mockProblem))
                .willReturn(Optional.of(mockReviewState));

        // When
        com.ebbinghaus.ttopullae.problem.application.dto.ProblemDetailResult result =
                problemService.getProblemDetail(userId, problemId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.problemId()).isEqualTo(problemId);
        assertThat(result.question()).isEqualTo("자바의 접근 제어자가 아닌 것은?");
        assertThat(result.problemType()).isEqualTo(ProblemType.MCQ);
        assertThat(result.choices()).hasSize(4);
        assertThat(result.choices()).containsExactly("public", "private", "protected", "friend");
        assertThat(result.currentGate()).isEqualTo(ReviewGate.GATE_1);
        assertThat(result.reviewCount()).isEqualTo(0);
        assertThat(result.includeInReview()).isTrue();

        verify(userRepository, times(1)).findById(userId);
        verify(problemRepository, times(1)).findById(problemId);
        verify(problemChoiceRepository, times(1)).findByProblem(mockProblem);
        verify(problemReviewStateRepository, times(1)).findByUserAndProblem(mockUser, mockProblem);
    }

    @Test
    @DisplayName("문제 상세 조회 성공 - 단답형 문제, 복습 상태 없음")
    void getProblemDetail_Success_ShortWithoutReviewState() {
        // Given
        Long userId = 1L;
        Long problemId = 2L;

        User mockUser = User.builder()
                .userId(userId)
                .email("test@example.com")
                .password("password")
                .username("테스터")
                .receiveNotifications(true)
                .build();

        User otherUser = User.builder()
                .userId(2L)
                .email("other@example.com")
                .password("password")
                .username("다른사람")
                .receiveNotifications(true)
                .build();

        StudyRoom mockStudyRoom = StudyRoom.builder()
                .studyRoomId(1L)
                .owner(otherUser)
                .roomType(RoomType.GROUP)
                .name("그룹 스터디")
                .build();

        Problem mockProblem = Problem.builder()
                .problemId(problemId)
                .studyRoom(mockStudyRoom)
                .creator(otherUser)
                .problemType(ProblemType.SHORT)
                .question("자바에서 문자열을 다루는 불변 클래스는?")
                .explanation("String 클래스입니다.")
                .answerText("String")
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(problemRepository.findById(problemId)).willReturn(Optional.of(mockProblem));
        given(studyRoomMemberRepository.existsByUserAndStudyRoomAndActive(mockUser, mockStudyRoom, true))
                .willReturn(true);
        given(problemReviewStateRepository.findByUserAndProblem(mockUser, mockProblem))
                .willReturn(Optional.empty());

        // When
        com.ebbinghaus.ttopullae.problem.application.dto.ProblemDetailResult result =
                problemService.getProblemDetail(userId, problemId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.problemId()).isEqualTo(problemId);
        assertThat(result.question()).isEqualTo("자바에서 문자열을 다루는 불변 클래스는?");
        assertThat(result.problemType()).isEqualTo(ProblemType.SHORT);
        assertThat(result.choices()).isNull();
        assertThat(result.currentGate()).isNull();
        assertThat(result.nextReviewDate()).isNull();
        assertThat(result.reviewCount()).isNull();
        assertThat(result.includeInReview()).isNull();

        verify(problemChoiceRepository, never()).findByProblem(any());
    }

    @Test
    @DisplayName("문제 상세 조회 실패 - 사용자 없음")
    void getProblemDetail_UserNotFound() {
        // Given
        Long userId = 999L;
        Long problemId = 1L;

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> problemService.getProblemDetail(userId, problemId))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", UserException.USER_NOT_FOUND);

        verify(problemRepository, never()).findById(any());
    }

    @Test
    @DisplayName("문제 상세 조회 실패 - 문제 없음")
    void getProblemDetail_ProblemNotFound() {
        // Given
        Long userId = 1L;
        Long problemId = 999L;

        User mockUser = User.builder()
                .userId(userId)
                .email("test@example.com")
                .password("password")
                .username("테스터")
                .receiveNotifications(true)
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(problemRepository.findById(problemId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> problemService.getProblemDetail(userId, problemId))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", ProblemException.PROBLEM_NOT_FOUND);

        verify(problemChoiceRepository, never()).findByProblem(any());
    }

    @Test
    @DisplayName("문제 상세 조회 실패 - 스터디룸 접근 권한 없음")
    void getProblemDetail_AccessDenied() {
        // Given
        Long userId = 1L;
        Long problemId = 1L;

        User mockUser = User.builder()
                .userId(userId)
                .email("test@example.com")
                .password("password")
                .username("테스터")
                .receiveNotifications(true)
                .build();

        User otherUser = User.builder()
                .userId(2L)
                .email("other@example.com")
                .password("password")
                .username("다른사람")
                .receiveNotifications(true)
                .build();

        StudyRoom mockStudyRoom = StudyRoom.builder()
                .studyRoomId(1L)
                .owner(otherUser)
                .roomType(RoomType.GROUP)
                .name("그룹 스터디")
                .build();

        Problem mockProblem = Problem.builder()
                .problemId(problemId)
                .studyRoom(mockStudyRoom)
                .creator(otherUser)
                .problemType(ProblemType.MCQ)
                .question("테스트 문제")
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(problemRepository.findById(problemId)).willReturn(Optional.of(mockProblem));
        given(studyRoomMemberRepository.existsByUserAndStudyRoomAndActive(mockUser, mockStudyRoom, true))
                .willReturn(false);

        // When & Then
        assertThatThrownBy(() -> problemService.getProblemDetail(userId, problemId))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", ProblemException.ROOM_ACCESS_DENIED);

        verify(problemChoiceRepository, never()).findByProblem(any());
        verify(problemReviewStateRepository, never()).findByUserAndProblem(any(), any());
    }
}
