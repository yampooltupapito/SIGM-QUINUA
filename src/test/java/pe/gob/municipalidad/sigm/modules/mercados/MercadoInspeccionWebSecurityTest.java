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
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.*;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MercadoInspeccionWebSecurityTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PersonaRepository personaRepository;
    @Autowired private RolRepository rolRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Autowired private MercadoMunicipalRepository mercadoRepository;
    @Autowired private InspeccionMercadoRepository inspeccionRepository;
    @Autowired private ActaInspeccionMercadoRepository actaRepository;

    private String adminToken;
    private String ciudadanoToken;
    private MercadoMunicipal mercado;
    private InspeccionMercado inspeccion;
    private String tokenVerificacionActa;

    @BeforeEach
    void setUp() {
        long ts = System.currentTimeMillis();

        Rol rolAdmin = rolRepository.findByCodigo("ROLE_ADMIN").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_ADMIN").nombre("Administrador").estado("ACTIVO").build()));

        Rol rolCiudadano = rolRepository.findByCodigo("ROLE_CIUDADANO").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_CIUDADANO").nombre("Ciudadano").estado("ACTIVO").build()));

        Persona personaAdmin = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("46" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Admin")
                .apellidoPaterno("Inspecciones")
                .correo("admin.insp" + ts + "@quinua.gob.pe")
                .estado("ACTIVO")
                .build());

        Usuario adminUser = usuarioRepository.save(Usuario.builder()
                .username("admin_insp_" + ts)
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
                        "MERCADOS_INSPECCION_VER",
                        "MERCADOS_INSPECCION_CREAR",
                        "MERCADOS_INSPECCION_PROGRAMAR",
                        "MERCADOS_INSPECCION_ASIGNAR",
                        "MERCADOS_INSPECCION_EJECUTAR",
                        "MERCADOS_INSPECCION_HALLAZGO_REGISTRAR",
                        "MERCADOS_INSPECCION_EVIDENCIA_GESTIONAR",
                        "MERCADOS_INSPECCION_MEDIDA_GESTIONAR",
                        "MERCADOS_INSPECCION_SUBSANACION_VERIFICAR",
                        "MERCADOS_INSPECCION_ACTA_ELABORAR",
                        "MERCADOS_INSPECCION_ACTA_APROBAR",
                        "MERCADOS_INSPECCION_REPORTES",
                        "MERCADOS_INSPECCION_AUDITORIA_VER"
                ))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("MERCADOS_INSPECCION_VER"),
                        new SimpleGrantedAuthority("MERCADOS_INSPECCION_CREAR"),
                        new SimpleGrantedAuthority("MERCADOS_INSPECCION_PROGRAMAR"),
                        new SimpleGrantedAuthority("MERCADOS_INSPECCION_ASIGNAR"),
                        new SimpleGrantedAuthority("MERCADOS_INSPECCION_EJECUTAR"),
                        new SimpleGrantedAuthority("MERCADOS_INSPECCION_HALLAZGO_REGISTRAR"),
                        new SimpleGrantedAuthority("MERCADOS_INSPECCION_EVIDENCIA_GESTIONAR"),
                        new SimpleGrantedAuthority("MERCADOS_INSPECCION_MEDIDA_GESTIONAR"),
                        new SimpleGrantedAuthority("MERCADOS_INSPECCION_SUBSANACION_VERIFICAR"),
                        new SimpleGrantedAuthority("MERCADOS_INSPECCION_ACTA_ELABORAR"),
                        new SimpleGrantedAuthority("MERCADOS_INSPECCION_ACTA_APROBAR"),
                        new SimpleGrantedAuthority("MERCADOS_INSPECCION_REPORTES"),
                        new SimpleGrantedAuthority("MERCADOS_INSPECCION_AUDITORIA_VER")
                ))
                .build();

        adminToken = jwtTokenProvider.generateAccessToken(adminDetails);

        Persona personaCiudadano = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("70" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Ciudadano")
                .apellidoPaterno("SinPermiso")
                .correo("ciudadano" + ts + "@gmail.com")
                .estado("ACTIVO")
                .build());

        Usuario ciudadanoUser = usuarioRepository.save(Usuario.builder()
                .username("ciudadano_" + ts)
                .persona(personaCiudadano)
                .passwordHash(passwordEncoder.encode("Ciudadano123!"))
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
                .codigo("MER-INSP-" + (ts % 10000))
                .nombre("Mercado Test Inspecciones")
                .direccion("Jr. Arica 100, Quinua")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .estado(EstadoMercado.ACTIVO)
                .build());

        inspeccion = inspeccionRepository.save(InspeccionMercado.builder()
                .codigoInspeccion("INS-SEC-" + (ts % 10000))
                .tipoInspeccion(TipoInspeccionMercado.PROGRAMADA)
                .mercado(mercado)
                .fechaProgramada(LocalDate.now())
                .horaProgramada(LocalTime.of(10, 0))
                .estado(EstadoInspeccion.PROGRAMADA)
                .motivo("Inspección de prueba")
                .objetivo("Verificar condiciones")
                .inspectorResponsable(adminUser)
                .build());

        tokenVerificacionActa = "TOKEN_SEC_" + UUID.randomUUID().toString().replace("-", "");

        actaRepository.save(ActaInspeccionMercado.builder()
                .inspeccion(inspeccion)
                .codigoActa("ACTA-SEC-" + (ts % 10000))
                .fechaElaboracion(OffsetDateTime.now())
                .resumen("Resumen de seguridad")
                .conclusiones("Conclusiones válidas")
                .estado(EstadoActaInspeccion.EMITIDA)
                .codigoVerificacion(tokenVerificacionActa)
                .hashSha256("hash_seguro_sha256")
                .responsable(adminUser)
                .build());
    }

    @Test
    @DisplayName("01. Renderizar Thymeleaf dashboard de supervisión para usuario autorizado")
    void test01_dashboardSupervision_autorizado() throws Exception {
        mockMvc.perform(get("/dashboard/mercados/inspecciones/dashboard")
                        .cookie(new Cookie(SecurityConstants.ACCESS_TOKEN_COOKIE_NAME, adminToken)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Supervisión y Fiscalización de Mercados")));
    }

    @Test
    @DisplayName("02. Renderizar bandeja de inspecciones para usuario autorizado")
    void test02_inspeccionesListado_autorizado() throws Exception {
        mockMvc.perform(get("/dashboard/mercados/inspecciones")
                        .cookie(new Cookie(SecurityConstants.ACCESS_TOKEN_COOKIE_NAME, adminToken)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Bandeja de Inspecciones")));
    }

    @Test
    @DisplayName("03. Renderizar calendario de inspecciones para usuario autorizado")
    void test03_calendario_autorizado() throws Exception {
        mockMvc.perform(get("/dashboard/mercados/inspecciones/calendario")
                        .cookie(new Cookie(SecurityConstants.ACCESS_TOKEN_COOKIE_NAME, adminToken)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cronograma y Calendario de Inspecciones")));
    }

    @Test
    @DisplayName("04. Renderizar panel de seguimiento de medidas correctivas")
    void test04_seguimientoMedidas_autorizado() throws Exception {
        mockMvc.perform(get("/dashboard/mercados/inspecciones/seguimiento")
                        .cookie(new Cookie(SecurityConstants.ACCESS_TOKEN_COOKIE_NAME, adminToken)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Control y Seguimiento de Medidas Correctivas")));
    }

    @Test
    @DisplayName("05. API inspecciones accesible para usuario con MERCADOS_INSPECCION_VER")
    void test05_apiInspecciones_autorizado() throws Exception {
        mockMvc.perform(get("/api/mercados/inspecciones")
                        .header("Authorization", "Bearer " + adminToken)
                        .cookie(new Cookie(SecurityConstants.ACCESS_TOKEN_COOKIE_NAME, adminToken)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("06. API inspecciones rechaza acceso (403) a usuario sin permisos")
    void test06_apiInspecciones_rechazaSinPermiso() throws Exception {
        mockMvc.perform(get("/api/mercados/inspecciones")
                        .header("Authorization", "Bearer " + ciudadanoToken)
                        .cookie(new Cookie(SecurityConstants.ACCESS_TOKEN_COOKIE_NAME, ciudadanoToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("07. Mutación POST a API rechaza petición sin token CSRF")
    void test07_proteccionCsrf_postSinToken() throws Exception {
        String body = """
                {
                    "tipoInspeccion": "PROGRAMADA",
                    "mercadoId": "%s",
                    "fechaProgramada": "2026-11-01",
                    "motivo": "Intento sin CSRF",
                    "objetivo": "Prueba de seguridad"
                }
                """.formatted(mercado.getId());

        mockMvc.perform(post("/api/mercados/inspecciones")
                        .header("Authorization", "Bearer " + adminToken)
                        .cookie(new Cookie(SecurityConstants.ACCESS_TOKEN_COOKIE_NAME, adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("08. Ruta pública de verificación QR de acta accesible sin autenticación")
    void test08_rutaPublicaVerificacionQr_sinAutenticacion() throws Exception {
        mockMvc.perform(get("/verificar/mercados/acta/" + tokenVerificacionActa))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valida").value(true))
                .andExpect(jsonPath("$.entidadEmisora").value(containsString("Municipalidad Distrital de Quinua")));
    }
}
