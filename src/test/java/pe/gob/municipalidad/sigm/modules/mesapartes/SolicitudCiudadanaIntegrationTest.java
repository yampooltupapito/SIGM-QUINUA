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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.constants.SecurityConstants;
import pe.gob.municipalidad.sigm.common.document.entity.CategoriaDocumento;
import pe.gob.municipalidad.sigm.common.document.entity.ClasificacionAcceso;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.document.repository.DocumentoRepository;
import pe.gob.municipalidad.sigm.common.model.*;
import pe.gob.municipalidad.sigm.common.repository.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvSolicitudDtos.GuardarBorradorSolicitudRequest;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvSolicitudDtos.PresentarSolicitudRequest;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoRelacionDocumentoExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.UnidadPlazo;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.*;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
@DisplayName("Pruebas de Integración - Registro de Solicitudes Ciudadanas y Gestión de Expedientes (Prompt 13)")
class SolicitudCiudadanaIntegrationTest {

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
    private TipoTramiteRepository tipoTramiteRepository;

    @Autowired
    private DocumentoRepository documentoRepository;

    @Autowired
    private ExpedienteDocumentoRepository expedienteDocumentoRepository;

    @Autowired
    private ExpedienteRepository expedienteRepository;

    @Autowired
    private PresentacionIdempotenciaRepository idempotenciaRepository;

    @Autowired
    private FormularioTramiteRepository formularioRepository;

    @Autowired
    private FormularioVersionRepository versionRepository;

    @Autowired
    private FormularioCampoRepository campoRepository;

    private Usuario ciudadano1;
    private Usuario ciudadano2;
    private Cookie ciudadano1Cookie;
    private Cookie ciudadano2Cookie;

    private TipoTramite tramiteGeneral;
    private TipoTramite tramitePosesion;

    @BeforeEach
    void setUp() {
        Municipalidad muni = municipalidadRepository.findAll().stream().findFirst().orElseGet(() ->
                municipalidadRepository.save(Municipalidad.builder()
                        .nombre("Municipalidad Distrital de Quinua")
                        .nombreCorto("MDQ")
                        .ubigeo("050108")
                        .departamento("Ayacucho")
                        .provincia("Huamanga")
                        .distrito("Quinua")
                        .direccion("Plaza de Armas s/n, Quinua")
                        .estado("ACTIVO")
                        .build()));

        Oficina secGen = oficinaRepository.findByCodigo("SEC-GEN").orElseGet(() ->
                oficinaRepository.save(Oficina.builder()
                        .codigo("SEC-GEN")
                        .nombre("Secretaría General y Mesa de Partes")
                        .sigla("SEC-GEN")
                        .municipalidad(muni)
                        .estado("ACTIVO")
                        .build()));

        Rol rolCiudadano = rolRepository.findByCodigo("ROLE_CIUDADANO").orElseGet(() ->
                rolRepository.save(Rol.builder()
                        .codigo("ROLE_CIUDADANO")
                        .nombre("Ciudadano / Administrado")
                        .estado("ACTIVO")
                        .build()));

        String suffix = UUID.randomUUID().toString().substring(0, 6);

        // Ciudadano 1
        Persona p1 = personaRepository.save(Persona.builder()
                .nombres("Wari")
                .apellidoPaterno("Quispe")
                .apellidoMaterno("Ayala")
                .tipoDocumento("DNI")
                .numeroDocumento("44" + suffix)
                .correo("wari." + suffix + "@quinua.gob.pe")
                .direccion("Barrio San Blas, Quinua")
                .telefono("966112233")
                .estado("ACTIVO")
                .build());

        ciudadano1 = usuarioRepository.save(Usuario.builder()
                .username("ciudadano1_" + suffix)
                .passwordHash(passwordEncoder.encode("Password123!"))
                .persona(p1)
                .enabled(true)
                .roles(Set.of(rolCiudadano))
                .build());

        // Ciudadano 2
        Persona p2 = personaRepository.save(Persona.builder()
                .nombres("Killa")
                .apellidoPaterno("Sulca")
                .apellidoMaterno("Mendoza")
                .tipoDocumento("DNI")
                .numeroDocumento("45" + suffix)
                .correo("killa." + suffix + "@quinua.gob.pe")
                .direccion("Jr. Sucre 450, Quinua")
                .telefono("966445566")
                .estado("ACTIVO")
                .build());

        ciudadano2 = usuarioRepository.save(Usuario.builder()
                .username("ciudadano2_" + suffix)
                .passwordHash(passwordEncoder.encode("Password123!"))
                .persona(p2)
                .enabled(true)
                .roles(Set.of(rolCiudadano))
                .build());

        ciudadano1Cookie = crearCookie(ciudadano1, List.of("ROLE_CIUDADANO", "MESAPARTES_EXPEDIENTE_CREAR", "MESAPARTES_EXPEDIENTE_VER_PROPIO"));
        ciudadano2Cookie = crearCookie(ciudadano2, List.of("ROLE_CIUDADANO", "MESAPARTES_EXPEDIENTE_CREAR", "MESAPARTES_EXPEDIENTE_VER_PROPIO"));

        tramiteGeneral = tipoTramiteRepository.findByCodigo("TRAM-GEN").orElseGet(() ->
                tipoTramiteRepository.save(TipoTramite.builder()
                        .codigo("TRAM-GEN")
                        .nombre("Solicitud General / Trámite Común")
                        .oficinaResponsable(secGen)
                        .plazoDias(30)
                        .unidadPlazo(UnidadPlazo.DIAS_HABILES)
                        .permitePresentacionVirtual(true)
                        .estado("ACTIVO")
                        .build()));

        tramitePosesion = tipoTramiteRepository.findByCodigo("TRAM-POS").orElseGet(() ->
                tipoTramiteRepository.save(TipoTramite.builder()
                        .codigo("TRAM-POS")
                        .nombre("Constancia de Posesión de Predio")
                        .oficinaResponsable(secGen)
                        .plazoDias(15)
                        .unidadPlazo(UnidadPlazo.DIAS_HABILES)
                        .permitePresentacionVirtual(true)
                        .estado("ACTIVO")
                        .build()));

        FormularioTramite formPos = formularioRepository.findByCodigo("FORM-POS").orElseGet(() ->
                formularioRepository.save(FormularioTramite.builder()
                        .tipoTramite(tramitePosesion)
                        .codigo("FORM-POS")
                        .nombre("Formulario para Constancia de Posesión")
                        .descripcion("Metadatos del inmueble o predio")
                        .activo(true)
                        .build()));
        formPos.setTipoTramite(tramitePosesion);
        formPos.setActivo(true);
        formularioRepository.save(formPos);

        versionRepository.findFirstByFormularioIdAndEstadoOrderByVersionDesc(formPos.getId(), pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoFormularioVersion.PUBLICADO)
                .orElseGet(() -> {
                    FormularioVersion v = versionRepository.save(FormularioVersion.builder()
                            .formulario(formPos)
                            .version(1)
                            .estado(pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoFormularioVersion.PUBLICADO)
                            .build());
                    campoRepository.save(FormularioCampo.builder()
                            .version(v)
                            .nombreCampo("direccion_predio")
                            .etiqueta("Dirección del Predio")
                            .tipoCampo(pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoCampoFormulario.TEXTO)
                            .obligatorio(true)
                            .orden(1)
                            .build());
                    return v;
                });

        formularioRepository.findByCodigo("FORM-GEN").ifPresent(f -> {
            f.setTipoTramite(tramiteGeneral);
            formularioRepository.save(f);
        });
    }

