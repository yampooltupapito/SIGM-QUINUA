package pe.gob.municipalidad.sigm.modules.mercados;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Rol;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.common.repository.RolRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.mercados.dto.PortalComercianteContactoUpdateRequest;
import pe.gob.municipalidad.sigm.modules.mercados.dto.PortalComercianteRenovacionRequest;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.*;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MercadoPortalComercianteWebSecurityTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtTokenProvider jwtTokenProvider;

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PersonaRepository personaRepository;
    @Autowired private RolRepository rolRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Autowired private ComercianteRepository comercianteRepository;
    @Autowired private VinculoUsuarioComercianteRepository vinculoRepository;
    @Autowired private MercadoMunicipalRepository mercadoRepository;
    @Autowired private PuestoComercialRepository puestoRepository;
    @Autowired private AsignacionPuestoRepository asignacionRepository;
    @Autowired private ContratoOcupacionRepository contratoRepository;
    @Autowired private ObligacionMercadoRepository obligacionRepository;
    @Autowired private ConceptoCobroMercadoRepository conceptoRepository;

    private String tokenComercianteA;
    private String tokenComercianteB;
    private Usuario usuarioA;
    private Usuario usuarioB;
    private Comerciante comercianteA;
    private Comerciante comercianteB;
    private PuestoComercial puestoA;
    private PuestoComercial puestoB;
    private AsignacionPuesto asignacionA;
    private ContratoOcupacion contratoB;
    private ObligacionMercado obligacionB;

    @BeforeEach
    void setUp() {
        long ts = System.currentTimeMillis();

        Rol rolComerciante = rolRepository.findByCodigo("ROLE_COMERCIANTE").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_COMERCIANTE").nombre("Comerciante").estado("ACTIVO").build()));

        // Usuario y Comerciante A
        Persona personaA = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento(String.valueOf(ts % 100000000))
                .nombres("Carlos")
                .apellidoPaterno("Huamán")
                .apellidoMaterno("Paucar")
                .correo("carlos" + ts + "@test.gob.pe")
                .telefono("966111222")
                .direccion("Plaza de Armas s/n")
                .estado("ACTIVO")
                .build());

        usuarioA = usuarioRepository.save(Usuario.builder()
                .username("carloshuaman_" + ts)
                .passwordHash(passwordEncoder.encode("Password123!"))
                .persona(personaA)
                .roles(Set.of(rolComerciante))
                .enabled(true)
                .accountLocked(false)
                .build());

        comercianteA = comercianteRepository.save(Comerciante.builder()
                .persona(personaA)
                .usuario(usuarioA)
                .codigoComerciante("COM-A-" + (ts % 10000))
                .tipoComerciante(TipoComerciante.PERSONA_NATURAL)
                .estado(EstadoComerciante.ACTIVO)
                .build());

        vinculoRepository.save(VinculoUsuarioComerciante.builder()
                .usuario(usuarioA)
                .comerciante(comercianteA)
                .tipoVinculo(TipoVinculoComerciante.TITULAR)
                .estado(EstadoVinculoComerciante.VERIFICADO)
                .fechaInicio(LocalDate.now().minusDays(10))
                .fechaVerificacion(OffsetDateTime.now())
                .verificadoPor(usuarioA)
                .build());

        // Usuario y Comerciante B
        Persona personaB = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento(String.valueOf((ts + 1) % 100000000))
                .nombres("Elena")
                .apellidoPaterno("Meza")
                .apellidoMaterno("Rojas")
                .correo("elena" + ts + "@test.gob.pe")
                .telefono("966333444")
                .estado("ACTIVO")
                .build());

        usuarioB = usuarioRepository.save(Usuario.builder()
                .username("elenameza_" + ts)
                .passwordHash(passwordEncoder.encode("Password123!"))
                .persona(personaB)
                .roles(Set.of(rolComerciante))
                .enabled(true)
                .accountLocked(false)
                .build());

        comercianteB = comercianteRepository.save(Comerciante.builder()
                .persona(personaB)
                .usuario(usuarioB)
                .codigoComerciante("COM-B-" + (ts % 10000))
                .tipoComerciante(TipoComerciante.PERSONA_NATURAL)
                .estado(EstadoComerciante.ACTIVO)
                .build());

        vinculoRepository.save(VinculoUsuarioComerciante.builder()
                .usuario(usuarioB)
                .comerciante(comercianteB)
                .tipoVinculo(TipoVinculoComerciante.TITULAR)
                .estado(EstadoVinculoComerciante.VERIFICADO)
                .fechaInicio(LocalDate.now().minusDays(10))
                .fechaVerificacion(OffsetDateTime.now())
                .verificadoPor(usuarioB)
                .build());

        // Mercado y Puestos
        MercadoMunicipal mercado = mercadoRepository.save(MercadoMunicipal.builder()
                .nombre("Mercado Test Seguridad " + ts)
                .codigo("MSEC-" + (ts % 1000))
                .direccion("Jr. Quinua 123")
                .estado(EstadoMercado.ACTIVO)
                .build());

        puestoA = puestoRepository.save(PuestoComercial.builder()
                .mercado(mercado)
                .codigo("PSEC-A-" + (ts % 1000))
                .numero("S-01")
                .areaMetrosCuadrados(BigDecimal.valueOf(7.0))
                .estado(EstadoPuesto.OCUPADO)
                .build());

        puestoB = puestoRepository.save(PuestoComercial.builder()
                .mercado(mercado)
                .codigo("PSEC-B-" + (ts % 1000))
                .numero("S-02")
                .areaMetrosCuadrados(BigDecimal.valueOf(7.0))
                .estado(EstadoPuesto.OCUPADO)
                .build());

        asignacionA = asignacionRepository.save(AsignacionPuesto.builder()
                .codigoAsignacion("ASIG-A-" + (ts % 10000))
                .puesto(puestoA)
                .comerciante(comercianteA)
                .fechaInicio(LocalDate.now().minusDays(5))
                .fechaFin(LocalDate.now().plusMonths(6))
                .estado(EstadoAsignacion.VIGENTE)
                .build());

        AsignacionPuesto asignacionB = asignacionRepository.save(AsignacionPuesto.builder()
                .codigoAsignacion("ASIG-B-" + (ts % 10000))
                .puesto(puestoB)
                .comerciante(comercianteB)
                .fechaInicio(LocalDate.now().minusDays(5))
                .fechaFin(LocalDate.now().plusMonths(6))
                .estado(EstadoAsignacion.VIGENTE)
                .build());

        contratoB = new ContratoOcupacion();
        contratoB.setCodigoContrato("CON-B-" + (ts % 10000));
        contratoB.setAsignacion(asignacionB);
        contratoB.setPuesto(puestoB);
        contratoB.setComerciante(comercianteB);
        contratoB.setTipoContratoCodigo("ESTANDAR");
        contratoB.setFechaInicio(LocalDate.now().minusDays(5));
        contratoB.setFechaFin(LocalDate.now().plusMonths(6));
        contratoB.setEstado(EstadoContrato.VIGENTE);
        contratoB = contratoRepository.save(contratoB);

        ConceptoCobroMercado concepto = conceptoRepository.findAll().stream().findFirst().orElseGet(() ->
                conceptoRepository.save(ConceptoCobroMercado.builder()
                        .codigo("TARIFA_GENERAL")
                        .nombre("Tarifa General")
                        .tipoConcepto(TipoConceptoCobro.USO_PUESTO_COMERCIAL)
                        .estado("ACTIVO")
                        .build()));

        obligacionB = obligacionRepository.save(ObligacionMercado.builder()
                .codigoObligacion("OBL-B-" + (ts % 10000))
                .comerciante(comercianteB)
                .puesto(puestoB)
                .concepto(concepto)
                .montoOriginal(BigDecimal.valueOf(40.0))
                .saldoPendiente(BigDecimal.valueOf(40.0))
                .estado(EstadoObligacion.PENDIENTE)
                .build());

        // JWT Tokens
        CustomUserDetails userDetailsA = CustomUserDetails.builder()
                .id(usuarioA.getId())
                .username(usuarioA.getUsername())
                .password("dummy")
                .enabled(true)
                .accountNonLocked(true)
                .roles(Set.of("ROLE_COMERCIANTE"))
                .permisos(Set.of(
                        "PORTAL_COMERCIANTE_ACCEDER",
                        "PORTAL_COMERCIANTE_PERFIL_VER",
                        "PORTAL_COMERCIANTE_CONTACTO_EDITAR",
                        "PORTAL_COMERCIANTE_PUESTOS_VER",
                        "PORTAL_COMERCIANTE_CONTRATOS_VER",
                        "PORTAL_COMERCIANTE_AUTORIZACIONES_VER",
                        "PORTAL_COMERCIANTE_FINANZAS_VER",
                        "PORTAL_COMERCIANTE_SOLICITUDES_VER",
                        "PORTAL_COMERCIANTE_SOLICITUDES_CREAR",
                        "PORTAL_COMERCIANTE_DOCUMENTOS_DESCARGAR",
                        "PORTAL_COMERCIANTE_INCIDENCIAS_CREAR"
                ))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_COMERCIANTE"),
                        new SimpleGrantedAuthority("PORTAL_COMERCIANTE_ACCEDER"),
                        new SimpleGrantedAuthority("PORTAL_COMERCIANTE_PERFIL_VER"),
                        new SimpleGrantedAuthority("PORTAL_COMERCIANTE_CONTACTO_EDITAR"),
                        new SimpleGrantedAuthority("PORTAL_COMERCIANTE_PUESTOS_VER"),
                        new SimpleGrantedAuthority("PORTAL_COMERCIANTE_CONTRATOS_VER"),
                        new SimpleGrantedAuthority("PORTAL_COMERCIANTE_AUTORIZACIONES_VER"),
                        new SimpleGrantedAuthority("PORTAL_COMERCIANTE_FINANZAS_VER"),
                        new SimpleGrantedAuthority("PORTAL_COMERCIANTE_SOLICITUDES_VER"),
                        new SimpleGrantedAuthority("PORTAL_COMERCIANTE_SOLICITUDES_CREAR"),
                        new SimpleGrantedAuthority("PORTAL_COMERCIANTE_DOCUMENTOS_DESCARGAR"),
                        new SimpleGrantedAuthority("PORTAL_COMERCIANTE_INCIDENCIAS_CREAR")
                ))
                .build();

        CustomUserDetails userDetailsB = CustomUserDetails.builder()
                .id(usuarioB.getId())
                .username(usuarioB.getUsername())
                .password("dummy")
                .enabled(true)
                .accountNonLocked(true)
                .roles(Set.of("ROLE_COMERCIANTE"))
                .permisos(Set.of("PORTAL_COMERCIANTE_ACCEDER", "PORTAL_COMERCIANTE_PERFIL_VER"))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_COMERCIANTE"),
                        new SimpleGrantedAuthority("PORTAL_COMERCIANTE_ACCEDER"),
                        new SimpleGrantedAuthority("PORTAL_COMERCIANTE_PERFIL_VER")
                ))
                .build();

        tokenComercianteA = jwtTokenProvider.generateAccessToken(userDetailsA);
        tokenComercianteB = jwtTokenProvider.generateAccessToken(userDetailsB);
    }

    @Test
    @DisplayName("Rechazo de usuario anónimo en endpoints protegidos del portal del comerciante")
    void testRechazoUsuarioAnonimo() throws Exception {
        mockMvc.perform(get("/api/portal/comerciante/dashboard"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/portal/comerciante/puestos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Acceso autorizado al dashboard vía API con JWT")
    void testAccesoDashboardApiConJwt() throws Exception {
        mockMvc.perform(get("/api/portal/comerciante/dashboard")
                        .header("Authorization", "Bearer " + tokenComercianteA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreTitular", containsString("Carlos Huamán")))
                .andExpect(jsonPath("$.puestosActivos").value(1));
    }

    @Test
    @DisplayName("Consulta de perfil propio vía API")
    void testConsultaPerfilApi() throws Exception {
        mockMvc.perform(get("/api/portal/comerciante/perfil")
                        .header("Authorization", "Bearer " + tokenComercianteA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreCompletoORazonSocial", containsString("Carlos Huamán")))
                .andExpect(jsonPath("$.documentoEnmascarado", containsString("***")));
    }

    @Test
    @DisplayName("Actualización autorizada de contacto vía API")
    void testActualizacionContactoApi() throws Exception {
        PortalComercianteContactoUpdateRequest req = PortalComercianteContactoUpdateRequest.builder()
                .telefono("966000111")
                .correoElectronico("carlos.nuevo@quinua.gob.pe")
                .direccion("Jr. Huamanga 456")
                .build();

        mockMvc.perform(put("/api/portal/comerciante/perfil/contacto")
                        .header("Authorization", "Bearer " + tokenComercianteA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.telefono").value("966000111"))
                .andExpect(jsonPath("$.correoElectronico").value("carlos.nuevo@quinua.gob.pe"));
    }

    @Test
    @DisplayName("Prevención estricta IDOR: Comerciante A intenta consultar contrato de Comerciante B vía API")
    void testRechazoIdorContratoAjenoApi() throws Exception {
        mockMvc.perform(get("/api/portal/comerciante/contratos/" + contratoB.getId())
                        .header("Authorization", "Bearer " + tokenComercianteA))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Prevención estricta IDOR: Comerciante A intenta consultar puesto ajeno vía API")
    void testRechazoIdorPuestoAjenoApi() throws Exception {
        mockMvc.perform(get("/api/portal/comerciante/puestos/" + puestoB.getId())
                        .header("Authorization", "Bearer " + tokenComercianteA))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Prevención estricta IDOR: Comerciante A intenta consultar obligación ajena vía API")
    void testRechazoIdorObligacionAjenaApi() throws Exception {
        mockMvc.perform(get("/api/portal/comerciante/obligaciones/" + obligacionB.getId())
                        .header("Authorization", "Bearer " + tokenComercianteA))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Descarga de estado de cuenta PDF vía API")
    void testDescargarEstadoCuentaApi() throws Exception {
        mockMvc.perform(get("/api/portal/comerciante/estado-cuenta/descargar")
                        .header("Authorization", "Bearer " + tokenComercianteA))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    @DisplayName("Presentación de solicitud de renovación válida vía API")
    void testPresentarRenovacionApi() throws Exception {
        PortalComercianteRenovacionRequest req = PortalComercianteRenovacionRequest.builder()
                .asignacionId(asignacionA.getId())
                .plazoMesesSolicitado(12)
                .justificacion("Prórroga anual de actividades comerciales.")
                .build();

        mockMvc.perform(post("/api/portal/comerciante/solicitudes/renovacion")
                        .header("Authorization", "Bearer " + tokenComercianteA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigoSolicitud").isNotEmpty());
    }
}
