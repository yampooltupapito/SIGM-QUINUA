package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortalComercianteDocumentoResponse {
    private String id;
    private String nombreDocumento;
    private String tipoDocumento;
    private String codigoReferencia;
    private String puestoCodigo;
    private String mercadoNombre;
    private LocalDate fechaEmision;
    private String estado;
    private String urlDescarga;
    private String urlVerificacion;
    private String extension;
}
