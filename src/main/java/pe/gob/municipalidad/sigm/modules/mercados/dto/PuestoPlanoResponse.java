package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoVisualPuesto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PuestoPlanoResponse {

    private UUID puestoId;
    private String codigo;
    private String numero;
    private String nombreReferencial;
    private String descripcion;
    private BigDecimal areaMetrosCuadrados;
    private BigDecimal anchoMetros;
    private BigDecimal largoMetros;

    // Jerarquía física
    private UUID mercadoId;
    private String mercadoNombre;
    private UUID sectorId;
    private String sectorNombre;
    private UUID pabellonId;
    private String pabellonNombre;
    private UUID zonaId;
    private String zonaNombre;
    private String tipoPuestoNombre;

    // Estado físico y visual
    private String estadoFisico;
    private EstadoVisualPuesto estadoVisual;
    private String estadoVisualLabel;
    private String colorHex;
    private Boolean ocupado;

    // Titular / Comerciante (respetando permisos de privacidad)
    private UUID comercianteId;
    private String comercianteNombre;
    private String comercianteDocumento;
    private String comercianteTipo;
    private String comercianteTelefono;
    private Boolean datosPrivadosVisibles;

    // Asignación de Ocupación
    private UUID asignacionId;
    private String asignacionCodigo;
    private LocalDate asignacionFechaInicio;
    private LocalDate asignacionFechaFin;
    private String asignacionEstado;

    // Contrato
    private UUID contratoId;
    private String contratoCodigo;
    private LocalDate contratoFechaFin;
    private String contratoEstado;

    // Autorización Comercial
    private UUID autorizacionId;
    private String autorizacionCodigo;
    private LocalDate autorizacionFechaFin;
    private String autorizacionEstado;

    // Incidencias
    private Long incidenciasPendientes;
    private String ultimaIncidenciaDescripcion;

    // Inspecciones
    private Long inspeccionesRealizadas;
    private LocalDate ultimaInspeccionFecha;
    private String ultimaInspeccionEstado;

    // Finanzas (solo visible si tiene permiso de finanzas)
    private Boolean finanzasVisibles;
    private BigDecimal totalDeudaPendiente;
    private Boolean tieneDeudaVencida;
}
