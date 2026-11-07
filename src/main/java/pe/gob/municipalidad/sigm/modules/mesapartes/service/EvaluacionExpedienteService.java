package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import org.springframework.web.multipart.MultipartFile;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.AtencionDtos.*;

import java.util.List;

public interface EvaluacionExpedienteService {

    EvaluacionResponse crearEvaluacion(Long expedienteId, CrearEvaluacionRequest request, String username);

    EvaluacionResponse finalizarEvaluacion(Long evaluacionId, String username);

    EvaluacionResponse anularEvaluacion(Long evaluacionId, String motivo, String username);

    EvaluacionDocumentoResponse adjuntarDocumento(Long evaluacionId, MultipartFile archivo, String tipoDocumento, String username);

    List<EvaluacionResponse> listarEvaluacionesPorExpediente(Long expedienteId, String username);
}
