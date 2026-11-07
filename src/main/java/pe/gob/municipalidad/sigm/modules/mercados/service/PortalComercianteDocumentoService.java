package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.modules.mercados.dto.PortalComercianteDocumentoResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoAutorizacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoContrato;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPagoMercado;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PortalComercianteDocumentoService {

    private final ComerciantePortalContextService contextService;
    private final ContratoOcupacionRepository contratoRepository;
    private final AutorizacionComercialRepository autorizacionRepository;
    private final PagoMercadoRepository pagoRepository;
    private final PortalComercianteContratoService contratoPortalService;
    private final PortalComercianteAutorizacionService autorizacionPortalService;
    private final PortalComercianteFinanzasService finanzasPortalService;

    public PortalComercianteDocumentoService(
            ComerciantePortalContextService contextService,
            ContratoOcupacionRepository contratoRepository,
            AutorizacionComercialRepository autorizacionRepository,
            PagoMercadoRepository pagoRepository,
            PortalComercianteContratoService contratoPortalService,
            PortalComercianteAutorizacionService autorizacionPortalService,
            PortalComercianteFinanzasService finanzasPortalService) {
        this.contextService = contextService;
        this.contratoRepository = contratoRepository;
        this.autorizacionRepository = autorizacionRepository;
        this.pagoRepository = pagoRepository;
        this.contratoPortalService = contratoPortalService;
        this.autorizacionPortalService = autorizacionPortalService;
        this.finanzasPortalService = finanzasPortalService;
    }

    @Transactional(readOnly = true)
    public Page<PortalComercianteDocumentoResponse> listarMisDocumentos(String tipo, Pageable pageable) {
        Comerciante c = contextService.getComercianteActivo();
        List<PortalComercianteDocumentoResponse> docs = new ArrayList<>();

        // 1. Contratos formalizados o vigentes
        if (tipo == null || tipo.isBlank() || "CONTRATO".equalsIgnoreCase(tipo)) {
            List<ContratoOcupacion> contratos = contratoRepository.findByComercianteId(c.getId());
            for (ContratoOcupacion con : contratos) {
                if (con.getEstado() != EstadoContrato.BORRADOR) {
                    docs.add(PortalComercianteDocumentoResponse.builder()
                            .id("CONTRATO_" + con.getId())
                            .nombreDocumento("Contrato de Ocupación N° " + con.getCodigoContrato())
                            .tipoDocumento("CONTRATO")
                            .codigoReferencia(con.getCodigoContrato())
                            .puestoCodigo(con.getPuesto() != null ? con.getPuesto().getCodigo() : "-")
                            .mercadoNombre(con.getPuesto() != null && con.getPuesto().getMercado() != null ? con.getPuesto().getMercado().getNombre() : "-")
                            .fechaEmision(con.getFechaElaboracion() != null ? con.getFechaElaboracion() : con.getFechaInicio())
                            .estado(con.getEstado().name())
                            .urlDescarga("/api/portal/comerciante/documentos/contrato/" + con.getId() + "/descargar")
                            .urlVerificacion(null)
                            .extension("PDF")
                            .build());
                }
            }
        }

        // 2. Autorizaciones comerciales
        if (tipo == null || tipo.isBlank() || "AUTORIZACION".equalsIgnoreCase(tipo)) {
            List<AutorizacionComercial> autorizaciones = autorizacionRepository.findByComercianteId(c.getId());
            for (AutorizacionComercial aut : autorizaciones) {
                if (aut.getEstado() == EstadoAutorizacion.VIGENTE || aut.getEstado() == EstadoAutorizacion.EMITIDA || aut.getEstado() == EstadoAutorizacion.VENCIDA) {
                    docs.add(PortalComercianteDocumentoResponse.builder()
                            .id("AUTORIZACION_" + aut.getId())
                            .nombreDocumento("Autorización Municipal N° " + aut.getCodigoAutorizacion())
                            .tipoDocumento("AUTORIZACION")
                            .codigoReferencia(aut.getCodigoAutorizacion())
                            .puestoCodigo(aut.getPuesto() != null ? aut.getPuesto().getCodigo() : "-")
                            .mercadoNombre(aut.getPuesto() != null && aut.getPuesto().getMercado() != null ? aut.getPuesto().getMercado().getNombre() : "-")
                            .fechaEmision(aut.getFechaEmision())
                            .estado(aut.getEstado().name())
                            .urlDescarga("/api/portal/comerciante/documentos/autorizacion/" + aut.getId() + "/descargar")
                            .urlVerificacion("/verificar/documento?codigo=" + aut.getCodigoSeguro())
                            .extension("PDF")
                            .build());
                }
            }
        }

        // 3. Constancias de pago
        if (tipo == null || tipo.isBlank() || "PAGO".equalsIgnoreCase(tipo)) {
            List<PagoMercado> pagos = pagoRepository.findByComercianteId(c.getId());
            for (PagoMercado p : pagos) {
                if (p.getEstado() == EstadoPagoMercado.CONFIRMADO) {
                    docs.add(PortalComercianteDocumentoResponse.builder()
                            .id("PAGO_" + p.getId())
                            .nombreDocumento("Recibo de Pago de Caja N° " + p.getCodigoPago())
                            .tipoDocumento("PAGO")
                            .codigoReferencia(p.getCodigoPago())
                            .puestoCodigo("-")
                            .mercadoNombre("-")
                            .fechaEmision(p.getFechaPago().toLocalDate())
                            .estado(p.getEstado().name())
                            .urlDescarga("/api/portal/comerciante/documentos/pago/" + p.getId() + "/descargar")
                            .urlVerificacion(null)
                            .extension("PDF")
                            .build());
                }
            }
        }

        docs.sort(Comparator.comparing(PortalComercianteDocumentoResponse::getFechaEmision, Comparator.nullsLast(Comparator.reverseOrder())));

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), docs.size());
        List<PortalComercianteDocumentoResponse> paged = start <= end ? docs.subList(start, end) : List.of();

        return new PageImpl<>(paged, pageable, docs.size());
    }

    @Transactional(readOnly = true)
    public DocumentoDescarga descargarDocumento(String tipo, UUID id) {
        if ("contrato".equalsIgnoreCase(tipo)) {
            byte[] bytes = contratoPortalService.descargarContratoPdf(id);
            return DocumentoDescarga.builder()
                    .contenido(bytes)
                    .nombreArchivo("contrato_" + id + ".pdf")
                    .contentType("application/pdf")
                    .build();
        } else if ("autorizacion".equalsIgnoreCase(tipo)) {
            byte[] bytes = autorizacionPortalService.descargarAutorizacionPdf(id);
            return DocumentoDescarga.builder()
                    .contenido(bytes)
                    .nombreArchivo("autorizacion_" + id + ".pdf")
                    .contentType("application/pdf")
                    .build();
        } else if ("pago".equalsIgnoreCase(tipo)) {
            byte[] bytes = finanzasPortalService.descargarConstanciaPagoPdf(id);
            return DocumentoDescarga.builder()
                    .contenido(bytes)
                    .nombreArchivo("recibo_pago_" + id + ".pdf")
                    .contentType("application/pdf")
                    .build();
        } else {
            throw new BusinessException("Tipo de documento no soportado para descarga: " + tipo);
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentoDescarga {
        private byte[] contenido;
        private String nombreArchivo;
        private String contentType;
    }
}
