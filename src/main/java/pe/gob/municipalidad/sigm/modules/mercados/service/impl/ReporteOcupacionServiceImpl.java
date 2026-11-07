package pe.gob.municipalidad.sigm.modules.mercados.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.mercados.dto.IndicadorOcupacionResponse;
import pe.gob.municipalidad.sigm.modules.mercados.dto.PuestoReporteItem;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ReporteMercadoFilter;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoAsignacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;
import pe.gob.municipalidad.sigm.modules.mercados.service.ReporteOcupacionService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReporteOcupacionServiceImpl implements ReporteOcupacionService {

    private final PuestoComercialRepository puestoRepository;
    private final MercadoMunicipalRepository mercadoRepository;
    private final SectorMercadoRepository sectorRepository;
    private final PabellonMercadoRepository pabellonRepository;
    private final AsignacionPuestoRepository asignacionRepository;

    public ReporteOcupacionServiceImpl(
            PuestoComercialRepository puestoRepository,
            MercadoMunicipalRepository mercadoRepository,
            SectorMercadoRepository sectorRepository,
            PabellonMercadoRepository pabellonRepository,
            AsignacionPuestoRepository asignacionRepository) {
        this.puestoRepository = puestoRepository;
        this.mercadoRepository = mercadoRepository;
        this.sectorRepository = sectorRepository;
        this.pabellonRepository = pabellonRepository;
        this.asignacionRepository = asignacionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public IndicadorOcupacionResponse obtenerIndicadorOcupacion(ReporteMercadoFilter filtro) {
        UUID mercadoId = filtro != null ? filtro.getMercadoId() : null;
        UUID sectorId = filtro != null ? filtro.getSectorId() : null;
        UUID pabellonId = filtro != null ? filtro.getPabellonId() : null;

        List<PuestoComercial> puestos = puestoRepository.findAll().stream()
                .filter(p -> (mercadoId == null || p.getMercado().getId().equals(mercadoId)) &&
                             (sectorId == null || (p.getSector() != null && p.getSector().getId().equals(sectorId))) &&
                             (pabellonId == null || (p.getPabellon() != null && p.getPabellon().getId().equals(pabellonId))))
                .collect(Collectors.toList());

        long total = puestos.size();
        long ocupados = puestos.stream().filter(p -> p.getEstado() == EstadoPuesto.OCUPADO).count();
        long disponibles = puestos.stream().filter(p -> p.getEstado() == EstadoPuesto.DISPONIBLE).count();
        long mantenimiento = puestos.stream().filter(p -> p.getEstado() == EstadoPuesto.EN_MANTENIMIENTO).count();
        long restringidos = puestos.stream().filter(p -> p.getEstado() == EstadoPuesto.INHABILITADO || p.getEstado() == EstadoPuesto.RESERVADO).count();
        long inactivos = puestos.stream().filter(p -> p.getEstado() == EstadoPuesto.NO_ASIGNABLE).count();

        long habilitados = total - inactivos;
        BigDecimal pct = BigDecimal.ZERO;
        String mensaje;
        if (habilitados > 0) {
            pct = BigDecimal.valueOf(ocupados).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(habilitados), 1, RoundingMode.HALF_UP);
            mensaje = pct + "% de puestos ocupados respecto al total habilitado";
        } else {
            mensaje = "Sin datos suficientes";
        }

        // Distribución por estado
        List<Map<String, Object>> distribucion = new ArrayList<>();
        distribucion.add(crearItemDistribucion("Ocupados", ocupados, "#10b981"));
        distribucion.add(crearItemDistribucion("Disponibles", disponibles, "#3b82f6"));
        distribucion.add(crearItemDistribucion("Mantenimiento", mantenimiento, "#f59e0b"));
        distribucion.add(crearItemDistribucion("Restringidos/Clausurados", restringidos, "#ef4444"));
        distribucion.add(crearItemDistribucion("Inactivos", inactivos, "#6b7280"));

        // Ocupación por Mercado
        List<Map<String, Object>> porMercado = mercadoRepository.findAll().stream().map(m -> {
            List<PuestoComercial> pList = puestoRepository.findByMercadoId(m.getId());
            long o = pList.stream().filter(p -> p.getEstado() == EstadoPuesto.OCUPADO).count();
            long d = pList.stream().filter(p -> p.getEstado() == EstadoPuesto.DISPONIBLE).count();
            long h = pList.stream().filter(p -> p.getEstado() != EstadoPuesto.NO_ASIGNABLE).count();
            long otros = pList.size() - o - d;
            BigDecimal pOcup = h > 0 ? BigDecimal.valueOf(o).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(h), 1, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            Map<String, Object> map = new HashMap<>();
            map.put("mercadoId", m.getId());
            map.put("mercadoNombre", m.getNombre());
            map.put("nombre", m.getNombre());
            map.put("total", pList.size());
            map.put("totalPuestos", pList.size());
            map.put("ocupados", o);
            map.put("disponibles", d);
            map.put("otros", otros);
            map.put("porcentaje", pOcup);
            map.put("porcentajeTexto", pOcup + "%");
            return map;
        }).collect(Collectors.toList());

        // Ocupación por Sector (si se seleccionó mercado o global)
        List<SectorMercado> sectores = mercadoId != null ? sectorRepository.findByMercadoIdOrderByOrdenVisualAsc(mercadoId) : sectorRepository.findAll();
        List<Map<String, Object>> porSector = sectores.stream().map(s -> {
            List<PuestoComercial> pList = puestoRepository.findBySectorIdOrderByCodigoAsc(s.getId());
            long o = pList.stream().filter(p -> p.getEstado() == EstadoPuesto.OCUPADO).count();
            long d = pList.stream().filter(p -> p.getEstado() == EstadoPuesto.DISPONIBLE).count();
            long h = pList.stream().filter(p -> p.getEstado() != EstadoPuesto.NO_ASIGNABLE).count();
            long otros = pList.size() - o - d;
            BigDecimal pOcup = h > 0 ? BigDecimal.valueOf(o).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(h), 1, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            Map<String, Object> map = new HashMap<>();
            map.put("sectorId", s.getId());
            map.put("sectorNombre", s.getNombre());
            map.put("nombre", s.getNombre());
            map.put("mercadoNombre", s.getMercado() != null ? s.getMercado().getNombre() : "-");
            map.put("total", pList.size());
            map.put("totalPuestos", pList.size());
            map.put("ocupados", o);
            map.put("disponibles", d);
            map.put("otros", otros);
            map.put("porcentaje", pOcup);
            map.put("porcentajeTexto", pOcup + "%");
            return map;
        }).collect(Collectors.toList());

        // Ocupación por Pabellón
        List<PabellonMercado> pabellones = mercadoId != null ? pabellonRepository.findByMercadoIdOrderByOrdenVisualAsc(mercadoId) : pabellonRepository.findAll();
        List<Map<String, Object>> porPabellon = pabellones.stream().map(pab -> {
            List<PuestoComercial> pList = puestoRepository.findByPabellonIdOrderByCodigoAsc(pab.getId());
            long o = pList.stream().filter(p -> p.getEstado() == EstadoPuesto.OCUPADO).count();
            long d = pList.stream().filter(p -> p.getEstado() == EstadoPuesto.DISPONIBLE).count();
            long h = pList.stream().filter(p -> p.getEstado() != EstadoPuesto.NO_ASIGNABLE).count();
            long otros = pList.size() - o - d;
            BigDecimal pOcup = h > 0 ? BigDecimal.valueOf(o).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(h), 1, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            Map<String, Object> map = new HashMap<>();
            map.put("pabellonId", pab.getId());
            map.put("pabellonNombre", pab.getNombre());
            map.put("nombre", pab.getNombre());
            map.put("mercadoNombre", pab.getMercado() != null ? pab.getMercado().getNombre() : "-");
            map.put("total", pList.size());
            map.put("totalPuestos", pList.size());
            map.put("ocupados", o);
            map.put("disponibles", d);
            map.put("otros", otros);
            map.put("porcentaje", pOcup);
            map.put("porcentajeTexto", pOcup + "%");
            return map;
        }).collect(Collectors.toList());

        return IndicadorOcupacionResponse.builder()
                .totalPuestos(total)
                .puestosHabilitados(habilitados)
                .puestosOcupados(ocupados)
                .puestosDisponibles(disponibles)
                .puestosMantenimiento(mantenimiento)
                .puestosRestringidos(restringidos)
                .puestosInactivos(inactivos)
                .porcentajeOcupacion(pct)
                .mensajeOcupacion(mensaje)
                .distribucionPorEstado(distribucion)
                .ocupacionPorMercado(porMercado)
                .ocupacionPorSector(porSector)
                .ocupacionPorPabellon(porPabellon)
                .build();
    }

    private Map<String, Object> crearItemDistribucion(String label, long cantidad, String color) {
        Map<String, Object> map = new HashMap<>();
        map.put("label", label);
        map.put("cantidad", cantidad);
        map.put("color", color);
        return map;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PuestoReporteItem> listarPuestos(ReporteMercadoFilter filtro, Pageable pageable) {
        List<PuestoReporteItem> todos = listarTodosPuestos(filtro);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), todos.size());
        List<PuestoReporteItem> sublist = start <= end ? todos.subList(start, end) : Collections.emptyList();
        return new PageImpl<>(sublist, pageable, todos.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PuestoReporteItem> listarTodosPuestos(ReporteMercadoFilter filtro) {
        UUID mercadoId = filtro != null ? filtro.getMercadoId() : null;
        UUID sectorId = filtro != null ? filtro.getSectorId() : null;
        UUID pabellonId = filtro != null ? filtro.getPabellonId() : null;
        String estadoStr = filtro != null ? filtro.getEstado() : null;
        String termino = filtro != null && filtro.getTermino() != null ? filtro.getTermino().trim().toLowerCase() : null;

        List<PuestoComercial> puestos = puestoRepository.findAll();
        Map<UUID, AsignacionPuesto> asignacionesActivas = asignacionRepository.findAll().stream()
                .filter(a -> a.getEstado() == EstadoAsignacion.VIGENTE && a.getPuesto() != null)
                .collect(Collectors.toMap(a -> a.getPuesto().getId(), a -> a, (a1, a2) -> a1));

        return puestos.stream()
                .filter(p -> (mercadoId == null || p.getMercado().getId().equals(mercadoId)) &&
                             (sectorId == null || (p.getSector() != null && p.getSector().getId().equals(sectorId))) &&
                             (pabellonId == null || (p.getPabellon() != null && p.getPabellon().getId().equals(pabellonId))) &&
                             (estadoStr == null || estadoStr.isBlank() || p.getEstado().name().equalsIgnoreCase(estadoStr)) &&
                             (termino == null || p.getCodigo().toLowerCase().contains(termino) ||
                              p.getNumero().toLowerCase().contains(termino) ||
                              (p.getNombreReferencial() != null && p.getNombreReferencial().toLowerCase().contains(termino))))
                .sorted(Comparator.comparing((PuestoComercial p) -> p.getMercado().getNombre()).thenComparing(PuestoComercial::getCodigo))
                .map(p -> {
                    AsignacionPuesto asig = asignacionesActivas.get(p.getId());
                    String titular = asig != null && asig.getComerciante() != null ? asig.getComerciante().getNombreOTitular() : "-";
                    String giro = "-";
                    if (asig != null && asig.getComerciante() != null && asig.getComerciante().getActividades() != null && !asig.getComerciante().getActividades().isEmpty()) {
                        giro = asig.getComerciante().getActividades().iterator().next().getNombre();
                    }

                    return PuestoReporteItem.builder()
                            .id(p.getId())
                            .codigo(p.getCodigo())
                            .numero(p.getNumero())
                            .mercadoNombre(p.getMercado().getNombre())
                            .sectorNombre(p.getSector() != null ? p.getSector().getNombre() : "General")
                            .pabellonNombre(p.getPabellon() != null ? p.getPabellon().getNombre() : "General")
                            .tipoPuesto(p.getTipoPuesto() != null ? p.getTipoPuesto().getNombre() : "Estándar")
                            .areaM2(p.getAreaMetrosCuadrados())
                            .estadoFisico(p.getEstadoFisico().name())
                            .estadoOcupacion(p.getEstado().name())
                            .titularActual(titular)
                            .giroActual(giro)
                            .build();
                }).collect(Collectors.toList());
    }
}
