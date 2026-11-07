package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.PagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.MedioPagoTributario;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PagoTributarioRepository extends JpaRepository<PagoTributario, UUID>, JpaSpecificationExecutor<PagoTributario> {

    @Query(value = "SELECT nextval('seq_tributaria_pago')", nativeQuery = true)
    Long obtenerSiguienteValorSecuencia();

    Optional<PagoTributario> findByCodigoPago(String codigoPago);

    Optional<PagoTributario> findByIdempotencyKey(String idempotencyKey);

    List<PagoTributario> findByContribuyenteId(UUID contribuyenteId);

    Page<PagoTributario> findByContribuyenteId(UUID contribuyenteId, Pageable pageable);

    List<PagoTributario> findBySesionCajaId(UUID sesionCajaId);

    Page<PagoTributario> findByEstado(EstadoPagoTributario estado, Pageable pageable);

    @Query("SELECT COUNT(p) FROM PagoTributario p WHERE p.estado IN ('CONFIRMADO', 'PROCESADO')")
    long countPagosConfirmados();

    @Query("SELECT COUNT(p) FROM PagoTributario p WHERE p.estado IN ('BORRADOR', 'PENDIENTE_VERIFICACION')")
    long countPagosPendientes();

    @Query("SELECT COALESCE(SUM(p.importe), 0) FROM PagoTributario p WHERE p.estado IN ('CONFIRMADO', 'PROCESADO') AND p.fechaHoraPago >= :desde AND p.fechaHoraPago <= :hasta")
    BigDecimal calcularRecaudacionPeriodo(@Param("desde") OffsetDateTime desde, @Param("hasta") OffsetDateTime hasta);

    @Query("SELECT COALESCE(SUM(p.importe), 0) FROM PagoTributario p WHERE p.estado IN ('CONFIRMADO', 'PROCESADO') AND p.medioPago = :medio AND p.fechaHoraPago >= :desde AND p.fechaHoraPago <= :hasta")
    BigDecimal calcularRecaudacionPorMedio(@Param("medio") MedioPagoTributario medio, @Param("desde") OffsetDateTime desde, @Param("hasta") OffsetDateTime hasta);

    @Query("SELECT COALESCE(SUM(p.importe), 0) FROM PagoTributario p WHERE p.estado IN ('CONFIRMADO', 'PROCESADO') AND p.sesionCaja.id = :sesionId AND p.medioPago = 'EFECTIVO_CAJA'")
    BigDecimal calcularEfectivoRecaudadoEnSesion(@Param("sesionId") UUID sesionId);

    @Query("SELECT COALESCE(SUM(p.importe), 0) FROM PagoTributario p WHERE p.estado IN ('CONFIRMADO', 'PROCESADO') AND p.sesionCaja.id = :sesionId AND p.medioPago <> 'EFECTIVO_CAJA'")
    BigDecimal calcularOtrosMediosRecaudadoEnSesion(@Param("sesionId") UUID sesionId);

    @Query("SELECT COUNT(p) FROM PagoTributario p WHERE p.sesionCaja.id = :sesionId AND p.estado IN ('CONFIRMADO', 'PROCESADO')")
    int countOperacionesEnSesion(@Param("sesionId") UUID sesionId);

    List<PagoTributario> findByEstadoInAndFechaHoraPagoBetween(List<EstadoPagoTributario> estados, OffsetDateTime desde, OffsetDateTime hasta);

    List<PagoTributario> findByEstadoIn(List<EstadoPagoTributario> estados);
}
