package pe.gob.municipalidad.sigm.modules.mercados.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.administracion.service.AuditoriaAdministrativaService;
import pe.gob.municipalidad.sigm.modules.mercados.dto.AsignacionFilter;
import pe.gob.municipalidad.sigm.modules.mercados.entity.AsignacionPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.repository.AsignacionPuestoRepository;
import pe.gob.municipalidad.sigm.modules.mercados.service.AsignacionExportService;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class AsignacionExportServiceImpl implements AsignacionExportService {

    private final AsignacionPuestoRepository asignacionRepository;
    private final AuditoriaAdministrativaService auditoriaService;

    public AsignacionExportServiceImpl(
            AsignacionPuestoRepository asignacionRepository,
            AuditoriaAdministrativaService auditoriaService) {
        this.asignacionRepository = asignacionRepository;
        this.auditoriaService = auditoriaService;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarExcel(AsignacionFilter filter) {
        Page<AsignacionPuesto> page = buscarAsignaciones(filter, PageRequest.of(0, 10000, Sort.by(Sort.Direction.DESC, "fechaSolicitud")));

        List<AsignacionPuesto> asignaciones = page.getContent();
        StringBuilder csv = new StringBuilder();

        // UTF-8 BOM para compatibilidad con Microsoft Excel
        csv.append('\ufeff');

        // Encabezados
        csv.append("Código Asignación;Mercado;Sector;Puesto;Titular;Tipo Doc;N° Documento;Estado;Tipo Asignación;Fecha Solicitud;Fecha Inicio;Fecha Fin;Motivo\n");

        for (AsignacionPuesto a : asignaciones) {
            String titular = "-";
            String docTipo = "DOC";
            String docNum = "-";
            if (a.getComerciante() != null) {
                if (a.getComerciante().getPersona() != null) {
                    titular = a.getComerciante().getPersona().getNombreCompleto();
                    docTipo = a.getComerciante().getPersona().getTipoDocumento() != null ?
                            a.getComerciante().getPersona().getTipoDocumento() : "DNI";
                    docNum = a.getComerciante().getPersona().getNumeroDocumento();
                } else if (a.getComerciante().getPersonaJuridica() != null) {
                    titular = a.getComerciante().getPersonaJuridica().getRazonSocial();
                    docTipo = "RUC";
                    docNum = a.getComerciante().getPersonaJuridica().getRuc();
                }
            }

            String puestoCodigo = a.getPuesto() != null ? a.getPuesto().getCodigo() : "-";
            String mercadoNombre = a.getPuesto() != null && a.getPuesto().getMercado() != null ? a.getPuesto().getMercado().getNombre() : "-";
            String sectorNombre = a.getPuesto() != null && a.getPuesto().getSector() != null ? a.getPuesto().getSector().getNombre() : "-";

            csv.append(sanitizar(a.getCodigoAsignacion())).append(";")
                    .append(sanitizar(mercadoNombre)).append(";")
                    .append(sanitizar(sectorNombre)).append(";")
                    .append(sanitizar(puestoCodigo)).append(";")
                    .append(sanitizar(titular)).append(";")
                    .append(sanitizar(docTipo)).append(";")
                    .append(sanitizar(docNum)).append(";")
                    .append(sanitizar(a.getEstado() != null ? a.getEstado().getDescripcion() : "-")).append(";")
                    .append(sanitizar(a.getTipoAsignacion() != null ? a.getTipoAsignacion().name() : "-")).append(";")
                    .append(sanitizar(a.getFechaSolicitud() != null ? a.getFechaSolicitud().toString() : "-")).append(";")
                    .append(sanitizar(a.getFechaInicio() != null ? a.getFechaInicio().toString() : "-")).append(";")
                    .append(sanitizar(a.getFechaFin() != null ? a.getFechaFin().toString() : "-")).append(";")
                    .append(sanitizar(a.getMotivo() != null ? a.getMotivo() : "-"))
                    .append("\n");
        }

        auditoriaService.registrar("EXPORTAR_ASIGNACIONES_EXCEL", "mercados_asignaciones", null, "EXITO",
                "Exportación de padrón de asignaciones en formato Excel/CSV. Total: " + asignaciones.size());

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarPdf(AsignacionFilter filter) {
        Page<AsignacionPuesto> page = buscarAsignaciones(filter, PageRequest.of(0, 1000, Sort.by(Sort.Direction.DESC, "fechaSolicitud")));

        List<AsignacionPuesto> asignaciones = page.getContent();
        Document document = new Document(PageSize.A4.rotate(), 25, 25, 30, 30);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(29, 78, 216));
            Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.DARK_GRAY);
            Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE);
            Font tableBodyFont = FontFactory.getFont(FontFactory.HELVETICA, 7, Color.BLACK);

            Paragraph pTitle = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA — PADRÓN DE ASIGNACIONES DE PUESTOS", titleFont);
            pTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(pTitle);

            String fechaStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            Paragraph pSub = new Paragraph("Generado el: " + fechaStr + " | Registros: " + asignaciones.size(), subFont);
            pSub.setAlignment(Element.ALIGN_CENTER);
            pSub.setSpacingAfter(12);
            document.add(pSub);

            float[] widths = {12f, 15f, 10f, 18f, 11f, 11f, 11f, 12f};
            PdfPTable table = new PdfPTable(widths);
            table.setWidthPercentage(100);

            String[] headers = {"Código", "Mercado", "Puesto", "Comerciante Titular", "Documento", "Estado", "Inicio", "Fin"};
            for (String ch : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(ch, tableHeaderFont));
                cell.setBackgroundColor(new Color(29, 78, 216));
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            int count = 0;
            Color altColor = new Color(248, 250, 252);
            for (AsignacionPuesto a : asignaciones) {
                Color bg = (count++ % 2 == 0) ? altColor : Color.WHITE;

                String titular = "-";
                String doc = "-";
                if (a.getComerciante() != null) {
                    if (a.getComerciante().getPersona() != null) {
                        titular = a.getComerciante().getPersona().getNombreCompleto();
                        doc = a.getComerciante().getPersona().getNumeroDocumento();
                    } else if (a.getComerciante().getPersonaJuridica() != null) {
                        titular = a.getComerciante().getPersonaJuridica().getRazonSocial();
                        doc = a.getComerciante().getPersonaJuridica().getRuc();
                    }
                }

                addCell(table, a.getCodigoAsignacion() != null ? a.getCodigoAsignacion() : "-", tableBodyFont, bg, Element.ALIGN_CENTER);
                addCell(table, a.getPuesto() != null && a.getPuesto().getMercado() != null ? a.getPuesto().getMercado().getNombre() : "-", tableBodyFont, bg, Element.ALIGN_LEFT);
                addCell(table, a.getPuesto() != null ? a.getPuesto().getCodigo() : "-", tableBodyFont, bg, Element.ALIGN_CENTER);
                addCell(table, titular, tableBodyFont, bg, Element.ALIGN_LEFT);
                addCell(table, doc, tableBodyFont, bg, Element.ALIGN_CENTER);
                addCell(table, a.getEstado() != null ? a.getEstado().getDescripcion() : "-", tableBodyFont, bg, Element.ALIGN_CENTER);
                addCell(table, a.getFechaInicio() != null ? a.getFechaInicio().toString() : "-", tableBodyFont, bg, Element.ALIGN_CENTER);
                addCell(table, a.getFechaFin() != null ? a.getFechaFin().toString() : "Indefinida", tableBodyFont, bg, Element.ALIGN_CENTER);
            }

            document.add(table);
            document.close();

            auditoriaService.registrar("EXPORTAR_ASIGNACIONES_PDF", "mercados_asignaciones", null, "EXITO",
                    "Exportación de padrón de asignaciones en PDF. Total: " + asignaciones.size());

            return out.toByteArray();

        } catch (Exception e) {
            log.error("Error al generar PDF de asignaciones", e);
            throw new RuntimeException("Error al generar reporte PDF de asignaciones", e);
        }
    }

    private void addCell(PdfPTable table, String text, Font font, Color bg, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "-", font));
        cell.setBackgroundColor(bg);
        cell.setPadding(3);
        cell.setHorizontalAlignment(align);
        cell.setBorderColor(new Color(226, 232, 240));
        table.addCell(cell);
    }

    private String sanitizar(String valor) {
        if (valor == null) return "";
        String limpio = valor.replace("\"", "\"\"").replace("\n", " ").replace("\r", " ").trim();
        // Prevención de CSV / Excel Formula Injection
        if (limpio.startsWith("=") || limpio.startsWith("+") || limpio.startsWith("-") || limpio.startsWith("@")) {
            limpio = "'" + limpio;
        }
        return "\"" + limpio + "\"";
    }

    private Page<AsignacionPuesto> buscarAsignaciones(AsignacionFilter filter, PageRequest pageRequest) {
        if (filter != null && filter.getTermino() != null && !filter.getTermino().trim().isEmpty()) {
            return asignacionRepository.buscarConFiltrosConTermino(
                    filter.getTermino().trim(),
                    filter.getMercadoId(),
                    filter.getSectorId(),
                    filter.getPabellonId(),
                    filter.getComercianteId(),
                    filter.getEstado(),
                    filter.getTipoAsignacion(),
                    filter.getFechaInicioDesde(),
                    filter.getFechaInicioHasta(),
                    pageRequest
            );
        } else {
            return asignacionRepository.buscarConFiltrosSinTermino(
                    filter != null ? filter.getMercadoId() : null,
                    filter != null ? filter.getSectorId() : null,
                    filter != null ? filter.getPabellonId() : null,
                    filter != null ? filter.getComercianteId() : null,
                    filter != null ? filter.getEstado() : null,
                    filter != null ? filter.getTipoAsignacion() : null,
                    filter != null ? filter.getFechaInicioDesde() : null,
                    filter != null ? filter.getFechaInicioHasta() : null,
                    pageRequest
            );
        }
    }
}
