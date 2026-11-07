package pe.gob.municipalidad.sigm.modules.mesapartes.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoExpediente;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class SeguimientoDtos {

    private SeguimientoDtos() {}

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConsultaPublicaRequest {
        @NotBlank(message = "El código de seguimiento o número de expediente es obligatorio")
        private String codigoSeguimiento;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EtapaProgresoDto {
        private int numeroEtapa;
        private String nombre;
        private String estado; // COMPLETADO, EN_CURSO, PENDIENTE, ALTERNATIVO
        private String fecha;
        private String descripcion;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConsultaPublicaResponse {
        private boolean encontrado;
        private String numeroExpediente;
        private String tipoTramite;
        private OffsetDateTime fechaPresentacion;
        private EstadoExpediente estado;
        private String estadoDescripcionCiudadana;
        private int etapaProgreso;
        private List<EtapaProgresoDto> etapas;
        private String oficinaActual;
        private OffsetDateTime fechaUltimoMovimiento;
        private boolean esObservado;
        private boolean esConcluido;
        private String mensajeInformativo;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EventoLineaTiempoCiudadanaDto {
        private OffsetDateTime fecha;
        private String estado;
        private String titulo;
        private String descripcion;
        private String oficina;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ObservacionCiudadanaDto {
        private Long id;
        private OffsetDateTime fecha;
        private String descripcion;
        private String requisitoNombre;
        private String estado;
        private OffsetDateTime fechaLimite;
        private Integer diasRestantes;
        private boolean permiteSubsanar;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubsanacionCiudadanaDto {
        private Long id;
        private OffsetDateTime fechaSubsanacion;
        private String requerimiento;
        private String respuestaCiudadana;
        private String estado;
        private int totalDocumentos;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DocumentoDisponibleDto {
        private Long id;
        private UUID uuid;
        private String nombreArchivo;
        private String tipoDocumento;
        private String hashSha256;
        private Long tamanoBytes;
        private OffsetDateTime fechaEmision;
        private String urlDescarga;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SeguimientoCiudadanoDetalleDto {
        private Long expedienteId;
        private UUID uuid;
        private String numeroExpediente;
        private String codigoSeguimiento;
        private String asunto;
        private String descripcion;
        private String tipoTramite;
        private String solicitanteNombre;
        private String solicitanteDocumento;
        private String solicitanteCorreo;
        private String oficinaResponsable;
        private EstadoExpediente estado;
        private String estadoDescripcionCiudadana;
        private int etapaProgreso;
        private OffsetDateTime fechaPresentacion;
        private OffsetDateTime fechaRecepcion;
        private OffsetDateTime fechaInicioAtencion;
        private OffsetDateTime fechaLimite;
        private Integer diasRestantes;
        private String resultadoFinal;
        private OffsetDateTime fechaCierre;
        private boolean esReabierto;
        private List<EtapaProgresoDto> etapasProgreso;
        private List<EventoLineaTiempoCiudadanaDto> lineaTiempo;
        private List<ObservacionCiudadanaDto> observaciones;
        private List<SubsanacionCiudadanaDto> subsanaciones;
        private List<DocumentoDisponibleDto> documentosDisponibles;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VerificarDocumentoRequest {
        @NotBlank(message = "El código o token de verificación es obligatorio")
        private String codigoVerificacion;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VerificarDocumentoResponse {
        private boolean valido;
        private String codigoVerificacion;
        private String tipoDocumento;
        private String numeroDocumento;
        private OffsetDateTime fechaEmision;
        private String estadoDocumento; // VIGENTE, ANULADO, REVOCADO, NO_VERIFICABLE
        private String entidadEmisora;
        private String autoridadFirmante;
        private String cargoFirmante;
        private String hashSha256;
        private int vecesConsultado;
        private String mensajeLegal;
    }
}
