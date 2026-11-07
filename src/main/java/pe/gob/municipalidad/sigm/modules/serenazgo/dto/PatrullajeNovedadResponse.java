package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoNovedadPatrullaje;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatrullajeNovedadResponse {
    private UUID id;
    private TipoNovedadPatrullaje tipoNovedad;
    private String tipoNovedadDescripcion;
    private OffsetDateTime fechaHora;
    private String descripcion;
    private UUID personalReportaId;
    private String personalReportaNombre;
    private UUID incidenciaVinculadaId;
    private String incidenciaVinculadaCodigo;
    private String observaciones;
}
