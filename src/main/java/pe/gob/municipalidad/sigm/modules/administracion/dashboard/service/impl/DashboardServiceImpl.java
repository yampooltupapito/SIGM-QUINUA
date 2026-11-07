package pe.gob.municipalidad.sigm.modules.administracion.dashboard.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.PersonaOficina;
import pe.gob.municipalidad.sigm.common.model.Rol;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.PersonaOficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.common.repository.RolRepository;
import pe.gob.municipalidad.sigm.common.repository.TipoActorRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.administracion.dashboard.dto.DashboardActivityDto;
import pe.gob.municipalidad.sigm.modules.administracion.dashboard.dto.DashboardChartDto;
import pe.gob.municipalidad.sigm.modules.administracion.dashboard.dto.DashboardSummaryDto;
import pe.gob.municipalidad.sigm.modules.administracion.dashboard.dto.ModuloMenuDto;
import pe.gob.municipalidad.sigm.modules.administracion.dashboard.dto.UserProfileDto;
import pe.gob.municipalidad.sigm.modules.administracion.dashboard.service.DashboardService;
import pe.gob.municipalidad.sigm.modules.administracion.entity.AuditoriaAdministrativa;
import pe.gob.municipalidad.sigm.modules.administracion.repository.AuditoriaAdministrativaRepository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UsuarioRepository usuarioRepository;
    private final OficinaRepository oficinaRepository;
    private final RolRepository rolRepository;
    private final PersonaRepository personaRepository;
    private final TipoActorRepository tipoActorRepository;
    private final PersonaOficinaRepository personaOficinaRepository;
    private final AuditoriaAdministrativaRepository auditoriaAdministrativaRepository;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteRepository expedienteRepository;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private pe.gob.municipalidad.sigm.modules.mercados.repository.PuestoComercialRepository puestoComercialRepository;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private pe.gob.municipalidad.sigm.modules.serenazgo.repository.IncidenciaSeguridadCiudadanaRepository incidenciaSeguridadCiudadanaRepository;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.PagoTributarioRepository pagoTributarioRepository;

    private static final ZoneId ZONA_PERU = ZoneId.of("America/Lima");
    private static final DateTimeFormatter FORMATO_FECHA_PERU =
            DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "PE"));

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryDto obtenerResumenEstadistico() {
        long activos = usuarioRepository.countByEnabledTrue();
        long inactivos = usuarioRepository.countByEnabledFalse();
        long bloqueados = usuarioRepository.countByAccountLockedTrue();
        long oficinas = oficinaRepository.countByEstado("ACTIVO");
        long roles = rolRepository.countByEstado("ACTIVO");
        long personas = personaRepository.count();
        long actores = tipoActorRepository.countByEstado("ACTIVO");

        String expStatus = expedienteRepository != null ? (expedienteRepository.count() + " Expedientes (Operativo)") : "Operativo";
        String mktStatus = puestoComercialRepository != null ? (puestoComercialRepository.count() + " Puestos (Operativo)") : "Operativo";
        String serenazgoStatus = incidenciaSeguridadCiudadanaRepository != null ? (incidenciaSeguridadCiudadanaRepository.count() + " Incidencias (Operativo)") : "Operativo";
        String tributariaStatus = pagoTributarioRepository != null ? (pagoTributarioRepository.count() + " Pagos (Operativo)") : "Operativo";

        return DashboardSummaryDto.builder()
                .totalUsuariosActivos(activos)
                .totalUsuariosInactivos(inactivos)
                .totalUsuariosBloqueados(bloqueados)
                .totalOficinasActivas(oficinas)
                .totalRolesActivos(roles)
                .totalPersonasRegistradas(personas)
                .totalTiposActores(actores)
                .expedientesRecibidos(expStatus)
                .mercadosRegistrados(mktStatus)
                .incidenciasSerenazgo(serenazgoStatus)
                .recaudacionTributaria(tributariaStatus)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardChartDto obtenerDatosGraficos() {
        DashboardChartDto charts = new DashboardChartDto();

        // 1. Usuarios por estado (Doughnut)
        long activos = usuarioRepository.countByEnabledTrue();
        long inactivos = usuarioRepository.countByEnabledFalse();
        long bloqueados = usuarioRepository.countByAccountLockedTrue();

        DashboardChartDto.ChartDataset estadoDataset = DashboardChartDto.ChartDataset.builder()
                .labels(List.of("Activos", "Inactivos", "Bloqueados"))
                .data(List.of(activos, inactivos, bloqueados))
                .backgroundColors(List.of("#16a34a", "#64748b", "#dc2626"))
                .borderColors(List.of("#15803d", "#475569", "#b91c1c"))
                .build();
        charts.setUsuariosPorEstado(estadoDataset);

        // 2. Personal por Oficina (Bar)
        List<Object[]> oficinasData = oficinaRepository.countPersonasGroupedByOficina();
        List<String> ofiLabels = new ArrayList<>();
        List<Long> ofiCounts = new ArrayList<>();
        List<String> ofiColors = new ArrayList<>();
        List<String> ofiBorders = new ArrayList<>();

        for (Object[] row : oficinasData) {
            String nombre = (String) row[0];
            Long count = ((Number) row[1]).longValue();
            ofiLabels.add(nombre);
            ofiCounts.add(count);
            ofiColors.add("#1d4ed8");
            ofiBorders.add("#3b82f6");
        }

        // Si no hay vinculaciones de oficina aún, incluir oficinas activas con conteo 0 para visualización
        if (ofiLabels.isEmpty()) {
            oficinaRepository.findByEstado("ACTIVO").forEach(o -> {
                ofiLabels.add(o.getNombre());
                ofiCounts.add(0L);
                ofiColors.add("#1d4ed8");
                ofiBorders.add("#3b82f6");
            });
        }

        DashboardChartDto.ChartDataset oficinaDataset = DashboardChartDto.ChartDataset.builder()
                .labels(ofiLabels)
                .data(ofiCounts)
                .backgroundColors(ofiColors)
                .borderColors(ofiBorders)
                .build();
        charts.setUsuariosPorOficina(oficinaDataset);

        // 3. Usuarios por Rol (Bar)
        List<Object[]> rolesData = usuarioRepository.countUsuariosGroupedByRol();
        List<String> rolLabels = new ArrayList<>();
        List<Long> rolCounts = new ArrayList<>();
        List<String> rolColors = new ArrayList<>();
        List<String> rolBorders = new ArrayList<>();

        String[] palette = {"#2563eb", "#0d9488", "#d97706", "#7c3aed", "#db2777", "#059669", "#ea580c"};
        int colorIdx = 0;

        for (Object[] row : rolesData) {
            String rolNombre = (String) row[0];
            Long count = ((Number) row[1]).longValue();
            rolLabels.add(rolNombre);
            rolCounts.add(count);
            rolColors.add(palette[colorIdx % palette.length]);
            rolBorders.add(palette[colorIdx % palette.length]);
            colorIdx++;
        }

        DashboardChartDto.ChartDataset rolDataset = DashboardChartDto.ChartDataset.builder()
                .labels(rolLabels)
                .data(rolCounts)
                .backgroundColors(rolColors)
                .borderColors(rolBorders)
                .build();
        charts.setUsuariosPorRol(rolDataset);

        // 4. Actividad administrativa por acción (Bar)
        List<Object[]> accionData = auditoriaAdministrativaRepository.countGroupedByAccion();
        List<String> actLabels = new ArrayList<>();
        List<Long> actCounts = new ArrayList<>();
        List<String> actColors = new ArrayList<>();
        List<String> actBorders = new ArrayList<>();

        for (int i = 0; i < Math.min(accionData.size(), 7); i++) {
            Object[] row = accionData.get(i);
            actLabels.add((String) row[0]);
            actCounts.add(((Number) row[1]).longValue());
            actColors.add("#3b82f6");
            actBorders.add("#60a5fa");
        }

        DashboardChartDto.ChartDataset actDataset = DashboardChartDto.ChartDataset.builder()
                .labels(actLabels)
                .data(actCounts)
                .backgroundColors(actColors)
                .borderColors(actBorders)
                .build();
        charts.setActividadPorAccion(actDataset);

        return charts;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DashboardActivityDto> obtenerActividadReciente(String periodo, String resultado, Pageable pageable) {
        Specification<AuditoriaAdministrativa> spec = (root, query, cb) -> cb.conjunction();

        if (periodo != null && !periodo.isBlank() && !"todos".equalsIgnoreCase(periodo)) {
            OffsetDateTime ahora = OffsetDateTime.now(ZONA_PERU);
            OffsetDateTime desde = null;

            switch (periodo.toLowerCase()) {
                case "hoy" -> desde = ahora.truncatedTo(ChronoUnit.DAYS);
                case "7dias" -> desde = ahora.minusDays(7);
                case "30dias" -> desde = ahora.minusDays(30);
                case "mes" -> desde = ahora.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
                default -> {}
            }

            if (desde != null) {
                final OffsetDateTime fechaDesde = desde;
                spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), fechaDesde));
            }
        }

        if (resultado != null && !resultado.isBlank() && !"todos".equalsIgnoreCase(resultado)) {
            spec = spec.and((root, query, cb) -> cb.equal(cb.upper(root.get("resultado")), resultado.toUpperCase()));
        }

        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return auditoriaAdministrativaRepository.findAll(spec, sortedPageable)
                .map(DashboardActivityDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileDto obtenerPerfilUsuario(String username) {
        Usuario usuario = usuarioRepository.findByUsernameWithRolesAndPersona(username).orElse(null);
        if (usuario == null) {
            return UserProfileDto.builder()
                    .username(username)
                    .nombreCompleto("Usuario Institucional")
                    .cargo("Servidor Público")
                    .oficina("Municipalidad Distrital de Quinua")
                    .fechaHoraActualPeru(LocalDate.now(ZONA_PERU).format(FORMATO_FECHA_PERU))
                    .esAdmin(false)
                    .build();
        }

        Persona persona = usuario.getPersona();
        String nombreCompleto = persona != null
                ? persona.getNombres() + " " + persona.getApellidoPaterno() + (persona.getApellidoMaterno() != null ? " " + persona.getApellidoMaterno() : "")
                : usuario.getUsername();

        String iniciales = "MQ";
        if (persona != null && persona.getNombres() != null && !persona.getNombres().isBlank()) {
            char p1 = persona.getNombres().charAt(0);
            char p2 = (persona.getApellidoPaterno() != null && !persona.getApellidoPaterno().isBlank())
                    ? persona.getApellidoPaterno().charAt(0) : 'Q';
            iniciales = ("" + p1 + p2).toUpperCase();
        }

        String cargo = "Servidor Municipal";
        Long oficinaId = null;
        String oficinaCodigo = null;
        String oficina = "Municipalidad Distrital de Quinua";

        if (persona != null) {
            List<PersonaOficina> asignaciones = personaOficinaRepository.findByPersonaIdAndEstado(persona.getId(), "ACTIVO");
            if (!asignaciones.isEmpty()) {
                PersonaOficina principal = asignaciones.stream()
                        .filter(po -> Boolean.TRUE.equals(po.getPrincipal()))
                        .findFirst()
                        .orElse(asignaciones.get(0));

                if (principal.getCargo() != null) {
                    cargo = principal.getCargo().getNombre();
                }
                if (principal.getOficina() != null) {
                    oficinaId = principal.getOficina().getId();
                    oficinaCodigo = principal.getOficina().getCodigo();
                    oficina = principal.getOficina().getNombre();
                }
            }
        }

        List<String> rolesNombres = usuario.getRoles() != null
                ? usuario.getRoles().stream().map(Rol::getNombre).collect(Collectors.toList())
                : new ArrayList<>();

        java.util.Set<String> codigosRol = usuario.getRoles() != null
                ? usuario.getRoles().stream().map(Rol::getCodigo).collect(Collectors.toSet())
                : java.util.Set.of();

        boolean esSuperAdmin = codigosRol.contains("ROLE_SUPERADMIN");
        boolean esAdmin = esSuperAdmin || codigosRol.contains("ROLE_ADMIN");
        boolean esJefeOficina = !esAdmin && codigosRol.contains("ROLE_JEFE_OFICINA");
        boolean esOperador = !esAdmin && !esJefeOficina && (codigosRol.contains("ROLE_OPERADOR")
                || codigosRol.contains("ROLE_MESA_PARTES")
                || codigosRol.contains("ROLE_SERENO")
                || codigosRol.contains("ROLE_CAJERO"));
        boolean esCiudadano = !esAdmin && !esJefeOficina && !esOperador;

        if (esCiudadano && "Servidor Municipal".equals(cargo)) {
            cargo = "Ciudadano Registrado";
        }

        return UserProfileDto.builder()
                .username(usuario.getUsername())
                .nombreCompleto(nombreCompleto)
                .iniciales(iniciales)
                .tipoDocumento(persona != null ? persona.getTipoDocumento() : "DNI")
                .numeroDocumento(persona != null ? persona.getNumeroDocumento() : "---")
                .correo(persona != null ? persona.getCorreo() : "---")
                .telefono(persona != null ? persona.getTelefono() : "---")
                .cargo(cargo)
                .oficinaId(oficinaId)
                .oficinaCodigo(oficinaCodigo)
                .oficina(oficina)
                .fotoUrl(persona != null ? persona.getFotoUrl() : null)
                .roles(rolesNombres)
                .fechaHoraActualPeru(LocalDate.now(ZONA_PERU).format(FORMATO_FECHA_PERU))
                .esSuperAdmin(esSuperAdmin)
                .esAdmin(esAdmin)
                .esJefeOficina(esJefeOficina)
                .esOperador(esOperador)
                .esCiudadano(esCiudadano)
                .build();
    }

    @Override
    public List<ModuloMenuDto> obtenerModulosSidebar(String username, String rutaActiva) {
        return List.of(
                ModuloMenuDto.builder()
                        .codigo("TRAMITE_DOCUMENTARIO")
                        .nombre("Mesa de Partes Virtual")
                        .descripcion("Expedientes y Mesa de Partes Digital")
                        .icono("file-text")
                        .ruta("/dashboard/mesa-partes/bandeja")
                        .disponible(true)
                        .activo(rutaActiva != null && rutaActiva.contains("mesa-partes"))
                        .autorizado(true)
                        .build(),
                ModuloMenuDto.builder()
                        .codigo("MERCADOS_MUNICIPALES")
                        .nombre("Mercados Municipales")
                        .descripcion("Padrón de puestos, contratos y comerciantes")
                        .icono("shopping-bag")
                        .ruta("/dashboard/mercados")
                        .disponible(true)
                        .activo(rutaActiva != null && rutaActiva.contains("mercados"))
                        .autorizado(true)
                        .build(),
                ModuloMenuDto.builder()
                        .codigo("SEGURIDAD_CIUDADANA")
                        .nombre("Seguridad Ciudadana")
                        .descripcion("Serenazgo, patrullajes, alertas y mapa")
                        .icono("shield")
                        .ruta("/dashboard/serenazgo")
                        .disponible(true)
                        .activo(rutaActiva != null && rutaActiva.contains("serenazgo"))
                        .autorizado(true)
                        .build(),
                ModuloMenuDto.builder()
                        .codigo("RENTAS_TRIBUTACION")
                        .nombre("Recaudación Tributaria")
                        .descripcion("Impuesto predial, arbitrios, caja y reportes")
                        .icono("dollar-sign")
                        .ruta("/dashboard/tributaria")
                        .disponible(true)
                        .activo(rutaActiva != null && rutaActiva.contains("tributaria"))
                        .autorizado(true)
                        .build(),
                ModuloMenuDto.builder()
                        .codigo("ADMINISTRACION_SISTEMA")
                        .nombre("Administración Integral")
                        .descripcion("Usuarios, roles, oficinas y auditoría")
                        .icono("settings")
                        .ruta("/admin/usuarios")
                        .disponible(true)
                        .activo(rutaActiva != null && rutaActiva.startsWith("/admin"))
                        .autorizado(true)
                        .build()
        );
    }
}
