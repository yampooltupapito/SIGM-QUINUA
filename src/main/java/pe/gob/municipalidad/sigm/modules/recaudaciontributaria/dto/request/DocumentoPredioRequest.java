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

    // Alias de entrada: la ficha del predio (detail.html) envía estos nombres de campo
    // (descripcion, numeroDocumento, emisor, fechaDocumento) y antes no llegaban a ningún atributo,
    // por lo que "titulo" quedaba vacío y el documento nunca se podía guardar.
    public void setDescripcion(String descripcion) {
        this.titulo = descripcion;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroFolio = numeroDocumento;
    }

    public void setEmisor(String emisor) {
        this.entidadEmisora = emisor;
    }

    public void setFechaDocumento(LocalDate fechaDocumento) {
        this.fechaEmision = fechaDocumento;
    }
}
