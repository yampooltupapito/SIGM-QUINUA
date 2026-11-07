package pe.gob.municipalidad.sigm.modules.serenazgo.dto.estadisticas;

import lombok.*;

/**
 * Elemento de distribución categórica para gráficos de barra o pastel.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistribucionItemDto {

    private String clave;
    private String etiqueta;
    private Long cantidad;
    private Double porcentaje;
}
