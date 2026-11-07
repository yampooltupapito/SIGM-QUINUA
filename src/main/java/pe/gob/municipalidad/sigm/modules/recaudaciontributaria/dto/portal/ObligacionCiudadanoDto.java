package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.portal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObligacionCiudadanoDto {
    private UUID id;
    private String codigoObligacion;
    private String conceptoNombre;
    private String conceptoCodigo;
    private String tipoDeterminacion;
    private Integer anioPeriodo;
    private Integer cuotaNumero;
    private Integer totalCuotas;
    private String predioCodigo;
    private String predioDireccion;
    private BigDecimal importeOriginal;
    private BigDecimal importeVigente;
    private BigDecimal importePagado;
    private BigDecimal saldo;
    private LocalDate fechaVencimiento;
    private String estado;
    private String estadoFinanciero;
    private String estadoExigibilidad;
    private boolean vencida;
}

