package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoComerciante;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoComerciante;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO de respuesta para listados y vistas generales de comerciantes.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComercianteResponse {

    private UUID id;
    private String codigoComerciante;
    private TipoComerciante tipoComerciante;
    private String tipoComercianteDescripcion;

    private String titularNombre;
    private String tipoDocumento;
    private String documentoEnmascarado;
    private String documentoCompleto; // Solo si posee permiso MERCADOS_COMERCIANTE_DATOS_SENSIBLES

    private String telefono;
    private String correo;
    private String direccion;

    private EstadoComerciante estado;
    private String estadoDescripcion;
    private LocalDate fechaRegistro;

    private String actividadPrincipal;
    private List<String> actividades;

    private Long version;
}
