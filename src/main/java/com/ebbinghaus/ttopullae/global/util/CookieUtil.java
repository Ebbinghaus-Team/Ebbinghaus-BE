package com.ebbinghaus.ttopullae.global.util;

import com.ebbinghaus.ttopullae.global.exception.ApplicationException;
import com.ebbinghaus.ttopullae.user.exception.UserException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;

/**
 * 쿠키 관련 유틸리티 클래스
 * JWT 토큰을 HttpOnly 쿠키에 저장하고 추출하는 기능 제공
 */
public class CookieUtil {

    private static final String TOKEN_COOKIE_NAME = "accessToken";

    /**
     * 응답 쿠키에 JWT 토큰 설정
     * @param accessToken JWT 토큰 값
     * @param cookieExpirationSeconds 쿠키 만료 시간 (초)
     * @param response HTTP 응답 객체
     */
    public static void setToken(String accessToken, int cookieExpirationSeconds, HttpServletResponse response) {

        ResponseCookie cookie = ResponseCookie.from(TOKEN_COOKIE_NAME, accessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(cookieExpirationSeconds)
                .sameSite("None")   // Lux 로 변경
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    /**
     * 쿠키에서 JWT 토큰 추출
     * @param cookies 요청에 포함된 쿠키 배열
     * @return JWT 토큰 값, 없으면 null
     */
    public static String extractToken(Cookie[] cookies) {
        if (cookies == null) {
            throw new ApplicationException(UserException.TOKEN_NOT_FOUND);
        }

        for (Cookie cookie : cookies) {
            if (TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        throw new ApplicationException(UserException.TOKEN_NOT_FOUND);
    }

    /**
     * 쿠키 삭제 (로그아웃 시 사용)
     * @param response HTTP 응답 객체
     */
    public static void deleteCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }
}
