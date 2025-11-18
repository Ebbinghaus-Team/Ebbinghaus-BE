package com.ebbinghaus.ttopullae.user.presentation;

import com.ebbinghaus.ttopullae.global.exception.ErrorResponse;
import com.ebbinghaus.ttopullae.user.presentation.dto.LoginRequest;
import com.ebbinghaus.ttopullae.user.presentation.dto.LoginResponse;
import com.ebbinghaus.ttopullae.user.presentation.dto.SignupRequest;
import com.ebbinghaus.ttopullae.user.presentation.dto.SignupResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Auth", description = "인증 API")
public interface AuthControllerDocs {

    @Operation(
            summary = "회원가입",
            description = "새로운 사용자를 등록합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "회원가입 성공",
                    content = @Content(
                            schema = @Schema(implementation = SignupResponse.class),
                            examples = @ExampleObject(
                                    name = "회원가입 성공 예시",
                                    value = """
                                            {
                                              "userId": 1,
                                              "email": "user@example.com",
                                              "username": "홍길동",
                                              "receiveNotifications": true
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "유효성 검증 실패",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "이메일 형식 오류",
                                    value = """
                                            {
                                              "title": "유효하지 않은 입력값",
                                              "status": 400,
                                              "detail": "올바른 이메일 형식이어야 합니다",
                                              "instance": "/api/auth/signup"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이메일 중복",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "이메일 중복",
                                    value = """
                                            {
                                              "title": "이메일 중복",
                                              "status": 409,
                                              "detail": "이미 사용 중인 이메일입니다.",
                                              "instance": "/api/auth/signup"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/signup")
    ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request);

    @Operation(
            summary = "로그인",
            description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다. 토큰은 HttpOnly 쿠키에 자동으로 저장됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(
                            schema = @Schema(implementation = LoginResponse.class),
                            examples = @ExampleObject(
                                    name = "로그인 성공 예시",
                                    value = """
                                            {
                                              "userId": 1,
                                              "email": "user@example.com",
                                              "username": "홍길동",
                                              "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "사용자 미존재",
                                    value = """
                                            {
                                              "title": "사용자를 찾을 수 없음",
                                              "status": 404,
                                              "detail": "요청한 ID의 사용자가 존재하지 않습니다.",
                                              "instance": "/api/auth/login"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "비밀번호 불일치",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "비밀번호 불일치",
                                    value = """
                                            {
                                              "title": "잘못된 비밀번호",
                                              "status": 401,
                                              "detail": "비밀번호가 일치하지 않습니다.",
                                              "instance": "/api/auth/login"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/login")
    ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse httpResponse
    );

    @Operation(
            summary = "로그아웃",
            description = "현재 로그인된 사용자를 로그아웃합니다. 쿠키에서 JWT 토큰을 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그아웃 성공"
            )
    })
    @PostMapping("/logout")
    ResponseEntity<Void> logout(HttpServletResponse httpResponse);
}