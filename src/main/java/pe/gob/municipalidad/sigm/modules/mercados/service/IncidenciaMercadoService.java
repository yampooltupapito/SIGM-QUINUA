package pe.gob.municipalidad.sigm.modules.mercados.service;

import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoIncidencia;
import pe.gob.municipalidad.sigm.modules.mercados.enums.OrigenIncidencia;
import pe.gob.municipalidad.sigm.modules.mercados.enums.PrioridadIncidencia;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoActuacionIncidencia;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;

import java.time.OffsetDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;

@Slf4j
@Service
public class IncidenciaMercadoService {

    private final IncidenciaMercadoRepository incidenciaRepository;
    private final TipoIncidenciaMercadoRepository tipoRepository;
    private final ActuacionIncidenciaRepository actuacionRepository;
    private final EvidenciaIncidenciaMercadoRepository evidenciaRepository;
    private final PresuntaInfraccionMercadoRepository infraccionRepository;
    private final MercadoMunicipalRepository mercadoRepository;
    private final SectorMercadoRepository sectorRepository;
    private final PabellonMercadoRepository pabellonRepository;
    private final PuestoComercialRepository puestoRepository;
    private final ComercianteRepository comercianteRepository;
    private final InspeccionMercadoRepository inspeccionRepository;
    private final OficinaRepository oficinaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaAdministrativaService auditoriaService;

