package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.SesionCajaMercado;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoSesionCaja;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SesionCajaMercadoRepository extends JpaRepository<SesionCajaMercado, UUID> {

    Optional<SesionCajaMercado> findByCajaIdAndEstado(UUID cajaId, EstadoSesionCaja estado);

    Optional<SesionCajaMercado> findByUsuarioCajeroIdAndEstado(Long usuarioCajeroId, EstadoSesionCaja estado);

    List<SesionCajaMercado> findByEstado(EstadoSesionCaja estado);

    @Query(value = "SELECT s.* FROM mercados_cajas_sesiones s " +
                   "WHERE (CAST(:cajaId AS uuid) IS NULL OR s.caja_id = CAST(:cajaId AS uuid)) " +
                   "AND (:estado IS NULL OR s.estado = :estado) " +
                   "ORDER BY s.fecha_apertura DESC",
           countQuery = "SELECT COUNT(*) FROM mercados_cajas_sesiones s " +
                        "WHERE (CAST(:cajaId AS uuid) IS NULL OR s.caja_id = CAST(:cajaId AS uuid)) " +
                        "AND (:estado IS NULL OR s.estado = :estado)",
           nativeQuery = true)
    Page<SesionCajaMercado> buscarPaginado(@Param("cajaId") UUID cajaId,
                                           @Param("estado") String estado,
                                           Pageable pageable);
}
