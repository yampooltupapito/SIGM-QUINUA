package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.ConceptoFilterDto;

import java.util.UUID;

public interface ConceptoExportService {

    byte[] exportarExcel(ConceptoFilterDto filtro);

    byte[] exportarPdf(ConceptoFilterDto filtro);

    byte[] exportarFichaConceptoPdf(UUID conceptoId);
}
