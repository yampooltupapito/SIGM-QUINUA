package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.GravedadHallazgo;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipificacionInfraccionResponse {
    private UUID id;
    private String codigo;
    private String descripcion;
    private String normaReferencia;
    private String articulo;
    private GravedadHallazgo gravedadBase;
    private String gravedadBaseLabel;
    private String autoridadCompetente;
    private LocalDate fechaInicioVigencia;
    private LocalDate fechaFinVigencia;
    private Integer versionNormativa;
    private String estado;
    private UUID documentoFuenteId;
    private Boolean esVigente;
}
