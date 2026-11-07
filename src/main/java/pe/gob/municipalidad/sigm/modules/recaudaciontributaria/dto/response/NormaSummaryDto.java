package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NormaSummaryDto {
    private UUID id;
    private String tipoNorma;
    private String numeroNorma;
    private String entidadEmisora;
    private String descripcion;
    private String fechaPublicacion;
    private String fechaInicioVigencia;
    private String estadoValidacion;
    private String badgeClass;
    private String validador;
}
