package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoDocumentoPredio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentoPredioResponse {
    private UUID id;
    private TipoDocumentoPredio tipoDocumento;
    private String tipoDocumentoDesc;
    private String titulo;
    private String archivoUrl;
    private String numeroFolio;
    private LocalDate fechaEmision;
    private String entidadEmisora;
    private String observaciones;

    public String getNumeroDocumento() {
        return numeroFolio != null ? numeroFolio : "";
    }

    public String getDescripcion() {
        return titulo != null ? titulo : "";
    }

    public String getEmisor() {
        return entidadEmisora != null ? entidadEmisora : "";
    }

    public LocalDate getFechaDocumento() {
        return fechaEmision;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaEmision != null ? fechaEmision.atStartOfDay() : LocalDateTime.now();
    }
}
