package com.ebbinghaus.ttopullae.problem.presentation;

import com.ebbinghaus.ttopullae.global.util.JwtTokenProvider;
import com.ebbinghaus.ttopullae.problem.application.AiGradingService;
import com.ebbinghaus.ttopullae.problem.application.dto.AiGradingRequest;
import com.ebbinghaus.ttopullae.problem.application.dto.AiGradingResult;
import com.ebbinghaus.ttopullae.user.domain.User;
import com.ebbinghaus.ttopullae.user.domain.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AiGradingControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private JwtTokenProvider jwtTokenProvider;

  @MockBean
  private AiGradingService aiGradingService;

  private User testUser;
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

    // JWT 토큰 생성
    accessToken = jwtTokenProvider.generateToken(testUser.getUserId());
  }

  @Test
  @DisplayName("AI 채점 테스트 성공 - 정답인 경우")
  void testAiGrading_Correct() throws Exception {
    // given
    Map<String, Object> request = new HashMap<>();
    request.put("topic", "Spring Framework");
    request.put("question", "IoC란 무엇인가?");
    request.put("modelAnswer", "제어의 역전으로, 객체의 생성과 관리를 개발자가 아닌 컨테이너가 담당한다.");
    request.put("keywords", List.of("제어의 역전", "컨테이너"));
    request.put("userAnswer", "제어의 역전이며, 스프링 컨테이너가 객체를 관리합니다.");

    AiGradingResult mockResult = new AiGradingResult(
        true,
        "정답입니다. '제어의 역전'의 개념과 '컨테이너'의 역할이 정확하게 설명되었습니다.",
        List.of(),
        "모든 핵심 키워드가 의미적으로 포함되었으며, 모범 답안의 핵심 내용과 일치합니다."
    );

    when(aiGradingService.gradeSubjectiveAnswer(any(AiGradingRequest.class)))
        .thenReturn(mockResult);

    // when & then
    mockMvc.perform(post("/api/grading/test")
            .cookie(new Cookie("accessToken", accessToken))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.isCorrect").value(true))
        .andExpect(jsonPath("$.feedback").value("정답입니다. '제어의 역전'의 개념과 '컨테이너'의 역할이 정확하게 설명되었습니다."))
        .andExpect(jsonPath("$.missingKeywords").isEmpty())
        .andExpect(jsonPath("$.scoringReason").exists());
  }

  @Test
  @DisplayName("AI 채점 테스트 성공 - 오답인 경우")
  void testAiGrading_Incorrect() throws Exception {
    // given
    Map<String, Object> request = new HashMap<>();
    request.put("topic", "Spring Framework");
    request.put("question", "IoC란 무엇인가?");
    request.put("modelAnswer", "제어의 역전으로, 객체의 생성과 관리를 개발자가 아닌 컨테이너가 담당한다.");
    request.put("keywords", List.of("제어의 역전", "컨테이너"));
    request.put("userAnswer", "객체를 자동으로 생성해주는 기능입니다.");

    AiGradingResult mockResult = new AiGradingResult(
        false,
        "핵심 개념인 '제어의 역전'에 대한 설명이 누락되었습니다.",
        List.of("제어의 역전"),
        "'제어의 역전' 개념이 누락되었습니다."
    );

    when(aiGradingService.gradeSubjectiveAnswer(any(AiGradingRequest.class)))
        .thenReturn(mockResult);

    // when & then
    mockMvc.perform(post("/api/grading/test")
            .cookie(new Cookie("accessToken", accessToken))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.isCorrect").value(false))
        .andExpect(jsonPath("$.feedback").exists())
        .andExpect(jsonPath("$.missingKeywords[0]").value("제어의 역전"))
        .andExpect(jsonPath("$.scoringReason").exists());
  }

  @Test
  @DisplayName("AI 채점 테스트 실패 - 필수 입력값 누락")
  void testAiGrading_MissingRequiredField() throws Exception {
    // given
    Map<String, Object> request = new HashMap<>();
    request.put("topic", "Spring Framework");
    // question 누락
    request.put("modelAnswer", "제어의 역전");
    request.put("keywords", List.of("제어의 역전"));
    request.put("userAnswer", "사용자 답안");

    // when & then
    mockMvc.perform(post("/api/grading/test")
            .cookie(new Cookie("accessToken", accessToken))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title").value("유효하지 않은 입력값"))
        .andExpect(jsonPath("$.status").value(400));
  }

  @Test
  @DisplayName("AI 채점 테스트 실패 - 키워드 리스트 비어있음")
  void testAiGrading_EmptyKeywords() throws Exception {
    // given
    Map<String, Object> request = new HashMap<>();
    request.put("topic", "Spring Framework");
    request.put("question", "IoC란 무엇인가?");
    request.put("modelAnswer", "제어의 역전");
    request.put("keywords", List.of()); // 빈 리스트
    request.put("userAnswer", "사용자 답안");

    // when & then
    mockMvc.perform(post("/api/grading/test")
            .cookie(new Cookie("accessToken", accessToken))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title").value("유효하지 않은 입력값"))
        .andExpect(jsonPath("$.status").value(400));
  }

  @Test
  @DisplayName("AI 채점 테스트 실패 - 인증 토큰 없음")
  void testAiGrading_Unauthorized() throws Exception {
    // given
    Map<String, Object> request = new HashMap<>();
    request.put("topic", "Spring Framework");
    request.put("question", "IoC란 무엇인가?");
    request.put("modelAnswer", "제어의 역전");
    request.put("keywords", List.of("제어의 역전"));
    request.put("userAnswer", "사용자 답안");

    // when & then (토큰 없이 요청)
    mockMvc.perform(post("/api/grading/test")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.title").value("인증되지 않은 요청"))
        .andExpect(jsonPath("$.status").value(401));
  }
}
