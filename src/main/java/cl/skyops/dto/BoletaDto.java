package cl.skyops.dto;

import cl.skyops.model.Boleta;
import cl.skyops.model.EstadoBoleta;
import java.time.LocalDateTime;
import java.util.List;

public record BoletaDto(
        Long id,
        LocalDateTime fecha,
        EstadoBoleta estado,
        int total,
        EntregaDto entrega,
        List<Detalle> detalles) {

    public record Detalle(Long productoId, String nombre, String pn, int cantidad, int precioUnitario, int subtotal) {
    }

    public static BoletaDto de(Boleta b) {
        var entrega = new EntregaDto(b.getMatricula(), b.getIngeniero(), b.getLicencia(), b.getCorreo(),
                b.getDireccion(), b.getDestino(), b.getTipoEntrega(), b.getUsuario().getId());
        var detalles = b.getDetalles().stream()
                .map(d -> new Detalle(d.getProducto().getId(), d.getProducto().getNombre(), d.getProducto().getPn(),
                        d.getCantidad(), d.getPrecioUnitario(), d.getCantidad() * d.getPrecioUnitario()))
                .toList();
        return new BoletaDto(b.getId(), b.getFecha(), b.getEstado(), b.getTotal(), entrega, detalles);
    }
}
