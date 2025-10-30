package com.nhonguoiyeucu.openlinkedhub.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@OpenAPIDefinition(
        info = @Info(
                title = "OpenLinkedHub — Data Core API",
                version = "v1",
                description = "REST APIs cho dữ liệu quận/huyện và doanh nghiệp",
                contact = @Contact(name = "OpenLinkedHub Team", email = "team@example.com")
        )
)
@Configuration
public class SwaggerConfig {


    /** API Key security cho nhóm admin (header X-API-KEY) */
    @Bean
    public OpenAPI baseOpenAPI() {
        String schemeName = "ApiKeyAuth";
        SecurityScheme apiKey = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("X-API-KEY");
        return new OpenAPI()
                .components(new Components().addSecuritySchemes(schemeName, apiKey))
                .addSecurityItem(new SecurityRequirement().addList(schemeName));
    }


    @Bean
    public GroupedOpenApi publicApis() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/api/v1/data/**")
                .build();
    }


    @Bean
    public GroupedOpenApi adminApis() {
        return GroupedOpenApi.builder()
                .group("admin")
                .pathsToMatch("/api/v1/admin/**")
                .build();
    }
}
