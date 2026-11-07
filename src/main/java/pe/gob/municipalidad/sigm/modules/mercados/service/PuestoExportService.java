package pe.gob.municipalidad.sigm.modules.mercados.service;

import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoFisicoPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPuesto;

import java.math.BigDecimal;
import java.util.UUID;

public interface PuestoExportService {

    byte[] exportarExcel(UUID mercadoId, String termino, UUID sectorId, UUID pabellonId, UUID zonaId,
                         Long tipoPuestoId, EstadoFisicoPuesto estadoFisico, EstadoPuesto estado,
                         Integer nivel, BigDecimal minArea, BigDecimal maxArea);

    byte[] exportarPdf(UUID mercadoId, String termino, UUID sectorId, UUID pabellonId, UUID zonaId,
                       Long tipoPuestoId, EstadoFisicoPuesto estadoFisico, EstadoPuesto estado,
                       Integer nivel, BigDecimal minArea, BigDecimal maxArea);
}
