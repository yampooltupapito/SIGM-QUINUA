package pe.gob.municipalidad.sigm.modules.mesapartes.mapper;

import org.springframework.stereotype.Component;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para transformar entidades de Mesa de Partes Virtual a DTOs de respuesta.
 */
@Component
public class MpvMapper {

    public MpvMapper() {
    }

    public ExpedienteResponse toExpedienteResponse(Expediente expediente) {
        if (expediente == null) {
            return null;
        }

        String solicitanteNombre = null;
        String solicitanteDoc = null;
        if (expediente.getSolicitante() != null) {
            solicitanteNombre = expediente.getSolicitante().getNombreCompleto();
            solicitanteDoc = expediente.getSolicitante().getNumeroDocumento();
        }

        String oficinaNombre = null;
        if (expediente.getOficinaResponsable() != null) {
            oficinaNombre = expediente.getOficinaResponsable().getNombre();
        }

        Long responsableId = null;
        String responsableNombre = null;
        if (expediente.getResponsableActual() != null) {
            responsableId = expediente.getResponsableActual().getId();
            if (expediente.getResponsableActual().getPersona() != null) {
                responsableNombre = expediente.getResponsableActual().getPersona().getNombreCompleto();
            } else {
                responsableNombre = expediente.getResponsableActual().getUsername();
            }
        }

        String tipoTramiteNombre = null;
        String tipoTramiteCodigo = null;
        if (expediente.getTipoTramite() != null) {
            tipoTramiteNombre = expediente.getTipoTramite().getNombre();
            tipoTramiteCodigo = expediente.getTipoTramite().getCodigo();
        }

        int totalDocs = expediente.getDocumentos() != null ? expediente.getDocumentos().size() : 0;

        return ExpedienteResponse.builder()
                .id(expediente.getId())
                .uuid(expediente.getUuid())
                .numeroExpediente(expediente.getNumeroExpediente())
                .anio(expediente.getAnio())
                .asunto(expediente.getAsunto())
                .descripcion(expediente.getDescripcion())
                .tipoTramiteId(expediente.getTipoTramite() != null ? expediente.getTipoTramite().getId() : null)
                .tipoTramiteNombre(tipoTramiteNombre)
                .tipoTramiteCodigo(tipoTramiteCodigo)
                .solicitanteId(expediente.getSolicitante() != null ? expediente.getSolicitante().getId() : null)
                .solicitanteNombreCompleto(solicitanteNombre)
                .solicitanteNumeroDocumento(solicitanteDoc)
                .oficinaResponsableId(expediente.getOficinaResponsable() != null ? expediente.getOficinaResponsable().getId() : null)
                .oficinaResponsableNombre(oficinaNombre)
                .responsableActualId(responsableId)
                .responsableActualNombre(responsableNombre)
                .estadoActual(expediente.getEstadoActual())
                .fechaPresentacion(expediente.getFechaPresentacion())
                .fechaRecepcion(expediente.getFechaRecepcion())
                .fechaLimite(expediente.getFechaLimite())
                .fechaCierre(expediente.getFechaCierre())
                .canalIngreso(expediente.getCanalIngreso())
                .prioridad(expediente.getPrioridad())
                .confidencialidad(expediente.getConfidencialidad())
                .totalDocumentos(totalDocs)
                .createdAt(expediente.getCreatedAt())
                .updatedAt(expediente.getUpdatedAt())
                .build();
    }

    public TipoTramiteResponse toTipoTramiteResponse(TipoTramite tipoTramite) {
        if (tipoTramite == null) {
            return null;
        }

        String oficinaNombre = tipoTramite.getOficinaResponsable() != null ? tipoTramite.getOficinaResponsable().getNombre() : null;
        Long oficinaId = tipoTramite.getOficinaResponsable() != null ? tipoTramite.getOficinaResponsable().getId() : null;

        return TipoTramiteResponse.builder()
                .id(tipoTramite.getId())
                .codigo(tipoTramite.getCodigo())
                .nombre(tipoTramite.getNombre())
                .descripcion(tipoTramite.getDescripcion())
                .oficinaResponsableId(oficinaId)
                .oficinaResponsableNombre(oficinaNombre)
                .requiereDocumentos(tipoTramite.getRequiereDocumentos())
                .permitePresentacionVirtual(tipoTramite.getPermitePresentacionVirtual())
                .plazoDias(tipoTramite.getPlazoDias())
                .unidadPlazo(tipoTramite.getUnidadPlazo())
                .estado(tipoTramite.getEstado())
                .build();
    }

