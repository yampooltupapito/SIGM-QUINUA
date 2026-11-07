package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoAutorizacion;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortalComercianteAutorizacionResponse {
    private UUID autorizacionId;
    private String codigoAutorizacion;
    private String numeroAutorizacion;
    private String codigoSeguro;
    private String tipoAutorizacion;
    private UUID puestoId;
    private String puestoCodigo;
    private String mercadoNombre;
    private LocalDate fechaEmision;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private EstadoAutorizacion estado;
    private String estadoLabel;
    private String giroComercial;
    private boolean vigente;
    private String urlVerificacionQr;
}
