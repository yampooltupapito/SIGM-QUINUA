package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.SolicitudFraccionamientoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoSolicitudFraccionamiento;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SolicitudFraccionamientoTributarioRepository
        extends JpaRepository<SolicitudFraccionamientoTributario, UUID>, JpaSpecificationExecutor<SolicitudFraccionamientoTributario> {

    Optional<SolicitudFraccionamientoTributario> findByCodigoSolicitud(String codigoSolicitud);

    boolean existsByCodigoSolicitud(String codigoSolicitud);

    Page<SolicitudFraccionamientoTributario> findByContribuyenteId(UUID contribuyenteId, Pageable pageable);

    List<SolicitudFraccionamientoTributario> findByContribuyenteIdAndEstadoIn(UUID contribuyenteId, List<EstadoSolicitudFraccionamiento> estados);

    long countByEstado(EstadoSolicitudFraccionamiento estado);

    @Query("SELECT COUNT(s) FROM SolicitudFraccionamientoTributario s WHERE s.estado IN ('PRESENTADA', 'EN_EVALUACION', 'SUBSANADA')")
    long countEnEvaluacionTotal();
}
