package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoRelacionContribuyentePredio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoRelacionContribuyentePredio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelacionContribuyenteResponse {
    private UUID id;
    private UUID contribuyenteId;
    private String codigoContribuyente;
    private String tipoDocumento;
    private String numeroDocumentoEnmascarado;
    private String nombreRazonSocial;
    private TipoRelacionContribuyentePredio tipoRelacion;
    private String tipoRelacionDesc;
    private BigDecimal porcentajeParticipacion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Boolean esPrincipal;
    private EstadoRelacionContribuyentePredio estado;
    private String estadoDesc;
    private String observaciones;

    // Campos referenciales a nivel predio
    private UUID predioId;
    private String codigoPredio;
    private String codigoCatastral;
    private String direccionPredio;

    public String getNombreContribuyente() {
        return nombreRazonSocial;
    }

    public BigDecimal getPorcentajePropiedad() {
        return porcentajeParticipacion;
    }

    public String getNumeroDocumento() {
        return numeroDocumentoEnmascarado;
    }

    public boolean isActivo() {
        return estado == EstadoRelacionContribuyentePredio.ACTIVO;
    }
}
