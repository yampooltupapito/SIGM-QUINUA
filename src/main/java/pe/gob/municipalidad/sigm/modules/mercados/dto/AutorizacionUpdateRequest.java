package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutorizacionUpdateRequest {

    private Long tipoAutorizacionId;

    private String tipoAutorizacion;

    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    private Long expedienteId;

    private Long oficinaEmisoraId;

    private Long autoridadCompetenteId;

    private String observaciones;
}
