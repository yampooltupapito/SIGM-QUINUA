package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ObligacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoObligacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoSolicitudFraccionamiento;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.DetalleSolicitudFraccionamientoRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ObligacionTributariaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Servicio de evaluación técnica de elegibilidad de deudas para fraccionamiento municipal.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ElegibilidadFraccionamientoService {

    private final ObligacionTributariaRepository obligacionRepository;
    private final DetalleSolicitudFraccionamientoRepository detalleSolicitudRepository;

    private static final List<EstadoSolicitudFraccionamiento> ESTADOS_SOLICITUD_EXCLUIDOS = List.of(
            EstadoSolicitudFraccionamiento.RECHAZADA,
            EstadoSolicitudFraccionamiento.DESISTIDA,
            EstadoSolicitudFraccionamiento.ARCHIVADA
    );

    public ResultadoElegibilidad evaluarObligacion(UUID obligacionId) {
        ObligacionTributaria obl = obligacionRepository.findById(obligacionId).orElse(null);
        if (obl == null) {
            return new ResultadoElegibilidad(false, "La obligación tributaria especificada no existe");
        }

        if (obl.getSaldo() == null || obl.getSaldo().compareTo(BigDecimal.ZERO) <= 0) {
            return new ResultadoElegibilidad(false, "La obligación no presenta saldo pendiente por amortizar");
        }

        if (obl.getEstado() == EstadoObligacionTributaria.ANULADA) {
            return new ResultadoElegibilidad(false, "La obligación se encuentra anulada o desestimada");
        }

        if (obl.getEstado() == EstadoObligacionTributaria.FRACCIONADA) {
            return new ResultadoElegibilidad(false, "La obligación ya se encuentra incorporada en un convenio vigente de fraccionamiento");
        }

        boolean enTramite = detalleSolicitudRepository.existsByObligacionIdAndSolicitudEstadoNotIn(obligacionId, ESTADOS_SOLICITUD_EXCLUIDOS);
        if (enTramite) {
            return new ResultadoElegibilidad(false, "La obligación ya está registrada en otra solicitud en trámite de fraccionamiento");
        }

        return new ResultadoElegibilidad(true, "Obligación conforme y elegible para fraccionamiento");
    }

    public record ResultadoElegibilidad(boolean elegible, String motivo) {}
}
