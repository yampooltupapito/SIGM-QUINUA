package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoInspeccionMercado;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlantillaInspeccionResponse {

    private UUID id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private TipoInspeccionMercado tipoInspeccion;
    private String tipoInspeccionDescripcion;
    private Integer version;
    private String estado;
    private Long oficinaResponsableId;
    private String oficinaResponsableNombre;
    private LocalDate fechaAprobacion;
    private String sustentoNormativo;
    private List<ItemVerificacionDto> items;
    private OffsetDateTime createdAt;
}
