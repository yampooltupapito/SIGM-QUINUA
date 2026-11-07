package pe.gob.municipalidad.sigm.modules.mesapartes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DTOs para el registro de solicitudes ciudadanas, formularios dinámicos, constancias y seguimiento.
 */
public final class MpvSolicitudDtos {

    private MpvSolicitudDtos() {}

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EsquemaTramiteResponse {
        private Long tipoTramiteId;
        private String codigo;
        private String nombre;
        private String descripcion;
        private String oficinaResponsableNombre;
        private Integer plazoDias;
        private String unidadPlazo;
        private Boolean permitePresentacionVirtual;
        private List<RequisitoDocumentalDto> requisitos;
        private FormularioVersionDto formulario;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FormularioVersionDto {
        private Long versionId;
        private Integer version;
        private String codigoFormulario;
        private String nombreFormulario;
        private List<FormularioCampoDto> campos;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FormularioCampoDto {
        private Long id;
        private String nombreCampo;
        private String etiqueta;
        private String tipoCampo;
        private Boolean obligatorio;
        private Integer longitudMaxima;
        private String valorDefecto;
        private String placeholder;
        private String mensajeAyuda;
        private String reglaValidacion;
        private Integer orden;
        private List<FormularioOpcionDto> opciones;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FormularioOpcionDto {
        private String valor;
        private String etiqueta;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RequisitoDocumentalDto {
        private Long id;
        private String codigo;
        private String nombre;
        private String descripcion;
        private Boolean obligatorio;
        private String formatosPermitidos;
        private Integer tamanoMaximoMb;
        private Integer orden;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GuardarBorradorSolicitudRequest {
        @NotNull(message = "El tipo de trámite es obligatorio.")
        private Long tipoTramiteId;

        @NotBlank(message = "El asunto no puede estar vacío.")
        @Size(max = 255, message = "El asunto no puede superar los 255 caracteres.")
        private String asunto;

        @Size(max = 4000, message = "La descripción no puede superar los 4000 caracteres.")
        private String descripcion;

        private Map<String, String> valoresFormulario;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PresentarSolicitudRequest {
        private String claveIdempotencia;

        @Builder.Default
        private Boolean declaracionVeracidad = true;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConstanciaPresentacionResponse {
        private UUID uuid;
        private String numeroExpediente;
        private String fechaPresentacion;
        private String tipoTramiteNombre;
        private String asunto;
        private String descripcion;
        private String solicitanteNombreCompleto;
        private String solicitanteTipoNumeroDocumento;
        private String solicitanteCorreo;
        private String solicitanteTelefono;
        private String solicitanteDireccion;
        private String oficinaResponsableNombre;
        private Integer totalDocumentos;
        private List<DocumentoAdjuntoResumenDto> documentos;
        private Map<String, String> camposDinamicos;
        private String estado;
        private String urlVerificacion;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DocumentoAdjuntoResumenDto {
        private UUID documentoUuid;
        private String nombreArchivo;
        private String tipoRelacion;
        private Long tamanoBytes;
        private String sha256;
        private String fechaAsociacion;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MisSolicitudesItemResponse {
        private Long id;
        private UUID uuid;
        private String numeroExpediente;
        private String asunto;
        private String tipoTramiteNombre;
        private String fechaRegistro;
        private String fechaPresentacion;
        private String estado;
        private String oficinaResponsableNombre;
        private Integer totalDocumentos;
        private Boolean esBorrador;
        private Boolean tieneObservaciones;
        private String resultadoFinal;
        private Boolean esResuelto;
        private Long documentoResolucionId;
        private String codigoSeguimiento;
        private Integer progresoEtapa;
        private String estadoDescripcionCiudadana;
    }
}
