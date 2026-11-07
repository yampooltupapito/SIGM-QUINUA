package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.document.repository.DocumentoRepository;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.administracion.service.AuditoriaAdministrativaService;
import pe.gob.municipalidad.sigm.modules.mercados.dto.FormalizacionGestionRequest;
import pe.gob.municipalidad.sigm.modules.mercados.dto.SolicitudGestionOcupacionResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.*;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Year;
import java.util.UUID;

@Slf4j
@Service
public class FormalizacionGestionOcupacionService {

    private final SolicitudGestionOcupacionRepository solicitudRepository;
    private final AsignacionPuestoRepository asignacionRepository;
    private final PuestoComercialRepository puestoRepository;
    private final DocumentoRepository documentoRepository;
    private final HistorialTitularidadService historialTitularidadService;
    private final HistorialOcupacionService historialOcupacionService;
    private final AuditoriaAdministrativaService auditoriaService;
    private final RenovacionOcupacionService renovacionService;

    public FormalizacionGestionOcupacionService(
            SolicitudGestionOcupacionRepository solicitudRepository,
            AsignacionPuestoRepository asignacionRepository,
            PuestoComercialRepository puestoRepository,
            DocumentoRepository documentoRepository,
            HistorialTitularidadService historialTitularidadService,
            HistorialOcupacionService historialOcupacionService,
            AuditoriaAdministrativaService auditoriaService,
            RenovacionOcupacionService renovacionService) {
        this.solicitudRepository = solicitudRepository;
        this.asignacionRepository = asignacionRepository;
        this.puestoRepository = puestoRepository;
        this.documentoRepository = documentoRepository;
        this.historialTitularidadService = historialTitularidadService;
        this.historialOcupacionService = historialOcupacionService;
        this.auditoriaService = auditoriaService;
        this.renovacionService = renovacionService;
    }

