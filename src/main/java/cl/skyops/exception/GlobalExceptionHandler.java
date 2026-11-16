package cl.skyops.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/** Todos los errores salen con el mismo formato: {"mensaje": "...", "errores": {campo: mensaje}}. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Errores de negocio que lanzan los servicios (404, 409, 401...). */
    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<Map<String, Object>> estado(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode()).body(Map.of("mensaje", e.getReason()));
    }

    /** Falló alguna validación del DTO (@NotBlank, @Size, ...). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> validacion(MethodArgumentNotValidException e) {
        Map<String, String> errores = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(f -> errores.putIfAbsent(f.getField(), f.getDefaultMessage()));
        return ResponseEntity.badRequest().body(Map.of("mensaje", "Revisa los datos enviados.", "errores", errores));
    }

    /** Red de seguridad: dato duplicado en la BD o dos compras simultáneas sobre el mismo stock. */
    @ExceptionHandler({DataIntegrityViolationException.class, ObjectOptimisticLockingFailureException.class})
    ResponseEntity<Map<String, Object>> conflicto(Exception e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("mensaje", "Los datos cambiaron o ya existen. Revisa e intenta de nuevo."));
    }
}
