package pe.gob.municipalidad.sigm.modules.mesapartes.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.ConfiguracionMpvDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.PlantillaDocumental;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoCambioConfiguracion;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoDocumentalPlantilla;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoEntidadConfiguracion;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.PlantillaDocumentalRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.ConfiguracionHistorialService;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.PlantillasDocumentalesService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PlantillasDocumentalesServiceImpl implements PlantillasDocumentalesService {

    private static final Logger log = LoggerFactory.getLogger(PlantillasDocumentalesServiceImpl.class);

    private static final Set<String> VARIABLES_PERMITIDAS = Set.of(
            "numero_expediente",
            "fecha",
            "solicitante_nombre",
            "solicitante_doc",
            "asunto",
            "oficina",
            "oficina_origen",
            "oficina_destino",
            "plazo_dias",
            "codigo_seguimiento",
            "resultado_final",
            "detalle_observacion",
            "fecha_limite",
            "motivo",
            "numero_resolucion"
    );

    private final PlantillaDocumentalRepository plantillaRepository;
    private final ConfiguracionHistorialService historialService;

    public PlantillasDocumentalesServiceImpl(
            PlantillaDocumentalRepository plantillaRepository,
            ConfiguracionHistorialService historialService
    ) {
        this.plantillaRepository = plantillaRepository;
        this.historialService = historialService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlantillaAdminResponse> listarPlantillas() {
        return plantillaRepository.findAllByOrderByCodigoAsc()
                .stream()
                .map(this::mapearResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PlantillaAdminResponse obtenerPorId(Long id) {
        PlantillaDocumental p = plantillaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plantilla documental no encontrada con ID: " + id));
        return mapearResponse(p);
    }

    @Override
    @Transactional(readOnly = true)
    public PlantillaAdminResponse obtenerPorTipo(TipoDocumentalPlantilla tipoDocumental) {
        return plantillaRepository.findByTipoDocumentalAndEstado(tipoDocumental, "PUBLICADO")
                .stream()
                .findFirst()
                .map(this::mapearResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró plantilla activa para el tipo: " + tipoDocumental));
    }

    @Override
    @Transactional
    public PlantillaAdminResponse guardarPlantilla(GuardarPlantillaRequest request, String username, String ipOrigen) {
        if (plantillaRepository.existsByCodigoIgnoreCase(request.getCodigo())) {
            throw new IllegalArgumentException("Ya existe una plantilla registrada con el código: " + request.getCodigo());
        }

        validarCuerpoSeguro(request.getCuerpoPlantilla());

        PlantillaDocumental p = PlantillaDocumental.builder()
                .codigo(request.getCodigo().trim().toUpperCase())
                .nombre(request.getNombre().trim())
                .descripcion(request.getDescripcion())
                .tipoDocumental(request.getTipoDocumental())
                .cuerpoPlantilla(request.getCuerpoPlantilla())
                .version(1)
                .estado(request.getEstado() != null ? request.getEstado() : "PUBLICADO")
                .createdBy(username)
                .build();

        PlantillaDocumental guardada = plantillaRepository.save(p);

        historialService.registrarCambio(
                TipoEntidadConfiguracion.PLANTILLA,
                guardada.getId(),
                guardada.getCodigo(),
                TipoCambioConfiguracion.CREACION,
                null,
                1,
                "Creación de plantilla documental " + guardada.getCodigo(),
                null,
                null,
                username,
                ipOrigen
        );

        return mapearResponse(guardada);
    }

    @Override
    @Transactional
    public PlantillaAdminResponse actualizarPlantilla(Long id, GuardarPlantillaRequest request, String username, String ipOrigen) {
        PlantillaDocumental p = plantillaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plantilla documental no encontrada con ID: " + id));

        validarCuerpoSeguro(request.getCuerpoPlantilla());

        int verAnt = p.getVersion() != null ? p.getVersion() : 1;
        int verNueva = verAnt + 1;

        p.setNombre(request.getNombre().trim());
        p.setDescripcion(request.getDescripcion());
        p.setTipoDocumental(request.getTipoDocumental());
        p.setCuerpoPlantilla(request.getCuerpoPlantilla());
        p.setVersion(verNueva);
        p.setEstado(request.getEstado() != null ? request.getEstado() : "PUBLICADO");
        p.setUpdatedBy(username);

        PlantillaDocumental actualizada = plantillaRepository.save(p);

        historialService.registrarCambio(
                TipoEntidadConfiguracion.PLANTILLA,
                actualizada.getId(),
                actualizada.getCodigo(),
                TipoCambioConfiguracion.VERSIONADO,
                verAnt,
                verNueva,
                "Actualización y nueva versión de plantilla documental (v" + verNueva + ")",
                null,
                null,
                username,
                ipOrigen
        );

        return mapearResponse(actualizada);
    }

    @Override
    @Transactional(readOnly = true)
    public String previsualizarContenido(Long id, Map<String, String> variables) {
        PlantillaDocumental p = plantillaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plantilla documental no encontrada con ID: " + id));

        String contenido = p.getCuerpoPlantilla();
        if (contenido == null) {
            return "";
        }

        Map<String, String> valores = variables != null ? variables : Map.of();

        for (String var : VARIABLES_PERMITIDAS) {
            String token = "{{" + var + "}}";
            String reemplazo = valores.getOrDefault(var, "[DATO_MUESTRA_" + var.toUpperCase() + "]");
            contenido = contenido.replace(token, reemplazo);
        }

        return contenido;
    }

    private void validarCuerpoSeguro(String cuerpo) {
        if (cuerpo == null) {
            return;
        }
        String lower = cuerpo.toLowerCase();
        if (lower.contains("<script") || lower.contains("javascript:") || lower.contains("<iframe")) {
            throw new IllegalArgumentException("El contenido de la plantilla contiene código no permitido.");
        }
    }

    private PlantillaAdminResponse mapearResponse(PlantillaDocumental p) {
        return PlantillaAdminResponse.builder()
                .id(p.getId())
                .codigo(p.getCodigo())
                .nombre(p.getNombre())
                .descripcion(p.getDescripcion())
                .tipoDocumental(p.getTipoDocumental())
                .cuerpoPlantilla(p.getCuerpoPlantilla())
                .version(p.getVersion())
                .estado(p.getEstado())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
