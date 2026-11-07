package pe.gob.municipalidad.sigm.modules.mercados.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.mercados.dto.DocumentoEstadisticasResponse;
import pe.gob.municipalidad.sigm.modules.mercados.dto.VigenciaResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.AutorizacionComercial;
import pe.gob.municipalidad.sigm.modules.mercados.entity.ContratoOcupacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoAutorizacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoContrato;
import pe.gob.municipalidad.sigm.modules.mercados.repository.AutorizacionComercialRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.ContratoOcupacionRepository;
import pe.gob.municipalidad.sigm.modules.notificaciones.service.NotificacionService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class VigenciaOcupacionService {

    private final ContratoOcupacionRepository contratoRepository;
    private final AutorizacionComercialRepository autorizacionRepository;
    private final NotificacionService notificacionService;

    public VigenciaOcupacionService(
            ContratoOcupacionRepository contratoRepository,
            AutorizacionComercialRepository autorizacionRepository,
            NotificacionService notificacionService) {
        this.contratoRepository = contratoRepository;
        this.autorizacionRepository = autorizacionRepository;
        this.notificacionService = notificacionService;
    }

    @Transactional(readOnly = true)
    public List<VigenciaResponse> consultarProximosVencimientos(int diasAnticipacion) {
        LocalDate hoy = LocalDate.now();
        LocalDate fechaLimite = hoy.plusDays(diasAnticipacion > 0 ? diasAnticipacion : 30);

        List<VigenciaResponse> resultados = new ArrayList<>();

        // Contratos próximos a vencer
        List<ContratoOcupacion> contratos = contratoRepository.consultarProximosVencimientos(hoy, fechaLimite);
        for (ContratoOcupacion c : contratos) {
            long dias = ChronoUnit.DAYS.between(hoy, c.getFechaFin());
            String nivel = dias <= 7 ? "CRITICO_7_DIAS" : "ALERTA_30_DIAS";
            resultados.add(new VigenciaResponse(
                    c.getId(),
                    "CONTRATO",
                    c.getCodigoContrato(),
                    c.getComerciante() != null ? c.getComerciante().getNombreOTitular() : "-",
                    c.getComerciante() != null ? c.getComerciante().getNumeroDocumentoTitular() : "-",
                    c.getPuesto() != null ? c.getPuesto().getCodigo() : "-",
                    c.getPuesto() != null ? c.getPuesto().getNumero() : "-",
                    c.getPuesto() != null && c.getPuesto().getMercado() != null ? c.getPuesto().getMercado().getNombre() : "-",
                    c.getFechaInicio(),
                    c.getFechaFin(),
                    dias,
                    c.getEstado().name(),
                    nivel
            ));
        }

        // Autorizaciones próximas a vencer
        List<AutorizacionComercial> autorizaciones = autorizacionRepository.consultarProximosVencimientos(hoy, fechaLimite);
        for (AutorizacionComercial a : autorizaciones) {
            long dias = ChronoUnit.DAYS.between(hoy, a.getFechaFin());
            String nivel = dias <= 7 ? "CRITICO_7_DIAS" : "ALERTA_30_DIAS";
            resultados.add(new VigenciaResponse(
                    a.getId(),
                    "AUTORIZACION",
                    a.getCodigoAutorizacion(),
                    a.getComerciante() != null ? a.getComerciante().getNombreOTitular() : "-",
                    a.getComerciante() != null ? a.getComerciante().getNumeroDocumentoTitular() : "-",
                    a.getPuesto() != null ? a.getPuesto().getCodigo() : "-",
                    a.getPuesto() != null ? a.getPuesto().getNumero() : "-",
                    a.getPuesto() != null && a.getPuesto().getMercado() != null ? a.getPuesto().getMercado().getNombre() : "-",
                    a.getFechaInicio(),
                    a.getFechaFin(),
                    dias,
                    a.getEstado().name(),
                    nivel
            ));
        }

        return resultados.stream()
                .sorted(Comparator.comparing(VigenciaResponse::getDiasRestantes))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VigenciaResponse> consultarVencidos() {
        LocalDate hoy = LocalDate.now();
        List<VigenciaResponse> resultados = new ArrayList<>();

        List<ContratoOcupacion> contratos = contratoRepository.consultarVencidos(hoy);
        for (ContratoOcupacion c : contratos) {
            long dias = ChronoUnit.DAYS.between(hoy, c.getFechaFin());
            resultados.add(new VigenciaResponse(
                    c.getId(),
                    "CONTRATO",
                    c.getCodigoContrato(),
                    c.getComerciante() != null ? c.getComerciante().getNombreOTitular() : "-",
                    c.getComerciante() != null ? c.getComerciante().getNumeroDocumentoTitular() : "-",
                    c.getPuesto() != null ? c.getPuesto().getCodigo() : "-",
                    c.getPuesto() != null ? c.getPuesto().getNumero() : "-",
                    c.getPuesto() != null && c.getPuesto().getMercado() != null ? c.getPuesto().getMercado().getNombre() : "-",
                    c.getFechaInicio(),
                    c.getFechaFin(),
                    dias,
                    c.getEstado().name(),
                    "VENCIDO"
            ));
        }

        List<AutorizacionComercial> autorizaciones = autorizacionRepository.consultarVencidos(hoy);
        for (AutorizacionComercial a : autorizaciones) {
            long dias = ChronoUnit.DAYS.between(hoy, a.getFechaFin());
            resultados.add(new VigenciaResponse(
                    a.getId(),
                    "AUTORIZACION",
                    a.getCodigoAutorizacion(),
                    a.getComerciante() != null ? a.getComerciante().getNombreOTitular() : "-",
                    a.getComerciante() != null ? a.getComerciante().getNumeroDocumentoTitular() : "-",
                    a.getPuesto() != null ? a.getPuesto().getCodigo() : "-",
                    a.getPuesto() != null ? a.getPuesto().getNumero() : "-",
                    a.getPuesto() != null && a.getPuesto().getMercado() != null ? a.getPuesto().getMercado().getNombre() : "-",
                    a.getFechaInicio(),
                    a.getFechaFin(),
                    dias,
                    a.getEstado().name(),
                    "VENCIDO"
            ));
        }

        return resultados.stream()
                .sorted(Comparator.comparing(VigenciaResponse::getDiasRestantes))
                .collect(Collectors.toList());
    }

    @Transactional
    public int emitirAlertasVencimiento(String username) {
        List<VigenciaResponse> porVencer = consultarProximosVencimientos(30);
        int emitidas = 0;

        for (VigenciaResponse v : porVencer) {
            if (v.getDiasRestantes() != null && v.getDiasRestantes() <= 15) {
                String titulo = "Vencimiento Próximo: " + v.getCodigo();
                String mensaje = String.format("El documento %s (%s) del comerciante %s vence en %d días (Fecha: %s).",
                        v.getCodigo(), v.getTipoDocumento(), v.getComercianteNombre(), v.getDiasRestantes(), v.getFechaFin());
                String url = v.getTipoDocumento().equals("CONTRATO") ?
                        "/dashboard/mercados/contratos/" + v.getId() :
                        "/dashboard/mercados/autorizaciones/" + v.getId();

                notificacionService.crearNotificacion(username != null ? username : "admin",
                        titulo, mensaje, "ALERTA_VENCIMIENTO", url, "MERCADOS_VIGENCIA");
                emitidas++;
            }
        }
        return emitidas;
    }

    @Transactional(readOnly = true)
    public DocumentoEstadisticasResponse obtenerEstadisticas() {
        long cRegistrados = contratoRepository.count();
        long cVigentes = contratoRepository.countByEstado(EstadoContrato.VIGENTE);
        long cVencidos = contratoRepository.countByEstado(EstadoContrato.VENCIDO);
        long cSuspendidos = contratoRepository.countByEstado(EstadoContrato.SUSPENDIDO);
        long cRevision = contratoRepository.countByEstado(EstadoContrato.EN_REVISION);

        long aRegistradas = autorizacionRepository.count();
        long aVigentes = autorizacionRepository.countByEstado(EstadoAutorizacion.VIGENTE) +
                         autorizacionRepository.countByEstado(EstadoAutorizacion.EMITIDA);
        long aVencidas = autorizacionRepository.countByEstado(EstadoAutorizacion.VENCIDA);
        long aRevocadas = autorizacionRepository.countByEstado(EstadoAutorizacion.REVOCADA);

        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(30);
        long aProximasVencer = autorizacionRepository.consultarProximosVencimientos(hoy, limite).size();

        long total = cRegistrados + aRegistradas;

        return new DocumentoEstadisticasResponse(
                cRegistrados, cVigentes, cVencidos, cSuspendidos, cRevision,
                aRegistradas, aVigentes, aProximasVencer, aVencidas, aRevocadas, total);
    }
}
