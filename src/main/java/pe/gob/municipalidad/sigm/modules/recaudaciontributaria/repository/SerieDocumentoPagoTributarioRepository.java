package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.SerieDocumentoPagoTributario;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SerieDocumentoPagoTributarioRepository extends JpaRepository<SerieDocumentoPagoTributario, UUID> {

    Optional<SerieDocumentoPagoTributario> findByTipoDocumentoIdAndCodigoSerie(UUID tipoDocumentoId, String codigoSerie);

    Optional<SerieDocumentoPagoTributario> findFirstByTipoDocumentoCodigoAndEstado(String tipoDocumentoCodigo, String estado);

    List<SerieDocumentoPagoTributario> findByTipoDocumentoId(UUID tipoDocumentoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SerieDocumentoPagoTributario s WHERE s.id = :id")
    Optional<SerieDocumentoPagoTributario> findByIdForUpdate(@Param("id") UUID id);
}
