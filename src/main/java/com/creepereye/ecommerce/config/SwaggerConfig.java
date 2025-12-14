package com.creepereye.ecommerce.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("Ecommerce API")
                .version("v1.0.0")
                .description("API for the Ecommerce application.");

        return new OpenAPI()
                .components(new Components())
                .info(info);
    }
}
