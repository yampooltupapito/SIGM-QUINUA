package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.notificacion.ValidarProcedimientoRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.NotificacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.TipoNotificacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ValidacionProcedimientoNotificacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.CategoriaTipoNotificacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoValidezLegalNotificacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.ResultadoValidacionProcedimental;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.NotificacionTributariaRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ValidacionProcedimientoNotificacionRepository;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidacionProcedimientoTest {

    @Mock
    private ValidacionProcedimientoNotificacionRepository validacionRepository;
    @Mock
    private NotificacionTributariaRepository notificacionRepository;
    @Mock
    private HistorialNotificacionService historialService;
    @Mock
    private PlazoNotificacionService plazoService;

    @InjectMocks
    private ValidacionNotificacionService validacionService;

    private NotificacionTributaria notificacion;
    private Usuario revisor;

    @BeforeEach
    void setUp() {
        revisor = Usuario.builder().id(2L).username("asesor_legal").build();

        TipoNotificacionTributaria tipo = TipoNotificacionTributaria.builder()
                .id(UUID.randomUUID())
                .codigo("NOTIF_ORDEN_PAGO")
                .nombre("Orden de Pago")
                .categoria(CategoriaTipoNotificacion.NOTIFICACION_FORMAL)
                .requiereValidacionLegal(true)
                .build();

        notificacion = NotificacionTributaria.builder()
                .id(UUID.randomUUID())
                .codigoNotificacion("NOTIF-2026-000001")
                .tipoNotificacion(tipo)
                .estadoValidezLegal(EstadoValidezLegalNotificacion.PENDIENTE_VERIFICACION)
                .build();
    }

    @Test
    @DisplayName("No debe permitir calificar como CONFORME_VALIDA si faltan requisitos indispensables de checklist")
    void testRechazoValidacionSiFaltaChecklist() {
        when(notificacionRepository.findById(notificacion.getId())).thenReturn(Optional.of(notificacion));

        ValidarProcedimientoRequest req = ValidarProcedimientoRequest.builder()
                .resultado(ResultadoValidacionProcedimental.CONFORME_VALIDA)
                .sustento("Entrega conforme")
                .cumpleDestinatarioIdentificado(true)
                .cumpleDomicilioValido(false) // Falla domicilio
                .cumpleEvidenciaMinima(true)
                .build();

        assertThatThrownBy(() -> validacionService.calificarProcedimiento(notificacion.getId(), req, revisor))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("No puede declararse formalmente válida si no cumple los requisitos indispensables");
    }

    @Test
    @DisplayName("Debe calificar formalmente como válida cuando el checklist está completo y fundamentado")
    void testValidacionExitosaConChecklist() {
        when(notificacionRepository.findById(notificacion.getId())).thenReturn(Optional.of(notificacion));
        when(validacionRepository.save(any(ValidacionProcedimientoNotificacion.class))).thenAnswer(i -> i.getArgument(0));

        ValidarProcedimientoRequest req = ValidarProcedimientoRequest.builder()
                .resultado(ResultadoValidacionProcedimental.CONFORME_VALIDA)
                .sustento("Cumple requisitos del Art. 104 del Código Tributario")
                .cumpleDestinatarioIdentificado(true)
                .cumpleDomicilioValido(true)
                .cumpleDocumentoAdjunto(true)
                .cumplePlazoProcedimental(true)
                .cumpleEvidenciaMinima(true)
                .cumpleDatosReceptorYCargo(true)
                .build();

        ValidacionProcedimientoNotificacion result = validacionService.calificarProcedimiento(notificacion.getId(), req, revisor);

        assertThat(result).isNotNull();
        assertThat(result.getResultado()).isEqualTo(ResultadoValidacionProcedimental.CONFORME_VALIDA);
        assertThat(notificacion.getEstadoValidezLegal()).isEqualTo(EstadoValidezLegalNotificacion.VALIDADA_POR_RESPONSABLE);
        verify(notificacionRepository).save(notificacion);
    }
}

