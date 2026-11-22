package com.certsniper.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Swagger(OpenAPI) 설정. JWT Bearer 인증 스키마를 등록한다. */
@Configuration
public class SwaggerConfig {

    private static final String JWT = "JWT";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("저격증 API")
                        .description("학과 기반 국가자격증 추천 및 학습 관리 플랫폼 · 기능명세서 v1.4.0 기준")
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(JWT))
                .components(new Components().addSecuritySchemes(JWT,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
