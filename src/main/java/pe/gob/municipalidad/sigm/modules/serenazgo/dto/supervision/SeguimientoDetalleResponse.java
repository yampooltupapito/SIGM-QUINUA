package pe.gob.municipalidad.sigm.modules.serenazgo.dto.supervision;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoSeguimientoSupervision;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.PrioridadAdministrativaSupervision;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoObservacionSupervision;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoRegistroSupervision;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoSeguimientoSupervision;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeguimientoDetalleResponse {

    private UUID id;
    private String codigo;
    private TipoRegistroSupervision tipoRegistroOrigen;
    private UUID registroOrigenId;
    private String registroOrigenCodigo;
    private Long oficinaResponsableId;
    private String oficinaResponsableNombre;
    private Long supervisorId;
    private String supervisorNombre;
    private Long responsableAsignadoId;
    private String responsableAsignadoNombre;
    private TipoSeguimientoSupervision tipoSeguimiento;
    private String descripcion;
    private EstadoSeguimientoSupervision estado;
    private PrioridadAdministrativaSupervision prioridadAdministrativa;
    private OffsetDateTime fechaCreacion;
    private OffsetDateTime fechaCompromiso;
    private OffsetDateTime fechaCierre;
    private String resultado;
    private String motivoCancelacion;
    private String oficinaEscalamiento;
    private Long version;
    private boolean vencido;

    // Enlace y datos de contexto del registro origen
    private String urlRegistroOrigen;
    private String resumenRegistroOrigen;

    // Observaciones y subsanaciones
    private List<ObservacionItemDto> observaciones;

    // Historial de trazabilidad
    private List<HistorialItemDto> historial;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ObservacionItemDto {
        private UUID id;
        private TipoObservacionSupervision tipo;
        private Long usuarioId;
        private String usuarioNombre;
        private String usuarioRol;
        private String motivo;
        private String detalle;
        private String estadoResultante;
        private OffsetDateTime fecha;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistorialItemDto {
        private UUID id;
        private Long usuarioId;
        private String usuarioNombre;
        private String accion;
        private String estadoAnterior;
        private String estadoNuevo;
        private String detalle;
        private String ipOrigen;
        private OffsetDateTime fecha;
    }
}
