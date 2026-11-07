package pe.gob.municipalidad.sigm.modules.serenazgo.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.SectorPatrullaje;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.ZonaPatrullaje;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoSectorPatrullaje;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoEntidadTerritorial;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.SectorPatrullajeRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.ZonaPatrullajeRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.GeoJsonValidatorService;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.SectorPatrullajeService;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.TerritorioSeguridadService;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.ZonaPatrullajeService;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TerritorioSeguridadServiceImpl implements TerritorioSeguridadService {

    private final SectorPatrullajeRepository sectorRepository;
    private final ZonaPatrullajeRepository zonaRepository;
    private final SectorPatrullajeService sectorService;
    private final ZonaPatrullajeService zonaService;
    private final GeoJsonValidatorService geoJsonValidatorService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public DashboardTerritorialResponse obtenerDashboardTerritorial() {
        long totalSectores = sectorRepository.count();
        long sectoresActivos = sectorRepository.countByEstado(EstadoSectorPatrullaje.ACTIVO);
        long totalZonas = zonaRepository.count();
        long zonasActivas = zonaRepository.countByEstado(EstadoSectorPatrullaje.ACTIVO);
        long sectoresConDelim = sectorRepository.countByTieneDelimitacionTrue();
        long zonasSinDelim = zonaRepository.countByTieneDelimitacionFalse();

        return DashboardTerritorialResponse.builder()
                .totalSectores(totalSectores)
                .sectoresActivos(sectoresActivos)
                .totalZonas(totalZonas)
                .zonasActivas(zonasActivas)
                .sectoresConDelimitacion(sectoresConDelim)
                .zonasSinDelimitacion(zonasSinDelim)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GeoJsonFeatureCollectionResponse obtenerMapaTerritorialCompleto() {
        List<GeoJsonFeatureCollectionResponse.GeoJsonFeature> features = new ArrayList<>();

        // 1. Sectores con delimitación
        List<SectorPatrullaje> sectoresConDelim = sectorRepository.findByTieneDelimitacionTrue();
        for (SectorPatrullaje s : sectoresConDelim) {
            try {
                if (s.getGeometriaGeojson() != null && !s.getGeometriaGeojson().trim().isEmpty()) {
                    JsonNode geomNode = extraerGeometriaNode(s.getGeometriaGeojson());
                    if (geomNode != null) {
                        Map<String, Object> props = new LinkedHashMap<>();
                        props.put("tipo", "SECTOR");
                        props.put("id", s.getId().toString());
                        props.put("codigo", s.getCodigo());
                        props.put("nombre", s.getNombre());
                        props.put("colorHex", s.getColorHex() != null ? s.getColorHex() : "#1d4ed8");
                        props.put("estado", s.getEstado().name());
                        props.put("estadoDesc", s.getEstado().getDescripcion());
                        props.put("oficina", s.getOficinaResponsable() != null ? s.getOficinaResponsable().getNombre() : "SIN ASIGNAR");
                        props.put("totalZonas", s.getZonas() != null ? s.getZonas().size() : 0);

                        features.add(GeoJsonFeatureCollectionResponse.GeoJsonFeature.builder()
                                .id(s.getId().toString())
                                .properties(props)
                                .geometry(geomNode)
                                .build());
                    }
                }
            } catch (Exception e) {
                log.warn("No se pudo parsear geometría del sector {}: {}", s.getCodigo(), e.getMessage());
            }
        }

        // 2. Zonas con delimitación
        List<ZonaPatrullaje> zonasConDelim = zonaRepository.findByTieneDelimitacionTrue();
        for (ZonaPatrullaje z : zonasConDelim) {
            try {
                if (z.getGeometriaGeojson() != null && !z.getGeometriaGeojson().trim().isEmpty()) {
                    JsonNode geomNode = extraerGeometriaNode(z.getGeometriaGeojson());
                    if (geomNode != null) {
                        Map<String, Object> props = new LinkedHashMap<>();
                        props.put("tipo", "ZONA");
                        props.put("id", z.getId().toString());
                        props.put("codigo", z.getCodigo());
                        props.put("nombre", z.getNombre());
                        props.put("colorHex", z.getSector() != null && z.getSector().getColorHex() != null ? z.getSector().getColorHex() : "#10b981");
                        props.put("estado", z.getEstado().name());
                        props.put("estadoDesc", z.getEstado().getDescripcion());
                        props.put("prioridad", z.getPrioridadOperativa().name());
                        props.put("prioridadDesc", z.getPrioridadOperativa().getDescripcion());
                        props.put("sectorId", z.getSector() != null ? z.getSector().getId().toString() : "");
                        props.put("sectorCodigo", z.getSector() != null ? z.getSector().getCodigo() : "");
                        props.put("sectorNombre", z.getSector() != null ? z.getSector().getNombre() : "");

                        features.add(GeoJsonFeatureCollectionResponse.GeoJsonFeature.builder()
                                .id(z.getId().toString())
                                .properties(props)
                                .geometry(geomNode)
                                .build());
                    }
                }
            } catch (Exception e) {
                log.warn("No se pudo parsear geometría de la zona {}: {}", z.getCodigo(), e.getMessage());
            }
        }

        return GeoJsonFeatureCollectionResponse.builder()
                .features(features)
                .build();
    }

    private JsonNode extraerGeometriaNode(String geoJsonStr) {
        try {
            JsonNode node = objectMapper.readTree(geoJsonStr.trim());
            if (node.has("type")) {
                String type = node.get("type").asText();
                if ("Feature".equalsIgnoreCase(type) && node.has("geometry")) {
                    return node.get("geometry");
                } else if ("FeatureCollection".equalsIgnoreCase(type) && node.has("features") && node.get("features").isArray() && !node.get("features").isEmpty()) {
                    return node.get("features").get(0).get("geometry");
                }
            }
            return node;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public ValidacionGeoJsonResult validarGeometria(String geoJsonStr) {
        return geoJsonValidatorService.validar(geoJsonStr);
    }

    @Override
    @Transactional
    public void importarGeoJson(ImportarGeoJsonRequest request, String username) {
        log.info("Importando GeoJSON para {} ID: {} por {}", request.getTipoEntidad(), request.getEntidadId(), username);

        ValidacionGeoJsonResult v = geoJsonValidatorService.validar(request.getContenidoGeojson());
        if (!v.isValido()) {
            throw new IllegalArgumentException("El archivo GeoJSON es inválido: " + v.getMensajeError());
        }

        DelimitacionGeoJsonRequest delimReq = DelimitacionGeoJsonRequest.builder()
                .geometriaGeojson(request.getContenidoGeojson())
                .motivo(request.getMotivo())
                .documentoReferencia(request.getDocumentoReferencia())
                .build();

        if (request.getTipoEntidad() == TipoEntidadTerritorial.SECTOR) {
            sectorService.guardarDelimitacion(request.getEntidadId(), delimReq, username);
        } else if (request.getTipoEntidad() == TipoEntidadTerritorial.ZONA) {
            zonaService.guardarDelimitacion(request.getEntidadId(), delimReq, username);
        } else {
            throw new IllegalArgumentException("Tipo de entidad territorial no soportado: " + request.getTipoEntidad());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String exportarGeoJson(TipoEntidadTerritorial tipo, UUID entidadId) {
        if (tipo == TipoEntidadTerritorial.SECTOR) {
            SectorPatrullaje s = sectorRepository.findById(entidadId)
                    .orElseThrow(() -> new NoSuchElementException("Sector no encontrado: " + entidadId));
            if (!Boolean.TRUE.equals(s.getTieneDelimitacion()) || s.getGeometriaGeojson() == null) {
                throw new IllegalStateException("El sector no cuenta con delimitación geográfica registrada");
            }
            return s.getGeometriaGeojson();
        } else if (tipo == TipoEntidadTerritorial.ZONA) {
            ZonaPatrullaje z = zonaRepository.findById(entidadId)
                    .orElseThrow(() -> new NoSuchElementException("Zona no encontrada: " + entidadId));
            if (!Boolean.TRUE.equals(z.getTieneDelimitacion()) || z.getGeometriaGeojson() == null) {
                throw new IllegalStateException("La zona no cuenta con delimitación geográfica registrada");
            }
            return z.getGeometriaGeojson();
        }
        throw new IllegalArgumentException("Tipo de entidad territorial no soportado");
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarExcel(SectorFilter sectorFilter, ZonaFilter zonaFilter) {
        List<SectorResponse> sectores = sectorService.listarSectores(sectorFilter, Pageable.unpaged()).getContent();
        List<ZonaPatrullajeResponse> zonas = zonaService.listarZonas(zonaFilter, Pageable.unpaged()).getContent();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            // Estilos
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Hoja 1: Sectores
            org.apache.poi.ss.usermodel.Sheet sheet1 = workbook.createSheet("Sectores de Patrullaje");
            String[] cols1 = {"CÓDIGO", "NOMBRE", "DESCRIPCIÓN", "OFICINA", "ESTADO", "INICIO VIGENCIA", "FIN VIGENCIA", "DELIMITACIÓN", "N° ZONAS"};
            Row r0_1 = sheet1.createRow(0);
            for (int i = 0; i < cols1.length; i++) {
                Cell c = r0_1.createCell(i);
                c.setCellValue(cols1[i]);
                c.setCellStyle(headerStyle);
            }

            int rowIdx1 = 1;
            for (SectorResponse s : sectores) {
                Row r = sheet1.createRow(rowIdx1++);
                r.createCell(0).setCellValue(sanitizar(s.getCodigo()));
                r.createCell(1).setCellValue(sanitizar(s.getNombre()));
                r.createCell(2).setCellValue(sanitizar(s.getDescripcion() != null ? s.getDescripcion() : ""));
                r.createCell(3).setCellValue(sanitizar(s.getOficinaResponsableNombre()));
                r.createCell(4).setCellValue(sanitizar(s.getEstadoDesc()));
                r.createCell(5).setCellValue(s.getFechaInicioVigencia() != null ? s.getFechaInicioVigencia().toString() : "");
                r.createCell(6).setCellValue(s.getFechaFinVigencia() != null ? s.getFechaFinVigencia().toString() : "");
                r.createCell(7).setCellValue(Boolean.TRUE.equals(s.getTieneDelimitacion()) ? "SÍ (GEOJSON)" : "NO");
                r.createCell(8).setCellValue(s.getTotalZonas());
            }

            for (int i = 0; i < cols1.length; i++) {
                sheet1.autoSizeColumn(i);
            }

            // Hoja 2: Zonas
            org.apache.poi.ss.usermodel.Sheet sheet2 = workbook.createSheet("Zonas de Patrullaje");
            String[] cols2 = {"CÓDIGO", "NOMBRE", "SECTOR ASOCIADO", "PRIORIDAD", "ESTADO", "INICIO VIGENCIA", "FIN VIGENCIA", "DELIMITACIÓN"};
            Row r0_2 = sheet2.createRow(0);
            for (int i = 0; i < cols2.length; i++) {
                Cell c = r0_2.createCell(i);
                c.setCellValue(cols2[i]);
                c.setCellStyle(headerStyle);
            }

            int rowIdx2 = 1;
            for (ZonaPatrullajeResponse z : zonas) {
                Row r = sheet2.createRow(rowIdx2++);
                r.createCell(0).setCellValue(sanitizar(z.getCodigo()));
                r.createCell(1).setCellValue(sanitizar(z.getNombre()));
                r.createCell(2).setCellValue(sanitizar(z.getSectorCodigo() + " - " + z.getSectorNombre()));
                r.createCell(3).setCellValue(sanitizar(z.getPrioridadOperativaDesc()));
                r.createCell(4).setCellValue(sanitizar(z.getEstadoDesc()));
                r.createCell(5).setCellValue(z.getFechaInicioVigencia() != null ? z.getFechaInicioVigencia().toString() : "");
                r.createCell(6).setCellValue(z.getFechaFinVigencia() != null ? z.getFechaFinVigencia().toString() : "");
                r.createCell(7).setCellValue(Boolean.TRUE.equals(z.getTieneDelimitacion()) ? "SÍ (GEOJSON)" : "NO");
            }

            for (int i = 0; i < cols2.length; i++) {
                sheet2.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error al exportar territorio a Excel", e);
            throw new RuntimeException("Error al exportar información territorial a Excel", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generarFichaSectorPdf(UUID sectorId) {
        SectorDetailResponse s = sectorService.obtenerDetallePorId(sectorId);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 36, 36, 40, 40);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            // Tipografías
            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);
            Font fontSub = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(29, 78, 216));
            Font fontTexto = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
            Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);
            Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE);
            Font fontRow = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);

            // Encabezado Municipal
            Paragraph pHeader = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA", fontTitulo);
            pHeader.setAlignment(Element.ALIGN_CENTER);
            doc.add(pHeader);

            Paragraph pSub = new Paragraph("SUBGERENCIA DE SEGURIDAD CIUDADANA Y SERENAZGO\nFICHA TÉCNICA DE SECTOR DE PATRULLAJE\n\n", fontSub);
            pSub.setAlignment(Element.ALIGN_CENTER);
            doc.add(pSub);

            // Tabla de datos generales
            PdfPTable tDatos = new PdfPTable(2);
            tDatos.setWidthPercentage(100);
            tDatos.setWidths(new float[]{30, 70});

            agregarFilaPdf(tDatos, "CÓDIGO INSTITUCIONAL:", s.getCodigo(), fontBold, fontTexto);
            agregarFilaPdf(tDatos, "NOMBRE DEL SECTOR:", s.getNombre(), fontBold, fontTexto);
            agregarFilaPdf(tDatos, "OFICINA RESPONSABLE:", s.getOficinaResponsableNombre(), fontBold, fontTexto);
            agregarFilaPdf(tDatos, "ESTADO OPERATIVO:", s.getEstadoDesc(), fontBold, fontTexto);
            agregarFilaPdf(tDatos, "COLOR ASIGNADO:", s.getColorHex(), fontBold, fontTexto);
            agregarFilaPdf(tDatos, "VIGENCIA:", (s.getFechaInicioVigencia() != null ? s.getFechaInicioVigencia() : "Indefinida") +
                    " al " + (s.getFechaFinVigencia() != null ? s.getFechaFinVigencia() : "Presente"), fontBold, fontTexto);
            agregarFilaPdf(tDatos, "DELIMITACIÓN REGISTRADA:", Boolean.TRUE.equals(s.getTieneDelimitacion()) ? "SÍ (Formato GeoJSON EPSG:4326)" : "NO DISPONIBLE", fontBold, fontTexto);
            agregarFilaPdf(tDatos, "DESCRIPCIÓN:", s.getDescripcion() != null ? s.getDescripcion() : "Sin descripción", fontBold, fontTexto);
            agregarFilaPdf(tDatos, "OBSERVACIONES:", s.getObservaciones() != null ? s.getObservaciones() : "Ninguna", fontBold, fontTexto);

            doc.add(tDatos);

            // Zonas asociadas
            Paragraph pZonas = new Paragraph("\nZONAS OPERATIVAS ASOCIADAS (" + (s.getZonas() != null ? s.getZonas().size() : 0) + "):", fontSub);
            doc.add(pZonas);

            PdfPTable tZonas = new PdfPTable(5);
            tZonas.setWidthPercentage(100);
            tZonas.setWidths(new float[]{18, 32, 18, 16, 16});

            String[] hCols = {"CÓDIGO", "NOMBRE", "PRIORIDAD", "ESTADO", "DELIMITACIÓN"};
            for (String col : hCols) {
                PdfPCell c = new PdfPCell(new Phrase(col, fontHeader));
                c.setBackgroundColor(new Color(30, 41, 59));
                c.setPadding(4);
                c.setHorizontalAlignment(Element.ALIGN_CENTER);
                tZonas.addCell(c);
            }

            if (s.getZonas() != null && !s.getZonas().isEmpty()) {
                for (ZonaPatrullajeResponse z : s.getZonas()) {
                    tZonas.addCell(new Phrase(z.getCodigo(), fontRow));
                    tZonas.addCell(new Phrase(z.getNombre(), fontRow));
                    tZonas.addCell(new Phrase(z.getPrioridadOperativaDesc(), fontRow));
                    tZonas.addCell(new Phrase(z.getEstadoDesc(), fontRow));
                    tZonas.addCell(new Phrase(Boolean.TRUE.equals(z.getTieneDelimitacion()) ? "SÍ" : "NO", fontRow));
                }
            } else {
                PdfPCell cVacio = new PdfPCell(new Phrase("No cuenta con zonas registradas", fontRow));
                cVacio.setColspan(5);
                cVacio.setPadding(6);
                cVacio.setHorizontalAlignment(Element.ALIGN_CENTER);
                tZonas.addCell(cVacio);
            }

            doc.add(tZonas);

            // Pie de página legal
            Paragraph pLegal = new Paragraph("\n\nDocumento emitido por el Sistema Integral de Gestión Municipal (SIGM-QUINUA).\n" +
                    "Las delimitaciones de patrullaje son de orden técnico-operativo municipal y no constituyen límites político-administrativos oficiales de la República del Perú.",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 7, Color.GRAY));
            pLegal.setAlignment(Element.ALIGN_CENTER);
            doc.add(pLegal);

            doc.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error al generar ficha técnica en PDF de sector: {}", sectorId, e);
            throw new RuntimeException("Error al generar PDF de sector de patrullaje", e);
        }
    }

    private void agregarFilaPdf(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, labelFont));
        c1.setBackgroundColor(new Color(241, 245, 249));
        c1.setPadding(4);
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(value != null ? value : "", valueFont));
        c2.setPadding(4);
        table.addCell(c2);
    }

    private String sanitizar(String val) {
        if (val == null) return "";
        String s = val.trim();
        if (s.startsWith("=") || s.startsWith("+") || s.startsWith("-") || s.startsWith("@")) {
            return "'" + s;
        }
        return s;
    }
}
