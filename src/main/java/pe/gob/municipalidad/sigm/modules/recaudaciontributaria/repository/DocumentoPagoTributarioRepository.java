package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.DocumentoPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoDocumentoPagoTributario;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentoPagoTributarioRepository extends JpaRepository<DocumentoPagoTributario, UUID>,
        JpaSpecificationExecutor<DocumentoPagoTributario> {

    Optional<DocumentoPagoTributario> findByCodigoDocumento(String codigoDocumento);

    Optional<DocumentoPagoTributario> findByCodigoVerificacion(String codigoVerificacion);

    Optional<DocumentoPagoTributario> findByPagoTributarioIdAndTipoDocumentoCodigoAndEstado(
            UUID pagoTributarioId, String tipoCodigo, EstadoDocumentoPagoTributario estado);

    List<DocumentoPagoTributario> findByPagoTributarioId(UUID pagoTributarioId);

    List<DocumentoPagoTributario> findByContribuyenteIdOrderByFechaEmisionDesc(UUID contribuyenteId);

    boolean existsByPagoTributarioIdAndTipoDocumentoIdAndEstado(
            UUID pagoTributarioId, UUID tipoDocumentoId, EstadoDocumentoPagoTributario estado);

    boolean existsByIdempotencyKey(String idempotencyKey);

    long countByEstado(EstadoDocumentoPagoTributario estado);

    @Query("SELECT COUNT(d) FROM DocumentoPagoTributario d WHERE d.fechaEmision >= :desde AND d.fechaEmision <= :hasta")
    long countByPeriodo(@Param("desde") OffsetDateTime desde, @Param("hasta") OffsetDateTime hasta);
}
