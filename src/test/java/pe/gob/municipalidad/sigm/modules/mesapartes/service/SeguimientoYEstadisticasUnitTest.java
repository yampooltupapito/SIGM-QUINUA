package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Rol;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.PersonaOficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.DerivacionDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvDtos.ExpedienteResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoDerivacion;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.UnidadPlazo;
import pe.gob.municipalidad.sigm.modules.mesapartes.mapper.DerivacionMapper;
import pe.gob.municipalidad.sigm.modules.mesapartes.mapper.MpvMapper;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.impl.EstadisticasDerivacionServiceImpl;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.impl.SeguimientoInternoServiceImpl;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeguimientoYEstadisticasUnitTest {

    @Mock
    private ExpedienteRepository expedienteRepository;
    @Mock
    private DerivacionExpedienteRepository derivacionRepository;
    @Mock
    private AsignacionExpedienteRepository asignacionRepository;
    @Mock
    private ExpedienteHistorialRepository historialRepository;
    @Mock
    private ActuacionExpedienteService actuacionService;
    @Mock
    private PlazoAdministrativoService plazoService;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private OficinaRepository oficinaRepository;
    @Mock
    private PersonaOficinaRepository personaOficinaRepository;
    @Mock
    private DerivacionMapper derivacionMapper;
    @Mock
    private MpvMapper mpvMapper;

    @InjectMocks
    private SeguimientoInternoServiceImpl seguimientoService;

    private EstadisticasDerivacionServiceImpl estadisticasService;

    private Usuario jefe;
    private Oficina oficina;
    private TipoTramite tipoTramite;
    private Expediente expediente;

    @BeforeEach
    void setUp() {
        estadisticasService = new EstadisticasDerivacionServiceImpl(
                derivacionRepository, expedienteRepository, asignacionRepository,
                oficinaRepository, usuarioRepository, personaOficinaRepository, derivacionMapper, mpvMapper
        );

        Persona persona = Persona.builder().id(1L).nombres("Jefe").apellidoPaterno("Oficina").build();
        Rol rolAdmin = Rol.builder().id(1L).codigo("ROLE_ADMIN").build();

        jefe = Usuario.builder()
                .id(1L)
                .username("jefe_oficina")
                .persona(persona)
                .roles(Set.of(rolAdmin))
                .enabled(true)
                .build();

        oficina = Oficina.builder().id(200L).codigo("DESURB").nombre("Desarrollo Urbano").estado("ACTIVO").build();

        tipoTramite = TipoTramite.builder()
                .id(10L)
                .nombre("Licencia de Edificación")
                .plazoDias(30)
                .unidadPlazo(UnidadPlazo.DIAS_HABILES)
                .build();

        expediente = Expediente.builder()
                .id(50L)
                .numeroExpediente("MPV-2026-00001")
                .asunto("Construcción Vivienda")
                .tipoTramite(tipoTramite)
                .estadoActual(EstadoExpediente.EN_ATENCION)
                .oficinaResponsable(oficina)
                .fechaPresentacion(OffsetDateTime.now().minusDays(5))
                .fechaRecepcion(OffsetDateTime.now().minusDays(4))
                .build();
    }

    @Test
    @DisplayName("Seguimiento: Construye recorrido y calcula plazos correctamente")
    void testObtenerSeguimientoCompleto() {
        when(expedienteRepository.findByIdWithDetalles(50L)).thenReturn(Optional.of(expediente));
        when(derivacionRepository.findByExpedienteIdOrderByFechaDerivacionDesc(50L)).thenReturn(Collections.emptyList());
        when(asignacionRepository.findByExpedienteIdOrderByFechaAsignacionDesc(50L)).thenReturn(Collections.emptyList());
        when(actuacionService.listarActuaciones(50L, "jefe_oficina")).thenReturn(Collections.emptyList());
        when(historialRepository.findByExpedienteIdOrderByFechaMovimientoAsc(50L)).thenReturn(Collections.emptyList());
        when(mpvMapper.toExpedienteResponse(any(Expediente.class))).thenReturn(ExpedienteResponse.builder().id(50L).build());

        OffsetDateTime fechaLimiteCalculada = OffsetDateTime.now().plusDays(25);
        when(plazoService.calcularFechaLimite(any(), eq(30), eq(UnidadPlazo.DIAS_HABILES))).thenReturn(fechaLimiteCalculada);

        SeguimientoInternoResponse resp = seguimientoService.obtenerSeguimiento(50L, "jefe_oficina");

        assertNotNull(resp);
        assertNotNull(resp.getPlazo());
        assertEquals("EN_PLAZO", resp.getPlazo().getEstadoPlazo());
        assertEquals(30, resp.getPlazo().getPlazoDias());
        assertNotNull(resp.getRecorrido());
        assertFalse(resp.getRecorrido().isEmpty());
    }

    @Test
    @DisplayName("Estadísticas: Retorna conteos agregados por oficina")
    void testObtenerEstadisticasPorOficina() {
        when(usuarioRepository.findByUsername("jefe_oficina")).thenReturn(Optional.of(jefe));
        when(derivacionRepository.countByOficinaOrigenId(200L)).thenReturn(10L);
        when(derivacionRepository.countByOficinaDestinoIdAndEstado(200L, EstadoDerivacion.PENDIENTE)).thenReturn(3L);
        when(derivacionRepository.countByOficinaDestinoIdAndEstado(200L, EstadoDerivacion.RECIBIDA)).thenReturn(7L);
        when(expedienteRepository.countByOficinaResponsableId(200L)).thenReturn(15L);
        when(expedienteRepository.countByOficinaResponsableIdAndResponsableActualIsNotNull(200L)).thenReturn(12L);
        when(expedienteRepository.countByOficinaResponsableIdAndResponsableActualIsNull(200L)).thenReturn(3L);

        EstadisticasDerivacionResponse stats = estadisticasService.obtenerEstadisticas(200L, "jefe_oficina");

        assertNotNull(stats);
        assertEquals(10L, stats.getDerivacionesEnviadas());
        assertEquals(3L, stats.getDerivacionesPendientes());
        assertEquals(7L, stats.getDerivacionesRecibidas());
        assertEquals(15L, stats.getTotalExpedientesOficina());
        assertEquals(12L, stats.getExpedientesAsignados());
        assertEquals(3L, stats.getExpedientesSinResponsable());
    }
}
