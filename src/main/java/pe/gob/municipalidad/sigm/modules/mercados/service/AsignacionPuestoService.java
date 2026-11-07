package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.administracion.service.AuditoriaAdministrativaService;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.document.repository.DocumentoRepository;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ConflictException;
import pe.gob.municipalidad.sigm.common.exception.InvalidRequestException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.mercados.entity.Comerciante;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoComerciante;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.dto.MercadosDtos.AsignacionRequest;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.*;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AsignacionPuestoService {

    private final AsignacionPuestoRepository asignacionRepository;
    private final PuestoComercialRepository puestoRepository;
    private final ComercianteRepository comercianteRepository;
    private final DocumentoRepository documentoRepository;
    private final AsignacionEvaluacionRepository evaluacionRepository;
    private final AsignacionDevolucionRepository devolucionRepository;
    private final HistorialOcupacionService historialService;
    private final DisponibilidadPuestoService disponibilidadService;
    private final AuditoriaAdministrativaService auditoriaService;

    public AsignacionPuestoService(
            AsignacionPuestoRepository asignacionRepository,
            PuestoComercialRepository puestoRepository,
            ComercianteRepository comercianteRepository,
            DocumentoRepository documentoRepository,
            AsignacionEvaluacionRepository evaluacionRepository,
            AsignacionDevolucionRepository devolucionRepository,
            HistorialOcupacionService historialService,
            DisponibilidadPuestoService disponibilidadService,
            AuditoriaAdministrativaService auditoriaService) {
        this.asignacionRepository = asignacionRepository;
        this.puestoRepository = puestoRepository;
        this.comercianteRepository = comercianteRepository;
        this.documentoRepository = documentoRepository;
        this.evaluacionRepository = evaluacionRepository;
        this.devolucionRepository = devolucionRepository;
        this.historialService = historialService;
        this.disponibilidadService = disponibilidadService;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public AsignacionDetailResponse registrarSolicitud(AsignacionCreateRequest request) {
        if (request.getFechaFin() != null && request.getFechaFin().isBefore(request.getFechaInicio())) {
            throw new InvalidRequestException("La fecha de culminación no puede ser anterior a la fecha de inicio.");
        }

        PuestoComercial puesto = puestoRepository.findById(request.getPuestoId())
                .orElseThrow(() -> new ResourceNotFoundException("Puesto comercial no encontrado con ID: " + request.getPuestoId()));

        Comerciante comerciante = comercianteRepository.findById(request.getComercianteId())
                .orElseThrow(() -> new ResourceNotFoundException("Comerciante no encontrado con ID: " + request.getComercianteId()));

        // Validar mercado activo
        if (puesto.getMercado() == null || puesto.getMercado().getEstado() != EstadoMercado.ACTIVO) {
            throw new BusinessException("No se pueden registrar asignaciones en un mercado que no esté activo.");
        }

        // Validar comerciante
        disponibilidadService.validarComercianteAptoParaAsignacion(comerciante.getId());

        // Generar código institucional seguro: ASI-YYYY-NNNNNN
        String codigoAsignacion = generarCodigoAsignacion();

        Documento documento = null;
        if (request.getDocumentoSustentoId() != null) {
            documento = documentoRepository.findById(request.getDocumentoSustentoId()).orElse(null);
        }

        String usuario = obtenerUsuarioAutenticado();
        EstadoAsignacion estadoInicial = request.isPresentarInmediatamente() ? EstadoAsignacion.SOLICITADA : EstadoAsignacion.BORRADOR;

        AsignacionPuesto asignacion = AsignacionPuesto.builder()
                .codigoAsignacion(codigoAsignacion)
                .puesto(puesto)
                .comerciante(comerciante)
                .fechaSolicitud(LocalDate.now())
                .fechaInicio(request.getFechaInicio())
                .fechaFin(request.getFechaFin())
                .estado(estadoInicial)
                .tipoAsignacion(request.getTipoAsignacion() != null ? request.getTipoAsignacion() : TipoAsignacion.REGULAR)
                .documentoSustento(documento)
                .expedienteId(request.getExpedienteId())
                .motivo(request.getMotivo())
                .observaciones(request.getObservaciones())
                .usuarioRegistro(usuario)
                .build();

        AsignacionPuesto guardada = asignacionRepository.save(asignacion);

        historialService.registrarMovimiento(
                puesto,
                comerciante,
                guardada,
                TipoMovimientoOcupacion.SOLICITUD,
                "Registro de solicitud de asignación (" + estadoInicial + "): " + codigoAsignacion,
                documento,
                null,
                estadoInicial.name(),
                request.getMotivo()
        );

        auditoriaService.registrar("REGISTRAR_SOLICITUD_ASIGNACION", "mercados_asignaciones", guardada.getId(), "EXITO",
                "Solicitud de asignación " + codigoAsignacion + " registrada para puesto " + puesto.getCodigo() + " en estado " + estadoInicial);

        return toDetailResponse(guardada);
    }

    @Transactional
    public AsignacionDetailResponse presentarSolicitud(UUID id) {
        AsignacionPuesto asignacion = asignacionRepository.findByIdCompleto(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada con ID: " + id));

        if (asignacion.getEstado() != EstadoAsignacion.BORRADOR) {
            throw new BusinessException("Solo las solicitudes en estado BORRADOR pueden ser presentadas. Estado actual: " + asignacion.getEstado());
        }

        EstadoAsignacion anterior = asignacion.getEstado();
        asignacion.setEstado(EstadoAsignacion.SOLICITADA);
        asignacionRepository.save(asignacion);

        historialService.registrarMovimiento(
                asignacion.getPuesto(),
                asignacion.getComerciante(),
                asignacion,
                TipoMovimientoOcupacion.CAMBIO_ESTADO,
                "Presentación formal de solicitud para evaluación técnica",
                asignacion.getDocumentoSustento(),
                anterior.name(),
                EstadoAsignacion.SOLICITADA.name(),
                null
        );

        auditoriaService.registrar("PRESENTAR_SOLICITUD_ASIGNACION", "mercados_asignaciones", asignacion.getId(), "EXITO",
                "Presentada solicitud " + asignacion.getCodigoAsignacion());

        return toDetailResponse(asignacion);
    }

    @Transactional
    public AsignacionDetailResponse actualizarSolicitud(UUID id, AsignacionUpdateRequest request) {
        AsignacionPuesto asignacion = asignacionRepository.findByIdCompleto(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada con ID: " + id));

        if (!asignacion.getEstado().permiteEdicion()) {
            throw new BusinessException("No se puede editar una solicitud en estado: " + asignacion.getEstado());
        }

        if (request.getFechaFin() != null && request.getFechaFin().isBefore(request.getFechaInicio())) {
            throw new InvalidRequestException("La fecha de culminación no puede ser anterior a la fecha de inicio.");
        }

        asignacion.setFechaInicio(request.getFechaInicio());
        asignacion.setFechaFin(request.getFechaFin());
        if (request.getTipoAsignacion() != null) {
            asignacion.setTipoAsignacion(request.getTipoAsignacion());
        }
        if (request.getExpedienteId() != null) {
            asignacion.setExpedienteId(request.getExpedienteId());
        }
        if (request.getDocumentoSustentoId() != null) {
            Documento doc = documentoRepository.findById(request.getDocumentoSustentoId()).orElse(null);
            asignacion.setDocumentoSustento(doc);
        }
        if (request.getMotivo() != null) {
            asignacion.setMotivo(request.getMotivo());
        }
        if (request.getObservaciones() != null) {
            asignacion.setObservaciones(request.getObservaciones());
        }

        AsignacionPuesto actualizada = asignacionRepository.save(asignacion);

        auditoriaService.registrar("ACTUALIZAR_SOLICITUD_ASIGNACION", "mercados_asignaciones", asignacion.getId(), "EXITO",
                "Actualizada solicitud " + asignacion.getCodigoAsignacion());

        return toDetailResponse(actualizada);
    }

    @Transactional
    public AsignacionDetailResponse anularAsignacion(UUID id, String motivo) {
        AsignacionPuesto asignacion = asignacionRepository.findByIdCompleto(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada con ID: " + id));

        if (asignacion.getEstado() == EstadoAsignacion.FINALIZADA || asignacion.getEstado() == EstadoAsignacion.ANULADA) {
            throw new BusinessException("No se puede anular una asignación que ya se encuentra " + asignacion.getEstado());
        }

        EstadoAsignacion anterior = asignacion.getEstado();
        asignacion.setEstado(EstadoAsignacion.ANULADA);
        asignacionRepository.save(asignacion);

        // Si estaba vigente, liberar el puesto
        PuestoComercial puesto = asignacion.getPuesto();
        if (anterior == EstadoAsignacion.VIGENTE && puesto != null) {
            puesto.setEstado(EstadoPuesto.DISPONIBLE);
            puestoRepository.save(puesto);
        }

        historialService.registrarMovimiento(
                puesto,
                asignacion.getComerciante(),
                asignacion,
                TipoMovimientoOcupacion.CAMBIO_ESTADO,
                "Anulación formal de asignación: " + (motivo != null ? motivo : "Sin motivo especificado"),
                null,
                anterior.name(),
                EstadoAsignacion.ANULADA.name(),
                motivo
        );

        auditoriaService.registrar("ANULAR_ASIGNACION_PUESTO", "mercados_asignaciones", asignacion.getId(), "EXITO",
                "Anulada asignación " + asignacion.getCodigoAsignacion() + " - " + motivo);

        return toDetailResponse(asignacion);
    }

    @Transactional(readOnly = true)
    public AsignacionDetailResponse obtenerPorIdCompleto(UUID id) {
        AsignacionPuesto asignacion = asignacionRepository.findByIdCompleto(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada con ID: " + id));

        AsignacionDetailResponse detail = toDetailResponse(asignacion);

        // Cargar evaluaciones
        List<AsignacionEvaluacionResponse> evalList = evaluacionRepository.findByAsignacionIdOrderByFechaEvaluacionDesc(id).stream()
                .map(e -> AsignacionEvaluacionResponse.builder()
                        .id(e.getId())
                        .tipoEvaluacion(e.getTipoEvaluacion())
                        .resultado(e.getResultado())
                        .motivo(e.getMotivo())
                        .documentoSustentoId(e.getDocumentoSustento() != null ? e.getDocumentoSustento().getId() : null)
                        .documentoSustentoNombre(e.getDocumentoSustento() != null ? e.getDocumentoSustento().getNombreOriginal() : null)
                        .usuarioEvaluador(e.getUsuarioEvaluador())
                        .fechaEvaluacion(e.getFechaEvaluacion())
                        .build())
                .collect(Collectors.toList());
        detail.setEvaluaciones(evalList);

        // Cargar historial
        detail.setHistorial(historialService.obtenerHistorialPorAsignacion(id));

        // Cargar devolución si existe
        devolucionRepository.findFirstByAsignacionIdOrderByFechaDevolucionDesc(id)
                .ifPresent(d -> detail.setDevolucion(DevolucionPuestoResponse.builder()
                        .id(d.getId())
                        .asignacionId(asignacion.getId())
                        .puestoId(d.getPuesto() != null ? d.getPuesto().getId() : null)
                        .fechaDevolucion(d.getFechaDevolucion())
                        .responsableRecepcion(d.getResponsableRecepcion())
                        .estadoFisicoPuesto(d.getEstadoFisicoPuesto())
                        .observaciones(d.getObservaciones())
                        .documentoActaId(d.getDocumentoActa() != null ? d.getDocumentoActa().getId() : null)
                        .documentoActaNombre(d.getDocumentoActa() != null ? d.getDocumentoActa().getNombreOriginal() : null)
                        .build()));

        return detail;
    }

    @Transactional(readOnly = true)
    public Page<AsignacionResponse> listarConFiltros(AsignacionFilter filter, Pageable pageable) {
        if (filter != null && filter.getTermino() != null && !filter.getTermino().trim().isEmpty()) {
            return asignacionRepository.buscarConFiltrosConTermino(
                    filter.getTermino().trim(),
                    filter.getMercadoId(),
                    filter.getSectorId(),
                    filter.getPabellonId(),
                    filter.getComercianteId(),
                    filter.getEstado(),
                    filter.getTipoAsignacion(),
                    filter.getFechaInicioDesde(),
                    filter.getFechaInicioHasta(),
                    pageable
            ).map(this::toResponse);
        } else {
            return asignacionRepository.buscarConFiltrosSinTermino(
                    filter != null ? filter.getMercadoId() : null,
                    filter != null ? filter.getSectorId() : null,
                    filter != null ? filter.getPabellonId() : null,
                    filter != null ? filter.getComercianteId() : null,
                    filter != null ? filter.getEstado() : null,
                    filter != null ? filter.getTipoAsignacion() : null,
                    filter != null ? filter.getFechaInicioDesde() : null,
                    filter != null ? filter.getFechaInicioHasta() : null,
                    pageable
            ).map(this::toResponse);
        }
    }

    @Transactional(readOnly = true)
    public AsignacionEstadisticasResponse obtenerEstadisticas() {
        long total = asignacionRepository.count();
        long borradores = asignacionRepository.countByEstado(EstadoAsignacion.BORRADOR);
        long solicitadas = asignacionRepository.countByEstado(EstadoAsignacion.SOLICITADA);
        long enEval = asignacionRepository.countByEstado(EstadoAsignacion.EN_EVALUACION);
        long observadas = asignacionRepository.countByEstado(EstadoAsignacion.OBSERVADA);
        long aprobadas = asignacionRepository.countByEstado(EstadoAsignacion.APROBADA);
        long rechazadas = asignacionRepository.countByEstado(EstadoAsignacion.RECHAZADA);
        long vigentes = asignacionRepository.countByEstado(EstadoAsignacion.VIGENTE);
        long suspendidas = asignacionRepository.countByEstado(EstadoAsignacion.SUSPENDIDA);
        long finalizadas = asignacionRepository.countByEstado(EstadoAsignacion.FINALIZADA);
        long anuladas = asignacionRepository.countByEstado(EstadoAsignacion.ANULADA);

        long puestosTotales = puestoRepository.count();
        long puestosDisponibles = puestoRepository.countByEstado(EstadoPuesto.DISPONIBLE);
        long puestosOcupados = puestoRepository.countByEstado(EstadoPuesto.OCUPADO);
        long puestosMantenimiento = puestoRepository.countByEstado(EstadoPuesto.EN_MANTENIMIENTO);

        Map<String, Long> porMercado = new HashMap<>();

        return AsignacionEstadisticasResponse.builder()
                .totalAsignaciones(total)
                .borradores(borradores)
                .solicitadas(solicitadas)
                .enEvaluacion(enEval)
                .observadas(observadas)
                .aprobadas(aprobadas)
                .rechazadas(rechazadas)
                .vigentes(vigentes)
                .suspendidas(suspendidas)
                .finalizadas(finalizadas)
                .anuladas(anuladas)
                .puestosTotales(puestosTotales)
                .puestosDisponibles(puestosDisponibles)
                .puestosOcupados(puestosOcupados)
                .puestosMantenimiento(puestosMantenimiento)
                .asignacionesPorMercado(porMercado)
                .build();
    }

    // ------------------------------------------------------------------------
    // Compatibilidad con invocaciones existentes de otros módulos
    // ------------------------------------------------------------------------
    @Transactional
    public pe.gob.municipalidad.sigm.modules.mercados.dto.MercadosDtos.AsignacionResponse crearAsignacion(AsignacionRequest request) {
        PuestoComercial puesto = puestoRepository.findById(request.getPuestoId())
                .orElseThrow(() -> new ResourceNotFoundException("Puesto comercial no encontrado con ID: " + request.getPuestoId()));
        if (puesto.getEstado() != EstadoPuesto.DISPONIBLE || asignacionRepository.existsByPuestoIdAndEstado(puesto.getId(), EstadoAsignacion.VIGENTE)) {
            throw new ConflictException("El puesto '" + puesto.getCodigo() + "' ya cuenta con una asignación vigente activa");
        }

        AsignacionCreateRequest createReq = AsignacionCreateRequest.builder()
                .puestoId(request.getPuestoId())
                .comercianteId(request.getComercianteId())
                .fechaInicio(request.getFechaInicio())
                .fechaFin(request.getFechaFin())
                .tipoAsignacion(request.getTipoAsignacion())
                .documentoSustentoId(request.getDocumentoSustentoId())
                .observaciones(request.getObservaciones())
                .presentarInmediatamente(true)
                .build();

        AsignacionDetailResponse detail = registrarSolicitud(createReq);
        AsignacionPuesto asignacion = asignacionRepository.findByIdCompleto(detail.getId()).orElseThrow();
        asignacion.setEstado(EstadoAsignacion.VIGENTE);
        asignacionRepository.save(asignacion);

        puesto.setEstado(EstadoPuesto.OCUPADO);
        puestoRepository.save(puesto);

        historialService.registrarMovimiento(
                puesto,
                asignacion.getComerciante(),
                asignacion,
                TipoMovimientoOcupacion.ACTIVACION,
                "Activación de asignación comercial y ocupación de puesto",
                asignacion.getDocumentoSustento(),
                EstadoAsignacion.SOLICITADA.name(),
                "OCUPADO",
                "Ocupación de puesto comercial"
        );

        return toLegacyResponse(toDetailResponse(asignacion));
    }

    @Transactional
    public pe.gob.municipalidad.sigm.modules.mercados.dto.MercadosDtos.AsignacionResponse finalizarAsignacion(UUID id, String motivo) {
        AsignacionFinalizacionRequest finReq = AsignacionFinalizacionRequest.builder()
                .motivoFinalizacion(motivo != null ? motivo : "Finalización de asignación comercial")
                .registrarDevolucionInmediata(false)
                .build();

        AsignacionPuesto asig = asignacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada con ID: " + id));

        asig.setEstado(EstadoAsignacion.FINALIZADA);
        asig.setMotivoFinalizacion(motivo);
        asignacionRepository.save(asig);

        if (asig.getPuesto() != null) {
            asig.getPuesto().setEstado(EstadoPuesto.DISPONIBLE);
            puestoRepository.save(asig.getPuesto());
        }

        return toLegacyResponse(toDetailResponse(asig));
    }

    @Transactional(readOnly = true)
    public pe.gob.municipalidad.sigm.modules.mercados.dto.MercadosDtos.AsignacionResponse obtenerPorId(UUID id) {
        return toLegacyResponse(obtenerPorIdCompleto(id));
    }

    @Transactional(readOnly = true)
    public Page<pe.gob.municipalidad.sigm.modules.mercados.dto.MercadosDtos.AsignacionResponse> listarPorEstado(EstadoAsignacion estado, Pageable pageable) {
        return asignacionRepository.findByEstado(estado, pageable).map(a -> toLegacyResponse(toDetailResponse(a)));
    }

    private pe.gob.municipalidad.sigm.modules.mercados.dto.MercadosDtos.AsignacionResponse toLegacyResponse(AsignacionDetailResponse d) {
        return pe.gob.municipalidad.sigm.modules.mercados.dto.MercadosDtos.AsignacionResponse.builder()
                .id(d.getId())
                .puestoId(d.getPuestoId())
                .puestoCodigo(d.getPuestoCodigo())
                .puestoNumero(d.getPuestoNumero())
                .mercadoNombre(d.getMercadoNombre())
                .comercianteId(d.getComercianteId())
                .comercianteCodigo(d.getComercianteCodigo())
                .comercianteNombre(d.getTitularNombre())
                .fechaInicio(d.getFechaInicio())
                .fechaFin(d.getFechaFin())
                .estado(d.getEstado())
                .tipoAsignacion(d.getTipoAsignacion())
                .documentoSustentoId(d.getDocumentoSustentoId())
                .observaciones(d.getObservaciones())
                .build();
    }

    // ------------------------------------------------------------------------
    // Mappers y utilitarios internos
    // ------------------------------------------------------------------------
    public AsignacionResponse toResponse(AsignacionPuesto a) {
        String titular = "-";
        String docEnmascarado = "-";
        if (a.getComerciante() != null) {
            if (a.getComerciante().getPersona() != null) {
                titular = a.getComerciante().getPersona().getNombreCompleto();
                docEnmascarado = enmascararDni(a.getComerciante().getPersona().getNumeroDocumento());
            } else if (a.getComerciante().getPersonaJuridica() != null) {
                titular = a.getComerciante().getPersonaJuridica().getRazonSocial();
                docEnmascarado = enmascararRuc(a.getComerciante().getPersonaJuridica().getRuc());
            }
        }

        return AsignacionResponse.builder()
                .id(a.getId())
                .codigoAsignacion(a.getCodigoAsignacion())
                .puestoId(a.getPuesto() != null ? a.getPuesto().getId() : null)
                .puestoCodigo(a.getPuesto() != null ? a.getPuesto().getCodigo() : null)
                .puestoNumero(a.getPuesto() != null ? a.getPuesto().getNumero() : null)
                .mercadoNombre(a.getPuesto() != null && a.getPuesto().getMercado() != null ? a.getPuesto().getMercado().getNombre() : "-")
                .sectorNombre(a.getPuesto() != null && a.getPuesto().getSector() != null ? a.getPuesto().getSector().getNombre() : "-")
                .pabellonNombre(a.getPuesto() != null && a.getPuesto().getPabellon() != null ? a.getPuesto().getPabellon().getNombre() : "-")
                .comercianteId(a.getComerciante() != null ? a.getComerciante().getId() : null)
                .comercianteCodigo(a.getComerciante() != null ? a.getComerciante().getCodigoComerciante() : "-")
                .comercianteTipo(a.getComerciante() != null && a.getComerciante().getTipoComerciante() != null ? a.getComerciante().getTipoComerciante().name() : "-")
                .titularNombre(titular)
                .documentoEnmascarado(docEnmascarado)
                .fechaSolicitud(a.getFechaSolicitud())
                .fechaInicio(a.getFechaInicio())
                .fechaFin(a.getFechaFin())
                .estado(a.getEstado())
                .estadoDescripcion(a.getEstado() != null ? a.getEstado().getDescripcion() : "-")
                .badgeClase(a.getEstado() != null ? a.getEstado().getBadgeClase() : "")
                .tipoAsignacion(a.getTipoAsignacion())
                .motivo(a.getMotivo())
                .usuarioResponsable(a.getUsuarioRegistro())
                .documentoSustentoId(a.getDocumentoSustento() != null ? a.getDocumentoSustento().getId() : null)
                .expedienteId(a.getExpedienteId())
                .observaciones(a.getObservaciones())
                .build();
    }

    public AsignacionDetailResponse toDetailResponse(AsignacionPuesto a) {
        String titular = "-";
        String doc = "-";
        if (a.getComerciante() != null) {
            if (a.getComerciante().getPersona() != null) {
                titular = a.getComerciante().getPersona().getNombreCompleto();
                doc = a.getComerciante().getPersona().getNumeroDocumento();
            } else if (a.getComerciante().getPersonaJuridica() != null) {
                titular = a.getComerciante().getPersonaJuridica().getRazonSocial();
                doc = a.getComerciante().getPersonaJuridica().getRuc();
            }
        }

        return AsignacionDetailResponse.builder()
                .id(a.getId())
                .codigoAsignacion(a.getCodigoAsignacion())
                .puestoId(a.getPuesto() != null ? a.getPuesto().getId() : null)
                .puestoCodigo(a.getPuesto() != null ? a.getPuesto().getCodigo() : null)
                .puestoNumero(a.getPuesto() != null ? a.getPuesto().getNumero() : null)
                .puestoEstado(a.getPuesto() != null ? a.getPuesto().getEstado().name() : null)
                .puestoArea(a.getPuesto() != null ? a.getPuesto().getAreaMetrosCuadrados() : null)
                .tipoPuestoNombre(a.getPuesto() != null && a.getPuesto().getTipoPuesto() != null ? a.getPuesto().getTipoPuesto().getNombre() : null)
                .mercadoId(a.getPuesto() != null && a.getPuesto().getMercado() != null ? a.getPuesto().getMercado().getId() : null)
                .mercadoNombre(a.getPuesto() != null && a.getPuesto().getMercado() != null ? a.getPuesto().getMercado().getNombre() : null)
                .mercadoCodigo(a.getPuesto() != null && a.getPuesto().getMercado() != null ? a.getPuesto().getMercado().getCodigo() : null)
                .sectorNombre(a.getPuesto() != null && a.getPuesto().getSector() != null ? a.getPuesto().getSector().getNombre() : null)
                .pabellonNombre(a.getPuesto() != null && a.getPuesto().getPabellon() != null ? a.getPuesto().getPabellon().getNombre() : null)
                .comercianteId(a.getComerciante() != null ? a.getComerciante().getId() : null)
                .comercianteCodigo(a.getComerciante() != null ? a.getComerciante().getCodigoComerciante() : null)
                .comercianteTipo(a.getComerciante() != null && a.getComerciante().getTipoComerciante() != null ? a.getComerciante().getTipoComerciante().name() : null)
                .titularNombre(titular)
                .titularDocumento(doc)
                .comercianteTelefono(a.getComerciante() != null ? a.getComerciante().getTelefonoContacto() : null)
                .comercianteCorreo(a.getComerciante() != null ? a.getComerciante().getCorreoContacto() : null)
                .comercianteDireccion(a.getComerciante() != null ? a.getComerciante().getDireccionContacto() : null)
                .comercianteEstado(a.getComerciante() != null && a.getComerciante().getEstado() != null ? a.getComerciante().getEstado().name() : null)
                .fechaSolicitud(a.getFechaSolicitud())
                .fechaInicio(a.getFechaInicio())
                .fechaFin(a.getFechaFin())
                .estado(a.getEstado())
                .estadoDescripcion(a.getEstado() != null ? a.getEstado().getDescripcion() : null)
                .badgeClase(a.getEstado() != null ? a.getEstado().getBadgeClase() : null)
                .tipoAsignacion(a.getTipoAsignacion())
                .motivo(a.getMotivo())
                .observaciones(a.getObservaciones())
                .usuarioRegistro(a.getUsuarioRegistro())
                .usuarioEvaluacion(a.getUsuarioEvaluacion())
                .fechaEvaluacion(a.getFechaEvaluacion())
                .observacionesEvaluacion(a.getObservacionesEvaluacion())
                .usuarioAprobacion(a.getUsuarioAprobacion())
                .fechaAprobacion(a.getFechaAprobacion())
                .motivoRechazo(a.getMotivoRechazo())
                .motivoSuspension(a.getMotivoSuspension())
                .motivoFinalizacion(a.getMotivoFinalizacion())
                .documentoSustentoId(a.getDocumentoSustento() != null ? a.getDocumentoSustento().getId() : null)
                .documentoSustentoNombre(a.getDocumentoSustento() != null ? a.getDocumentoSustento().getNombreOriginal() : null)
                .expedienteId(a.getExpedienteId())
                .version(a.getVersion())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }

    private synchronized String generarCodigoAsignacion() {
        Long seq = asignacionRepository.obtenerSiguienteValorSecuencia();
        int anio = LocalDate.now().getYear();
        return String.format("ASI-%d-%06d", anio, seq);
    }

    private String enmascararDni(String dni) {
        if (dni == null || dni.length() < 4) return "****";
        return dni.substring(0, 4) + "****";
    }

    private String enmascararRuc(String ruc) {
        if (ruc == null || ruc.length() < 11) return "***********";
        return ruc.substring(0, 4) + "******" + ruc.substring(10);
    }

    private String obtenerUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        return "SISTEMA";
    }
}
