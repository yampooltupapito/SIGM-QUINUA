package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoPersonalResponse {
    private UUID id;
    private Long documentoId;
    private String nombreArchivo;
    private String tipoDocumento;
    private String numeroDocumento;
    private String descripcion;
    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;
    private String estado;
    private String mimeType;
    private Long tamanoBytes;
    private OffsetDateTime createdAt;
}
