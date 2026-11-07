package pe.gob.municipalidad.sigm.modules.mercados;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.administracion.service.AuditoriaAdministrativaService;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.*;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;
import pe.gob.municipalidad.sigm.modules.mercados.service.IncidenciaMercadoService;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MercadoIncidenciaServiceTest {

    @Mock private IncidenciaMercadoRepository incidenciaRepository;
    @Mock private TipoIncidenciaMercadoRepository tipoRepository;
    @Mock private ActuacionIncidenciaRepository actuacionRepository;
    @Mock private EvidenciaIncidenciaMercadoRepository evidenciaRepository;
    @Mock private PresuntaInfraccionMercadoRepository infraccionRepository;
    @Mock private MercadoMunicipalRepository mercadoRepository;
    @Mock private SectorMercadoRepository sectorRepository;
    @Mock private PabellonMercadoRepository pabellonRepository;
    @Mock private PuestoComercialRepository puestoRepository;
    @Mock private ComercianteRepository comercianteRepository;
    @Mock private InspeccionMercadoRepository inspeccionRepository;
    @Mock private OficinaRepository oficinaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private AuditoriaAdministrativaService auditoriaService;

    private IncidenciaMercadoService incidenciaService;

    private MercadoMunicipal mercado1;
    private MercadoMunicipal mercado2;
    private SectorMercado sector1;
    private PuestoComercial puesto1;
    private TipoIncidenciaMercado tipo1;
    private Usuario responsable;
    private Oficina oficina;

    @BeforeEach
    void setUp() {
        incidenciaService = new IncidenciaMercadoService(
                incidenciaRepository,
                tipoRepository,
                actuacionRepository,
                evidenciaRepository,
                infraccionRepository,
                mercadoRepository,
                sectorRepository,
                pabellonRepository,
                puestoRepository,
                comercianteRepository,
                inspeccionRepository,
                oficinaRepository,
                usuarioRepository,
                auditoriaService
        );

        mercado1 = MercadoMunicipal.builder()
                .id(UUID.randomUUID())
                .codigo("MER-01")
                .nombre("Mercado Central de Quinua")
                .estado(EstadoMercado.ACTIVO)
                .build();

        mercado2 = MercadoMunicipal.builder()
                .id(UUID.randomUUID())
                .codigo("MER-02")
                .nombre("Mercado de Abastos San Pedro")
                .estado(EstadoMercado.ACTIVO)
                .build();

        sector1 = SectorMercado.builder()
                .id(UUID.randomUUID())
                .mercado(mercado1)
                .codigo("SEC-A")
                .nombre("Sector A")
                .build();

        puesto1 = PuestoComercial.builder()
                .id(UUID.randomUUID())
                .mercado(mercado1)
                .sector(sector1)
                .numero("P-101")
                .estado(EstadoPuesto.OCUPADO)
                .build();

        tipo1 = TipoIncidenciaMercado.builder()
                .id(UUID.randomUUID())
                .codigo("INFRAESTRUCTURA")
                .nombre("Problema de Infraestructura")
                .activo(true)
                .build();

        oficina = Oficina.builder()
                .id(1L)
                .nombre("Subgerencia de Servicios Municipales")
                .build();

        Persona personaResp = Persona.builder()
                .nombres("Carlos")
                .apellidoPaterno("Mendoza")
                .build();

        responsable = Usuario.builder()
                .id(10L)
                .username("cmendoza")
                .persona(personaResp)
                .build();
    }

    @Test
    @DisplayName("Debe registrar incidencia exitosamente con correlativo institucional")
    void registrarIncidenciaExitoso() {
        IncidenciaCreateRequest request = IncidenciaCreateRequest.builder()
                .mercadoId(mercado1.getId())
                .sectorId(sector1.getId())
                .puestoId(puesto1.getId())
                .tipoIncidenciaId(tipo1.getId())
                .origen(OrigenIncidencia.PERSONAL_MUNICIPAL)
                .descripcion("Fisura visible en muro lateral norte del puesto")
                .prioridad(PrioridadIncidencia.MEDIA)
                .build();

        when(mercadoRepository.findById(mercado1.getId())).thenReturn(Optional.of(mercado1));
        when(sectorRepository.findById(sector1.getId())).thenReturn(Optional.of(sector1));
        when(puestoRepository.findById(puesto1.getId())).thenReturn(Optional.of(puesto1));
        when(tipoRepository.findById(tipo1.getId())).thenReturn(Optional.of(tipo1));
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(responsable));
        when(incidenciaRepository.obtenerSiguienteCorrelativo()).thenReturn(1L);

        when(incidenciaRepository.save(any(IncidenciaMercado.class))).thenAnswer(invocation -> {
            IncidenciaMercado entidad = invocation.getArgument(0);
            entidad.setId(UUID.randomUUID());
            return entidad;
        });

        IncidenciaResponse response = incidenciaService.registrar(request, "admin");

        assertThat(response).isNotNull();
        assertThat(response.getCodigoIncidencia()).startsWith("INC-MER-");
        assertThat(response.getEstado()).isEqualTo(EstadoIncidencia.REGISTRADA);
        assertThat(response.getMercadoNombre()).isEqualTo("Mercado Central de Quinua");

        verify(incidenciaRepository, times(1)).save(any(IncidenciaMercado.class));
        verify(auditoriaService, times(1)).registrar(eq("REGISTRAR_INCIDENCIA_MERCADO"), anyString(), any(), eq("EXITO"), anyString());
    }

    @Test
    @DisplayName("Debe rechazar registro si el puesto pertenece a otro mercado")
    void registrarIncidenciaRechazaPuestoInconsistente() {
        PuestoComercial puestoOtroMercado = PuestoComercial.builder()
                .id(UUID.randomUUID())
                .mercado(mercado2)
                .numero("P-202")
                .build();

        IncidenciaCreateRequest request = IncidenciaCreateRequest.builder()
                .mercadoId(mercado1.getId())
                .puestoId(puestoOtroMercado.getId())
                .tipoIncidenciaId(tipo1.getId())
                .origen(OrigenIncidencia.PERSONAL_MUNICIPAL)
                .descripcion("Fuga de agua en lavadero")
                .prioridad(PrioridadIncidencia.ALTA)
                .build();

        when(mercadoRepository.findById(mercado1.getId())).thenReturn(Optional.of(mercado1));
        when(puestoRepository.findById(puestoOtroMercado.getId())).thenReturn(Optional.of(puestoOtroMercado));

        assertThatThrownBy(() -> incidenciaService.registrar(request, "admin"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no pertenece al mercado indicado");

        verify(incidenciaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe asignar responsable operativo y cambiar estado a ASIGNADA")
    void asignarResponsableExitoso() {
        UUID incidenciaId = UUID.randomUUID();
        IncidenciaMercado incidencia = IncidenciaMercado.builder()
                .id(incidenciaId)
                .codigoIncidencia("INC-MER-2026-000001")
                .mercado(mercado1)
                .tipoIncidencia(tipo1)
                .descripcion("Fisura en muro")
                .prioridad(PrioridadIncidencia.MEDIA)
                .estado(EstadoIncidencia.REGISTRADA)
                .build();

        IncidenciaAsignacionRequest request = IncidenciaAsignacionRequest.builder()
                .oficinaResponsableId(1L)
                .responsableId(10L)
                .prioridad(PrioridadIncidencia.ALTA)
                .observaciones("Inspeccionar urgentemente")
                .build();

        when(incidenciaRepository.findById(incidenciaId)).thenReturn(Optional.of(incidencia));
        when(oficinaRepository.findById(1L)).thenReturn(Optional.of(oficina));
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(responsable));
        when(usuarioRepository.findByUsername("supervisor")).thenReturn(Optional.of(responsable));
        when(incidenciaRepository.save(any(IncidenciaMercado.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IncidenciaResponse response = incidenciaService.asignarResponsable(incidenciaId, request, "supervisor");

        assertThat(response.getEstado()).isEqualTo(EstadoIncidencia.ASIGNADA);
        assertThat(response.getPrioridad()).isEqualTo(PrioridadIncidencia.ALTA);
        assertThat(response.getResponsableNombre()).isEqualTo("Carlos Mendoza");

        verify(actuacionRepository, times(1)).save(any(ActuacionIncidenciaMercado.class));
    }

    @Test
    @DisplayName("Debe registrar actuación cronológica y actualizar estado a EN_ATENCION")
    void registrarActuacionExitoso() {
        UUID incidenciaId = UUID.randomUUID();
        IncidenciaMercado incidencia = IncidenciaMercado.builder()
                .id(incidenciaId)
                .codigoIncidencia("INC-MER-2026-000001")
                .mercado(mercado1)
                .tipoIncidencia(tipo1)
                .descripcion("Fisura en muro")
                .prioridad(PrioridadIncidencia.ALTA)
                .estado(EstadoIncidencia.ASIGNADA)
                .actuaciones(new ArrayList<>())
                .build();

        IncidenciaActuacionRequest request = IncidenciaActuacionRequest.builder()
                .tipoActuacion(TipoActuacionIncidencia.DILIGENCIA_CAMPO)
                .descripcion("Se realizó visita técnica verificando el estado del muro")
                .nuevoEstado(EstadoIncidencia.EN_ATENCION)
                .build();

        when(incidenciaRepository.findById(incidenciaId)).thenReturn(Optional.of(incidencia));
        when(usuarioRepository.findByUsername("cmendoza")).thenReturn(Optional.of(responsable));
        when(incidenciaRepository.save(any(IncidenciaMercado.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IncidenciaResponse response = incidenciaService.registrarActuacion(incidenciaId, request, "cmendoza");

        assertThat(response.getEstado()).isEqualTo(EstadoIncidencia.EN_ATENCION);
        verify(actuacionRepository, times(1)).save(any(ActuacionIncidenciaMercado.class));
    }

    @Test
    @DisplayName("Debe resolver incidencia y guardar fecha y sustento de resolución")
    void resolverIncidenciaExitoso() {
        UUID incidenciaId = UUID.randomUUID();
        IncidenciaMercado incidencia = IncidenciaMercado.builder()
                .id(incidenciaId)
                .codigoIncidencia("INC-MER-2026-000001")
                .mercado(mercado1)
                .tipoIncidencia(tipo1)
                .descripcion("Fisura en muro")
                .estado(EstadoIncidencia.EN_ATENCION)
                .build();

        IncidenciaResolucionRequest request = IncidenciaResolucionRequest.builder()
                .motivoResolucion("Muro resanado y reforzado por personal de obras públicas")
                .build();

        when(incidenciaRepository.findById(incidenciaId)).thenReturn(Optional.of(incidencia));
        when(usuarioRepository.findByUsername("cmendoza")).thenReturn(Optional.of(responsable));
        when(incidenciaRepository.save(any(IncidenciaMercado.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IncidenciaResponse response = incidenciaService.resolver(incidenciaId, request, "cmendoza");

        assertThat(response.getEstado()).isEqualTo(EstadoIncidencia.RESUELTA);
        verify(actuacionRepository, times(1)).save(any(ActuacionIncidenciaMercado.class));
    }

    @Test
    @DisplayName("Debe rechazar resolver incidencia si ya está CERRADA")
    void rechazaResolverIncidenciaCerrada() {
        UUID incidenciaId = UUID.randomUUID();
        IncidenciaMercado incidencia = IncidenciaMercado.builder()
                .id(incidenciaId)
                .codigoIncidencia("INC-MER-2026-000001")
                .mercado(mercado1)
                .tipoIncidencia(tipo1)
                .estado(EstadoIncidencia.CERRADA)
                .build();

        IncidenciaResolucionRequest request = IncidenciaResolucionRequest.builder()
                .motivoResolucion("Intento de resolver cerrada")
                .build();

        when(incidenciaRepository.findById(incidenciaId)).thenReturn(Optional.of(incidencia));

        assertThatThrownBy(() -> incidenciaService.resolver(incidenciaId, request, "cmendoza"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("No se puede resolver una incidencia en estado");
    }

    @Test
    @DisplayName("Debe cerrar incidencia con motivo justificado")
    void cerrarIncidenciaExitoso() {
        UUID incidenciaId = UUID.randomUUID();
        IncidenciaMercado incidencia = IncidenciaMercado.builder()
                .id(incidenciaId)
                .codigoIncidencia("INC-MER-2026-000001")
                .mercado(mercado1)
                .tipoIncidencia(tipo1)
                .estado(EstadoIncidencia.RESUELTA)
                .build();

        IncidenciaCierreRequest request = IncidenciaCierreRequest.builder()
                .estadoFinal(EstadoIncidencia.CERRADA)
                .motivoCierre("Verificación final conforme de obras concluidas")
                .build();

        when(incidenciaRepository.findById(incidenciaId)).thenReturn(Optional.of(incidencia));
        when(usuarioRepository.findByUsername("supervisor")).thenReturn(Optional.of(responsable));
        when(incidenciaRepository.save(any(IncidenciaMercado.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IncidenciaResponse response = incidenciaService.cerrar(incidenciaId, request, "supervisor");

        assertThat(response.getEstado()).isEqualTo(EstadoIncidencia.CERRADA);
    }
}
