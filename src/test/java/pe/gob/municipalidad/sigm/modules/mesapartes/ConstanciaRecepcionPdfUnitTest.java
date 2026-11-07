package pe.gob.municipalidad.sigm.modules.mesapartes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.gob.municipalidad.sigm.common.document.pdf.QrCodeGenerator;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Rol;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.Expediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.ExpedienteHistorial;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.TipoTramite;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteHistorialRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.ConstanciaRecepcionServiceImpl;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias - Generación de Constancia Oficial de Recepción PDF (Prompt 14)")
class ConstanciaRecepcionPdfUnitTest {

    @Mock
    private ExpedienteRepository expedienteRepository;

    @Mock
    private ExpedienteHistorialRepository historialRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private QrCodeGenerator qrCodeGenerator;

    @InjectMocks
    private ConstanciaRecepcionServiceImpl constanciaRecepcionService;

    private Usuario operador;
    private Expediente expedienteRecibido;
    private Expediente expedientePresentado;

    @BeforeEach
    void setUp() {
        Rol rolOp = Rol.builder().id(1L).nombre("ROLE_OPERADOR").build();
        Persona perOp = Persona.builder().id(10L).nombres("Operador").build();
        operador = Usuario.builder().id(1L).username("operador_recepcion").persona(perOp).roles(Set.of(rolOp)).build();

        Persona sol = Persona.builder().id(20L).nombres("María").apellidoPaterno("Quispe").tipoDocumento("DNI").numeroDocumento("44556677").build();
        Oficina ofi = Oficina.builder().id(30L).nombre("Gerencia de Rentas").build();
        TipoTramite tramite = TipoTramite.builder().id(5L).nombre("Licencia de Funcionamiento").build();

        expedienteRecibido = Expediente.builder()
                .id(100L)
                .uuid(UUID.randomUUID())
                .numeroExpediente("MPV-2026-000123")
                .asunto("Solicitud de Licencia Comercial")
                .solicitante(sol)
                .oficinaResponsable(ofi)
                .tipoTramite(tramite)
                .estadoActual(EstadoExpediente.RECIBIDO)
                .fechaPresentacion(OffsetDateTime.now().minusHours(2))
                .fechaRecepcion(OffsetDateTime.now().minusHours(1))
                .documentos(Collections.emptyList())
                .build();

        expedientePresentado = Expediente.builder()
                .id(101L)
                .uuid(UUID.randomUUID())
                .numeroExpediente("MPV-2026-000124")
                .estadoActual(EstadoExpediente.PRESENTADO)
                .build();
    }

    @Test
    @DisplayName("Debe rechazar la generación de constancia de recepción para expedientes no recibidos formalmente")
    void debeRechazarConstanciaParaExpedienteNoRecibido() {
        when(usuarioRepository.findByUsernameWithRolesAndPersona("operador_recepcion")).thenReturn(Optional.of(operador));
        when(expedienteRepository.findByIdWithDetalles(101L)).thenReturn(Optional.of(expedientePresentado));

        assertThatThrownBy(() -> constanciaRecepcionService.generarConstanciaRecepcionPdf(101L, "http://localhost:8080", "operador_recepcion"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no ha sido recibido formalmente");
    }

    @Test
    @DisplayName("Debe generar exitosamente el documento PDF de la constancia de recepción")
    void debeGenerarConstanciaRecepcionPdfExitosamente() {
        when(usuarioRepository.findByUsernameWithRolesAndPersona("operador_recepcion")).thenReturn(Optional.of(operador));
        when(expedienteRepository.findByIdWithDetalles(100L)).thenReturn(Optional.of(expedienteRecibido));

        ExpedienteHistorial hist = ExpedienteHistorial.builder()
                .accion("RECEPCION_EXPEDIENTE")
                .usuarioResponsable(operador)
                .fechaMovimiento(OffsetDateTime.now())
                .build();
        when(historialRepository.findByExpedienteIdOrderByFechaMovimientoAsc(100L)).thenReturn(List.of(hist));

        // Mock QrCodeGenerator
        byte[] dummyQr = new byte[]{1, 2, 3};
        // Use real 1x1 png or mock
        try {
            // Generate valid 1x1 png bytes so iText Image doesn't complain
            byte[] miniPng = new byte[]{
                    (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D,
                    0x49, 0x48, 0x44, 0x52, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
                    0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4, (byte) 0x89, 0x00,
                    0x00, 0x00, 0x0A, 0x49, 0x44, 0x41, 0x54, 0x78, (byte) 0x9C, 0x63, 0x00,
                    0x01, 0x00, 0x00, 0x05, 0x00, 0x01, 0x0D, 0x0A, 0x2D, (byte) 0xB4, 0x00,
                    0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, (byte) 0xAE, 0x42, 0x60, (byte) 0x82
            };
            when(qrCodeGenerator.generateQrCodePng(anyString(), anyInt(), anyInt())).thenReturn(miniPng);
        } catch (Exception ignored) {}

        byte[] pdfBytes = constanciaRecepcionService.generarConstanciaRecepcionPdf(100L, "http://localhost:8080", "operador_recepcion");

        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(500);
        // Header %PDF-
        assertThat(new String(pdfBytes, 0, 5)).isEqualTo("%PDF-");
    }
}
