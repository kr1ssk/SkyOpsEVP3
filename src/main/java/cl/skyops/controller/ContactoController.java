package cl.skyops.controller;

import cl.skyops.dto.ContactoRequest;
import cl.skyops.model.Contacto;
import cl.skyops.repository.ContactoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Sin lógica de negocio, por eso usa el repositorio directo y no tiene service. */
@RestController
@RequestMapping("/api/v1/contactos")
@Tag(name = "Contacto", description = "Formulario de contacto. Enviar es público; leer los mensajes es solo ADMIN.")
public class ContactoController {

    private final ContactoRepository contactos;

    public ContactoController(ContactoRepository contactos) {
        this.contactos = contactos;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Guarda un mensaje de contacto (público)")
    public Contacto enviar(@Valid @RequestBody ContactoRequest r) {
        var contacto = new Contacto();
        contacto.setNombre(r.nombre().trim());
        contacto.setCorreo(r.correo().trim());
        contacto.setMensaje(r.mensaje().trim());
        contacto.setFecha(LocalDateTime.now());
        return contactos.save(contacto);
    }

    @GetMapping
    @SecurityRequirement(name = "bearer")
    @Operation(summary = "Lista los mensajes recibidos (ADMIN)")
    public List<Contacto> listar() {
        return contactos.findAllByOrderByFechaDesc();
    }
}
