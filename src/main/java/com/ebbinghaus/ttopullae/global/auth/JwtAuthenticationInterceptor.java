package com.ebbinghaus.ttopullae.global.auth;

import com.ebbinghaus.ttopullae.global.exception.ApplicationException;
import com.ebbinghaus.ttopullae.global.util.CookieUtil;
import com.ebbinghaus.ttopullae.global.util.JwtTokenProvider;
import com.ebbinghaus.ttopullae.user.exception.UserException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 토큰 인증을 처리하는 인터셉터
 * 쿠키에서 토큰을 추출하고 검증한 후, 사용자 ID를 request attribute에 저장
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private static final String USER_ID_ATTRIBUTE = "userId";

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 쿠키에서 JWT 토큰 추출
        String token = CookieUtil.extractToken(request.getCookies());

        // 토큰 유효성 검증 (예외 발생 시 GlobalExceptionHandler에서 처리)
        jwtTokenProvider.validateToken(token);

        // 토큰에서 사용자 ID 추출 후 request attribute에 저장
        Long userId = jwtTokenProvider.getUserId(token);
        request.setAttribute(USER_ID_ATTRIBUTE, userId);

        log.debug("사용자 인증 성공. userId: {}, URI: {}", userId, request.getRequestURI());
        return true;
    }
}
