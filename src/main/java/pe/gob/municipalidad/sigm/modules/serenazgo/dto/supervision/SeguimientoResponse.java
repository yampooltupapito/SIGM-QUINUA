package pe.gob.municipalidad.sigm.modules.serenazgo.dto.supervision;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoSeguimientoSupervision;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.PrioridadAdministrativaSupervision;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoRegistroSupervision;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoSeguimientoSupervision;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeguimientoResponse {

    private UUID id;
    private String codigo;
    private TipoRegistroSupervision tipoRegistroOrigen;
    private UUID registroOrigenId;
    private String registroOrigenCodigo;
    private Long oficinaResponsableId;
    private String oficinaResponsableNombre;
    private Long supervisorId;
    private String supervisorNombre;
    private Long responsableAsignadoId;
    private String responsableAsignadoNombre;
    private TipoSeguimientoSupervision tipoSeguimiento;
    private String descripcion;
    private EstadoSeguimientoSupervision estado;
    private PrioridadAdministrativaSupervision prioridadAdministrativa;
    private OffsetDateTime fechaCreacion;
    private OffsetDateTime fechaCompromiso;
    private OffsetDateTime fechaCierre;
    private String resultado;
    private String oficinaEscalamiento;
    private Long version;
    private int totalObservaciones;
    private boolean vencido;
}
