package cl.skyops.controller;

import cl.skyops.dto.ProductoDto;
import cl.skyops.service.ProductoService;
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
@RequestMapping("/api/v1/productos")
@Tag(name = "Productos", description = "Catálogo de repuestos. Leer es público; crear, editar y borrar es solo ADMIN.")
public class ProductoController {

    private final ProductoService productos;

    public ProductoController(ProductoService productos) {
        this.productos = productos;
    }

    @GetMapping
    @Operation(summary = "Lista todos los productos (público)")
    public List<ProductoDto> listar() {
        return productos.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un producto por id (público). No existe: 404.")
    public ProductoDto obtener(@PathVariable Long id) {
        return productos.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearer")
    @Operation(summary = "Crea un producto (ADMIN). Datos malos: 400. PN repetido: 409. Sin token: 401. Sin rol: 403.")
    public ProductoDto crear(@Valid @RequestBody ProductoDto dto) {
        return productos.crear(dto);
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearer")
    @Operation(summary = "Actualiza un producto (ADMIN). No existe: 404.")
    public ProductoDto actualizar(@PathVariable Long id, @Valid @RequestBody ProductoDto dto) {
        return productos.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearer")
    @Operation(summary = "Elimina un producto (ADMIN, 204). No existe: 404.")
    public void eliminar(@PathVariable Long id) {
        productos.eliminar(id);
    }
}
