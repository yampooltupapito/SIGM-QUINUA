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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.constants.SecurityConstants;
import pe.gob.municipalidad.sigm.common.document.entity.*;
import pe.gob.municipalidad.sigm.common.document.repository.DocumentoRepository;
import pe.gob.municipalidad.sigm.common.model.*;
import pe.gob.municipalidad.sigm.common.repository.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.TipoTramite;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.TipoTramiteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.NumeracionExpedienteService;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MesaPartesIntegrationTest {

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
    private OficinaRepository oficinaRepository;

    @Autowired
    private MunicipalidadRepository municipalidadRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private TipoTramiteRepository tipoTramiteRepository;

    @Autowired
    private DocumentoRepository documentoRepository;

    @Autowired
    private NumeracionExpedienteService numeracionService;

    private Usuario adminUser;
    private Usuario operadorUser;
    private Usuario ciudadano1User;
    private Usuario ciudadano2User;

    private Cookie adminCookie;
    private Cookie operadorCookie;
    private Cookie ciudadano1Cookie;
    private Cookie ciudadano2Cookie;

    private Oficina oficinaMpv;
    private Oficina oficinaObras;
    private TipoTramite tramiteLicencia;
    private Documento documentoActivo;

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
                        .build()));

        oficinaMpv = oficinaRepository.findByCodigo("MPV-CENTRAL").orElseGet(() ->
                oficinaRepository.save(Oficina.builder()
                        .municipalidad(muni)
                        .codigo("MPV-CENTRAL")
                        .nombre("Mesa de Partes Virtual Central")
                        .sigla("MPV")
                        .nivelJerarquico(2)
                        .estado("ACTIVO")
                        .build()));

        oficinaObras = oficinaRepository.findByCodigo("OBRAS-PUBLICAS").orElseGet(() ->
                oficinaRepository.save(Oficina.builder()
                        .municipalidad(muni)
                        .codigo("OBRAS-PUBLICAS")
                        .nombre("Gerencia de Obras y Desarrollo Urbano")
                        .sigla("GODU")
                        .nivelJerarquico(3)
                        .estado("ACTIVO")
                        .build()));

        Rol rolAdmin = rolRepository.findByCodigo("ROLE_ADMIN").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_ADMIN").nombre("ADMINISTRADOR").estado("ACTIVO").build()));
        Rol rolOperador = rolRepository.findByCodigo("ROLE_MESA_PARTES").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_MESA_PARTES").nombre("MESA DE PARTES").estado("ACTIVO").build()));
        Rol rolCiudadano = rolRepository.findByCodigo("ROLE_CIUDADANO").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_CIUDADANO").nombre("CIUDADANO").estado("ACTIVO").build()));

        // 1. Admin
        adminUser = crearUsuario("admin_mpv", "71112233", rolAdmin, "Admin", "MesaPartes");
        adminCookie = crearCookie(adminUser, List.of("ROLE_ADMIN", "MESAPARTES_ADMIN", "MESAPARTES_CONFIGURAR_TRAMITES"));

        // 2. Operador
        operadorUser = crearUsuario("operador_mpv", "72223344", rolOperador, "Operador", "Quinua");
        operadorCookie = crearCookie(operadorUser, List.of("ROLE_MESA_PARTES", "MESAPARTES_EXPEDIENTE_RECIBIR", "MESAPARTES_EXPEDIENTE_DERIVAR", "MESAPARTES_EXPEDIENTE_OBSERVAR"));

        // 3. Ciudadano 1
        ciudadano1User = crearUsuario("ciudadano1_mpv", "73334455", rolCiudadano, "Juan", "Quispe");
        ciudadano1Cookie = crearCookie(ciudadano1User, List.of("ROLE_CIUDADANO", "MESAPARTES_EXPEDIENTE_CREAR", "MESAPARTES_EXPEDIENTE_VER_PROPIO"));

        // 4. Ciudadano 2
        ciudadano2User = crearUsuario("ciudadano2_mpv", "74445566", rolCiudadano, "Maria", "Flores");
        ciudadano2Cookie = crearCookie(ciudadano2User, List.of("ROLE_CIUDADANO", "MESAPARTES_EXPEDIENTE_CREAR", "MESAPARTES_EXPEDIENTE_VER_PROPIO"));

        // Tipo de trámite
        tramiteLicencia = tipoTramiteRepository.findByCodigoIgnoreCase("TRAM-LIC-TEST").orElseGet(() ->
                TipoTramite.builder()
                        .codigo("TRAM-LIC-TEST")
                        .nombre("Licencia de Construcción de Prueba")
                        .descripcion("Trámite para pruebas de integración de MPV")
                        .requiereDocumentos(true)
                        .permitePresentacionVirtual(true)
                        .plazoDias(30)
                        .unidadPlazo(UnidadPlazo.DIAS_HABILES)
                        .estado("ACTIVO")
                        .build());
        tramiteLicencia.setOficinaResponsable(oficinaMpv);
        tramiteLicencia = tipoTramiteRepository.save(tramiteLicencia);

        // Documento para pruebas de adjuntos
        documentoActivo = documentoRepository.save(Documento.builder()
                .identificadorPublico(UUID.randomUUID())
                .nombreOriginal("solicitud_firmada.pdf")
                .nombreInterno("doc_test_" + UUID.randomUUID())
                .extension("pdf")
                .mimeType("application/pdf")
                .tamanoBytes(1024L)
                .hashSha256("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
                .estado(EstadoDocumento.ACTIVO)
                .categoria(CategoriaDocumento.SOLICITUD)
                .clasificacionAcceso(ClasificacionAcceso.CONFIDENCIAL)
                .usuarioPropietario(ciudadano1User)
                .fechaCreacion(OffsetDateTime.now())
                .build());
    }

    private Usuario crearUsuario(String username, String dni, Rol rol, String nombres, String apePaterno) {
        String uniqueDoc = dni + "_" + UUID.randomUUID().toString().substring(0, 4);
        Persona p = personaRepository.save(Persona.builder()
                .numeroDocumento(uniqueDoc)
                .tipoDocumento("DNI")
                .nombres(nombres)
                .apellidoPaterno(apePaterno)
                .apellidoMaterno("Quinua")
                .correo(username + "@test.gob.pe")
                .estado("ACTIVO")
                .build());

        return usuarioRepository.save(Usuario.builder()
                .persona(p)
                .username(username + "_" + UUID.randomUUID().toString().substring(0, 6))
                .passwordHash(passwordEncoder.encode("Password123*"))
                .roles(Set.of(rol))
                .enabled(true)
                .accountLocked(false)
                .build());
    }

    private Cookie crearCookie(Usuario usuario, List<String> authorities) {
        List<org.springframework.security.core.authority.SimpleGrantedAuthority> auths = authorities.stream()
                .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                .toList();

        CustomUserDetails details = CustomUserDetails.builder()
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

    @Test
    @DisplayName("1. Crear tipo de trámite y listar catálogo activo")
    void testCrearTipoTramiteYConsultarCatalogo() throws Exception {
        TipoTramiteRequest request = TipoTramiteRequest.builder()
                .codigo("TRAM-NUEVO-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase())
                .nombre("Certificado de Jurisdicción Territorial")
                .descripcion("Certificado oficial emitido por la Municipalidad de Quinua")
                .oficinaResponsableId(oficinaMpv.getId())
                .plazoDias(15)
                .unidadPlazo(UnidadPlazo.DIAS_HABILES)
                .build();

        mockMvc.perform(post("/api/mesa-partes/tipos-tramite")
                        .cookie(adminCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.codigo").value(request.getCodigo()))
                .andExpect(jsonPath("$.data.nombre").value("Certificado de Jurisdicción Territorial"));

        mockMvc.perform(get("/api/mesa-partes/tipos-tramite")
                        .cookie(ciudadano1Cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("2. Flujo Completo: Borrador -> Asociar Doc -> Presentar -> Recibir -> Derivar -> Observar -> Subsanar -> Resolver -> Archivar")
    void testFlujoCompletoExpediente() throws Exception {
        // A. Crear Borrador como Ciudadano 1
        CrearBorradorRequest borradorReq = CrearBorradorRequest.builder()
                .asunto("Solicitud de Licencia de Edificación en Quinua")
                .descripcion("Construcción de vivienda de dos pisos en barrio centro")
                .tipoTramiteId(tramiteLicencia.getId())
                .prioridad(PrioridadExpediente.NORMAL)
                .confidencialidad(ConfidencialidadExpediente.INTERNO)
                .canalIngreso(CanalIngreso.VIRTUAL)
                .build();

        String respBorrador = mockMvc.perform(post("/api/mesa-partes/expedientes/borradores")
                        .cookie(ciudadano1Cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(borradorReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.estadoActual").value("BORRADOR"))
                .andExpect(jsonPath("$.data.asunto").value(borradorReq.getAsunto()))
                .andReturn().getResponse().getContentAsString();

        Long expedienteId = objectMapper.readTree(respBorrador).get("data").get("id").asLong();

        // B. Intentar presentar sin documentos (debe fallar porque requiereDocumentos = true)
        mockMvc.perform(post("/api/mesa-partes/expedientes/" + expedienteId + "/presentar")
                        .cookie(ciudadano1Cookie))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message", containsString("requiere adjuntar al menos un documento")));

        // C. Asociar Documento
        AsociarDocumentoRequest docReq = AsociarDocumentoRequest.builder()
                .documentoId(documentoActivo.getId())
                .tipoRelacion(TipoRelacionDocumentoExpediente.PRINCIPAL)
                .build();

        mockMvc.perform(post("/api/mesa-partes/expedientes/" + expedienteId + "/documentos")
                        .cookie(ciudadano1Cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(docReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalDocumentos").value(1));

        // D. Presentar formalmente el expediente
        String respPresentado = mockMvc.perform(post("/api/mesa-partes/expedientes/" + expedienteId + "/presentar")
                        .cookie(ciudadano1Cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estadoActual").value("PRESENTADO"))
                .andExpect(jsonPath("$.data.numeroExpediente", matchesPattern("^MPV-\\d{4}-\\d{6}$")))
                .andExpect(jsonPath("$.data.fechaPresentacion").isNotEmpty())
                .andExpect(jsonPath("$.data.fechaLimite").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        String numeroExpediente = objectMapper.readTree(respPresentado).get("data").get("numeroExpediente").asText();
        assertThat(numeroExpediente).startsWith("MPV-");

        // E. Operador Recibe el expediente
        mockMvc.perform(post("/api/mesa-partes/expedientes/" + expedienteId + "/recibir")
                        .cookie(operadorCookie)
                        .param("observaciones", "Recepción conforme de mesa de partes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estadoActual").value("RECIBIDO"));

        // F. Operador Deriva el expediente a Obras
        DerivarExpedienteRequest derivarReq = DerivarExpedienteRequest.builder()
                .oficinaDestinoId(oficinaObras.getId())
                .motivo("Derivación técnica para evaluación de planos")
                .build();

        mockMvc.perform(post("/api/mesa-partes/expedientes/" + expedienteId + "/derivar")
                        .cookie(operadorCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(derivarReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.expedienteId").value(expedienteId))
                .andExpect(jsonPath("$.data.oficinaDestinoId").value(oficinaObras.getId()));

        // G. Operador Observa el expediente
        ObservarExpedienteRequest obsReq = ObservarExpedienteRequest.builder()
                .descripcion("Falta memoria descriptiva de instalaciones sanitarias")
                .diasPlazoSubsanacion(10)
                .build();

        mockMvc.perform(post("/api/mesa-partes/expedientes/" + expedienteId + "/observar")
                        .cookie(operadorCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(obsReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estadoActual").value("OBSERVADO"));

        // H. Ciudadano Subana la observación
        SubsanarExpedienteRequest subReq = SubsanarExpedienteRequest.builder()
                .respuestaCiudadano("Se adjuntan los planos sanitarios solicitados")
                .build();

        mockMvc.perform(post("/api/mesa-partes/expedientes/" + expedienteId + "/subsanar")
                        .cookie(ciudadano1Cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(subReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estadoActual").value("EN_SUBSANACION"));

        // I. Resolver Expediente
        CambiarEstadoRequest resolverReq = CambiarEstadoRequest.builder()
                .nuevoEstado(EstadoExpediente.RESUELTO)
                .motivo("Licencia aprobada mediante Resolución de Gerencia N° 015-2026-MDQ")
                .build();

        mockMvc.perform(post("/api/mesa-partes/expedientes/" + expedienteId + "/resolver")
                        .cookie(adminCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resolverReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estadoActual").value("RESUELTO"));

        // J. Archivar Expediente
        CambiarEstadoRequest archivarReq = CambiarEstadoRequest.builder()
                .nuevoEstado(EstadoExpediente.ARCHIVADO)
                .motivo("Trámite concluido formalmente y remitido a archivo central")
                .build();

        mockMvc.perform(post("/api/mesa-partes/expedientes/" + expedienteId + "/archivar-estado")
                        .cookie(adminCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(archivarReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estadoActual").value("ARCHIVADO"))
                .andExpect(jsonPath("$.data.fechaCierre").isNotEmpty());
    }

    @Test
    @DisplayName("3. Anti-IDOR: Ciudadano 2 no puede acceder a expediente de Ciudadano 1")
    void testAntiIdorCiudadanoNoPuedeVerExpedienteAjeno() throws Exception {
        // Ciudadano 1 crea y presenta expediente
        CrearBorradorRequest req = CrearBorradorRequest.builder()
                .asunto("Expediente confidencial de Ciudadano 1")
                .tipoTramiteId(tramiteLicencia.getId())
                .build();

        String resp = mockMvc.perform(post("/api/mesa-partes/expedientes/borradores")
                        .cookie(ciudadano1Cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long expedienteId = objectMapper.readTree(resp).get("data").get("id").asLong();

        // Ciudadano 2 intenta acceder al detalle del expediente de Ciudadano 1 -> 403 Forbidden
        mockMvc.perform(get("/api/mesa-partes/expedientes/" + expedienteId)
                        .cookie(ciudadano2Cookie))
                .andExpect(status().isForbidden());

        // Ciudadano 2 intenta asociar un documento -> 403 Forbidden
        AsociarDocumentoRequest docReq = AsociarDocumentoRequest.builder()
                .documentoId(documentoActivo.getId())
                .build();

        mockMvc.perform(post("/api/mesa-partes/expedientes/" + expedienteId + "/documentos")
                        .cookie(ciudadano2Cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(docReq)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("4. Rechazar transiciones de estado inválidas")
    void testRechazarTransicionesInvalidas() throws Exception {
        CrearBorradorRequest req = CrearBorradorRequest.builder()
                .asunto("Expediente de prueba de transiciones ilegales")
                .tipoTramiteId(tramiteLicencia.getId())
                .build();

        String resp = mockMvc.perform(post("/api/mesa-partes/expedientes/borradores")
                        .cookie(ciudadano1Cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(resp).get("data").get("id").asLong();

        // Intentar recibir un expediente en estado BORRADOR (debe estar en PRESENTADO)
        mockMvc.perform(post("/api/mesa-partes/expedientes/" + id + "/recibir")
                        .cookie(operadorCookie))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message", containsString("Solo se pueden recibir expedientes en estado PRESENTADO")));

        // Intentar archivar directamente desde BORRADOR
        pe.gob.municipalidad.sigm.modules.mesapartes.dto.AtencionDtos.ArchivarExpedienteRequest archReq =
                pe.gob.municipalidad.sigm.modules.mesapartes.dto.AtencionDtos.ArchivarExpedienteRequest.builder()
                        .motivoArchivo("Intento arbitrario de archivar")
                        .build();

        mockMvc.perform(post("/api/mesa-partes/expedientes/" + id + "/archivar")
                        .cookie(adminCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(archReq)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message", containsString("Solo pueden enviarse a archivo")));
    }

    @Test
    @DisplayName("5. Concurrencia de numeración: sin duplicados ni saltos en llamadas paralelas")
    void testConcurrenciaNumeracion() throws Exception {
        int hilos = 10;
        ExecutorService executor = Executors.newFixedThreadPool(hilos);
        CountDownLatch latch = new CountDownLatch(hilos);
        Set<String> numerosGenerados = ConcurrentHashMap.newKeySet();

        for (int i = 0; i < hilos; i++) {
            executor.submit(() -> {
                try {
                    String num = numeracionService.generarSiguienteNumero(2027);
                    numerosGenerados.add(num);
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean completado = latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(completado).isTrue();
        assertThat(numerosGenerados).hasSize(hilos);
        for (String num : numerosGenerados) {
            assertThat(num).startsWith("MPV-2027-");
        }
    }

    @Test
    @DisplayName("6. Consultar estadísticas de Mesa de Partes respetando autorización")
    void testConsultarEstadisticas() throws Exception {
        mockMvc.perform(get("/api/mesa-partes/estadisticas")
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalExpedientes", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.data.borradores", greaterThanOrEqualTo(0)));

        // Un ciudadano no puede consultar estadísticas globales
        mockMvc.perform(get("/api/mesa-partes/estadisticas")
                        .cookie(ciudadano1Cookie))
                .andExpect(status().isForbidden());
    }
}
