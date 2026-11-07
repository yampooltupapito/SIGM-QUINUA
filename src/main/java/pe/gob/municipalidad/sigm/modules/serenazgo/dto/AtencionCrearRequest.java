package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.CanalComunicacionDespacho;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.PrioridadIncidenciaSeguridad;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtencionCrearRequest {

    @NotNull(message = "El identificador de la incidencia es obligatorio")
    private UUID incidenciaId;

    private PrioridadIncidenciaSeguridad prioridadOperativa;

    private CanalComunicacionDespacho canalComunicacion;

    @Size(max = 2000, message = "Las instrucciones no pueden superar los 2000 caracteres")
    private String instruccionesDespacho;

    private UUID unidadMovilId;

    private UUID responsableOperativoId;

    @Builder.Default
    private List<UUID> personalIds = new ArrayList<>();

    @Builder.Default
    private List<UUID> equipamientoIds = new ArrayList<>();

    private UUID sectorId;
    private UUID zonaId;

    @Builder.Default
    private Boolean despacharInmediatamente = false;

    private String observaciones;

    public UUID getResponsablePersonalId() {
        return responsableOperativoId;
    }

    public void setResponsablePersonalId(UUID responsablePersonalId) {
        this.responsableOperativoId = responsablePersonalId;
    }
}
