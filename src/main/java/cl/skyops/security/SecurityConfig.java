package cl.skyops.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Aquí están todas las reglas de acceso, en un solo lugar. Spring las revisa en orden y gana la primera que coincide.
 * Se evalúan antes de llegar al controlador, así un usuario sin permiso recibe 403 aunque mande datos inválidos.
 * Sin token: 401. Con token pero sin el rol: 403.
 */
@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, JwtService jwt) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // API sin cookies de sesión: no aplica CSRF
                .cors(Customizer.withDefaults()) // usa la configuración de CorsConfig
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(a -> a
                        // Público: sin token
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/productos/**", "/api/v1/categorias/**", "/api/v1/flota").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/contactos").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // Tomcat reenvía los errores (como el 403) a /error; sin esto un 403 salía como 401
                        .requestMatchers("/error").permitAll()

                        // Solo ADMIN: lo que queda de productos y categorías (crear, editar, borrar),
                        // los mensajes de contacto, los usuarios y resolver boletas
                        .requestMatchers("/api/v1/productos/**", "/api/v1/categorias/**", "/api/v1/contactos/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/usuarios/me").authenticated()
                        .requestMatchers("/api/v1/usuarios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/boletas/*/resolver").hasRole("ADMIN")

                        // Comprar: CLIENTE o ADMIN (el VENDEDOR solo consulta)
                        .requestMatchers(HttpMethod.POST, "/api/v1/boletas").hasAnyRole("CLIENTE", "ADMIN")

                        // Ver boletas: cualquier usuario con sesión (el servicio filtra según el rol)
                        .anyRequest().authenticated())
                .addFilterBefore(new JwtFilter(jwt), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
