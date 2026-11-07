package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.MedioPagoTributario;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface PagoExportService {

    byte[] exportarPagosExcel(String busqueda, UUID contribuyenteId, UUID sesionCajaId,
                              MedioPagoTributario medioPago, EstadoPagoTributario estado,
                              OffsetDateTime fechaDesde, OffsetDateTime fechaHasta);

    byte[] exportarPagosPdf(String busqueda, UUID contribuyenteId, UUID sesionCajaId,
                            MedioPagoTributario medioPago, EstadoPagoTributario estado,
                            OffsetDateTime fechaDesde, OffsetDateTime fechaHasta);

    byte[] exportarCierreSesionPdf(UUID sesionCajaId);
}
