package com.ebbinghaus.ttopullae.studyroom.presentation;

import com.ebbinghaus.ttopullae.global.auth.LoginUser;
import com.ebbinghaus.ttopullae.global.exception.ErrorResponse;
import com.ebbinghaus.ttopullae.studyroom.presentation.dto.GroupRoomCreateRequest;
import com.ebbinghaus.ttopullae.studyroom.presentation.dto.GroupRoomCreateResponse;
import com.ebbinghaus.ttopullae.studyroom.presentation.dto.GroupRoomJoinRequest;
import com.ebbinghaus.ttopullae.studyroom.presentation.dto.GroupRoomJoinResponse;
import com.ebbinghaus.ttopullae.studyroom.presentation.dto.GroupRoomListResponse;
import com.ebbinghaus.ttopullae.studyroom.presentation.dto.PersonalRoomCreateRequest;
import com.ebbinghaus.ttopullae.studyroom.presentation.dto.PersonalRoomCreateResponse;
import com.ebbinghaus.ttopullae.studyroom.presentation.dto.PersonalRoomListResponse;
import com.ebbinghaus.ttopullae.studyroom.presentation.dto.PersonalRoomProblemListResponse;
import com.ebbinghaus.ttopullae.studyroom.presentation.dto.GroupRoomProblemListResponse;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Study Room", description = "스터디룸 관리 API")
public interface StudyRoomControllerDocs {

