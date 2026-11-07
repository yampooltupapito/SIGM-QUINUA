package pe.gob.municipalidad.sigm.modules.mesapartes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvDtos.ExpedienteHistorialResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvDtos.ExpedienteResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class DerivacionDtos {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DerivarExpedienteRequest {
        @NotNull(message = "La oficina de destino es obligatoria")
        private Long oficinaDestinoId;

        @NotBlank(message = "El motivo de la derivación es obligatorio")
        @Size(max = 1000, message = "El motivo no puede exceder los 1000 caracteres")
        private String motivo;

        private String indicaciones;

        private PrioridadExpediente prioridad;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecepcionDerivacionRequest {
        @Size(max = 1000, message = "Las observaciones no pueden superar los 1000 caracteres")
        private String observaciones;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RechazoDevolucionRequest {
        @NotBlank(message = "El motivo de rechazo o devolución es obligatorio")
        @Size(max = 1000, message = "El motivo no puede superar los 1000 caracteres")
        private String motivo;

        private String observaciones;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AsignarExpedienteRequest {
        @NotNull(message = "El trabajador asignado es obligatorio")
        private Long usuarioAsignadoId;

        private String motivo;

        private String indicaciones;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReasignarExpedienteRequest {
        @NotNull(message = "El nuevo trabajador responsable es obligatorio")
        private Long nuevoUsuarioId;

        @NotBlank(message = "El motivo de la reasignación es obligatorio")
        @Size(max = 1000, message = "El motivo no puede superar los 1000 caracteres")
        private String motivo;

        private String indicaciones;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RegistrarActuacionRequest {
        @NotNull(message = "El tipo de actuación es obligatorio")
        private TipoActuacionExpediente tipo;

        @NotBlank(message = "La descripción de la actuación es obligatoria")
        private String descripcion;

        @Builder.Default
        private VisibilidadObservacion visibilidad = VisibilidadObservacion.INTERNA;

        @Builder.Default
        private List<Long> documentoIds = new ArrayList<>();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DerivacionResponse {
        private Long id;
        private Long expedienteId;
        private String numeroExpediente;
        private String asunto;
        private Long oficinaOrigenId;
        private String oficinaOrigenNombre;
        private Long oficinaDestinoId;
        private String oficinaDestinoNombre;
        private Long usuarioDerivadorId;
        private String usuarioDerivadorNombre;
        private Long usuarioReceptorId;
        private String usuarioReceptorNombre;
        private String motivo;
        private String indicaciones;
        private String motivoDevolucion;
        private OffsetDateTime fechaDerivacion;
        private OffsetDateTime fechaRecepcion;
        private EstadoDerivacion estado;
        private String observaciones;
        private PrioridadExpediente prioridad;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AsignacionResponse {
        private Long id;
        private Long expedienteId;
        private String numeroExpediente;
        private Long oficinaId;
        private String oficinaNombre;
        private Long usuarioAsignadorId;
        private String usuarioAsignadorNombre;
        private Long usuarioAsignadoId;
        private String usuarioAsignadoNombre;
        private Long usuarioAnteriorId;
        private String usuarioAnteriorNombre;
        private String motivo;
        private String indicaciones;
        private OffsetDateTime fechaAsignacion;
        private OffsetDateTime fechaFin;
        private EstadoAsignacion estado;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ActuacionResponse {
        private Long id;
        private Long expedienteId;
        private TipoActuacionExpediente tipo;
        private String descripcion;
        private Long usuarioResponsableId;
        private String usuarioResponsableNombre;
        private Long oficinaId;
        private String oficinaNombre;
        private OffsetDateTime fechaActuacion;
        private VisibilidadObservacion visibilidad;
        @Builder.Default
        private List<DocumentoSimpleDto> documentos = new ArrayList<>();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DocumentoSimpleDto {
        private Long id;
        private String nombreOriginal;
        private Long tamanoBytes;
        private String tipoMime;
        private String hashSha256;
        private String tipoDocumento;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OficinaSimpleDto {
        private Long id;
        private String codigo;
        private String nombre;
        private String sigla;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UsuarioSimpleDto {
        private Long id;
        private String username;
        private String nombreCompleto;
        private String cargo;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecorridoNodoDto {
        private Integer orden;
        private Long oficinaId;
        private String oficinaNombre;
        private String accion;
        private OffsetDateTime fechaEntrada;
        private OffsetDateTime fechaSalida;
        private boolean actual;
        private String responsableNombre;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PlazoSeguimientoDto {
        private OffsetDateTime fechaInicio;
        private Integer plazoDias;
        private String unidadPlazo;
        private OffsetDateTime fechaLimite;
        private Long diasTranscurridos;
        private Long diasRestantes;
        private String estadoPlazo; // SIN_PLAZO_CONFIGURADO, EN_PLAZO, PROXIMO_VENCIMIENTO, VENCIDO, SUSPENDIDO
        private String porcentajeTranscurrido;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SeguimientoInternoResponse {
        private ExpedienteResponse expediente;
        private OficinaSimpleDto oficinaOrigen;
        private OficinaSimpleDto oficinaActual;
        private UsuarioSimpleDto responsableActual;
        private EstadoExpediente estadoExpediente;
        @Builder.Default
        private List<DerivacionResponse> derivaciones = new ArrayList<>();
        @Builder.Default
        private List<AsignacionResponse> asignaciones = new ArrayList<>();
        @Builder.Default
        private List<ActuacionResponse> actuaciones = new ArrayList<>();
        @Builder.Default
        private List<ExpedienteHistorialResponse> timeline = new ArrayList<>();
        @Builder.Default
        private List<RecorridoNodoDto> recorrido = new ArrayList<>();
        private PlazoSeguimientoDto plazo;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EstadisticasDerivacionResponse {
        private long derivacionesEnviadas;
        private long derivacionesPendientes;
        private long derivacionesRecibidas;
        private long derivacionesRechazadas;
        private long derivacionesDevueltas;
        private long expedientesAsignados;
        private long expedientesSinResponsable;
        private long totalExpedientesOficina;
        private long proximosAVencer;
        private long vencidos;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CargaTrabajadorDto {
        private Long usuarioId;
        private String username;
        private String nombreCompleto;
        private String cargo;
        private long totalAsignados;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PanelOficinaResponse {
        private OficinaSimpleDto oficina;
        private EstadisticasDerivacionResponse estadisticas;
        @Builder.Default
        private List<ExpedienteResponse> pendientesAsignacion = new ArrayList<>();
        @Builder.Default
        private List<ExpedienteResponse> enAtencion = new ArrayList<>();
        @Builder.Default
        private List<CargaTrabajadorDto> cargaTrabajadores = new ArrayList<>();
    }
}
