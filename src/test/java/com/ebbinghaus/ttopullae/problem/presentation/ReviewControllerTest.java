package com.ebbinghaus.ttopullae.problem.presentation;

import com.ebbinghaus.ttopullae.global.util.JwtTokenProvider;
import com.ebbinghaus.ttopullae.problem.domain.Problem;
import com.ebbinghaus.ttopullae.problem.domain.ProblemAttempt;
import com.ebbinghaus.ttopullae.problem.domain.ProblemReviewState;
import com.ebbinghaus.ttopullae.problem.domain.ProblemType;
import com.ebbinghaus.ttopullae.problem.domain.ReviewGate;
import com.ebbinghaus.ttopullae.problem.domain.repository.ProblemAttemptRepository;
import com.ebbinghaus.ttopullae.problem.domain.repository.ProblemRepository;
import com.ebbinghaus.ttopullae.problem.domain.repository.ProblemReviewStateRepository;
import com.ebbinghaus.ttopullae.studyroom.domain.RoomType;
import com.ebbinghaus.ttopullae.studyroom.domain.StudyRoom;
import com.ebbinghaus.ttopullae.studyroom.domain.repository.StudyRoomRepository;
import com.ebbinghaus.ttopullae.user.domain.User;
import com.ebbinghaus.ttopullae.user.domain.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudyRoomRepository studyRoomRepository;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private ProblemReviewStateRepository problemReviewStateRepository;

    @Autowired
    private ProblemAttemptRepository problemAttemptRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User testUser;
    private StudyRoom testStudyRoom;
    private String accessToken;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = User.builder()
                .email("test@example.com")
                .password("password123")
                .username("테스트유저")
                .receiveNotifications(true)
                .build();
        userRepository.save(testUser);

        // 테스트용 스터디룸 생성
        testStudyRoom = StudyRoom.builder()
                .owner(testUser)
                .roomType(RoomType.PERSONAL)
                .name("자바 스터디")
                .description("자바 개념 정리")
                .category("프로그래밍")
                .build();
        studyRoomRepository.save(testStudyRoom);

        // JWT 토큰 생성
        accessToken = jwtTokenProvider.generateToken(testUser.getUserId());
    }

    @Test
    @DisplayName("오늘의 복습 문제 조회 성공 - 필터 ALL")
    void getTodayReviewProblems_Success_FilterAll() throws Exception {
        // given
        LocalDate today = LocalDate.now();

        // 문제 1: GATE_1, 오늘 복습 대상
        Problem problem1 = Problem.builder()
                .studyRoom(testStudyRoom)
                .creator(testUser)
                .problemType(ProblemType.MCQ)
                .question("자바의 접근 제어자가 아닌 것은?")
                .explanation("friend는 C++의 접근 제어자입니다.")
                .correctChoiceIndex(3)
                .build();
        problemRepository.save(problem1);

        ProblemReviewState reviewState1 = ProblemReviewState.builder()
                .user(testUser)
                .problem(problem1)
                .gate(ReviewGate.GATE_1)
                .nextReviewDate(today)
                .reviewCount(0)
                .build();
        problemReviewStateRepository.save(reviewState1);

        // 문제 2: GATE_2, 오늘 복습 대상이며 이미 완료
        Problem problem2 = Problem.builder()
                .studyRoom(testStudyRoom)
                .creator(testUser)
                .problemType(ProblemType.SHORT)
                .question("JPA의 영속성 컨텍스트란?")
                .explanation("EntityManager가 관리하는 엔티티 집합입니다.")
                .answerText("영속성 컨텍스트")
                .build();
        problemRepository.save(problem2);

        ProblemReviewState reviewState2 = ProblemReviewState.builder()
                .user(testUser)
                .problem(problem2)
                .gate(ReviewGate.GATE_2)
                .nextReviewDate(today)
                .reviewCount(1)
                .todayReviewFirstAttemptDate(today)
                .build();
        problemReviewStateRepository.save(reviewState2);

        // 문제 2의 오늘 풀이 기록 생성 (정답)
        ProblemAttempt attempt2 = ProblemAttempt.builder()
                .user(testUser)
                .problem(problem2)
                .isCorrect(true)
                .build();
        problemAttemptRepository.save(attempt2);

        // 스냅샷 생성 (배치 시뮬레이션)
        problemReviewStateRepository.snapshotTodayReviewProblems(today);

        // when & then
        mockMvc.perform(get("/api/review/today")
                        .cookie(new Cookie("accessToken", accessToken))
                        .param("filter", "ALL"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dashboard").exists())
                .andExpect(jsonPath("$.dashboard.totalCount").value(2))
                .andExpect(jsonPath("$.dashboard.completedCount").value(1))
                .andExpect(jsonPath("$.dashboard.incompletedCount").value(1))
                .andExpect(jsonPath("$.dashboard.progressRate").value(50.0))
                .andExpect(jsonPath("$.problems").isArray())
                .andExpect(jsonPath("$.problems.length()").value(2))
                .andExpect(jsonPath("$.problems[0].problemId").value(problem1.getProblemId()))
                .andExpect(jsonPath("$.problems[0].question").value("자바의 접근 제어자가 아닌 것은?"))
                .andExpect(jsonPath("$.problems[0].problemType").value("MCQ"))
                .andExpect(jsonPath("$.problems[0].gate").value("GATE_1"))
                .andExpect(jsonPath("$.problems[0].attemptStatus").value("NOT_ATTEMPTED"))
                .andExpect(jsonPath("$.problems[1].problemId").value(problem2.getProblemId()))
                .andExpect(jsonPath("$.problems[1].problemType").value("SHORT"))
                .andExpect(jsonPath("$.problems[1].gate").value("GATE_2"))
                .andExpect(jsonPath("$.problems[1].attemptStatus").value("CORRECT"));
    }

    @Test
    @DisplayName("오늘의 복습 문제 조회 성공 - 필터 GATE_1")
    void getTodayReviewProblems_Success_FilterGate1() throws Exception {
        // given
        LocalDate today = LocalDate.now();

        // GATE_1 문제
        Problem problem1 = Problem.builder()
                .studyRoom(testStudyRoom)
                .creator(testUser)
                .problemType(ProblemType.MCQ)
                .question("자바의 접근 제어자가 아닌 것은?")
                .explanation("friend는 C++의 접근 제어자입니다.")
                .correctChoiceIndex(3)
                .build();
        problemRepository.save(problem1);

        ProblemReviewState reviewState1 = ProblemReviewState.builder()
                .user(testUser)
                .problem(problem1)
                .gate(ReviewGate.GATE_1)
                .nextReviewDate(today)
                .reviewCount(0)
                .build();
        problemReviewStateRepository.save(reviewState1);

        // GATE_2 문제 (필터링되어야 함)
        Problem problem2 = Problem.builder()
                .studyRoom(testStudyRoom)
                .creator(testUser)
                .problemType(ProblemType.SHORT)
                .question("JPA의 영속성 컨텍스트란?")
                .explanation("EntityManager가 관리하는 엔티티 집합입니다.")
                .answerText("영속성 컨텍스트")
                .build();
        problemRepository.save(problem2);

        ProblemReviewState reviewState2 = ProblemReviewState.builder()
                .user(testUser)
                .problem(problem2)
                .gate(ReviewGate.GATE_2)
                .nextReviewDate(today)
                .reviewCount(1)
                .build();
        problemReviewStateRepository.save(reviewState2);

        // 스냅샷 생성 (배치 시뮬레이션)
        problemReviewStateRepository.snapshotTodayReviewProblems(today);

        // when & then
        mockMvc.perform(get("/api/review/today")
                        .cookie(new Cookie("accessToken", accessToken))
                        .param("filter", "GATE_1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dashboard.totalCount").value(1))
                .andExpect(jsonPath("$.problems.length()").value(1))
                .andExpect(jsonPath("$.problems[0].gate").value("GATE_1"));
    }

    @Test
    @DisplayName("오늘의 복습 문제 조회 성공 - 필터 GATE_2")
    void getTodayReviewProblems_Success_FilterGate2() throws Exception {
        // given
        LocalDate today = LocalDate.now();

        // GATE_1 문제 (필터링되어야 함)
        Problem problem1 = Problem.builder()
                .studyRoom(testStudyRoom)
                .creator(testUser)
                .problemType(ProblemType.MCQ)
                .question("자바의 접근 제어자가 아닌 것은?")
                .explanation("friend는 C++의 접근 제어자입니다.")
                .correctChoiceIndex(3)
                .build();
        problemRepository.save(problem1);

        ProblemReviewState reviewState1 = ProblemReviewState.builder()
                .user(testUser)
                .problem(problem1)
                .gate(ReviewGate.GATE_1)
                .nextReviewDate(today)
                .reviewCount(0)
                .build();
        problemReviewStateRepository.save(reviewState1);

        // GATE_2 문제
        Problem problem2 = Problem.builder()
                .studyRoom(testStudyRoom)
                .creator(testUser)
                .problemType(ProblemType.SHORT)
                .question("JPA의 영속성 컨텍스트란?")
                .explanation("EntityManager가 관리하는 엔티티 집합입니다.")
                .answerText("영속성 컨텍스트")
                .build();
        problemRepository.save(problem2);

        ProblemReviewState reviewState2 = ProblemReviewState.builder()
                .user(testUser)
                .problem(problem2)
                .gate(ReviewGate.GATE_2)
                .nextReviewDate(today)
                .reviewCount(1)
                .build();
        problemReviewStateRepository.save(reviewState2);

        // 스냅샷 생성 (배치 시뮬레이션)
        problemReviewStateRepository.snapshotTodayReviewProblems(today);

        // when & then
        mockMvc.perform(get("/api/review/today")
                        .cookie(new Cookie("accessToken", accessToken))
                        .param("filter", "GATE_2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dashboard.totalCount").value(1))
                .andExpect(jsonPath("$.problems.length()").value(1))
                .andExpect(jsonPath("$.problems[0].gate").value("GATE_2"));
    }

    @Test
    @DisplayName("오늘의 복습 문제 조회 성공 - 빈 목록")
    void getTodayReviewProblems_Success_EmptyList() throws Exception {
        // given
        // 복습 대상 문제가 없는 상태

        // when & then
        mockMvc.perform(get("/api/review/today")
                        .cookie(new Cookie("accessToken", accessToken))
                        .param("filter", "ALL"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dashboard.totalCount").value(0))
                .andExpect(jsonPath("$.dashboard.completedCount").value(0))
                .andExpect(jsonPath("$.dashboard.incompletedCount").value(0))
                .andExpect(jsonPath("$.dashboard.progressRate").value(0.0))
                .andExpect(jsonPath("$.problems").isArray())
                .andExpect(jsonPath("$.problems.length()").value(0));
    }

    @Test
    @DisplayName("오늘의 복습 문제 조회 성공 - 기본 필터값 (파라미터 없음)")
    void getTodayReviewProblems_Success_DefaultFilter() throws Exception {
        // given
        LocalDate today = LocalDate.now();

        Problem problem1 = Problem.builder()
                .studyRoom(testStudyRoom)
                .creator(testUser)
                .problemType(ProblemType.OX)
                .question("JVM은 Java Virtual Machine의 약자이다.")
                .explanation("맞습니다.")
                .answerBoolean(true)
                .build();
        problemRepository.save(problem1);

        ProblemReviewState reviewState1 = ProblemReviewState.builder()
                .user(testUser)
                .problem(problem1)
                .gate(ReviewGate.GATE_1)
                .nextReviewDate(today)
                .reviewCount(0)
                .build();
        problemReviewStateRepository.save(reviewState1);

        // 스냅샷 생성 (배치 시뮬레이션)
        problemReviewStateRepository.snapshotTodayReviewProblems(today);

        // when & then - filter 파라미터를 생략하면 defaultValue="ALL" 적용됨
        mockMvc.perform(get("/api/review/today")
                        .cookie(new Cookie("accessToken", accessToken)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dashboard.totalCount").value(1))
                .andExpect(jsonPath("$.problems.length()").value(1));
    }

    @Test
    @DisplayName("오늘의 복습 문제 조회 실패 - 잘못된 필터 값")
    void getTodayReviewProblems_Fail_InvalidFilter() throws Exception {
        // when & then
        mockMvc.perform(get("/api/review/today")
                        .cookie(new Cookie("accessToken", accessToken))
                        .param("filter", "INVALID_FILTER"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("오늘의 복습 문제 조회 실패 - 인증 토큰 없음")
    void getTodayReviewProblems_Fail_NoToken() throws Exception {
        // when & then
        mockMvc.perform(get("/api/review/today")
                        .param("filter", "ALL"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("오늘의 복습 문제 조회 성공 - 졸업한 문제도 오늘 날짜이면 포함")
    void getTodayReviewProblems_Success_IncludeGraduatedToday() throws Exception {
        // given
        LocalDate today = LocalDate.now();

        // 오늘 졸업한 문제
        Problem graduatedProblem = Problem.builder()
                .studyRoom(testStudyRoom)
                .creator(testUser)
                .problemType(ProblemType.OX)
                .question("졸업 테스트 문제")
                .explanation("오늘 졸업한 문제입니다.")
                .answerBoolean(true)
                .build();
        problemRepository.save(graduatedProblem);

        ProblemReviewState graduatedState = ProblemReviewState.builder()
                .user(testUser)
                .problem(graduatedProblem)
                .gate(ReviewGate.GRADUATED)
                .nextReviewDate(today.plusYears(100)) // 졸업 상태이므로 먼 미래 날짜 설정
                .reviewCount(3)
                .todayReviewIncludedDate(today)
                .todayReviewIncludedGate(ReviewGate.GATE_2)
                .todayReviewFirstAttemptDate(today)
                .build();
        problemReviewStateRepository.save(graduatedState);

        // when & then
        mockMvc.perform(get("/api/review/today")
                        .cookie(new Cookie("accessToken", accessToken))
                        .param("filter", "ALL"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dashboard.totalCount").value(1))
                .andExpect(jsonPath("$.dashboard.completedCount").value(1))
                .andExpect(jsonPath("$.problems.length()").value(1))
                .andExpect(jsonPath("$.problems[0].problemId").value(graduatedProblem.getProblemId()))
                .andExpect(jsonPath("$.problems[0].gate").value("GATE_2"));  // 스냅샷 시점의 gate 반환

        // 데이터베이스 검증
        assertThat(problemReviewStateRepository.count()).isEqualTo(1);
    }
}