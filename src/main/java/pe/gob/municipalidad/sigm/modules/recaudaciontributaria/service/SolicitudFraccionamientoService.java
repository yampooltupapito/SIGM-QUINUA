package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.document.repository.DocumentoRepository;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.fraccionamiento.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio de negocio para registro, evaluación y control de solicitudes de fraccionamiento tributario.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SolicitudFraccionamientoService {

    private final SolicitudFraccionamientoTributarioRepository solicitudRepository;
    private final DetalleSolicitudFraccionamientoRepository detalleRepository;
    private final ProgramaFraccionamientoTributarioRepository programaRepository;
    private final ContribuyenteTributarioRepository contribuyenteRepository;
    private final ObligacionTributariaRepository obligacionRepository;
    private final OficinaRepository oficinaRepository;
    private final DocumentoRepository documentoRepository;
    private final CronogramaFraccionamientoTributarioRepository cronogramaRepository;
    private final CuotaFraccionamientoTributarioRepository cuotaRepository;
    private final ConvenioFraccionamientoTributarioRepository convenioRepository;
    private final ElegibilidadFraccionamientoService elegibilidadService;
    private final SimulacionFraccionamientoService simulacionService;

    @Transactional
    public SolicitudFraccionamientoResponse crearSolicitud(SolicitudFraccionamientoRequest request, Usuario usuario) {
        if (request == null) {
            throw new BusinessException("Datos de la solicitud inválidos");
        }

        ContribuyenteTributario contribuyente = contribuyenteRepository.findById(request.getContribuyenteId())
                .orElseThrow(() -> new BusinessException("Contribuyente no encontrado"));

        ProgramaFraccionamientoTributario programa = programaRepository.findById(request.getProgramaId())
                .orElseThrow(() -> new BusinessException("Programa de fraccionamiento no encontrado"));

        if (programa.getEstado() != EstadoProgramaFraccionamiento.ACTIVO) {
            throw new BusinessException("El programa de fraccionamiento no se encuentra activo");
        }

        if (request.getObligacionIds() == null || request.getObligacionIds().isEmpty()) {
            throw new BusinessException("Debe seleccionar al menos una obligación a fraccionar");
        }

        // Validar elegibilidad de cada obligación
        BigDecimal montoTotalDeuda = BigDecimal.ZERO;
        List<ObligacionTributaria> obligacionesValidadas = new ArrayList<>();
        LocalDate fechaCorte = LocalDate.now();

        for (UUID oblId : request.getObligacionIds()) {
            ElegibilidadFraccionamientoService.ResultadoElegibilidad res = elegibilidadService.evaluarObligacion(oblId);
            if (!res.elegible()) {
                throw new BusinessException(res.motivo());
            }

            ObligacionTributaria obl = obligacionRepository.findById(oblId).orElseThrow();
            if (!obl.getContribuyente().getId().equals(contribuyente.getId())) {
                throw new BusinessException("La obligación " + obl.getCodigoObligacion() + " no pertenece al contribuyente solicitante");
            }

            montoTotalDeuda = montoTotalDeuda.add(obl.getSaldo());
            obligacionesValidadas.add(obl);
        }

        // Generar código correlativo de solicitud: SOL-FRAC-YYYY-XXXXX
        long conteoAnual = solicitudRepository.count() + 1;
        String codigoSolicitud = String.format("SOL-FRAC-%d-%05d", fechaCorte.getYear(), conteoAnual);

        Oficina oficina = null;
        if (request.getOficinaId() != null) {
            oficina = oficinaRepository.findById(request.getOficinaId()).orElse(null);
        }

        Documento docSustento = null;
        if (request.getDocumentoSustentoId() != null) {
            docSustento = documentoRepository.findByIdentificadorPublico(request.getDocumentoSustentoId()).orElse(null);
        }

        SolicitudFraccionamientoTributario solicitud = SolicitudFraccionamientoTributario.builder()
                .codigoSolicitud(codigoSolicitud)
                .contribuyente(contribuyente)
                .programa(programa)
                .fechaSolicitud(fechaCorte)
                .estado(EstadoSolicitudFraccionamiento.PRESENTADA)
                .motivo(request.getMotivo())
                .numeroCuotasPropuestas(request.getNumeroCuotasPropuestas())
                .montoTotalDeuda(montoTotalDeuda)
                .montoCuotaInicial(request.getMontoCuotaInicialPropuesta() != null ? request.getMontoCuotaInicialPropuesta() : BigDecimal.ZERO)
                .oficina(oficina)
                .responsable(usuario)
                .documentoSustento(docSustento)
                .build();

        solicitud = solicitudRepository.save(solicitud);

        // Guardar detalles
        for (ObligacionTributaria obl : obligacionesValidadas) {
            DetalleSolicitudFraccionamiento det = DetalleSolicitudFraccionamiento.builder()
                    .solicitud(solicitud)
                    .obligacion(obl)
                    .saldoEvaluado(obl.getSaldo())
                    .fechaCorte(fechaCorte)
                    .estadoValidacion(EstadoValidacionDetalleFraccionamiento.CONFORME)
                    .build();
            detalleRepository.save(det);
            solicitud.getDetalles().add(det);
        }

        // Generar simulación y primer cronograma simulado
        SimulacionFraccionamientoResponse sim = simulacionService.simular(SimulacionFraccionamientoRequest.builder()
                .programaId(programa.getId())
                .importeBase(montoTotalDeuda)
                .importeCuotaInicial(solicitud.getMontoCuotaInicial())
                .numeroCuotas(solicitud.getNumeroCuotasPropuestas())
                .fechaInicio(fechaCorte)
                .build());

        CronogramaFraccionamientoTributario cronograma = CronogramaFraccionamientoTributario.builder()
                .solicitud(solicitud)
                .versionCronograma(1)
                .importeBase(sim.getImporteBase())
                .importeCuotaInicial(sim.getCuotaInicial())
                .importeFinanciado(sim.getImporteFinanciado())
                .importeAccesorios(sim.getTotalIntereses())
                .importeTotal(sim.getImporteTotalFinanciado())
                .numeroCuotas(sim.getNumeroCuotas())
                .tasaInteresAplicada(sim.getTasaInteresMensual())
                .fechaGeneracion(OffsetDateTime.now())
                .estado(EstadoCronogramaFraccionamiento.SIMULADO)
                .build();

        cronograma = cronogramaRepository.save(cronograma);

        for (CuotaSimuladaDto cuotaSim : sim.getCuotas()) {
            CuotaFraccionamientoTributario cuota = CuotaFraccionamientoTributario.builder()
                    .cronograma(cronograma)
                    .numeroCuota(cuotaSim.getNumeroCuota())
                    .fechaVencimiento(cuotaSim.getFechaVencimiento())
                    .importeCapital(cuotaSim.getCapital())
                    .importeInteres(cuotaSim.getInteres())
                    .importeTotal(cuotaSim.getTotalCuota())
                    .importePagado(BigDecimal.ZERO)
                    .saldo(cuotaSim.getTotalCuota())
                    .estado(EstadoCuotaFraccionamiento.PENDIENTE)
                    .build();
            cuotaRepository.save(cuota);
        }

        log.info("Solicitud de fraccionamiento {} registrada con éxito para contribuyente {}", codigoSolicitud, contribuyente.getNumeroDocumento());

        return mapearResponse(solicitud);
    }

    @Transactional(readOnly = true)
    public SolicitudFraccionamientoResponse obtenerPorId(UUID id) {
        SolicitudFraccionamientoTributario sol = solicitudRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Solicitud de fraccionamiento no encontrada: " + id));
        return mapearResponse(sol);
    }

    @Transactional(readOnly = true)
    public Page<SolicitudFraccionamientoResponse> listarSolicitudes(Specification<SolicitudFraccionamientoTributario> spec, Pageable pageable) {
        return solicitudRepository.findAll(spec, pageable).map(this::mapearResponse);
    }

    @Transactional
    public SolicitudFraccionamientoResponse evaluarSolicitud(UUID id, EvaluarSolicitudRequest request, Usuario evaluador) {
        SolicitudFraccionamientoTributario sol = solicitudRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Solicitud no encontrada"));

        if (sol.getEstado() == EstadoSolicitudFraccionamiento.APROBADA || sol.getEstado() == EstadoSolicitudFraccionamiento.RECHAZADA) {
            throw new BusinessException("La solicitud ya fue dictaminada con anterioridad");
        }

        sol.setEvaluador(evaluador);
        sol.setFechaEvaluacion(OffsetDateTime.now());
        sol.setObservacionEvaluacion(request.getObservaciones());

        if (Boolean.TRUE.equals(request.getConforme())) {
            sol.setEstado(EstadoSolicitudFraccionamiento.EN_EVALUACION);
            log.info("Solicitud {} evaluada como CONFORME por {}", sol.getCodigoSolicitud(), evaluador.getUsername());
        } else {
            sol.setEstado(EstadoSolicitudFraccionamiento.OBSERVADA);
            log.warn("Solicitud {} evaluada con OBSERVACIONES por {}", sol.getCodigoSolicitud(), evaluador.getUsername());
        }

        sol = solicitudRepository.save(sol);
        return mapearResponse(sol);
    }

    @Transactional
    public SolicitudFraccionamientoResponse observarSolicitud(UUID id, ObservarSolicitudRequest request, Usuario usuario) {
        SolicitudFraccionamientoTributario sol = solicitudRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Solicitud no encontrada"));

        sol.setEstado(EstadoSolicitudFraccionamiento.OBSERVADA);
        sol.setObservacionEvaluacion(request.getObservacion());
        sol.setFechaEvaluacion(OffsetDateTime.now());
        sol.setEvaluador(usuario);

        sol = solicitudRepository.save(sol);
        log.info("Solicitud {} observada por {}", sol.getCodigoSolicitud(), usuario.getUsername());
        return mapearResponse(sol);
    }

    @Transactional
    public SolicitudFraccionamientoResponse subsanarSolicitud(UUID id, SubsanarSolicitudRequest request, Usuario usuario) {
        SolicitudFraccionamientoTributario sol = solicitudRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Solicitud no encontrada"));

        if (sol.getEstado() != EstadoSolicitudFraccionamiento.OBSERVADA) {
            throw new BusinessException("Solo pueden subsanarse solicitudes en estado OBSERVADA");
        }

        sol.setEstado(EstadoSolicitudFraccionamiento.SUBSANADA);
        sol.setMotivo(sol.getMotivo() + "\n\n[Subsanación " + LocalDate.now() + "]: " + request.getDetalleSubsanacion());

        if (request.getDocumentoSustentoId() != null) {
            Documento doc = documentoRepository.findByIdentificadorPublico(request.getDocumentoSustentoId()).orElse(null);
            sol.setDocumentoSustento(doc);
        }

        sol = solicitudRepository.save(sol);
        log.info("Solicitud {} subsanada por {}", sol.getCodigoSolicitud(), usuario.getUsername());
        return mapearResponse(sol);
    }

    @Transactional
    public SolicitudFraccionamientoResponse rechazarSolicitud(UUID id, String motivo, Usuario usuario) {
        SolicitudFraccionamientoTributario sol = solicitudRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Solicitud no encontrada"));

        sol.setEstado(EstadoSolicitudFraccionamiento.RECHAZADA);
        sol.setObservacionEvaluacion(motivo);
        sol.setFechaEvaluacion(OffsetDateTime.now());
        sol.setEvaluador(usuario);

        sol = solicitudRepository.save(sol);
        log.info("Solicitud {} desestimada formalmente por {}", sol.getCodigoSolicitud(), usuario.getUsername());
        return mapearResponse(sol);
    }

    @Transactional(readOnly = true)
    public DashboardFraccionamientoResponse obtenerDashboardMetrics() {
        long totalSol = solicitudRepository.count();
        long enEval = solicitudRepository.countEnEvaluacionTotal();
        long obs = solicitudRepository.countByEstado(EstadoSolicitudFraccionamiento.OBSERVADA);
        long aprob = solicitudRepository.countByEstado(EstadoSolicitudFraccionamiento.APROBADA);

        long vigentes = convenioRepository.countVigentes();
        long cumplidos = convenioRepository.countCumplidos();
        long cuotasPend = cuotaRepository.countCuotasPendientes();
        long cuotasVenc = cuotaRepository.countCuotasVencidas(LocalDate.now());

        return DashboardFraccionamientoResponse.builder()
                .totalSolicitudes(totalSol)
                .solicitudesEnEvaluacion(enEval)
                .solicitudesObservadas(obs)
                .solicitudesAprobadas(aprob)
                .conveniosVigentes(vigentes)
                .conveniosCumplidos(cumplidos)
                .cuotasPendientes(cuotasPend)
                .cuotasVencidas(cuotasVenc)
                .build();
    }

    public SolicitudFraccionamientoResponse mapearResponse(SolicitudFraccionamientoTributario sol) {
        if (sol == null) return null;

        List<DetalleObligacionFraccionadaDto> detallesDto = detalleRepository.findBySolicitudId(sol.getId()).stream()
                .map(d -> DetalleObligacionFraccionadaDto.builder()
                        .id(d.getId())
                        .obligacionId(d.getObligacion().getId())
                        .codigoObligacion(d.getObligacion().getCodigoObligacion())
                        .conceptoNombre(d.getObligacion().getConcepto() != null ? d.getObligacion().getConcepto().getNombre() : "Tributo Municipal")
                        .periodoAnio(d.getObligacion().getPeriodo() != null ? String.valueOf(d.getObligacion().getPeriodo().getAnio()) : "-")
                        .cuotaNumero(d.getObligacion().getCuotaNumero())
                        .fechaVencimiento(d.getObligacion().getFechaVencimiento())
                        .saldoEvaluado(d.getSaldoEvaluado())
                        .fechaCorte(d.getFechaCorte())
                        .estadoValidacion(d.getEstadoValidacion())
                        .estadoDescripcion(d.getEstadoValidacion() != null ? d.getEstadoValidacion().getDescripcion() : "")
                        .estadoBadgeClass(d.getEstadoValidacion() != null ? d.getEstadoValidacion().getBadgeClass() : "")
                        .observaciones(d.getObservaciones())
                        .build())
                .collect(Collectors.toList());

        CronogramaDto cronogramaDto = null;
        List<CronogramaFraccionamientoTributario> cronogramas = cronogramaRepository.findBySolicitudIdOrderByVersionCronogramaDesc(sol.getId());
        if (!cronogramas.isEmpty()) {
            CronogramaFraccionamientoTributario cron = cronogramas.get(0);
            List<CuotaDto> cuotasDto = cuotaRepository.findByCronogramaIdOrderByNumeroCuotaAsc(cron.getId()).stream()
                    .map(c -> CuotaDto.builder()
                            .id(c.getId())
                            .numeroCuota(c.getNumeroCuota())
                            .fechaVencimiento(c.getFechaVencimiento())
                            .importeCapital(c.getImporteCapital())
                            .importeInteres(c.getImporteInteres())
                            .importeTotal(c.getImporteTotal())
                            .importePagado(c.getImportePagado())
                            .saldo(c.getSaldo())
                            .estado(c.getEstado())
                            .estadoDescripcion(c.getEstado() != null ? c.getEstado().getDescripcion() : "")
                            .estadoBadgeClass(c.getEstado() != null ? c.getEstado().getBadgeClass() : "")
                            .fechaPago(c.getFechaPago())
                            .vencida(c.getFechaVencimiento().isBefore(LocalDate.now()) && c.getSaldo().compareTo(BigDecimal.ZERO) > 0)
                            .build())
                    .collect(Collectors.toList());

            cronogramaDto = CronogramaDto.builder()
                    .id(cron.getId())
                    .versionCronograma(cron.getVersionCronograma())
                    .importeBase(cron.getImporteBase())
                    .importeCuotaInicial(cron.getImporteCuotaInicial())
                    .importeFinanciado(cron.getImporteFinanciado())
                    .importeAccesorios(cron.getImporteAccesorios())
                    .importeTotal(cron.getImporteTotal())
                    .numeroCuotas(cron.getNumeroCuotas())
                    .tasaInteresAplicada(cron.getTasaInteresAplicada())
                    .fechaGeneracion(cron.getFechaGeneracion())
                    .estado(cron.getEstado())
                    .estadoDescripcion(cron.getEstado() != null ? cron.getEstado().getDescripcion() : "")
                    .estadoBadgeClass(cron.getEstado() != null ? cron.getEstado().getBadgeClass() : "")
                    .cuotas(cuotasDto)
                    .build();
        }

        UUID convenioId = null;
        String codigoConvenio = null;
        ConvenioFraccionamientoTributario conv = convenioRepository.findBySolicitudId(sol.getId()).orElse(null);
        if (conv != null) {
            convenioId = conv.getId();
            codigoConvenio = conv.getCodigoConvenio();
        }

        return SolicitudFraccionamientoResponse.builder()
                .id(sol.getId())
                .codigoSolicitud(sol.getCodigoSolicitud())
                .contribuyenteId(sol.getContribuyente().getId())
                .contribuyenteNombre(sol.getContribuyente().getNombreRazonSocial())
                .contribuyenteDocumento(sol.getContribuyente().getNumeroDocumento())
                .contribuyenteCodigo(sol.getContribuyente().getCodigoContribuyente())
                .programaId(sol.getPrograma().getId())
                .programaNombre(sol.getPrograma().getNombre())
                .fechaSolicitud(sol.getFechaSolicitud())
                .estado(sol.getEstado())
                .estadoDescripcion(sol.getEstado() != null ? sol.getEstado().getDescripcion() : "")
                .estadoBadgeClass(sol.getEstado() != null ? sol.getEstado().getBadgeClass() : "")
                .motivo(sol.getMotivo())
                .numeroCuotasPropuestas(sol.getNumeroCuotasPropuestas())
                .montoTotalDeuda(sol.getMontoTotalDeuda())
                .montoCuotaInicial(sol.getMontoCuotaInicial())
                .oficinaNombre(sol.getOficina() != null ? sol.getOficina().getNombre() : "Rentas y Administración Tributaria")
                .responsableUsername(sol.getResponsable() != null ? sol.getResponsable().getUsername() : "-")
                .evaluadorUsername(sol.getEvaluador() != null ? sol.getEvaluador().getUsername() : null)
                .fechaEvaluacion(sol.getFechaEvaluacion())
                .observacionEvaluacion(sol.getObservacionEvaluacion())
                .documentoSustentoId(sol.getDocumentoSustento() != null ? sol.getDocumentoSustento().getIdentificadorPublico() : null)
                .detalles(detallesDto)
                .cronogramaVigente(cronogramaDto)
                .convenioId(convenioId)
                .codigoConvenio(codigoConvenio)
                .build();
    }
}
