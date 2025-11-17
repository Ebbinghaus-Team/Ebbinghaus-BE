package com.ebbinghaus.ttopullae.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @io.swagger.v3.oas.annotations.info.Info(
                title = "Ebbinghaus: tto-pul-lae",
                description = """
                        ### 또풀래(가제)
                        #### [Github](https://github.com/Ebbinghaus-Team/Ebbinghaus-BE)""",
                version = "1.0v"
        )
)
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI();
    }
}
