package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoContrato;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortalComercianteContratoResponse {
    private UUID contratoId;
    private String codigoContrato;
    private String codigoSeguro;
    private UUID puestoId;
    private String puestoCodigo;
    private String mercadoNombre;
    private String tipoContratoCodigo;
    private LocalDate fechaElaboracion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private EstadoContrato estado;
    private String estadoLabel;
    private BigDecimal montoCanonMensual;
    private String moneda;
    private boolean formalizado;
    private Long documentoId;
}
