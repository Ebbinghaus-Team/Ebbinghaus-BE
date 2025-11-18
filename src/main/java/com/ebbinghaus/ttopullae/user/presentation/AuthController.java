package com.ebbinghaus.ttopullae.user.presentation;

import com.ebbinghaus.ttopullae.global.util.CookieUtil;
import com.ebbinghaus.ttopullae.user.application.AuthService;
import com.ebbinghaus.ttopullae.user.application.dto.LoginResult;
import com.ebbinghaus.ttopullae.user.application.dto.SignupResult;
import com.ebbinghaus.ttopullae.user.presentation.dto.LoginRequest;
import com.ebbinghaus.ttopullae.user.presentation.dto.LoginResponse;
import com.ebbinghaus.ttopullae.user.presentation.dto.SignupRequest;
import com.ebbinghaus.ttopullae.user.presentation.dto.SignupResponse;
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
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerDocs {

    private final AuthService authService;

    @Value("${jwt.cookie-expiration-seconds}")
    private int cookieExpirationSeconds;

    /**
     * 회원가입
     *
     * @param request 회원가입 요청 정보
     * @return 생성된 사용자 정보
     */
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        SignupResult result = authService.signup(request.toCommand());
        SignupResponse response = SignupResponse.from(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 로그인
     *
     * @param request 로그인 요청 정보
     * @param httpResponse HTTP 응답 객체 (쿠키 설정용)
     * @return JWT 토큰 및 사용자 정보
     */
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

    /**
     * 로그아웃
     *
     * @param httpResponse HTTP 응답 객체 (쿠키 삭제용)
     * @return 성공 응답
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse httpResponse) {
        // 쿠키에서 JWT 토큰 삭제
        CookieUtil.deleteCookie(httpResponse);
        return ResponseEntity.ok().build();
    }
}