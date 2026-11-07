package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoComerciante;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoComerciante;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Ficha técnica integral de un comerciante con todas sus pestañas de detalle.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComercianteDetailResponse {

    private UUID id;
    private String codigoComerciante;
    private TipoComerciante tipoComerciante;
    private String tipoComercianteDescripcion;

    // Identidad Persona Natural
    private Long personaId;
    private String nombres;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String nombreCompleto;

    // Identidad Persona Jurídica
    private UUID personaJuridicaId;
    private String ruc;
    private String razonSocial;
    private String nombreComercial;
    private String domicilioFiscal;
    private String estadoRegistroInterno;

    // Documento del Titular
    private String tipoDocumento;
    private String numeroDocumento;
    private String documentoEnmascarado;

    // Contacto
    private String telefono;
    private String correo;
    private String direccion;

    // Estado y Auditoría
    private EstadoComerciante estado;
    private String estadoDescripcion;
    private LocalDate fechaRegistro;
    private String observaciones;
    private Long version;

    // Sub-colecciones para pestañas
    @Builder.Default
    private List<ActividadComercianteResponse> actividades = new ArrayList<>();

    @Builder.Default
    private List<RepresentanteResponse> representantes = new ArrayList<>();

    @Builder.Default
    private List<ComercianteDocumentoResponse> documentos = new ArrayList<>();

    @Builder.Default
    private List<ComerciantePuestoAsociadoResponse> puestosAsociados = new ArrayList<>();

    @Builder.Default
    private List<ComercianteAutorizacionResponse> autorizaciones = new ArrayList<>();

    @Builder.Default
    private List<ComercianteExpedienteResponse> expedientes = new ArrayList<>();

    @Builder.Default
    private List<ComercianteCambioEstadoResponse> historialEstados = new ArrayList<>();

    public String getTitularNombre() {
        if (tipoComerciante == TipoComerciante.PERSONA_JURIDICA) {
            return razonSocial != null ? razonSocial : "";
        }
        return nombreCompleto != null ? nombreCompleto : "";
    }

    public boolean isPersonaNatural() {
        return tipoComerciante == TipoComerciante.PERSONA_NATURAL;
    }

    public boolean isPersonaJuridica() {
        return tipoComerciante == TipoComerciante.PERSONA_JURIDICA;
    }
}
