package cl.skyops.controller;

import cl.skyops.dto.CategoriaDto;
import cl.skyops.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/v1/categorias")
@Tag(name = "Categorías", description = "Capítulos ATA. Leer es público; crear, editar y borrar es solo ADMIN.")
public class CategoriaController {

    private final CategoriaService categorias;

    public CategoriaController(CategoriaService categorias) {
        this.categorias = categorias;
    }

    @GetMapping
    @Operation(summary = "Lista las categorías (público)")
    public List<CategoriaDto> listar() {
        return categorias.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearer")
    @Operation(summary = "Crea una categoría con formato ATA 27 (ADMIN). Repetida: 409.")
    public CategoriaDto crear(@Valid @RequestBody CategoriaDto dto) {
        return categorias.crear(dto);
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearer")
    @Operation(summary = "Renombra una categoría (ADMIN). No existe: 404.")
    public CategoriaDto actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaDto dto) {
        return categorias.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearer")
    @Operation(summary = "Elimina una categoría (ADMIN, 204). Con productos asociados: 409.")
    public void eliminar(@PathVariable Long id) {
        categorias.eliminar(id);
    }
}
