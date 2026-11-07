package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.CanalAlertaCiudadana;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoAlertaCiudadana;
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
public class AlertaResponse {

    private UUID id;
    private String codigo;
    private String titulo;
    private UUID tipoId;
    private String tipoNombre;
    private CanalAlertaCiudadana canalOrigen;
    private String canalOrigenDesc;
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
    private Boolean reportanteAnonimo;
    private String reportanteNombre;
    private String reportanteTelefono;
    private NivelConfidencialidad nivelConfidencialidad;
    private UUID incidenciaId;
    private String incidenciaCodigo;
    private Boolean tieneSolicitudPendiente;

    public String getCodigoSeguimiento() {
        return codigo;
    }

    public String getTipoReporteNombre() {
        return tipoNombre;
    }
}
