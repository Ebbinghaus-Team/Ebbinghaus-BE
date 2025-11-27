package com.ebbinghaus.ttopullae.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@OpenAPIDefinition(
        info = @io.swagger.v3.oas.annotations.info.Info(
                title = "Ebbinghaus: tto-pul-lae",
                description = """
                        ### 또풀래(가제)
                        #### [Github](https://github.com/Ebbinghaus-Team/Ebbinghaus-BE)

                        ---

                        ### 인증 방식
                        이 API는 **JWT 쿠키 기반 인증**을 사용합니다.

                        1. `/api/auth/login` API로 로그인하면 HttpOnly 쿠키에 JWT 토큰이 자동으로 저장됩니다.
                        2. 이후 모든 API 요청에 자동으로 쿠키가 포함됩니다.
                        3. `/api/auth/logout` API로 로그아웃하면 쿠키가 삭제됩니다.

                        **Swagger UI에서 테스트하는 방법:**
                        1. 먼저 `/api/auth/signup`으로 회원가입하거나 기존 계정 사용
                        2. `/api/auth/login`으로 로그인 (쿠키에 JWT 토큰 자동 저장)
                        3. 우측 상단 "Authorize" 버튼 클릭 후 쿠키 값 입력 (선택사항)
                        4. 인증이 필요한 API 테스트

                        **주의:** Swagger UI에서는 쿠키가 자동으로 전송되지 않을 수 있습니다.
                        브라우저의 개발자 도구를 통해 실제 쿠키를 확인하고 테스트하세요.
                        """,
                version = "2.0"
        )
)
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "cookieAuth";

    @Bean
    public OpenAPI openAPI() {

        List<Server> servers = List.of(
                new Server().url("http://localhost:8080").description("로컬 서버"),
                new Server().url("https://ebbinghaus.chxghee.com").description("메인 서버")
        );

        return new OpenAPI()
                .servers(servers)
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, createSecurityScheme()))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }

    /**
     * JWT 쿠키 기반 인증을 위한 Security Scheme 생성
     */
    private SecurityScheme createSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.COOKIE)
                .name("accessToken")
                .description("JWT 토큰이 저장된 HttpOnly 쿠키");
    }
}
