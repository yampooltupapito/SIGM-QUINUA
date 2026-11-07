package pe.gob.municipalidad.sigm.modules.mercados.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.administracion.service.AuditoriaAdministrativaService;
import pe.gob.municipalidad.sigm.common.document.pdf.QrCodeGenerator;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.mercados.entity.AsignacionPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.repository.AsignacionPuestoRepository;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
public class ConstanciaAsignacionService {

    private final AsignacionPuestoRepository asignacionRepository;
    private final QrCodeGenerator qrCodeGenerator;
    private final AuditoriaAdministrativaService auditoriaService;

    public ConstanciaAsignacionService(
            AsignacionPuestoRepository asignacionRepository,
            QrCodeGenerator qrCodeGenerator,
            AuditoriaAdministrativaService auditoriaService) {
        this.asignacionRepository = asignacionRepository;
        this.qrCodeGenerator = qrCodeGenerator;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public byte[] generarConstanciaPdf(UUID asignacionId) {
        AsignacionPuesto asignacion = asignacionRepository.findByIdCompleto(asignacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada con ID: " + asignacionId));

        Document document = new Document(PageSize.A4, 40, 40, 50, 50);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Tipografías
            Font fontHeaderMuni = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);
            Font fontSubMuni = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, new Color(29, 78, 216)); // Azul municipal
            Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.DARK_GRAY);
            Font fontLabel = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.DARK_GRAY);
            Font fontValue = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
            Font fontDisclaimer = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, new Color(100, 116, 139));

            // Encabezado Institucional
            Paragraph pHeader = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA", fontHeaderMuni);
            pHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(pHeader);

            Paragraph pSubHeader = new Paragraph("Provincia de Huamanga — Departamento de Ayacucho\nGerencia de Servicios Municipales y Desarrollo Económico\nSubgerencia de Comercialización y Mercados", fontSubMuni);
            pSubHeader.setAlignment(Element.ALIGN_CENTER);
            pSubHeader.setSpacingAfter(15);
            document.add(pSubHeader);

            // Línea separadora
            Paragraph separator = new Paragraph("_____________________________________________________________________________\n", fontSubMuni);
            separator.setAlignment(Element.ALIGN_CENTER);
            separator.setSpacingAfter(15);
            document.add(separator);

            // Título de la Constancia
            Paragraph pTitulo = new Paragraph("CONSTANCIA ADMINISTRATIVA DE ASIGNACIÓN DE PUESTO", fontTitulo);
            pTitulo.setAlignment(Element.ALIGN_CENTER);
            document.add(pTitulo);

            String codigo = asignacion.getCodigoAsignacion() != null ? asignacion.getCodigoAsignacion() : "S/C";
            Paragraph pCodigo = new Paragraph("N° DE REGISTRO: " + codigo, fontSubtitulo);
            pCodigo.setAlignment(Element.ALIGN_CENTER);
            pCodigo.setSpacingAfter(20);
            document.add(pCodigo);

            // Datos del Comerciante y Titular
            String titular = "-";
            String docTipo = "DOC";
            String docNum = "-";
            if (asignacion.getComerciante() != null) {
                if (asignacion.getComerciante().getPersona() != null) {
                    titular = asignacion.getComerciante().getPersona().getNombreCompleto();
                    docTipo = asignacion.getComerciante().getPersona().getTipoDocumento() != null ?
                            asignacion.getComerciante().getPersona().getTipoDocumento() : "DNI";
                    docNum = asignacion.getComerciante().getPersona().getNumeroDocumento();
                } else if (asignacion.getComerciante().getPersonaJuridica() != null) {
                    titular = asignacion.getComerciante().getPersonaJuridica().getRazonSocial();
                    docTipo = "RUC";
                    docNum = asignacion.getComerciante().getPersonaJuridica().getRuc();
                }
            }

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{35f, 65f});
            table.setSpacingBefore(10);
            table.setSpacingAfter(15);

            agregarFila(table, "CÓDIGO DE ASIGNACIÓN:", codigo, fontLabel, fontValue);
            agregarFila(table, "ESTADO ADMINISTRATIVO:", asignacion.getEstado().getDescripcion().toUpperCase(), fontLabel, fontValue);
            agregarFila(table, "TIPO DE ASIGNACIÓN:", asignacion.getTipoAsignacion().name(), fontLabel, fontValue);
            agregarFila(table, "COMERCIANTE TITULAR:", titular, fontLabel, fontValue);
            agregarFila(table, "DOCUMENTO (" + docTipo + "):", docNum, fontLabel, fontValue);
            agregarFila(table, "MERCADO MUNICIPAL:", asignacion.getPuesto() != null && asignacion.getPuesto().getMercado() != null ?
                    asignacion.getPuesto().getMercado().getNombre() : "-", fontLabel, fontValue);
            agregarFila(table, "SECTOR / PABELLÓN:", (asignacion.getPuesto() != null && asignacion.getPuesto().getSector() != null ? asignacion.getPuesto().getSector().getNombre() : "-") +
                    " / " + (asignacion.getPuesto() != null && asignacion.getPuesto().getPabellon() != null ? asignacion.getPuesto().getPabellon().getNombre() : "-"), fontLabel, fontValue);
            agregarFila(table, "PUESTO COMERCIAL:", asignacion.getPuesto() != null ?
                    (asignacion.getPuesto().getCodigo() + " (N° " + asignacion.getPuesto().getNumero() + ")") : "-", fontLabel, fontValue);
            agregarFila(table, "ÁREA APROXIMADA:", asignacion.getPuesto() != null && asignacion.getPuesto().getAreaMetrosCuadrados() != null ?
                    (asignacion.getPuesto().getAreaMetrosCuadrados() + " m²") : "-", fontLabel, fontValue);
            agregarFila(table, "FECHA DE INICIO:", asignacion.getFechaInicio() != null ?
                    asignacion.getFechaInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-", fontLabel, fontValue);
            agregarFila(table, "FECHA DE CULMINACIÓN:", asignacion.getFechaFin() != null ?
                    asignacion.getFechaFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Indefinida / Sujeta a normatividad", fontLabel, fontValue);

            if (asignacion.getMotivo() != null) {
                agregarFila(table, "SUSTENTO / RESOLUCIÓN:", asignacion.getMotivo(), fontLabel, fontValue);
            }

            document.add(table);

            // Código QR institucional de verificación
            String urlVerificacion = "https://sigm.quinua.gob.pe/verificar/mercados/asignacion/" + asignacion.getId();
            byte[] qrBytes = qrCodeGenerator.generateQrCodePng(urlVerificacion, 120, 120);
            Image qrImage = Image.getInstance(qrBytes);
            qrImage.setAlignment(Element.ALIGN_CENTER);
            qrImage.setSpacingBefore(10);
            qrImage.setSpacingAfter(10);
            document.add(qrImage);

            Paragraph pQrTexto = new Paragraph("Código QR oficial de verificación de autenticidad documental", fontDisclaimer);
            pQrTexto.setAlignment(Element.ALIGN_CENTER);
            pQrTexto.setSpacingAfter(20);
            document.add(pQrTexto);

            // Cláusula legal / Disclaimer institucional
            Paragraph pDisclaimer = new Paragraph(
                    "NOTA LEGAL: La presente constancia acredita la inscripción administrativa en el Padrón Municipal de Mercados " +
                    "y el control del uso de los espacios públicos comerciales de la Municipalidad Distrital de Quinua. " +
                    "No constituye título traslativo de dominio ni sustituye las licencias o inspecciones sanitarias reglamentarias. " +
                    "Toda alteración invalida el presente documento.", fontDisclaimer);
            pDisclaimer.setAlignment(Element.ALIGN_JUSTIFIED);
            document.add(pDisclaimer);

            document.close();

            auditoriaService.registrar("GENERAR_CONSTANCIA_ASIGNACION_PDF", "mercados_asignaciones", asignacion.getId(), "EXITO",
                    "Constancia oficial emitida para la asignación " + codigo);

            return out.toByteArray();

        } catch (Exception e) {
            log.error("Error al generar constancia PDF para la asignación {}", asignacionId, e);
            throw new RuntimeException("Error al generar constancia administrativa en PDF", e);
        }
    }

    private void agregarFila(PdfPTable table, String label, String value, Font fLabel, Font fValue) {
        PdfPCell cLabel = new PdfPCell(new Phrase(label, fLabel));
        cLabel.setBackgroundColor(new Color(241, 245, 249)); // #f1f5f9
        cLabel.setPadding(6);
        cLabel.setBorderColor(new Color(203, 213, 225));

        PdfPCell cValue = new PdfPCell(new Phrase(value != null ? value : "-", fValue));
        cValue.setPadding(6);
        cValue.setBorderColor(new Color(203, 213, 225));

        table.addCell(cLabel);
        table.addCell(cValue);
    }
}
