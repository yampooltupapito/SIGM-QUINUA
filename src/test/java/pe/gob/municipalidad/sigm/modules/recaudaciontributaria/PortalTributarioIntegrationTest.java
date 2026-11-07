package pe.gob.municipalidad.sigm.modules.recaudaciontributaria;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.constants.SecurityConstants;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.*;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class PortalTributarioIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ContribuyenteTributarioRepository contribuyenteRepository;

    @Autowired
    private VinculacionUsuarioContribuyenteRepository vinculacionRepository;

    @Autowired
    private SolicitudVinculacionTributariaRepository solicitudRepository;

    @Autowired
    private PredioTributarioRepository predioRepository;

    @Autowired
    private RelacionContribuyentePredioRepository relacionPredioRepository;

    @Autowired
    private CuentaCorrienteTributariaRepository cuentaCorrienteRepository;

    private Usuario ciudadanoUser;
    private Cookie ciudadanoCookie;

    private Usuario adminUser;
    private Cookie adminCookie;

    private ContribuyenteTributario contribuyente1;
    private ContribuyenteTributario contribuyente2;
    private VinculacionUsuarioContribuyente vinculacion1;
    private VinculacionUsuarioContribuyente vinculacion2;

    @BeforeEach
    void setUp() {
        long suffix = System.currentTimeMillis() % 100000000;

        // 1. Crear Persona y Usuario Ciudadano
        Persona personaCiudadano = Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento(String.format("%08d", (suffix + 1000) % 100000000))
                .nombres("Carlos")
                .apellidoPaterno("Quispe")
                .apellidoMaterno("Flores")
                .build();
        personaCiudadano = personaRepository.save(personaCiudadano);

        ciudadanoUser = Usuario.builder()
                .username("carlos_" + suffix)
                .passwordHash("$2a$10$abcdefghijklmnopqrstuvwxyz123456")
                .persona(personaCiudadano)
                .enabled(true)
                .build();
        ciudadanoUser = usuarioRepository.save(ciudadanoUser);

        CustomUserDetails userDetails = CustomUserDetails.builder()
                .id(ciudadanoUser.getId())
                .username(ciudadanoUser.getUsername())
                .password("dummy")
                .enabled(true)
                .accountNonLocked(true)
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_CIUDADANO"),
                        new SimpleGrantedAuthority("PORTAL_TRIBUTARIO_ACCEDER"),
                        new SimpleGrantedAuthority("PORTAL_TRIBUTARIO_CONSULTAR_PREDIOS"),
                        new SimpleGrantedAuthority("PORTAL_TRIBUTARIO_CONSULTAR_OBLIGACIONES"),
                        new SimpleGrantedAuthority("PORTAL_TRIBUTARIO_CONSULTAR_PAGOS"),
                        new SimpleGrantedAuthority("PORTAL_TRIBUTARIO_CONSULTAR_DEUDAS"),
                        new SimpleGrantedAuthority("PORTAL_TRIBUTARIO_CONSULTAR_FRACCIONAMIENTOS"),
                        new SimpleGrantedAuthority("PORTAL_TRIBUTARIO_DESCARGAR_ESTADO_CUENTA"),
                        new SimpleGrantedAuthority("PORTAL_TRIBUTARIO_SOLICITAR_VINCULACION")
                ))
                .build();

        String token = jwtTokenProvider.generateAccessToken(userDetails);
        ciudadanoCookie = new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, token);
        ciudadanoCookie.setPath("/");

        // 2. Crear Operador/Admin de Rentas
        Persona personaAdmin = Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento(String.format("%08d", (suffix + 9000) % 100000000))
                .nombres("Operador")
                .apellidoPaterno("Rentas")
                .apellidoMaterno("Quinua")
                .build();
        personaAdmin = personaRepository.save(personaAdmin);

        Usuario admin = Usuario.builder()
                .username("admin_rentas_" + suffix)
                .passwordHash("$2a$10$abcdefghijklmnopqrstuvwxyz123456")
                .persona(personaAdmin)
                .enabled(true)
                .build();
        adminUser = usuarioRepository.save(admin);

        CustomUserDetails adminDetails = CustomUserDetails.builder()
                .id(adminUser.getId())
                .username(adminUser.getUsername())
                .password("dummy")
                .enabled(true)
                .accountNonLocked(true)
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("ROLE_TRIBUTACION"),
                        new SimpleGrantedAuthority("TRIBUTARIA_PORTAL_VALIDAR_VINCULACION"),
                        new SimpleGrantedAuthority("TRIBUTARIA_PORTAL_REVOCAR_VINCULACION")
                ))
                .build();

        String adminToken = jwtTokenProvider.generateAccessToken(adminDetails);
        adminCookie = new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, adminToken);
        adminCookie.setPath("/");

        // 3. Crear Contribuyentes
        contribuyente1 = ContribuyenteTributario.builder()
                .codigoContribuyente("C-" + suffix + "-1")
                .tipoDocumento("DNI")
                .numeroDocumento(personaCiudadano.getNumeroDocumento())
                .tipoContribuyente(TipoContribuyente.PERSONA_NATURAL)
                .nombreRazonSocial("CARLOS QUISPE FLORES")
                .persona(personaCiudadano)
                .estado(EstadoContribuyente.ACTIVO)
                .build();
        contribuyente1 = contribuyenteRepository.save(contribuyente1);

        contribuyente2 = ContribuyenteTributario.builder()
                .codigoContribuyente("C-" + suffix + "-2")
                .tipoDocumento("RUC")
                .numeroDocumento("20" + String.format("%09d", (suffix + 2000) % 1000000000))
                .tipoContribuyente(TipoContribuyente.PERSONA_JURIDICA)
                .nombreRazonSocial("QUINUA ARTESANIAS S.A.C.")
                .estado(EstadoContribuyente.ACTIVO)
                .build();
        contribuyente2 = contribuyenteRepository.save(contribuyente2);

        // 4. Crear Cuentas Corrientes
        CuentaCorrienteTributaria cta1 = CuentaCorrienteTributaria.builder()
                .codigoCuenta("CTA-" + suffix + "-1")
                .contribuyente(contribuyente1)
                .saldoActual(BigDecimal.ZERO)
                .totalCargos(BigDecimal.ZERO)
                .totalAbonos(BigDecimal.ZERO)
                .fechaApertura(LocalDate.now())
                .estado(EstadoCuentaCorriente.ACTIVA)
                .build();
        cuentaCorrienteRepository.save(cta1);

        // 5. Vincular contribuyente 1 (Titular Aprobado)
        vinculacion1 = VinculacionUsuarioContribuyente.builder()
                .usuario(ciudadanoUser)
                .contribuyente(contribuyente1)
                .tipoVinculacion(TipoVinculacionContribuyente.TITULAR_VERIFICADO)
                .estado(EstadoVinculacionContribuyente.APROBADA)
                .activo(true)
                .fechaSolicitud(OffsetDateTime.now())
                .fechaVerificacion(OffsetDateTime.now())
                .build();
        vinculacion1 = vinculacionRepository.save(vinculacion1);

        // 6. Vincular contribuyente 2 (Representante Legal Aprobado para multi-perfil)
        vinculacion2 = VinculacionUsuarioContribuyente.builder()
                .usuario(ciudadanoUser)
                .contribuyente(contribuyente2)
                .tipoVinculacion(TipoVinculacionContribuyente.REPRESENTANTE_ACREDITADO)
                .estado(EstadoVinculacionContribuyente.APROBADA)
                .activo(true)
                .fechaSolicitud(OffsetDateTime.now())
                .fechaVerificacion(OffsetDateTime.now())
                .build();
        vinculacion2 = vinculacionRepository.save(vinculacion2);

        // 7. Predio para contribuyente 1
        PredioTributario predio = PredioTributario.builder()
                .codigoPredio("PRED-" + suffix)
                .tipoPredio(TipoPredio.URBANO)
                .direccion("Jr. Sucre N° 120, Quinua")
                .areaTerreno(new BigDecimal("150.00"))
                .valorAutovaluoReferencial(new BigDecimal("45000.00"))
                .estado(EstadoPredio.ACTIVO)
                .build();
        predio = predioRepository.save(predio);

        RelacionContribuyentePredio rcp = RelacionContribuyentePredio.builder()
                .contribuyente(contribuyente1)
                .predio(predio)
                .tipoRelacion(TipoRelacionContribuyentePredio.PROPIETARIO_UNICO)
                .porcentajeParticipacion(new BigDecimal("100.00"))
                .esPrincipal(true)
                .estado(EstadoRelacionContribuyentePredio.ACTIVO)
                .build();
        relacionPredioRepository.save(rcp);
    }

    @Test
    @DisplayName("Acceso no autenticado al Portal Tributario debe redirigir a Login")
    void testAccesoNoAutenticadoRedirigeLogin() throws Exception {
        mockMvc.perform(get("/portal/tributario/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/login")));
    }

    @Test
    @DisplayName("Ciudadano autenticado accede exitosamente a su Dashboard tributario")
    void testCiudadanoAccedeDashboard() throws Exception {
        mockMvc.perform(get("/portal/tributario/dashboard")
                        .cookie(ciudadanoCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("portal/tributario/dashboard"))
                .andExpect(model().attributeExists("dashboard"))
                .andExpect(model().attributeExists("contribuyenteActivo"));
    }

    @Test
    @DisplayName("Consulta de predios propios del contribuyente activo")
    void testConsultaPrediosCiudadano() throws Exception {
        mockMvc.perform(get("/portal/tributario/predios")
                        .cookie(ciudadanoCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("portal/tributario/predios"))
                .andExpect(model().attributeExists("predios"));
    }

    @Test
    @DisplayName("Consulta de obligaciones tributarias ciudadanas")
    void testConsultaObligacionesCiudadano() throws Exception {
        mockMvc.perform(get("/portal/tributario/obligaciones")
                        .cookie(ciudadanoCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("portal/tributario/obligaciones"))
                .andExpect(model().attributeExists("obligacionesPage"));
    }

    @Test
    @DisplayName("Consulta de estado de cuenta corriente y libro contable")
    void testConsultaCuentaCorriente() throws Exception {
        mockMvc.perform(get("/portal/tributario/cuenta-corriente")
                        .cookie(ciudadanoCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("portal/tributario/cuenta-corriente"))
                .andExpect(model().attributeExists("movimientosPage"));
    }

    @Test
    @DisplayName("Descarga de Estado de Cuenta en PDF genera documento válido")
    void testDescargarEstadoCuentaPdf() throws Exception {
        mockMvc.perform(get("/portal/tributario/cuenta-corriente/pdf")
                        .cookie(ciudadanoCookie))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"estado_cuenta_tributario_quinua.pdf\""));
    }

    @Test
    @DisplayName("Conmutación de perfil activo entre múltiples contribuyentes representados")
    void testConmutacionPerfilActivo() throws Exception {
        mockMvc.perform(post("/portal/tributario/cambiar-contexto")
                        .param("contribuyenteId", contribuyente2.getId().toString())
                        .with(csrf())
                        .cookie(ciudadanoCookie))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/portal/tributario/dashboard"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("Bandeja Administrativa de Calificación de Vinculaciones accesible por operador")
    void testBandejaAdministrativaVinculaciones() throws Exception {
        mockMvc.perform(get("/dashboard/tributaria/portal/vinculaciones")
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tributaria/portal/vinculaciones-admin"))
                .andExpect(model().attributeExists("vinculaciones"));
    }

    @Test
    @DisplayName("Aprobación por operador administrativo de una solicitud de vinculación pendiente")
    void testAprobacionAdministrativaSolicitud() throws Exception {
        // Crear una vinculación pendiente y su solicitud
        VinculacionUsuarioContribuyente vincPend = VinculacionUsuarioContribuyente.builder()
                .usuario(ciudadanoUser)
                .contribuyente(contribuyente1)
                .tipoVinculacion(TipoVinculacionContribuyente.APODERADO_ACREDITADO)
                .estado(EstadoVinculacionContribuyente.PENDIENTE)
                .activo(false)
                .fechaSolicitud(OffsetDateTime.now())
                .build();
        vincPend = vinculacionRepository.save(vincPend);

        SolicitudVinculacionTributaria sol = SolicitudVinculacionTributaria.builder()
                .vinculacion(vincPend)
                .codigoSolicitud("SOL-TEST-" + System.currentTimeMillis())
                .tipoSolicitud("VINCULACION_APODERADO")
                .estado("PENDIENTE")
                .sustento("Poder otorgado por escritura pública")
                .build();
        sol = solicitudRepository.save(sol);

        mockMvc.perform(post("/dashboard/tributaria/portal/vinculaciones/" + vincPend.getId() + "/aprobar")
                        .param("motivo", "Poder verificado en registro municipal.")
                        .with(csrf())
                        .cookie(adminCookie))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard/tributaria/portal/vinculaciones"))
                .andExpect(flash().attributeExists("successMessage"));

        VinculacionUsuarioContribuyente vincActualizada = vinculacionRepository.findById(vincPend.getId()).orElseThrow();
        assertThat(vincActualizada.getEstado()).isEqualTo(EstadoVinculacionContribuyente.APROBADA);
        assertThat(vincActualizada.isActivo()).isTrue();
    }
}
