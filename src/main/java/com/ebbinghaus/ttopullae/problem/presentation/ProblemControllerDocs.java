package com.ebbinghaus.ttopullae.problem.presentation;

import com.ebbinghaus.ttopullae.global.auth.LoginUser;
import com.ebbinghaus.ttopullae.global.exception.ErrorResponse;
import com.ebbinghaus.ttopullae.problem.presentation.dto.ProblemCreateRequest;
import com.ebbinghaus.ttopullae.problem.presentation.dto.ProblemCreateResponse;
import com.ebbinghaus.ttopullae.problem.presentation.dto.ProblemDetailResponse;
import com.ebbinghaus.ttopullae.problem.presentation.dto.ProblemReviewInclusionRequest;
import com.ebbinghaus.ttopullae.problem.presentation.dto.ProblemReviewInclusionResponse;
import com.ebbinghaus.ttopullae.problem.presentation.dto.ProblemSubmitRequest;
import com.ebbinghaus.ttopullae.problem.presentation.dto.ProblemSubmitResponse;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
    @PostMapping("/study-rooms/{studyRoomId}/problems")
    ResponseEntity<ProblemCreateResponse> createProblem(
            @Parameter(hidden = true) @LoginUser Long userId,
            @PathVariable Long studyRoomId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "문제 생성 요청 데이터 (유형별로 필요한 필드가 다름)",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ProblemCreateRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "객관식 (MCQ)",
                                            value = """
                                                    {
                                                      "problemType": "MCQ",
                                                      "question": "자바의 접근 제어자가 아닌 것은?",
                                                      "explanation": "friend는 C++의 접근 제어자입니다.",
                                                      "choices": ["public", "private", "protected", "friend"],
                                                      "correctChoiceIndex": 3
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "OX",
                                            value = """
                                                    {
                                                      "problemType": "OX",
                                                      "question": "JVM은 Java Virtual Machine의 약자이다.",
                                                      "explanation": "맞습니다.",
                                                      "answerBoolean": true
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "단답형 (SHORT)",
                                            value = """
                                                    {
                                                      "problemType": "SHORT",
                                                      "question": "자바에서 문자열을 다루는 불변 클래스는?",
                                                      "explanation": "String 클래스입니다.",
                                                      "answerText": "String"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "서술형 (SUBJECTIVE)",
                                            value = """
                                                    {
                                                      "problemType": "SUBJECTIVE",
                                                      "question": "DDD의 핵심 개념에 대해 설명하시오.",
                                                      "explanation": "DDD는 도메인 중심 설계입니다.",
                                                      "modelAnswerText": "DDD는 도메인을 중심으로 소프트웨어를 설계하는 방법론입니다.",
                                                      "keywords": ["도메인", "엔티티", "리포지토리"]
                                                    }
                                                    """
                                    )
                            }
                    )
            )
            @Valid @RequestBody ProblemCreateRequest request
    );

    @Operation(
            summary = "문제 상세 조회",
            description = "문제를 풀기 전에 문제의 상세 정보를 조회합니다. 정답 정보는 노출되지 않습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "문제 조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetailResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "객관식 문제 조회",
                                            value = """
                                                    {
                                                      "problemId": 1,
                                                      "question": "자바의 접근 제어자가 아닌 것은?",
                                                      "problemType": "MCQ",
                                                      "studyRoomId": 1,
                                                      "choices": ["public", "private", "protected", "friend"],
                                                      "currentGate": "GATE_1",
                                                      "nextReviewDate": "2025-01-29",
                                                      "reviewCount": 0,
                                                      "includeInReview": true
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "단답형 문제 조회 (복습 상태 없음)",
                                            value = """
                                                    {
                                                      "problemId": 5,
                                                      "question": "자바에서 문자열을 다루는 불변 클래스는?",
                                                      "problemType": "SHORT",
                                                      "studyRoomId": 2,
                                                      "choices": null,
                                                      "currentGate": null,
                                                      "nextReviewDate": null,
                                                      "reviewCount": null,
                                                      "includeInReview": null
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
                                              "instance": "/api/problems/1"
                                            }
                                            """
                            )
                    )
            ),

            @ApiResponse(responseCode = "403", description = "스터디룸 접근 권한 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "스터디룸 멤버가 아닌 경우",
                                    value = """
                                            {
                                              "title": "스터디룸 접근 권한 없음",
                                              "status": 403,
                                              "detail": "해당 스터디룸의 문제를 풀 수 있는 권한이 없습니다.",
                                              "instance": "/api/problems/1"
                                            }
                                            """
                            )
                    )
            ),

            @ApiResponse(responseCode = "404", description = "문제를 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "존재하지 않는 문제 ID로 요청한 경우",
                                    value = """
                                            {
                                              "title": "문제를 찾을 수 없음",
                                              "status": 404,
                                              "detail": "요청한 ID의 문제가 존재하지 않습니다.",
                                              "instance": "/api/problems/999"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/problems/{problemId}")
    ResponseEntity<ProblemDetailResponse> getProblemDetail(
            @Parameter(hidden = true) @LoginUser Long userId,
            @PathVariable Long problemId
    );

    @Operation(
            summary = "문제 풀이 제출",
            description = """
                    문제를 풀고 답안을 제출합니다. 채점 결과와 함께 복습 상태 정보를 반환합니다.

                    **동작 방식:**
                    - **오늘의 복습 문제 첫 시도**: 채점 + 상태 전이 (GATE 승급/강등)
                    - **오늘의 복습 문제 재시도**: 채점만 제공 (상태 불변)
                    - **비복습 문제** (미래/졸업 문제): 채점만 제공 (상태 불변)
                    - **그룹방 타인 문제 첫 풀이**: ReviewState 생성 없음, 채점만 제공 (복습 상태 필드들은 null 반환)
                      - 복습 루프에 추가하려면 별도의 "복습 루프 포함 설정 API" 호출 필요

                    **답안 형식:**
                    - 객관식: 선택지 인덱스 (0부터 시작, 문자열로 전달)
                    - OX: "true" 또는 "false"
                    - 단답형/서술형: 답안 텍스트
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "문제 풀이 제출 성공",
                    content = @Content(
                            schema = @Schema(implementation = ProblemSubmitResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "오늘의 복습 문제 첫 시도 정답 (GATE_1 → GATE_2 승급)",
                                            value = """
                                                    {
                                                      "isCorrect": true,
                                                      "explanation": "public은 모든 클래스에서 접근 가능합니다.",
                                                      "aiFeedback": null,
                                                      "currentGate": "GATE_2",
                                                      "reviewCount": 1,
                                                      "nextReviewDate": "2025-01-31",
                                                      "isFirstAttempt": true,
                                                      "isReviewStateChanged": true
                                                    }
                                                    """
                                            ),
                                    @ExampleObject(
                                            name = "오늘의 복습 문제 첫 시도 오답 (GATE_2 → GATE_1 강등)",
                                            value = """
                                                    {
                                                      "isCorrect": false,
                                                      "explanation": "Java는 인터페이스를 통한 다중 구현만 지원하며, 클래스 다중 상속은 지원하지 않습니다.",
                                                      "aiFeedback": null,
                                                      "currentGate": "GATE_1",
                                                      "reviewCount": 2,
                                                      "nextReviewDate": "2025-01-25",
                                                      "isFirstAttempt": true,
                                                      "isReviewStateChanged": true
                                                    }
                                                    """
                                            ),
                                    @ExampleObject(
                                            name = "오늘의 복습 문제 재시도 (상태 불변)",
                                            value = """
                                                    {
                                                      "isCorrect": true,
                                                      "explanation": "Garbage Collector가 Heap 영역의 사용하지 않는 객체를 자동으로 정리합니다.",
                                                      "aiFeedback": null,
                                                      "currentGate": "GATE_2",
                                                      "reviewCount": 2,
                                                      "nextReviewDate": "2025-01-24",
                                                      "isFirstAttempt": false,
                                                      "isReviewStateChanged": false
                                                    }
                                                    """
                                            ),
                                    @ExampleObject(
                                            name = "서술형 문제 AI 채점 (정답)",
                                            value = """
                                                    {
                                                      "isCorrect": true,
                                                      "explanation": "IoC는 객체의 생성과 의존성 관리를 개발자가 아닌 프레임워크(Spring Container)가 담당하는 설계 원칙입니다.",
                                                      "aiFeedback": "필수 키워드를 모두 포함하고 정확하게 설명하셨습니다. 제어의 역전, Spring Container, 객체 생성 개념이 명확히 드러나 있습니다.",
                                                      "currentGate": "GATE_2",
                                                      "reviewCount": 1,
                                                      "nextReviewDate": "2025-01-31",
                                                      "isFirstAttempt": true,
                                                      "isReviewStateChanged": true
                                                    }
                                                    """
                                            ),
                                    @ExampleObject(
                                            name = "비복습 문제 풀이 (상태 불변)",
                                            value = """
                                                    {
                                                      "isCorrect": false,
                                                      "explanation": "200번대는 성공, 300번대는 리다이렉션, 400번대는 클라이언트 오류, 500번대는 서버 오류를 나타냅니다.",
                                                      "aiFeedback": null,
                                                      "currentGate": "GRADUATED",
                                                      "reviewCount": 3,
                                                      "nextReviewDate": null,
                                                      "isFirstAttempt": false,
                                                      "isReviewStateChanged": false
                                                    }
                                                    """
                                            ),
                                    @ExampleObject(
                                            name = "그룹방 타인 문제 첫 풀이 (ReviewState 없음)",
                                            value = """
                                                    {
                                                      "isCorrect": true,
                                                      "explanation": "JVM은 Java Virtual Machine의 약자로 자바 가상 머신을 의미합니다.",
                                                      "aiFeedback": null,
                                                      "currentGate": null,
                                                      "reviewCount": null,
                                                      "nextReviewDate": null,
                                                      "isFirstAttempt": false,
                                                      "isReviewStateChanged": false
                                                    }
                                                    """
                                    )
                                    }
                            )
                    ),

            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "답안 누락",
                                    value = """
                                            {
                                              "title": "유효하지 않은 입력값",
                                              "status": 400,
                                              "detail": "answer: 답안은 필수입니다",
                                              "instance": "/api/1/submit"
                                            }
                                            """
                            )
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
                                              "instance": "/api/1/submit"
                                            }
                                            """
                            )
                    )
            ),

            @ApiResponse(responseCode = "404", description = "문제를 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "존재하지 않는 문제 ID로 요청한 경우",
                                    value = """
                                            {
                                              "title": "문제를 찾을 수 없음",
                                              "status": 404,
                                              "detail": "요청한 ID의 문제가 존재하지 않습니다.",
                                              "instance": "/api/999/submit"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/{problemId}/submit")
    ResponseEntity<ProblemSubmitResponse> submitProblemAnswer(
            @Parameter(hidden = true) @LoginUser Long userId,
            @PathVariable Long problemId,
            @Valid @RequestBody ProblemSubmitRequest request
    );

    @Operation(
            summary = "문제 복습 루프 포함 설정",
            description = """
                    그룹방 타인 문제를 복습 주기에 포함할지 설정합니다.

                    **복습 루프 포함 의미:**
                    - true: 문제가 복습 주기에 포함되어 "오늘의 복습"에 노출
                    - false: 문제가 복습 주기에서 제외
                    - 복습 루프에 문제가 1개 이상 있으면 이메일 알림 발송

                    **설정 가능 조건:**
                    - 타인이 만든 그룹방 문제여야 함
                    - 본인이 만든 문제는 항상 true로 고정 (설정 변경 불가)
                    - 아직 설정을 변경하지 않았어야 함 (한 번만 설정 가능)

                    **동작 방식:**
                    - ReviewState가 없으면 이 API 호출 시 생성됨 (복습 주기에 추가)
                    - 본인이 만든 문제: 생성 시 자동으로 복습 루프 포함 (true, 변경 불가)
                    - 타인이 만든 문제: 기본값 false, 이 API로 한 번만 설정 가능
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "복습 루프 포함 설정 성공",
                    content = @Content(
                            schema = @Schema(implementation = ProblemReviewInclusionResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "복습 루프 포함 설정 성공",
                                            value = """
                                                    {
                                                      "includeInReview": true,
                                                      "message": "복습 루프 포함 설정이 완료되었습니다."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "복습 루프 제외 설정 성공",
                                            value = """
                                                    {
                                                      "includeInReview": false,
                                                      "message": "복습 루프 포함 설정이 완료되었습니다."
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
                                            name = "본인 문제 설정 시도",
                                            value = """
                                                    {
                                                      "title": "복습 루프 설정 변경 불가",
                                                      "status": 400,
                                                      "detail": "본인이 만든 문제는 복습 루프 포함 설정을 변경할 수 없습니다.",
                                                      "instance": "/api/problems/1/review-inclusion"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "이미 설정 완료",
                                            value = """
                                                    {
                                                      "title": "복습 루프 설정 이미 완료",
                                                      "status": 400,
                                                      "detail": "복습 루프 포함 설정은 한 번만 변경할 수 있습니다.",
                                                      "instance": "/api/problems/6/review-inclusion"
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
                                              "instance": "/api/problems/6/review-inclusion"
                                            }
                                            """
                            )
                    )
            ),

            @ApiResponse(responseCode = "404", description = "문제를 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "존재하지 않는 문제 ID로 요청한 경우",
                                    value = """
                                            {
                                              "title": "문제를 찾을 수 없음",
                                              "status": 404,
                                              "detail": "요청한 ID의 문제가 존재하지 않습니다.",
                                              "instance": "/api/problems/999/review-inclusion"
                                            }
                                            """
                            )
                    )
            )
    })
    @PatchMapping("/{problemId}/review-inclusion")
    ResponseEntity<ProblemReviewInclusionResponse> configureReviewInclusion(
            @Parameter(hidden = true) @LoginUser Long userId,
            @PathVariable Long problemId,
            @Valid @RequestBody ProblemReviewInclusionRequest request
    );
}
