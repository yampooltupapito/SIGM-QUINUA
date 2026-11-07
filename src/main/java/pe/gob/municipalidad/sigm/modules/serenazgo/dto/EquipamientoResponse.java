package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoEquipamientoSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoEquipamientoSerenazgo;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipamientoResponse {

    private UUID id;
    private String codigoInterno;
    private TipoEquipamientoSerenazgo tipoEquipo;
    private String tipoEquipoDesc;
    private String descripcion;
    private String marca;
    private String modelo;
    private String numeroSerie;
    private String codigoPatrimonial;
    private Long oficinaResponsableId;
    private String oficinaResponsableNombre;
    private EstadoEquipamientoSerenazgo estado;
    private String estadoDesc;
    private Boolean esControladoPorCantidad;
    private Integer stockTotal;
    private Integer stockDisponible;
    private UUID responsableId;
    private String responsableNombre;
    private UUID unidadMovilId;
    private String unidadMovilInfo;
    private LocalDate fechaAsignacion;
    private LocalDate fechaIncorporacion;
    private String observaciones;
    private Boolean disponibleParaAsignacion;
}
