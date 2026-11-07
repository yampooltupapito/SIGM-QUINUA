package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoVisitaPunto;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.MetodoRegistroVisita;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatrullajeVisitaPuntoResponse {
    private UUID id;
    private UUID puntoControlId;
    private Integer orden;
    private String codigoPunto;
    private String nombrePunto;
    private String referenciaUbicacion;
    private Integer ordenVisita;
    private OffsetDateTime fechaHoraRegistro;
    private UUID personalRegistroId;
    private String personalRegistroNombre;
    private MetodoRegistroVisita metodoRegistro;
    private EstadoVisitaPunto estadoVisita;
    private String motivoOmision;
    private BigDecimal latitudRegistro;
    private BigDecimal longitudRegistro;
    private String observaciones;
}
