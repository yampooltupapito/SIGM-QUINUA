package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvAdminDtos.BandejaEstadisticasAdminResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvDtos.ExpedienteResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.CanalIngreso;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.PrioridadExpediente;

import java.time.LocalDate;

/**
 * Servicio de consulta institucional para la bandeja de entrada de Mesa de Partes Virtual.
 */
public interface BandejaMesaPartesService {

    Page<ExpedienteResponse> listarBandeja(
            EstadoExpediente estado,
            Long oficinaId,
            Long tipoTramiteId,
            PrioridadExpediente prioridad,
            CanalIngreso canalIngreso,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            String termino,
            Pageable pageable,
            String username
    );

    BandejaEstadisticasAdminResponse obtenerEstadisticasBandeja(String username);
}
