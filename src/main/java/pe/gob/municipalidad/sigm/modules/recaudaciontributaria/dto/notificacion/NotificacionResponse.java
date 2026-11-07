package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.notificacion;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.CategoriaTipoNotificacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoAdministrativoNotificacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoEntregaNotificacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoValidezLegalNotificacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificacionResponse {

    private UUID id;
    private String codigoNotificacion;
    private UUID tipoNotificacionId;
    private String tipoNotificacionCodigo;
    private String tipoNotificacionNombre;
    private CategoriaTipoNotificacion categoria;
    private Boolean requiereDocumento;
    private Boolean requiereValidacionLegal;
    private Boolean requiereEvidencia;

    private UUID contribuyenteId;
    private String contribuyenteNombre;
    private String contribuyenteNumeroDoc;

    private String destinatarioNombre;
    private String destinatarioNumeroDoc;
    private String representanteNombre;

    private String domicilioNotificacion;
    private String referenciaUbicacion;

    private UUID medioNotificacionId;
    private String medioNotificacionCodigo;
    private String medioNotificacionNombre;
    private Boolean esMedioFormal;

    private Long documentoOrigenId;
    private String documentoOrigenNumero;
    private Long documentoNotificacionId;
    private String hashDocumento;
    private String codigoQr;

    private UUID obligacionId;
    private String obligacionCodigo;
    private UUID convenioId;
    private String convenioCodigo;
    private UUID seguimientoDeudaId;

    private UUID loteId;
    private String loteCodigo;

    private OffsetDateTime fechaCreacion;
    private LocalDate fechaProgramada;

    private EstadoAdministrativoNotificacion estadoAdministrativo;
    private EstadoEntregaNotificacion estadoEntrega;
    private EstadoValidezLegalNotificacion estadoValidezLegal;

    private Long oficinaResponsableId;
    private String oficinaResponsableNombre;
    private Long usuarioResponsableId;
    private String usuarioResponsableNombre;

    private String observaciones;
    private String motivoAnulacion;
    private BigDecimal montoTotalObligaciones;

    private Boolean tieneCargo;
    private String receptorCargo;
    private OffsetDateTime fechaRecepcionCargo;
    private Integer totalIntentos;
    private Integer totalEvidencias;

    private String plazoEstado;
    private LocalDate plazoVencimiento;
    private Integer plazoDiasRestantes;
}

