package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoIntervencionSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoIntervencionSerenazgo;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntervencionFilter {

    private String termino;

    private String codigo;

    private TipoIntervencionSerenazgo tipo;

    private EstadoIntervencionSerenazgo estado;

    private UUID sectorId;

    private UUID zonaId;

    private UUID personalId;

    private UUID incidenciaId;

    private UUID atencionId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate desde;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate hasta;
}
