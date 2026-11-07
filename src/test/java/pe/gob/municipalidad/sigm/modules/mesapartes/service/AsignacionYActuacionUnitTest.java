package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.*;
import pe.gob.municipalidad.sigm.common.notification.service.CentralNotificationService;
import pe.gob.municipalidad.sigm.common.repository.PersonaOficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.DerivacionDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoAsignacion;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoActuacionExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.VisibilidadObservacion;
import pe.gob.municipalidad.sigm.modules.mesapartes.mapper.DerivacionMapper;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ActuacionDocumentoRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ActuacionExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.AsignacionExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteHistorialRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.impl.ActuacionExpedienteServiceImpl;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.impl.AsignacionExpedienteServiceImpl;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsignacionYActuacionUnitTest {

    @Mock
    private AsignacionExpedienteRepository asignacionRepository;
    @Mock
    private ExpedienteRepository expedienteRepository;
    @Mock
    private ExpedienteHistorialRepository historialRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PersonaOficinaRepository personaOficinaRepository;
    @Mock
    private CentralNotificationService notificationService;
    @Mock
    private DerivacionMapper derivacionMapper;

    @InjectMocks
    private AsignacionExpedienteServiceImpl asignacionService;

    @Mock
    private ActuacionExpedienteRepository actuacionRepository;
    @Mock
    private ActuacionDocumentoRepository actuacionDocumentoRepository;
    @Mock
    private pe.gob.municipalidad.sigm.common.document.repository.DocumentoRepository documentoRepository;

    private ActuacionExpedienteServiceImpl actuacionService;

    private Usuario jefe;
    private Usuario trabajador;
    private Oficina oficina;
    private Expediente expediente;
    private Persona personaTrabajador;

    @BeforeEach
    void setUp() {
        actuacionService = new ActuacionExpedienteServiceImpl(
                actuacionRepository, actuacionDocumentoRepository, expedienteRepository,
                historialRepository, documentoRepository, usuarioRepository,
                personaOficinaRepository, derivacionMapper
        );

        Persona personaJefe = Persona.builder().id(1L).nombres("Jefe").apellidoPaterno("Oficina").build();
        Rol rolAdmin = Rol.builder().id(1L).codigo("ROLE_ADMIN").nombre("Administrador").build();

        jefe = Usuario.builder()
                .id(1L)
                .username("jefe_desurb")
                .persona(personaJefe)
                .roles(Set.of(rolAdmin))
                .enabled(true)
                .build();

        personaTrabajador = Persona.builder().id(2L).nombres("Especialista").apellidoPaterno("Tecnico").build();
        trabajador = Usuario.builder()
                .id(2L)
                .username("esp_tecnico")
                .persona(personaTrabajador)
                .roles(Set.of(Rol.builder().id(2L).codigo("ROLE_OPERADOR").build()))
                .enabled(true)
                .accountLocked(false)
                .build();

        oficina = Oficina.builder().id(200L).codigo("DESURB").nombre("Desarrollo Urbano").estado("ACTIVO").build();

        expediente = Expediente.builder()
                .id(50L)
                .numeroExpediente("MPV-2026-00001")
                .asunto("Evaluación de Edificación")
                .estadoActual(EstadoExpediente.EN_ATENCION)
                .oficinaResponsable(oficina)
                .build();
    }

    @Test
    @DisplayName("Asignar responsable: Exitoso y registra en historial")
    void testAsignarResponsableExitoso() {
        when(usuarioRepository.findByUsername("jefe_desurb")).thenReturn(Optional.of(jefe));
        when(expedienteRepository.findByIdWithDetalles(50L)).thenReturn(Optional.of(expediente));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(trabajador));

        PersonaOficina po = PersonaOficina.builder().persona(personaTrabajador).oficina(oficina).estado("ACTIVO").build();
        when(personaOficinaRepository.findByPersonaIdAndEstado(2L, "ACTIVO")).thenReturn(List.of(po));

        AsignacionExpediente asigGuardada = AsignacionExpediente.builder()
                .id(1L)
                .expediente(expediente)
                .usuarioAsignador(jefe)
                .usuarioAsignado(trabajador)
                .estado(EstadoAsignacion.ACTIVO)
                .build();

        when(asignacionRepository.save(any(AsignacionExpediente.class))).thenReturn(asigGuardada);
        when(derivacionMapper.toAsignacionResponse(any(AsignacionExpediente.class)))
                .thenReturn(AsignacionResponse.builder().id(1L).estado(EstadoAsignacion.ACTIVO).build());

        AsignarExpedienteRequest req = AsignarExpedienteRequest.builder()
                .usuarioAsignadoId(2L)
                .indicaciones("Emitir informe en 3 días")
                .build();

        AsignacionResponse resp = asignacionService.asignarResponsable(50L, req, "jefe_desurb");

        assertNotNull(resp);
        assertEquals(trabajador, expediente.getResponsableActual());
        verify(historialRepository).save(any(ExpedienteHistorial.class));
    }

    @Test
    @DisplayName("Asignar responsable: Falla si trabajador no pertenece a la oficina")
    void testAsignarResponsableOficinaAjenaFalla() {
        when(usuarioRepository.findByUsername("jefe_desurb")).thenReturn(Optional.of(jefe));
        when(expedienteRepository.findByIdWithDetalles(50L)).thenReturn(Optional.of(expediente));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(trabajador));
        when(personaOficinaRepository.findByPersonaIdAndEstado(2L, "ACTIVO")).thenReturn(List.of()); // Sin asignación en esta oficina

        AsignarExpedienteRequest req = AsignarExpedienteRequest.builder()
                .usuarioAsignadoId(2L)
                .build();

        assertThrows(BusinessException.class, () -> asignacionService.asignarResponsable(50L, req, "jefe_desurb"));
    }

    @Test
    @DisplayName("Registrar actuación: Guarda informe técnico con visibilidad interna")
    void testRegistrarActuacionExitoso() {
        when(usuarioRepository.findByUsername("jefe_desurb")).thenReturn(Optional.of(jefe));
        when(expedienteRepository.findByIdWithDetalles(50L)).thenReturn(Optional.of(expediente));

        ActuacionExpediente guardada = ActuacionExpediente.builder()
                .id(1L)
                .tipo(TipoActuacionExpediente.INFORME)
                .descripcion("Informe de inspección ocular favorable.")
                .visibilidad(VisibilidadObservacion.INTERNA)
                .build();

        when(actuacionRepository.save(any(ActuacionExpediente.class))).thenReturn(guardada);
        when(derivacionMapper.toActuacionResponse(any(ActuacionExpediente.class)))
                .thenReturn(ActuacionResponse.builder().id(1L).tipo(TipoActuacionExpediente.INFORME).build());

        RegistrarActuacionRequest req = RegistrarActuacionRequest.builder()
                .tipo(TipoActuacionExpediente.INFORME)
                .descripcion("Informe de inspección ocular favorable.")
                .visibilidad(VisibilidadObservacion.INTERNA)
                .build();

        ActuacionResponse resp = actuacionService.registrarActuacion(50L, req, "jefe_desurb");

        assertNotNull(resp);
        assertEquals(TipoActuacionExpediente.INFORME, resp.getTipo());
        verify(historialRepository).save(any(ExpedienteHistorial.class));
    }
}
