package com.ebbinghaus.ttopullae.problem.presentation;

import com.ebbinghaus.ttopullae.global.auth.LoginUser;
import com.ebbinghaus.ttopullae.global.exception.ErrorResponse;
import com.ebbinghaus.ttopullae.problem.presentation.dto.AiGradingTestRequest;
import com.ebbinghaus.ttopullae.problem.presentation.dto.AiGradingTestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "AI Grading", description = "AI 자동 채점 API")
public interface AiGradingControllerDocs {

  @Operation(
      summary = "AI 채점 테스트",
      description = "서술형 답안에 대한 AI 자동 채점 기능을 테스트합니다. OpenAI GPT-4o-mini 모델을 사용하여 사용자 답안을 채점하고 피드백을 제공합니다. 개발 및 테스트 목적으로 사용됩니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "AI 채점에 성공한 경우 (정답)",
          content = @Content(
              schema = @Schema(implementation = AiGradingTestResponse.class),
              examples = @ExampleObject(
                  name = "정답으로 판정된 경우",
                  value = """
                          {
                            "isCorrect": true,
                            "feedback": "정답입니다. '제어의 역전'의 개념과 '컨테이너'의 역할이 정확하게 설명되었습니다.",
                            "missingKeywords": [],
                            "scoringReason": "모든 핵심 키워드가 의미적으로 포함되었으며, 모범 답안의 핵심 내용과 일치합니다."
                          }
                          """
              )
          )
      ),

      @ApiResponse(responseCode = "200", description = "AI 채점에 성공한 경우 (오답)",
          content = @Content(
              schema = @Schema(implementation = AiGradingTestResponse.class),
              examples = @ExampleObject(
                  name = "오답으로 판정된 경우",
                  value = """
                          {
                            "isCorrect": false,
                            "feedback": "핵심 개념인 '제어의 역전'에 대한 설명이 누락되었습니다. 현재 답안은 '컨테이너'의 역할에 초점을 맞추고 있지만, '누가 제어의 주체인지'가 바뀌는 점이 포함되어야 합니다.",
                            "missingKeywords": ["제어의 역전"],
                            "scoringReason": "'제어의 역전' 개념이 누락되었습니다. '컨테이너'의 역할은 올바르게 설명되었습니다."
                          }
                          """
              )
          )
      ),

      @ApiResponse(responseCode = "400", description = "잘못된 요청",
          content = @Content(
              schema = @Schema(implementation = ErrorResponse.class),
              examples = {
                  @ExampleObject(
                      name = "필수 입력값을 누락한 경우",
                      value = """
                              {
                                "title": "유효하지 않은 입력값",
                                "status": 400,
                                "detail": "question: 문제는 필수입니다",
                                "instance": "/api/grading/test"
                              }
                              """
                  ),
                  @ExampleObject(
                      name = "키워드 리스트가 비어있는 경우",
                      value = """
                              {
                                "title": "유효하지 않은 입력값",
                                "status": 400,
                                "detail": "keywords: 최소 1개 이상의 키워드가 필요합니다",
                                "instance": "/api/grading/test"
                              }
                              """
                  )
              }
          )
      ),

      @ApiResponse(responseCode = "401", description = "인증 실패",
          content = @Content(
              schema = @Schema(implementation = ErrorResponse.class),
              examples = @ExampleObject(
                  name = "JWT 토큰이 없거나 유효하지 않은 경우",
                  value = """
                          {
                            "title": "토큰을 찾을 수 없음",
                            "status": 401,
                            "detail": "인증 토큰이 제공되지 않았습니다.",
                            "instance": "/api/grading/test"
                          }
                          """
              )
          )
      ),

      @ApiResponse(responseCode = "500", description = "서버 오류 (OpenAI API 호출 실패 등)",
          content = @Content(
              schema = @Schema(implementation = ErrorResponse.class),
              examples = @ExampleObject(
                  name = "AI 채점 서비스 오류",
                  value = """
                          {
                            "title": "서버 내부 오류",
                            "status": 500,
                            "detail": "AI 채점 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                            "instance": "/api/grading/test"
                          }
                          """
              )
          )
      )
  })
  @PostMapping("/test")
  ResponseEntity<AiGradingTestResponse> testAiGrading(
      @Parameter(hidden = true) @LoginUser Long userId,
      @Valid @RequestBody AiGradingTestRequest request
  );
}
