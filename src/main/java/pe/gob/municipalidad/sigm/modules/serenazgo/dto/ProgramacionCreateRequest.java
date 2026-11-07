package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoProgramacionServicio;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoServicioSerenazgo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramacionCreateRequest {

    @NotNull(message = "El turno es obligatorio")
    private UUID turnoId;

    @NotNull(message = "La fecha de servicio es obligatoria")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaServicio;

    private UUID sectorId;
    private UUID zonaId;

    @Builder.Default
    private TipoServicioSerenazgo tipoServicio = TipoServicioSerenazgo.PATRULLAJE_PREVENTIVO;

    private String descripcion;
    private String observaciones;
    private Long oficinaResponsableId;
    private UUID responsableServicioId;

    @Builder.Default
    private EstadoProgramacionServicio estado = EstadoProgramacionServicio.PROGRAMADA;

    @Builder.Default
    private List<AsignacionPersonalItemRequest> personalAsignado = new ArrayList<>();

    @Builder.Default
    private List<ReservaUnidadItemRequest> unidadesReservadas = new ArrayList<>();

    @Builder.Default
    private List<ReservaEquipamientoItemRequest> equipamientoReservado = new ArrayList<>();
}
