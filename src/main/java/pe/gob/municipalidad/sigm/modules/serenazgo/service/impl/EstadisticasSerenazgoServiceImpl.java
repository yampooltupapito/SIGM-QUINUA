package pe.gob.municipalidad.sigm.modules.serenazgo.service.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.estadisticas.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.EstadisticasSerenazgoService;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EstadisticasSerenazgoServiceImpl implements EstadisticasSerenazgoService {

    public static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");

    private final IncidenciaSeguridadCiudadanaRepository incidenciaRepository;
    private final AlertaCiudadanaSeguridadRepository alertaRepository;
    private final AtencionIncidenciaSerenazgoRepository atencionRepository;
    private final IntervencionSerenazgoRepository intervencionRepository;
    private final PatrullajeSerenazgoRepository patrullajeRepository;
    private final SectorPatrullajeRepository sectorRepository;
    private final ZonaPatrullajeRepository zonaRepository;
    private final PersonalSerenazgoRepository personalRepository;
    private final UnidadMovilSerenazgoRepository unidadRepository;
    private final DocumentoSeguridadCiudadanaRepository documentoRepository;
    private final SeguimientoSupervisionSerenazgoRepository seguimientoRepository;
    private final CatalogoIndicadorSerenazgoRepository catalogoRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardEstadisticoResponse obtenerDashboardEstadistico(FiltroEstadisticoRequest filtro, CustomUserDetails user) {
        RangoFechas rangoActual = resolverRangoFechasActual(filtro);
        RangoFechas rangoAnterior = resolverRangoFechasAnterior(filtro, rangoActual);

        // 1. Obtención de datos del período actual
        List<IncidenciaSeguridadCiudadana> incidenciasActual = obtenerIncidenciasFiltradas(rangoActual, filtro);
        List<IncidenciaSeguridadCiudadana> incidenciasAnterior = obtenerIncidenciasFiltradas(rangoAnterior, filtro);

        List<AlertaCiudadanaSeguridad> alertasActual = obtenerAlertasFiltradas(rangoActual);
        List<AlertaCiudadanaSeguridad> alertasAnterior = obtenerAlertasFiltradas(rangoAnterior);

        List<AtencionIncidenciaSerenazgo> atencionesActual = obtenerAtencionesFiltradas(rangoActual);
        List<AtencionIncidenciaSerenazgo> atencionesAnterior = obtenerAtencionesFiltradas(rangoAnterior);

        List<IntervencionSerenazgo> intervencionesActual = obtenerIntervencionesFiltradas(rangoActual, filtro);
        List<IntervencionSerenazgo> intervencionesAnterior = obtenerIntervencionesFiltradas(rangoAnterior, filtro);

        List<PatrullajeSerenazgo> patrullajesActual = obtenerPatrullajesFiltrados(rangoActual, filtro);
        List<PatrullajeSerenazgo> patrullajesAnterior = obtenerPatrullajesFiltrados(rangoAnterior, filtro);

        List<SeguimientoSupervisionSerenazgo> seguimientosActual = obtenerSeguimientosFiltrados(rangoActual);
        List<SeguimientoSupervisionSerenazgo> seguimientosAnterior = obtenerSeguimientosFiltrados(rangoAnterior);

        // 2. Construcción de KPIs Comparativos
        List<IndicadorKpiDto> kpis = new ArrayList<>();

        kpis.add(crearKpi("IND_INC_TOTAL", "Incidencias Registradas", "INCIDENCIAS",
                (long) incidenciasActual.size(), (long) incidenciasAnterior.size(),
                "Incidencias", "Total de incidencias reportadas en el período.",
                "COUNT(id)", "No equivale a delitos comprobados.", "serenazgo_incidencias", "alert-triangle"));

        long incCerradasAct = incidenciasActual.stream()
                .filter(i -> i.getEstado() == EstadoIncidenciaSeguridad.CERRADA || i.getEstado() == EstadoIncidenciaSeguridad.RESUELTA)
                .count();
        long incCerradasAnt = incidenciasAnterior.stream()
                .filter(i -> i.getEstado() == EstadoIncidenciaSeguridad.CERRADA || i.getEstado() == EstadoIncidenciaSeguridad.RESUELTA)
                .count();
        kpis.add(crearKpi("IND_INC_CERRADAS", "Incidencias Resueltas / Cerradas", "INCIDENCIAS",
                incCerradasAct, incCerradasAnt,
                "Incidencias", "Incidencias formalmente cerradas con resultado registrado.",
                "COUNT(id) WHERE estado IN ('CERRADA', 'RESUELTA')", "Solo cuenta cierres formales.", "serenazgo_incidencias", "check-circle-2"));

        kpis.add(crearKpi("IND_ALE_TOTAL", "Alertas Ciudadanas", "ALERTAS",
                (long) alertasActual.size(), (long) alertasAnterior.size(),
                "Alertas", "Comunicaciones preventivas vecinales recibidas.",
                "COUNT(id)", "Reportes vecinales que requieren validación.", "serenazgo_alertas_ciudadanas", "bell"));

        long patFinAct = patrullajesActual.stream().filter(p -> p.getEstado() == EstadoPatrullajeSerenazgo.FINALIZADO).count();
        long patFinAnt = patrullajesAnterior.stream().filter(p -> p.getEstado() == EstadoPatrullajeSerenazgo.FINALIZADO).count();
        kpis.add(crearKpi("IND_PAT_EJEC", "Patrullajes Finalizados", "PATRULLAJES",
                patFinAct, patFinAnt,
                "Patrullajes", "Patrullajes concluidos con reporte de fin de turno.",
                "COUNT(id) WHERE estado = 'FINALIZADO'", "Presencia operativa real concluida.", "serenazgo_patrullajes", "shield-check"));

        long intValAct = intervencionesActual.stream().filter(in -> in.getEstado() == EstadoIntervencionSerenazgo.VALIDADA).count();
        long intValAnt = intervencionesAnterior.stream().filter(in -> in.getEstado() == EstadoIntervencionSerenazgo.VALIDADA).count();
        kpis.add(crearKpi("IND_INT_VAL", "Intervenciones Validadas", "INTERVENCIONES",
                intValAct, intValAnt,
                "Intervenciones", "Actuaciones de serenazgo validadas por supervisión.",
                "COUNT(id) WHERE estado = 'VALIDADA'", "Actuaciones administrativas de serenazgo.", "serenazgo_intervenciones", "award"));

        long segAbiertos = seguimientosActual.stream()
                .filter(s -> s.getEstado() != EstadoSeguimientoSupervision.CERRADO && s.getEstado() != EstadoSeguimientoSupervision.CANCELADO)
                .count();
        long segAbiertosAnt = seguimientosAnterior.stream()
                .filter(s -> s.getEstado() != EstadoSeguimientoSupervision.CERRADO && s.getEstado() != EstadoSeguimientoSupervision.CANCELADO)
                .count();
        kpis.add(crearKpi("IND_SUP_SEGUIM", "Expedientes de Supervisión Activos", "SUPERVISION",
                segAbiertos, segAbiertosAnt,
                "Expedientes", "Expedientes en trámite de revisión o subsanación.",
                "COUNT(id) WHERE estado NOT IN ('CERRADO', 'CANCELADO')", "Carga de control administrativo.", "serenazgo_supervision_seguimientos", "file-text"));

        // 3. Series Temporales (Incidencias y Patrullajes)
        SerieTemporalDto seriesInc = generarSerieTemporalIncidencias(incidenciasActual, incidenciasAnterior, rangoActual, filtro.getAgrupacion());
        SerieTemporalDto seriesPat = generarSerieTemporalPatrullajes(patrullajesActual, patrullajesAnterior, rangoActual, filtro.getAgrupacion());

        // 4. Distribuciones Categóricas
        List<DistribucionItemDto> incPorTipo = calcularDistribucionIncidenciasPorTipo(incidenciasActual);
        List<DistribucionItemDto> incPorEstado = calcularDistribucionIncidenciasPorEstado(incidenciasActual);
        List<DistribucionItemDto> incPorPrioridad = calcularDistribucionIncidenciasPorPrioridad(incidenciasActual);
        List<DistribucionItemDto> alePorEstado = calcularDistribucionAlertasPorEstado(alertasActual);
        List<DistribucionItemDto> intPorTipo = calcularDistribucionIntervencionesPorTipo(intervencionesActual);
        List<DistribucionItemDto> intPorResultado = calcularDistribucionIntervencionesPorResultado(intervencionesActual);
        List<DistribucionItemDto> patPorModalidad = calcularDistribucionPatrullajesPorModalidad(patrullajesActual);

        // 5. Tiempos de Atención y Desempeño Operativo
        TiemposAtencionEstadisticasDto tiempos = calcularTiemposAtencionConLista(atencionesActual);

        // 6. Estadísticas de Patrullaje y Puntos de Control
        EstadisticasPatrullajeDto estadisticasPat = calcularEstadisticasPatrullajeConLista(patrullajesActual);

        // 7. Análisis Territorial por Sector
        List<EstadisticasTerritorialesDto> porSector = calcularEstadisticasPorSectorConDatos(incidenciasActual, patrullajesActual, intervencionesActual);

        // 8. Catálogo Técnico Oficial
        List<CatalogoIndicadorDto> catalogo = obtenerCatalogoIndicadores();

        return DashboardEstadisticoResponse.builder()
                .fechaConsulta(OffsetDateTime.now(ZONA_LIMA))
                .zonaHoraria("America/Lima")
                .periodoEtiqueta(rangoActual.getEtiqueta())
                .periodoComparacionEtiqueta(rangoAnterior.getEtiqueta())
                .kpis(kpis)
                .seriesIncidencias(seriesInc)
                .seriesPatrullajes(seriesPat)
                .incidenciasPorTipo(incPorTipo)
                .incidenciasPorEstado(incPorEstado)
                .incidenciasPorPrioridad(incPorPrioridad)
                .alertasPorEstado(alePorEstado)
                .intervencionesPorTipo(intPorTipo)
                .intervencionesPorResultado(intPorResultado)
                .patrullajesPorModalidad(patPorModalidad)
                .tiemposAtencion(tiempos)
                .estadisticasPatrullaje(estadisticasPat)
                .estadisticasPorSector(porSector)
                .catalogoIndicadores(catalogo)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CatalogoIndicadorDto> obtenerCatalogoIndicadores() {
        return catalogoRepository.findByEstadoOrderByModuloOrigenAscCodigoAsc("ACTIVO").stream()
                .map(e -> CatalogoIndicadorDto.builder()
                        .id(e.getId())
                        .codigo(e.getCodigo())
                        .nombre(e.getNombre())
                        .moduloOrigen(e.getModuloOrigen())
                        .definicion(e.getDefinicion())
                        .fuenteDatos(e.getFuenteDatos())
                        .formulaCalculo(e.getFormulaCalculo())
                        .unidadMedida(e.getUnidadMedida())
                        .limitacionesInterpretacion(e.getLimitacionesInterpretacion())
                        .estado(e.getEstado())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstadisticasTerritorialesDto> obtenerEstadisticasPorSector(FiltroEstadisticoRequest filtro) {
        RangoFechas rango = resolverRangoFechasActual(filtro);
        List<IncidenciaSeguridadCiudadana> inc = obtenerIncidenciasFiltradas(rango, filtro);
        List<PatrullajeSerenazgo> pat = obtenerPatrullajesFiltrados(rango, filtro);
        List<IntervencionSerenazgo> in = obtenerIntervencionesFiltradas(rango, filtro);
        return calcularEstadisticasPorSectorConDatos(inc, pat, in);
    }

    @Override
    @Transactional(readOnly = true)
    public TiemposAtencionEstadisticasDto calcularTiemposAtencion(FiltroEstadisticoRequest filtro) {
        RangoFechas rango = resolverRangoFechasActual(filtro);
        List<AtencionIncidenciaSerenazgo> atenciones = obtenerAtencionesFiltradas(rango);
        return calcularTiemposAtencionConLista(atenciones);
    }

    @Override
    @Transactional(readOnly = true)
    public EstadisticasPatrullajeDto calcularEstadisticasPatrullaje(FiltroEstadisticoRequest filtro) {
        RangoFechas rango = resolverRangoFechasActual(filtro);
        List<PatrullajeSerenazgo> pat = obtenerPatrullajesFiltrados(rango, filtro);
        return calcularEstadisticasPatrullajeConLista(pat);
    }

    // =========================================================================
    // Métodos Auxiliares de Consulta Agregada y Filtrado
    // =========================================================================

    private List<IncidenciaSeguridadCiudadana> obtenerIncidenciasFiltradas(RangoFechas rango, FiltroEstadisticoRequest f) {
        return incidenciaRepository.findAll().stream()
                .filter(i -> i.getFechaReporte() != null &&
                        !i.getFechaReporte().isBefore(rango.getInicio()) &&
                        !i.getFechaReporte().isAfter(rango.getFin()))
                .filter(i -> f.getSectorId() == null || (i.getSector() != null && f.getSectorId().equals(i.getSector().getId())))
                .filter(i -> f.getZonaId() == null || (i.getZona() != null && f.getZonaId().equals(i.getZona().getId())))
                .filter(i -> f.getTipoIncidenciaId() == null || (i.getTipoIncidencia() != null && f.getTipoIncidenciaId().equals(i.getTipoIncidencia().getId())))
                .filter(i -> f.getEstadoIncidencia() == null || f.getEstadoIncidencia().isBlank() || i.getEstado().name().equalsIgnoreCase(f.getEstadoIncidencia()))
                .filter(i -> f.getPrioridadIncidencia() == null || f.getPrioridadIncidencia().isBlank() || i.getPrioridad().name().equalsIgnoreCase(f.getPrioridadIncidencia()))
                .collect(Collectors.toList());
    }

    private List<AlertaCiudadanaSeguridad> obtenerAlertasFiltradas(RangoFechas rango) {
        return alertaRepository.findAll().stream()
                .filter(a -> a.getFechaRecepcion() != null &&
                        !a.getFechaRecepcion().isBefore(rango.getInicio()) &&
                        !a.getFechaRecepcion().isAfter(rango.getFin()))
                .collect(Collectors.toList());
    }

    private List<AtencionIncidenciaSerenazgo> obtenerAtencionesFiltradas(RangoFechas rango) {
        return atencionRepository.findAll().stream()
                .filter(at -> at.getFechaHoraAsignacion() != null &&
                        !at.getFechaHoraAsignacion().isBefore(rango.getInicio()) &&
                        !at.getFechaHoraAsignacion().isAfter(rango.getFin()))
                .collect(Collectors.toList());
    }

    private List<IntervencionSerenazgo> obtenerIntervencionesFiltradas(RangoFechas rango, FiltroEstadisticoRequest f) {
        return intervencionRepository.findAll().stream()
                .filter(in -> in.getFechaInicio() != null &&
                        !in.getFechaInicio().isBefore(rango.getInicio()) &&
                        !in.getFechaInicio().isAfter(rango.getFin()))
                .filter(in -> f.getSectorId() == null || (in.getSector() != null && f.getSectorId().equals(in.getSector().getId())))
                .collect(Collectors.toList());
    }

    private List<PatrullajeSerenazgo> obtenerPatrullajesFiltrados(RangoFechas rango, FiltroEstadisticoRequest f) {
        return patrullajeRepository.findAll().stream()
                .filter(p -> p.getFechaInicio() != null &&
                        !p.getFechaInicio().isBefore(rango.getInicio()) &&
                        !p.getFechaInicio().isAfter(rango.getFin()))
                .filter(p -> f.getSectorId() == null || (p.getSector() != null && f.getSectorId().equals(p.getSector().getId())))
                .filter(p -> f.getModalidadPatrullaje() == null || f.getModalidadPatrullaje().isBlank() ||
                        (p.getTipoPatrullaje() != null && p.getTipoPatrullaje().name().equalsIgnoreCase(f.getModalidadPatrullaje())))
                .collect(Collectors.toList());
    }

    private List<SeguimientoSupervisionSerenazgo> obtenerSeguimientosFiltrados(RangoFechas rango) {
        return seguimientoRepository.findAll().stream()
                .filter(s -> s.getFechaCreacion() != null &&
                        !s.getFechaCreacion().isBefore(rango.getInicio()) &&
                        !s.getFechaCreacion().isAfter(rango.getFin()))
                .collect(Collectors.toList());
    }

    // =========================================================================
    // Algoritmos Estadísticos: KPIs, Variaciones, Promedios, Medianas y Percentiles
    // =========================================================================

    private IndicadorKpiDto crearKpi(String codigo, String nombre, String modulo, Long actual, Long anterior,
                                     String unidad, String definicion, String formula, String limitaciones,
                                     String fuente, String icono) {
        long act = actual != null ? actual : 0L;
        long ant = anterior != null ? anterior : 0L;
        long dif = act - ant;

        Double varPorc = null;
        boolean indefinida = true;
        if (ant > 0) {
            varPorc = Math.round(((double) dif / (double) ant * 100.0) * 10.0) / 10.0;
            indefinida = false;
        }

        String tendencia = "NEUTRA";
        if (dif > 0) {
            tendencia = "POSITIVA";
        } else if (dif < 0) {
            tendencia = "NEGATIVA";
        }
        if (indefinida) {
            tendencia = "INDEFINIDA";
        }

        return IndicadorKpiDto.builder()
                .codigo(codigo)
                .nombre(nombre)
                .modulo(modulo)
                .valorActual(act)
                .valorAnterior(ant)
                .variacionAbsoluta(dif)
                .variacionPorcentual(varPorc)
                .variacionIndefinida(indefinida)
                .unidad(unidad)
                .definicion(definicion)
                .formula(formula)
                .limitaciones(limitaciones)
                .fuente(fuente)
                .iconoLucide(icono)
                .tendencia(tendencia)
                .build();
    }

    private TiemposAtencionEstadisticasDto calcularTiemposAtencionConLista(List<AtencionIncidenciaSerenazgo> atenciones) {
        List<Double> asig = new ArrayList<>();
        List<Double> desp = new ArrayList<>();
        List<Double> acep = new ArrayList<>();
        List<Double> dur = new ArrayList<>();
        long incompletos = 0;

        for (AtencionIncidenciaSerenazgo a : atenciones) {
            boolean tieneAlgunTiempo = false;

            // Registro a Asignación
            if (a.getIncidencia() != null && a.getIncidencia().getFechaReporte() != null && a.getFechaHoraAsignacion() != null) {
                double min = Duration.between(a.getIncidencia().getFechaReporte(), a.getFechaHoraAsignacion()).toSeconds() / 60.0;
                if (min >= 0) {
                    asig.add(min);
                    tieneAlgunTiempo = true;
                }
            }

            // Asignación a Despacho
            if (a.getFechaHoraAsignacion() != null && a.getFechaHoraDespacho() != null) {
                double min = Duration.between(a.getFechaHoraAsignacion(), a.getFechaHoraDespacho()).toSeconds() / 60.0;
                if (min >= 0) {
                    desp.add(min);
                    tieneAlgunTiempo = true;
                }
            }

            // Despacho a Aceptación
            if (a.getFechaHoraDespacho() != null && a.getFechaHoraAceptacion() != null) {
                double min = Duration.between(a.getFechaHoraDespacho(), a.getFechaHoraAceptacion()).toSeconds() / 60.0;
                if (min >= 0) {
                    acep.add(min);
                    tieneAlgunTiempo = true;
                }
            }

            // Duración registrada de atención (Inicio a Fin)
            if (a.getFechaHoraInicio() != null && a.getFechaHoraFinalizacion() != null) {
                double min = Duration.between(a.getFechaHoraInicio(), a.getFechaHoraFinalizacion()).toSeconds() / 60.0;
                if (min >= 0) {
                    dur.add(min);
                    tieneAlgunTiempo = true;
                }
            }

            if (!tieneAlgunTiempo) {
                incompletos++;
            }
        }

        return TiemposAtencionEstadisticasDto.builder()
                .totalAtencionesValidas(atenciones.size() - incompletos)
                .registrosIncompletosExcluidos(incompletos)
                .promedioAsignacionMinutos(calcularPromedio(asig))
                .medianaAsignacionMinutos(calcularMediana(asig))
                .percentil90AsignacionMinutos(calcularPercentil(asig, 90))
                .promedioDespachoMinutos(calcularPromedio(desp))
                .medianaDespachoMinutos(calcularMediana(desp))
                .promedioAceptacionMinutos(calcularPromedio(acep))
                .medianaAceptacionMinutos(calcularMediana(acep))
                .promedioDuracionMinutos(calcularPromedio(dur))
                .medianaDuracionMinutos(calcularMediana(dur))
                .percentil90DuracionMinutos(calcularPercentil(dur, 90))
                .build();
    }

    private EstadisticasPatrullajeDto calcularEstadisticasPatrullajeConLista(List<PatrullajeSerenazgo> patrullajes) {
        long prog = patrullajes.size();
        long ini = patrullajes.stream().filter(p -> p.getEstado() == EstadoPatrullajeSerenazgo.EN_CURSO || p.getEstado() == EstadoPatrullajeSerenazgo.FINALIZADO).count();
        long fin = patrullajes.stream().filter(p -> p.getEstado() == EstadoPatrullajeSerenazgo.FINALIZADO).count();
        long can = patrullajes.stream().filter(p -> p.getEstado() == EstadoPatrullajeSerenazgo.CANCELADO).count();

        Double tasaCumpl = null;
        if (prog > 0) {
            tasaCumpl = Math.round(((double) fin / (double) prog * 100.0) * 10.0) / 10.0;
        }

        List<Double> durHoras = new ArrayList<>();
        long ptsPlan = 0;
        long ptsVis = 0;
        double distPlan = 0.0;
        double distGps = 0.0;

        for (PatrullajeSerenazgo p : patrullajes) {
            if (p.getFechaInicio() != null && p.getFechaFin() != null) {
                double horas = Duration.between(p.getFechaInicio(), p.getFechaFin()).toMinutes() / 60.0;
                if (horas >= 0) durHoras.add(horas);
            }
            if (p.getRuta() != null && p.getRuta().getPuntosControl() != null) {
                ptsPlan += p.getRuta().getPuntosControl().size();
            }
            if (p.getVisitasPuntos() != null) {
                ptsVis += p.getVisitasPuntos().stream().filter(pt -> pt.getEstadoVisita() == EstadoVisitaPunto.VISITADO).count();
            }
            if (p.getKmInicio() != null && p.getKmFin() != null) {
                double dist = p.getKmFin().subtract(p.getKmInicio()).doubleValue();
                if (dist >= 0) distGps += dist;
            }
        }

        Double tasaVisita = null;
        if (ptsPlan > 0) {
            tasaVisita = Math.round(((double) ptsVis / (double) ptsPlan * 100.0) * 10.0) / 10.0;
        }

        return EstadisticasPatrullajeDto.builder()
                .programados(prog)
                .iniciados(ini)
                .finalizados(fin)
                .cancelados(can)
                .tasaCumplimientoPorcentaje(tasaCumpl)
                .duracionPromedioHoras(calcularPromedio(durHoras))
                .duracionMedianaHoras(calcularMediana(durHoras))
                .puntosPlanificados(ptsPlan)
                .puntosVisitados(ptsVis)
                .puntosPendientes(ptsPlan - ptsVis)
                .tasaVisitaPuntosPorcentaje(tasaVisita)
                .distanciaPlanificadaKm(Math.round(distPlan * 10.0) / 10.0)
                .distanciaGpsRegistradaKm(Math.round(distGps * 10.0) / 10.0)
                .build();
    }

    private List<EstadisticasTerritorialesDto> calcularEstadisticasPorSectorConDatos(
            List<IncidenciaSeguridadCiudadana> inc,
            List<PatrullajeSerenazgo> pat,
            List<IntervencionSerenazgo> in) {

        List<SectorPatrullaje> sectores = sectorRepository.findAll();
        long totalInc = inc.size();

        Map<UUID, Long> incMap = inc.stream()
                .filter(i -> i.getSector() != null)
                .collect(Collectors.groupingBy(i -> i.getSector().getId(), Collectors.counting()));

        Map<UUID, Long> patMap = pat.stream()
                .filter(p -> p.getSector() != null)
                .collect(Collectors.groupingBy(p -> p.getSector().getId(), Collectors.counting()));

        Map<UUID, Long> intMap = in.stream()
                .filter(inter -> inter.getSector() != null)
                .collect(Collectors.groupingBy(inter -> inter.getSector().getId(), Collectors.counting()));

        return sectores.stream().map(s -> {
            long cInc = incMap.getOrDefault(s.getId(), 0L);
            long cPat = patMap.getOrDefault(s.getId(), 0L);
            long cInt = intMap.getOrDefault(s.getId(), 0L);
            Double porc = totalInc > 0 ? Math.round(((double) cInc / (double) totalInc * 100.0) * 10.0) / 10.0 : 0.0;

            return EstadisticasTerritorialesDto.builder()
                    .sectorId(s.getId())
                    .sectorCodigo(s.getCodigo())
                    .sectorNombre(s.getNombre())
                    .totalIncidencias(cInc)
                    .totalPatrullajes(cPat)
                    .totalIntervenciones(cInt)
                    .porcentajeIncidencias(porc)
                    .build();
        }).sorted(Comparator.comparing(EstadisticasTerritorialesDto::getTotalIncidencias).reversed())
          .collect(Collectors.toList());
    }

    // =========================================================================
    // Agregaciones de Distribución Categórica
    // =========================================================================

    private List<DistribucionItemDto> calcularDistribucionIncidenciasPorTipo(List<IncidenciaSeguridadCiudadana> lista) {
        long total = lista.size();
        Map<String, Long> conteo = lista.stream()
                .map(i -> i.getTipoIncidencia() != null ? i.getTipoIncidencia().getNombre() : "Sin Tipo")
                .collect(Collectors.groupingBy(k -> k, Collectors.counting()));

        return mapearDistribucion(conteo, total);
    }

    private List<DistribucionItemDto> calcularDistribucionIncidenciasPorEstado(List<IncidenciaSeguridadCiudadana> lista) {
        long total = lista.size();
        Map<String, Long> conteo = lista.stream()
                .map(i -> i.getEstado().name())
                .collect(Collectors.groupingBy(k -> k, Collectors.counting()));

        return mapearDistribucion(conteo, total);
    }

    private List<DistribucionItemDto> calcularDistribucionIncidenciasPorPrioridad(List<IncidenciaSeguridadCiudadana> lista) {
        long total = lista.size();
        Map<String, Long> conteo = lista.stream()
                .map(i -> i.getPrioridad().name())
                .collect(Collectors.groupingBy(k -> k, Collectors.counting()));

        return mapearDistribucion(conteo, total);
    }

    private List<DistribucionItemDto> calcularDistribucionAlertasPorEstado(List<AlertaCiudadanaSeguridad> lista) {
        long total = lista.size();
        Map<String, Long> conteo = lista.stream()
                .map(a -> a.getEstado().name())
                .collect(Collectors.groupingBy(k -> k, Collectors.counting()));

        return mapearDistribucion(conteo, total);
    }

    private List<DistribucionItemDto> calcularDistribucionIntervencionesPorTipo(List<IntervencionSerenazgo> lista) {
        long total = lista.size();
        Map<String, Long> conteo = lista.stream()
                .map(in -> in.getTipoIntervencion() != null ? in.getTipoIntervencion().name() : "OTRO")
                .collect(Collectors.groupingBy(k -> k, Collectors.counting()));

        return mapearDistribucion(conteo, total);
    }

    private List<DistribucionItemDto> calcularDistribucionIntervencionesPorResultado(List<IntervencionSerenazgo> lista) {
        long total = lista.size();
        Map<String, Long> conteo = lista.stream()
                .map(in -> in.getResultado() != null ? in.getResultado().name() : "SIN_RESULTADO")
                .collect(Collectors.groupingBy(k -> k, Collectors.counting()));

        return mapearDistribucion(conteo, total);
    }

    private List<DistribucionItemDto> calcularDistribucionPatrullajesPorModalidad(List<PatrullajeSerenazgo> lista) {
        long total = lista.size();
        Map<String, Long> conteo = lista.stream()
                .map(p -> p.getTipoPatrullaje() != null ? p.getTipoPatrullaje().name() : "PATRULLAJE_MUNICIPAL")
                .collect(Collectors.groupingBy(k -> k, Collectors.counting()));

        return mapearDistribucion(conteo, total);
    }

    private List<DistribucionItemDto> mapearDistribucion(Map<String, Long> conteo, long total) {
        return conteo.entrySet().stream()
                .map(e -> {
                    double porc = total > 0 ? Math.round(((double) e.getValue() / (double) total * 100.0) * 10.0) / 10.0 : 0.0;
                    return DistribucionItemDto.builder()
                            .clave(e.getKey())
                            .etiqueta(e.getKey())
                            .cantidad(e.getValue())
                            .porcentaje(porc)
                            .build();
                })
                .sorted(Comparator.comparing(DistribucionItemDto::getCantidad).reversed())
                .collect(Collectors.toList());
    }

    // =========================================================================
    // Agregaciones de Series Temporales (Días, Semanas, Meses)
    // =========================================================================

    private SerieTemporalDto generarSerieTemporalIncidencias(
            List<IncidenciaSeguridadCiudadana> actual,
            List<IncidenciaSeguridadCiudadana> anterior,
            RangoFechas rango,
            String agrupacionStr) {

        AgrupacionTemporal agrupacion = parseAgrupacion(agrupacionStr);
        Map<String, Long> mapaAct = agruparIncidenciasPorTiempo(actual, agrupacion);
        Map<String, Long> mapaAnt = agruparIncidenciasPorTiempo(anterior, agrupacion);

        List<SerieTemporalPuntoDto> puntos = generarPuntosCronologicos(rango, agrupacion, mapaAct, mapaAnt);

        return SerieTemporalDto.builder()
                .identificador("SERIE_INCIDENCIAS")
                .nombreSerie("Evolución de Incidencias Registradas")
                .unidad("Incidencias")
                .puntos(puntos)
                .build();
    }

    private SerieTemporalDto generarSerieTemporalPatrullajes(
            List<PatrullajeSerenazgo> actual,
            List<PatrullajeSerenazgo> anterior,
            RangoFechas rango,
            String agrupacionStr) {

        AgrupacionTemporal agrupacion = parseAgrupacion(agrupacionStr);
        Map<String, Long> mapaAct = agruparPatrullajesPorTiempo(actual, agrupacion);
        Map<String, Long> mapaAnt = agruparPatrullajesPorTiempo(anterior, agrupacion);

        List<SerieTemporalPuntoDto> puntos = generarPuntosCronologicos(rango, agrupacion, mapaAct, mapaAnt);

        return SerieTemporalDto.builder()
                .identificador("SERIE_PATRULLAJES")
                .nombreSerie("Patrullajes Ejecutados")
                .unidad("Patrullajes")
                .puntos(puntos)
                .build();
    }

    private Map<String, Long> agruparIncidenciasPorTiempo(List<IncidenciaSeguridadCiudadana> lista, AgrupacionTemporal ag) {
        return lista.stream()
                .filter(i -> i.getFechaReporte() != null)
                .collect(Collectors.groupingBy(i -> formatearFechaSegunAgrupacion(i.getFechaReporte().toLocalDate(), ag), Collectors.counting()));
    }

    private Map<String, Long> agruparPatrullajesPorTiempo(List<PatrullajeSerenazgo> lista, AgrupacionTemporal ag) {
        return lista.stream()
                .filter(p -> p.getFechaInicio() != null)
                .collect(Collectors.groupingBy(p -> formatearFechaSegunAgrupacion(p.getFechaInicio().toLocalDate(), ag), Collectors.counting()));
    }

    private List<SerieTemporalPuntoDto> generarPuntosCronologicos(
            RangoFechas rango, AgrupacionTemporal ag, Map<String, Long> act, Map<String, Long> ant) {

        List<SerieTemporalPuntoDto> puntos = new ArrayList<>();
        LocalDate cur = rango.getInicio().toLocalDate();
        LocalDate fin = rango.getFin().toLocalDate();

        Set<String> procesados = new LinkedHashSet<>();
        while (!cur.isAfter(fin)) {
            String clave = formatearFechaSegunAgrupacion(cur, ag);
            if (procesados.add(clave)) {
                long vAct = act.getOrDefault(clave, 0L);
                long vAnt = ant.getOrDefault(clave, 0L);
                puntos.add(SerieTemporalPuntoDto.builder()
                        .periodoClave(clave)
                        .etiqueta(generarEtiquetaPunto(cur, ag))
                        .valorActual(vAct)
                        .valorAnterior(vAnt)
                        .build());
            }

            if (ag == AgrupacionTemporal.DIA) cur = cur.plusDays(1);
            else if (ag == AgrupacionTemporal.SEMANA) cur = cur.plusWeeks(1);
            else if (ag == AgrupacionTemporal.MES) cur = cur.plusMonths(1);
            else if (ag == AgrupacionTemporal.TRIMESTRE) cur = cur.plusMonths(3);
            else cur = cur.plusYears(1);
        }

        return puntos;
    }

    private String formatearFechaSegunAgrupacion(LocalDate fecha, AgrupacionTemporal ag) {
        switch (ag) {
            case DIA:
                return fecha.format(DateTimeFormatter.ISO_LOCAL_DATE);
            case SEMANA:
                int semana = fecha.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                int anioSemana = fecha.get(IsoFields.WEEK_BASED_YEAR);
                return String.format("%d-W%02d", anioSemana, semana);
            case MES:
                return fecha.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            case TRIMESTRE:
                int q = (fecha.getMonthValue() - 1) / 3 + 1;
                return String.format("%d-Q%d", fecha.getYear(), q);
            case ANIO:
            default:
                return String.valueOf(fecha.getYear());
        }
    }

    private String generarEtiquetaPunto(LocalDate fecha, AgrupacionTemporal ag) {
        switch (ag) {
            case DIA:
                return fecha.format(DateTimeFormatter.ofPattern("dd/MM"));
            case SEMANA:
                return "Sem " + fecha.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
            case MES:
                return fecha.format(DateTimeFormatter.ofPattern("MMM yyyy", new Locale("es", "PE")));
            case TRIMESTRE:
                return "T" + ((fecha.getMonthValue() - 1) / 3 + 1) + " " + fecha.getYear();
            case ANIO:
            default:
                return String.valueOf(fecha.getYear());
        }
    }

    private AgrupacionTemporal parseAgrupacion(String ag) {
        if (ag == null || ag.isBlank()) return AgrupacionTemporal.MES;
        try {
            return AgrupacionTemporal.valueOf(ag.toUpperCase().trim());
        } catch (Exception e) {
            return AgrupacionTemporal.MES;
        }
    }

    // =========================================================================
    // Métodos Matemáticos: Promedio, Mediana y Percentil
    // =========================================================================

    private Double calcularPromedio(List<Double> valores) {
        if (valores == null || valores.isEmpty()) return null;
        double sum = 0.0;
        for (Double v : valores) sum += v;
        return Math.round((sum / valores.size()) * 10.0) / 10.0;
    }

    private Double calcularMediana(List<Double> valores) {
        if (valores == null || valores.isEmpty()) return null;
        List<Double> ordenados = new ArrayList<>(valores);
        Collections.sort(ordenados);
        int n = ordenados.size();
        if (n % 2 == 1) {
            return Math.round(ordenados.get(n / 2) * 10.0) / 10.0;
        } else {
            double m = (ordenados.get((n / 2) - 1) + ordenados.get(n / 2)) / 2.0;
            return Math.round(m * 10.0) / 10.0;
        }
    }

    private Double calcularPercentil(List<Double> valores, int p) {
        if (valores == null || valores.isEmpty()) return null;
        List<Double> ordenados = new ArrayList<>(valores);
        Collections.sort(ordenados);
        int idx = (int) Math.ceil((p / 100.0) * ordenados.size()) - 1;
        if (idx < 0) idx = 0;
        if (idx >= ordenados.size()) idx = ordenados.size() - 1;
        return Math.round(ordenados.get(idx) * 10.0) / 10.0;
    }

    // =========================================================================
    // Resolución de Rangos de Fechas en America/Lima
    // =========================================================================

    private RangoFechas resolverRangoFechasActual(FiltroEstadisticoRequest f) {
        LocalDate hoy = LocalDate.now(ZONA_LIMA);
        String tipo = f.getPeriodoTipo() != null ? f.getPeriodoTipo().toUpperCase().trim() : "MES_ACTUAL";

        OffsetDateTime ini;
        OffsetDateTime fin;
        String etiqueta;

        switch (tipo) {
            case "HOY":
                ini = hoy.atStartOfDay(ZONA_LIMA).toOffsetDateTime();
                fin = hoy.atTime(23, 59, 59).atZone(ZONA_LIMA).toOffsetDateTime();
                etiqueta = "Hoy (" + hoy.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ")";
                break;
            case "ULTIMOS_7_DIAS":
                LocalDate desde7 = hoy.minusDays(6);
                ini = desde7.atStartOfDay(ZONA_LIMA).toOffsetDateTime();
                fin = hoy.atTime(23, 59, 59).atZone(ZONA_LIMA).toOffsetDateTime();
                etiqueta = "Últimos 7 días (" + desde7.format(DateTimeFormatter.ofPattern("dd/MM")) + " - " + hoy.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ")";
                break;
            case "TRIMESTRE_ACTUAL":
                int mesInicioTrimestre = ((hoy.getMonthValue() - 1) / 3) * 3 + 1;
                LocalDate inicioQ = LocalDate.of(hoy.getYear(), mesInicioTrimestre, 1);
                LocalDate finQ = inicioQ.plusMonths(3).minusDays(1);
                ini = inicioQ.atStartOfDay(ZONA_LIMA).toOffsetDateTime();
                fin = finQ.atTime(23, 59, 59).atZone(ZONA_LIMA).toOffsetDateTime();
                etiqueta = "Trimestre " + ((hoy.getMonthValue() - 1) / 3 + 1) + " " + hoy.getYear();
                break;
            case "ANIO_ACTUAL":
                LocalDate inicioAnio = LocalDate.of(hoy.getYear(), 1, 1);
                LocalDate finAnio = LocalDate.of(hoy.getYear(), 12, 31);
                ini = inicioAnio.atStartOfDay(ZONA_LIMA).toOffsetDateTime();
                fin = finAnio.atTime(23, 59, 59).atZone(ZONA_LIMA).toOffsetDateTime();
                etiqueta = "Año " + hoy.getYear();
                break;
            case "PERSONALIZADO":
                LocalDate fIni = f.getFechaInicio() != null ? f.getFechaInicio() : hoy.withDayOfMonth(1);
                LocalDate fFin = f.getFechaFin() != null ? f.getFechaFin() : hoy;
                if (fIni.isAfter(fFin)) fIni = fFin;
                ini = fIni.atStartOfDay(ZONA_LIMA).toOffsetDateTime();
                fin = fFin.atTime(23, 59, 59).atZone(ZONA_LIMA).toOffsetDateTime();
                etiqueta = fIni.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " - " + fFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                break;
            case "MES_ACTUAL":
            default:
                LocalDate inicioMes = hoy.withDayOfMonth(1);
                LocalDate finMes = hoy.withDayOfMonth(hoy.lengthOfMonth());
                ini = inicioMes.atStartOfDay(ZONA_LIMA).toOffsetDateTime();
                fin = finMes.atTime(23, 59, 59).atZone(ZONA_LIMA).toOffsetDateTime();
                etiqueta = hoy.format(DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("es", "PE")));
                break;
        }

        return new RangoFechas(ini, fin, etiqueta);
    }

    private RangoFechas resolverRangoFechasAnterior(FiltroEstadisticoRequest f, RangoFechas actual) {
        String tipo = f.getPeriodoTipo() != null ? f.getPeriodoTipo().toUpperCase().trim() : "MES_ACTUAL";
        LocalDate hoy = LocalDate.now(ZONA_LIMA);

        OffsetDateTime ini;
        OffsetDateTime fin;
        String etiqueta;

        switch (tipo) {
            case "HOY":
                LocalDate ayer = hoy.minusDays(1);
                ini = ayer.atStartOfDay(ZONA_LIMA).toOffsetDateTime();
                fin = ayer.atTime(23, 59, 59).atZone(ZONA_LIMA).toOffsetDateTime();
                etiqueta = "Ayer (" + ayer.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ")";
                break;
            case "ULTIMOS_7_DIAS":
                LocalDate finPrev = hoy.minusDays(7);
                LocalDate iniPrev = finPrev.minusDays(6);
                ini = iniPrev.atStartOfDay(ZONA_LIMA).toOffsetDateTime();
                fin = finPrev.atTime(23, 59, 59).atZone(ZONA_LIMA).toOffsetDateTime();
                etiqueta = "7 días previos (" + iniPrev.format(DateTimeFormatter.ofPattern("dd/MM")) + " - " + finPrev.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ")";
                break;
            case "TRIMESTRE_ACTUAL":
                OffsetDateTime iniQAct = actual.getInicio();
                ini = iniQAct.minusMonths(3);
                fin = actual.getFin().minusMonths(3);
                etiqueta = "Trimestre Anterior";
                break;
            case "ANIO_ACTUAL":
                int anioAnt = hoy.getYear() - 1;
                LocalDate inicioAnioAnt = LocalDate.of(anioAnt, 1, 1);
                LocalDate finAnioAnt = LocalDate.of(anioAnt, 12, 31);
                ini = inicioAnioAnt.atStartOfDay(ZONA_LIMA).toOffsetDateTime();
                fin = finAnioAnt.atTime(23, 59, 59).atZone(ZONA_LIMA).toOffsetDateTime();
                etiqueta = "Año " + anioAnt;
                break;
            case "PERSONALIZADO":
                long dias = Duration.between(actual.getInicio(), actual.getFin()).toDays() + 1;
                fin = actual.getInicio().minusSeconds(1);
                ini = fin.minusDays(dias - 1).withHour(0).withMinute(0).withSecond(0);
                etiqueta = "Intervalo previo equivalente (" + dias + " días)";
                break;
            case "MES_ACTUAL":
            default:
                LocalDate mesAnt = hoy.minusMonths(1);
                LocalDate iniMesAnt = mesAnt.withDayOfMonth(1);
                LocalDate finMesAnt = mesAnt.withDayOfMonth(mesAnt.lengthOfMonth());
                ini = iniMesAnt.atStartOfDay(ZONA_LIMA).toOffsetDateTime();
                fin = finMesAnt.atTime(23, 59, 59).atZone(ZONA_LIMA).toOffsetDateTime();
                etiqueta = mesAnt.format(DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("es", "PE")));
                break;
        }

        return new RangoFechas(ini, fin, etiqueta);
    }

    @Getter
    @RequiredArgsConstructor
    public static class RangoFechas {
        private final OffsetDateTime inicio;
        private final OffsetDateTime fin;
        private final String etiqueta;
    }
}
