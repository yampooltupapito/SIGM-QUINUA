package pe.gob.municipalidad.sigm.modules.mesapartes.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * DTOs para el Panel de Configuración Administrativa de Mesa de Partes Virtual (Prompt 19).
 */
public final class ConfiguracionMpvDtos {

    private ConfiguracionMpvDtos() {}

    // =========================================================================
    // 1. CATÁLOGO DE TRÁMITES Y PROCEDIMIENTOS TUPA
    // =========================================================================

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TramiteAdminResponse {
        private Long id;
        private String codigo;
        private String nombre;
        private String descripcion;
        private String codigoTupa;
        private String denominacionOficial;
        private String baseNormativa;
        private String categoria;
        private Long oficinaResponsableId;
        private String oficinaResponsableNombre;
        private Boolean requiereDocumentos;
        private Boolean permitePresentacionVirtual;
        private Boolean permitePresencial;
        private Integer plazoDias;
        private UnidadPlazo unidadPlazo;
        private Integer versionActual;
        private EstadoCatalogoTramite estadoCatalogo;
        private Boolean requiereTasa;
        private BigDecimal montoTasa;
        private String cuentaTasa;
        private OffsetDateTime fechaVigenciaDesde;
        private OffsetDateTime fechaVigenciaHasta;
        private String aprobadoPor;
        private OffsetDateTime fechaAprobacion;
        private String publicadoPor;
        private OffsetDateTime fechaPublicacion;
        private String estado;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
        private Long totalExpedientesAsociados;
        private Integer totalRequisitos;
        private Boolean tieneFormularioDinamico;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GuardarTramiteRequest {
        @NotBlank(message = "El código de trámite es obligatorio.")
        @Size(max = 50, message = "El código no puede superar 50 caracteres.")
        @Pattern(regexp = "^[A-Z0-9\\-_]+$", message = "El código solo debe contener letras mayúsculas, números, guiones y subguiones.")
        private String codigo;

        @NotBlank(message = "El nombre del trámite es obligatorio.")
        @Size(max = 200, message = "El nombre no puede superar 200 caracteres.")
        private String nombre;

        @Size(max = 1000, message = "La descripción no puede superar 1000 caracteres.")
        private String descripcion;

        @Size(max = 50, message = "El código TUPA no puede superar 50 caracteres.")
        private String codigoTupa;

        @Size(max = 250, message = "La denominación oficial no puede superar 250 caracteres.")
        private String denominacionOficial;

        private String baseNormativa;

        @Size(max = 100, message = "La categoría no puede superar 100 caracteres.")
        @Builder.Default
        private String categoria = "GENERAL";

        @NotNull(message = "La oficina responsable es obligatoria.")
        private Long oficinaResponsableId;

        @Builder.Default
        private Boolean permitePresentacionVirtual = true;

        @Builder.Default
        private Boolean permitePresencial = true;

        @Builder.Default
        private Boolean requiereDocumentos = true;

        @NotNull(message = "El plazo en días es obligatorio.")
        @Min(value = 1, message = "El plazo debe ser de al menos 1 día.")
        @Max(value = 365, message = "El plazo no puede superar 365 días.")
        @Builder.Default
        private Integer plazoDias = 30;

        @NotNull(message = "La unidad de plazo es obligatoria.")
        @Builder.Default
        private UnidadPlazo unidadPlazo = UnidadPlazo.DIAS_HABILES;

        @Builder.Default
        private Boolean requiereTasa = false;

        @DecimalMin(value = "0.00", message = "El monto de tasa no puede ser negativo.")
        @Builder.Default
        private BigDecimal montoTasa = BigDecimal.ZERO;

        @Size(max = 100, message = "La cuenta bancaria no puede superar 100 caracteres.")
        private String cuentaTasa;

        private OffsetDateTime fechaVigenciaDesde;
        private OffsetDateTime fechaVigenciaHasta;

        @Size(max = 500, message = "El motivo de cambio no puede superar 500 caracteres.")
        private String motivoCambio;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TramiteVersionResponse {
        private Long id;
        private Long tipoTramiteId;
        private Integer numeroVersion;
        private String nombre;
        private String descripcion;
        private String codigoTupa;
        private String denominacionOficial;
        private String baseNormativa;
        private String oficinaResponsableNombre;
        private Integer plazoDias;
        private UnidadPlazo unidadPlazo;
        private Boolean requiereTasa;
        private BigDecimal montoTasa;
        private Boolean permiteVirtual;
        private Boolean permitePresencial;
        private EstadoCatalogoTramite estado;
        private String motivoCambio;
        private String aprobadoPor;
        private OffsetDateTime fechaAprobacion;
        private String publicadoPor;
        private OffsetDateTime fechaPublicacion;
        private OffsetDateTime createdAt;
        private String createdBy;
    }

    // =========================================================================
    // 2. REQUISITOS DOCUMENTALES
    // =========================================================================

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RequisitoAdminResponse {
        private Long id;
        private Long tipoTramiteId;
        private String codigo;
        private String nombre;
        private String descripcion;
        private Boolean obligatorio;
        private String formatosPermitidos;
        private Integer tamanoMaximoMb;
        private Integer orden;
        private String estado;
        private OffsetDateTime createdAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GuardarRequisitoRequest {
        @NotNull(message = "El tipo de trámite es obligatorio.")
        private Long tipoTramiteId;

        @NotBlank(message = "El código de requisito es obligatorio.")
        @Size(max = 50, message = "El código no puede superar 50 caracteres.")
        private String codigo;

        @NotBlank(message = "El nombre del requisito es obligatorio.")
        @Size(max = 200, message = "El nombre no puede superar 200 caracteres.")
        private String nombre;

        @Size(max = 1000, message = "La descripción no puede superar 1000 caracteres.")
        private String descripcion;

        @Builder.Default
        private Boolean obligatorio = true;

        @NotBlank(message = "Debe indicar los formatos admitidos.")
        @Builder.Default
        private String formatosPermitidos = "application/pdf,image/jpeg,image/png";

        @NotNull(message = "Debe indicar el tamaño máximo en MB.")
        @Min(value = 1, message = "El tamaño mínimo permitido es 1 MB.")
        @Max(value = 50, message = "El tamaño máximo permitido es 50 MB.")
        @Builder.Default
        private Integer tamanoMaximoMb = 15;

        @NotNull(message = "El orden es obligatorio.")
        @Min(value = 1, message = "El orden debe ser mayor o igual a 1.")
        @Builder.Default
        private Integer orden = 1;

        @Builder.Default
        private String estado = "ACTIVO";
    }

    // =========================================================================
    // 3. FORMULARIOS DINÁMICOS Y CONSTRUCTOR VISUAL
    // =========================================================================

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FormularioAdminResponse {
        private Long id;
        private Long tipoTramiteId;
        private String tipoTramiteNombre;
        private String codigo;
        private String nombre;
        private String descripcion;
        private Boolean activo;
        private Integer versionActual;
        private EstadoFormularioVersion estadoVersion;
        private Integer totalCampos;
        private OffsetDateTime updatedAt;
        private List<CampoConfigResponse> campos;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CampoConfigResponse {
        private Long id;
        private String nombreCampo;
        private String etiqueta;
        private TipoCampoFormulario tipoCampo;
        private Boolean obligatorio;
        private Integer orden;
        private String placeholder;
        private String textoAyuda;
        private Integer longitudMinima;
        private Integer longitudMaxima;
        private String expresionRegular;
        private String mensajeValidacion;
        private List<OpcionCampoResponse> opciones;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OpcionCampoResponse {
        private Long id;
        private String valor;
        private String etiqueta;
        private Integer orden;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GuardarFormularioRequest {
        @NotNull(message = "El tipo de trámite es obligatorio.")
        private Long tipoTramiteId;

        @NotBlank(message = "El código es obligatorio.")
        @Size(max = 50, message = "El código no puede superar 50 caracteres.")
        private String codigo;

        @NotBlank(message = "El nombre del formulario es obligatorio.")
        @Size(max = 200, message = "El nombre no puede superar 200 caracteres.")
        private String nombre;

        @Size(max = 1000, message = "La descripción no puede superar 1000 caracteres.")
        private String descripcion;

        private List<GuardarCampoRequest> campos;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GuardarCampoRequest {
        private Long id;

        @NotBlank(message = "El nombre de campo (clave interna) es obligatorio.")
        @Size(max = 50, message = "El nombre de campo no puede superar 50 caracteres.")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "El nombre interno solo admite letras, números y subguiones sin espacios.")
        private String nombreCampo;

        @NotBlank(message = "La etiqueta visible es obligatoria.")
        @Size(max = 150, message = "La etiqueta no puede superar 150 caracteres.")
        private String etiqueta;

        @NotNull(message = "El tipo de campo es obligatorio.")
        private TipoCampoFormulario tipoCampo;

        @Builder.Default
        private Boolean obligatorio = false;

        @NotNull(message = "El orden es obligatorio.")
        @Min(1)
        private Integer orden;

        @Size(max = 150)
        private String placeholder;

        @Size(max = 300)
        private String textoAyuda;

        @Min(0)
        private Integer longitudMinima;

        @Min(1)
        private Integer longitudMaxima;

        @Size(max = 200)
        private String expresionRegular;

        @Size(max = 200)
        private String mensajeValidacion;

        private List<GuardarOpcionRequest> opciones;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GuardarOpcionRequest {
        @NotBlank(message = "El valor de la opción es obligatorio.")
        @Size(max = 100)
        private String valor;

        @NotBlank(message = "La etiqueta de la opción es obligatoria.")
        @Size(max = 150)
        private String etiqueta;

        @NotNull
        @Min(1)
        private Integer orden;
    }

    // =========================================================================
    // 4. CALENDARIO ADMINISTRATIVO Y FERIADOS
    // =========================================================================

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CalendarioAdminResponse {
        private Long id;
        private Integer anio;
        private LocalDate fecha;
        private String nombre;
        private TipoCalendarioFeriado tipo;
        private Boolean esLaborable;
        private Boolean aplicaTodosTramites;
        private String baseNormativa;
        private Boolean activo;
        private OffsetDateTime createdAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GuardarCalendarioRequest {
        @NotNull(message = "La fecha es obligatoria.")
        private LocalDate fecha;

        @NotBlank(message = "El nombre de la festividad o feriado es obligatorio.")
        @Size(max = 150, message = "El nombre no puede superar 150 caracteres.")
        private String nombre;

        @NotNull(message = "El tipo de feriado o día cívico es obligatorio.")
        @Builder.Default
        private TipoCalendarioFeriado tipo = TipoCalendarioFeriado.FERIADO_NACIONAL;

        @Builder.Default
        private Boolean esLaborable = false;

        @Builder.Default
        private Boolean aplicaTodosTramites = true;

        @Size(max = 250, message = "La base normativa no puede superar 250 caracteres.")
        private String baseNormativa;

        @Builder.Default
        private Boolean activo = true;
    }

    // =========================================================================
    // 5. PLANTILLAS DOCUMENTALES
    // =========================================================================

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PlantillaAdminResponse {
        private Long id;
        private String codigo;
        private String nombre;
        private String descripcion;
        private TipoDocumentalPlantilla tipoDocumental;
        private String cuerpoPlantilla;
        private Integer version;
        private String estado;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GuardarPlantillaRequest {
        @NotBlank(message = "El código es obligatorio.")
        @Size(max = 50, message = "El código no puede superar 50 caracteres.")
        private String codigo;

        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 200, message = "El nombre no puede superar 200 caracteres.")
        private String nombre;

        @Size(max = 500, message = "La descripción no puede superar 500 caracteres.")
        private String descripcion;

        @NotNull(message = "El tipo documental es obligatorio.")
        private TipoDocumentalPlantilla tipoDocumental;

        @NotBlank(message = "El contenido o cuerpo de la plantilla es obligatorio.")
        private String cuerpoPlantilla;

        @Builder.Default
        private String estado = "PUBLICADO";
    }

    // =========================================================================
    // 6. NUMERACIÓN Y CORRELATIVOS
    // =========================================================================

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NumeracionResponse {
        private Long id;
        private Integer anio;
        private Long ultimoNumero;
        private String formatoSugerido;
        private String prefijo;
        private String sufijo;
        private String entidad;
        private OffsetDateTime updatedAt;
    }

    // =========================================================================
    // 7. HISTORIAL DE AUDITORÍA Y CONTROL DE VERSIONES
    // =========================================================================

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HistorialConfiguracionResponse {
        private Long id;
        private TipoEntidadConfiguracion entidadTipo;
        private Long entidadId;
        private String codigoReferencia;
        private TipoCambioConfiguracion tipoCambio;
        private Integer versionAnterior;
        private Integer versionNueva;
        private String motivo;
        private String cambiosJson;
        private String username;
        private String ipOrigen;
        private OffsetDateTime fechaCambio;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AccionTransicionRequest {
        @Size(max = 500, message = "El motivo no puede superar 500 caracteres.")
        private String motivo;
    }
}
