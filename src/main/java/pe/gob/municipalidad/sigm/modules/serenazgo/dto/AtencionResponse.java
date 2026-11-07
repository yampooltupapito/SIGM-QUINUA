package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.CanalComunicacionDespacho;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoAtencionIncidencia;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.PrioridadIncidenciaSeguridad;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.ResultadoAtencionIncidencia;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtencionResponse {

    private UUID id;
    private String codigo;

    // Datos Incidencia
    private UUID incidenciaId;
    private String incidenciaCodigo;
    private String incidenciaTitulo;
    private String incidenciaTipo;
    private String incidenciaUbicacion;

    // Sector / Zona
    private UUID sectorId;
    private String sectorNombre;
    private UUID zonaId;
    private String zonaNombre;

    // Asignación y Fechas
    private OffsetDateTime fechaHoraAsignacion;
    private OffsetDateTime fechaHoraDespacho;
    private OffsetDateTime fechaHoraAceptacion;
    private OffsetDateTime fechaHoraInicio;
    private OffsetDateTime fechaHoraFinalizacion;

    // Estado y Prioridad
    private EstadoAtencionIncidencia estado;
    private String estadoDesc;
    private PrioridadIncidenciaSeguridad prioridadOperativa;
    private String prioridadOperativaDesc;
    private CanalComunicacionDespacho canalComunicacion;
    private String canalComunicacionDesc;

    // Recursos Asignados
    private UUID unidadMovilId;
    private String unidadMovilCodigo;
    private String unidadMovilPlaca;
    private UUID responsableOperativoId;
    private String responsableOperativoNombre;
    private int cantidadEfectivos;

    // Resultados
    private ResultadoAtencionIncidencia resultadoAdministrativo;
    private String resultadoAdministrativoDesc;
    private String motivoFinalizacion;
    private String supervisorAsignadorNombre;
}
