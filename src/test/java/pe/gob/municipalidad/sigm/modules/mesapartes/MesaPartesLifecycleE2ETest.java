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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.constants.SecurityConstants;
import pe.gob.municipalidad.sigm.common.document.entity.CategoriaDocumento;
import pe.gob.municipalidad.sigm.common.document.entity.ClasificacionAcceso;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.document.entity.EstadoDocumento;
import pe.gob.municipalidad.sigm.common.document.repository.DocumentoRepository;
import pe.gob.municipalidad.sigm.common.model.*;
import pe.gob.municipalidad.sigm.common.repository.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.AtencionDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.DerivacionDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvSolicitudDtos.GuardarBorradorSolicitudRequest;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.*;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Suite End-to-End (E2E) y de Integración Completa de Mesa de Partes Virtual (Prompt 20).
 * Valida de manera exhaustiva el ciclo de vida documental administrativo institucional:
 * Ciudadano -> Presentación -> Recepción -> Revisión -> Derivación -> Asignación ->
 * Atención -> Decisión -> Notificación -> Cierre -> Archivo -> Seguimiento -> Reportes.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
@DisplayName("E2E y Validación Final de Mesa de Partes Virtual (Prompt 20)")
public class MesaPartesLifecycleE2ETest {

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
    private PermisoRepository permisoRepository;

    @Autowired
    private ModuloSistemaRepository moduloRepository;

    @Autowired
    private OficinaRepository oficinaRepository;

    @Autowired
    private MunicipalidadRepository municipalidadRepository;

    @Autowired
    private TipoTramiteRepository tipoTramiteRepository;

    @Autowired
    private RequisitoDocumentalRepository requisitoRepository;

    @Autowired
    private ExpedienteRepository expedienteRepository;

    @Autowired
    private DocumentoRepository documentoRepository;

    @Autowired
    private DerivacionExpedienteRepository derivacionRepository;

    @Autowired
    private PersonaOficinaRepository personaOficinaRepository;

    @Autowired
    private TipoActorRepository tipoActorRepository;

    private Usuario ciudadano;
    private Usuario operadorMpv;
    private Usuario jefeObras;
    private Usuario especialistaObras;
    private Usuario supervisorAdmin;

    private Cookie ciudadanoCookie;
    private Cookie operadorCookie;
    private Cookie jefeObrasCookie;
    private Cookie especialistaCookie;
    private Cookie adminCookie;

    private Oficina oficinaMpv;
    private Oficina oficinaObras;
    private TipoTramite tramitePublicado;
    private RequisitoDocumental requisitoObligatorio;
    private Documento documentoMock;

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        // 1. Municipalidad institucional de Quinua
        Municipalidad muni = municipalidadRepository.findAll().stream().findFirst().orElseGet(() ->
                municipalidadRepository.save(Municipalidad.builder()
                        .nombre("Municipalidad Distrital de Quinua")
                        .nombreCorto("MD Quinua")
                        .ubigeo("050108")
                        .departamento("Ayacucho")
                        .provincia("Huamanga")
                        .distrito("Quinua")
                        .direccion("Plaza de Armas s/n, Quinua, Ayacucho")
                        .telefono("066-812345")
                        .correoInstitucional("mesadepartes@muniquinua.gob.pe")
                        .build())
        );

        // 2. Oficinas municipales
        oficinaMpv = oficinaRepository.save(Oficina.builder()
                .codigo("OFI-MPV-" + suffix)
                .nombre("Mesa de Partes Virtual y Trámite Documentario")
                .sigla("MPV")
                .nivelJerarquico(1)
                .estado("ACTIVO")
                .municipalidad(muni)
                .build());

        oficinaObras = oficinaRepository.save(Oficina.builder()
                .codigo("OFI-OBRAS-" + suffix)
                .nombre("Subgerencia de Desarrollo Urbano y Obras Públicas")
                .sigla("SGDU")
                .nivelJerarquico(2)
                .estado("ACTIVO")
                .municipalidad(muni)
                .build());

        // 3. Roles y Permisos institucionales
        Rol rolCiudadano = obtenerOCrearRol("ROLE_CIUDADANO", "Ciudadano");
        Rol rolOperador = obtenerOCrearRol("ROLE_MESA_PARTES", "Operador Mesa Partes");
        Rol rolJefe = obtenerOCrearRol("ROLE_JEFE_OFICINA", "Jefe de Oficina");
        Rol rolEspecialista = obtenerOCrearRol("ROLE_OPERADOR", "Especialista Técnico");
        Rol rolAdmin = obtenerOCrearRol("ROLE_ADMIN", "Administrador Municipal");

