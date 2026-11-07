package pe.gob.municipalidad.sigm.modules.serenazgo.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.CatalogoSeguridadResponse;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.ItemCatalogoResponse;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.TipoIncidenciaResponse;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.CargoOperativoSerenazgoRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.TipoIncidenciaSeguridadRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.TurnoSerenazgoRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.CatalogoSeguridadService;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogoSeguridadServiceImpl implements CatalogoSeguridadService {

    private final CargoOperativoSerenazgoRepository cargoRepository;
    private final TipoIncidenciaSeguridadRepository tipoIncidenciaRepository;
    private final TurnoSerenazgoRepository turnoRepository;

    @Override
    public CatalogoSeguridadResponse obtenerCatalogosCompletos() {
        return CatalogoSeguridadResponse.builder()
                .cargosOperativos(obtenerCargosOperativos())
                .tiposIncidencia(obtenerTiposIncidenciaActivos())
                .turnos(obtenerTurnos())
                .prioridades(obtenerPrioridades())
                .estadosIncidencia(obtenerEstadosIncidencia())
                .canalesReporte(obtenerCanalesReporte())
                .tiposUnidad(obtenerTiposUnidad())
                .estadosUnidad(obtenerEstadosUnidad())
                .tiposPatrullaje(obtenerTiposPatrullaje())
                .resultadosIntervencion(obtenerResultadosIntervencion())
                .build();
    }

    @Override
    public List<TipoIncidenciaResponse> obtenerTiposIncidenciaActivos() {
        return tipoIncidenciaRepository.findByActivoTrueOrderByNombreAsc().stream()
                .map(t -> TipoIncidenciaResponse.builder()
                        .id(t.getId())
                        .codigo(t.getCodigo())
                        .nombre(t.getNombre())
                        .descripcion(t.getDescripcion())
                        .categoria(t.getCategoria().name())
                        .prioridadSugerida(t.getPrioridadSugerida().name())
                        .requiereUnidad(t.getRequiereUnidad())
                        .activo(t.getActivo())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemCatalogoResponse> obtenerCargosOperativos() {
        return cargoRepository.findByActivoTrueOrderByNivelJerarquicoAsc().stream()
                .map(c -> ItemCatalogoResponse.builder()
                        .codigo(c.getCodigo())
                        .nombre(c.getNombre())
                        .descripcion(c.getDescripcion())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemCatalogoResponse> obtenerTurnos() {
        return turnoRepository.findByEstadoOrderByHoraInicioAsc(EstadoTurnoSerenazgo.ACTIVO).stream()
                .map(t -> ItemCatalogoResponse.builder()
                        .codigo(t.getCodigo())
                        .nombre(t.getNombre())
                        .descripcion(String.format("%s a %s%s", t.getHoraInicio(), t.getHoraFin(),
                                t.getCruzaMedianoche() ? " (Cruza medianoche)" : ""))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemCatalogoResponse> obtenerPrioridades() {
        return Arrays.stream(PrioridadIncidenciaSeguridad.values())
                .map(p -> new ItemCatalogoResponse(p.name(), p.name(), p.getDescripcion()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemCatalogoResponse> obtenerEstadosIncidencia() {
        return Arrays.stream(EstadoIncidenciaSeguridad.values())
                .map(e -> new ItemCatalogoResponse(e.name(), e.name(), e.getDescripcion()))
                .collect(Collectors.toList());
    }

    private List<ItemCatalogoResponse> obtenerCanalesReporte() {
        return Arrays.stream(CanalReporteIncidencia.values())
                .map(c -> new ItemCatalogoResponse(c.name(), c.name(), c.getDescripcion()))
                .collect(Collectors.toList());
    }

    private List<ItemCatalogoResponse> obtenerTiposUnidad() {
        return Arrays.stream(TipoUnidadMovil.values())
                .map(t -> new ItemCatalogoResponse(t.name(), t.name(), t.getDescripcion()))
                .collect(Collectors.toList());
    }

    private List<ItemCatalogoResponse> obtenerEstadosUnidad() {
        return Arrays.stream(EstadoUnidadMovil.values())
                .map(e -> new ItemCatalogoResponse(e.name(), e.name(), e.getDescripcion()))
                .collect(Collectors.toList());
    }

    private List<ItemCatalogoResponse> obtenerTiposPatrullaje() {
        return Arrays.stream(TipoPatrullajeSerenazgo.values())
                .map(t -> new ItemCatalogoResponse(t.name(), t.name(), t.getDescripcion()))
                .collect(Collectors.toList());
    }

    private List<ItemCatalogoResponse> obtenerResultadosIntervencion() {
        return Arrays.stream(ResultadoIntervencionSerenazgo.values())
                .map(r -> new ItemCatalogoResponse(r.name(), r.name(), r.getDescripcion()))
                .collect(Collectors.toList());
    }
}
