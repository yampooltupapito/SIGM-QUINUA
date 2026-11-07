package pe.gob.municipalidad.sigm.modules.administracion.perfil;

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
import pe.gob.municipalidad.sigm.common.model.Cargo;
import pe.gob.municipalidad.sigm.common.model.ModuloSistema;
import pe.gob.municipalidad.sigm.common.model.Municipalidad;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Permiso;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.PersonaOficina;
import pe.gob.municipalidad.sigm.common.model.Rol;
import pe.gob.municipalidad.sigm.common.model.TipoActor;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.CargoRepository;
import pe.gob.municipalidad.sigm.common.repository.ModuloSistemaRepository;
import pe.gob.municipalidad.sigm.common.repository.MunicipalidadRepository;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.PermisoRepository;
import pe.gob.municipalidad.sigm.common.repository.PersonaOficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.common.repository.RolRepository;
import pe.gob.municipalidad.sigm.common.repository.TipoActorRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.administracion.perfil.dto.PerfilDtos.ActualizarDatosPersonalesRequest;
import pe.gob.municipalidad.sigm.modules.administracion.perfil.dto.PerfilDtos.CambiarPasswordRequest;
import pe.gob.municipalidad.sigm.modules.administracion.perfil.dto.PerfilDtos.UsuarioPreferenciaDto;
import pe.gob.municipalidad.sigm.modules.administracion.perfil.repository.UsuarioPreferenciaRepository;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;
import pe.gob.municipalidad.sigm.security.model.RefreshToken;
import pe.gob.municipalidad.sigm.security.repository.RefreshTokenRepository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
@DisplayName("Pruebas de Integración - Módulo de Gestión de Perfiles y Cuentas (Prompt 9)")
class PerfilIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PermisoRepository permisoRepository;

    @Autowired
    private ModuloSistemaRepository moduloSistemaRepository;

    @Autowired
    private MunicipalidadRepository municipalidadRepository;

    @Autowired
    private OficinaRepository oficinaRepository;

    @Autowired
    private CargoRepository cargoRepository;

    @Autowired
    private TipoActorRepository tipoActorRepository;

    @Autowired
    private PersonaOficinaRepository personaOficinaRepository;

    @Autowired
    private UsuarioPreferenciaRepository preferenciaRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Usuario testUser;
    private Usuario anotherUser;
    private Persona testPersona;
    private Cookie authCookie;
    private String authToken;

    @BeforeEach
    void setUp() {
        ModuloSistema moduloAdmin = moduloSistemaRepository.findByCodigo("ADMINISTRACION").orElseGet(() ->
                moduloSistemaRepository.save(ModuloSistema.builder()
                        .codigo("ADMINISTRACION")
                        .nombre("Módulo de Administración")
                        .orden(1)
                        .build()));

        Permiso permisoLectura = permisoRepository.findByCodigo("PERFIL_VIEW").orElseGet(() ->
                permisoRepository.save(Permiso.builder()
                        .codigo("PERFIL_VIEW")
                        .nombre("Ver perfil propio")
                        .modulo(moduloAdmin)
                        .accion("VIEW")
                        .build()));

        Rol rolOperador = rolRepository.findByCodigo("ROLE_OPERADOR").orElseGet(() ->
                rolRepository.save(Rol.builder()
                        .codigo("ROLE_OPERADOR")
                        .nombre("Operador Municipal")
                        .estado("ACTIVO")
                        .build()));

        if (rolOperador.getPermisos() == null) {
            rolOperador.setPermisos(new HashSet<>());
        }
        rolOperador.getPermisos().add(permisoLectura);
        rolOperador = rolRepository.save(rolOperador);

        Municipalidad muni = municipalidadRepository.findAll().stream().findFirst().orElseGet(() ->
                municipalidadRepository.save(Municipalidad.builder()
                        .nombre("Municipalidad Distrital de Quinua")
                        .nombreCorto("MDQ")
                        .ubigeo("050108")
                        .departamento("Ayacucho")
                        .provincia("Huamanga")
                        .distrito("Quinua")
                        .build()));

        Oficina oficina = oficinaRepository.findByCodigo("OGTI").orElseGet(() ->
                oficinaRepository.save(Oficina.builder()
                        .municipalidad(muni)
                        .codigo("OGTI")
                        .nombre("Oficina General de Tecnologías de la Información")
                        .sigla("OGTI")
                        .nivelJerarquico(2)
                        .estado("ACTIVO")
                        .build()));

        Cargo cargo = cargoRepository.findByCodigo("ESP_SIST").orElseGet(() ->
                cargoRepository.save(Cargo.builder()
                        .codigo("ESP_SIST")
                        .nombre("Especialista en Sistemas")
                        .nivelJerarquico(1)
                        .estado("ACTIVO")
                        .build()));

        TipoActor tipoTrabajador = tipoActorRepository.findByCodigo("TRABAJADOR_MUNICIPAL").orElseGet(() ->
                tipoActorRepository.save(TipoActor.builder()
                        .codigo("TRABAJADOR_MUNICIPAL")
                        .nombre("Trabajador Municipal")
                        .estado("ACTIVO")
                        .build()));

        testPersona = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("45678901")
                .nombres("María Elena")
                .apellidoPaterno("Quispe")
                .apellidoMaterno("Huamán")
                .correo("mquispe.test@quinua.gob.pe")
                .telefono("966123456")
                .direccion("Plaza de Armas S/N, Quinua")
                .build());

        personaOficinaRepository.save(PersonaOficina.builder()
                .persona(testPersona)
                .oficina(oficina)
                .cargo(cargo)
                .tipoActor(tipoTrabajador)
                .fechaInicio(LocalDate.now())
                .principal(true)
                .estado("ACTIVO")
                .build());

        testUser = usuarioRepository.save(Usuario.builder()
                .username("mquispe.perfil")
                .persona(testPersona)
                .passwordHash(passwordEncoder.encode("ActualPass2026!"))
                .enabled(true)
                .accountLocked(false)
                .roles(Set.of(rolOperador))
                .build());

        Persona personaOtra = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("78901234")
                .nombres("Juan Carlos")
                .apellidoPaterno("Flores")
                .correo("jflores.otro@quinua.gob.pe")
                .build());

        anotherUser = usuarioRepository.save(Usuario.builder()
                .username("jflores.otro")
                .persona(personaOtra)
                .passwordHash(passwordEncoder.encode("Pass2026!"))
                .enabled(true)
                .accountLocked(false)
                .roles(Set.of(rolOperador))
                .build());

        CustomUserDetails details = CustomUserDetails.builder()
                .id(testUser.getId())
                .username(testUser.getUsername())
                .password("dummy")
                .enabled(true)
                .accountNonLocked(true)
                .roles(Set.of("ROLE_OPERADOR"))
                .permisos(Set.of("PERFIL_VIEW"))
                .authorities(List.of(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_OPERADOR"),
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("PERFIL_VIEW")
                ))
                .build();

        authToken = jwtTokenProvider.generateAccessToken(details);
        authCookie = new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, authToken);
        authCookie.setPath("/");
    }

    // =========================================================================
    // 1. CONSULTAR PERFIL PROPIO (WEB Y REST)
    // =========================================================================

    @Test
    @DisplayName("1. Debe renderizar vista Thymeleaf de perfil para usuario autenticado")
    void testRenderPerfilWeb() throws Exception {
        mockMvc.perform(get("/perfil")
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Mi Perfil Institucional")))
                .andExpect(content().string(containsString("mquispe.perfil")))
                .andExpect(content().string(containsString("María Elena")));
    }

    @Test
    @DisplayName("2. Debe devolver datos de perfil vía API REST con DNI parcialmente enmascarado")
    void testGetPerfilApi() throws Exception {
        mockMvc.perform(get("/api/perfil")
                        .cookie(authCookie)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username", is("mquispe.perfil")))
                .andExpect(jsonPath("$.data.nombres", is("María Elena")))
                .andExpect(jsonPath("$.data.apellidoPaterno", is("Quispe")))
                .andExpect(jsonPath("$.data.correo", is("mquispe.test@quinua.gob.pe")))
                .andExpect(jsonPath("$.data.numeroDocumentoEnmascarado", containsString("****")))
                .andExpect(jsonPath("$.data.oficinaPrincipal.nombre", containsString("Oficina General de Tecnologías")))
                .andExpect(jsonPath("$.data.cargoPrincipal.nombre", is("Especialista en Sistemas")))
                .andExpect(jsonPath("$.data.roles", hasSize(1)));
    }

    // =========================================================================
    // 2. RECHAZAR ACCESO ANÓNIMO
    // =========================================================================

    @Test
    @DisplayName("3. Debe redirigir al login si un usuario anónimo intenta acceder a /perfil")
    void testRechazarAccesoAnonimoWeb() throws Exception {
        mockMvc.perform(get("/perfil"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/login")));
    }

    @Test
    @DisplayName("4. Debe rechazar con 401 si se accede a /api/perfil sin autenticación")
    void testRechazarAccesoAnonimoApi() throws Exception {
        mockMvc.perform(get("/api/perfil"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // 3. EDITAR DATOS PERSONALES Y VALIDACIONES
    // =========================================================================

    @Test
    @DisplayName("5. Debe actualizar teléfono y dirección del usuario autenticado")
    void testActualizarDatosPersonalesExito() throws Exception {
        ActualizarDatosPersonalesRequest request = ActualizarDatosPersonalesRequest.builder()
                .telefono("966999888")
                .correo("mquispe.nuevo@quinua.gob.pe")
                .direccion("Av. Centenario 123, Quinua")
                .build();

        mockMvc.perform(patch("/api/perfil")
                        .with(csrf())
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.telefono", is("966999888")))
                .andExpect(jsonPath("$.data.correo", is("mquispe.nuevo@quinua.gob.pe")))
                .andExpect(jsonPath("$.data.direccion", is("Av. Centenario 123, Quinua")));

        Persona actualizada = personaRepository.findById(testPersona.getId()).orElseThrow();
        assertThat(actualizada.getTelefono()).isEqualTo("966999888");
        assertThat(actualizada.getCorreo()).isEqualTo("mquispe.nuevo@quinua.gob.pe");
    }

    @Test
    @DisplayName("6. Debe rechazar actualización si el correo ya está registrado por otra persona")
    void testRechazarCorreoDuplicado() throws Exception {
        ActualizarDatosPersonalesRequest request = ActualizarDatosPersonalesRequest.builder()
                .telefono("966111222")
                .correo("jflores.otro@quinua.gob.pe") // Pertenece a anotherUser
                .direccion("Av. Los Libertadores 456")
                .build();

        mockMvc.perform(patch("/api/perfil")
                        .with(csrf())
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("correo")));
    }

    // =========================================================================
    // 4. CAMBIO SEGURO DE CONTRASEÑA
    // =========================================================================

    @Test
    @DisplayName("7. Debe cambiar contraseña exitosamente cuando los datos son válidos")
    void testCambiarPasswordExito() throws Exception {
        refreshTokenRepository.save(RefreshToken.builder()
                .usuario(testUser)
                .tokenId("test-token-id-1")
                .tokenHash("hash-test-token-1")
                .familyId("family-1")
                .expiresAt(OffsetDateTime.now().plusDays(7))
                .revoked(false)
                .userAgent("Mozilla/5.0 Android")
                .createdByIp("190.236.1.1")
                .build());

        CambiarPasswordRequest request = CambiarPasswordRequest.builder()
                .passwordActual("ActualPass2026!")
                .passwordNuevo("NuevaClaveSegura2026!")
                .confirmacionPasswordNuevo("NuevaClaveSegura2026!")
                .build();

        mockMvc.perform(post("/api/perfil/cambiar-password")
                        .with(csrf())
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("exitosamente")));

        Usuario userUpdated = usuarioRepository.findById(testUser.getId()).orElseThrow();
        assertThat(passwordEncoder.matches("NuevaClaveSegura2026!", userUpdated.getPasswordHash())).isTrue();

        // Verificar que las otras sesiones se revocaron
        List<RefreshToken> activeTokens = refreshTokenRepository.findActiveByUsuarioId(testUser.getId(), OffsetDateTime.now());
        assertThat(activeTokens).isEmpty();
    }

    @Test
    @DisplayName("8. Debe rechazar cambio de contraseña si la actual es incorrecta")
    void testRechazarPasswordActualIncorrecta() throws Exception {
        CambiarPasswordRequest request = CambiarPasswordRequest.builder()
                .passwordActual("ClaveEquivocada123!")
                .passwordNuevo("NuevaClaveSegura2026!")
                .confirmacionPasswordNuevo("NuevaClaveSegura2026!")
                .build();

        mockMvc.perform(post("/api/perfil/cambiar-password")
                        .with(csrf())
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("contraseña actual")));
    }

    @Test
    @DisplayName("9. Debe rechazar cambio si la confirmación no coincide")
    void testRechazarPasswordConfirmacionNoCoincide() throws Exception {
        CambiarPasswordRequest request = CambiarPasswordRequest.builder()
                .passwordActual("ActualPass2026!")
                .passwordNuevo("NuevaClaveSegura2026!")
                .confirmacionPasswordNuevo("OtraClaveDiferente2026!")
                .build();

        mockMvc.perform(post("/api/perfil/cambiar-password")
                        .with(csrf())
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // 5. ROLES, PERMISOS Y ASIGNACIONES
    // =========================================================================

    @Test
    @DisplayName("10. Debe consultar roles y permisos consolidados en modo lectura")
    void testConsultarPermisos() throws Exception {
        mockMvc.perform(get("/api/perfil/permisos")
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roles[0].codigo", is("ROLE_OPERADOR")))
                .andExpect(jsonPath("$.data.modulosAutorizados", hasItem("ADMINISTRACION")));
    }

    @Test
    @DisplayName("11. Debe consultar oficinas y cargos asignados")
    void testConsultarAsignaciones() throws Exception {
        mockMvc.perform(get("/api/perfil/asignaciones")
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].oficinaCodigo", is("OGTI")))
                .andExpect(jsonPath("$.data[0].cargoNombre", is("Especialista en Sistemas")));
    }

    // =========================================================================
    // 6. PREFERENCIAS DE USUARIO
    // =========================================================================

    @Test
    @DisplayName("12. Debe consultar y actualizar preferencias de usuario")
    void testGestionPreferencias() throws Exception {
        // Consultar (valores por defecto)
        mockMvc.perform(get("/api/perfil/preferencias")
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tema", is("OSCURO")));

        // Actualizar
        UsuarioPreferenciaDto updatePref = UsuarioPreferenciaDto.builder()
                .tema("CLARO")
                .idioma("es")
                .notifEmail(false)
                .notifSistema(true)
                .pageSize(50)
                .sidebarColapsado(true)
                .build();

        mockMvc.perform(patch("/api/perfil/preferencias")
                        .with(csrf())
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePref)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tema", is("CLARO")))
                .andExpect(jsonPath("$.data.notifEmail", is(false)))
                .andExpect(jsonPath("$.data.pageSize", is(50)))
                .andExpect(jsonPath("$.data.sidebarColapsado", is(true)));
    }

    // =========================================================================
    // 7. GESTIÓN DE FOTOGRAFÍA DE PERFIL
    // =========================================================================

    @Test
    @DisplayName("13. Debe permitir subir fotografía JPEG con Magic Bytes válidos")
    void testSubirFotografiaValida() throws Exception {
        // Magic bytes para JPEG: FF D8 FF E0
        byte[] jpegContent = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10, 0x4A, 0x46, 0x49, 0x46};
        MockMultipartFile file = new MockMultipartFile(
                "archivo",
                "perfil_test.jpg",
                "image/jpeg",
                jpegContent
        );

        mockMvc.perform(multipart("/api/perfil/fotografia")
                        .file(file)
                        .with(csrf())
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("éxito")))
                .andExpect(jsonPath("$.data.fotoUrl", containsString("/api/perfil/fotografia/avatar_")));

        Persona actualizada = personaRepository.findById(testPersona.getId()).orElseThrow();
        assertThat(actualizada.getFotoUrl()).isNotEmpty();
    }

    @Test
    @DisplayName("14. Debe rechazar archivo ejecutable o con extensión engañosa")
    void testRechazarArchivoFalso() throws Exception {
        byte[] fakeContent = "echo 'malicious script'".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "archivo",
                "malicious.jpg",
                "image/jpeg",
                fakeContent
        );

        mockMvc.perform(multipart("/api/perfil/fotografia")
                        .file(file)
                        .with(csrf())
                        .cookie(authCookie))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("no coincide con una imagen válida")));
    }

    @Test
    @DisplayName("15. Debe eliminar fotografía de perfil")
    void testEliminarFotografia() throws Exception {
        testPersona.setFotoUrl("/api/perfil/fotografia/foto-previa.png");
        personaRepository.save(testPersona);

        mockMvc.perform(delete("/api/perfil/fotografia")
                        .with(csrf())
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("eliminada")));

        Persona actualizada = personaRepository.findById(testPersona.getId()).orElseThrow();
        assertThat(actualizada.getFotoUrl()).isNull();
    }

    // =========================================================================
    // 8. SESIONES ACTIVAS Y REVOCACIÓN
    // =========================================================================

    @Test
    @DisplayName("16. Debe listar sesiones activas y permitir revocar una sesión remota")
    void testSesionesActivasYRevocacion() throws Exception {
        RefreshToken tokenRemoto = refreshTokenRepository.save(RefreshToken.builder()
                .usuario(testUser)
                .tokenId("session-token-to-revoke")
                .tokenHash("hash-token-to-revoke")
                .familyId("family-remoto")
                .expiresAt(OffsetDateTime.now().plusDays(7))
                .revoked(false)
                .userAgent("Mozilla/5.0 Windows Chrome")
                .createdByIp("190.236.5.5")
                .build());

        // Listar sesiones
        mockMvc.perform(get("/api/perfil/sesiones")
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].ip", is("190.236.5.5")));

        // Revocar sesión remota
        mockMvc.perform(delete("/api/perfil/sesiones/" + tokenRemoto.getId())
                        .with(csrf())
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("correctamente")));

        RefreshToken revocado = refreshTokenRepository.findById(tokenRemoto.getId()).orElseThrow();
        assertThat(revocado.getRevoked()).isTrue();
    }

    @Test
    @DisplayName("17. Debe permitir revocar todas las demás sesiones excepto la actual")
    void testRevocarTodasLasOtrasSesiones() throws Exception {
        refreshTokenRepository.save(RefreshToken.builder()
                .usuario(testUser)
                .tokenId("session-1")
                .tokenHash("hash-session-1")
                .familyId("family-s1")
                .expiresAt(OffsetDateTime.now().plusDays(7))
                .revoked(false)
                .userAgent("Mobile App")
                .createdByIp("190.236.1.1")
                .build());

        refreshTokenRepository.save(RefreshToken.builder()
                .usuario(testUser)
                .tokenId("session-2")
                .tokenHash("hash-session-2")
                .familyId("family-s2")
                .expiresAt(OffsetDateTime.now().plusDays(7))
                .revoked(false)
                .userAgent("Firefox Mac")
                .createdByIp("190.236.2.2")
                .build());

        mockMvc.perform(post("/api/perfil/sesiones/revocar-otras")
                        .with(csrf())
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("exitosamente")));

        List<RefreshToken> activas = refreshTokenRepository.findActiveByUsuarioId(testUser.getId(), OffsetDateTime.now());
        assertThat(activas).isEmpty();
    }

    // =========================================================================
    // 9. ACTIVIDAD Y AUDITORÍA
    // =========================================================================

    @Test
    @DisplayName("18. Debe consultar actividad propia paginada y proteger datos de terceros")
    void testConsultarActividad() throws Exception {
        mockMvc.perform(get("/api/perfil/actividad?page=0&size=10")
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    // =========================================================================
    // 10. PREVENCIÓN DE IDOR Y SEGURIDAD
    // =========================================================================

    @Test
    @DisplayName("19. Prevención de IDOR: La API de perfil no acepta ID de usuario y opera únicamente sobre el usuario autenticado")
    void testIdorPrevention() throws Exception {
        ActualizarDatosPersonalesRequest request = ActualizarDatosPersonalesRequest.builder()
                .telefono("966777111")
                .correo("mquispe.cambio@quinua.gob.pe")
                .direccion("Quinua Centro")
                .build();

        mockMvc.perform(patch("/api/perfil")
                        .with(csrf())
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username", is("mquispe.perfil")));

        // Verificar que anotherUser sigue intacto
        Usuario otherInDb = usuarioRepository.findById(anotherUser.getId()).orElseThrow();
        assertThat(otherInDb.getPersona().getCorreo()).isEqualTo("jflores.otro@quinua.gob.pe");
    }
}
