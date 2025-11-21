package com.ebbinghaus.ttopullae.problem.application;

import com.ebbinghaus.ttopullae.global.exception.ApplicationException;
import com.ebbinghaus.ttopullae.global.exception.CommonException;
import com.ebbinghaus.ttopullae.global.util.PromptLoader;
import com.ebbinghaus.ttopullae.problem.application.dto.AiGradingRequest;
import com.ebbinghaus.ttopullae.problem.application.dto.AiGradingResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * OpenAI API를 사용한 AI 채점 서비스 구현체
 * GPT-4o-mini 모델을 사용하여 서술형 답안을 채점한다
 */
@Service
public class OpenAiGradingService implements AiGradingService {

  private static final Logger log = LoggerFactory.getLogger(OpenAiGradingService.class);

  private final WebClient webClient;
  private final PromptLoader promptLoader;
  private final ObjectMapper objectMapper;

  @Value("${openai.api-key}")
  private String apiKey;

  @Value("${openai.model}")
  private String model;

  @Value("${openai.api-url}")
  private String apiUrl;

  public OpenAiGradingService(WebClient.Builder webClientBuilder, PromptLoader promptLoader,
      ObjectMapper objectMapper) {
    this.webClient = webClientBuilder.build();
    this.promptLoader = promptLoader;
    this.objectMapper = objectMapper;
  }

  @Override
  public AiGradingResult gradeSubjectiveAnswer(AiGradingRequest request) {
    try {
      // 1. 프롬프트 템플릿 로드 및 변수 치환
      String promptTemplate = promptLoader.loadPromptTemplate(
          "classpath:prompts/grading_system_prompt.txt");
      String filledPrompt = promptLoader.fillTemplate(promptTemplate, Map.of(
          "TOPIC", request.topic(),
          "QUESTION", request.question(),
          "MODEL_ANSWER", request.modelAnswer(),
          "KEYWORDS_LIST", String.join(", ", request.keywords()),
          "USER_ANSWER", request.userAnswer()
      ));

      // 2. OpenAI API 요청 바디 구성
      Map<String, Object> requestBody = Map.of(
          "model", model,
          "messages", List.of(
              Map.of("role", "user", "content", filledPrompt)
          ),
          "response_format", Map.of("type", "json_object")
      );

      // 3. OpenAI API 호출
      String response = webClient.post()
          .uri(apiUrl)
          .header("Authorization", "Bearer " + apiKey)
          .header("Content-Type", "application/json")
          .bodyValue(requestBody)
          .retrieve()
          .bodyToMono(String.class)
          .block();

      // 4. 응답 파싱
      return parseAiResponse(response);

    } catch (IOException e) {
      log.error("프롬프트 파일 로드 실패", e);
      return createFailureResult("프롬프트 파일을 읽는 중 오류가 발생했습니다.");
    } catch (Exception e) {
      log.error("AI 채점 실패", e);
      return createFailureResult("AI 채점 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    }
  }

  /**
   * OpenAI API 응답을 파싱하여 AiGradingResult로 변환
   */
  private AiGradingResult parseAiResponse(String response) throws Exception {
    JsonNode root = objectMapper.readTree(response);
    JsonNode choices = root.get("choices");

    if (choices == null || choices.isEmpty()) {
      throw new ApplicationException(CommonException.INTERNAL_SERVER_ERROR);
    }

    String content = choices.get(0).get("message").get("content").asText();
    JsonNode gradingResult = objectMapper.readTree(content);

    return new AiGradingResult(
        gradingResult.get("isCorrect").asBoolean(),
        gradingResult.get("feedback").asText(),
        objectMapper.convertValue(gradingResult.get("missingKeywords"),
            objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)),
        gradingResult.get("scoringReason").asText()
    );
  }

  /**
   * Graceful Degradation: 오류 발생 시 사용자에게 안내하는 실패 결과 생성
   */
  private AiGradingResult createFailureResult(String errorMessage) {
    return new AiGradingResult(
        false,
        errorMessage,
        List.of(),
        "시스템 오류로 인해 채점을 완료할 수 없습니다."
    );
  }
}
