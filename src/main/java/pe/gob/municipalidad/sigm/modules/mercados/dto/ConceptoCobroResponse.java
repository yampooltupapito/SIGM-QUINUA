package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.PeriodicidadConcepto;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoConceptoCobro;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConceptoCobroResponse {
    private UUID id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private TipoConceptoCobro tipoConcepto;
    private String tipoConceptoDescripcion;
    private PeriodicidadConcepto periodicidad;
    private String periodicidadDescripcion;
    private String estado;
    private boolean requiereAsignacion;
    private Long oficinaResponsableId;
    private String oficinaResponsableNombre;
}
