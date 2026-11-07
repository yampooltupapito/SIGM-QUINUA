package pe.gob.municipalidad.sigm.modules.mercados.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.mercados.dto.DashboardMercadosResponse;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ReporteMercadoFilter;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.*;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;
import pe.gob.municipalidad.sigm.modules.mercados.service.IndicadoresMercadosService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class IndicadoresMercadosServiceImpl implements IndicadoresMercadosService {

    private final MercadoMunicipalRepository mercadoRepository;
    private final SectorMercadoRepository sectorRepository;
    private final PabellonMercadoRepository pabellonRepository;
    private final PuestoComercialRepository puestoRepository;
    private final ComercianteRepository comercianteRepository;
    private final AsignacionPuestoRepository asignacionRepository;
    private final ContratoOcupacionRepository contratoRepository;
    private final AutorizacionComercialRepository autorizacionRepository;
    private final ObligacionMercadoRepository obligacionRepository;
    private final PagoMercadoRepository pagoRepository;
    private final ReversoPagoMercadoRepository reversoRepository;
    private final InspeccionMercadoRepository inspeccionRepository;
    private final IncidenciaMercadoRepository incidenciaRepository;
    private final SolicitudGestionOcupacionRepository solicitudRepository;

    public IndicadoresMercadosServiceImpl(
            MercadoMunicipalRepository mercadoRepository,
            SectorMercadoRepository sectorRepository,
            PabellonMercadoRepository pabellonRepository,
            PuestoComercialRepository puestoRepository,
            ComercianteRepository comercianteRepository,
            AsignacionPuestoRepository asignacionRepository,
            ContratoOcupacionRepository contratoRepository,
            AutorizacionComercialRepository autorizacionRepository,
            ObligacionMercadoRepository obligacionRepository,
            PagoMercadoRepository pagoRepository,
            ReversoPagoMercadoRepository reversoRepository,
            InspeccionMercadoRepository inspeccionRepository,
            IncidenciaMercadoRepository incidenciaRepository,
            SolicitudGestionOcupacionRepository solicitudRepository) {
        this.mercadoRepository = mercadoRepository;
        this.sectorRepository = sectorRepository;
        this.pabellonRepository = pabellonRepository;
        this.puestoRepository = puestoRepository;
        this.comercianteRepository = comercianteRepository;
        this.asignacionRepository = asignacionRepository;
        this.contratoRepository = contratoRepository;
        this.autorizacionRepository = autorizacionRepository;
        this.obligacionRepository = obligacionRepository;
        this.pagoRepository = pagoRepository;
        this.reversoRepository = reversoRepository;
        this.inspeccionRepository = inspeccionRepository;
        this.incidenciaRepository = incidenciaRepository;
        this.solicitudRepository = solicitudRepository;
    }

    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");

    @Override
    @Transactional(readOnly = true)
    public DashboardMercadosResponse obtenerDashboard(ReporteMercadoFilter filtro) {
        UUID mercadoId = filtro != null ? filtro.getMercadoId() : null;
        LocalDate[] rango = filtro != null ? filtro.resolverRangoEfectivo() : PeriodoConsulta.MES_ACTUAL.resolverRango(null, null);
        LocalDate fechaDesde = rango[0];
        LocalDate fechaHasta = rango[1];

        // 1. Mercados, Sectores y Pabellones
        List<MercadoMunicipal> mercados = mercadoRepository.findAll();
        long totalMercados = mercados.size();
        long mercadosActivos = mercados.stream().filter(m -> m.getEstado() == EstadoMercado.ACTIVO).count();

        long totalSectores = mercadoId != null ? sectorRepository.countByMercadoId(mercadoId) : sectorRepository.count();
        long totalPabellones = mercadoId != null ? pabellonRepository.countByMercadoId(mercadoId) : pabellonRepository.count();

        // 2. Puestos y Ocupación
        List<PuestoComercial> puestos = mercadoId != null ? puestoRepository.findByMercadoId(mercadoId) : puestoRepository.findAll();
        long totalPuestos = puestos.size();
        long ocupados = puestos.stream().filter(p -> p.getEstado() == EstadoPuesto.OCUPADO).count();
        long disponibles = puestos.stream().filter(p -> p.getEstado() == EstadoPuesto.DISPONIBLE).count();
        long mantenimiento = puestos.stream().filter(p -> p.getEstado() == EstadoPuesto.EN_MANTENIMIENTO).count();
        long restringidos = puestos.stream().filter(p -> p.getEstado() == EstadoPuesto.INHABILITADO || p.getEstado() == EstadoPuesto.RESERVADO).count();
        long inactivos = puestos.stream().filter(p -> p.getEstado() == EstadoPuesto.NO_ASIGNABLE).count();

        long habilitados = totalPuestos - inactivos;
        BigDecimal porcentajeOcupacion = BigDecimal.ZERO;
        String textoOcupacion;
        if (habilitados > 0) {
            porcentajeOcupacion = BigDecimal.valueOf(ocupados)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(habilitados), 1, RoundingMode.HALF_UP);
            textoOcupacion = porcentajeOcupacion + "% ocupación";
        } else {
            textoOcupacion = "Sin datos suficientes";
        }

        // 3. Comerciantes y Asignaciones
        List<Comerciante> comerciantes = comercianteRepository.findAll();
        long totalComerciantes = comerciantes.size();
        long comerciantesActivos = comerciantes.stream().filter(c -> c.getEstado() == EstadoComerciante.ACTIVO).count();

        List<AsignacionPuesto> asignaciones = asignacionRepository.findAll();
        long asignacionesVigentes = asignaciones.stream()
                .filter(a -> a.getEstado() == EstadoAsignacion.VIGENTE && (mercadoId == null || a.getPuesto().getMercado().getId().equals(mercadoId)))
                .count();

        List<ContratoOcupacion> contratos = contratoRepository.findAll();
        long contratosVigentes = contratos.stream()
                .filter(c -> c.getEstado() == EstadoContrato.VIGENTE && (mercadoId == null || (c.getPuesto() != null && c.getPuesto().getMercado().getId().equals(mercadoId))))
                .count();

        List<AutorizacionComercial> autorizaciones = autorizacionRepository.findAll();
        long autorizacionesVigentes = autorizaciones.stream()
                .filter(a -> a.getEstado() == EstadoAutorizacion.VIGENTE && (mercadoId == null || (a.getPuesto() != null && a.getPuesto().getMercado().getId().equals(mercadoId))))
                .count();

        LocalDate hoy = LocalDate.now(ZONA_LIMA);
        LocalDate fechaLimiteVencimiento = hoy.plusDays(30);
        long contratosPorVencer = contratos.stream()
                .filter(c -> c.getEstado() == EstadoContrato.VIGENTE && c.getFechaFin() != null && !c.getFechaFin().isBefore(hoy) && !c.getFechaFin().isAfter(fechaLimiteVencimiento))
                .count();
        long autPorVencer = autorizaciones.stream()
                .filter(a -> a.getEstado() == EstadoAutorizacion.VIGENTE && a.getFechaFin() != null && !a.getFechaFin().isBefore(hoy) && !a.getFechaFin().isAfter(fechaLimiteVencimiento))
                .count();
        long instrumentosPorVencer = contratosPorVencer + autPorVencer;

        // 4. Finanzas en Rango de Fechas
        List<ObligacionMercado> obligaciones = obligacionRepository.findAll();
        BigDecimal obligacionesEmitidas = obligaciones.stream()
                .filter(o -> (mercadoId == null || (o.getPuesto() != null && o.getPuesto().getMercado().getId().equals(mercadoId))) &&
                             (o.getFechaEmision() != null && !o.getFechaEmision().isBefore(fechaDesde) && !o.getFechaEmision().isAfter(fechaHasta)))
                .map(ObligacionMercado::getMontoOriginal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long obligacionesPendientesCount = obligaciones.stream()
                .filter(o -> (mercadoId == null || (o.getPuesto() != null && o.getPuesto().getMercado().getId().equals(mercadoId))) &&
                             (o.getEstado() == EstadoObligacion.PENDIENTE || o.getEstado() == EstadoObligacion.VENCIDA || o.getEstado() == EstadoObligacion.PARCIALMENTE_PAGADA))
                .count();

        BigDecimal deudaExigible = obligaciones.stream()
                .filter(o -> (mercadoId == null || (o.getPuesto() != null && o.getPuesto().getMercado().getId().equals(mercadoId))) &&
                             (o.getEstado() == EstadoObligacion.PENDIENTE || o.getEstado() == EstadoObligacion.VENCIDA || o.getEstado() == EstadoObligacion.PARCIALMENTE_PAGADA))
                .map(ObligacionMercado::getSaldoPendiente)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        OffsetDateTime startTz = fechaDesde.atStartOfDay(ZONA_LIMA).toOffsetDateTime();
        OffsetDateTime endTz = fechaHasta.atTime(23, 59, 59).atZone(ZONA_LIMA).toOffsetDateTime();

        List<PagoMercado> pagos = pagoRepository.findAll();
        List<PagoMercado> pagosPeriodo = pagos.stream()
                .filter(p -> p.getEstado() == EstadoPagoMercado.CONFIRMADO &&
                             !p.getFechaPago().isBefore(startTz) && !p.getFechaPago().isAfter(endTz))
                .collect(Collectors.toList());

        BigDecimal recaudacionBruta = pagosPeriodo.stream()
                .map(PagoMercado::getMontoRecibido)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<ReversoPagoMercado> reversos = reversoRepository.findAll();
        BigDecimal reversosMonto = reversos.stream()
                .filter(r -> !r.getFechaReverso().isBefore(startTz) && !r.getFechaReverso().isAfter(endTz))
                .map(ReversoPagoMercado::getMontoReversado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal recaudacionNeta = recaudacionBruta.subtract(reversosMonto);
        if (recaudacionNeta.compareTo(BigDecimal.ZERO) < 0) {
            recaudacionNeta = BigDecimal.ZERO;
        }

        // 5. Comparación con período anterior
        long diasPeriodo = java.time.temporal.ChronoUnit.DAYS.between(fechaDesde, fechaHasta) + 1;
        LocalDate desdeAnt = fechaDesde.minusDays(diasPeriodo);
        LocalDate hastaAnt = fechaDesde.minusDays(1);
        OffsetDateTime startAnt = desdeAnt.atStartOfDay(ZONA_LIMA).toOffsetDateTime();
        OffsetDateTime endAnt = hastaAnt.atTime(23, 59, 59).atZone(ZONA_LIMA).toOffsetDateTime();

        BigDecimal recAntBruta = pagos.stream()
                .filter(p -> p.getEstado() == EstadoPagoMercado.CONFIRMADO &&
                             !p.getFechaPago().isBefore(startAnt) && !p.getFechaPago().isAfter(endAnt))
                .map(PagoMercado::getMontoRecibido)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal revAntMonto = reversos.stream()
                .filter(r -> !r.getFechaReverso().isBefore(startAnt) && !r.getFechaReverso().isAfter(endAnt))
                .map(ReversoPagoMercado::getMontoReversado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal recaudacionAntNeta = recAntBruta.subtract(revAntMonto);
        if (recaudacionAntNeta.compareTo(BigDecimal.ZERO) < 0) recaudacionAntNeta = BigDecimal.ZERO;

        BigDecimal variacionPorcentaje = BigDecimal.ZERO;
        boolean tendenciaPositiva = true;
        if (recaudacionAntNeta.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diff = recaudacionNeta.subtract(recaudacionAntNeta);
            variacionPorcentaje = diff.multiply(BigDecimal.valueOf(100)).divide(recaudacionAntNeta, 1, RoundingMode.HALF_UP);
            tendenciaPositiva = diff.compareTo(BigDecimal.ZERO) >= 0;
        }

        // 6. Fiscalización y Operaciones
        List<InspeccionMercado> inspecciones = inspeccionRepository.findAll();
        long totalInspeccionesPeriodo = inspecciones.stream()
                .filter(i -> (mercadoId == null || i.getMercado().getId().equals(mercadoId)) &&
                             (i.getFechaProgramada() != null && !i.getFechaProgramada().isBefore(fechaDesde) && !i.getFechaProgramada().isAfter(fechaHasta)))
                .count();

        long inspeccionesConHallazgos = inspecciones.stream()
                .filter(i -> (mercadoId == null || i.getMercado().getId().equals(mercadoId)) &&
                             i.getResultadoGeneral() != null && (i.getResultadoGeneral().contains("OBSERVAD") || i.getResultadoGeneral().contains("DESFAVORABLE")))
                .count();

        List<IncidenciaMercado> incidencias = incidenciaRepository.findAll();
        long incidenciasAbiertas = incidencias.stream()
                .filter(inc -> (mercadoId == null || inc.getMercado().getId().equals(mercadoId)) &&
                               (inc.getEstado() == EstadoIncidencia.REGISTRADA || inc.getEstado() == EstadoIncidencia.EN_ATENCION))
                .count();

        long incidenciasCriticas = incidencias.stream()
                .filter(inc -> (mercadoId == null || inc.getMercado().getId().equals(mercadoId)) &&
                               inc.getPrioridad() == PrioridadIncidencia.CRITICA &&
                               (inc.getEstado() == EstadoIncidencia.REGISTRADA || inc.getEstado() == EstadoIncidencia.EN_ATENCION))
                .count();

        long renovacionesEnTramite = solicitudRepository.countByEstado(EstadoSolicitudGestion.EN_EVALUACION);

        // 7. Gráficos Reales
        // Ocupación por mercado
        List<Map<String, Object>> ocupacionPorMercado = mercados.stream().map(m -> {
            List<PuestoComercial> pMerc = puestoRepository.findByMercadoId(m.getId());
            long ocu = pMerc.stream().filter(p -> p.getEstado() == EstadoPuesto.OCUPADO).count();
            long hab = pMerc.stream().filter(p -> p.getEstado() != EstadoPuesto.NO_ASIGNABLE).count();
            BigDecimal pct = hab > 0 ? BigDecimal.valueOf(ocu).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(hab), 1, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            Map<String, Object> map = new HashMap<>();
            map.put("mercadoNombre", m.getNombre());
            map.put("total", pMerc.size());
            map.put("ocupados", ocu);
            map.put("disponibles", pMerc.stream().filter(p -> p.getEstado() == EstadoPuesto.DISPONIBLE).count());
            map.put("porcentaje", pct);
            return map;
        }).collect(Collectors.toList());

        // Recaudación mensual de los últimos 6 meses
        List<Map<String, Object>> recMensual = new ArrayList<>();
        YearMonth ymActual = YearMonth.from(hoy);
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = ymActual.minusMonths(i);
            LocalDate d1 = ym.atDay(1);
            LocalDate d2 = ym.atEndOfMonth();
            OffsetDateTime ot1 = d1.atStartOfDay(ZONA_LIMA).toOffsetDateTime();
            OffsetDateTime ot2 = d2.atTime(23, 59, 59).atZone(ZONA_LIMA).toOffsetDateTime();

            BigDecimal monto = pagos.stream()
                    .filter(p -> p.getEstado() == EstadoPagoMercado.CONFIRMADO &&
                                 !p.getFechaPago().isBefore(ot1) && !p.getFechaPago().isAfter(ot2))
                    .map(PagoMercado::getMontoRecibido)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> mesMap = new HashMap<>();
            mesMap.put("mes", ym.getMonth().name().substring(0, 3) + " " + ym.getYear());
            mesMap.put("monto", monto);
            recMensual.add(mesMap);
        }

        // Incidencias por estado
        List<Map<String, Object>> incPorEstado = Arrays.stream(EstadoIncidencia.values()).map(est -> {
            long count = incidencias.stream()
                    .filter(inc -> (mercadoId == null || inc.getMercado().getId().equals(mercadoId)) && inc.getEstado() == est)
                    .count();
            Map<String, Object> map = new HashMap<>();
            map.put("estado", est.getNombre());
            map.put("cantidad", count);
            return map;
        }).collect(Collectors.toList());

        // Obligaciones por concepto (top 5)
        Map<String, BigDecimal> conceptosMap = new HashMap<>();
        for (ObligacionMercado o : obligaciones) {
            if (mercadoId == null || (o.getPuesto() != null && o.getPuesto().getMercado().getId().equals(mercadoId))) {
                String cNombre = o.getConcepto() != null ? o.getConcepto().getNombre() : "Tarifa General";
                conceptosMap.put(cNombre, conceptosMap.getOrDefault(cNombre, BigDecimal.ZERO).add(o.getMontoOriginal()));
            }
        }
        List<Map<String, Object>> oblPorConcepto = conceptosMap.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(5)
                .map(e -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("concepto", e.getKey());
                    map.put("monto", e.getValue());
                    return map;
                }).collect(Collectors.toList());

        return DashboardMercadosResponse.builder()
                .totalMercados(totalMercados)
                .mercadosActivos(mercadosActivos)
                .totalSectores(totalSectores)
                .totalPabellones(totalPabellones)
                .totalPuestos(totalPuestos)
                .puestosOcupados(ocupados)
                .puestosDisponibles(disponibles)
                .puestosMantenimiento(mantenimiento)
                .puestosRestringidos(restringidos)
                .porcentajeOcupacion(porcentajeOcupacion)
                .textoOcupacion(textoOcupacion)
                .totalComerciantes(totalComerciantes)
                .comerciantesActivos(comerciantesActivos)
                .asignacionesVigentes(asignacionesVigentes)
                .contratosVigentes(contratosVigentes)
                .autorizacionesVigentes(autorizacionesVigentes)
                .instrumentosPorVencer(instrumentosPorVencer)
                .obligacionesEmitidasMonto(obligacionesEmitidas)
                .recaudacionBrutaMonto(recaudacionBruta)
                .reversosMonto(reversosMonto)
                .recaudacionNetaMonto(recaudacionNeta)
                .deudaExigibleMonto(deudaExigible)
                .obligacionesPendientesCount(obligacionesPendientesCount)
                .pagosConfirmadosCount(pagosPeriodo.size())
                .inspeccionesRealizadas(totalInspeccionesPeriodo)
                .inspeccionesConHallazgos(inspeccionesConHallazgos)
                .incidenciasAbiertas(incidenciasAbiertas)
                .incidenciasCriticas(incidenciasCriticas)
                .renovacionesEnTramite(renovacionesEnTramite)
                .recaudacionPeriodoAnterior(recaudacionAntNeta)
                .variacionRecaudacionPorcentaje(variacionPorcentaje)
                .tendenciaRecaudacionPositiva(tendenciaPositiva)
                .ocupacionPorMercado(ocupacionPorMercado)
                .recaudacionMensual(recMensual)
                .incidenciasPorEstado(incPorEstado)
                .obligacionesPorConcepto(oblPorConcepto)
                .build();
    }
}
