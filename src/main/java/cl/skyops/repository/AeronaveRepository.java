package cl.skyops.repository;

import cl.skyops.model.Aeronave;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AeronaveRepository extends JpaRepository<Aeronave, Long> {
}
