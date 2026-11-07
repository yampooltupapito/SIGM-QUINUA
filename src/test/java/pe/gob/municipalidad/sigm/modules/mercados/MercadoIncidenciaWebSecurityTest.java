package pe.gob.municipalidad.sigm.modules.mercados;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.constants.SecurityConstants;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Rol;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.common.repository.RolRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.MercadoMunicipal;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoMercado;
import pe.gob.municipalidad.sigm.modules.mercados.repository.MercadoMunicipalRepository;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MercadoIncidenciaWebSecurityTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PersonaRepository personaRepository;
    @Autowired private RolRepository rolRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private MercadoMunicipalRepository mercadoRepository;

    private String adminToken;
    private String ciudadanoToken;
    private MercadoMunicipal mercado;

    @BeforeEach
    void setUp() {
        long ts = System.currentTimeMillis();

        Rol rolAdmin = rolRepository.findByCodigo("ROLE_ADMIN").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_ADMIN").nombre("Administrador").estado("ACTIVO").build()));

        Rol rolCiudadano = rolRepository.findByCodigo("ROLE_CIUDADANO").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_CIUDADANO").nombre("Ciudadano").estado("ACTIVO").build()));

        Persona personaAdmin = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("47" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Admin")
                .apellidoPaterno("Incidencias")
                .correo("admin.inc" + ts + "@quinua.gob.pe")
                .estado("ACTIVO")
                .build());

        Usuario adminUser = usuarioRepository.save(Usuario.builder()
                .username("admin_inc_" + ts)
                .persona(personaAdmin)
                .passwordHash(passwordEncoder.encode("AdminPass123!"))
                .enabled(true)
                .accountLocked(false)
                .roles(Set.of(rolAdmin))
                .build());

        CustomUserDetails adminDetails = CustomUserDetails.builder()
                .id(adminUser.getId())
                .username(adminUser.getUsername())
                .password("dummy")
                .enabled(true)
                .accountNonLocked(true)
                .roles(Set.of("ROLE_ADMIN"))
                .permisos(Set.of(
                        "ROLE_ADMIN",
                        "MERCADOS_INCIDENCIA_VER",
                        "MERCADOS_INCIDENCIA_CREAR",
                        "MERCADOS_INCIDENCIA_EDITAR",
                        "MERCADOS_INCIDENCIA_ASIGNAR",
                        "MERCADOS_INCIDENCIA_ATENDER",
                        "MERCADOS_INCIDENCIA_EVIDENCIA_GESTIONAR",
                        "MERCADOS_INCIDENCIA_RESOLVER",
                        "MERCADOS_INCIDENCIA_CERRAR",
                        "MERCADOS_INCIDENCIA_REPORTES",
                        "MERCADOS_INFRACCION_VER",
                        "MERCADOS_INFRACCION_REGISTRAR",
                        "MERCADOS_INFRACCION_EVALUAR",
                        "MERCADOS_INFRACCION_DERIVAR",
                        "MERCADOS_TIPIFICACION_GESTIONAR",
                        "MERCADOS_INCIDENCIA_AUDITORIA_VER"
                ))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("MERCADOS_INCIDENCIA_VER"),
                        new SimpleGrantedAuthority("MERCADOS_INCIDENCIA_CREAR"),
                        new SimpleGrantedAuthority("MERCADOS_INCIDENCIA_ASIGNAR"),
                        new SimpleGrantedAuthority("MERCADOS_INCIDENCIA_ATENDER"),
                        new SimpleGrantedAuthority("MERCADOS_INCIDENCIA_RESOLVER"),
                        new SimpleGrantedAuthority("MERCADOS_INCIDENCIA_CERRAR"),
                        new SimpleGrantedAuthority("MERCADOS_INCIDENCIA_REPORTES"),
                        new SimpleGrantedAuthority("MERCADOS_INFRACCION_VER"),
                        new SimpleGrantedAuthority("MERCADOS_INFRACCION_REGISTRAR"),
                        new SimpleGrantedAuthority("MERCADOS_INFRACCION_EVALUAR"),
                        new SimpleGrantedAuthority("MERCADOS_INFRACCION_DERIVAR"),
                        new SimpleGrantedAuthority("MERCADOS_TIPIFICACION_GESTIONAR")
                ))
                .build();

        adminToken = jwtTokenProvider.generateAccessToken(adminDetails);

        Persona personaCiudadano = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("48" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Ciudadano")
                .apellidoPaterno("Comun")
                .correo("ciudadano" + ts + "@gmail.com")
                .estado("ACTIVO")
                .build());

        Usuario ciudadanoUser = usuarioRepository.save(Usuario.builder()
                .username("ciudadano_" + ts)
                .persona(personaCiudadano)
                .passwordHash(passwordEncoder.encode("CiudadanoPass123!"))
                .enabled(true)
                .accountLocked(false)
                .roles(Set.of(rolCiudadano))
                .build());

        CustomUserDetails ciudadanoDetails = CustomUserDetails.builder()
                .id(ciudadanoUser.getId())
                .username(ciudadanoUser.getUsername())
                .password("dummy")
                .enabled(true)
                .accountNonLocked(true)
                .roles(Set.of("ROLE_CIUDADANO"))
                .permisos(Set.of("ROLE_CIUDADANO"))
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_CIUDADANO")))
                .build();

        ciudadanoToken = jwtTokenProvider.generateAccessToken(ciudadanoDetails);

        mercado = mercadoRepository.save(MercadoMunicipal.builder()
                .codigo("MER-SEC-" + (ts % 10000))
                .nombre("Mercado Test Seguridad Incidencias")
                .direccion("Plaza de Armas S/N, Quinua")
                .estado(EstadoMercado.ACTIVO)
                .build());
    }

    @Test
    @DisplayName("Admin puede acceder al Dashboard de Incidencias")
    void adminAccedeADashboardIncidencias() throws Exception {
        mockMvc.perform(get("/dashboard/mercados/incidencias/dashboard")
                        .cookie(new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, adminToken)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Gestión de Incidencias en Mercados")));
    }

    @Test
    @DisplayName("Admin puede acceder al Listado de Incidencias")
    void adminAccedeAListadoIncidencias() throws Exception {
        mockMvc.perform(get("/dashboard/mercados/incidencias")
                        .cookie(new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, adminToken)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Bandeja de Incidencias")));
    }

    @Test
    @DisplayName("Admin puede acceder al Formulario de Nueva Incidencia")
    void adminAccedeAFormularioNuevaIncidencia() throws Exception {
        mockMvc.perform(get("/dashboard/mercados/incidencias/nueva")
                        .cookie(new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, adminToken)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Registro de Nueva Incidencia")));
    }

    @Test
    @DisplayName("Admin puede acceder al Tablero de Seguimiento Operativo")
    void adminAccedeASeguimientoIncidencias() throws Exception {
        mockMvc.perform(get("/dashboard/mercados/incidencias/seguimiento")
                        .cookie(new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, adminToken)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tablero de Seguimiento Operativo")));
    }

    @Test
    @DisplayName("Admin puede acceder al Generador de Reportes")
    void adminAccedeAReportesIncidencias() throws Exception {
        mockMvc.perform(get("/dashboard/mercados/incidencias/reportes")
                        .cookie(new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, adminToken)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Generador de Reportes de Incidencias")));
    }

    @Test
    @DisplayName("Admin puede descargar Reporte de Incidencias en Excel")
    void adminDescargaReporteExcel() throws Exception {
        mockMvc.perform(get("/dashboard/mercados/incidencias/reportes/excel")
                        .cookie(new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, adminToken)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("reporte_incidencias_mercados.xlsx")))
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @Test
    @DisplayName("Admin puede descargar Reporte de Incidencias en PDF")
    void adminDescargaReportePdf() throws Exception {
        mockMvc.perform(get("/dashboard/mercados/incidencias/reportes/pdf")
                        .cookie(new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, adminToken)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("reporte_incidencias_mercados.pdf")))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }

    @Test
    @DisplayName("Admin puede acceder a Presuntas Infracciones")
    void adminAccedeAPresuntasInfracciones() throws Exception {
        mockMvc.perform(get("/dashboard/mercados/infracciones")
                        .cookie(new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, adminToken)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Presuntas Infracciones en Mercados")));
    }

    @Test
    @DisplayName("Admin puede consultar catálogo de tipos de incidencia vía API")
    void adminConsultaTiposIncidenciaApi() throws Exception {
        mockMvc.perform(get("/api/mercados/tipos-incidencia")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Admin puede consultar catálogo de tipificaciones normativas vía API")
    void adminConsultaTipificacionesApi() throws Exception {
        mockMvc.perform(get("/api/mercados/tipificaciones")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Acceso anónimo a API de incidencias debe ser rechazado (401 o 403)")
    void accesoAnonimoRechazado() throws Exception {
        mockMvc.perform(get("/api/mercados/incidencias"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status != 401 && status != 403) {
                        throw new AssertionError("Se esperaba 401 o 403 pero fue: " + status);
                    }
                });
    }

    @Test
    @DisplayName("Ciudadano sin permisos administrativos es rechazado al intentar registrar incidencia (403)")
    void ciudadanoEsRechazadoEnNuevaIncidencia() throws Exception {
        mockMvc.perform(get("/dashboard/mercados/incidencias/nueva")
                        .cookie(new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, ciudadanoToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Ciudadano sin permisos es rechazado al intentar registrar presunta infracción (403)")
    void ciudadanoEsRechazadoEnNuevaInfraccion() throws Exception {
        mockMvc.perform(get("/dashboard/mercados/infracciones/nueva")
                        .cookie(new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, ciudadanoToken)))
                .andExpect(status().isForbidden());
    }
}