        asegurarPermisos(rolCiudadano, List.of(
                "MESAPARTES_EXPEDIENTE_VER_PROPIO", "MESAPARTES_EXPEDIENTE_SUBSANAR"
        ));

        asegurarPermisos(rolOperador, List.of(
                "MESAPARTES_BANDEJA", "MESAPARTES_EXPEDIENTE_VER_TODOS",
                "MESAPARTES_EXPEDIENTE_RECIBIR", "MESAPARTES_EXPEDIENTE_OBSERVAR",
                "MESAPARTES_DERIVACION_CREAR", "MESAPARTES_EXPEDIENTE_PRIORIDAD"
        ));

        asegurarPermisos(rolJefe, List.of(
                "MESAPARTES_BANDEJA", "MESAPARTES_EXPEDIENTE_VER_TODOS",
                "MESAPARTES_DERIVACION_RECIBIR", "MESAPARTES_ASIGNACION_CREAR",
                "MESAPARTES_PROPUESTA_APROBAR", "MESAPARTES_DECISION_EMITIR",
                "MESAPARTES_DOCUMENTO_FORMALIZAR", "MESAPARTES_DOCUMENTO_GENERAR",
                "MESAPARTES_EXPEDIENTE_CERRAR", "MESAPARTES_EXPEDIENTE_ARCHIVAR"
        ));

        asegurarPermisos(rolEspecialista, List.of(
                "MESAPARTES_BANDEJA", "MESAPARTES_EXPEDIENTE_VER_TODOS",
                "MESAPARTES_ATENCION_INICIAR", "MESAPARTES_PROPUESTA_CREAR",
                "MESAPARTES_ACTUACION_CREAR", "MESAPARTES_ATENCION_VER"
        ));

        asegurarPermisos(rolAdmin, List.of(
                "MESAPARTES_CONFIG_VER", "MESAPARTES_REPORTES_VER",
                "MESAPARTES_REPORTES_EXPORTAR", "MESAPARTES_SUPERVISION_VER",
                "MESAPARTES_EXPEDIENTE_VER_TODOS"
        ));

        // 4. Personas y Usuarios de prueba
        ciudadano = crearUsuario("ciu_" + suffix, "Ciudadano", "Quinua", "vecino." + suffix + "@gmail.com", "45" + suffix, rolCiudadano);
        operadorMpv = crearUsuario("op_" + suffix, "Operador", "Mesa", "operador." + suffix + "@quinua.gob.pe", "70" + suffix, rolOperador);
        jefeObras = crearUsuario("jefe_" + suffix, "Jefe", "Obras", "jefe.obras." + suffix + "@quinua.gob.pe", "71" + suffix, rolJefe);
        especialistaObras = crearUsuario("esp_" + suffix, "Especialista", "Tecnico", "especialista." + suffix + "@quinua.gob.pe", "72" + suffix, rolEspecialista);
        supervisorAdmin = crearUsuario("adm_" + suffix, "Supervisor", "General", "admin." + suffix + "@quinua.gob.pe", "73" + suffix, rolAdmin);

        // 4.1. Asignaciones de Oficina Institucional (PersonaOficina)
        TipoActor tipoActor = tipoActorRepository.findAll().stream().findFirst().orElseGet(() ->
                tipoActorRepository.save(TipoActor.builder()
                        .codigo("ACTOR_FUNCIONAL")
                        .nombre("Servidor Municipal")
                        .descripcion("Servidor de planta o CAS")
                        .build())
        );

        personaOficinaRepository.save(PersonaOficina.builder()
                .persona(operadorMpv.getPersona())
                .oficina(oficinaMpv)
                .tipoActor(tipoActor)
                .fechaInicio(java.time.LocalDate.now())
                .principal(true)
                .estado("ACTIVO")
                .build());

        personaOficinaRepository.save(PersonaOficina.builder()
                .persona(jefeObras.getPersona())
                .oficina(oficinaObras)
                .tipoActor(tipoActor)
                .fechaInicio(java.time.LocalDate.now())
                .principal(true)
                .estado("ACTIVO")
                .build());

        personaOficinaRepository.save(PersonaOficina.builder()
                .persona(especialistaObras.getPersona())
                .oficina(oficinaObras)
                .tipoActor(tipoActor)
                .fechaInicio(java.time.LocalDate.now())
                .principal(true)
                .estado("ACTIVO")
                .build());

