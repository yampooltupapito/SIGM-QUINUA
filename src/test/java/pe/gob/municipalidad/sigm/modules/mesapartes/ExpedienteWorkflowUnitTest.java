package pe.gob.municipalidad.sigm.modules.mesapartes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.Expediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.ExpedienteHistorial;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteHistorialRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.workflow.ExpedienteWorkflowService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpedienteWorkflowUnitTest {

    @Mock
    private ExpedienteRepository expedienteRepository;

    @Mock
    private ExpedienteHistorialRepository historialRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ExpedienteWorkflowService workflowService;

    @BeforeEach
    void setUp() {
        workflowService = new ExpedienteWorkflowService(expedienteRepository, historialRepository, eventPublisher);
    }

    @Test
    @DisplayName("Debe validar transiciones legales del grafo de estados")
    void testTransicionesLegales() {
        assertThat(workflowService.esTransicionPermitida(EstadoExpediente.BORRADOR, EstadoExpediente.PRESENTADO)).isTrue();
        assertThat(workflowService.esTransicionPermitida(EstadoExpediente.PRESENTADO, EstadoExpediente.RECIBIDO)).isTrue();
        assertThat(workflowService.esTransicionPermitida(EstadoExpediente.RECIBIDO, EstadoExpediente.DERIVADO)).isTrue();
        assertThat(workflowService.esTransicionPermitida(EstadoExpediente.RECIBIDO, EstadoExpediente.OBSERVADO)).isTrue();
        assertThat(workflowService.esTransicionPermitida(EstadoExpediente.OBSERVADO, EstadoExpediente.EN_SUBSANACION)).isTrue();
        assertThat(workflowService.esTransicionPermitida(EstadoExpediente.EN_SUBSANACION, EstadoExpediente.EN_REVISION)).isTrue();
        assertThat(workflowService.esTransicionPermitida(EstadoExpediente.EN_ATENCION, EstadoExpediente.RESUELTO)).isTrue();
        assertThat(workflowService.esTransicionPermitida(EstadoExpediente.RESUELTO, EstadoExpediente.ARCHIVADO)).isTrue();
    }

    @Test
    @DisplayName("Debe rechazar transiciones ilegales")
    void testTransicionesIlegales() {
        assertThat(workflowService.esTransicionPermitida(EstadoExpediente.BORRADOR, EstadoExpediente.ARCHIVADO)).isFalse();
        assertThat(workflowService.esTransicionPermitida(EstadoExpediente.ARCHIVADO, EstadoExpediente.BORRADOR)).isFalse();
        assertThat(workflowService.esTransicionPermitida(EstadoExpediente.ANULADO, EstadoExpediente.PRESENTADO)).isFalse();
        assertThat(workflowService.esTransicionPermitida(EstadoExpediente.RESUELTO, EstadoExpediente.EN_REVISION)).isFalse();
    }

    @Test
    @DisplayName("Debe lanzar BusinessException al intentar transición no permitida")
    void testLanzarExcepcionEnTransicionInvalida() {
        Expediente expediente = Expediente.builder()
                .id(10L)
                .numeroExpediente("MPV-2026-000010")
                .estadoActual(EstadoExpediente.ARCHIVADO)
                .build();

        Usuario usuario = Usuario.builder().id(1L).username("admin").build();

        assertThatThrownBy(() -> workflowService.transicionar(expediente, EstadoExpediente.BORRADOR, "REVERTIR", "No permitido", usuario, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Transición no permitida");

        verifyNoInteractions(historialRepository);
    }

    @Test
    @DisplayName("Debe ejecutar transición válida, registrar historial y publicar evento")
    void testTransicionValidaExitosa() {
        Expediente expediente = Expediente.builder()
                .id(1L)
                .numeroExpediente("MPV-2026-000001")
                .asunto("Solicitud de constancia de posesión")
                .estadoActual(EstadoExpediente.BORRADOR)
                .build();

        Usuario usuario = Usuario.builder().id(2L).username("ciudadano1").build();
        Oficina oficina = Oficina.builder().id(3L).nombre("Mesa de Partes").build();

        when(expedienteRepository.save(any(Expediente.class))).thenAnswer(i -> i.getArgument(0));

        Expediente resultado = workflowService.transicionar(expediente, EstadoExpediente.PRESENTADO, "PRESENTAR", "Presentación ciudadana", usuario, oficina);

        assertThat(resultado.getEstadoActual()).isEqualTo(EstadoExpediente.PRESENTADO);
        assertThat(resultado.getFechaPresentacion()).isNotNull();

        verify(expedienteRepository).save(expediente);
        verify(historialRepository).save(any(ExpedienteHistorial.class));
        verify(eventPublisher).publishEvent(any());
    }
}
