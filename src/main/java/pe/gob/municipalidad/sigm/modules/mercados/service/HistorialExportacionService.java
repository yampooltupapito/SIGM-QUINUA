package pe.gob.municipalidad.sigm.modules.mercados.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ExportacionReporteRequest;
import pe.gob.municipalidad.sigm.modules.mercados.dto.HistorialExportacionResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.HistorialExportacionMercado;

import java.util.UUID;

public interface HistorialExportacionService {
    HistorialExportacionMercado registrarExportacion(ExportacionReporteRequest request, byte[] contenido, String username, String ip, String userAgent);
    Page<HistorialExportacionResponse> listarHistorial(Pageable pageable, String username, boolean esAdmin);
    byte[] descargarArchivo(UUID historialId, String username, boolean esAdmin);
    HistorialExportacionMercado obtenerRegistro(UUID historialId);
}
