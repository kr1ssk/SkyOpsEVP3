package cl.skyops.service;

import cl.skyops.dto.CategoriaDto;
import cl.skyops.model.Categoria;
import cl.skyops.repository.CategoriaRepository;
import cl.skyops.repository.ProductoRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CategoriaService {

    private final CategoriaRepository categorias;
    private final ProductoRepository productos;

    public CategoriaService(CategoriaRepository categorias, ProductoRepository productos) {
        this.categorias = categorias;
        this.productos = productos;
    }

    public List<CategoriaDto> listar() {
        return categorias.findAll().stream().map(CategoriaDto::de).toList();
    }

    public CategoriaDto crear(CategoriaDto dto) {
        return guardar(new Categoria(), dto);
    }

    public CategoriaDto actualizar(Long id, CategoriaDto dto) {
        return guardar(buscar(id), dto);
    }

    /** Regla de negocio: una categoría con productos no se puede borrar. */
    public void eliminar(Long id) {
        buscar(id);
        if (productos.existsByCategoriaId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La categoría tiene productos asociados.");
        }
        categorias.deleteById(id);
    }

    private Categoria buscar(Long id) {
        return categorias.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La categoría no existe."));
    }

    private CategoriaDto guardar(Categoria categoria, CategoriaDto dto) {
        String nombre = dto.nombre().trim().toUpperCase();
        categorias.findByNombre(nombre).ifPresent(otra -> {
            if (!otra.getId().equals(categoria.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "La categoría ya existe.");
            }
        });
        categoria.setNombre(nombre);
        return CategoriaDto.de(categorias.save(categoria));
    }
}
