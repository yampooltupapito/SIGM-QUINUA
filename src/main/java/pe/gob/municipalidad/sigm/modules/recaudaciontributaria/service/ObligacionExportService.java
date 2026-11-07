package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.ObligacionFilterDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCuentaCorriente;

import java.util.UUID;

public interface ObligacionExportService {

    byte[] exportarPadronObligacionesExcel(ObligacionFilterDto filter);

    byte[] exportarPadronObligacionesPdf(ObligacionFilterDto filter);

    byte[] exportarFichaObligacionPdf(UUID obligacionId);

    byte[] exportarEstadoCuentaPdf(UUID cuentaId);

    byte[] exportarCuentasCorrientesExcel(String query, EstadoCuentaCorriente estado, Boolean conDeuda);
}
