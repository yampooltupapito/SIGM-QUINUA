package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoAsignacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoAsignacion;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionFilter {

    private String termino;
    private UUID mercadoId;
    private UUID sectorId;
    private UUID pabellonId;
    private UUID comercianteId;
    private EstadoAsignacion estado;
    private TipoAsignacion tipoAsignacion;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaInicioDesde;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaInicioHasta;
}
