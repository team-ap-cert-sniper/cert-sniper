package com.certsniper.global.config;

import com.certsniper.global.security.LoginUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.Arrays;
import java.util.List;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;

/** Swagger(OpenAPI) 설정. JWT Bearer 인증 스키마를 등록하고, 로그인 없이 쓰는 API는 인증 표시를 뺀다. */
@Configuration
public class SwaggerConfig {

    private static final String JWT = "JWT";

    static {
        SpringDocUtils.getConfig().addAnnotationsToIgnore(LoginUser.class);
    }

    /** 문서의 필드 이름을 실제 JSON(snake_case)과 같게 표시 */
    @Bean
    public ModelResolver modelResolver(ObjectMapper objectMapper) {
        return new ModelResolver(objectMapper);
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("저격증 API")
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(JWT))
                .components(new Components().addSecuritySchemes(JWT,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    /** SecurityConfig의 공개 경로와 같은 API는 문서에서 인증 표시를 뺀다 */
    @Bean
    public OpenApiCustomizer publicApiCustomizer() {
        AntPathMatcher matcher = new AntPathMatcher();
        return openApi -> openApi.getPaths().forEach((path, item) ->
                item.readOperationsMap().forEach((method, operation) -> {
                    boolean loginRequired = method == PathItem.HttpMethod.PATCH
                            && Arrays.stream(SecurityConfig.LOGIN_REQUIRED_PATCH)
                            .anyMatch(pattern -> matcher.match(pattern, path));
                    boolean publicAny = !loginRequired
                            && Arrays.stream(SecurityConfig.PUBLIC_ANY)
                            .anyMatch(pattern -> matcher.match(pattern, path));
                    boolean publicGet = method == PathItem.HttpMethod.GET
                            && Arrays.stream(SecurityConfig.PUBLIC_GET)
                            .anyMatch(pattern -> matcher.match(pattern, path));
                    if (publicAny || publicGet) {
                        operation.setSecurity(List.of());
                    }
                }));
    }
}
