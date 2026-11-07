package pe.gob.municipalidad.sigm.modules.mesapartes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Rol;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvAdminDtos.BandejaEstadisticasAdminResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvDtos.ExpedienteResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.Expediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.CanalIngreso;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.PrioridadExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.mapper.MpvMapper;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.BandejaMesaPartesServiceImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias - Bandeja Institucional y Estadísticas (Prompt 14)")
class BandejaMesaPartesUnitTest {

    @Mock
    private ExpedienteRepository expedienteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private MpvMapper mpvMapper;

    @InjectMocks
    private BandejaMesaPartesServiceImpl bandejaService;

    private Usuario usuarioOperador;
    private Usuario usuarioCiudadano;

    @BeforeEach
    void setUp() {
        Rol rolOperador = Rol.builder().id(1L).nombre("ROLE_OPERADOR").build();
        Rol rolCiudadano = Rol.builder().id(2L).nombre("ROLE_CIUDADANO").build();

        Persona personaOperador = Persona.builder().id(10L).nombres("Carlos").apellidoPaterno("Operador").build();
        Persona personaCiudadano = Persona.builder().id(20L).nombres("María").apellidoPaterno("Ciudadana").build();

        usuarioOperador = Usuario.builder()
                .id(1L)
                .username("operador_mpv")
                .persona(personaOperador)
                .roles(Set.of(rolOperador))
                .build();

        usuarioCiudadano = Usuario.builder()
                .id(2L)
                .username("ciudadano_vecino")
                .persona(personaCiudadano)
                .roles(Set.of(rolCiudadano))
                .build();
    }

    @Test
    @DisplayName("Debe rechazar el acceso a la bandeja institucional para usuarios con rol exclusivo de ciudadano")
    void debeRechazarAccesoBandejaParaCiudadanos() {
        when(usuarioRepository.findByUsernameWithRolesAndPersona("ciudadano_vecino"))
                .thenReturn(Optional.of(usuarioCiudadano));

        assertThatThrownBy(() -> bandejaService.listarBandeja(
                null, null, null, null, null, null, null, null, PageRequest.of(0, 10), "ciudadano_vecino"
        )).isInstanceOf(AccessDeniedException.class)
          .hasMessageContaining("Acceso restringido");
    }

    @Test
    @DisplayName("Debe listar expedientes en la bandeja institucional para operadores autorizados")
    void debeListarExpedientesParaOperador() {
        when(usuarioRepository.findByUsernameWithRolesAndPersona("operador_mpv"))
                .thenReturn(Optional.of(usuarioOperador));

        Expediente exp = Expediente.builder()
                .id(100L)
                .uuid(UUID.randomUUID())
                .numeroExpediente("MPV-2026-000001")
                .asunto("Solicitud de Licencia")
                .estadoActual(EstadoExpediente.PRESENTADO)
                .prioridad(PrioridadExpediente.NORMAL)
                .canalIngreso(CanalIngreso.VIRTUAL)
                .build();

        Page<Expediente> page = new PageImpl<>(List.of(exp));
        when(expedienteRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(page);

        ExpedienteResponse expResp = ExpedienteResponse.builder()
                .id(100L)
                .numeroExpediente("MPV-2026-000001")
                .asunto("Solicitud de Licencia")
                .estadoActual(EstadoExpediente.PRESENTADO)
                .build();
        when(mpvMapper.toExpedienteResponse(exp)).thenReturn(expResp);

        Page<ExpedienteResponse> result = bandejaService.listarBandeja(
                EstadoExpediente.PRESENTADO, null, null, PrioridadExpediente.NORMAL, CanalIngreso.VIRTUAL,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), "licencia", PageRequest.of(0, 10), "operador_mpv"
        );

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getNumeroExpediente()).isEqualTo("MPV-2026-000001");
    }

    @Test
    @DisplayName("Debe calcular estadísticas agregadas reales para la bandeja")
    void debeCalcularEstadisticasBandeja() {
        when(usuarioRepository.findByUsernameWithRolesAndPersona("operador_mpv"))
                .thenReturn(Optional.of(usuarioOperador));

        when(expedienteRepository.countByOficina(any())).thenReturn(50L);
        when(expedienteRepository.countByEstadoAndOficina(eq(EstadoExpediente.PRESENTADO), any())).thenReturn(10L);
        when(expedienteRepository.countByEstadoAndOficina(eq(EstadoExpediente.RECIBIDO), any())).thenReturn(15L);
        when(expedienteRepository.countByEstadoAndOficina(eq(EstadoExpediente.EN_REVISION), any())).thenReturn(8L);
        when(expedienteRepository.countByEstadoAndOficina(eq(EstadoExpediente.OBSERVADO), any())).thenReturn(5L);
        when(expedienteRepository.countByEstadoAndOficina(eq(EstadoExpediente.EN_SUBSANACION), any())).thenReturn(2L);
        when(expedienteRepository.countByEstadoAndOficina(eq(EstadoExpediente.RESUELTO), any())).thenReturn(10L);

        BandejaEstadisticasAdminResponse stats = bandejaService.obtenerEstadisticasBandeja("operador_mpv");

        assertThat(stats).isNotNull();
        assertThat(stats.getTotalExpedientes()).isEqualTo(50L);
        assertThat(stats.getPresentados()).isEqualTo(10L);
        assertThat(stats.getRecibidos()).isEqualTo(15L);
        assertThat(stats.getEnRevision()).isEqualTo(8L);
        assertThat(stats.getObservados()).isEqualTo(5L);
        assertThat(stats.getPendientesSubsanacion()).isEqualTo(2L);
        assertThat(stats.getResueltos()).isEqualTo(10L);
    }
}
