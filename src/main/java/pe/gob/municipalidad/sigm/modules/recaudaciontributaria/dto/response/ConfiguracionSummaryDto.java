package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoConfiguracionConcepto;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionSummaryDto {
    private UUID id;
    private UUID conceptoId;
    private String conceptoCodigo;
    private String conceptoNombre;
    private UUID periodoId;
    private Integer anioPeriodo;
    private Integer numeroVersion;
    private EstadoConfiguracionConcepto estado;
    private String estadoDesc;
    private String badgeClass;
    private String normaNumero;
    private LocalDate fechaInicioVigencia;
    private LocalDate fechaFinVigencia;
    private int cantidadParametros;
    private int cantidadReglas;
    private String usuarioCreador;
}
