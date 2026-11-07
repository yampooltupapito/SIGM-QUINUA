package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComparacionVersionesResponse {
    private UUID conceptoId;
    private String conceptoCodigo;
    private String conceptoNombre;

    // Versión Base (Anterior)
    private UUID versionBaseId;
    private Integer numeroVersionBase;
    private String estadoVersionBase;
    private String normaVersionBase;
    private String fechaVigenciaBase;

    // Versión Comparada (Nueva)
    private UUID versionComparadaId;
    private Integer numeroVersionComparada;
    private String estadoVersionComparada;
    private String normaVersionComparada;
    private String fechaVigenciaComparada;

    // Parámetros analizados
    @Builder.Default
    private List<DiferenciaParametroDto> parametros = new ArrayList<>();

    private int totalModificados;
    private int totalAgregados;
    private int totalEliminados;
    private int totalSinCambio;
}
