package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ControlPlazoNotificacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.NotificacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoPlazoNotificacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoEventoNotificacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ControlPlazoNotificacionRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlazoNotificacionService {

    private final ControlPlazoNotificacionRepository controlPlazoRepository;
    private final HistorialNotificacionService historialService;

    /**
     * Calcula la fecha de vencimiento según días hábiles (lunes a viernes) o días calendario.
     * En el procedimiento tributario peruano (Art. 106 CT), el cómputo de plazos por días
     * inicia el día hábil siguiente a aquel en que surta efectos la notificación legal.
     */
    public LocalDate calcularFechaVencimiento(LocalDate hitoInicial, int numeroDias, boolean diasHabiles) {
        if (hitoInicial == null || numeroDias <= 0) {
            throw new BusinessException("El hito inicial y el número de días deben ser válidos.");
        }

        LocalDate fecha = hitoInicial;
        if (diasHabiles) {
            int contados = 0;
            while (contados < numeroDias) {
                fecha = fecha.plusDays(1);
                if (fecha.getDayOfWeek() != DayOfWeek.SATURDAY && fecha.getDayOfWeek() != DayOfWeek.SUNDAY) {
                    contados++;
                }
            }
        } else {
            fecha = fecha.plusDays(numeroDias);
        }
        return fecha;
    }

    /**
     * Calcula los días hábiles restantes entre la fecha actual y la fecha de vencimiento.
     */
    public int calcularDiasRestantes(LocalDate fechaVencimiento, boolean diasHabiles) {
        LocalDate hoy = LocalDate.now();
        if (hoy.isAfter(fechaVencimiento)) {
            return 0;
        }

        if (diasHabiles) {
            int dias = 0;
            LocalDate cur = hoy;
            while (cur.isBefore(fechaVencimiento)) {
                cur = cur.plusDays(1);
                if (cur.getDayOfWeek() != DayOfWeek.SATURDAY && cur.getDayOfWeek() != DayOfWeek.SUNDAY) {
                    dias++;
                }
            }
            return dias;
        } else {
            return (int) ChronoUnit.DAYS.between(hoy, fechaVencimiento);
        }
    }

    @Transactional
    public ControlPlazoNotificacion registrarOActualizarPlazo(NotificacionTributaria notificacion,
                                                              String tipoPlazo,
                                                              boolean diasHabiles,
                                                              int numeroDias,
                                                              LocalDate hitoInicial,
                                                              Usuario usuario) {
        LocalDate fechaVenc = calcularFechaVencimiento(hitoInicial, numeroDias, diasHabiles);
        int diasRestantes = calcularDiasRestantes(fechaVenc, diasHabiles);

        EstadoPlazoNotificacion estado = EstadoPlazoNotificacion.VIGENTE;
        if (LocalDate.now().isAfter(fechaVenc)) {
            estado = EstadoPlazoNotificacion.VENCIDO;
        } else if (diasRestantes <= 3) {
            estado = EstadoPlazoNotificacion.PROXIMO_A_VENCER;
        }

        Optional<ControlPlazoNotificacion> opt = controlPlazoRepository.findByNotificacionId(notificacion.getId());
        ControlPlazoNotificacion control = opt.orElseGet(() -> ControlPlazoNotificacion.builder()
                .notificacion(notificacion)
                .build());

        control.setTipoPlazo(tipoPlazo);
        control.setDiasHabiles(diasHabiles);
        control.setNumeroDias(numeroDias);
        control.setFechaHitoInicial(hitoInicial);
        control.setFechaVencimiento(fechaVenc);
        control.setDiasRestantes(diasRestantes);
        control.setEstadoPlazo(estado);

        ControlPlazoNotificacion guardado = controlPlazoRepository.save(control);

        historialService.registrarEvento(
                notificacion,
                TipoEventoNotificacion.PLAZO_COMPUTADO,
                "Cómputo de plazo " + tipoPlazo + " (" + numeroDias + (diasHabiles ? " días hábiles" : " días calendario") +
                        ") con vencimiento el " + fechaVenc,
                null,
                "Vencimiento: " + fechaVenc + ", Estado: " + estado,
                usuario,
                null
        );

        return guardado;
    }

    @Transactional(readOnly = true)
    public Optional<ControlPlazoNotificacion> obtenerPorNotificacion(UUID notificacionId) {
        return controlPlazoRepository.findByNotificacionId(notificacionId);
    }

    @Transactional(readOnly = true)
    public List<ControlPlazoNotificacion> listarProximosAVencer() {
        return controlPlazoRepository.findProximosAVencer(LocalDate.now().plusDays(5));
    }
}

