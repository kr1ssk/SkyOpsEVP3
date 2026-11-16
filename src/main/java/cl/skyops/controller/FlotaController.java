package cl.skyops.controller;

import cl.skyops.model.Aeronave;
import cl.skyops.repository.AeronaveRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Solo lectura: la flota se carga al iniciar (ver DataLoader). */
@RestController
@RequestMapping("/api/v1/flota")
@Tag(name = "Flota", description = "Aeronaves registradas. Público, solo lectura.")
public class FlotaController {

    private final AeronaveRepository aeronaves;

    public FlotaController(AeronaveRepository aeronaves) {
        this.aeronaves = aeronaves;
    }

    @GetMapping
    @Operation(summary = "Lista las aeronaves (público)")
    public List<Aeronave> listar() {
        return aeronaves.findAll();
    }
}
