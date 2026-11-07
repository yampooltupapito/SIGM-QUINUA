package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.NotificacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.NotificacionTributariaRepository;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReporteNotificacionesService {

    private final NotificacionTributariaRepository notificacionRepository;

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Transactional(readOnly = true)
    public byte[] exportarCsv(Specification<NotificacionTributaria> spec) {
        List<NotificacionTributaria> lista = notificacionRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "fechaCreacion"));

        StringBuilder csv = new StringBuilder();
        // BOM UTF-8 for Excel
        csv.append('\ufeff');
        csv.append("CODIGO;TIPO;CATEGORIA;CONTRIBUYENTE;DESTINATARIO;DOCUMENTO_DEST;DOMICILIO;MEDIO;FECHA_EMISION;ESTADO_ADM;ESTADO_ENTREGA;ESTADO_LEGAL;RESPONSABLE\n");

        for (NotificacionTributaria n : lista) {
            csv.append(sanitizarCsv(n.getCodigoNotificacion())).append(";");
            csv.append(sanitizarCsv(n.getTipoNotificacion().getNombre())).append(";");
            csv.append(sanitizarCsv(n.getTipoNotificacion().getCategoria().getNombre())).append(";");
            csv.append(sanitizarCsv(n.getContribuyente().getCodigoContribuyente())).append(";");
            csv.append(sanitizarCsv(n.getDestinatarioNombre())).append(";");
            csv.append(sanitizarCsv(n.getDestinatarioNumeroDoc())).append(";");
            csv.append(sanitizarCsv(n.getDomicilioNotificacion())).append(";");
            csv.append(sanitizarCsv(n.getMedioNotificacion().getNombre())).append(";");
            csv.append(sanitizarCsv(n.getFechaCreacion().format(FORMATO_FECHA))).append(";");
            csv.append(sanitizarCsv(n.getEstadoAdministrativo().getEtiqueta())).append(";");
            csv.append(sanitizarCsv(n.getEstadoEntrega().getEtiqueta())).append(";");
            csv.append(sanitizarCsv(n.getEstadoValidezLegal().getEtiqueta())).append(";");
            csv.append(sanitizarCsv(n.getUsuarioResponsable() != null ? n.getUsuarioResponsable().getUsername() : "-")).append("\n");
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional(readOnly = true)
    public byte[] exportarPdfConsolidado(Specification<NotificacionTributaria> spec) {
        List<NotificacionTributaria> lista = notificacionRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "fechaCreacion"));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 20, 20, 20, 20);
            PdfWriter.getInstance(document, baos);
            document.open();

            Color azul = new Color(29, 78, 216);
            Color grisBorde = new Color(203, 213, 225);
            Color grisFondo = new Color(241, 245, 249);
            Color texto = new Color(30, 41, 59);

            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, azul);
            Font fontSub = FontFactory.getFont(FontFactory.HELVETICA, 8, texto);
            Font fontTh = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, Color.WHITE);
            Font fontTd = FontFactory.getFont(FontFactory.HELVETICA, 7, texto);

            Paragraph pTitulo = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA - RECAUDACIÓN TRIBUTARIA", fontTitulo);
            pTitulo.setAlignment(Element.ALIGN_CENTER);
            Paragraph pSub = new Paragraph("Reporte Consolidado de Notificaciones y Comunicaciones Tributarias", fontSub);
            pSub.setAlignment(Element.ALIGN_CENTER);

            document.add(pTitulo);
            document.add(pSub);
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{14f, 15f, 18f, 10f, 13f, 10f, 10f, 10f, 10f});

            String[] headers = {"CÓDIGO", "TIPO", "DESTINATARIO", "DOC. ID", "MEDIO", "FECHA", "ESTADO ADM", "ENTREGA", "VALIDEZ LEGAL"};
            for (String h : headers) {
                PdfPCell c = new PdfPCell(new Phrase(h, fontTh));
                c.setBackgroundColor(azul);
                c.setPadding(4);
                table.addCell(c);
            }

            for (NotificacionTributaria n : lista) {
                table.addCell(crearCelda(n.getCodigoNotificacion(), fontTd, grisBorde));
                table.addCell(crearCelda(n.getTipoNotificacion().getNombre(), fontTd, grisBorde));
                table.addCell(crearCelda(n.getDestinatarioNombre(), fontTd, grisBorde));
                table.addCell(crearCelda(n.getDestinatarioNumeroDoc(), fontTd, grisBorde));
                table.addCell(crearCelda(n.getMedioNotificacion().getNombre(), fontTd, grisBorde));
                table.addCell(crearCelda(n.getFechaCreacion().format(FORMATO_FECHA), fontTd, grisBorde));
                table.addCell(crearCelda(n.getEstadoAdministrativo().getEtiqueta(), fontTd, grisBorde));
                table.addCell(crearCelda(n.getEstadoEntrega().getEtiqueta(), fontTd, grisBorde));
                table.addCell(crearCelda(n.getEstadoValidezLegal().getEtiqueta(), fontTd, grisBorde));
            }

            document.add(table);
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error al exportar reporte PDF de notificaciones", e);
            throw new BusinessException("Error al generar el reporte PDF: " + e.getMessage());
        }
    }

    private PdfPCell crearCelda(String txt, Font font, Color borde) {
        PdfPCell c = new PdfPCell(new Phrase(txt != null ? txt : "-", font));
        c.setBorderColor(borde);
        c.setPadding(3);
        return c;
    }

    /**
     * Previene Formula Injection en Excel ante caracteres =, +, -, @.
     */
    private String sanitizarCsv(String valor) {
        if (valor == null) return "";
        String v = valor.replace("\"", "\"\"").replace(";", ",");
        if (v.startsWith("=") || v.startsWith("+") || v.startsWith("-") || v.startsWith("@")) {
            v = "'" + v;
        }
        return "\"" + v + "\"";
    }
}

