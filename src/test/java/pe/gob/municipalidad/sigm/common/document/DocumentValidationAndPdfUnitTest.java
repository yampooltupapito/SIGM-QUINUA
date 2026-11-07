package pe.gob.municipalidad.sigm.common.document;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import pe.gob.municipalidad.sigm.common.document.dto.DocumentDtos.GenerarPdfRequest;
import pe.gob.municipalidad.sigm.common.document.pdf.PdfGeneratorService;
import pe.gob.municipalidad.sigm.common.document.pdf.QrCodeGenerator;
import pe.gob.municipalidad.sigm.common.document.validation.DocumentFileValidator;
import pe.gob.municipalidad.sigm.common.document.validation.Sha256Calculator;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentValidationAndPdfUnitTest {

    private DocumentFileValidator fileValidator;
    private PdfGeneratorService pdfGeneratorService;

    @BeforeEach
    void setUp() {
        fileValidator = new DocumentFileValidator(26214400L, "pdf,jpeg,jpg,png,webp,docx,xlsx");
        QrCodeGenerator qrCodeGenerator = new QrCodeGenerator();
        pdfGeneratorService = new PdfGeneratorService(qrCodeGenerator);
    }

    @Test
    @DisplayName("Debe validar exitosamente un archivo PDF legítimo con Magic Bytes")
    void debeValidarPdfLegitimo() {
        byte[] pdfContent = "%PDF-1.7 Contenido simulado de prueba municipal".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("archivo", "oficio_alcaldia.pdf", "application/pdf", pdfContent);

        fileValidator.validate(file);

        String hash = Sha256Calculator.calculate(pdfContent);
        assertThat(hash).isNotNull().hasSize(64);
    }

    @Test
    @DisplayName("Debe rechazar archivos vacíos")
    void debeRechazarArchivoVacio() {
        MockMultipartFile file = new MockMultipartFile("archivo", "vacio.pdf", "application/pdf", new byte[0]);

        assertThatThrownBy(() -> fileValidator.validate(file))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("vacío");
    }

    @Test
    @DisplayName("Debe rechazar extensiones ejecutables peligrosas como .exe o .bat")
    void debeRechazarExtensionesPeligrosas() {
        MockMultipartFile fileExe = new MockMultipartFile("archivo", "malware.exe", "application/octet-stream", "dummy binary".getBytes());

        assertThatThrownBy(() -> fileValidator.validate(fileExe))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("bloqueada");
    }

    @Test
    @DisplayName("Debe detectar y rechazar secuencias de Path Traversal en el nombre original")
    void debeRechazarPathTraversalEnNombre() {
        MockMultipartFile file = new MockMultipartFile("archivo", "../../../etc/passwd.pdf", "application/pdf", "%PDF-data".getBytes());

        assertThatThrownBy(() -> fileValidator.validate(file))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("caracteres o secuencias no permitidas");
    }

    @Test
    @DisplayName("Debe rechazar archivos con extensión PDF que no contengan los Magic Bytes %PDF-")
    void debeRechazarMagicBytesInvalidosParaPdf() {
        byte[] fakePdf = "ESTO NO ES UN PDF REAL SINO TEXTO".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("archivo", "documento_falso.pdf", "application/pdf", fakePdf);

        assertThatThrownBy(() -> fileValidator.validate(file))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no corresponde a un documento PDF legítimo");
    }

    @Test
    @DisplayName("Debe generar un documento PDF institucional con OpenPDF y código QR de verificación")
    void debeGenerarPdfInstitucionalConQr() {
        GenerarPdfRequest request = GenerarPdfRequest.builder()
                .tipoPlantilla("CONSTANCIA")
                .codigoDocumental("CONST-2026-0099")
                .titulo("Constancia de Trámite Municipal")
                .destinatario("Rosa Huamán Quispe")
                .asunto("Certificación de empadronamiento")
                .cuerpo("La Municipalidad Distrital de Quinua hace constar que la administrada se encuentra registrada.")
                .metadatosExtra(Map.of("Distrito", "Quinua", "Provincia", "Huamanga"))
                .build();

        byte[] pdfBytes = pdfGeneratorService.generarDocumentoPdf(request, "https://muniquinua.gob.pe/verificar/CONST-2026-0099");

        assertThat(pdfBytes).isNotNull().isNotEmpty();
        // Encabezado estándar de PDF
        String header = new String(pdfBytes, 0, 8, StandardCharsets.ISO_8859_1);
        assertThat(header).startsWith("%PDF-");
    }
}
