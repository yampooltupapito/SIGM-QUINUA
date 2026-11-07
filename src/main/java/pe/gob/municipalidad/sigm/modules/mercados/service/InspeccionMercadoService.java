package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.administracion.service.AuditoriaAdministrativaService;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoInspeccion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoInspeccionMercado;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class InspeccionMercadoService {

    private final InspeccionMercadoRepository inspeccionRepository;
    private final InspeccionParticipanteRepository participanteRepository;
    private final MercadoMunicipalRepository mercadoRepository;
    private final SectorMercadoRepository sectorRepository;
    private final PabellonMercadoRepository pabellonRepository;
    private final PuestoComercialRepository puestoRepository;
    private final ComercianteRepository comercianteRepository;
    private final PlantillaInspeccionRepository plantillaRepository;
    private final OficinaRepository oficinaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaAdministrativaService auditoriaService;

    public InspeccionMercadoService(InspeccionMercadoRepository inspeccionRepository,
                                   InspeccionParticipanteRepository participanteRepository,
                                   MercadoMunicipalRepository mercadoRepository,
                                   SectorMercadoRepository sectorRepository,
                                   PabellonMercadoRepository pabellonRepository,
                                   PuestoComercialRepository puestoRepository,
                                   ComercianteRepository comercianteRepository,
                                   PlantillaInspeccionRepository plantillaRepository,
                                   OficinaRepository oficinaRepository,
                                   UsuarioRepository usuarioRepository,
                                   AuditoriaAdministrativaService auditoriaService) {
        this.inspeccionRepository = inspeccionRepository;
        this.participanteRepository = participanteRepository;
        this.mercadoRepository = mercadoRepository;
        this.sectorRepository = sectorRepository;
        this.pabellonRepository = pabellonRepository;
        this.puestoRepository = puestoRepository;
        this.comercianteRepository = comercianteRepository;
        this.plantillaRepository = plantillaRepository;
        this.oficinaRepository = oficinaRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public InspeccionResponse crear(InspeccionCreateRequest request, String username) {
        MercadoMunicipal mercado = mercadoRepository.findById(request.getMercadoId())
                .orElseThrow(() -> new ResourceNotFoundException("Mercado municipal no encontrado: " + request.getMercadoId()));

        SectorMercado sector = null;
        if (request.getSectorId() != null) {
            sector = sectorRepository.findById(request.getSectorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sector no encontrado: " + request.getSectorId()));
            if (!sector.getMercado().getId().equals(mercado.getId())) {
                throw new BusinessException("El sector seleccionado no pertenece al mercado: " + mercado.getNombre());
            }
        }

        PabellonMercado pabellon = null;
        if (request.getPabellonId() != null) {
            pabellon = pabellonRepository.findById(request.getPabellonId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pabellón no encontrado: " + request.getPabellonId()));
            if (!pabellon.getMercado().getId().equals(mercado.getId())) {
                throw new BusinessException("El pabellón seleccionado no pertenece al mercado: " + mercado.getNombre());
            }
            if (sector != null && pabellon.getSector() != null && !pabellon.getSector().getId().equals(sector.getId())) {
                throw new BusinessException("El pabellón seleccionado no pertenece al sector indicado.");
            }
        }

        PuestoComercial puesto = null;
        if (request.getPuestoId() != null) {
            puesto = puestoRepository.findById(request.getPuestoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Puesto comercial no encontrado: " + request.getPuestoId()));
            if (!puesto.getMercado().getId().equals(mercado.getId())) {
                throw new BusinessException("El puesto comercial no pertenece al mercado: " + mercado.getNombre());
            }
        }

        Comerciante comerciante = null;
        if (request.getComercianteId() != null) {
            comerciante = comercianteRepository.findById(request.getComercianteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Comerciante no encontrado: " + request.getComercianteId()));
        }

        InspeccionMercado inspeccionOrigen = null;
        if (request.getInspeccionOrigenId() != null) {
            inspeccionOrigen = inspeccionRepository.findById(request.getInspeccionOrigenId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inspección origen no encontrada: " + request.getInspeccionOrigenId()));
        }

        PlantillaInspeccion plantilla = null;
        if (request.getPlantillaId() != null) {
            plantilla = plantillaRepository.findById(request.getPlantillaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Plantilla no encontrada: " + request.getPlantillaId()));
        }

        Oficina oficina = null;
        if (request.getOficinaResponsableId() != null) {
            oficina = oficinaRepository.findById(request.getOficinaResponsableId()).orElse(null);
        }

        Usuario inspector = null;
        if (request.getInspectorResponsableId() != null) {
            inspector = usuarioRepository.findById(request.getInspectorResponsableId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inspector no encontrado: " + request.getInspectorResponsableId()));
        }

        Long correlativo = inspeccionRepository.obtenerSiguienteCorrelativo();
        int anio = Year.now().getValue();
        String codigo = String.format("INS-MER-%d-%06d", anio, correlativo);

        EstadoInspeccion estadoInicial = inspector != null ? EstadoInspeccion.ASIGNADA : EstadoInspeccion.PROGRAMADA;

        InspeccionMercado entity = InspeccionMercado.builder()
                .codigoInspeccion(codigo)
                .tipoInspeccion(request.getTipoInspeccion())
                .mercado(mercado)
                .sector(sector)
                .pabellon(pabellon)
                .puesto(puesto)
                .comerciante(comerciante)
                .inspeccionOrigen(inspeccionOrigen)
                .plantilla(plantilla)
                .fechaProgramada(request.getFechaProgramada())
                .horaProgramada(request.getHoraProgramada())
                .estado(estadoInicial)
                .motivo(request.getMotivo().trim())
                .objetivo(request.getObjetivo().trim())
                .oficinaResponsable(oficina)
                .inspectorResponsable(inspector)
                .observacionesGenerales(request.getObservacionesGenerales())
                .participantes(new ArrayList<>())
                .verificaciones(new ArrayList<>())
                .hallazgos(new ArrayList<>())
                .evidencias(new ArrayList<>())
                .build();

        if (inspector != null) {
            InspeccionParticipante p = InspeccionParticipante.builder()
                    .inspeccion(entity)
                    .usuario(inspector)
                    .funcion("INSPECTOR_PRINCIPAL")
                    .estado("ACTIVO")
                    .build();
            entity.getParticipantes().add(p);
        }

        InspeccionMercado guardada = inspeccionRepository.save(entity);

        auditoriaService.registrar("CREAR_INSPECCION_MERCADO", "mercados_inspecciones", guardada.getId(),
                "EXITO", "Inspección creada: " + guardada.getCodigoInspeccion() + " en " + mercado.getNombre());

        return toResponse(guardada);
    }

    @Transactional
    public InspeccionResponse programar(UUID id, InspeccionProgramacionRequest request, String username) {
        InspeccionMercado entity = inspeccionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inspección no encontrada: " + id));

        if (entity.getEstado() == EstadoInspeccion.CANCELADA || entity.getEstado() == EstadoInspeccion.FINALIZADA) {
            throw new BusinessException("No se puede reprogramar una inspección en estado: " + entity.getEstado());
        }

        entity.setFechaProgramada(request.getFechaProgramada());
        if (request.getHoraProgramada() != null) {
            entity.setHoraProgramada(request.getHoraProgramada());
        }
        if (request.getObservaciones() != null) {
            entity.setObservacionesGenerales(request.getObservaciones());
        }
        if (entity.getEstado() == EstadoInspeccion.BORRADOR) {
            entity.setEstado(EstadoInspeccion.PROGRAMADA);
        }

        InspeccionMercado guardada = inspeccionRepository.save(entity);

        auditoriaService.registrar("PROGRAMAR_INSPECCION_MERCADO", "mercados_inspecciones", guardada.getId(),
                "EXITO", "Inspección reprogramada para: " + guardada.getFechaProgramada());

        return toResponse(guardada);
    }

    @Transactional
    public InspeccionResponse asignarInspectores(UUID id, InspeccionAsignacionRequest request, String username) {
        InspeccionMercado entity = inspeccionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inspección no encontrada: " + id));

        if (entity.getEstado() == EstadoInspeccion.CANCELADA || entity.getEstado() == EstadoInspeccion.FINALIZADA) {
            throw new BusinessException("No se pueden asignar inspectores a una inspección " + entity.getEstado());
        }

        Usuario inspectorPrincipal = usuarioRepository.findById(request.getInspectorResponsableId())
                .orElseThrow(() -> new ResourceNotFoundException("Inspector responsable no encontrado: " + request.getInspectorResponsableId()));

        entity.setInspectorResponsable(inspectorPrincipal);
        entity.setEstado(EstadoInspeccion.ASIGNADA);

        // Limpiar participantes anteriores y reasignar
        entity.getParticipantes().clear();
        entity.getParticipantes().add(InspeccionParticipante.builder()
                .inspeccion(entity)
                .usuario(inspectorPrincipal)
                .funcion("INSPECTOR_PRINCIPAL")
                .estado("ACTIVO")
                .build());

        if (request.getParticipantesAdicionales() != null) {
            for (InspeccionAsignacionRequest.ParticipanteAsignacionDto adic : request.getParticipantesAdicionales()) {
                if (!adic.getUsuarioId().equals(inspectorPrincipal.getId())) {
                    Usuario u = usuarioRepository.findById(adic.getUsuarioId()).orElse(null);
                    if (u != null) {
                        entity.getParticipantes().add(InspeccionParticipante.builder()
                                .inspeccion(entity)
                                .usuario(u)
                                .funcion(adic.getFuncion() != null ? adic.getFuncion() : "INSPECTOR_APOYO")
                                .estado("ACTIVO")
                                .build());
                    }
                }
            }
        }

        InspeccionMercado guardada = inspeccionRepository.save(entity);

        auditoriaService.registrar("ASIGNAR_INSPECTORES_MERCADO", "mercados_inspecciones", guardada.getId(),
                "EXITO", "Inspector responsable asignado: " + inspectorPrincipal.getUsername());

        return toResponse(guardada);
    }

    @Transactional
    public InspeccionResponse iniciarInspeccion(UUID id, InspeccionInicioRequest request, String username) {
        InspeccionMercado entity = inspeccionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inspección no encontrada: " + id));

        if (entity.getEstado() == EstadoInspeccion.CANCELADA || entity.getEstado() == EstadoInspeccion.FINALIZADA) {
            throw new BusinessException("No se puede iniciar una inspección en estado: " + entity.getEstado());
        }

        if (entity.getEstado() == EstadoInspeccion.EN_PROCESO) {
            throw new BusinessException("La inspección ya se encuentra en ejecución.");
        }

        entity.setEstado(EstadoInspeccion.EN_PROCESO);
        entity.setFechaInicioReal(request != null && request.getFechaInicio() != null ? request.getFechaInicio() : OffsetDateTime.now());
        if (request != null && request.getObservaciones() != null) {
            entity.setObservacionesGenerales(request.getObservaciones());
        }

        InspeccionMercado guardada = inspeccionRepository.save(entity);

        auditoriaService.registrar("INICIAR_INSPECCION_MERCADO", "mercados_inspecciones", guardada.getId(),
                "EXITO", "Inspección iniciada in situ: " + guardada.getCodigoInspeccion());

        return toResponse(guardada);
    }

    @Transactional
    public InspeccionResponse finalizarInspeccion(UUID id, String resultadoGeneral, String observaciones, String username) {
        InspeccionMercado entity = inspeccionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inspección no encontrada: " + id));

        if (entity.getEstado() != EstadoInspeccion.EN_PROCESO && entity.getEstado() != EstadoInspeccion.PENDIENTE_REVISION) {
            throw new BusinessException("Para finalizar la inspección debe encontrarse en estado EN_PROCESO o PENDIENTE_REVISION.");
        }

        entity.setFechaFinReal(OffsetDateTime.now());
        entity.setResultadoGeneral(resultadoGeneral != null ? resultadoGeneral : "CONFORME");
        if (observaciones != null) {
            entity.setObservacionesGenerales(observaciones);
        }
        entity.setEstado(EstadoInspeccion.PENDIENTE_REVISION);

        InspeccionMercado guardada = inspeccionRepository.save(entity);

        auditoriaService.registrar("FINALIZAR_INSPECCION_MERCADO", "mercados_inspecciones", guardada.getId(),
                "EXITO", "Inspección concluida in situ, pasa a revisión: " + guardada.getCodigoInspeccion());

        return toResponse(guardada);
    }

    @Transactional
    public InspeccionResponse cancelarInspeccion(UUID id, String motivoCancelacion, String username) {
        if (motivoCancelacion == null || motivoCancelacion.trim().isEmpty()) {
            throw new BusinessException("El motivo de cancelación es obligatorio.");
        }

        InspeccionMercado entity = inspeccionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inspección no encontrada: " + id));

        if (entity.getEstado() == EstadoInspeccion.FINALIZADA) {
            throw new BusinessException("No se puede cancelar una inspección que ya ha sido FINALIZADA con acta.");
        }

        entity.setEstado(EstadoInspeccion.CANCELADA);
        entity.setMotivoCancelacion(motivoCancelacion.trim());

        InspeccionMercado guardada = inspeccionRepository.save(entity);

        auditoriaService.registrar("CANCELAR_INSPECCION_MERCADO", "mercados_inspecciones", guardada.getId(),
                "EXITO", "Inspección cancelada: " + motivoCancelacion);

        return toResponse(guardada);
    }

    @Transactional
    public InspeccionResponse crearInspeccionSeguimiento(UUID inspeccionOrigenId, LocalDate fechaProgramada, String username) {
        InspeccionMercado origen = inspeccionRepository.findById(inspeccionOrigenId)
                .orElseThrow(() -> new ResourceNotFoundException("Inspección origen no encontrada: " + inspeccionOrigenId));

        InspeccionCreateRequest request = InspeccionCreateRequest.builder()
                .tipoInspeccion(TipoInspeccionMercado.SEGUIMIENTO)
                .mercadoId(origen.getMercado().getId())
                .sectorId(origen.getSector() != null ? origen.getSector().getId() : null)
                .pabellonId(origen.getPabellon() != null ? origen.getPabellon().getId() : null)
                .puestoId(origen.getPuesto() != null ? origen.getPuesto().getId() : null)
                .comercianteId(origen.getComerciante() != null ? origen.getComerciante().getId() : null)
                .inspeccionOrigenId(origen.getId())
                .plantillaId(origen.getPlantilla() != null ? origen.getPlantilla().getId() : null)
                .fechaProgramada(fechaProgramada != null ? fechaProgramada : LocalDate.now().plusDays(7))
                .motivo("Seguimiento y verificación de subsanación de medidas correctivas de la inspección: " + origen.getCodigoInspeccion())
                .objetivo("Constatar la subsanación oportuna de los hallazgos observados en el acta.")
                .inspectorResponsableId(origen.getInspectorResponsable() != null ? origen.getInspectorResponsable().getId() : null)
                .oficinaResponsableId(origen.getOficinaResponsable() != null ? origen.getOficinaResponsable().getId() : null)
                .build();

        return crear(request, username);
    }

    @Transactional(readOnly = true)
    public InspeccionResponse obtenerPorId(UUID id) {
        return inspeccionRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Inspección no encontrada: " + id));
    }

    @Transactional(readOnly = true)
    public InspeccionDetailResponse obtenerDetalle(UUID id) {
        InspeccionMercado entity = inspeccionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inspección no encontrada: " + id));

        InspeccionResponse base = toResponse(entity);

        List<InspeccionDetailResponse.ParticipanteDto> partDtos = entity.getParticipantes().stream()
                .map(p -> InspeccionDetailResponse.ParticipanteDto.builder()
                        .usuarioId(p.getUsuario().getId())
                        .usuarioNombre(p.getUsuario().getPersona() != null ? p.getUsuario().getPersona().getNombreCompleto() : p.getUsuario().getUsername())
                        .usuarioUsername(p.getUsuario().getUsername())
                        .funcion(p.getFuncion())
                        .estado(p.getEstado())
                        .build())
                .collect(Collectors.toList());

        List<RespuestaVerificacionDto> verifDtos = entity.getVerificaciones().stream()
                .map(v -> RespuestaVerificacionDto.builder()
                        .id(v.getId())
                        .itemVerificacionId(v.getItemVerificacion().getId())
                        .itemCodigo(v.getItemVerificacion().getCodigo())
                        .itemCategoria(v.getItemVerificacion().getCategoria())
                        .itemDescripcion(v.getItemVerificacion().getDescripcion())
                        .itemObligatorio(v.getItemVerificacion().getObligatorio())
                        .itemReferenciaNormativa(v.getItemVerificacion().getReferenciaNormativa())
                        .resultado(v.getResultado())
                        .resultadoDescripcion(v.getResultado() != null ? v.getResultado().getDescripcion() : null)
                        .observaciones(v.getObservaciones())
                        .fechaRegistro(v.getFechaRegistro())
                        .inspectorRegistroNombre(v.getInspectorRegistro() != null ? v.getInspectorRegistro().getUsername() : null)
                        .build())
                .collect(Collectors.toList());

        List<HallazgoResponse> hallazgoDtos = entity.getHallazgos().stream()
                .map(h -> HallazgoResponse.builder()
                        .id(h.getId())
                        .inspeccionId(h.getInspeccion().getId())
                        .codigo(h.getCodigo())
                        .categoria(h.getCategoria())
                        .descripcion(h.getDescripcion())
                        .gravedadTecnica(h.getGravedadTecnica())
                        .gravedadTecnicaDescripcion(h.getGravedadTecnica() != null ? h.getGravedadTecnica().getDescripcion() : null)
                        .estado(h.getEstado())
                        .fechaRegistro(h.getFechaRegistro())
                        .responsableRegistroNombre(h.getResponsableRegistro() != null ? h.getResponsableRegistro().getUsername() : null)
                        .totalMedidas(h.getMedidasCorrectivas() != null ? h.getMedidasCorrectivas().size() : 0)
                        .totalEvidencias(h.getEvidencias() != null ? h.getEvidencias().size() : 0)
                        .build())
                .collect(Collectors.toList());

        List<InspeccionDetailResponse.EvidenciaDto> evidDtos = entity.getEvidencias().stream()
                .map(e -> InspeccionDetailResponse.EvidenciaDto.builder()
                        .id(e.getId().toString())
                        .hallazgoId(e.getHallazgo() != null ? e.getHallazgo().getId().toString() : null)
                        .tipoEvidencia(e.getTipoEvidencia())
                        .nombreArchivo(e.getNombreArchivo())
                        .contentType(e.getContentType())
                        .tamanioBytes(e.getTamanioBytes())
                        .rutaAlmacenamiento(e.getRutaAlmacenamiento())
                        .hashSha256(e.getHashSha256())
                        .descripcion(e.getDescripcion())
                        .fechaSubida(e.getFechaSubida().toString())
                        .usuarioSubidaNombre(e.getUsuarioSubida() != null ? e.getUsuarioSubida().getUsername() : null)
                        .build())
                .collect(Collectors.toList());

        List<MedidaCorrectivaResponse> medidasDtos = new ArrayList<>();
        if (entity.getHallazgos() != null) {
            for (HallazgoInspeccionMercado h : entity.getHallazgos()) {
                if (h.getMedidasCorrectivas() != null) {
                    for (MedidaCorrectivaMercado m : h.getMedidasCorrectivas()) {
                        medidasDtos.add(toMedidaResponse(m));
                    }
                }
            }
        }

        ActaInspeccionResponse actaDto = null;
        if (entity.getActa() != null) {
            actaDto = toActaResponse(entity.getActa());
        }

        return InspeccionDetailResponse.builder()
                .inspeccion(base)
                .participantes(partDtos)
                .verificaciones(verifDtos)
                .hallazgos(hallazgoDtos)
                .evidencias(evidDtos)
                .medidasCorrectivas(medidasDtos)
                .acta(actaDto)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<InspeccionResponse> buscarPaginado(
            UUID mercadoId, UUID sectorId, UUID pabellonId, UUID puestoId, UUID comercianteId,
            TipoInspeccionMercado tipo, EstadoInspeccion estado, Long inspectorId,
            LocalDate fechaDesde, LocalDate fechaHasta, String q, Pageable pageable) {
        String qNorm = (q != null && !q.trim().isEmpty()) ? "%" + q.trim().toLowerCase() + "%" : null;
        return inspeccionRepository.buscarPaginado(mercadoId, sectorId, pabellonId, puestoId, comercianteId,
                tipo, estado, inspectorId, fechaDesde, fechaHasta, qNorm, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<InspeccionResponse> listarCalendario(LocalDate desde, LocalDate hasta) {
        return inspeccionRepository.findByFechaProgramadaBetweenOrderByFechaProgramadaAscHoraProgramadaAsc(desde, hasta).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InspeccionResponse> listarPorPuesto(UUID puestoId) {
        return inspeccionRepository.findByPuestoIdOrderByFechaProgramadaDesc(puestoId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InspeccionResponse> listarPorComerciante(UUID comercianteId) {
        return inspeccionRepository.findByComercianteIdOrderByFechaProgramadaDesc(comercianteId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public InspeccionResponse toResponse(InspeccionMercado i) {
        return InspeccionResponse.builder()
                .id(i.getId())
                .codigoInspeccion(i.getCodigoInspeccion())
                .tipoInspeccion(i.getTipoInspeccion())
                .tipoInspeccionDescripcion(i.getTipoInspeccion() != null ? i.getTipoInspeccion().getDescripcion() : null)
                .mercadoId(i.getMercado() != null ? i.getMercado().getId() : null)
                .mercadoNombre(i.getMercado() != null ? i.getMercado().getNombre() : null)
                .mercadoCodigo(i.getMercado() != null ? i.getMercado().getCodigo() : null)
                .sectorId(i.getSector() != null ? i.getSector().getId() : null)
                .sectorNombre(i.getSector() != null ? i.getSector().getNombre() : null)
                .pabellonId(i.getPabellon() != null ? i.getPabellon().getId() : null)
                .pabellonNombre(i.getPabellon() != null ? i.getPabellon().getNombre() : null)
                .puestoId(i.getPuesto() != null ? i.getPuesto().getId() : null)
                .puestoCodigo(i.getPuesto() != null ? i.getPuesto().getCodigo() : null)
                .puestoNumero(i.getPuesto() != null ? i.getPuesto().getNumero() : null)
                .comercianteId(i.getComerciante() != null ? i.getComerciante().getId() : null)
                .comercianteNombre(i.getComerciante() != null && i.getComerciante().getPersona() != null ? i.getComerciante().getPersona().getNombreCompleto() : null)
                .comercianteDocumento(i.getComerciante() != null && i.getComerciante().getPersona() != null ? i.getComerciante().getPersona().getNumeroDocumento() : null)
                .inspeccionOrigenId(i.getInspeccionOrigen() != null ? i.getInspeccionOrigen().getId() : null)
                .inspeccionOrigenCodigo(i.getInspeccionOrigen() != null ? i.getInspeccionOrigen().getCodigoInspeccion() : null)
                .plantillaId(i.getPlantilla() != null ? i.getPlantilla().getId() : null)
                .plantillaNombre(i.getPlantilla() != null ? i.getPlantilla().getNombre() : null)
                .fechaProgramada(i.getFechaProgramada())
                .horaProgramada(i.getHoraProgramada())
                .fechaInicioReal(i.getFechaInicioReal())
                .fechaFinReal(i.getFechaFinReal())
                .estado(i.getEstado())
                .estadoDescripcion(i.getEstado() != null ? i.getEstado().getDescripcion() : null)
                .motivo(i.getMotivo())
                .objetivo(i.getObjetivo())
                .oficinaResponsableId(i.getOficinaResponsable() != null ? i.getOficinaResponsable().getId() : null)
                .oficinaResponsableNombre(i.getOficinaResponsable() != null ? i.getOficinaResponsable().getNombre() : null)
                .inspectorResponsableId(i.getInspectorResponsable() != null ? i.getInspectorResponsable().getId() : null)
                .inspectorResponsableNombre(i.getInspectorResponsable() != null && i.getInspectorResponsable().getPersona() != null ?
                        i.getInspectorResponsable().getPersona().getNombreCompleto() : (i.getInspectorResponsable() != null ? i.getInspectorResponsable().getUsername() : null))
                .resultadoGeneral(i.getResultadoGeneral())
                .motivoCancelacion(i.getMotivoCancelacion())
                .totalHallazgos(i.getHallazgos() != null ? i.getHallazgos().size() : 0)
                .totalMedidas(i.getHallazgos() != null ? i.getHallazgos().stream().mapToInt(h -> h.getMedidasCorrectivas() != null ? h.getMedidasCorrectivas().size() : 0).sum() : 0)
                .tieneActa(i.getActa() != null)
                .codigoActa(i.getActa() != null ? i.getActa().getCodigoActa() : null)
                .createdAt(i.getCreatedAt())
                .build();
    }

    private MedidaCorrectivaResponse toMedidaResponse(MedidaCorrectivaMercado m) {
        boolean vencida = m.getFechaLimite() != null && m.getFechaLimite().isBefore(LocalDate.now()) &&
                (m.getEstado() == pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoMedidaCorrectiva.PENDIENTE ||
                 m.getEstado() == pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoMedidaCorrectiva.EN_PROCESO);

        return MedidaCorrectivaResponse.builder()
                .id(m.getId())
                .hallazgoId(m.getHallazgo().getId())
                .hallazgoCodigo(m.getHallazgo().getCodigo())
                .hallazgoDescripcion(m.getHallazgo().getDescripcion())
                .inspeccionId(m.getHallazgo().getInspeccion().getId())
                .inspeccionCodigo(m.getHallazgo().getInspeccion().getCodigoInspeccion())
                .mercadoNombre(m.getHallazgo().getInspeccion().getMercado() != null ? m.getHallazgo().getInspeccion().getMercado().getNombre() : null)
                .puestoCodigo(m.getHallazgo().getInspeccion().getPuesto() != null ? m.getHallazgo().getInspeccion().getPuesto().getCodigo() : null)
                .descripcion(m.getDescripcion())
                .tipoResponsable(m.getTipoResponsable())
                .responsableId(m.getResponsable() != null ? m.getResponsable().getId() : null)
                .responsableNombre(m.getResponsable() != null ? m.getResponsable().getUsername() : null)
                .comercianteResponsableId(m.getComercianteResponsable() != null ? m.getComercianteResponsable().getId() : null)
                .comercianteResponsableNombre(m.getComercianteResponsable() != null && m.getComercianteResponsable().getPersona() != null ?
                        m.getComercianteResponsable().getPersona().getNombreCompleto() : null)
                .fechaAsignacion(m.getFechaAsignacion())
                .fechaLimite(m.getFechaLimite())
                .estado(m.getEstado())
                .estadoDescripcion(m.getEstado() != null ? m.getEstado().getDescripcion() : null)
                .vencida(vencida)
                .observaciones(m.getObservaciones())
                .createdAt(m.getCreatedAt())
                .build();
    }

    private ActaInspeccionResponse toActaResponse(ActaInspeccionMercado a) {
        return ActaInspeccionResponse.builder()
                .id(a.getId())
                .inspeccionId(a.getInspeccion().getId())
                .codigoInspeccion(a.getInspeccion().getCodigoInspeccion())
                .codigoActa(a.getCodigoActa())
                .fechaElaboracion(a.getFechaElaboracion())
                .resumen(a.getResumen())
                .conclusiones(a.getConclusiones())
                .recomendaciones(a.getRecomendaciones())
                .estado(a.getEstado())
                .estadoDescripcion(a.getEstado() != null ? a.getEstado().getDescripcion() : null)
                .codigoVerificacion(a.getCodigoVerificacion())
                .hashSha256(a.getHashSha256())
                .responsableId(a.getResponsable().getId())
                .responsableNombre(a.getResponsable().getUsername())
                .build();
    }
}
