package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoEvaluacionInfraccion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.SentidoDecisionInfraccion;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresuntaInfraccionResponse {
    private UUID id;
    private String codigo;
    private UUID incidenciaId;
    private String incidenciaCodigo;
    private UUID inspeccionId;
    private String inspeccionCodigo;
    private UUID hallazgoId;
    private String hallazgoCodigo;
    private UUID mercadoId;
    private String mercadoNombre;
    private UUID sectorId;
    private String sectorNombre;
    private UUID pabellonId;
    private String pabellonNombre;
    private UUID puestoId;
    private String puestoNumero;
    private UUID comercianteId;
    private String comercianteNombre;
    private String comercianteDocumento;
    private UUID tipificacionId;
    private String tipificacionCodigo;
    private String tipificacionDescripcion;
    private String normaReferencia;
    private String articulo;
    private String autoridadCompetente;
    private String descripcionHechos;
    private LocalDate fechaDeteccion;
    private EstadoEvaluacionInfraccion estadoEvaluacion;
    private Long responsableEvaluacionId;
    private String responsableEvaluacionNombre;
    private String informeEvaluacion;

    public String getEvaluacionInforme() {
        return informeEvaluacion;
    }
    private OffsetDateTime fechaEvaluacion;
    private String motivoDescarte;
    private UUID expedienteId;
    private String expedienteNumero;
    private Boolean tieneDescargo;
    private OffsetDateTime fechaDescargo;
    private String documentoDescargoRef;
    private String resumenDescargo;
    private String resolucionDecisionRef;
    private LocalDate fechaDecision;
    private SentidoDecisionInfraccion sentidoDecision;
    private OffsetDateTime createdAt;
}
