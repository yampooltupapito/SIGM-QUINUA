package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.cajapago.ItemAplicacionPagoRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.cajapago.RegistrarPagoRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoValidatorTest {

    @Mock
    private PagoTributarioRepository pagoRepository;
    @Mock
    private ObligacionTributariaRepository obligacionRepository;
    @Mock
    private CuotaObligacionTributariaRepository cuotaRepository;
    @Mock
    private SesionCajaMunicipalRepository sesionCajaRepository;
    @Mock
    private CajaMunicipalRepository cajaRepository;

    @InjectMocks
    private PagoValidator pagoValidator;

    private ContribuyenteTributario contribuyente;
    private ObligacionTributaria obligacion;
    private SesionCajaMunicipal sesion;
    private CajaMunicipal caja;

    @BeforeEach
    void setUp() {
        contribuyente = ContribuyenteTributario.builder()
                .id(UUID.randomUUID())
                .codigoContribuyente("CT-0001")
                .numeroDocumento("10203040")
                .nombreRazonSocial("Comunidad Quinua")
                .build();

        obligacion = ObligacionTributaria.builder()
                .id(UUID.randomUUID())
                .codigoObligacion("OBL-2026-00001")
                .importeVigente(new BigDecimal("100.00"))
                .saldo(new BigDecimal("100.00"))
                .estado(EstadoObligacionTributaria.VIGENTE)
                .estadoFinanciero(EstadoFinancieroObligacion.PENDIENTE)
                .build();

        caja = CajaMunicipal.builder()
                .id(UUID.randomUUID())
                .codigo("CAJA-01")
                .nombre("Ventanilla 1")
                .estado(EstadoCajaMunicipal.ACTIVA)
                .build();

        sesion = SesionCajaMunicipal.builder()
                .id(UUID.randomUUID())
                .codigoSesion("SES-2026-00001")
                .caja(caja)
                .estado(EstadoSesionCaja.ABIERTA)
                .build();
    }

    @Test
    @DisplayName("Debe rechazar pago si contribuyente es nulo")
    void testContribuyenteNulo() {
        RegistrarPagoRequest req = RegistrarPagoRequest.builder()
                .importeTotal(new BigDecimal("100.00"))
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () ->
                pagoValidator.validarRegistroPago(req, null));
        assertTrue(ex.getMessage().contains("no existe"));
    }

    @Test
    @DisplayName("Debe rechazar pago si suma de aplicaciones no coincide con importe total")
    void testDiscrepanciaSumaAplicaciones() {
        ItemAplicacionPagoRequest app1 = ItemAplicacionPagoRequest.builder()
                .obligacionId(obligacion.getId())
                .importeAplicado(new BigDecimal("40.00"))
                .build();

        RegistrarPagoRequest req = RegistrarPagoRequest.builder()
                .contribuyenteId(contribuyente.getId())
                .importeTotal(new BigDecimal("100.00"))
                .medioPago(MedioPagoTributario.TRANSFERENCIA_BANCARIA)
                .numeroOperacionBancaria("BN-999")
                .aplicaciones(List.of(app1))
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () ->
                pagoValidator.validarRegistroPago(req, contribuyente));
        assertTrue(ex.getMessage().contains("Inconsistencia financiera"));
    }

    @Test
    @DisplayName("Debe rechazar pago en efectivo si no hay sesión de caja abierta")
    void testEfectivoSinSesionAbierta() {
        ItemAplicacionPagoRequest app1 = ItemAplicacionPagoRequest.builder()
                .obligacionId(obligacion.getId())
                .importeAplicado(new BigDecimal("100.00"))
                .build();

        RegistrarPagoRequest req = RegistrarPagoRequest.builder()
                .contribuyenteId(contribuyente.getId())
                .importeTotal(new BigDecimal("100.00"))
                .medioPago(MedioPagoTributario.EFECTIVO_CAJA)
                .sesionCajaId(null)
                .aplicaciones(List.of(app1))
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () ->
                pagoValidator.validarRegistroPago(req, contribuyente));
        assertTrue(ex.getMessage().contains("sesión de caja municipal"));
    }

    @Test
    @DisplayName("Debe rechazar sobrepago de una obligación")
    void testSobrepagoObligacion() {
        ItemAplicacionPagoRequest app1 = ItemAplicacionPagoRequest.builder()
                .obligacionId(obligacion.getId())
                .importeAplicado(new BigDecimal("150.00"))
                .build();

        RegistrarPagoRequest req = RegistrarPagoRequest.builder()
                .contribuyenteId(contribuyente.getId())
                .importeTotal(new BigDecimal("150.00"))
                .medioPago(MedioPagoTributario.TRANSFERENCIA_BANCARIA)
                .numeroOperacionBancaria("BN-123")
                .aplicaciones(List.of(app1))
                .build();

        when(obligacionRepository.findById(obligacion.getId())).thenReturn(Optional.of(obligacion));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                pagoValidator.validarRegistroPago(req, contribuyente));
        assertTrue(ex.getMessage().contains("Sobrepago prohibido"));
    }

    @Test
    @DisplayName("Debe rechazar pago si clave de idempotencia ya fue utilizada")
    void testIdempotenciaDuplicada() {
        ItemAplicacionPagoRequest app1 = ItemAplicacionPagoRequest.builder()
                .obligacionId(obligacion.getId())
                .importeAplicado(new BigDecimal("100.00"))
                .build();

        RegistrarPagoRequest req = RegistrarPagoRequest.builder()
                .contribuyenteId(contribuyente.getId())
                .importeTotal(new BigDecimal("100.00"))
                .medioPago(MedioPagoTributario.TRANSFERENCIA_BANCARIA)
                .numeroOperacionBancaria("BN-123")
                .idempotencyKey("KEY-123")
                .aplicaciones(List.of(app1))
                .build();

        when(pagoRepository.findByIdempotencyKey("KEY-123")).thenReturn(Optional.of(new PagoTributario()));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                pagoValidator.validarRegistroPago(req, contribuyente));
        assertTrue(ex.getMessage().contains("Operación duplicada"));
    }

    @Test
    @DisplayName("Debe rechazar apertura de sesión si la caja está inactiva")
    void testAperturaCajaInactiva() {
        caja.setEstado(EstadoCajaMunicipal.INACTIVA);
        when(cajaRepository.findById(caja.getId())).thenReturn(Optional.of(caja));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                pagoValidator.validarAperturaSesion(caja.getId(), 1L));
        assertTrue(ex.getMessage().contains("no se encuentra ACTIVA"));
    }

    @Test
    @DisplayName("Debe rechazar apertura de sesión si ya existe una sesión abierta para la caja")
    void testAperturaSesionDoble() {
        when(cajaRepository.findById(caja.getId())).thenReturn(Optional.of(caja));
        when(sesionCajaRepository.findSesionActivaPorCaja(caja.getId())).thenReturn(Optional.of(sesion));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                pagoValidator.validarAperturaSesion(caja.getId(), 1L));
        assertTrue(ex.getMessage().contains("ya tiene una sesión abierta"));
    }

    @Test
    @DisplayName("Debe rechazar reversión si el pago no está confirmado")
    void testReversionPagoNoConfirmado() {
        PagoTributario pago = PagoTributario.builder()
                .estado(EstadoPagoTributario.BORRADOR)
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () ->
                pagoValidator.validarReversionPago(pago, "Error de tipeo"));
        assertTrue(ex.getMessage().contains("Sólo pueden revertirse financieramente"));
    }
}
