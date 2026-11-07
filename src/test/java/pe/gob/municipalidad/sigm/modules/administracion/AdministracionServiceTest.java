package pe.gob.municipalidad.sigm.modules.administracion;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ConflictException;
import pe.gob.municipalidad.sigm.common.model.*;
import pe.gob.municipalidad.sigm.common.repository.*;
import pe.gob.municipalidad.sigm.modules.administracion.dto.CargoDtos.CargoCreateRequest;
import pe.gob.municipalidad.sigm.modules.administracion.dto.CargoDtos.CargoResponse;
import pe.gob.municipalidad.sigm.modules.administracion.dto.OficinaDtos.OficinaCreateRequest;
import pe.gob.municipalidad.sigm.modules.administracion.dto.OficinaDtos.OficinaResponse;
import pe.gob.municipalidad.sigm.modules.administracion.dto.OficinaDtos.OficinaUpdateRequest;
import pe.gob.municipalidad.sigm.modules.administracion.dto.PermisoDtos.PermisoResponse;
import pe.gob.municipalidad.sigm.modules.administracion.dto.RolDtos.RolCreateRequest;
import pe.gob.municipalidad.sigm.modules.administracion.dto.RolDtos.RolResponse;
import pe.gob.municipalidad.sigm.modules.administracion.dto.TipoActorDtos.TipoActorCreateRequest;
import pe.gob.municipalidad.sigm.modules.administracion.dto.TipoActorDtos.TipoActorResponse;
import pe.gob.municipalidad.sigm.modules.administracion.dto.UsuarioDtos.UsuarioCreateRequest;
import pe.gob.municipalidad.sigm.modules.administracion.dto.UsuarioDtos.UsuarioResponse;
import pe.gob.municipalidad.sigm.modules.administracion.dto.UsuarioDtos.UsuarioUpdateRequest;
import pe.gob.municipalidad.sigm.modules.administracion.service.*;
import pe.gob.municipalidad.sigm.security.service.RefreshTokenService;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias de Reglas de Negocio y Administración - SIGM-QUINUA")
class AdministracionServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PersonaRepository personaRepository;
    @Mock
    private RolRepository rolRepository;
    @Mock
    private PermisoRepository permisoRepository;
    @Mock
    private ModuloSistemaRepository moduloRepository;
    @Mock
    private OficinaRepository oficinaRepository;
    @Mock
    private MunicipalidadRepository municipalidadRepository;
    @Mock
    private CargoRepository cargoRepository;
    @Mock
    private TipoActorRepository tipoActorRepository;
    @Mock
    private PersonaOficinaRepository personaOficinaRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private AuditoriaAdministrativaService auditoriaService;

    @InjectMocks
    private UsuarioAdminService usuarioAdminService;
    @InjectMocks
    private RolAdminService rolAdminService;
    @InjectMocks
    private PermisoAdminService permisoAdminService;
    @InjectMocks
    private OficinaAdminService oficinaAdminService;
    @InjectMocks
    private CargoAdminService cargoAdminService;
    @InjectMocks
    private TipoActorAdminService tipoActorAdminService;

    @BeforeEach
    void setUpSecurityContext() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "admin_operador", "credentials", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // 1. Crear usuario exitoso
    @Test
    @DisplayName("1. Crear usuario exitoso con codificación BCrypt")
    void testCrearUsuarioExitoso() {
        UsuarioCreateRequest req = UsuarioCreateRequest.builder()
                .username("cajero_quinua")
                .password("Password123#")
                .tipoDocumento("DNI")
                .numeroDocumento("45892314")
                .nombres("Carlos")
                .apellidoPaterno("Mendoza")
                .correo("carlos@quinua.gob.pe")
                .rolIds(Set.of(1L))
                .build();

        Rol rolCajero = Rol.builder().id(1L).codigo("ROLE_CAJERO").nombre("Cajero").build();
        Persona persona = Persona.builder().id(10L).tipoDocumento("DNI").numeroDocumento("45892314")
                .nombres("Carlos").apellidoPaterno("Mendoza").correo("carlos@quinua.gob.pe").build();

        when(usuarioRepository.existsByUsername("cajero_quinua")).thenReturn(false);
        when(personaRepository.findByTipoDocumentoAndNumeroDocumento("DNI", "45892314")).thenReturn(Optional.of(persona));
        when(usuarioRepository.existsByPersonaId(10L)).thenReturn(false);
        when(rolRepository.findAllById(anySet())).thenReturn(List.of(rolCajero));
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$encodedHashMock");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(100L);
            return u;
        });

        UsuarioResponse resp = usuarioAdminService.registrarUsuario(req);

        assertThat(resp).isNotNull();
        assertThat(resp.getId()).isEqualTo(100L);
        assertThat(resp.getUsername()).isEqualTo("cajero_quinua");
        verify(passwordEncoder).encode("Password123#");
        verify(auditoriaService).registrar(eq("REGISTRAR_USUARIO"), eq("USUARIO"), eq(100L), eq("EXITO"), anyString());
    }

    // 2. Username duplicado
    @Test
    @DisplayName("2. Error al intentar registrar username duplicado")
    void testUsernameDuplicado() {
        UsuarioCreateRequest req = UsuarioCreateRequest.builder().username("admin_existente").build();
        when(usuarioRepository.existsByUsername("admin_existente")).thenReturn(true);

        assertThatThrownBy(() -> usuarioAdminService.registrarUsuario(req))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Ya existe un usuario registrado con el username");
    }

    // 3. Persona ya tiene usuario asignado
    @Test
    @DisplayName("3. Error si la persona ya cuenta con un usuario en el sistema")
    void testPersonaConUsuarioAsignado() {
        UsuarioCreateRequest req = UsuarioCreateRequest.builder()
                .username("nuevo_user")
                .tipoDocumento("DNI")
                .numeroDocumento("12345678")
                .build();

        Persona persona = Persona.builder().id(5L).numeroDocumento("12345678").build();
        when(usuarioRepository.existsByUsername("nuevo_user")).thenReturn(false);
        when(personaRepository.findByTipoDocumentoAndNumeroDocumento("DNI", "12345678")).thenReturn(Optional.of(persona));
        when(usuarioRepository.existsByPersonaId(5L)).thenReturn(true);

        assertThatThrownBy(() -> usuarioAdminService.registrarUsuario(req))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("ya tiene una cuenta de usuario asignada");
    }

    // 4. Editar usuario exitoso
    @Test
    @DisplayName("4. Editar usuario y actualizar datos personales")
    void testEditarUsuarioExitoso() {
        Persona persona = Persona.builder().id(1L).nombres("Juan").apellidoPaterno("Perez").correo("antiguo@quinua.gob.pe").build();
        Usuario usuario = Usuario.builder().id(20L).username("jperez").persona(persona).roles(new HashSet<>()).build();

        when(usuarioRepository.findWithRolesAndPersonaById(20L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        UsuarioUpdateRequest req = UsuarioUpdateRequest.builder()
                .nombres("Juan Carlos")
                .apellidoPaterno("Pérez")
                .correo("nuevo@quinua.gob.pe")
                .build();

        UsuarioResponse resp = usuarioAdminService.actualizarUsuario(20L, req);

        assertThat(resp).isNotNull();
        assertThat(persona.getCorreo()).isEqualTo("nuevo@quinua.gob.pe");
        verify(auditoriaService).registrar(eq("ACTUALIZAR_USUARIO"), eq("USUARIO"), eq(20L), eq("EXITO"), anyString());
    }

    // 5. Desactivar usuario e impedir auto-desactivación
    @Test
    @DisplayName("5. Desactivar usuario exitoso e impedir auto-desactivación del propio operador")
    void testDesactivarUsuarioYAutoDesactivacion() {
        Persona personaTarget = Persona.builder().id(30L).nombres("Otro").apellidoPaterno("Usuario").build();
        Usuario target = Usuario.builder().id(30L).username("otro_usuario").persona(personaTarget).roles(new HashSet<>()).enabled(true).accountLocked(false).build();
        when(usuarioRepository.findWithRolesAndPersonaById(30L)).thenReturn(Optional.of(target));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(target);

        UsuarioResponse resp = usuarioAdminService.desactivarCuenta(30L);
        assertThat(resp.getEnabled()).isFalse();
        verify(refreshTokenService).revokeAllUserTokens(30L);

        // Intento de auto-desactivación (usuario autenticado es "admin_operador")
        Persona personaPropio = Persona.builder().id(99L).nombres("Admin").apellidoPaterno("Operador").build();
        Usuario propio = Usuario.builder().id(99L).username("admin_operador").persona(personaPropio).roles(new HashSet<>()).enabled(true).build();
        when(usuarioRepository.findWithRolesAndPersonaById(99L)).thenReturn(Optional.of(propio));

        assertThatThrownBy(() -> usuarioAdminService.desactivarCuenta(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no puede desactivar su propia cuenta");
    }

    // 6. Bloquear usuario e impedir auto-bloqueo
    @Test
    @DisplayName("6. Bloquear usuario preventivamente e impedir auto-bloqueo")
    void testBloquearUsuarioYAutoBloqueo() {
        Persona personaPropio = Persona.builder().id(99L).nombres("Admin").apellidoPaterno("Operador").build();
        Usuario propio = Usuario.builder().id(99L).username("admin_operador").persona(personaPropio).roles(new HashSet<>()).accountLocked(false).build();
        when(usuarioRepository.findWithRolesAndPersonaById(99L)).thenReturn(Optional.of(propio));

        assertThatThrownBy(() -> usuarioAdminService.bloquearCuenta(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no puede bloquear su propia cuenta");
    }

    // 6b. Protección estricta del último Superadministrador
    @Test
    @DisplayName("6b. Impedir desactivación o bloqueo del último Superadministrador activo")
    void testProteccionUltimoSuperadmin() {
        Rol rolSuperAdmin = Rol.builder().id(9L).codigo("ROLE_SUPERADMIN").nombre("Superadmin").build();
        Persona personaSA = Persona.builder().id(77L).nombres("Super").apellidoPaterno("Admin").build();
        Usuario superadmin = Usuario.builder()
                .id(77L)
                .username("superadmin_quinua")
                .persona(personaSA)
                .roles(Set.of(rolSuperAdmin))
                .enabled(true)
                .accountLocked(false)
                .build();

        when(usuarioRepository.findWithRolesAndPersonaById(77L)).thenReturn(Optional.of(superadmin));
        when(usuarioRepository.countActiveUsersWithRole("ROLE_SUPERADMIN")).thenReturn(1L);

        // Desactivación rechazada
        assertThatThrownBy(() -> usuarioAdminService.desactivarCuenta(77L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("No es posible desactivar al único Superadministrador");

        // Bloqueo rechazado
        assertThatThrownBy(() -> usuarioAdminService.bloquearCuenta(77L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("No es posible bloquear al único Superadministrador");
    }

    // 7. Crear rol exitoso
    @Test
    @DisplayName("7. Crear rol dinámico en base de datos")
    void testCrearRolExitoso() {
        RolCreateRequest req = RolCreateRequest.builder()
                .codigo("FISCALIZADOR")
                .nombre("Fiscalizador Municipal")
                .descripcion("Inspección de tributos")
                .build();

        when(rolRepository.existsByCodigo("ROLE_FISCALIZADOR")).thenReturn(false);
        when(rolRepository.save(any(Rol.class))).thenAnswer(inv -> {
            Rol r = inv.getArgument(0);
            r.setId(50L);
            return r;
        });

        RolResponse resp = rolAdminService.crearRol(req);

        assertThat(resp).isNotNull();
        assertThat(resp.getCodigo()).isEqualTo("ROLE_FISCALIZADOR");
        verify(auditoriaService).registrar(eq("CREAR_ROL"), eq("ROL"), eq(50L), eq("EXITO"), anyString());
    }

    // 8. Asignar rol a usuario
    @Test
    @DisplayName("8. Asignar rol a usuario e invalidar sesiones activas")
    void testAsignarRolAUsuario() {
        Persona persona = Persona.builder().id(1L).nombres("Ana").apellidoPaterno("Torres").build();
        Rol rolSereno = Rol.builder().id(2L).codigo("ROLE_SERENO").nombre("Sereno").build();
        Usuario usuario = Usuario.builder().id(40L).username("atorres").persona(persona).roles(new HashSet<>()).build();

        when(usuarioRepository.findWithRolesAndPersonaById(40L)).thenReturn(Optional.of(usuario));
        when(rolRepository.findAllById(Set.of(2L))).thenReturn(List.of(rolSereno));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        UsuarioResponse resp = usuarioAdminService.asignarRoles(40L, Set.of(2L));

        assertThat(resp.getRoles()).contains("ROLE_SERENO");
        verify(refreshTokenService).revokeAllUserTokens(40L);
    }

    // 9. Prevenir que un admin revoque su propio rol administrador
    @Test
    @DisplayName("9. Prevenir auto-revocación de rol administrativo crítico")
    void testPrevenirAutoRevocacionRolCritico() {
        Persona persona = Persona.builder().id(1L).nombres("Admin").apellidoPaterno("General").build();
        Rol rolAdmin = Rol.builder().id(1L).codigo("ROLE_ADMIN").nombre("Admin").build();
        Rol rolCajero = Rol.builder().id(2L).codigo("ROLE_CAJERO").nombre("Cajero").build();
        Usuario adminUser = Usuario.builder().id(99L).username("admin_operador").persona(persona)
                .roles(new HashSet<>(Set.of(rolAdmin))).build();

        when(usuarioRepository.findWithRolesAndPersonaById(99L)).thenReturn(Optional.of(adminUser));
        when(rolRepository.findAllById(Set.of(2L))).thenReturn(List.of(rolCajero));

        assertThatThrownBy(() -> usuarioAdminService.asignarRoles(99L, Set.of(2L)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no puede revocar sus propios privilegios de administración");
    }

    // 10. Crear permiso
    @Test
    @DisplayName("10. Crear permiso granular para un módulo")
    void testCrearPermiso() {
        ModuloSistema modulo = ModuloSistema.builder().id(1L).codigo("MESA_PARTES").nombre("Mesa de Partes").build();
        when(permisoRepository.existsByCodigo("MESA_PARTES_MANAGE")).thenReturn(false);
        when(moduloRepository.findById(1L)).thenReturn(Optional.of(modulo));
        when(permisoRepository.save(any(Permiso.class))).thenAnswer(inv -> {
            Permiso p = inv.getArgument(0);
            p.setId(101L);
            return p;
        });

        PermisoResponse resp = permisoAdminService.crearPermiso(1L, "MESA_PARTES_MANAGE", "Gestión Completa", "MANAGE", "Desc");

        assertThat(resp).isNotNull();
        assertThat(resp.getCodigo()).isEqualTo("MESA_PARTES_MANAGE");
        verify(auditoriaService).registrar(eq("CREAR_PERMISO"), eq("PERMISO"), eq(101L), eq("EXITO"), anyString());
    }

    // 11. Asignar matriz de permisos a rol
    @Test
    @DisplayName("11. Asignar conjunto de permisos a un rol dinámico")
    void testAsignarPermisosARol() {
        Rol rol = Rol.builder().id(3L).codigo("ROLE_OPERADOR").nombre("Operador").permisos(new HashSet<>()).build();
        Permiso p1 = Permiso.builder().id(10L).codigo("MERCADOS_VIEW").build();
        Permiso p2 = Permiso.builder().id(11L).codigo("MERCADOS_CREATE").build();

        when(rolRepository.findWithPermisosById(3L)).thenReturn(Optional.of(rol));
        when(permisoRepository.findAllById(Set.of(10L, 11L))).thenReturn(List.of(p1, p2));
        when(rolRepository.save(any(Rol.class))).thenReturn(rol);
        when(usuarioRepository.findAll()).thenReturn(Collections.emptyList());

        RolResponse resp = rolAdminService.actualizarPermisosRol(3L, Set.of(10L, 11L));

        assertThat(resp).isNotNull();
        verify(auditoriaService).registrar(eq("ASIGNAR_PERMISOS_ROL"), eq("ROL"), eq(3L), eq("EXITO"), anyString());
    }

    // 12. Prevenir desactivación de rol crítico del sistema
    @Test
    @DisplayName("12. Denegar desactivación de rol crítico del sistema")
    void testDenegarDesactivacionRolCritico() {
        Rol superAdmin = Rol.builder().id(1L).codigo("ROLE_SUPERADMIN").estado("ACTIVO").build();
        when(rolRepository.findById(1L)).thenReturn(Optional.of(superAdmin));

        assertThatThrownBy(() -> rolAdminService.cambiarEstadoRol(1L, "INACTIVO"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("No está permitido desactivar los roles críticos del sistema");
    }

    // 13. Crear oficina institucional
    @Test
    @DisplayName("13. Crear oficina municipal exitosamente")
    void testCrearOficina() {
        Municipalidad muni = Municipalidad.builder().id(1L).nombre("Municipalidad de Quinua").build();
        OficinaCreateRequest req = OficinaCreateRequest.builder()
                .codigo("SG")
                .nombre("Secretaría General")
                .sigla("SG")
                .build();

        when(oficinaRepository.existsByCodigo("SG")).thenReturn(false);
        when(municipalidadRepository.findAll()).thenReturn(List.of(muni));
        when(oficinaRepository.save(any(Oficina.class))).thenAnswer(inv -> {
            Oficina o = inv.getArgument(0);
            o.setId(200L);
            return o;
        });

        OficinaResponse resp = oficinaAdminService.crearOficina(req);

        assertThat(resp).isNotNull();
        assertThat(resp.getCodigo()).isEqualTo("SG");
        verify(auditoriaService).registrar(eq("CREAR_OFICINA"), eq("OFICINA"), eq(200L), eq("EXITO"), anyString());
    }

    // 14. Detectar ciclo jerárquico de oficinas
    @Test
    @DisplayName("14. Detectar ciclo jerárquico al vincular oficinas dependientes")
    void testDetectarCicloJerarquicoOficinas() {
        // Oficina A (id 1) -> Padre es B (id 2) -> Padre es C (id 3)
        // Intentar que A sea su propio padre o que B dependa de A cuando A depende de B
        Oficina ofA = Oficina.builder().id(1L).codigo("OF_A").build();
        Oficina ofB = Oficina.builder().id(2L).codigo("OF_B").oficinaPadre(ofA).build();

        when(oficinaRepository.findById(1L)).thenReturn(Optional.of(ofA));
        when(oficinaRepository.findById(2L)).thenReturn(Optional.of(ofB));

        OficinaUpdateRequest req = OficinaUpdateRequest.builder()
                .nombre("Oficina A Modificada")
                .oficinaPadreId(2L) // Haría que A dependa de B, cuando B ya depende de A -> Ciclo
                .build();

        assertThatThrownBy(() -> oficinaAdminService.actualizarOficina(1L, req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ciclo");
    }

    // 15. Crear cargo institucional
    @Test
    @DisplayName("15. Crear cargo municipal dinámico")
    void testCrearCargo() {
        CargoCreateRequest req = CargoCreateRequest.builder()
                .codigo("SECRETARIO_GENERAL")
                .nombre("Secretario General")
                .nivelJerarquico(2)
                .build();

        when(cargoRepository.existsByCodigo("SECRETARIO_GENERAL")).thenReturn(false);
        when(cargoRepository.save(any(Cargo.class))).thenAnswer(inv -> {
            Cargo c = inv.getArgument(0);
            c.setId(15L);
            return c;
        });

        CargoResponse resp = cargoAdminService.crearCargo(req);

        assertThat(resp).isNotNull();
        assertThat(resp.getCodigo()).isEqualTo("SECRETARIO_GENERAL");
        verify(auditoriaService).registrar(eq("CREAR_CARGO"), eq("CARGO"), eq(15L), eq("EXITO"), anyString());
    }

    // 16. Crear tipo de actor institucional
    @Test
    @DisplayName("16. Crear tipo de actor institucional en catálogo dinámico")
    void testCrearTipoActor() {
        TipoActorCreateRequest req = TipoActorCreateRequest.builder()
                .codigo("COMERCIANTE_MERCADO")
                .nombre("Comerciante de Mercado")
                .descripcion("Conductor de puesto de abastos")
                .build();

        when(tipoActorRepository.existsByCodigo("COMERCIANTE_MERCADO")).thenReturn(false);
        when(tipoActorRepository.save(any(TipoActor.class))).thenAnswer(inv -> {
            TipoActor a = inv.getArgument(0);
            a.setId(8L);
            return a;
        });

        TipoActorResponse resp = tipoActorAdminService.crearTipoActor(req);

        assertThat(resp).isNotNull();
        assertThat(resp.getCodigo()).isEqualTo("COMERCIANTE_MERCADO");
        verify(auditoriaService).registrar(eq("CREAR_TIPO_ACTOR"), eq("TIPO_ACTOR"), eq(8L), eq("EXITO"), anyString());
    }

    // 17. Prevenir escalamiento de privilegios hacia ROLE_SUPERADMIN
    @Test
    @DisplayName("17. Prevenir escalamiento de privilegios cuando operador no es SUPERADMIN")
    void testPrevenirEscalamientoPrivilegios() {
        Rol rolSuperAdmin = Rol.builder().id(999L).codigo("ROLE_SUPERADMIN").nombre("Super Admin").build();

        // Operador autenticado sólo tiene ROLE_ADMIN, no ROLE_SUPERADMIN
        UsuarioCreateRequest req = UsuarioCreateRequest.builder()
                .username("nuevo_super")
                .password("SecPass123#")
                .tipoDocumento("DNI")
                .numeroDocumento("88888888")
                .nombres("Pedro")
                .apellidoPaterno("Gomez")
                .correo("pedro@quinua.gob.pe")
                .rolIds(Set.of(999L))
                .build();

        Persona persona = Persona.builder().id(88L).tipoDocumento("DNI").numeroDocumento("88888888").build();
        when(usuarioRepository.existsByUsername("nuevo_super")).thenReturn(false);
        when(personaRepository.findByTipoDocumentoAndNumeroDocumento("DNI", "88888888")).thenReturn(Optional.of(persona));
        when(usuarioRepository.existsByPersonaId(88L)).thenReturn(false);
        when(rolRepository.findAllById(Set.of(999L))).thenReturn(List.of(rolSuperAdmin));

        assertThatThrownBy(() -> usuarioAdminService.registrarUsuario(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("No tiene privilegios para asignar el rol ROLE_SUPERADMIN");
    }
}
