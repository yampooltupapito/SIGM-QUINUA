package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtencionEquipamientoResponse {

    private UUID id;
    private UUID equipamientoId;
    private String codigoInterno;
    private String descripcion;
    private String tipoEquipoDesc;
    private Integer cantidad;
    private OffsetDateTime fechaAsignacion;
    private OffsetDateTime fechaDevolucion;
    private Boolean devuelto;
    private String observaciones;

    public String getEquipamientoCodigo() {
        return codigoInterno;
    }

    public String getEquipamientoNombre() {
        return descripcion;
    }

    public String getEquipamientoTipo() {
        return tipoEquipoDesc;
    }

    /** El detalle de atención no registra custodio individual del equipo. */
    public String getCustodioNombre() {
        return null;
    }

    public String getFechaAsignacionFormateada() {
        return FormatoFechaVista.fechaHora(fechaAsignacion);
    }

    public String getFechaDevolucionFormateada() {
        return FormatoFechaVista.fechaHora(fechaDevolucion);
    }
}
