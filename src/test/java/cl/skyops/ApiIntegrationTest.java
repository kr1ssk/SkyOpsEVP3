package cl.skyops;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Pruebas de integración: levantan toda la app y llaman a la API como lo haría el frontend.
 * Corren sobre H2 en memoria (src/test/resources/application.properties), sin necesitar MySQL.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;

    @Test
    void corsPermiteAlFrontendYNoAOtrosOrigenes() throws Exception {
        mvc.perform(options("/api/v1/productos")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));

        mvc.perform(options("/api/v1/productos")
                        .header("Origin", "http://sitio-malo.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }

    @Test
    void loginConClaveMalaDevuelve401() throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("correo", "admin@skyops.cl", "clave", "mala"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void catalogoEsPublicoPeroCrearRequiereAdmin() throws Exception {
        mvc.perform(get("/api/v1/productos")).andExpect(status().isOk());

        String body = json(producto("PN-" + UUID.randomUUID(), 5));
        mvc.perform(post("/api/v1/productos").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());

        mvc.perform(post("/api/v1/productos").header("Authorization", "Bearer " + tokenCliente())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());

        mvc.perform(post("/api/v1/productos").header("Authorization", "Bearer " + tokenAdmin())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void crudDeProductoComoAdmin() throws Exception {
        String admin = "Bearer " + tokenAdmin();
        String pn = "PN-" + UUID.randomUUID();

        var creado = mvc.perform(post("/api/v1/productos").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content(json(producto(pn, 5))))
                .andExpect(status().isCreated()).andReturn();
        int id = JsonPath.read(creado.getResponse().getContentAsString(), "$.id");

        mvc.perform(get("/api/v1/productos/" + id)).andExpect(status().isOk()).andExpect(jsonPath("$.stock").value(5));

        mvc.perform(put("/api/v1/productos/" + id).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content(json(producto(pn, 9))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.stock").value(9));

        // mismo PN en otro producto: 409
        mvc.perform(post("/api/v1/productos").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content(json(producto(pn, 1))))
                .andExpect(status().isConflict());

        // datos inválidos: 400
        mvc.perform(post("/api/v1/productos").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content(json(producto("PN-" + UUID.randomUUID(), -1))))
                .andExpect(status().isBadRequest());

        mvc.perform(delete("/api/v1/productos/" + id).header("Authorization", admin)).andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/productos/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void refreshRenuevaTokensYNoSirveComoTokenDeAcceso() throws Exception {
        var login = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("correo", "admin@skyops.cl", "clave", "Skyops123"))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String refresh = JsonPath.read(login, "$.refreshToken");

        mvc.perform(get("/api/v1/usuarios/me").header("Authorization", "Bearer " + refresh))
                .andExpect(status().isUnauthorized());

        mvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("refreshToken", refresh))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").isNotEmpty());

        mvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("refreshToken", "token.falso.aqui"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void compraDescuentaStockSoloSiEsAprobadaYNoPermiteSobrepasarlo() throws Exception {
        String admin = "Bearer " + tokenAdmin();
        var creado = mvc.perform(post("/api/v1/productos").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content(json(producto("PN-" + UUID.randomUUID(), 5))))
                .andReturn().getResponse().getContentAsString();
        int id = JsonPath.read(creado, "$.id");
        String cliente = "Bearer " + tokenCliente();

        // aprobada: 2 unidades a 1000 con 10% de descuento = 1800, stock 5 -> 3
        var boleta = mvc.perform(post("/api/v1/boletas").header("Authorization", cliente)
                        .contentType(MediaType.APPLICATION_JSON).content(json(compra(id, 2, true))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(1800))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.detalles[0].subtotal").value(1800))
                .andReturn().getResponse().getContentAsString();
        int boletaId = JsonPath.read(boleta, "$.id");
        mvc.perform(get("/api/v1/productos/" + id)).andExpect(jsonPath("$.stock").value(3));

        // más que el stock: 409 y el stock no cambia
        mvc.perform(post("/api/v1/boletas").header("Authorization", cliente)
                        .contentType(MediaType.APPLICATION_JSON).content(json(compra(id, 4, true))))
                .andExpect(status().isConflict());

        // rechazada: se registra pero no toca el stock
        mvc.perform(post("/api/v1/boletas").header("Authorization", cliente)
                        .contentType(MediaType.APPLICATION_JSON).content(json(compra(id, 1, false))))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.estado").value("RECHAZADO"));
        mvc.perform(get("/api/v1/productos/" + id)).andExpect(jsonPath("$.stock").value(3));

        // un producto ya vendido no se puede borrar
        mvc.perform(delete("/api/v1/productos/" + id).header("Authorization", admin)).andExpect(status().isConflict());

        // el cliente ve solo sus 2 boletas y no puede resolverlas
        mvc.perform(get("/api/v1/boletas").header("Authorization", cliente))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        mvc.perform(put("/api/v1/boletas/" + boletaId + "/resolver").header("Authorization", cliente))
                .andExpect(status().isForbidden());

        // otro cliente no puede ver una boleta ajena
        mvc.perform(get("/api/v1/boletas/" + boletaId).header("Authorization", "Bearer " + tokenCliente()))
                .andExpect(status().isForbidden());

        // el admin la resuelve una vez; la segunda vez es 409
        mvc.perform(put("/api/v1/boletas/" + boletaId + "/resolver").header("Authorization", admin))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("RESUELTO"));
        mvc.perform(put("/api/v1/boletas/" + boletaId + "/resolver").header("Authorization", admin))
                .andExpect(status().isConflict());
    }

    @Test
    void vendedorSoloPuedeVerProductosYBoletas() throws Exception {
        String vendedor = "Bearer " + login("vendedor@skyops.cl", "Skyops123");

        mvc.perform(get("/api/v1/productos")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/boletas").header("Authorization", vendedor)).andExpect(status().isOk());

        // todo lo demás: 403
        mvc.perform(post("/api/v1/productos").header("Authorization", vendedor)
                        .contentType(MediaType.APPLICATION_JSON).content(json(producto("PN-" + UUID.randomUUID(), 5))))
                .andExpect(status().isForbidden());
        // sin permiso se responde 403 antes de validar el cuerpo
        mvc.perform(post("/api/v1/productos").header("Authorization", vendedor)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/boletas").header("Authorization", vendedor)
                        .contentType(MediaType.APPLICATION_JSON).content(json(compra(1, 1, true))))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/usuarios").header("Authorization", vendedor)).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/contactos").header("Authorization", vendedor)).andExpect(status().isForbidden());
    }

    // ---------- ayudas ----------

    private String json(Object o) throws Exception {
        return json.writeValueAsString(o);
    }

    private String login(String correo, String clave) throws Exception {
        var respuesta = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("correo", correo, "clave", clave))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(respuesta, "$.accessToken");
    }

    private String tokenAdmin() throws Exception {
        return login("admin@skyops.cl", "Skyops123");
    }

    /** Registra un cliente nuevo (así cada prueba es independiente) y devuelve su token. */
    private String tokenCliente() throws Exception {
        String correo = UUID.randomUUID() + "@test.cl";
        mvc.perform(post("/api/v1/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nombre", "Cliente Prueba", "correo", correo, "clave", "Clave1234",
                                "rol", "ADMIN")))) // el rol enviado se ignora: el registro siempre crea CLIENTE
                .andExpect(status().isCreated()).andExpect(jsonPath("$.rol").value("CLIENTE"));
        return login(correo, "Clave1234");
    }

    private Map<String, Object> producto(String pn, int stock) {
        return Map.of("nombre", "Repuesto de prueba", "pn", pn, "categoria", "ATA 27",
                "descripcion", "Repuesto usado en las pruebas.", "ubicacion", "Bodega T-01",
                "stock", stock, "precio", 1000, "descuento", 10, "certificado", true, "imagen", "");
    }

    private Map<String, Object> compra(int productoId, int cantidad, boolean aprobada) {
        return Map.of(
                "entrega", Map.of("matricula", "CC-BFA", "ingeniero", "Juan Perez", "licencia", "AB-1234",
                        "correo", "juan@test.cl", "direccion", "Av. Siempre Viva 123", "destino", "SCL",
                        "tipoEntrega", "Retiro en bodega"),
                "detalles", List.of(Map.of("productoId", productoId, "cantidad", cantidad)),
                "aprobada", aprobada);
    }
}
