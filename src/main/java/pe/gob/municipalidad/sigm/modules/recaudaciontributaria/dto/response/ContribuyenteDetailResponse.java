package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoContribuyente;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoVerificacionDocumento;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.MedioContactoPreferido;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoContribuyente;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContribuyenteDetailResponse {
    private UUID id;
    private String codigoContribuyente;
    private Long personaId;
    private TipoContribuyente tipoContribuyente;
    private String tipoContribuyenteDesc;
    private String tipoDocumento;
    private String numeroDocumento;
    private String numeroDocumentoEnmascarado;
    private String nombreRazonSocial;
    private String nombreComercial;

    private String nombres;
    private String primerApellido;
    private String segundoApellido;

    private String representanteLegalNombre;
    private String representanteLegalDocumento;
    private String representanteLegalDocumentoEnmascarado;

    private String domicilioFiscal;
    private String referenciaDomicilio;

    private String telefono;
    private String telefonoEnmascarado;
    private String telefonoAlternativo;
    private String correo;
    private String correoEnmascarado;
    private MedioContactoPreferido medioContactoPreferido;
    private String medioContactoPreferidoDesc;

    private EstadoContribuyente estado;
    private String estadoDesc;
    private String badgeClass;

    private EstadoVerificacionDocumento estadoVerificacion;
    private String estadoVerificacionDesc;
    private String estadoVerificacionBadgeClass;

    private LocalDate fechaRegistro;
    private String oficinaRegistroNombre;
    private String observaciones;
    private Long version;

    private List<RepresentanteResponse> representantes;
    private List<DomicilioResponse> domicilios;
    private List<ContactoResponse> contactos;
    private List<HistorialEstadoResponse> historialEstados;
}
