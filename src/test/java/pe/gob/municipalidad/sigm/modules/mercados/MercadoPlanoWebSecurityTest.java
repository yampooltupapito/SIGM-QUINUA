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
import pe.gob.municipalidad.sigm.modules.mercados.dto.ElementoPlanoRequest;
import pe.gob.municipalidad.sigm.modules.mercados.dto.GuardarPlanoRequest;
import pe.gob.municipalidad.sigm.modules.mercados.dto.PlanoMercadoCreateRequest;
import pe.gob.municipalidad.sigm.modules.mercados.dto.PlanoMercadoResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.*;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;
import pe.gob.municipalidad.sigm.modules.mercados.service.PlanoMercadoService;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MercadoPlanoWebSecurityTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PersonaRepository personaRepository;
    @Autowired private RolRepository rolRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Autowired private MercadoMunicipalRepository mercadoRepository;
    @Autowired private PuestoComercialRepository puestoRepository;
    @Autowired private ComercianteRepository comercianteRepository;
    @Autowired private AsignacionPuestoRepository asignacionRepository;
    @Autowired private PlanoMercadoService planoService;

    private String adminToken;
    private String operadorSinEdicionToken;
    private MercadoMunicipal mercado;
    private PuestoComercial puesto;
    private PlanoMercadoResponse plano;
    private Comerciante comerciante;

    @BeforeEach
    void setUp() {
        long ts = System.currentTimeMillis();

        Rol rolAdmin = rolRepository.findByCodigo("ROLE_ADMIN").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_ADMIN").nombre("Administrador").estado("ACTIVO").build()));

        Rol rolOperador = rolRepository.findByCodigo("ROLE_OPERADOR").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_OPERADOR").nombre("Operador").estado("ACTIVO").build()));

        Persona personaAdmin = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("49" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Admin")
                .apellidoPaterno("Planos")
                .correo("admin.planos" + ts + "@quinua.gob.pe")
                .estado("ACTIVO")
                .build());

        Usuario adminUser = usuarioRepository.save(Usuario.builder()
                .username("admin_plano_" + ts)
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
                        "MERCADOS_PLANO_VER",
                        "MERCADOS_PLANO_CREAR",
                        "MERCADOS_PLANO_EDITAR",
                        "MERCADOS_PLANO_GUARDAR",
                        "MERCADOS_PLANO_PUBLICAR",
                        "MERCADOS_PLANO_VERSIONES_VER",
                        "MERCADOS_PLANO_RESTAURAR",
                        "MERCADOS_COMERCIANTE_VER"
                ))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("MERCADOS_PLANO_VER"),
                        new SimpleGrantedAuthority("MERCADOS_PLANO_CREAR"),
                        new SimpleGrantedAuthority("MERCADOS_PLANO_EDITAR"),
                        new SimpleGrantedAuthority("MERCADOS_PLANO_GUARDAR"),
                        new SimpleGrantedAuthority("MERCADOS_PLANO_PUBLICAR"),
                        new SimpleGrantedAuthority("MERCADOS_PLANO_VERSIONES_VER"),
                        new SimpleGrantedAuthority("MERCADOS_PLANO_RESTAURAR"),
                        new SimpleGrantedAuthority("MERCADOS_COMERCIANTE_VER")
                ))
                .build();
        adminToken = jwtTokenProvider.generateAccessToken(adminDetails);

        // Usuario que solo tiene permiso de ver plano pero NO editar ni ver comerciantes sensibles
        Persona personaOp = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("47" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Operador")
                .apellidoPaterno("Consulta")
                .correo("op.consulta" + ts + "@quinua.gob.pe")
                .estado("ACTIVO")
                .build());

        Usuario opUser = usuarioRepository.save(Usuario.builder()
                .username("op_cons_" + ts)
                .persona(personaOp)
                .passwordHash(passwordEncoder.encode("OpPass123!"))
                .enabled(true)
                .accountLocked(false)
                .roles(Set.of(rolOperador))
                .build());

        CustomUserDetails opDetails = CustomUserDetails.builder()
                .id(opUser.getId())
                .username(opUser.getUsername())
                .password("dummy")
                .enabled(true)
                .accountNonLocked(true)
                .roles(Set.of("ROLE_OPERADOR"))
                .permisos(Set.of(
                        "MERCADOS_PLANO_VER"
                ))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_OPERADOR"),
                        new SimpleGrantedAuthority("MERCADOS_PLANO_VER")
                ))
                .build();
        operadorSinEdicionToken = jwtTokenProvider.generateAccessToken(opDetails);

        // Crear mercado y puesto
        mercado = mercadoRepository.save(MercadoMunicipal.builder()
                .nombre("Mercado Test Seguridad " + ts)
                .codigo("MKT-SEC-" + (ts % 10000))
                .direccion("Plaza Principal")
                .estado(EstadoMercado.ACTIVO)
                .build());

        puesto = puestoRepository.save(PuestoComercial.builder()
                .mercado(mercado)
                .codigo("P-SEC-01")
                .numero("01")
                .nombreReferencial("Puesto Central")
                .estado(EstadoPuesto.DISPONIBLE)
                .estadoFisico(EstadoFisicoPuesto.OPERATIVO)
                .build());

        // Crear plano inicial
        plano = planoService.crearPlano(PlanoMercadoCreateRequest.builder()
                .mercadoId(mercado.getId())
                .codigo("PLN-SEC-" + ts)
                .nombre("Plano Seguridad")
                .anchoBase(new BigDecimal("1400"))
                .altoBase(new BigDecimal("900"))
                .build(), "admin", null);

        // Comerciante con asignación vigente
        Persona personaCom = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("70123456")
                .nombres("Rosa")
                .apellidoPaterno("Flores")
                .build());

        comerciante = comercianteRepository.save(Comerciante.builder()
                .codigoComerciante("COM-SEC-" + ts)
                .persona(personaCom)
                .tipoComerciante(TipoComerciante.PERSONA_NATURAL)
                .build());

        asignacionRepository.save(AsignacionPuesto.builder()
                .codigoAsignacion("ASIG-SEC-" + ts)
                .puesto(puesto)
                .comerciante(comerciante)
                .fechaInicio(LocalDate.now().minusDays(30))
                .fechaFin(LocalDate.now().plusMonths(6))
                .estado(EstadoAsignacion.VIGENTE)
                .build());
    }

    @Test
    @DisplayName("Usuario con permiso MERCADOS_PLANO_VER accede a la bandeja de planos")
    void testAccesoBandejaPlanosConPermiso() throws Exception {
        mockMvc.perform(get("/dashboard/mercados/planos")
                        .cookie(new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, adminToken)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Planos Interactivos y Distribución Espacial")));
    }

    @Test
    @DisplayName("Usuario no autenticado es redirigido a login al intentar ver planos")
    void testAccesoNoAutenticadoRedirige() throws Exception {
        mockMvc.perform(get("/dashboard/mercados/planos"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("Usuario sin permiso MERCADOS_PLANO_EDITAR recibe 403 al intentar acceder al editor")
    void testAccesoEditorSinPermisoForbidden() throws Exception {
        mockMvc.perform(get("/dashboard/mercados/planos/" + mercado.getId() + "/editar")
                        .cookie(new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, operadorSinEdicionToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("API /api/mercados/{id}/plano devuelve datos completos con token válido")
    void testApiObtenerPlanoConToken() throws Exception {
        mockMvc.perform(get("/api/mercados/" + mercado.getId() + "/plano")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(plano.getCodigo()))
                .andExpect(jsonPath("$.mercadoNombre").value(mercado.getNombre()));
    }

    @Test
    @DisplayName("API /api/mercados/puestos/{id}/informacion-plano enmascara DNI para usuario sin MERCADOS_COMERCIANTE_VER")
    void testDetallePuestoEnmascaraDatosPrivadosSinPermiso() throws Exception {
        mockMvc.perform(get("/api/mercados/puestos/" + puesto.getId() + "/informacion-plano")
                        .header("Authorization", "Bearer " + operadorSinEdicionToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ocupado").value(true))
                .andExpect(jsonPath("$.datosPrivadosVisibles").value(false))
                .andExpect(jsonPath("$.comercianteDocumento").value(containsString("****")));
    }

    @Test
    @DisplayName("API /api/mercados/puestos/{id}/informacion-plano muestra datos completos para usuario autorizado")
    void testDetallePuestoMuestraDatosCompletosConPermiso() throws Exception {
        mockMvc.perform(get("/api/mercados/puestos/" + puesto.getId() + "/informacion-plano")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ocupado").value(true))
                .andExpect(jsonPath("$.datosPrivadosVisibles").value(true))
                .andExpect(jsonPath("$.comercianteDocumento").value("70123456"))
                .andExpect(jsonPath("$.comercianteNombre").value(containsString("Rosa Flores")));
    }
}
