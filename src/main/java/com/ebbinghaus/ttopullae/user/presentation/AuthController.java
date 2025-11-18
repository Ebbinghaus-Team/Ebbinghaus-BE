package com.ebbinghaus.ttopullae.user.presentation;

import com.ebbinghaus.ttopullae.global.util.CookieUtil;
import com.ebbinghaus.ttopullae.user.application.AuthService;
import com.ebbinghaus.ttopullae.user.application.dto.LoginResult;
import com.ebbinghaus.ttopullae.user.application.dto.SignupResult;
import com.ebbinghaus.ttopullae.user.presentation.dto.LoginRequest;
import com.ebbinghaus.ttopullae.user.presentation.dto.LoginResponse;
import com.ebbinghaus.ttopullae.user.presentation.dto.SignupRequest;
import com.ebbinghaus.ttopullae.user.presentation.dto.SignupResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 관련 API를 제공하는 컨트롤러
 */
@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${jwt.cookie-expiration-seconds}")
    private int cookieExpirationSeconds;

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "회원가입 성공",
                    content = @Content(schema = @Schema(implementation = SignupResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이메일 중복"
            )
    })
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        SignupResult result = authService.signup(request.toCommand());
        SignupResponse response = SignupResponse.from(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다. 토큰은 HttpOnly 쿠키에 자동으로 저장됩니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "비밀번호 불일치"
            )
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse httpResponse
    ) {
        LoginResult result = authService.login(request.toCommand());

        // JWT 토큰을 HttpOnly 쿠키에 저장
        CookieUtil.setToken(result.accessToken(), cookieExpirationSeconds, httpResponse);

        LoginResponse response = LoginResponse.from(result);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "로그아웃", description = "현재 로그인된 사용자를 로그아웃합니다. 쿠키에서 JWT 토큰을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그아웃 성공"
            )
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse httpResponse) {
        // 쿠키에서 JWT 토큰 삭제
        CookieUtil.deleteCookie(httpResponse);
        return ResponseEntity.ok().build();
    }
}