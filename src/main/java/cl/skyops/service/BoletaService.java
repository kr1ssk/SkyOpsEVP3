package cl.skyops.service;

import cl.skyops.dto.BoletaDto;
import cl.skyops.dto.BoletaRequest;
import cl.skyops.dto.EntregaDto;
import cl.skyops.model.Boleta;
import cl.skyops.model.DetalleBoleta;
import cl.skyops.model.EstadoBoleta;
import cl.skyops.repository.BoletaRepository;
import cl.skyops.repository.ProductoRepository;
import cl.skyops.repository.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BoletaService {

    private final BoletaRepository boletas;
    private final ProductoRepository productos;
    private final UsuarioRepository usuarios;

    public BoletaService(BoletaRepository boletas, ProductoRepository productos, UsuarioRepository usuarios) {
        this.boletas = boletas;
        this.productos = productos;
        this.usuarios = usuarios;
    }

    /**
     * Lógica central del negocio. El servidor recalcula todo: no confía en precios ni totales del cliente.
     * Si algo falla, @Transactional deshace los cambios y no se descuenta stock a medias.
     */
    @Transactional
    public BoletaDto crear(Long usuarioId, BoletaRequest r) {
        Boleta boleta = new Boleta();
        boleta.setUsuario(usuarios.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Inicia sesión.")));
        copiarEntrega(boleta, r.entrega());

        // Si el mismo producto viene en dos líneas, se suman antes de revisar el stock
        Map<Long, Integer> cantidades = new LinkedHashMap<>();
        r.detalles().forEach(d -> cantidades.merge(d.productoId(), d.cantidad(), Integer::sum));

        int total = 0;
        for (var linea : cantidades.entrySet()) {
            var producto = productos.findById(linea.getKey())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "El producto " + linea.getKey() + " no existe."));
            int cantidad = linea.getValue();
            if (cantidad > producto.getStock()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El stock cambió. Revisa el manifiesto.");
            }
            int precioFinal = (int) Math.round(producto.getPrecio() * (1 - producto.getDescuento() / 100.0));

            DetalleBoleta detalle = new DetalleBoleta();
            detalle.setBoleta(boleta);
            detalle.setProducto(producto);
            detalle.setCantidad(cantidad);
            detalle.setPrecioUnitario(precioFinal);
            boleta.getDetalles().add(detalle);

            total += precioFinal * cantidad;
            if (r.aprobada()) {
                producto.setStock(producto.getStock() - cantidad);
            }
        }

        boleta.setFecha(LocalDateTime.now());
        boleta.setTotal(total);
        boleta.setEstado(r.aprobada() ? EstadoBoleta.PENDIENTE : EstadoBoleta.RECHAZADO);
        return BoletaDto.de(boletas.save(boleta));
    }

    /** ADMIN y VENDEDOR ven todas las boletas; el CLIENTE solo las suyas. */
    public List<BoletaDto> listar(Long usuarioId, boolean veTodas) {
        var lista = veTodas ? boletas.findAllByOrderByFechaDesc() : boletas.findByUsuarioIdOrderByFechaDesc(usuarioId);
        return lista.stream().map(BoletaDto::de).toList();
    }

    public BoletaDto obtener(Long id, Long usuarioId, boolean veTodas) {
        Boleta boleta = buscar(id);
        if (!veTodas && !boleta.getUsuario().getId().equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esa boleta no te pertenece.");
        }
        return BoletaDto.de(boleta);
    }

    @Transactional
    public BoletaDto resolver(Long id) {
        Boleta boleta = buscar(id);
        if (boleta.getEstado() != EstadoBoleta.PENDIENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se pueden resolver boletas pendientes.");
        }
        boleta.setEstado(EstadoBoleta.RESUELTO);
        return BoletaDto.de(boleta);
    }

    private Boleta buscar(Long id) {
        return boletas.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La boleta no existe."));
    }

    private void copiarEntrega(Boleta b, EntregaDto e) {
        b.setMatricula(e.matricula().trim().toUpperCase());
        b.setIngeniero(e.ingeniero().trim());
        b.setLicencia(e.licencia().trim().toUpperCase());
        b.setCorreo(e.correo().trim());
        b.setDireccion(e.direccion().trim());
        b.setDestino(e.destino());
        b.setTipoEntrega(e.tipoEntrega());
    }
}
