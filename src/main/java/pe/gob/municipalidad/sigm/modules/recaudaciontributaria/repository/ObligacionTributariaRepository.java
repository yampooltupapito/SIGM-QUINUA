package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ObligacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoObligacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoDeterminacionTributaria;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ObligacionTributariaRepository extends JpaRepository<ObligacionTributaria, UUID>, JpaSpecificationExecutor<ObligacionTributaria> {

    @Query(value = "SELECT nextval('seq_tributaria_obligacion')", nativeQuery = true)
    Long obtenerSiguienteValorSecuencia();

    Optional<ObligacionTributaria> findByCodigoObligacion(String codigoObligacion);

    List<ObligacionTributaria> findByContribuyenteId(UUID contribuyenteId);

    Page<ObligacionTributaria> findByContribuyenteId(UUID contribuyenteId, Pageable pageable);

    List<ObligacionTributaria> findByContribuyenteIdAndEstado(UUID contribuyenteId, EstadoObligacionTributaria estado);

    List<ObligacionTributaria> findByContribuyenteIdAndEstadoNotIn(UUID contribuyenteId, List<EstadoObligacionTributaria> estados);

    Optional<ObligacionTributaria> findByDeterminacionOrigenIdAndCuotaNumero(UUID determinacionOrigenId, Integer cuotaNumero);

    List<ObligacionTributaria> findByDeterminacionOrigenId(UUID determinacionOrigenId);

    boolean existsByDeterminacionOrigenIdAndEstadoNotIn(UUID determinacionOrigenId, List<EstadoObligacionTributaria> estadosExcluidos);

    @Query("SELECT o FROM ObligacionTributaria o WHERE o.contribuyente.id = :contribuyenteId AND o.saldo > 0 AND o.estado NOT IN ('ANULADA', 'BORRADOR', 'RECTIFICADA') ORDER BY o.fechaVencimiento ASC NULLS LAST")
    List<ObligacionTributaria> findDeudasPendientesByContribuyente(@Param("contribuyenteId") UUID contribuyenteId);

    @Query("SELECT COALESCE(SUM(o.saldo), 0) FROM ObligacionTributaria o WHERE o.contribuyente.id = :contribuyenteId AND o.estado NOT IN ('ANULADA', 'BORRADOR', 'RECTIFICADA')")
    BigDecimal calcularDeudaTotalContribuyente(@Param("contribuyenteId") UUID contribuyenteId);

    long countByEstado(EstadoObligacionTributaria estado);

    long countByTipoDeterminacion(TipoDeterminacionTributaria tipoDeterminacion);

    @Query("SELECT COALESCE(SUM(o.importeDeterminado), 0) FROM ObligacionTributaria o WHERE o.estado NOT IN ('ANULADA', 'BORRADOR')")
    BigDecimal sumarTotalImporteDeterminado();

    @Query("SELECT COALESCE(SUM(o.saldo), 0) FROM ObligacionTributaria o WHERE o.estado NOT IN ('ANULADA', 'BORRADOR')")
    BigDecimal sumarTotalSaldoPendiente();

    @Query("SELECT COALESCE(SUM(o.saldo), 0) FROM ObligacionTributaria o WHERE o.saldo > 0 AND o.fechaVencimiento < :fechaCorte AND o.estado NOT IN ('ANULADA', 'BORRADOR', 'RECTIFICADA')")
    BigDecimal sumarSaldoVencido(@Param("fechaCorte") LocalDate fechaCorte);

    @Query("SELECT COALESCE(SUM(o.saldo), 0) FROM ObligacionTributaria o WHERE o.saldo > 0 AND (o.fechaVencimiento IS NULL OR o.fechaVencimiento >= :fechaCorte) AND o.estado NOT IN ('ANULADA', 'BORRADOR', 'RECTIFICADA')")
    BigDecimal sumarSaldoNoVencido(@Param("fechaCorte") LocalDate fechaCorte);

    @Query("SELECT COALESCE(SUM(o.saldo), 0) FROM ObligacionTributaria o WHERE o.saldo > 0 AND o.estadoExigibilidad IN (pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoExigibilidadObligacion.EXIGIBLE, pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoExigibilidadObligacion.EN_COBRANZA_ORDINARIA) AND o.estado NOT IN ('ANULADA', 'BORRADOR', 'RECTIFICADA')")
    BigDecimal sumarSaldoExigible();

    @Query("SELECT COUNT(DISTINCT o.contribuyente.id) FROM ObligacionTributaria o WHERE o.saldo > 0 AND o.estado NOT IN ('ANULADA', 'BORRADOR', 'RECTIFICADA')")
    long countContribuyentesConDeuda();
}
