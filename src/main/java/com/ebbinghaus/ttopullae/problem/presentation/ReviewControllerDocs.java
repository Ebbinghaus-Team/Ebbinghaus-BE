package com.ebbinghaus.ttopullae.problem.presentation;

import com.ebbinghaus.ttopullae.problem.presentation.dto.TodayReviewResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Review API", description = "복습 관련 API")
public interface ReviewControllerDocs {

    @Operation(
        summary = "오늘의 복습 문제 조회",
        description = """
            사용자의 오늘 복습해야 할 문제 목록과 대시보드 정보를 조회합니다.

            **대시보드 정보**:
            - 총 문제 수 (totalCount)
            - 완료 수 (completedCount)
            - 미완료 수 (incompletedCount)
            - 진행률 (progressRate)

            **문제 목록**:
            - 오늘 복습 대상 문제들 (관문 필터 적용 가능)
            - GRADUATED 문제도 오늘 졸업한 경우 목록에 포함됩니다.

            **필터 옵션**:
            - ALL: 모든 관문 (GATE_1 + GATE_2)
            - GATE_1: 1일차 복습 관문만
            - GATE_2: 7일차 복습 관문만
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TodayReviewResponse.class),
                examples = @ExampleObject(
                    name = "오늘의 복습 문제 조회 성공",
                    value = """
                        {
                          "dashboard": {
                            "totalCount": 5,
                            "completedCount": 2,
                            "incompletedCount": 3,
                            "progressRate": 40.0
                          },
                          "problems": [
                            {
                              "problemId": 1,
                              "question": "자바의 접근 제어자 종류는?",
                              "problemType": "MCQ",
                              "gate": "GATE_1",
                              "nextReviewDate": "2025-01-24"
                            },
                            {
                              "problemId": 2,
                              "question": "JPA의 영속성 컨텍스트란?",
                              "problemType": "SUBJECTIVE",
                              "gate": "GATE_2",
                              "nextReviewDate": "2025-01-24"
                            }
                          ]
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 필터 값",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "잘못된 필터 값",
                    value = """
                        {
                          "title": "잘못된 요청",
                          "status": 400,
                          "detail": "유효하지 않은 필터 값입니다.",
                          "instance": "/api/review/today"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "인증 실패",
                    value = """
                        {
                          "title": "토큰을 찾을 수 없음",
                          "status": 401,
                          "detail": "인증 토큰이 제공되지 않았습니다.",
                          "instance": "/api/review/today"
                        }
                        """
                )
            )
        )
    })
    ResponseEntity<TodayReviewResponse> getTodayReviewProblems(
        @Parameter(description = "사용자 ID (자동 주입)", hidden = true)
        Long userId,

        @Parameter(
            description = "관문 필터 (ALL: 전체, GATE_1: 1차 관문, GATE_2: 2차 관문)",
            example = "ALL"
        )
        String filter
    );
}