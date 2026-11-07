package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import pe.gob.municipalidad.sigm.modules.mesapartes.dto.AtencionDtos.*;

import java.util.List;

public interface DecisionExpedienteService {

    DecisionResponse registrarDecision(Long expedienteId, RegistrarDecisionRequest request, String username);

    DecisionResponse generarDocumentoPdf(Long decisionId, String baseUrl, String username);

    DecisionResponse registrarFormalizacionEmision(Long decisionId, FormalizarDecisionRequest request, String username);

    List<DecisionResponse> listarDecisionesPorExpediente(Long expedienteId, String username);

    DecisionResponse obtenerPorId(Long decisionId, String username);

    byte[] descargarDocumentoDecisionPdf(Long decisionId, String username);
}
