package pe.gob.municipalidad.sigm.modules.mesapartes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.UnidadPlazo;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.PlazoAdministrativoService;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class PlazoAdministrativoUnitTest {

    private PlazoAdministrativoService plazoService;

    @BeforeEach
    void setUp() {
        plazoService = new PlazoAdministrativoService();
    }

    @Test
    @DisplayName("Debe calcular plazo en días calendario de forma exacta")
    void testDiasCalendario() {
        OffsetDateTime inicio = OffsetDateTime.of(2026, 5, 10, 10, 0, 0, 0, ZoneOffset.ofHours(-5));
        OffsetDateTime limite = plazoService.calcularFechaLimite(inicio, 15, UnidadPlazo.DIAS_CALENDARIO);

        assertThat(limite).isNotNull();
        assertThat(limite.toLocalDate()).isEqualTo(LocalDate.of(2026, 5, 25));
    }

    @Test
    @DisplayName("Debe calcular plazo en días hábiles excluyendo sábados y domingos")
    void testDiasHabilesExcluyeFinesDeSemana() {
        // Viernes 15 de Mayo de 2026 (sin feriados inmediatos)
        OffsetDateTime inicio = OffsetDateTime.of(2026, 5, 15, 9, 0, 0, 0, ZoneOffset.ofHours(-5));

        // 3 días hábiles: lunes 18 (día 1), martes 19 (día 2), miércoles 20 (día 3)
        OffsetDateTime limite = plazoService.calcularFechaLimite(inicio, 3, UnidadPlazo.DIAS_HABILES);

        assertThat(limite).isNotNull();
        assertThat(limite.toLocalDate()).isEqualTo(LocalDate.of(2026, 5, 20));
    }

    @Test
    @DisplayName("Debe identificar feriados nacionales peruanos como no hábiles")
    void testFeriadosNacionales() {
        // 28 de Julio (Fiestas Patrias)
        assertThat(plazoService.esDiaHabil(LocalDate.of(2026, 7, 28))).isFalse();
        // 9 de Diciembre (Batalla de Ayacucho)
        assertThat(plazoService.esDiaHabil(LocalDate.of(2026, 12, 9))).isFalse();
        // 1 de Mayo (Día del Trabajo)
        assertThat(plazoService.esDiaHabil(LocalDate.of(2026, 5, 1))).isFalse();

        // Un miércoles ordinario (ej: 13 de Mayo 2026)
        assertThat(plazoService.esDiaHabil(LocalDate.of(2026, 5, 13))).isTrue();
    }
}
