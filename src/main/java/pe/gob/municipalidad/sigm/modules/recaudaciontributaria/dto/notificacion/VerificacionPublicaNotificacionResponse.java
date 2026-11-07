package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.notificacion;

import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * DTO para consulta pública de validez y verificación de autenticidad documental institucional.
 * Protegido con minimización estricta de datos personales según la Ley N° 29733.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificacionPublicaNotificacionResponse {

    private boolean existe;
    private String codigoNotificacion;
    private String tipoDocumento;
    private String categoria;
    private String destinatarioAnonimizado;
    private String numeroDocEnmascarado;
    private OffsetDateTime fechaEmision;
    private String estadoValidezLegal;
    private String estadoEntrega;
    private String entidadEmisora;
    private String hashDocumento;
    private String mensajeVerificacion;
    private Boolean esValido;
}

