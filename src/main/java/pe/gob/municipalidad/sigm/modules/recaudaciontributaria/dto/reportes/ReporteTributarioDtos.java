package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.reportes;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoMetaRecaudacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.FormatoExportacionReporte;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.MedioPagoTributario;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class ReporteTributarioDtos {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecaudacionPeriodoItemDto {
        private String etiqueta; // "Ene 2026" o "28/10/2026"
        private BigDecimal monto;
        private long operaciones;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecaudacionConceptoItemDto {
        private UUID conceptoId;
        private String conceptoCodigo;
        private String conceptoNombre;
        private String tipoConcepto;
        private BigDecimal montoRecaudado;
        private BigDecimal porcentaje;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecaudacionMedioItemDto {
        private MedioPagoTributario medioPago;
        private String nombreMedio;
        private BigDecimal montoRecaudado;
        private long cantidadPagos;
        private BigDecimal porcentaje;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CarteraTramoItemDto {
        private String tramo; // "0 a 30 días", "31 a 90 días", etc.
        private BigDecimal saldo;
        private long cantidadObligaciones;
        private BigDecimal porcentaje;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CajaResumenItemDto {
        private UUID cajaId;
        private String nombreCaja;
        private long numeroSesiones;
        private BigDecimal totalEfectivo;
        private BigDecimal totalOtrosMedios;
        private BigDecimal totalRecaudado;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContribuyenteCumplimientoDto {
        private String tipoContribuyente;
        private long totalRegistrados;
        private long conDeuda;
        private long alDia;
        private BigDecimal porcentajeCumplimiento;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PredioResumenDto {
        private String tipoPredio;
        private long totalPredios;
        private BigDecimal totalAutovaluoReferencial;
        private BigDecimal porcentaje;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FraccionamientoResumenDto {
        private long totalConvenios;
        private BigDecimal montoTotalConvenios;
        private BigDecimal montoAmortizado;
        private BigDecimal saldoPendiente;
        private BigDecimal porcentajeRecuperacion;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NotificacionResumenDto {
        private String tipoNotificacion;
        private long totalEmitidas;
        private long totalEntregadas;
        private long totalValidezLegal;
        private BigDecimal porcentajeEfectividad;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MetaRecaudacionItemDto {
        private UUID id;
        private Integer ejercicio;
        private Integer periodo;
        private String periodoNombre;
        private String conceptoNombre;
        private BigDecimal importeMeta;
        private BigDecimal recaudadoReal;
        private BigDecimal porcentajeCumplimiento;
        private EstadoMetaRecaudacion estado;
        private String observacion;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CrearMetaRecaudacionRequest {
        private Integer ejercicio;
        private Integer periodo;
        private UUID conceptoId;
        private BigDecimal importeMeta;
        private String observacion;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HistorialExportacionItemDto {
        private UUID id;
        private String reporteCodigo;
        private String reporteNombre;
        private OffsetDateTime fechaSolicitud;
        private FormatoExportacionReporte formato;
        private String estado;
        private String usuarioUsername;
        private String nombreArchivo;
        private Long tamanioBytes;
        private String checksumSha256;
    }
}

