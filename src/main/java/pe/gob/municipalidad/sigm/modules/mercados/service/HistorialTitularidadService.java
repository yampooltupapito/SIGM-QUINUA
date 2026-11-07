package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.modules.mercados.dto.HistorialTitularidadResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoOperacionTitularidad;
import pe.gob.municipalidad.sigm.modules.mercados.repository.HistorialTitularidadPuestoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HistorialTitularidadService {

    private final HistorialTitularidadPuestoRepository historialRepository;

    public HistorialTitularidadService(HistorialTitularidadPuestoRepository historialRepository) {
        this.historialRepository = historialRepository;
    }

    @Transactional
    public HistorialTitularidadPuesto registrarCambioTitularidad(
            PuestoComercial puesto,
            SolicitudGestionOcupacion solicitud,
            AsignacionPuesto asignacionAnterior,
            AsignacionPuesto asignacionNueva,
            Comerciante titularAnterior,
            Comerciante titularNuevo,
            TipoOperacionTitularidad tipoOperacion,
            LocalDate fechaEfectiva,
            Documento documentoSustento,
            String motivo,
            String usuario) {

        log.info("Registrando en historial inmutable titularidad del puesto {}: Anterior={} -> Nuevo={}",
                puesto.getNumero(), titularAnterior.getPersona().getNumeroDocumento(), titularNuevo.getPersona().getNumeroDocumento());

        HistorialTitularidadPuesto h = HistorialTitularidadPuesto.builder()
                .puesto(puesto)
                .solicitud(solicitud)
                .asignacionAnterior(asignacionAnterior)
                .asignacionNueva(asignacionNueva)
                .titularAnterior(titularAnterior)
                .titularNuevo(titularNuevo)
                .tipoOperacion(tipoOperacion)
                .fechaEfectiva(fechaEfectiva != null ? fechaEfectiva : LocalDate.now())
                .documentoSustento(documentoSustento)
                .motivo(motivo)
                .usuarioRegistro(usuario)
                .build();

        return historialRepository.save(h);
    }

    @Transactional(readOnly = true)
    public List<HistorialTitularidadResponse> listarPorPuesto(UUID puestoId) {
        return historialRepository.findByPuestoIdOrderByFechaEfectivaDesc(puestoId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<HistorialTitularidadResponse> buscarPaginadoPorPuesto(UUID puestoId, Pageable pageable) {
        return historialRepository.findByPuestoId(puestoId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<HistorialTitularidadResponse> listarPorComerciante(UUID comercianteId) {
        return historialRepository.findByTitularAnteriorIdOrTitularNuevoIdOrderByFechaEfectivaDesc(comercianteId, comercianteId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public HistorialTitularidadResponse toResponse(HistorialTitularidadPuesto h) {
        return HistorialTitularidadResponse.builder()
                .id(h.getId())
                .puestoId(h.getPuesto().getId())
                .puestoNumero(h.getPuesto().getNumero())
                .mercadoNombre(h.getPuesto().getMercado().getNombre())
                .solicitudId(h.getSolicitud() != null ? h.getSolicitud().getId() : null)
                .codigoSolicitud(h.getSolicitud() != null ? h.getSolicitud().getCodigoSolicitud() : null)
                .asignacionAnteriorId(h.getAsignacionAnterior() != null ? h.getAsignacionAnterior().getId() : null)
                .asignacionAnteriorCodigo(h.getAsignacionAnterior() != null ? h.getAsignacionAnterior().getCodigoAsignacion() : null)
                .asignacionNuevaId(h.getAsignacionNueva() != null ? h.getAsignacionNueva().getId() : null)
                .asignacionNuevaCodigo(h.getAsignacionNueva() != null ? h.getAsignacionNueva().getCodigoAsignacion() : null)
                .titularAnteriorId(h.getTitularAnterior().getId())
                .titularAnteriorNombre(h.getTitularAnterior().getPersona().getNombreCompleto())
                .titularAnteriorDocumento(h.getTitularAnterior().getPersona().getNumeroDocumento())
                .titularNuevoId(h.getTitularNuevo().getId())
                .titularNuevoNombre(h.getTitularNuevo().getPersona().getNombreCompleto())
                .titularNuevoDocumento(h.getTitularNuevo().getPersona().getNumeroDocumento())
                .tipoOperacion(h.getTipoOperacion())
                .tipoOperacionLabel(h.getTipoOperacion().getLabel())
                .fechaEfectiva(h.getFechaEfectiva())
                .documentoSustentoId(h.getDocumentoSustento() != null ? h.getDocumentoSustento().getId() : null)
                .motivo(h.getMotivo())
                .usuarioRegistro(h.getUsuarioRegistro())
                .createdAt(h.getCreatedAt())
                .build();
    }
}
