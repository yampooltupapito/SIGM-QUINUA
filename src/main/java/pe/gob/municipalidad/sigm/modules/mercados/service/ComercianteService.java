package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.document.repository.DocumentoRepository;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.DuplicateResourceException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.PersonaJuridica;
import pe.gob.municipalidad.sigm.common.repository.PersonaJuridicaRepository;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.modules.administracion.service.AuditoriaAdministrativaService;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.entity.ActividadComercial;
import pe.gob.municipalidad.sigm.modules.mercados.entity.Comerciante;
import pe.gob.municipalidad.sigm.modules.mercados.entity.ComercianteCambioEstado;
import pe.gob.municipalidad.sigm.modules.mercados.entity.ComercianteDocumento;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoComerciante;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoComerciante;
import pe.gob.municipalidad.sigm.modules.mercados.repository.ActividadComercialRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.ComercianteCambioEstadoRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.ComercianteDocumentoRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.ComercianteRepository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio principal de administración del Padrón de Comerciantes Municipales.
 */
@Slf4j
@Service
public class ComercianteService {

    private final ComercianteRepository comercianteRepository;
    private final PersonaRepository personaRepository;
    private final PersonaJuridicaRepository personaJuridicaRepository;
    private final ActividadComercialRepository actividadRepository;
    private final ComercianteCambioEstadoRepository cambioEstadoRepository;
    private final ComercianteDocumentoRepository documentoRepository;
    private final DocumentoRepository centralDocumentoRepository;
    private final ComercianteRegistroService registroService;
    private final ComercianteConsultaService consultaService;
    private final ComercianteValidator validator;
    private final AuditoriaAdministrativaService auditoriaService;

