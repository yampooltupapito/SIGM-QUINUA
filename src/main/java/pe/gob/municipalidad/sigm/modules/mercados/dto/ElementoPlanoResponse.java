package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoVisualPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoElementoPlano;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElementoPlanoResponse {
    private UUID id;
    private UUID versionPlanoId;
    private TipoElementoPlano tipoElemento;
    private String tipoElementoLabel;
    private UUID referenciaEntidadId;
    private String codigoVisual;
    private String etiqueta;
    private BigDecimal coordenadaX;
    private BigDecimal coordenadaY;
    private BigDecimal ancho;
    private BigDecimal alto;
    private BigDecimal rotacion;
    private Integer ordenCapa;
    private String estiloConfigurado;

    // Campos enriquecidos dinámicamente si es un puesto comercial
    private EstadoVisualPuesto estadoVisual;
    private String estadoVisualLabel;
    private String colorHex;
    private String puestoNumero;
    private String sectorNombre;
    private String pabellonNombre;
    private String comercianteNombre;
    private Boolean ocupado;
}
