package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoIntervencionSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.NivelConfidencialidad;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.ResultadoIntervencionSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoIntervencionSerenazgo;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntervencionDetailResponse {

    private UUID id;
    private String codigo;

    private UUID incidenciaId;
    private String incidenciaCodigo;
    private String incidenciaTitulo;
    private String incidenciaTipo;
    private String incidenciaUbicacion;

    private UUID atencionId;
    private String atencionCodigo;
    private String atencionEstado;

    private TipoIntervencionSerenazgo tipoIntervencion;
    private String tipoIntervencionDesc;

    private OffsetDateTime fechaInicio;
    private String fechaInicioFormateada;
    private OffsetDateTime fechaFin;
    private String fechaFinFormateada;

    private UUID sectorId;
    private String sectorNombre;
    private UUID zonaId;
    private String zonaNombre;
    private String ubicacionDescripcion;
    private Double latitud;
    private Double longitud;

    private UUID responsableId;
    private String responsableNombre;
    private String responsableCodigo;
    private String responsableCargo;

    private UUID unidadMovilId;
    private String unidadMovilCodigo;
    private String unidadMovilPlaca;
    private String unidadMovilTipo;

    private String descripcionActuacion;
    private ResultadoIntervencionSerenazgo resultado;
    private String resultadoDesc;

    private EstadoIntervencionSerenazgo estado;
    private String estadoDesc;

    private NivelConfidencialidad nivelConfidencialidad;
    private String nivelConfidencialidadDesc;

    private OffsetDateTime fechaValidacion;
    private String fechaValidacionFormateada;
    private String usuarioValidadorNombre;

    private String motivoObservacion;
    private String motivoArchivo;

    private Boolean registroRetrospectivo;
    private String motivoRegistroRetrospectivo;
    private String observaciones;

    private Long version;
    private OffsetDateTime createdAt;
    private String createdAtFormateada;
    private OffsetDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    @Builder.Default
    private List<IntervencionParticipanteResponse> participantes = new ArrayList<>();

    @Builder.Default
    private List<IntervencionActuacionResponse> actuaciones = new ArrayList<>();

    @Builder.Default
    private List<IntervencionComunicacionResponse> comunicaciones = new ArrayList<>();

    @Builder.Default
    private List<IntervencionEquipamientoResponse> equipamiento = new ArrayList<>();

    @Builder.Default
    private List<IntervencionDocumentoResponse> documentos = new ArrayList<>();

    @Builder.Default
    private List<IntervencionHistorialResponse> historial = new ArrayList<>();

    public boolean isEditable() {
        return estado != null && estado.isEditable();
    }

    public boolean isValidada() {
        return estado != null && estado.isValidada();
    }

    public boolean isObservada() {
        return estado != null && estado.isObservada();
    }

    public boolean isArchivada() {
        return estado != null && estado.isArchivada();
    }
}
