package pe.gob.municipalidad.sigm.modules.mesapartes.mapper;

import org.springframework.stereotype.Component;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.DerivacionDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.ActuacionDocumento;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.ActuacionExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.AsignacionExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.DerivacionExpediente;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DerivacionMapper {

    public DerivacionResponse toDerivacionResponse(DerivacionExpediente d) {
        if (d == null) return null;

        String derivadorNombre = obtenerNombreUsuario(d.getUsuarioDerivador());
        String receptorNombre = obtenerNombreUsuario(d.getUsuarioReceptor());

        return DerivacionResponse.builder()
                .id(d.getId())
                .expedienteId(d.getExpediente() != null ? d.getExpediente().getId() : null)
                .numeroExpediente(d.getExpediente() != null ? d.getExpediente().getNumeroExpediente() : null)
                .asunto(d.getExpediente() != null ? d.getExpediente().getAsunto() : null)
                .oficinaOrigenId(d.getOficinaOrigen() != null ? d.getOficinaOrigen().getId() : null)
                .oficinaOrigenNombre(d.getOficinaOrigen() != null ? d.getOficinaOrigen().getNombre() : null)
                .oficinaDestinoId(d.getOficinaDestino() != null ? d.getOficinaDestino().getId() : null)
                .oficinaDestinoNombre(d.getOficinaDestino() != null ? d.getOficinaDestino().getNombre() : null)
                .usuarioDerivadorId(d.getUsuarioDerivador() != null ? d.getUsuarioDerivador().getId() : null)
                .usuarioDerivadorNombre(derivadorNombre)
                .usuarioReceptorId(d.getUsuarioReceptor() != null ? d.getUsuarioReceptor().getId() : null)
                .usuarioReceptorNombre(receptorNombre)
                .motivo(d.getMotivo())
                .indicaciones(d.getIndicaciones())
                .motivoDevolucion(d.getMotivoDevolucion())
                .fechaDerivacion(d.getFechaDerivacion())
                .fechaRecepcion(d.getFechaRecepcion())
                .estado(d.getEstado())
                .observaciones(d.getObservaciones())
                .prioridad(d.getExpediente() != null ? d.getExpediente().getPrioridad() : null)
                .build();
    }

    public AsignacionResponse toAsignacionResponse(AsignacionExpediente a) {
        if (a == null) return null;

        return AsignacionResponse.builder()
                .id(a.getId())
                .expedienteId(a.getExpediente() != null ? a.getExpediente().getId() : null)
                .numeroExpediente(a.getExpediente() != null ? a.getExpediente().getNumeroExpediente() : null)
                .oficinaId(a.getOficina() != null ? a.getOficina().getId() : null)
                .oficinaNombre(a.getOficina() != null ? a.getOficina().getNombre() : null)
                .usuarioAsignadorId(a.getUsuarioAsignador() != null ? a.getUsuarioAsignador().getId() : null)
                .usuarioAsignadorNombre(obtenerNombreUsuario(a.getUsuarioAsignador()))
                .usuarioAsignadoId(a.getUsuarioAsignado() != null ? a.getUsuarioAsignado().getId() : null)
                .usuarioAsignadoNombre(obtenerNombreUsuario(a.getUsuarioAsignado()))
                .usuarioAnteriorId(a.getUsuarioAnterior() != null ? a.getUsuarioAnterior().getId() : null)
                .usuarioAnteriorNombre(obtenerNombreUsuario(a.getUsuarioAnterior()))
                .motivo(a.getMotivo())
                .indicaciones(a.getIndicaciones())
                .fechaAsignacion(a.getFechaAsignacion())
                .fechaFin(a.getFechaFin())
                .estado(a.getEstado())
                .build();
    }

    public ActuacionResponse toActuacionResponse(ActuacionExpediente act) {
        if (act == null) return null;

        List<DocumentoSimpleDto> docs = act.getDocumentos() != null
                ? act.getDocumentos().stream().map(this::toDocumentoSimpleDto).collect(Collectors.toList())
                : Collections.emptyList();

        return ActuacionResponse.builder()
                .id(act.getId())
                .expedienteId(act.getExpediente() != null ? act.getExpediente().getId() : null)
                .tipo(act.getTipo())
                .descripcion(act.getDescripcion())
                .usuarioResponsableId(act.getUsuarioResponsable() != null ? act.getUsuarioResponsable().getId() : null)
                .usuarioResponsableNombre(obtenerNombreUsuario(act.getUsuarioResponsable()))
                .oficinaId(act.getOficina() != null ? act.getOficina().getId() : null)
                .oficinaNombre(act.getOficina() != null ? act.getOficina().getNombre() : null)
                .fechaActuacion(act.getFechaActuacion())
                .visibilidad(act.getVisibilidad())
                .documentos(docs)
                .build();
    }

    public DocumentoSimpleDto toDocumentoSimpleDto(ActuacionDocumento ad) {
        if (ad == null || ad.getDocumento() == null) return null;
        return DocumentoSimpleDto.builder()
                .id(ad.getDocumento().getId())
                .nombreOriginal(ad.getDocumento().getNombreOriginal())
                .tamanoBytes(ad.getDocumento().getTamanoBytes())
                .tipoMime(ad.getDocumento().getMimeType())
                .hashSha256(ad.getDocumento().getHashSha256())
                .tipoDocumento(ad.getTipoDocumento())
                .build();
    }

    public OficinaSimpleDto toOficinaSimpleDto(Oficina o) {
        if (o == null) return null;
        return OficinaSimpleDto.builder()
                .id(o.getId())
                .codigo(o.getCodigo())
                .nombre(o.getNombre())
                .sigla(o.getSigla())
                .build();
    }

    public UsuarioSimpleDto toUsuarioSimpleDto(Usuario u) {
        if (u == null) return null;
        return UsuarioSimpleDto.builder()
                .id(u.getId())
                .username(u.getUsername())
                .nombreCompleto(obtenerNombreUsuario(u))
                .cargo(null)
                .build();
    }

    private String obtenerNombreUsuario(Usuario u) {
        if (u == null) return null;
        if (u.getPersona() != null) {
            Persona p = u.getPersona();
            String nombres = p.getNombres() != null ? p.getNombres() : "";
            String paterno = p.getApellidoPaterno() != null ? p.getApellidoPaterno() : "";
            String materno = p.getApellidoMaterno() != null ? p.getApellidoMaterno() : "";
            String fullName = (nombres + " " + paterno + " " + materno).trim();
            if (!fullName.isEmpty()) return fullName;
        }
        return u.getUsername();
    }
}
