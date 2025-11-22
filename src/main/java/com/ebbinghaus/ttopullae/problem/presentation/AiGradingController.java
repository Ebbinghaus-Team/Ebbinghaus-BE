package com.ebbinghaus.ttopullae.problem.presentation;

import com.ebbinghaus.ttopullae.global.auth.LoginUser;
import com.ebbinghaus.ttopullae.problem.application.AiGradingService;
import com.ebbinghaus.ttopullae.problem.application.dto.AiGradingRequest;
import com.ebbinghaus.ttopullae.problem.application.dto.AiGradingResult;
import com.ebbinghaus.ttopullae.problem.presentation.dto.AiGradingTestRequest;
import com.ebbinghaus.ttopullae.problem.presentation.dto.AiGradingTestResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 자동 채점 컨트롤러
 * 개발 및 테스트 목적으로 AI 채점 기능을 직접 호출할 수 있는 API를 제공
 */
@RestController
@RequestMapping("/api/grading")
public class AiGradingController implements AiGradingControllerDocs {

  private final AiGradingService aiGradingService;

  public AiGradingController(AiGradingService aiGradingService) {
    this.aiGradingService = aiGradingService;
  }

  @Override
  @PostMapping("/test")
  public ResponseEntity<AiGradingTestResponse> testAiGrading(
      @LoginUser Long userId,
      @Valid @RequestBody AiGradingTestRequest request
  ) {
    // Presentation DTO를 Application DTO로 변환
    AiGradingRequest aiGradingRequest = new AiGradingRequest(
        request.topic(),
        request.question(),
        request.modelAnswer(),
        request.keywords(),
        request.userAnswer()
    );

    // AI 채점 서비스 호출
    AiGradingResult result = aiGradingService.gradeSubjectiveAnswer(aiGradingRequest);

    // Application DTO를 Presentation DTO로 변환
    AiGradingTestResponse response = new AiGradingTestResponse(
        result.isCorrect(),
        result.feedback(),
        result.missingKeywords(),
        result.scoringReason()
    );

    return ResponseEntity.ok(response);
  }
}
