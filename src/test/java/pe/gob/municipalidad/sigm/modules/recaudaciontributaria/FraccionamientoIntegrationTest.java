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
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.fraccionamiento.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.*;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class FraccionamientoIntegrationTest {

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
    private ObligacionTributariaRepository obligacionRepository;

    @Autowired
    private ConceptoTributarioRepository conceptoRepository;

    @Autowired
    private PeriodoTributarioRepository periodoRepository;

    @Autowired
    private ProgramaFraccionamientoTributarioRepository programaRepository;

    @Autowired
    private SolicitudFraccionamientoTributarioRepository solicitudRepository;

    @Autowired
    private ConvenioFraccionamientoTributarioRepository convenioRepository;

    @Autowired
    private CuotaFraccionamientoTributarioRepository cuotaRepository;

    @Autowired
    private SolicitudFraccionamientoService solicitudService;

    @Autowired
    private ConvenioFraccionamientoService convenioService;

    @Autowired
    private AplicacionPagoFraccionamientoService aplicacionPagoService;

    private Usuario testUser;
    private Cookie authCookie;
    private ContribuyenteTributario contribuyente;
    private ProgramaFraccionamientoTributario programa;
    private ObligacionTributaria obligacion;

    @BeforeEach
    void setUp() {
        long suffix = System.currentTimeMillis() % 100000000;

        Persona p = Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento(String.format("%08d", (suffix + 4321) % 100000000))
                .nombres("Especialista")
                .apellidoPaterno("Fraccionamientos")
                .apellidoMaterno("Quinua")
                .build();
        p = personaRepository.save(p);

        testUser = Usuario.builder()
                .username("fraccionador_" + suffix)
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
                        "TRIBUTARIA_FRACCIONAMIENTOS_VER",
                        "TRIBUTARIA_FRACCIONAMIENTOS_CONFIGURAR",
                        "TRIBUTARIA_FRACCIONAMIENTOS_SOLICITAR",
                        "TRIBUTARIA_FRACCIONAMIENTOS_EVALUAR",
                        "TRIBUTARIA_FRACCIONAMIENTOS_OBSERVAR",
                        "TRIBUTARIA_FRACCIONAMIENTOS_SUBSANAR",
                        "TRIBUTARIA_FRACCIONAMIENTOS_SIMULAR",
                        "TRIBUTARIA_FRACCIONAMIENTOS_APROBAR",
                        "TRIBUTARIA_FRACCIONAMIENTOS_RECHAZAR",
                        "TRIBUTARIA_FRACCIONAMIENTOS_GESTIONAR_CONVENIO",
                        "TRIBUTARIA_FRACCIONAMIENTOS_REPROGRAMAR",
                        "TRIBUTARIA_FRACCIONAMIENTOS_REVISAR_INCUMPLIMIENTO",
                        "TRIBUTARIA_FRACCIONAMIENTOS_VER_HISTORIAL",
                        "TRIBUTARIA_FRACCIONAMIENTOS_EXPORTAR",
                        "TRIBUTARIA_FRACCIONAMIENTOS_REPORTES"
                ))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("ROLE_TRIBUTACION"),
                        new SimpleGrantedAuthority("TRIBUTARIA_FRACCIONAMIENTOS_VER"),
                        new SimpleGrantedAuthority("TRIBUTARIA_FRACCIONAMIENTOS_CONFIGURAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_FRACCIONAMIENTOS_SOLICITAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_FRACCIONAMIENTOS_EVALUAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_FRACCIONAMIENTOS_OBSERVAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_FRACCIONAMIENTOS_SUBSANAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_FRACCIONAMIENTOS_SIMULAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_FRACCIONAMIENTOS_APROBAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_FRACCIONAMIENTOS_RECHAZAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_FRACCIONAMIENTOS_GESTIONAR_CONVENIO"),
                        new SimpleGrantedAuthority("TRIBUTARIA_FRACCIONAMIENTOS_REPROGRAMAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_FRACCIONAMIENTOS_REVISAR_INCUMPLIMIENTO"),
                        new SimpleGrantedAuthority("TRIBUTARIA_FRACCIONAMIENTOS_VER_HISTORIAL"),
                        new SimpleGrantedAuthority("TRIBUTARIA_FRACCIONAMIENTOS_EXPORTAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_FRACCIONAMIENTOS_REPORTES")
                ))
                .build();

        String jwt = jwtTokenProvider.generateAccessToken(userDetails);
        authCookie = new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, jwt);
        authCookie.setPath("/");

        contribuyente = ContribuyenteTributario.builder()
                .codigoContribuyente("CONT-TEST-" + suffix)
                .tipoContribuyente(TipoContribuyente.PERSONA_NATURAL)
                .tipoDocumento("DNI")
                .numeroDocumento(String.format("%08d", (suffix + 8888) % 100000000))
                .nombres("Mario")
                .primerApellido("Condori")
                .nombreRazonSocial("Mario Condori")
                .domicilioFiscal("Jr. Sucre 456, Quinua")
                .estado(EstadoContribuyente.ACTIVO)
                .build();
        contribuyente = contribuyenteRepository.save(contribuyente);

        // Buscar programa activo o crear uno
        programa = programaRepository.findAll().stream()
                .filter(p1 -> p1.getEstado() == EstadoProgramaFraccionamiento.ACTIVO)
                .findFirst()
                .orElseGet(() -> {
                    ProgramaFraccionamientoTributario p1 = ProgramaFraccionamientoTributario.builder()
                            .codigo("PROG-TEST-" + System.currentTimeMillis())
                            .nombre("Programa Test 2026")
                            .estado(EstadoProgramaFraccionamiento.ACTIVO)
                            .fechaInicioVigencia(LocalDate.of(2026, 1, 1))
                            .build();
                    return programaRepository.save(p1);
                });

        ConceptoTributario concepto = conceptoRepository.findAll().stream().findFirst().orElse(null);
        PeriodoTributario periodo = periodoRepository.findAll().stream().findFirst().orElse(null);

        obligacion = ObligacionTributaria.builder()
                .codigoObligacion("OBL-TEST-" + suffix)
                .contribuyente(contribuyente)
                .concepto(concepto)
                .periodo(periodo)
                .estado(EstadoObligacionTributaria.VIGENTE)
                .tipoDeterminacion(TipoDeterminacionTributaria.IMPUESTO_PREDIAL)
                .importeDeterminado(new BigDecimal("900.00"))
                .saldo(new BigDecimal("900.00"))
                .cuotaNumero(1)
                .fechaVencimiento(LocalDate.of(2026, 2, 28))
                .build();
        obligacion = obligacionRepository.save(obligacion);
    }

    @Test
    @DisplayName("GET /dashboard/tributaria/fraccionamientos - Visualizar dashboard con autenticación")
    void testVerDashboard() throws Exception {
        mockMvc.perform(get("/dashboard/tributaria/fraccionamientos")
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tributaria/fraccionamientos/dashboard"))
                .andExpect(model().attributeExists("dashboard"));
    }

    @Test
    @DisplayName("GET /dashboard/tributaria/fraccionamientos/solicitudes - Listado de solicitudes")
    void testListarSolicitudes() throws Exception {
        mockMvc.perform(get("/dashboard/tributaria/fraccionamientos/solicitudes")
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tributaria/fraccionamientos/solicitudes_list"))
                .andExpect(model().attributeExists("solicitudes"));
    }

    @Test
    @DisplayName("GET /dashboard/tributaria/fraccionamientos/solicitudes/nueva - Formulario de registro")
    void testNuevaSolicitudForm() throws Exception {
        mockMvc.perform(get("/dashboard/tributaria/fraccionamientos/solicitudes/nueva")
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tributaria/fraccionamientos/solicitud_nueva"))
                .andExpect(model().attributeExists("programas", "formSolicitud"));
    }

    @Test
    @DisplayName("GET /dashboard/tributaria/fraccionamientos/convenios - Listado de convenios")
    void testListarConvenios() throws Exception {
        mockMvc.perform(get("/dashboard/tributaria/fraccionamientos/convenios")
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tributaria/fraccionamientos/convenios_list"))
                .andExpect(model().attributeExists("convenios"));
    }

    @Test
    @DisplayName("GET /dashboard/tributaria/fraccionamientos/simulador - Vista del simulador")
    void testSimuladorView() throws Exception {
        mockMvc.perform(get("/dashboard/tributaria/fraccionamientos/simulador")
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tributaria/fraccionamientos/simulador"))
                .andExpect(model().attributeExists("programas"));
    }

    @Test
    @Transactional
    @DisplayName("Flujo Completo: Solicitud -> Evaluación -> Aprobación de Convenio -> Cuotas -> PDF")
    void testFlujoCompletoFraccionamiento() throws Exception {
        Usuario authUser = testUser;

        // 1. Crear Solicitud
        SolicitudFraccionamientoRequest req = SolicitudFraccionamientoRequest.builder()
                .contribuyenteId(contribuyente.getId())
                .programaId(programa.getId())
                .motivo("Disminución de ingresos agropecuarios")
                .numeroCuotasPropuestas(4)
                .montoCuotaInicialPropuesta(new BigDecimal("90.00"))
                .obligacionIds(List.of(obligacion.getId()))
                .build();

        SolicitudFraccionamientoResponse solResp = solicitudService.crearSolicitud(req, authUser);
        assertThat(solResp).isNotNull();
        assertThat(solResp.getId()).isNotNull();
        assertThat(solResp.getEstado()).isEqualTo(EstadoSolicitudFraccionamiento.PRESENTADA);
        assertThat(solResp.getCronogramaVigente()).isNotNull();
        assertThat(solResp.getCronogramaVigente().getCuotas()).hasSize(4);

        // 2. Evaluar Solicitud
        EvaluarSolicitudRequest evalReq = EvaluarSolicitudRequest.builder()
                .conforme(true)
                .observaciones("Requisitos documentales validados conforme a normativa")
                .build();
        SolicitudFraccionamientoResponse evalResp = solicitudService.evaluarSolicitud(solResp.getId(), evalReq, authUser);
        assertThat(evalResp.getEstado()).isEqualTo(EstadoSolicitudFraccionamiento.EN_EVALUACION);

        // 3. Aprobar Convenio
        AprobarConvenioRequest aprobReq = AprobarConvenioRequest.builder()
                .resolucionAprobacion("RES-GER-TRIB-2026-" + System.currentTimeMillis() % 10000)
                .fechaInicioVigencia(LocalDate.now())
                .observaciones("Convenio aprobado por la Gerencia de Rentas")
                .build();
        ConvenioFraccionamientoResponse convResp = convenioService.aprobarConvenio(solResp.getId(), aprobReq, authUser);
        assertThat(convResp).isNotNull();
        assertThat(convResp.getCodigoConvenio()).startsWith("CONV-2026-");
        assertThat(convResp.getEstado()).isEqualTo(EstadoConvenioFraccionamiento.VIGENTE);

        // 4. Verificar que la obligación original pasó a FRACCIONADA
        ObligacionTributaria oblActualizada = obligacionRepository.findById(obligacion.getId()).orElseThrow();
        assertThat(oblActualizada.getEstado()).isEqualTo(EstadoObligacionTributaria.FRACCIONADA);

        // 5. Descargar PDF del convenio formal
        mockMvc.perform(get("/dashboard/tributaria/fraccionamientos/convenios/" + convResp.getId() + "/pdf")
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("CONV-2026-")));

        // 6. Aplicar pago a la primera cuota
        List<CuotaFraccionamientoTributario> cuotas = cuotaRepository.findByCronogramaIdOrderByNumeroCuotaAsc(convResp.getCronograma().getId());
        assertThat(cuotas).isNotEmpty();
        CuotaFraccionamientoTributario cuota1 = cuotas.get(0);

        aplicacionPagoService.aplicarPagoACuota(cuota1.getId(), cuota1.getImporteTotal(), authUser);

        CuotaFraccionamientoTributario cuota1Pagada = cuotaRepository.findById(cuota1.getId()).orElseThrow();
        assertThat(cuota1Pagada.getEstado()).isEqualTo(EstadoCuotaFraccionamiento.PAGADA);
        assertThat(cuota1Pagada.getSaldo()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Seguridad: Intento de acceso sin autenticación es rechazado")
    void testAccesoSinAutenticacion() throws Exception {
        mockMvc.perform(get("/dashboard/tributaria/fraccionamientos"))
                .andExpect(status().is3xxRedirection());
    }
}
