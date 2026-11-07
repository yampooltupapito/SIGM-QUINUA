package pe.gob.municipalidad.sigm.modules.mesapartes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.ReaperturaExpediente;

import java.util.List;

@Repository
public interface ReaperturaExpedienteRepository extends JpaRepository<ReaperturaExpediente, Long> {

    List<ReaperturaExpediente> findByExpedienteIdOrderByFechaReaperturaDesc(Long expedienteId);
}
