package pe.gob.municipalidad.sigm.modules.mesapartes.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.ReporteDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.EstadisticasTramiteService;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.ReporteMesaPartesService;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class EstadisticasTramiteServiceImpl implements EstadisticasTramiteService {

    private final ExpedienteRepository expedienteRepository;
    private final ReporteMesaPartesService reporteService;

    public EstadisticasTramiteServiceImpl(ExpedienteRepository expedienteRepository,
                                          ReporteMesaPartesService reporteService) {
        this.expedienteRepository = expedienteRepository;
        this.reporteService = reporteService;
    }

    @Override
    public List<EstadisticaTramiteDto> obtenerEstadisticasPorTramite(FiltroReporteRequest filtro, String username) {
        Long oficinaId = reporteService.resolverOficinaEfectiva(filtro != null ? filtro.getOficinaId() : null, username);
        OffsetDateTime[] fechas = ReporteFilterHelper.resolverRangoFechas(filtro);

        List<Object[]> resultados = expedienteRepository.countGroupByTipoTramite(oficinaId, fechas[0], fechas[1]);

        long granTotal = 0;
        for (Object[] r : resultados) {
            granTotal += ((Number) r[3]).longValue();
        }

        List<EstadisticaTramiteDto> lista = new ArrayList<>();
        for (Object[] r : resultados) {
            Long tramiteId = (Long) r[0];
            String nombre = (String) r[1];
            String codigo = (String) r[2];
            long cantidad = ((Number) r[3]).longValue();

            double pct = granTotal > 0 ? Math.round(((double) cantidad / (double) granTotal) * 1000.0) / 10.0 : 0.0;

            lista.add(EstadisticaTramiteDto.builder()
                    .tipoTramiteId(tramiteId)
                    .nombreTramite(nombre)
                    .codigoTramite(codigo)
                    .cantidad(cantidad)
                    .porcentaje(pct)
                    .build());
        }

        lista.sort((a, b) -> Long.compare(b.getCantidad(), a.getCantidad()));
        return lista;
    }
}
