package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.portal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionCiudadanoDto {
    private UUID id;
    private String codigoNotificacion;
    private String tipoNombre;
    private String categoria;
    private String asunto;
    private String medioEntrega;
    private OffsetDateTime fechaEmision;
    private String estadoEntrega;
    private String estadoValidezLegal;
    private String hashDocumento;
    private boolean formal;
    private boolean tieneDocumentoAdjunto;
    private String observaciones;
}

