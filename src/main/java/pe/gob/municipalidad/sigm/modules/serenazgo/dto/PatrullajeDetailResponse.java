package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoPatrullajeSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoPatrullajeSerenazgo;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatrullajeDetailResponse {
    private UUID id;
    private String codigo;
    private TipoPatrullajeSerenazgo tipoPatrullaje;
    private String tipoPatrullajeDescripcion;
    private EstadoPatrullajeSerenazgo estado;
    private String estadoDescripcion;

    private UUID sectorId;
    private String sectorCodigo;
    private String sectorNombre;

    private UUID zonaId;
    private String zonaCodigo;
    private String zonaNombre;

    private UUID rutaId;
    private String rutaCodigo;
    private String rutaNombre;
    private BigDecimal rutaDistanciaKm;
    private Integer rutaTiempoMin;

    private UUID programacionServicioId;
    private String programacionServicioCodigo;

    private Long oficinaResponsableId;

    private UUID responsableId;
    private String responsableCodigo;
    private String responsableNombre;
    private String responsableCargo;

    private UUID unidadMovilId;
    private String unidadPlaca;
    private String unidadTipo;
    private String unidadCodigoInterno;

    private OffsetDateTime fechaHoraInicioProgramada;
    private OffsetDateTime fechaHoraFinProgramada;
    private OffsetDateTime fechaHoraInicioReal;
    private OffsetDateTime fechaHoraFinReal;

    private String objetivoOperativo;
    private String descripcion;
    private String resultadoAdministrativo;
    private String observaciones;

    private BigDecimal kmInicio;
    private BigDecimal kmFin;
    private BigDecimal distanciaRecorridaEstimadaKm;

    private String motivoPausa;
    private String motivoCancelacion;
    private String cambioRutaMotivo;

    private Boolean esExtraordinario;
    private String motivoExtraordinario;

    private Long version;
    private String createdBy;
    private String updatedBy;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    // Métricas y colecciones
    private int totalPuntosRuta;
    private int puntosVisitados;
    private int puntosOmitidos;
    private int puntosPendientes;
    private double porcentajeAvance;

    @Builder.Default
    private List<PatrullajeParticipanteResponse> participantes = new ArrayList<>();

    @Builder.Default
    private List<PatrullajeVisitaPuntoResponse> visitasPuntos = new ArrayList<>();

    @Builder.Default
    private List<PuntoControlResponse> puntosControlRuta = new ArrayList<>();

    @Builder.Default
    private List<PatrullajeNovedadResponse> novedades = new ArrayList<>();

    @Builder.Default
    private List<PatrullajeEquipamientoResponse> equipamiento = new ArrayList<>();

    @Builder.Default
    private List<PatrullajeHistorialResponse> historial = new ArrayList<>();
}
