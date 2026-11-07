package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoConceptoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.NaturalezaTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.PeriodicidadConcepto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoConceptoTributario;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConceptoDetailResponse {
    private UUID id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private TipoConceptoTributario tipoConcepto;
    private String tipoConceptoDesc;
    private NaturalezaTributaria naturalezaTributaria;
    private String naturalezaTributariaDesc;
    private PeriodicidadConcepto periodicidad;
    private String periodicidadDesc;
    private Boolean aplicaAPredio;
    private String cuentaPresupuestal;
    private EstadoConceptoTributario estado;
    private String estadoDesc;
    private String badgeClass;
    private LocalDate fechaInicioVigencia;
    private LocalDate fechaFinVigencia;
    private UUID normaLegalId;
    private String normaLegalNumero;
    private String normaLegalDescripcion;
    private Long oficinaResponsableId;
    private Long version;

    // Versión activa actual si existe
    private ConfiguracionDetailResponse configuracionActiva;

    // Lista de todas las versiones configuradas
    @Builder.Default
    private List<ConfiguracionSummaryDto> configuraciones = new ArrayList<>();

    // Calendario de cuotas para el período actual
    @Builder.Default
    private List<CalendarioCuotaResponse> calendario = new ArrayList<>();
}
