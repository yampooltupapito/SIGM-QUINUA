package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoSolicitudGestion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoSolicitudGestion;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortalComercianteSolicitudResponse {
    private UUID solicitudId;
    private String codigoSolicitud;
    private TipoSolicitudGestion tipoSolicitud;
    private String tipoSolicitudLabel;
    private String procedimientoNombre;
    private String mercadoNombre;
    private String puestoCodigo;
    private LocalDate fechaPresentacion;
    private EstadoSolicitudGestion estado;
    private String estadoLabel;
    private String tokenVerificacionQr;
    private boolean tieneObservacionesPendientes;
    private int cantidadObservaciones;
    private String motivoObservacionReciente;
}
