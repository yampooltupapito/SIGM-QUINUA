package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoEquipamientoSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoEquipamientoSerenazgo;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipamientoFilter {

    private String q; // Código, descripción, serie o código patrimonial
    private TipoEquipamientoSerenazgo tipoEquipo;
    private Long oficinaId;
    private EstadoEquipamientoSerenazgo estado;
    private UUID responsableId;
}
