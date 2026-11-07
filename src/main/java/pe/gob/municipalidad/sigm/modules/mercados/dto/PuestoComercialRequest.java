package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoFisicoPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPuesto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO general para solicitudes de creación o modificación de puestos comerciales.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PuestoComercialRequest {

    private UUID mercadoId;

    private UUID sectorId;

    private UUID pabellonId;

    private UUID zonaId;

    @NotNull(message = "El tipo de puesto es obligatorio.")
    private Long tipoPuestoId;

    @NotBlank(message = "El código identificador es obligatorio.")
    @Size(max = 50, message = "El código no debe superar los 50 caracteres.")
    private String codigo;

    @NotBlank(message = "El número visible es obligatorio.")
    @Size(max = 30, message = "El número no debe superar los 30 caracteres.")
    private String numero;

    @Size(max = 150, message = "El nombre referencial no debe superar los 150 caracteres.")
    private String nombreReferencial;

    @Size(max = 500, message = "La descripción no debe superar los 500 caracteres.")
    private String descripcion;

    @DecimalMin(value = "0.00", message = "El área en metros cuadrados no puede ser negativa.")
    @Digits(integer = 6, fraction = 2, message = "El área debe tener hasta 6 enteros y 2 decimales.")
    private BigDecimal areaMetrosCuadrados;

    @DecimalMin(value = "0.00", message = "El ancho no puede ser negativo.")
    @Digits(integer = 6, fraction = 2, message = "El ancho debe tener hasta 6 enteros y 2 decimales.")
    private BigDecimal anchoMetros;

    @DecimalMin(value = "0.00", message = "El largo no puede ser negativo.")
    @Digits(integer = 6, fraction = 2, message = "El largo debe tener hasta 6 enteros y 2 decimales.")
    private BigDecimal largoMetros;

    @Min(value = 1, message = "El nivel debe ser al menos 1.")
    @Max(value = 10, message = "El nivel no puede superar el piso 10.")
    @Builder.Default
    private Integer nivel = 1;

    @Size(max = 255, message = "La referencia de ubicación no debe superar los 255 caracteres.")
    private String ubicacionReferencia;

    @Builder.Default
    private EstadoFisicoPuesto estadoFisico = EstadoFisicoPuesto.OPERATIVO;

    @Builder.Default
    private EstadoPuesto estado = EstadoPuesto.DISPONIBLE;

    private Long version;
}
