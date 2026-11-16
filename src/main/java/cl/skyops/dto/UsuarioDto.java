package cl.skyops.dto;

import cl.skyops.model.Rol;
import cl.skyops.model.Usuario;

/** Usuario hacia afuera: nunca incluye la clave. */
public record UsuarioDto(Long id, String nombre, String correo, Rol rol, String direccion, String licencia) {

    public static UsuarioDto de(Usuario u) {
        return new UsuarioDto(u.getId(), u.getNombre(), u.getCorreo(), u.getRol(), u.getDireccion(), u.getLicencia());
    }
}
