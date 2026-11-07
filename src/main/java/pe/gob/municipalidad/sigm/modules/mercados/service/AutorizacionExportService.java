package pe.gob.municipalidad.sigm.modules.mercados.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.mercados.entity.AutorizacionComercial;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoAutorizacion;
import pe.gob.municipalidad.sigm.modules.mercados.repository.AutorizacionComercialRepository;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class AutorizacionExportService {

    private final AutorizacionComercialRepository autorizacionRepository;

    public AutorizacionExportService(AutorizacionComercialRepository autorizacionRepository) {
        this.autorizacionRepository = autorizacionRepository;
    }

    @Transactional(readOnly = true)
    public byte[] exportarExcel(String termino, UUID mercadoId, EstadoAutorizacion estado, String tipoAutorizacion, LocalDate fechaDesde, LocalDate fechaHasta) {
        Pageable pageable = PageRequest.of(0, 5000);
        Page<AutorizacionComercial> page;
        if (termino != null && !termino.trim().isEmpty()) {
            page = autorizacionRepository.buscarConFiltrosConTermino(termino.trim(), mercadoId, estado, tipoAutorizacion, fechaDesde, fechaHasta, pageable);
        } else {
            page = autorizacionRepository.buscarConFiltrosSinTermino(mercadoId, estado, tipoAutorizacion, fechaDesde, fechaHasta, pageable);
        }

        List<AutorizacionComercial> lista = page.getContent();
        StringBuilder csv = new StringBuilder();
        csv.append('\ufeff');

        csv.append("Código Autorización;Mercado;Puesto;Titular;N° Doc;Tipo;Estado;Fecha Solicitud;Fecha Emisión;Fecha Inicio;Fecha Fin;Código Seguridad\n");

        for (AutorizacionComercial a : lista) {
            csv.append(sanitizar(a.getCodigoAutorizacion())).append(";");
            csv.append(a.getPuesto() != null && a.getPuesto().getMercado() != null ? sanitizar(a.getPuesto().getMercado().getNombre()) : "-").append(";");
            csv.append(a.getPuesto() != null ? sanitizar(a.getPuesto().getCodigo()) : "-").append(";");
            csv.append(a.getComerciante() != null ? sanitizar(a.getComerciante().getNombreOTitular()) : "-").append(";");
            csv.append(a.getComerciante() != null ? sanitizar(a.getComerciante().getNumeroDocumentoTitular()) : "-").append(";");
            csv.append(a.getTipoAutorizacionEntity() != null ? sanitizar(a.getTipoAutorizacionEntity().getNombre()) : sanitizar(a.getTipoAutorizacion())).append(";");
            csv.append(a.getEstado().name()).append(";");
            csv.append(a.getFechaSolicitud() != null ? a.getFechaSolicitud().toString() : "-").append(";");
            csv.append(a.getFechaEmision() != null ? a.getFechaEmision().toString() : "-").append(";");
            csv.append(a.getFechaInicio() != null ? a.getFechaInicio().toString() : "-").append(";");
            csv.append(a.getFechaFin() != null ? a.getFechaFin().toString() : "-").append(";");
            csv.append(a.getCodigoSeguro() != null ? sanitizar(a.getCodigoSeguro()) : "-").append("\n");
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional(readOnly = true)
    public byte[] exportarPdf(String termino, UUID mercadoId, EstadoAutorizacion estado, String tipoAutorizacion, LocalDate fechaDesde, LocalDate fechaHasta) {
        Pageable pageable = PageRequest.of(0, 1000);
        Page<AutorizacionComercial> page;
        if (termino != null && !termino.trim().isEmpty()) {
            page = autorizacionRepository.buscarConFiltrosConTermino(termino.trim(), mercadoId, estado, tipoAutorizacion, fechaDesde, fechaHasta, pageable);
        } else {
            page = autorizacionRepository.buscarConFiltrosSinTermino(mercadoId, estado, tipoAutorizacion, fechaDesde, fechaHasta, pageable);
        }

        List<AutorizacionComercial> lista = page.getContent();
        Document document = new Document(PageSize.A4.rotate(), 20, 20, 30, 30);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(29, 78, 216));
            Font fontSub = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.DARK_GRAY);
            Font fontTh = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.5f, Color.WHITE);
            Font fontTd = FontFactory.getFont(FontFactory.HELVETICA, 7.5f, Color.BLACK);

            Paragraph pHeader = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA — REPORTE DE AUTORIZACIONES COMERCIALES", fontTitulo);
            pHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(pHeader);

            Paragraph pFecha = new Paragraph("Generado el: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + " | Registros: " + lista.size(), fontSub);
            pFecha.setAlignment(Element.ALIGN_CENTER);
            pFecha.setSpacingAfter(15);
            document.add(pFecha);

            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{14f, 16f, 10f, 22f, 10f, 14f, 10f, 10f});

            String[] headers = {"CÓDIGO", "MERCADO", "PUESTO", "TITULAR", "DOC", "MODALIDAD", "EMISIÓN", "ESTADO"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fontTh));
                cell.setBackgroundColor(new Color(30, 41, 59));
                cell.setPadding(4f);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            for (AutorizacionComercial a : lista) {
                table.addCell(crearCelda(a.getCodigoAutorizacion(), fontTd));
                table.addCell(crearCelda(a.getPuesto() != null && a.getPuesto().getMercado() != null ? a.getPuesto().getMercado().getNombre() : "-", fontTd));
                table.addCell(crearCelda(a.getPuesto() != null ? a.getPuesto().getCodigo() : "-", fontTd));
                table.addCell(crearCelda(a.getComerciante() != null ? a.getComerciante().getNombreOTitular() : "-", fontTd));
                table.addCell(crearCelda(a.getComerciante() != null ? a.getComerciante().getNumeroDocumentoTitular() : "-", fontTd));
                table.addCell(crearCelda(a.getTipoAutorizacionEntity() != null ? a.getTipoAutorizacionEntity().getNombre() : a.getTipoAutorizacion(), fontTd));
                table.addCell(crearCelda(a.getFechaEmision() != null ? a.getFechaEmision().toString() : "-", fontTd));
                table.addCell(crearCelda(a.getEstado().name(), fontTd));
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    private PdfPCell crearCelda(String texto, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(texto != null ? texto : "-", font));
        c.setPadding(3.5f);
        c.setBorderColor(new Color(226, 232, 240));
        return c;
    }

    private String sanitizar(String valor) {
        if (valor == null) return "-";
        String limpio = valor.replace(";", ",");
        if (limpio.startsWith("=") || limpio.startsWith("+") || limpio.startsWith("-") || limpio.startsWith("@")) {
            limpio = "'" + limpio;
        }
        return limpio;
    }
}
