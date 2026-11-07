package pe.gob.municipalidad.sigm.modules.serenazgo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.documentos.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.DocumentoSeguridadCiudadanaService;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DocumentoSeguridadCiudadanaServiceTest {

    @Autowired
    private DocumentoSeguridadCiudadanaService documentoService;

    @Autowired
    private DocumentoSeguridadCiudadanaRepository documentoRepository;

    @Autowired
    private IncidenciaSeguridadCiudadanaRepository incidenciaRepository;

    @Autowired
    private TipoIncidenciaSeguridadRepository tipoIncidenciaRepository;

    @Autowired
    private SectorPatrullajeRepository sectorRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PersonaRepository personaRepository;

    private CustomUserDetails userAdmin;
    private SectorPatrullaje sectorTest;
    private IncidenciaSeguridadCiudadana incidenciaTest;

    @BeforeEach
    void setUp() {
        long ts = System.currentTimeMillis();

        Persona persona = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("48" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Carlos")
                .apellidoPaterno("Mendoza")
                .apellidoMaterno("Vargas")
                .direccion("Plaza de Armas Quinua")
                .estado("ACTIVO")
                .build());

        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .username("sereno_doc_test_" + ts)
                .passwordHash("$2a$10$abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
                .persona(persona)
                .enabled(true)
                .accountLocked(false)
                .build());

        userAdmin = CustomUserDetails.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .password("dummy")
                .enabled(true)
                .accountNonLocked(true)
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("SERENAZGO_DOCUMENTOS_VER"),
                        new SimpleGrantedAuthority("SERENAZGO_DOCUMENTOS_CREAR"),
                        new SimpleGrantedAuthority("SERENAZGO_DOCUMENTOS_DESCARGAR"),
                        new SimpleGrantedAuthority("SERENAZGO_DOCUMENTOS_VERIFICAR"),
                        new SimpleGrantedAuthority("SERENAZGO_DOCUMENTOS_VALIDAR"),
                        new SimpleGrantedAuthority("SERENAZGO_DOCUMENTOS_RESTRINGIR"),
                        new SimpleGrantedAuthority("SERENAZGO_DOCUMENTOS_ARCHIVAR"),
                        new SimpleGrantedAuthority("SERENAZGO_DOCUMENTOS_VERSIONAR"),
                        new SimpleGrantedAuthority("SERENAZGO_DOCUMENTOS_EXPORTAR")
                ))
                .build();

        sectorTest = sectorRepository.findByCodigo("SEC-01").orElseGet(() ->
                sectorRepository.save(SectorPatrullaje.builder()
                        .codigo("SEC-DOC-01")
                        .nombre("Sector Centro Histórico")
                        .estado(EstadoSectorPatrullaje.ACTIVO)
                        .descripcion("Sector para pruebas documentales")
                        .build())
        );

        TipoIncidenciaSeguridad tipoInc = tipoIncidenciaRepository.findAll().stream().findFirst()
                .orElseGet(() -> tipoIncidenciaRepository.save(TipoIncidenciaSeguridad.builder()
                        .codigo("TIP-DOC-" + ts)
                        .nombre("Incidencia de Prueba")
                        .categoria(CategoriaIncidenciaSeguridad.ORDEN_PUBLICO)
                        .prioridadSugerida(PrioridadIncidenciaSeguridad.MEDIA)
                        .activo(true)
                        .build()));

        incidenciaTest = incidenciaRepository.save(IncidenciaSeguridadCiudadana.builder()
                .codigo("INC-DOC-" + ts)
                .titulo("Incidencia con evidencia fotográfica")
                .descripcion("Hecho verificado en campo")
                .ubicacionReferencia("Jr. Sucre cuadra 2")
                .fechaReporte(OffsetDateTime.now())
                .sector(sectorTest)
                .tipoIncidencia(tipoInc)
                .estado(EstadoIncidenciaSeguridad.REGISTRADA)
                .prioridad(PrioridadIncidenciaSeguridad.ALTA)
                .canalReporte(CanalReporteIncidencia.LLAMADA_OPERADOR)
                .latitud(new BigDecimal("-13.0450000"))
                .longitud(new BigDecimal("-74.1380000"))
                .usuarioRegistro(usuario)
                .build());
    }

    @Test
    @Order(1)
    @DisplayName("1. Registrar evidencia fotográfica y calcular huella SHA-256 exitosamente")
    void testRegistrarDocumentoConHashSha256() {
        byte[] contenido = "CONTENIDO_FOTOGRAFIA_QUINUA_2026".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile(
                "archivo",
                "foto_intervencion.jpg",
                "image/jpeg",
                contenido
        );

        DocumentoCargaRequest req = DocumentoCargaRequest.builder()
                .archivo(file)
                .tipoDocumento(TipoDocumentoSeguridad.FOTOGRAFIA)
                .descripcion("Fotografía capturada durante patrullaje preventivo")
                .nivelConfidencialidad(NivelConfidencialidad.CONFIDENCIAL)
                .origenDocumento(OrigenDocumentoSeguridad.ADMINISTRATIVO)
                .incidenciaId(incidenciaTest.getId())
                .fechaCaptura(LocalDate.now())
                .ubicacionCaptura("Plaza de Armas Quinua")
                .latitud(new BigDecimal("-13.0451000"))
                .longitud(new BigDecimal("-74.1382000"))
                .autoriaDeclarada("Sereno 04 - Patrulla Móvil 01")
                .build();

        DocumentoSeguridadResponse resp = documentoService.registrarDocumento(req, userAdmin, "192.168.1.100");

        assertThat(resp).isNotNull();
        assertThat(resp.getId()).isNotNull();
        assertThat(resp.getCodigo()).startsWith("DOC-SEG-");
        assertThat(resp.getHashSha256()).isNotEmpty();
        assertThat(resp.getHashSha256().length()).isEqualTo(64); // SHA-256 hex string
        assertThat(resp.getTipoDocumento()).isEqualTo(TipoDocumentoSeguridad.FOTOGRAFIA);
        assertThat(resp.getEstado()).isEqualTo(EstadoDocumentoSeguridad.REGISTRADO);
        assertThat(resp.getUltimoResultadoVerificacion()).isEqualTo(ResultadoVerificacionIntegridad.COINCIDENTE);
        assertThat(resp.getRegistroAsociadoCodigo()).isEqualTo(incidenciaTest.getCodigo());
        assertThat(resp.isEsImagen()).isTrue();
    }

    @Test
    @Order(2)
    @DisplayName("2. Rechazar archivo vacío o con extensión no autorizada")
    void testRechazarArchivoInvalido() {
        MockMultipartFile archivoVacio = new MockMultipartFile("archivo", "test.jpg", "image/jpeg", new byte[0]);
        DocumentoCargaRequest reqVacio = DocumentoCargaRequest.builder()
                .archivo(archivoVacio)
                .tipoDocumento(TipoDocumentoSeguridad.FOTOGRAFIA)
                .build();

        assertThrows(BusinessException.class, () -> documentoService.registrarDocumento(reqVacio, userAdmin, "127.0.0.1"));

        MockMultipartFile archivoPeligroso = new MockMultipartFile("archivo", "malware.exe", "application/octet-stream", "bad code".getBytes());
        DocumentoCargaRequest reqPeligroso = DocumentoCargaRequest.builder()
                .archivo(archivoPeligroso)
                .tipoDocumento(TipoDocumentoSeguridad.OTRO)
                .build();

        assertThrows(BusinessException.class, () -> documentoService.registrarDocumento(reqPeligroso, userAdmin, "127.0.0.1"));
    }

    @Test
    @Order(3)
    @DisplayName("3. Recalcular y verificar integridad digital SHA-256")
    void testVerificarIntegridadSha256() {
        byte[] contenido = "ARCHIVO_INTEGRIDAD_SEGURA".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("archivo", "acta.pdf", "application/pdf", contenido);

        DocumentoCargaRequest req = DocumentoCargaRequest.builder()
                .archivo(file)
                .tipoDocumento(TipoDocumentoSeguridad.ACTA_ADMINISTRATIVA)
                .descripcion("Acta de intervención municipal")
                .nivelConfidencialidad(NivelConfidencialidad.ORDINARIO)
                .build();

        DocumentoSeguridadResponse creado = documentoService.registrarDocumento(req, userAdmin, "127.0.0.1");

        VerificacionIntegridadResponse verif = documentoService.verificarIntegridad(creado.getId(), userAdmin);

        assertThat(verif).isNotNull();
        assertThat(verif.isCoincide()).isTrue();
        assertThat(verif.getResultado()).isEqualTo(ResultadoVerificacionIntegridad.COINCIDENTE);
        assertThat(verif.getHashRegistrado()).isEqualTo(creado.getHashSha256());
        assertThat(verif.getHashCalculado()).isEqualTo(creado.getHashSha256());
    }

    @Test
    @Order(4)
    @DisplayName("4. Transiciones de estado: Revisión, Observación, Validación Administrativa y Restricción")
    void testTransicionesDeEstado() {
        byte[] contenido = "DOCUMENTO_DE_TRANSICION".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("archivo", "informe.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", contenido);

        DocumentoCargaRequest req = DocumentoCargaRequest.builder()
                .archivo(file)
                .tipoDocumento(TipoDocumentoSeguridad.INFORME)
                .descripcion("Informe de novedades")
                .build();

        DocumentoSeguridadResponse doc = documentoService.registrarDocumento(req, userAdmin, "127.0.0.1");

        // 1. En revisión
        TransicionEstadoRequest t1 = TransicionEstadoRequest.builder().motivo("Revisión de supervisión").build();
        DocumentoSeguridadResponse r1 = documentoService.revisar(doc.getId(), t1, userAdmin);
        assertThat(r1.getEstado()).isEqualTo(EstadoDocumentoSeguridad.EN_REVISION);

        // 2. Observar
        TransicionEstadoRequest t2 = TransicionEstadoRequest.builder().motivo("Faltan firmas operativas").build();
        DocumentoSeguridadResponse r2 = documentoService.observar(doc.getId(), t2, userAdmin);
        assertThat(r2.getEstado()).isEqualTo(EstadoDocumentoSeguridad.OBSERVADO);

        // 3. Validar administrativamente
        TransicionEstadoRequest t3 = TransicionEstadoRequest.builder().observaciones("Conforme con Ley 31297").build();
        DocumentoSeguridadResponse r3 = documentoService.validarAdministrativamente(doc.getId(), t3, userAdmin);
        assertThat(r3.getEstado()).isEqualTo(EstadoDocumentoSeguridad.VALIDADO_ADMINISTRATIVAMENTE);

        // 4. Restringir
        TransicionEstadoRequest t4 = TransicionEstadoRequest.builder().motivo("Protección de datos personales").nuevoNivel(NivelConfidencialidad.RESTRINGIDO).build();
        DocumentoSeguridadResponse r4 = documentoService.restringir(doc.getId(), t4, userAdmin);
        assertThat(r4.getEstado()).isEqualTo(EstadoDocumentoSeguridad.RESTRINGIDO);
        assertThat(r4.getNivelConfidencialidad()).isEqualTo(NivelConfidencialidad.RESTRINGIDO);
    }

    @Test
    @Order(5)
    @DisplayName("5. Versionado inmutable: Crear nueva versión incrementa correlativo y enlaza padre")
    void testCrearNuevaVersion() {
        byte[] v1 = "CONTENIDO_VERSION_1".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file1 = new MockMultipartFile("archivo", "informe_v1.pdf", "application/pdf", v1);

        DocumentoCargaRequest req = DocumentoCargaRequest.builder()
                .archivo(file1)
                .tipoDocumento(TipoDocumentoSeguridad.INFORME)
                .descripcion("Informe inicial")
                .build();

        DocumentoSeguridadResponse docV1 = documentoService.registrarDocumento(req, userAdmin, "127.0.0.1");
        assertThat(docV1.getVersionActual()).isEqualTo(1);

        byte[] v2 = "CONTENIDO_VERSION_2_AMPLIADO".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file2 = new MockMultipartFile("nuevoArchivo", "informe_v2.pdf", "application/pdf", v2);
        NuevaVersionRequest nvr = NuevaVersionRequest.builder()
                .nuevoArchivo(file2)
                .motivoVersion("Ampliación de hechos verificados")
                .build();

        DocumentoSeguridadResponse docV2 = documentoService.crearNuevaVersion(docV1.getId(), nvr, userAdmin, "127.0.0.1");

        assertThat(docV2.getVersionActual()).isEqualTo(2);
        assertThat(docV2.getHashSha256()).isNotEqualTo(docV1.getHashSha256());

        DocumentoSeguridadDetalleResponse detPadre = documentoService.obtenerDetalle(docV1.getId(), userAdmin);
        assertThat(detPadre.getVersionActual()).isEqualTo(2);
        assertThat(detPadre.getVersiones()).hasSize(1);
        assertThat(detPadre.getVersiones().get(0).getVersion()).isEqualTo(2);
    }

    @Test
    @Order(6)
    @DisplayName("6. Listar con filtros y verificar paginación")
    void testListarConFiltros() {
        byte[] b = "VIDEO_PREVENTIVO".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile f = new MockMultipartFile("archivo", "patrullaje.mp4", "video/mp4", b);

        documentoService.registrarDocumento(DocumentoCargaRequest.builder()
                .archivo(f)
                .tipoDocumento(TipoDocumentoSeguridad.VIDEO)
                .descripcion("Video de patrullaje")
                .build(), userAdmin, "127.0.0.1");

        DocumentoFiltroRequest filtroVideo = DocumentoFiltroRequest.builder()
                .tipoDocumento(TipoDocumentoSeguridad.VIDEO)
                .build();

        Page<DocumentoSeguridadResponse> pag = documentoService.listar(filtroVideo, PageRequest.of(0, 10));
        assertThat(pag.getContent()).isNotEmpty();
        assertThat(pag.getContent()).allMatch(d -> d.getTipoDocumento() == TipoDocumentoSeguridad.VIDEO);
    }

    @Test
    @Order(7)
    @DisplayName("7. Obtener estadísticas reales del acervo documental")
    void testObtenerEstadisticas() {
        DocumentoEstadisticasResponse stats = documentoService.obtenerEstadisticas();
        assertThat(stats).isNotNull();
        assertThat(stats.getTotalDocumentos()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @Order(8)
    @DisplayName("8. Exportar registros a Excel y Ficha Técnica en PDF")
    void testExportacionesExcelYPdf() {
        byte[] b = "DOCUMENTO_PARA_EXPORTAR".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile f = new MockMultipartFile("archivo", "sustento.pdf", "application/pdf", b);

        DocumentoSeguridadResponse doc = documentoService.registrarDocumento(DocumentoCargaRequest.builder()
                .archivo(f)
                .tipoDocumento(TipoDocumentoSeguridad.DOCUMENTO_RESPALDO)
                .descripcion("Documento de sustento")
                .build(), userAdmin, "127.0.0.1");

        // Excel
        byte[] excelBytes = documentoService.exportarExcel(new DocumentoFiltroRequest());
        assertThat(excelBytes).isNotNull();
        assertThat(excelBytes.length).isGreaterThan(100);

        // PDF Ficha Técnica
        byte[] pdfBytes = documentoService.exportarPdfFicha(doc.getId(), userAdmin);
        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(100);
        assertThat(new String(pdfBytes, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");
    }

    @Test
    @Order(9)
    @DisplayName("9. Descargar y obtener vista previa del archivo físico")
    void testDescargarYVistaPrevia() {
        byte[] contenido = "CONTENIDO_REAL_STREAMING".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile f = new MockMultipartFile("archivo", "comunicado.txt", "text/plain", contenido);

        DocumentoSeguridadResponse doc = documentoService.registrarDocumento(DocumentoCargaRequest.builder()
                .archivo(f)
                .tipoDocumento(TipoDocumentoSeguridad.COMUNICACION_INSTITUCIONAL)
                .descripcion("Comunicado oficial")
                .build(), userAdmin, "127.0.0.1");

        Resource resDescarga = documentoService.descargar(doc.getId(), userAdmin, "127.0.0.1");
        assertThat(resDescarga).isNotNull();

        Resource resPreview = documentoService.obtenerVistaPrevia(doc.getId(), userAdmin);
        assertThat(resPreview).isNotNull();
    }
}
