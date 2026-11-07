package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoInspeccion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoInspeccionMercado;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InspeccionResponse {

    private UUID id;
    private String codigoInspeccion;
    private TipoInspeccionMercado tipoInspeccion;
    private String tipoInspeccionDescripcion;

    private UUID mercadoId;
    private String mercadoNombre;
    private String mercadoCodigo;

    private UUID sectorId;
    private String sectorNombre;

    private UUID pabellonId;
    private String pabellonNombre;

    private UUID puestoId;
    private String puestoCodigo;
    private String puestoNumero;

    private UUID comercianteId;
    private String comercianteNombre;
    private String comercianteDocumento;

    private UUID inspeccionOrigenId;
    private String inspeccionOrigenCodigo;

    private UUID plantillaId;
    private String plantillaNombre;

    private LocalDate fechaProgramada;
    private LocalTime horaProgramada;
    private OffsetDateTime fechaInicioReal;
    private OffsetDateTime fechaFinReal;

    private EstadoInspeccion estado;
    private String estadoDescripcion;

    private String motivo;
    private String objetivo;

    private Long oficinaResponsableId;
    private String oficinaResponsableNombre;

    private Long inspectorResponsableId;
    private String inspectorResponsableNombre;

    private String resultadoGeneral;
    private String motivoCancelacion;

    private int totalHallazgos;
    private int totalMedidas;
    private boolean tieneActa;
    private String codigoActa;

    private OffsetDateTime createdAt;
}
