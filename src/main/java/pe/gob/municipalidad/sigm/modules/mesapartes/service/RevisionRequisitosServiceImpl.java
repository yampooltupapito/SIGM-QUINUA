package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.document.repository.DocumentoRepository;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Rol;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvAdminDtos.RevisionRequisitoResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvAdminDtos.VerificarRequisitoRequest;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoRevisionRequisito;
import pe.gob.municipalidad.sigm.modules.mesapartes.mapper.MpvMapper;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.RequisitoDocumentalRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.RevisionRequisitoRepository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RevisionRequisitosServiceImpl implements RevisionRequisitosService {

    private final RevisionRequisitoRepository revisionRequisitoRepository;
    private final RequisitoDocumentalRepository requisitoDocumentalRepository;
    private final ExpedienteRepository expedienteRepository;
    private final DocumentoRepository documentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final MpvMapper mpvMapper;

    public RevisionRequisitosServiceImpl(RevisionRequisitoRepository revisionRequisitoRepository,
                                         RequisitoDocumentalRepository requisitoDocumentalRepository,
                                         ExpedienteRepository expedienteRepository,
                                         DocumentoRepository documentoRepository,
                                         UsuarioRepository usuarioRepository,
                                         MpvMapper mpvMapper) {
        this.revisionRequisitoRepository = revisionRequisitoRepository;
        this.requisitoDocumentalRepository = requisitoDocumentalRepository;
        this.expedienteRepository = expedienteRepository;
        this.documentoRepository = documentoRepository;
        this.usuarioRepository = usuarioRepository;
        this.mpvMapper = mpvMapper;
    }

    @Override
    @Transactional
    public List<RevisionRequisitoResponse> obtenerOInicializarRequisitos(Long expedienteId, String username) {
        Usuario usuario = obtenerUsuario(username);
        Expediente expediente = expedienteRepository.findByIdWithDetalles(expedienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Expediente no encontrado con ID: " + expedienteId));

        validarAccesoLectura(expediente, usuario);

        List<RevisionRequisito> existentes = revisionRequisitoRepository.findByExpedienteIdWithDetails(expedienteId);
        if (!existentes.isEmpty()) {
            return existentes.stream().map(mpvMapper::toRevisionRequisitoResponse).collect(Collectors.toList());
        }

        // Si no existen revisiones inicializadas, crearlas a partir del catálogo de requisitos del trámite
        if (expediente.getTipoTramite() == null) {
            return List.of();
        }

        List<RequisitoDocumental> catalogados = requisitoDocumentalRepository
                .findByTipoTramiteIdAndEstadoOrderByOrdenAsc(expediente.getTipoTramite().getId(), "ACTIVO");

        if (catalogados.isEmpty()) {
            return List.of();
        }

        List<RevisionRequisito> nuevasRevisiones = new ArrayList<>();
        List<ExpedienteDocumento> docsExp = expediente.getDocumentos();

        for (int i = 0; i < catalogados.size(); i++) {
            RequisitoDocumental req = catalogados.get(i);
            Documento docSugerido = null;
            if (docsExp != null && i < docsExp.size()) {
                docSugerido = docsExp.get(i).getDocumento();
            }

            RevisionRequisito rev = RevisionRequisito.builder()
                    .expediente(expediente)
                    .requisito(req)
                    .documento(docSugerido)
                    .estadoRevision(EstadoRevisionRequisito.PENDIENTE_REVISION)
                    .build();

            nuevasRevisiones.add(revisionRequisitoRepository.save(rev));
        }

        return nuevasRevisiones.stream().map(mpvMapper::toRevisionRequisitoResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RevisionRequisitoResponse verificarRequisito(Long expedienteId, Long requisitoId, VerificarRequisitoRequest request, String username) {
        Usuario usuario = obtenerUsuario(username);
        if (esCiudadano(usuario)) {
            throw new AccessDeniedException("Operación no autorizada: la verificación de requisitos es potestad exclusiva del operador municipal.");
        }

        Expediente expediente = expedienteRepository.findById(expedienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Expediente no encontrado con ID: " + expedienteId));

        RevisionRequisito revision = revisionRequisitoRepository.findByExpedienteIdAndRequisitoId(expedienteId, requisitoId)
                .orElseGet(() -> {
                    RequisitoDocumental req = requisitoDocumentalRepository.findById(requisitoId)
                            .orElseThrow(() -> new ResourceNotFoundException("Requisito no encontrado con ID: " + requisitoId));
                    return RevisionRequisito.builder()
                            .expediente(expediente)
                            .requisito(req)
                            .build();
                });

        revision.setEstadoRevision(request.getEstadoRevision());
        revision.setObservaciones(request.getObservaciones());
        revision.setUsuarioRevisor(usuario);
        revision.setFechaRevision(OffsetDateTime.now());

        if (request.getDocumentoId() != null) {
            Documento doc = documentoRepository.findById(request.getDocumentoId()).orElse(null);
            revision.setDocumento(doc);
        }

        RevisionRequisito guardada = revisionRequisitoRepository.save(revision);
        return mpvMapper.toRevisionRequisitoResponse(guardada);
    }

    private Usuario obtenerUsuario(String username) {
        return usuarioRepository.findByUsernameWithRolesAndPersona(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
    }

    private boolean esCiudadano(Usuario usuario) {
        if (usuario.getRoles() == null || usuario.getRoles().isEmpty()) return true;
        return usuario.getRoles().stream()
                .allMatch(r -> r.getNombre().equalsIgnoreCase("ROLE_CIUDADANO"));
    }

    private void validarAccesoLectura(Expediente expediente, Usuario usuario) {
        if (esCiudadano(usuario)) {
            if (expediente.getSolicitante() == null || usuario.getPersona() == null
                    || !expediente.getSolicitante().getId().equals(usuario.getPersona().getId())) {
                throw new AccessDeniedException("No tiene permisos para ver requisitos de este expediente.");
            }
        }
    }
}
