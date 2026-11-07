package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MercadosDtos {

    // -------------------------------------------------------------
    // Dashboard DTO
    // -------------------------------------------------------------
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardMercadosMetrics {
        private long totalMercados;
        private long mercadosActivos;
        private long totalPuestos;
        private long puestosDisponibles;
        private long puestosOcupados;
        private long puestosMantenimiento;
        private long totalComerciantes;
        private long comerciantesActivos;
        private long asignacionesVigentes;
    }


    // -------------------------------------------------------------
    // Puesto Comercial DTOs
    // -------------------------------------------------------------
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PuestoRequest {
        @NotNull(message = "El ID del mercado es obligatorio")
        private UUID mercadoId;

        private UUID sectorId;
        private UUID pabellonId;

        @NotBlank(message = "El código del puesto es obligatorio")
        @Size(max = 50, message = "El código no debe exceder 50 caracteres")
        private String codigo;

        @NotBlank(message = "El número del puesto es obligatorio")
        @Size(max = 30, message = "El número no debe exceder 30 caracteres")
        private String numero;

        @Size(max = 500, message = "La descripción no debe exceder 500 caracteres")
        private String descripcion;

        @DecimalMin(value = "0.00", message = "El área no puede ser negativa")
        private BigDecimal areaMetrosCuadrados;

        @Size(max = 255, message = "La ubicación de referencia no debe exceder 255 caracteres")
        private String ubicacionReferencia;

        private EstadoPuesto estado;
        private Long tipoPuestoId;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PuestoResponse {
        private UUID id;
        private UUID mercadoId;
        private String mercadoNombre;
        private UUID sectorId;
        private String sectorNombre;
        private UUID pabellonId;
        private String pabellonNombre;
        private String codigo;
        private String numero;
        private String descripcion;
        private BigDecimal areaMetrosCuadrados;
        private String ubicacionReferencia;
        private EstadoPuesto estado;
        private Long tipoPuestoId;
        private String tipoPuestoNombre;
        private Long version;
    }

    // -------------------------------------------------------------
    // Comerciante DTOs
    // -------------------------------------------------------------
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComercianteRequest {
        @NotNull(message = "El ID de la persona central es obligatorio")
        private Long personaId;

        @NotBlank(message = "El código de comerciante es obligatorio")
        @Size(max = 50, message = "El código no debe exceder 50 caracteres")
        private String codigoComerciante;

        private EstadoComerciante estado;
        private String observaciones;
        private Long usuarioId;
        private Set<Long> actividadIds;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComercianteResponse {
        private UUID id;
        private Long personaId;
        private String numeroDocumento;
        private String tipoDocumento;
        private String nombreCompleto;
        private String telefono;
        private String correo;
        private String codigoComerciante;
        private LocalDate fechaRegistro;
        private EstadoComerciante estado;
        private String observaciones;
        private List<String> actividades;
    }

    // -------------------------------------------------------------
    // Asignación DTOs
    // -------------------------------------------------------------
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AsignacionRequest {
        @NotNull(message = "El puesto es obligatorio")
        private UUID puestoId;

        @NotNull(message = "El comerciante es obligatorio")
        private UUID comercianteId;

        @NotNull(message = "La fecha de inicio es obligatoria")
        private LocalDate fechaInicio;

        private LocalDate fechaFin;

        private EstadoAsignacion estado;
        private TipoAsignacion tipoAsignacion;
        private Long documentoSustentoId;
        private String observaciones;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AsignacionResponse {
        private UUID id;
        private UUID puestoId;
        private String puestoCodigo;
        private String puestoNumero;
        private String mercadoNombre;
        private UUID comercianteId;
        private String comercianteCodigo;
        private String comercianteNombre;
        private LocalDate fechaInicio;
        private LocalDate fechaFin;
        private EstadoAsignacion estado;
        private TipoAsignacion tipoAsignacion;
        private Long documentoSustentoId;
        private String observaciones;
    }
}
