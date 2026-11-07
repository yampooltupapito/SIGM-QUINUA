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
public class PersonalSerenazgoResponse {
    private UUID id;
    private String codigoInterno;
    private Long personaId;
    private String nombreCompleto;
    private String tipoDocumentoIdentidad;
    private String numeroDocumentoEnmascarado;
    private Long usuarioId;
    private String username;
    private Long oficinaId;
    private String nombreOficina;
    private UUID cargoOperativoId;
    private String nombreCargoOperativo;
    private String codigoCargoOperativo;
    private String estado;
    private LocalDate fechaIngreso;
    private LocalDate fechaSalida;
    private boolean disponible;
}
