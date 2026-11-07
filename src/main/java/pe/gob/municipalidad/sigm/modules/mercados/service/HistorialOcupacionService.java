package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.modules.mercados.entity.Comerciante;
import pe.gob.municipalidad.sigm.modules.mercados.dto.HistorialOcupacionResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.AsignacionPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.entity.HistorialOcupacion;
import pe.gob.municipalidad.sigm.modules.mercados.entity.PuestoComercial;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoMovimientoOcupacion;
import pe.gob.municipalidad.sigm.modules.mercados.repository.HistorialOcupacionRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HistorialOcupacionService {

    private final HistorialOcupacionRepository historialRepository;

    public HistorialOcupacionService(HistorialOcupacionRepository historialRepository) {
        this.historialRepository = historialRepository;
    }

    @Transactional
    public HistorialOcupacion registrarMovimiento(
            PuestoComercial puesto,
            Comerciante comerciante,
            AsignacionPuesto asignacion,
            TipoMovimientoOcupacion tipoMovimiento,
            String motivo,
            Documento documentoSustento,
            String estadoAnterior,
            String estadoNuevo,
            String observaciones) {

        String usuario = obtenerUsuarioAutenticado();

        HistorialOcupacion hist = HistorialOcupacion.builder()
                .puesto(puesto)
                .comerciante(comerciante)
                .asignacion(asignacion)
                .tipoMovimiento(tipoMovimiento)
                .fecha(OffsetDateTime.now())
                .usuarioResponsable(usuario)
                .motivo(motivo != null ? motivo : ("Movimiento: " + tipoMovimiento.name()))
                .documentoSustento(documentoSustento)
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(estadoNuevo != null ? estadoNuevo : "-")
                .observaciones(observaciones)
                .build();

        HistorialOcupacion guardado = historialRepository.save(hist);
        log.info("Registrado movimiento de ocupación: {} para puesto {}", tipoMovimiento, puesto != null ? puesto.getCodigo() : "N/A");
        return guardado;
    }

    @Transactional(readOnly = true)
    public List<HistorialOcupacionResponse> obtenerHistorialPorAsignacion(UUID asignacionId) {
        return historialRepository.findByAsignacionIdOrderByFechaDesc(asignacionId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<HistorialOcupacionResponse> obtenerHistorialPorPuesto(UUID puestoId) {
        return historialRepository.findByPuestoIdOrderByFechaDesc(puestoId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<HistorialOcupacionResponse> obtenerHistorialPorComerciante(UUID comercianteId) {
        return historialRepository.findByComercianteIdOrderByFechaDesc(comercianteId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public HistorialOcupacionResponse toResponse(HistorialOcupacion h) {
        String titularNombre = "-";
        if (h.getComerciante() != null) {
            if (h.getComerciante().getPersona() != null) {
                titularNombre = h.getComerciante().getPersona().getNombreCompleto();
            } else if (h.getComerciante().getPersonaJuridica() != null) {
                titularNombre = h.getComerciante().getPersonaJuridica().getRazonSocial();
            }
        }

        return HistorialOcupacionResponse.builder()
                .id(h.getId())
                .puestoId(h.getPuesto() != null ? h.getPuesto().getId() : null)
                .puestoCodigo(h.getPuesto() != null ? h.getPuesto().getCodigo() : null)
                .comercianteId(h.getComerciante() != null ? h.getComerciante().getId() : null)
                .comercianteCodigo(h.getComerciante() != null ? h.getComerciante().getCodigoComerciante() : null)
                .titularNombre(titularNombre)
                .asignacionId(h.getAsignacion() != null ? h.getAsignacion().getId() : null)
                .tipoMovimiento(h.getTipoMovimiento() != null ? h.getTipoMovimiento().name() : null)
                .fecha(h.getFecha())
                .usuarioResponsable(h.getUsuarioResponsable())
                .motivo(h.getMotivo())
                .documentoSustentoId(h.getDocumentoSustento() != null ? h.getDocumentoSustento().getId() : null)
                .documentoSustentoNombre(h.getDocumentoSustento() != null ? h.getDocumentoSustento().getNombreOriginal() : null)
                .estadoAnterior(h.getEstadoAnterior())
                .estadoNuevo(h.getEstadoNuevo())
                .observaciones(h.getObservaciones())
                .build();
    }

    private String obtenerUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        return "SISTEMA";
    }
}
