package com.ebbinghaus.ttopullae.studyroom.presentation;

import com.ebbinghaus.ttopullae.global.exception.ErrorResponse;
import com.ebbinghaus.ttopullae.studyroom.presentation.dto.GroupRoomCreateRequest;
import com.ebbinghaus.ttopullae.studyroom.presentation.dto.GroupRoomCreateResponse;
import com.ebbinghaus.ttopullae.studyroom.presentation.dto.PersonalRoomCreateRequest;
import com.ebbinghaus.ttopullae.studyroom.presentation.dto.PersonalRoomCreateResponse;
import io.swagger.v3.oas.annotations.Operation;
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
            description = "새로운 개인 공부방을 생성합니다. 개인 공부방은 소유자 1명만 존재하며, 생성 시 자동으로 소유자가 멤버로 등록됩니다."
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
                            examples = {
                                    @ExampleObject(
                                            name = "필수 입력값(사용자 ID)을 누락하여 요청한 경우",
                                            value = """
                                                    {
                                                      "title": "유효하지 않은 입력값",
                                                      "status": 400,
                                                      "detail": "userId: 사용자 ID는 필수입니다",
                                                      "instance": "/api/study-rooms/personal"
                                                    }
                                                    """
                                    ),

                                    @ExampleObject(
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
                            }
                    )
            ),

            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자로 요청한 경우",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "존재하지 않는 사용자 ID로 요청",
                                    value = """
                                            {
                                              "title": "사용자를 찾을 수 없음",
                                              "status": 404,
                                              "detail": "요청한 ID의 사용자가 존재하지 않습니다.",
                                              "instance": "/api/study-rooms/personal"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/personal")
    ResponseEntity<PersonalRoomCreateResponse> createPersonalRoom(
            @Valid @RequestBody PersonalRoomCreateRequest request
    );

    @Operation(
            summary = "그룹 스터디 생성",
            description = "새로운 그룹 스터디를 생성합니다. 생성 시 고유한 참여 코드가 자동으로 발급되며, 다른 사용자는 이 코드를 통해 그룹 스터디에 참여할 수 있습니다."
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
                            examples = {
                                    @ExampleObject(
                                            name = "필수 입력값(사용자 ID)을 누락하여 요청한 경우",
                                            value = """
                                                    {
                                                      "title": "유효하지 않은 입력값",
                                                      "status": 400,
                                                      "detail": "userId: 사용자 ID는 필수입니다",
                                                      "instance": "/api/study-rooms/group"
                                                    }
                                                    """
                                    ),

                                    @ExampleObject(
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
                            }
                    )
            ),

            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자로 요청한 경우",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "존재하지 않는 사용자 ID로 요청",
                                    value = """
                                            {
                                              "title": "사용자를 찾을 수 없음",
                                              "status": 404,
                                              "detail": "요청한 ID의 사용자가 존재하지 않습니다.",
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
            @Valid @RequestBody GroupRoomCreateRequest request
    );
}
