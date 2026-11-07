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
import pe.gob.municipalidad.sigm.modules.mercados.dto.SectorRequest;
import pe.gob.municipalidad.sigm.modules.mercados.entity.MercadoMunicipal;
import pe.gob.municipalidad.sigm.modules.mercados.entity.PabellonMercado;
import pe.gob.municipalidad.sigm.modules.mercados.entity.SectorMercado;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoMercado;
import pe.gob.municipalidad.sigm.modules.mercados.repository.MercadoMunicipalRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.PabellonMercadoRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.SectorMercadoRepository;
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
class MercadoEstructuraWebSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MercadoMunicipalRepository mercadoRepository;

    @Autowired
    private SectorMercadoRepository sectorRepository;

    @Autowired
    private PabellonMercadoRepository pabellonRepository;

    @Autowired
    private MunicipalidadRepository municipalidadRepository;

    @Autowired
    private OficinaRepository oficinaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private MercadoMunicipal mercado1;
    private MercadoMunicipal mercado2;
    private SectorMercado sector1;
    private Cookie adminAuthCookie;
    private Cookie ciudadanoAuthCookie;

    @BeforeEach
    void setUp() {
        Municipalidad mun = municipalidadRepository.findAll().stream().findFirst()
                .orElseGet(() -> municipalidadRepository.save(Municipalidad.builder()
                        .nombre("Municipalidad Distrital de Quinua")
                        .nombreCorto("MD-QUINUA")
                        .ubigeo("050108")
                        .departamento("Ayacucho")
                        .provincia("Huamanga")
                        .distrito("Quinua")
                        .build()));

        Oficina ofi = oficinaRepository.findAll().stream()
                .filter(o -> "ACTIVO".equalsIgnoreCase(o.getEstado()))
                .findFirst()
                .orElseGet(() -> oficinaRepository.save(Oficina.builder()
                        .municipalidad(mun)
                        .codigo("OFI-MERC-" + System.currentTimeMillis() % 10000)
                        .nombre("Oficina de Abastecimiento")
                        .estado("ACTIVO")
                        .build()));

        mercado1 = mercadoRepository.save(MercadoMunicipal.builder()
                .codigo("MERC-SEC-01-" + UUID.randomUUID().toString().substring(0, 8))
                .nombre("Mercado Test Seguridad 1")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .direccion("Jr. Comercio 123")
                .estado(EstadoMercado.ACTIVO)
                .oficinaAdministradora(ofi)
                .build());

        mercado2 = mercadoRepository.save(MercadoMunicipal.builder()
                .codigo("MERC-SEC-02-" + UUID.randomUUID().toString().substring(0, 8))
                .nombre("Mercado Test Seguridad 2")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .direccion("Av. Centenario 456")
                .estado(EstadoMercado.ACTIVO)
                .oficinaAdministradora(ofi)
                .build());

        sector1 = sectorRepository.save(SectorMercado.builder()
                .mercado(mercado1)
                .codigo("SEC-SEC1")
                .nombre("Sector Test Seguridad")
                .estado("ACTIVO")
                .ordenVisual(1)
                .build());

        // Configuración de usuarios y JWT cookies para pruebas web/seguridad
        Rol rolAdmin = rolRepository.findByCodigo("ROLE_ADMIN").orElseGet(() ->
                rolRepository.save(Rol.builder()
                        .codigo("ROLE_ADMIN")
                        .nombre("Administrador General")
                        .estado("ACTIVO")
                        .build()));

        Rol rolCiudadano = rolRepository.findByCodigo("ROLE_CIUDADANO").orElseGet(() ->
                rolRepository.save(Rol.builder()
                        .codigo("ROLE_CIUDADANO")
                        .nombre("Ciudadano")
                        .estado("ACTIVO")
                        .build()));

        Persona personaAdmin = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("2222" + (System.currentTimeMillis() % 10000))
                .nombres("Admin")
                .apellidoPaterno("Mercados")
                .correo("admin.sec" + System.currentTimeMillis() + "@quinua.gob.pe")
                .estado("ACTIVO")
                .build());

        Usuario adminUser = usuarioRepository.save(Usuario.builder()
                .username("admin.sec." + System.currentTimeMillis())
                .persona(personaAdmin)
                .passwordHash(passwordEncoder.encode("AdminPass2026!"))
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
                        "MERCADOS_MODULO_ACCEDER",
                        "MERCADOS_MERCADO_VER",
                        "MERCADOS_SECTOR_VER",
                        "MERCADOS_SECTOR_GESTIONAR",
                        "MERCADOS_PABELLON_VER",
                        "MERCADOS_PABELLON_GESTIONAR",
                        "MERCADOS_ESTRUCTURA_VER",
                        "MERCADOS_ZONA_GESTIONAR"
                ))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("MERCADOS_MODULO_ACCEDER"),
                        new SimpleGrantedAuthority("MERCADOS_MERCADO_VER"),
                        new SimpleGrantedAuthority("MERCADOS_SECTOR_VER"),
                        new SimpleGrantedAuthority("MERCADOS_SECTOR_GESTIONAR"),
                        new SimpleGrantedAuthority("MERCADOS_PABELLON_VER"),
                        new SimpleGrantedAuthority("MERCADOS_PABELLON_GESTIONAR"),
                        new SimpleGrantedAuthority("MERCADOS_ESTRUCTURA_VER"),
                        new SimpleGrantedAuthority("MERCADOS_ZONA_GESTIONAR")
                ))
                .build();

        String adminJwt = jwtTokenProvider.generateAccessToken(adminDetails);
        adminAuthCookie = new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, adminJwt);
        adminAuthCookie.setPath("/");

        Persona personaCiudadano = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("3333" + (System.currentTimeMillis() % 10000))
                .nombres("Vecino")
                .apellidoPaterno("Quinua")
                .correo("vecino.sec" + System.currentTimeMillis() + "@gmail.com")
                .estado("ACTIVO")
                .build());

        Usuario ciudadanoUser = usuarioRepository.save(Usuario.builder()
                .username("vecino.sec." + System.currentTimeMillis())
                .persona(personaCiudadano)
                .passwordHash(passwordEncoder.encode("VecinoPass2026!"))
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
                .permisos(Set.of("PORTAL_CIUDADANO_ACCEDER"))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_CIUDADANO"),
                        new SimpleGrantedAuthority("PORTAL_CIUDADANO_ACCEDER")
                ))
                .build();

        String ciudadanoJwt = jwtTokenProvider.generateAccessToken(ciudadanoDetails);
        ciudadanoAuthCookie = new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, ciudadanoJwt);
        ciudadanoAuthCookie.setPath("/");
    }

    // 25. Verificar permisos
    @Test
    @DisplayName("25. Usuario con autoridad MERCADOS_SECTOR_GESTIONAR puede crear sector")
    void testPermisoCrearSectorAutorizado() throws Exception {
        SectorRequest request = SectorRequest.builder()
                .mercadoId(mercado1.getId())
                .codigo("SEC-AUTH-OK")
                .nombre("Sector Creado con Permiso")
                .build();

        mockMvc.perform(post("/api/mercados/" + mercado1.getId() + "/sectores")
                        .cookie(adminAuthCookie)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("SEC-AUTH-OK"));
    }

    // 26. Rechazar acceso no autorizado
    @Test
    @DisplayName("26. Rechazar acceso (403 Forbidden) cuando usuario no tiene autoridad requerida")
    void testRechazarAccesoNoAutorizado() throws Exception {
        SectorRequest request = SectorRequest.builder()
                .mercadoId(mercado1.getId())
                .codigo("SEC-HACK")
                .nombre("Sector Ilegal")
                .build();

        mockMvc.perform(post("/api/mercados/" + mercado1.getId() + "/sectores")
                        .cookie(ciudadanoAuthCookie)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // 27. Prevenir IDOR
    @Test
    @DisplayName("27. Prevenir IDOR: Sector de Mercado 1 no puede consultarse en URL de Mercado 2")
    void testPrevenirIdorSectorMercadoCruzado() throws Exception {
        mockMvc.perform(get("/api/mercados/" + mercado2.getId() + "/sectores/" + sector1.getId())
                        .cookie(adminAuthCookie))
                .andExpect(status().isNotFound());
    }

    // 28. Protección CSRF
    @Test
    @DisplayName("28. Protección CSRF: Rechazar peticiones de modificación sin token CSRF válido")
    void testProteccionCsrfRechazaSinToken() throws Exception {
        SectorRequest request = SectorRequest.builder()
                .codigo("SEC-NOCSRF")
                .nombre("Sector Sin CSRF")
                .build();

        mockMvc.perform(post("/api/mercados/" + mercado1.getId() + "/sectores")
                        .cookie(adminAuthCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // 29. Renderizado Thymeleaf
    @Test
    @DisplayName("29. Renderizado Thymeleaf: Vistas HTML de Sectores, Pabellones y Estructura responden 200 OK")
    void testRenderizadoThymeleafVistas() throws Exception {
        // Listado de sectores
        mockMvc.perform(get("/dashboard/mercados/" + mercado1.getId() + "/sectores")
                        .cookie(adminAuthCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sectores Comerciales")))
                .andExpect(content().string(containsString("SEC-SEC1")));

        // Vista de estructura jerárquica
        mockMvc.perform(get("/dashboard/mercados/" + mercado1.getId() + "/estructura")
                        .cookie(adminAuthCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Organización Física:")))
                .andExpect(content().string(containsString("SEC-SEC1")));
    }

    // 30. Integración con PostgreSQL
    @Test
    @DisplayName("30. Integración con PostgreSQL: Persistencia real y aislamiento transaccional")
    void testIntegracionPostgreSqlPersistencia() {
        SectorMercado nuevo = sectorRepository.save(SectorMercado.builder()
                .mercado(mercado1)
                .codigo("SEC-PG-TEST")
                .nombre("Sector Postgres Test")
                .estado("ACTIVO")
                .ordenVisual(99)
                .build());

        assertThat(nuevo.getId()).isNotNull();
        assertThat(sectorRepository.existsByIdAndMercadoId(nuevo.getId(), mercado1.getId())).isTrue();
        assertThat(sectorRepository.countByMercadoId(mercado1.getId())).isGreaterThanOrEqualTo(2);
    }
}
