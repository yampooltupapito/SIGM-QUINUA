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
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Rol;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.common.repository.RolRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.*;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;
import pe.gob.municipalidad.sigm.modules.mercados.service.ObligacionMercadoService;
import pe.gob.municipalidad.sigm.modules.mercados.service.PagoMercadoService;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

import java.math.BigDecimal;
import java.time.LocalDate;
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
class MercadoFinanzasWebSecurityTest {

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
    private MercadoMunicipalRepository mercadoRepository;

    @Autowired
    private PuestoComercialRepository puestoRepository;

    @Autowired
    private ComercianteRepository comercianteRepository;

    @Autowired
    private ConceptoCobroMercadoRepository conceptoRepository;

    @Autowired
    private ObligacionMercadoService obligacionService;

    @Autowired
    private PagoMercadoService pagoService;

    private String adminToken;
    private String ciudadanoToken;
    private Comerciante comerciante;
    private ConceptoCobroMercado concepto;
    private PuestoComercial puesto;

    @BeforeEach
    void setUp() {
        long ts = System.currentTimeMillis();

        Rol rolAdmin = rolRepository.findByCodigo("ROLE_ADMIN").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_ADMIN").nombre("Administrador").estado("ACTIVO").build()));

        Rol rolCiudadano = rolRepository.findByCodigo("ROLE_CIUDADANO").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_CIUDADANO").nombre("Ciudadano").estado("ACTIVO").build()));

        Persona personaAdmin = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("45" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Admin")
                .apellidoPaterno("Finanzas")
                .correo("admin.fin" + ts + "@quinua.gob.pe")
                .estado("ACTIVO")
                .build());

        Usuario adminUser = usuarioRepository.save(Usuario.builder()
                .username("admin_fin_" + ts)
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
                        "MERCADOS_FINANZAS_VER",
                        "MERCADOS_CONCEPTO_GESTIONAR",
                        "MERCADOS_TARIFA_GESTIONAR",
                        "MERCADOS_OBLIGACION_VER",
                        "MERCADOS_OBLIGACION_CREAR",
                        "MERCADOS_OBLIGACION_AJUSTAR",
                        "MERCADOS_PAGO_VER",
                        "MERCADOS_PAGO_REGISTRAR",
                        "MERCADOS_PAGO_REVERSAR",
                        "MERCADOS_CAJA_ABRIR",
                        "MERCADOS_CAJA_CERRAR",
                        "MERCADOS_CONCILIACION_GESTIONAR",
                        "MERCADOS_FINANZAS_EXPORTAR",
                        "MERCADOS_FINANZAS_AUDITORIA_VER"
                ))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("MERCADOS_FINANZAS_VER"),
                        new SimpleGrantedAuthority("MERCADOS_CONCEPTO_GESTIONAR"),
                        new SimpleGrantedAuthority("MERCADOS_TARIFA_GESTIONAR"),
                        new SimpleGrantedAuthority("MERCADOS_OBLIGACION_VER"),
                        new SimpleGrantedAuthority("MERCADOS_OBLIGACION_CREAR"),
                        new SimpleGrantedAuthority("MERCADOS_OBLIGACION_AJUSTAR"),
                        new SimpleGrantedAuthority("MERCADOS_PAGO_VER"),
                        new SimpleGrantedAuthority("MERCADOS_PAGO_REGISTRAR"),
                        new SimpleGrantedAuthority("MERCADOS_PAGO_REVERSAR"),
                        new SimpleGrantedAuthority("MERCADOS_CAJA_ABRIR"),
                        new SimpleGrantedAuthority("MERCADOS_CAJA_CERRAR"),
                        new SimpleGrantedAuthority("MERCADOS_CONCILIACION_GESTIONAR"),
                        new SimpleGrantedAuthority("MERCADOS_FINANZAS_EXPORTAR"),
                        new SimpleGrantedAuthority("MERCADOS_FINANZAS_AUDITORIA_VER")
                ))
                .build();

        adminToken = jwtTokenProvider.generateAccessToken(adminDetails);

        Persona personaCiudadano = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("46" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Ciudadano")
                .apellidoPaterno("SinPermiso")
                .correo("ciudadano" + ts + "@quinua.gob.pe")
                .estado("ACTIVO")
                .build());

        Usuario ciudadanoUser = usuarioRepository.save(Usuario.builder()
                .username("ciudadano_" + ts)
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
                .permisos(Set.of("ROLE_CIUDADANO"))
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_CIUDADANO")))
                .build();

        ciudadanoToken = jwtTokenProvider.generateAccessToken(ciudadanoDetails);

        MercadoMunicipal mercado = mercadoRepository.save(MercadoMunicipal.builder()
                .codigo("MER-SEC-" + (ts % 10000))
                .nombre("Mercado Test Seguridad")
                .direccion("Jr. Sucre 456, Quinua")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .estado(EstadoMercado.ACTIVO)
                .build());

        puesto = puestoRepository.save(PuestoComercial.builder()
                .mercado(mercado)
                .codigo("PUE-S-" + (ts % 10000))
                .numero("S-01")
                .areaMetrosCuadrados(new BigDecimal("10.00"))
                .estado(EstadoPuesto.OCUPADO)
                .build());

        comerciante = comercianteRepository.save(Comerciante.builder()
                .persona(personaAdmin)
                .tipoComerciante(TipoComerciante.PERSONA_NATURAL)
                .codigoComerciante("COM-S-" + (ts % 10000))
                .estado(EstadoComerciante.ACTIVO)
                .fechaRegistro(LocalDate.now())
                .build());

        concepto = conceptoRepository.save(ConceptoCobroMercado.builder()
                .codigo("CONC_SEC_" + (ts % 10000))
                .nombre("Concepto Seguro")
                .tipoConcepto(TipoConceptoCobro.USO_PUESTO_COMERCIAL)
                .periodicidad(PeriodicidadConcepto.MENSUAL)
                .estado("ACTIVO")
                .requiereAsignacion(false)
                .build());
    }

    @Test
    @DisplayName("01. Renderizar Thymeleaf dashboard financiero para usuario autorizado")
    void test01_dashboardFinanciero_autorizado() throws Exception {
        mockMvc.perform(get("/dashboard/mercados/finanzas")
                        .cookie(new Cookie(SecurityConstants.ACCESS_TOKEN_COOKIE_NAME, adminToken)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Finanzas y Recaudación de Mercados")));
    }

    @Test
    @DisplayName("02. Renderizar listado de obligaciones comerciales")
    void test02_obligacionesListado_autorizado() throws Exception {
        mockMvc.perform(get("/dashboard/mercados/obligaciones")
                        .cookie(new Cookie(SecurityConstants.ACCESS_TOKEN_COOKIE_NAME, adminToken)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Obligaciones Comerciales")));
    }

    @Test
    @DisplayName("03. Renderizar listado de pagos de mercado")
    void test03_pagosListado_autorizado() throws Exception {
        mockMvc.perform(get("/dashboard/mercados/pagos")
                        .cookie(new Cookie(SecurityConstants.ACCESS_TOKEN_COOKIE_NAME, adminToken)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Pagos y Recaudaciones")));
    }

    @Test
    @DisplayName("04. Renderizar panel de sesiones de caja")
    void test04_cajaListado_autorizado() throws Exception {
        mockMvc.perform(get("/dashboard/mercados/caja")
                        .cookie(new Cookie(SecurityConstants.ACCESS_TOKEN_COOKIE_NAME, adminToken)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Control de Caja y Arqueo")));
    }

    @Test
    @DisplayName("05. API conceptos de cobro accesible para usuario con MERCADOS_FINANZAS_VER")
    void test05_apiConceptos_autorizado() throws Exception {
        mockMvc.perform(get("/api/mercados/conceptos-cobro")
                        .header("Authorization", "Bearer " + adminToken)
                        .cookie(new Cookie(SecurityConstants.ACCESS_TOKEN_COOKIE_NAME, adminToken)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("06. Denegar creación de concepto a usuario sin permiso (403 Forbidden)")
    void test06_crearConcepto_denegadoParaCiudadano() throws Exception {
        ConceptoCobroRequest req = ConceptoCobroRequest.builder()
                .codigo("CONC_NO_AUTH")
                .nombre("Intento No Autorizado")
                .tipoConcepto(TipoConceptoCobro.SERVICIO_MERCADO)
                .periodicidad(PeriodicidadConcepto.MENSUAL)
                .build();

        mockMvc.perform(post("/api/mercados/conceptos-cobro")
                        .with(csrf())
                        .header("Authorization", "Bearer " + ciudadanoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("07. Denegar creación de pago a usuario sin permiso (403 Forbidden)")
    void test07_crearPago_denegadoParaCiudadano() throws Exception {
        PagoCreateRequest req = PagoCreateRequest.builder()
                .comercianteId(comerciante.getId())
                .montoRecibido(new BigDecimal("100.00"))
                .medioPago(MedioPagoMercado.EFECTIVO)
                .aplicaciones(List.of(
                        AplicacionItemRequest.builder().obligacionId(UUID.randomUUID()).monto(new BigDecimal("100.00")).build()
                ))
                .build();

        mockMvc.perform(post("/api/mercados/pagos")
                        .with(csrf())
                        .header("Authorization", "Bearer " + ciudadanoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("08. Rechazar POST en API sin token CSRF cuando se accede vía navegador/cookies")
    void test08_csrfProtection_sinTokenCsrf() throws Exception {
        ConceptoCobroRequest req = ConceptoCobroRequest.builder()
                .codigo("CONC_CSRF_TEST")
                .nombre("Test CSRF")
                .tipoConcepto(TipoConceptoCobro.SERVICIO_MERCADO)
                .periodicidad(PeriodicidadConcepto.MENSUAL)
                .build();

        // Sin .with(csrf()) en request que usa cookies de sesión -> Spring Security CSRF Filter rechaza
        mockMvc.perform(post("/api/mercados/conceptos-cobro")
                        .cookie(new Cookie(SecurityConstants.ACCESS_TOKEN_COOKIE_NAME, adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }
}
