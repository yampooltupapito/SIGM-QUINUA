package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoDocumentoPredio;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentoPredioRequest {

    @NotNull(message = "El tipo de documento es obligatorio")
    private TipoDocumentoPredio tipoDocumento;

    @NotBlank(message = "El título o descripción es obligatorio")
    private String titulo;

    private String archivoUrl;
    private String numeroFolio;
    private LocalDate fechaEmision;
    private String entidadEmisora;
    private String observaciones;
}
