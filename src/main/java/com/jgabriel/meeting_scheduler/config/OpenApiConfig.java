package com.jgabriel.meeting_scheduler.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Time Block Availability Service")
                        .version("1.0.0")
                        .description("Backend service handling high-concurreny scheduling.")
                        .contact(new Contact()
                                .name("Gabriel")
                                .url("https://github.com/gabrielmiranda94")
                        )
                );
    }
}