package pe.gob.municipalidad.sigm.modules.mesapartes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvSolicitudDtos.EsquemaTramiteResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoFormularioVersion;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoCampoFormulario;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.UnidadPlazo;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.FormularioDinamicoServiceImpl;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias - Motor de Formularios Dinámicos")
class FormularioDinamicoUnitTest {

    @Mock
    private TipoTramiteRepository tipoTramiteRepository;

    @Mock
    private FormularioTramiteRepository formularioRepository;

    @Mock
    private FormularioVersionRepository versionRepository;

    @Mock
    private FormularioCampoRepository campoRepository;

    @Mock
    private RespuestaFormularioRepository respuestaRepository;

    @Mock
    private RespuestaValorRepository respuestaValorRepository;

    @Mock
    private RequisitoDocumentalRepository requisitoRepository;

    @InjectMocks
    private FormularioDinamicoServiceImpl formularioService;

    private TipoTramite tramite;
    private FormularioTramite form;
    private FormularioVersion version;
    private List<FormularioCampo> campos;

    @BeforeEach
    void setUp() {
        tramite = TipoTramite.builder()
                .id(1L)
                .codigo("TRAM-POS")
                .nombre("Constancia de Posesión")
                .descripcion("Trámite para constancia de posesión en Quinua")
                .plazoDias(15)
                .unidadPlazo(UnidadPlazo.DIAS_HABILES)
                .permitePresentacionVirtual(true)
                .estado("ACTIVO")
                .build();

        form = FormularioTramite.builder()
                .id(10L)
                .tipoTramite(tramite)
                .codigo("FORM-POS")
                .nombre("Formulario de Posesión")
                .activo(true)
                .build();

        version = FormularioVersion.builder()
                .id(100L)
                .formulario(form)
                .version(1)
                .estado(EstadoFormularioVersion.PUBLICADO)
                .build();

        FormularioCampo c1 = FormularioCampo.builder()
                .id(1L)
                .version(version)
                .nombreCampo("direccion_predio")
                .etiqueta("Dirección del Predio")
                .tipoCampo(TipoCampoFormulario.TEXTO)
                .obligatorio(true)
                .longitudMaxima(100)
                .orden(1)
                .build();

        FormularioCampo c2 = FormularioCampo.builder()
                .id(2L)
                .version(version)
                .nombreCampo("area_m2")
                .etiqueta("Área en m²")
                .tipoCampo(TipoCampoFormulario.NUMERO)
                .obligatorio(true)
                .orden(2)
                .build();

        FormularioCampo c3 = FormularioCampo.builder()
                .id(3L)
                .version(version)
                .nombreCampo("fecha_posesion")
                .etiqueta("Fecha de Inicio de Posesión")
                .tipoCampo(TipoCampoFormulario.FECHA)
                .obligatorio(false)
                .orden(3)
                .build();

        campos = List.of(c1, c2, c3);
    }

    @Test
    @DisplayName("Debe recuperar el esquema completo del trámite con formulario y requisitos")
    void testObtenerEsquemaTramite() {
        when(tipoTramiteRepository.findById(1L)).thenReturn(Optional.of(tramite));
        when(requisitoRepository.findByTipoTramiteIdAndEstadoOrderByOrdenAsc(1L, "ACTIVO"))
                .thenReturn(List.of(
                        RequisitoDocumental.builder().id(1L).codigo("MEM").nombre("Memoria").obligatorio(true).formatosPermitidos("application/pdf").tamanoMaximoMb(15).orden(1).build()
                ));
        when(formularioRepository.findByTipoTramiteIdAndActivoTrue(1L)).thenReturn(Optional.of(form));
        when(versionRepository.findFirstByFormularioIdAndEstadoOrderByVersionDesc(10L, EstadoFormularioVersion.PUBLICADO)).thenReturn(Optional.of(version));
        when(campoRepository.findByVersionIdWithOpciones(100L)).thenReturn(campos);

        EsquemaTramiteResponse esquema = formularioService.obtenerEsquemaTramite(1L);

        assertNotNull(esquema);
        assertEquals("TRAM-POS", esquema.getCodigo());
        assertEquals(1, esquema.getRequisitos().size());
        assertNotNull(esquema.getFormulario());
        assertEquals(3, esquema.getFormulario().getCampos().size());
    }

    @Test
    @DisplayName("Debe validar exitosamente cuando todos los campos requeridos son válidos")
    void testValidarRespuestasExitoso() {
        when(campoRepository.findByVersionIdWithOpciones(100L)).thenReturn(campos);

        Map<String, String> respuestas = new HashMap<>();
        respuestas.put("direccion_predio", "Jr. Sucre 123, Quinua");
        respuestas.put("area_m2", "150.50");
        respuestas.put("fecha_posesion", "2020-05-18");

        assertDoesNotThrow(() -> formularioService.validarRespuestas(version, respuestas, true));
    }

    @Test
    @DisplayName("Debe rechazar la presentación si falta un campo obligatorio")
    void testValidarRespuestasFaltaObligatorio() {
        when(campoRepository.findByVersionIdWithOpciones(100L)).thenReturn(campos);

        Map<String, String> respuestas = new HashMap<>();
        respuestas.put("area_m2", "150"); // Falta direccion_predio

        BusinessException ex = assertThrows(BusinessException.class,
                () -> formularioService.validarRespuestas(version, respuestas, true));
        assertTrue(ex.getMessage().contains("Dirección del Predio"));
    }

    @Test
    @DisplayName("Debe rechazar si un campo numérico contiene texto no válido")
    void testValidarRespuestasNumeroInvalido() {
        when(campoRepository.findByVersionIdWithOpciones(100L)).thenReturn(campos);

        Map<String, String> respuestas = new HashMap<>();
        respuestas.put("direccion_predio", "Calle Real 456");
        respuestas.put("area_m2", "CIENTO_CINCUENTA"); // No es número

        BusinessException ex = assertThrows(BusinessException.class,
                () -> formularioService.validarRespuestas(version, respuestas, false));
        assertTrue(ex.getMessage().contains("valor numérico válido"));
    }

    @Test
    @DisplayName("Debe rechazar si la fecha no tiene formato AAAA-MM-DD")
    void testValidarRespuestasFechaInvalida() {
        when(campoRepository.findByVersionIdWithOpciones(100L)).thenReturn(campos);

        Map<String, String> respuestas = new HashMap<>();
        respuestas.put("direccion_predio", "Calle Real 456");
        respuestas.put("area_m2", "200");
        respuestas.put("fecha_posesion", "18/05/2020"); // Formato incorrecto

        BusinessException ex = assertThrows(BusinessException.class,
                () -> formularioService.validarRespuestas(version, respuestas, false));
        assertTrue(ex.getMessage().contains("AAAA-MM-DD"));
    }
}
