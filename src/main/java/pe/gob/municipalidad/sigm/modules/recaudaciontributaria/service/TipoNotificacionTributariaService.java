package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.NormaTributariaConfigurada;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.TipoNotificacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.CategoriaTipoNotificacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.NormaTributariaConfiguradaRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.TipoNotificacionTributariaRepository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TipoNotificacionTributariaService {

    private final TipoNotificacionTributariaRepository tipoRepository;
    private final NormaTributariaConfiguradaRepository normaRepository;

    @Transactional(readOnly = true)
    public List<TipoNotificacionTributaria> listarActivos() {
        return tipoRepository.findByEstadoOrderByNombreAsc("ACTIVO");
    }

    @Transactional(readOnly = true)
    public TipoNotificacionTributaria obtenerPorId(UUID id) {
        return tipoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Tipo de notificación no encontrado con ID: " + id));
    }

    @Transactional(readOnly = true)
    public TipoNotificacionTributaria obtenerPorCodigo(String codigo) {
        return tipoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new BusinessException("Tipo de notificación no encontrado con código: " + codigo));
    }

    @Transactional
    public TipoNotificacionTributaria crearTipo(String codigo, String nombre, String descripcion,
                                                CategoriaTipoNotificacion categoria, boolean requiereDoc,
                                                boolean requiereValLegal, boolean requiereEvid,
                                                UUID normaId, String username) {
        if (tipoRepository.existsByCodigo(codigo)) {
            throw new BusinessException("Ya existe un tipo de notificación con el código: " + codigo);
        }

        NormaTributariaConfigurada norma = null;
        if (normaId != null) {
            norma = normaRepository.findById(normaId).orElse(null);
        }

        TipoNotificacionTributaria tipo = TipoNotificacionTributaria.builder()
                .codigo(codigo)
                .nombre(nombre)
                .descripcion(descripcion)
                .categoria(categoria)
                .requiereDocumento(requiereDoc)
                .requiereValidacionLegal(requiereValLegal)
                .requiereEvidencia(requiereEvid)
                .norma(norma)
                .estado("ACTIVO")
                .build();
        tipo.setCreatedBy(username);
        return tipoRepository.save(tipo);
    }
}

