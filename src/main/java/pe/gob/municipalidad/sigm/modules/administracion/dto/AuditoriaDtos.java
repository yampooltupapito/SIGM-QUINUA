package pe.gob.municipalidad.sigm.modules.administracion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * DTO para consulta de registros de auditoría administrativa.
 */
public final class AuditoriaDtos {

    private AuditoriaDtos() {}

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuditoriaAdministrativaResponse {
        private Long id;
        private String usuarioResponsable;
        private String accion;
        private String entidadAfectada;
        private Long entidadId;
        private String resultado;
        private String detalle;
        private String ipOrigen;
        private String userAgent;
        private String traceId;
        private OffsetDateTime createdAt;
    }
}
