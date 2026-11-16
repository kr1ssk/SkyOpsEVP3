package cl.skyops.dto;

import cl.skyops.model.Categoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CategoriaDto(
        Long id,
        @NotBlank(message = "Ingresa el nombre.")
        @Pattern(regexp = "(?i)ATA \\d{2}", message = "Usa el formato ATA 27.") String nombre) {

    public static CategoriaDto de(Categoria c) {
        return new CategoriaDto(c.getId(), c.getNombre());
    }
}
