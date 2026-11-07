package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.document.entity.CategoriaDocumento;
import pe.gob.municipalidad.sigm.common.document.entity.ClasificacionAcceso;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.document.entity.EstadoDocumento;
import pe.gob.municipalidad.sigm.common.document.pdf.QrCodeGenerator;
import pe.gob.municipalidad.sigm.common.document.repository.DocumentoRepository;
import pe.gob.municipalidad.sigm.common.document.storage.DocumentStorageService;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.documentopago.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.DocumentoPagoTributarioService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.PdfDocumentoTributarioService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.specification.DocumentoSpecification;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.validator.DocumentoValidator;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentoPagoTributarioServiceImpl implements DocumentoPagoTributarioService {

    private final DocumentoPagoTributarioRepository documentoRepository;
    private final TipoDocumentoPagoTributarioRepository tipoDocumentoRepository;
    private final SerieDocumentoPagoTributarioRepository serieRepository;
    private final HistorialDocumentoPagoTributarioRepository historialRepository;
    private final PagoTributarioRepository pagoRepository;
    private final AplicacionPagoTributarioRepository aplicacionPagoRepository;
    private final DocumentoRepository commonDocumentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final DocumentStorageService storageService;
    private final PdfDocumentoTributarioService pdfDocumentoService;
    private final QrCodeGenerator qrCodeGenerator;
    private final DocumentoValidator documentoValidator;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");

    @Override
    @Transactional
    public DocumentoPagoResponse emitirDocumento(EmitirDocumentoRequest request, CustomUserDetails user, String ip, String userAgent) {
        if (request == null || request.getPagoId() == null) {
            throw new BusinessException("La solicitud de emisión debe especificar el identificador de pago");
        }

        // Idempotencia
        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank()) {
            var existente = documentoRepository.findAll((root, cq, cb) ->
                    cb.equal(root.get("idempotencyKey"), request.getIdempotencyKey())).stream().findFirst();
            if (existente.isPresent()) {
                log.info("Documento ya emitido previamente con llave idempotente: {}", request.getIdempotencyKey());
                return mapToResponse(existente.get());
            }
        }

        PagoTributario pago = pagoRepository.findById(request.getPagoId())
                .orElseThrow(() -> new BusinessException("Pago tributario no encontrado: " + request.getPagoId()));

        String codTipo = request.getTipoDocumentoCodigo() != null && !request.getTipoDocumentoCodigo().isBlank()
                ? request.getTipoDocumentoCodigo()
                : "RECIBO_PAGO";

        TipoDocumentoPagoTributario tipoDoc = tipoDocumentoRepository.findByCodigo(codTipo)
                .orElseThrow(() -> new BusinessException("Tipo de documento no encontrado o no configurado: " + codTipo));

        documentoValidator.validarPagoParaEmision(pago, tipoDoc);

        // Serie de emisión
        SerieDocumentoPagoTributario serie;
        if (request.getSerieCodigo() != null && !request.getSerieCodigo().isBlank()) {
            serie = serieRepository.findByTipoDocumentoIdAndCodigoSerie(tipoDoc.getId(), request.getSerieCodigo())
                    .orElseThrow(() -> new BusinessException("Serie documental no encontrada: " + request.getSerieCodigo()));
        } else {
            serie = serieRepository.findFirstByTipoDocumentoCodigoAndEstado(codTipo, "ACTIVA")
                    .orElseThrow(() -> new BusinessException("No existe ninguna serie ACTIVA para el tipo documental: " + tipoDoc.getNombre()));
        }

        documentoValidator.validarSerieParaEmision(serie, tipoDoc);

        // Asignación atómica de correlativo con bloqueo pesimista
        SerieDocumentoPagoTributario serieBloqueada = serieRepository.findByIdForUpdate(serie.getId())
                .orElseThrow(() -> new BusinessException("Error al bloquear serie documental para asignación de correlativo"));

        long nuevoCorrelativo = serieBloqueada.getNumeroCorrelativoActual() + 1;
        serieBloqueada.setNumeroCorrelativoActual(nuevoCorrelativo);
        serieRepository.save(serieBloqueada);

        String codigoDocumento = String.format("%s-%08d", serieBloqueada.getCodigoSerie(), nuevoCorrelativo);
        String codigoVerificacion = "VER-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        String urlVerificacion = "/verificar/documento-tributario/" + codigoVerificacion;

        Usuario emisor = null;
        if (user != null && user.getId() != null) {
            emisor = usuarioRepository.findById(user.getId()).orElse(null);
        }

        DocumentoPagoTributario doc = DocumentoPagoTributario.builder()
                .codigoDocumento(codigoDocumento)
                .tipoDocumento(tipoDoc)
                .serie(serieBloqueada)
                .numeroCorrelativo(nuevoCorrelativo)
                .pagoTributario(pago)
                .contribuyente(pago.getContribuyente())
                .fechaEmision(OffsetDateTime.now(ZONA_LIMA))
                .importeTotal(pago.getImporte())
                .moneda(pago.getMoneda() != null ? pago.getMoneda() : "PEN")
                .estado(EstadoDocumentoPagoTributario.EMITIDO)
                .usuarioEmisor(emisor)
                .oficinaEmisora(serieBloqueada.getOficina())
                .codigoVerificacion(codigoVerificacion)
                .urlVerificacion(urlVerificacion)
                .idempotencyKey(request.getIdempotencyKey())
                .numeroReimpresiones(0)
                .build();

        // Obtener aplicaciones del pago para la tabla del PDF
        List<AplicacionPagoTributario> aplicaciones = aplicacionPagoRepository.findByPagoIdAndEstado(pago.getId(), EstadoAplicacionPago.ACTIVA);

        // Generar PDF y calcular hash
        String fullUrlVerificacion = baseUrl.replaceAll("/+$", "") + urlVerificacion;
        byte[] pdfBytes = pdfDocumentoService.generarPdfDocumento(doc, aplicaciones, fullUrlVerificacion);
        String hashSha256 = pdfDocumentoService.calcularSha256(pdfBytes);

        // Guardar archivo en DocumentStorageService
        String nombreInterno = storageService.store(new ByteArrayInputStream(pdfBytes), "pdf");

        // Registrar metadatos en tabla `documentos`
        Documento archivo = Documento.builder()
                .identificadorPublico(UUID.randomUUID())
                .nombreOriginal(codigoDocumento + ".pdf")
                .nombreInterno(nombreInterno)
                .extension("pdf")
                .mimeType("application/pdf")
                .tamanoBytes((long) pdfBytes.length)
                .hashSha256(hashSha256)
                .categoria(CategoriaDocumento.COMPROBANTE)
                .clasificacionAcceso(ClasificacionAcceso.INTERNO)
                .descripcion(tipoDoc.getNombre() + " " + codigoDocumento + " por S/ " + pago.getImporte())
                .estado(EstadoDocumento.ACTIVO)
                .build();
        archivo = commonDocumentoRepository.save(archivo);

        doc.setHashDocumento(hashSha256);
        doc.setArchivoDocumento(archivo);
        doc.setNombreArchivo(codigoDocumento + ".pdf");

        doc = documentoRepository.save(doc);

        // Registrar evento de auditoría
        registrarEvento(doc, TipoEventoDocumentoPago.EMISION,
                "Emisión de documento " + codigoDocumento + " por importe S/ " + pago.getImporte(),
                ip, userAgent, emisor != null ? emisor.getId() : null);

        log.info("Documento tributario emitido exitosamente: {} para pago {}", codigoDocumento, pago.getCodigoPago());
        return mapToResponse(doc);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentoPagoResponse obtenerPorId(UUID id) {
        DocumentoPagoTributario doc = documentoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Documento tributario no encontrado: " + id));
        return mapToResponse(doc);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentoPagoResponse obtenerPorCodigo(String codigoDocumento) {
        DocumentoPagoTributario doc = documentoRepository.findByCodigoDocumento(codigoDocumento)
                .orElseThrow(() -> new BusinessException("Documento tributario no encontrado: " + codigoDocumento));
        return mapToResponse(doc);
    }

    @Override
    @Transactional
    public byte[] obtenerContenidoPdf(UUID id, CustomUserDetails user, String ip, String userAgent) {
        DocumentoPagoTributario doc = documentoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Documento tributario no encontrado: " + id));

        byte[] contenido = null;
        if (doc.getArchivoDocumento() != null && doc.getArchivoDocumento().getNombreInterno() != null) {
            try (InputStream is = storageService.read(doc.getArchivoDocumento().getNombreInterno())) {
                contenido = is.readAllBytes();
            } catch (Exception e) {
                log.warn("No se pudo leer el archivo físico de {}: {}. Regenerando PDF...", doc.getCodigoDocumento(), e.getMessage());
            }
        }

        if (contenido == null) {
            List<AplicacionPagoTributario> aplicaciones = aplicacionPagoRepository.findByPagoIdAndEstado(
                    doc.getPagoTributario().getId(), EstadoAplicacionPago.ACTIVA);
            String fullUrl = baseUrl.replaceAll("/+$", "") + doc.getUrlVerificacion();
            contenido = pdfDocumentoService.generarPdfDocumento(doc, aplicaciones, fullUrl);
        }

        registrarEvento(doc, TipoEventoDocumentoPago.DESCARGA,
                "Descarga de archivo PDF del documento " + doc.getCodigoDocumento(),
                ip, userAgent, user != null ? user.getId() : null);

        return contenido;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] obtenerQrPng(UUID id) {
        DocumentoPagoTributario doc = documentoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Documento tributario no encontrado: " + id));
        String fullUrl = baseUrl.replaceAll("/+$", "") + doc.getUrlVerificacion();
        return qrCodeGenerator.generateQrCodePng(fullUrl, 200, 200);
    }

    @Override
    @Transactional
    public DocumentoPagoResponse registrarReimpresion(UUID id, CustomUserDetails user, String ip, String userAgent) {
        DocumentoPagoTributario doc = documentoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Documento tributario no encontrado: " + id));

        if (doc.getEstado() == EstadoDocumentoPagoTributario.ANULADO) {
            throw new BusinessException("No se puede reimprimir un documento anulado");
        }

        doc.setNumeroReimpresiones(doc.getNumeroReimpresiones() + 1);
        doc = documentoRepository.save(doc);

        registrarEvento(doc, TipoEventoDocumentoPago.REIMPRESION,
                "Reimpresión autorizada de copia N° " + doc.getNumeroReimpresiones() + " del documento " + doc.getCodigoDocumento(),
                ip, userAgent, user != null ? user.getId() : null);

        log.info("Reimpresión registrada para documento {}. Total reimpresiones: {}", doc.getCodigoDocumento(), doc.getNumeroReimpresiones());
        return mapToResponse(doc);
    }

    @Override
    @Transactional
    public DocumentoPagoResponse anularDocumento(UUID id, AnularDocumentoRequest request, CustomUserDetails user, String ip, String userAgent) {
        DocumentoPagoTributario doc = documentoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Documento tributario no encontrado: " + id));

        documentoValidator.validarParaAnulacion(doc, request.getMotivo());

        Usuario usuario = null;
        if (user != null && user.getId() != null) {
            usuario = usuarioRepository.findById(user.getId()).orElse(null);
        }

        doc.setEstado(EstadoDocumentoPagoTributario.ANULADO);
        doc.setMotivoAnulacion(request.getMotivo());
        doc.setFechaAnulacion(OffsetDateTime.now(ZONA_LIMA));
        doc.setUsuarioAnulacion(usuario);

        doc = documentoRepository.save(doc);

        registrarEvento(doc, TipoEventoDocumentoPago.ANULACION,
                "Anulación documental: " + request.getMotivo(),
                ip, userAgent, usuario != null ? usuario.getId() : null);

        log.warn("Documento tributario {} fue anulado por usuario {}", doc.getCodigoDocumento(), user != null ? user.getUsername() : "ANÓNIMO");
        return mapToResponse(doc);
    }

    @Override
    @Transactional
    public DocumentoPagoResponse sustituirDocumento(UUID id, SustituirDocumentoRequest request, CustomUserDetails user, String ip, String userAgent) {
        DocumentoPagoTributario docOriginal = documentoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Documento original no encontrado: " + id));

        documentoValidator.validarParaSustitucion(docOriginal, request.getMotivo());

        Usuario usuario = null;
        if (user != null && user.getId() != null) {
            usuario = usuarioRepository.findById(user.getId()).orElse(null);
        }

        // Marcar original como SUSTITUIDO
        docOriginal.setEstado(EstadoDocumentoPagoTributario.SUSTITUIDO);
        docOriginal.setMotivoAnulacion("Sustituido: " + request.getMotivo());
        docOriginal.setFechaAnulacion(OffsetDateTime.now(ZONA_LIMA));
        docOriginal.setUsuarioAnulacion(usuario);

        // Emitir nuevo documento para el mismo pago
        EmitirDocumentoRequest emitirReq = EmitirDocumentoRequest.builder()
                .pagoId(docOriginal.getPagoTributario().getId())
                .tipoDocumentoCodigo(docOriginal.getTipoDocumento().getCodigo())
                .serieCodigo(request.getSerieCodigo() != null ? request.getSerieCodigo() : docOriginal.getSerie().getCodigoSerie())
                .idempotencyKey(UUID.randomUUID().toString())
                .build();

        // Para evitar colisión con el original que ahora pasa a SUSTITUIDO, guardamos el original primero
        docOriginal = documentoRepository.save(docOriginal);

        DocumentoPagoResponse nuevoDocResp = emitirDocumento(emitirReq, user, ip, userAgent);
        DocumentoPagoTributario nuevoDoc = documentoRepository.findById(nuevoDocResp.getId())
                .orElseThrow(() -> new BusinessException("Error al recuperar documento sustitutorio emitido"));

        docOriginal.setDocumentoSustitutorio(nuevoDoc);
        documentoRepository.save(docOriginal);

        registrarEvento(docOriginal, TipoEventoDocumentoPago.SUSTITUCION,
                "Documento sustituido por " + nuevoDoc.getCodigoDocumento() + ". Motivo: " + request.getMotivo(),
                ip, userAgent, usuario != null ? usuario.getId() : null);

        log.info("Documento {} fue sustituido por {}", docOriginal.getCodigoDocumento(), nuevoDoc.getCodigoDocumento());
        return mapToResponse(nuevoDoc);
    }

    @Override
    @Transactional
    public VerificacionPublicaDocumentoResponse verificarPublicamente(String codigoVerificacionOQr, String ip, String userAgent) {
        if (codigoVerificacionOQr == null || codigoVerificacionOQr.trim().isEmpty()) {
            return VerificacionPublicaDocumentoResponse.builder()
                    .existe(false)
                    .valido(false)
                    .mensajeResultado("El código de verificación documental proporcionado está vacío")
                    .build();
        }

        String codigoLimpio = codigoVerificacionOQr.trim();

        // Buscar por codigoVerificacion o por codigoDocumento
        var docOpt = documentoRepository.findByCodigoVerificacion(codigoLimpio);
        if (docOpt.isEmpty()) {
            docOpt = documentoRepository.findByCodigoDocumento(codigoLimpio);
        }

        if (docOpt.isEmpty()) {
            return VerificacionPublicaDocumentoResponse.builder()
                    .existe(false)
                    .valido(false)
                    .mensajeResultado("No se encontró ningún documento tributario emitido con el código: " + codigoLimpio)
                    .build();
        }

        DocumentoPagoTributario doc = docOpt.get();

        // Minimización de datos personales: nombre enmascarado
        String nombreEnmascarado = enmascararNombre(doc.getContribuyente().getNombreRazonSocial());

        // Conceptos pagados
        List<String> conceptos = new ArrayList<>();
        List<AplicacionPagoTributario> aplicaciones = aplicacionPagoRepository.findByPagoIdAndEstado(
                doc.getPagoTributario().getId(), EstadoAplicacionPago.ACTIVA);

        for (AplicacionPagoTributario apl : aplicaciones) {
            var obl = apl.getObligacion();
            String cNombre = obl != null && obl.getConcepto() != null ? obl.getConcepto().getNombre() : "Tributo";
            String cPeriodo = obl != null && obl.getPeriodo() != null ? String.valueOf(obl.getPeriodo().getAnio()) : "";
            conceptos.add(cNombre + (cPeriodo.isEmpty() ? "" : " - " + cPeriodo) + ": S/ " + apl.getImporteAplicado());
        }

        boolean esValido = (doc.getEstado() == EstadoDocumentoPagoTributario.EMITIDO);
        String mensaje = esValido
                ? "Documento VÁLIDO y auténtico emitido por la Municipalidad Distrital de Quinua."
                : "ATENCIÓN: Este documento se encuentra en estado " + doc.getEstado().name() +
                (doc.getMotivoAnulacion() != null ? " (" + doc.getMotivoAnulacion() + ")" : "");

        registrarEvento(doc, TipoEventoDocumentoPago.VERIFICACION_PUBLICA,
                "Verificación pública realizada desde IP: " + (ip != null ? ip : "Desconocida"),
                ip, userAgent, null);

        return VerificacionPublicaDocumentoResponse.builder()
                .existe(true)
                .valido(esValido)
                .codigoDocumento(doc.getCodigoDocumento())
                .tipoDocumentoNombre(doc.getTipoDocumento().getNombre())
                .fechaEmision(doc.getFechaEmision())
                .contribuyenteEnmascarado(nombreEnmascarado)
                .importeTotal(doc.getImporteTotal())
                .moneda(doc.getMoneda())
                .estado(doc.getEstado())
                .hashDocumento(doc.getHashDocumento())
                .codigoVerificacion(doc.getCodigoVerificacion())
                .conceptosPagados(conceptos)
                .mensajeResultado(mensaje)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentoPagoResponse> listarDocumentos(
            String query, UUID tipoDocId, UUID serieId, UUID contribuyenteId, UUID pagoId,
            EstadoDocumentoPagoTributario estado, LocalDate desde, LocalDate hasta, Pageable pageable) {

        var spec = DocumentoSpecification.conFiltros(query, tipoDocId, serieId, contribuyenteId, pagoId, estado, desde, hasta);
        return documentoRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialDocumentoResponse> listarHistorial(UUID documentoId) {
        return historialRepository.findByDocumentoIdOrderByFechaEventoDesc(documentoId).stream()
                .map(h -> {
                    String uNombre = "Público / Ciudadano";
                    if (h.getUsuarioId() != null) {
                        uNombre = usuarioRepository.findById(h.getUsuarioId())
                                .map(Usuario::getNombreCompleto)
                                .orElse("Usuario ID: " + h.getUsuarioId());
                    }
                    return HistorialDocumentoResponse.builder()
                            .id(h.getId())
                            .tipoEvento(h.getTipoEvento().name())
                            .descripcion(h.getDescripcion())
                            .ipOrigen(h.getIpOrigen())
                            .userAgent(h.getUserAgent())
                            .usuarioNombre(uNombre)
                            .fechaEvento(h.getFechaEvento())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardDocumentalResponse obtenerMetricas() {
        long emitidos = documentoRepository.countByEstado(EstadoDocumentoPagoTributario.EMITIDO);
        long anulados = documentoRepository.countByEstado(EstadoDocumentoPagoTributario.ANULADO);
        long sustituidos = documentoRepository.countByEstado(EstadoDocumentoPagoTributario.SUSTITUIDO);

        OffsetDateTime inicioMes = LocalDate.now().withDayOfMonth(1).atStartOfDay(ZONA_LIMA).toOffsetDateTime();
        OffsetDateTime ahora = OffsetDateTime.now(ZONA_LIMA);
        long periodo = documentoRepository.countByPeriodo(inicioMes, ahora);

        var tipoRecibo = tipoDocumentoRepository.findByCodigo("RECIBO_PAGO");
        var tipoConst = tipoDocumentoRepository.findByCodigo("CONSTANCIA_PAGO");

        long recs = tipoRecibo.map(t -> documentoRepository.count((root, cq, cb) ->
                cb.equal(root.get("tipoDocumento").get("id"), t.getId()))).orElse(0L);
        long consts = tipoConst.map(t -> documentoRepository.count((root, cq, cb) ->
                cb.equal(root.get("tipoDocumento").get("id"), t.getId()))).orElse(0L);

        return DashboardDocumentalResponse.builder()
                .totalEmitidos(emitidos)
                .totalAnulados(anulados)
                .totalSustituidos(sustituidos)
                .totalPeriodo(periodo)
                .recibosEmitidos(recs)
                .constanciasEmitidas(consts)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoDocumentoPagoTributario> listarTiposDocumento() {
        return tipoDocumentoRepository.findByEstado("ACTIVO");
    }

    @Override
    @Transactional(readOnly = true)
    public List<SerieDocumentoPagoTributario> listarSeriesPorTipo(UUID tipoId) {
        return serieRepository.findByTipoDocumentoId(tipoId);
    }

    private void registrarEvento(DocumentoPagoTributario doc, TipoEventoDocumentoPago tipo, String descripcion, String ip, String userAgent, Long usuarioId) {
        HistorialDocumentoPagoTributario hist = HistorialDocumentoPagoTributario.builder()
                .documento(doc)
                .tipoEvento(tipo)
                .descripcion(descripcion)
                .ipOrigen(ip)
                .userAgent(userAgent)
                .usuarioId(usuarioId)
                .fechaEvento(OffsetDateTime.now(ZONA_LIMA))
                .build();
        historialRepository.save(hist);
    }

    private String enmascararNombre(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) {
            return "CONTRIBUYENTE ANÓNIMO";
        }
        String[] partes = nombreCompleto.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < partes.length; i++) {
            String p = partes[i];
            if (p.length() <= 2) {
                sb.append(p.charAt(0)).append("*");
            } else {
                sb.append(p.charAt(0))
                  .append("*".repeat(p.length() - 1));
            }
            if (i < partes.length - 1) sb.append(" ");
        }
        return sb.toString();
    }

    private DocumentoPagoResponse mapToResponse(DocumentoPagoTributario d) {
        String emisor = d.getUsuarioEmisor() != null ? d.getUsuarioEmisor().getNombreCompleto() : "Sistema";
        String anulador = d.getUsuarioAnulacion() != null ? d.getUsuarioAnulacion().getNombreCompleto() : null;
        String oficina = d.getOficinaEmisora() != null ? d.getOficinaEmisora().getNombre() : "Rentas";
        String sustitutorioCod = d.getDocumentoSustitutorio() != null ? d.getDocumentoSustitutorio().getCodigoDocumento() : null;
        UUID sustitutorioId = d.getDocumentoSustitutorio() != null ? d.getDocumentoSustitutorio().getId() : null;

        return DocumentoPagoResponse.builder()
                .id(d.getId())
                .codigoDocumento(d.getCodigoDocumento())
                .tipoDocumentoCodigo(d.getTipoDocumento().getCodigo())
                .tipoDocumentoNombre(d.getTipoDocumento().getNombre())
                .serieCodigo(d.getSerie().getCodigoSerie())
                .numeroCorrelativo(d.getNumeroCorrelativo())
                .pagoId(d.getPagoTributario().getId())
                .codigoPago(d.getPagoTributario().getCodigoPago())
                .medioPago(d.getPagoTributario().getMedioPago() != null ? d.getPagoTributario().getMedioPago().name() : "EFECTIVO")
                .contribuyenteId(d.getContribuyente().getId())
                .codigoContribuyente(d.getContribuyente().getCodigoContribuyente())
                .nombreContribuyente(d.getContribuyente().getNombreRazonSocial())
                .tipoDocumentoContribuyente(d.getContribuyente().getTipoDocumento())
                .numeroDocumentoContribuyente(d.getContribuyente().getNumeroDocumento())
                .fechaEmision(d.getFechaEmision())
                .importeTotal(d.getImporteTotal())
                .moneda(d.getMoneda())
                .estado(d.getEstado())
                .usuarioEmisorNombre(emisor)
                .oficinaEmisoraNombre(oficina)
                .nombreArchivo(d.getNombreArchivo())
                .hashDocumento(d.getHashDocumento())
                .codigoVerificacion(d.getCodigoVerificacion())
                .urlVerificacion(d.getUrlVerificacion())
                .motivoAnulacion(d.getMotivoAnulacion())
                .fechaAnulacion(d.getFechaAnulacion())
                .usuarioAnulacionNombre(anulador)
                .documentoSustitutorioId(sustitutorioId)
                .documentoSustitutorioCodigo(sustitutorioCod)
                .numeroReimpresiones(d.getNumeroReimpresiones())
                .build();
    }
}