    @Test
    @DisplayName("1. Debe verificar acceso autenticado y denegar a usuarios anónimos en /mesa-partes/nueva-solicitud")
    void testAccesoAutenticadoNuevaSolicitud() throws Exception {
        // Sin autenticación: redirigido al login
        mockMvc.perform(get("/mesa-partes/nueva-solicitud"))
                .andExpect(status().is3xxRedirection());

        // Con autenticación ciudadana: renderiza 200 OK con modelo
        mockMvc.perform(get("/mesa-partes/nueva-solicitud")
                        .cookie(ciudadano1Cookie))
                .andExpect(status().isOk())
                .andExpect(view().name("mesa-partes/nueva-solicitud"))
                .andExpect(model().attributeExists("usuario", "persona", "tramites"));
    }

    @Test
    @DisplayName("2. Debe consultar el esquema y formulario dinámico configurado para un trámite")
    void testEsquemaTramiteFormularioDinamico() throws Exception {
        mockMvc.perform(get("/api/mesa-partes/tipos-tramite/{id}/formulario", tramitePosesion.getId())
                        .cookie(ciudadano1Cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.codigo").value("TRAM-POS"))
                .andExpect(jsonPath("$.data.requisitos").isArray())
                .andExpect(jsonPath("$.data.formulario").exists());
    }

    @Test
    @DisplayName("3. Debe crear, recuperar y actualizar un borrador de solicitud con campos dinámicos")
    void testGuardarYActualizarBorrador() throws Exception {
        Map<String, String> valores = new HashMap<>();
        valores.put("referencia_documento", "Carta N° 005-2026");
        valores.put("solicita_audiencia", "true");

        GuardarBorradorSolicitudRequest req = GuardarBorradorSolicitudRequest.builder()
                .tipoTramiteId(tramiteGeneral.getId())
                .asunto("Solicitud de mantenimiento de vía comunal")
                .descripcion("Requerimiento vecinal para la vía principal de Quinua")
                .valoresFormulario(valores)
                .build();

        // 1. Crear borrador
        String respJson = mockMvc.perform(post("/api/mesa-partes/expedientes/borradores")
                        .cookie(ciudadano1Cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.expediente.uuid").isNotEmpty())
                .andExpect(jsonPath("$.data.expediente.estadoActual").value("BORRADOR"))
                .andReturn().getResponse().getContentAsString();

        String uuidStr = objectMapper.readTree(respJson).path("data").path("expediente").path("uuid").asText();
        UUID expedienteUuid = UUID.fromString(uuidStr);

        // 2. Recuperar borrador
        mockMvc.perform(get("/api/mesa-partes/expedientes/borradores/{uuid}", expedienteUuid)
                        .cookie(ciudadano1Cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.expediente.asunto").value("Solicitud de mantenimiento de vía comunal"))
                .andExpect(jsonPath("$.data.expediente.estadoActual").value("BORRADOR"));

        // 3. Actualizar borrador
        req.setAsunto("Solicitud de mantenimiento de vía comunal - Actualizado");
        mockMvc.perform(put("/api/mesa-partes/expedientes/borradores/{uuid}", expedienteUuid)
                        .cookie(ciudadano1Cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.expediente.asunto").value("Solicitud de mantenimiento de vía comunal - Actualizado"));
    }

    @Test
    @DisplayName("4. Debe rechazar consulta o edición de borrador ajeno (Anti-IDOR)")
    void testRechazarBorradorAjenoAntiIdor() throws Exception {
        GuardarBorradorSolicitudRequest req = GuardarBorradorSolicitudRequest.builder()
                .tipoTramiteId(tramiteGeneral.getId())
                .asunto("Solicitud confidencial de ciudadano 1")
                .descripcion("Contenido privado")
                .build();

        String respJson = mockMvc.perform(post("/api/mesa-partes/expedientes/borradores")
                        .cookie(ciudadano1Cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String uuidStr = objectMapper.readTree(respJson).path("data").path("expediente").path("uuid").asText();
        UUID uuidExpediente = UUID.fromString(uuidStr);

        // Ciudadano 2 intenta acceder al borrador de Ciudadano 1
        mockMvc.perform(get("/api/mesa-partes/expedientes/borradores/{uuid}", uuidExpediente)
                        .cookie(ciudadano2Cookie))
                .andExpect(status().isForbidden());

        // Ciudadano 2 intenta modificar el borrador de Ciudadano 1
        req.setAsunto("Modificación no autorizada");
        mockMvc.perform(put("/api/mesa-partes/expedientes/borradores/{uuid}", uuidExpediente)
                        .cookie(ciudadano2Cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("5. Debe permitir adjuntar archivo al borrador y desvincularlo correctamente")
    void testAdjuntarYEliminarDocumentoBorrador() throws Exception {
        GuardarBorradorSolicitudRequest req = GuardarBorradorSolicitudRequest.builder()
                .tipoTramiteId(tramiteGeneral.getId())
                .asunto("Solicitud con documento adjunto")
                .descripcion("Se adjunta solicitud firmada")
                .build();

        String respJson = mockMvc.perform(post("/api/mesa-partes/expedientes/borradores")
                        .cookie(ciudadano1Cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String uuidStr = objectMapper.readTree(respJson).path("data").path("expediente").path("uuid").asText();
        UUID expedienteUuid = UUID.fromString(uuidStr);

        // Subir archivo PDF
        MockMultipartFile file = new MockMultipartFile(
                "archivo",
                "solicitud_firmada.pdf",
                "application/pdf",
                "%PDF-1.7 Contenido de prueba de la solicitud firmada".getBytes()
        );

        String subidaJson = mockMvc.perform(multipart("/api/mesa-partes/expedientes/{uuid}/documentos/subir", expedienteUuid)
                        .file(file)
                        .param("tipoRelacion", "PRINCIPAL")
                        .cookie(ciudadano1Cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.documentos", hasSize(1)))
                .andReturn().getResponse().getContentAsString();

        String docUuidStr = objectMapper.readTree(subidaJson).path("data").path("documentos").get(0).path("documentoUuid").asText();
        UUID documentoUuid = UUID.fromString(docUuidStr);

        // Desvincular archivo
        mockMvc.perform(delete("/api/mesa-partes/expedientes/{uuid}/documentos/archivo/{docUuid}", expedienteUuid, documentoUuid)
                        .cookie(ciudadano1Cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Verificar que ya no tiene documentos
        mockMvc.perform(get("/api/mesa-partes/expedientes/borradores/{uuid}", expedienteUuid)
                        .cookie(ciudadano1Cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.documentos", hasSize(0)));
    }

    @Test
    @DisplayName("6. Debe presentar formalmente la solicitud, generar número correlativo único y garantizar idempotencia")
    void testPresentarSolicitudEIdempotencia() throws Exception {
        GuardarBorradorSolicitudRequest req = GuardarBorradorSolicitudRequest.builder()
                .tipoTramiteId(tramiteGeneral.getId())
                .asunto("Solicitud formal para inauguración comunal")
                .descripcion("Petitorio con fundamentación legal")
                .build();

        String respJson = mockMvc.perform(post("/api/mesa-partes/expedientes/borradores")
                        .cookie(ciudadano1Cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String uuidStr = objectMapper.readTree(respJson).path("data").path("expediente").path("uuid").asText();
        UUID expedienteUuid = UUID.fromString(uuidStr);

        // Presentar con clave de idempotencia
        String claveIdempotencia = "IDEM-" + UUID.randomUUID();
        PresentarSolicitudRequest presReq = PresentarSolicitudRequest.builder()
                .claveIdempotencia(claveIdempotencia)
                .declaracionVeracidad(true)
                .build();

        String presJson1 = mockMvc.perform(post("/api/mesa-partes/expedientes/{uuid}/presentar", expedienteUuid)
                        .cookie(ciudadano1Cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(presReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.numeroExpediente", startsWith("MPV-")))
                .andExpect(jsonPath("$.data.estadoActual").value("PRESENTADO"))
                .andReturn().getResponse().getContentAsString();

        String numExpediente = objectMapper.readTree(presJson1).path("data").path("numeroExpediente").asText();

        // 2. Reintento inmediato con la MISMA clave de idempotencia (simulando doble clic o reintento de red)
        String presJson2 = mockMvc.perform(post("/api/mesa-partes/expedientes/{uuid}/presentar", expedienteUuid)
                        .cookie(ciudadano1Cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(presReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.numeroExpediente").value(numExpediente))
                .andReturn().getResponse().getContentAsString();

        String numExpediente2 = objectMapper.readTree(presJson2).path("data").path("numeroExpediente").asText();
        long id1 = objectMapper.readTree(presJson1).path("data").path("id").asLong();
        long id2 = objectMapper.readTree(presJson2).path("data").path("id").asLong();

        assertEquals(numExpediente, numExpediente2, "El número de expediente debe ser exactamente el mismo para la misma clave de idempotencia");
        assertEquals(id1, id2, "El ID del expediente debe ser el mismo, sin generar duplicados");

        // 3. Descargar constancia oficial en PDF
        byte[] pdfBytes = mockMvc.perform(get("/api/mesa-partes/expedientes/{uuid}/constancia", expedienteUuid)
                        .cookie(ciudadano1Cookie))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andReturn().getResponse().getContentAsByteArray();

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 500);

        // 4. Ciudadano 2 no puede descargar la constancia de Ciudadano 1
        mockMvc.perform(get("/api/mesa-partes/expedientes/{uuid}/constancia", expedienteUuid)
                        .cookie(ciudadano2Cookie))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("7. Debe listar las solicitudes del ciudadano autenticado y consultar bandeja web")
    void testListarMisSolicitudes() throws Exception {
        // Crear un borrador para ciudadano 1
        mockMvc.perform(post("/api/mesa-partes/expedientes/borradores")
                        .cookie(ciudadano1Cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(GuardarBorradorSolicitudRequest.builder()
                                .tipoTramiteId(tramiteGeneral.getId())
                                .asunto("Solicitud en mi bandeja")
                                .descripcion("Prueba")
                                .build())))
                .andExpect(status().isCreated());

        // API REST: /api/mesa-partes/mis-solicitudes
        mockMvc.perform(get("/api/mesa-partes/mis-solicitudes")
                        .cookie(ciudadano1Cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements", greaterThanOrEqualTo(1)));

        // Vista Web: /mesa-partes/mis-solicitudes
        mockMvc.perform(get("/mesa-partes/mis-solicitudes")
                        .cookie(ciudadano1Cookie))
                .andExpect(status().isOk())
                .andExpect(view().name("mesa-partes/mis-solicitudes"))
                .andExpect(model().attributeExists("solicitudes"));
    }

    private Cookie crearCookie(Usuario usuario, List<String> authorities) {
        List<org.springframework.security.core.authority.SimpleGrantedAuthority> auths = authorities.stream()
                .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                .toList();

        pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails details = pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .password(usuario.getPasswordHash())
                .enabled(true)
                .accountNonLocked(true)
                .authorities(auths)
                .build();

        Cookie c = new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, jwtTokenProvider.generateAccessToken(details));
        c.setPath("/");
        return c;
    }
}
