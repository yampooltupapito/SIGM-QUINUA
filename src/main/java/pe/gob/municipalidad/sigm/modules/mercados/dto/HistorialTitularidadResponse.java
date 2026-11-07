package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoOperacionTitularidad;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistorialTitularidadResponse {

    private Long id;
    private UUID puestoId;
    private String puestoNumero;
    private String mercadoNombre;

    private UUID solicitudId;
    private String codigoSolicitud;

    private UUID asignacionAnteriorId;
    private String asignacionAnteriorCodigo;
    private UUID asignacionNuevaId;
    private String asignacionNuevaCodigo;

    private UUID titularAnteriorId;
    private String titularAnteriorNombre;
    private String titularAnteriorDocumento;

    private UUID titularNuevoId;
    private String titularNuevoNombre;
    private String titularNuevoDocumento;

    private TipoOperacionTitularidad tipoOperacion;
    private String tipoOperacionLabel;

    private LocalDate fechaEfectiva;
    private Long documentoSustentoId;
    private String motivo;
    private String usuarioRegistro;
    private OffsetDateTime createdAt;
}
