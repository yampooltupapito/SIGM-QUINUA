package pe.gob.municipalidad.sigm.modules.mesapartes.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.ReporteDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.EstadisticasExpedienteService;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.ReporteMesaPartesService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class EstadisticasExpedienteServiceImpl implements EstadisticasExpedienteService {

    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");
    private final ExpedienteRepository expedienteRepository;
    private final ReporteMesaPartesService reporteService;

    public EstadisticasExpedienteServiceImpl(ExpedienteRepository expedienteRepository,
                                             ReporteMesaPartesService reporteService) {
        this.expedienteRepository = expedienteRepository;
        this.reporteService = reporteService;
    }

    @Override
    public GraficoEstadoDto obtenerGraficoEstado(FiltroReporteRequest filtro, String username) {
        Long oficinaId = reporteService.resolverOficinaEfectiva(filtro != null ? filtro.getOficinaId() : null, username);
        OffsetDateTime[] fechas = ReporteFilterHelper.resolverRangoFechas(filtro);

        List<Object[]> resultados = expedienteRepository.countGroupByEstado(oficinaId, fechas[0], fechas[1]);

        long presentados = 0;
        long recibidos = 0;
        long enRevision = 0;
        long observados = 0;
        long enAtencion = 0;
        long resueltos = 0;
        long archivados = 0;

        for (Object[] r : resultados) {
            EstadoExpediente est = (EstadoExpediente) r[0];
            long count = ((Number) r[1]).longValue();
            if (est == null) continue;

            if (est == EstadoExpediente.PRESENTADO || est == EstadoExpediente.BORRADOR) {
                presentados += count;
            } else if (est == EstadoExpediente.RECIBIDO) {
                recibidos += count;
            } else if (est == EstadoExpediente.EN_REVISION) {
                enRevision += count;
            } else if (est == EstadoExpediente.OBSERVADO || est == EstadoExpediente.EN_SUBSANACION) {
                observados += count;
            } else if (est == EstadoExpediente.EN_ATENCION || est == EstadoExpediente.DERIVADO) {
                enAtencion += count;
            } else if (est == EstadoExpediente.RESUELTO || est == EstadoExpediente.NOTIFICADO) {
                resueltos += count;
            } else if (est == EstadoExpediente.ARCHIVADO || est == EstadoExpediente.ANULADO) {
                archivados += count;
            }
        }

        List<String> labels = List.of(
                "Presentados",
                "Recibidos",
                "En Revisión",
                "Observados",
                "En Atención",
                "Resueltos",
                "Archivados"
        );

        List<Long> datos = List.of(
                presentados,
                recibidos,
                enRevision,
                observados,
                enAtencion,
                resueltos,
                archivados
        );

        List<String> colores = List.of(
                "#3b82f6", // Azul
                "#06b6d4", // Cian
                "#f59e0b", // Ámbar
                "#ef4444", // Rojo
                "#8b5cf6", // Púrpura
                "#10b981", // Esmeralda
                "#64748b"  // Pizarra
        );

        long total = datos.stream().mapToLong(Long::longValue).sum();

        return GraficoEstadoDto.builder()
                .labels(labels)
                .datos(datos)
                .colores(colores)
                .total(total)
                .build();
    }

    @Override
    public EvolucionMensualDto obtenerEvolucionMensual(Integer anio, Long oficinaId, String username) {
        Long ofiId = reporteService.resolverOficinaEfectiva(oficinaId, username);
        int anioSel = anio != null ? anio : LocalDate.now(ZONA_LIMA).getYear();

        List<String> nombresMeses = List.of("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Set", "Oct", "Nov", "Dic");
        List<Long> presentados = new ArrayList<>(12);
        List<Long> resueltos = new ArrayList<>(12);
        List<Long> cerrados = new ArrayList<>(12);

        for (int mes = 1; mes <= 12; mes++) {
            LocalDate inicioMes = LocalDate.of(anioSel, mes, 1);
            LocalDate finMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());

            OffsetDateTime dtInicio = inicioMes.atStartOfDay(ZONA_LIMA).toOffsetDateTime();
            OffsetDateTime dtFin = finMes.atTime(LocalTime.MAX).atZone(ZONA_LIMA).toOffsetDateTime();

            long pres = expedienteRepository.countPresentadosEnRango(ofiId, dtInicio, dtFin);
            long res = expedienteRepository.countResueltosEnRango(ofiId, dtInicio, dtFin);

            presentados.add(pres);
            resueltos.add(res);
            cerrados.add(res); // En SIGM-QUINUA la resolución concluye el trámite
        }

        return EvolucionMensualDto.builder()
                .anio(anioSel)
                .meses(nombresMeses)
                .presentados(presentados)
                .resueltos(resueltos)
                .cerrados(cerrados)
                .build();
    }

    @Override
    public ComparacionPeriodoDto compararPeriodos(String tipoPeriodo, Long oficinaId, String username) {
        Long ofiId = reporteService.resolverOficinaEfectiva(oficinaId, username);
        LocalDate hoy = LocalDate.now(ZONA_LIMA);

        LocalDate desdeActual;
        LocalDate hastaActual;
        LocalDate desdeAnterior;
        LocalDate hastaAnterior;
        String labelActual;
        String labelAnterior;

        String tipo = tipoPeriodo != null ? tipoPeriodo.toUpperCase() : "MES";

        switch (tipo) {
            case "TRIMESTRE":
                int primerMesActual = ((hoy.getMonthValue() - 1) / 3) * 3 + 1;
                desdeActual = LocalDate.of(hoy.getYear(), primerMesActual, 1);
                hastaActual = desdeActual.plusMonths(2).withDayOfMonth(desdeActual.plusMonths(2).lengthOfMonth());

                LocalDate finTrimAnt = desdeActual.minusDays(1);
                int primerMesAnt = ((finTrimAnt.getMonthValue() - 1) / 3) * 3 + 1;
                desdeAnterior = LocalDate.of(finTrimAnt.getYear(), primerMesAnt, 1);
                hastaAnterior = finTrimAnt;

                labelActual = "Trimestre Actual";
                labelAnterior = "Trimestre Anterior";
                break;
            case "ANIO":
                desdeActual = LocalDate.of(hoy.getYear(), 1, 1);
                hastaActual = LocalDate.of(hoy.getYear(), 12, 31);
                desdeAnterior = LocalDate.of(hoy.getYear() - 1, 1, 1);
                hastaAnterior = LocalDate.of(hoy.getYear() - 1, 12, 31);
                labelActual = "Año " + hoy.getYear();
                labelAnterior = "Año " + (hoy.getYear() - 1);
                break;
            case "MES":
            default:
                desdeActual = hoy.withDayOfMonth(1);
                hastaActual = hoy.withDayOfMonth(hoy.lengthOfMonth());
                LocalDate mesAnt = hoy.minusMonths(1);
                desdeAnterior = mesAnt.withDayOfMonth(1);
                hastaAnterior = mesAnt.withDayOfMonth(mesAnt.lengthOfMonth());
                labelActual = "Mes Actual (" + hoy.getMonth().name().substring(0, 3) + ")";
                labelAnterior = "Mes Anterior (" + mesAnt.getMonth().name().substring(0, 3) + ")";
                break;
        }

        OffsetDateTime dtDesdeActual = desdeActual.atStartOfDay(ZONA_LIMA).toOffsetDateTime();
        OffsetDateTime dtHastaActual = hastaActual.atTime(LocalTime.MAX).atZone(ZONA_LIMA).toOffsetDateTime();

        OffsetDateTime dtDesdeAnterior = desdeAnterior.atStartOfDay(ZONA_LIMA).toOffsetDateTime();
        OffsetDateTime dtHastaAnterior = hastaAnterior.atTime(LocalTime.MAX).atZone(ZONA_LIMA).toOffsetDateTime();

        long presAct = expedienteRepository.countPresentadosEnRango(ofiId, dtDesdeActual, dtHastaActual);
        long presAnt = expedienteRepository.countPresentadosEnRango(ofiId, dtDesdeAnterior, dtHastaAnterior);
        long resAct = expedienteRepository.countResueltosEnRango(ofiId, dtDesdeActual, dtHastaActual);
        long resAnt = expedienteRepository.countResueltosEnRango(ofiId, dtDesdeAnterior, dtHastaAnterior);
        long obsAct = expedienteRepository.countObservadosEnRango(ofiId, dtDesdeActual, dtHastaActual);
        long obsAnt = expedienteRepository.countObservadosEnRango(ofiId, dtDesdeAnterior, dtHastaAnterior);

        return ComparacionPeriodoDto.builder()
                .periodoActualLabel(labelActual)
                .periodoAnteriorLabel(labelAnterior)
                .presentadosActual(presAct)
                .presentadosAnterior(presAnt)
                .variacionPresentados(presAct - presAnt)
                .variacionPctPresentados(calcularVariacionPorcentual(presAct, presAnt))
                .resueltosActual(resAct)
                .resueltosAnterior(resAnt)
                .variacionResueltos(resAct - resAnt)
                .variacionPctResueltos(calcularVariacionPorcentual(resAct, resAnt))
                .observadosActual(obsAct)
                .observadosAnterior(obsAnt)
                .variacionObservados(obsAct - obsAnt)
                .variacionPctObservados(calcularVariacionPorcentual(obsAct, obsAnt))
                .build();
    }

    private String calcularVariacionPorcentual(long actual, long anterior) {
        if (anterior == 0) {
            if (actual == 0) {
                return "0.0%";
            }
            return "+100.0% (Base 0)";
        }
        double variacion = (((double) actual - (double) anterior) / (double) anterior) * 100.0;
        double redondeado = Math.round(variacion * 10.0) / 10.0;
        if (redondeado > 0) {
            return "+" + redondeado + "%";
        }
        return redondeado + "%";
    }
}