    @Transactional
    public SolicitudGestionOcupacionResponse formalizarSolicitud(UUID solicitudId, FormalizacionGestionRequest request, String username) {
        log.info("Iniciando formalización de solicitud de gestión ID: {} por usuario: {}", solicitudId, username);

        SolicitudGestionOcupacion solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada: " + solicitudId));

        if (solicitud.getEstado() != EstadoSolicitudGestion.APROBADA &&
            solicitud.getEstado() != EstadoSolicitudGestion.PENDIENTE_FORMALIZACION) {
            throw new BusinessException("Solo las solicitudes en estado APROBADA o PENDIENTE_FORMALIZACION pueden formalizarse. Estado actual: " + solicitud.getEstado());
        }

        Documento docFormalizacion = null;
        if (request.getDocumentoFormalizacionId() != null) {
            docFormalizacion = documentoRepository.findById(request.getDocumentoFormalizacionId()).orElse(null);
        }

        LocalDate fechaEfectiva = request.getFechaEfectiva() != null ? request.getFechaEfectiva() : LocalDate.now();
        AsignacionPuesto asignacionOrigen = solicitud.getAsignacionOrigen();
        PuestoComercial puesto = solicitud.getPuesto();

        if (solicitud.getTipoSolicitud() == TipoSolicitudGestion.CAMBIO_TITULAR ||
            solicitud.getTipoSolicitud() == TipoSolicitudGestion.TRANSFERENCIA_AUTORIZADA) {

            Comerciante comerciantePropuesto = solicitud.getComerciantePropuesto();
            if (comerciantePropuesto == null) {
                throw new BusinessException("La solicitud no contiene un titular propuesto para la formalización.");
            }

            // 1. Cerrar formalmente asignación anterior
            asignacionOrigen.setEstado(EstadoAsignacion.FINALIZADA);
            asignacionOrigen.setFechaFin(fechaEfectiva);
            asignacionOrigen.setMotivoFinalizacion("Finalizada por formalización de " + solicitud.getTipoSolicitud().getLabel() + " - " + solicitud.getCodigoSolicitud());
            asignacionRepository.save(asignacionOrigen);

            // 2. Generar código de asignación nuevo
            Long corrAsig = asignacionRepository.obtenerSiguienteValorSecuencia();
            String codigoNuevaAsig = String.format("ASIG-MER-%d-%06d", Year.now().getValue(), corrAsig);

            LocalDate fechaFinNueva = request.getNuevaFechaFin() != null ? request.getNuevaFechaFin() :
                    (solicitud.getFechaFinSolicitada() != null ? solicitud.getFechaFinSolicitada() : LocalDate.now().plusYears(1));

            // 3. Crear nueva asignación sin mutar la histórica
            AsignacionPuesto nuevaAsignacion = AsignacionPuesto.builder()
                    .codigoAsignacion(codigoNuevaAsig)
                    .puesto(puesto)
                    .comerciante(comerciantePropuesto)
                    .fechaSolicitud(solicitud.getFechaPresentacion())
                    .fechaInicio(fechaEfectiva)
                    .fechaFin(fechaFinNueva)
                    .estado(EstadoAsignacion.VIGENTE)
                    .tipoAsignacion(TipoAsignacion.REGULAR)
                    .motivo("Asignación originada por formalización de " + solicitud.getTipoSolicitud().getLabel() + " (" + solicitud.getCodigoSolicitud() + ")")
                    .usuarioAprobacion(username)
                    .fechaAprobacion(OffsetDateTime.now())
                    .documentoSustento(docFormalizacion)
                    .build();

            AsignacionPuesto asignacionGuardada = asignacionRepository.save(nuevaAsignacion);

            // 4. Asegurar estado del puesto como ocupado
            puesto.setEstado(EstadoPuesto.OCUPADO);
            puestoRepository.save(puesto);

            // 5. Registrar en Historial Inmutable de Titularidad
            TipoOperacionTitularidad opTit = (solicitud.getTipoSolicitud() == TipoSolicitudGestion.CAMBIO_TITULAR)
                    ? TipoOperacionTitularidad.CAMBIO_TITULAR : TipoOperacionTitularidad.TRANSFERENCIA_AUTORIZADA;

            historialTitularidadService.registrarCambioTitularidad(
                    puesto,
                    solicitud,
                    asignacionOrigen,
                    asignacionGuardada,
                    solicitud.getComercianteActual(),
                    comerciantePropuesto,
                    opTit,
                    fechaEfectiva,
                    docFormalizacion,
                    request.getMotivo(),
                    username
            );

            // 6. Registrar en Historial general de ocupación
            historialOcupacionService.registrarMovimiento(
                    puesto,
                    comerciantePropuesto,
                    asignacionGuardada,
                    TipoMovimientoOcupacion.ASIGNACION,
                    "Formalización de " + solicitud.getTipoSolicitud().getLabel() + " (" + solicitud.getCodigoSolicitud() + ")",
                    docFormalizacion,
                    EstadoAsignacion.FINALIZADA.name(),
                    EstadoAsignacion.VIGENTE.name(),
                    "Titular anterior: " + solicitud.getComercianteActual().getCodigoComerciante() + " -> Nuevo titular: " + comerciantePropuesto.getCodigoComerciante()
            );

            solicitud.setAsignacionNueva(asignacionGuardada);

        } else if (solicitud.getTipoSolicitud() == TipoSolicitudGestion.RENOVACION_ASIGNACION) {

            LocalDate nuevaFechaFin = request.getNuevaFechaFin() != null ? request.getNuevaFechaFin() :
                    (solicitud.getFechaFinSolicitada() != null ? solicitud.getFechaFinSolicitada() :
                            (asignacionOrigen.getFechaFin() != null ? asignacionOrigen.getFechaFin().plusYears(1) : LocalDate.now().plusYears(1)));

            asignacionOrigen.setFechaFin(nuevaFechaFin);
            asignacionOrigen.setEstado(EstadoAsignacion.VIGENTE);
            asignacionRepository.save(asignacionOrigen);

            // Registrar en historial de titularidad como renovación
            historialTitularidadService.registrarCambioTitularidad(
                    puesto,
                    solicitud,
                    asignacionOrigen,
                    asignacionOrigen,
                    solicitud.getComercianteActual(),
                    solicitud.getComercianteActual(),
                    TipoOperacionTitularidad.RENOVACION_ASIGNACION,
                    fechaEfectiva,
                    docFormalizacion,
                    request.getMotivo(),
                    username
            );

            // Registrar en Historial de ocupación
            historialOcupacionService.registrarMovimiento(
                    puesto,
                    solicitud.getComercianteActual(),
                    asignacionOrigen,
                    TipoMovimientoOcupacion.RENOVACION,
                    "Formalización de renovación de asignación hasta " + nuevaFechaFin,
                    docFormalizacion,
                    EstadoAsignacion.VIGENTE.name(),
                    EstadoAsignacion.VIGENTE.name(),
                    "Vigencia ampliada hasta " + nuevaFechaFin
            );

        } else if (solicitud.getTipoSolicitud() == TipoSolicitudGestion.RENOVACION_CONTRATO) {

            if (solicitud.getContratoOrigen() != null) {
                ContratoOcupacion contrato = solicitud.getContratoOrigen();
                LocalDate nuevaFechaFin = request.getNuevaFechaFin() != null ? request.getNuevaFechaFin() :
                        (solicitud.getFechaFinSolicitada() != null ? solicitud.getFechaFinSolicitada() :
                                (contrato.getFechaFin() != null ? contrato.getFechaFin().plusYears(1) : LocalDate.now().plusYears(1)));
                contrato.setFechaFin(nuevaFechaFin);
                contrato.setEstado(EstadoContrato.VIGENTE);
            }

        } else if (solicitud.getTipoSolicitud() == TipoSolicitudGestion.RENOVACION_AUTORIZACION) {

            if (solicitud.getAutorizacionOrigen() != null) {
                AutorizacionComercial aut = solicitud.getAutorizacionOrigen();
                LocalDate nuevaFechaFin = request.getNuevaFechaFin() != null ? request.getNuevaFechaFin() :
                        (solicitud.getFechaFinSolicitada() != null ? solicitud.getFechaFinSolicitada() :
                                (aut.getFechaFin() != null ? aut.getFechaFin().plusYears(1) : LocalDate.now().plusYears(1)));
                aut.setFechaFin(nuevaFechaFin);
                aut.setEstado(EstadoAutorizacion.VIGENTE);
            }
        }

        solicitud.setEstado(EstadoSolicitudGestion.FORMALIZADA);
        solicitud.setUsuarioFormalizacion(username);
        solicitud.setFechaFormalizacion(OffsetDateTime.now());
        solicitud.setDocumentoFormalizacion(docFormalizacion);

        SolicitudGestionOcupacion guardada = solicitudRepository.save(solicitud);

        auditoriaService.registrar("FORMALIZAR_SOLICITUD_GESTION", "mercados_solicitudes_gestion", guardada.getId(),
                "EXITO", "Formalización concluida con éxito para solicitud " + guardada.getCodigoSolicitud() +
                        " (" + guardada.getTipoSolicitud().getLabel() + ")");

        return renovacionService.toResponse(guardada);
    }
}