    public ComercianteService(
            ComercianteRepository comercianteRepository,
            PersonaRepository personaRepository,
            PersonaJuridicaRepository personaJuridicaRepository,
            ActividadComercialRepository actividadRepository,
            ComercianteCambioEstadoRepository cambioEstadoRepository,
            ComercianteDocumentoRepository documentoRepository,
            DocumentoRepository centralDocumentoRepository,
            ComercianteRegistroService registroService,
            ComercianteConsultaService consultaService,
            ComercianteValidator validator,
            AuditoriaAdministrativaService auditoriaService) {
        this.comercianteRepository = comercianteRepository;
        this.personaRepository = personaRepository;
        this.personaJuridicaRepository = personaJuridicaRepository;
        this.actividadRepository = actividadRepository;
        this.cambioEstadoRepository = cambioEstadoRepository;
        this.documentoRepository = documentoRepository;
        this.centralDocumentoRepository = centralDocumentoRepository;
        this.registroService = registroService;
        this.consultaService = consultaService;
        this.validator = validator;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public ComercianteResponse registrarComercianteNatural(ComercianteNaturalCreateRequest request) {
        Comerciante guardado = registroService.registrarComercianteNatural(request);
        return consultaService.toResponse(guardado, consultaService.tienePermisoDatosSensibles());
    }

    @Transactional
    public ComercianteResponse registrarComercianteJuridico(ComercianteJuridicoCreateRequest request) {
        Comerciante guardado = registroService.registrarComercianteJuridico(request);
        return consultaService.toResponse(guardado, consultaService.tienePermisoDatosSensibles());
    }

    @Transactional
    public MercadosDtos.ComercianteResponse registrarComerciante(MercadosDtos.ComercianteRequest request) {
        Persona persona = personaRepository.findById(request.getPersonaId())
                .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada con ID: " + request.getPersonaId()));

        if (comercianteRepository.existsByPersonaId(persona.getId())) {
            throw new DuplicateResourceException("La persona ya se encuentra registrada como comerciante.");
        }

        if (comercianteRepository.existsByCodigoComerciante(request.getCodigoComerciante())) {
            throw new DuplicateResourceException("El código de comerciante '" + request.getCodigoComerciante() + "' ya existe.");
        }

        Set<ActividadComercial> actividades = new HashSet<>();
        if (request.getActividadIds() != null && !request.getActividadIds().isEmpty()) {
            actividades.addAll(actividadRepository.findAllById(request.getActividadIds()));
        }

        Comerciante comerciante = Comerciante.builder()
                .tipoComerciante(TipoComerciante.PERSONA_NATURAL)
                .persona(persona)
                .codigoComerciante(request.getCodigoComerciante())
                .estado(request.getEstado() != null ? request.getEstado() : EstadoComerciante.ACTIVO)
                .fechaRegistro(LocalDate.now())
                .observaciones(request.getObservaciones())
                .actividades(actividades)
                .build();

        Comerciante guardado = comercianteRepository.save(comerciante);

        return MercadosDtos.ComercianteResponse.builder()
                .id(guardado.getId())
                .personaId(persona.getId())
                .numeroDocumento(persona.getNumeroDocumento())
                .tipoDocumento(persona.getTipoDocumento())
                .nombreCompleto(persona.getNombreCompleto())
                .telefono(persona.getTelefono())
                .correo(persona.getCorreo())
                .codigoComerciante(guardado.getCodigoComerciante())
                .fechaRegistro(guardado.getFechaRegistro())
                .estado(guardado.getEstado())
                .observaciones(guardado.getObservaciones())
                .actividades(guardado.getActividades().stream().map(ActividadComercial::getNombre).collect(Collectors.toList()))
                .build();
    }

    @Transactional
    public ComercianteResponse actualizarComerciante(UUID id, ComercianteUpdateRequest request) {
        Comerciante comerciante = comercianteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comerciante no encontrado con ID: " + id));

        // Control de concurrencia optimista
        if (request.getVersion() != null && !request.getVersion().equals(comerciante.getVersion())) {
            throw new BusinessException("El comerciante fue modificado por otro usuario en paralelo. Por favor recargue la página.");
        }

        if (request.getCodigoComerciante() != null && !request.getCodigoComerciante().trim().isEmpty()) {
            validator.validarActualizacionCodigo(id, request.getCodigoComerciante().trim());
            comerciante.setCodigoComerciante(request.getCodigoComerciante().trim().toUpperCase());
        }

        // Actualizar datos de contacto según tipo
        if (comerciante.isPersonaNatural() && comerciante.getPersona() != null) {
            Persona p = comerciante.getPersona();
            if (request.getTelefono() != null) p.setTelefono(request.getTelefono().trim());
            if (request.getCorreo() != null) p.setCorreo(request.getCorreo().trim());
            if (request.getDireccion() != null) p.setDireccion(request.getDireccion().trim());
            personaRepository.save(p);
        } else if (comerciante.isPersonaJuridica() && comerciante.getPersonaJuridica() != null) {
            PersonaJuridica pj = comerciante.getPersonaJuridica();
            if (request.getTelefono() != null) pj.setTelefono(request.getTelefono().trim());
            if (request.getCorreo() != null) pj.setCorreo(request.getCorreo().trim());
            if (request.getDireccion() != null) pj.setDomicilioFiscal(request.getDireccion().trim());
            if (request.getNombreComercial() != null) pj.setNombreComercial(request.getNombreComercial().trim());
            personaJuridicaRepository.save(pj);
        }

        if (request.getObservaciones() != null) {
            comerciante.setObservaciones(request.getObservaciones().trim());
        }

        if (request.getActividadIds() != null) {
            Set<ActividadComercial> actividades = new HashSet<>(actividadRepository.findAllById(request.getActividadIds()));
            comerciante.setActividades(actividades);
        }

        Comerciante actualizado = comercianteRepository.save(comerciante);

        auditoriaService.registrar("ACTUALIZAR_COMERCIANTE", "mercados_comerciantes", id.toString(), "EXITO",
                "Actualización de datos del comerciante " + actualizado.getCodigoComerciante());

        return consultaService.toResponse(actualizado, consultaService.tienePermisoDatosSensibles());
    }

    @Transactional
    public ComercianteResponse cambiarEstado(UUID id, ComercianteEstadoRequest request) {
        Comerciante comerciante = comercianteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comerciante no encontrado con ID: " + id));

        EstadoComerciante anterior = comerciante.getEstado();
        EstadoComerciante nuevo = request.getNuevoEstado();

        if (anterior == nuevo) {
            throw new BusinessException("El comerciante ya se encuentra en estado " + nuevo.getDescripcion());
        }

        comerciante.setEstado(nuevo);
        Comerciante actualizado = comercianteRepository.save(comerciante);

        String username = "SISTEMA";
        Long usuarioId = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            username = auth.getName();
        }

        Documento docSustento = null;
        if (request.getDocumentoSustentoId() != null) {
            docSustento = centralDocumentoRepository.findById(request.getDocumentoSustentoId()).orElse(null);
        }

        ComercianteCambioEstado hist = ComercianteCambioEstado.builder()
                .comerciante(actualizado)
                .estadoAnterior(anterior)
                .estadoNuevo(nuevo)
                .motivo(request.getMotivo().trim())
                .documentoSustento(docSustento)
                .observaciones(request.getObservaciones() != null ? request.getObservaciones().trim() : null)
                .usuarioId(usuarioId)
                .usuarioUsername(username)
                .fechaCambio(OffsetDateTime.now())
                .build();
        cambioEstadoRepository.save(hist);

        auditoriaService.registrar("CAMBIAR_ESTADO_COMERCIANTE", "mercados_comerciantes", id.toString(), "EXITO",
                "Cambio de estado de comerciante " + comerciante.getCodigoComerciante() + ": " + anterior + " -> " + nuevo + ". Motivo: " + request.getMotivo());

        return consultaService.toResponse(actualizado, consultaService.tienePermisoDatosSensibles());
    }

