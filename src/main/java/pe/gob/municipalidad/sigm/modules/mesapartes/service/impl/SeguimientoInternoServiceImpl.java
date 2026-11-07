package pe.gob.municipalidad.sigm.modules.mesapartes.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.DerivacionDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvDtos.ExpedienteHistorialResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.AsignacionExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.DerivacionExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.Expediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoDerivacion;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.mapper.DerivacionMapper;
import pe.gob.municipalidad.sigm.modules.mesapartes.mapper.MpvMapper;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.AsignacionExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.DerivacionExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteHistorialRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.ActuacionExpedienteService;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.PlazoAdministrativoService;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.SeguimientoInternoService;
import pe.gob.municipalidad.sigm.security.authorization.OfficeSecurityService;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SeguimientoInternoServiceImpl implements SeguimientoInternoService {

    private final ExpedienteRepository expedienteRepository;
    private final DerivacionExpedienteRepository derivacionRepository;
    private final AsignacionExpedienteRepository asignacionRepository;
    private final ExpedienteHistorialRepository historialRepository;
    private final ActuacionExpedienteService actuacionService;
    private final PlazoAdministrativoService plazoService;
    private final UsuarioRepository usuarioRepository;
    private final DerivacionMapper derivacionMapper;
    private final MpvMapper mpvMapper;
    private final OfficeSecurityService officeSecurityService;

    public SeguimientoInternoServiceImpl(
            ExpedienteRepository expedienteRepository,
            DerivacionExpedienteRepository derivacionRepository,
            AsignacionExpedienteRepository asignacionRepository,
            ExpedienteHistorialRepository historialRepository,
            ActuacionExpedienteService actuacionService,
            PlazoAdministrativoService plazoService,
            UsuarioRepository usuarioRepository,
            DerivacionMapper derivacionMapper,
            MpvMapper mpvMapper,
            OfficeSecurityService officeSecurityService) {
        this.expedienteRepository = expedienteRepository;
        this.derivacionRepository = derivacionRepository;
        this.asignacionRepository = asignacionRepository;
        this.historialRepository = historialRepository;
        this.actuacionService = actuacionService;
        this.plazoService = plazoService;
        this.usuarioRepository = usuarioRepository;
        this.derivacionMapper = derivacionMapper;
        this.mpvMapper = mpvMapper;
        this.officeSecurityService = officeSecurityService;
    }

    @Override
    @Transactional(readOnly = true)
    public SeguimientoInternoResponse obtenerSeguimiento(Long expedienteId, String username) {
        Expediente expediente = expedienteRepository.findByIdWithDetalles(expedienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Expediente no encontrado con ID: " + expedienteId));

        List<DerivacionExpediente> derivaciones = derivacionRepository.findByExpedienteIdOrderByFechaDerivacionDesc(expedienteId);

        if (username != null) {
            usuarioRepository.findByUsernameWithRolesAndPersona(username).ifPresent(usuario -> {
                if (!officeSecurityService.tieneAccesoTransversalMesaPartes(usuario)) {
                    Long oficinaUsuarioId = officeSecurityService.obtenerOficinaActivaId(username);
                    if (oficinaUsuarioId != null) {
                        boolean esDeSuOficina = expediente.getOficinaResponsable() != null
                                && oficinaUsuarioId.equals(expediente.getOficinaResponsable().getId());
                        boolean esSuOrigen = expediente.getOficinaOrigen() != null
                                && oficinaUsuarioId.equals(expediente.getOficinaOrigen().getId());
                        boolean esResponsableDirecto = expediente.getResponsableActual() != null
                                && usuario.getId().equals(expediente.getResponsableActual().getId());
                        boolean participoEnDerivacion = derivaciones.stream()
                                .anyMatch(d -> (d.getOficinaOrigen() != null && oficinaUsuarioId.equals(d.getOficinaOrigen().getId()))
                                        || (d.getOficinaDestino() != null && oficinaUsuarioId.equals(d.getOficinaDestino().getId())));

                        if (!esDeSuOficina && !esSuOrigen && !esResponsableDirecto && !participoEnDerivacion) {
                            throw new AccessDeniedException("Acceso restringido: El seguimiento de este expediente pertenece a otra unidad orgánica.");
                        }
                    }
                }
            });
        }
        List<AsignacionExpediente> asignaciones = asignacionRepository.findByExpedienteIdOrderByFechaAsignacionDesc(expedienteId);
        List<ActuacionResponse> actuaciones = actuacionService.listarActuaciones(expedienteId, username);

        List<ExpedienteHistorialResponse> timeline = historialRepository.findByExpedienteIdOrderByFechaMovimientoAsc(expedienteId)
                .stream()
                .map(mpvMapper::toHistorialResponse)
                .collect(Collectors.toList());

        // Armar mapa de recorrido administrativo real
        List<RecorridoNodoDto> recorrido = construirRecorrido(expediente, derivaciones);

        // Control de plazos institucional
        PlazoSeguimientoDto plazo = calcularPlazo(expediente);

        return SeguimientoInternoResponse.builder()
                .expediente(mpvMapper.toExpedienteResponse(expediente))
                .oficinaOrigen(derivacionMapper.toOficinaSimpleDto(expediente.getOficinaOrigen()))
                .oficinaActual(derivacionMapper.toOficinaSimpleDto(expediente.getOficinaResponsable()))
                .responsableActual(derivacionMapper.toUsuarioSimpleDto(expediente.getResponsableActual()))
                .estadoExpediente(expediente.getEstadoActual())
                .derivaciones(derivaciones.stream().map(derivacionMapper::toDerivacionResponse).collect(Collectors.toList()))
                .asignaciones(asignaciones.stream().map(derivacionMapper::toAsignacionResponse).collect(Collectors.toList()))
                .actuaciones(actuaciones)
                .timeline(timeline)
                .recorrido(recorrido)
                .plazo(plazo)
                .build();
    }

    private List<RecorridoNodoDto> construirRecorrido(Expediente expediente, List<DerivacionExpediente> derivacionesDesc) {
        List<RecorridoNodoDto> nodos = new ArrayList<>();
        int orden = 1;

        // Nodo 1: Mesa de Partes / Origen
        String nombreOrigen = expediente.getOficinaOrigen() != null ? expediente.getOficinaOrigen().getNombre() : "Mesa de Partes General";
        Long origenId = expediente.getOficinaOrigen() != null ? expediente.getOficinaOrigen().getId() : null;

        List<DerivacionExpediente> derivacionesAsc = new ArrayList<>(derivacionesDesc);
        Collections.reverse(derivacionesAsc);

        OffsetDateTime fechaSalidaOrigen = !derivacionesAsc.isEmpty() ? derivacionesAsc.get(0).getFechaDerivacion() : null;
        boolean origenEsActual = derivacionesAsc.isEmpty() ||
                (expediente.getOficinaResponsable() != null && expediente.getOficinaResponsable().getId().equals(origenId));

        nodos.add(RecorridoNodoDto.builder()
                .orden(orden++)
                .oficinaId(origenId)
                .oficinaNombre(nombreOrigen)
                .accion("PRESENTACION_RECEPCION")
                .fechaEntrada(expediente.getFechaPresentacion() != null ? expediente.getFechaPresentacion() : expediente.getCreatedAt())
                .fechaSalida(fechaSalidaOrigen)
                .actual(origenEsActual)
                .responsableNombre(expediente.getUsuarioRegistro() != null ? expediente.getUsuarioRegistro().getUsername() : null)
                .build());

        // Nodos sucesivos: por cada derivación recibida o pendiente
        for (int i = 0; i < derivacionesAsc.size(); i++) {
            DerivacionExpediente d = derivacionesAsc.get(i);
            boolean esUltima = (i == derivacionesAsc.size() - 1);
            boolean esActual = esUltima && (d.getEstado() == EstadoDerivacion.RECIBIDA || d.getEstado() == EstadoDerivacion.PENDIENTE);

            OffsetDateTime salida = (i + 1 < derivacionesAsc.size()) ? derivacionesAsc.get(i + 1).getFechaDerivacion() : null;

            nodos.add(RecorridoNodoDto.builder()
                    .orden(orden++)
                    .oficinaId(d.getOficinaDestino() != null ? d.getOficinaDestino().getId() : null)
                    .oficinaNombre(d.getOficinaDestino() != null ? d.getOficinaDestino().getNombre() : "Dependencia Municipal")
                    .accion(d.getEstado().name())
                    .fechaEntrada(d.getFechaRecepcion() != null ? d.getFechaRecepcion() : d.getFechaDerivacion())
                    .fechaSalida(salida)
                    .actual(esActual)
                    .responsableNombre(d.getUsuarioReceptor() != null ? d.getUsuarioReceptor().getUsername() : null)
                    .build());
        }

        return nodos;
    }

    private PlazoSeguimientoDto calcularPlazo(Expediente expediente) {
        if (expediente.getTipoTramite() == null || expediente.getTipoTramite().getPlazoDias() == null || expediente.getTipoTramite().getPlazoDias() <= 0) {
            return PlazoSeguimientoDto.builder()
                    .estadoPlazo("SIN_PLAZO_CONFIGURADO")
                    .diasTranscurridos(0L)
                    .diasRestantes(0L)
                    .porcentajeTranscurrido("0%")
                    .build();
        }

        Integer plazoDias = expediente.getTipoTramite().getPlazoDias();
        String unidadPlazo = expediente.getTipoTramite().getUnidadPlazo() != null ? expediente.getTipoTramite().getUnidadPlazo().name() : "DIAS_HABILES";
        OffsetDateTime fechaInicio = expediente.getFechaRecepcion() != null ? expediente.getFechaRecepcion() : expediente.getFechaPresentacion();
        if (fechaInicio == null) {
            fechaInicio = expediente.getCreatedAt();
        }

        OffsetDateTime fechaLimite = expediente.getFechaLimite();
        if (fechaLimite == null) {
            fechaLimite = plazoService.calcularFechaLimite(fechaInicio, plazoDias, expediente.getTipoTramite().getUnidadPlazo());
        }

        OffsetDateTime ahora = OffsetDateTime.now();
        long diasTranscurridos = Math.max(0, Duration.between(fechaInicio, ahora).toDays());
        long diasRestantes = (fechaLimite != null) ? Duration.between(ahora, fechaLimite).toDays() : 0L;

        String estadoPlazo;
        if (expediente.getEstadoActual() == EstadoExpediente.OBSERVADO) {
            estadoPlazo = "SUSPENDIDO";
        } else if (fechaLimite != null && ahora.isAfter(fechaLimite)) {
            estadoPlazo = "VENCIDO";
        } else if (diasRestantes <= 3) {
            estadoPlazo = "PROXIMO_VENCIMIENTO";
        } else {
            estadoPlazo = "EN_PLAZO";
        }

        double pct = Math.min(100.0, Math.max(0.0, ((double) diasTranscurridos / (double) plazoDias) * 100.0));

        return PlazoSeguimientoDto.builder()
                .fechaInicio(fechaInicio)
                .plazoDias(plazoDias)
                .unidadPlazo(unidadPlazo)
                .fechaLimite(fechaLimite)
                .diasTranscurridos(diasTranscurridos)
                .diasRestantes(diasRestantes)
                .estadoPlazo(estadoPlazo)
                .porcentajeTranscurrido(String.format("%.1f%%", pct))
                .build();
    }
}
