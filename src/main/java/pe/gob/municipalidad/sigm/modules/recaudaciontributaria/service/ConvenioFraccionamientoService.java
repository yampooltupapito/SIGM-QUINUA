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
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.fraccionamiento.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio de gestión de convenios formales de fraccionamiento tributario municipal.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConvenioFraccionamientoService {

    private final ConvenioFraccionamientoTributarioRepository convenioRepository;
    private final SolicitudFraccionamientoTributarioRepository solicitudRepository;
    private final DetalleSolicitudFraccionamientoRepository detalleRepository;
    private final CronogramaFraccionamientoTributarioRepository cronogramaRepository;
    private final CuotaFraccionamientoTributarioRepository cuotaRepository;
    private final IncidenciaConvenioFraccionamientoRepository incidenciaRepository;
    private final ObligacionTributariaRepository obligacionRepository;
    private final SeguimientoDeudaTributariaRepository seguimientoDeudaRepository;
    private final DocumentoRepository documentoRepository;
    private final ParametroFraccionamientoTributarioRepository parametroRepository;

    @Transactional
    public ConvenioFraccionamientoResponse aprobarConvenio(UUID solicitudId, AprobarConvenioRequest request, Usuario aprobador) {
        if (request == null) {
            throw new BusinessException("Datos de aprobación inválidos");
        }

        SolicitudFraccionamientoTributario solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new BusinessException("Solicitud no encontrada: " + solicitudId));

        if (solicitud.getEstado() == EstadoSolicitudFraccionamiento.APROBADA) {
            throw new BusinessException("La solicitud ya fue aprobada y cuenta con un convenio formal");
        }

        if (solicitud.getEstado() == EstadoSolicitudFraccionamiento.RECHAZADA || solicitud.getEstado() == EstadoSolicitudFraccionamiento.ARCHIVADA) {
            throw new BusinessException("No puede aprobarse una solicitud en estado: " + solicitud.getEstado());
        }

        // 1. Revalidación técnica de obligaciones y saldos actuales
        List<DetalleSolicitudFraccionamiento> detalles = detalleRepository.findBySolicitudId(solicitud.getId());
        if (detalles.isEmpty()) {
            throw new BusinessException("La solicitud no contiene obligaciones asociadas");
        }

        for (DetalleSolicitudFraccionamiento det : detalles) {
            ObligacionTributaria obl = det.getObligacion();
            if (obl.getSaldo() == null || obl.getSaldo().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("La obligación " + obl.getCodigoObligacion() + " ya no presenta saldo pendiente");
            }
            if (obl.getEstado() == EstadoObligacionTributaria.ANULADA) {
                throw new BusinessException("La obligación " + obl.getCodigoObligacion() + " fue anulada");
            }
        }

        // 2. Localizar cronograma vigente de la solicitud
        List<CronogramaFraccionamientoTributario> cronogramas = cronogramaRepository.findBySolicitudIdOrderByVersionCronogramaDesc(solicitud.getId());
        if (cronogramas.isEmpty()) {
            throw new BusinessException("La solicitud no cuenta con cronograma calculado");
        }
        CronogramaFraccionamientoTributario cronograma = cronogramas.get(0);
        cronograma.setEstado(EstadoCronogramaFraccionamiento.VIGENTE);
        cronograma = cronogramaRepository.save(cronograma);

        // 3. Marcar solicitud como APROBADA
        solicitud.setEstado(EstadoSolicitudFraccionamiento.APROBADA);
        solicitud.setFechaEvaluacion(OffsetDateTime.now());
        solicitud.setEvaluador(aprobador);
        solicitudRepository.save(solicitud);

        // 4. Actualizar estado de las obligaciones a FRACCIONADA sin eliminar ni duplicar
        for (DetalleSolicitudFraccionamiento det : detalles) {
            ObligacionTributaria obl = det.getObligacion();
            obl.setEstado(EstadoObligacionTributaria.FRACCIONADA);
            obligacionRepository.save(obl);

            // Actualizar clasificación en seguimiento si existe
            seguimientoDeudaRepository.findByObligacionId(obl.getId()).ifPresent(seg -> {
                seg.setClasificacion(ClasificacionDeudaTributaria.CON_FRACCIONAMIENTO_VIGENTE);
                seg.setFechaUltimaRevision(OffsetDateTime.now());
                seguimientoDeudaRepository.save(seg);
            });
        }

        // 5. Generar código correlativo de convenio y código de verificación QR
        LocalDate fechaHoy = LocalDate.now();
        long correlativoConvenio = convenioRepository.count() + 1;
        String codigoConvenio = String.format("CONV-%d-%05d", fechaHoy.getYear(), correlativoConvenio);
        String codigoVerificacion = UUID.randomUUID().toString().replace("-", "").substring(0, 24).toUpperCase();

        ConvenioFraccionamientoTributario convenio = ConvenioFraccionamientoTributario.builder()
                .codigoConvenio(codigoConvenio)
                .solicitud(solicitud)
                .contribuyente(solicitud.getContribuyente())
                .programa(solicitud.getPrograma())
                .cronograma(cronograma)
                .fechaAprobacion(fechaHoy)
                .fechaInicioVigencia(request.getFechaInicioVigencia() != null ? request.getFechaInicioVigencia() : fechaHoy)
                .resolucionAprobacion(request.getResolucionAprobacion())
                .estado(EstadoConvenioFraccionamiento.VIGENTE)
                .responsableAprobacion(aprobador)
                .codigoVerificacion(codigoVerificacion)
                .observaciones(request.getObservaciones())
                .build();

        convenio = convenioRepository.save(convenio);

        // 6. Registrar bitácora de incidencia inicial
        IncidenciaConvenioFraccionamiento incInicial = IncidenciaConvenioFraccionamiento.builder()
                .convenio(convenio)
                .tipoIncidencia(TipoIncidenciaConvenio.REVISION_ADMINISTRATIVA)
                .descripcion("Convenio formal de fraccionamiento aprobado y puesto en vigencia mediante " + request.getResolucionAprobacion())
                .responsable(aprobador)
                .fechaRegistro(OffsetDateTime.now())
                .build();
        incidenciaRepository.save(incInicial);
        convenio.getIncidencias().add(incInicial);

        log.info("Convenio de fraccionamiento {} formalizado exitosamente para solicitud {}", codigoConvenio, solicitud.getCodigoSolicitud());
        return mapearConvenioResponse(convenio);
    }

    @Transactional(readOnly = true)
    public ConvenioFraccionamientoResponse obtenerPorId(UUID id) {
        ConvenioFraccionamientoTributario conv = convenioRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Convenio de fraccionamiento no encontrado: " + id));
        return mapearConvenioResponse(conv);
    }

    @Transactional(readOnly = true)
    public Page<ConvenioFraccionamientoResponse> listarConvenios(Specification<ConvenioFraccionamientoTributario> spec, Pageable pageable) {
        return convenioRepository.findAll(spec, pageable).map(this::mapearConvenioResponse);
    }

    @Transactional
    public ConvenioFraccionamientoResponse reprogramarConvenio(UUID convenioId, ReprogramarConvenioRequest request, Usuario usuario) {
        if (request == null) {
            throw new BusinessException("Datos de reprogramación inválidos");
        }

        ConvenioFraccionamientoTributario convenio = convenioRepository.findById(convenioId)
                .orElseThrow(() -> new BusinessException("Convenio no encontrado"));

        if (convenio.getEstado() != EstadoConvenioFraccionamiento.VIGENTE && convenio.getEstado() != EstadoConvenioFraccionamiento.EN_REVISION) {
            throw new BusinessException("Solo convenios vigentes o en revisión pueden ser reprogramados");
        }

        CronogramaFraccionamientoTributario cronogramaAnt = convenio.getCronograma();
        cronogramaAnt.setEstado(EstadoCronogramaFraccionamiento.MODIFICADO);
        cronogramaRepository.save(cronogramaAnt);

        // Calcular saldo insoluto financiado pendiente
        List<CuotaFraccionamientoTributario> cuotasAnt = cuotaRepository.findByCronogramaIdOrderByNumeroCuotaAsc(cronogramaAnt.getId());
        BigDecimal saldoCapitalPendiente = BigDecimal.ZERO;
        for (CuotaFraccionamientoTributario cuota : cuotasAnt) {
            if (cuota.getEstado() != EstadoCuotaFraccionamiento.PAGADA) {
                saldoCapitalPendiente = saldoCapitalPendiente.add(cuota.getSaldo());
            }
        }

        if (saldoCapitalPendiente.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El convenio no presenta saldo pendiente por reprogramar");
        }

        // Generar nuevo cronograma reprogramado
        int nuevasCuotas = request.getNuevasCuotas();
        BigDecimal capitalPorCuota = saldoCapitalPendiente.divide(BigDecimal.valueOf(nuevasCuotas), 2, RoundingMode.HALF_UP);
        BigDecimal tasaMensual = cronogramaAnt.getTasaInteresAplicada();
        BigDecimal factorTasa = tasaMensual.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);

        CronogramaFraccionamientoTributario nuevoCronograma = CronogramaFraccionamientoTributario.builder()
                .solicitud(convenio.getSolicitud())
                .versionCronograma(cronogramaAnt.getVersionCronograma() + 1)
                .importeBase(saldoCapitalPendiente)
                .importeCuotaInicial(BigDecimal.ZERO)
                .importeFinanciado(saldoCapitalPendiente)
                .importeAccesorios(BigDecimal.ZERO)
                .importeTotal(saldoCapitalPendiente)
                .numeroCuotas(nuevasCuotas)
                .tasaInteresAplicada(tasaMensual)
                .fechaGeneracion(OffsetDateTime.now())
                .estado(EstadoCronogramaFraccionamiento.VIGENTE)
                .build();

        nuevoCronograma = cronogramaRepository.save(nuevoCronograma);

        LocalDate fechaVenc = request.getNuevaFechaInicio();
        BigDecimal saldoRestante = saldoCapitalPendiente;
        BigDecimal sumaCapital = BigDecimal.ZERO;
        BigDecimal sumaIntereses = BigDecimal.ZERO;

        for (int i = 1; i <= nuevasCuotas; i++) {
            BigDecimal capitalCuota = (i == nuevasCuotas) ? saldoCapitalPendiente.subtract(sumaCapital) : capitalPorCuota;
            BigDecimal interesCuota = saldoRestante.multiply(factorTasa).setScale(2, RoundingMode.HALF_UP);
            BigDecimal totalCuota = capitalCuota.add(interesCuota);

            saldoRestante = saldoRestante.subtract(capitalCuota);
            if (saldoRestante.compareTo(BigDecimal.ZERO) < 0) saldoRestante = BigDecimal.ZERO;

            sumaCapital = sumaCapital.add(capitalCuota);
            sumaIntereses = sumaIntereses.add(interesCuota);

            CuotaFraccionamientoTributario c = CuotaFraccionamientoTributario.builder()
                    .cronograma(nuevoCronograma)
                    .numeroCuota(i)
                    .fechaVencimiento(fechaVenc)
                    .importeCapital(capitalCuota)
                    .importeInteres(interesCuota)
                    .importeTotal(totalCuota)
                    .importePagado(BigDecimal.ZERO)
                    .saldo(totalCuota)
                    .estado(EstadoCuotaFraccionamiento.PENDIENTE)
                    .build();
            cuotaRepository.save(c);

            fechaVenc = fechaVenc.plusDays(30);
        }

        nuevoCronograma.setImporteAccesorios(sumaIntereses);
        nuevoCronograma.setImporteTotal(saldoCapitalPendiente.add(sumaIntereses));
        cronogramaRepository.save(nuevoCronograma);

        convenio.setCronograma(nuevoCronograma);
        convenio.setEstado(EstadoConvenioFraccionamiento.VIGENTE);
        convenioRepository.save(convenio);

        Documento docSustento = null;
        if (request.getDocumentoSustentoId() != null) {
            docSustento = documentoRepository.findByIdentificadorPublico(request.getDocumentoSustentoId()).orElse(null);
        }

        // Registrar incidencia de reprogramación
        IncidenciaConvenioFraccionamiento inc = IncidenciaConvenioFraccionamiento.builder()
                .convenio(convenio)
                .tipoIncidencia(TipoIncidenciaConvenio.REPROGRAMACION_AUTORIZADA)
                .descripcion("Reprogramación autorizada mediante " + request.getResolucionSustento() + ". Motivo: " + request.getMotivo())
                .responsable(usuario)
                .documentoSustento(docSustento)
                .fechaRegistro(OffsetDateTime.now())
                .build();
        incidenciaRepository.save(inc);

        log.info("Convenio {} reprogramado exitosamente a versión {}", convenio.getCodigoConvenio(), nuevoCronograma.getVersionCronograma());
        return mapearConvenioResponse(convenio);
    }

    @Transactional
    public ConvenioFraccionamientoResponse registrarIncidencia(UUID convenioId, RegistrarIncidenciaConvenioRequest request, Usuario usuario) {
        ConvenioFraccionamientoTributario convenio = convenioRepository.findById(convenioId)
                .orElseThrow(() -> new BusinessException("Convenio no encontrado"));

        Documento doc = null;
        if (request.getDocumentoSustentoId() != null) {
            doc = documentoRepository.findByIdentificadorPublico(request.getDocumentoSustentoId()).orElse(null);
        }

        IncidenciaConvenioFraccionamiento inc = IncidenciaConvenioFraccionamiento.builder()
                .convenio(convenio)
                .tipoIncidencia(request.getTipoIncidencia())
                .descripcion(request.getDescripcion())
                .responsable(usuario)
                .documentoSustento(doc)
                .fechaRegistro(OffsetDateTime.now())
                .build();
        incidenciaRepository.save(inc);

        log.info("Incidencia {} registrada para convenio {}", request.getTipoIncidencia(), convenio.getCodigoConvenio());
        return mapearConvenioResponse(convenio);
    }

    @Transactional
    public ConvenioFraccionamientoResponse revisarIncumplimiento(UUID convenioId, Usuario usuario) {
        ConvenioFraccionamientoTributario convenio = convenioRepository.findById(convenioId)
                .orElseThrow(() -> new BusinessException("Convenio no encontrado"));

        LocalDate hoy = LocalDate.now();
        List<CuotaFraccionamientoTributario> cuotasVencidas = cuotaRepository.findCuotasImpagasVencidas(convenio.getCronograma().getId(), hoy);

        int maxCuotasImpagas = 2;
        if (convenio.getPrograma() != null) {
            maxCuotasImpagas = parametroRepository.findPrimerVigente(convenio.getPrograma().getId(), "CUOTAS_IMPAGAS_CAUSAL_PERDIDA", hoy)
                    .map(p -> p.getValorNumerico().intValue())
                    .orElse(2);
        }

        if (cuotasVencidas.size() >= maxCuotasImpagas) {
            convenio.setEstado(EstadoConvenioFraccionamiento.EN_REVISION);
            convenioRepository.save(convenio);

            IncidenciaConvenioFraccionamiento inc = IncidenciaConvenioFraccionamiento.builder()
                    .convenio(convenio)
                    .tipoIncidencia(TipoIncidenciaConvenio.INCUMPLIMIENTO_PAGO)
                    .descripcion(String.format("Convenio registra %d cuotas impagas vencidas. Pasa a estado EN_REVISION para dictamen técnico según parámetro normativo.", cuotasVencidas.size()))
                    .responsable(usuario)
                    .fechaRegistro(OffsetDateTime.now())
                    .build();
            incidenciaRepository.save(inc);
            log.warn("Convenio {} en causal de incumplimiento con {} cuotas vencidas", convenio.getCodigoConvenio(), cuotasVencidas.size());
        }

        return mapearConvenioResponse(convenio);
    }

    public ConvenioFraccionamientoResponse mapearConvenioResponse(ConvenioFraccionamientoTributario conv) {
        if (conv == null) return null;

        CronogramaFraccionamientoTributario cron = conv.getCronograma();
        List<CuotaDto> cuotasDto = new ArrayList<>();
        BigDecimal totalPagado = BigDecimal.ZERO;
        BigDecimal saldoPendiente = BigDecimal.ZERO;
        long pagadas = 0;
        long pendientes = 0;
        long vencidas = 0;

        if (cron != null) {
            List<CuotaFraccionamientoTributario> cuotas = cuotaRepository.findByCronogramaIdOrderByNumeroCuotaAsc(cron.getId());
            for (CuotaFraccionamientoTributario c : cuotas) {
                boolean isVencida = c.getFechaVencimiento().isBefore(LocalDate.now()) && c.getSaldo().compareTo(BigDecimal.ZERO) > 0;
                if (c.getEstado() == EstadoCuotaFraccionamiento.PAGADA) {
                    pagadas++;
                } else {
                    pendientes++;
                    if (isVencida) vencidas++;
                }
                totalPagado = totalPagado.add(c.getImportePagado());
                saldoPendiente = saldoPendiente.add(c.getSaldo());

                cuotasDto.add(CuotaDto.builder()
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
                        .vencida(isVencida)
                        .build());
            }
        }

        CronogramaDto cronogramaDto = null;
        if (cron != null) {
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

        List<DetalleObligacionFraccionadaDto> oblDto = detalleRepository.findBySolicitudId(conv.getSolicitud().getId()).stream()
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

        List<IncidenciaConvenioDto> incidenciasDto = incidenciaRepository.findByConvenioIdOrderByFechaRegistroDesc(conv.getId()).stream()
                .map(inc -> IncidenciaConvenioDto.builder()
                        .id(inc.getId())
                        .tipoIncidencia(inc.getTipoIncidencia())
                        .tipoIncidenciaDescripcion(inc.getTipoIncidencia() != null ? inc.getTipoIncidencia().getDescripcion() : "")
                        .descripcion(inc.getDescripcion())
                        .fechaRegistro(inc.getFechaRegistro())
                        .responsableNombre(inc.getResponsable() != null ? inc.getResponsable().getUsername() : "-")
                        .documentoSustentoId(inc.getDocumentoSustento() != null ? inc.getDocumentoSustento().getIdentificadorPublico() : null)
                        .build())
                .collect(Collectors.toList());

        return ConvenioFraccionamientoResponse.builder()
                .id(conv.getId())
                .codigoConvenio(conv.getCodigoConvenio())
                .solicitudId(conv.getSolicitud().getId())
                .codigoSolicitud(conv.getSolicitud().getCodigoSolicitud())
                .contribuyenteId(conv.getContribuyente().getId())
                .contribuyenteNombre(conv.getContribuyente().getNombreRazonSocial())
                .contribuyenteDocumento(conv.getContribuyente().getNumeroDocumento())
                .contribuyenteCodigo(conv.getContribuyente().getCodigoContribuyente())
                .contribuyenteDireccion(conv.getContribuyente().getDomicilioFiscal())
                .programaId(conv.getPrograma().getId())
                .programaNombre(conv.getPrograma().getNombre())
                .programaNorma(conv.getPrograma().getNorma() != null ? conv.getPrograma().getNorma().getNumeroNorma() : "Art. 36 TUO Código Tributario")
                .fechaAprobacion(conv.getFechaAprobacion())
                .fechaInicioVigencia(conv.getFechaInicioVigencia())
                .resolucionAprobacion(conv.getResolucionAprobacion())
                .estado(conv.getEstado())
                .estadoDescripcion(conv.getEstado() != null ? conv.getEstado().getDescripcion() : "")
                .estadoBadgeClass(conv.getEstado() != null ? conv.getEstado().getBadgeClass() : "")
                .responsableAprobacionNombre(conv.getResponsableAprobacion() != null ? conv.getResponsableAprobacion().getUsername() : "-")
                .documentoConvenioId(conv.getDocumentoConvenio() != null ? conv.getDocumentoConvenio().getIdentificadorPublico() : null)
                .codigoVerificacion(conv.getCodigoVerificacion())
                .observaciones(conv.getObservaciones())
                .importeBase(cron != null ? cron.getImporteBase() : BigDecimal.ZERO)
                .importeCuotaInicial(cron != null ? cron.getImporteCuotaInicial() : BigDecimal.ZERO)
                .importeFinanciado(cron != null ? cron.getImporteFinanciado() : BigDecimal.ZERO)
                .importeAccesorios(cron != null ? cron.getImporteAccesorios() : BigDecimal.ZERO)
                .importeTotal(cron != null ? cron.getImporteTotal() : BigDecimal.ZERO)
                .numeroCuotas(cron != null ? cron.getNumeroCuotas() : 0)
                .totalPagado(totalPagado)
                .saldoPendiente(saldoPendiente)
                .cuotasPagadas(pagadas)
                .cuotasPendientes(pendientes)
                .cuotasVencidas(vencidas)
                .cronograma(cronogramaDto)
                .obligaciones(oblDto)
                .incidencias(incidenciasDto)
                .build();
    }
}
