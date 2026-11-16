package cl.skyops.service;

import cl.skyops.dto.LoginRequest;
import cl.skyops.dto.TokenDto;
import cl.skyops.dto.UsuarioDto;
import cl.skyops.model.Usuario;
import cl.skyops.repository.UsuarioRepository;
import cl.skyops.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder encoder, JwtService jwt) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    public TokenDto login(LoginRequest r) {
        Usuario u = usuarios.findByCorreo(r.correo().trim().toLowerCase())
                .filter(x -> encoder.matches(r.clave(), x.getClave()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos."));
        return tokens(u);
    }

    /** Cambia un refresh token válido por un par de tokens nuevos. */
    public TokenDto refrescar(String refreshToken) {
        try {
            Claims claims = jwt.leer(refreshToken);
            if (!"refresh".equals(claims.get("tipo"))) {
                throw new JwtException("No es un refresh token");
            }
            Usuario u = usuarios.findById(Long.valueOf(claims.getSubject()))
                    .orElseThrow(() -> new JwtException("El usuario ya no existe"));
            return tokens(u); // el rol se lee de la BD, así un cambio de rol se aplica al renovar
        } catch (JwtException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "La sesión venció. Inicia sesión nuevamente.");
        }
    }

    private TokenDto tokens(Usuario u) {
        return new TokenDto(jwt.generarAcceso(u), jwt.generarRefresh(u), UsuarioDto.de(u));
    }
}
