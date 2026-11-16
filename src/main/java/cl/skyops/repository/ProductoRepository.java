package cl.skyops.repository;

import cl.skyops.model.Producto;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    Optional<Producto> findByPn(String pn);

    boolean existsByCategoriaId(Long categoriaId);
}
