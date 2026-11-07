package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatrullajeEquipamientoResponse {
    private UUID id;
    private UUID equipamientoId;
    private String codigoPatrimonial;
    private String codigoInterno;
    private String tipoEquipo;
    private String descripcion;
    private Integer cantidad;
    private String estadoEntrega;
    private String estadoDevolucion;
    private OffsetDateTime fechaDevolucion;
    private String observaciones;
}
