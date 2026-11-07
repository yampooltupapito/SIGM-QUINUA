package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ConvenioFraccionamientoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoConvenioFraccionamiento;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConvenioFraccionamientoTributarioRepository
        extends JpaRepository<ConvenioFraccionamientoTributario, UUID>, JpaSpecificationExecutor<ConvenioFraccionamientoTributario> {

    Optional<ConvenioFraccionamientoTributario> findByCodigoConvenio(String codigoConvenio);

    boolean existsByCodigoConvenio(String codigoConvenio);

    Optional<ConvenioFraccionamientoTributario> findByCodigoVerificacion(String codigoVerificacion);

    Optional<ConvenioFraccionamientoTributario> findBySolicitudId(UUID solicitudId);

    Optional<ConvenioFraccionamientoTributario> findByCronogramaId(UUID cronogramaId);

    Page<ConvenioFraccionamientoTributario> findByContribuyenteId(UUID contribuyenteId, Pageable pageable);

    List<ConvenioFraccionamientoTributario> findByContribuyenteIdAndEstadoIn(UUID contribuyenteId, List<EstadoConvenioFraccionamiento> estados);

    long countByEstado(EstadoConvenioFraccionamiento estado);

    @Query("SELECT COUNT(c) FROM ConvenioFraccionamientoTributario c WHERE c.estado = 'VIGENTE'")
    long countVigentes();

    @Query("SELECT COUNT(c) FROM ConvenioFraccionamientoTributario c WHERE c.estado = 'CUMPLIDO'")
    long countCumplidos();

    @Query("SELECT c FROM ConvenioFraccionamientoTributario c " +
           "WHERE c.estado = 'VIGENTE' " +
           "AND EXISTS (" +
           "    SELECT cu FROM CuotaFraccionamientoTributario cu " +
           "    WHERE cu.cronograma = c.cronograma " +
           "    AND cu.estado IN ('PENDIENTE', 'PARCIALMENTE_PAGADA', 'VENCIDA') " +
           "    AND cu.fechaVencimiento < CURRENT_DATE" +
           ")")
    List<ConvenioFraccionamientoTributario> findConveniosConCuotasVencidas();
}
