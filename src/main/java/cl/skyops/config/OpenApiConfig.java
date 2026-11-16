package cl.skyops.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/** Swagger UI en http://localhost:8080/swagger-ui.html. El botón "Authorize" recibe el accessToken del login. */
@Configuration
@OpenAPIDefinition(info = @Info(title = "SkyOps API", version = "1.0",
        description = "API REST de la plataforma de abastecimiento de repuestos aeronáuticos SkyOps."))
@SecurityScheme(name = "bearer", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {
}
