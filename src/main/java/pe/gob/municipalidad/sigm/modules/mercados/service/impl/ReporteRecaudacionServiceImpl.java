package pe.gob.municipalidad.sigm.modules.mercados.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.mercados.dto.DeudaReporteItem;
import pe.gob.municipalidad.sigm.modules.mercados.dto.RecaudacionReporteItem;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ReporteMercadoFilter;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoObligacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPagoMercado;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;
import pe.gob.municipalidad.sigm.modules.mercados.service.ReporteRecaudacionService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReporteRecaudacionServiceImpl implements ReporteRecaudacionService {

    private final PagoMercadoRepository pagoRepository;
    private final ReversoPagoMercadoRepository reversoRepository;
    private final ObligacionMercadoRepository obligacionRepository;
    private final MercadoMunicipalRepository mercadoRepository;

    public ReporteRecaudacionServiceImpl(
            PagoMercadoRepository pagoRepository,
            ReversoPagoMercadoRepository reversoRepository,
            ObligacionMercadoRepository obligacionRepository,
            MercadoMunicipalRepository mercadoRepository) {
        this.pagoRepository = pagoRepository;
        this.reversoRepository = reversoRepository;
        this.obligacionRepository = obligacionRepository;
        this.mercadoRepository = mercadoRepository;
    }

    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");

    @Override
    @Transactional(readOnly = true)
    public Page<RecaudacionReporteItem> listarRecaudacion(ReporteMercadoFilter filtro, Pageable pageable) {
        List<RecaudacionReporteItem> todos = listarTodaRecaudacion(filtro);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), todos.size());
        List<RecaudacionReporteItem> sublist = start <= end ? todos.subList(start, end) : Collections.emptyList();
        return new PageImpl<>(sublist, pageable, todos.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecaudacionReporteItem> listarTodaRecaudacion(ReporteMercadoFilter filtro) {
        UUID mercadoId = filtro != null ? filtro.getMercadoId() : null;
        String termino = filtro != null && filtro.getTermino() != null ? filtro.getTermino().trim().toLowerCase() : null;
        LocalDate[] rango = filtro != null ? filtro.resolverRangoEfectivo() : new LocalDate[]{LocalDate.now().minusMonths(1), LocalDate.now()};
        OffsetDateTime startTz = rango[0].atStartOfDay(ZONA_LIMA).toOffsetDateTime();
        OffsetDateTime endTz = rango[1].atTime(23, 59, 59).atZone(ZONA_LIMA).toOffsetDateTime();

        List<PagoMercado> pagos = pagoRepository.findAll();

        return pagos.stream()
                .filter(p -> {
                    // Criterio 26 y 27: Incluir únicamente pagos válidos según criterio contable
                    if (p.getFechaPago().isBefore(startTz) || p.getFechaPago().isAfter(endTz)) {
                        return false;
                    }
                    if (termino != null) {
                        String cod = p.getCodigoPago().toLowerCase();
                        String com = p.getComerciante() != null ? p.getComerciante().getNombreOTitular().toLowerCase() : "";
                        String ref = p.getReferenciaOperacion() != null ? p.getReferenciaOperacion().toLowerCase() : "";
                        if (!cod.contains(termino) && !com.contains(termino) && !ref.contains(termino)) {
                            return false;
                        }
                    }
                    return true;
                })
                .flatMap(p -> {
                    // Criterio 30: Atribuir importes a conceptos usando aplicaciones de pago
                    if (p.getAplicaciones() != null && !p.getAplicaciones().isEmpty()) {
                        return p.getAplicaciones().stream().map(ap -> {
                            ObligacionMercado obl = ap.getObligacion();
                            String mercNom = obl.getPuesto() != null ? obl.getPuesto().getMercado().getNombre() : "Mercado General";
                            UUID mercId = obl.getPuesto() != null ? obl.getPuesto().getMercado().getId() : null;

                            if (mercadoId != null && !mercadoId.equals(mercId)) {
                                return null;
                            }

                            return RecaudacionReporteItem.builder()
                                    .id(p.getId())
                                    .codigoPago(p.getCodigoPago())
                                    .fechaPago(p.getFechaPago())
                                    .mercadoNombre(mercNom)
                                    .puestoCodigo(obl.getPuesto() != null ? obl.getPuesto().getCodigo() : "-")
                                    .comercianteNombre(p.getComerciante() != null ? p.getComerciante().getNombreOTitular() : "-")
                                    .conceptoNombre(obl.getConcepto() != null ? obl.getConcepto().getNombre() : "Tarifa de Uso")
                                    .montoRecibido(p.getMontoRecibido())
                                    .montoAplicado(ap.getMontoAplicado())
                                    .medioPago(p.getMedioPago() != null ? p.getMedioPago().name() : "EFECTIVO")
                                    .referenciaOperacion(p.getReferenciaOperacion() != null ? p.getReferenciaOperacion() : "-")
                                    .estado(p.getEstado().name())
                                    .sesionCajaCodigo(p.getSesionCaja() != null ? p.getSesionCaja().getCodigoSesion() : "-")
                                    .usuarioCajero(p.getUsuarioRegistro())
                                    .build();
                        }).filter(Objects::nonNull);
                    } else {
                        if (mercadoId != null) return null;
                        return java.util.stream.Stream.of(RecaudacionReporteItem.builder()
                                .id(p.getId())
                                .codigoPago(p.getCodigoPago())
                                .fechaPago(p.getFechaPago())
                                .mercadoNombre("General")
                                .puestoCodigo("-")
                                .comercianteNombre(p.getComerciante() != null ? p.getComerciante().getNombreOTitular() : "-")
                                .conceptoNombre("Recaudación General")
                                .montoRecibido(p.getMontoRecibido())
                                .montoAplicado(p.getMontoAplicado())
                                .medioPago(p.getMedioPago() != null ? p.getMedioPago().name() : "EFECTIVO")
                                .referenciaOperacion(p.getReferenciaOperacion() != null ? p.getReferenciaOperacion() : "-")
                                .estado(p.getEstado().name())
                                .sesionCajaCodigo(p.getSesionCaja() != null ? p.getSesionCaja().getCodigoSesion() : "-")
                                .usuarioCajero(p.getUsuarioRegistro())
                                .build());
                    }
                })
                .sorted(Comparator.comparing(RecaudacionReporteItem::getFechaPago).reversed())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DeudaReporteItem> listarDeudas(ReporteMercadoFilter filtro, Pageable pageable) {
        List<DeudaReporteItem> todos = listarTodasDeudas(filtro);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), todos.size());
        List<DeudaReporteItem> sublist = start <= end ? todos.subList(start, end) : Collections.emptyList();
        return new PageImpl<>(sublist, pageable, todos.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeudaReporteItem> listarTodasDeudas(ReporteMercadoFilter filtro) {
        UUID mercadoId = filtro != null ? filtro.getMercadoId() : null;
        String termino = filtro != null && filtro.getTermino() != null ? filtro.getTermino().trim().toLowerCase() : null;
        LocalDate hoy = LocalDate.now();

        List<ObligacionMercado> obligaciones = obligacionRepository.findAll();

        return obligaciones.stream()
                .filter(o -> {
                    // Criterio 31 y 32: Solo obligaciones con saldo pendiente exigible
                    if (o.getEstado() != EstadoObligacion.PENDIENTE &&
                        o.getEstado() != EstadoObligacion.VENCIDA &&
                        o.getEstado() != EstadoObligacion.PARCIALMENTE_PAGADA) {
                        return false;
                    }
                    if (o.getSaldoPendiente() == null || o.getSaldoPendiente().compareTo(BigDecimal.ZERO) <= 0) {
                        return false;
                    }
                    if (mercadoId != null && (o.getPuesto() == null || !o.getPuesto().getMercado().getId().equals(mercadoId))) {
                        return false;
                    }
                    if (termino != null) {
                        String cod = o.getCodigoObligacion().toLowerCase();
                        String com = o.getComerciante() != null ? o.getComerciante().getNombreOTitular().toLowerCase() : "";
                        String doc = o.getComerciante() != null ? o.getComerciante().getNumeroDocumentoTitular() : "";
                        if (!cod.contains(termino) && !com.contains(termino) && !doc.contains(termino)) {
                            return false;
                        }
                    }
                    return true;
                })
                .sorted(Comparator.comparing(ObligacionMercado::getFechaVencimiento, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(o -> {
                    long diasMora = 0;
                    String rango = "NO_VENCIDA";
                    if (o.getFechaVencimiento() != null && o.getFechaVencimiento().isBefore(hoy)) {
                        diasMora = ChronoUnit.DAYS.between(o.getFechaVencimiento(), hoy);
                        if (diasMora <= 30) {
                            rango = "DE_1_A_30_DIAS";
                        } else if (diasMora <= 60) {
                            rango = "DE_31_A_60_DIAS";
                        } else {
                            rango = "MAS_DE_60_DIAS";
                        }
                    }

                    return DeudaReporteItem.builder()
                            .obligacionId(o.getId())
                            .codigoObligacion(o.getCodigoObligacion())
                            .comercianteNombre(o.getComerciante() != null ? o.getComerciante().getNombreOTitular() : "-")
                            .documentoComerciante(o.getComerciante() != null ? o.getComerciante().getNumeroDocumentoTitular() : "-")
                            .mercadoNombre(o.getPuesto() != null ? o.getPuesto().getMercado().getNombre() : "-")
                            .puestoCodigo(o.getPuesto() != null ? o.getPuesto().getCodigo() : "-")
                            .conceptoNombre(o.getConcepto() != null ? o.getConcepto().getNombre() : "Tarifa de Uso")
                            .fechaEmision(o.getFechaEmision())
                            .fechaVencimiento(o.getFechaVencimiento())
                            .montoOriginal(o.getMontoOriginal())
                            .saldoPendiente(o.getSaldoPendiente())
                            .estado(o.getEstado().name())
                            .diasMora(diasMora)
                            .rangoAntiguedad(rango)
                            .build();
                }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerResumenDeudas(ReporteMercadoFilter filtro) {
        List<DeudaReporteItem> deudas = listarTodasDeudas(filtro);

        BigDecimal totalDeuda = deudas.stream()
                .map(DeudaReporteItem::getSaldoPendiente)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal noVencida = deudas.stream()
                .filter(d -> "NO_VENCIDA".equals(d.getRangoAntiguedad()))
                .map(DeudaReporteItem::getSaldoPendiente)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal rango30 = deudas.stream()
                .filter(d -> "DE_1_A_30_DIAS".equals(d.getRangoAntiguedad()))
                .map(DeudaReporteItem::getSaldoPendiente)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal rango60 = deudas.stream()
                .filter(d -> "DE_31_A_60_DIAS".equals(d.getRangoAntiguedad()))
                .map(DeudaReporteItem::getSaldoPendiente)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal mas60 = deudas.stream()
                .filter(d -> "MAS_DE_60_DIAS".equals(d.getRangoAntiguedad()))
                .map(DeudaReporteItem::getSaldoPendiente)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> resumen = new HashMap<>();
        resumen.put("totalDeuda", totalDeuda);
        resumen.put("cantidadObligaciones", deudas.size());
        resumen.put("noVencida", noVencida);
        resumen.put("rango1a30", rango30);
        resumen.put("monto1a30", rango30);
        resumen.put("rango31a60", rango60);
        resumen.put("monto31a60", rango60);
        resumen.put("masDe60", mas60);
        resumen.put("montoMas60", mas60);

        List<Map<String, Object>> antiguedadList = new ArrayList<>();
        antiguedadList.add(crearItemRango("No vencidas", noVencida, "#10b981"));
        antiguedadList.add(crearItemRango("Vencidas 1-30 días", rango30, "#f59e0b"));
        antiguedadList.add(crearItemRango("Vencidas 31-60 días", rango60, "#f97316"));
        antiguedadList.add(crearItemRango("Vencidas > 60 días", mas60, "#ef4444"));
        resumen.put("antiguedadGrafico", antiguedadList);

        return resumen;
    }

    private Map<String, Object> crearItemRango(String label, BigDecimal monto, String color) {
        Map<String, Object> map = new HashMap<>();
        map.put("rango", label);
        map.put("monto", monto);
        map.put("color", color);
        return map;
    }
}
