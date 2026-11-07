package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.entity.MercadoMunicipal;
import pe.gob.municipalidad.sigm.modules.mercados.entity.PabellonMercado;
import pe.gob.municipalidad.sigm.modules.mercados.entity.PuestoComercial;
import pe.gob.municipalidad.sigm.modules.mercados.entity.SectorMercado;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoMercado;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EstructuraMercadoService {

    private final MercadoMunicipalRepository mercadoRepository;
    private final SectorMercadoRepository sectorRepository;
    private final PabellonMercadoRepository pabellonRepository;
    private final PuestoComercialRepository puestoRepository;
    private final ZonaComercialRepository zonaRepository;
    private final SectorMercadoService sectorService;
    private final PabellonMercadoService pabellonService;
    private final ZonaComercialService zonaService;

    public EstructuraMercadoService(
            MercadoMunicipalRepository mercadoRepository,
            SectorMercadoRepository sectorRepository,
            PabellonMercadoRepository pabellonRepository,
            PuestoComercialRepository puestoRepository,
            ZonaComercialRepository zonaRepository,
            SectorMercadoService sectorService,
            PabellonMercadoService pabellonService,
            ZonaComercialService zonaService) {
        this.mercadoRepository = mercadoRepository;
        this.sectorRepository = sectorRepository;
        this.pabellonRepository = pabellonRepository;
        this.puestoRepository = puestoRepository;
        this.zonaRepository = zonaRepository;
        this.sectorService = sectorService;
        this.pabellonService = pabellonService;
        this.zonaService = zonaService;
    }

    @Transactional(readOnly = true)
    public EstructuraMercadoResponse obtenerEstructuraCompleta(UUID mercadoId) {
        MercadoMunicipal mercado = mercadoRepository.findById(mercadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Mercado municipal no encontrado con ID: " + mercadoId));

        // 1. Sectores organizados jerárquicamente
        List<SectorMercado> sectoresEntidades = sectorRepository.findByMercadoIdOrderByOrdenVisualAsc(mercadoId);
        List<SectorEstructuraNodo> sectoresNodos = new ArrayList<>();

        for (SectorMercado sector : sectoresEntidades) {
            SectorResponse sectorResp = sectorService.toResponse(sector);

            // Pabellones del sector
            List<PabellonMercado> pabellonesDelSector = pabellonRepository.findBySectorIdOrderByOrdenVisualAsc(sector.getId());
            List<PabellonEstructuraNodo> pabellonesNodos = new ArrayList<>();

            for (PabellonMercado pab : pabellonesDelSector) {
                PabellonResponse pabResp = pabellonService.toResponse(pab);
                List<PuestoResumenNodo> puestosPabellon = puestoRepository.findByPabellonIdOrderByCodigoAsc(pab.getId())
                        .stream()
                        .map(this::toPuestoNodo)
                        .collect(Collectors.toList());

                pabellonesNodos.add(PabellonEstructuraNodo.builder()
                        .pabellon(pabResp)
                        .puestos(puestosPabellon)
                        .build());
            }

            // Puestos directos del sector (sin pabellón)
            List<PuestoResumenNodo> puestosDirectosSector = puestoRepository.findBySectorIdAndPabellonIdIsNullOrderByCodigoAsc(sector.getId())
                    .stream()
                    .map(this::toPuestoNodo)
                    .collect(Collectors.toList());

            sectoresNodos.add(SectorEstructuraNodo.builder()
                    .sector(sectorResp)
                    .pabellones(pabellonesNodos)
                    .puestosDirectos(puestosDirectosSector)
                    .build());
        }

        // 2. Pabellones sin sector (directos bajo el mercado)
        List<PabellonMercado> pabellonesSinSector = pabellonRepository.findByMercadoIdAndSectorIdIsNullOrderByOrdenVisualAsc(mercadoId);
        List<PabellonEstructuraNodo> pabellonesSinSectorNodos = new ArrayList<>();

        for (PabellonMercado pab : pabellonesSinSector) {
            PabellonResponse pabResp = pabellonService.toResponse(pab);
            List<PuestoResumenNodo> puestosPabellon = puestoRepository.findByPabellonIdOrderByCodigoAsc(pab.getId())
                    .stream()
                    .map(this::toPuestoNodo)
                    .collect(Collectors.toList());

            pabellonesSinSectorNodos.add(PabellonEstructuraNodo.builder()
                    .pabellon(pabResp)
                    .puestos(puestosPabellon)
                    .build());
        }

        // 3. Puestos directos del mercado (sin sector ni pabellón)
        List<PuestoResumenNodo> puestosDirectosMercado = puestoRepository.findByMercadoIdAndSectorIdIsNullAndPabellonIdIsNullOrderByCodigoAsc(mercadoId)
                .stream()
                .map(this::toPuestoNodo)
                .collect(Collectors.toList());

        // 4. Zonas comerciales y áreas comunes
        List<ZonaResponse> zonas = zonaRepository.findByMercadoIdOrderByOrdenVisualAsc(mercadoId)
                .stream()
                .map(zonaService::toResponse)
                .collect(Collectors.toList());

        // 5. Estadísticas agregadas
        EstructuraEstadisticasResponse estadisticas = obtenerEstadisticas(mercadoId);

        return EstructuraMercadoResponse.builder()
                .mercadoId(mercado.getId())
                .mercadoCodigo(mercado.getCodigo())
                .mercadoNombre(mercado.getNombre())
                .mercadoEstado(mercado.getEstado() != null ? mercado.getEstado().name() : null)
                .estadisticas(estadisticas)
                .sectores(sectoresNodos)
                .pabellonesSinSector(pabellonesSinSectorNodos)
                .puestosDirectos(puestosDirectosMercado)
                .zonas(zonas)
                .build();
    }

    @Transactional(readOnly = true)
    public EstructuraEstadisticasResponse obtenerEstadisticas(UUID mercadoId) {
        MercadoMunicipal mercado = mercadoRepository.findById(mercadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Mercado municipal no encontrado con ID: " + mercadoId));

        long totalSectores = sectorRepository.countByMercadoId(mercadoId);
        long sectoresActivos = sectorRepository.countByMercadoIdAndEstado(mercadoId, "ACTIVO");
        long sectoresInactivos = Math.max(0, totalSectores - sectoresActivos);

        long totalPabellones = pabellonRepository.countByMercadoId(mercadoId);
        long pabellonesActivos = pabellonRepository.countByMercadoIdAndEstado(mercadoId, "ACTIVO");
        long pabellonesInactivos = Math.max(0, totalPabellones - pabellonesActivos);

        long totalZonas = zonaRepository.countByMercadoId(mercadoId);
        long zonasActivas = zonaRepository.countByMercadoIdAndEstado(mercadoId, EstadoMercado.ACTIVO);
        long zonasAsignables = zonaRepository.countByMercadoIdAndEsAsignable(mercadoId, true);
        long zonasComunes = zonaRepository.countByMercadoIdAndEsAsignable(mercadoId, false);

        long totalPuestos = puestoRepository.countByMercadoId(mercadoId);
        long puestosDisponibles = puestoRepository.countByMercadoIdAndEstado(mercadoId, EstadoPuesto.DISPONIBLE);
        long puestosOcupados = puestoRepository.countByMercadoIdAndEstado(mercadoId, EstadoPuesto.OCUPADO);
        long puestosReservados = puestoRepository.countByMercadoIdAndEstado(mercadoId, EstadoPuesto.RESERVADO);
        long puestosMantenimiento = puestoRepository.countByMercadoIdAndEstado(mercadoId, EstadoPuesto.EN_MANTENIMIENTO);

        return EstructuraEstadisticasResponse.builder()
                .mercadoId(mercado.getId())
                .mercadoNombre(mercado.getNombre())
                .totalSectores(totalSectores)
                .sectoresActivos(sectoresActivos)
                .sectoresInactivos(sectoresInactivos)
                .totalPabellones(totalPabellones)
                .pabellonesActivos(pabellonesActivos)
                .pabellonesInactivos(pabellonesInactivos)
                .totalZonas(totalZonas)
                .zonasActivas(zonasActivas)
                .zonasAsignables(zonasAsignables)
                .zonasComunes(zonasComunes)
                .totalPuestos(totalPuestos)
                .puestosDisponibles(puestosDisponibles)
                .puestosOcupados(puestosOcupados)
                .puestosReservados(puestosReservados)
                .puestosMantenimiento(puestosMantenimiento)
                .build();
    }

    private PuestoResumenNodo toPuestoNodo(PuestoComercial p) {
        return PuestoResumenNodo.builder()
                .id(p.getId())
                .codigo(p.getCodigo())
                .numero(p.getNumero())
                .areaM2(p.getAreaMetrosCuadrados())
                .estado(p.getEstado() != null ? p.getEstado().name() : "DISPONIBLE")
                .tipoPuestoNombre(p.getTipoPuesto() != null ? p.getTipoPuesto().getNombre() : null)
                .build();
    }
}
