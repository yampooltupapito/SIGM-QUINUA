package pe.gob.municipalidad.sigm.modules.serenazgo.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.administracion.service.AuditoriaAdministrativaService;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.estadisticas.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.ReporteEstadisticoSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.SectorPatrullaje;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.ZonaPatrullaje;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.ReporteEstadisticoSerenazgoRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.SectorPatrullajeRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.ZonaPatrullajeRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.EstadisticasSerenazgoService;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.ReportesEstadisticosSerenazgoService;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportesEstadisticosSerenazgoServiceImpl implements ReportesEstadisticosSerenazgoService {

    public static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final ReporteEstadisticoSerenazgoRepository reporteRepository;
    private final EstadisticasSerenazgoService estadisticasService;
    private final SectorPatrullajeRepository sectorRepository;
    private final ZonaPatrullajeRepository zonaRepository;
    private final UsuarioRepository usuarioRepository;
    private final OficinaRepository oficinaRepository;
    private final AuditoriaAdministrativaService auditoriaService;

    @Override
    @Transactional
    public ReporteHistorialDto generarReporte(GenerarReporteRequest req, CustomUserDetails user, String ip) {
        Usuario usuario = usuarioRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException("Usuario no autenticado en el sistema municipal"));

        // 1. Obtener datos analíticos
        FiltroEstadisticoRequest filtro = req.getFiltro() != null ? req.getFiltro() : new FiltroEstadisticoRequest();
        DashboardEstadisticoResponse datos = estadisticasService.obtenerDashboardEstadistico(filtro, user);

        // 2. Generar código correlativo inmutable
        Long correlativo = reporteRepository.obtenerSiguienteValorSecuencia();
        int anio = LocalDate.now(ZONA_LIMA).getYear();
        String codigo = String.format("REP-SER-%d-%05d", anio, correlativo);

        // 3. Resolver metadatos
        String titulo = (req.getTitulo() != null && !req.getTitulo().isBlank()) ?
                req.getTitulo() : req.getTipoReporte().getTitulo();
        String extension = req.getFormato() == FormatoReporteEstadistico.PDF ? ".pdf" : ".xlsx";
        String nombreArchivo = codigo + "_" + req.getTipoReporte().name().toLowerCase() + extension;
        String mimeType = req.getFormato().getMimeType();

        SectorPatrullaje sector = null;
        if (filtro.getSectorId() != null) {
            sector = sectorRepository.findById(filtro.getSectorId()).orElse(null);
        }
        ZonaPatrullaje zona = null;
        if (filtro.getZonaId() != null) {
            zona = zonaRepository.findById(filtro.getZonaId()).orElse(null);
        }
        Oficina oficina = null;
        if (filtro.getOficinaId() != null) {
            oficina = oficinaRepository.findById(filtro.getOficinaId()).orElse(null);
        }

        TipoPeriodoEstadistico tipoPeriodo = TipoPeriodoEstadistico.MES_ACTUAL;
        if (filtro.getPeriodoTipo() != null) {
            try {
                tipoPeriodo = TipoPeriodoEstadistico.valueOf(filtro.getPeriodoTipo().toUpperCase().trim());
            } catch (Exception ignored) {}
        }

        OffsetDateTime fInicio = filtro.getFechaInicio() != null ?
                filtro.getFechaInicio().atStartOfDay(ZONA_LIMA).toOffsetDateTime() : null;
        OffsetDateTime fFin = filtro.getFechaFin() != null ?
                filtro.getFechaFin().atTime(23, 59, 59).atZone(ZONA_LIMA).toOffsetDateTime() : null;

        // 4. Compilar archivo binario
        byte[] bytesArchivo;
        if (req.getFormato() == FormatoReporteEstadistico.PDF) {
            bytesArchivo = compilarReportePdf(codigo, titulo, req.getTipoReporte(), filtro, datos, usuario.getUsername());
        } else {
            bytesArchivo = compilarReporteExcel(codigo, titulo, req.getTipoReporte(), filtro, datos, usuario.getUsername());
        }

        // 5. Almacenar archivo en disco local
        String rutaDestino = "storage/serenazgo/reportes/" + nombreArchivo;
        try {
            Path path = Paths.get(rutaDestino);
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Files.write(path, bytesArchivo);
        } catch (IOException e) {
            log.error("Error al escribir archivo en disco {}: {}", rutaDestino, e.getMessage());
            throw new BusinessException("No se pudo almacenar el archivo generado del reporte en el servidor");
        }

        // 6. Persistir registro en BD
        ReporteEstadisticoSerenazgo reporte = ReporteEstadisticoSerenazgo.builder()
                .codigo(codigo)
                .tipoReporte(req.getTipoReporte())
                .titulo(titulo)
                .descripcion(req.getDescripcion())
                .formato(req.getFormato())
                .parametrosJson("Periodo: " + datos.getPeriodoEtiqueta() + " | Agrupacion: " + filtro.getAgrupacion())
                .periodoTipo(tipoPeriodo)
                .fechaInicio(fInicio)
                .fechaFin(fFin)
                .sector(sector)
                .zona(zona)
                .usuarioGenerador(usuario)
                .usernameGenerador(usuario.getUsername())
                .oficina(oficina)
                .estado(EstadoReporteEstadistico.COMPLETADO)
                .nombreArchivo(nombreArchivo)
                .mimeType(mimeType)
                .tamanioBytes((long) bytesArchivo.length)
                .rutaArchivo(rutaDestino)
                .fechaGeneracion(OffsetDateTime.now(ZONA_LIMA))
                .build();

        ReporteEstadisticoSerenazgo guardado = reporteRepository.save(reporte);

        // 7. Registro de Auditoría
        auditoriaService.registrar("GENERAR_REPORTE_ESTADISTICO", "serenazgo_reportes_estadisticos", guardado.getId(), "EXITO",
                "Reporte " + guardado.getCodigo() + " [" + req.getFormato().name() + "] tipo: " + req.getTipoReporte().name());

        log.info("Reporte estadístico {} [{}] generado exitosamente por {}", guardado.getCodigo(), req.getFormato(), usuario.getUsername());

        return mapearHistorialDto(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReporteHistorialDto> listarHistorialReportes(TipoReporteEstadistico tipo, LocalDate desde, LocalDate hasta, Pageable pageable) {
        if (tipo == null && desde == null && hasta == null) {
            return reporteRepository.findAllByOrderByFechaGeneracionDesc(pageable)
                    .map(this::mapearHistorialDto);
        }

        org.springframework.data.jpa.domain.Specification<ReporteEstadisticoSerenazgo> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            if (tipo != null) {
                predicates.add(cb.equal(root.get("tipoReporte"), tipo));
            }
            if (desde != null) {
                OffsetDateTime d = desde.atStartOfDay(ZONA_LIMA).toOffsetDateTime();
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaGeneracion"), d));
            }
            if (hasta != null) {
                OffsetDateTime h = hasta.atTime(23, 59, 59).atZone(ZONA_LIMA).toOffsetDateTime();
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaGeneracion"), h));
            }
            if (query != null) {
                query.orderBy(cb.desc(root.get("fechaGeneracion")));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return reporteRepository.findAll(spec, pageable)
                .map(this::mapearHistorialDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource descargarReporte(UUID id, CustomUserDetails user) {
        ReporteEstadisticoSerenazgo rep = reporteRepository.findById(id)
                .orElseThrow(() -> new BusinessException("El reporte estadístico solicitado no existe"));

        // Verificación de archivo
        if (rep.getRutaArchivo() == null || rep.getRutaArchivo().isBlank()) {
            throw new BusinessException("El archivo físico del reporte no se encuentra disponible");
        }

        try {
            Path path = Paths.get(rep.getRutaArchivo());
            if (!Files.exists(path)) {
                throw new BusinessException("El archivo generado no se localizó en el sistema de almacenamiento");
            }
            byte[] bytes = Files.readAllBytes(path);

            // Auditoría de descarga
            auditoriaService.registrar("DESCARGAR_REPORTE_ESTADISTICO", "serenazgo_reportes_estadisticos", rep.getId(), "EXITO",
                    "Descarga de reporte " + rep.getCodigo() + " por " + user.getUsername());

            return new ByteArrayResource(bytes) {
                @Override
                public String getFilename() {
                    return rep.getNombreArchivo();
                }
            };
        } catch (IOException e) {
            log.error("Error al leer archivo {}: {}", rep.getRutaArchivo(), e.getMessage());
            throw new BusinessException("Ocurrió un error al leer el archivo para su descarga");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteEstadisticoSerenazgo obtenerPorId(UUID id) {
        return reporteRepository.findById(id)
                .orElseThrow(() -> new BusinessException("El reporte solicitado no existe"));
    }

    // =========================================================================
    // Generador PDF Profesional (OpenPDF)
    // =========================================================================

    private byte[] compilarReportePdf(String codigo, String titulo, TipoReporteEstadistico tipo,
                                      FiltroEstadisticoRequest filtro, DashboardEstadisticoResponse datos, String username) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 36, 36, 40, 40);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            // Fuentes
            Font fTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15, Color.DARK_GRAY);
            Font fSub = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
            Font fSeccion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(29, 78, 216));
            Font fCabecera = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE);
            Font fTexto = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);
            Font fTextoBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.BLACK);
            Font fLegal = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 7, new Color(100, 116, 139));

            // Encabezado Institucional
            Paragraph pMun = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA", fTitulo);
            pMun.setAlignment(Element.ALIGN_CENTER);
            doc.add(pMun);

            Paragraph pDep = new Paragraph("Gerencia de Seguridad Ciudadana y Serenazgo — Sistema SIGM-QUINUA", fSub);
            pDep.setAlignment(Element.ALIGN_CENTER);
            doc.add(pDep);

            doc.add(new Paragraph(" "));

            // Título del Reporte y Código
            PdfPTable tEnc = new PdfPTable(2);
            tEnc.setWidthPercentage(100);
            tEnc.setWidths(new float[]{70, 30});

            PdfPCell cTit = new PdfPCell(new Phrase(titulo.toUpperCase(), fSeccion));
            cTit.setBorder(PdfPCell.NO_BORDER);
            tEnc.addCell(cTit);

            PdfPCell cCod = new PdfPCell(new Phrase(codigo, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK)));
            cCod.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cCod.setBorder(PdfPCell.NO_BORDER);
            tEnc.addCell(cCod);
            doc.add(tEnc);

            // Metadatos
            Paragraph pMeta = new Paragraph(
                    "Período evaluado: " + datos.getPeriodoEtiqueta() +
                    " | Comparación previa: " + datos.getPeriodoComparacionEtiqueta() + "\n" +
                    "Generado por: " + username +
                    " | Fecha y Hora de emisión: " + OffsetDateTime.now(ZONA_LIMA).format(FORMATO_FECHA_HORA) + " (America/Lima)",
                    FontFactory.getFont(FontFactory.HELVETICA, 8, Color.DARK_GRAY));
            pMeta.setSpacingAfter(10);
            doc.add(pMeta);

            // Descargo Legal Obligatorio
            PdfPTable tLegal = new PdfPTable(1);
            tLegal.setWidthPercentage(100);
            PdfPCell cLeg = new PdfPCell(new Phrase(
                    "MARCO LEGAL Y METODOLÓGICO (Ley N° 31297 / Ley N° 27933):\n" +
                    "Las cifras, tiempos y mapas contenidos en este reporte reflejan exclusivamente información de supervisión administrativa registrada en la base de datos de Serenazgo de Quinua. " +
                    "No constituyen estadísticas oficiales de criminalidad ni prueba fehaciente en procesos penales o policiales. " +
                    "Los reportes vecinales e intervenciones no equivalen a delitos comprobados judicialmente.",
                    fLegal));
            cLeg.setBackgroundColor(new Color(241, 245, 249));
            cLeg.setPadding(6);
            cLeg.setBorderColor(new Color(203, 213, 225));
            tLegal.addCell(cLeg);
            tLegal.setSpacingAfter(12);
            doc.add(tLegal);

            // Tabla 1: Indicadores Clave de Desempeño (KPIs)
            doc.add(new Paragraph("1. INDICADORES Y RESUMEN OPERATIVO COMPARATIVO", fSeccion));
            PdfPTable tKpi = new PdfPTable(5);
            tKpi.setWidthPercentage(100);
            tKpi.setWidths(new float[]{35, 15, 15, 15, 20});
            tKpi.setSpacingBefore(6);
            tKpi.setSpacingAfter(12);

            String[] hKpi = {"Indicador Operativo", "Período Actual", "Período Previo", "Var. Absoluta", "Var. %"};
            for (String h : hKpi) {
                PdfPCell ch = new PdfPCell(new Phrase(h, fCabecera));
                ch.setBackgroundColor(new Color(29, 78, 216));
                ch.setPadding(5);
                tKpi.addCell(ch);
            }

            for (IndicadorKpiDto k : datos.getKpis()) {
                tKpi.addCell(new PdfPCell(new Phrase(k.getNombre(), fTextoBold)));
                tKpi.addCell(new PdfPCell(new Phrase(k.getValorActual() + " " + k.getUnidad(), fTexto)));
                tKpi.addCell(new PdfPCell(new Phrase(k.getValorAnterior() + " " + k.getUnidad(), fTexto)));
                tKpi.addCell(new PdfPCell(new Phrase((k.getVariacionAbsoluta() >= 0 ? "+" : "") + k.getVariacionAbsoluta(), fTexto)));
                String varTxt = k.isVariacionIndefinida() ? "N/D" : ((k.getVariacionPorcentual() >= 0 ? "+" : "") + k.getVariacionPorcentual() + "%");
                tKpi.addCell(new PdfPCell(new Phrase(varTxt, fTextoBold)));
            }
            doc.add(tKpi);

            // Tabla 2: Tiempos de Atención y Desempeño Operativo
            if (datos.getTiemposAtencion() != null) {
                doc.add(new Paragraph("2. TIEMPOS OPERATIVOS DE ATENCIÓN (MINUTOS)", fSeccion));
                PdfPTable tTiempos = new PdfPTable(4);
                tTiempos.setWidthPercentage(100);
                tTiempos.setWidths(new float[]{40, 20, 20, 20});
                tTiempos.setSpacingBefore(6);
                tTiempos.setSpacingAfter(12);

                String[] hT = {"Fase Operativa Registrada", "Promedio", "Mediana", "Percentil 90"};
                for (String h : hT) {
                    PdfPCell ch = new PdfPCell(new Phrase(h, fCabecera));
                    ch.setBackgroundColor(new Color(30, 41, 59));
                    ch.setPadding(5);
                    tTiempos.addCell(ch);
                }

                TiemposAtencionEstadisticasDto tm = datos.getTiemposAtencion();
                agregarFilaTiempo(tTiempos, "Registro a Asignación", tm.getPromedioAsignacionMinutos(), tm.getMedianaAsignacionMinutos(), tm.getPercentil90AsignacionMinutos(), fTexto);
                agregarFilaTiempo(tTiempos, "Asignación a Despacho", tm.getPromedioDespachoMinutos(), tm.getMedianaDespachoMinutos(), null, fTexto);
                agregarFilaTiempo(tTiempos, "Despacho a Aceptación", tm.getPromedioAceptacionMinutos(), tm.getMedianaAceptacionMinutos(), null, fTexto);
                agregarFilaTiempo(tTiempos, "Duración Total de Atención", tm.getPromedioDuracionMinutos(), tm.getMedianaDuracionMinutos(), tm.getPercentil90DuracionMinutos(), fTexto);

                doc.add(tTiempos);

                Paragraph pIncomp = new Paragraph("Atenciones válidas analizadas: " + tm.getTotalAtencionesValidas() +
                        " | Registros con marcas temporales incompletas excluidos del cálculo: " + tm.getRegistrosIncompletosExcluidos(), fLegal);
                pIncomp.setSpacingAfter(10);
                doc.add(pIncomp);
            }

            // Tabla 3: Distribución Territorial por Sector
            if (datos.getEstadisticasPorSector() != null && !datos.getEstadisticasPorSector().isEmpty()) {
                doc.add(new Paragraph("3. DISTRIBUCIÓN TERRITORIAL POR SECTOR DE PATRULLAJE", fSeccion));
                PdfPTable tSec = new PdfPTable(5);
                tSec.setWidthPercentage(100);
                tSec.setWidths(new float[]{35, 15, 15, 15, 20});
                tSec.setSpacingBefore(6);
                tSec.setSpacingAfter(12);

                String[] hS = {"Sector", "Incidencias", "Patrullajes", "Intervenciones", "% Incidencias"};
                for (String h : hS) {
                    PdfPCell ch = new PdfPCell(new Phrase(h, fCabecera));
                    ch.setBackgroundColor(new Color(30, 41, 59));
                    ch.setPadding(5);
                    tSec.addCell(ch);
                }

                for (EstadisticasTerritorialesDto s : datos.getEstadisticasPorSector()) {
                    tSec.addCell(new PdfPCell(new Phrase(s.getSectorNombre(), fTextoBold)));
                    tSec.addCell(new PdfPCell(new Phrase(String.valueOf(s.getTotalIncidencias()), fTexto)));
                    tSec.addCell(new PdfPCell(new Phrase(String.valueOf(s.getTotalPatrullajes()), fTexto)));
                    tSec.addCell(new PdfPCell(new Phrase(String.valueOf(s.getTotalIntervenciones()), fTexto)));
                    tSec.addCell(new PdfPCell(new Phrase(s.getPorcentajeIncidencias() + "%", fTextoBold)));
                }
                doc.add(tSec);
            }

            // Tabla 4: Cumplimiento de Patrullaje y Puntos de Control
            if (datos.getEstadisticasPatrullaje() != null) {
                doc.add(new Paragraph("4. RENDIMIENTO DE PATRULLAJES Y RECORRIDOS", fSeccion));
                EstadisticasPatrullajeDto ep = datos.getEstadisticasPatrullaje();
                Paragraph pPat = new Paragraph(
                        "• Patrullajes Programados: " + ep.getProgramados() +
                        " | Finalizados: " + ep.getFinalizados() +
                        " | Tasa de Cumplimiento: " + (ep.getTasaCumplimientoPorcentaje() != null ? ep.getTasaCumplimientoPorcentaje() + "%" : "N/D") + "\n" +
                        "• Puntos de Control Planificados: " + ep.getPuntosPlanificados() +
                        " | Puntos Visitados: " + ep.getPuntosVisitados() +
                        " | Cobertura de Puntos: " + (ep.getTasaVisitaPuntosPorcentaje() != null ? ep.getTasaVisitaPuntosPorcentaje() + "%" : "N/D") + "\n" +
                        "• Distancia Planificada: " + ep.getDistanciaPlanificadaKm() + " km" +
                        " | Distancia GPS Registrada: " + ep.getDistanciaGpsRegistradaKm() + " km" +
                        " | Duración Promedio: " + (ep.getDuracionPromedioHoras() != null ? ep.getDuracionPromedioHoras() + " hrs" : "N/D"),
                        fTexto);
                pPat.setSpacingBefore(4);
                pPat.setSpacingAfter(16);
                doc.add(pPat);
            }

            // Pie de Página Institucional
            Paragraph pPie = new Paragraph("SIGM-QUINUA — Submódulo de Estadísticas y Reportes de Seguridad Ciudadana\nDocumento emitido para fines estrictamente administrativos y de planificación municipal.", fLegal);
            pPie.setAlignment(Element.ALIGN_CENTER);
            doc.add(pPie);

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error al compilar reporte PDF: {}", e.getMessage(), e);
            throw new BusinessException("Error al compilar el documento PDF: " + e.getMessage());
        }
    }

    private void agregarFilaTiempo(PdfPTable tabla, String fase, Double prom, Double med, Double p90, Font f) {
        tabla.addCell(new PdfPCell(new Phrase(fase, f)));
        tabla.addCell(new PdfPCell(new Phrase(prom != null ? prom + " min" : "N/D", f)));
        tabla.addCell(new PdfPCell(new Phrase(med != null ? med + " min" : "N/D", f)));
        tabla.addCell(new PdfPCell(new Phrase(p90 != null ? p90 + " min" : "N/A", f)));
    }

    // =========================================================================
    // Generador Excel Profesional (Apache POI con Prevención de Formula Injection)
    // =========================================================================

    private byte[] compilarReporteExcel(String codigo, String titulo, TipoReporteEstadistico tipo,
                                        FiltroEstadisticoRequest filtro, DashboardEstadisticoResponse datos, String username) {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            // Estilos
            CellStyle hStyle = wb.createCellStyle();
            hStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            hStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            org.apache.poi.ss.usermodel.Font fH = wb.createFont();
            fH.setColor(IndexedColors.WHITE.getIndex());
            fH.setBold(true);
            hStyle.setFont(fH);

            CellStyle secStyle = wb.createCellStyle();
            secStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            secStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            org.apache.poi.ss.usermodel.Font fSec = wb.createFont();
            fSec.setBold(true);
            secStyle.setFont(fSec);

            // Hoja 1: Resumen Ejecutivo
            Sheet s1 = wb.createSheet("Resumen Ejecutivo");
            Row r0 = s1.createRow(0);
            r0.createCell(0).setCellValue(sanitizar("MUNICIPALIDAD DISTRITAL DE QUINUA - SERENAZGO"));
            Row r1 = s1.createRow(1);
            r1.createCell(0).setCellValue(sanitizar("Reporte: " + titulo + " [" + codigo + "]"));
            Row r2 = s1.createRow(2);
            r2.createCell(0).setCellValue(sanitizar("Período: " + datos.getPeriodoEtiqueta() + " | Generado por: " + username + " | Fecha: " + OffsetDateTime.now(ZONA_LIMA).format(FORMATO_FECHA_HORA)));
            Row r3 = s1.createRow(3);
            r3.createCell(0).setCellValue(sanitizar("Ley N° 31297 / Ley N° 27933: Datos administrativos de supervisión. Sin valor probatorio penal ni pericial automático."));

            Row rHeadKpi = s1.createRow(5);
            String[] colKpi = {"Código", "Indicador", "Módulo", "Valor Actual", "Valor Previo", "Var. Absoluta", "Var. %", "Unidad"};
            for (int i = 0; i < colKpi.length; i++) {
                Cell c = rHeadKpi.createCell(i);
                c.setCellValue(colKpi[i]);
                c.setCellStyle(hStyle);
            }

            int rowIdx = 6;
            for (IndicadorKpiDto k : datos.getKpis()) {
                Row r = s1.createRow(rowIdx++);
                r.createCell(0).setCellValue(sanitizar(k.getCodigo()));
                r.createCell(1).setCellValue(sanitizar(k.getNombre()));
                r.createCell(2).setCellValue(sanitizar(k.getModulo()));
                r.createCell(3).setCellValue(k.getValorActual() != null ? k.getValorActual() : 0);
                r.createCell(4).setCellValue(k.getValorAnterior() != null ? k.getValorAnterior() : 0);
                r.createCell(5).setCellValue(k.getVariacionAbsoluta() != null ? k.getVariacionAbsoluta() : 0);
                if (k.isVariacionIndefinida() || k.getVariacionPorcentual() == null) {
                    r.createCell(6).setCellValue("N/D");
                } else {
                    r.createCell(6).setCellValue(k.getVariacionPorcentual());
                }
                r.createCell(7).setCellValue(sanitizar(k.getUnidad()));
            }

            for (int i = 0; i < colKpi.length; i++) {
                s1.autoSizeColumn(i);
            }

            // Hoja 2: Desglose Territorial por Sector
            Sheet s2 = wb.createSheet("Desglose por Sector");
            Row rHeadSec = s2.createRow(0);
            String[] colSec = {"Código", "Sector de Patrullaje", "Incidencias", "Patrullajes", "Intervenciones", "% Incidencias"};
            for (int i = 0; i < colSec.length; i++) {
                Cell c = rHeadSec.createCell(i);
                c.setCellValue(colSec[i]);
                c.setCellStyle(hStyle);
            }

            int rowSecIdx = 1;
            for (EstadisticasTerritorialesDto st : datos.getEstadisticasPorSector()) {
                Row r = s2.createRow(rowSecIdx++);
                r.createCell(0).setCellValue(sanitizar(st.getSectorCodigo()));
                r.createCell(1).setCellValue(sanitizar(st.getSectorNombre()));
                r.createCell(2).setCellValue(st.getTotalIncidencias());
                r.createCell(3).setCellValue(st.getTotalPatrullajes());
                r.createCell(4).setCellValue(st.getTotalIntervenciones());
                r.createCell(5).setCellValue(st.getPorcentajeIncidencias() != null ? st.getPorcentajeIncidencias() : 0.0);
            }

            for (int i = 0; i < colSec.length; i++) {
                s2.autoSizeColumn(i);
            }

            // Hoja 3: Tiempos de Atención
            if (datos.getTiemposAtencion() != null) {
                Sheet s3 = wb.createSheet("Tiempos de Atención");
                Row rhT = s3.createRow(0);
                String[] colT = {"Fase Operativa", "Promedio (min)", "Mediana (min)", "Percentil 90 (min)"};
                for (int i = 0; i < colT.length; i++) {
                    Cell c = rhT.createCell(i);
                    c.setCellValue(colT[i]);
                    c.setCellStyle(hStyle);
                }

                TiemposAtencionEstadisticasDto tm = datos.getTiemposAtencion();
                agregarFilaTiempoExcel(s3, 1, "Registro a Asignación", tm.getPromedioAsignacionMinutos(), tm.getMedianaAsignacionMinutos(), tm.getPercentil90AsignacionMinutos());
                agregarFilaTiempoExcel(s3, 2, "Asignación a Despacho", tm.getPromedioDespachoMinutos(), tm.getMedianaDespachoMinutos(), null);
                agregarFilaTiempoExcel(s3, 3, "Despacho a Aceptación", tm.getPromedioAceptacionMinutos(), tm.getMedianaAceptacionMinutos(), null);
                agregarFilaTiempoExcel(s3, 4, "Duración Total de Atención", tm.getPromedioDuracionMinutos(), tm.getMedianaDuracionMinutos(), tm.getPercentil90DuracionMinutos());

                for (int i = 0; i < colT.length; i++) {
                    s3.autoSizeColumn(i);
                }
            }

            // Hoja 4: Catálogo Técnico y Metodología
            Sheet s4 = wb.createSheet("Catálogo y Metodología");
            Row rhCat = s4.createRow(0);
            String[] colCat = {"Código", "Nombre del Indicador", "Módulo", "Fórmula de Cálculo", "Fuente de Datos", "Limitaciones Técnicas"};
            for (int i = 0; i < colCat.length; i++) {
                Cell c = rhCat.createCell(i);
                c.setCellValue(colCat[i]);
                c.setCellStyle(hStyle);
            }

            int rowCatIdx = 1;
            for (CatalogoIndicadorDto ci : datos.getCatalogoIndicadores()) {
                Row r = s4.createRow(rowCatIdx++);
                r.createCell(0).setCellValue(sanitizar(ci.getCodigo()));
                r.createCell(1).setCellValue(sanitizar(ci.getNombre()));
                r.createCell(2).setCellValue(sanitizar(ci.getModuloOrigen()));
                r.createCell(3).setCellValue(sanitizar(ci.getFormulaCalculo()));
                r.createCell(4).setCellValue(sanitizar(ci.getFuenteDatos()));
                r.createCell(5).setCellValue(sanitizar(ci.getLimitacionesInterpretacion()));
            }

            for (int i = 0; i < colCat.length; i++) {
                s4.autoSizeColumn(i);
            }

            wb.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error al compilar reporte Excel: {}", e.getMessage(), e);
            throw new BusinessException("Error al compilar el libro Excel: " + e.getMessage());
        }
    }

    private void agregarFilaTiempoExcel(Sheet s, int rowIdx, String fase, Double prom, Double med, Double p90) {
        Row r = s.createRow(rowIdx);
        r.createCell(0).setCellValue(sanitizar(fase));
        if (prom != null) r.createCell(1).setCellValue(prom); else r.createCell(1).setCellValue("N/D");
        if (med != null) r.createCell(2).setCellValue(med); else r.createCell(2).setCellValue("N/D");
        if (p90 != null) r.createCell(3).setCellValue(p90); else r.createCell(3).setCellValue("N/A");
    }

    /**
     * Prevención estricta de Formula Injection en celdas de texto exportadas a Excel (OWASP CSV/Formula Injection).
     */
    private String sanitizar(String texto) {
        if (texto == null) return "";
        String t = texto.trim();
        if (t.startsWith("=") || t.startsWith("+") || t.startsWith("-") || t.startsWith("@")) {
            return "'" + t;
        }
        return t;
    }

    private ReporteHistorialDto mapearHistorialDto(ReporteEstadisticoSerenazgo r) {
        long bytes = r.getTamanioBytes() != null ? r.getTamanioBytes() : 0L;
        String tamFormateado;
        if (bytes < 1024) tamFormateado = bytes + " B";
        else if (bytes < 1024 * 1024) tamFormateado = (bytes / 1024) + " KB";
        else tamFormateado = String.format("%.2f MB", (bytes / (1024.0 * 1024.0)));

        return ReporteHistorialDto.builder()
                .id(r.getId())
                .codigo(r.getCodigo())
                .tipoReporte(r.getTipoReporte())
                .tipoReporteTitulo(r.getTipoReporte().getTitulo())
                .titulo(r.getTitulo())
                .formato(r.getFormato())
                .formatoExtension(r.getFormato().getExtension())
                .fechaGeneracion(r.getFechaGeneracion())
                .usernameGenerador(r.getUsernameGenerador())
                .tamanioBytes(bytes)
                .tamanioFormateado(tamFormateado)
                .estado(r.getEstado())
                .estadoDescripcion(r.getEstado().getDescripcion())
                .estadoBadgeClass(r.getEstado().getBadgeClass())
                .periodoEtiqueta(r.getParametrosJson())
                .build();
    }
}