    @Transactional
    public ComercianteDocumentoResponse asociarDocumento(UUID comercianteId, Long documentoId, String tipo, String observaciones) {
        Comerciante comerciante = comercianteRepository.findById(comercianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Comerciante no encontrado con ID: " + comercianteId));

        Documento documento = centralDocumentoRepository.findById(documentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento institucional no encontrado con ID: " + documentoId));

        if (documentoRepository.existsByComercianteIdAndDocumentoId(comercianteId, documentoId)) {
            throw new DuplicateResourceException("El documento ya se encuentra asociado a este comerciante.");
        }

        String username = "SISTEMA";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            username = auth.getName();
        }

        ComercianteDocumento doc = ComercianteDocumento.builder()
                .comerciante(comerciante)
                .documento(documento)
                .tipoDocumentoComerciante(tipo != null ? tipo.trim() : "SUSTENTO")
                .observaciones(observaciones != null ? observaciones.trim() : null)
                .createdAt(OffsetDateTime.now())
                .createdBy(username)
                .build();

        ComercianteDocumento guardado = documentoRepository.save(doc);

        auditoriaService.registrar("ASOCIAR_DOCUMENTO_COMERCIANTE", "mercados_comerciantes_documentos", guardado.getId().toString(), "EXITO",
                "Asociado documento " + documento.getNombreOriginal() + " a comerciante " + comerciante.getCodigoComerciante());

        return ComercianteDocumentoResponse.builder()
                .id(guardado.getId())
                .documentoId(documento.getId())
                .documentoUuid(documento.getIdentificadorPublico())
                .tipoDocumento(guardado.getTipoDocumentoComerciante())
                .numeroDocumento(String.valueOf(documento.getId()))
                .titulo(documento.getNombreOriginal())
                .extension(documento.getExtension())
                .tamanioBytes(documento.getTamanoBytes())
                .observaciones(guardado.getObservaciones())
                .fechaSubida(guardado.getCreatedAt())
                .subidoPor(guardado.getCreatedBy())
                .build();
    }

    @Transactional(readOnly = true)
    public ComercianteResponse obtenerPorId(UUID id) {
        return consultaService.obtenerPorId(id);
    }

    @Transactional(readOnly = true)
    public ComercianteResponse obtenerPorCodigo(String codigo) {
        return consultaService.obtenerPorCodigo(codigo);
    }

    @Transactional(readOnly = true)
    public ComercianteDetailResponse obtenerDetalleCompleto(UUID id) {
        return consultaService.obtenerDetalleCompleto(id);
    }

    @Transactional(readOnly = true)
    public Page<ComercianteResponse> listarComerciantes(ComercianteFilter filter, Pageable pageable) {
        return consultaService.listarPaginado(filter, pageable);
    }

    @Transactional(readOnly = true)
    public ComercianteEstadisticasResponse obtenerEstadisticas() {
        return consultaService.obtenerEstadisticas();
    }

    // Sobrecarga de conveniencia para controladores y pruebas anteriores
    @Transactional(readOnly = true)
    public Page<ComercianteResponse> listarComerciantes(String termino, EstadoComerciante estado, Pageable pageable) {
        ComercianteFilter filter = ComercianteFilter.builder()
                .termino(termino)
                .estado(estado)
                .build();
        return consultaService.listarPaginado(filter, pageable);
    }

    public ComercianteResponse toResponse(Comerciante c) {
        return consultaService.toResponse(c, consultaService.tienePermisoDatosSensibles());
    }
}
