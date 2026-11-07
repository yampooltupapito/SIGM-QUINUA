package pe.gob.municipalidad.sigm.modules.mesapartes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.CorrelativoExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.CorrelativoExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.NumeracionExpedienteService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NumeracionExpedienteUnitTest {

    @Mock
    private CorrelativoExpedienteRepository correlativoRepository;

    private NumeracionExpedienteService numeracionService;

    @BeforeEach
    void setUp() {
        numeracionService = new NumeracionExpedienteService(correlativoRepository);
    }

    @Test
    @DisplayName("Debe generar número correlativo con formato MPV-YYYY-000001 para nuevo registro")
    void testGenerarPrimerNumero() {
        when(correlativoRepository.findForUpdate("MPV", 2026)).thenReturn(Optional.empty());
        when(correlativoRepository.saveAndFlush(any(CorrelativoExpediente.class))).thenAnswer(i -> i.getArgument(0));

        String numero = numeracionService.generarSiguienteNumero(2026);

        assertThat(numero).isEqualTo("MPV-2026-000001");
        verify(correlativoRepository).save(any(CorrelativoExpediente.class));
    }

    @Test
    @DisplayName("Debe incrementar correlativo existente respetando formato de 6 dígitos")
    void testIncrementarNumero() {
        CorrelativoExpediente existente = CorrelativoExpediente.builder()
                .id(1L)
                .prefijo("MPV")
                .anio(2026)
                .ultimoNumero(42)
                .build();

        when(correlativoRepository.findForUpdate("MPV", 2026)).thenReturn(Optional.of(existente));

        String numero = numeracionService.generarSiguienteNumero(2026);

        assertThat(numero).isEqualTo("MPV-2026-000043");
        assertThat(existente.getUltimoNumero()).isEqualTo(43);
        verify(correlativoRepository).save(existente);
    }
}
