package cl.skyops.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Se ejecuta en cada petición. Si trae "Authorization: Bearer <token>" válido, deja al usuario autenticado
 * con su rol. Si el token falta, está vencido o es falso, no autentica y Spring Security responde 401.
 * No es un @Component a propósito: se registra solo dentro de la cadena de seguridad (ver SecurityConfig).
 */
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwt;

    public JwtFilter(JwtService jwt) {
        this.jwt = jwt;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String cabecera = request.getHeader("Authorization");
        if (cabecera != null && cabecera.startsWith("Bearer ")) {
            try {
                Claims claims = jwt.leer(cabecera.substring(7));
                if ("acceso".equals(claims.get("tipo"))) { // un refresh token no sirve para entrar a la API
                    var autoridad = new SimpleGrantedAuthority("ROLE_" + claims.get("rol"));
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(claims.getSubject(), null, List.of(autoridad)));
                }
            } catch (JwtException | IllegalArgumentException e) {
                // token inválido o vencido: se sigue sin autenticar
            }
        }
        chain.doFilter(request, response);
    }
}
