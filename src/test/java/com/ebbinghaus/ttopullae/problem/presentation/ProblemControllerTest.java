package com.ebbinghaus.ttopullae.problem.presentation;

import com.ebbinghaus.ttopullae.global.util.JwtTokenProvider;
import com.ebbinghaus.ttopullae.problem.domain.ProblemType;
import com.ebbinghaus.ttopullae.problem.domain.repository.ProblemChoiceRepository;
import com.ebbinghaus.ttopullae.problem.domain.repository.ProblemKeywordRepository;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProblemControllerTest {

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
    private ProblemChoiceRepository problemChoiceRepository;

    @Autowired
    private ProblemKeywordRepository problemKeywordRepository;

    @Autowired
    private ProblemReviewStateRepository problemReviewStateRepository;

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
    @DisplayName("객관식 문제 생성 성공")
    void createMcqProblem_Success() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("problemType", "MCQ");
        request.put("question", "자바의 접근 제어자가 아닌 것은?");
        request.put("explanation", "friend는 C++의 접근 제어자입니다.");
        request.put("choices", List.of("public", "private", "protected", "friend"));
        request.put("correctChoiceIndex", 3);

        // when & then
        mockMvc.perform(post("/api/study-rooms/{studyRoomId}", testStudyRoom.getStudyRoomId())
                        .cookie(new Cookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.problemId").exists())
                .andExpect(jsonPath("$.studyRoomId").value(testStudyRoom.getStudyRoomId()))
                .andExpect(jsonPath("$.problemType").value("MCQ"))
                .andExpect(jsonPath("$.question").value("자바의 접근 제어자가 아닌 것은?"))
                .andExpect(jsonPath("$.createdAt").exists());

        // 데이터베이스 검증
        assertThat(problemRepository.count()).isEqualTo(1);
        assertThat(problemChoiceRepository.count()).isEqualTo(4);
        assertThat(problemReviewStateRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("OX 문제 생성 성공")
    void createOxProblem_Success() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("problemType", "OX");
        request.put("question", "JVM은 Java Virtual Machine의 약자이다.");
        request.put("explanation", "맞습니다.");
        request.put("answerBoolean", true);

        // when & then
        mockMvc.perform(post("/api/study-rooms/{studyRoomId}", testStudyRoom.getStudyRoomId())
                        .cookie(new Cookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.problemId").exists())
                .andExpect(jsonPath("$.studyRoomId").value(testStudyRoom.getStudyRoomId()))
                .andExpect(jsonPath("$.problemType").value("OX"))
                .andExpect(jsonPath("$.question").value("JVM은 Java Virtual Machine의 약자이다."))
                .andExpect(jsonPath("$.createdAt").exists());

        // 데이터베이스 검증
        assertThat(problemRepository.count()).isEqualTo(1);
        assertThat(problemReviewStateRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("단답형 문제 생성 성공")
    void createShortProblem_Success() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("problemType", "SHORT");
        request.put("question", "자바에서 문자열을 다루는 불변 클래스는?");
        request.put("explanation", "String 클래스입니다.");
        request.put("answerText", "String");

        // when & then
        mockMvc.perform(post("/api/study-rooms/{studyRoomId}", testStudyRoom.getStudyRoomId())
                        .cookie(new Cookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.problemId").exists())
                .andExpect(jsonPath("$.studyRoomId").value(testStudyRoom.getStudyRoomId()))
                .andExpect(jsonPath("$.problemType").value("SHORT"))
                .andExpect(jsonPath("$.question").value("자바에서 문자열을 다루는 불변 클래스는?"))
                .andExpect(jsonPath("$.createdAt").exists());

        // 데이터베이스 검증
        assertThat(problemRepository.count()).isEqualTo(1);
        assertThat(problemReviewStateRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("서술형 문제 생성 성공")
    void createSubjectiveProblem_Success() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("problemType", "SUBJECTIVE");
        request.put("question", "DDD의 핵심 개념에 대해 설명하시오.");
        request.put("explanation", "DDD는 도메인 중심 설계입니다.");
        request.put("modelAnswerText", "DDD는 도메인을 중심으로 소프트웨어를 설계하는 방법론입니다.");
        request.put("keywords", List.of("도메인", "엔티티", "리포지토리"));

        // when & then
        mockMvc.perform(post("/api/study-rooms/{studyRoomId}", testStudyRoom.getStudyRoomId())
                        .cookie(new Cookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.problemId").exists())
                .andExpect(jsonPath("$.studyRoomId").value(testStudyRoom.getStudyRoomId()))
                .andExpect(jsonPath("$.problemType").value("SUBJECTIVE"))
                .andExpect(jsonPath("$.question").value("DDD의 핵심 개념에 대해 설명하시오."))
                .andExpect(jsonPath("$.createdAt").exists());

        // 데이터베이스 검증
        assertThat(problemRepository.count()).isEqualTo(1);
        assertThat(problemKeywordRepository.count()).isEqualTo(3);
        assertThat(problemReviewStateRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("문제 생성 실패 - JWT 토큰 없음")
    void createProblem_Fail_NoToken() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("problemType", "OX");
        request.put("question", "테스트 문제");
        request.put("explanation", "해설");
        request.put("answerBoolean", true);

        // when & then
        mockMvc.perform(post("/api/study-rooms/{studyRoomId}", testStudyRoom.getStudyRoomId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("문제 생성 실패 - 잘못된 JWT 토큰")
    void createProblem_Fail_InvalidToken() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("problemType", "OX");
        request.put("question", "테스트 문제");
        request.put("explanation", "해설");
        request.put("answerBoolean", true);

        // when & then
        mockMvc.perform(post("/api/study-rooms/{studyRoomId}", testStudyRoom.getStudyRoomId())
                        .cookie(new Cookie("accessToken", "invalid.jwt.token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("문제 생성 실패 - 문제 내용 누락")
    void createProblem_Fail_MissingQuestion() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("problemType", "OX");
        request.put("question", "");
        request.put("explanation", "해설");
        request.put("answerBoolean", true);

        // when & then
        mockMvc.perform(post("/api/study-rooms/{studyRoomId}", testStudyRoom.getStudyRoomId())
                        .cookie(new Cookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("문제 생성 실패 - 해설 누락")
    void createProblem_Fail_MissingExplanation() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("problemType", "OX");
        request.put("question", "테스트 문제");
        request.put("explanation", "");
        request.put("answerBoolean", true);

        // when & then
        mockMvc.perform(post("/api/study-rooms/{studyRoomId}", testStudyRoom.getStudyRoomId())
                        .cookie(new Cookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("문제 생성 실패 - 존재하지 않는 스터디룸")
    void createProblem_Fail_StudyRoomNotFound() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("problemType", "OX");
        request.put("question", "테스트 문제");
        request.put("explanation", "해설");
        request.put("answerBoolean", true);

        // when & then
        mockMvc.perform(post("/api/study-rooms/{studyRoomId}", 99999L)
                        .cookie(new Cookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("스터디룸을 찾을 수 없음"));
    }

    @Test
    @DisplayName("객관식 문제 생성 실패 - 선택지 누락")
    void createMcqProblem_Fail_MissingChoices() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("problemType", "MCQ");
        request.put("question", "자바의 접근 제어자가 아닌 것은?");
        request.put("explanation", "해설");
        request.put("correctChoiceIndex", 3);

        // when & then
        mockMvc.perform(post("/api/study-rooms/{studyRoomId}", testStudyRoom.getStudyRoomId())
                        .cookie(new Cookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("객관식 데이터 오류"));
    }

    @Test
    @DisplayName("객관식 문제 생성 실패 - 정답 인덱스 누락")
    void createMcqProblem_Fail_MissingCorrectIndex() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("problemType", "MCQ");
        request.put("question", "자바의 접근 제어자가 아닌 것은?");
        request.put("explanation", "해설");
        request.put("choices", List.of("public", "private", "protected", "friend"));

        // when & then
        mockMvc.perform(post("/api/study-rooms/{studyRoomId}", testStudyRoom.getStudyRoomId())
                        .cookie(new Cookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("객관식 데이터 오류"));
    }

    @Test
    @DisplayName("OX 문제 생성 실패 - 정답 누락")
    void createOxProblem_Fail_MissingAnswer() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("problemType", "OX");
        request.put("question", "JVM은 Java Virtual Machine의 약자이다.");
        request.put("explanation", "해설");

        // when & then
        mockMvc.perform(post("/api/study-rooms/{studyRoomId}", testStudyRoom.getStudyRoomId())
                        .cookie(new Cookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("OX 데이터 오류"));
    }

    @Test
    @DisplayName("단답형 문제 생성 실패 - 정답 텍스트 누락")
    void createShortProblem_Fail_MissingAnswerText() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("problemType", "SHORT");
        request.put("question", "자바에서 문자열을 다루는 불변 클래스는?");
        request.put("explanation", "해설");

        // when & then
        mockMvc.perform(post("/api/study-rooms/{studyRoomId}", testStudyRoom.getStudyRoomId())
                        .cookie(new Cookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("단답형 데이터 오류"));
    }

    @Test
    @DisplayName("서술형 문제 생성 실패 - 모범 답안 누락")
    void createSubjectiveProblem_Fail_MissingModelAnswer() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("problemType", "SUBJECTIVE");
        request.put("question", "DDD의 핵심 개념에 대해 설명하시오.");
        request.put("explanation", "해설");
        request.put("keywords", List.of("도메인", "엔티티"));

        // when & then
        mockMvc.perform(post("/api/study-rooms/{studyRoomId}", testStudyRoom.getStudyRoomId())
                        .cookie(new Cookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("서술형 데이터 오류"));
    }

    @Test
    @DisplayName("서술형 문제 생성 실패 - 키워드 목록 누락")
    void createSubjectiveProblem_Fail_MissingKeywords() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("problemType", "SUBJECTIVE");
        request.put("question", "DDD의 핵심 개념에 대해 설명하시오.");
        request.put("explanation", "해설");
        request.put("modelAnswerText", "DDD는 도메인을 중심으로 소프트웨어를 설계하는 방법론입니다.");

        // when & then
        mockMvc.perform(post("/api/study-rooms/{studyRoomId}", testStudyRoom.getStudyRoomId())
                        .cookie(new Cookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("서술형 데이터 오류"));
    }
}