        // 5. Cookies JWT
        ciudadanoCookie = buildJwtCookie(ciudadano, "ROLE_CIUDADANO", List.of("MESAPARTES_EXPEDIENTE_VER_PROPIO", "MESAPARTES_EXPEDIENTE_SUBSANAR"));
        operadorCookie = buildJwtCookie(operadorMpv, "ROLE_MESA_PARTES", List.of("MESAPARTES_BANDEJA", "MESAPARTES_EXPEDIENTE_VER_TODOS", "MESAPARTES_EXPEDIENTE_RECIBIR", "MESAPARTES_EXPEDIENTE_OBSERVAR", "MESAPARTES_DERIVACION_CREAR"));
        jefeObrasCookie = buildJwtCookie(jefeObras, "ROLE_JEFE_OFICINA", List.of("MESAPARTES_BANDEJA", "MESAPARTES_EXPEDIENTE_VER_TODOS", "MESAPARTES_DERIVACION_RECIBIR", "MESAPARTES_ASIGNACION_CREAR", "MESAPARTES_PROPUESTA_APROBAR", "MESAPARTES_DECISION_EMITIR", "MESAPARTES_DOCUMENTO_FORMALIZAR", "MESAPARTES_DOCUMENTO_GENERAR", "MESAPARTES_EXPEDIENTE_CERRAR", "MESAPARTES_EXPEDIENTE_ARCHIVAR"));
        especialistaCookie = buildJwtCookie(especialistaObras, "ROLE_OPERADOR", List.of("MESAPARTES_BANDEJA", "MESAPARTES_EXPEDIENTE_VER_TODOS", "MESAPARTES_ATENCION_INICIAR", "MESAPARTES_PROPUESTA_CREAR", "MESAPARTES_ACTUACION_CREAR", "MESAPARTES_ATENCION_VER"));
        adminCookie = buildJwtCookie(supervisorAdmin, "ROLE_ADMIN", List.of("MESAPARTES_CONFIG_VER", "MESAPARTES_REPORTES_VER", "MESAPARTES_REPORTES_EXPORTAR", "MESAPARTES_SUPERVISION_VER", "MESAPARTES_EXPEDIENTE_VER_TODOS"));

        // 6. Trámite Oficial Publicado con Requisito Documental
        tramitePublicado = tipoTramiteRepository.save(TipoTramite.builder()
                .codigo("TR-E2E-" + suffix)
                .nombre("Licencia de Edificación y Conformidad de Obra " + suffix)
                .descripcion("Procedimiento administrativo formal para autorizar edificaciones en Quinua")
                .codigoTupa("TUPA-URB-" + suffix)
                .baseNormativa("Ordenanza Municipal N° 004-2025-MDQ y TUO Ley 27444")
                .plazoDias(30)
                .unidadPlazo(UnidadPlazo.DIAS_HABILES)
                .estado("ACTIVO")
                .estadoCatalogo(EstadoCatalogoTramite.PUBLICADO)
                .versionActual(1)
                .permitePresentacionVirtual(true)
                .permitePresencial(true)
                .requiereTasa(true)
                .montoTasa(new BigDecimal("85.50"))
                .oficinaResponsable(oficinaObras)
                .build());

        requisitoObligatorio = requisitoRepository.save(RequisitoDocumental.builder()
                .tipoTramite(tramitePublicado)
                .codigo("REQ-PLANO-" + suffix)
                .nombre("Plano de Arquitectura y Memoria Descriptiva")
                .descripcion("Archivo PDF firmado digital o físicamente")
                .obligatorio(true)
                .formatosPermitidos("application/pdf,image/jpeg")
                .tamanoMaximoMb(15)
                .orden(1)
                .estado("ACTIVO")
                .build());

        // 7. Documento inicial persistido para pruebas
        documentoMock = documentoRepository.save(Documento.builder()
                .identificadorPublico(UUID.randomUUID())
                .nombreOriginal("Plano_Arquitectura.pdf")
                .nombreInterno("doc_plano_" + suffix + ".pdf")
                .extension(".pdf")
                .mimeType("application/pdf")
                .tamanoBytes(1024L * 500)
                .hashSha256("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
                .categoria(CategoriaDocumento.SOLICITUD)
                .clasificacionAcceso(ClasificacionAcceso.PUBLICO)
                .estado(EstadoDocumento.ACTIVO)
                .versionActual(1)
                .usuarioPropietario(ciudadano)
                .oficinaResponsable(oficinaObras)
                .moduloOrigen("MESA_PARTES_VIRTUAL")
                .build());
    }

    private Rol obtenerOCrearRol(String codigo, String nombre) {
        return rolRepository.findByCodigo(codigo).orElseGet(() ->
                rolRepository.save(Rol.builder()
                        .codigo(codigo)
                        .nombre(nombre)
                        .descripcion(nombre)
                        .estado("ACTIVO")
                        .build())
        );
    }

