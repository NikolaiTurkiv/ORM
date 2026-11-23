package ru.study.platform.web.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Learning Platform API", version = "1.0",
        description = "Учебная платформа: курсы, задания и тесты"))
public class OpenApiConfig {
}
