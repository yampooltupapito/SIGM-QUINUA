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
import pe.gob.municipalidad.sigm.modules.mercados.entity.ContratoOcupacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoContrato;
import pe.gob.municipalidad.sigm.modules.mercados.repository.ContratoOcupacionRepository;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class ContratoExportService {

    private final ContratoOcupacionRepository contratoRepository;

    public ContratoExportService(ContratoOcupacionRepository contratoRepository) {
        this.contratoRepository = contratoRepository;
    }

    @Transactional(readOnly = true)
    public byte[] exportarExcel(String termino, UUID mercadoId, EstadoContrato estado, String tipoContratoCodigo, LocalDate fechaDesde, LocalDate fechaHasta) {
        String term = (termino != null && !termino.trim().isEmpty()) ? termino.trim() : null;
        String tipo = (tipoContratoCodigo != null && !tipoContratoCodigo.trim().isEmpty()) ? tipoContratoCodigo.trim() : null;

        Pageable pageable = PageRequest.of(0, 5000);
        Page<ContratoOcupacion> page;
        if (term != null) {
            page = contratoRepository.buscarConFiltrosConTermino(term, mercadoId, estado, tipo, fechaDesde, fechaHasta, pageable);
        } else {
            page = contratoRepository.buscarConFiltrosSinTermino(mercadoId, estado, tipo, fechaDesde, fechaHasta, pageable);
        }

        List<ContratoOcupacion> contratos = page.getContent();
        StringBuilder csv = new StringBuilder();
        csv.append('\ufeff'); // UTF-8 BOM

        csv.append("Código Contrato;Asignación;Mercado;Puesto;Titular;N° Doc;Tipo Contrato;Estado;Fecha Elaboración;Fecha Inicio;Fecha Fin;Código Seguridad\n");

        for (ContratoOcupacion c : contratos) {
            csv.append(sanitizar(c.getCodigoContrato())).append(";");
            csv.append(c.getAsignacion() != null ? sanitizar(c.getAsignacion().getCodigoAsignacion()) : "-").append(";");
            csv.append(c.getPuesto() != null && c.getPuesto().getMercado() != null ? sanitizar(c.getPuesto().getMercado().getNombre()) : "-").append(";");
            csv.append(c.getPuesto() != null ? sanitizar(c.getPuesto().getCodigo()) : "-").append(";");
            csv.append(c.getComerciante() != null ? sanitizar(c.getComerciante().getNombreOTitular()) : "-").append(";");
            csv.append(c.getComerciante() != null ? sanitizar(c.getComerciante().getNumeroDocumentoTitular()) : "-").append(";");
            csv.append(c.getTipoContrato() != null ? sanitizar(c.getTipoContrato().getNombre()) : sanitizar(c.getTipoContratoCodigo())).append(";");
            csv.append(c.getEstado().name()).append(";");
            csv.append(c.getFechaElaboracion() != null ? c.getFechaElaboracion().toString() : "-").append(";");
            csv.append(c.getFechaInicio() != null ? c.getFechaInicio().toString() : "-").append(";");
            csv.append(c.getFechaFin() != null ? c.getFechaFin().toString() : "-").append(";");
            csv.append(c.getCodigoSeguro() != null ? sanitizar(c.getCodigoSeguro()) : "-").append("\n");
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional(readOnly = true)
    public byte[] exportarPdf(String termino, UUID mercadoId, EstadoContrato estado, String tipoContratoCodigo, LocalDate fechaDesde, LocalDate fechaHasta) {
        String term = (termino != null && !termino.trim().isEmpty()) ? termino.trim() : null;
        String tipo = (tipoContratoCodigo != null && !tipoContratoCodigo.trim().isEmpty()) ? tipoContratoCodigo.trim() : null;

        Pageable pageable = PageRequest.of(0, 1000);
        Page<ContratoOcupacion> page;
        if (term != null) {
            page = contratoRepository.buscarConFiltrosConTermino(term, mercadoId, estado, tipo, fechaDesde, fechaHasta, pageable);
        } else {
            page = contratoRepository.buscarConFiltrosSinTermino(mercadoId, estado, tipo, fechaDesde, fechaHasta, pageable);
        }

        List<ContratoOcupacion> contratos = page.getContent();
        Document document = new Document(PageSize.A4.rotate(), 20, 20, 30, 30);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(29, 78, 216));
            Font fontSub = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.DARK_GRAY);
            Font fontTh = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.5f, Color.WHITE);
            Font fontTd = FontFactory.getFont(FontFactory.HELVETICA, 7.5f, Color.BLACK);

            Paragraph pHeader = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA — REPORTE OFICIAL DE CONTRATOS DE OCUPACIÓN", fontTitulo);
            pHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(pHeader);

            Paragraph pFecha = new Paragraph("Generado el: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + " | Registros: " + contratos.size(), fontSub);
            pFecha.setAlignment(Element.ALIGN_CENTER);
            pFecha.setSpacingAfter(15);
            document.add(pFecha);

            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{14f, 16f, 10f, 22f, 10f, 14f, 10f, 10f});

            String[] headers = {"CÓDIGO", "MERCADO", "PUESTO", "TITULAR", "DOC", "MODALIDAD", "INICIO", "ESTADO"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fontTh));
                cell.setBackgroundColor(new Color(30, 41, 59));
                cell.setPadding(4f);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            for (ContratoOcupacion c : contratos) {
                table.addCell(crearCelda(c.getCodigoContrato(), fontTd));
                table.addCell(crearCelda(c.getPuesto() != null && c.getPuesto().getMercado() != null ? c.getPuesto().getMercado().getNombre() : "-", fontTd));
                table.addCell(crearCelda(c.getPuesto() != null ? c.getPuesto().getCodigo() : "-", fontTd));
                table.addCell(crearCelda(c.getComerciante() != null ? c.getComerciante().getNombreOTitular() : "-", fontTd));
                table.addCell(crearCelda(c.getComerciante() != null ? c.getComerciante().getNumeroDocumentoTitular() : "-", fontTd));
                table.addCell(crearCelda(c.getTipoContrato() != null ? c.getTipoContrato().getNombre() : c.getTipoContratoCodigo(), fontTd));
                table.addCell(crearCelda(c.getFechaInicio() != null ? c.getFechaInicio().toString() : "-", fontTd));
                table.addCell(crearCelda(c.getEstado().name(), fontTd));
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
