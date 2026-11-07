package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoIntervencionSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.NivelConfidencialidad;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.ResultadoIntervencionSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoIntervencionSerenazgo;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntervencionResponse {

    private UUID id;
    private String codigo;

    private UUID incidenciaId;
    private String incidenciaCodigo;
    private String incidenciaTitulo;

    private UUID atencionId;
    private String atencionCodigo;

    private TipoIntervencionSerenazgo tipoIntervencion;
    private String tipoIntervencionDesc;

    private OffsetDateTime fechaInicio;
    private String fechaInicioFormateada;
    private OffsetDateTime fechaFin;
    private String fechaFinFormateada;

    private UUID sectorId;
    private String sectorNombre;
    private UUID zonaId;
    private String zonaNombre;
    private String ubicacionDescripcion;

    private UUID responsableId;
    private String responsableNombre;
    private String responsableCodigo;

    private UUID unidadMovilId;
    private String unidadMovilCodigo;
    private String unidadMovilPlaca;

    private ResultadoIntervencionSerenazgo resultado;
    private String resultadoDesc;

    private EstadoIntervencionSerenazgo estado;
    private String estadoDesc;

    private NivelConfidencialidad nivelConfidencialidad;
    private String nivelConfidencialidadDesc;

    private int cantidadParticipantes;
    private int cantidadActuaciones;
    private int cantidadComunicaciones;
    private int cantidadDocumentos;

    private Boolean registroRetrospectivo;
    private OffsetDateTime fechaValidacion;
    private String usuarioValidadorNombre;
}
