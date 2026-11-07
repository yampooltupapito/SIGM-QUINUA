package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.CompromisoSeguimientoDeuda;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCompromisoDeuda;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface CompromisoSeguimientoDeudaRepository extends JpaRepository<CompromisoSeguimientoDeuda, UUID> {

    List<CompromisoSeguimientoDeuda> findBySeguimientoIdOrderByFechaComprometidaAsc(UUID seguimientoId);

    List<CompromisoSeguimientoDeuda> findByEstadoAndFechaComprometidaBefore(EstadoCompromisoDeuda estado, LocalDate fecha);

    long countByEstado(EstadoCompromisoDeuda estado);
}
