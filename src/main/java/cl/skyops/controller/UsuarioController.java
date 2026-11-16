package cl.skyops.controller;

import cl.skyops.dto.UsuarioDto;
import cl.skyops.dto.UsuarioRequest;
import cl.skyops.model.Rol;
import cl.skyops.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuarios", description = "Administración de usuarios (ADMIN). /me es para cualquier usuario con sesión.")
@SecurityRequirement(name = "bearer")
public class UsuarioController {

    private final UsuarioService usuarios;

    public UsuarioController(UsuarioService usuarios) {
        this.usuarios = usuarios;
    }

    @GetMapping("/me")
    @Operation(summary = "Devuelve el perfil del usuario con sesión")
    public UsuarioDto miPerfil(Authentication auth) {
        return usuarios.obtener(idDe(auth));
    }

    @PutMapping("/me")
    @Operation(summary = "Actualiza el perfil propio. El rol enviado se ignora. Clave vacía = no cambia.")
    public UsuarioDto actualizarPerfil(Authentication auth, @Valid @RequestBody UsuarioRequest r) {
        return usuarios.actualizar(idDe(auth), r, false);
    }

    @GetMapping
    @Operation(summary = "Lista los usuarios (ADMIN)")
    public List<UsuarioDto> listar() {
        return usuarios.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un usuario por id (ADMIN). No existe: 404.")
    public UsuarioDto obtener(@PathVariable Long id) {
        return usuarios.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crea un usuario con el rol indicado, CLIENTE si no se indica (ADMIN). Correo repetido: 409.")
    public UsuarioDto crear(@Valid @RequestBody UsuarioRequest r) {
        return usuarios.crear(r, r.rol() != null ? r.rol() : Rol.CLIENTE);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza un usuario y su rol (ADMIN). No se puede dejar el sistema sin administradores (409).")
    public UsuarioDto actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest r) {
        return usuarios.actualizar(id, r, true);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina un usuario (ADMIN, 204). A sí mismo, al último admin o con pedidos: 409.")
    public void eliminar(@PathVariable Long id, Authentication auth) {
        usuarios.eliminar(id, idDe(auth));
    }

    private Long idDe(Authentication auth) {
        return Long.valueOf(auth.getName());
    }
}
