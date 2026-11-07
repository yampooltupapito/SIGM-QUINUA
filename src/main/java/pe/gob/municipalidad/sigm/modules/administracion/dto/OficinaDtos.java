package pe.gob.municipalidad.sigm.modules.administracion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class OficinaDtos {

    private OficinaDtos() {}

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OficinaCreateRequest {

        @NotBlank(message = "El código de oficina es obligatorio.")
        @Size(min = 2, max = 50, message = "El código de oficina debe tener entre 2 y 50 caracteres.")
        @Pattern(regexp = "^[A-Z0-9_-]+$", message = "El código solo puede contener mayúsculas, números, guiones y guiones bajos.")
        private String codigo;

        @NotBlank(message = "El nombre de la oficina es obligatorio.")
        @Size(max = 150, message = "El nombre no puede superar los 150 caracteres.")
        private String nombre;

        @Size(max = 30, message = "La sigla no puede superar los 30 caracteres.")
        private String sigla;

        @Size(max = 500, message = "La descripción no puede superar los 500 caracteres.")
        private String descripcion;

        private Long oficinaPadreId;
        private Long municipalidadId;
        private Integer nivelJerarquico;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OficinaUpdateRequest {

        @NotBlank(message = "El nombre de la oficina es obligatorio.")
        @Size(max = 150, message = "El nombre no puede superar los 150 caracteres.")
        private String nombre;

        @Size(max = 30, message = "La sigla no puede superar los 30 caracteres.")
        private String sigla;

        @Size(max = 500, message = "La descripción no puede superar los 500 caracteres.")
        private String descripcion;

        private Long oficinaPadreId;
        private String estado;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OficinaResponse {
        private Long id;
        private String codigo;
        private String nombre;
        private String sigla;
        private String descripcion;
        private Long oficinaPadreId;
        private String oficinaPadreNombre;
        private Integer nivelJerarquico;
        private String estado;
        private int totalSuboficinas;
        private int totalTrabajadores;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OficinaTreeDto {
        private Long id;
        private String codigo;
        private String nombre;
        private String sigla;
        private Integer nivelJerarquico;
        private String estado;
        @Builder.Default
        private List<OficinaTreeDto> suboficinas = new ArrayList<>();
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AsignacionOficinaRequest {
        @NotNull(message = "La persona es obligatoria.")
        private Long personaId;

        @NotNull(message = "La oficina es obligatoria.")
        private Long oficinaId;

        private Long cargoId;

        @NotNull(message = "El tipo de actor es obligatorio.")
        private Long tipoActorId;

        private LocalDate fechaInicio;
        private LocalDate fechaFin;
        private Boolean principal;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AsignacionOficinaResponse {
        private Long id;
        private Long personaId;
        private String personaNombre;
        private Long oficinaId;
        private String oficinaCodigo;
        private String oficinaNombre;
        private Long cargoId;
        private String cargoNombre;
        private Long tipoActorId;
        private String tipoActorNombre;
        private LocalDate fechaInicio;
        private LocalDate fechaFin;
        private Boolean principal;
        private String estado;
    }
}
