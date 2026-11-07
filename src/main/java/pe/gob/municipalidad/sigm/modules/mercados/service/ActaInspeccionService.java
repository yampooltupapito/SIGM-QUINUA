package pe.gob.municipalidad.sigm.modules.mercados.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.document.pdf.QrCodeGenerator;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.administracion.service.AuditoriaAdministrativaService;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ActaInspeccionRequest;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ActaInspeccionResponse;
import pe.gob.municipalidad.sigm.modules.mercados.dto.VerificacionActaPublicaResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoActaInspeccion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoInspeccion;
import pe.gob.municipalidad.sigm.modules.mercados.repository.ActaInspeccionMercadoRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.InspeccionMercadoRepository;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class ActaInspeccionService {

    private final ActaInspeccionMercadoRepository actaRepository;
    private final InspeccionMercadoRepository inspeccionRepository;
    private final UsuarioRepository usuarioRepository;
    private final QrCodeGenerator qrCodeGenerator;
    private final AuditoriaAdministrativaService auditoriaService;

    public ActaInspeccionService(ActaInspeccionMercadoRepository actaRepository,
                                 InspeccionMercadoRepository inspeccionRepository,
                                 UsuarioRepository usuarioRepository,
                                 QrCodeGenerator qrCodeGenerator,
                                 AuditoriaAdministrativaService auditoriaService) {
        this.actaRepository = actaRepository;
        this.inspeccionRepository = inspeccionRepository;
        this.usuarioRepository = usuarioRepository;
        this.qrCodeGenerator = qrCodeGenerator;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public ActaInspeccionResponse elaborarBorradorActa(UUID inspeccionId, ActaInspeccionRequest request, String username, Long usuarioId) {
        InspeccionMercado inspeccion = inspeccionRepository.findById(inspeccionId)
                .orElseThrow(() -> new ResourceNotFoundException("Inspección no encontrada: " + inspeccionId));

        if (inspeccion.getEstado() == EstadoInspeccion.CANCELADA) {
            throw new BusinessException("No se puede elaborar acta de una inspección CANCELADA.");
        }

        Usuario responsable = resolverUsuario(usuarioId, username);

        Optional<ActaInspeccionMercado> existenteOpt = actaRepository.findByInspeccionId(inspeccionId);
        ActaInspeccionMercado acta;

        if (existenteOpt.isPresent()) {
            acta = existenteOpt.get();
            if (acta.getEstado() == EstadoActaInspeccion.EMITIDA) {
                throw new BusinessException("El acta ya ha sido EMITIDA formalmente y es inmutable.");
            }
            acta.setResumen(request.getResumen().trim());
            acta.setConclusiones(request.getConclusiones().trim());
            acta.setRecomendaciones(request.getRecomendaciones());
            acta.setResponsable(responsable);
        } else {
            Long correlativo = actaRepository.obtenerSiguienteCorrelativo();
            int anio = Year.now().getValue();
            String codigoActa = String.format("ACTA-INS-%d-%06d", anio, correlativo);
            String codigoVerificacion = UUID.randomUUID().toString().replace("-", "") + Long.toHexString(System.currentTimeMillis());

            acta = ActaInspeccionMercado.builder()
                    .inspeccion(inspeccion)
                    .codigoActa(codigoActa)
                    .fechaElaboracion(OffsetDateTime.now())
                    .resumen(request.getResumen().trim())
                    .conclusiones(request.getConclusiones().trim())
                    .recomendaciones(request.getRecomendaciones())
                    .estado(EstadoActaInspeccion.BORRADOR)
                    .codigoVerificacion(codigoVerificacion)
                    .hashSha256("PENDIENTE_EMISION")
                    .responsable(responsable)
                    .build();
        }

        ActaInspeccionMercado guardada = actaRepository.save(acta);

        auditoriaService.registrar("ELABORAR_ACTA_INSPECCION", "mercados_actas_inspeccion", guardada.getId(),
                "EXITO", "Borrador de acta elaborado: " + guardada.getCodigoActa() + " para inspección: " + inspeccion.getCodigoInspeccion());

        return toResponse(guardada);
    }

    @Transactional
    public ActaInspeccionResponse aprobarYEmitirActa(UUID inspeccionId, String username, Long usuarioId) {
        ActaInspeccionMercado acta = actaRepository.findByInspeccionId(inspeccionId)
                .orElseThrow(() -> new ResourceNotFoundException("No se ha registrado un acta para la inspección: " + inspeccionId));

        if (acta.getEstado() == EstadoActaInspeccion.EMITIDA) {
            throw new BusinessException("El acta ya se encuentra emitida.");
        }

        InspeccionMercado inspeccion = acta.getInspeccion();

        // Calcular firma hash SHA-256
        String rawData = acta.getCodigoActa() + "|" + inspeccion.getCodigoInspeccion() + "|" +
                acta.getResumen() + "|" + acta.getConclusiones() + "|" + acta.getCodigoVerificacion();
        String hashSha256;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(rawData.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            hashSha256 = sb.toString();
        } catch (Exception e) {
            hashSha256 = "SHA256_DEFAULT_" + UUID.randomUUID();
        }

        acta.setEstado(EstadoActaInspeccion.EMITIDA);
        acta.setHashSha256(hashSha256);
        ActaInspeccionMercado guardada = actaRepository.save(acta);

        // Actualizar inspección a FINALIZADA
        inspeccion.setEstado(EstadoInspeccion.FINALIZADA);
        inspeccionRepository.save(inspeccion);

        auditoriaService.registrar("EMITIR_ACTA_INSPECCION", "mercados_actas_inspeccion", guardada.getId(),
                "EXITO", "Acta emitida y formalizada con hash: " + hashSha256.substring(0, 8) + "... para inspección: " + inspeccion.getCodigoInspeccion());

        return toResponse(guardada);
    }

    @Transactional(readOnly = true)
    public byte[] generarPdfActa(UUID inspeccionId) {
        ActaInspeccionMercado acta = actaRepository.findByInspeccionId(inspeccionId)
                .orElseThrow(() -> new ResourceNotFoundException("Acta no encontrada para la inspección: " + inspeccionId));

        InspeccionMercado inspeccion = acta.getInspeccion();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 40, 40);
            PdfWriter.getInstance(document, out);
            document.open();

            // Colores institucionales
            Color colorPrimario = new Color(29, 78, 216); // #1d4ed8
            Color colorFondo = new Color(241, 245, 249);  // slate-100
            Color colorTexto = new Color(15, 23, 42);     // slate-900

            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, colorPrimario);
            Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, colorTexto);
            Font fontHeaderTabla = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE);
            Font fontCelda = FontFactory.getFont(FontFactory.HELVETICA, 8, colorTexto);
            Font fontNegrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, colorTexto);
            Font fontPie = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 7, Color.GRAY);

            // 1. Encabezado institucional
            Paragraph pHeader = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA", fontTitulo);
            pHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(pHeader);

            Paragraph pSub = new Paragraph("Subgerencia de Desarrollo Económico y Servicios Municipales\n" +
                    "Área de Fiscalización y Administración de Mercados", fontSubtitulo);
            pSub.setAlignment(Element.ALIGN_CENTER);
            pSub.setSpacingAfter(10);
            document.add(pSub);

            // 2. Título del Acta
            PdfPTable tblTitulo = new PdfPTable(1);
            tblTitulo.setWidthPercentage(100);
            PdfPCell cellTit = new PdfPCell(new Phrase("ACTA DE INSPECCIÓN Y SUPERVISIÓN COMERCIAL - " + acta.getCodigoActa(), fontSubtitulo));
            cellTit.setBackgroundColor(colorFondo);
            cellTit.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellTit.setPadding(8);
            tblTitulo.addCell(cellTit);
            tblTitulo.setSpacingAfter(12);
            document.add(tblTitulo);

            // 3. Datos Generales de la Inspección
            PdfPTable tblDatos = new PdfPTable(4);
            tblDatos.setWidthPercentage(100);
            tblDatos.setWidths(new float[]{22f, 28f, 22f, 28f});

            addCellLabelVal(tblDatos, "Código Inspección:", inspeccion.getCodigoInspeccion(), fontNegrita, fontCelda);
            addCellLabelVal(tblDatos, "Tipo Inspección:", inspeccion.getTipoInspeccion() != null ? inspeccion.getTipoInspeccion().getDescripcion() : "N/A", fontNegrita, fontCelda);

            addCellLabelVal(tblDatos, "Mercado Municipal:", inspeccion.getMercado() != null ? inspeccion.getMercado().getNombre() : "N/A", fontNegrita, fontCelda);
            addCellLabelVal(tblDatos, "Puesto / Sector:", (inspeccion.getPuesto() != null ? inspeccion.getPuesto().getCodigo() : "GENERAL"), fontNegrita, fontCelda);

            addCellLabelVal(tblDatos, "Comerciante Titular:", inspeccion.getComerciante() != null && inspeccion.getComerciante().getPersona() != null ?
                    inspeccion.getComerciante().getPersona().getNombreCompleto() : "INSPECCIÓN DE ESPACIOS COMUNES", fontNegrita, fontCelda);
            addCellLabelVal(tblDatos, "Documento:", inspeccion.getComerciante() != null && inspeccion.getComerciante().getPersona() != null ?
                    inspeccion.getComerciante().getPersona().getNumeroDocumento() : "N/A", fontNegrita, fontCelda);

            addCellLabelVal(tblDatos, "Inspector Responsable:", inspeccion.getInspectorResponsable() != null ? inspeccion.getInspectorResponsable().getUsername() : "N/A", fontNegrita, fontCelda);
            addCellLabelVal(tblDatos, "Fecha y Hora:", inspeccion.getFechaProgramada() + (inspeccion.getHoraProgramada() != null ? " " + inspeccion.getHoraProgramada() : ""), fontNegrita, fontCelda);

            tblDatos.setSpacingAfter(12);
            document.add(tblDatos);

            // 4. Verificaciones In Situ
            if (inspeccion.getVerificaciones() != null && !inspeccion.getVerificaciones().isEmpty()) {
                Paragraph pTitVerif = new Paragraph("1. RESULTADOS DE VERIFICACIÓN IN SITU", fontSubtitulo);
                pTitVerif.setSpacingAfter(6);
                document.add(pTitVerif);

                PdfPTable tblVerif = new PdfPTable(4);
                tblVerif.setWidthPercentage(100);
                tblVerif.setWidths(new float[]{12f, 25f, 45f, 18f});

                addTableHeader(tblVerif, "Código", fontHeaderTabla, colorPrimario);
                addTableHeader(tblVerif, "Categoría", fontHeaderTabla, colorPrimario);
                addTableHeader(tblVerif, "Criterio Evaluado", fontHeaderTabla, colorPrimario);
                addTableHeader(tblVerif, "Resultado", fontHeaderTabla, colorPrimario);

                for (RespuestaVerificacionInspeccion v : inspeccion.getVerificaciones()) {
                    tblVerif.addCell(new Phrase(v.getItemVerificacion().getCodigo(), fontCelda));
                    tblVerif.addCell(new Phrase(v.getItemVerificacion().getCategoria(), fontCelda));
                    tblVerif.addCell(new Phrase(v.getItemVerificacion().getDescripcion(), fontCelda));
                    tblVerif.addCell(new Phrase(v.getResultado() != null ? v.getResultado().name() : "-", fontCelda));
                }
                tblVerif.setSpacingAfter(12);
                document.add(tblVerif);
            }

            // 5. Hallazgos Técnicos
            if (inspeccion.getHallazgos() != null && !inspeccion.getHallazgos().isEmpty()) {
                Paragraph pTitHal = new Paragraph("2. HALLAZGOS TÉCNICOS DETECTADOS", fontSubtitulo);
                pTitHal.setSpacingAfter(6);
                document.add(pTitHal);

                PdfPTable tblHal = new PdfPTable(4);
                tblHal.setWidthPercentage(100);
                tblHal.setWidths(new float[]{12f, 20f, 50f, 18f});

                addTableHeader(tblHal, "Código", fontHeaderTabla, colorPrimario);
                addTableHeader(tblHal, "Categoría", fontHeaderTabla, colorPrimario);
                addTableHeader(tblHal, "Descripción de la Inobservancia", fontHeaderTabla, colorPrimario);
                addTableHeader(tblHal, "Gravedad", fontHeaderTabla, colorPrimario);

                for (HallazgoInspeccionMercado h : inspeccion.getHallazgos()) {
                    tblHal.addCell(new Phrase(h.getCodigo(), fontCelda));
                    tblHal.addCell(new Phrase(h.getCategoria(), fontCelda));
                    tblHal.addCell(new Phrase(h.getDescripcion(), fontCelda));
                    tblHal.addCell(new Phrase(h.getGravedadTecnica() != null ? h.getGravedadTecnica().name() : "N/A", fontCelda));
                }
                tblHal.setSpacingAfter(12);
                document.add(tblHal);
            }

            // 6. Resumen y Conclusiones
            Paragraph pTitConc = new Paragraph("3. CONCLUSIONES Y RECOMENDACIONES", fontSubtitulo);
            pTitConc.setSpacingAfter(4);
            document.add(pTitConc);

            Paragraph pResumen = new Paragraph("Resumen: " + acta.getResumen(), fontCelda);
            pResumen.setSpacingAfter(4);
            document.add(pResumen);

            Paragraph pConc = new Paragraph("Conclusiones: " + acta.getConclusiones(), fontCelda);
            pConc.setSpacingAfter(4);
            document.add(pConc);

            if (acta.getRecomendaciones() != null && !acta.getRecomendaciones().isEmpty()) {
                Paragraph pRec = new Paragraph("Recomendaciones: " + acta.getRecomendaciones(), fontCelda);
                pRec.setSpacingAfter(10);
                document.add(pRec);
            }

            // 7. Código QR de Verificación Pública y Firma Hash
            String urlVerificacion = "https://quinua.gob.pe/verificar/mercados/acta/" + acta.getCodigoVerificacion();
            byte[] qrBytes = qrCodeGenerator.generateQrCodePng(urlVerificacion, 110, 110);

            PdfPTable tblFirma = new PdfPTable(2);
            tblFirma.setWidthPercentage(100);
            tblFirma.setWidths(new float[]{75f, 25f});

            PdfPCell cellTxt = new PdfPCell();
            cellTxt.setBorder(Rectangle.NO_BORDER);
            cellTxt.addElement(new Paragraph("ESTADO DEL ACTA: " + acta.getEstado().name(), fontNegrita));
            cellTxt.addElement(new Paragraph("CÓDIGO DE VERIFICACIÓN: " + acta.getCodigoVerificacion(), fontCelda));
            cellTxt.addElement(new Paragraph("FIRMA DIGITAL HASH (SHA-256):\n" + acta.getHashSha256(), fontPie));
            cellTxt.addElement(new Paragraph("Documento emitido formalmente por la Municipalidad Distrital de Quinua conforme al marco municipal vigente.", fontPie));
            tblFirma.addCell(cellTxt);

            PdfPCell cellQr = new PdfPCell();
            cellQr.setBorder(Rectangle.NO_BORDER);
            cellQr.setHorizontalAlignment(Element.ALIGN_RIGHT);
            if (qrBytes != null && qrBytes.length > 0) {
                try {
                    Image imgQr = Image.getInstance(qrBytes);
                    imgQr.scaleAbsolute(90f, 90f);
                    cellQr.addElement(imgQr);
                } catch (Exception ex) {
                    log.warn("No se pudo incrustar el código QR en el PDF: {}", ex.getMessage());
                }
            }
            tblFirma.addCell(cellQr);

            document.add(tblFirma);
            document.close();

            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error al generar PDF del acta de inspección: {}", e.getMessage(), e);
            throw new BusinessException("No fue posible generar el documento PDF del acta: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public VerificacionActaPublicaResponse verificarActaPublica(String codigoVerificacion) {
        Optional<ActaInspeccionMercado> actaOpt = actaRepository.findByCodigoVerificacion(codigoVerificacion);
        if (actaOpt.isEmpty()) {
            return VerificacionActaPublicaResponse.builder()
                    .valida(false)
                    .mensaje("El código de verificación no corresponde a ningún acta de inspección registrada en la Municipalidad Distrital de Quinua.")
                    .build();
        }

        ActaInspeccionMercado a = actaOpt.get();
        InspeccionMercado i = a.getInspeccion();

        return VerificacionActaPublicaResponse.builder()
                .valida(true)
                .codigoActa(a.getCodigoActa())
                .codigoInspeccion(i.getCodigoInspeccion())
                .tipoDocumento("ACTA OFICIAL DE INSPECCIÓN Y SUPERVISIÓN DE MERCADOS")
                .entidadEmisora("Municipalidad Distrital de Quinua - Huamanga - Ayacucho")
                .mercadoNombre(i.getMercado() != null ? i.getMercado().getNombre() : "MERCADO MUNICIPAL")
                .puestoCodigo(i.getPuesto() != null ? i.getPuesto().getCodigo() : "ÁREA GENERAL")
                .fechaEmision(a.getFechaElaboracion())
                .estadoActa(a.getEstado().name())
                .hashIntegridad(a.getHashSha256())
                .mensaje("Acta de inspección auténtica y válida en el registro municipal de Quinua.")
                .build();
    }

    public ActaInspeccionResponse toResponse(ActaInspeccionMercado a) {
        return ActaInspeccionResponse.builder()
                .id(a.getId())
                .inspeccionId(a.getInspeccion().getId())
                .codigoInspeccion(a.getInspeccion().getCodigoInspeccion())
                .codigoActa(a.getCodigoActa())
                .fechaElaboracion(a.getFechaElaboracion())
                .resumen(a.getResumen())
                .conclusiones(a.getConclusiones())
                .recomendaciones(a.getRecomendaciones())
                .estado(a.getEstado())
                .estadoDescripcion(a.getEstado() != null ? a.getEstado().getDescripcion() : null)
                .codigoVerificacion(a.getCodigoVerificacion())
                .hashSha256(a.getHashSha256())
                .responsableId(a.getResponsable().getId())
                .responsableNombre(a.getResponsable().getUsername())
                .build();
    }

    private void addTableHeader(PdfPTable table, String text, Font font, Color bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(4);
        table.addCell(cell);
    }

    private void addCellLabelVal(PdfPTable table, String label, String val, Font fontLbl, Font fontVal) {
        PdfPCell cLbl = new PdfPCell(new Phrase(label, fontLbl));
        cLbl.setBorder(Rectangle.NO_BORDER);
        cLbl.setPadding(3);
        table.addCell(cLbl);

        PdfPCell cVal = new PdfPCell(new Phrase(val != null ? val : "-", fontVal));
        cVal.setBorder(Rectangle.NO_BORDER);
        cVal.setPadding(3);
        table.addCell(cVal);
    }

    private Usuario resolverUsuario(Long usuarioId, String username) {
        if (usuarioId != null) {
            Optional<Usuario> u = usuarioRepository.findById(usuarioId);
            if (u.isPresent()) {
                return u.get();
            }
        }
        if (username != null && !username.isBlank() && !"sistema".equalsIgnoreCase(username.trim())) {
            Optional<Usuario> u = usuarioRepository.findByUsername(username.trim());
            if (u.isPresent()) {
                return u.get();
            }
        }
        if (username != null && !username.isBlank()) {
            Optional<Usuario> u = usuarioRepository.findByUsername(username.trim());
            if (u.isPresent()) {
                return u.get();
            }
        }
        return usuarioRepository.findAll().stream()
                .filter(u -> Boolean.TRUE.equals(u.getEnabled()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró ningún usuario habilitado en el sistema."));
    }
}
