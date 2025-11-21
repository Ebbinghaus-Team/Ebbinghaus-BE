package com.ebbinghaus.ttopullae.problem.presentation;

import com.ebbinghaus.ttopullae.global.auth.LoginUser;
import com.ebbinghaus.ttopullae.global.exception.ErrorResponse;
import com.ebbinghaus.ttopullae.problem.presentation.dto.ProblemCreateRequest;
import com.ebbinghaus.ttopullae.problem.presentation.dto.ProblemCreateResponse;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Problem", description = "문제 관리 API")
public interface ProblemControllerDocs {

    @Operation(
            summary = "문제 생성",
            description = "스터디룸에 새로운 학습 문제를 생성합니다. 4가지 유형(객관식, OX, 단답형, 서술형)을 지원하며, 유형별로 필요한 데이터가 다릅니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "문제 생성 성공",
                    content = @Content(
                            schema = @Schema(implementation = ProblemCreateResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "객관식 문제 생성 성공 예시",
                                            value = """
                                                    {
                                                      "problemId": 1,
                                                      "studyRoomId": 1,
                                                      "problemType": "MCQ",
                                                      "question": "자바의 접근 제어자가 아닌 것은?",
                                                      "createdAt": "2025-01-21T10:30:00"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "OX 문제 생성 성공 예시",
                                            value = """
                                                    {
                                                      "problemId": 2,
                                                      "studyRoomId": 1,
                                                      "problemType": "OX",
                                                      "question": "JVM은 Java Virtual Machine의 약자이다.",
                                                      "createdAt": "2025-01-21T10:35:00"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "단답형 문제 생성 성공 예시",
                                            value = """
                                                    {
                                                      "problemId": 3,
                                                      "studyRoomId": 1,
                                                      "problemType": "SHORT",
                                                      "question": "자바에서 문자열을 다루는 불변 클래스는?",
                                                      "createdAt": "2025-01-21T10:40:00"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "서술형 문제 생성 성공 예시",
                                            value = """
                                                    {
                                                      "problemId": 4,
                                                      "studyRoomId": 1,
                                                      "problemType": "SUBJECTIVE",
                                                      "question": "DDD의 핵심 개념에 대해 설명하시오.",
                                                      "createdAt": "2025-01-21T10:45:00"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),

            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "필수 입력값 누락",
                                            value = """
                                                    {
                                                      "title": "유효하지 않은 입력값",
                                                      "status": 400,
                                                      "detail": "question: 문제 내용은 필수입니다",
                                                      "instance": "/api/study-rooms/1/problems"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "객관식 데이터 오류",
                                            value = """
                                                    {
                                                      "title": "객관식 데이터 오류",
                                                      "status": 400,
                                                      "detail": "객관식 문제는 선택지 목록과 정답 인덱스가 필요합니다.",
                                                      "instance": "/api/study-rooms/1/problems"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "OX 데이터 오류",
                                            value = """
                                                    {
                                                      "title": "OX 데이터 오류",
                                                      "status": 400,
                                                      "detail": "OX 문제는 정답(true/false)이 필요합니다.",
                                                      "instance": "/api/study-rooms/1/problems"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "단답형 데이터 오류",
                                            value = """
                                                    {
                                                      "title": "단답형 데이터 오류",
                                                      "status": 400,
                                                      "detail": "단답형 문제는 정답 텍스트가 필요합니다.",
                                                      "instance": "/api/study-rooms/1/problems"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "서술형 데이터 오류",
                                            value = """
                                                    {
                                                      "title": "서술형 데이터 오류",
                                                      "status": 400,
                                                      "detail": "서술형 문제는 모범 답안과 키워드 목록이 필요합니다.",
                                                      "instance": "/api/study-rooms/1/problems"
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
                                              "instance": "/api/study-rooms/1/problems"
                                            }
                                            """
                            )
                    )
            ),

            @ApiResponse(responseCode = "404", description = "스터디룸을 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "존재하지 않는 스터디룸 ID로 요청한 경우",
                                    value = """
                                            {
                                              "title": "스터디룸을 찾을 수 없음",
                                              "status": 404,
                                              "detail": "요청한 ID의 스터디룸이 존재하지 않습니다.",
                                              "instance": "/api/study-rooms/999/problems"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping
    ResponseEntity<ProblemCreateResponse> createProblem(
            @Parameter(hidden = true) @LoginUser Long userId,
            @PathVariable Long studyRoomId,
            @Valid @RequestBody ProblemCreateRequest request
    );
}
