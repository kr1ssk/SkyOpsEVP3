package cl.skyops.dto;

import cl.skyops.model.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Datos de un usuario al registrarse, al editar el perfil o cuando el admin lo administra.
 * "clave" es obligatoria al crear y opcional al editar. "rol" solo lo respeta el admin.
 */
public record UsuarioRequest(
        @NotBlank(message = "Ingresa el nombre.") String nombre,
        @NotBlank(message = "Ingresa un correo válido.") @Email(message = "Ingresa un correo válido.") String correo,
        @Pattern(regexp = "^$|^(?=.*[A-Za-z])(?=.*\\d).{8,}$", message = "Usa al menos 8 caracteres con letras y números.") String clave,
        Rol rol,
        String direccion,
        String licencia) {
}
