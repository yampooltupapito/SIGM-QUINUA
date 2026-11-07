package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.portal.MovimientoCuentaCiudadanoDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ContribuyenteTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.CuentaCorrienteTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.MovimientoCuentaCorrienteTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.NaturalezaMovimiento;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.CuentaCorrienteTributariaRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.MovimientoCuentaCorrienteTributariaRepository;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Consulta de cuenta corriente y generación de Estado de Cuenta Informativo ciudadano.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultaCuentaCorrienteCiudadanoService {

    private final PortalTributarioContextService contextService;
    private final CuentaCorrienteTributariaRepository cuentaRepository;
    private final MovimientoCuentaCorrienteTributariaRepository movimientoRepository;

    @Transactional(readOnly = true)
    public Optional<CuentaCorrienteTributaria> obtenerMiCuentaCorriente() {
        Optional<ContribuyenteTributario> contribOpt = contextService.obtenerContribuyenteActivo();
        if (contribOpt.isEmpty()) {
            return Optional.empty();
        }
        return cuentaRepository.findByContribuyenteId(contribOpt.get().getId());
    }

    @Transactional(readOnly = true)
    public Page<MovimientoCuentaCiudadanoDto> listarMisMovimientos(Pageable pageable) {
        Optional<CuentaCorrienteTributaria> ctaOpt = obtenerMiCuentaCorriente();
        if (ctaOpt.isEmpty()) {
            return Page.empty(pageable);
        }

        Page<MovimientoCuentaCorrienteTributaria> page = movimientoRepository
                .findByCuentaCorrienteIdOrderByNumeroSecuenciaDesc(ctaOpt.get().getId(), pageable);

        return page.map(this::convertirADto);
    }

    @Transactional(readOnly = true)
    public byte[] generarEstadoCuentaPdf() {
        ContribuyenteTributario c = contextService.obtenerContribuyenteActivo()
                .orElseThrow(() -> new BusinessException("No cuenta con un contribuyente activo habilitado."));

        CuentaCorrienteTributaria cuenta = cuentaRepository.findByContribuyenteId(c.getId())
                .orElseThrow(() -> new BusinessException("No se encontró cuenta corriente registrada para el contribuyente."));

        List<MovimientoCuentaCorrienteTributaria> movimientos = movimientoRepository
                .findByCuentaCorrienteIdOrderByNumeroSecuenciaAsc(cuenta.getId());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 40, 40);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(16, 24, 39));
            Font fontSub = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(100, 116, 139));
            Font fontDisclaimer = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, new Color(185, 28, 28));
            Font fontHeaderTabla = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
            Font fontCuerpo = FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(30, 41, 59));
            Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, new Color(30, 41, 59));

            // Encabezado Institucional
            Paragraph pMun = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA", fontTitulo);
            pMun.setAlignment(Element.ALIGN_CENTER);
            document.add(pMun);

            Paragraph pSub = new Paragraph("Subgerencia de Administración Tributaria y Rentas\nESTADO DE CUENTA CORRIENTE CIUDADANO (INFORMATIVO)", fontSub);
            pSub.setAlignment(Element.ALIGN_CENTER);
            pSub.setSpacingAfter(10f);
            document.add(pSub);

            // Alerta legal: Es documento informativo
            Paragraph pAviso = new Paragraph("AVISO IMPORTANTE: El presente documento tiene carácter exclusivamente INFORMATIVO. No constituye constancia o certificado oficial de no adeudo tributario ni liberación definitiva de obligaciones fiscales.", fontDisclaimer);
            pAviso.setAlignment(Element.ALIGN_CENTER);
            pAviso.setSpacingAfter(15f);
            document.add(pAviso);

            // Datos del Contribuyente
            PdfPTable datosTable = new PdfPTable(2);
            datosTable.setWidthPercentage(100);
            datosTable.setWidths(new float[]{30f, 70f});

            agregarCampo(datosTable, "Código Contribuyente:", c.getCodigoContribuyente(), fontBold, fontCuerpo);
            agregarCampo(datosTable, "Nombre / Razón Social:", c.getNombreRazonSocial(), fontBold, fontCuerpo);
            agregarCampo(datosTable, "Documento de Identidad:", c.getTipoDocumento() + " " + c.getNumeroDocumento(), fontBold, fontCuerpo);
            agregarCampo(datosTable, "Domicilio Fiscal:", c.getDomicilioFiscal() != null ? c.getDomicilioFiscal() : "Quinua, Ayacucho", fontBold, fontCuerpo);
            agregarCampo(datosTable, "Código Cuenta Corriente:", cuenta.getCodigoCuenta(), fontBold, fontCuerpo);
            agregarCampo(datosTable, "Saldo Actual Vigente:", "S/ " + (cuenta.getSaldoActual() != null ? cuenta.getSaldoActual() : BigDecimal.ZERO), fontBold, fontCuerpo);
            agregarCampo(datosTable, "Fecha de Emisión:", OffsetDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), fontBold, fontCuerpo);

            datosTable.setSpacingAfter(15f);
            document.add(datosTable);

            // Tabla de Movimientos
            Paragraph pMov = new Paragraph("HISTORIAL DE MOVIMIENTOS FINANCIEROS REGISTRADOS", fontBold);
            pMov.setSpacingAfter(5f);
            document.add(pMov);

            PdfPTable tabla = new PdfPTable(6);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{10f, 15f, 25f, 15f, 15f, 20f});

            Color headerColor = new Color(29, 78, 216);
            agregarHeader(tabla, "N°", headerColor, fontHeaderTabla);
            agregarHeader(tabla, "Fecha", headerColor, fontHeaderTabla);
            agregarHeader(tabla, "Operación / Glosa", headerColor, fontHeaderTabla);
            agregarHeader(tabla, "Cargo (S/)", headerColor, fontHeaderTabla);
            agregarHeader(tabla, "Abono (S/)", headerColor, fontHeaderTabla);
            agregarHeader(tabla, "Saldo (S/)", headerColor, fontHeaderTabla);

            if (movimientos.isEmpty()) {
                PdfPCell cVacia = new PdfPCell(new Phrase("No registra movimientos históricos en cuenta corriente.", fontCuerpo));
                cVacia.setColspan(6);
                cVacia.setPadding(8f);
                cVacia.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabla.addCell(cVacia);
            } else {
                for (MovimientoCuentaCorrienteTributaria m : movimientos) {
                    PdfPCell c1 = new PdfPCell(new Phrase(String.valueOf(m.getNumeroSecuencia()), fontCuerpo));
                    c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                    c1.setPadding(4f);
                    tabla.addCell(c1);

                    BigDecimal cargo = m.getNaturaleza() == NaturalezaMovimiento.DEBITO ? m.getImporte() : BigDecimal.ZERO;
                    BigDecimal abono = m.getNaturaleza() == NaturalezaMovimiento.CREDITO ? m.getImporte() : BigDecimal.ZERO;

                    PdfPCell c2 = new PdfPCell(new Phrase(m.getFechaRegistro() != null ? m.getFechaRegistro().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : m.getFechaOperacion().toString(), fontCuerpo));
                    c2.setHorizontalAlignment(Element.ALIGN_CENTER);
                    c2.setPadding(4f);
                    tabla.addCell(c2);

                    PdfPCell c3 = new PdfPCell(new Phrase(m.getDescripcion() != null ? m.getDescripcion() : m.getTipoMovimiento().name(), fontCuerpo));
                    c3.setPadding(4f);
                    tabla.addCell(c3);

                    PdfPCell c4 = new PdfPCell(new Phrase(cargo.compareTo(BigDecimal.ZERO) > 0 ? cargo.toString() : "0.00", fontCuerpo));
                    c4.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c4.setPadding(4f);
                    tabla.addCell(c4);

                    PdfPCell c5 = new PdfPCell(new Phrase(abono.compareTo(BigDecimal.ZERO) > 0 ? abono.toString() : "0.00", fontCuerpo));
                    c5.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c5.setPadding(4f);
                    tabla.addCell(c5);

                    PdfPCell c6 = new PdfPCell(new Phrase(m.getSaldoPosterior() != null ? m.getSaldoPosterior().toString() : "0.00", fontBold));
                    c6.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c6.setPadding(4f);
                    tabla.addCell(c6);
                }
            }

            document.add(tabla);
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generando estado de cuenta informativo PDF: {}", e.getMessage(), e);
            throw new BusinessException("No fue posible generar el estado de cuenta en PDF: " + e.getMessage());
        }
    }

    private void agregarHeader(PdfPTable table, String text, Color bg, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setBackgroundColor(bg);
        c.setPadding(6f);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c);
    }

    private void agregarCampo(PdfPTable table, String label, String value, Font fLabel, Font fVal) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, fLabel));
        c1.setBackgroundColor(new Color(248, 250, 252));
        c1.setPadding(4f);
        c1.setBorderColor(new Color(226, 232, 240));
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(value != null ? value : "—", fVal));
        c2.setPadding(4f);
        c2.setBorderColor(new Color(226, 232, 240));
        table.addCell(c2);
    }

    private MovimientoCuentaCiudadanoDto convertirADto(MovimientoCuentaCorrienteTributaria m) {
        BigDecimal cargo = m.getNaturaleza() == NaturalezaMovimiento.DEBITO ? m.getImporte() : BigDecimal.ZERO;
        BigDecimal abono = m.getNaturaleza() == NaturalezaMovimiento.CREDITO ? m.getImporte() : BigDecimal.ZERO;
        String concepto = (m.getObligacion() != null && m.getObligacion().getConcepto() != null)
                ? m.getObligacion().getConcepto().getNombre() : null;

        return MovimientoCuentaCiudadanoDto.builder()
                .id(m.getId())
                .numeroSecuencia(m.getNumeroSecuencia())
                .fechaMovimiento(m.getFechaRegistro())
                .tipoMovimiento(m.getTipoMovimiento().name())
                .origenOperacion(m.getDocumentoSustento() != null ? m.getDocumentoSustento() : "SISTEMA")
                .referenciaOperacion(m.getReferenciaOrigen())
                .montoCargo(cargo)
                .montoAbono(abono)
                .saldoPosterior(m.getSaldoPosterior())
                .conceptoNombre(concepto)
                .glosa(m.getDescripcion())
                .build();
    }
}

