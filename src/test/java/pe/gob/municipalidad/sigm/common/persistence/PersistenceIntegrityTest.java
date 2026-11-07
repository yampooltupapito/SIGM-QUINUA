package pe.gob.municipalidad.sigm.common.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
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

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class PersistenceIntegrityTest {

    @Autowired
    private MunicipalidadRepository municipalidadRepository;

    @Autowired
    private OficinaRepository oficinaRepository;

    @Autowired
    private CargoRepository cargoRepository;

    @Autowired
    private TipoActorRepository tipoActorRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PersonaOficinaRepository personaOficinaRepository;

    @Autowired
    private ModuloSistemaRepository moduloSistemaRepository;

    @Autowired
    private PermisoRepository permisoRepository;

    @Autowired
    private RolRepository rolRepository;

    @Test
    @DisplayName("Los módulos municipales deben ser sembrados por Flyway")
    void flywayShouldSeedInitialModules() {
        var modulos = moduloSistemaRepository.findAll();
        assertEquals(11, modulos.size(), "Deben existir 11 módulos institucionales (incluyendo Mercados Municipales)");

        assertTrue(moduloSistemaRepository.existsByCodigo("TRAMITE_DOCUMENTARIO"));
        assertTrue(moduloSistemaRepository.existsByCodigo("RENTAS_TRIBUTACION"));
        assertTrue(moduloSistemaRepository.existsByCodigo("ADMINISTRACION_SISTEMA"));
        assertTrue(moduloSistemaRepository.existsByCodigo("MERCADOS_MUNICIPALES"));
    }

    @Test
    @DisplayName("El catálogo dinámico de actores debe ser sembrado por Flyway")
    void flywayShouldSeedInitialActorTypes() {
        var actores = tipoActorRepository.findAll();
        assertFalse(actores.isEmpty(), "Deben existir tipos de actores sembrados por Flyway");
        assertTrue(tipoActorRepository.existsByCodigo("CIUDADANO"));
        assertTrue(tipoActorRepository.existsByCodigo("TRABAJADOR_MUNICIPAL"));
    }

    @Test
    @DisplayName("Debe persistir Municipalidad con auditoría de creación")
    void shouldPersistMunicipalidad() {
        Municipalidad muni = Municipalidad.builder()
                .nombre("Municipalidad Distrital de Quinua")
                .nombreCorto("Muni Quinua")
                .ubigeo("050108")
                .departamento("Ayacucho")
                .provincia("Huamanga")
                .distrito("Quinua")
                .estado("ACTIVO")
                .build();

        Municipalidad guardada = municipalidadRepository.save(muni);

        assertNotNull(guardada.getId());
        assertNotNull(guardada.getCreatedAt(), "Auditoría debe registrar createdAt automáticamente");
        assertEquals("050108", guardada.getUbigeo());
    }

    @Test
    @DisplayName("Jerarquía de Oficinas: Una oficina hija debe vincularse correctamente a su oficina padre")
    void shouldPersistHierarchicalOficinas() {
        Municipalidad muni = municipalidadRepository.save(Municipalidad.builder()
                .nombre("Muni Quinua")
                .nombreCorto("MQ")
                .ubigeo("050108")
                .departamento("Ayacucho")
                .provincia("Huamanga")
                .distrito("Quinua")
                .build());

        Oficina gerencia = oficinaRepository.save(Oficina.builder()
                .municipalidad(muni)
                .codigo("GM")
                .nombre("Gerencia Municipal")
                .nivelJerarquico(1)
                .build());

        Oficina subgerencia = oficinaRepository.save(Oficina.builder()
                .municipalidad(muni)
                .oficinaPadre(gerencia)
                .codigo("SGRT")
                .nombre("Subgerencia de Rentas y Tributación")
                .nivelJerarquico(2)
                .build());

        assertNotNull(subgerencia.getId());
        assertEquals("GM", subgerencia.getOficinaPadre().getCodigo());

        var hijas = oficinaRepository.findByOficinaPadreId(gerencia.getId());
        assertEquals(1, hijas.size());
        assertEquals("SGRT", hijas.get(0).getCodigo());
    }

    @Test
    @DisplayName("Código de Oficina duplicado debe lanzar DataIntegrityViolationException")
    void duplicateOficinaCodigoShouldThrowException() {
        Municipalidad muni = municipalidadRepository.save(Municipalidad.builder()
                .nombre("Muni")
                .nombreCorto("M")
                .ubigeo("050108")
                .departamento("Ayacucho")
                .provincia("Huamanga")
                .distrito("Quinua")
                .build());

        oficinaRepository.save(Oficina.builder()
                .municipalidad(muni)
                .codigo("ALC")
                .nombre("Alcaldía")
                .build());

        Oficina duplicada = Oficina.builder()
                .municipalidad(muni)
                .codigo("ALC")
                .nombre("Otra Alcaldía")
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            oficinaRepository.saveAndFlush(duplicada);
        });
    }

    @Test
    @DisplayName("Persona con documento único debe registrarse y duplicado debe fallar")
    void shouldPersistPersonaAndEnforceUniqueDocument() {
        Persona persona1 = Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("45892134")
                .nombres("Juan Carlos")
                .apellidoPaterno("Quispe")
                .apellidoMaterno("Mendoza")
                .correo("jquispe@muniquinua.gob.pe")
                .build();

        Persona guardada = personaRepository.save(persona1);
        assertNotNull(guardada.getId());
        assertEquals("Juan Carlos Quispe Mendoza", guardada.getNombreCompleto());

        Persona duplicada = Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("45892134")
                .nombres("Otro Juan")
                .apellidoPaterno("Quispe")
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            personaRepository.saveAndFlush(duplicada);
        });
    }

    @Test
    @DisplayName("Relación 1:1 Usuario - Persona: Usuario debe vincularse a Persona y username debe ser único")
    void shouldPersistUsuarioLinkedToPersona() {
        Persona persona = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("10293847")
                .nombres("Rosa")
                .apellidoPaterno("Flores")
                .build());

        Usuario usuario = Usuario.builder()
                .persona(persona)
                .username("rflores")
                .passwordHash("$2a$12$e0MYzXyjpJS7Pd0RVvHwHe9h3G3vXFzZqZqZqZqZqZqZqZqZqZqZq")
                .enabled(true)
                .build();

        Usuario guardado = usuarioRepository.save(usuario);
        assertNotNull(guardado.getId());
        assertEquals("rflores", guardado.getUsername());
        assertNotNull(guardado.getCreatedAt());

        Optional<Usuario> encontrado = usuarioRepository.findByUsername("rflores");
        assertTrue(encontrado.isPresent());
        assertEquals(persona.getId(), encontrado.get().getPersona().getId());
    }

    @Test
    @DisplayName("Relación M:N Rol - Permisos y Usuario - Roles")
    void shouldManageUserRolesAndRolePermissions() {
        ModuloSistema modulo = moduloSistemaRepository.findByCodigo("TRAMITE_DOCUMENTARIO").orElseThrow();

        Permiso permisoView = permisoRepository.save(Permiso.builder()
                .modulo(modulo)
                .codigo("TRAMITE_VIEW")
                .nombre("Ver Trámites")
                .accion("VIEW")
                .build());

        Permiso permisoCreate = permisoRepository.save(Permiso.builder()
                .modulo(modulo)
                .codigo("TRAMITE_CREATE")
                .nombre("Crear Trámites")
                .accion("CREATE")
                .build());

        Rol rolOperador = rolRepository.findByCodigo("ROLE_OPERADOR").orElseThrow();
        rolOperador.getPermisos().add(permisoView);
        rolOperador.getPermisos().add(permisoCreate);
        rolRepository.saveAndFlush(rolOperador);

        Persona persona = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("77889900")
                .nombres("Carlos")
                .apellidoPaterno("Soto")
                .build());

        Usuario usuario = Usuario.builder()
                .persona(persona)
                .username("csoto")
                .passwordHash("$2a$12$securehash123")
                .roles(Set.of(rolOperador))
                .build();

        Usuario usuarioGuardado = usuarioRepository.saveAndFlush(usuario);

        Usuario recuperado = usuarioRepository.findById(usuarioGuardado.getId()).orElseThrow();
        assertEquals(1, recuperado.getRoles().size());
        assertTrue(recuperado.getRoles().contains(rolOperador));
    }

    @Test
    @DisplayName("Asignación de Persona a Oficina con Cargo y Tipo de Actor")
    void shouldPersistPersonaOficinaAssignment() {
        Municipalidad muni = municipalidadRepository.save(Municipalidad.builder()
                .nombre("Muni")
                .nombreCorto("MQ")
                .ubigeo("050108")
                .departamento("Ayacucho")
                .provincia("Huamanga")
                .distrito("Quinua")
                .build());

        Oficina oficina = oficinaRepository.save(Oficina.builder()
                .municipalidad(muni)
                .codigo("MP")
                .nombre("Mesa de Partes")
                .build());

        Cargo cargo = cargoRepository.save(Cargo.builder()
                .codigo("ASIST_ADM")
                .nombre("Asistente Administrativo")
                .build());

        TipoActor actor = tipoActorRepository.findByCodigo("TRABAJADOR_MUNICIPAL").orElseThrow();

        Persona persona = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("33445566")
                .nombres("Ana")
                .apellidoPaterno("Gutiérrez")
                .build());

        PersonaOficina asignacion = PersonaOficina.builder()
                .persona(persona)
                .oficina(oficina)
                .cargo(cargo)
                .tipoActor(actor)
                .fechaInicio(LocalDate.of(2026, 1, 1))
                .principal(true)
                .build();

        PersonaOficina guardada = personaOficinaRepository.save(asignacion);
        assertNotNull(guardada.getId());
        assertEquals("MP", guardada.getOficina().getCodigo());
        assertEquals("ASIST_ADM", guardada.getCargo().getCodigo());
        assertEquals("TRABAJADOR_MUNICIPAL", guardada.getTipoActor().getCodigo());
    }
}
