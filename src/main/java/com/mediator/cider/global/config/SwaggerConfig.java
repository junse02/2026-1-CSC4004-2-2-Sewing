package com.mediator.cider.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        String jwtSchemeName = "jwtAuth";

        // 1. 모든 API 요청 시 헤더에 인증 정보(Authorization)를 포함하도록 설정
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);

        // 2. JWT 인증 스킴 정의 (Bearer 방식)
        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP) // HTTP 방식
                        .scheme("bearer")            // bearer 토큰 사용
                        .bearerFormat("JWT"));       // 포맷은 JWT

        return new OpenAPI()
                .addSecurityItem(securityRequirement)
                .components(components);
    }
}