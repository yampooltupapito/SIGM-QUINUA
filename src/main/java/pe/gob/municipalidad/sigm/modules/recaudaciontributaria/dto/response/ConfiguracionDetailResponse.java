package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoConfiguracionConcepto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionDetailResponse {
    private UUID id;
    private UUID conceptoId;
    private String conceptoCodigo;
    private String conceptoNombre;
    private UUID periodoId;
    private Integer anioPeriodo;
    private UUID normaId;
    private String normaNumero;
    private String normaDescripcion;
    private Integer numeroVersion;
    private UUID versionAnteriorId;
    private EstadoConfiguracionConcepto estado;
    private String estadoDesc;
    private String badgeClass;
    private String descripcionMotivo;
    private LocalDate fechaInicioVigencia;
    private LocalDate fechaFinVigencia;
    private UUID sectorId;
    private String sectorNombre;
    private String zona;
    private Long oficinaResponsableId;
    private String usuarioCreador;
    private String usuarioAprobador;
    private OffsetDateTime fechaAprobacion;
    private String resolucionAprobacion;
    private String usuarioActivador;
    private OffsetDateTime fechaActivacion;
    private String motivoDesactivacion;
    private Long version;

    @Builder.Default
    private List<ParametroResponse> parametros = new ArrayList<>();

    @Builder.Default
    private List<ReglaResponse> reglas = new ArrayList<>();

    @Builder.Default
    private List<HistorialConfiguracionResponse> historial = new ArrayList<>();

    public boolean isEditable() {
        return this.estado == EstadoConfiguracionConcepto.BORRADOR;
    }
}
