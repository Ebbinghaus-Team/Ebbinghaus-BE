package com.ebbinghaus.ttopullae.problem.application;

import com.ebbinghaus.ttopullae.global.exception.ApplicationException;
import com.ebbinghaus.ttopullae.problem.application.dto.ProblemCreateCommand;
import com.ebbinghaus.ttopullae.problem.application.dto.ProblemCreateResult;
import com.ebbinghaus.ttopullae.problem.domain.*;
import com.ebbinghaus.ttopullae.problem.domain.repository.ProblemChoiceRepository;
import com.ebbinghaus.ttopullae.problem.domain.repository.ProblemKeywordRepository;
import com.ebbinghaus.ttopullae.problem.domain.repository.ProblemRepository;
import com.ebbinghaus.ttopullae.problem.domain.repository.ProblemReviewStateRepository;
import com.ebbinghaus.ttopullae.problem.exception.ProblemException;
import com.ebbinghaus.ttopullae.studyroom.domain.RoomType;
import com.ebbinghaus.ttopullae.studyroom.domain.StudyRoom;
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
    private StudyRoomRepository studyRoomRepository;

    @Mock
    private UserRepository userRepository;

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
}
