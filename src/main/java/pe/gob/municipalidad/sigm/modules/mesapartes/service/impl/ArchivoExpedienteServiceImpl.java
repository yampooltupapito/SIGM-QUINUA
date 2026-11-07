package pe.gob.municipalidad.sigm.modules.mesapartes.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.PersonaOficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.AtencionDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.ArchivoExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.Expediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.ReaperturaExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ArchivoExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ReaperturaExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.ArchivoExpedienteService;
import pe.gob.municipalidad.sigm.modules.mesapartes.workflow.ExpedienteWorkflowService;

import java.time.OffsetDateTime;

@Service
public class ArchivoExpedienteServiceImpl implements ArchivoExpedienteService {

    private static final Logger log = LoggerFactory.getLogger(ArchivoExpedienteServiceImpl.class);

    private final ArchivoExpedienteRepository archivoRepository;
    private final ReaperturaExpedienteRepository reaperturaRepository;
    private final ExpedienteRepository expedienteRepository;
    private final ExpedienteWorkflowService workflowService;
    private final UsuarioRepository usuarioRepository;
    private final PersonaOficinaRepository personaOficinaRepository;

    public ArchivoExpedienteServiceImpl(
            ArchivoExpedienteRepository archivoRepository,
            ReaperturaExpedienteRepository reaperturaRepository,
            ExpedienteRepository expedienteRepository,
            ExpedienteWorkflowService workflowService,
            UsuarioRepository usuarioRepository,
            PersonaOficinaRepository personaOficinaRepository) {
        this.archivoRepository = archivoRepository;
        this.reaperturaRepository = reaperturaRepository;
        this.expedienteRepository = expedienteRepository;
        this.workflowService = workflowService;
        this.usuarioRepository = usuarioRepository;
        this.personaOficinaRepository = personaOficinaRepository;
    }

