package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoDocumentoPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.DocumentoPagoTributarioRepository;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentoValidatorTest {

    @Mock
    private DocumentoPagoTributarioRepository documentoRepository;

    @InjectMocks
    private DocumentoValidator validator;

    private PagoTributario pagoConfirmado;
    private TipoDocumentoPagoTributario tipoRecibo;
    private SerieDocumentoPagoTributario serieRecibo;

    @BeforeEach
    void setUp() {
        tipoRecibo = TipoDocumentoPagoTributario.builder()
                .id(UUID.randomUUID())
                .codigo("RECIBO_PAGO")
                .nombre("Recibo de Caja Municipal")
                .estado("ACTIVO")
                .build();

        serieRecibo = SerieDocumentoPagoTributario.builder()
                .id(UUID.randomUUID())
                .tipoDocumento(tipoRecibo)
                .codigoSerie("REC01")
                .estado("ACTIVA")
                .numeroCorrelativoActual(10L)
                .build();

        pagoConfirmado = PagoTributario.builder()
                .id(UUID.randomUUID())
                .codigoPago("PAG-2026-00001")
                .estado(EstadoPagoTributario.CONFIRMADO)
                .build();
    }

    @Test
    @DisplayName("Valida exitosamente pago confirmado sin documentos previos")
    void testValidarPagoConfirmadoExitoso() {
        when(documentoRepository.existsByPagoTributarioIdAndTipoDocumentoIdAndEstado(
                pagoConfirmado.getId(), tipoRecibo.getId(), EstadoDocumentoPagoTributario.EMITIDO))
                .thenReturn(false);

        assertDoesNotThrow(() -> validator.validarPagoParaEmision(pagoConfirmado, tipoRecibo));
    }

    @Test
    @DisplayName("Rechaza emisión si pago está en BORRADOR")
    void testRechazaPagoEnBorrador() {
        pagoConfirmado.setEstado(EstadoPagoTributario.BORRADOR);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> validator.validarPagoParaEmision(pagoConfirmado, tipoRecibo));
        assertTrue(ex.getMessage().contains("Solo se pueden emitir documentos oficiales sobre pagos en estado CONFIRMADO"));
    }

    @Test
    @DisplayName("Rechaza emisión si pago fue REVERTIDO")
    void testRechazaPagoRevertido() {
        pagoConfirmado.setEstado(EstadoPagoTributario.REVERTIDO);
        pagoConfirmado.setFechaReversion(OffsetDateTime.now());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> validator.validarPagoParaEmision(pagoConfirmado, tipoRecibo));
        assertTrue(ex.getMessage().contains("CONFIRMADO") || ex.getMessage().contains("revertido"));
    }

    @Test
    @DisplayName("Impide doble emisión activa para el mismo pago")
    void testImpideDobleEmisionActiva() {
        when(documentoRepository.existsByPagoTributarioIdAndTipoDocumentoIdAndEstado(
                pagoConfirmado.getId(), tipoRecibo.getId(), EstadoDocumentoPagoTributario.EMITIDO))
                .thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> validator.validarPagoParaEmision(pagoConfirmado, tipoRecibo));
        assertTrue(ex.getMessage().contains("Ya existe un documento activo"));
    }

    @Test
    @DisplayName("Valida serie activa correspondiente al tipo documental")
    void testValidarSerieExitoso() {
        assertDoesNotThrow(() -> validator.validarSerieParaEmision(serieRecibo, tipoRecibo));
    }

    @Test
    @DisplayName("Rechaza serie inactiva")
    void testRechazaSerieInactiva() {
        serieRecibo.setEstado("INACTIVA");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> validator.validarSerieParaEmision(serieRecibo, tipoRecibo));
        assertTrue(ex.getMessage().contains("no está ACTIVA"));
    }

    @Test
    @DisplayName("Valida anulación formal de documento emitido con motivo válido")
    void testValidarAnulacionExitoso() {
        DocumentoPagoTributario doc = DocumentoPagoTributario.builder()
                .estado(EstadoDocumentoPagoTributario.EMITIDO)
                .build();

        assertDoesNotThrow(() -> validator.validarParaAnulacion(doc, "Error involuntario en serie de ventanilla"));
    }

    @Test
    @DisplayName("Rechaza anulación si motivo es inferior a 10 caracteres")
    void testRechazaAnulacionMotivoCorto() {
        DocumentoPagoTributario doc = DocumentoPagoTributario.builder()
                .estado(EstadoDocumentoPagoTributario.EMITIDO)
                .build();

        BusinessException ex = assertThrows(BusinessException.class,
                () -> validator.validarParaAnulacion(doc, "Error"));
        assertTrue(ex.getMessage().contains("al menos 10 caracteres"));
    }
}
