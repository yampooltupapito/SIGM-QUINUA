package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.AplicacionPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoAplicacionPago;

import java.util.List;
import java.util.UUID;

@Repository
public interface AplicacionPagoTributarioRepository extends JpaRepository<AplicacionPagoTributario, UUID> {

    List<AplicacionPagoTributario> findByPagoId(UUID pagoId);

    List<AplicacionPagoTributario> findByPagoIdAndEstado(UUID pagoId, EstadoAplicacionPago estado);

    List<AplicacionPagoTributario> findByObligacionId(UUID obligacionId);

    List<AplicacionPagoTributario> findByObligacionIdAndEstado(UUID obligacionId, EstadoAplicacionPago estado);

    List<AplicacionPagoTributario> findByCuotaId(UUID cuotaId);

    List<AplicacionPagoTributario> findByEstado(EstadoAplicacionPago estado);

    List<AplicacionPagoTributario> findByEstadoAndPagoFechaHoraPagoBetween(
            EstadoAplicacionPago estado, java.time.OffsetDateTime desde, java.time.OffsetDateTime hasta);
}
