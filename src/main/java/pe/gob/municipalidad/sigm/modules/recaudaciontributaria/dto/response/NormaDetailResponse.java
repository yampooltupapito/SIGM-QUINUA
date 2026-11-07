package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoValidacionNorma;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoNormaTributaria;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NormaDetailResponse {
    private UUID id;
    private TipoNormaTributaria tipoNorma;
    private String tipoNormaDesc;
    private String numeroNorma;
    private String entidadEmisora;
    private String descripcion;
    private LocalDate fechaPublicacion;
    private LocalDate fechaInicioVigencia;
    private LocalDate fechaFinVigencia;
    private String enlaceOficial;
    private String alcanceTerritorial;
    private String archivoHash;
    private EstadoValidacionNorma estadoValidacion;
    private String estadoValidacionDesc;
    private String badgeClass;
    private UUID normaModificaId;
    private String normaModificaNumero;
    private String validador;
    private String observaciones;
}