    public ExpedienteDocumentoResponse toDocumentoResponse(ExpedienteDocumento ed) {
        if (ed == null) {
            return null;
        }

        var doc = ed.getDocumento();
        String usuarioNombre = ed.getUsuarioAsocia() != null ? ed.getUsuarioAsocia().getUsername() : null;

        return ExpedienteDocumentoResponse.builder()
                .id(ed.getId())
                .documentoId(doc != null ? doc.getId() : null)
                .documentoUuid(doc != null ? doc.getIdentificadorPublico() : null)
                .nombreOriginal(doc != null ? doc.getNombreOriginal() : null)
                .extension(doc != null ? doc.getExtension() : null)
                .mimeType(doc != null ? doc.getMimeType() : null)
                .tamanoBytes(doc != null ? doc.getTamanoBytes() : null)
                .hashSha256(doc != null ? doc.getHashSha256() : null)
                .tipoRelacion(ed.getTipoRelacion())
                .fechaAsociacion(ed.getFechaAsociacion())
                .usuarioAsociaNombre(usuarioNombre)
                .orden(ed.getOrden())
                .estado(ed.getEstado())
                .build();
    }

    public ExpedienteHistorialResponse toHistorialResponse(ExpedienteHistorial eh) {
        if (eh == null) {
            return null;
        }

        String usuarioNombre = eh.getUsuarioResponsable() != null ? eh.getUsuarioResponsable().getUsername() : null;
        String oficinaNombre = eh.getOficina() != null ? eh.getOficina().getNombre() : null;

        return ExpedienteHistorialResponse.builder()
                .id(eh.getId())
                .estadoAnterior(eh.getEstadoAnterior())
                .estadoNuevo(eh.getEstadoNuevo())
                .accion(eh.getAccion())
                .observacion(eh.getObservacion())
                .usuarioResponsableNombre(usuarioNombre)
                .oficinaNombre(oficinaNombre)
                .fechaMovimiento(eh.getFechaMovimiento())
                .identificadorCorrelacion(eh.getIdentificadorCorrelacion())
                .build();
    }

    public DerivacionResponse toDerivacionResponse(DerivacionExpediente d) {
        if (d == null) {
            return null;
        }

        String oficinaOrigenNombre = d.getOficinaOrigen() != null ? d.getOficinaOrigen().getNombre() : null;
        String oficinaDestinoNombre = d.getOficinaDestino() != null ? d.getOficinaDestino().getNombre() : null;
        String usuarioNombre = d.getUsuarioDerivador() != null ? d.getUsuarioDerivador().getUsername() : null;

        return DerivacionResponse.builder()
                .id(d.getId())
                .oficinaOrigenId(d.getOficinaOrigen() != null ? d.getOficinaOrigen().getId() : null)
                .oficinaOrigenNombre(oficinaOrigenNombre)
                .oficinaDestinoId(d.getOficinaDestino() != null ? d.getOficinaDestino().getId() : null)
                .oficinaDestinoNombre(oficinaDestinoNombre)
                .usuarioDerivadorNombre(usuarioNombre)
                .motivo(d.getMotivo())
                .fechaDerivacion(d.getFechaDerivacion())
                .fechaRecepcion(d.getFechaRecepcion())
                .estado(d.getEstado())
                .observaciones(d.getObservaciones())
                .build();
    }

    public ObservacionResponse toObservacionResponse(ObservacionExpediente o) {
        if (o == null) {
            return null;
        }

        String usuarioNombre = o.getUsuarioObservador() != null ? o.getUsuarioObservador().getUsername() : null;

        List<SubsanacionResponse> subsanacionesList = (o.getSubsanaciones() != null)
                ? o.getSubsanaciones().stream().map(this::toSubsanacionResponse).collect(Collectors.toList())
                : Collections.emptyList();

        return ObservacionResponse.builder()
                .id(o.getId())
                .descripcion(o.getDescripcion())
                .usuarioObservadorNombre(usuarioNombre)
                .fechaRegistro(o.getFechaRegistro())
                .fechaNotificacion(o.getFechaNotificacion())
                .fechaLimite(o.getFechaLimite())
                .estado(o.getEstado())
                .tipoObservacion(o.getTipoObservacion())
                .visibilidad(o.getVisibilidad())
                .requisitoId(o.getRequisito() != null ? o.getRequisito().getId() : null)
                .requisitoNombre(o.getRequisito() != null ? o.getRequisito().getNombre() : null)
                .fechaRespuesta(o.getFechaRespuesta())
                .subsanaciones(subsanacionesList)
                .build();
    }

