package com.autoguide.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Autoguide Hotel Booking API")
                        .version("v1")
                        .description("Reactive hotel booking backend with PostgreSQL, MongoDB, Redis and Keycloak")
                        .contact(new Contact().name("Autoguide Booking Team"))
                        .license(new License().name("MIT")));
    }
}
