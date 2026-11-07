package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.notificacion.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionTributariaServiceTest {

    @Mock
    private NotificacionTributariaRepository notificacionRepository;
    @Mock
    private DetalleNotificacionObligacionRepository detalleObligacionRepository;
    @Mock
    private CargoRecepcionTributariaRepository cargoRepository;
    @Mock
    private IntentoNotificacionTributariaRepository intentoRepository;
    @Mock
    private EvidenciaNotificacionTributariaRepository evidenciaRepository;
    @Mock
    private ControlPlazoNotificacionRepository controlPlazoRepository;
    @Mock
    private LoteNotificacionesTributariasRepository loteRepository;
    @Mock
    private PdfNotificacionTributariaService pdfService;
    @Mock
    private HistorialNotificacionService historialService;

    @InjectMocks
    private NotificacionTributariaService notificacionService;

    private Usuario usuario;
    private ContribuyenteTributario contribuyente;
    private TipoNotificacionTributaria tipoFormal;
    private TipoNotificacionTributaria tipoInformativo;
    private MedioNotificacionTributaria medioFisico;
    private NotificacionTributaria notificacion;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .username("operador_quinua")
                .build();

        Persona persona = Persona.builder()
                .id(10L)
                .nombres("Carlos")
                .apellidoPaterno("Quispe")
                .apellidoMaterno("Huamán")
                .tipoDocumento("DNI")
                .numeroDocumento("45892314")
                .build();

        contribuyente = ContribuyenteTributario.builder()
                .id(UUID.randomUUID())
                .codigoContribuyente("CONT-0001")
                .persona(persona)
                .build();

        tipoFormal = TipoNotificacionTributaria.builder()
                .id(UUID.randomUUID())
                .codigo("NOTIF_ORDEN_PAGO")
                .nombre("Notificación Formal de Orden de Pago")
                .categoria(CategoriaTipoNotificacion.NOTIFICACION_FORMAL)
                .requiereDocumento(true)
                .requiereValidacionLegal(true)
                .requiereEvidencia(true)
                .estado("ACTIVO")
                .build();

        tipoInformativo = TipoNotificacionTributaria.builder()
                .id(UUID.randomUUID())
                .codigo("COM_INF_VENCIMIENTO_PREDIAL")
                .nombre("Aviso Preventivo de Vencimiento")
                .categoria(CategoriaTipoNotificacion.INFORMATIVA)
                .requiereDocumento(false)
                .requiereValidacionLegal(false)
                .requiereEvidencia(false)
                .estado("ACTIVO")
                .build();

        medioFisico = MedioNotificacionTributaria.builder()
                .id(UUID.randomUUID())
                .codigo("ENTREGA_FISICA_DIRECTA")
                .nombre("Entrega Física con Cédula")
                .esMedioFormal(true)
                .requiereDireccionFisica(true)
                .estado("ACTIVO")
                .build();

        notificacion = NotificacionTributaria.builder()
                .id(UUID.randomUUID())
                .codigoNotificacion("NOTIF-2026-000001")
                .tipoNotificacion(tipoFormal)
                .contribuyente(contribuyente)
                .destinatarioNombre("Carlos Quispe Huamán")
                .destinatarioNumeroDoc("45892314")
                .domicilioNotificacion("Jr. Sucre N° 123, Quinua Cercado")
                .medioNotificacion(medioFisico)
                .fechaCreacion(OffsetDateTime.now())
                .estadoAdministrativo(EstadoAdministrativoNotificacion.BORRADOR)
                .estadoEntrega(EstadoEntregaNotificacion.NO_PROGRAMADA)
                .estadoValidezLegal(EstadoValidezLegalNotificacion.PENDIENTE_VERIFICACION)
                .build();
    }

    @Test
    @DisplayName("Debe validar formalmente una notificación en borrador cambiando su estado a VALIDADA")
    void testValidarNotificacionBorrador() {
        when(notificacionRepository.findById(notificacion.getId())).thenReturn(Optional.of(notificacion));
        when(notificacionRepository.save(any(NotificacionTributaria.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotificacionResponse response = notificacionService.validarNotificacion(notificacion.getId(), usuario);

        assertThat(response).isNotNull();
        assertThat(response.getEstadoAdministrativo()).isEqualTo(EstadoAdministrativoNotificacion.VALIDADA);
        verify(historialService).registrarEvento(eq(notificacion), eq(TipoEventoNotificacion.VALIDACION), anyString(), any(), any(), eq(usuario), any());
    }

    @Test
    @DisplayName("No debe permitir validar una notificación ya autorizada o anulada")
    void testNoValidarSiYaAutorizada() {
        notificacion.setEstadoAdministrativo(EstadoAdministrativoNotificacion.AUTORIZADA);
        when(notificacionRepository.findById(notificacion.getId())).thenReturn(Optional.of(notificacion));

        assertThatThrownBy(() -> notificacionService.validarNotificacion(notificacion.getId(), usuario))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Solo se pueden validar notificaciones en borrador u observadas");
    }

    @Test
    @DisplayName("Debe autorizar una notificación validada generando cédula PDF, QR y hash SHA-256")
    void testAutorizarNotificacion() {
        notificacion.setEstadoAdministrativo(EstadoAdministrativoNotificacion.VALIDADA);
        when(notificacionRepository.findById(notificacion.getId())).thenReturn(Optional.of(notificacion));
        when(pdfService.generarDocumentoNotificacion(any(NotificacionTributaria.class), anyString()))
                .thenReturn(new PdfNotificacionTributariaService.DocumentoPdfResult(new byte[]{1, 2, 3}, "E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855"));
        when(notificacionRepository.save(any(NotificacionTributaria.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotificacionResponse resp = notificacionService.autorizarNotificacion(notificacion.getId(), "https://quinua.gob.pe", usuario);

        assertThat(resp).isNotNull();
        assertThat(resp.getEstadoAdministrativo()).isEqualTo(EstadoAdministrativoNotificacion.AUTORIZADA);
        assertThat(resp.getHashDocumento()).isEqualTo("E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855");
        assertThat(resp.getCodigoQr()).contains("https://quinua.gob.pe/verificar/notificacion/NOTIF-2026-000001");
        verify(historialService).registrarEvento(eq(notificacion), eq(TipoEventoNotificacion.AUTORIZACION), anyString(), any(), any(), eq(usuario), any());
    }

    @Test
    @DisplayName("Debe anular una notificación con motivo motivado y sin alterar deudas")
    void testAnularNotificacion() {
        when(notificacionRepository.findById(notificacion.getId())).thenReturn(Optional.of(notificacion));
        when(notificacionRepository.save(any(NotificacionTributaria.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotificacionResponse resp = notificacionService.anularNotificacion(notificacion.getId(), "Error en el domicilio consignado", usuario);

        assertThat(resp).isNotNull();
        assertThat(resp.getEstadoAdministrativo()).isEqualTo(EstadoAdministrativoNotificacion.ANULADA);
        assertThat(resp.getMotivoAnulacion()).isEqualTo("Error en el domicilio consignado");
        verify(historialService).registrarEvento(eq(notificacion), eq(TipoEventoNotificacion.ANULACION), anyString(), any(), any(), eq(usuario), any());
    }

    @Test
    @DisplayName("La verificación pública debe anonimizar datos personales estrictamente sin revelar deudas")
    void testVerificacionPublicaAnonimizada() {
        when(notificacionRepository.findByCodigoNotificacion("NOTIF-2026-000001")).thenReturn(Optional.of(notificacion));

        VerificacionPublicaNotificacionResponse pub = notificacionService.verificarPublicamente("NOTIF-2026-000001");

        assertThat(pub.isExiste()).isTrue();
        assertThat(pub.getCodigoNotificacion()).isEqualTo("NOTIF-2026-000001");
        assertThat(pub.getDestinatarioAnonimizado()).contains("C***");
        assertThat(pub.getDestinatarioAnonimizado()).doesNotContain("Quispe Huamán");
        assertThat(pub.getNumeroDocEnmascarado()).startsWith("45****");
        assertThat(pub.getEntidadEmisora()).isEqualTo("Municipalidad Distrital de Quinua");
    }
}

