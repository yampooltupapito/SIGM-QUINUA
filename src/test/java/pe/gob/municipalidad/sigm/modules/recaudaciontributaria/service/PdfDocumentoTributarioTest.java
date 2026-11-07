package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.gob.municipalidad.sigm.common.document.pdf.QrCodeGenerator;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.AplicacionPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ConceptoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ObligacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.PeriodoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ContribuyenteTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.DocumentoPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.PagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.SerieDocumentoPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.TipoDocumentoPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoDocumentoPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.MedioPagoTributario;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PdfDocumentoTributarioTest {

    private PdfDocumentoTributarioService pdfService;
    private DocumentoPagoTributario doc;
    private List<AplicacionPagoTributario> aplicaciones;

    @BeforeEach
    void setUp() {
        QrCodeGenerator qrGenerator = new QrCodeGenerator();
        pdfService = new PdfDocumentoTributarioService(qrGenerator);

        TipoDocumentoPagoTributario tipo = TipoDocumentoPagoTributario.builder()
                .codigo("RECIBO_PAGO")
                .nombre("Recibo de Caja Municipal")
                .build();

        SerieDocumentoPagoTributario serie = SerieDocumentoPagoTributario.builder()
                .codigoSerie("REC01")
                .build();

        ContribuyenteTributario contribuyente = ContribuyenteTributario.builder()
                .codigoContribuyente("CT-00001")
                .nombreRazonSocial("Juan Pérez Quispe")
                .tipoDocumento("DNI")
                .numeroDocumento("44556677")
                .build();

        PagoTributario pago = PagoTributario.builder()
                .id(UUID.randomUUID())
                .codigoPago("PAG-2026-00001")
                .medioPago(MedioPagoTributario.EFECTIVO_CAJA)
                .importe(new BigDecimal("150.00"))
                .contribuyente(contribuyente)
                .build();

        doc = DocumentoPagoTributario.builder()
                .id(UUID.randomUUID())
                .codigoDocumento("REC01-00000001")
                .tipoDocumento(tipo)
                .serie(serie)
                .numeroCorrelativo(1L)
                .pagoTributario(pago)
                .contribuyente(contribuyente)
                .fechaEmision(OffsetDateTime.now())
                .importeTotal(new BigDecimal("150.00"))
                .moneda("PEN")
                .estado(EstadoDocumentoPagoTributario.EMITIDO)
                .codigoVerificacion("VER-ABC123XYZ456")
                .hashDocumento("a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2")
                .urlVerificacion("/verificar/documento-tributario/VER-ABC123XYZ456")
                .build();

        ConceptoTributario concepto = ConceptoTributario.builder()
                .nombre("Impuesto Predial Anual")
                .build();

        PeriodoTributario periodo = PeriodoTributario.builder()
                .anio(2026)
                .build();

        ObligacionTributaria obl = ObligacionTributaria.builder()
                .concepto(concepto)
                .periodo(periodo)
                .cuotaNumero(1)
                .build();

        AplicacionPagoTributario apl = AplicacionPagoTributario.builder()
                .obligacion(obl)
                .importeAplicado(new BigDecimal("150.00"))
                .build();

        aplicaciones = List.of(apl);
    }

    @Test
    @DisplayName("Genera PDF válido y no vacío con OpenPDF y QR embebido")
    void testGenerarPdfExitoso() {
        byte[] pdfBytes = pdfService.generarPdfDocumento(
                doc, aplicaciones, "https://muniquinua.gob.pe/verificar/documento-tributario/" + doc.getCodigoVerificacion());

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 500, "El PDF debe tener un tamaño razonable");

        // Cabecera PDF típica "%PDF-"
        assertEquals('%', (char) pdfBytes[0]);
        assertEquals('P', (char) pdfBytes[1]);
        assertEquals('D', (char) pdfBytes[2]);
        assertEquals('F', (char) pdfBytes[3]);
    }

    @Test
    @DisplayName("Calcula hash SHA-256 de 64 caracteres en formato hexadecimal")
    void testCalculoSha256Hex() {
        byte[] sample = "Prueba de Integridad Documental Quinua".getBytes();
        String hash = pdfService.calcularSha256(sample);

        assertNotNull(hash);
        assertEquals(64, hash.length());
        assertTrue(hash.matches("^[a-f0-9]{64}$"));
    }

    @Test
    @DisplayName("Genera PDF con marcas de copia si tiene reimpresiones")
    void testGenerarPdfConReimpresion() {
        doc.setNumeroReimpresiones(2);

        byte[] pdfBytes = pdfService.generarPdfDocumento(
                doc, aplicaciones, "https://muniquinua.gob.pe/verificar/documento-tributario/" + doc.getCodigoVerificacion());

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 500);
    }
}
