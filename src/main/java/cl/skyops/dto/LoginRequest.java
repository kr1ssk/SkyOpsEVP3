package cl.skyops.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Ingresa el correo.") String correo,
        @NotBlank(message = "Ingresa la contraseña.") String clave) {
}
