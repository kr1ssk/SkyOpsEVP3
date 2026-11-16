package cl.skyops.dto;

import cl.skyops.model.Producto;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Se usa para recibir y para devolver un producto. "categoria" es el nombre (ej: "ATA 27"). */
public record ProductoDto(
        Long id,
        @NotBlank @Size(min = 3, message = "Escribe un nombre de al menos 3 caracteres.") String nombre,
        @NotBlank(message = "Ingresa el número de parte.") String pn,
        @NotBlank(message = "Ingresa una categoría.") String categoria,
        @NotBlank(message = "Ingresa una descripción.") @Size(max = 500) String descripcion,
        @NotBlank(message = "Ingresa la ubicación.") String ubicacion,
        @NotNull(message = "Ingresa el stock.") @Min(value = 0, message = "El stock debe ser 0 o más.") Integer stock,
        @NotNull(message = "Ingresa el precio.") @Min(value = 1, message = "El precio debe ser mayor a 0.") Integer precio,
        @NotNull(message = "Ingresa el descuento.") @Min(value = 0, message = "El descuento debe estar entre 0 y 100.")
        @Max(value = 100, message = "El descuento debe estar entre 0 y 100.") Integer descuento,
        boolean certificado,
        @Pattern(regexp = "^$|^[a-zA-Z0-9_-]+\\.(svg|png|jpg|jpeg|webp)$", message = "Usa un nombre como repuesto.svg") String imagen) {

    public static ProductoDto de(Producto p) {
        return new ProductoDto(p.getId(), p.getNombre(), p.getPn(), p.getCategoria().getNombre(),
                p.getDescripcion(), p.getUbicacion(), p.getStock(), p.getPrecio(), p.getDescuento(),
                p.isCertificado(), p.getImagen());
    }
}
