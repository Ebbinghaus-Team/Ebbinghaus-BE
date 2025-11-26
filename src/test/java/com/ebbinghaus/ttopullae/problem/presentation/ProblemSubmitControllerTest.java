package com.ebbinghaus.ttopullae.problem.presentation;

import com.ebbinghaus.ttopullae.global.util.JwtTokenProvider;
import com.ebbinghaus.ttopullae.problem.application.AiGradingService;
import com.ebbinghaus.ttopullae.problem.domain.*;
import com.ebbinghaus.ttopullae.problem.domain.repository.*;
import com.ebbinghaus.ttopullae.studyroom.domain.RoomType;
import com.ebbinghaus.ttopullae.studyroom.domain.StudyRoom;
import com.ebbinghaus.ttopullae.studyroom.domain.StudyRoomMember;
import com.ebbinghaus.ttopullae.studyroom.domain.repository.StudyRoomMemberRepository;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProblemSubmitControllerTest {

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
    private ProblemChoiceRepository problemChoiceRepository;

    @Autowired
    private ProblemAttemptRepository problemAttemptRepository;

    @Autowired
    private StudyRoomMemberRepository studyRoomMemberRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private AiGradingService aiGradingService;

    private User testUser;
    private StudyRoom testStudyRoom;
    private String accessToken;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        today = LocalDate.now();

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
    @DisplayName("객관식 문제 풀이 - 오늘의 복습 첫 시도 정답 (GATE_1 → GATE_2 승급)")
    void submitMcqProblem_TodayReviewFirstAttemptCorrect_Promotion() throws Exception {
        // Given: 오늘의 복습 문제 (GATE_1, 첫 시도 대상)
        Problem problem = createMcqProblem();
        ProblemReviewState reviewState = ProblemReviewState.builder()
                .user(testUser)
                .problem(problem)
                .gate(ReviewGate.GATE_1)
                .nextReviewDate(today)
                .reviewCount(0)
                .todayReviewIncludedDate(today)
                .todayReviewIncludedGate(ReviewGate.GATE_1)
                .todayReviewFirstAttemptDate(null)
                .build();
        problemReviewStateRepository.save(reviewState);

        Map<String, Object> request = new HashMap<>();
        request.put("answer", "3");  // 정답

        // When & Then
        mockMvc.perform(post("/api/{problemId}/submit", problem.getProblemId())
                        .cookie(new Cookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isCorrect").value(true))
                .andExpect(jsonPath("$.explanation").value("public은 모든 클래스에서 접근 가능합니다."))
                .andExpect(jsonPath("$.aiFeedback").isEmpty())
                .andExpect(jsonPath("$.currentGate").value("GATE_2"))
                .andExpect(jsonPath("$.reviewCount").value(1))
                .andExpect(jsonPath("$.nextReviewDate").value(today.plusDays(7).toString()))
                .andExpect(jsonPath("$.isFirstAttempt").value(true))
                .andExpect(jsonPath("$.isReviewStateChanged").value(true));

        // DB 검증
        ProblemReviewState updatedState = problemReviewStateRepository.findByUserAndProblem(testUser, problem).get();
        assertThat(updatedState.getGate()).isEqualTo(ReviewGate.GATE_2);
        assertThat(updatedState.getReviewCount()).isEqualTo(1);
        assertThat(problemAttemptRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("OX 문제 풀이 - 오늘의 복습 첫 시도 오답 (GATE_2 → GATE_1 강등)")
    void submitOxProblem_TodayReviewFirstAttemptWrong_Demotion() throws Exception {
        // Given: 오늘의 복습 문제 (GATE_2, 첫 시도 대상)
        Problem problem = createOxProblem();
        ProblemReviewState reviewState = ProblemReviewState.builder()
                .user(testUser)
                .problem(problem)
                .gate(ReviewGate.GATE_2)
                .nextReviewDate(today)
                .reviewCount(1)
                .todayReviewIncludedDate(today)
                .todayReviewIncludedGate(ReviewGate.GATE_2)
                .todayReviewFirstAttemptDate(null)
                .build();
        problemReviewStateRepository.save(reviewState);

        Map<String, Object> request = new HashMap<>();
        request.put("answer", "true");  // 오답 (정답은 false)

        // When & Then
        mockMvc.perform(post("/api/{problemId}/submit", problem.getProblemId())
                        .cookie(new Cookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isCorrect").value(false))
                .andExpect(jsonPath("$.currentGate").value("GATE_1"))
                .andExpect(jsonPath("$.reviewCount").value(2))
                .andExpect(jsonPath("$.nextReviewDate").value(today.plusDays(1).toString()))
                .andExpect(jsonPath("$.isFirstAttempt").value(true))
                .andExpect(jsonPath("$.isReviewStateChanged").value(true));

        // DB 검증
        ProblemReviewState updatedState = problemReviewStateRepository.findByUserAndProblem(testUser, problem).get();
        assertThat(updatedState.getGate()).isEqualTo(ReviewGate.GATE_1);
    }

    @Test
    @DisplayName("단답형 문제 풀이 - 오늘의 복습 재시도 (상태 불변)")
    void submitShortProblem_TodayReviewRetry_NoStateChange() throws Exception {
        // Given: 오늘의 복습 문제 (이미 첫 시도 완료)
        Problem problem = createShortProblem();
        ProblemReviewState reviewState = ProblemReviewState.builder()
                .user(testUser)
                .problem(problem)
                .gate(ReviewGate.GATE_2)
                .nextReviewDate(today)
                .reviewCount(2)
                .todayReviewIncludedDate(today)
                .todayReviewIncludedGate(ReviewGate.GATE_2)
                .todayReviewFirstAttemptDate(today)  // 이미 오늘 첫 시도 완료
                .build();
        problemReviewStateRepository.save(reviewState);

        Map<String, Object> request = new HashMap<>();
        request.put("answer", "Garbage Collector");  // 정답

        // When & Then
        mockMvc.perform(post("/api/{problemId}/submit", problem.getProblemId())
                        .cookie(new Cookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isCorrect").value(true))
                .andExpect(jsonPath("$.currentGate").value("GATE_2"))
                .andExpect(jsonPath("$.reviewCount").value(2))  // 변화 없음
                .andExpect(jsonPath("$.isFirstAttempt").value(false))
                .andExpect(jsonPath("$.isReviewStateChanged").value(false));

        // DB 검증 - 상태 불변
        ProblemReviewState updatedState = problemReviewStateRepository.findByUserAndProblem(testUser, problem).get();
        assertThat(updatedState.getGate()).isEqualTo(ReviewGate.GATE_2);
        assertThat(updatedState.getReviewCount()).isEqualTo(2);
    }

    // 졸업 문제 테스트는 단위 테스트에서 커버됨

    @Test
    @DisplayName("그룹방 타인 문제 첫 풀이 - ReviewState 생성하지 않음")
    void submitProblem_GroupOtherUserProblem_NoReviewStateCreation() throws Exception {
        // Given: 다른 사용자의 그룹방 문제 (ReviewState 없음)
        User otherUser = User.builder()
                .email("other@example.com")
                .password("password")
                .username("다른유저")
                .receiveNotifications(false)
                .build();
        userRepository.save(otherUser);

        StudyRoom groupRoom = StudyRoom.builder()
                .owner(otherUser)
                .roomType(RoomType.GROUP)
                .name("그룹 스터디")
                .description("그룹 학습")
                .category("개발")
                .joinCode("GROUP123")
                .build();
        studyRoomRepository.save(groupRoom);

        // testUser를 그룹 멤버로 추가
        StudyRoomMember member = StudyRoomMember.builder()
                .user(testUser)
                .studyRoom(groupRoom)
                .active(true)
                .build();
        studyRoomMemberRepository.save(member);

        Problem problem = Problem.builder()
                .creator(otherUser)
                .studyRoom(groupRoom)
                .problemType(ProblemType.MCQ)
                .question("테스트 문제")
                .explanation("해설")
                .correctChoiceIndex(1)
                .build();
        problemRepository.save(problem);

        createChoices(problem);

        // ReviewState 없음 확인
        assertThat(problemReviewStateRepository.findByUserAndProblem(testUser, problem)).isEmpty();

        Map<String, Object> request = new HashMap<>();
        request.put("answer", "1");  // 정답

        // When & Then
        mockMvc.perform(post("/api/{problemId}/submit", problem.getProblemId())
                        .cookie(new Cookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isCorrect").value(true))
                .andExpect(jsonPath("$.explanation").value("해설"))
                .andExpect(jsonPath("$.currentGate").isEmpty())  // null
                .andExpect(jsonPath("$.reviewCount").isEmpty())  // null
                .andExpect(jsonPath("$.nextReviewDate").isEmpty())  // null
                .andExpect(jsonPath("$.isFirstAttempt").value(false))
                .andExpect(jsonPath("$.isReviewStateChanged").value(false));

        // ReviewState 생성되지 않음 확인
        assertThat(problemReviewStateRepository.findByUserAndProblem(testUser, problem)).isEmpty();

        // ProblemAttempt는 생성됨 확인 (풀이 기록)
        assertThat(problemAttemptRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("문제 풀이 실패 - 답안 누락")
    void submitProblem_Fail_MissingAnswer() throws Exception {
        // Given
        Problem problem = createMcqProblem();

        Map<String, Object> request = new HashMap<>();
        // answer 필드 누락

        // When & Then
        mockMvc.perform(post("/api/{problemId}/submit", problem.getProblemId())
                        .cookie(new Cookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("문제 풀이 실패 - JWT 토큰 없음")
    void submitProblem_Fail_NoToken() throws Exception {
        // Given
        Problem problem = createMcqProblem();

        Map<String, Object> request = new HashMap<>();
        request.put("answer", "3");

        // When & Then
        mockMvc.perform(post("/api/{problemId}/submit", problem.getProblemId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("문제 풀이 실패 - 존재하지 않는 문제")
    void submitProblem_Fail_ProblemNotFound() throws Exception {
        // Given
        Map<String, Object> request = new HashMap<>();
        request.put("answer", "3");

        // When & Then
        mockMvc.perform(post("/api/{problemId}/submit", 99999L)
                        .cookie(new Cookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("문제를 찾을 수 없음"));
    }

    // Helper methods
    private Problem createMcqProblem() {
        Problem problem = Problem.builder()
                .creator(testUser)
                .studyRoom(testStudyRoom)
                .problemType(ProblemType.MCQ)
                .question("Java의 접근 제어자 중 가장 넓은 범위는?")
                .explanation("public은 모든 클래스에서 접근 가능합니다.")
                .correctChoiceIndex(3)
                .build();
        problemRepository.save(problem);
        createChoices(problem);
        return problem;
    }

    private Problem createOxProblem() {
        Problem problem = Problem.builder()
                .creator(testUser)
                .studyRoom(testStudyRoom)
                .problemType(ProblemType.OX)
                .question("Java는 다중 상속을 지원한다.")
                .explanation("Java는 인터페이스를 통한 다중 구현만 지원하며, 클래스 다중 상속은 지원하지 않습니다.")
                .answerBoolean(false)
                .build();
        problemRepository.save(problem);
        return problem;
    }

    private Problem createShortProblem() {
        Problem problem = Problem.builder()
                .creator(testUser)
                .studyRoom(testStudyRoom)
                .problemType(ProblemType.SHORT)
                .question("Java의 가비지 컬렉션을 담당하는 JVM 구성 요소는?")
                .explanation("Garbage Collector가 Heap 영역의 사용하지 않는 객체를 자동으로 정리합니다.")
                .answerText("Garbage Collector")
                .build();
        problemRepository.save(problem);
        return problem;
    }

    @Test
    @DisplayName("첫 시도 우선 법칙 - GATE_1 틀림 후 당일 재시도 성공해도 강등 상태 유지")
    void submitProblem_Gate1_FailThenSuccessSameDay_StayGate1() throws Exception {
        // Given: GATE_1, 오늘의 복습 문제
        Problem problem = createMcqProblem();
        ProblemReviewState reviewState = ProblemReviewState.builder()
                .user(testUser)
                .problem(problem)
                .gate(ReviewGate.GATE_1)
                .nextReviewDate(today)
                .reviewCount(0)
                .todayReviewIncludedDate(today)
                .todayReviewIncludedGate(ReviewGate.GATE_1)
                .todayReviewFirstAttemptDate(null)  // 아직 첫 시도 안 함
                .build();
        problemReviewStateRepository.save(reviewState);

        // When 1: 첫 시도 - 틀림
        Map<String, Object> wrongRequest = new HashMap<>();
        wrongRequest.put("answer", "1");  // 오답 (정답은 3)

        mockMvc.perform(post("/api/{problemId}/submit", problem.getProblemId())
                        .cookie(new Cookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isCorrect").value(false))
                .andExpect(jsonPath("$.currentGate").value("GATE_1"))  // 유지
                .andExpect(jsonPath("$.reviewCount").value(1))
                .andExpect(jsonPath("$.nextReviewDate").value(today.plusDays(1).toString()))
                .andExpect(jsonPath("$.isFirstAttempt").value(true))
                .andExpect(jsonPath("$.isReviewStateChanged").value(true));

        // When 2: 재시도 - 성공 (같은 날)
        Map<String, Object> correctRequest = new HashMap<>();
        correctRequest.put("answer", "3");  // 정답

        mockMvc.perform(post("/api/{problemId}/submit", problem.getProblemId())
                        .cookie(new Cookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(correctRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isCorrect").value(true))
                .andExpect(jsonPath("$.currentGate").value("GATE_1"))  // 여전히 GATE_1
                .andExpect(jsonPath("$.reviewCount").value(1))  // 변화 없음
                .andExpect(jsonPath("$.isFirstAttempt").value(false))  // 재시도
                .andExpect(jsonPath("$.isReviewStateChanged").value(false));  // 상태 불변

        // Then: GATE_1 유지 확인
        ProblemReviewState updatedState = problemReviewStateRepository.findByUserAndProblem(testUser, problem).get();
        assertThat(updatedState.getGate()).isEqualTo(ReviewGate.GATE_1);
        assertThat(updatedState.getReviewCount()).isEqualTo(1);
        assertThat(problemAttemptRepository.count()).isEqualTo(2);  // 2번 시도
    }

    @Test
    @DisplayName("첫 시도 우선 법칙 - GATE_2 틀려서 강등 후 당일 성공해도 GATE_1 유지")
    void submitProblem_Gate2_FailDemoteThenSuccessSameDay_StayGate1() throws Exception {
        // Given: GATE_2, 오늘의 복습 문제
        Problem problem = createOxProblem();
        ProblemReviewState reviewState = ProblemReviewState.builder()
                .user(testUser)
                .problem(problem)
                .gate(ReviewGate.GATE_2)
                .nextReviewDate(today)
                .reviewCount(1)
                .todayReviewIncludedDate(today)
                .todayReviewIncludedGate(ReviewGate.GATE_2)
                .todayReviewFirstAttemptDate(null)
                .build();
        problemReviewStateRepository.save(reviewState);

        // When 1: 첫 시도 - 틀림 (GATE_2 → GATE_1 강등)
        Map<String, Object> wrongRequest = new HashMap<>();
        wrongRequest.put("answer", "true");  // 오답 (정답은 false)

        mockMvc.perform(post("/api/{problemId}/submit", problem.getProblemId())
                        .cookie(new Cookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isCorrect").value(false))
                .andExpect(jsonPath("$.currentGate").value("GATE_1"))  // 강등됨
                .andExpect(jsonPath("$.reviewCount").value(2))
                .andExpect(jsonPath("$.isFirstAttempt").value(true))
                .andExpect(jsonPath("$.isReviewStateChanged").value(true));

        // When 2: 재시도 - 성공 (같은 날)
        Map<String, Object> correctRequest = new HashMap<>();
        correctRequest.put("answer", "false");  // 정답

        mockMvc.perform(post("/api/{problemId}/submit", problem.getProblemId())
                        .cookie(new Cookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(correctRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isCorrect").value(true))
                .andExpect(jsonPath("$.currentGate").value("GATE_1"))  // GATE_1 유지 (다시 승급 안 됨)
                .andExpect(jsonPath("$.reviewCount").value(2))  // 변화 없음
                .andExpect(jsonPath("$.isFirstAttempt").value(false))
                .andExpect(jsonPath("$.isReviewStateChanged").value(false));

        // Then: GATE_1 유지 확인 (GATE_2로 다시 승급 안 됨)
        ProblemReviewState updatedState = problemReviewStateRepository.findByUserAndProblem(testUser, problem).get();
        assertThat(updatedState.getGate()).isEqualTo(ReviewGate.GATE_1);
        assertThat(updatedState.getReviewCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("첫 시도 우선 법칙 - 오늘의 복습 문제는 여러 번 풀 수 있음")
    void submitProblem_TodayReview_CanRetryMultipleTimes() throws Exception {
        // Given: GATE_1, 오늘의 복습 문제
        Problem problem = createShortProblem();
        ProblemReviewState reviewState = ProblemReviewState.builder()
                .user(testUser)
                .problem(problem)
                .gate(ReviewGate.GATE_1)
                .nextReviewDate(today)
                .reviewCount(0)
                .todayReviewIncludedDate(today)
                .todayReviewIncludedGate(ReviewGate.GATE_1)
                .todayReviewFirstAttemptDate(null)
                .build();
        problemReviewStateRepository.save(reviewState);

        // When 1: 첫 시도 - 성공 (GATE_1 → GATE_2 승급)
        Map<String, Object> correctRequest1 = new HashMap<>();
        correctRequest1.put("answer", "Garbage Collector");  // 정답

        mockMvc.perform(post("/api/{problemId}/submit", problem.getProblemId())
                        .cookie(new Cookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(correctRequest1)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isCorrect").value(true))
                .andExpect(jsonPath("$.currentGate").value("GATE_2"))  // 승급
                .andExpect(jsonPath("$.reviewCount").value(1))
                .andExpect(jsonPath("$.isFirstAttempt").value(true))
                .andExpect(jsonPath("$.isReviewStateChanged").value(true));

        // When 2: 재시도 1 - 틀림 (상태 불변)
        Map<String, Object> wrongRequest = new HashMap<>();
        wrongRequest.put("answer", "JVM");  // 오답

        mockMvc.perform(post("/api/{problemId}/submit", problem.getProblemId())
                        .cookie(new Cookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isCorrect").value(false))
                .andExpect(jsonPath("$.currentGate").value("GATE_2"))  // 여전히 GATE_2
                .andExpect(jsonPath("$.reviewCount").value(1))  // 변화 없음
                .andExpect(jsonPath("$.isFirstAttempt").value(false))
                .andExpect(jsonPath("$.isReviewStateChanged").value(false));

        // When 3: 재시도 2 - 성공 (상태 불변)
        Map<String, Object> correctRequest2 = new HashMap<>();
        correctRequest2.put("answer", "Garbage Collector");  // 정답

        mockMvc.perform(post("/api/{problemId}/submit", problem.getProblemId())
                        .cookie(new Cookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(correctRequest2)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isCorrect").value(true))
                .andExpect(jsonPath("$.currentGate").value("GATE_2"))  // 여전히 GATE_2
                .andExpect(jsonPath("$.reviewCount").value(1))  // 변화 없음
                .andExpect(jsonPath("$.isFirstAttempt").value(false))
                .andExpect(jsonPath("$.isReviewStateChanged").value(false));

        // Then: GATE_2 유지, 여러 번 풀어도 상태 변화 없음
        ProblemReviewState updatedState = problemReviewStateRepository.findByUserAndProblem(testUser, problem).get();
        assertThat(updatedState.getGate()).isEqualTo(ReviewGate.GATE_2);
        assertThat(updatedState.getReviewCount()).isEqualTo(1);
        assertThat(problemAttemptRepository.count()).isEqualTo(3);  // 3번 시도
    }

    // Helper methods
    private void createChoices(Problem problem) {
        List<String> choices = List.of("private", "protected", "default", "public");
        for (int i = 0; i < choices.size(); i++) {
            ProblemChoice choice = ProblemChoice.builder()
                    .problem(problem)
                    .choiceOrder(i)
                    .choiceText(choices.get(i))
                    .build();
            problemChoiceRepository.save(choice);
        }
    }
}
