package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.CanalReporteIncidencia;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoIncidenciaSeguridad;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.NivelConfidencialidad;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.PrioridadIncidenciaSeguridad;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidenciaResponse {

    private UUID id;
    private String codigo;
    private String titulo;
    private String descripcion;
    private UUID tipoId;
    private String tipoNombre;
    private String tipoCodigo;
    private PrioridadIncidenciaSeguridad prioridad;
    private String prioridadDesc;
    private EstadoIncidenciaSeguridad estado;
    private String estadoDesc;
    private CanalReporteIncidencia canalReporte;
    private String canalReporteDesc;
    private UUID sectorId;
    private String sectorNombre;
    private String sectorCodigo;
    private UUID zonaId;
    private String zonaNombre;
    private String ubicacionReferencia;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private NivelConfidencialidad nivelConfidencialidad;
    private Boolean reportanteAnonimo;
    private String reportanteNombre;
    private String reportanteTelefono;
    private OffsetDateTime fechaReporte;
    private OffsetDateTime fechaOcurrencia;
    private Integer totalDocumentos;
    private Integer totalComentarios;
    private Long version;
    private OffsetDateTime createdAt;
}
