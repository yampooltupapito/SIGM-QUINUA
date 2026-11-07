package pe.gob.municipalidad.sigm.common.document.mapper;

import pe.gob.municipalidad.sigm.common.document.dto.DocumentDtos;
import pe.gob.municipalidad.sigm.common.document.dto.DocumentDtos.*;
import pe.gob.municipalidad.sigm.common.document.entity.*;

public final class DocumentoMapper {

    private DocumentoMapper() {
    }

    public static DocumentoResponse toResponse(Documento doc) {
        if (doc == null) return null;

        String nombrePropietario = null;
        Long propietarioId = null;
        if (doc.getUsuarioPropietario() != null) {
            propietarioId = doc.getUsuarioPropietario().getId();
            nombrePropietario = doc.getUsuarioPropietario().getUsername();
        }

        String nombreOficina = null;
        Long oficinaId = null;
        if (doc.getOficinaResponsable() != null) {
            oficinaId = doc.getOficinaResponsable().getId();
            nombreOficina = doc.getOficinaResponsable().getNombre();
        }

        return DocumentoResponse.builder()
                .id(doc.getId())
                .identificadorPublico(doc.getIdentificadorPublico())
                .nombreOriginal(doc.getNombreOriginal())
                .nombreInterno(doc.getNombreInterno())
                .extension(doc.getExtension())
                .mimeType(doc.getMimeType())
                .tamanoBytes(doc.getTamanoBytes())
                .tamanoFormateado(DocumentDtos.formatBytes(doc.getTamanoBytes()))
                .hashSha256(doc.getHashSha256())
                .categoria(doc.getCategoria())
                .clasificacionAcceso(doc.getClasificacionAcceso())
                .descripcion(doc.getDescripcion())
                .estado(doc.getEstado())
                .versionActual(doc.getVersionActual())
                .bloqueoEliminacion(doc.getBloqueoEliminacion())
                .motivoEliminacion(doc.getMotivoEliminacion())
                .usuarioPropietarioId(propietarioId)
                .nombrePropietario(nombrePropietario)
                .oficinaId(oficinaId)
                .nombreOficina(nombreOficina)
                .moduloOrigen(doc.getModuloOrigen())
                .fechaCreacion(doc.getFechaCreacion())
                .fechaActualizacion(doc.getFechaActualizacion())
                .visualizable(doc.isVisualizable())
                .build();
    }

    public static DocumentoVersionResponse toVersionResponse(DocumentoVersion ver) {
        if (ver == null) return null;

        Long usuarioId = null;
        String nombreUsuario = null;
        if (ver.getUsuarioResponsable() != null) {
            usuarioId = ver.getUsuarioResponsable().getId();
            nombreUsuario = ver.getUsuarioResponsable().getUsername();
        }

        return DocumentoVersionResponse.builder()
                .id(ver.getId())
                .numeroVersion(ver.getNumeroVersion())
                .nombreInterno(ver.getNombreInterno())
                .tamanoBytes(ver.getTamanoBytes())
                .tamanoFormateado(DocumentDtos.formatBytes(ver.getTamanoBytes()))
                .mimeType(ver.getMimeType())
                .hashSha256(ver.getHashSha256())
                .motivoCambio(ver.getMotivoCambio())
                .usuarioResponsableId(usuarioId)
                .nombreUsuario(nombreUsuario)
                .fechaCreacion(ver.getFechaCreacion())
                .build();
    }

    public static VerificacionIntegridadResponse toVerificacionResponse(DocumentoVerificacion verif) {
        if (verif == null) return null;

        String nombreVerificador = null;
        if (verif.getVerificadoPor() != null) {
            nombreVerificador = verif.getVerificadoPor().getUsername();
        }

        return VerificacionIntegridadResponse.builder()
                .documentoId(verif.getDocumento().getId())
                .identificadorPublico(verif.getDocumento().getIdentificadorPublico())
                .versionEvaluada(verif.getVersionEvaluada())
                .hashEsperado(verif.getHashEsperado())
                .hashCalculado(verif.getHashCalculado())
                .resultado(verif.getResultado())
                .verificadoPor(nombreVerificador)
                .fechaVerificacion(verif.getFechaVerificacion())
                .detalle(verif.getDetalle())
                .build();
    }

    public static DocumentoAuditoriaResponse toAuditoriaResponse(DocumentoAcceso acc) {
        if (acc == null) return null;

        String usuario = acc.getUsuario() != null ? acc.getUsuario().getUsername() : "ANONIMO";

        return DocumentoAuditoriaResponse.builder()
                .id(acc.getId())
                .accion(acc.getAccion().name())
                .usuario(usuario)
                .direccionIp(acc.getDireccionIp())
                .userAgent(acc.getUserAgent())
                .fechaAcceso(acc.getFechaAcceso())
                .resultado(acc.getResultado())
                .build();
    }
}
