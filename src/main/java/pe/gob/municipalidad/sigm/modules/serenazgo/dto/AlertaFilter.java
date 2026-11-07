package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.CanalAlertaCiudadana;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoAlertaCiudadana;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.PrioridadIncidenciaSeguridad;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertaFilter {

    private String codigo;
    private String termino;
    private UUID tipoId;
    private EstadoAlertaCiudadana estado;
    private PrioridadIncidenciaSeguridad prioridad;
    private CanalAlertaCiudadana canal;
    private UUID sectorId;
    private UUID zonaId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate desde;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate hasta;

    private Boolean vinculada;
}
