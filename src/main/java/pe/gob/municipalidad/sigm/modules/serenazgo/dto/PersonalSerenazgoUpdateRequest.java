package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonalSerenazgoUpdateRequest {
    private Long oficinaId;
    private Long usuarioId;
    private String tipoDocumentoLaboral;
    private LocalDate fechaIngreso;
    private LocalDate fechaSalida;
    private String observaciones;
}
