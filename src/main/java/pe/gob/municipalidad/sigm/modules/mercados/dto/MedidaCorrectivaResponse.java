package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoMedidaCorrectiva;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedidaCorrectivaResponse {

    private UUID id;
    private UUID hallazgoId;
    private String hallazgoCodigo;
    private String hallazgoDescripcion;

    private UUID inspeccionId;
    private String inspeccionCodigo;
    private String mercadoNombre;
    private String puestoCodigo;

    private String descripcion;
    private String tipoResponsable;
    private Long responsableId;
    private String responsableNombre;
    private UUID comercianteResponsableId;
    private String comercianteResponsableNombre;

    private LocalDate fechaAsignacion;
    private LocalDate fechaLimite;
    private EstadoMedidaCorrectiva estado;
    private String estadoDescripcion;

    private boolean vencida;
    private String observaciones;
    private List<SubsanacionDto> subsanaciones;

    private OffsetDateTime createdAt;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubsanacionDto {
        private UUID id;
        private String descripcionSubsanacion;
        private OffsetDateTime fechaPresentacion;
        private String presentadoPorNombre;
        private String estadoVerificacion;
        private OffsetDateTime fechaVerificacion;
        private String inspectorVerificadorNombre;
        private String observacionesVerificacion;
    }
}
