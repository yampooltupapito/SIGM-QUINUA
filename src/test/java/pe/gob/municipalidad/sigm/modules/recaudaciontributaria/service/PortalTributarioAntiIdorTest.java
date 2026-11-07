package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoVinculacionContribuyente;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoVinculacionContribuyente;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.*;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortalTributarioAntiIdorTest {

    @Mock
    private VinculacionUsuarioContribuyenteRepository vinculacionRepository;

    @Mock
    private RelacionContribuyentePredioRepository relacionPredioRepository;

    @Mock
    private ObligacionTributariaRepository obligacionRepository;

    @Mock
    private PagoTributarioRepository pagoRepository;

    @Mock
    private DocumentoPagoTributarioRepository reciboRepository;

    @Mock
    private ConvenioFraccionamientoTributarioRepository convenioRepository;

    @Mock
    private NotificacionTributariaRepository notificacionRepository;

    @Mock
    private HistorialAccesoPortalTributarioRepository historialRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpSession session;

    @InjectMocks
    private PortalTributarioContextService contextService;

    private Usuario usuarioAutenticado;
    private ContribuyenteTributario contribuyenteActivo;
    private ContribuyenteTributario contribuyenteTercero;

    @BeforeEach
    void setUp() {
        usuarioAutenticado = Usuario.builder()
                .id(1L)
                .username("ciudadano_quinua")
                .build();

        contribuyenteActivo = ContribuyenteTributario.builder()
                .id(UUID.randomUUID())
                .codigoContribuyente("CONT-001")
                .nombreRazonSocial("CONTRIBUYENTE AUTORIZADO")
                .build();

        contribuyenteTercero = ContribuyenteTributario.builder()
                .id(UUID.randomUUID())
                .codigoContribuyente("CONT-999")
                .nombreRazonSocial("CONTRIBUYENTE TERCERO AJENO")
                .build();

        CustomUserDetails userDetails = CustomUserDetails.builder()
                .id(1L)
                .username("ciudadano_quinua")
                .password("hash")
                .authorities(Collections.emptyList())
                .enabled(true)
                .accountNonLocked(true)
                .build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(usuarioRepository.findByUsername("ciudadano_quinua")).thenReturn(Optional.of(usuarioAutenticado));

        VinculacionUsuarioContribuyente vinculacion = VinculacionUsuarioContribuyente.builder()
                .id(UUID.randomUUID())
                .usuario(usuarioAutenticado)
                .contribuyente(contribuyenteActivo)
                .tipoVinculacion(TipoVinculacionContribuyente.TITULAR_VERIFICADO)
                .estado(EstadoVinculacionContribuyente.APROBADA)
                .activo(true)
                .build();

        when(vinculacionRepository.findByUsuarioIdAndEstadoAndActivoTrue(1L, EstadoVinculacionContribuyente.APROBADA))
                .thenReturn(List.of(vinculacion));
    }

    @Test
    @DisplayName("Anti-IDOR Obligación: Intento de consultar obligación perteneciente a un tercero debe arrojar AccessDeniedException")
    void testAntiIdorObligacionAjenaBloqueada() {
        UUID obligacionAjenaId = UUID.randomUUID();
        ObligacionTributaria obligacionAjena = ObligacionTributaria.builder()
                .id(obligacionAjenaId)
                .codigoObligacion("OBL-TERCERO-001")
                .contribuyente(contribuyenteTercero)
                .build();

        when(obligacionRepository.findById(obligacionAjenaId)).thenReturn(Optional.of(obligacionAjena));

        assertThatThrownBy(() -> contextService.validarAccesoObligacion(obligacionAjenaId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("no corresponde a su perfil activo");
    }

    @Test
    @DisplayName("Anti-IDOR Pago: Intento de consultar pago perteneciente a un tercero debe arrojar AccessDeniedException")
    void testAntiIdorPagoAjenoBloqueado() {
        UUID pagoAjenoId = UUID.randomUUID();
        PagoTributario pagoAjeno = PagoTributario.builder()
                .id(pagoAjenoId)
                .codigoPago("PAGO-TERCERO-001")
                .contribuyente(contribuyenteTercero)
                .build();

        when(pagoRepository.findById(pagoAjenoId)).thenReturn(Optional.of(pagoAjeno));

        assertThatThrownBy(() -> contextService.validarAccesoPago(pagoAjenoId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("la operación de pago no corresponde a su cuenta");
    }

    @Test
    @DisplayName("Anti-IDOR Recibo: Intento de descargar recibo perteneciente a un tercero debe arrojar AccessDeniedException")
    void testAntiIdorReciboAjenoBloqueado() {
        UUID reciboAjenoId = UUID.randomUUID();
        DocumentoPagoTributario reciboAjeno = DocumentoPagoTributario.builder()
                .id(reciboAjenoId)
                .codigoDocumento("REC-TERCERO-001")
                .contribuyente(contribuyenteTercero)
                .build();

        when(reciboRepository.findById(reciboAjenoId)).thenReturn(Optional.of(reciboAjeno));

        assertThatThrownBy(() -> contextService.validarAccesoRecibo(reciboAjenoId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("el comprobante no corresponde a su cuenta");
    }

    @Test
    @DisplayName("Anti-IDOR Convenio: Intento de ver convenio de fraccionamiento de un tercero debe arrojar AccessDeniedException")
    void testAntiIdorConvenioAjenoBloqueado() {
        UUID convenioAjenoId = UUID.randomUUID();
        ConvenioFraccionamientoTributario convenioAjeno = ConvenioFraccionamientoTributario.builder()
                .id(convenioAjenoId)
                .codigoConvenio("CONV-TERCERO-001")
                .contribuyente(contribuyenteTercero)
                .build();

        when(convenioRepository.findById(convenioAjenoId)).thenReturn(Optional.of(convenioAjeno));

        assertThatThrownBy(() -> contextService.validarAccesoConvenio(convenioAjenoId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("el convenio solicitado no corresponde a su cuenta");
    }

    @Test
    @DisplayName("Anti-IDOR Notificación: Intento de ver notificación formal de un tercero debe arrojar AccessDeniedException")
    void testAntiIdorNotificacionAjenaBloqueada() {
        UUID notificacionAjenaId = UUID.randomUUID();
        NotificacionTributaria notificacionAjena = NotificacionTributaria.builder()
                .id(notificacionAjenaId)
                .codigoNotificacion("NOTIF-TERCERO-001")
                .contribuyente(contribuyenteTercero)
                .build();

        when(notificacionRepository.findById(notificacionAjenaId)).thenReturn(Optional.of(notificacionAjena));

        assertThatThrownBy(() -> contextService.validarAccesoNotificacion(notificacionAjenaId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("la notificación requerida no corresponde a su cuenta");
    }

    @Test
    @DisplayName("Anti-IDOR Predio: Si el predio no está asociado al contribuyente activo debe arrojar AccessDeniedException")
    void testAntiIdorPredioAjenoBloqueado() {
        UUID predioAjenoId = UUID.randomUUID();
        when(relacionPredioRepository.findByContribuyenteIdAndPredioId(contribuyenteActivo.getId(), predioAjenoId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> contextService.validarAccesoPredio(predioAjenoId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("el predio no está vinculado a su cuenta tributaria");
    }

    @Test
    @DisplayName("Acceso Permitido: Cuando la obligación pertenece al contribuyente activo debe resolverse con éxito")
    void testAccesoPermitidoObligacionPropia() {
        UUID obligacionPropiaId = UUID.randomUUID();
        ObligacionTributaria obligacionPropia = ObligacionTributaria.builder()
                .id(obligacionPropiaId)
                .codigoObligacion("OBL-PROPIA-001")
                .contribuyente(contribuyenteActivo)
                .build();

        when(obligacionRepository.findById(obligacionPropiaId)).thenReturn(Optional.of(obligacionPropia));

        ObligacionTributaria resultado = contextService.validarAccesoObligacion(obligacionPropiaId);
        assertThat(resultado).isNotNull();
        assertThat(resultado.getCodigoObligacion()).isEqualTo("OBL-PROPIA-001");
    }
}

