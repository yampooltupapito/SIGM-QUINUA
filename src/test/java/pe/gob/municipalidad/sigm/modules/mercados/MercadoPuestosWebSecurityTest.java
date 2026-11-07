package pe.gob.municipalidad.sigm.modules.mercados;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import pe.gob.municipalidad.sigm.common.model.Municipalidad;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Rol;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.MunicipalidadRepository;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.common.repository.RolRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.mercados.dto.PuestoComercialCreateRequest;
import pe.gob.municipalidad.sigm.modules.mercados.entity.MercadoMunicipal;
import pe.gob.municipalidad.sigm.modules.mercados.entity.PuestoComercial;
import pe.gob.municipalidad.sigm.modules.mercados.entity.TipoPuestoComercial;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoMercado;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.repository.MercadoMunicipalRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.PuestoComercialRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.TipoPuestoComercialRepository;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MercadoPuestosWebSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MunicipalidadRepository municipalidadRepository;

    @Autowired
    private OficinaRepository oficinaRepository;

    @Autowired
    private MercadoMunicipalRepository mercadoRepository;

    @Autowired
    private PuestoComercialRepository puestoRepository;

    @Autowired
    private TipoPuestoComercialRepository tipoPuestoRepository;

    private MercadoMunicipal mercado1;
    private MercadoMunicipal mercado2;
    private PuestoComercial puesto1;
    private TipoPuestoComercial tipoFijo;

    private Cookie adminCookie;
    private Cookie sinPermisoCookie;

    @BeforeEach
    void setUp() {
        Municipalidad muni = municipalidadRepository.findAll().stream().findFirst().orElseGet(() ->
                municipalidadRepository.save(Municipalidad.builder()
                        .nombre("Municipalidad Distrital de Quinua")
                        .ubigeo("050108")
                        .departamento("Ayacucho")
                        .provincia("Huamanga")
                        .distrito("Quinua")
                        .build())
        );

        Oficina ofi = oficinaRepository.findAll().stream()
                .filter(o -> "ACTIVO".equalsIgnoreCase(o.getEstado()))
                .findFirst()
                .orElseGet(() -> oficinaRepository.save(Oficina.builder()
                        .codigo("OFI-PUESTOS-TEST-" + (System.currentTimeMillis() % 10000))
                        .nombre("Oficina de Abastecimiento y Mercados")
                        .sigla("OAM")
                        .municipalidad(muni)
                        .estado("ACTIVO")
                        .build())
        );

        mercado1 = mercadoRepository.save(MercadoMunicipal.builder()
                .codigo("MERC-SEC-01-" + UUID.randomUUID().toString().substring(0, 8))
                .nombre("Mercado de Abastos Quinua")
                .direccion("Jr. Sucre 456, Quinua")
                .estado(EstadoMercado.ACTIVO)
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .oficinaAdministradora(ofi)
                .version(0L)
                .build());

        mercado2 = mercadoRepository.save(MercadoMunicipal.builder()
                .codigo("MERC-SEC-02-" + UUID.randomUUID().toString().substring(0, 8))
                .nombre("Mercado Feria Dominical")
                .direccion("Carretera Quinua Km 2, Quinua")
                .estado(EstadoMercado.ACTIVO)
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .oficinaAdministradora(ofi)
                .version(0L)
                .build());

        tipoFijo = tipoPuestoRepository.findByCodigo("FIJO").orElseGet(() ->
                tipoPuestoRepository.save(TipoPuestoComercial.builder()
                        .codigo("FIJO")
                        .nombre("Puesto Fijo")
                        .permiteInstalacionFija(true)
                        .estado("ACTIVO")
                        .build())
        );

        puesto1 = puestoRepository.save(PuestoComercial.builder()
                .mercado(mercado1)
                .codigo("P-001")
                .numero("01")
                .tipoPuesto(tipoFijo)
                .estado(EstadoPuesto.DISPONIBLE)
                .version(0L)
                .build());

        // Rol ADMIN
        Rol rolAdmin = rolRepository.findByCodigo("ROLE_ADMIN").orElseGet(() ->
                rolRepository.save(Rol.builder()
                        .codigo("ROLE_ADMIN")
                        .nombre("Administrador")
                        .estado("ACTIVO")
                        .build())
        );

        // Rol CIUDADANO (sin permisos de mercados)
        Rol rolCiudadano = rolRepository.findByCodigo("ROLE_CIUDADANO").orElseGet(() ->
                rolRepository.save(Rol.builder()
                        .codigo("ROLE_CIUDADANO")
                        .nombre("Ciudadano")
                        .estado("ACTIVO")
                        .build())
        );

        // Usuario Admin
        Persona personaAdmin = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("4455" + (System.currentTimeMillis() % 10000))
                .nombres("Admin")
                .apellidoPaterno("Mercados")
                .apellidoMaterno("Quinua")
                .correo("admin.mercados" + System.currentTimeMillis() + "@quinua.gob.pe")
                .estado("ACTIVO")
                .build());

        Usuario adminUser = usuarioRepository.save(Usuario.builder()
                .username("admin_puestos_" + System.currentTimeMillis())
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
                        "MERCADOS_MODULO_ACCEDER",
                        "MERCADOS_MERCADO_VER",
                        "MERCADOS_PUESTO_VER",
                        "MERCADOS_PUESTO_GESTIONAR",
                        "MERCADOS_PUESTO_CREAR",
                        "MERCADOS_PUESTO_EDITAR",
                        "MERCADOS_PUESTO_CAMBIAR_ESTADO",
                        "MERCADOS_PUESTO_CAMBIAR_UBICACION"
                ))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("MERCADOS_MODULO_ACCEDER"),
                        new SimpleGrantedAuthority("MERCADOS_MERCADO_VER"),
                        new SimpleGrantedAuthority("MERCADOS_PUESTO_VER"),
                        new SimpleGrantedAuthority("MERCADOS_PUESTO_GESTIONAR"),
                        new SimpleGrantedAuthority("MERCADOS_PUESTO_CREAR"),
                        new SimpleGrantedAuthority("MERCADOS_PUESTO_EDITAR"),
                        new SimpleGrantedAuthority("MERCADOS_PUESTO_CAMBIAR_ESTADO"),
                        new SimpleGrantedAuthority("MERCADOS_PUESTO_CAMBIAR_UBICACION")
                ))
                .build();

        String adminToken = jwtTokenProvider.generateAccessToken(adminDetails);
        adminCookie = new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, adminToken);
        adminCookie.setPath("/");

        // Usuario Ciudadano sin permisos
        Persona personaCiudadano = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("9988" + (System.currentTimeMillis() % 10000))
                .nombres("Juan")
                .apellidoPaterno("Perez")
                .apellidoMaterno("Común")
                .correo("juan.comun" + System.currentTimeMillis() + "@gmail.com")
                .estado("ACTIVO")
                .build());

        Usuario ciudadanoUser = usuarioRepository.save(Usuario.builder()
                .username("ciudadano_" + System.currentTimeMillis())
                .persona(personaCiudadano)
                .passwordHash(passwordEncoder.encode("UserPass123!"))
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
                .permisos(Set.of())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_CIUDADANO")))
                .build();

        String sinPermisoToken = jwtTokenProvider.generateAccessToken(ciudadanoDetails);
        sinPermisoCookie = new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, sinPermisoToken);
        sinPermisoCookie.setPath("/");
    }

    // 29. Verificar permisos
    @Test
    @DisplayName("29. Usuario con permisos accede a listado API de puestos exitosamente (HTTP 200)")
    void testVerificarPermisosAccesoExitoso() throws Exception {
        mockMvc.perform(get("/api/mercados/{mercadoId}/puestos", mercado1.getId())
                        .cookie(adminCookie)
                        .header("Authorization", "Bearer " + adminCookie.getValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    // 30. Rechazar acceso no autorizado
    @Test
    @DisplayName("30. Rechazar acceso a usuario sin permisos (HTTP 403 Forbidden)")
    void testRechazarAccesoNoAutorizado() throws Exception {
        mockMvc.perform(get("/api/mercados/{mercadoId}/puestos", mercado1.getId())
                        .cookie(sinPermisoCookie)
                        .header("Authorization", "Bearer " + sinPermisoCookie.getValue()))
                .andExpect(status().isForbidden());
    }

    // 31. Prevenir IDOR
    @Test
    @DisplayName("31. Prevenir IDOR: Rechazar acceso a puesto de mercado 1 solicitándolo con mercado 2 (HTTP 404)")
    void testPrevenirIdor() throws Exception {
        mockMvc.perform(get("/api/mercados/{mercadoId}/puestos/{puestoId}", mercado2.getId(), puesto1.getId())
                        .cookie(adminCookie)
                        .header("Authorization", "Bearer " + adminCookie.getValue()))
                .andExpect(status().isNotFound());
    }

    // 32. Protección CSRF
    @Test
    @DisplayName("32. Protección CSRF: Rechazar solicitud POST sin token CSRF (HTTP 403)")
    void testProteccionCsrf() throws Exception {
        PuestoComercialCreateRequest req = PuestoComercialCreateRequest.builder()
                .mercadoId(mercado1.getId())
                .tipoPuestoId(tipoFijo.getId())
                .codigo("P-CSRF")
                .numero("99")
                .build();

        mockMvc.perform(post("/api/mercados/{mercadoId}/puestos", mercado1.getId())
                        .cookie(adminCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                // Sin .with(csrf()) en solicitud basada en cookies debe dar 403
                .andExpect(status().isForbidden());
    }

    // 33. Renderizado Thymeleaf
    @Test
    @DisplayName("33. Renderizado de vistas Thymeleaf de puestos comerciales (HTTP 200)")
    void testRenderizadoThymeleaf() throws Exception {
        // Listado
        mockMvc.perform(get("/dashboard/mercados/{mercadoId}/puestos", mercado1.getId())
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("Padrón de Puestos Comerciales")));

        // Formulario Nuevo
        mockMvc.perform(get("/dashboard/mercados/{mercadoId}/puestos/nuevo", mercado1.getId())
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("Registrar Nuevo Puesto Comercial")));

        // Detalle
        mockMvc.perform(get("/dashboard/mercados/{mercadoId}/puestos/{puestoId}", mercado1.getId(), puesto1.getId())
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("PUESTO")));

        // Edición
        mockMvc.perform(get("/dashboard/mercados/{mercadoId}/puestos/{puestoId}/editar", mercado1.getId(), puesto1.getId())
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("Modificar Datos de Puesto")));
    }

    // 34. Integración PostgreSQL
    @Test
    @DisplayName("34. Integración PostgreSQL: Registro y persistencia transaccional real")
    void testIntegracionPostgresql() throws Exception {
        PuestoComercialCreateRequest req = PuestoComercialCreateRequest.builder()
                .mercadoId(mercado1.getId())
                .tipoPuestoId(tipoFijo.getId())
                .codigo("P-PG-INT")
                .numero("777")
                .build();

        mockMvc.perform(post("/api/mercados/{mercadoId}/puestos", mercado1.getId())
                        .cookie(adminCookie)
                        .header("Authorization", "Bearer " + adminCookie.getValue())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("P-PG-INT"));

        boolean exists = puestoRepository.existsByMercadoIdAndCodigo(mercado1.getId(), "P-PG-INT");
        assertThat(exists).isTrue();
    }
}
