package pe.gob.municipalidad.sigm.modules.serenazgo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.TurnoCreateRequest;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.TurnoResponse;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.TurnoUpdateRequest;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoTurnoSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoTurnoSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.TurnoSerenazgoRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.TurnoSerenazgoService;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class TurnoSerenazgoServiceTest {

    @Autowired
    private TurnoSerenazgoService turnoService;

    @Autowired
    private TurnoSerenazgoRepository turnoRepository;

    @Test
    @DisplayName("01: Creación de turno diurno regular y cálculo de duración")
    void test01_crearTurnoDiurnoExitoso() {
        long ts = System.currentTimeMillis();
        TurnoCreateRequest req = TurnoCreateRequest.builder()
                .nombre("Turno Mañana Operativo " + ts)
                .tipoTurno(TipoTurnoSerenazgo.DIURNO)
                .horaInicio(LocalTime.of(7, 0))
                .horaFin(LocalTime.of(15, 0))
                .cruzaMedianoche(false)
                .colorHex("#2563eb")
                .descripcion("Patrullaje preventivo matutino")
                .build();

        TurnoResponse resp = turnoService.crearTurno(req, "admin");

        assertThat(resp).isNotNull();
        assertThat(resp.getId()).isNotNull();
        assertThat(resp.getCodigo()).startsWith("TUR-");
        assertThat(resp.getNombre()).isEqualTo("Turno Mañana Operativo " + ts);
        assertThat(resp.getDuracionReferencialMinutos()).isEqualTo(480); // 8 horas = 480 minutos
        assertThat(resp.getCruzaMedianoche()).isFalse();
        assertThat(resp.getEstado()).isEqualTo(EstadoTurnoSerenazgo.ACTIVO);
    }

    @Test
    @DisplayName("02: Creación de turno nocturno que cruza medianoche (22:00 a 06:00)")
    void test02_crearTurnoCruzaMedianoche() {
        long ts = System.currentTimeMillis();
        TurnoCreateRequest req = TurnoCreateRequest.builder()
                .nombre("Turno Amanecida " + ts)
                .tipoTurno(TipoTurnoSerenazgo.NOCTURNO)
                .horaInicio(LocalTime.of(22, 0))
                .horaFin(LocalTime.of(6, 0))
                .cruzaMedianoche(true)
                .colorHex("#4f46e5")
                .descripcion("Guardia nocturna y amanecida de seguridad")
                .build();

        TurnoResponse resp = turnoService.crearTurno(req, "supervisor");

        assertThat(resp).isNotNull();
        assertThat(resp.getCruzaMedianoche()).isTrue();
        // 22:00 a medianoche = 120 mins, medianoche a 06:00 = 360 mins. Total = 480 mins
        assertThat(resp.getDuracionReferencialMinutos()).isEqualTo(480);
    }

    @Test
    @DisplayName("03: Detección automática de cruce de medianoche si horaFin <= horaInicio")
    void test03_autoDeteccionCruzaMedianoche() {
        long ts = System.currentTimeMillis();
        TurnoCreateRequest req = TurnoCreateRequest.builder()
                .nombre("Turno Noche Especial " + ts)
                .tipoTurno(TipoTurnoSerenazgo.ESPECIAL)
                .horaInicio(LocalTime.of(20, 0))
                .horaFin(LocalTime.of(4, 0))
                .cruzaMedianoche(false) // marcado false pero horaFin < horaInicio
                .colorHex("#7c3aed")
                .build();

        TurnoResponse resp = turnoService.crearTurno(req, "admin");

        assertThat(resp.getCruzaMedianoche()).isTrue();
        // 20:00 a 04:00 = 8 horas = 480 mins
        assertThat(resp.getDuracionReferencialMinutos()).isEqualTo(480);
    }

    @Test
    @DisplayName("04: Rechazo de turno con nombre duplicado")
    void test04_rechazarTurnoDuplicado() {
        long ts = System.currentTimeMillis();
        String nombre = "Turno Único " + ts;

        turnoService.crearTurno(TurnoCreateRequest.builder()
                .nombre(nombre)
                .tipoTurno(TipoTurnoSerenazgo.DIURNO)
                .horaInicio(LocalTime.of(8, 0))
                .horaFin(LocalTime.of(16, 0))
                .build(), "admin");

        assertThatThrownBy(() -> turnoService.crearTurno(TurnoCreateRequest.builder()
                .nombre(nombre)
                .tipoTurno(TipoTurnoSerenazgo.DIURNO)
                .horaInicio(LocalTime.of(8, 0))
                .horaFin(LocalTime.of(16, 0))
                .build(), "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe un turno registrado con el nombre");
    }

    @Test
    @DisplayName("05: Actualización de turno existente")
    void test05_actualizarTurnoExitoso() {
        long ts = System.currentTimeMillis();
        TurnoResponse creado = turnoService.crearTurno(TurnoCreateRequest.builder()
                .nombre("Turno Tarde " + ts)
                .tipoTurno(TipoTurnoSerenazgo.DIURNO)
                .horaInicio(LocalTime.of(14, 0))
                .horaFin(LocalTime.of(22, 0))
                .build(), "admin");

        TurnoUpdateRequest updateReq = TurnoUpdateRequest.builder()
                .nombre("Turno Tarde Modificado " + ts)
                .tipoTurno(TipoTurnoSerenazgo.MIXTO)
                .horaInicio(LocalTime.of(15, 0))
                .horaFin(LocalTime.of(23, 0))
                .cruzaMedianoche(false)
                .descripcion("Horario vespertino extendido")
                .colorHex("#ea580c")
                .estado(EstadoTurnoSerenazgo.ACTIVO)
                .build();

        TurnoResponse actualizado = turnoService.actualizarTurno(creado.getId(), updateReq, "admin");

        assertThat(actualizado.getNombre()).isEqualTo("Turno Tarde Modificado " + ts);
        assertThat(actualizado.getTipoTurno()).isEqualTo(TipoTurnoSerenazgo.MIXTO);
        assertThat(actualizado.getHoraInicio()).isEqualTo(LocalTime.of(15, 0));
        assertThat(actualizado.getHoraFin()).isEqualTo(LocalTime.of(23, 0));
        assertThat(actualizado.getDuracionReferencialMinutos()).isEqualTo(480);
    }

    @Test
    @DisplayName("06: Conmutación de estado de turno (ACTIVO <-> INACTIVO)")
    void test06_cambiarEstadoTurno() {
        long ts = System.currentTimeMillis();
        TurnoResponse creado = turnoService.crearTurno(TurnoCreateRequest.builder()
                .nombre("Turno Temporal " + ts)
                .tipoTurno(TipoTurnoSerenazgo.EXTRAORDINARIO)
                .horaInicio(LocalTime.of(10, 0))
                .horaFin(LocalTime.of(14, 0))
                .build(), "admin");

        TurnoResponse inactivo = turnoService.cambiarEstadoTurno(creado.getId(), EstadoTurnoSerenazgo.INACTIVO, "admin");
        assertThat(inactivo.getEstado()).isEqualTo(EstadoTurnoSerenazgo.INACTIVO);

        TurnoResponse reactivado = turnoService.cambiarEstadoTurno(creado.getId(), EstadoTurnoSerenazgo.ACTIVO, "admin");
        assertThat(reactivado.getEstado()).isEqualTo(EstadoTurnoSerenazgo.ACTIVO);
    }
}
