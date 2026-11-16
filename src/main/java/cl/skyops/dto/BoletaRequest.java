package cl.skyops.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Compra nueva. "aprobada" simula el resultado del pago:
 * true crea la boleta PENDIENTE y descuenta stock; false la crea RECHAZADO sin tocar el stock.
 */
public record BoletaRequest(
        @NotNull @Valid EntregaDto entrega,
        @NotEmpty(message = "El manifiesto está vacío.") List<@Valid Item> detalles,
        boolean aprobada) {

    public record Item(
            @NotNull Long productoId,
            @NotNull @Min(value = 1, message = "La cantidad debe ser al menos 1.") Integer cantidad) {
    }
}
