package pe.gob.municipalidad.sigm.modules.mercados;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import pe.gob.municipalidad.sigm.common.document.pdf.QrCodeGenerator;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.administracion.service.AuditoriaAdministrativaService;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoActaInspeccion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoInspeccion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoInspeccionMercado;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;
import pe.gob.municipalidad.sigm.modules.mercados.service.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MercadoActaYReporteTest {

    @Mock private ActaInspeccionMercadoRepository actaRepository;
    @Mock private InspeccionMercadoRepository inspeccionRepository;
    @Mock private HallazgoInspeccionRepository hallazgoRepository;
    @Mock private MedidaCorrectivaMercadoRepository medidaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private QrCodeGenerator qrCodeGenerator;
    @Mock private AuditoriaAdministrativaService auditoriaService;
    @Mock private InspeccionMercadoService inspeccionService;

    private ActaInspeccionService actaService;
    private SupervisionMercadoReporteService reporteService;

    private InspeccionMercado inspeccion;
    private Usuario inspectorUser;

    @BeforeEach
    void setUp() {
        actaService = new ActaInspeccionService(
                actaRepository, inspeccionRepository, usuarioRepository,
                qrCodeGenerator, auditoriaService);

        reporteService = new SupervisionMercadoReporteService(
                inspeccionRepository, hallazgoRepository, medidaRepository,
                inspeccionService);

        inspectorUser = Usuario.builder()
                .id(5L)
                .username("inspector.lider")
                .build();

        inspeccion = InspeccionMercado.builder()
                .id(UUID.randomUUID())
                .codigoInspeccion("INS-MER-2026-000001")
                .tipoInspeccion(TipoInspeccionMercado.PROGRAMADA)
                .estado(EstadoInspeccion.EN_PROCESO)
                .fechaProgramada(LocalDate.now())
                .motivo("Control general")
                .objetivo("Verificación")
                .mercado(MercadoMunicipal.builder().id(UUID.randomUUID()).nombre("Mercado Central").direccion("Plaza Quinua").build())
                .verificaciones(new ArrayList<>())
                .hallazgos(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("01. Elaborar borrador de acta de inspección")
    void test01_elaborarBorradorActa() {
        when(inspeccionRepository.findById(inspeccion.getId())).thenReturn(Optional.of(inspeccion));
        when(usuarioRepository.findById(inspectorUser.getId())).thenReturn(Optional.of(inspectorUser));
        when(actaRepository.findByInspeccionId(inspeccion.getId())).thenReturn(Optional.empty());
        when(actaRepository.obtenerSiguienteCorrelativo()).thenReturn(1L);

        when(actaRepository.save(any(ActaInspeccionMercado.class))).thenAnswer(i -> {
            ActaInspeccionMercado a = i.getArgument(0);
            a.setId(UUID.randomUUID());
            a.setFechaElaboracion(OffsetDateTime.now());
            return a;
        });

        ActaInspeccionRequest req = ActaInspeccionRequest.builder()
                .resumen("Se constató adecuada limpieza general con observación en extintor")
                .conclusiones("Puesto en condiciones operativas, sujeto a subsanación menor")
                .recomendaciones("Mantener tarjeta de inspección de extintores al día")
                .build();

        ActaInspeccionResponse response = actaService.elaborarBorradorActa(inspeccion.getId(), req, "inspector", inspectorUser.getId());

        assertThat(response).isNotNull();
        assertThat(response.getCodigoActa()).startsWith("ACTA-INS-");
        assertThat(response.getEstado()).isEqualTo(EstadoActaInspeccion.BORRADOR);
        verify(auditoriaService).registrar(eq("ELABORAR_ACTA_INSPECCION"), any(), any(), eq("EXITO"), any());
    }

    @Test
    @DisplayName("02. Aprobar y emitir acta formalmente genera hash SHA-256 y finaliza inspección")
    void test02_aprobarYEmitirActa() {
        ActaInspeccionMercado acta = ActaInspeccionMercado.builder()
                .id(UUID.randomUUID())
                .codigoActa("ACTA-INS-2026-000001")
                .codigoVerificacion("TOKEN1234567890ABCDEF")
                .resumen("Resumen")
                .conclusiones("Conclusiones")
                .estado(EstadoActaInspeccion.BORRADOR)
                .inspeccion(inspeccion)
                .responsable(inspectorUser)
                .build();

        when(actaRepository.findByInspeccionId(inspeccion.getId())).thenReturn(Optional.of(acta));
        when(actaRepository.save(any(ActaInspeccionMercado.class))).thenAnswer(i -> i.getArgument(0));

        ActaInspeccionResponse response = actaService.aprobarYEmitirActa(inspeccion.getId(), "inspector", inspectorUser.getId());

        assertThat(response.getEstado()).isEqualTo(EstadoActaInspeccion.EMITIDA);
        assertThat(response.getHashSha256()).isNotNull();
        assertThat(inspeccion.getEstado()).isEqualTo(EstadoInspeccion.FINALIZADA);
        verify(auditoriaService).registrar(eq("EMITIR_ACTA_INSPECCION"), any(), any(), eq("EXITO"), any());
    }

    @Test
    @DisplayName("03. Generar PDF oficial del acta con OpenPDF y código QR")
    void test03_generarPdfActa() throws Exception {
        ActaInspeccionMercado acta = ActaInspeccionMercado.builder()
                .id(UUID.randomUUID())
                .codigoActa("ACTA-INS-2026-000001")
                .codigoVerificacion("TOKEN_PUBLICO_QR_12345")
                .hashSha256("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
                .resumen("Resumen ejecutivo de la visita")
                .conclusiones("Conclusiones técnicas determinantes")
                .estado(EstadoActaInspeccion.EMITIDA)
                .inspeccion(inspeccion)
                .responsable(inspectorUser)
                .build();

        when(actaRepository.findByInspeccionId(inspeccion.getId())).thenReturn(Optional.of(acta));
        when(qrCodeGenerator.generateQrCodePng(anyString(), anyInt(), anyInt())).thenReturn(new byte[]{1, 2, 3});

        byte[] pdfBytes = actaService.generarPdfActa(inspeccion.getId());

        assertThat(pdfBytes).isNotEmpty();
        // Verificar firma mágica de archivo PDF (%PDF)
        String header = new String(pdfBytes, 0, 4);
        assertThat(header).isEqualTo("%PDF");
    }

    @Test
    @DisplayName("04. Verificación pública tokenizada de acta de inspección")
    void test04_verificacionPublicaActa() {
        String token = "TOKEN_SEGURO_ABC_987";
        ActaInspeccionMercado acta = ActaInspeccionMercado.builder()
                .codigoActa("ACTA-INS-2026-000005")
                .codigoVerificacion(token)
                .hashSha256("hash_valido")
                .estado(EstadoActaInspeccion.EMITIDA)
                .fechaElaboracion(OffsetDateTime.now())
                .inspeccion(inspeccion)
                .build();

        when(actaRepository.findByCodigoVerificacion(token)).thenReturn(Optional.of(acta));

        VerificacionActaPublicaResponse resp = actaService.verificarActaPublica(token);

        assertThat(resp.isValida()).isTrue();
        assertThat(resp.getCodigoActa()).isEqualTo("ACTA-INS-2026-00005".replace("00005", "000005"));
        assertThat(resp.getEntidadEmisora()).contains("Municipalidad Distrital de Quinua");
    }

    @Test
    @DisplayName("05. Calcular estadísticas consolidadas de supervisión de mercados")
    void test05_estadisticasSupervision() {
        when(inspeccionRepository.count()).thenReturn(15L);
        when(inspeccionRepository.countByEstado(EstadoInspeccion.PROGRAMADA)).thenReturn(4L);
        when(inspeccionRepository.countByEstado(EstadoInspeccion.ASIGNADA)).thenReturn(2L);
        when(inspeccionRepository.countByEstado(EstadoInspeccion.FINALIZADA)).thenReturn(8L);
        when(hallazgoRepository.count()).thenReturn(10L);
        when(medidaRepository.count()).thenReturn(6L);

        EstadisticasSupervisionResponse stats = reporteService.obtenerEstadisticas();

        assertThat(stats.getTotalInspecciones()).isEqualTo(15L);
        assertThat(stats.getProgramadas()).isEqualTo(6L);
        assertThat(stats.getFinalizadas()).isEqualTo(8L);
        assertThat(stats.getTotalHallazgos()).isEqualTo(10L);
    }

    @Test
    @DisplayName("06. Exportar reporte de inspecciones a Excel (Apache POI)")
    void test06_exportarExcel() {
        InspeccionResponse r = InspeccionResponse.builder()
                .codigoInspeccion("INS-MER-2026-000001")
                .tipoInspeccion(TipoInspeccionMercado.PROGRAMADA)
                .mercadoNombre("Mercado Central")
                .fechaProgramada(LocalDate.now())
                .estado(EstadoInspeccion.PROGRAMADA)
                .motivo("Supervisión periódica")
                .build();

        when(inspeccionService.buscarPaginado(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(r)));

        byte[] excelBytes = reporteService.exportarInspeccionesExcel(LocalDate.now().minusDays(10), LocalDate.now());

        assertThat(excelBytes).isNotEmpty();
        // PK ZIP header de archivos XLSX (0x50, 0x4B)
        assertThat(excelBytes[0]).isEqualTo((byte) 0x50);
        assertThat(excelBytes[1]).isEqualTo((byte) 0x4B);
    }
}
