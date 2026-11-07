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
import pe.gob.municipalidad.sigm.common.model.*;
import pe.gob.municipalidad.sigm.common.repository.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.ConfiguracionMpvDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.*;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
@DisplayName("Pruebas de Integración - Configuración Administrativa y Catálogo de Trámites (Prompt 19)")
class ConfiguracionMpvIntegrationTest {

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
    private MunicipalidadRepository municipalidadRepository;

    @Autowired
    private OficinaRepository oficinaRepository;

    @Autowired
    private TipoTramiteRepository tipoTramiteRepository;

    @Autowired
    private TramiteVersionRepository tramiteVersionRepository;

    @Autowired
    private RequisitoDocumentalRepository requisitoRepository;

    @Autowired
    private FormularioTramiteRepository formularioRepository;

    @Autowired
    private CalendarioAdministrativoRepository calendarioRepository;

    @Autowired
    private PlantillaDocumentalRepository plantillaRepository;

    private Usuario usuarioAdmin;
    private Usuario usuarioCiudadano;
    private Cookie adminCookie;
    private Cookie ciudadanoCookie;
    private Oficina oficinaTest;
    private String uniqueSuffix;

    @BeforeEach
    void setUp() {
        uniqueSuffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();

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

        oficinaTest = oficinaRepository.save(Oficina.builder()
                .codigo("OFI-CONF-" + uniqueSuffix)
                .nombre("Oficina de Desarrollo Urbano " + uniqueSuffix)
                .sigla("ODU")
                .nivelJerarquico(1)
                .estado("ACTIVO")
                .municipalidad(muni)
                .build());

        Rol rolAdmin = rolRepository.findByCodigo("ROLE_ADMIN").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_ADMIN").nombre("Administrador").estado("ACTIVO").build())
        );
        Rol rolCiudadano = rolRepository.findByCodigo("ROLE_CIUDADANO").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_CIUDADANO").nombre("Ciudadano").estado("ACTIVO").build())
        );

        Persona personaAdmin = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("70" + String.format("%06d", new Random().nextInt(999999)))
                .nombres("Carlos")
                .apellidoPaterno("Admin")
                .correo("admin." + uniqueSuffix + "@muniquinua.gob.pe")
                .build());

        usuarioAdmin = usuarioRepository.save(Usuario.builder()
                .username("admin_cfg_" + uniqueSuffix)
                .passwordHash(passwordEncoder.encode("Password2026!"))
                .persona(personaAdmin)
                .roles(Set.of(rolAdmin))
                .enabled(true)
                .build());

        Persona personaCiu = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("71" + String.format("%06d", new Random().nextInt(999999)))
                .nombres("Manuel")
                .apellidoPaterno("Vecino")
                .correo("vecino." + uniqueSuffix + "@gmail.com")
                .build());

        usuarioCiudadano = usuarioRepository.save(Usuario.builder()
                .username("ciu_cfg_" + uniqueSuffix)
                .passwordHash(passwordEncoder.encode("Password2026!"))
                .persona(personaCiu)
                .roles(Set.of(rolCiudadano))
                .enabled(true)
                .build());

        adminCookie = buildJwtCookie(usuarioAdmin, "ROLE_ADMIN", List.of(
                "MESAPARTES_CONFIG_VER",
                "MESAPARTES_TRAMITE_CREAR",
                "MESAPARTES_TRAMITE_EDITAR",
                "MESAPARTES_TRAMITE_REVISAR",
                "MESAPARTES_TRAMITE_APROBAR",
                "MESAPARTES_TRAMITE_PUBLICAR",
                "MESAPARTES_TRAMITE_SUSPENDER",
                "MESAPARTES_REQUISITO_GESTIONAR",
                "MESAPARTES_FORMULARIO_GESTIONAR",
                "MESAPARTES_FORMULARIO_PUBLICAR",
                "MESAPARTES_PLAZO_GESTIONAR",
                "MESAPARTES_CALENDARIO_GESTIONAR",
                "MESAPARTES_NUMERACION_CONFIGURAR",
                "MESAPARTES_PLANTILLA_GESTIONAR",
                "MESAPARTES_CONFIG_HISTORIAL_VER"
        ));

        ciudadanoCookie = buildJwtCookie(usuarioCiudadano, "ROLE_CIUDADANO", List.of());
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

    // =========================================================================
    // 1. PRUEBAS DE VISTAS WEB THYMELEAF
    // =========================================================================

    @Test
    @DisplayName("Debe redireccionar /dashboard/mesa-partes/configuracion hacia /tramites")
    void testRedireccionConfiguracion() throws Exception {
        mockMvc.perform(get("/dashboard/mesa-partes/configuracion")
                        .cookie(adminCookie))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard/mesa-partes/configuracion/tramites"));
    }

    @Test
    @DisplayName("Debe renderizar la vista de catálogo de trámites")
    void testRenderizarVistaTramites() throws Exception {
        mockMvc.perform(get("/dashboard/mesa-partes/configuracion/tramites")
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/mesa-partes/configuracion/tramites"))
                .andExpect(model().attributeExists("tramitesPage", "oficinas", "estadosCatalogo", "pestanaActiva"));
    }

    @Test
    @DisplayName("Debe renderizar la vista de requisitos documentales")
    void testRenderizarVistaRequisitos() throws Exception {
        mockMvc.perform(get("/dashboard/mesa-partes/configuracion/requisitos")
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/mesa-partes/configuracion/requisitos"))
                .andExpect(model().attributeExists("tramites", "pestanaActiva"));
    }

    @Test
    @DisplayName("Debe renderizar la vista de constructor de formularios")
    void testRenderizarVistaFormularios() throws Exception {
        mockMvc.perform(get("/dashboard/mesa-partes/configuracion/formularios")
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/mesa-partes/configuracion/formularios"))
                .andExpect(model().attributeExists("tramites", "tiposCampo", "pestanaActiva"));
    }

    @Test
    @DisplayName("Debe renderizar la vista de plazos administrativos")
    void testRenderizarVistaPlazos() throws Exception {
        mockMvc.perform(get("/dashboard/mesa-partes/configuracion/plazos")
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/mesa-partes/configuracion/plazos"))
                .andExpect(model().attributeExists("plazos", "unidadesPlazo"));
    }

    @Test
    @DisplayName("Debe renderizar la vista de calendario administrativo")
    void testRenderizarVistaCalendario() throws Exception {
        mockMvc.perform(get("/dashboard/mesa-partes/configuracion/calendario")
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/mesa-partes/configuracion/calendario"))
                .andExpect(model().attributeExists("feriados", "anio"));
    }

    @Test
    @DisplayName("Debe renderizar la vista de numeración y correlativos")
    void testRenderizarVistaNumeracion() throws Exception {
        mockMvc.perform(get("/dashboard/mesa-partes/configuracion/numeracion")
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/mesa-partes/configuracion/numeracion"))
                .andExpect(model().attributeExists("correlativos"));
    }

    @Test
    @DisplayName("Debe renderizar la vista de plantillas documentales")
    void testRenderizarVistaPlantillas() throws Exception {
        mockMvc.perform(get("/dashboard/mesa-partes/configuracion/plantillas")
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/mesa-partes/configuracion/plantillas"))
                .andExpect(model().attributeExists("plantillas", "tiposDocumentales"));
    }

    @Test
    @DisplayName("Debe renderizar la vista de historial de auditoría")
    void testRenderizarVistaHistorial() throws Exception {
        mockMvc.perform(get("/dashboard/mesa-partes/configuracion/historial")
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/mesa-partes/configuracion/historial"))
                .andExpect(model().attributeExists("historialPage"));
    }

    // =========================================================================
    // 2. PRUEBAS DE ENDPOINTS REST Y SEGURIDAD
    // =========================================================================

    @Test
    @DisplayName("Seguridad: Ciudadano no autorizado debe recibir 403 Forbidden al intentar acceder a la configuración")
    void testCiudadanoProhibidoEnConfiguracion() throws Exception {
        mockMvc.perform(get("/api/mesa-partes/configuracion/tramites")
                        .cookie(ciudadanoCookie))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("REST: Ciclo completo de creación, edición, revisión, aprobación y publicación de trámite")
    void testCicloCompletoTramiteRest() throws Exception {
        String cod = "TR-IT-" + uniqueSuffix;

        GuardarTramiteRequest nuevoReq = GuardarTramiteRequest.builder()
                .codigo(cod)
                .nombre("Autorización de Obras Menores " + uniqueSuffix)
                .codigoTupa("TUPA-URB-" + uniqueSuffix)
                .denominacionOficial("Autorización Municipal de Obras Menores en Quinua")
                .oficinaResponsableId(oficinaTest.getId())
                .plazoDias(20)
                .unidadPlazo(UnidadPlazo.DIAS_HABILES)
                .categoria("OBRAS")
                .requiereTasa(true)
                .montoTasa(new BigDecimal("45.00"))
                .requiereDocumentos(false)
                .build();

        // 1. Crear Borrador
        MvcResult resCreate = mockMvc.perform(post("/api/mesa-partes/configuracion/tramites")
                        .cookie(adminCookie)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.codigo").value(cod))
                .andExpect(jsonPath("$.data.estadoCatalogo").value("BORRADOR"))
                .andReturn();

        String bodyCreate = resCreate.getResponse().getContentAsString();
        Long tramiteId = objectMapper.readTree(bodyCreate).path("data").path("id").asLong();

        // 2. Editar Borrador
        nuevoReq.setNombre("Autorización de Obras Menores y Refacciones " + uniqueSuffix);
        mockMvc.perform(put("/api/mesa-partes/configuracion/tramites/" + tramiteId)
                        .cookie(adminCookie)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nombre").value("Autorización de Obras Menores y Refacciones " + uniqueSuffix));

        // 3. Enviar a Revisión
        mockMvc.perform(post("/api/mesa-partes/configuracion/tramites/" + tramiteId + "/enviar-revision")
                        .cookie(adminCookie)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AccionTransicionRequest("Enviado para revisión técnica"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estadoCatalogo").value("EN_REVISION"));

        // 4. Aprobar
        mockMvc.perform(post("/api/mesa-partes/configuracion/tramites/" + tramiteId + "/aprobar")
                        .cookie(adminCookie)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AccionTransicionRequest("Conforme a normas urbanas"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estadoCatalogo").value("APROBADO"));

        // 5. Publicar Oficialmente (Crea versión inmutable)
        mockMvc.perform(post("/api/mesa-partes/configuracion/tramites/" + tramiteId + "/publicar")
                        .cookie(adminCookie)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AccionTransicionRequest("Publicación institucional"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estadoCatalogo").value("PUBLICADO"))
                .andExpect(jsonPath("$.data.versionActual").value(1));

        // 6. Consultar Versiones Inmutables
        mockMvc.perform(get("/api/mesa-partes/configuracion/tramites/" + tramiteId + "/versiones")
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].numeroVersion").value(1))
                .andExpect(jsonPath("$.data[0].estado").value("PUBLICADO"));

        // 7. Suspender
        mockMvc.perform(post("/api/mesa-partes/configuracion/tramites/" + tramiteId + "/suspender")
                        .cookie(adminCookie)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AccionTransicionRequest("Pausa por actualización"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estadoCatalogo").value("SUSPENDIDO"));
    }

    @Test
    @DisplayName("REST: Gestión de Requisitos Documentales por Trámite")
    void testGestionRequisitosRest() throws Exception {
        TipoTramite tr = tipoTramiteRepository.save(TipoTramite.builder()
                .codigo("TR-REQ-" + uniqueSuffix)
                .nombre("Trámite para Requisitos " + uniqueSuffix)
                .oficinaResponsable(oficinaTest)
                .plazoDias(15)
                .estado("ACTIVO")
                .build());

        GuardarRequisitoRequest req = GuardarRequisitoRequest.builder()
                .tipoTramiteId(tr.getId())
                .codigo("REQ-MEM-" + uniqueSuffix)
                .nombre("Memoria Descriptiva Firmada")
                .descripcion("Documento técnico visado por ingeniero")
                .orden(1)
                .tamanoMaximoMb(15)
                .formatosPermitidos("application/pdf")
                .obligatorio(true)
                .build();

        mockMvc.perform(post("/api/mesa-partes/configuracion/requisitos")
                        .cookie(adminCookie)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.codigo").value("REQ-MEM-" + uniqueSuffix))
                .andExpect(jsonPath("$.data.obligatorio").value(true));

        mockMvc.perform(get("/api/mesa-partes/configuracion/requisitos/tramite/" + tr.getId())
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].codigo").value("REQ-MEM-" + uniqueSuffix));
    }

    @Test
    @DisplayName("REST: Constructor de Formularios Dinámicos y Previsualización")
    void testFormulariosYPrevisualizacionRest() throws Exception {
        TipoTramite tr = tipoTramiteRepository.save(TipoTramite.builder()
                .codigo("TR-FRM-" + uniqueSuffix)
                .nombre("Trámite para Formulario " + uniqueSuffix)
                .oficinaResponsable(oficinaTest)
                .plazoDias(15)
                .estado("ACTIVO")
                .build());

        GuardarFormularioRequest fReq = GuardarFormularioRequest.builder()
                .tipoTramiteId(tr.getId())
                .codigo("FORM-CFG-" + uniqueSuffix)
                .nombre("Formulario de Inspección Técnica")
                .descripcion("Campos para inspección")
                .campos(List.of(
                        GuardarCampoRequest.builder()
                                .nombreCampo("numero_lote")
                                .etiqueta("Número de Lote o Manzana")
                                .tipoCampo(TipoCampoFormulario.TEXTO)
                                .obligatorio(true)
                                .orden(1)
                                .build(),
                        GuardarCampoRequest.builder()
                                .nombreCampo("tipo_inmueble")
                                .etiqueta("Tipo de Inmueble")
                                .tipoCampo(TipoCampoFormulario.SELECCION)
                                .obligatorio(false)
                                .orden(2)
                                .opciones(List.of(
                                        GuardarOpcionRequest.builder().valor("VIV").etiqueta("Vivienda").orden(1).build(),
                                        GuardarOpcionRequest.builder().valor("COM").etiqueta("Comercial").orden(2).build()
                                ))
                                .build()
                ))
                .build();

        // 1. Guardar Formulario y Campos
        MvcResult resF = mockMvc.perform(post("/api/mesa-partes/configuracion/formularios")
                        .cookie(adminCookie)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.codigo").value("FORM-CFG-" + uniqueSuffix))
                .andExpect(jsonPath("$.data.totalCampos").value(2))
                .andReturn();

        Long formId = objectMapper.readTree(resF.getResponse().getContentAsString()).path("data").path("id").asLong();

        // 2. Previsualizar Formulario
        mockMvc.perform(get("/api/mesa-partes/configuracion/formularios/" + formId + "/preview")
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nombre").value(org.hamcrest.Matchers.containsString("[PREVISUALIZACIÓN]")))
                .andExpect(jsonPath("$.data.formulario.campos").isArray())
                .andExpect(jsonPath("$.data.formulario.campos[0].nombreCampo").value("numero_lote"));

        // 3. Publicar Versión Inmutable
        mockMvc.perform(post("/api/mesa-partes/configuracion/formularios/" + formId + "/publicar")
                        .cookie(adminCookie)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estadoVersion").value("PUBLICADO"));
    }

    @Test
    @DisplayName("REST: Configuración de Plazos y Calendario de Feriados")
    void testPlazosYCalendarioRest() throws Exception {
        TipoTramite tr = tipoTramiteRepository.save(TipoTramite.builder()
                .codigo("TR-PLZ-" + uniqueSuffix)
                .nombre("Trámite para Plazos " + uniqueSuffix)
                .oficinaResponsable(oficinaTest)
                .plazoDias(30)
                .estado("ACTIVO")
                .build());

        // Actualizar Plazo
        mockMvc.perform(put("/api/mesa-partes/configuracion/plazos/" + tr.getId())
                        .cookie(adminCookie)
                        .with(csrf())
                        .param("dias", "25")
                        .param("unidad", "DIAS_CALENDARIO")
                        .param("baseNormativa", "Ley Orgánica de Municipalidades")
                        .param("motivo", "Ajuste de días"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.plazoDias").value(25))
                .andExpect(jsonPath("$.data.unidadPlazo").value("DIAS_CALENDARIO"));

        // Registrar Feriado en Quinua
        LocalDate fechaTest = LocalDate.of(2026, 12, 10);
        GuardarCalendarioRequest cReq = GuardarCalendarioRequest.builder()
                .fecha(fechaTest)
                .nombre("Día Cívico Post-Batalla " + uniqueSuffix)
                .tipo(TipoCalendarioFeriado.DIA_NO_LABORABLE_CIVICO)
                .esLaborable(false)
                .activo(true)
                .build();

        mockMvc.perform(post("/api/mesa-partes/configuracion/calendario")
                        .cookie(adminCookie)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nombre").value("Día Cívico Post-Batalla " + uniqueSuffix));
    }

    @Test
    @DisplayName("REST: Plantillas Documentales y Previsualización de Sustitución")
    void testPlantillasDocumentalesRest() throws Exception {
        GuardarPlantillaRequest pReq = GuardarPlantillaRequest.builder()
                .codigo("PLANT-INT-" + uniqueSuffix)
                .nombre("Notificación de Verificación " + uniqueSuffix)
                .tipoDocumental(TipoDocumentalPlantilla.NOTIFICACION_OBSERVACION)
                .cuerpoPlantilla("MDQ: Expediente {{numero_expediente}}, Solicitante: {{solicitante_nombre}}")
                .build();

        MvcResult resPlant = mockMvc.perform(post("/api/mesa-partes/configuracion/plantillas")
                        .cookie(adminCookie)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.codigo").value("PLANT-INT-" + uniqueSuffix))
                .andReturn();

        Long plantId = objectMapper.readTree(resPlant.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Previsualizar
        mockMvc.perform(post("/api/mesa-partes/configuracion/plantillas/" + plantId + "/preview")
                        .cookie(adminCookie)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "numero_expediente", "MPV-2026-999999",
                                "solicitante_nombre", "VALERIO TINCO"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.containsString("MPV-2026-999999")))
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.containsString("VALERIO TINCO")));
    }

    @Test
    @DisplayName("REST: Numeración y Pista Inmutable de Historial de Cambios")
    void testNumeracionYHistorialRest() throws Exception {
        // Consultar correlativos
        mockMvc.perform(get("/api/mesa-partes/configuracion/numeracion")
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        // Consultar historial
        mockMvc.perform(get("/api/mesa-partes/configuracion/historial")
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }
}
