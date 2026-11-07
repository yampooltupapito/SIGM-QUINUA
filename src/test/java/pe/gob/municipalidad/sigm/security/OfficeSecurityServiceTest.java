package pe.gob.municipalidad.sigm.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.PersonaOficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.PersonaOficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.security.authorization.OfficeSecurityService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias de Autorización por Oficina - SIGM-QUINUA")
class OfficeSecurityServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PersonaOficinaRepository personaOficinaRepository;

    @InjectMocks
    private OfficeSecurityService officeSecurityService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("17. Acceso a otra oficina: Denegado cuando el trabajador no pertenece a la unidad orgánica")
    void shouldDenyAccessToResourceOfDifferentOffice() {
        // Usuario autenticado con rol de operador municipal asignado a la Oficina 10 (Rentas)
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "operador.rentas",
                "credencial",
                List.of(new SimpleGrantedAuthority("ROLE_OPERADOR"), new SimpleGrantedAuthority("TRIBUTACION_VIEW"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        Persona persona = Persona.builder().id(50L).nombres("Ana").apellidoPaterno("Quispe").build();
        Usuario usuario = Usuario.builder().id(5L).username("operador.rentas").persona(persona).build();

        Oficina oficinaRentas = Oficina.builder().id(10L).codigo("RENTAS").build();
        PersonaOficina asignacionRentas = PersonaOficina.builder()
                .persona(persona)
                .oficina(oficinaRentas)
                .estado("ACTIVO")
                .build();

        when(usuarioRepository.findByUsername("operador.rentas")).thenReturn(Optional.of(usuario));
        when(personaOficinaRepository.findByPersonaId(50L)).thenReturn(List.of(asignacionRentas));

        // Intenta acceder a expediente de la Oficina 20 (Mesa de Partes / Trámite Documentario)
        boolean hasAccessToOtherOffice = officeSecurityService.canAccessResourceOfOffice(20L);
        assertThat(hasAccessToOtherOffice).isFalse();

        // Intenta acceder a expediente de su propia Oficina 10 (Rentas)
        boolean hasAccessToOwnOffice = officeSecurityService.canAccessResourceOfOffice(10L);
        assertThat(hasAccessToOwnOffice).isTrue();
    }

    @Test
    @DisplayName("17. Acceso a oficina transversal: Permitido para Administrador General (ROLE_ADMIN)")
    void shouldAllowAccessToAnyOfficeForSystemAdmin() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "admin.quinua",
                "credencial",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ADMINISTRACION_MANAGE"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Administrador tiene acceso transversal sin requerir asignación a esa oficina específica
        boolean hasAccess = officeSecurityService.canAccessResourceOfOffice(999L);
        assertThat(hasAccess).isTrue();
    }

    @Test
    @DisplayName("Acceso anónimo: Denegado si no hay usuario autenticado")
    void shouldDenyAccessWhenAnonymous() {
        boolean hasAccess = officeSecurityService.canAccessResourceOfOffice(10L);
        assertThat(hasAccess).isFalse();
    }
}
