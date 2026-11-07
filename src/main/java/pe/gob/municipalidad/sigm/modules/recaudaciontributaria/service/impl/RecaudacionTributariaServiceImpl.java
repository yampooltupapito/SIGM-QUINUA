package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoPeriodoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.RecaudacionTributariaService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecaudacionTributariaServiceImpl implements RecaudacionTributariaService {

    private final ContribuyenteTributarioRepository contribuyenteRepository;
    private final PredioTributarioRepository predioRepository;
    private final ConceptoTributarioRepository conceptoRepository;
    private final PeriodoTributarioRepository periodoRepository;
    private final NormaTributariaConfiguradaRepository normaRepository;
    private final ObligacionTributariaRepository obligacionRepository;
    private final PagoTributarioRepository pagoRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    @Transactional(readOnly = true)
    public DashboardTributarioResponse obtenerDashboard() {
        log.info("Consultando panel inicial de Recaudación Tributaria Municipal");

        long totalContribuyentes = contribuyenteRepository.count();
        long totalPredios = predioRepository.count();
        long totalConceptos = conceptoRepository.count();
        long totalPeriodos = periodoRepository.count();
        long totalObligaciones = obligacionRepository.count();
        long totalPagos = pagoRepository.count();

        OffsetDateTime inicioAnio = OffsetDateTime.now().withDayOfYear(1).withHour(0).withMinute(0).withSecond(0);
        OffsetDateTime ahora = OffsetDateTime.now();
        BigDecimal recaudacionTotal = pagoRepository.calcularRecaudacionPeriodo(inicioAnio, ahora);

        Optional<PeriodoTributario> periodoActivoOpt = periodoRepository.findByEstado(EstadoPeriodoTributario.APERTURADO)
                .stream().findFirst();

        String periodoActivo = periodoActivoOpt
                .map(p -> p.getAnio() + " — " + p.getEstado().getDescripcion())
                .orElse("Sin período aperturado");

        BigDecimal uitVigente = periodoActivoOpt
                .map(PeriodoTributario::getUitVigente)
                .orElse(BigDecimal.ZERO);

        // Obtener últimos contribuyentes registrados (máx 5)
        Page<ContribuyenteTributario> recientesPage = contribuyenteRepository.findAll(
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        List<ContribuyenteSummaryDto> contribuyentesRecientes = recientesPage.getContent().stream()
                .map(this::mapToContribuyenteSummary)
                .collect(Collectors.toList());

        // Obtener periodos registrados
        List<PeriodoSummaryDto> periodos = periodoRepository.findAll(Sort.by(Sort.Direction.DESC, "anio")).stream()
                .map(this::mapToPeriodoSummary)
                .collect(Collectors.toList());

        // Obtener conceptos tributarios vigentes
        List<ConceptoSummaryDto> conceptos = conceptoRepository.findAll(Sort.by(Sort.Direction.ASC, "codigo")).stream()
                .map(this::mapToConceptoSummary)
                .collect(Collectors.toList());

        return DashboardTributarioResponse.builder()
                .totalContribuyentes(totalContribuyentes)
                .totalPredios(totalPredios)
                .totalConceptos(totalConceptos)
                .totalPeriodos(totalPeriodos)
                .totalObligaciones(totalObligaciones)
                .totalPagos(totalPagos)
                .recaudacionTotal(recaudacionTotal != null ? recaudacionTotal : BigDecimal.ZERO)
                .periodoActivo(periodoActivo)
                .uitVigente(uitVigente)
                .contribuyentesRecientes(contribuyentesRecientes)
                .periodosRegistrados(periodos)
                .conceptosVigentes(conceptos)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContribuyenteSummaryDto> listarContribuyentes(String q, Pageable pageable) {
        Page<ContribuyenteTributario> page = (q != null && !q.trim().isEmpty())
                ? contribuyenteRepository.buscarContribuyentes(q.trim(), pageable)
                : contribuyenteRepository.findAll(pageable);

        return page.map(this::mapToContribuyenteSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PredioSummaryDto> listarPredios(String q, Pageable pageable) {
        Page<PredioTributario> page = (q != null && !q.trim().isEmpty())
                ? predioRepository.buscarPredios(q.trim(), pageable)
                : predioRepository.findAll(pageable);

        return page.map(this::mapToPredioSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConceptoSummaryDto> listarConceptos(Pageable pageable) {
        return conceptoRepository.findAllByOrderByCodigoAsc(pageable)
                .map(this::mapToConceptoSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PeriodoSummaryDto> listarPeriodos(Pageable pageable) {
        return periodoRepository.findAllByOrderByAnioDesc(pageable)
                .map(this::mapToPeriodoSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NormaSummaryDto> listarNormas(Pageable pageable) {
        return normaRepository.findAllByOrderByFechaInicioVigenciaDesc(pageable)
                .map(this::mapToNormaSummary);
    }

    private ContribuyenteSummaryDto mapToContribuyenteSummary(ContribuyenteTributario c) {
        return ContribuyenteSummaryDto.builder()
                .id(c.getId())
                .codigoContribuyente(c.getCodigoContribuyente())
                .tipoContribuyente(c.getTipoContribuyente() != null ? c.getTipoContribuyente().getDescripcion() : "-")
                .tipoDocumento(c.getTipoDocumento())
                .numeroDocumentoEnmascarado(c.getNumeroDocumentoEnmascarado())
                .nombreRazonSocial(c.getNombreRazonSocial())
                .domicilioFiscal(c.getDomicilioFiscal() != null ? c.getDomicilioFiscal() : "No registrado")
                .estado(c.getEstado() != null ? c.getEstado().getDescripcion() : "-")
                .badgeClass(c.getEstado() != null ? c.getEstado().getBadgeClass() : "bg-slate-500/10 text-slate-400")
                .fechaRegistro(c.getFechaRegistro() != null ? c.getFechaRegistro().format(DATE_FORMATTER) : "-")
                .build();
    }

    private PredioSummaryDto mapToPredioSummary(PredioTributario p) {
        return PredioSummaryDto.builder()
                .id(p.getId())
                .codigoPredio(p.getCodigoPredio())
                .codigoCatastral(p.getCodigoCatastral() != null ? p.getCodigoCatastral() : "Sin Catastro")
                .tipoPredio(p.getTipoPredio() != null ? p.getTipoPredio().getDescripcion() : "-")
                .direccion(p.getDireccion())
                .sectorNombre(p.getSectorNombre() != null ? p.getSectorNombre() : (p.getSector() != null ? p.getSector().getNombre() : "Distrito Quinua"))
                .areaTerreno(p.getAreaTerreno())
                .areaConstruida(p.getAreaConstruida())
                .unidadMedidaArea(p.getUnidadMedidaArea())
                .estado(p.getEstado() != null ? p.getEstado().getDescripcion() : "-")
                .badgeClass(p.getEstado() != null ? p.getEstado().getBadgeClass() : "bg-slate-500/10 text-slate-400")
                .fechaRegistro(p.getFechaRegistro() != null ? p.getFechaRegistro().format(DATE_FORMATTER) : "-")
                .build();
    }

    private ConceptoSummaryDto mapToConceptoSummary(ConceptoTributario c) {
        return ConceptoSummaryDto.builder()
                .id(c.getId())
                .codigo(c.getCodigo())
                .nombre(c.getNombre())
                .descripcion(c.getDescripcion())
                .tipoConcepto(c.getTipoConcepto() != null ? c.getTipoConcepto().getDescripcion() : "-")
                .periodicidad(c.getPeriodicidad() != null ? c.getPeriodicidad().getDescripcion() : "-")
                .aplicaAPredio(Boolean.TRUE.equals(c.getAplicaAPredio()))
                .cuentaPresupuestal(c.getCuentaPresupuestal() != null ? c.getCuentaPresupuestal() : "-")
                .estado(c.getEstado() != null ? c.getEstado().getDescripcion() : "-")
                .badgeClass(c.getEstado() != null ? c.getEstado().getBadgeClass() : "bg-slate-500/10 text-slate-400")
                .fechaInicioVigencia(c.getFechaInicioVigencia() != null ? c.getFechaInicioVigencia().format(DATE_FORMATTER) : "-")
                .build();
    }

    private PeriodoSummaryDto mapToPeriodoSummary(PeriodoTributario p) {
        return PeriodoSummaryDto.builder()
                .id(p.getId())
                .anio(p.getAnio())
                .fechaInicio(p.getFechaInicio() != null ? p.getFechaInicio().format(DATE_FORMATTER) : "-")
                .fechaFin(p.getFechaFin() != null ? p.getFechaFin().format(DATE_FORMATTER) : "-")
                .uitVigente(p.getUitVigente())
                .estado(p.getEstado() != null ? p.getEstado().getDescripcion() : "-")
                .badgeClass(p.getEstado() != null ? p.getEstado().getBadgeClass() : "bg-slate-500/10 text-slate-400")
                .resolucionAprobacion(p.getResolucionAprobacion() != null ? p.getResolucionAprobacion() : "Pendiente")
                .observaciones(p.getObservaciones())
                .build();
    }

    private NormaSummaryDto mapToNormaSummary(NormaTributariaConfigurada n) {
        return NormaSummaryDto.builder()
                .id(n.getId())
                .tipoNorma(n.getTipoNorma() != null ? n.getTipoNorma().getDescripcion() : "-")
                .numeroNorma(n.getNumeroNorma())
                .entidadEmisora(n.getEntidadEmisora())
                .descripcion(n.getDescripcion())
                .fechaPublicacion(n.getFechaPublicacion() != null ? n.getFechaPublicacion().format(DATE_FORMATTER) : "-")
                .fechaInicioVigencia(n.getFechaInicioVigencia() != null ? n.getFechaInicioVigencia().format(DATE_FORMATTER) : "-")
                .estadoValidacion(n.getEstadoValidacion() != null ? n.getEstadoValidacion().getDescripcion() : "-")
                .badgeClass(n.getEstadoValidacion() != null ? n.getEstadoValidacion().getBadgeClass() : "bg-slate-500/10 text-slate-400")
                .validador(n.getUsuarioValidador() != null ? n.getUsuarioValidador().getUsername() : "Pendiente")
                .build();
    }
}
