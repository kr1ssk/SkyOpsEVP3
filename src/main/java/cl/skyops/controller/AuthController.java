package cl.skyops.controller;

import cl.skyops.dto.LoginRequest;
import cl.skyops.dto.RefreshRequest;
import cl.skyops.dto.TokenDto;
import cl.skyops.dto.UsuarioDto;
import cl.skyops.dto.UsuarioRequest;
import cl.skyops.model.Rol;
import cl.skyops.service.AuthService;
import cl.skyops.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Registro, login y renovación de tokens. Endpoints públicos.")
public class AuthController {

    private final AuthService auth;
    private final UsuarioService usuarios;

    public AuthController(AuthService auth, UsuarioService usuarios) {
        this.auth = auth;
        this.usuarios = usuarios;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra una cuenta nueva con rol CLIENTE (201). Correo repetido: 409.")
    public UsuarioDto registro(@Valid @RequestBody UsuarioRequest r) {
        return usuarios.crear(r, Rol.CLIENTE);
    }

    @PostMapping("/login")
    @Operation(summary = "Inicia sesión y devuelve accessToken + refreshToken (200). Credenciales malas: 401.")
    public TokenDto login(@Valid @RequestBody LoginRequest r) {
        return auth.login(r);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Cambia un refreshToken válido por tokens nuevos (200). Token vencido o falso: 401.")
    public TokenDto refresh(@Valid @RequestBody RefreshRequest r) {
        return auth.refrescar(r.refreshToken());
    }
}
