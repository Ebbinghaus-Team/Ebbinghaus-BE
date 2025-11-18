package com.ebbinghaus.ttopullae.global.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

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
        Cookie cookie = new Cookie(TOKEN_COOKIE_NAME, accessToken);
        cookie.setHttpOnly(true); // XSS 공격 방지
        cookie.setSecure(false); // HTTPS 환경에서만 전송 (개발 환경에서는 false)
        cookie.setPath("/"); // 모든 경로에서 쿠키 접근 가능
        cookie.setMaxAge(cookieExpirationSeconds); // 쿠키 만료 시간 설정
        cookie.setAttribute("SameSite", "None"); // CSRF 방지 (개발 환경 호환성)

        response.addCookie(cookie);
    }

    /**
     * 쿠키에서 JWT 토큰 추출
     * @param cookies 요청에 포함된 쿠키 배열
     * @return JWT 토큰 값, 없으면 null
     */
    public static String extractToken(Cookie[] cookies) {
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    /**
     * 쿠키 삭제 (로그아웃 시 사용)
     * @param response HTTP 응답 객체
     */
    public static void deleteCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(TOKEN_COOKIE_NAME, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0); // 즉시 만료
        cookie.setAttribute("SameSite", "None");

        response.addCookie(cookie);
    }
}