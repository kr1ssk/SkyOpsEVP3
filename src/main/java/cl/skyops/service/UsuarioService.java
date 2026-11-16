package cl.skyops.service;

import cl.skyops.dto.UsuarioDto;
import cl.skyops.dto.UsuarioRequest;
import cl.skyops.model.Rol;
import cl.skyops.model.Usuario;
import cl.skyops.repository.BoletaRepository;
import cl.skyops.repository.UsuarioRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final BoletaRepository boletas;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository usuarios, BoletaRepository boletas, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.boletas = boletas;
        this.encoder = encoder;
    }

    public List<UsuarioDto> listar() {
        return usuarios.findAll().stream().map(UsuarioDto::de).toList();
    }

    public UsuarioDto obtener(Long id) {
        return UsuarioDto.de(buscar(id));
    }

    /** El que llama decide el rol: el registro público siempre pasa CLIENTE. */
    public UsuarioDto crear(UsuarioRequest r, Rol rol) {
        if (r.clave() == null || r.clave().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usa al menos 8 caracteres con letras y números.");
        }
        return guardar(new Usuario(), r, rol);
    }

    /** puedeCambiarRol es true solo cuando edita un ADMIN; en "mi perfil" se ignora el rol enviado. */
    public UsuarioDto actualizar(Long id, UsuarioRequest r, boolean puedeCambiarRol) {
        Usuario u = buscar(id);
        Rol rol = puedeCambiarRol && r.rol() != null ? r.rol() : u.getRol();
        if (u.getRol() == Rol.ADMIN && rol != Rol.ADMIN && usuarios.countByRol(Rol.ADMIN) == 1) {
            throw conflicto("Debe quedar al menos un administrador.");
        }
        return guardar(u, r, rol);
    }

    public void eliminar(Long id, Long idSesion) {
        Usuario u = buscar(id);
        if (id.equals(idSesion)) {
            throw conflicto("No puedes eliminar tu propia cuenta.");
        }
        if (u.getRol() == Rol.ADMIN && usuarios.countByRol(Rol.ADMIN) == 1) {
            throw conflicto("Debe quedar al menos un administrador.");
        }
        if (boletas.existsByUsuarioId(id)) {
            throw conflicto("El usuario tiene boletas asociadas.");
        }
        usuarios.delete(u);
    }

    private Usuario buscar(Long id) {
        return usuarios.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario no existe."));
    }

    private UsuarioDto guardar(Usuario u, UsuarioRequest r, Rol rol) {
        String correo = r.correo().trim().toLowerCase();
        usuarios.findByCorreo(correo).ifPresent(otro -> {
            if (!otro.getId().equals(u.getId())) {
                throw conflicto("Ese correo ya está registrado.");
            }
        });
        u.setNombre(r.nombre().trim());
        u.setCorreo(correo);
        u.setRol(rol);
        u.setDireccion(r.direccion() == null ? "" : r.direccion().trim());
        u.setLicencia(r.licencia() == null ? "" : r.licencia().trim());
        if (r.clave() != null && !r.clave().isBlank()) {
            u.setClave(encoder.encode(r.clave())); // se guarda el hash, nunca la clave
        }
        return UsuarioDto.de(usuarios.save(u));
    }

    private ResponseStatusException conflicto(String mensaje) {
        return new ResponseStatusException(HttpStatus.CONFLICT, mensaje);
    }
}
