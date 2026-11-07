package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.CanalComunicacionDespacho;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoAtencionIncidencia;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.PrioridadIncidenciaSeguridad;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.ResultadoAtencionIncidencia;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtencionDetailResponse {

    private UUID id;
    private String codigo;

    // Incidencia Relacionada
    private UUID incidenciaId;
    private String incidenciaCodigo;
    private String incidenciaTitulo;
    private String incidenciaDescripcion;
    private String incidenciaTipo;
    private String incidenciaPrioridad;
    private String incidenciaUbicacion;
    private BigDecimal incidenciaLatitud;
    private BigDecimal incidenciaLongitud;
    private OffsetDateTime incidenciaFechaReporte;
    private OffsetDateTime incidenciaFechaOcurrencia;
    private String incidenciaEstado;

    // Territorial
    private UUID sectorId;
    private String sectorNombre;
    private UUID zonaId;
    private String zonaNombre;

    // Datos Operativos
    private OffsetDateTime fechaHoraAsignacion;
    private OffsetDateTime fechaHoraDespacho;
    private OffsetDateTime fechaHoraAceptacion;
    private OffsetDateTime fechaHoraRechazo;
    private OffsetDateTime fechaHoraInicio;
    private OffsetDateTime fechaHoraFinalizacion;

    private EstadoAtencionIncidencia estado;
    private String estadoDesc;
    private PrioridadIncidenciaSeguridad prioridadOperativa;
    private String prioridadOperativaDesc;
    private CanalComunicacionDespacho canalComunicacion;
    private String canalComunicacionDesc;

    // Recursos
    private Long supervisorAsignadorId;
    private String supervisorAsignadorNombre;
    private UUID unidadMovilId;
    private String unidadMovilCodigo;
    private String unidadMovilPlaca;
    private String unidadMovilTipo;
    private UUID responsableOperativoId;
    private String responsableOperativoNombre;
    private String responsableOperativoCodigo;

    // Textos Operativos
    private String instruccionesDespacho;
    private String motivoRechazo;
    private String motivoEspera;
    private String motivoCancelacion;
    private ResultadoAtencionIncidencia resultadoAdministrativo;
    private String resultadoAdministrativoDesc;
    private String motivoFinalizacion;
    private String observacionesOperativas;

    // Listas Hijas
    @Builder.Default
    private List<AtencionPersonalResponse> personalAsignado = new ArrayList<>();

    @Builder.Default
    private List<AtencionEquipamientoResponse> equipamientoAsignado = new ArrayList<>();

    @Builder.Default
    private List<AtencionEventoResponse> eventos = new ArrayList<>();

    @Builder.Default
    private List<AtencionEscalamientoResponse> escalamientos = new ArrayList<>();

    // Concurrencia
    private Long version;

    public List<AtencionPersonalResponse> getPersonal() {
        return personalAsignado;
    }

    public List<AtencionEquipamientoResponse> getEquipamiento() {
        return equipamientoAsignado;
    }
}