    public pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvAdminDtos.RevisionRequisitoResponse toRevisionRequisitoResponse(pe.gob.municipalidad.sigm.modules.mesapartes.entity.RevisionRequisito r) {
        if (r == null) {
            return null;
        }
        var req = r.getRequisito();
        var doc = r.getDocumento();
        String revisor = r.getUsuarioRevisor() != null ? r.getUsuarioRevisor().getUsername() : null;

        return pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvAdminDtos.RevisionRequisitoResponse.builder()
                .id(r.getId())
                .requisitoId(req != null ? req.getId() : null)
                .codigoRequisito(req != null ? req.getCodigo() : null)
                .nombreRequisito(req != null ? req.getNombre() : null)
                .descripcionRequisito(req != null ? req.getDescripcion() : null)
                .obligatorio(req != null ? req.getObligatorio() : false)
                .documentoId(doc != null ? doc.getId() : null)
                .documentoUuid(doc != null ? doc.getIdentificadorPublico() : null)
                .nombreDocumento(doc != null ? doc.getNombreOriginal() : null)
                .estadoRevision(r.getEstadoRevision())
                .observaciones(r.getObservaciones())
                .usuarioRevisorNombre(revisor)
                .fechaRevision(r.getFechaRevision())
                .build();
    }

    public pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvAdminDtos.CambioPrioridadResponse toCambioPrioridadResponse(pe.gob.municipalidad.sigm.modules.mesapartes.entity.CambioPrioridadExpediente c) {
        if (c == null) {
            return null;
        }
        String usuarioNombre = c.getUsuario() != null ? c.getUsuario().getUsername() : null;

        return pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvAdminDtos.CambioPrioridadResponse.builder()
                .id(c.getId())
                .prioridadAnterior(c.getPrioridadAnterior())
                .prioridadNueva(c.getPrioridadNueva())
                .motivo(c.getMotivo())
                .usuarioNombre(usuarioNombre)
                .fechaCambio(c.getFechaCambio())
                .build();
    }

    public SubsanacionResponse toSubsanacionResponse(SubsanacionExpediente s) {
        if (s == null) {
            return null;
        }

        String usuarioNombre = s.getUsuarioSubsanador() != null ? s.getUsuarioSubsanador().getUsername() : null;

        return SubsanacionResponse.builder()
                .id(s.getId())
                .observacionId(s.getObservacion() != null ? s.getObservacion().getId() : null)
                .respuestaCiudadano(s.getRespuestaCiudadano())
                .fechaSubsanacion(s.getFechaSubsanacion())
                .usuarioSubsanadorNombre(usuarioNombre)
                .estado(s.getEstado())
                .build();
    }

    public ExpedienteDetalleResponse toExpedienteDetalleResponse(Expediente expediente) {
        if (expediente == null) {
            return null;
        }

        List<ExpedienteDocumentoResponse> docs = expediente.getDocumentos() != null
                ? expediente.getDocumentos().stream().map(this::toDocumentoResponse).collect(Collectors.toList())
                : Collections.emptyList();

        List<ExpedienteHistorialResponse> hist = expediente.getHistorial() != null
                ? expediente.getHistorial().stream().map(this::toHistorialResponse).collect(Collectors.toList())
                : Collections.emptyList();

        List<DerivacionResponse> derivaciones = expediente.getDerivaciones() != null
                ? expediente.getDerivaciones().stream().map(this::toDerivacionResponse).collect(Collectors.toList())
                : Collections.emptyList();

        List<ObservacionResponse> observaciones = expediente.getObservaciones() != null
                ? expediente.getObservaciones().stream()
                        .filter(o -> o.getVisibilidad() == null || o.getVisibilidad() == pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.VisibilidadObservacion.CIUDADANO)
                        .map(this::toObservacionResponse).collect(Collectors.toList())
                : Collections.emptyList();

        return ExpedienteDetalleResponse.builder()
                .expediente(toExpedienteResponse(expediente))
                .documentos(docs)
                .historial(hist)
                .derivaciones(derivaciones)
                .observaciones(observaciones)
                .build();
    }
}
