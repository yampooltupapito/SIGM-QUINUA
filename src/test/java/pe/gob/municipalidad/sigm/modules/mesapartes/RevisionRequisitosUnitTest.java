package pe.gob.municipalidad.sigm.modules.mesapartes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import pe.gob.municipalidad.sigm.common.document.repository.DocumentoRepository;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Rol;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvAdminDtos.RevisionRequisitoResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvAdminDtos.VerificarRequisitoRequest;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.Expediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.RequisitoDocumental;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.RevisionRequisito;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.TipoTramite;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoRevisionRequisito;
import pe.gob.municipalidad.sigm.modules.mesapartes.mapper.MpvMapper;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.RequisitoDocumentalRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.RevisionRequisitoRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.RevisionRequisitosServiceImpl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias - Verificación de Requisitos Documentales (Prompt 14)")
class RevisionRequisitosUnitTest {

    @Mock
    private RevisionRequisitoRepository revisionRequisitoRepository;

    @Mock
    private RequisitoDocumentalRepository requisitoDocumentalRepository;

    @Mock
    private ExpedienteRepository expedienteRepository;

    @Mock
    private DocumentoRepository documentoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private MpvMapper mpvMapper;

    @InjectMocks
    private RevisionRequisitosServiceImpl revisionService;

    private Usuario operador;
    private Usuario ciudadano;
    private Expediente expediente;
    private TipoTramite tipoTramite;
    private RequisitoDocumental reqDni;

    @BeforeEach
    void setUp() {
        Rol rolOp = Rol.builder().id(1L).nombre("ROLE_OPERADOR").build();
        Rol rolCiu = Rol.builder().id(2L).nombre("ROLE_CIUDADANO").build();

        Persona perOp = Persona.builder().id(10L).nombres("Operador").build();
        Persona perCiu = Persona.builder().id(20L).nombres("Ciudadano").build();

        operador = Usuario.builder().id(1L).username("operador_1").persona(perOp).roles(Set.of(rolOp)).build();
        ciudadano = Usuario.builder().id(2L).username("ciudadano_1").persona(perCiu).roles(Set.of(rolCiu)).build();

        tipoTramite = TipoTramite.builder().id(5L).nombre("Licencia de Funcionamiento").build();
        expediente = Expediente.builder().id(100L).solicitante(perCiu).tipoTramite(tipoTramite).build();

        reqDni = RequisitoDocumental.builder().id(50L).codigo("REQ-DNI").nombre("Copia de DNI").obligatorio(true).build();
    }

    @Test
    @DisplayName("Debe inicializar requisitos documentales a partir del catálogo cuando no existen")
    void debeInicializarRequisitosDesdeCatalogo() {
        when(usuarioRepository.findByUsernameWithRolesAndPersona("operador_1")).thenReturn(Optional.of(operador));
        when(expedienteRepository.findByIdWithDetalles(100L)).thenReturn(Optional.of(expediente));
        when(revisionRequisitoRepository.findByExpedienteIdWithDetails(100L)).thenReturn(Collections.emptyList());
        when(requisitoDocumentalRepository.findByTipoTramiteIdAndEstadoOrderByOrdenAsc(5L, "ACTIVO"))
                .thenReturn(List.of(reqDni));

        RevisionRequisito revCreada = RevisionRequisito.builder()
                .id(1L)
                .expediente(expediente)
                .requisito(reqDni)
                .estadoRevision(EstadoRevisionRequisito.PENDIENTE_REVISION)
                .build();
        when(revisionRequisitoRepository.save(any(RevisionRequisito.class))).thenReturn(revCreada);

        RevisionRequisitoResponse resp = RevisionRequisitoResponse.builder()
                .id(1L)
                .requisitoId(50L)
                .codigoRequisito("REQ-DNI")
                .nombreRequisito("Copia de DNI")
                .estadoRevision(EstadoRevisionRequisito.PENDIENTE_REVISION)
                .build();
        when(mpvMapper.toRevisionRequisitoResponse(any())).thenReturn(resp);

        List<RevisionRequisitoResponse> result = revisionService.obtenerOInicializarRequisitos(100L, "operador_1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCodigoRequisito()).isEqualTo("REQ-DNI");
        assertThat(result.get(0).getEstadoRevision()).isEqualTo(EstadoRevisionRequisito.PENDIENTE_REVISION);
    }

    @Test
    @DisplayName("Debe rechazar la calificación de requisitos cuando es ejecutada por un ciudadano")
    void debeRechazarCalificacionPorCiudadano() {
        when(usuarioRepository.findByUsernameWithRolesAndPersona("ciudadano_1")).thenReturn(Optional.of(ciudadano));

        VerificarRequisitoRequest req = VerificarRequisitoRequest.builder()
                .estadoRevision(EstadoRevisionRequisito.CONFORME)
                .observaciones("Todo correcto")
                .build();

        assertThatThrownBy(() -> revisionService.verificarRequisito(100L, 50L, req, "ciudadano_1"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("potestad exclusiva del operador municipal");
    }

    @Test
    @DisplayName("Debe calificar un requisito documental exitosamente por el operador")
    void debeCalificarRequisitoExitosamente() {
        when(usuarioRepository.findByUsernameWithRolesAndPersona("operador_1")).thenReturn(Optional.of(operador));
        when(expedienteRepository.findById(100L)).thenReturn(Optional.of(expediente));

        RevisionRequisito revExistente = RevisionRequisito.builder()
                .id(1L)
                .expediente(expediente)
                .requisito(reqDni)
                .estadoRevision(EstadoRevisionRequisito.PENDIENTE_REVISION)
                .build();
        when(revisionRequisitoRepository.findByExpedienteIdAndRequisitoId(100L, 50L))
                .thenReturn(Optional.of(revExistente));

        when(revisionRequisitoRepository.save(any(RevisionRequisito.class))).thenAnswer(inv -> inv.getArgument(0));

        RevisionRequisitoResponse respConforme = RevisionRequisitoResponse.builder()
                .id(1L)
                .requisitoId(50L)
                .estadoRevision(EstadoRevisionRequisito.CONFORME)
                .observaciones("Copia legible y vigente")
                .build();
        when(mpvMapper.toRevisionRequisitoResponse(any())).thenReturn(respConforme);

        VerificarRequisitoRequest req = VerificarRequisitoRequest.builder()
                .estadoRevision(EstadoRevisionRequisito.CONFORME)
                .observaciones("Copia legible y vigente")
                .build();

        RevisionRequisitoResponse response = revisionService.verificarRequisito(100L, 50L, req, "operador_1");

        assertThat(response).isNotNull();
        assertThat(response.getEstadoRevision()).isEqualTo(EstadoRevisionRequisito.CONFORME);
        assertThat(response.getObservaciones()).isEqualTo("Copia legible y vigente");
    }
}
