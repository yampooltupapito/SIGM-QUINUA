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
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.deuda.ClasificarDeudaRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.deuda.RegistrarCompromisoDeudaRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.deuda.RegistrarGestionDeudaRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ContribuyenteTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ObligacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.SeguimientoDeudaTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.CompromisoSeguimientoDeudaRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.GestionSeguimientoDeudaRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ObligacionTributariaRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.SeguimientoDeudaTributariaRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.impl.DeudaTributariaServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeudaTributariaServiceTest {

    @Mock
    private ObligacionTributariaRepository obligacionRepository;

    @Mock
    private SeguimientoDeudaTributariaRepository seguimientoRepository;

    @Mock
    private GestionSeguimientoDeudaRepository gestionRepository;

    @Mock
    private CompromisoSeguimientoDeudaRepository compromisoRepository;

    @Mock
    private InteresMoratorioService interesService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private OficinaRepository oficinaRepository;

    @InjectMocks
    private DeudaTributariaServiceImpl deudaService;

    private ContribuyenteTributario testContribuyente;
    private ObligacionTributaria testObligacion;

    @BeforeEach
    void setUp() {
        testContribuyente = ContribuyenteTributario.builder()
                .id(UUID.randomUUID())
                .codigoContribuyente("CTR-001")
                .nombreRazonSocial("Juan Pérez Quispe")
                .tipoDocumento("DNI")
                .numeroDocumento("44556677")
                .build();

        testObligacion = ObligacionTributaria.builder()
                .id(UUID.randomUUID())
                .codigoObligacion("OBL-2026-0001")
                .contribuyente(testContribuyente)
                .importeDeterminado(new BigDecimal("150.00"))
                .importePagado(BigDecimal.ZERO)
                .saldo(new BigDecimal("150.00"))
                .moneda("PEN")
                .fechaVencimiento(LocalDate.of(2026, 2, 28))
                .estado(EstadoObligacionTributaria.VIGENTE)
                .estadoExigibilidad(EstadoExigibilidadObligacion.EXIGIBLE)
                .build();
    }

    @Test
    @DisplayName("Cálculo de tramo de antigüedad según días de morosidad")
    void testCalculoTramosAntiguedad() {
        assertThat(TramoAntiguedadDeuda.calcularTramo(0)).isEqualTo(TramoAntiguedadDeuda.NO_VENCIDA);
        assertThat(TramoAntiguedadDeuda.calcularTramo(-5)).isEqualTo(TramoAntiguedadDeuda.NO_VENCIDA);
        assertThat(TramoAntiguedadDeuda.calcularTramo(15)).isEqualTo(TramoAntiguedadDeuda.DE_01_A_30_DIAS);
        assertThat(TramoAntiguedadDeuda.calcularTramo(30)).isEqualTo(TramoAntiguedadDeuda.DE_01_A_30_DIAS);
        assertThat(TramoAntiguedadDeuda.calcularTramo(45)).isEqualTo(TramoAntiguedadDeuda.DE_31_A_60_DIAS);
        assertThat(TramoAntiguedadDeuda.calcularTramo(75)).isEqualTo(TramoAntiguedadDeuda.DE_61_A_90_DIAS);
        assertThat(TramoAntiguedadDeuda.calcularTramo(120)).isEqualTo(TramoAntiguedadDeuda.MAS_DE_90_DIAS);
    }

    @Test
    @DisplayName("Rechazar compromiso de pago en obligación con saldo cero")
    void testRechazarCompromisoSaldoCero() {
        testObligacion.setSaldo(BigDecimal.ZERO);
        when(obligacionRepository.findById(testObligacion.getId())).thenReturn(Optional.of(testObligacion));

        RegistrarCompromisoDeudaRequest req = RegistrarCompromisoDeudaRequest.builder()
                .fechaComprometida(LocalDate.now().plusDays(10))
                .montoComprometido(new BigDecimal("50.00"))
                .build();

        assertThatThrownBy(() -> deudaService.registrarCompromiso(testObligacion.getId(), req, "cajero"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("No se pueden registrar compromisos de pago para una obligación sin saldo");
    }

    @Test
    @DisplayName("Reclasificar obligación administrativamente")
    void testReclasificarObligacion() {
        when(obligacionRepository.findById(testObligacion.getId())).thenReturn(Optional.of(testObligacion));

        SeguimientoDeudaTributaria seg = SeguimientoDeudaTributaria.builder()
                .id(UUID.randomUUID())
                .obligacion(testObligacion)
                .contribuyente(testContribuyente)
                .clasificacion(ClasificacionDeudaTributaria.VENCIDA_EXIGIBLE)
                .build();

        when(seguimientoRepository.findByObligacionId(testObligacion.getId())).thenReturn(Optional.of(seg));
        when(seguimientoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ClasificarDeudaRequest req = ClasificarDeudaRequest.builder()
                .clasificacion(ClasificacionDeudaTributaria.OBSERVADA)
                .observaciones("Obligación en auditoría predial")
                .build();

        deudaService.clasificarObligacion(testObligacion.getId(), req, "auditor_tributario");

        verify(gestionRepository).save(any());
        assertThat(seg.getClasificacion()).isEqualTo(ClasificacionDeudaTributaria.OBSERVADA);
    }

    @Test
    @DisplayName("Registrar gestión administrativa con responsable")
    void testRegistrarGestionAdministrativa() {
        when(obligacionRepository.findById(testObligacion.getId())).thenReturn(Optional.of(testObligacion));

        SeguimientoDeudaTributaria seg = SeguimientoDeudaTributaria.builder()
                .id(UUID.randomUUID())
                .obligacion(testObligacion)
                .contribuyente(testContribuyente)
                .clasificacion(ClasificacionDeudaTributaria.VENCIDA_EXIGIBLE)
                .build();

        when(seguimientoRepository.findByObligacionId(testObligacion.getId())).thenReturn(Optional.of(seg));
        Usuario user = Usuario.builder().username("gestor_rentas").build();
        when(usuarioRepository.findByUsername("gestor_rentas")).thenReturn(Optional.of(user));

        RegistrarGestionDeudaRequest req = RegistrarGestionDeudaRequest.builder()
                .tipoGestion(TipoGestionDeuda.ATENCION_PRESENCIAL)
                .resultado("Contribuyente acudió a ventanilla a solicitar estado de cuenta")
                .observaciones("Se emitió estado de cuenta preliminar")
                .build();

        deudaService.registrarGestion(testObligacion.getId(), req, "gestor_rentas");

        verify(gestionRepository).save(any());
    }
}
