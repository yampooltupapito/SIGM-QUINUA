package pe.gob.municipalidad.sigm.modules.administracion.dashboard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import pe.gob.municipalidad.sigm.common.model.Cargo;
import pe.gob.municipalidad.sigm.common.model.Oficina;
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
import pe.gob.municipalidad.sigm.modules.administracion.dashboard.service.impl.DashboardServiceImpl;
import pe.gob.municipalidad.sigm.modules.administracion.entity.AuditoriaAdministrativa;
import pe.gob.municipalidad.sigm.modules.administracion.repository.AuditoriaAdministrativaRepository;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private OficinaRepository oficinaRepository;
    @Mock
    private RolRepository rolRepository;
    @Mock
    private PersonaRepository personaRepository;
    @Mock
    private TipoActorRepository tipoActorRepository;
    @Mock
    private PersonaOficinaRepository personaOficinaRepository;
    @Mock
    private AuditoriaAdministrativaRepository auditoriaAdministrativaRepository;

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardServiceImpl(
                usuarioRepository,
                oficinaRepository,
                rolRepository,
                personaRepository,
                tipoActorRepository,
                personaOficinaRepository,
                auditoriaAdministrativaRepository
        );
    }

    @Test
    @DisplayName("1. Resumen Estadístico: Recupera métricas reales de conteo desde los repositorios")
    void testObtenerResumenEstadistico() {
        when(usuarioRepository.countByEnabledTrue()).thenReturn(15L);
        when(usuarioRepository.countByEnabledFalse()).thenReturn(2L);
        when(usuarioRepository.countByAccountLockedTrue()).thenReturn(1L);
        when(oficinaRepository.countByEstado("ACTIVO")).thenReturn(8L);
        when(rolRepository.countByEstado("ACTIVO")).thenReturn(5L);
        when(personaRepository.count()).thenReturn(20L);
        when(tipoActorRepository.countByEstado("ACTIVO")).thenReturn(4L);

        DashboardSummaryDto summary = dashboardService.obtenerResumenEstadistico();

        assertThat(summary).isNotNull();
        assertThat(summary.getTotalUsuariosActivos()).isEqualTo(15L);
        assertThat(summary.getTotalUsuariosInactivos()).isEqualTo(2L);
        assertThat(summary.getTotalUsuariosBloqueados()).isEqualTo(1L);
        assertThat(summary.getTotalOficinasActivas()).isEqualTo(8L);
        assertThat(summary.getTotalRolesActivos()).isEqualTo(5L);
        assertThat(summary.getTotalPersonasRegistradas()).isEqualTo(20L);
        assertThat(summary.getTotalTiposActores()).isEqualTo(4L);
        assertThat(summary.getRecaudacionTributaria()).isEqualTo("Operativo");
    }

    @Test
    @DisplayName("2. Gráficos Interactivos: Dataset de estado, oficinas, roles y acciones")
    void testObtenerDatosGraficos() {
        when(usuarioRepository.countByEnabledTrue()).thenReturn(10L);
        when(usuarioRepository.countByEnabledFalse()).thenReturn(1L);
        when(usuarioRepository.countByAccountLockedTrue()).thenReturn(0L);

        Object[] ofiRow = new Object[]{"Gerencia Municipal", 4L};
        when(oficinaRepository.countPersonasGroupedByOficina()).thenReturn(java.util.Collections.singletonList(ofiRow));

        Object[] rolRow = new Object[]{"Administrador", 3L};
        when(usuarioRepository.countUsuariosGroupedByRol()).thenReturn(java.util.Collections.singletonList(rolRow));

        Object[] actRow = new Object[]{"CREAR_USUARIO", 12L};
        when(auditoriaAdministrativaRepository.countGroupedByAccion()).thenReturn(java.util.Collections.singletonList(actRow));

        DashboardChartDto charts = dashboardService.obtenerDatosGraficos();

        assertThat(charts).isNotNull();
        assertThat(charts.getUsuariosPorEstado().getData()).containsExactly(10L, 1L, 0L);
        assertThat(charts.getUsuariosPorOficina().getLabels()).contains("Gerencia Municipal");
        assertThat(charts.getUsuariosPorOficina().getData()).contains(4L);
        assertThat(charts.getUsuariosPorRol().getLabels()).contains("Administrador");
        assertThat(charts.getActividadPorAccion().getLabels()).contains("CREAR_USUARIO");
    }

    @Test
    @DisplayName("3. Gráficos sin datos de oficina: No falla y muestra lista vacía o de respaldo")
    void testObtenerDatosGraficosSinDatosOficina() {
        when(usuarioRepository.countByEnabledTrue()).thenReturn(0L);
        when(usuarioRepository.countByEnabledFalse()).thenReturn(0L);
        when(usuarioRepository.countByAccountLockedTrue()).thenReturn(0L);
        when(oficinaRepository.countPersonasGroupedByOficina()).thenReturn(Collections.emptyList());
        when(oficinaRepository.findByEstado("ACTIVO")).thenReturn(Collections.emptyList());
        when(usuarioRepository.countUsuariosGroupedByRol()).thenReturn(Collections.emptyList());
        when(auditoriaAdministrativaRepository.countGroupedByAccion()).thenReturn(Collections.emptyList());

        DashboardChartDto charts = dashboardService.obtenerDatosGraficos();

        assertThat(charts).isNotNull();
        assertThat(charts.getUsuariosPorOficina().getData()).isEmpty();
        assertThat(charts.getUsuariosPorRol().getData()).isEmpty();
    }

    @Test
    @DisplayName("4. Actividad Reciente: Paginación y mapeo a DTO con hora formateada")
    void testObtenerActividadReciente() {
        AuditoriaAdministrativa audit = AuditoriaAdministrativa.builder()
                .id(1L)
                .usuarioResponsable("admin.quinua")
                .accion("CREAR_ROL")
                .entidadAfectada("ROL")
                .entidadId(5L)
                .resultado("EXITO")
                .detalle("Creación de rol de prueba")
                .createdAt(OffsetDateTime.now())
                .build();

        Page<AuditoriaAdministrativa> pageMock = new PageImpl<>(List.of(audit));
        when(auditoriaAdministrativaRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(pageMock);

        Page<DashboardActivityDto> result = dashboardService.obtenerActividadReciente("todos", "EXITO", PageRequest.of(0, 10));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAccion()).isEqualTo("CREAR_ROL");
        assertThat(result.getContent().get(0).getUsuarioResponsable()).isEqualTo("admin.quinua");
    }

    @Test
    @DisplayName("5. Perfil de Usuario: Datos institucionales, cargo, oficina e iniciales")
    void testObtenerPerfilUsuario() {
        Persona persona = Persona.builder()
                .id(100L)
                .nombres("Juan Carlos")
                .apellidoPaterno("Quispe")
                .apellidoMaterno("Flores")
                .tipoDocumento("DNI")
                .numeroDocumento("45892314")
                .correo("jcquispe@quinua.gob.pe")
                .build();

        Rol rolAdmin = Rol.builder().id(1L).codigo("ROLE_ADMIN").nombre("Administrador").build();

        Usuario usuario = Usuario.builder()
                .id(1L)
                .username("jcquispe")
                .persona(persona)
                .roles(Set.of(rolAdmin))
                .build();

        when(usuarioRepository.findByUsernameWithRolesAndPersona("jcquispe")).thenReturn(Optional.of(usuario));

        Oficina ofi = Oficina.builder().id(2L).nombre("Alcaldía Municipal").build();
        Cargo cargo = Cargo.builder().id(3L).nombre("Alcalde Distrital").build();

        PersonaOficina po = PersonaOficina.builder()
                .id(10L)
                .persona(persona)
                .oficina(ofi)
                .cargo(cargo)
                .principal(true)
                .build();

        when(personaOficinaRepository.findByPersonaIdAndEstado(100L, "ACTIVO")).thenReturn(List.of(po));

        UserProfileDto profile = dashboardService.obtenerPerfilUsuario("jcquispe");

        assertThat(profile).isNotNull();
        assertThat(profile.getUsername()).isEqualTo("jcquispe");
        assertThat(profile.getNombreCompleto()).isEqualTo("Juan Carlos Quispe Flores");
        assertThat(profile.getIniciales()).isEqualTo("JQ");
        assertThat(profile.getOficina()).isEqualTo("Alcaldía Municipal");
        assertThat(profile.getCargo()).isEqualTo("Alcalde Distrital");
        assertThat(profile.isEsAdmin()).isTrue();
    }

    @Test
    @DisplayName("6. Módulos del Sidebar: Incluye los 4 servicios municipales oficiales y administración")
    void testObtenerModulosSidebar() {
        List<ModuloMenuDto> modulos = dashboardService.obtenerModulosSidebar("admin", "/dashboard/administracion");

        assertThat(modulos).hasSize(5);
        assertThat(modulos.stream().map(ModuloMenuDto::getCodigo))
                .containsExactly(
                        "TRAMITE_DOCUMENTARIO",
                        "MERCADOS_MUNICIPALES",
                        "SEGURIDAD_CIUDADANA",
                        "RENTAS_TRIBUTACION",
                        "ADMINISTRACION_SISTEMA"
                );

        ModuloMenuDto adminMod = modulos.stream()
                .filter(m -> "ADMINISTRACION_SISTEMA".equals(m.getCodigo()))
                .findFirst()
                .orElse(null);

        assertThat(adminMod).isNotNull();
        assertThat(adminMod.isDisponible()).isTrue();
    }
}
