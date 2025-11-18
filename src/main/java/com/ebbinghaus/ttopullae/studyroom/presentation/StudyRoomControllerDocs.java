package com.ebbinghaus.ttopullae.studyroom.presentation;

import com.ebbinghaus.ttopullae.global.auth.LoginUser;
import com.ebbinghaus.ttopullae.global.exception.ErrorResponse;
import com.ebbinghaus.ttopullae.studyroom.presentation.dto.GroupRoomCreateRequest;
import com.ebbinghaus.ttopullae.studyroom.presentation.dto.GroupRoomCreateResponse;
import com.ebbinghaus.ttopullae.studyroom.presentation.dto.PersonalRoomCreateRequest;
import com.ebbinghaus.ttopullae.studyroom.presentation.dto.PersonalRoomCreateResponse;
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
}
