package pe.gob.municipalidad.sigm.modules.mercados;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ConflictException;
import pe.gob.municipalidad.sigm.common.exception.DuplicateResourceException;
import pe.gob.municipalidad.sigm.common.exception.InvalidRequestException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Municipalidad;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.repository.MunicipalidadRepository;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoMercado;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoZona;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;
import pe.gob.municipalidad.sigm.modules.mercados.service.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MercadoEstructuraFisicaServiceTest {

    @Autowired
    private SectorMercadoService sectorService;

    @Autowired
    private PabellonMercadoService pabellonService;

    @Autowired
    private ZonaComercialService zonaService;

    @Autowired
    private EstructuraMercadoService estructuraService;

    @Autowired
    private MercadoMunicipalRepository mercadoRepository;

    @Autowired
    private SectorMercadoRepository sectorRepository;

    @Autowired
    private PabellonMercadoRepository pabellonRepository;

    @Autowired
    private PuestoComercialRepository puestoRepository;

    @Autowired
    private ZonaComercialRepository zonaRepository;

    @Autowired
    private MunicipalidadRepository municipalidadRepository;

    @Autowired
    private OficinaRepository oficinaRepository;

    private MercadoMunicipal mercadoPrincipal;
    private MercadoMunicipal mercadoSecundario;
    private Oficina oficina;

    @BeforeEach
    void setUp() {
        Municipalidad mun = municipalidadRepository.findAll().stream().findFirst()
                .orElseGet(() -> municipalidadRepository.save(Municipalidad.builder()
                        .nombre("Municipalidad Distrital de Quinua")
                        .nombreCorto("MD-QUINUA")
                        .ubigeo("050108")
                        .departamento("Ayacucho")
                        .provincia("Huamanga")
                        .distrito("Quinua")
                        .build()));

        oficina = oficinaRepository.findAll().stream()
                .filter(o -> "ACTIVO".equalsIgnoreCase(o.getEstado()))
                .findFirst()
                .orElseGet(() -> oficinaRepository.save(Oficina.builder()
                        .municipalidad(mun)
                        .codigo("OFI-MERC-" + System.currentTimeMillis() % 10000)
                        .nombre("Oficina de Abastos y Comercialización")
                        .estado("ACTIVO")
                        .build()));

        mercadoPrincipal = mercadoRepository.save(MercadoMunicipal.builder()
                .codigo("MERC-QUINUA-TEST-" + UUID.randomUUID().toString().substring(0, 8))
                .nombre("Mercado Central de Quinua Test")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .direccion("Jr. Sucre s/n")
                .estado(EstadoMercado.ACTIVO)
                .oficinaAdministradora(oficina)
                .capacidadReferencial(100)
                .build());

        mercadoSecundario = mercadoRepository.save(MercadoMunicipal.builder()
                .codigo("MERC-SECUNDARIO-" + UUID.randomUUID().toString().substring(0, 8))
                .nombre("Mercado Zonal de Huayllapampa Test")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .direccion("Av. Los Libertadores s/n")
                .estado(EstadoMercado.ACTIVO)
                .oficinaAdministradora(oficina)
                .capacidadReferencial(50)
                .build());
    }

    // 1. Crear sector
    @Test
    @DisplayName("1. Crear sector con datos válidos")
    void testCrearSectorExitoso() {
        SectorRequest request = SectorRequest.builder()
                .mercadoId(mercadoPrincipal.getId())
                .codigo("SEC-CARNES")
                .nombre("Sector Carnes y Embutidos")
                .descripcion("Sección de venta de carnes rojas y blancas")
                .categoriaComercial("Carnes")
                .tipoSector("PERECIBLES")
                .ordenVisual(1)
                .build();

        SectorResponse response = sectorService.crearSector(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getCodigo()).isEqualTo("SEC-CARNES");
        assertThat(response.getNombre()).isEqualTo("Sector Carnes y Embutidos");
        assertThat(response.getCategoriaComercial()).isEqualTo("Carnes");
        assertThat(response.getEstado()).isEqualTo("ACTIVO");
    }

    // 2. Rechazar código duplicado dentro del mercado
    @Test
    @DisplayName("2. Rechazar código duplicado dentro del mismo mercado")
    void testRechazarCodigoDuplicadoMismoMercado() {
        SectorRequest request1 = SectorRequest.builder()
                .mercadoId(mercadoPrincipal.getId())
                .codigo("SEC-ABARROTES")
                .nombre("Sector Abarrotes Primer Nivel")
                .build();
        sectorService.crearSector(request1);

        SectorRequest request2 = SectorRequest.builder()
                .mercadoId(mercadoPrincipal.getId())
                .codigo("SEC-ABARROTES")
                .nombre("Sector Abarrotes Segundo Nivel")
                .build();

        assertThatThrownBy(() -> sectorService.crearSector(request2))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("ya existe en este mercado");
    }

    // 3. Permitir código equivalente en otro mercado
    @Test
    @DisplayName("3. Permitir código idéntico en mercados diferentes")
    void testPermitirCodigoIdenticoEnDistintoMercado() {
        SectorRequest reqMercado1 = SectorRequest.builder()
                .mercadoId(mercadoPrincipal.getId())
                .codigo("SEC-FRUTAS")
                .nombre("Sector Frutas Quinua")
                .build();
        SectorResponse resp1 = sectorService.crearSector(reqMercado1);

        SectorRequest reqMercado2 = SectorRequest.builder()
                .mercadoId(mercadoSecundario.getId())
                .codigo("SEC-FRUTAS")
                .nombre("Sector Frutas Huayllapampa")
                .build();
        SectorResponse resp2 = sectorService.crearSector(reqMercado2);

        assertThat(resp1.getCodigo()).isEqualTo(resp2.getCodigo());
        assertThat(resp1.getMercadoId()).isNotEqualTo(resp2.getMercadoId());
    }

    // 4. Validar mercado inexistente
    @Test
    @DisplayName("4. Rechazar sector con mercado inexistente")
    void testRechazarMercadoInexistente() {
        SectorRequest request = SectorRequest.builder()
                .mercadoId(UUID.randomUUID())
                .codigo("SEC-FANTASMA")
                .nombre("Sector Invalido")
                .build();

        assertThatThrownBy(() -> sectorService.crearSector(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Mercado municipal no encontrado");
    }

    // 5. Validar nombre obligatorio
    @Test
    @DisplayName("5. Rechazar sector sin nombre obligatorio")
    void testRechazarNombreObligatorio() {
        SectorRequest request = SectorRequest.builder()
                .mercadoId(mercadoPrincipal.getId())
                .codigo("SEC-VACIO")
                .nombre("   ")
                .build();

        assertThatThrownBy(() -> sectorService.crearSector(request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("nombre del sector es obligatorio");
    }

    // 6. Editar sector
    @Test
    @DisplayName("6. Editar campos permitidos del sector")
    void testEditarSector() {
        SectorResponse creado = sectorService.crearSector(SectorRequest.builder()
                .mercadoId(mercadoPrincipal.getId())
                .codigo("SEC-ARTESANIA")
                .nombre("Sector Artesanías")
                .ordenVisual(1)
                .build());

        SectorActualizacionRequest actReq = SectorActualizacionRequest.builder()
                .nombre("Sector Artesanías y Cerámica Tradicional")
                .descripcion("Venta de iglesias de quinua y telares")
                .categoriaComercial("Artesanías")
                .tipoSector("CULTURAL")
                .ordenVisual(5)
                .version(creado.getVersion())
                .build();

        SectorResponse actualizado = sectorService.actualizarSector(creado.getId(), mercadoPrincipal.getId(), actReq);

        assertThat(actualizado.getNombre()).isEqualTo("Sector Artesanías y Cerámica Tradicional");
        assertThat(actualizado.getDescripcion()).isEqualTo("Venta de iglesias de quinua y telares");
        assertThat(actualizado.getOrdenVisual()).isEqualTo(5);
    }

    // 7. Cambiar estado
    @Test
    @DisplayName("7. Cambiar estado de sector con motivo registrado")
    void testCambiarEstadoSector() {
        SectorResponse creado = sectorService.crearSector(SectorRequest.builder()
                .mercadoId(mercadoPrincipal.getId())
                .codigo("SEC-MANT")
                .nombre("Sector en Remodelación")
                .build());

        CambioEstadoEstructuraRequest cambioReq = CambioEstadoEstructuraRequest.builder()
                .nuevoEstado(EstadoMercado.EN_MANTENIMIENTO)
                .motivo("Mantenimiento preventivo del techo y pisos sanitarios")
                .build();

        SectorResponse modificado = sectorService.cambiarEstado(creado.getId(), mercadoPrincipal.getId(), cambioReq);

        assertThat(modificado.getEstado()).isEqualTo("EN_MANTENIMIENTO");
    }

    // 8. Rechazar transición inválida
    @Test
    @DisplayName("8. Rechazar transición al mismo estado actual")
    void testRechazarTransicionMismoEstado() {
        SectorResponse creado = sectorService.crearSector(SectorRequest.builder()
                .mercadoId(mercadoPrincipal.getId())
                .codigo("SEC-ACTIVO")
                .nombre("Sector Activo")
                .build());

        CambioEstadoEstructuraRequest cambioReq = CambioEstadoEstructuraRequest.builder()
                .nuevoEstado(EstadoMercado.ACTIVO)
                .motivo("Reactivar sector")
                .build();

        assertThatThrownBy(() -> sectorService.cambiarEstado(creado.getId(), mercadoPrincipal.getId(), cambioReq))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("ya se encuentra en estado");
    }

    // 9. Crear pabellón
    @Test
    @DisplayName("9. Crear pabellón exitosamente")
    void testCrearPabellonExitoso() {
        PabellonRequest request = PabellonRequest.builder()
                .mercadoId(mercadoPrincipal.getId())
                .codigo("PAB-CENTRAL")
                .nombre("Pabellón Central Techado")
                .nivel(1)
                .ordenVisual(1)
                .build();

        PabellonResponse response = pabellonService.crearPabellon(request);

        assertThat(response).isNotNull();
        assertThat(response.getCodigo()).isEqualTo("PAB-CENTRAL");
        assertThat(response.getNivel()).isEqualTo(1);
    }

    // 10. Asociar pabellón con sector
    @Test
    @DisplayName("10. Asociar pabellón a un sector del mismo mercado")
    void testAsociarPabellonConSector() {
        SectorResponse sector = sectorService.crearSector(SectorRequest.builder()
                .mercadoId(mercadoPrincipal.getId())
                .codigo("SEC-GASTRO")
                .nombre("Sector Gastronomía")
                .build());

        PabellonRequest request = PabellonRequest.builder()
                .mercadoId(mercadoPrincipal.getId())
                .sectorId(sector.getId())
                .codigo("PAB-COMEDOR")
                .nombre("Pabellón Comedor Principal")
                .nivel(1)
                .build();

        PabellonResponse response = pabellonService.crearPabellon(request);

        assertThat(response.getSectorId()).isEqualTo(sector.getId());
        assertThat(response.getSectorNombre()).isEqualTo("Sector Gastronomía");
    }

    // 11. Rechazar sector de otro mercado
    @Test
    @DisplayName("11. Rechazar asociación de pabellón con sector de otro mercado")
    void testRechazarSectorDeOtroMercado() {
        SectorResponse sectorMercado2 = sectorService.crearSector(SectorRequest.builder()
                .mercadoId(mercadoSecundario.getId())
                .codigo("SEC-REMOTO")
                .nombre("Sector Huayllapampa")
                .build());

        PabellonRequest requestIncoherente = PabellonRequest.builder()
                .mercadoId(mercadoPrincipal.getId())
                .sectorId(sectorMercado2.getId())
                .codigo("PAB-INVALIDO")
                .nombre("Pabellón Incoherente")
                .build();

        assertThatThrownBy(() -> pabellonService.crearPabellon(requestIncoherente))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Incoherencia estructural");
    }

    // 12. Crear pabellón sin sector
    @Test
    @DisplayName("12. Crear pabellón independiente sin sector")
    void testCrearPabellonSinSector() {
        PabellonRequest request = PabellonRequest.builder()
                .mercadoId(mercadoPrincipal.getId())
                .codigo("PAB-LIBRE")
                .nombre("Pabellón Nave Abierta")
                .nivel(2)
                .build();

        PabellonResponse response = pabellonService.crearPabellon(request);

        assertThat(response.getSectorId()).isNull();
        assertThat(response.getSectorNombre()).isNull();
    }

    // 13. Editar pabellón
    @Test
    @DisplayName("13. Editar campos y reasociar sector de pabellón")
    void testEditarPabellon() {
        SectorResponse sector1 = sectorService.crearSector(SectorRequest.builder()
                .mercadoId(mercadoPrincipal.getId())
                .codigo("SEC-A")
                .nombre("Sector A")
                .build());

        PabellonResponse pab = pabellonService.crearPabellon(PabellonRequest.builder()
                .mercadoId(mercadoPrincipal.getId())
                .sectorId(sector1.getId())
                .codigo("PAB-EDIT")
                .nombre("Pabellón Original")
                .nivel(1)
                .build());

        PabellonActualizacionRequest actReq = PabellonActualizacionRequest.builder()
                .nombre("Pabellón Remodelado")
                .descripcion("Nueva iluminación LED y techado")
                .nivel(2)
                .ordenVisual(3)
                .version(pab.getVersion())
                .build();

        PabellonResponse actResp = pabellonService.actualizarPabellon(pab.getId(), mercadoPrincipal.getId(), actReq);

        assertThat(actResp.getNombre()).isEqualTo("Pabellón Remodelado");
        assertThat(actResp.getNivel()).isEqualTo(2);
    }

    // 14. Cambiar estado de pabellón
    @Test
    @DisplayName("14. Cambiar estado de pabellón y verificar historial")
    void testCambiarEstadoPabellon() {
        PabellonResponse pab = pabellonService.crearPabellon(PabellonRequest.builder()
                .mercadoId(mercadoPrincipal.getId())
                .codigo("PAB-ESTADO")
                .nombre("Pabellón Estado Test")
                .build());

        CambioEstadoEstructuraRequest req = CambioEstadoEstructuraRequest.builder()
                .nuevoEstado(EstadoMercado.SUSPENDIDO)
                .motivo("Suspensión por inspección técnica de Defensa Civil")
                .build();

        PabellonResponse modificado = pabellonService.cambiarEstado(pab.getId(), mercadoPrincipal.getId(), req);

        assertThat(modificado.getEstado()).isEqualTo("SUSPENDIDO");
    }

    // 15. Consultar estructura
    @Test
    @DisplayName("15. Consultar árbol de estructura completo del mercado")
    void testConsultarEstructuraCompleta() {
        SectorResponse sec = sectorService.crearSector(SectorRequest.builder()
                .mercadoId(mercadoPrincipal.getId())
                .codigo("SEC-TREE")
                .nombre("Sector para Árbol")
                .build());

        pabellonService.crearPabellon(PabellonRequest.builder()
                .mercadoId(mercadoPrincipal.getId())
                .sectorId(sec.getId())
                .codigo("PAB-TREE")
                .nombre("Pabellón del Árbol")
                .build());

        EstructuraMercadoResponse estructura = estructuraService.obtenerEstructuraCompleta(mercadoPrincipal.getId());

        assertThat(estructura).isNotNull();
        assertThat(estructura.getMercadoId()).isEqualTo(mercadoPrincipal.getId());
        assertThat(estructura.getSectores()).isNotEmpty();
    }

    // 16. Contar sectores
    @Test
    @DisplayName("16. Conteo exacto de sectores por mercado")
    void testContarSectores() {
        sectorService.crearSector(SectorRequest.builder().mercadoId(mercadoPrincipal.getId()).codigo("SEC-C1").nombre("Sector C1").build());
        sectorService.crearSector(SectorRequest.builder().mercadoId(mercadoPrincipal.getId()).codigo("SEC-C2").nombre("Sector C2").build());

        EstructuraEstadisticasResponse stats = estructuraService.obtenerEstadisticas(mercadoPrincipal.getId());
        assertThat(stats.getTotalSectores()).isGreaterThanOrEqualTo(2);
    }

    // 17. Contar pabellones
    @Test
    @DisplayName("17. Conteo exacto de pabellones por mercado")
    void testContarPabellones() {
        pabellonService.crearPabellon(PabellonRequest.builder().mercadoId(mercadoPrincipal.getId()).codigo("PAB-K1").nombre("Pab K1").build());
        pabellonService.crearPabellon(PabellonRequest.builder().mercadoId(mercadoPrincipal.getId()).codigo("PAB-K2").nombre("Pab K2").build());

        EstructuraEstadisticasResponse stats = estructuraService.obtenerEstadisticas(mercadoPrincipal.getId());
        assertThat(stats.getTotalPabellones()).isGreaterThanOrEqualTo(2);
    }

    // 18. Contar puestos sin duplicados
    @Test
    @DisplayName("18. Contar puestos sin duplicar aunque pertenezcan a sector y pabellón")
    void testContarPuestosSinDuplicados() {
        SectorMercado sec = sectorRepository.save(SectorMercado.builder()
                .mercado(mercadoPrincipal)
                .codigo("SEC-PUESTOS")
                .nombre("Sector Puestos")
                .estado("ACTIVO")
                .build());

        PabellonMercado pab = pabellonRepository.save(PabellonMercado.builder()
                .mercado(mercadoPrincipal)
                .sector(sec)
                .codigo("PAB-PUESTOS")
                .nombre("Pabellón Puestos")
                .estado("ACTIVO")
                .build());

        // Puesto vinculado a la vez con sector y pabellón
        puestoRepository.save(PuestoComercial.builder()
                .mercado(mercadoPrincipal)
                .sector(sec)
                .pabellon(pab)
                .codigo("PST-UNICO-1")
                .numero("01")
                .estado(EstadoPuesto.DISPONIBLE)
                .areaMetrosCuadrados(new BigDecimal("12.50"))
                .build());

        // Puesto vinculado solo a sector
        puestoRepository.save(PuestoComercial.builder()
                .mercado(mercadoPrincipal)
                .sector(sec)
                .codigo("PST-UNICO-2")
                .numero("02")
                .estado(EstadoPuesto.OCUPADO)
                .areaMetrosCuadrados(new BigDecimal("10.00"))
                .build());

        EstructuraEstadisticasResponse stats = estructuraService.obtenerEstadisticas(mercadoPrincipal.getId());

        assertThat(stats.getTotalPuestos()).isEqualTo(2);
        assertThat(stats.getPuestosDisponibles()).isEqualTo(1);
        assertThat(stats.getPuestosOcupados()).isEqualTo(1);
    }

    // 19. Reordenar sectores
    @Test
    @DisplayName("19. Reordenar sectores visualmente")
    void testReordenarSectores() {
        SectorResponse s1 = sectorService.crearSector(SectorRequest.builder().mercadoId(mercadoPrincipal.getId()).codigo("SEC-ORD1").nombre("Primero").ordenVisual(1).build());
        SectorResponse s2 = sectorService.crearSector(SectorRequest.builder().mercadoId(mercadoPrincipal.getId()).codigo("SEC-ORD2").nombre("Segundo").ordenVisual(2).build());

        sectorService.reordenarSectores(mercadoPrincipal.getId(), List.of(
                new ReordenarItemRequest(s1.getId(), 10),
                new ReordenarItemRequest(s2.getId(), 5)
        ));

        SectorResponse mod1 = sectorService.obtenerPorIdYMercado(s1.getId(), mercadoPrincipal.getId());
        SectorResponse mod2 = sectorService.obtenerPorIdYMercado(s2.getId(), mercadoPrincipal.getId());

        assertThat(mod1.getOrdenVisual()).isEqualTo(10);
        assertThat(mod2.getOrdenVisual()).isEqualTo(5);
    }

    // 20. Reordenar pabellones
    @Test
    @DisplayName("20. Reordenar pabellones visualmente")
    void testReordenarPabellones() {
        PabellonResponse p1 = pabellonService.crearPabellon(PabellonRequest.builder().mercadoId(mercadoPrincipal.getId()).codigo("PAB-O1").nombre("Pab 1").ordenVisual(1).build());
        PabellonResponse p2 = pabellonService.crearPabellon(PabellonRequest.builder().mercadoId(mercadoPrincipal.getId()).codigo("PAB-O2").nombre("Pab 2").ordenVisual(2).build());

        pabellonService.reordenarPabellones(mercadoPrincipal.getId(), List.of(
                new ReordenarItemRequest(p1.getId(), 20),
                new ReordenarItemRequest(p2.getId(), 15)
        ));

        PabellonResponse mod1 = pabellonService.obtenerPorIdYMercado(p1.getId(), mercadoPrincipal.getId());
        PabellonResponse mod2 = pabellonService.obtenerPorIdYMercado(p2.getId(), mercadoPrincipal.getId());

        assertThat(mod1.getOrdenVisual()).isEqualTo(20);
        assertThat(mod2.getOrdenVisual()).isEqualTo(15);
    }

    // 21. Controlar concurrencia
    @Test
    @DisplayName("21. Control de concurrencia optimista con @Version")
    void testControlConcurrenciaOptimista() {
        SectorResponse creado = sectorService.crearSector(SectorRequest.builder()
                .mercadoId(mercadoPrincipal.getId())
                .codigo("SEC-CONC")
                .nombre("Sector Concurrente")
                .build());

        SectorActualizacionRequest actReq1 = SectorActualizacionRequest.builder()
                .nombre("Nombre modificado por usuario A")
                .version(creado.getVersion())
                .build();
        sectorService.actualizarSector(creado.getId(), mercadoPrincipal.getId(), actReq1);

        // Intento de actualización con versión obsoleta
        SectorActualizacionRequest actReqObsoleto = SectorActualizacionRequest.builder()
                .nombre("Nombre modificado por usuario B con versión vieja")
                .version(creado.getVersion())
                .build();

        assertThatThrownBy(() -> sectorService.actualizarSector(creado.getId(), mercadoPrincipal.getId(), actReqObsoleto))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("modificado concurrentemente");
    }

    // 22. Consultar historial
    @Test
    @DisplayName("22. Consultar historial inmutable de cambios de estado")
    void testConsultarHistorialInmutable() {
        SectorResponse sector = sectorService.crearSector(SectorRequest.builder()
                .mercadoId(mercadoPrincipal.getId())
                .codigo("SEC-HIST")
                .nombre("Sector con Historial")
                .build());

        sectorService.cambiarEstado(sector.getId(), mercadoPrincipal.getId(), CambioEstadoEstructuraRequest.builder()
                .nuevoEstado(EstadoMercado.EN_MANTENIMIENTO)
                .motivo("Mantenimiento primer ciclo")
                .build());

        sectorService.cambiarEstado(sector.getId(), mercadoPrincipal.getId(), CambioEstadoEstructuraRequest.builder()
                .nuevoEstado(EstadoMercado.ACTIVO)
                .motivo("Reapertura tras mantenimiento")
                .build());

        List<HistorialCambioEstadoResponse> historial = sectorService.consultarHistorial(sector.getId(), mercadoPrincipal.getId());

        assertThat(historial).hasSize(2);
        assertThat(historial.get(0).getEstadoNuevo()).isEqualTo(EstadoMercado.ACTIVO);
        assertThat(historial.get(1).getEstadoNuevo()).isEqualTo(EstadoMercado.EN_MANTENIMIENTO);
    }

    // 23. Filtrar
    @Test
    @DisplayName("23. Filtrar sectores por término y estado")
    void testFiltrarSectores() {
        sectorService.crearSector(SectorRequest.builder().mercadoId(mercadoPrincipal.getId()).codigo("SEC-VERD").nombre("Verduras Frescas").categoriaComercial("Frutas y verduras").build());
        sectorService.crearSector(SectorRequest.builder().mercadoId(mercadoPrincipal.getId()).codigo("SEC-ABAR").nombre("Abarrotes").categoriaComercial("Abarrotes").build());

        Page<SectorResponse> res = sectorService.listarSectores(mercadoPrincipal.getId(), "Verduras", "ACTIVO", null, PageRequest.of(0, 10));

        assertThat(res.getContent()).hasSize(1);
        assertThat(res.getContent().get(0).getCodigo()).isEqualTo("SEC-VERD");
    }

    // 24. Paginar
    @Test
    @DisplayName("24. Paginación consistente de pabellones")
    void testPaginarPabellones() {
        for (int i = 1; i <= 7; i++) {
            pabellonService.crearPabellon(PabellonRequest.builder()
                    .mercadoId(mercadoPrincipal.getId())
                    .codigo("PAB-PAG-" + i)
                    .nombre("Pabellón Paginación " + i)
                    .build());
        }

        Page<PabellonResponse> pagina0 = pabellonService.listarPabellones(mercadoPrincipal.getId(), null, null, null, null, PageRequest.of(0, 5));
        Page<PabellonResponse> pagina1 = pabellonService.listarPabellones(mercadoPrincipal.getId(), null, null, null, null, PageRequest.of(1, 5));

        assertThat(pagina0.getContent()).hasSize(5);
        assertThat(pagina1.getContent()).hasSizeGreaterThanOrEqualTo(2);
    }
}
