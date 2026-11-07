package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.ConfiguracionMpvDtos.HistorialConfiguracionResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoCambioConfiguracion;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoEntidadConfiguracion;

import java.util.List;

public interface ConfiguracionHistorialService {

    void registrarCambio(
            TipoEntidadConfiguracion entidadTipo,
            Long entidadId,
            String codigoReferencia,
            TipoCambioConfiguracion tipoCambio,
            Integer versionAnterior,
            Integer versionNueva,
            String motivo,
            String cambiosJson,
            Long usuarioId,
            String username,
            String ipOrigen
    );

    Page<HistorialConfiguracionResponse> listarHistorial(TipoEntidadConfiguracion entidadTipo, Pageable pageable);

    List<HistorialConfiguracionResponse> listarPorEntidad(TipoEntidadConfiguracion entidadTipo, Long entidadId);
}