    @Operation(
            summary = "개인 공부방 생성",
            description = "새로운 개인 공부방을 생성합니다. JWT 쿠키를 통해 인증된 사용자가 자동으로 소유자로 등록됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "개인 공부방 생성에 성공한 경우",
                    content = @Content(
                            schema = @Schema(implementation = PersonalRoomCreateResponse.class),
                            examples = @ExampleObject(
                                    name = "개인 공부방 생성 성공 예시",
                                    value = """
                                            {
                                              "studyRoomId": 1,
                                              "name": "자바 마스터하기",
                                              "category": "프로그래밍",
                                              "description": "자바 기초부터 고급까지",
                                              "createdAt": "2025-01-17T10:30:00"
                                            }
                                            """
                            )
                    )
            ),

            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "필수 입력값(공부방 이름)을 누락하여 요청한 경우",
                                    value = """
                                            {
                                              "title": "유효하지 않은 입력값",
                                              "status": 400,
                                              "detail": "name: 공부방 이름은 필수입니다",
                                              "instance": "/api/study-rooms/personal"
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
                                              "instance": "/api/study-rooms/personal"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/personal")
    ResponseEntity<PersonalRoomCreateResponse> createPersonalRoom(
            @Parameter(hidden = true) @LoginUser Long userId,
            @Valid @RequestBody PersonalRoomCreateRequest request
    );

    @Operation(
            summary = "그룹 스터디 생성",
            description = "새로운 그룹 스터디를 생성합니다. JWT 쿠키를 통해 인증된 사용자가 방장으로 자동 등록되며, 고유한 참여 코드가 발급됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "그룹 스터디 생성에 성공한 경우",
                    content = @Content(
                            schema = @Schema(implementation = GroupRoomCreateResponse.class),
                            examples = @ExampleObject(
                                    name = "그룹 스터디 생성 성공 예시",
                                    value = """
                                            {
                                              "studyRoomId": 2,
                                              "name": "알고리즘 스터디",
                                              "category": "코딩테스트",
                                              "description": "매주 월요일 알고리즘 문제 풀이",
                                              "joinCode": "ABC12345",
                                              "createdAt": "2025-01-17T11:00:00"
                                            }
                                            """
                            )
                    )
            ),

            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "필수 입력값(그룹 스터디 이름)을 누락하여 요청한 경우",
                                    value = """
                                            {
                                              "title": "유효하지 않은 입력값",
                                              "status": 400,
                                              "detail": "name: 그룹 스터디 이름은 필수입니다",
                                              "instance": "/api/study-rooms/group"
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
                                              "instance": "/api/study-rooms/group"
                                            }
                                            """
                            )
                    )
            ),

            @ApiResponse(responseCode = "500", description = "참여 코드 생성에 실패한 경우",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "고유한 참여 코드 생성 실패",
                                    value = """
                                            {
                                              "title": "참여 코드 생성 실패",
                                              "status": 500,
                                              "detail": "고유한 참여 코드 생성에 실패했습니다. 잠시 후 다시 시도해주세요.",
                                              "instance": "/api/study-rooms/group"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/group")
    ResponseEntity<GroupRoomCreateResponse> createGroupRoom(
            @Parameter(hidden = true) @LoginUser Long userId,
            @Valid @RequestBody GroupRoomCreateRequest request
    );

    @Operation(
            summary = "그룹 스터디 참여",
            description = "참여 코드를 사용하여 기존 그룹 스터디에 참여합니다. JWT 쿠키를 통해 인증된 사용자가 그룹 멤버로 등록됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "그룹 스터디 참여에 성공한 경우",
                    content = @Content(
                            schema = @Schema(implementation = GroupRoomJoinResponse.class),
                            examples = @ExampleObject(
                                    name = "그룹 스터디 참여 성공 예시",
                                    value = """
                                            {
                                              "studyRoomId": 2,
                                              "name": "알고리즘 스터디",
                                              "category": "코딩테스트",
                                              "description": "매주 월요일 알고리즘 문제 풀이",
                                              "joinedAt": "2025-01-17T14:20:00"
                                            }
                                            """
                            )
                    )
            ),

            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "개인 공부방의 참여 코드로 참여를 시도한 경우",
                                    value = """
                                            {
                                              "title": "그룹 스터디가 아님",
                                              "status": 400,
                                              "detail": "참여 코드가 유효하지 않습니다. 개인 공부방에는 참여할 수 없습니다.",
                                              "instance": "/api/study-rooms/group/join"
                                            }
                                            """
                            )
                    )
            ),

            @ApiResponse(responseCode = "404", description = "스터디룸을 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "존재하지 않는 참여 코드로 요청한 경우",
                                    value = """
                                            {
                                              "title": "스터디룸을 찾을 수 없음",
                                              "status": 404,
                                              "detail": "요청한 참여 코드의 스터디룸이 존재하지 않습니다.",
                                              "instance": "/api/study-rooms/group/join"
                                            }
                                            """
                            )
                    )
            ),

            @ApiResponse(responseCode = "409", description = "이미 참여한 스터디룸",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "이미 참여한 그룹 스터디에 재참여를 시도한 경우",
                                    value = """
                                            {
                                              "title": "이미 참여한 스터디룸",
                                              "status": 409,
                                              "detail": "이미 참여한 스터디룸입니다.",
                                              "instance": "/api/study-rooms/group/join"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/group/join")
    ResponseEntity<GroupRoomJoinResponse> joinGroupRoom(
            @Parameter(hidden = true) @LoginUser Long userId,
            @Valid @RequestBody GroupRoomJoinRequest request
    );

    @Operation(
            summary = "개인 공부방 목록 조회",
            description = "사용자의 개인 공부방 목록을 조회합니다. 각 공부방의 전체 문제 수와 완료한 문제 수(GRADUATED 상태)가 포함됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "개인 공부방 목록 조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = PersonalRoomListResponse.class),
                            examples = @ExampleObject(
                                    name = "개인 공부방 목록 조회 성공 예시",
                                    value = """
                                            {
                                              "rooms": [
                                                {
                                                  "studyRoomId": 1,
                                                  "name": "자바 마스터하기",
                                                  "category": "프로그래밍",
                                                  "description": "자바 기초부터 고급까지",
                                                  "totalProblems": 10,
                                                  "graduatedProblems": 5,
                                                  "createdAt": "2025-01-17T10:30:00"
                                                },
                                                {
                                                  "studyRoomId": 3,
                                                  "name": "스프링 부트 심화",
                                                  "category": "프레임워크",
                                                  "description": "스프링 부트 고급 기능",
                                                  "totalProblems": 15,
                                                  "graduatedProblems": 8,
                                                  "createdAt": "2025-01-18T09:00:00"
                                                }
                                              ],
                                              "totalCount": 2
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
                                              "instance": "/api/study-rooms/personal"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/personal")
    ResponseEntity<PersonalRoomListResponse> getPersonalRooms(
            @Parameter(hidden = true) @LoginUser Long userId
    );

    @Operation(
            summary = "그룹 스터디 목록 조회",
            description = "사용자가 속한 그룹 스터디 목록을 조회합니다. 각 그룹의 전체 문제 수와 완료한 문제 수(GRADUATED 상태)가 포함됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "그룹 스터디 목록 조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = GroupRoomListResponse.class),
                            examples = @ExampleObject(
                                    name = "그룹 스터디 목록 조회 성공 예시",
                                    value = """
                                            {
                                              "rooms": [
                                                {
                                                  "studyRoomId": 2,
                                                  "name": "알고리즘 스터디",
                                                  "category": "코딩테스트",
                                                  "description": "매주 월요일 알고리즘 문제 풀이",
                                                  "joinCode": "ABC12345",
                                                  "totalProblems": 20,
                                                  "graduatedProblems": 12,
                                                  "joinedAt": "2025-01-17T11:00:00"
                                                },
                                                {
                                                  "studyRoomId": 5,
                                                  "name": "CS 면접 대비",
                                                  "category": "면접",
                                                  "description": "CS 기초 지식 스터디",
                                                  "joinCode": "XYZ98765",
                                                  "totalProblems": 30,
                                                  "graduatedProblems": 18,
                                                  "joinedAt": "2025-01-18T15:30:00"
                                                }
                                              ],
                                              "totalCount": 2
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
                                              "instance": "/api/study-rooms/group"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/group")
    ResponseEntity<GroupRoomListResponse> getGroupRooms(
            @Parameter(hidden = true) @LoginUser Long userId
    );

    @Operation(
            summary = "개인 공부방 문제 목록 조회",
            description = "개인 공부방의 문제 목록을 조회합니다. 필터(ALL, GATE_1, GATE_2, GRADUATED)를 사용하여 복습 상태별로 문제를 조회할 수 있습니다. 개인 공부방의 소유자만 접근할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "개인 공부방 문제 목록 조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = PersonalRoomProblemListResponse.class),
                            examples = @ExampleObject(
                                    name = "개인 공부방 문제 목록 조회 성공 예시",
                                    value = """
                                            {
                                              "studyRoomId": 1,
                                              "studyRoomName": "자바 마스터하기",
                                              "problems": [
                                                {
                                                  "problemId": 1,
                                                  "question": "자바의 특징을 3가지 설명하시오",
                                                  "problemType": "SUBJECTIVE",
                                                  "reviewGate": "GATE_1",
                                                  "createdAt": "2025-01-17T10:00:00",
                                                  "lastReviewedAt": "2025-01-18T14:30:00",
                                                  "reviewCount": 1
                                                },
                                                {
                                                  "problemId": 2,
                                                  "question": "다음 중 접근 제어자가 아닌 것은?",
                                                  "problemType": "MCQ",
                                                  "reviewGate": "GATE_2",
                                                  "createdAt": "2025-01-15T09:20:00",
                                                  "lastReviewedAt": "2025-01-18T10:15:00",
                                                  "reviewCount": 2
                                                },
                                                {
                                                  "problemId": 3,
                                                  "question": "객체지향 프로그래밍의 4대 특징은?",
                                                  "problemType": "SHORT",
                                                  "reviewGate": "GRADUATED",
                                                  "createdAt": "2025-01-10T11:00:00",
                                                  "lastReviewedAt": "2025-01-17T16:00:00",
                                                  "reviewCount": 3
                                                }
                                              ],
                                              "totalCount": 3
                                            }
                                            """
                            )
                    )
            ),

            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "그룹 스터디 ID로 요청한 경우",
                                    value = """
                                            {
                                              "title": "개인 공부방이 아님",
                                              "status": 400,
                                              "detail": "해당 스터디룸은 개인 공부방이 아닙니다.",
                                              "instance": "/api/study-rooms/personal/2/problems"
                                            }
                                            """
                            )
                    )
            ),

            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "개인 공부방의 소유자가 아닌 경우",
                                    value = """
                                            {
                                              "title": "스터디룸 소유자가 아님",
                                              "status": 403,
                                              "detail": "해당 스터디룸의 소유자만 접근할 수 있습니다.",
                                              "instance": "/api/study-rooms/personal/1/problems"
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
                                              "detail": "요청한 참여 코드의 스터디룸이 존재하지 않습니다.",
                                              "instance": "/api/study-rooms/personal/999/problems"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/personal/{studyRoomId}/problems")
    ResponseEntity<PersonalRoomProblemListResponse> getPersonalRoomProblems(
            @Parameter(hidden = true) @LoginUser Long userId,
            @Parameter(description = "스터디룸 ID", required = true) @PathVariable Long studyRoomId,
            @Parameter(description = "필터 타입 (ALL, GATE_1, GATE_2, GRADUATED)", example = "ALL") @RequestParam(defaultValue = "ALL") String filter
    );

    @Operation(
            summary = "그룹 공부방 문제 목록 조회",
            description = "그룹 공부방의 문제 목록을 조회합니다. 필터(ALL, NOT_IN_REVIEW, GATE_1, GATE_2, GRADUATED)를 사용하여 복습 상태별로 문제를 조회할 수 있습니다. 그룹 멤버만 접근할 수 있습니다. 응답에는 isMyProblem(내가 생성한 문제 여부)과 creatorName(생성자 이름)이 포함됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "그룹 공부방 문제 목록 조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = GroupRoomProblemListResponse.class),
                            examples = @ExampleObject(
                                    name = "그룹 공부방 문제 목록 조회 성공 예시",
                                    value = """
                                            {
                                              "studyRoomId": 2,
                                              "studyRoomName": "알고리즘 스터디",
                                              "problems": [
                                                {
                                                  "problemId": 10,
                                                  "question": "이진 탐색 트리를 설명하시오",
                                                  "problemType": "SUBJECTIVE",
                                                  "reviewGate": "GATE_1",
                                                  "createdAt": "2025-01-17T10:00:00",
                                                  "lastReviewedAt": "2025-01-18T14:30:00",
                                                  "reviewCount": 1,
                                                  "isMyProblem": true,
                                                  "creatorName": "김철수"
                                                },
                                                {
                                                  "problemId": 11,
                                                  "question": "다익스트라 알고리즘의 시간 복잡도는?",
                                                  "problemType": "SHORT",
                                                  "reviewGate": "NOT_IN_REVIEW",
                                                  "createdAt": "2025-01-16T09:20:00",
                                                  "lastReviewedAt": null,
                                                  "reviewCount": 0,
                                                  "isMyProblem": false,
                                                  "creatorName": "박영희"
                                                },
                                                {
                                                  "problemId": 12,
                                                  "question": "다음 중 정렬 알고리즘이 아닌 것은?",
                                                  "problemType": "MCQ",
                                                  "reviewGate": "GRADUATED",
                                                  "createdAt": "2025-01-15T11:00:00",
                                                  "lastReviewedAt": "2025-01-17T16:00:00",
                                                  "reviewCount": 3,
                                                  "isMyProblem": false,
                                                  "creatorName": "이민호"
                                                }
                                              ],
                                              "totalCount": 3
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
                                            name = "개인 공부방 ID로 요청한 경우",
                                            value = """
                                                    {
                                                      "title": "그룹 스터디가 아님",
                                                      "status": 400,
                                                      "detail": "참여 코드가 유효하지 않습니다. 개인 공부방에는 참여할 수 없습니다.",
                                                      "instance": "/api/study-rooms/group/1/problems"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "잘못된 필터 값으로 요청한 경우",
                                            value = """
                                                    {
                                                      "title": "잘못된 필터",
                                                      "status": 400,
                                                      "detail": "유효하지 않은 필터 값입니다. ALL, NOT_IN_REVIEW, GATE_1, GATE_2, GRADUATED 중 하나를 사용하세요.",
                                                      "instance": "/api/study-rooms/group/2/problems"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),

            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "그룹 멤버가 아닌 경우",
                                    value = """
                                            {
                                              "title": "그룹 멤버가 아님",
                                              "status": 403,
                                              "detail": "해당 그룹 스터디의 멤버만 접근할 수 있습니다.",
                                              "instance": "/api/study-rooms/group/2/problems"
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
                                              "detail": "요청한 참여 코드의 스터디룸이 존재하지 않습니다.",
                                              "instance": "/api/study-rooms/group/999/problems"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/group/{studyRoomId}/problems")
    ResponseEntity<GroupRoomProblemListResponse> getGroupRoomProblems(
            @Parameter(hidden = true) @LoginUser Long userId,
            @Parameter(description = "스터디룸 ID", required = true) @PathVariable Long studyRoomId,
            @Parameter(description = "필터 타입 (ALL, NOT_IN_REVIEW, GATE_1, GATE_2, GRADUATED)", example = "ALL") @RequestParam(defaultValue = "ALL") String filter
    );
}
