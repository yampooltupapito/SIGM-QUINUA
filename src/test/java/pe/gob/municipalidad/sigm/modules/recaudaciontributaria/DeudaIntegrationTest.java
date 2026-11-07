package pe.gob.municipalidad.sigm.modules.recaudaciontributaria;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pe.gob.municipalidad.sigm.common.constants.SecurityConstants;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.deuda.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ConceptoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ContribuyenteTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ObligacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.PeriodoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ConceptoTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ContribuyenteTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ObligacionTributariaRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.PeriodoTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.DeudaTributariaService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.InteresMoratorioService;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class DeudaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DeudaTributariaService deudaService;

    @Autowired
    private InteresMoratorioService interesService;

    @Autowired
    private ObligacionTributariaRepository obligacionRepository;

    @Autowired
    private ContribuyenteTributarioRepository contribuyenteRepository;

    @Autowired
    private ConceptoTributarioRepository conceptoRepository;

    @Autowired
    private PeriodoTributarioRepository periodoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Usuario testUser;
    private Cookie authCookie;
    private ContribuyenteTributario testContribuyente;
    private ObligacionTributaria testObligacion;

    @BeforeEach
    void setUp() {
        long suffix = System.currentTimeMillis() % 100000000;

        Persona p = Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento(String.format("%08d", (suffix + 1234) % 100000000))
                .nombres("Gestor")
                .apellidoPaterno("Tributario")
                .apellidoMaterno("Quinua")
                .build();
        p = personaRepository.save(p);

        testUser = Usuario.builder()
                .username("gestor_deudas_" + suffix)
                .passwordHash("$2a$10$abcdefghijklmnopqrstuvwxyz123456")
                .persona(p)
                .enabled(true)
                .build();
        testUser = usuarioRepository.save(testUser);

        CustomUserDetails userDetails = CustomUserDetails.builder()
                .id(testUser.getId())
                .username(testUser.getUsername())
                .password("dummy")
                .enabled(true)
                .accountNonLocked(true)
                .roles(java.util.Set.of("ROLE_ADMIN", "ROLE_TRIBUTACION"))
                .permisos(java.util.Set.of(
                        "TRIBUTARIA_DEUDAS_VER",
                        "TRIBUTARIA_DEUDAS_VER_DETALLE",
                        "TRIBUTARIA_DEUDAS_CLASIFICAR",
                        "TRIBUTARIA_DEUDAS_ASIGNAR",
                        "TRIBUTARIA_DEUDAS_REGISTRAR_GESTION",
                        "TRIBUTARIA_DEUDAS_REGISTRAR_COMPROMISO",
                        "TRIBUTARIA_DEUDAS_CALCULAR_INTERESES",
                        "TRIBUTARIA_DEUDAS_REVISAR_INTERESES"
                ))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("ROLE_TRIBUTACION"),
                        new SimpleGrantedAuthority("TRIBUTARIA_DEUDAS_VER"),
                        new SimpleGrantedAuthority("TRIBUTARIA_DEUDAS_VER_DETALLE"),
                        new SimpleGrantedAuthority("TRIBUTARIA_DEUDAS_CLASIFICAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_DEUDAS_ASIGNAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_DEUDAS_REGISTRAR_GESTION"),
                        new SimpleGrantedAuthority("TRIBUTARIA_DEUDAS_REGISTRAR_COMPROMISO"),
                        new SimpleGrantedAuthority("TRIBUTARIA_DEUDAS_CALCULAR_INTERESES"),
                        new SimpleGrantedAuthority("TRIBUTARIA_DEUDAS_REVISAR_INTERESES")
                ))
                .build();

        String jwt = jwtTokenProvider.generateAccessToken(userDetails);
        authCookie = new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, jwt);
        authCookie.setPath("/");

        testContribuyente = ContribuyenteTributario.builder()
                .codigoContribuyente("CTR-D-" + suffix)
                .tipoContribuyente(TipoContribuyente.PERSONA_NATURAL)
                .tipoDocumento("DNI")
                .numeroDocumento(String.format("%08d", (suffix + 7777) % 100000000))
                .nombreRazonSocial("Contribuyente Deudas Test")
                .domicilioFiscal("Plaza Principal de Quinua")
                .estado(EstadoContribuyente.ACTIVO)
                .build();
        testContribuyente = contribuyenteRepository.save(testContribuyente);

        ConceptoTributario concepto = conceptoRepository.findAll().stream().findFirst().orElse(null);
        PeriodoTributario periodo = periodoRepository.findAll().stream().findFirst().orElse(null);

        testObligacion = ObligacionTributaria.builder()
                .codigoObligacion("OBL-D-" + suffix)
                .contribuyente(testContribuyente)
                .concepto(concepto)
                .periodo(periodo)
                .importeDeterminado(new BigDecimal("180.00"))
                .importePagado(new BigDecimal("30.00"))
                .saldo(new BigDecimal("150.00"))
                .moneda("PEN")
                .fechaDeterminacion(LocalDate.of(2026, 1, 1))
                .fechaVencimiento(LocalDate.of(2026, 2, 28))
                .estado(EstadoObligacionTributaria.VIGENTE)
                .estadoFinanciero(EstadoFinancieroObligacion.PARCIALMENTE_CUBIERTA)
                .estadoExigibilidad(EstadoExigibilidadObligacion.EXIGIBLE)
                .cuotaNumero(1)
                .totalCuotas(4)
                .fechaRegistro(OffsetDateTime.now())
                .build();
        testObligacion = obligacionRepository.save(testObligacion);
    }

    @Test
    @DisplayName("Flujo Completo de Cartera: Consulta de saldos, reclasificación, asignación, gestiones y compromisos")
    void testFlujoCompletoGestionDeudas() {
        // 1. Obtener Ficha de Deuda
        LocalDate corte = LocalDate.of(2026, 3, 30); // 30 días después del vencimiento
        DeudaDetalleResponse ficha = deudaService.obtenerFichaDeuda(testObligacion.getId(), corte);

        assertThat(ficha).isNotNull();
        assertThat(ficha.getSaldo()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(ficha.getDiasMorosidad()).isEqualTo(30);
        assertThat(ficha.getTramoAntiguedad()).isEqualTo(TramoAntiguedadDeuda.DE_01_A_30_DIAS);
        assertThat(ficha.getClasificacion()).isEqualTo(ClasificacionDeudaTributaria.VENCIDA_EXIGIBLE);

        // 2. Reclasificar deuda a OBSERVADA
        ClasificarDeudaRequest clasifReq = ClasificarDeudaRequest.builder()
                .clasificacion(ClasificacionDeudaTributaria.OBSERVADA)
                .observaciones("En revisión de documentación de predio")
                .build();

        DeudaDetalleResponse fichaReclasificada = deudaService.clasificarObligacion(
                testObligacion.getId(), clasifReq, testUser.getUsername());

        assertThat(fichaReclasificada.getClasificacion()).isEqualTo(ClasificacionDeudaTributaria.OBSERVADA);

        // 3. Asignar responsable
        AsignarResponsableRequest asignReq = AsignarResponsableRequest.builder()
                .responsableId(testUser.getId())
                .observaciones("Asignado a gestor para contacto telefónico")
                .build();

        DeudaDetalleResponse fichaAsignada = deudaService.asignarResponsable(
                testObligacion.getId(), asignReq, testUser.getUsername());

        assertThat(fichaAsignada.getResponsableNombre()).isEqualTo(testUser.getNombreCompleto());
        assertThat(fichaAsignada.getEstadoSeguimiento()).isEqualTo(EstadoSeguimientoDeuda.ASIGNADO);

        // 4. Registrar Gestión Administrativa
        RegistrarGestionDeudaRequest gestReq = RegistrarGestionDeudaRequest.builder()
                .tipoGestion(TipoGestionDeuda.CONTACTO_ADMINISTRATIVO)
                .resultado("Se comunicó telefónicamente con contribuyente")
                .observaciones("Contribuyente manifiesta que cancelará en fecha próxima")
                .build();

        DeudaDetalleResponse fichaGestion = deudaService.registrarGestion(
                testObligacion.getId(), gestReq, testUser.getUsername());

        assertThat(fichaGestion.getGestiones()).isNotEmpty();

        // 5. Registrar Compromiso Informativo
        RegistrarCompromisoDeudaRequest compReq = RegistrarCompromisoDeudaRequest.builder()
                .fechaComprometida(LocalDate.now().plusDays(7))
                .montoComprometido(new BigDecimal("150.00"))
                .observaciones("Pago en caja municipal")
                .build();

        DeudaDetalleResponse fichaCompromiso = deudaService.registrarCompromiso(
                testObligacion.getId(), compReq, testUser.getUsername());

        assertThat(fichaCompromiso.getCompromisos()).isNotEmpty();
        assertThat(fichaCompromiso.getEstadoSeguimiento()).isEqualTo(EstadoSeguimientoDeuda.COMPROMISO_PENDIENTE);
    }

    @Test
    @DisplayName("Navegación MVC: Dashboard, Cartera, Detalle y Simulador TIM retornan 200 OK")
    void testNavegacionMvcDeudas() throws Exception {
        // 1. Dashboard
        mockMvc.perform(get("/dashboard/tributaria/deudas").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("dashboard", "fechaCorte"));

        // 2. Cartera
        mockMvc.perform(get("/dashboard/tributaria/deudas/cartera").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("cartera", "tramos", "conceptos"));

        // 3. Ficha Detalle
        mockMvc.perform(get("/dashboard/tributaria/deudas/" + testObligacion.getId()).cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("ficha", "clasificaciones", "tiposGestion"));

        // 4. Pantalla Intereses
        mockMvc.perform(get("/dashboard/tributaria/deudas/intereses").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("parametros", "conceptos"));
    }
}
