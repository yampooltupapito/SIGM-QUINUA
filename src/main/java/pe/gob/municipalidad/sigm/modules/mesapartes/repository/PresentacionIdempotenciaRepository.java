package pe.gob.municipalidad.sigm.modules.mesapartes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.PresentacionIdempotencia;

import java.util.Optional;

@Repository
public interface PresentacionIdempotenciaRepository extends JpaRepository<PresentacionIdempotencia, Long> {

    Optional<PresentacionIdempotencia> findByClaveIdempotencia(String claveIdempotencia);

    Optional<PresentacionIdempotencia> findByExpedienteId(Long expedienteId);
}
