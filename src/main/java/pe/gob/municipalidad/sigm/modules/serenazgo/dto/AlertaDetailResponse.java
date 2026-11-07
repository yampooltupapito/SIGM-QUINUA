package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.CanalAlertaCiudadana;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoAlertaCiudadana;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.NivelConfidencialidad;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.PrioridadIncidenciaSeguridad;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertaDetailResponse {

    private UUID id;
    private String codigo;
    private String titulo;
    private UUID tipoId;
    private String tipoNombre;
    private String tipoCodigo;
    private CanalAlertaCiudadana canalOrigen;
    private String canalOrigenDesc;
    private String descripcion;
    private OffsetDateTime fechaRecepcion;
    private OffsetDateTime fechaOcurrencia;
    private PrioridadIncidenciaSeguridad prioridad;
    private String prioridadDesc;
    private String ubicacionReferencia;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private UUID sectorId;
    private String sectorNombre;
    private UUID zonaId;
    private String zonaNombre;
    private EstadoAlertaCiudadana estado;
    private String estadoDesc;
    private Long reportanteUsuarioId;
    private Boolean reportanteAnonimo;
    private String reportanteNombre;
    private String reportanteEmail;
    private String reportanteTelefono;
    private UUID incidenciaId;
    private String incidenciaCodigo;
    private String incidenciaTitulo;
    private Long oficinaResponsableId;
    private String oficinaResponsableNombre;
    private NivelConfidencialidad nivelConfidencialidad;
    private String observacionesInternas;
    private String motivoCierre;
    private String resultadoCiudadano;
    private String motivoDescarte;
    private Long version;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @Builder.Default
    private List<AlertaHistorialResponse> historial = new ArrayList<>();

    @Builder.Default
    private List<AlertaDocumentoResponse> documentos = new ArrayList<>();

    @Builder.Default
    private List<AlertaSolicitudInfoResponse> solicitudesInfo = new ArrayList<>();

    public String getCodigoSeguimiento() {
        return codigo;
    }
}
