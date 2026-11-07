package pe.gob.municipalidad.sigm.modules.notificaciones;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.notificaciones.dto.NotificacionDto;
import pe.gob.municipalidad.sigm.modules.notificaciones.model.Notificacion;
import pe.gob.municipalidad.sigm.modules.notificaciones.repository.NotificacionRepository;
import pe.gob.municipalidad.sigm.modules.notificaciones.service.NotificacionService;
import pe.gob.municipalidad.sigm.modules.notificaciones.service.impl.NotificacionServiceImpl;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    @Mock
    private NotificacionRepository notificacionRepository;
    @Mock
    private UsuarioRepository usuarioRepository;

    private NotificacionService notificacionService;

    @BeforeEach
    void setUp() {
        notificacionService = new NotificacionServiceImpl(notificacionRepository, usuarioRepository);
    }

    @Test
    @DisplayName("1. Listar notificaciones recientes del usuario autenticado")
    void testObtenerNotificacionesRecientes() {
        Usuario usuario = Usuario.builder().id(1L).username("admin.quinua").build();
        when(usuarioRepository.findByUsername("admin.quinua")).thenReturn(Optional.of(usuario));

        Notificacion notif = Notificacion.builder()
                .id(10L)
                .usuario(usuario)
                .titulo("Alerta de Sistema")
                .mensaje("Operación completada")
                .tipo("INFO")
                .leida(false)
                .fechaCreacion(OffsetDateTime.now())
                .build();

        when(notificacionRepository.findTop5ByUsuarioIdOrderByFechaCreacionDesc(1L))
                .thenReturn(List.of(notif));

        List<NotificacionDto> resultado = notificacionService.obtenerNotificacionesRecientes("admin.quinua");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getTitulo()).isEqualTo("Alerta de Sistema");
        assertThat(resultado.get(0).isLeida()).isFalse();
    }

    @Test
    @DisplayName("2. Contar notificaciones no leídas")
    void testContarNoLeidas() {
        Usuario usuario = Usuario.builder().id(1L).username("admin.quinua").build();
        when(usuarioRepository.findByUsername("admin.quinua")).thenReturn(Optional.of(usuario));
        when(notificacionRepository.countByUsuarioIdAndLeidaFalse(1L)).thenReturn(3L);

        long noLeidas = notificacionService.contarNoLeidas("admin.quinua");

        assertThat(noLeidas).isEqualTo(3L);
    }

    @Test
    @DisplayName("3. Marcar notificación como leída con validación de pertenencia")
    void testMarcarComoLeida() {
        Usuario usuario = Usuario.builder().id(1L).username("admin.quinua").build();
        when(usuarioRepository.findByUsername("admin.quinua")).thenReturn(Optional.of(usuario));

        Notificacion notif = Notificacion.builder()
                .id(10L)
                .usuario(usuario)
                .leida(false)
                .build();

        when(notificacionRepository.findById(10L)).thenReturn(Optional.of(notif));

        notificacionService.marcarComoLeida(10L, "admin.quinua");

        assertThat(notif.isLeida()).isTrue();
        assertThat(notif.getFechaLectura()).isNotNull();
        verify(notificacionRepository).save(notif);
    }

    @Test
    @DisplayName("4. Marcar todas las notificaciones como leídas en bloque")
    void testMarcarTodasComoLeidas() {
        Usuario usuario = Usuario.builder().id(1L).username("admin.quinua").build();
        when(usuarioRepository.findByUsername("admin.quinua")).thenReturn(Optional.of(usuario));

        notificacionService.marcarTodasComoLeidas("admin.quinua");

        verify(notificacionRepository).marcarTodasComoLeidas(eq(1L), any(OffsetDateTime.class));
    }

    @Test
    @DisplayName("5. Crear nueva notificación para usuario")
    void testCrearNotificacion() {
        Usuario usuario = Usuario.builder().id(2L).username("operador.mesapartes").build();
        when(usuarioRepository.findByUsername("operador.mesapartes")).thenReturn(Optional.of(usuario));

        notificacionService.crearNotificacion(
                "operador.mesapartes",
                "Nuevo Trámite",
                "Se recibió un expediente",
                "INFO",
                "/mesa-partes",
                "MESAPARTES"
        );

        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificacionRepository).save(captor.capture());

        Notificacion guardada = captor.getValue();
        assertThat(guardada.getTitulo()).isEqualTo("Nuevo Trámite");
        assertThat(guardada.getUsuario().getId()).isEqualTo(2L);
        assertThat(guardada.getUrlDestino()).isEqualTo("/mesa-partes");
        assertThat(guardada.isLeida()).isFalse();
    }
}
