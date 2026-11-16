package cl.skyops.repository;

import cl.skyops.model.Contacto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactoRepository extends JpaRepository<Contacto, Long> {
    List<Contacto> findAllByOrderByFechaDesc();
}
