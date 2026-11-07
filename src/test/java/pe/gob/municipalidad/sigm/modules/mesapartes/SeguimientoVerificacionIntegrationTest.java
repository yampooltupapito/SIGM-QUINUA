package pe.gob.municipalidad.sigm.modules.mesapartes;

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
import pe.gob.municipalidad.sigm.common.model.*;
import pe.gob.municipalidad.sigm.common.repository.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.SeguimientoDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.*;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

import java.time.OffsetDateTime;
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
@DisplayName("Pruebas de Integración - Seguimiento Ciudadano y Verificación Documental (Prompt 17)")
class SeguimientoVerificacionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private OficinaRepository oficinaRepository;

    @Autowired
    private MunicipalidadRepository municipalidadRepository;

    @Autowired
    private ExpedienteRepository expedienteRepository;

    @Autowired
    private TipoTramiteRepository tipoTramiteRepository;

    @Autowired
    private CodigoSeguimientoRepository codigoSeguimientoRepository;

    @Autowired
    private VerificacionDocumentalRepository verificacionDocumentalRepository;

    private Usuario usuarioCiudadano;
    private Usuario usuarioOtroCiudadano;
    private Expediente expedientePrueba;
    private CodigoSeguimiento codigoSeguimiento;
    private VerificacionDocumental verificacionDoc;
    private Cookie ciudadanoCookie;
    private Cookie otroCiudadanoCookie;

    @BeforeEach
    void setUp() {
        Municipalidad muni = municipalidadRepository.findAll().stream().findFirst().orElseGet(() ->
                municipalidadRepository.save(Municipalidad.builder()
                        .nombre("Municipalidad Distrital de Quinua")
                        .nombreCorto("MD Quinua")
                        .ubigeo("050108")
                        .departamento("Ayacucho")
                        .provincia("Huamanga")
                        .distrito("Quinua")
                        .direccion("Plaza de Armas s/n, Quinua, Huamanga, Ayacucho")
                        .telefono("066-812345")
                        .correoInstitucional("mesadepartes@muniquinua.gob.pe")
                        .estado("ACTIVO")
                        .build())
        );

        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 6);

        Oficina oficina = oficinaRepository.save(Oficina.builder()
                .codigo("OFI-MPV-" + uniqueSuffix)
                .nombre("Mesa de Partes General")
                .sigla("MPV")
                .nivelJerarquico(1)
                .estado("ACTIVO")
                .municipalidad(muni)
                .build());

        Rol rolCiudadano = rolRepository.findByCodigo("ROLE_CIUDADANO").orElseGet(() ->
                rolRepository.save(Rol.builder()
                        .codigo("ROLE_CIUDADANO")
                        .nombre("Ciudadano")
                        .descripcion("Ciudadano")
                        .estado("ACTIVO")
                        .build())
        );

        Persona personaCiudadano = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("77" + String.format("%06d", new Random().nextInt(999999)))
                .nombres("Carlos")
                .apellidoPaterno("Quispe")
                .apellidoMaterno("Huamán")
                .correo("carlos." + uniqueSuffix + "@gmail.com")
                .build());

        usuarioCiudadano = usuarioRepository.save(Usuario.builder()
                .username("ciu_" + uniqueSuffix)
                .passwordHash(passwordEncoder.encode("Password2026!"))
                .persona(personaCiudadano)
                .roles(Set.of(rolCiudadano))
                .enabled(true)
                .build());

        Persona personaOtro = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("78" + String.format("%06d", new Random().nextInt(999999)))
                .nombres("María")
                .apellidoPaterno("Flores")
                .apellidoMaterno("Sánchez")
                .correo("maria." + uniqueSuffix + "@gmail.com")
                .build());

        usuarioOtroCiudadano = usuarioRepository.save(Usuario.builder()
                .username("otro_" + uniqueSuffix)
                .passwordHash(passwordEncoder.encode("Password2026!"))
                .persona(personaOtro)
                .roles(Set.of(rolCiudadano))
                .enabled(true)
                .build());

        TipoTramite tipoTramite = tipoTramiteRepository.save(TipoTramite.builder()
                .codigo("TT-POS-" + uniqueSuffix)
                .nombre("Constancia de Posesión")
                .oficinaResponsable(oficina)
                .plazoDias(15)
                .estado("ACTIVO")
                .build());

        expedientePrueba = new Expediente();
        expedientePrueba.setUuid(UUID.randomUUID());
        expedientePrueba.setNumeroExpediente("MPV-2026-990001");
        expedientePrueba.setCodigoSeguimiento("SEG-TEST1234");
        expedientePrueba.setAsunto("Constancia de Posesión para servicio de agua potable");
        expedientePrueba.setDescripcion("Predio ubicado en el Barrio Central de Quinua");
        expedientePrueba.setSolicitante(personaCiudadano);
        expedientePrueba.setTipoTramite(tipoTramite);
        expedientePrueba.setOficinaResponsable(oficina);
        expedientePrueba.setEstadoActual(EstadoExpediente.RECIBIDO);
        expedientePrueba.setFechaPresentacion(OffsetDateTime.now().minusDays(1));
        expedientePrueba.setFechaRecepcion(OffsetDateTime.now().minusHours(10));
        expedientePrueba = expedienteRepository.save(expedientePrueba);

        codigoSeguimiento = CodigoSeguimiento.builder()
                .expediente(expedientePrueba)
                .codigo("SEG-TEST1234")
                .tokenHash("hash-seg-1234")
                .estado(EstadoCodigoSeguimiento.ACTIVO)
                .fechaEmision(OffsetDateTime.now())
                .fechaExpiracion(OffsetDateTime.now().plusMonths(6))
                .intentosFallidos(0)
                .build();
        codigoSeguimiento = codigoSeguimientoRepository.save(codigoSeguimiento);

        verificacionDoc = VerificacionDocumental.builder()
                .codigoVerificacion("VER-990001")
                .tokenQr("qr-token-test-990001")
                .tipoDocumento("CONSTANCIA_PRESENTACION")
                .numeroDocumento("CONST-MPV-2026-990001")
                .fechaEmision(OffsetDateTime.now())
                .entidadEmisora("Municipalidad Distrital de Quinua")
                .autoridadFirmante("Mesa de Partes Virtual")
                .cargoFirmante("Sistema Institucional")
                .hashSha256("a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2")
                .estadoDocumento(EstadoVerificacionDocumento.VIGENTE)
                .urlVerificacion("/verificar-documento?codigo=VER-990001")
                .vecesConsultado(0)
                .build();
        verificacionDoc = verificacionDocumentalRepository.save(verificacionDoc);

        List<String> permisosCiudadano = List.of("MESAPARTES_SEGUIMIENTO_CIUDADANO", "MESAPARTES_SOLICITUD_CONSULTAR");
        ciudadanoCookie = buildJwtCookie(usuarioCiudadano, "ROLE_CIUDADANO", permisosCiudadano);
        otroCiudadanoCookie = buildJwtCookie(usuarioOtroCiudadano, "ROLE_CIUDADANO", permisosCiudadano);
    }

    private Cookie buildJwtCookie(Usuario usuario, String role, List<String> permissions) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(role));
        for (String p : permissions) {
            authorities.add(new SimpleGrantedAuthority(p));
        }

        CustomUserDetails userDetails = CustomUserDetails.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .personaId(usuario.getPersona() != null ? usuario.getPersona().getId() : null)
                .nombreCompleto(usuario.getPersona() != null ? usuario.getPersona().getNombreCompleto() : usuario.getUsername())
                .roles(Set.of(role))
                .permisos(new HashSet<>(permissions))
                .authorities(authorities)
                .enabled(true)
                .accountNonLocked(true)
                .build();

        String token = jwtTokenProvider.generateAccessToken(userDetails);
        Cookie cookie = new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, token);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        return cookie;
    }

    // 1. ACCESO PÚBLICO A LA PÁGINA WEB DE SEGUIMIENTO
    @Test
    @DisplayName("GET /mesa-partes/seguimiento debe responder 200 OK y renderizar vista pública")
    void testPaginaSeguimientoWeb() throws Exception {
        mockMvc.perform(get("/mesa-partes/seguimiento"))
                .andExpect(status().isOk())
                .andExpect(view().name("mesa-partes/seguimiento"))
                .andExpect(model().attributeExists("paginaActiva"));
    }

    // 2. CONSULTA PÚBLICA MEDIANTE CÓDIGO SEGURO
    @Test
    @DisplayName("POST /api/publico/mesa-partes/consultar debe retornar datos públicos mínimos sin PII")
    void testConsultaPublicaApi() throws Exception {
        ConsultaPublicaRequest request = new ConsultaPublicaRequest("SEG-TEST1234");

        mockMvc.perform(post("/api/publico/mesa-partes/consultar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.encontrado").value(true))
                .andExpect(jsonPath("$.data.numeroExpediente").value("MPV-2026-990001"))
                .andExpect(jsonPath("$.data.tipoTramite").value("Constancia de Posesión"))
                .andExpect(jsonPath("$.data.etapaProgreso").value(2))
                .andExpect(jsonPath("$.data.etapas").isArray());
    }

    // 3. CONSULTA PÚBLICA CON CÓDIGO INEXISTENTE
    @Test
    @DisplayName("POST /api/publico/mesa-partes/consultar con código inválido debe devolver 404 Not Found")
    void testConsultaPublicaApiInexistente() throws Exception {
        ConsultaPublicaRequest request = new ConsultaPublicaRequest("SEG-INEXISTENTE");

        mockMvc.perform(post("/api/publico/mesa-partes/consultar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // 4. ACCESO PÚBLICO A LA PÁGINA WEB DE VERIFICACIÓN DOCUMENTAL
    @Test
    @DisplayName("GET /verificar-documento debe responder 200 OK y renderizar vista de verificación")
    void testPaginaVerificarDocumentoWeb() throws Exception {
        mockMvc.perform(get("/verificar-documento"))
                .andExpect(status().isOk())
                .andExpect(view().name("mesa-partes/verificar-documento"))
                .andExpect(model().attributeExists("paginaActiva"));
    }

    // 5. VERIFICACIÓN PÚBLICA DE DOCUMENTO VIGENTE
    @Test
    @DisplayName("POST /api/publico/documentos/verificar debe verificar la autenticidad y hash SHA-256")
    void testVerificarDocumentoApi() throws Exception {
        VerificarDocumentoRequest request = new VerificarDocumentoRequest("VER-990001");

        mockMvc.perform(post("/api/publico/documentos/verificar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.valido").value(true))
                .andExpect(jsonPath("$.data.codigoVerificacion").value("VER-990001"))
                .andExpect(jsonPath("$.data.tipoDocumento").value("CONSTANCIA_PRESENTACION"))
                .andExpect(jsonPath("$.data.estadoDocumento").value("VIGENTE"))
                .andExpect(jsonPath("$.data.hashSha256").value("a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2"));
    }

    // 6. SEGUIMIENTO INTEGRAL AUTENTICADO: ACCESO DEL TITULAR
    @Test
    @DisplayName("GET /api/mesa-partes/mis-solicitudes/{id}/seguimiento debe permitir al titular consultar su expediente")
    void testSeguimientoCiudadanoAutenticadoTitular() throws Exception {
        mockMvc.perform(get("/api/mesa-partes/mis-solicitudes/" + expedientePrueba.getId() + "/seguimiento")
                        .cookie(ciudadanoCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.expedienteId").value(expedientePrueba.getId()))
                .andExpect(jsonPath("$.data.numeroExpediente").value("MPV-2026-990001"))
                .andExpect(jsonPath("$.data.solicitanteNombre").value("Carlos Quispe Huamán"))
                .andExpect(jsonPath("$.data.codigoSeguimiento").value("SEG-TEST1234"));
    }

    // 7. PREVENCIÓN ANTI-IDOR: RECHAZO A EXPEDIENTE AJENO
    @Test
    @DisplayName("Anti-IDOR: GET /api/mesa-partes/mis-solicitudes/{id}/seguimiento con otro usuario debe responder 403 Forbidden")
    void testAntiIdorRechazoExpedienteAjeno() throws Exception {
        mockMvc.perform(get("/api/mesa-partes/mis-solicitudes/" + expedientePrueba.getId() + "/seguimiento")
                        .cookie(otroCiudadanoCookie))
                .andExpect(status().isForbidden());
    }

    // 8. ACCESO NO AUTENTICADO A ENDPOINT PROTEGIDO
    @Test
    @DisplayName("GET /api/mesa-partes/mis-solicitudes/{id}/seguimiento sin autenticación debe responder 401 Unauthorized")
    void testSeguimientoSinAutenticacion() throws Exception {
        mockMvc.perform(get("/api/mesa-partes/mis-solicitudes/" + expedientePrueba.getId() + "/seguimiento"))
                .andExpect(status().isUnauthorized());
    }

    // 9. HISTORIAL CIUDADANO COMUNICABLE
    @Test
    @DisplayName("GET /api/mesa-partes/mis-solicitudes/{id}/historial debe retornar lista comunicable")
    void testHistorialCiudadanoApi() throws Exception {
        mockMvc.perform(get("/api/mesa-partes/mis-solicitudes/" + expedientePrueba.getId() + "/historial")
                        .cookie(ciudadanoCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }
}
