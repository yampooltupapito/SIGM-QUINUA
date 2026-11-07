package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.AsignacionNotificadorTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.NotificacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoAdministrativoNotificacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoAsignacionNotificador;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoEntregaNotificacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoEventoNotificacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.AsignacionNotificadorTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.NotificacionTributariaRepository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsignacionNotificadorService {

    private final AsignacionNotificadorTributarioRepository asignacionRepository;
    private final NotificacionTributariaRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistorialNotificacionService historialService;

    @Transactional
    public AsignacionNotificadorTributario asignarNotificador(UUID notificacionId,
                                                               Long notificadorId,
                                                               LocalDate fechaProgramada,
                                                               String sectorRuta,
                                                               String observaciones,
                                                               Usuario operador) {
        NotificacionTributaria notif = notificacionRepository.findById(notificacionId)
                .orElseThrow(() -> new BusinessException("Notificación no encontrada con ID: " + notificacionId));

        if (notif.getEstadoAdministrativo() == EstadoAdministrativoNotificacion.ANULADA ||
            notif.getEstadoAdministrativo() == EstadoAdministrativoNotificacion.CERRADA) {
            throw new BusinessException("No se puede asignar una notificación en estado " + notif.getEstadoAdministrativo());
        }

        Usuario notificador = usuarioRepository.findById(notificadorId)
                .orElseThrow(() -> new BusinessException("Usuario notificador no encontrado con ID: " + notificadorId));

        // Cerrar o actualizar asignación previa activa si existiese (reasignación)
        Optional<AsignacionNotificadorTributario> prevOpt = asignacionRepository
                .findFirstByNotificacionIdAndEstadoOrderByFechaAsignacionDesc(notificacionId, EstadoAsignacionNotificador.ASIGNADA);
        if (prevOpt.isPresent()) {
            AsignacionNotificadorTributario prev = prevOpt.get();
            prev.setEstado(EstadoAsignacionNotificador.REASIGNADA);
            prev.setMotivoReasignacion("Reasignado al notificador " + notificador.getNombreCompleto());
            asignacionRepository.save(prev);
        }

        AsignacionNotificadorTributario asignacion = AsignacionNotificadorTributario.builder()
                .notificacion(notif)
                .notificador(notificador)
                .fechaAsignacion(OffsetDateTime.now())
                .fechaProgramada(fechaProgramada != null ? fechaProgramada : LocalDate.now().plusDays(1))
                .estado(EstadoAsignacionNotificador.ASIGNADA)
                .sectorRuta(sectorRuta)
                .observaciones(observaciones)
                .build();
        asignacion.setCreatedBy(operador != null ? operador.getUsername() : "SISTEMA");

        AsignacionNotificadorTributario guardada = asignacionRepository.save(asignacion);

        notif.setFechaProgramada(asignacion.getFechaProgramada());
        if (notif.getEstadoEntrega() == EstadoEntregaNotificacion.NO_PROGRAMADA) {
            notif.setEstadoEntrega(EstadoEntregaNotificacion.PROGRAMADA);
        }
        if (notif.getEstadoAdministrativo() == EstadoAdministrativoNotificacion.AUTORIZADA) {
            notif.setEstadoAdministrativo(EstadoAdministrativoNotificacion.EN_GESTION);
        }
        notificacionRepository.save(notif);

        historialService.registrarEvento(
                notif,
                prevOpt.isPresent() ? TipoEventoNotificacion.REASIGNACION : TipoEventoNotificacion.ASIGNACION_NOTIFICADOR,
                "Asignada a notificador: " + notificador.getNombreCompleto() + " (" + notificador.getUsername() + ")" +
                        (sectorRuta != null ? " - Sector: " + sectorRuta : ""),
                prevOpt.map(p -> p.getNotificador().getUsername()).orElse(null),
                notificador.getUsername(),
                operador,
                null
        );

        return guardada;
    }

    @Transactional(readOnly = true)
    public List<AsignacionNotificadorTributario> listarPorNotificacion(UUID notificacionId) {
        return asignacionRepository.findByNotificacionIdOrderByFechaAsignacionDesc(notificacionId);
    }

    @Transactional(readOnly = true)
    public Page<AsignacionNotificadorTributario> listarBandejaNotificador(Long notificadorId,
                                                                         EstadoAsignacionNotificador estado,
                                                                         Pageable pageable) {
        if (estado != null) {
            return asignacionRepository.findByNotificadorIdAndEstado(notificadorId, estado, pageable);
        }
        return asignacionRepository.findByNotificadorId(notificadorId, pageable);
    }
}

