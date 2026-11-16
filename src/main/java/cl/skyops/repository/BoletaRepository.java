package cl.skyops.repository;

import cl.skyops.model.Boleta;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoletaRepository extends JpaRepository<Boleta, Long> {
    List<Boleta> findAllByOrderByFechaDesc();

    List<Boleta> findByUsuarioIdOrderByFechaDesc(Long usuarioId);

    boolean existsByUsuarioId(Long usuarioId);

    /** Para no borrar un producto que ya aparece en alguna boleta. */
    boolean existsByDetallesProductoId(Long productoId);
}
