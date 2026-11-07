package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContribuyenteSummaryDto {
    private UUID id;
    private String codigoContribuyente;
    private String tipoContribuyente;
    private String tipoContribuyenteDesc;
    private String tipoDocumento;
    private String numeroDocumento;
    private String numeroDocumentoEnmascarado;
    private String nombreRazonSocial;
    private String domicilioFiscal;
    private String telefono;
    private String correo;
    private String estado;
    private String estadoDesc;
    private String badgeClass;
    private String fechaRegistro;
}
