package pe.gob.municipalidad.sigm.modules.mesapartes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.ExpedienteHistorial;

import java.util.List;

@Repository
public interface ExpedienteHistorialRepository extends JpaRepository<ExpedienteHistorial, Long> {

    List<ExpedienteHistorial> findByExpedienteIdOrderByFechaMovimientoAsc(Long expedienteId);

    java.util.Optional<ExpedienteHistorial> findFirstByExpedienteIdOrderByFechaMovimientoDesc(Long expedienteId);
}
