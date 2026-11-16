package cl.skyops.dto;

/** Respuesta de login y refresh: token de acceso (corto), token de renovación (largo) y el usuario. */
public record TokenDto(String accessToken, String refreshToken, UsuarioDto usuario) {
}
