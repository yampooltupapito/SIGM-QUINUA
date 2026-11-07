package pe.gob.municipalidad.sigm.modules.mesapartes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.VerificacionDocumental;

import java.util.List;
import java.util.Optional;

@Repository
public interface VerificacionDocumentalRepository extends JpaRepository<VerificacionDocumental, Long> {

    Optional<VerificacionDocumental> findByCodigoVerificacion(String codigoVerificacion);

    Optional<VerificacionDocumental> findByTokenQr(String tokenQr);

    Optional<VerificacionDocumental> findByHashSha256(String hashSha256);

    Optional<VerificacionDocumental> findByNumeroDocumento(String numeroDocumento);

    Optional<VerificacionDocumental> findByDecisionId(Long decisionId);

    List<VerificacionDocumental> findByExpedienteId(Long expedienteId);
}
