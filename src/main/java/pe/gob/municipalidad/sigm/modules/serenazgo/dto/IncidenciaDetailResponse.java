package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.CanalReporteIncidencia;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoIncidenciaSeguridad;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.NivelConfidencialidad;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.OrigenHechoIncidencia;
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
public class IncidenciaDetailResponse {

    private UUID id;
    private String codigo;
    private String titulo;
    private String descripcion;

    // Tipología
    private UUID tipoId;
    private String tipoNombre;
    private String tipoCodigo;
    private String categoriaDesc;

    // Prioridad y Estado
    private PrioridadIncidenciaSeguridad prioridad;
    private String prioridadDesc;
    private EstadoIncidenciaSeguridad estado;
    private String estadoDesc;

    // Origen y Canal
    private OrigenHechoIncidencia origenHecho;
    private String origenHechoDesc;
    private CanalReporteIncidencia canalReporte;
    private String canalReporteDesc;

    // Ubicación geográfica
    private UUID sectorId;
    private String sectorNombre;
    private String sectorCodigo;
    private String sectorColorHex;
    private UUID zonaId;
    private String zonaNombre;
    private String zonaCodigo;
    private String ubicacionReferencia;
    private BigDecimal latitud;
    private BigDecimal longitud;

    // Oficina y Personal
    private Long oficinaResponsableId;
    private String oficinaResponsableNombre;
    private String usuarioRegistroNombre;
    private UUID responsableId;
    private String responsableNombre;
    private UUID personalReportaId;
    private String personalReportaNombre;
    private UUID programacionServicioId;
    private String programacionCodigo;

    // Confidencialidad y Reportante
    private NivelConfidencialidad nivelConfidencialidad;
    private Boolean reportanteAnonimo;
    private String reportanteNombre;
    private String reportanteTelefono;
    private String reportanteDireccion;

    // Observaciones y Cierre
    private String observacionesInternas;
    private String resultadoAtencion;
    private String motivoCierre;
    private String motivoDescarte;
    private OffsetDateTime fechaCierre;

    // Fechas y Auditoría
    private OffsetDateTime fechaReporte;
    private OffsetDateTime fechaOcurrencia;
    private Long version;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    // Colecciones hijas
    @Builder.Default
    private List<IncidenciaHistorialResponse> historial = new ArrayList<>();

    @Builder.Default
    private List<IncidenciaDocumentoResponse> documentos = new ArrayList<>();

    @Builder.Default
    private List<IncidenciaComentarioResponse> comentarios = new ArrayList<>();
}