    private void asegurarPermisos(Rol rol, List<String> codigosPermiso) {
        ModuloSistema mod = moduloRepository.findByCodigo("TRAMITE_DOCUMENTARIO").orElseGet(() ->
                moduloRepository.save(ModuloSistema.builder()
                        .codigo("TRAMITE_DOCUMENTARIO")
                        .nombre("Trámite Documentario y Mesa de Partes")
                        .descripcion("Trámite Documentario")
                        .estado("ACTIVO")
                        .orden(1)
                        .build())
        );

        for (String cod : codigosPermiso) {
            Permiso p = permisoRepository.findByCodigo(cod).orElseGet(() ->
                    permisoRepository.save(Permiso.builder()
                            .codigo(cod)
                            .nombre("Permiso " + cod)
                            .accion("VIEW")
                            .modulo(mod)
                            .estado("ACTIVO")
                            .descripcion("Permiso de prueba " + cod)
                            .build())
            );
            if (rol.getPermisos() == null) {
                rol.setPermisos(new HashSet<>());
            }
            rol.getPermisos().add(p);
        }
        rolRepository.save(rol);
    }

    private Usuario crearUsuario(String username, String nombres, String apePaterno, String correo, String dni, Rol rol) {
        Persona per = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento(dni)
                .nombres(nombres)
                .apellidoPaterno(apePaterno)
                .apellidoMaterno("Quinua")
                .correo(correo)
                .direccion("Quinua Centro, Ayacucho")
                .telefono("966000111")
                .estado("ACTIVO")
                .build());

