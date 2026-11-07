package pe.gob.municipalidad.sigm.modules.serenazgo;

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
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.CambioEstadoPersonalRequest;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.PersonalSerenazgoCreateRequest;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.PersonalSerenazgoResponse;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.CargoOperativoSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoPersonalSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.CargoOperativoSerenazgoRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.PersonalSerenazgoService;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class PersonalSerenazgoWebSecurityTest {

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
    private CargoOperativoSerenazgoRepository cargoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PersonalSerenazgoService personalService;

    private String adminToken;
    private String ciudadanoToken;
    private Persona personaParaCrear;
    private CargoOperativoSerenazgo cargoTest;
    private PersonalSerenazgoResponse personalExistente;

    @BeforeEach
    void setUp() {
        long ts = System.currentTimeMillis();

        Rol rolAdmin = rolRepository.findByCodigo("ROLE_ADMIN").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_ADMIN").nombre("Administrador").estado("ACTIVO").build()));

        Rol rolCiudadano = rolRepository.findByCodigo("ROLE_CIUDADANO").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_CIUDADANO").nombre("Ciudadano").estado("ACTIVO").build()));

        // Admin
        Persona pAdmin = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("80" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Admin").apellidoPaterno("Seguridad").estado("ACTIVO").build());
        Usuario uAdmin = usuarioRepository.save(Usuario.builder()
                .username("admin_ps_" + ts).persona(pAdmin).passwordHash(passwordEncoder.encode("Pass123!"))
                .enabled(true).accountLocked(false).roles(Set.of(rolAdmin)).build());

        CustomUserDetails adminDetails = CustomUserDetails.builder()
                .id(uAdmin.getId()).username(uAdmin.getUsername()).password("dummy").enabled(true).accountNonLocked(true)
                .roles(Set.of("ROLE_ADMIN"))
                .permisos(Set.of(
                        "ROLE_ADMIN",
                        "SERENAZGO_PERSONAL_VER",
                        "SERENAZGO_PERSONAL_CREAR",
                        "SERENAZGO_PERSONAL_EDITAR",
                        "SERENAZGO_PERSONAL_ESTADO_CAMBIAR",
                        "SERENAZGO_PERSONAL_CARGO_CAMBIAR",
                        "SERENAZGO_PERSONAL_DOCUMENTOS_VER",
                        "SERENAZGO_PERSONAL_DOCUMENTOS_GESTIONAR",
                        "SERENAZGO_PERSONAL_HISTORIAL_VER",
                        "SERENAZGO_PERSONAL_FICHA_EXPORTAR",
                        "SERENAZGO_PERSONAL_ESTADISTICAS_VER"
                ))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("SERENAZGO_PERSONAL_VER"),
                        new SimpleGrantedAuthority("SERENAZGO_PERSONAL_CREAR"),
                        new SimpleGrantedAuthority("SERENAZGO_PERSONAL_EDITAR"),
                        new SimpleGrantedAuthority("SERENAZGO_PERSONAL_ESTADO_CAMBIAR"),
                        new SimpleGrantedAuthority("SERENAZGO_PERSONAL_CARGO_CAMBIAR"),
                        new SimpleGrantedAuthority("SERENAZGO_PERSONAL_DOCUMENTOS_VER"),
                        new SimpleGrantedAuthority("SERENAZGO_PERSONAL_DOCUMENTOS_GESTIONAR"),
                        new SimpleGrantedAuthority("SERENAZGO_PERSONAL_HISTORIAL_VER"),
                        new SimpleGrantedAuthority("SERENAZGO_PERSONAL_FICHA_EXPORTAR"),
                        new SimpleGrantedAuthority("SERENAZGO_PERSONAL_ESTADISTICAS_VER")
                ))
                .build();
        adminToken = jwtTokenProvider.generateAccessToken(adminDetails);

        // Ciudadano
        Persona pCiudadano = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("81" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Ciudadano").apellidoPaterno("Comun").estado("ACTIVO").build());
        Usuario uCiudadano = usuarioRepository.save(Usuario.builder()
                .username("ciud_ps_" + ts).persona(pCiudadano).passwordHash(passwordEncoder.encode("Pass123!"))
                .enabled(true).accountLocked(false).roles(Set.of(rolCiudadano)).build());

        CustomUserDetails ciudadanoDetails = CustomUserDetails.builder()
                .id(uCiudadano.getId()).username(uCiudadano.getUsername()).password("dummy").enabled(true).accountNonLocked(true)
                .roles(Set.of("ROLE_CIUDADANO"))
                .permisos(Set.of())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_CIUDADANO")))
                .build();
        ciudadanoToken = jwtTokenProvider.generateAccessToken(ciudadanoDetails);

        cargoTest = cargoRepository.findByCodigo("SERENO_PATRULLERO").orElseThrow();

        // Persona para nuevo registro
        personaParaCrear = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("82" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Efectivo").apellidoPaterno("Prueba").estado("ACTIVO").build());

        // Personal existente para pruebas
        Persona pExistente = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("83" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Efectivo").apellidoPaterno("Existente").estado("ACTIVO").build());

        personalExistente = personalService.registrar(PersonalSerenazgoCreateRequest.builder()
                .personaId(pExistente.getId())
                .cargoOperativoId(cargoTest.getId())
                .fechaIngreso(LocalDate.now())
                .build(), "admin");
    }

    @Test
    @DisplayName("01: Acceso anónimo a la API de personal es rechazado (HTTP 401)")
    void test01_acceso_anonimo_rechazado() throws Exception {
        mockMvc.perform(get("/api/serenazgo/personal")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("02: Listar personal con permisos de administrador (HTTP 200)")
    void test02_listar_personal_admin() throws Exception {
        mockMvc.perform(get("/api/serenazgo/personal")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("03: Registrar nuevo personal vía REST con código correlativo (HTTP 201)")
    void test03_registrar_personal_rest() throws Exception {
        PersonalSerenazgoCreateRequest req = PersonalSerenazgoCreateRequest.builder()
                .personaId(personaParaCrear.getId())
                .cargoOperativoId(cargoTest.getId())
                .tipoDocumentoLaboral("Contrato CAS 2026")
                .fechaIngreso(LocalDate.now())
                .observaciones("Registro vía REST API")
                .build();

        mockMvc.perform(post("/api/serenazgo/personal")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.codigoInterno").isNotEmpty())
                .andExpect(jsonPath("$.estado").value("ACTIVO"));
    }

    @Test
    @DisplayName("04: Modificar estado de revista vía REST (HTTP 200)")
    void test04_cambiar_estado_rest() throws Exception {
        CambioEstadoPersonalRequest req = CambioEstadoPersonalRequest.builder()
                .nuevoEstado(EstadoPersonalSerenazgo.INACTIVO)
                .fechaEfectiva(LocalDate.now())
                .motivo("Descanso temporal sin goce")
                .build();

        mockMvc.perform(patch("/api/serenazgo/personal/" + personalExistente.getId() + "/estado")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("INACTIVO"));
    }

    @Test
    @DisplayName("05: Descargar ficha técnica PDF del personal (HTTP 200)")
    void test05_descargar_ficha_pdf() throws Exception {
        mockMvc.perform(get("/api/serenazgo/personal/" + personalExistente.getId() + "/ficha.pdf")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("ficha_serenazgo_")));
    }

    @Test
    @DisplayName("06: Denegación de acceso a usuario ciudadano sin permisos (HTTP 403)")
    void test06_ciudadano_sin_permiso_rechazado() throws Exception {
        mockMvc.perform(get("/api/serenazgo/personal")
                        .header("Authorization", "Bearer " + ciudadanoToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("07: Renderizado exitoso de vistas Thymeleaf de personal (HTTP 200)")
    void test07_vistas_thymeleaf_render_ok() throws Exception {
        Cookie authCookie = new Cookie(SecurityConstants.ACCESS_TOKEN_COOKIE_NAME, adminToken);

        // 1. Listado
        mockMvc.perform(get("/dashboard/serenazgo/personal").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/serenazgo/personal/listado"))
                .andExpect(model().attributeExists("pagina"))
                .andExpect(model().attributeExists("estadisticas"));

        // 2. Nuevo
        mockMvc.perform(get("/dashboard/serenazgo/personal/nuevo").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/serenazgo/personal/formulario"))
                .andExpect(model().attributeExists("form"))
                .andExpect(model().attributeExists("cargos"));

        // 3. Detalle
        mockMvc.perform(get("/dashboard/serenazgo/personal/" + personalExistente.getId()).cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/serenazgo/personal/detalle"))
                .andExpect(model().attributeExists("sereno"));

        // 4. Editar
        mockMvc.perform(get("/dashboard/serenazgo/personal/" + personalExistente.getId() + "/editar").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/serenazgo/personal/editar"))
                .andExpect(model().attributeExists("form"));

        // 5. Historial
        mockMvc.perform(get("/dashboard/serenazgo/personal/" + personalExistente.getId() + "/historial").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/serenazgo/personal/historial"))
                .andExpect(model().attributeExists("sereno"));

        // 6. Documentos
        mockMvc.perform(get("/dashboard/serenazgo/personal/" + personalExistente.getId() + "/documentos").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/serenazgo/personal/documentos"))
                .andExpect(model().attributeExists("sereno"));
    }
}
