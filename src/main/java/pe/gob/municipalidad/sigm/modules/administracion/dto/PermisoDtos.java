package pe.gob.municipalidad.sigm.modules.administracion.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PermisoDtos {

    private PermisoDtos() {}

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PermisoResponse {
        private Long id;
        private Long moduloId;
        private String moduloCodigo;
        private String moduloNombre;
        private String codigo;
        private String nombre;
        private String accion;
        private String descripcion;
        private String estado;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModuloPermisosDto {
        private Long moduloId;
        private String moduloCodigo;
        private String moduloNombre;
        private String moduloIcono;
        private Integer orden;
        private Map<String, PermisoResponse> permisosPorAccion;
        private List<PermisoResponse> permisos;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatrizPermisosResponse {
        private Long rolId;
        private String rolCodigo;
        private String rolNombre;
        private List<ModuloPermisosDto> modulos;
        private Set<Long> permisosAsignadosIds;
        private Set<Long> permisoIdsAsignados;

        public Set<Long> getPermisosAsignadosIds() {
            return permisosAsignadosIds != null ? permisosAsignadosIds : permisoIdsAsignados;
        }
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActualizarPermisosRolRequest {
        @NotNull(message = "El identificador del rol es obligatorio.")
        private Long rolId;

        private Set<Long> permisoIds;
    }
}
