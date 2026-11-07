package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.gob.municipalidad.sigm.common.document.repository.DocumentoRepository;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.portal.AprobarRechazarVinculacionRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.portal.SolicitarVinculacionRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.portal.VinculacionResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ContribuyenteTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.SolicitudVinculacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.VinculacionUsuarioContribuyente;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoVinculacionContribuyente;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoVinculacionContribuyente;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ContribuyenteTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.SolicitudVinculacionTributariaRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.VinculacionUsuarioContribuyenteRepository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VinculacionContribuyenteServiceTest {

    @Mock
    private VinculacionUsuarioContribuyenteRepository vinculacionRepository;

    @Mock
    private SolicitudVinculacionTributariaRepository solicitudRepository;

    @Mock
    private ContribuyenteTributarioRepository contribuyenteRepository;

    @Mock
    private DocumentoRepository documentoRepository;

    @InjectMocks
    private VinculacionContribuyenteService vinculacionService;

    private Usuario usuario;
    private Persona persona;
    private ContribuyenteTributario contribuyente;

    @BeforeEach
    void setUp() {
        persona = Persona.builder()
                .id(100L)
                .tipoDocumento("DNI")
                .numeroDocumento("40552100")
                .nombres("Juan")
                .apellidoPaterno("Pérez")
                .apellidoMaterno("Quispe")
                .build();

        usuario = Usuario.builder()
                .id(1L)
                .username("jperez")
                .persona(persona)
                .build();

        contribuyente = ContribuyenteTributario.builder()
                .id(UUID.randomUUID())
                .codigoContribuyente("CONT-001")
                .numeroDocumento("40552100")
                .tipoDocumento("DNI")
                .nombreRazonSocial("JUAN PEREZ QUISPE")
                .persona(persona)
                .build();
    }

    @Test
    @DisplayName("Solicitud de titular que coincide con DNI y Persona en padrón debe ser auto-aprobada")
    void testSolicitarVinculacionTitularAutoAprobada() {
        SolicitarVinculacionRequest req = SolicitarVinculacionRequest.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("40552100")
                .tipoVinculacion(TipoVinculacionContribuyente.TITULAR_VERIFICADO)
                .sustento("Soy el titular de los predios en Quinua")
                .build();

        when(contribuyenteRepository.findByNumeroDocumento("40552100")).thenReturn(Optional.of(contribuyente));
        when(vinculacionRepository.findByUsuarioIdAndContribuyenteId(1L, contribuyente.getId())).thenReturn(Optional.empty());

        when(solicitudRepository.save(any(SolicitudVinculacionTributaria.class))).thenAnswer(i -> {
            SolicitudVinculacionTributaria s = i.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });

        when(vinculacionRepository.save(any(VinculacionUsuarioContribuyente.class))).thenAnswer(i -> {
            VinculacionUsuarioContribuyente v = i.getArgument(0);
            v.setId(UUID.randomUUID());
            return v;
        });

        VinculacionResponse response = vinculacionService.solicitarVinculacion(req, usuario);

        assertThat(response).isNotNull();
        assertThat(response.getEstado()).isEqualTo(EstadoVinculacionContribuyente.APROBADA);
        assertThat(response.isActivo()).isTrue();
        verify(vinculacionRepository, times(1)).save(any(VinculacionUsuarioContribuyente.class));
    }

    @Test
    @DisplayName("Solicitud de representante legal debe quedar en estado PENDIENTE de evaluación")
    void testSolicitarVinculacionRepresentantePendiente() {
        Persona personaRep = Persona.builder()
                .id(200L)
                .tipoDocumento("DNI")
                .numeroDocumento("70112233")
                .nombres("Abogado")
                .apellidoPaterno("Gestor")
                .build();

        Usuario usuarioRep = Usuario.builder()
                .id(2L)
                .username("rep_legal")
                .persona(personaRep)
                .build();

        SolicitarVinculacionRequest req = SolicitarVinculacionRequest.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("40552100")
                .tipoVinculacion(TipoVinculacionContribuyente.REPRESENTANTE_ACREDITADO)
                .sustento("Poder por escritura pública debidamente inscrita en SUNARP")
                .build();

        when(contribuyenteRepository.findByNumeroDocumento("40552100")).thenReturn(Optional.of(contribuyente));
        when(vinculacionRepository.findByUsuarioIdAndContribuyenteId(2L, contribuyente.getId())).thenReturn(Optional.empty());

        when(solicitudRepository.save(any(SolicitudVinculacionTributaria.class))).thenAnswer(i -> {
            SolicitudVinculacionTributaria s = i.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });

        when(vinculacionRepository.save(any(VinculacionUsuarioContribuyente.class))).thenAnswer(i -> {
            VinculacionUsuarioContribuyente v = i.getArgument(0);
            v.setId(UUID.randomUUID());
            return v;
        });

        VinculacionResponse response = vinculacionService.solicitarVinculacion(req, usuarioRep);

        assertThat(response).isNotNull();
        assertThat(response.getEstado()).isEqualTo(EstadoVinculacionContribuyente.PENDIENTE);
        assertThat(response.isVigente()).isFalse();
    }

    @Test
    @DisplayName("Solicitud de titular con DNI discordante debe lanzar BusinessException")
    void testSolicitarVinculacionTitularDniDiscordanteRechazo() {
        SolicitarVinculacionRequest req = SolicitarVinculacionRequest.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("99999999") // No coincide con el DNI del usuario (40552100)
                .tipoVinculacion(TipoVinculacionContribuyente.TITULAR_VERIFICADO)
                .sustento("Intento de vinculación indebida")
                .build();

        ContribuyenteTributario otroContrib = ContribuyenteTributario.builder()
                .id(UUID.randomUUID())
                .numeroDocumento("99999999")
                .build();

        when(contribuyenteRepository.findByNumeroDocumento("99999999")).thenReturn(Optional.of(otroContrib));
        when(vinculacionRepository.findByUsuarioIdAndContribuyenteId(1L, otroContrib.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vinculacionService.solicitarVinculacion(req, usuario))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("el documento de identidad debe coincidir exactamente");
    }

    @Test
    @DisplayName("Aprobación administrativa de vinculación por parte del operador de rentas")
    void testAprobarVinculacionPorOperador() {
        UUID vinculacionId = UUID.randomUUID();
        VinculacionUsuarioContribuyente v = VinculacionUsuarioContribuyente.builder()
                .id(vinculacionId)
                .usuario(usuario)
                .contribuyente(contribuyente)
                .tipoVinculacion(TipoVinculacionContribuyente.APODERADO_ACREDITADO)
                .estado(EstadoVinculacionContribuyente.PENDIENTE)
                .activo(false)
                .build();

        when(vinculacionRepository.findById(vinculacionId)).thenReturn(Optional.of(v));
        when(vinculacionRepository.save(any(VinculacionUsuarioContribuyente.class))).thenAnswer(i -> i.getArgument(0));

        Usuario operador = Usuario.builder().id(99L).username("operador_rentas").build();
        AprobarRechazarVinculacionRequest req = AprobarRechazarVinculacionRequest.builder()
                .motivo("Documento de poder revisado y conforme en el archivo municipal.")
                .build();

        VinculacionResponse response = vinculacionService.aprobarVinculacion(vinculacionId, req, operador);

        assertThat(response).isNotNull();
        assertThat(response.getEstado()).isEqualTo(EstadoVinculacionContribuyente.APROBADA);
        assertThat(response.isActivo()).isTrue();
        assertThat(v.isActivo()).isTrue();
        assertThat(v.getEstado()).isEqualTo(EstadoVinculacionContribuyente.APROBADA);
    }

    @Test
    @DisplayName("Rechazo administrativo de vinculación con fundamentación obligatoria")
    void testRechazarVinculacion() {
        UUID vinculacionId = UUID.randomUUID();
        VinculacionUsuarioContribuyente v = VinculacionUsuarioContribuyente.builder()
                .id(vinculacionId)
                .usuario(usuario)
                .contribuyente(contribuyente)
                .tipoVinculacion(TipoVinculacionContribuyente.OTRA_RELACION_AUTORIZADA)
                .estado(EstadoVinculacionContribuyente.PENDIENTE)
                .activo(false)
                .build();

        when(vinculacionRepository.findById(vinculacionId)).thenReturn(Optional.of(v));
        when(vinculacionRepository.save(any(VinculacionUsuarioContribuyente.class))).thenAnswer(i -> i.getArgument(0));

        Usuario operador = Usuario.builder().id(99L).username("operador_rentas").build();
        AprobarRechazarVinculacionRequest req = AprobarRechazarVinculacionRequest.builder()
                .motivo("No adjuntó carta poder con firma legalizada notarialmente.")
                .build();

        VinculacionResponse response = vinculacionService.rechazarVinculacion(vinculacionId, req, operador);

        assertThat(response).isNotNull();
        assertThat(response.getEstado()).isEqualTo(EstadoVinculacionContribuyente.RECHAZADA);
        assertThat(v.isActivo()).isFalse();
        assertThat(v.getMotivoRechazoRevocacion()).contains("No adjuntó carta poder");
    }

    @Test
    @DisplayName("Revocación inmediata de vinculación previamente activa")
    void testRevocarVinculacionActiva() {
        UUID vinculacionId = UUID.randomUUID();
        VinculacionUsuarioContribuyente v = VinculacionUsuarioContribuyente.builder()
                .id(vinculacionId)
                .usuario(usuario)
                .contribuyente(contribuyente)
                .tipoVinculacion(TipoVinculacionContribuyente.APODERADO_ACREDITADO)
                .estado(EstadoVinculacionContribuyente.APROBADA)
                .activo(true)
                .build();

        when(vinculacionRepository.findById(vinculacionId)).thenReturn(Optional.of(v));
        when(vinculacionRepository.save(any(VinculacionUsuarioContribuyente.class))).thenAnswer(i -> i.getArgument(0));

        Usuario operador = Usuario.builder().id(99L).username("supervisor_rentas").build();
        AprobarRechazarVinculacionRequest req = AprobarRechazarVinculacionRequest.builder()
                .motivo("Titular solicitó el cese de facultades para este usuario.")
                .build();

        VinculacionResponse response = vinculacionService.revocarVinculacion(vinculacionId, req, operador);

        assertThat(response).isNotNull();
        assertThat(response.getEstado()).isEqualTo(EstadoVinculacionContribuyente.REVOCADA);
        assertThat(response.isActivo()).isFalse();
        assertThat(v.isActivo()).isFalse();
        assertThat(v.getEstado()).isEqualTo(EstadoVinculacionContribuyente.REVOCADA);
    }
}
