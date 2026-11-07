package pe.gob.municipalidad.sigm.common.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.common.notification.entity.NotificacionEnvio;
import pe.gob.municipalidad.sigm.common.notification.entity.NotificacionPreferencias;
import pe.gob.municipalidad.sigm.common.notification.entity.NotificacionProgramacion;
import pe.gob.municipalidad.sigm.modules.notificaciones.model.Notificacion;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Contenedor de Data Transfer Objects para el sistema de notificaciones,
 * correos electrónicos y preferencias de comunicación de SIGM-QUINUA.
 */
public class NotificationDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificacionItemDto {
        private Long id;
        private Long usuarioId;
        private String titulo;
        private String mensaje;
        private String tipo;
        private boolean leida;
        private OffsetDateTime fechaCreacion;
        private OffsetDateTime fechaLectura;
        private String urlDestino;
        private String referenciaId;
        private String origen;
        private String tiempoTranscurrido;
        private String fecha;

        public String getFecha() {
            if (fecha != null && !fecha.isBlank()) return fecha;
            if (tiempoTranscurrido != null && !tiempoTranscurrido.isBlank()) return tiempoTranscurrido;
            if (fechaCreacion != null) return fechaCreacion.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            return "";
        }

        public static NotificacionItemDto fromEntity(Notificacion n) {
            if (n == null) return null;
            return NotificacionItemDto.builder()
                    .id(n.getId())
                    .usuarioId(n.getUsuario() != null ? n.getUsuario().getId() : null)
                    .titulo(n.getTitulo())
                    .mensaje(n.getMensaje())
                    .tipo(n.getTipo())
                    .leida(n.isLeida())
                    .fechaCreacion(n.getFechaCreacion())
                    .fechaLectura(n.getFechaLectura())
                    .urlDestino(n.getUrlDestino())
                    .referenciaId(n.getReferenciaId())
                    .origen(n.getOrigen())
                    .tiempoTranscurrido(calcularTiempoTranscurrido(n.getFechaCreacion()))
                    .fecha(n.getFechaCreacion() != null ? n.getFechaCreacion().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "")
                    .build();
        }

