package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.mercados.entity.Comerciante;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoComerciante;
import pe.gob.municipalidad.sigm.modules.mercados.dto.DisponibilidadPuestoResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.AsignacionPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.entity.PuestoComercial;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoAsignacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoMercado;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.repository.AsignacionPuestoRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.ComercianteRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.PuestoComercialRepository;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class DisponibilidadPuestoService {

    private final PuestoComercialRepository puestoRepository;
    private final AsignacionPuestoRepository asignacionRepository;
    private final ComercianteRepository comercianteRepository;

    public DisponibilidadPuestoService(
            PuestoComercialRepository puestoRepository,
            AsignacionPuestoRepository asignacionRepository,
            ComercianteRepository comercianteRepository) {
        this.puestoRepository = puestoRepository;
        this.asignacionRepository = asignacionRepository;
        this.comercianteRepository = comercianteRepository;
    }

    @Transactional(readOnly = true)
    public DisponibilidadPuestoResponse consultarDisponibilidad(UUID puestoId) {
        PuestoComercial puesto = puestoRepository.findById(puestoId)
                .orElseThrow(() -> new ResourceNotFoundException("Puesto comercial no encontrado con ID: " + puestoId));

        boolean disponible = true;
        String motivoNoDisponible = null;

        if (puesto.getMercado() == null || puesto.getMercado().getEstado() != EstadoMercado.ACTIVO) {
            disponible = false;
            motivoNoDisponible = "El mercado municipal no se encuentra activo.";
        } else if (puesto.getEstado() != EstadoPuesto.DISPONIBLE) {
            disponible = false;
            motivoNoDisponible = "El puesto se encuentra en estado físico/operativo: " + puesto.getEstado();
        }

        Optional<AsignacionPuesto> vigenteOpt = asignacionRepository.findFirstByPuestoIdAndEstado(puestoId, EstadoAsignacion.VIGENTE);
        String ocupanteCodigo = null;
        String ocupanteNombre = null;
        String fechaInicioOcupacion = null;
        String fechaFinOcupacion = null;

        if (vigenteOpt.isPresent()) {
            disponible = false;
            AsignacionPuesto asig = vigenteOpt.get();
            motivoNoDisponible = "El puesto cuenta con ocupación vigente (Asignación: " + asig.getCodigoAsignacion() + ").";
            if (asig.getComerciante() != null) {
                ocupanteCodigo = asig.getComerciante().getCodigoComerciante();
                ocupanteNombre = asig.getComerciante().getPersona() != null ?
                        asig.getComerciante().getPersona().getNombreCompleto() :
                        (asig.getComerciante().getPersonaJuridica() != null ? asig.getComerciante().getPersonaJuridica().getRazonSocial() : "Titular no disponible");
            }
            fechaInicioOcupacion = asig.getFechaInicio() != null ? asig.getFechaInicio().toString() : null;
            fechaFinOcupacion = asig.getFechaFin() != null ? asig.getFechaFin().toString() : "Indefinida";
        }

        return DisponibilidadPuestoResponse.builder()
                .puestoId(puesto.getId())
                .puestoCodigo(puesto.getCodigo())
                .puestoNumero(puesto.getNumero())
                .puestoEstado(puesto.getEstado().name())
                .disponible(disponible)
                .motivoNoDisponible(motivoNoDisponible)
                .areaMetrosCuadrados(puesto.getAreaMetrosCuadrados())
                .tipoPuestoNombre(puesto.getTipoPuesto() != null ? puesto.getTipoPuesto().getNombre() : null)
                .mercadoId(puesto.getMercado() != null ? puesto.getMercado().getId() : null)
                .mercadoNombre(puesto.getMercado() != null ? puesto.getMercado().getNombre() : null)
                .sectorNombre(puesto.getSector() != null ? puesto.getSector().getNombre() : null)
                .pabellonNombre(puesto.getPabellon() != null ? puesto.getPabellon().getNombre() : null)
                .ocupanteActualCodigo(ocupanteCodigo)
                .ocupanteActualNombre(ocupanteNombre)
                .fechaInicioOcupacion(fechaInicioOcupacion)
                .fechaFinOcupacion(fechaFinOcupacion)
                .build();
    }

    @Transactional(readOnly = true)
    public void validarComercianteAptoParaAsignacion(UUID comercianteId) {
        Comerciante comerciante = comercianteRepository.findById(comercianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Comerciante no encontrado con ID: " + comercianteId));

        if (comerciante.getEstado() != EstadoComerciante.ACTIVO) {
            throw new BusinessException("El comerciante no se encuentra en estado ACTIVO. Estado actual: " + comerciante.getEstado());
        }

        // Regla institucional: Verificación de límite de puestos por comerciante (configurable, ej. máximo 2 puestos vigentes)
        long puestosVigentes = asignacionRepository.countByComercianteIdAndEstado(comercianteId, EstadoAsignacion.VIGENTE);
        if (puestosVigentes >= 2) {
            throw new BusinessException("El comerciante ya cuenta con " + puestosVigentes + " puestos vigentes asignados. " +
                    "Conforme a las directivas de equidad municipal en mercados, el límite por comerciante es de 2 puestos.");
        }
    }
}
