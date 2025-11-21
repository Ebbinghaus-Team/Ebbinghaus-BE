package com.ebbinghaus.ttopullae.problem.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.ebbinghaus.ttopullae.problem.application.dto.AiGradingRequest;
import com.ebbinghaus.ttopullae.problem.application.dto.AiGradingResult;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * AI 채점 서비스 통합 테스트
 * 실제 OpenAI API 연동은 비용과 시간이 소요되므로, Mock 기반 테스트로 대체
 */
@SpringBootTest
class AiGradingServiceTest {

  @Autowired
  private AiGradingService aiGradingService;

  @Test
  @DisplayName("AI 채점 서비스가 정상적으로 주입된다")
  void aiGradingService_Injection_Success() {
    // then
    assertThat(aiGradingService).isNotNull();
    assertThat(aiGradingService).isInstanceOf(OpenAiGradingService.class);
  }

  @Test
  @DisplayName("AiGradingRequest DTO가 정상적으로 생성된다")
  void aiGradingRequest_Creation_Success() {
    // given
    AiGradingRequest request = new AiGradingRequest(
        "Spring Framework",
        "IoC란 무엇인가?",
        "제어의 역전",
        List.of("제어의 역전", "컨테이너"),
        "사용자 답안"
    );

    // then
    assertThat(request.topic()).isEqualTo("Spring Framework");
    assertThat(request.question()).isEqualTo("IoC란 무엇인가?");
    assertThat(request.modelAnswer()).isEqualTo("제어의 역전");
    assertThat(request.keywords()).hasSize(2);
    assertThat(request.userAnswer()).isEqualTo("사용자 답안");
  }

  @Test
  @DisplayName("AiGradingResult DTO가 정상적으로 생성된다")
  void aiGradingResult_Creation_Success() {
    // given
    AiGradingResult result = new AiGradingResult(
        true,
        "정답입니다",
        List.of(),
        "모든 키워드 포함"
    );

    // then
    assertThat(result.isCorrect()).isTrue();
    assertThat(result.feedback()).isEqualTo("정답입니다");
    assertThat(result.missingKeywords()).isEmpty();
    assertThat(result.scoringReason()).isEqualTo("모든 키워드 포함");
  }
}
