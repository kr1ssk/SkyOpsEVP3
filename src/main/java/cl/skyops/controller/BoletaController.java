package cl.skyops.controller;

import cl.skyops.dto.BoletaDto;
import cl.skyops.dto.BoletaRequest;
import cl.skyops.service.BoletaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/boletas")
@Tag(name = "Boletas", description = "Compras (órdenes). Comprar: CLIENTE o ADMIN. Ver: todos con sesión. Resolver: ADMIN.")
@SecurityRequirement(name = "bearer")
public class BoletaController {

    private final BoletaService boletas;

    public BoletaController(BoletaService boletas) {
        this.boletas = boletas;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Confirma el manifiesto y crea la boleta (CLIENTE, ADMIN). Descuenta stock si aprobada=true. Sin stock: 409.")
    public BoletaDto crear(Authentication auth, @Valid @RequestBody BoletaRequest r) {
        return boletas.crear(idDe(auth), r);
    }

    @GetMapping
    @Operation(summary = "Lista boletas: todas si eres ADMIN o VENDEDOR, solo las tuyas si eres CLIENTE")
    public List<BoletaDto> listar(Authentication auth) {
        return boletas.listar(idDe(auth), veTodas(auth));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de una boleta. Un CLIENTE solo puede ver las suyas (403). No existe: 404.")
    public BoletaDto obtener(@PathVariable Long id, Authentication auth) {
        return boletas.obtener(id, idDe(auth), veTodas(auth));
    }

    @PutMapping("/{id}/resolver")
    @Operation(summary = "Marca una boleta PENDIENTE como RESUELTO (ADMIN). Otro estado: 409.")
    public BoletaDto resolver(@PathVariable Long id) {
        return boletas.resolver(id);
    }

    private Long idDe(Authentication auth) {
        return Long.valueOf(auth.getName());
    }

    private boolean veTodas(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_VENDEDOR"));
    }
}
