package cl.skyops.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContactoRequest(
        @NotBlank @Size(min = 3, message = "Ingresa tu nombre.") String nombre,
        @NotBlank(message = "Ingresa un correo válido.") @Email(message = "Ingresa un correo válido.") String correo,
        @NotBlank @Size(min = 20, max = 2000, message = "Escribe un mensaje de al menos 20 caracteres.") String mensaje) {
}
