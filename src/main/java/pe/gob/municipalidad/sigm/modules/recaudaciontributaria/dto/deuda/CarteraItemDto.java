package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.deuda;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.ClasificacionDeudaTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoExigibilidadObligacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoObligacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TramoAntiguedadDeuda;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarteraItemDto {

    private UUID obligacionId;
    private String codigoObligacion;

    private UUID contribuyenteId;
    private String contribuyenteNombre;
    private String contribuyenteNumeroDoc;
    private String contribuyenteTipoDoc;

    private String conceptoCodigo;
    private String conceptoNombre;

    private String periodoAnio;
    private Integer cuotaNumero;
    private Integer totalCuotas;

    private String predioCodigo;
    private String predioDireccion;

    private BigDecimal importeDeterminado;
    private BigDecimal importePagado;
    private BigDecimal saldo;
    private String moneda;

    private LocalDate fechaVencimiento;
    private long diasMorosidad;
    private TramoAntiguedadDeuda tramoAntiguedad;

    private EstadoObligacionTributaria estadoObligacion;
    private EstadoExigibilidadObligacion estadoExigibilidad;
    private ClasificacionDeudaTributaria clasificacion;

    private UUID seguimientoId;
    private String responsableNombre;
    private String oficinaNombre;
    private int totalGestiones;
    private int totalCompromisos;
}
