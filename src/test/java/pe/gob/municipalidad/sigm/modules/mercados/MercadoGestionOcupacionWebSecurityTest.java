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
import pe.gob.municipalidad.sigm.modules.mercados.dto.RenovacionRequest;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.*;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;
import pe.gob.municipalidad.sigm.modules.mercados.service.RenovacionOcupacionService;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MercadoGestionOcupacionWebSecurityTest {

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
    @Autowired private ProcedimientoGestionOcupacionRepository procedimientoRepository;
    @Autowired private SolicitudGestionOcupacionRepository solicitudRepository;
    @Autowired private RenovacionOcupacionService renovacionService;

    private String adminToken;
    private String ciudadanoToken;
    private SolicitudGestionOcupacion solicitud;
    private AsignacionPuesto asignacion;
    private ProcedimientoGestionOcupacion procRenovacion;

    @BeforeEach
    void setUp() {
        long ts = System.currentTimeMillis();

        Rol rolAdmin = rolRepository.findByCodigo("ROLE_ADMIN").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_ADMIN").nombre("Administrador").estado("ACTIVO").build()));

        Rol rolCiudadano = rolRepository.findByCodigo("ROLE_CIUDADANO").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_CIUDADANO").nombre("Ciudadano").estado("ACTIVO").build()));

        Persona personaAdmin = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("48" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Admin")
                .apellidoPaterno("Gestion")
                .correo("admin.gestion" + ts + "@quinua.gob.pe")
                .estado("ACTIVO")
                .build());

        Usuario adminUser = usuarioRepository.save(Usuario.builder()
                .username("admin_gest_" + ts)
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
                        "MERCADOS_RENOVACION_VER",
                        "MERCADOS_RENOVACION_CREAR",
                        "MERCADOS_RENOVACION_EVALUAR",
                        "MERCADOS_RENOVACION_APROBAR",
                        "MERCADOS_CAMBIO_TITULAR_VER",
                        "MERCADOS_CAMBIO_TITULAR_CREAR",
                        "MERCADOS_CAMBIO_TITULAR_EVALUAR",
                        "MERCADOS_CAMBIO_TITULAR_APROBAR",
                        "MERCADOS_TRANSFERENCIA_VER",
                        "MERCADOS_TRANSFERENCIA_CREAR",
                        "MERCADOS_TRANSFERENCIA_EVALUAR",
                        "MERCADOS_TRANSFERENCIA_APROBAR",
                        "MERCADOS_GESTION_OCUPACION_FORMALIZAR",
                        "MERCADOS_GESTION_OCUPACION_HISTORIAL_VER",
                        "MERCADOS_GESTION_OCUPACION_EXPORTAR"
                ))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("MERCADOS_RENOVACION_VER"),
                        new SimpleGrantedAuthority("MERCADOS_RENOVACION_CREAR"),
                        new SimpleGrantedAuthority("MERCADOS_RENOVACION_EVALUAR"),
                        new SimpleGrantedAuthority("MERCADOS_RENOVACION_APROBAR"),
                        new SimpleGrantedAuthority("MERCADOS_CAMBIO_TITULAR_VER"),
                        new SimpleGrantedAuthority("MERCADOS_CAMBIO_TITULAR_CREAR"),
                        new SimpleGrantedAuthority("MERCADOS_CAMBIO_TITULAR_EVALUAR"),
                        new SimpleGrantedAuthority("MERCADOS_CAMBIO_TITULAR_APROBAR"),
                        new SimpleGrantedAuthority("MERCADOS_TRANSFERENCIA_VER"),
                        new SimpleGrantedAuthority("MERCADOS_TRANSFERENCIA_CREAR"),
                        new SimpleGrantedAuthority("MERCADOS_TRANSFERENCIA_EVALUAR"),
                        new SimpleGrantedAuthority("MERCADOS_TRANSFERENCIA_APROBAR"),
                        new SimpleGrantedAuthority("MERCADOS_GESTION_OCUPACION_FORMALIZAR"),
                        new SimpleGrantedAuthority("MERCADOS_GESTION_OCUPACION_HISTORIAL_VER"),
                        new SimpleGrantedAuthority("MERCADOS_GESTION_OCUPACION_EXPORTAR")
                ))
                .build();

        adminToken = jwtTokenProvider.generateAccessToken(adminDetails);

        Persona personaCiudadano = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("71" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Ciudadano")
                .apellidoPaterno("Comun")
                .correo("ciudadano" + ts + "@quinua.gob.pe")
                .estado("ACTIVO")
                .build());

        Usuario ciudadanoUser = usuarioRepository.save(Usuario.builder()
                .username("ciud_" + ts)
                .persona(personaCiudadano)
                .passwordHash(passwordEncoder.encode("Ciud123!"))
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

        // Estructura para la solicitud
        MercadoMunicipal mercado = mercadoRepository.save(MercadoMunicipal.builder()
                .nombre("Mercado Seguridad " + ts)
                .codigo("MKT-SEC-" + (ts % 10000))
                .direccion("Plaza Central")
                .estado(EstadoMercado.ACTIVO)
                .build());

        PuestoComercial puesto = puestoRepository.save(PuestoComercial.builder()
                .mercado(mercado)
                .codigo("PS-" + (ts % 10000))
                .numero("PS-" + (ts % 10000))
                .areaMetrosCuadrados(new BigDecimal("10.00"))
                .estado(EstadoPuesto.OCUPADO)
                .build());

        Comerciante comerciante = comercianteRepository.save(Comerciante.builder()
                .persona(personaAdmin)
                .codigoComerciante("COM-SEC-" + (ts % 10000))
                .tipoComerciante(TipoComerciante.PERSONA_NATURAL)
                .estado(EstadoComerciante.ACTIVO)
                .build());

        asignacion = asignacionRepository.save(AsignacionPuesto.builder()
                .codigoAsignacion("ASG-SEC-" + (ts % 10000))
                .puesto(puesto)
                .comerciante(comerciante)
                .fechaInicio(LocalDate.now().minusMonths(3))
                .fechaFin(LocalDate.now().plusMonths(3))
                .estado(EstadoAsignacion.VIGENTE)
                .build());

        procRenovacion = procedimientoRepository.findByCodigo("PROC-REN-ASG").orElseGet(() ->
                procedimientoRepository.save(ProcedimientoGestionOcupacion.builder()
                        .codigo("PROC-REN-ASG")
                        .nombre("Renovación de Asignación de Puesto")
                        .tipoSolicitud(TipoSolicitudGestion.RENOVACION_ASIGNACION)
                        .baseNormativa("Reglamento de Mercados Art. 25")
                        .activo(true)
                        .permiteTransferencia(false)
                        .build()));

        solicitud = solicitudRepository.save(SolicitudGestionOcupacion.builder()
                .codigoSolicitud("SOL-MER-2026-999999")
                .tipoSolicitud(TipoSolicitudGestion.RENOVACION_ASIGNACION)
                .procedimiento(procRenovacion)
                .mercado(mercado)
                .puesto(puesto)
                .asignacionOrigen(asignacion)
                .comercianteActual(comerciante)
                .fechaPresentacion(LocalDate.now())
                .fechaInicioSolicitada(LocalDate.now().plusMonths(3).plusDays(1))
                .fechaFinSolicitada(LocalDate.now().plusYears(1))
                .motivo("Renovacion formal")
                .estado(EstadoSolicitudGestion.APROBADA)
                .tokenVerificacionQr("TOKEN-SEC-VERIF-123456789")
                .usuarioRegistro("admin")
                .build());
    }

    @Test
    @DisplayName("Endpoint público de verificación QR debe responder 200 sin autenticación previa")
    void testVerificacionPublicaQrSinAutenticacion() throws Exception {
        mockMvc.perform(get("/verificar/gestion-ocupacion/" + solicitud.getTokenVerificacionQr()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Verificación Oficial de Gestión de Ocupación")))
                .andExpect(content().string(containsString("MUNICIPALIDAD DISTRITAL DE QUINUA")))
                .andExpect(content().string(containsString(solicitud.getCodigoSolicitud())));
    }

    @Test
    @DisplayName("Bandeja de renovaciones debe cargar 200 con autoridad MERCADOS_RENOVACION_VER")
    void testBandejaRenovacionesAutorizada() throws Exception {
        mockMvc.perform(get("/dashboard/mercados/renovaciones")
                        .cookie(new Cookie(SecurityConstants.ACCESS_TOKEN_COOKIE_NAME, adminToken)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Renovaciones de Ocupación")));
    }

    @Test
    @DisplayName("Bandeja de renovaciones debe denegar acceso 403 a usuario sin autoridad requerida")
    void testBandejaRenovacionesDenegada() throws Exception {
        mockMvc.perform(get("/dashboard/mercados/renovaciones")
                        .cookie(new Cookie(SecurityConstants.ACCESS_TOKEN_COOKIE_NAME, ciudadanoToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Dashboard consolidado de gestiones debe responder 200 para usuario administrativo")
    void testDashboardConsolidado() throws Exception {
        mockMvc.perform(get("/dashboard/mercados/gestiones-ocupacion/dashboard")
                        .cookie(new Cookie(SecurityConstants.ACCESS_TOKEN_COOKIE_NAME, adminToken)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Panel de Gestiones de Ocupación")));
    }

    @Test
    @DisplayName("Exportación de reportes Excel debe emitir archivo binario para usuario con permiso")
    void testDescargaReporteExcel() throws Exception {
        mockMvc.perform(get("/dashboard/mercados/gestiones-ocupacion/reportes/excel")
                        .cookie(new Cookie(SecurityConstants.ACCESS_TOKEN_COOKIE_NAME, adminToken)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("reporte_gestiones_ocupacion.xlsx")));
    }

    @Test
    @DisplayName("Exportación de reportes PDF debe emitir documento binario con cabeceras correctas")
    void testDescargaReportePdf() throws Exception {
        mockMvc.perform(get("/dashboard/mercados/gestiones-ocupacion/reportes/pdf")
                        .cookie(new Cookie(SecurityConstants.ACCESS_TOKEN_COOKIE_NAME, adminToken)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }
}
