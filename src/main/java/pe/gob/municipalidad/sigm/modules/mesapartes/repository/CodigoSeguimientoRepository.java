package pe.gob.municipalidad.sigm.modules.mesapartes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.CodigoSeguimiento;

import java.util.Optional;

@Repository
public interface CodigoSeguimientoRepository extends JpaRepository<CodigoSeguimiento, Long> {

    Optional<CodigoSeguimiento> findByCodigo(String codigo);

    Optional<CodigoSeguimiento> findByExpedienteId(Long expedienteId);

    Optional<CodigoSeguimiento> findFirstByExpedienteIdOrderByFechaEmisionDesc(Long expedienteId);

    boolean existsByCodigo(String codigo);
}
