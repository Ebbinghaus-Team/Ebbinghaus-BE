package com.ebbinghaus.ttopullae.global.config;

import com.ebbinghaus.ttopullae.global.auth.JwtAuthenticationInterceptor;
import com.ebbinghaus.ttopullae.global.auth.LoginUserArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Spring MVC 웹 설정
 * - JWT 인증 인터셉터 등록
 * - @LoginUser ArgumentResolver 등록
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final JwtAuthenticationInterceptor jwtAuthenticationInterceptor;
    private final LoginUserArgumentResolver loginUserArgumentResolver;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthenticationInterceptor)
                .addPathPatterns("/api/**") // /api로 시작하는 모든 경로에 인터셉터 적용
                .excludePathPatterns(
                        "/api/auth/signup",  // 회원가입은 인증 불필요
                        "/api/auth/login",   // 로그인은 인증 불필요
                        "/api/auth/logout",  // 로그아웃은 인증 불필요 (쿠키만 삭제)
                        "/docs/**",          // Swagger 문서는 인증 불필요
                        "/swagger-ui/**",    // Swagger UI는 인증 불필요
                        "/v3/api-docs/**"    // OpenAPI 문서는 인증 불필요
                );
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginUserArgumentResolver);
    }
}