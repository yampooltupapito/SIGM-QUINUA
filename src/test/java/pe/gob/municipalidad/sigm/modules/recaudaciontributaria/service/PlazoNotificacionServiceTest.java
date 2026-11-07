package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ControlPlazoNotificacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.NotificacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoPlazoNotificacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ControlPlazoNotificacionRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlazoNotificacionServiceTest {

    @Mock
    private ControlPlazoNotificacionRepository controlPlazoRepository;
    @Mock
    private HistorialNotificacionService historialService;

    @InjectMocks
    private PlazoNotificacionService plazoService;

    private NotificacionTributaria notificacion;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder().id(1L).username("abogado_tributario").build();
        notificacion = NotificacionTributaria.builder()
                .id(UUID.randomUUID())
                .codigoNotificacion("NOTIF-2026-000001")
                .build();
    }

    @Test
    @DisplayName("Debe calcular plazo en días hábiles excluyendo sábados y domingos")
    void testCalcularPlazoDiasHabiles() {
        // Miércoles 28 de Octubre 2026:
        // +1 Jue 29 (1), +1 Vie 30 (2), Sab/Dom omitidos, +1 Lun 2 Nov (3)
        LocalDate miercoles = LocalDate.of(2026, 10, 28);
        LocalDate vencimiento = plazoService.calcularFechaVencimiento(miercoles, 3, true);

        assertThat(vencimiento.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(vencimiento).isEqualTo(LocalDate.of(2026, 11, 2));
    }

    @Test
    @DisplayName("Debe registrar y persistir control de plazo con cómputo de días restantes")
    void testRegistrarOActualizarPlazo() {
        when(controlPlazoRepository.findByNotificacionId(notificacion.getId())).thenReturn(Optional.empty());
        when(controlPlazoRepository.save(any(ControlPlazoNotificacion.class))).thenAnswer(i -> i.getArgument(0));

        LocalDate hito = LocalDate.now();
        ControlPlazoNotificacion plazo = plazoService.registrarOActualizarPlazo(
                notificacion,
                "Impugnación 20 días hábiles",
                true,
                20,
                hito,
                usuario
        );

        assertThat(plazo).isNotNull();
        assertThat(plazo.getNumeroDias()).isEqualTo(20);
        assertThat(plazo.getDiasHabiles()).isTrue();
        assertThat(plazo.getEstadoPlazo()).isIn(EstadoPlazoNotificacion.VIGENTE, EstadoPlazoNotificacion.PROXIMO_A_VENCER);
        verify(controlPlazoRepository).save(any(ControlPlazoNotificacion.class));
    }
}

