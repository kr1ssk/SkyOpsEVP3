package cl.skyops.security;

import cl.skyops.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Crea y verifica tokens JWT firmados con HMAC-SHA256.
 * Hay dos tipos: "acceso" (15 min, se envía en cada petición) y "refresh" (7 días, solo sirve para pedir tokens nuevos).
 * El subject del token es el id del usuario y el claim "rol" guarda su rol.
 */
@Service
public class JwtService {

    private final SecretKey clave;
    private final Duration duracionAcceso;
    private final Duration duracionRefresh;

    public JwtService(@Value("${jwt.secret}") String secreto,
                      @Value("${jwt.acceso-minutos}") long accesoMinutos,
                      @Value("${jwt.refresh-dias}") long refreshDias) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8)); // exige 32+ caracteres
        this.duracionAcceso = Duration.ofMinutes(accesoMinutos);
        this.duracionRefresh = Duration.ofDays(refreshDias);
    }

    public String generarAcceso(Usuario u) {
        return construir(u, "acceso", duracionAcceso);
    }

    public String generarRefresh(Usuario u) {
        return construir(u, "refresh", duracionRefresh);
    }

    /** Verifica firma y vencimiento. Lanza JwtException si el token no sirve. */
    public Claims leer(String token) {
        return Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
    }

    private String construir(Usuario u, String tipo, Duration duracion) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(String.valueOf(u.getId()))
                .claim("rol", u.getRol().name())
                .claim("tipo", tipo)
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + duracion.toMillis()))
                .signWith(clave)
                .compact();
    }
}
