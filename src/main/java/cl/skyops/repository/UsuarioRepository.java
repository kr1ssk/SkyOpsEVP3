package cl.skyops.repository;

import cl.skyops.model.Rol;
import cl.skyops.model.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreo(String correo);

    long countByRol(Rol rol);
}
