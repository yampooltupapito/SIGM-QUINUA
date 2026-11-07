package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import org.springframework.web.multipart.MultipartFile;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.AtencionDtos.*;

import java.util.List;

public interface NotificacionResultadoService {

    NotificacionResponse registrarNotificacion(Long expedienteId, RegistrarNotificacionRequest request, String username);

    NotificacionResponse registrarIntentoEnvio(Long notificacionId, String observaciones, String username);

    NotificacionResponse acreditarNotificacionValida(Long notificacionId, AcreditarNotificacionRequest request, MultipartFile cargoPdf, String username);

    List<NotificacionResponse> listarNotificacionesPorExpediente(Long expedienteId, String username);
}
