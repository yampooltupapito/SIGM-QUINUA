package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.SesionCajaMunicipal;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoSesionCaja;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SesionCajaMunicipalRepository extends JpaRepository<SesionCajaMunicipal, UUID>, JpaSpecificationExecutor<SesionCajaMunicipal> {

    @Query(value = "SELECT nextval('seq_tributaria_sesion_caja')", nativeQuery = true)
    Long obtenerSiguienteValorSecuencia();

    Optional<SesionCajaMunicipal> findByCodigoSesion(String codigoSesion);

    @Query("SELECT s FROM SesionCajaMunicipal s WHERE s.caja.id = :cajaId AND s.estado IN ('ABIERTA', 'EN_CIERRE')")
    Optional<SesionCajaMunicipal> findSesionActivaPorCaja(@Param("cajaId") UUID cajaId);

    @Query("SELECT s FROM SesionCajaMunicipal s WHERE s.cajero.id = :cajeroId AND s.estado IN ('ABIERTA', 'EN_CIERRE')")
    Optional<SesionCajaMunicipal> findSesionActivaPorCajero(@Param("cajeroId") Long cajeroId);

    List<SesionCajaMunicipal> findByEstado(EstadoSesionCaja estado);

    Page<SesionCajaMunicipal> findByEstado(EstadoSesionCaja estado, Pageable pageable);

    @Query("SELECT COUNT(s) FROM SesionCajaMunicipal s WHERE s.estado = 'ABIERTA'")
    long countSesionesAbiertas();

    @Query("SELECT COALESCE(SUM(s.totalRecaudadoGeneral), 0) FROM SesionCajaMunicipal s WHERE s.fechaApertura >= :desde AND s.fechaApertura <= :hasta")
    BigDecimal calcularTotalRecaudadoPeriodo(@Param("desde") OffsetDateTime desde, @Param("hasta") OffsetDateTime hasta);

    @Query("SELECT COALESCE(SUM(s.diferencia), 0) FROM SesionCajaMunicipal s WHERE s.estado IN ('CERRADA', 'OBSERVADA')")
    BigDecimal calcularTotalDiferencias();

    long countByCajaId(UUID cajaId);
}
