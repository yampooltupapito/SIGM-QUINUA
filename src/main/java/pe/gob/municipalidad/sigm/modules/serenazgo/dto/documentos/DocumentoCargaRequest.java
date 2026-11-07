package pe.gob.municipalidad.sigm.modules.serenazgo.dto.documentos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.NivelConfidencialidad;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.OrigenDocumentoSeguridad;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoDocumentoSeguridad;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentoCargaRequest {

    private MultipartFile archivo;

    @NotNull(message = "El tipo de documento es obligatorio")
    private TipoDocumentoSeguridad tipoDocumento;

    @Size(max = 1000, message = "La descripción no puede superar 1000 caracteres")
    private String descripcion;

    @Builder.Default
    private NivelConfidencialidad nivelConfidencialidad = NivelConfidencialidad.CONFIDENCIAL;

    @Builder.Default
    private OrigenDocumentoSeguridad origenDocumento = OrigenDocumentoSeguridad.ADMINISTRATIVO;

    // Registros vinculados
    private UUID incidenciaId;
    private UUID alertaId;
    private UUID atencionId;
    private UUID intervencionId;
    private UUID patrullajeId;

    // Metadatos de campo
    private LocalDate fechaCaptura;
    private String horaCaptura;
    private String ubicacionCaptura;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private String autoriaDeclarada;
    private String observaciones;
}
