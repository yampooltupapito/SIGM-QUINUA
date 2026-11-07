package pe.gob.municipalidad.sigm.modules.mesapartes.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.SeguimientoDtos.ConsultaPublicaRequest;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.SeguimientoDtos.ConsultaPublicaResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.SeguimientoDtos.EtapaProgresoDto;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.CodigoSeguimiento;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.ConsultaSeguimientoAuditoria;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.Expediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.ResultadoConsultaSeguimiento;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoConsultaSeguimiento;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ConsultaSeguimientoAuditoriaRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteHistorialRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.CodigoSeguimientoService;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.ConsultaPublicaExpedienteService;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ConsultaPublicaExpedienteServiceImpl implements ConsultaPublicaExpedienteService {

    private static final int MAX_CONSULTAS_VENTANA = 30; // máx 30 consultas por IP en 5 minutos
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final CodigoSeguimientoService codigoSeguimientoService;
    private final ExpedienteRepository expedienteRepository;
    private final ExpedienteHistorialRepository historialRepository;
    private final ConsultaSeguimientoAuditoriaRepository auditoriaRepository;

    public ConsultaPublicaExpedienteServiceImpl(CodigoSeguimientoService codigoSeguimientoService,
                                               ExpedienteRepository expedienteRepository,
                                               ExpedienteHistorialRepository historialRepository,
                                               ConsultaSeguimientoAuditoriaRepository auditoriaRepository) {
        this.codigoSeguimientoService = codigoSeguimientoService;
        this.expedienteRepository = expedienteRepository;
        this.historialRepository = historialRepository;
        this.auditoriaRepository = auditoriaRepository;
    }

    @Override
    @Transactional
    public ConsultaPublicaResponse consultarPublicamente(ConsultaPublicaRequest request, String ipOrigen, String userAgent) {
        long inicioMs = System.currentTimeMillis();

        if (request == null || request.getCodigoSeguimiento() == null || request.getCodigoSeguimiento().trim().isBlank()) {
            throw new BusinessException("Debe ingresar un código de seguimiento válido.");
        }

        String codigo = request.getCodigoSeguimiento().trim().toUpperCase();

        // 1. Control de límite de solicitudes (Rate Limiting)
        String ip = ipOrigen != null ? ipOrigen : "127.0.0.1";
        long consultasRecientes = auditoriaRepository.countByIpAndTipoAndFechaAfter(
                ip,
                TipoConsultaSeguimiento.SEGUIMIENTO_EXPEDIENTE,
                OffsetDateTime.now().minusMinutes(5)
        );

        if (consultasRecientes >= MAX_CONSULTAS_VENTANA) {
            registrarAuditoria(null, codigo, ip, userAgent, ResultadoConsultaSeguimiento.RATE_LIMITED, (int)(System.currentTimeMillis() - inicioMs));
            throw new BusinessException("Ha superado el límite de consultas permitidas. Por favor espere unos minutos antes de volver a intentarlo.");
        }

        // 2. Búsqueda y validación de código seguro
        CodigoSeguimiento cs;
        try {
            cs = codigoSeguimientoService.validarCodigoSeguimiento(codigo, ip);
        } catch (Exception ex) {
            registrarAuditoria(null, codigo, ip, userAgent, ResultadoConsultaSeguimiento.CODIGO_INVALIDO, (int)(System.currentTimeMillis() - inicioMs));
            throw ex;
        }

        Expediente exp = cs.getExpediente();
        if (exp == null) {
            registrarAuditoria(null, codigo, ip, userAgent, ResultadoConsultaSeguimiento.NO_ENCONTRADO, (int)(System.currentTimeMillis() - inicioMs));
            throw new BusinessException("No se encontró información asociada al código ingresado.");
        }

        registrarAuditoria(exp, codigo, ip, userAgent, ResultadoConsultaSeguimiento.EXITOSA, (int)(System.currentTimeMillis() - inicioMs));

        // 3. Obtener fecha de último movimiento
        OffsetDateTime ultimoMovimiento = historialRepository.findFirstByExpedienteIdOrderByFechaMovimientoDesc(exp.getId())
                .map(h -> h.getFechaMovimiento())
                .orElse(exp.getFechaPresentacion() != null ? exp.getFechaPresentacion() : exp.getCreatedAt());

        // 4. Mapear estado legible para el ciudadano
        String descCiudadana = traducirEstadoCiudadano(exp.getEstadoActual());
        int etapaActual = calcularNumeroEtapa(exp.getEstadoActual());
        List<EtapaProgresoDto> etapas = generarEtapasProgreso(exp);

        boolean esObs = exp.getEstadoActual() == EstadoExpediente.OBSERVADO;
        boolean esConc = exp.getEstadoActual() == EstadoExpediente.RESUELTO
                || exp.getEstadoActual() == EstadoExpediente.NOTIFICADO
                || exp.getEstadoActual() == EstadoExpediente.ARCHIVADO;

        String mensajeInformativo = generarMensajeInformativo(exp.getEstadoActual());

        return ConsultaPublicaResponse.builder()
                .encontrado(true)
                .numeroExpediente(exp.getNumeroExpediente() != null ? exp.getNumeroExpediente() : "EN TRÁMITE INICIAL")
                .tipoTramite(exp.getTipoTramite() != null ? exp.getTipoTramite().getNombre() : "Trámite General")
                .fechaPresentacion(exp.getFechaPresentacion())
                .estado(exp.getEstadoActual())
                .estadoDescripcionCiudadana(descCiudadana)
                .etapaProgreso(etapaActual)
                .etapas(etapas)
                .oficinaActual(exp.getOficinaResponsable() != null ? exp.getOficinaResponsable().getNombre() : "Mesa de Partes General")
                .fechaUltimoMovimiento(ultimoMovimiento)
                .esObservado(esObs)
                .esConcluido(esConc)
                .mensajeInformativo(mensajeInformativo)
                .build();
    }

    private void registrarAuditoria(Expediente exp, String codigo, String ip, String ua, ResultadoConsultaSeguimiento resultado, int tiempoMs) {
        try {
            ConsultaSeguimientoAuditoria aud = ConsultaSeguimientoAuditoria.builder()
                    .tipoConsulta(TipoConsultaSeguimiento.SEGUIMIENTO_EXPEDIENTE)
                    .criterioBusqueda(codigo)
                    .expediente(exp)
                    .ipOrigen(ip)
                    .userAgent(ua != null && ua.length() > 250 ? ua.substring(0, 250) : ua)
                    .resultado(resultado)
                    .fechaConsulta(OffsetDateTime.now())
                    .tiempoRespuestaMs(tiempoMs)
                    .build();
            auditoriaRepository.save(aud);
        } catch (Exception e) {
            log.warn("Error al registrar auditoría de consulta de seguimiento: {}", e.getMessage());
        }
    }

    public static String traducirEstadoCiudadano(EstadoExpediente estado) {
        if (estado == null) return "En registro preliminar";
        return switch (estado) {
            case BORRADOR -> "Borrador de solicitud no presentada";
            case PRESENTADO -> "Solicitud presentada en espera de recepción formal";
            case RECIBIDO -> "Solicitud recepcionada por Mesa de Partes";
            case EN_REVISION -> "En revisión y verificación de requisitos documentales";
            case OBSERVADO -> "Trámite observado (requiere subsanación del solicitante)";
            case EN_SUBSANACION -> "Subsanación presentada en proceso de verificación";
            case DERIVADO -> "Remitido a la unidad orgánica competente";
            case EN_ATENCION -> "En proceso de análisis técnico y sustanciación";
            case RESUELTO -> "Procedimiento resuelto (acto administrativo emitido)";
            case NOTIFICADO -> "Resultado notificado formalmente con acuse de recibo";
            case ARCHIVADO -> "Expediente concluido y remitido a archivo pasivo";
            case ANULADO -> "Expediente anulado administrativamente";
        };
    }

    public static int calcularNumeroEtapa(EstadoExpediente estado) {
        if (estado == null) return 1;
        return switch (estado) {
            case BORRADOR, PRESENTADO -> 1;
            case RECIBIDO -> 2;
            case EN_REVISION, OBSERVADO, EN_SUBSANACION -> 3;
            case DERIVADO, EN_ATENCION -> 4;
            case RESUELTO -> 5;
            case NOTIFICADO, ARCHIVADO -> 6;
            case ANULADO -> 0;
        };
    }

    public static List<EtapaProgresoDto> generarEtapasProgreso(Expediente exp) {
        List<EtapaProgresoDto> etapas = new ArrayList<>();
        int etapaActual = calcularNumeroEtapa(exp.getEstadoActual());
        boolean esObservado = exp.getEstadoActual() == EstadoExpediente.OBSERVADO;
        boolean esSubsanacion = exp.getEstadoActual() == EstadoExpediente.EN_SUBSANACION;

        // 1. Presentado
        etapas.add(EtapaProgresoDto.builder()
                .numeroEtapa(1)
                .nombre("Presentado")
                .estado(etapaActual > 1 ? "COMPLETADO" : (etapaActual == 1 ? "EN_CURSO" : "PENDIENTE"))
                .fecha(exp.getFechaPresentacion() != null ? exp.getFechaPresentacion().format(FORMATO_FECHA) : null)
                .descripcion("Solicitud ingresada formalmente a través de la Mesa de Partes Virtual")
                .build());

        // 2. Recibido
        etapas.add(EtapaProgresoDto.builder()
                .numeroEtapa(2)
                .nombre("Recepcionado")
                .estado(etapaActual > 2 ? "COMPLETADO" : (etapaActual == 2 ? "EN_CURSO" : "PENDIENTE"))
                .fecha(exp.getFechaRecepcion() != null ? exp.getFechaRecepcion().format(FORMATO_FECHA) : null)
                .descripcion("Recepción formal y conformidad preliminar por el operador de Mesa de Partes")
                .build());

        // 3. Revisión / Observaciones
        String estadoEtapa3 = "PENDIENTE";
        String descEtapa3 = "Revisión técnica de requisitos normativos y documentos anexos";
        if (esObservado) {
            estadoEtapa3 = "OBSERVADO";
            descEtapa3 = "Se registraron observaciones pendientes de subsanar por el administrado";
        } else if (esSubsanacion) {
            estadoEtapa3 = "EN_SUBSANACION";
            descEtapa3 = "Subsanación presentada por el solicitante en verificación";
        } else if (etapaActual > 3) {
            estadoEtapa3 = "COMPLETADO";
        } else if (etapaActual == 3) {
            estadoEtapa3 = "EN_CURSO";
        }

        etapas.add(EtapaProgresoDto.builder()
                .numeroEtapa(3)
                .nombre(esObservado ? "Observado" : (esSubsanacion ? "Subsanación" : "En Revisión"))
                .estado(estadoEtapa3)
                .descripcion(descEtapa3)
                .build());

        // 4. En Atención
        etapas.add(EtapaProgresoDto.builder()
                .numeroEtapa(4)
                .nombre("En Atención")
                .estado(etapaActual > 4 ? "COMPLETADO" : (etapaActual == 4 ? "EN_CURSO" : "PENDIENTE"))
                .fecha(exp.getFechaInicioAtencion() != null ? exp.getFechaInicioAtencion().format(FORMATO_FECHA) : null)
                .descripcion("Evaluación técnica o sustanciación por la unidad orgánica responsable")
                .build());

        // 5. Resuelto
        etapas.add(EtapaProgresoDto.builder()
                .numeroEtapa(5)
                .nombre("Resuelto")
                .estado(etapaActual > 5 ? "COMPLETADO" : (etapaActual == 5 ? "EN_CURSO" : "PENDIENTE"))
                .descripcion("Emisión formal del acto administrativo o resolución correspondiente")
                .build());

        // 6. Concluido y Notificado
        etapas.add(EtapaProgresoDto.builder()
                .numeroEtapa(6)
                .nombre("Concluido")
                .estado(etapaActual >= 6 ? "COMPLETADO" : "PENDIENTE")
                .fecha(exp.getFechaCierre() != null ? exp.getFechaCierre().format(FORMATO_FECHA) : null)
                .descripcion("Notificación formal practicada válidamente y cierre del procedimiento")
                .build());

        return etapas;
    }

    private String generarMensajeInformativo(EstadoExpediente estado) {
        if (estado == null) return "Su trámite se encuentra en preparación.";
        return switch (estado) {
            case BORRADOR -> "Su solicitud aún no ha sido enviada formalmente. Complete los datos y proceda con el envío.";
            case PRESENTADO -> "Su solicitud ha sido ingresada correctamente y se encuentra a la espera de la recepción formal.";
            case RECIBIDO -> "Su expediente fue recepcionado y derivado a la unidad orgánica correspondiente para su trámite.";
            case EN_REVISION -> "El personal municipal está verificando los requisitos y documentos anexos presentados.";
            case OBSERVADO -> "Se han registrado observaciones en su solicitud. Ingrese a 'Mis Solicitudes' para subsanar los requisitos.";
            case EN_SUBSANACION -> "Hemos recibido sus documentos de subsanación y están siendo verificados por el personal revisor.";
            case DERIVADO -> "El expediente está circulando hacia el despacho responsable de la atención final.";
            case EN_ATENCION -> "El área técnica se encuentra elaborando el informe y proyecto de resolución.";
            case RESUELTO -> "Se ha expedido el acto administrativo resolutivo. Se procederá con la notificación formal.";
            case NOTIFICADO -> "El resultado de su trámite ha sido notificado válidamente conforme a la Ley N° 27444.";
            case ARCHIVADO -> "El procedimiento administrativo se encuentra culminado y debidamente archivado.";
            case ANULADO -> "El trámite fue dejado sin efecto conforme a las disposiciones legales aplicables.";
        };
    }
}
