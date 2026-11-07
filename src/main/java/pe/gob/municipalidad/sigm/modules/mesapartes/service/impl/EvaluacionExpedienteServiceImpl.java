package pe.gob.municipalidad.sigm.modules.mesapartes.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.municipalidad.sigm.common.document.dto.DocumentDtos.DocumentoResponse;
import pe.gob.municipalidad.sigm.common.document.dto.DocumentDtos.SubirDocumentoRequest;
import pe.gob.municipalidad.sigm.common.document.entity.CategoriaDocumento;
import pe.gob.municipalidad.sigm.common.document.entity.ClasificacionAcceso;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.document.repository.DocumentoRepository;
import pe.gob.municipalidad.sigm.common.document.service.DocumentoService;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.PersonaOficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.AtencionDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.EvaluacionDocumento;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.EvaluacionExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.Expediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoEvaluacion;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.EvaluacionDocumentoRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.EvaluacionExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.EvaluacionExpedienteService;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EvaluacionExpedienteServiceImpl implements EvaluacionExpedienteService {

    private static final Logger log = LoggerFactory.getLogger(EvaluacionExpedienteServiceImpl.class);

    private final EvaluacionExpedienteRepository evaluacionRepository;
    private final EvaluacionDocumentoRepository evaluacionDocumentoRepository;
    private final ExpedienteRepository expedienteRepository;
    private final DocumentoRepository documentoRepository;
    private final DocumentoService documentoService;
    private final UsuarioRepository usuarioRepository;
    private final PersonaOficinaRepository personaOficinaRepository;

    public EvaluacionExpedienteServiceImpl(
            EvaluacionExpedienteRepository evaluacionRepository,
            EvaluacionDocumentoRepository evaluacionDocumentoRepository,
            ExpedienteRepository expedienteRepository,
            DocumentoRepository documentoRepository,
            DocumentoService documentoService,
            UsuarioRepository usuarioRepository,
            PersonaOficinaRepository personaOficinaRepository) {
        this.evaluacionRepository = evaluacionRepository;
        this.evaluacionDocumentoRepository = evaluacionDocumentoRepository;
        this.expedienteRepository = expedienteRepository;
        this.documentoRepository = documentoRepository;
        this.documentoService = documentoService;
        this.usuarioRepository = usuarioRepository;
        this.personaOficinaRepository = personaOficinaRepository;
    }

    @Override
    @Transactional
    public EvaluacionResponse crearEvaluacion(Long expedienteId, CrearEvaluacionRequest request, String username) {
        Usuario usuario = obtenerUsuario(username);
        Expediente expediente = expedienteRepository.findByIdWithDetalles(expedienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Expediente no encontrado con ID: " + expedienteId));

        Oficina oficina = expediente.getOficinaResponsable();
        if (oficina == null) {
            throw new BusinessException("El expediente no tiene asignada una oficina responsable.");
        }

        validarAccesoTrabajadorOAdmin(usuario, oficina.getId());

        EvaluacionExpediente evaluacion = EvaluacionExpediente.builder()
                .expediente(expediente)
                .oficina(oficina)
                .usuarioEvaluador(usuario)
                .tipoEvaluacion(request.getTipoEvaluacion())
                .descripcion(request.getDescripcion().trim())
                .conclusiones(request.getConclusiones().trim())
                .recomendacion(request.getRecomendacion() != null ? request.getRecomendacion().trim() : null)
                .estado(EstadoEvaluacion.BORRADOR)
                .fechaEvaluacion(OffsetDateTime.now())
                .createdBy(usuario.getUsername())
                .build();

        EvaluacionExpediente guardada = evaluacionRepository.save(evaluacion);
        log.info("Evaluación creada ID {} para expediente {}", guardada.getId(), expediente.getNumeroExpediente());

        return mapearAResponse(guardada);
    }

    @Override
    @Transactional
    public EvaluacionResponse finalizarEvaluacion(Long evaluacionId, String username) {
        Usuario usuario = obtenerUsuario(username);
        EvaluacionExpediente evaluacion = evaluacionRepository.findById(evaluacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluación no encontrada con ID: " + evaluacionId));

        if (evaluacion.getEstado() != EstadoEvaluacion.BORRADOR) {
            throw new BusinessException("Solo es posible finalizar evaluaciones que se encuentren en estado BORRADOR.");
        }

        boolean esAutor = evaluacion.getUsuarioEvaluador().getId().equals(usuario.getId());
        boolean esAdmin = esAdmin(usuario);

        if (!esAutor && !esAdmin) {
            throw new AccessDeniedException("Solo el autor de la evaluación o un administrador institucional puede finalizarla.");
        }

        evaluacion.setEstado(EstadoEvaluacion.FINALIZADA);
        evaluacion.setUpdatedBy(usuario.getUsername());
        EvaluacionExpediente actualizada = evaluacionRepository.save(evaluacion);

        log.info("Evaluación ID {} finalizada por {}", evaluacionId, username);
        return mapearAResponse(actualizada);
    }

    @Override
    @Transactional
    public EvaluacionResponse anularEvaluacion(Long evaluacionId, String motivo, String username) {
        Usuario usuario = obtenerUsuario(username);
        EvaluacionExpediente evaluacion = evaluacionRepository.findById(evaluacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluación no encontrada con ID: " + evaluacionId));

        if (motivo == null || motivo.trim().length() < 10) {
            throw new BusinessException("Debe ingresar un motivo de anulación formal de al menos 10 caracteres.");
        }

        validarAccesoTrabajadorOAdmin(usuario, evaluacion.getOficina().getId());

        evaluacion.setEstado(EstadoEvaluacion.ANULADA_CON_TRAZABILIDAD);
        evaluacion.setRecomendacion((evaluacion.getRecomendacion() != null ? evaluacion.getRecomendacion() + "\n" : "") +
                "[ANULADO por " + username + " - Motivo: " + motivo.trim() + "]");
        evaluacion.setUpdatedBy(usuario.getUsername());

        EvaluacionExpediente actualizada = evaluacionRepository.save(evaluacion);
        return mapearAResponse(actualizada);
    }

    @Override
    @Transactional
    public EvaluacionDocumentoResponse adjuntarDocumento(Long evaluacionId, MultipartFile archivo, String tipoDocumento, String username) {
        Usuario usuario = obtenerUsuario(username);
        EvaluacionExpediente evaluacion = evaluacionRepository.findById(evaluacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluación no encontrada con ID: " + evaluacionId));

        if (evaluacion.getEstado() != EstadoEvaluacion.BORRADOR) {
            throw new BusinessException("No se pueden adjuntar documentos a una evaluación ya finalizada o anulada.");
        }

        validarAccesoTrabajadorOAdmin(usuario, evaluacion.getOficina().getId());

        SubirDocumentoRequest uploadRequest = SubirDocumentoRequest.builder()
                .categoria(CategoriaDocumento.INFORME)
                .clasificacionAcceso(ClasificacionAcceso.INTERNO)
                .descripcion("Informe adjunto a evaluación técnica #" + evaluacion.getId() + " - Exp: " + evaluacion.getExpediente().getNumeroExpediente())
                .oficinaId(evaluacion.getOficina().getId())
                .build();

        DocumentoResponse docResp = documentoService.subirDocumento(archivo, uploadRequest, username);
        Documento documento = documentoRepository.findById(docResp.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Error al recuperar documento subido con ID: " + docResp.getId()));

        EvaluacionDocumento evalDoc = EvaluacionDocumento.builder()
                .evaluacion(evaluacion)
                .documento(documento)
                .tipoDocumento(tipoDocumento != null ? tipoDocumento : "INFORME_TECNICO")
                .build();

        EvaluacionDocumento guardado = evaluacionDocumentoRepository.save(evalDoc);

        return EvaluacionDocumentoResponse.builder()
                .id(guardado.getId())
                .documentoId(documento.getId())
                .nombreOriginal(documento.getNombreOriginal())
                .mimeType(documento.getMimeType())
                .tamanoFormateado(docResp.getTamanoFormateado())
                .tipoDocumento(guardado.getTipoDocumento())
                .hashSha256(documento.getHashSha256())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvaluacionResponse> listarEvaluacionesPorExpediente(Long expedienteId, String username) {
        return evaluacionRepository.findByExpedienteIdOrderByFechaEvaluacionDesc(expedienteId)
                .stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    private EvaluacionResponse mapearAResponse(EvaluacionExpediente e) {
        List<EvaluacionDocumentoResponse> docs = e.getDocumentos().stream()
                .map(ed -> {
                    String tamano = (ed.getDocumento().getTamanoBytes() != null)
                            ? (ed.getDocumento().getTamanoBytes() / 1024) + " KB"
                            : "-";
                    return EvaluacionDocumentoResponse.builder()
                            .id(ed.getId())
                            .documentoId(ed.getDocumento().getId())
                            .nombreOriginal(ed.getDocumento().getNombreOriginal())
                            .mimeType(ed.getDocumento().getMimeType())
                            .tamanoFormateado(tamano)
                            .tipoDocumento(ed.getTipoDocumento())
                            .hashSha256(ed.getDocumento().getHashSha256())
                            .build();
                })
                .collect(Collectors.<EvaluacionDocumentoResponse>toList());

        return EvaluacionResponse.builder()
                .id(e.getId())
                .expedienteId(e.getExpediente().getId())
                .numeroExpediente(e.getExpediente().getNumeroExpediente())
                .oficinaId(e.getOficina().getId())
                .oficinaNombre(e.getOficina().getNombre())
                .usuarioEvaluadorId(e.getUsuarioEvaluador().getId())
                .usuarioEvaluadorNombre(e.getUsuarioEvaluador().getNombreCompleto())
                .tipoEvaluacion(e.getTipoEvaluacion())
                .descripcion(e.getDescripcion())
                .conclusiones(e.getConclusiones())
                .recomendacion(e.getRecomendacion())
                .estado(e.getEstado())
                .fechaEvaluacion(e.getFechaEvaluacion())
                .version(e.getVersion())
                .documentos(docs)
                .build();
    }

    private Usuario obtenerUsuario(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
    }

    private boolean esAdmin(Usuario usuario) {
        return usuario.getRoles().stream()
                .anyMatch(r -> r.getCodigo().equals("ROLE_ADMIN") || r.getCodigo().equals("ROLE_SUPERADMIN"));
    }

    private void validarAccesoTrabajadorOAdmin(Usuario usuario, Long oficinaId) {
        if (esAdmin(usuario)) return;
        boolean activoEnOficina = personaOficinaRepository.findByPersonaIdAndEstado(usuario.getPersona().getId(), "ACTIVO")
                .stream()
                .anyMatch(po -> po.getOficina().getId().equals(oficinaId));
        if (!activoEnOficina) {
            throw new AccessDeniedException("No pertenece a la oficina responsable del expediente.");
        }
    }
}
