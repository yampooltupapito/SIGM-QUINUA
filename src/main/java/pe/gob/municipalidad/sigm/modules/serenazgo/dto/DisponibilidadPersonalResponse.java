package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisponibilidadPersonalResponse {
    private UUID personalId;
    private String codigoInterno;
    private String nombreCompleto;
    private LocalDate fechaConsulta;
    private boolean disponible;
    private String motivo;
    private String turnoAsignado;
    private String sectorAsignado;
}
