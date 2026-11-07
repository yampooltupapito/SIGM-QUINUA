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
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.PersonaOficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.DerivacionDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoDerivacion;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.mapper.DerivacionMapper;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.DerivacionEventoRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.DerivacionExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.impl.DerivacionExpedienteServiceImpl;
import pe.gob.municipalidad.sigm.modules.mesapartes.workflow.ExpedienteWorkflowService;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DerivacionExpedienteUnitTest {

    @Mock
    private DerivacionExpedienteRepository derivacionRepository;
    @Mock
    private DerivacionEventoRepository derivacionEventoRepository;
    @Mock
    private ExpedienteRepository expedienteRepository;
    @Mock
    private OficinaRepository oficinaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PersonaOficinaRepository personaOficinaRepository;
    @Mock
    private ExpedienteWorkflowService workflowService;
    @Mock
    private CentralNotificationService notificationService;
    @Mock
    private DerivacionMapper derivacionMapper;

    @InjectMocks
    private DerivacionExpedienteServiceImpl derivacionService;

    private Usuario operador;
    private Oficina oficinaOrigen;
    private Oficina oficinaDestino;
    private Expediente expediente;

    @BeforeEach
    void setUp() {
        Persona persona = Persona.builder().id(10L).nombres("Carlos").apellidoPaterno("Quispe").build();
        Rol rolAdmin = Rol.builder().id(1L).codigo("ROLE_ADMIN").nombre("Administrador").build();

        operador = Usuario.builder()
                .id(1L)
                .username("operador_mpv")
                .persona(persona)
                .roles(Set.of(rolAdmin))
                .enabled(true)
                .build();

        oficinaOrigen = Oficina.builder().id(100L).codigo("MPV").nombre("Mesa de Partes").estado("ACTIVO").build();
        oficinaDestino = Oficina.builder().id(200L).codigo("DESURB").nombre("Desarrollo Urbano").estado("ACTIVO").build();

        expediente = Expediente.builder()
                .id(50L)
                .numeroExpediente("MPV-2026-00001")
                .asunto("Licencia de Construcción")
                .estadoActual(EstadoExpediente.RECIBIDO)
                .oficinaResponsable(oficinaOrigen)
                .build();
    }

    @Test
    @DisplayName("Derivar expediente: Exitoso cuando cumple condiciones")
    void testDerivarExpedienteExitoso() {
        when(usuarioRepository.findByUsername("operador_mpv")).thenReturn(Optional.of(operador));
        when(expedienteRepository.findByIdWithDetalles(50L)).thenReturn(Optional.of(expediente));
        when(oficinaRepository.findById(200L)).thenReturn(Optional.of(oficinaDestino));
        when(derivacionRepository.findFirstByExpedienteIdAndEstado(50L, EstadoDerivacion.PENDIENTE)).thenReturn(Optional.empty());

        DerivacionExpediente derivGuardada = DerivacionExpediente.builder()
                .id(1L)
                .expediente(expediente)
                .oficinaOrigen(oficinaOrigen)
                .oficinaDestino(oficinaDestino)
                .usuarioDerivador(operador)
                .motivo("Para informe técnico")
                .estado(EstadoDerivacion.PENDIENTE)
                .build();

        when(derivacionRepository.save(any(DerivacionExpediente.class))).thenReturn(derivGuardada);
        when(derivacionMapper.toDerivacionResponse(any(DerivacionExpediente.class)))
                .thenReturn(DerivacionResponse.builder().id(1L).estado(EstadoDerivacion.PENDIENTE).build());

        DerivarExpedienteRequest req = DerivarExpedienteRequest.builder()
                .oficinaDestinoId(200L)
                .motivo("Para informe técnico")
                .build();

        DerivacionResponse resp = derivacionService.derivarExpediente(50L, req, "operador_mpv");

        assertNotNull(resp);
        assertEquals(EstadoDerivacion.PENDIENTE, resp.getEstado());
        verify(workflowService).transicionar(eq(expediente), eq(EstadoExpediente.DERIVADO), anyString(), anyString(), eq(operador), eq(oficinaOrigen));
        verify(derivacionEventoRepository).save(any(DerivacionEvento.class));
    }

    @Test
    @DisplayName("Derivar expediente: Falla si la oficina de destino es la misma que la origen")
    void testDerivarExpedienteMismaOficinaRechazada() {
        when(usuarioRepository.findByUsername("operador_mpv")).thenReturn(Optional.of(operador));
        when(expedienteRepository.findByIdWithDetalles(50L)).thenReturn(Optional.of(expediente));
        when(oficinaRepository.findById(100L)).thenReturn(Optional.of(oficinaOrigen));

        DerivarExpedienteRequest req = DerivarExpedienteRequest.builder()
                .oficinaDestinoId(100L)
                .motivo("Misma oficina")
                .build();

        assertThrows(BusinessException.class, () -> derivacionService.derivarExpediente(50L, req, "operador_mpv"));
    }

    @Test
    @DisplayName("Derivar expediente: Falla si ya existe derivación pendiente para el expediente")
    void testDerivarExpedienteDuplicadaRechazada() {
        when(usuarioRepository.findByUsername("operador_mpv")).thenReturn(Optional.of(operador));
        when(expedienteRepository.findByIdWithDetalles(50L)).thenReturn(Optional.of(expediente));
        when(oficinaRepository.findById(200L)).thenReturn(Optional.of(oficinaDestino));

        DerivacionExpediente existente = DerivacionExpediente.builder().oficinaDestino(oficinaDestino).build();
        when(derivacionRepository.findFirstByExpedienteIdAndEstado(50L, EstadoDerivacion.PENDIENTE)).thenReturn(Optional.of(existente));

        DerivarExpedienteRequest req = DerivarExpedienteRequest.builder()
                .oficinaDestinoId(200L)
                .motivo("Intento duplicado")
                .build();

        assertThrows(BusinessException.class, () -> derivacionService.derivarExpediente(50L, req, "operador_mpv"));
    }

    @Test
    @DisplayName("Recibir derivación: Acepta y transiciona a EN_ATENCION")
    void testRecibirDerivacionExitoso() {
        DerivacionExpediente derivacion = DerivacionExpediente.builder()
                .id(1L)
                .expediente(expediente)
                .oficinaOrigen(oficinaOrigen)
                .oficinaDestino(oficinaDestino)
                .usuarioDerivador(operador)
                .estado(EstadoDerivacion.PENDIENTE)
                .build();

        when(usuarioRepository.findByUsername("operador_mpv")).thenReturn(Optional.of(operador));
        when(derivacionRepository.findById(1L)).thenReturn(Optional.of(derivacion));
        when(derivacionRepository.save(any(DerivacionExpediente.class))).thenReturn(derivacion);
        when(derivacionMapper.toDerivacionResponse(any(DerivacionExpediente.class)))
                .thenReturn(DerivacionResponse.builder().id(1L).estado(EstadoDerivacion.RECIBIDA).build());

        RecepcionDerivacionRequest req = RecepcionDerivacionRequest.builder().observaciones("Conforme en despacho").build();
        DerivacionResponse resp = derivacionService.recibirDerivacion(1L, req, "operador_mpv");

        assertNotNull(resp);
        assertEquals(EstadoDerivacion.RECIBIDA, resp.getEstado());
        assertEquals(oficinaDestino, expediente.getOficinaResponsable());
        verify(workflowService).transicionar(eq(expediente), eq(EstadoExpediente.EN_ATENCION), anyString(), anyString(), eq(operador), eq(oficinaDestino));
    }

    @Test
    @DisplayName("Devolver derivación: Requiere motivo obligatorio y retorna a oficina origen")
    void testDevolverDerivacionConMotivo() {
        DerivacionExpediente derivacion = DerivacionExpediente.builder()
                .id(1L)
                .expediente(expediente)
                .oficinaOrigen(oficinaOrigen)
                .oficinaDestino(oficinaDestino)
                .usuarioDerivador(operador)
                .estado(EstadoDerivacion.PENDIENTE)
                .build();

        when(usuarioRepository.findByUsername("operador_mpv")).thenReturn(Optional.of(operador));
        when(derivacionRepository.findById(1L)).thenReturn(Optional.of(derivacion));
        when(derivacionRepository.save(any(DerivacionExpediente.class))).thenReturn(derivacion);
        when(derivacionMapper.toDerivacionResponse(any(DerivacionExpediente.class)))
                .thenReturn(DerivacionResponse.builder().id(1L).estado(EstadoDerivacion.DEVUELTA).build());

        RechazoDevolucionRequest req = RechazoDevolucionRequest.builder().motivo("Falta informe de rentas").build();
        DerivacionResponse resp = derivacionService.devolverDerivacion(1L, req, "operador_mpv");

        assertNotNull(resp);
        assertEquals(EstadoDerivacion.DEVUELTA, resp.getEstado());
        assertEquals(oficinaOrigen, expediente.getOficinaResponsable());
        verify(workflowService).transicionar(eq(expediente), eq(EstadoExpediente.RECIBIDO), anyString(), anyString(), eq(operador), eq(oficinaDestino));
    }
}