    @Override
    @Transactional
    public ArchivoResponse archivarExpediente(Long expedienteId, ArchivarExpedienteRequest request, String username) {
        Usuario usuario = obtenerUsuario(username);
        Expediente expediente = expedienteRepository.findByIdWithDetalles(expedienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Expediente no encontrado con ID: " + expedienteId));

        if (expediente.getEstadoActual() != EstadoExpediente.RESUELTO &&
                expediente.getEstadoActual() != EstadoExpediente.NOTIFICADO &&
                expediente.getEstadoActual() != EstadoExpediente.OBSERVADO) {
            throw new BusinessException("Solo pueden enviarse a archivo expedientes concluidos, resueltos, notificados o con observaciones no subsanadas.");
        }

        Oficina oficina = expediente.getOficinaResponsable();
        validarPermisoArchivo(usuario);

        ArchivoExpediente archivo = ArchivoExpediente.builder()
                .expediente(expediente)
                .usuarioArchivo(usuario)
                .oficinaArchivo(oficina)
                .codigoArchivador(request.getCodigoArchivador() != null ? request.getCodigoArchivador().trim() : null)
                .ubicacionFisica(request.getUbicacionFisica() != null ? request.getUbicacionFisica().trim() : null)
                .tomoFolio(request.getTomoFolio() != null ? request.getTomoFolio().trim() : null)
                .motivoArchivo(request.getMotivoArchivo().trim())
                .fechaArchivo(OffsetDateTime.now())
                .createdBy(usuario.getUsername())
                .build();

        ArchivoExpediente guardado = archivoRepository.save(archivo);

        expediente.setUbicacionArchivo(archivo.getUbicacionFisica());

        workflowService.transicionar(
                expediente,
                EstadoExpediente.ARCHIVADO,
                "ENVIO_ARCHIVO_ADMINISTRATIVO",
                "Expediente archivado en ubicación: " + (archivo.getUbicacionFisica() != null ? archivo.getUbicacionFisica() : "Archivo Central") + " - Motivo: " + request.getMotivoArchivo(),
                usuario,
                oficina
        );

        log.info("Expediente {} archivado exitosamente.", expediente.getNumeroExpediente());
        return mapearAResponse(guardado);
    }

    @Override
    @Transactional
    public ReaperturaResponse reabrirExpediente(Long expedienteId, ReabrirExpedienteRequest request, String username) {
        Usuario usuario = obtenerUsuario(username);
        Expediente expediente = expedienteRepository.findByIdWithDetalles(expedienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Expediente no encontrado con ID: " + expedienteId));

        if (expediente.getEstadoActual() != EstadoExpediente.ARCHIVADO &&
                expediente.getEstadoActual() != EstadoExpediente.RESUELTO &&
                expediente.getEstadoActual() != EstadoExpediente.NOTIFICADO) {
            throw new BusinessException("Solo es procedente reabrir expedientes que se encuentren en estado ARCHIVADO, RESUELTO o NOTIFICADO.");
        }

        validarPermisoReapertura(usuario);

        ReaperturaExpediente reapertura = ReaperturaExpediente.builder()
                .expediente(expediente)
                .usuarioReapertura(usuario)
                .motivoLegal(request.getMotivoLegal().trim())
                .actoReaperturaReferencia(request.getActoReaperturaReferencia() != null ? request.getActoReaperturaReferencia().trim() : null)
                .fechaReapertura(OffsetDateTime.now())
                .createdBy(usuario.getUsername())
                .build();

        ReaperturaExpediente guardada = reaperturaRepository.save(reapertura);

        expediente.setEsReabierto(true);
        expediente.setMotivoReapertura(request.getMotivoLegal());

        workflowService.transicionar(
                expediente,
                EstadoExpediente.EN_ATENCION,
                "REAPERTURA_ADMINISTRATIVA",
                "Reapertura autorizada por mandato administrativo o legal. Motivo: " + request.getMotivoLegal(),
                usuario,
                expediente.getOficinaResponsable()
        );

        log.info("Expediente {} reabierto formalmente por {}", expediente.getNumeroExpediente(), username);

        return ReaperturaResponse.builder()
                .id(guardada.getId())
                .expedienteId(expediente.getId())
                .numeroExpediente(expediente.getNumeroExpediente())
                .usuarioReaperturaId(usuario.getId())
                .usuarioReaperturaNombre(usuario.getNombreCompleto())
                .motivoLegal(guardada.getMotivoLegal())
                .actoReaperturaReferencia(guardada.getActoReaperturaReferencia())
                .fechaReapertura(guardada.getFechaReapertura())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ArchivoResponse obtenerArchivo(Long expedienteId, String username) {
        return archivoRepository.findFirstByExpedienteIdOrderByFechaArchivoDesc(expedienteId)
                .map(this::mapearAResponse)
                .orElse(null);
    }

    private ArchivoResponse mapearAResponse(ArchivoExpediente a) {
        return ArchivoResponse.builder()
                .id(a.getId())
                .expedienteId(a.getExpediente().getId())
                .numeroExpediente(a.getExpediente().getNumeroExpediente())
                .usuarioArchivoId(a.getUsuarioArchivo().getId())
                .usuarioArchivoNombre(a.getUsuarioArchivo().getNombreCompleto())
                .oficinaArchivoId(a.getOficinaArchivo() != null ? a.getOficinaArchivo().getId() : null)
                .oficinaArchivoNombre(a.getOficinaArchivo() != null ? a.getOficinaArchivo().getNombre() : "Archivo Central")
                .codigoArchivador(a.getCodigoArchivador())
                .ubicacionFisica(a.getUbicacionFisica())
                .tomoFolio(a.getTomoFolio())
                .motivoArchivo(a.getMotivoArchivo())
                .fechaArchivo(a.getFechaArchivo())
                .build();
    }

    private Usuario obtenerUsuario(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
    }

    private void validarPermisoArchivo(Usuario usuario) {
        boolean tienePermiso = usuario.getRoles().stream()
                .anyMatch(r -> r.getCodigo().equals("ROLE_SUPERADMIN") ||
                        r.getCodigo().equals("ROLE_ADMIN") ||
                        r.getCodigo().equals("ROLE_JEFE_OFICINA") ||
                        r.getCodigo().equals("ROLE_MESA_PARTES"));
        if (!tienePermiso) {
            throw new AccessDeniedException("No posee facultades para remitir expedientes al archivo pasivo institucional.");
        }
    }

    private void validarPermisoReapertura(Usuario usuario) {
        boolean tienePermiso = usuario.getRoles().stream()
                .anyMatch(r -> r.getCodigo().equals("ROLE_SUPERADMIN") ||
                        r.getCodigo().equals("ROLE_ADMIN") ||
                        r.getCodigo().equals("ROLE_JEFE_OFICINA"));
        if (!tienePermiso) {
            throw new AccessDeniedException("La reapertura de expedientes es un acto privativo de autoridades superiores.");
        }
    }
}
