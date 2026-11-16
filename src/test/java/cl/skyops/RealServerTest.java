package cl.skyops;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

/**
 * Con un servidor Tomcat real (MockMvc no pasa por el contenedor). Aquí falló una vez un 403 que salía como 401,
 * porque Tomcat reenvía los errores a /error y esa ruta exigía autenticación.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RealServerTest {

    @Autowired
    private TestRestTemplate http;

    @Test
    void clienteEnRutaDeAdminRecibe403YSinTokenRecibe401() {
        var login = http.postForObject("/api/v1/auth/login",
                Map.of("correo", "cliente@skyops.cl", "clave", "Skyops123"), String.class);
        String token = JsonPath.read(login, "$.accessToken");

        var cabeceras = new HttpHeaders();
        cabeceras.setBearerAuth(token);
        var conToken = http.exchange("/api/v1/usuarios", HttpMethod.GET, new HttpEntity<>(cabeceras), String.class);
        assertThat(conToken.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        var sinToken = http.getForEntity("/api/v1/usuarios", String.class);
        assertThat(sinToken.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