        private static String calcularTiempoTranscurrido(OffsetDateTime fecha) {
            if (fecha == null) return "Reciente";
            Duration diff = Duration.between(fecha, OffsetDateTime.now());
            long minutes = Math.max(0, diff.toMinutes());
            if (minutes < 1) return "Hace un momento";
            if (minutes < 60) return "Hace " + minutes + " min";
            long hours = diff.toHours();
            if (hours < 24) return "Hace " + hours + " h";
            long days = diff.toDays();
            if (days < 30) return "Hace " + days + " d";
            return fecha.toLocalDate().toString();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnviarNotificacionRequest {
        @NotNull(message = "El ID del usuario destinatario es obligatorio.")
        private Long usuarioId;

        @NotBlank(message = "El título de la notificación es obligatorio.")
        @Size(max = 200, message = "El título no puede superar los 200 caracteres.")
        private String titulo;

        @NotBlank(message = "El mensaje de la notificación es obligatorio.")
        private String mensaje;

        @Pattern(regexp = "INFORMACION|INFO|EXITO|ADVERTENCIA|ALERTA|ERROR|SEGURIDAD|TRAMITE|DOCUMENTO|PAGO|APROBACION|OBSERVACION|SISTEMA",
                message = "El tipo de notificación no es válido.")
        @Builder.Default
        private String tipo = "INFORMACION";

        @Size(max = 255)
        private String urlDestino;

        @Size(max = 100)
        private String referenciaId;

        private boolean enviarCorreo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnviarNotificacionOficinaRequest {
        @NotNull(message = "El ID de la oficina destino es obligatorio.")
        private Long oficinaId;

        @NotBlank(message = "El título es obligatorio.")
        @Size(max = 200)
        private String titulo;

        @NotBlank(message = "El mensaje es obligatorio.")
        private String mensaje;

        @Pattern(regexp = "INFORMACION|INFO|EXITO|ADVERTENCIA|ALERTA|ERROR|SEGURIDAD|TRAMITE|DOCUMENTO|PAGO|APROBACION|OBSERVACION|SISTEMA",
                message = "El tipo de notificación no es válido.")
        @Builder.Default
        private String tipo = "INFORMACION";

        @Size(max = 255)
        private String urlDestino;

        @Pattern(regexp = "INTERNO|CORREO|AMBOS", message = "El canal debe ser INTERNO, CORREO o AMBOS.")
        @Builder.Default
        private String canal = "INTERNO";
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProgramarNotificacionRequest {
        private Long usuarioId;
        private Long oficinaId;

        @NotBlank(message = "El título es obligatorio.")
        @Size(max = 200)
        private String titulo;

        @NotBlank(message = "El mensaje es obligatorio.")
        private String mensaje;

        @Pattern(regexp = "INFORMACION|INFO|EXITO|ADVERTENCIA|ALERTA|ERROR|SEGURIDAD|TRAMITE|DOCUMENTO|PAGO|APROBACION|OBSERVACION|SISTEMA",
                message = "El tipo no es válido.")
        @Builder.Default
        private String tipo = "INFORMACION";

        @Pattern(regexp = "INTERNO|CORREO|AMBOS", message = "El canal debe ser INTERNO, CORREO o AMBOS.")
        @Builder.Default
        private String canal = "INTERNO";

        @NotNull(message = "La fecha y hora de programación es obligatoria.")
        @Future(message = "La fecha programada debe ser futura.")
        private OffsetDateTime fechaProgramada;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificacionPreferenciasDto {
        @NotNull(message = "La preferencia de canal interno es obligatoria.")
        private Boolean canalInterno;

        @NotNull(message = "La preferencia de canal de correo es obligatoria.")
        private Boolean canalEmail;

        @Builder.Default
        private Boolean alertasSeguridad = true;

        @NotNull(message = "La preferencia de avisos de trámites es obligatoria.")
        private Boolean avisosTramites;

        @NotNull(message = "La preferencia de avisos de documentos es obligatoria.")
        private Boolean avisosDocumentos;

        @NotNull(message = "La preferencia de avisos de pagos es obligatoria.")
        private Boolean avisosPagos;

        @NotNull(message = "La preferencia de comunicaciones generales es obligatoria.")
        private Boolean comunicacionesGenerales;

        public static NotificacionPreferenciasDto fromEntity(NotificacionPreferencias p) {
            if (p == null) {
                return NotificacionPreferenciasDto.builder()
                        .canalInterno(true)
                        .canalEmail(true)
                        .alertasSeguridad(true)
                        .avisosTramites(true)
                        .avisosDocumentos(true)
                        .avisosPagos(true)
                        .comunicacionesGenerales(true)
                        .build();
            }
            return NotificacionPreferenciasDto.builder()
                    .canalInterno(p.getCanalInterno())
                    .canalEmail(p.getCanalEmail())
                    .alertasSeguridad(p.getAlertasSeguridad())
                    .avisosTramites(p.getAvisosTramites())
                    .avisosDocumentos(p.getAvisosDocumentos())
                    .avisosPagos(p.getAvisosPagos())
                    .comunicacionesGenerales(p.getComunicacionesGenerales())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailRequestDto {
        @NotBlank(message = "El correo del destinatario es obligatorio.")
        @Email(message = "El formato de correo no es válido.")
        private String destinatarioCorreo;

        private String destinatarioNombre;
        private Long usuarioId;

        @NotBlank(message = "El código de plantilla es obligatorio.")
        private String plantillaCodigo;

        private String asuntoPersonalizado;
        private Map<String, Object> variables;
        private String idempotencyKey;

        @Builder.Default
        private String tipo = "INFORMACION";
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificacionEnvioDto {
        private Long id;
        private String destinatarioCorreo;
        private String destinatarioNombre;
        private String plantillaCodigo;
        private String asunto;
        private String tipo;
        private String estado;
        private Integer intentos;
        private OffsetDateTime proximoIntento;
        private OffsetDateTime fechaEnvio;
        private String ultimoError;

        public static NotificacionEnvioDto fromEntity(NotificacionEnvio e) {
            if (e == null) return null;
            return NotificacionEnvioDto.builder()
                    .id(e.getId())
                    .destinatarioCorreo(e.getDestinatarioCorreo())
                    .destinatarioNombre(e.getDestinatarioNombre())
                    .plantillaCodigo(e.getPlantillaCodigo())
                    .asunto(e.getAsunto())
                    .tipo(e.getTipo())
                    .estado(e.getEstado())
                    .intentos(e.getIntentos())
                    .proximoIntento(e.getProximoIntento())
                    .fechaEnvio(e.getFechaEnvio())
                    .ultimoError(e.getUltimoError())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificacionProgramadaDto {
        private Long id;
        private String titulo;
        private String mensaje;
        private String tipo;
        private String canal;
        private OffsetDateTime fechaProgramada;
        private Boolean ejecutada;
        private Boolean cancelada;
        private OffsetDateTime fechaEjecucion;
        private String creadoPor;

        public static NotificacionProgramadaDto fromEntity(NotificacionProgramacion p) {
            if (p == null) return null;
            return NotificacionProgramadaDto.builder()
                    .id(p.getId())
                    .titulo(p.getTitulo())
                    .mensaje(p.getMensaje())
                    .tipo(p.getTipo())
                    .canal(p.getCanal())
                    .fechaProgramada(p.getFechaProgramada())
                    .ejecutada(p.getEjecutada())
                    .cancelada(p.getCancelada())
                    .fechaEjecucion(p.getFechaEjecucion())
                    .creadoPor(p.getCreadoPor())
                    .build();
        }
    }
}