    public IncidenciaMercadoService(
            IncidenciaMercadoRepository incidenciaRepository,
            TipoIncidenciaMercadoRepository tipoRepository,
            ActuacionIncidenciaRepository actuacionRepository,
            EvidenciaIncidenciaMercadoRepository evidenciaRepository,
            PresuntaInfraccionMercadoRepository infraccionRepository,
            MercadoMunicipalRepository mercadoRepository,
            SectorMercadoRepository sectorRepository,
            PabellonMercadoRepository pabellonRepository,
            PuestoComercialRepository puestoRepository,
            ComercianteRepository comercianteRepository,
            InspeccionMercadoRepository inspeccionRepository,
            OficinaRepository oficinaRepository,
            UsuarioRepository usuarioRepository,
            AuditoriaAdministrativaService auditoriaService) {
        this.incidenciaRepository = incidenciaRepository;
        this.tipoRepository = tipoRepository;
        this.actuacionRepository = actuacionRepository;
        this.evidenciaRepository = evidenciaRepository;
        this.infraccionRepository = infraccionRepository;
        this.mercadoRepository = mercadoRepository;
        this.sectorRepository = sectorRepository;
        this.pabellonRepository = pabellonRepository;
        this.puestoRepository = puestoRepository;
        this.comercianteRepository = comercianteRepository;
        this.inspeccionRepository = inspeccionRepository;
        this.oficinaRepository = oficinaRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public IncidenciaResponse registrar(IncidenciaCreateRequest request, String username) {
        log.info("Registrando nueva incidencia en mercado ID: {} por usuario: {}", request.getMercadoId(), username);

        MercadoMunicipal mercado = mercadoRepository.findById(request.getMercadoId())
                .orElseThrow(() -> new ResourceNotFoundException("Mercado municipal no encontrado: " + request.getMercadoId()));

        SectorMercado sector = null;
        if (request.getSectorId() != null) {
            sector = sectorRepository.findById(request.getSectorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sector no encontrado: " + request.getSectorId()));
            if (!sector.getMercado().getId().equals(mercado.getId())) {
                throw new BusinessException("El sector no pertenece al mercado indicado: " + mercado.getNombre());
            }
        }

        PabellonMercado pabellon = null;
        if (request.getPabellonId() != null) {
            pabellon = pabellonRepository.findById(request.getPabellonId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pabellón no encontrado: " + request.getPabellonId()));
            if (!pabellon.getMercado().getId().equals(mercado.getId())) {
                throw new BusinessException("El pabellón no pertenece al mercado indicado: " + mercado.getNombre());
            }
            if (sector != null && pabellon.getSector() != null && !pabellon.getSector().getId().equals(sector.getId())) {
                throw new BusinessException("El pabellón no pertenece al sector indicado: " + sector.getNombre());
            }
        }

        PuestoComercial puesto = null;
        if (request.getPuestoId() != null) {
            puesto = puestoRepository.findById(request.getPuestoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Puesto no encontrado: " + request.getPuestoId()));
            if (!puesto.getMercado().getId().equals(mercado.getId())) {
                throw new BusinessException("El puesto no pertenece al mercado indicado: " + mercado.getNombre());
            }
            if (sector != null && puesto.getSector() != null && !puesto.getSector().getId().equals(sector.getId())) {
                throw new BusinessException("El puesto no pertenece al sector indicado: " + sector.getNombre());
            }
            if (pabellon != null && puesto.getPabellon() != null && !puesto.getPabellon().getId().equals(pabellon.getId())) {
                throw new BusinessException("El puesto no pertenece al pabellón indicado: " + pabellon.getNombre());
            }
        }

        Comerciante comerciante = null;
        if (request.getComercianteId() != null) {
            comerciante = comercianteRepository.findById(request.getComercianteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Comerciante no encontrado: " + request.getComercianteId()));
        }

        TipoIncidenciaMercado tipo = tipoRepository.findById(request.getTipoIncidenciaId())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de incidencia no encontrado: " + request.getTipoIncidenciaId()));

        if (Boolean.TRUE.equals(tipo.getRequierePuesto()) && puesto == null) {
            throw new BusinessException("El tipo de incidencia '" + tipo.getNombre() + "' requiere obligatoriamente asociar un puesto comercial.");
        }

        Usuario reportanteUser = null;
        if (username != null) {
            reportanteUser = usuarioRepository.findByUsername(username).orElse(null);
        }

        Usuario responsable = null;
        if (request.getResponsableId() != null) {
            responsable = usuarioRepository.findById(request.getResponsableId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario responsable no encontrado: " + request.getResponsableId()));
        }

        Oficina oficina = null;
        if (request.getOficinaResponsableId() != null) {
            oficina = oficinaRepository.findById(request.getOficinaResponsableId())
                    .orElseThrow(() -> new ResourceNotFoundException("Oficina no encontrada: " + request.getOficinaResponsableId()));
        }

        InspeccionMercado inspeccion = null;
        if (request.getInspeccionId() != null) {
            inspeccion = inspeccionRepository.findById(request.getInspeccionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inspección no encontrada: " + request.getInspeccionId()));
        }

        PrioridadIncidencia prioridadFinal = request.getPrioridad() != null ? request.getPrioridad() : tipo.getPrioridadSugerida();
        EstadoIncidencia estadoInicial = responsable != null ? EstadoIncidencia.ASIGNADA : EstadoIncidencia.REGISTRADA;

        Long correlativo = incidenciaRepository.obtenerSiguienteCorrelativo();
        int anio = Year.now().getValue();
        String codigo = String.format("INC-MER-%d-%06d", anio, correlativo);

        IncidenciaMercado incidencia = IncidenciaMercado.builder()
                .codigoIncidencia(codigo)
                .mercado(mercado)
                .sector(sector)
                .pabellon(pabellon)
                .puesto(puesto)
                .comerciante(comerciante)
                .tipoIncidencia(tipo)
                .origen(request.getOrigen())
                .descripcion(request.getDescripcion().trim())
                .fechaReporte(OffsetDateTime.now())
                .fechaOcurrencia(request.getFechaOcurrencia())
                .prioridad(prioridadFinal)
                .estado(estadoInicial)
                .reportanteNombre(Boolean.TRUE.equals(request.getEsAnonimo()) ? "Anónimo (Identidad Reservada)" : request.getReportanteNombre())
                .reportanteContacto(Boolean.TRUE.equals(request.getEsAnonimo()) ? null : request.getReportanteContacto())
                .esAnonimo(Boolean.TRUE.equals(request.getEsAnonimo()))
                .reportanteUsuario(reportanteUser)
                .responsable(responsable)
                .oficinaResponsable(oficina)
                .inspeccion(inspeccion)
                .expedienteId(request.getExpedienteId())
                .expedienteNumero(request.getExpedienteNumero())
                .observaciones(request.getObservaciones())
                .actuaciones(new ArrayList<>())
                .evidencias(new ArrayList<>())
                .build();

        IncidenciaMercado guardada = incidenciaRepository.save(incidencia);

        // Actuación inicial automática
        Usuario usuarioActuacion = reportanteUser != null ? reportanteUser : responsable;
        if (usuarioActuacion == null) {
            usuarioActuacion = usuarioRepository.findByUsername(username).orElse(null);
        }
        if (usuarioActuacion == null) {
            usuarioActuacion = usuarioRepository.findAll().stream().findFirst().orElse(null);
        }

        ActuacionIncidenciaMercado actuacionInicial = ActuacionIncidenciaMercado.builder()
                .incidencia(guardada)
                .tipoActuacion(TipoActuacionIncidencia.REGISTRO)
                .descripcion("Registro inicial de la incidencia con origen: " + request.getOrigen().getLabel())
                .fecha(OffsetDateTime.now())
                .responsable(usuarioActuacion)
                .estadoAnterior(null)
                .estadoNuevo(estadoInicial)
                .build();
        actuacionRepository.save(actuacionInicial);
        guardada.getActuaciones().add(actuacionInicial);

        auditoriaService.registrar("REGISTRAR_INCIDENCIA_MERCADO", "mercados_incidencias", guardada.getId(),
                "EXITO", "Incidencia registrada: " + guardada.getCodigoIncidencia());

        return toResponse(guardada);
    }

    @Transactional
    public IncidenciaResponse asignarResponsable(UUID id, IncidenciaAsignacionRequest request, String username) {
        IncidenciaMercado incidencia = incidenciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incidencia no encontrada: " + id));

        if (incidencia.getEstado() == EstadoIncidencia.CERRADA || incidencia.getEstado() == EstadoIncidencia.DESCARTADA || incidencia.getEstado() == EstadoIncidencia.RESUELTA) {
            throw new BusinessException("No se puede asignar responsable a una incidencia en estado: " + incidencia.getEstado());
        }

        Usuario responsable = usuarioRepository.findById(request.getResponsableId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario responsable no encontrado: " + request.getResponsableId()));

        Oficina oficina = null;
        if (request.getOficinaResponsableId() != null) {
            oficina = oficinaRepository.findById(request.getOficinaResponsableId())
                    .orElseThrow(() -> new ResourceNotFoundException("Oficina no encontrada: " + request.getOficinaResponsableId()));
            incidencia.setOficinaResponsable(oficina);
        }

        Usuario operador = resolverUsuario(username);

        EstadoIncidencia estadoAnterior = incidencia.getEstado();
        incidencia.setResponsable(responsable);
        if (request.getPrioridad() != null) {
            incidencia.setPrioridad(request.getPrioridad());
        }
        if (request.getObservaciones() != null) {
            incidencia.setObservaciones(request.getObservaciones());
        }

        if (incidencia.getEstado() == EstadoIncidencia.REGISTRADA || incidencia.getEstado() == EstadoIncidencia.EN_REVISION) {
            incidencia.setEstado(EstadoIncidencia.ASIGNADA);
        }

        IncidenciaMercado guardada = incidenciaRepository.save(incidencia);

        // Registro de actuación
        ActuacionIncidenciaMercado actuacion = ActuacionIncidenciaMercado.builder()
                .incidencia(guardada)
                .tipoActuacion(TipoActuacionIncidencia.ASIGNACION)
                .descripcion("Asignado a: " + responsable.getUsername() + " (" + (oficina != null ? oficina.getNombre() : "Oficina Asignada") + "). " + (request.getObservaciones() != null ? request.getObservaciones() : ""))
                .fecha(OffsetDateTime.now())
                .responsable(operador)
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(guardada.getEstado())
                .build();
        actuacionRepository.save(actuacion);

        auditoriaService.registrar("ASIGNAR_RESPONSABLE_INCIDENCIA", "mercados_incidencias", guardada.getId(),
                "EXITO", "Asignado responsable " + responsable.getUsername() + " a incidencia: " + guardada.getCodigoIncidencia());

        return toResponse(guardada);
    }

    @Transactional
    public IncidenciaResponse registrarActuacion(UUID id, IncidenciaActuacionRequest request, String username) {
        IncidenciaMercado incidencia = incidenciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incidencia no encontrada: " + id));

        if (incidencia.getEstado() == EstadoIncidencia.CERRADA || incidencia.getEstado() == EstadoIncidencia.DESCARTADA) {
            throw new BusinessException("No se pueden registrar actuaciones en una incidencia cerrada o descartada.");
        }

        Usuario operador = resolverUsuario(username);

        EstadoIncidencia estadoAnterior = incidencia.getEstado();
        if (request.getNuevoEstado() != null) {
            incidencia.setEstado(request.getNuevoEstado());
        } else if (incidencia.getEstado() == EstadoIncidencia.ASIGNADA) {
            incidencia.setEstado(EstadoIncidencia.EN_ATENCION);
        }

        IncidenciaMercado guardada = incidenciaRepository.save(incidencia);

        ActuacionIncidenciaMercado actuacion = ActuacionIncidenciaMercado.builder()
                .incidencia(guardada)
                .tipoActuacion(request.getTipoActuacion())
                .descripcion(request.getDescripcion().trim())
                .fecha(OffsetDateTime.now())
                .responsable(operador)
                .documentoId(request.getDocumentoId())
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(guardada.getEstado())
                .build();
        actuacionRepository.save(actuacion);

        auditoriaService.registrar("REGISTRAR_ACTUACION_INCIDENCIA", "mercados_incidencias", guardada.getId(),
                "EXITO", "Actuación registrada en incidencia: " + guardada.getCodigoIncidencia());

        return toResponse(guardada);
    }

    @Transactional
    public EvidenciaIncidenciaDto adjuntarEvidencia(
            UUID id, String tipoEvidencia, String nombreArchivo, String rutaArchivo,
            String contentType, Long tamanoBytes, String hashSha256, String descripcion, String username) {
        IncidenciaMercado incidencia = incidenciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incidencia no encontrada: " + id));

        Usuario operador = resolverUsuario(username);

        EvidenciaIncidenciaMercado ev = EvidenciaIncidenciaMercado.builder()
                .incidencia(incidencia)
                .tipoEvidencia(tipoEvidencia != null ? tipoEvidencia : "FOTOGRAFIA")
                .nombreArchivo(nombreArchivo)
                .rutaArchivo(rutaArchivo)
                .contentType(contentType)
                .tamanoBytes(tamanoBytes)
                .hashSha256(hashSha256)
                .descripcion(descripcion)
                .subidoPor(operador)
                .build();

        EvidenciaIncidenciaMercado guardada = evidenciaRepository.save(ev);

        auditoriaService.registrar("ADJUNTAR_EVIDENCIA_INCIDENCIA", "mercados_incidencias_evidencias", guardada.getId(),
                "EXITO", "Evidencia adjunta a incidencia " + incidencia.getCodigoIncidencia() + ": " + nombreArchivo);

        return EvidenciaIncidenciaDto.builder()
                .id(guardada.getId())
                .tipoEvidencia(guardada.getTipoEvidencia())
                .nombreArchivo(guardada.getNombreArchivo())
                .rutaArchivo(guardada.getRutaArchivo())
                .contentType(guardada.getContentType())
                .tamanoBytes(guardada.getTamanoBytes())
                .hashSha256(guardada.getHashSha256())
                .descripcion(guardada.getDescripcion())
                .fechaRegistro(guardada.getFechaRegistro())
                .subidoPorId(operador.getId())
                .subidoPorNombre(operador.getUsername())
                .build();
    }

    @Transactional
    public EvidenciaIncidenciaDto adjuntarEvidenciaArchivo(UUID id, MultipartFile archivo, String tipoEvidencia, String descripcion, String username) {
        if (archivo == null || archivo.isEmpty()) {
            throw new BusinessException("El archivo de evidencia es obligatorio.");
        }
        if (archivo.getSize() > 15 * 1024 * 1024) {
            throw new BusinessException("El archivo excede el tamaño máximo permitido de 15 MB.");
        }
        String originalFilename = archivo.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new BusinessException("Nombre de archivo inválido.");
        }

        IncidenciaMercado incidencia = incidenciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incidencia no encontrada: " + id));

        Usuario operador = resolverUsuario(username);

        try {
            byte[] bytes = archivo.getBytes();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(bytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            String hashSha256 = sb.toString();

            String baseDir = System.getProperty("user.home") + "/.sigm_quinua/evidencias_incidencias";
            Path uploadPath = Paths.get(baseDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String savedFileName = UUID.randomUUID() + "_" + originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path targetFile = uploadPath.resolve(savedFileName);
            Files.copy(archivo.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);

            EvidenciaIncidenciaMercado ev = EvidenciaIncidenciaMercado.builder()
                    .incidencia(incidencia)
                    .tipoEvidencia(tipoEvidencia != null ? tipoEvidencia : "FOTOGRAFIA")
                    .nombreArchivo(originalFilename)
                    .rutaArchivo(targetFile.toAbsolutePath().toString())
                    .contentType(archivo.getContentType() != null ? archivo.getContentType() : "application/octet-stream")
                    .tamanoBytes(archivo.getSize())
                    .hashSha256(hashSha256)
                    .descripcion(descripcion)
                    .subidoPor(operador)
                    .build();

            EvidenciaIncidenciaMercado guardada = evidenciaRepository.save(ev);
            auditoriaService.registrar("ADJUNTAR_EVIDENCIA_INCIDENCIA", "mercados_incidencias_evidencias", guardada.getId(),
                    "EXITO", "Evidencia adjunta a incidencia " + incidencia.getCodigoIncidencia() + ": " + originalFilename);

            return EvidenciaIncidenciaDto.builder()
                    .id(guardada.getId())
                    .tipoEvidencia(guardada.getTipoEvidencia())
                    .nombreArchivo(guardada.getNombreArchivo())
                    .rutaArchivo(guardada.getRutaArchivo())
                    .contentType(guardada.getContentType())
                    .tamanoBytes(guardada.getTamanoBytes())
                    .hashSha256(guardada.getHashSha256())
                    .descripcion(guardada.getDescripcion())
                    .fechaRegistro(guardada.getFechaRegistro())
                    .subidoPorId(operador.getId())
                    .subidoPorNombre(operador.getUsername())
                    .build();
        } catch (Exception e) {
            log.error("Error al almacenar evidencia de incidencia", e);
            throw new BusinessException("Error al almacenar el archivo de evidencia: " + e.getMessage());
        }
    }

    @Transactional
    public IncidenciaResponse resolver(UUID id, IncidenciaResolucionRequest request, String username) {
        IncidenciaMercado incidencia = incidenciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incidencia no encontrada: " + id));

        if (incidencia.getEstado() == EstadoIncidencia.CERRADA || incidencia.getEstado() == EstadoIncidencia.DESCARTADA) {
            throw new BusinessException("No se puede resolver una incidencia en estado: " + incidencia.getEstado());
        }

        Usuario operador = resolverUsuario(username);

        EstadoIncidencia anterior = incidencia.getEstado();
        incidencia.setEstado(EstadoIncidencia.RESUELTA);
        incidencia.setFechaResolucion(OffsetDateTime.now());
        incidencia.setMotivoResolucion(request.getMotivoResolucion().trim());

        IncidenciaMercado guardada = incidenciaRepository.save(incidencia);

        ActuacionIncidenciaMercado actuacion = ActuacionIncidenciaMercado.builder()
                .incidencia(guardada)
                .tipoActuacion(TipoActuacionIncidencia.RESOLUCION)
                .descripcion("Incidencia resuelta. Medidas adoptadas: " + request.getMotivoResolucion().trim())
                .fecha(OffsetDateTime.now())
                .responsable(operador)
                .documentoId(request.getDocumentoId())
                .estadoAnterior(anterior)
                .estadoNuevo(EstadoIncidencia.RESUELTA)
                .build();
        actuacionRepository.save(actuacion);

        auditoriaService.registrar("RESOLVER_INCIDENCIA_MERCADO", "mercados_incidencias", guardada.getId(),
                "EXITO", "Incidencia resuelta: " + guardada.getCodigoIncidencia());

        return toResponse(guardada);
    }

    @Transactional
    public IncidenciaResponse cerrar(UUID id, IncidenciaCierreRequest request, String username) {
        IncidenciaMercado incidencia = incidenciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incidencia no encontrada: " + id));

        if (request.getEstadoFinal() != EstadoIncidencia.CERRADA && request.getEstadoFinal() != EstadoIncidencia.DESCARTADA) {
            throw new BusinessException("El estado final debe ser CERRADA o DESCARTADA.");
        }

        Usuario operador = resolverUsuario(username);

        EstadoIncidencia anterior = incidencia.getEstado();
        incidencia.setEstado(request.getEstadoFinal());
        incidencia.setFechaCierre(OffsetDateTime.now());
        incidencia.setMotivoCierre(request.getMotivoCierre().trim());

        IncidenciaMercado guardada = incidenciaRepository.save(incidencia);

        TipoActuacionIncidencia tipoAct = request.getEstadoFinal() == EstadoIncidencia.CERRADA ? TipoActuacionIncidencia.CIERRE : TipoActuacionIncidencia.DESCARTE;
        ActuacionIncidenciaMercado actuacion = ActuacionIncidenciaMercado.builder()
                .incidencia(guardada)
                .tipoActuacion(tipoAct)
                .descripcion("Incidencia " + request.getEstadoFinal().name() + ". Motivo: " + request.getMotivoCierre().trim())
                .fecha(OffsetDateTime.now())
                .responsable(operador)
                .estadoAnterior(anterior)
                .estadoNuevo(request.getEstadoFinal())
                .build();
        actuacionRepository.save(actuacion);

        auditoriaService.registrar("CERRAR_INCIDENCIA_MERCADO", "mercados_incidencias", guardada.getId(),
                "EXITO", "Incidencia " + guardada.getCodigoIncidencia() + " en estado: " + request.getEstadoFinal());

        return toResponse(guardada);
    }

    @Transactional(readOnly = true)
    public IncidenciaDetailResponse obtenerDetalle(UUID id) {
        IncidenciaMercado i = incidenciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incidencia no encontrada: " + id));

        List<ActuacionIncidenciaDto> actuaciones = actuacionRepository.findByIncidenciaIdOrderByFechaAsc(id).stream()
                .map(a -> ActuacionIncidenciaDto.builder()
                        .id(a.getId())
                        .tipoActuacion(a.getTipoActuacion())
                        .tipoActuacionLabel(a.getTipoActuacion().getLabel())
                        .descripcion(a.getDescripcion())
                        .fecha(a.getFecha())
                        .responsableId(a.getResponsable().getId())
                        .responsableNombre(a.getResponsable().getUsername())
                        .documentoId(a.getDocumentoId())
                        .estadoAnterior(a.getEstadoAnterior())
                        .estadoNuevo(a.getEstadoNuevo())
                        .build())
                .collect(Collectors.toList());

        List<EvidenciaIncidenciaDto> evidencias = evidenciaRepository.findByIncidenciaIdOrderByFechaRegistroAsc(id).stream()
                .map(e -> EvidenciaIncidenciaDto.builder()
                        .id(e.getId())
                        .tipoEvidencia(e.getTipoEvidencia())
                        .nombreArchivo(e.getNombreArchivo())
                        .rutaArchivo(e.getRutaArchivo())
                        .contentType(e.getContentType())
                        .tamanoBytes(e.getTamanoBytes())
                        .hashSha256(e.getHashSha256())
                        .descripcion(e.getDescripcion())
                        .fechaRegistro(e.getFechaRegistro())
                        .subidoPorId(e.getSubidoPor().getId())
                        .subidoPorNombre(e.getSubidoPor().getUsername())
                        .build())
                .collect(Collectors.toList());

        List<PresuntaInfraccionResponse> infracciones = infraccionRepository.findByIncidenciaId(id).stream()
                .map(this::toInfraccionResponse)
                .collect(Collectors.toList());

        return IncidenciaDetailResponse.builder()
                .id(i.getId())
                .codigoIncidencia(i.getCodigoIncidencia())
                .mercadoId(i.getMercado().getId())
                .mercadoNombre(i.getMercado().getNombre())
                .sectorId(i.getSector() != null ? i.getSector().getId() : null)
                .sectorNombre(i.getSector() != null ? i.getSector().getNombre() : null)
                .pabellonId(i.getPabellon() != null ? i.getPabellon().getId() : null)
                .pabellonNombre(i.getPabellon() != null ? i.getPabellon().getNombre() : null)
                .puestoId(i.getPuesto() != null ? i.getPuesto().getId() : null)
                .puestoNumero(i.getPuesto() != null ? i.getPuesto().getNumero() : null)
                .comercianteId(i.getComerciante() != null ? i.getComerciante().getId() : null)
                .comercianteNombre(i.getComerciante() != null && i.getComerciante().getPersona() != null ? i.getComerciante().getPersona().getNombreCompleto() : null)
                .comercianteDocumento(i.getComerciante() != null && i.getComerciante().getPersona() != null ? i.getComerciante().getPersona().getNumeroDocumento() : null)
                .tipoIncidenciaId(i.getTipoIncidencia().getId())
                .tipoIncidenciaNombre(i.getTipoIncidencia().getNombre())
                .tipoIncidenciaCodigo(i.getTipoIncidencia().getCodigo())
                .origen(i.getOrigen())
                .descripcion(i.getDescripcion())
                .fechaReporte(i.getFechaReporte())
                .fechaOcurrencia(i.getFechaOcurrencia())
                .prioridad(i.getPrioridad())
                .estado(i.getEstado())
                .reportanteNombre(i.getReportanteNombre())
                .reportanteContacto(i.getReportanteContacto())
                .esAnonimo(i.getEsAnonimo())
                .reportanteUsuarioId(i.getReportanteUsuario() != null ? i.getReportanteUsuario().getId() : null)
                .responsableId(i.getResponsable() != null ? i.getResponsable().getId() : null)
                .responsableNombre(i.getResponsable() != null ? (i.getResponsable().getPersona() != null ? i.getResponsable().getPersona().getNombreCompleto() : i.getResponsable().getUsername()) : null)
                .oficinaResponsableId(i.getOficinaResponsable() != null ? i.getOficinaResponsable().getId() : null)
                .oficinaResponsableNombre(i.getOficinaResponsable() != null ? i.getOficinaResponsable().getNombre() : null)
                .inspeccionId(i.getInspeccion() != null ? i.getInspeccion().getId() : null)
                .inspeccionCodigo(i.getInspeccion() != null ? i.getInspeccion().getCodigoInspeccion() : null)
                .expedienteId(i.getExpedienteId())
                .expedienteNumero(i.getExpedienteNumero())
                .observaciones(i.getObservaciones())
                .fechaResolucion(i.getFechaResolucion())
                .motivoResolucion(i.getMotivoResolucion())
                .fechaCierre(i.getFechaCierre())
                .motivoCierre(i.getMotivoCierre())
                .actuaciones(actuaciones)
                .evidencias(evidencias)
                .presuntasInfracciones(infracciones)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<IncidenciaResponse> buscarPaginado(
            UUID mercadoId, UUID sectorId, UUID pabellonId, UUID puestoId, UUID comercianteId,
            UUID tipoId, OrigenIncidencia origen, PrioridadIncidencia prioridad, EstadoIncidencia estado,
            Long responsableId, OffsetDateTime fechaDesde, OffsetDateTime fechaHasta, UUID inspeccionId,
            String q, Pageable pageable) {

        Specification<IncidenciaMercado> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (mercadoId != null) {
                predicates.add(cb.equal(root.get("mercado").get("id"), mercadoId));
            }
            if (sectorId != null) {
                predicates.add(cb.equal(root.get("sector").get("id"), sectorId));
            }
            if (pabellonId != null) {
                predicates.add(cb.equal(root.get("pabellon").get("id"), pabellonId));
            }
            if (puestoId != null) {
                predicates.add(cb.equal(root.get("puesto").get("id"), puestoId));
            }
            if (comercianteId != null) {
                predicates.add(cb.equal(root.get("comerciante").get("id"), comercianteId));
            }
            if (tipoId != null) {
                predicates.add(cb.equal(root.get("tipoIncidencia").get("id"), tipoId));
            }
            if (origen != null) {
                predicates.add(cb.equal(root.get("origen"), origen));
            }
            if (prioridad != null) {
                predicates.add(cb.equal(root.get("prioridad"), prioridad));
            }
            if (estado != null) {
                predicates.add(cb.equal(root.get("estado"), estado));
            }
            if (responsableId != null) {
                predicates.add(cb.equal(root.get("responsable").get("id"), responsableId));
            }
            if (fechaDesde != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaReporte"), fechaDesde));
            }
            if (fechaHasta != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaReporte"), fechaHasta));
            }
            if (inspeccionId != null) {
                predicates.add(cb.equal(root.get("inspeccion").get("id"), inspeccionId));
            }
            if (q != null && !q.trim().isEmpty()) {
                String pattern = "%" + q.trim().toLowerCase() + "%";
                Predicate porCodigo = cb.like(cb.lower(root.get("codigoIncidencia")), pattern);
                Predicate porDesc = cb.like(cb.lower(root.get("descripcion")), pattern);
                predicates.add(cb.or(porCodigo, porDesc));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return incidenciaRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<IncidenciaResponse> listarPorPuesto(UUID puestoId) {
        return incidenciaRepository.findByPuestoIdOrderByFechaReporteDesc(puestoId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<IncidenciaResponse> listarPorComerciante(UUID comercianteId) {
        return incidenciaRepository.findByComercianteIdOrderByFechaReporteDesc(comercianteId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public IncidenciaResponse toResponse(IncidenciaMercado i) {
        return IncidenciaResponse.builder()
                .id(i.getId())
                .codigoIncidencia(i.getCodigoIncidencia())
                .mercadoId(i.getMercado().getId())
                .mercadoNombre(i.getMercado().getNombre())
                .sectorId(i.getSector() != null ? i.getSector().getId() : null)
                .sectorNombre(i.getSector() != null ? i.getSector().getNombre() : null)
                .pabellonId(i.getPabellon() != null ? i.getPabellon().getId() : null)
                .pabellonNombre(i.getPabellon() != null ? i.getPabellon().getNombre() : null)
                .puestoId(i.getPuesto() != null ? i.getPuesto().getId() : null)
                .puestoNumero(i.getPuesto() != null ? i.getPuesto().getNumero() : null)
                .comercianteId(i.getComerciante() != null ? i.getComerciante().getId() : null)
                .comercianteNombre(i.getComerciante() != null && i.getComerciante().getPersona() != null ? i.getComerciante().getPersona().getNombreCompleto() : null)
                .tipoIncidenciaId(i.getTipoIncidencia().getId())
                .tipoIncidenciaNombre(i.getTipoIncidencia().getNombre())
                .tipoIncidenciaCodigo(i.getTipoIncidencia().getCodigo())
                .origen(i.getOrigen())
                .descripcion(i.getDescripcion())
                .fechaReporte(i.getFechaReporte())
                .fechaOcurrencia(i.getFechaOcurrencia())
                .prioridad(i.getPrioridad())
                .estado(i.getEstado())
                .responsableId(i.getResponsable() != null ? i.getResponsable().getId() : null)
                .responsableNombre(i.getResponsable() != null ? (i.getResponsable().getPersona() != null ? i.getResponsable().getPersona().getNombreCompleto() : i.getResponsable().getUsername()) : null)
                .oficinaResponsableId(i.getOficinaResponsable() != null ? i.getOficinaResponsable().getId() : null)
                .oficinaResponsableNombre(i.getOficinaResponsable() != null ? i.getOficinaResponsable().getNombre() : null)
                .inspeccionId(i.getInspeccion() != null ? i.getInspeccion().getId() : null)
                .inspeccionCodigo(i.getInspeccion() != null ? i.getInspeccion().getCodigoInspeccion() : null)
                .expedienteId(i.getExpedienteId())
                .expedienteNumero(i.getExpedienteNumero())
                .totalActuaciones(i.getActuaciones() != null ? i.getActuaciones().size() : 0)
                .totalEvidencias(i.getEvidencias() != null ? i.getEvidencias().size() : 0)
                .build();
    }

    private PresuntaInfraccionResponse toInfraccionResponse(PresuntaInfraccionMercado p) {
        return PresuntaInfraccionResponse.builder()
                .id(p.getId())
                .codigo(p.getCodigo())
                .incidenciaId(p.getIncidencia() != null ? p.getIncidencia().getId() : null)
                .incidenciaCodigo(p.getIncidencia() != null ? p.getIncidencia().getCodigoIncidencia() : null)
                .inspeccionId(p.getInspeccion() != null ? p.getInspeccion().getId() : null)
                .inspeccionCodigo(p.getInspeccion() != null ? p.getInspeccion().getCodigoInspeccion() : null)
                .hallazgoId(p.getHallazgo() != null ? p.getHallazgo().getId() : null)
                .hallazgoCodigo(p.getHallazgo() != null ? p.getHallazgo().getCodigo() : null)
                .mercadoId(p.getMercado().getId())
                .mercadoNombre(p.getMercado().getNombre())
                .puestoId(p.getPuesto() != null ? p.getPuesto().getId() : null)
                .puestoNumero(p.getPuesto() != null ? p.getPuesto().getNumero() : null)
                .comercianteId(p.getComerciante() != null ? p.getComerciante().getId() : null)
                .comercianteNombre(p.getComerciante() != null && p.getComerciante().getPersona() != null ? p.getComerciante().getPersona().getNombreCompleto() : null)
                .tipificacionId(p.getTipificacion() != null ? p.getTipificacion().getId() : null)
                .tipificacionCodigo(p.getTipificacion() != null ? p.getTipificacion().getCodigo() : null)
                .tipificacionDescripcion(p.getTipificacion() != null ? p.getTipificacion().getDescripcion() : null)
                .normaReferencia(p.getTipificacion() != null ? p.getTipificacion().getNormaReferencia() : null)
                .descripcionHechos(p.getDescripcionHechos())
                .fechaDeteccion(p.getFechaDeteccion())
                .estadoEvaluacion(p.getEstadoEvaluacion())
                .responsableEvaluacionId(p.getResponsableEvaluacion() != null ? p.getResponsableEvaluacion().getId() : null)
                .responsableEvaluacionNombre(p.getResponsableEvaluacion() != null ? p.getResponsableEvaluacion().getUsername() : null)
                .informeEvaluacion(p.getInformeEvaluacion())
                .fechaEvaluacion(p.getFechaEvaluacion())
                .motivoDescarte(p.getMotivoDescarte())
                .expedienteId(p.getExpedienteId())
                .expedienteNumero(p.getExpedienteNumero())
                .tieneDescargo(p.getTieneDescargo())
                .fechaDescargo(p.getFechaDescargo())
                .documentoDescargoRef(p.getDocumentoDescargoRef())
                .resumenDescargo(p.getResumenDescargo())
                .resolucionDecisionRef(p.getResolucionDecisionRef())
                .fechaDecision(p.getFechaDecision())
                .sentidoDecision(p.getSentidoDecision())
                .createdAt(p.getCreatedAt())
                .build();
    }

    private Usuario resolverUsuario(String username) {
        if (username != null && !username.isBlank() && !"sistema".equalsIgnoreCase(username.trim())) {
            Optional<Usuario> u = usuarioRepository.findByUsername(username.trim());
            if (u.isPresent()) {
                return u.get();
            }
        }
        return usuarioRepository.findAll().stream()
                .filter(u -> Boolean.TRUE.equals(u.getEnabled()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró usuario activo en el sistema"));
    }
}
