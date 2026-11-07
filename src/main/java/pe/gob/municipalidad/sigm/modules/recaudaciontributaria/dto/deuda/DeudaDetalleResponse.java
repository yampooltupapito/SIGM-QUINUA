package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.deuda;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeudaDetalleResponse {

    // Obligación Base
    private UUID obligacionId;
    private String codigoObligacion;
    private String tipoDeterminacion;
    private BigDecimal importeDeterminado;
    private BigDecimal importePagado;
    private BigDecimal importeAjustes;
    private BigDecimal saldo;
    private String moneda;
    private LocalDate fechaDeterminacion;
    private LocalDate fechaVencimiento;
    private EstadoObligacionTributaria estadoObligacion;
    private EstadoFinancieroObligacion estadoFinanciero;
    private EstadoExigibilidadObligacion estadoExigibilidad;
    private Integer cuotaNumero;
    private Integer totalCuotas;

    // Contribuyente
    private UUID contribuyenteId;
    private String contribuyenteNombre;
    private String contribuyenteNumeroDoc;
    private String contribuyenteTipoDoc;
    private String contribuyenteDomicilio;

    // Concepto & Período & Predio
    private String conceptoCodigo;
    private String conceptoNombre;
    private String periodoAnio;
    private String predioCodigo;
    private String predioDireccion;

    // Morosidad y Antigüedad
    private long diasMorosidad;
    private TramoAntiguedadDeuda tramoAntiguedad;
    private ClasificacionDeudaTributaria clasificacion;

    // Seguimiento Administrativo
    private UUID seguimientoId;
    private EstadoSeguimientoDeuda estadoSeguimiento;
    private Long responsableId;
    private String responsableNombre;
    private Long oficinaId;
    private String oficinaNombre;
    private OffsetDateTime fechaInicioSeguimiento;
    private OffsetDateTime fechaUltimaRevision;
    private String observacionesSeguimiento;

    // Gestiones y Compromisos
    private List<GestionDto> gestiones;
    private List<CompromisoDto> compromisos;

    // Simulación de Intereses actual
    private SimulacionInteresResponse simulacionInteres;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GestionDto {
        private UUID id;
        private TipoGestionDeuda tipoGestion;
        private OffsetDateTime fechaGestion;
        private String responsableNombre;
        private String resultado;
        private String observaciones;
        private Long documentoSustentoId;
        private String documentoNombre;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CompromisoDto {
        private UUID id;
        private OffsetDateTime fechaRegistro;
        private LocalDate fechaComprometida;
        private BigDecimal montoComprometido;
        private String observaciones;
        private EstadoCompromisoDeuda estado;
        private String responsableNombre;
    }
}
