package pe.gob.municipalidad.sigm.modules.mercados.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.mercados.dto.VerificacionDocumentoResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.AutorizacionComercial;
import pe.gob.municipalidad.sigm.modules.mercados.entity.ContratoOcupacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoAutorizacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoContrato;
import pe.gob.municipalidad.sigm.modules.mercados.repository.AutorizacionComercialRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.ContratoOcupacionRepository;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class VerificacionDocumentoMercadoService {

    private final ContratoOcupacionRepository contratoRepository;
    private final AutorizacionComercialRepository autorizacionRepository;

    public VerificacionDocumentoMercadoService(
            ContratoOcupacionRepository contratoRepository,
            AutorizacionComercialRepository autorizacionRepository) {
        this.contratoRepository = contratoRepository;
        this.autorizacionRepository = autorizacionRepository;
    }

    @Transactional(readOnly = true)
    public VerificacionDocumentoResponse verificarPorCodigoSeguro(String codigoSeguro) {
        if (codigoSeguro == null || codigoSeguro.trim().isEmpty()) {
            return respuestaNoValida("Código de verificación no especificado.");
        }

        // 1. Buscar en Contratos
        Optional<ContratoOcupacion> optContrato = contratoRepository.findByCodigoSeguroConRelaciones(codigoSeguro.trim());
        if (optContrato.isPresent()) {
            ContratoOcupacion c = optContrato.get();
            boolean vigente = c.getEstado() == EstadoContrato.VIGENTE &&
                    (c.getFechaFin() == null || !c.getFechaFin().isBefore(LocalDate.now()));

            String estadoDesc = vigente ? "DOCUMENTO OFICIAL AUTÉNTICO Y VIGENTE" :
                    "DOCUMENTO REGISTRADO EN ESTADO " + c.getEstado().name();

            return new VerificacionDocumentoResponse(
                    c.getCodigoContrato(),
                    "CONTRATO DE OCUPACIÓN",
                    c.getTipoContrato() != null ? c.getTipoContrato().getNombre() : c.getTipoContratoCodigo(),
                    estadoDesc,
                    vigente,
                    c.getFechaFormalizacion() != null ? c.getFechaFormalizacion().toLocalDate() : c.getFechaElaboracion(),
                    c.getFechaInicio(),
                    c.getFechaFin(),
                    c.getPuesto() != null && c.getPuesto().getMercado() != null ? c.getPuesto().getMercado().getNombre() : "-",
                    c.getPuesto() != null ? c.getPuesto().getCodigo() : "-",
                    enmascararTitular(c.getComerciante() != null ? c.getComerciante().getNombreOTitular() : "-"),
                    c.getOficinaResponsable() != null ? c.getOficinaResponsable().getNombre() : "Subgerencia de Comercialización",
                    c.getHashDocumento(),
                    vigente ? "Documento formal emitido válidamente por la Municipalidad Distrital de Quinua." :
                            "El documento no se encuentra en estado VIGENTE en el registro oficial."
            );
        }

        // 2. Buscar en Autorizaciones
        Optional<AutorizacionComercial> optAut = autorizacionRepository.findByCodigoSeguroConRelaciones(codigoSeguro.trim());
        if (optAut.isPresent()) {
            AutorizacionComercial a = optAut.get();
            boolean vigente = (a.getEstado() == EstadoAutorizacion.VIGENTE || a.getEstado() == EstadoAutorizacion.EMITIDA) &&
                    (a.getFechaFin() == null || !a.getFechaFin().isBefore(LocalDate.now()));

            String estadoDesc = vigente ? "AUTORIZACIÓN COMERCIAL AUTÉNTICA Y VIGENTE" :
                    "AUTORIZACIÓN REGISTRADA EN ESTADO " + a.getEstado().name();

            return new VerificacionDocumentoResponse(
                    a.getCodigoAutorizacion(),
                    "AUTORIZACIÓN COMERCIAL MUNICIPAL",
                    a.getTipoAutorizacionEntity() != null ? a.getTipoAutorizacionEntity().getNombre() : a.getTipoAutorizacion(),
                    estadoDesc,
                    vigente,
                    a.getFechaEmision() != null ? a.getFechaEmision() : a.getFechaSolicitud(),
                    a.getFechaInicio(),
                    a.getFechaFin(),
                    a.getPuesto() != null && a.getPuesto().getMercado() != null ? a.getPuesto().getMercado().getNombre() : "-",
                    a.getPuesto() != null ? a.getPuesto().getCodigo() : "-",
                    enmascararTitular(a.getComerciante() != null ? a.getComerciante().getNombreOTitular() : "-"),
                    a.getOficinaEmisora() != null ? a.getOficinaEmisora().getNombre() : "Subgerencia de Comercialización",
                    a.getHashDocumento(),
                    vigente ? "Acto administrativo habilitante registrado válidamente en el padrón municipal de Quinua." :
                            "La autorización no se encuentra en estado activo en el padrón oficial."
            );
        }

        return respuestaNoValida("No se encontró ningún contrato o autorización con el código de verificación proporcionado.");
    }

    private VerificacionDocumentoResponse respuestaNoValida(String mensaje) {
        return new VerificacionDocumentoResponse(
                "-", "-", "-", "DOCUMENTO NO VÁLIDO O NO ENCONTRADO",
                false, null, null, null, "-", "-", "-", "-", null, mensaje);
    }

    private String enmascararTitular(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.isBlank() || nombreCompleto.equals("-")) {
            return "NO IDENTIFICADO";
        }
        String[] partes = nombreCompleto.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < partes.length; i++) {
            String p = partes[i];
            if (p.length() > 2) {
                sb.append(p.charAt(0)).append("***").append(p.charAt(p.length() - 1));
            } else {
                sb.append(p.charAt(0)).append("*");
            }
            if (i < partes.length - 1) sb.append(" ");
        }
        return sb.toString();
    }
}
