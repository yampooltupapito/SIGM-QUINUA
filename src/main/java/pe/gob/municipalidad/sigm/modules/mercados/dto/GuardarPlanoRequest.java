package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuardarPlanoRequest {

    @NotNull(message = "El ID de la versión es obligatorio")
    private UUID versionId;

    @Size(max = 500, message = "La descripción de cambios no debe exceder 500 caracteres")
    private String descripcionCambios;

    @Valid
    @Builder.Default
    private List<ElementoPlanoRequest> elementos = new ArrayList<>();

    // Configuración de imagen de referencia
    private String imagenReferenciaUrl;
    private BigDecimal imagenEscala;
    private BigDecimal imagenPosX;
    private BigDecimal imagenPosY;
    private BigDecimal imagenOpacidad;
    private Boolean imagenVisible;
    private Boolean imagenBloqueada;

    // Control de concurrencia optimista
    private Long version;
}
