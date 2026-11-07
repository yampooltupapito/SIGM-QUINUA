package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.document.repository.DocumentoRepository;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.notificacion.RegistrarCargoRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.CargoRecepcionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.IntentoNotificacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.NotificacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoEntregaNotificacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoValidezLegalNotificacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoEventoNotificacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.CargoRecepcionTributariaRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.IntentoNotificacionTributariaRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.NotificacionTributariaRepository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CargoRecepcionService {

    private final CargoRecepcionTributariaRepository cargoRepository;
    private final NotificacionTributariaRepository notificacionRepository;
    private final IntentoNotificacionTributariaRepository intentoRepository;
    private final DocumentoRepository documentoRepository;
    private final HistorialNotificacionService historialService;

    @Transactional
    public CargoRecepcionTributaria registrarCargo(UUID notificacionId,
                                                   RegistrarCargoRequest request,
                                                   Usuario operador) {
        NotificacionTributaria notif = notificacionRepository.findById(notificacionId)
                .orElseThrow(() -> new BusinessException("Notificación no encontrada con ID: " + notificacionId));

        IntentoNotificacionTributaria intento = null;
        if (request.getIntentoId() != null) {
            intento = intentoRepository.findById(request.getIntentoId()).orElse(null);
        }

        Documento docEvidencia = null;
        if (request.getDocumentoEvidenciaId() != null) {
            docEvidencia = documentoRepository.findById(request.getDocumentoEvidenciaId()).orElse(null);
        }

        OffsetDateTime fechaRecepcion = request.getFechaRecepcionDeclarada() != null
                ? request.getFechaRecepcionDeclarada() : OffsetDateTime.now();

        Optional<CargoRecepcionTributaria> existenteOpt = cargoRepository.findByNotificacionId(notificacionId);
        CargoRecepcionTributaria cargo = existenteOpt.orElseGet(() -> CargoRecepcionTributaria.builder()
                .notificacion(notif)
                .build());

        cargo.setIntento(intento);
        cargo.setFechaRecepcionDeclarada(fechaRecepcion);
        cargo.setReceptorIdentificado(request.getReceptorIdentificado());
        cargo.setReceptorNumeroDoc(request.getReceptorNumeroDoc());
        cargo.setTipoVinculoReceptor(request.getTipoVinculoReceptor());
        cargo.setDocumentoEvidencia(docEvidencia);
        cargo.setTieneFirma(request.getTieneFirma() != null ? request.getTieneFirma() : false);
        cargo.setTieneSello(request.getTieneSello() != null ? request.getTieneSello() : false);
        cargo.setObservaciones(request.getObservaciones());
        cargo.setEstadoValidacion("REGISTRADO");
        cargo.setCreatedBy(operador != null ? operador.getUsername() : "SISTEMA");

        CargoRecepcionTributaria guardado = cargoRepository.save(cargo);

        // Actualizar estados de la notificación:
        // La entrega material queda registrada, pero la validez legal queda estrictamente en revisión/verificación
        notif.setEstadoEntrega(EstadoEntregaNotificacion.ENTREGADA_CON_EVIDENCIA);
        if (notif.getTipoNotificacion().getRequiereValidacionLegal()) {
            notif.setEstadoValidezLegal(EstadoValidezLegalNotificacion.PENDIENTE_VERIFICACION);
        }
        notificacionRepository.save(notif);

        historialService.registrarEvento(
                notif,
                TipoEventoNotificacion.CARGO_REGISTRADO,
                "Cargo de recepción registrado: Receptor " + request.getReceptorIdentificado() +
                        " (Vínculo: " + request.getTipoVinculoReceptor().getEtiqueta() + ", Firma: " + cargo.getTieneFirma() + ")",
                null,
                "Receptor: " + request.getReceptorIdentificado(),
                operador,
                null
        );

        return guardado;
    }

    @Transactional(readOnly = true)
    public Optional<CargoRecepcionTributaria> obtenerPorNotificacion(UUID notificacionId) {
        return cargoRepository.findByNotificacionId(notificacionId);
    }
}

