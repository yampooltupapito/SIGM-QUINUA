package pe.gob.municipalidad.sigm.modules.administracion.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.ConflictException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.ModuloSistema;
import pe.gob.municipalidad.sigm.common.model.Permiso;
import pe.gob.municipalidad.sigm.common.model.Rol;
import pe.gob.municipalidad.sigm.common.repository.ModuloSistemaRepository;
import pe.gob.municipalidad.sigm.common.repository.PermisoRepository;
import pe.gob.municipalidad.sigm.common.repository.RolRepository;
import pe.gob.municipalidad.sigm.modules.administracion.dto.PermisoDtos.MatrizPermisosResponse;
import pe.gob.municipalidad.sigm.modules.administracion.dto.PermisoDtos.ModuloPermisosDto;
import pe.gob.municipalidad.sigm.modules.administracion.dto.PermisoDtos.PermisoResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio de administración y consulta de permisos granulares por módulo y acción.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermisoAdminService {

    private final PermisoRepository permisoRepository;
    private final ModuloSistemaRepository moduloRepository;
    private final RolRepository rolRepository;
    private final AuditoriaAdministrativaService auditoriaService;

    @Transactional(readOnly = true)
    public List<PermisoResponse> listarTodos() {
        return permisoRepository.findAllActivosWithModulo().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MatrizPermisosResponse obtenerMatrizPermisos(Long rolId) {
        List<Permiso> todosPermisos = permisoRepository.findAllActivosWithModulo();
        Set<Long> permisosAsignadosIds = Collections.emptySet();
        String rolCodigo = null;
        String rolNombre = null;

        if (rolId != null) {
            Rol rol = rolRepository.findWithPermisosById(rolId)
                    .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con ID: " + rolId));
            rolCodigo = rol.getCodigo();
            rolNombre = rol.getNombre();
            permisosAsignadosIds = rol.getPermisos().stream()
                    .map(Permiso::getId)
                    .collect(Collectors.toSet());
        }

        // Agrupar permisos por módulo conservando orden
        Map<ModuloSistema, List<Permiso>> porModulo = new LinkedHashMap<>();
        for (Permiso p : todosPermisos) {
            porModulo.computeIfAbsent(p.getModulo(), k -> new ArrayList<>()).add(p);
        }

        List<ModuloPermisosDto> modulosDto = new ArrayList<>();
        for (Map.Entry<ModuloSistema, List<Permiso>> entry : porModulo.entrySet()) {
            ModuloSistema m = entry.getKey();
            List<PermisoResponse> permisosResp = entry.getValue().stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());

            modulosDto.add(ModuloPermisosDto.builder()
                    .moduloId(m.getId())
                    .moduloCodigo(m.getCodigo())
                    .moduloNombre(m.getNombre())
                    .permisos(permisosResp)
                    .build());
        }

        return MatrizPermisosResponse.builder()
                .rolId(rolId)
                .rolCodigo(rolCodigo)
                .rolNombre(rolNombre)
                .permisosAsignadosIds(permisosAsignadosIds)
                .modulos(modulosDto)
                .build();
    }

    @Transactional
    public PermisoResponse crearPermiso(Long moduloId, String codigo, String nombre, String accion, String descripcion) {
        if (permisoRepository.existsByCodigo(codigo)) {
            throw new ConflictException("Ya existe un permiso registrado con el código: " + codigo);
        }
        ModuloSistema modulo = moduloRepository.findById(moduloId)
                .orElseThrow(() -> new ResourceNotFoundException("Módulo no encontrado con ID: " + moduloId));

        Permiso permiso = Permiso.builder()
                .modulo(modulo)
                .codigo(codigo.toUpperCase().trim())
                .nombre(nombre.trim())
                .accion(accion.toUpperCase().trim())
                .descripcion(descripcion)
                .estado("ACTIVO")
                .createdAt(LocalDateTime.now())
                .build();

        Permiso guardado = permisoRepository.save(permiso);
        auditoriaService.registrar("CREAR_PERMISO", "PERMISO", guardado.getId(), "EXITO",
                "Permiso creado con código: " + guardado.getCodigo());

        return toResponse(guardado);
    }

    private PermisoResponse toResponse(Permiso p) {
        return PermisoResponse.builder()
                .id(p.getId())
                .moduloId(p.getModulo().getId())
                .moduloCodigo(p.getModulo().getCodigo())
                .moduloNombre(p.getModulo().getNombre())
                .codigo(p.getCodigo())
                .nombre(p.getNombre())
                .accion(p.getAccion())
                .descripcion(p.getDescripcion())
                .estado(p.getEstado())
                .build();
    }
}
