package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.CuotaFraccionamientoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCuotaFraccionamiento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CuotaFraccionamientoTributarioRepository
        extends JpaRepository<CuotaFraccionamientoTributario, UUID> {

    List<CuotaFraccionamientoTributario> findByCronogramaIdOrderByNumeroCuotaAsc(UUID cronogramaId);

    Optional<CuotaFraccionamientoTributario> findByCronogramaIdAndNumeroCuota(UUID cronogramaId, Integer numeroCuota);

    @Query("SELECT COUNT(c) FROM CuotaFraccionamientoTributario c WHERE c.estado IN ('PENDIENTE', 'PARCIALMENTE_PAGADA')")
    long countCuotasPendientes();

    @Query("SELECT COUNT(c) FROM CuotaFraccionamientoTributario c " +
           "WHERE c.estado IN ('PENDIENTE', 'PARCIALMENTE_PAGADA', 'VENCIDA') " +
           "AND c.fechaVencimiento < :fechaActual")
    long countCuotasVencidas(@Param("fechaActual") LocalDate fechaActual);

    @Query("SELECT COALESCE(SUM(c.saldo), 0) FROM CuotaFraccionamientoTributario c " +
           "WHERE c.cronograma.id = :cronogramaId " +
           "AND c.estado IN ('PENDIENTE', 'PARCIALMENTE_PAGADA', 'VENCIDA')")
    BigDecimal sumSaldoPendienteCronograma(@Param("cronogramaId") UUID cronogramaId);

    @Query("SELECT c FROM CuotaFraccionamientoTributario c " +
           "WHERE c.cronograma.id = :cronogramaId " +
           "AND c.estado IN ('PENDIENTE', 'PARCIALMENTE_PAGADA', 'VENCIDA') " +
           "AND c.fechaVencimiento < :fechaActual " +
           "ORDER BY c.numeroCuota ASC")
    List<CuotaFraccionamientoTributario> findCuotasImpagasVencidas(
            @Param("cronogramaId") UUID cronogramaId,
            @Param("fechaActual") LocalDate fechaActual
    );
}
