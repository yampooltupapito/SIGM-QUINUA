package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoPatrullajeSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoPatrullajeSerenazgo;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatrullajeResponse {
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
    private String zonaNombre;

    private UUID rutaId;
    private String rutaCodigo;
    private String rutaNombre;

    private UUID responsableId;
    private String responsableCodigo;
    private String responsableNombre;

    private UUID unidadMovilId;
    private String unidadPlaca;
    private String unidadTipo;

    private OffsetDateTime fechaHoraInicioProgramada;
    private OffsetDateTime fechaHoraFinProgramada;
    private OffsetDateTime fechaHoraInicioReal;
    private OffsetDateTime fechaHoraFinReal;

    private String objetivoOperativo;
    private Boolean esExtraordinario;

    private int totalPuntosRuta;
    private int puntosVisitados;
    private int puntosOmitidos;
    private int totalNovedades;
    private int totalParticipantes;

    private BigDecimal kmInicio;
    private BigDecimal kmFin;
    private OffsetDateTime createdAt;
}