        return usuarioRepository.save(Usuario.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode("Password2026!"))
                .persona(per)
                .roles(Set.of(rol))
                .enabled(true)
                .build());
    }

    private Cookie buildJwtCookie(Usuario usuario, String roleName, List<String> permissions) {
        List<SimpleGrantedAuthority> authoritiesList = new ArrayList<>();
        authoritiesList.add(new SimpleGrantedAuthority(roleName));
        for (String p : permissions) {
            authoritiesList.add(new SimpleGrantedAuthority(p));
        }

        CustomUserDetails userDetails = CustomUserDetails.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .personaId(usuario.getPersona().getId())
                .nombreCompleto(usuario.getPersona().getNombres() + " " + usuario.getPersona().getApellidoPaterno())
                .roles(Set.of(roleName))
                .permisos(new HashSet<>(permissions))
                .authorities(authoritiesList)
                .enabled(true)
                .accountNonLocked(true)
                .build();

        String token = jwtTokenProvider.generateAccessToken(userDetails);
        Cookie cookie = new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, token);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        return cookie;
    }

    // =========================================================================
    // ESCENARIO A: SOLICITUD CIUDADANA
    // =========================================================================
    @Test
    @DisplayName("Escenario A: Solicitud Ciudadana - Borrador, Presentación, Correlativo Único y Constancia PDF")
    void testEscenarioA_SolicitudCiudadana() throws Exception {
        // 1. Guardar Borrador
        GuardarBorradorSolicitudRequest borradorReq = GuardarBorradorSolicitudRequest.builder()
                .tipoTramiteId(tramitePublicado.getId())
                .asunto("Solicitud de Licencia de Construcción Vivienda Unifamiliar")
                .descripcion("Construcción de predio de 2 niveles en Jr. Sucre s/n, Quinua")
                .build();

        MvcResult resultBorrador = mockMvc.perform(post("/api/mesa-partes/expedientes/borradores")
                        .cookie(ciudadanoCookie)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(borradorReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.expediente.estadoActual").value("BORRADOR"))
                .andReturn();

        String respBorradorJson = resultBorrador.getResponse().getContentAsString();
        Long expedienteId = objectMapper.readTree(respBorradorJson).at("/data/expediente/id").asLong();
        String uuidStr = objectMapper.readTree(respBorradorJson).at("/data/expediente/uuid").asText();

        // 2. Asociar documento al borrador
        AsociarDocumentoRequest asocReq = AsociarDocumentoRequest.builder()
                .documentoId(documentoMock.getId())
                .tipoRelacion(TipoRelacionDocumentoExpediente.PRINCIPAL)
                .build();

        mockMvc.perform(post("/api/mesa-partes/expedientes/" + expedienteId + "/documentos")
                        .cookie(ciudadanoCookie)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(asocReq)))
                .andExpect(status().isOk());

        // 3. Presentar Solicitud formalmente
        PresentarExpedienteRequest presentarReq = PresentarExpedienteRequest.builder()
                .observaciones("Presentación con documentación conforme a TUPA")
                .build();

        MvcResult resultPresentar = mockMvc.perform(post("/api/mesa-partes/expedientes/" + expedienteId + "/presentar")
                        .cookie(ciudadanoCookie)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(presentarReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estadoActual").value("PRESENTADO"))
                .andExpect(jsonPath("$.data.numeroExpediente", notNullValue()))
                .andReturn();

        String numeroExpediente = objectMapper.readTree(resultPresentar.getResponse().getContentAsString()).at("/data/numeroExpediente").asText();
        assertThat(numeroExpediente).matches("^(EXP|MPV)-.*");

        // 4. Descargar Constancia de Registro PDF
        mockMvc.perform(get("/api/mesa-partes/expedientes/" + uuidStr + "/constancia")
                        .cookie(ciudadanoCookie))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", is("application/pdf")));

        // 5. Consultar 'Mis Solicitudes'
        mockMvc.perform(get("/api/mesa-partes/mis-solicitudes")
                        .cookie(ciudadanoCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data.content[0].numeroExpediente").value(numeroExpediente));
    }

    // =========================================================================
    // ESCENARIO B: RECEPCIÓN
    // =========================================================================
    @Test
    @DisplayName("Escenario B: Recepción Institucional - Bandeja, Revisión Conforme y Sello de Recepción")
    void testEscenarioB_RecepcionOperador() throws Exception {
        Long expId = crearYPresentarExpediente();

        // 1. Operador consulta la bandeja de entrada institucional
        mockMvc.perform(get("/api/mesa-partes/bandeja")
                        .cookie(operadorCookie)
                        .param("estado", "PRESENTADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", not(empty())));

        // 2. Operador revisa detalle y documentos
        mockMvc.perform(get("/api/mesa-partes/expedientes/" + expId)
                        .cookie(operadorCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(expId));

        // 3. Operador ejecuta Recepción Conforme
        mockMvc.perform(post("/api/mesa-partes/expedientes/" + expId + "/recibir")
                        .cookie(operadorCookie)
                        .with(csrf())
                        .param("observaciones", "Documentación física y digital verificada conforme a TUPA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estadoActual").value("RECIBIDO"))
                .andExpect(jsonPath("$.data.fechaRecepcion", notNullValue()));

        // 4. Descargar Constancia de Recepción PDF
        mockMvc.perform(get("/api/mesa-partes/expedientes/" + expId + "/constancia-recepcion")
                        .cookie(operadorCookie))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", is("application/pdf")));
    }

    // =========================================================================
    // ESCENARIO C: OBSERVACIÓN Y SUBSANACIÓN
    // =========================================================================
    @Test
    @DisplayName("Escenario C: Observación y Subsanación - Notificación de Reparo y Reanudación")
    void testEscenarioC_ObservacionYSubsanacion() throws Exception {
        Long expId = crearYPresentarExpediente();

        // 1. Operador genera observación con plazo de 10 días
        ObservarExpedienteRequest obsReq = ObservarExpedienteRequest.builder()
                .descripcion("Falta firma digital en la memoria descriptiva y comprobante de pago de tasa")
                .diasPlazoSubsanacion(10)
                .build();

        mockMvc.perform(post("/api/mesa-partes/expedientes/" + expId + "/observar")
                        .cookie(operadorCookie)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(obsReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estadoActual").value("OBSERVADO"));

        // 2. Ciudadano consulta su expediente observado
        mockMvc.perform(get("/api/mesa-partes/expedientes/" + expId)
                        .cookie(ciudadanoCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estadoActual").value("OBSERVADO"));

        // 3. Ciudadano subsana la observación adjuntando documento rectificado
        SubsanarExpedienteRequest subReq = SubsanarExpedienteRequest.builder()
                .documentoIds(List.of(documentoMock.getId()))
                .respuestaCiudadano("Se adjunta memoria descriptiva firmada y comprobante BN N° 45892")
                .build();

        mockMvc.perform(post("/api/mesa-partes/expedientes/" + expId + "/subsanar")
                        .cookie(ciudadanoCookie)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(subReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estadoActual").value("EN_SUBSANACION"));
    }

    // =========================================================================
    // ESCENARIO D: DERIVACIÓN Y ASIGNACIÓN
    // =========================================================================
    @Test
    @DisplayName("Escenario D: Derivación y Asignación - Pase Inter-oficinas y Asignación de Especialista")
    void testEscenarioD_DerivacionYAsignacion() throws Exception {
        Long expId = crearYRecibirExpediente();
        Expediente exp = expedienteRepository.findById(expId).orElseThrow();
        exp.setOficinaResponsable(oficinaMpv);
        expedienteRepository.save(exp);

        // 1. Mesa de Partes deriva a la Subgerencia de Desarrollo Urbano
        pe.gob.municipalidad.sigm.modules.mesapartes.dto.DerivacionDtos.DerivarExpedienteRequest derivarReq =
                pe.gob.municipalidad.sigm.modules.mesapartes.dto.DerivacionDtos.DerivarExpedienteRequest.builder()
                        .oficinaDestinoId(oficinaObras.getId())
                        .motivo("Remisión técnica para emisión de informe de zonificación y compatibilidad")
                        .indicaciones("Prioridad media, verificar alineamiento vial")
                        .prioridad(PrioridadExpediente.NORMAL)
                        .build();

        MvcResult resultDeriv = mockMvc.perform(post("/api/mesa-partes/expedientes/" + expId + "/derivar")
                        .cookie(operadorCookie)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(derivarReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.oficinaDestinoId").value(oficinaObras.getId()))
                .andReturn();

        Long derivacionId = objectMapper.readTree(resultDeriv.getResponse().getContentAsString()).at("/data/id").asLong();

        // 2. Subgerencia de Obras recibe la derivación
        mockMvc.perform(post("/api/mesa-partes/derivaciones/" + derivacionId + "/recibir")
                        .cookie(jefeObrasCookie)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estado").value("RECIBIDA"));

        // 3. Jefe de Obras asigna expediente al Especialista Técnico
        AsignarExpedienteRequest asignReq = AsignarExpedienteRequest.builder()
                .usuarioAsignadoId(especialistaObras.getId())
                .motivo("Asignación de evaluación técnica de planos")
                .indicaciones("Inspección de campo si se requiere")
                .build();

        mockMvc.perform(post("/api/mesa-partes/expedientes/" + expId + "/asignar")
                        .cookie(jefeObrasCookie)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(asignReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.usuarioAsignadoId").value(especialistaObras.getId()));

        // 4. Especialista registra actuación administrativa
        RegistrarActuacionRequest actReq = RegistrarActuacionRequest.builder()
                .tipo(TipoActuacionExpediente.EVALUACION)
                .descripcion("Inspección ocular realizada en el predio. Terreno sin afectación a vía pública.")
                .visibilidad(VisibilidadObservacion.INTERNA)
                .build();

        mockMvc.perform(post("/api/mesa-partes/expedientes/" + expId + "/actuaciones")
                        .cookie(especialistaCookie)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.tipo").value("EVALUACION"));
    }

    // =========================================================================
    // ESCENARIO E: RESOLUCIÓN Y DECISIÓN ADMINISTRATIVA
    // =========================================================================
    @Test
    @DisplayName("Escenario E: Atención, Propuesta Técnica y Formalización de Resolución Administrativa")
    void testEscenarioE_AtencionPropuestaYResolucion() throws Exception {
        Long expId = crearYRecibirExpediente();

        // 1. Especialista inicia la atención formal
        mockMvc.perform(post("/api/mesa-partes/expedientes/" + expId + "/iniciar-atencion")
                        .cookie(especialistaCookie)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(IniciarAtencionRequest.builder().indicaciones("Inicio de evaluación formal").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estado").value("EN_ATENCION"));

        // 2. Especialista formula Propuesta de Decisión
        CrearPropuestaRequest propReq = CrearPropuestaRequest.builder()
                .tipoDecision(TipoDecisionExpediente.RESOLUCION_SUBGERENCIAL)
                .resultadoPropuesto(ResultadoDecision.FAVORABLE)
                .fundamento("El proyecto cumple con los parámetros urbanísticos del distrito de Quinua")
                .descripcion("Emitir Resolución Subgerencial de Licencia de Edificación favorable")
                .build();

        MvcResult resultProp = mockMvc.perform(post("/api/mesa-partes/expedientes/" + expId + "/propuestas")
                        .cookie(especialistaCookie)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(propReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.resultadoPropuesto").value("FAVORABLE"))
                .andReturn();

        Long propuestaId = objectMapper.readTree(resultProp.getResponse().getContentAsString()).at("/data/id").asLong();

        // 3. Especialista eleva a revisión
        mockMvc.perform(post("/api/mesa-partes/propuestas/" + propuestaId + "/enviar-revision")
                        .cookie(especialistaCookie)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estado").value("EN_REVISION"));

        // 4. Jefe de Oficina aprueba internamente la propuesta técnica
        RevisarPropuestaRequest revReq = RevisarPropuestaRequest.builder()
                .accion(AccionRevisionPropuesta.APROBADA_INTERNAMENTE)
                .observaciones("Conforme con el informe técnico de zonificación y verificación física")
                .build();

        mockMvc.perform(post("/api/mesa-partes/propuestas/" + propuestaId + "/aprobar")
                        .cookie(jefeObrasCookie)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(revReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estado").value("APROBADA_INTERNAMENTE"));

        // 5. Funcionario emite decisión administrativa formal
        RegistrarDecisionRequest decReq = RegistrarDecisionRequest.builder()
                .propuestaId(propuestaId)
                .tipoDecision(TipoDecisionExpediente.RESOLUCION_SUBGERENCIAL)
                .resultado(ResultadoDecision.FAVORABLE)
                .fundamento("TUO Ley 27444 y Ley 29090 de Regulación de Habilitaciones Urbanas y Edificaciones")
                .numeroDocumento("RESOLUCION SUBGERENCIAL N° 045-2026-MDQ/SGDU")
                .build();

        MvcResult resultDec = mockMvc.perform(post("/api/mesa-partes/expedientes/" + expId + "/decisiones")
                        .cookie(jefeObrasCookie)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(decReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.resultado").value("FAVORABLE"))
                .andReturn();

        Long decisionId = objectMapper.readTree(resultDec.getResponse().getContentAsString()).at("/data/id").asLong();

        // 6. Generar documento PDF de la resolución
        mockMvc.perform(post("/api/mesa-partes/decisiones/" + decisionId + "/generar-documento")
                        .cookie(jefeObrasCookie)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.documentoId", notNullValue()));

        // 7. Formalizar emisión del acto
        FormalizarDecisionRequest formReq = FormalizarDecisionRequest.builder()
                .numeroDocumento("RESOLUCION SUBGERENCIAL N° 045-2026-MDQ/SGDU")
                .evidenciaFormalizacion("Firma autorizada y registrada en libro oficial de resoluciones")
                .build();

        mockMvc.perform(post("/api/mesa-partes/decisiones/" + decisionId + "/registrar-emision")
                        .cookie(jefeObrasCookie)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(formReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estadoEmision").value("EMITIDO"));
    }

    // =========================================================================
    // ESCENARIO F: NOTIFICACIÓN, CIERRE Y ARCHIVO
    // =========================================================================
    @Test
    @DisplayName("Escenario F: Notificación al Administrado, Cierre Administrativo y Archivo Central")
    void testEscenarioF_NotificacionCierreYArchivo() throws Exception {
        Long expId = crearYRecibirExpediente();

        // 1. Notificación formal del resultado
        RegistrarNotificacionRequest notReq = RegistrarNotificacionRequest.builder()
                .destinatarioNombre("Juan Quispe Huamán")
                .destinatarioEmail("vecino.quinua@gmail.com")
                .destinatarioDireccion("Jr. Sucre s/n, Quinua Centro")
                .medioNotificacion(MedioNotificacion.CASILLA_ELECTRONICA)
                .observaciones("Notificación de acto resolutivo favorable")
                .build();

        mockMvc.perform(post("/api/mesa-partes/expedientes/" + expId + "/notificaciones")
                        .cookie(jefeObrasCookie)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.destinatarioNombre").value("Juan Quispe Huamán"));

        // 2. Transición a RESUELTO para habilitar el cierre formal por atención concluida (LPAG)
        Expediente expResuelto = expedienteRepository.findById(expId).orElseThrow();
        expResuelto.setEstadoActual(EstadoExpediente.RESUELTO);
        expedienteRepository.save(expResuelto);

        // Cierre administrativo
        CerrarExpedienteRequest cierreReq = CerrarExpedienteRequest.builder()
                .tipoCierre(TipoCierreExpediente.ATENCION_CONCLUIDA)
                .motivoCierre("Procedimiento administrativo formalmente concluido con emisión de licencia conforme a ley")
                .build();

        mockMvc.perform(post("/api/mesa-partes/expedientes/" + expId + "/cerrar")
                        .cookie(jefeObrasCookie)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cierreReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tipoCierre").value("ATENCION_CONCLUIDA"));

        // 3. Remisión a Archivo Central
        ArchivarExpedienteRequest archReq = ArchivarExpedienteRequest.builder()
                .motivoArchivo("Expediente resuelto y notificado. Custodia permanente en archivo municipal.")
                .codigoArchivador("ARCH-2026-SGDU-B2")
                .ubicacionFisica("Estante 3, Fila B, Casillero 12")
                .tomoFolio("Tomo I, Folios 1-45")
                .build();

        mockMvc.perform(post("/api/mesa-partes/expedientes/" + expId + "/archivar")
                        .cookie(jefeObrasCookie)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(archReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.codigoArchivador").value("ARCH-2026-SGDU-B2"));
    }

    // =========================================================================
    // ESCENARIO G: SEGUIMIENTO CIUDADANO Y VERIFICACIÓN QR
    // =========================================================================
    @Test
    @DisplayName("Escenario G: Seguimiento Ciudadano, Trazabilidad Cronológica y Verificación QR Anti-IDOR")
    void testEscenarioG_SeguimientoCiudadanoYVerificacionQR() throws Exception {
        Long expId = crearYPresentarExpediente();
        Expediente exp = expedienteRepository.findById(expId).orElseThrow();

        // 1. Seguimiento ciudadano de ficha integral
        mockMvc.perform(get("/api/mesa-partes/mis-solicitudes/" + expId + "/seguimiento")
                        .cookie(ciudadanoCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.numeroExpediente").value(exp.getNumeroExpediente()))
                .andExpect(jsonPath("$.data.estado").value("PRESENTADO"));

        // 2. Línea de tiempo ciudadana
        mockMvc.perform(get("/api/mesa-partes/mis-solicitudes/" + expId + "/historial")
                        .cookie(ciudadanoCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", not(empty())));

        // 3. Consulta pública general de verificación
        mockMvc.perform(get("/verificar-documento"))
                .andExpect(status().isOk())
                .andExpect(view().name("mesa-partes/verificar-documento"));

        // 4. Consulta de verificación documental pública
        mockMvc.perform(get("/api/publico/documentos/verificar")
                        .param("codigo", "DOC-NO-EXISTE-999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.valido").value(false));
    }

    // =========================================================================
    // ESCENARIO H: REPORTES Y SUPERVISIÓN (EXCEL Y PDF)
    // =========================================================================
    @Test
    @DisplayName("Escenario H: Reportes de Supervisión Municipal, Indicadores de Ley y Exportación Excel/PDF")
    void testEscenarioH_ReportesYSupervision() throws Exception {
        crearYRecibirExpediente();

        // 1. Resumen de estados y KPIs
        mockMvc.perform(get("/api/mesa-partes/reportes/resumen")
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalPresentados", greaterThanOrEqualTo(0)));

        // 2. Indicadores de plazos conforme al TUO Ley 27444
        mockMvc.perform(get("/api/mesa-partes/reportes/plazos")
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalConPlazo", notNullValue()));

        // 3. Exportación oficial a Excel XLSX con Apache POI
        mockMvc.perform(get("/api/mesa-partes/reportes/exportar/excel")
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("spreadsheetml")));

        // 4. Exportación ejecutiva a PDF apaisado con OpenPDF
        mockMvc.perform(get("/api/mesa-partes/reportes/exportar/pdf")
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", is("application/pdf")));
    }

    // =========================================================================
    // MÉTODOS AUXILIARES DE SOPORTE E2E
    // =========================================================================
    private Long crearYPresentarExpediente() throws Exception {
        GuardarBorradorSolicitudRequest borradorReq = GuardarBorradorSolicitudRequest.builder()
                .tipoTramiteId(tramitePublicado.getId())
                .asunto("Expediente E2E - Solicitud de Obras")
                .descripcion("Descripción técnica del proyecto")
                .build();

        MvcResult resBorrador = mockMvc.perform(post("/api/mesa-partes/expedientes/borradores")
                        .cookie(ciudadanoCookie)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(borradorReq)))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readTree(resBorrador.getResponse().getContentAsString()).at("/data/expediente/id").asLong();

        // Asociar documento obligatorio
        AsociarDocumentoRequest asocReq = AsociarDocumentoRequest.builder()
                .documentoId(documentoMock.getId())
                .tipoRelacion(TipoRelacionDocumentoExpediente.PRINCIPAL)
                .build();

        mockMvc.perform(post("/api/mesa-partes/expedientes/" + id + "/documentos")
                        .cookie(ciudadanoCookie)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(asocReq)))
                .andExpect(status().isOk());

        PresentarExpedienteRequest presReq = PresentarExpedienteRequest.builder()
                .observaciones("Presentación de prueba E2E")
                .build();

        mockMvc.perform(post("/api/mesa-partes/expedientes/" + id + "/presentar")
                        .cookie(ciudadanoCookie)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(presReq)))
                .andExpect(status().isOk());

        return id;
    }

    private Long crearYRecibirExpediente() throws Exception {
        Long id = crearYPresentarExpediente();

        mockMvc.perform(post("/api/mesa-partes/expedientes/" + id + "/recibir")
                        .cookie(operadorCookie)
                        .with(csrf())
                        .param("observaciones", "Recepción conforme E2E"))
                .andExpect(status().isOk());

        return id;
    }
}
