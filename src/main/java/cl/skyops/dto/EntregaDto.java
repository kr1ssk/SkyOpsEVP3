package cl.skyops.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Datos de entrega del pedido. "usuarioId" solo se llena en la respuesta. */
public record EntregaDto(
        @NotBlank @Pattern(regexp = "(?i)[A-Z]{2}-[A-Z]{3}", message = "Usa el formato CC-BFA.") String matricula,
        @NotBlank @Pattern(regexp = "^\\s*\\S+(\\s+\\S+)+\\s*$", message = "Ingresa nombre y apellido.") String ingeniero,
        @NotBlank @Pattern(regexp = "(?i)[A-Z]{2}-\\d{4}", message = "Usa el formato AA-1234.") String licencia,
        @NotBlank(message = "Ingresa un correo válido.") @Email(message = "Ingresa un correo válido.") String correo,
        @NotBlank @Size(min = 5, message = "Ingresa una dirección de entrega.") String direccion,
        @NotBlank @Pattern(regexp = "SCL|ANF|CCP|IQQ", message = "Selecciona un aeropuerto.") String destino,
        @NotBlank @Pattern(regexp = "Retiro en bodega|Despacho a aeropuerto", message = "Selecciona una forma de entrega.") String tipoEntrega,
        Long usuarioId) {
}
