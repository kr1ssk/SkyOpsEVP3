package cl.skyops.service;

import cl.skyops.dto.ProductoDto;
import cl.skyops.model.Producto;
import cl.skyops.repository.BoletaRepository;
import cl.skyops.repository.CategoriaRepository;
import cl.skyops.repository.ProductoRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProductoService {

    private final ProductoRepository productos;
    private final CategoriaRepository categorias;
    private final BoletaRepository boletas;

    public ProductoService(ProductoRepository productos,
            CategoriaRepository categorias,
            BoletaRepository boletas) {
        this.productos = productos;
        this.categorias = categorias;
        this.boletas = boletas;
    }

    public List<ProductoDto> listar() {
        return productos.findAll().stream().map(ProductoDto::de).toList();
    }

    public ProductoDto obtener(Long id) {
        return ProductoDto.de(buscar(id));
    }

    public ProductoDto crear(ProductoDto dto) {
        return guardar(new Producto(), dto);
    }

    public ProductoDto actualizar(Long id, ProductoDto dto) {
        return guardar(buscar(id), dto);
    }

    /** Regla de negocio: un producto que ya se vendió no se puede borrar, la boleta lo necesita. */
    public void eliminar(Long id) {
        buscar(id);
        if (boletas.existsByDetallesProductoId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El producto tiene ventas registradas.");
        }
        productos.deleteById(id);
    }

    private Producto buscar(Long id) {
        return productos.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El producto no existe."));
    }

    /** Reglas de negocio: la categoría debe existir y el número de parte (PN) no puede repetirse. */
    private ProductoDto guardar(Producto producto, ProductoDto dto) {
        var categoria = categorias.findByNombre(dto.categoria().trim().toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona una categoría registrada."));

        String pn = dto.pn().trim().toUpperCase();
        productos.findByPn(pn).ifPresent(otro -> {
            if (!otro.getId().equals(producto.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un producto con ese número de parte.");
            }
        });

        producto.setNombre(dto.nombre().trim());
        producto.setPn(pn);
        producto.setCategoria(categoria);
        producto.setDescripcion(dto.descripcion().trim());
        producto.setUbicacion(dto.ubicacion().trim());
        producto.setStock(dto.stock());
        producto.setPrecio(dto.precio());
        producto.setDescuento(dto.descuento());
        producto.setCertificado(dto.certificado());
        producto.setImagen(dto.imagen() == null ? "" : dto.imagen().trim());
        return ProductoDto.de(productos.save(producto));
    }
}
