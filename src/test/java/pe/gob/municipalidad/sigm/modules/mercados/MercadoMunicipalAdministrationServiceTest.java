package pe.gob.municipalidad.sigm.modules.mercados;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.document.entity.CategoriaDocumento;
import pe.gob.municipalidad.sigm.common.document.entity.ClasificacionAcceso;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.document.repository.DocumentoRepository;
import pe.gob.municipalidad.sigm.common.exception.ConflictException;
import pe.gob.municipalidad.sigm.common.exception.DuplicateResourceException;
import pe.gob.municipalidad.sigm.common.exception.InvalidRequestException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Municipalidad;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.repository.MunicipalidadRepository;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.dto.MercadosDtos.*;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.*;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;
import pe.gob.municipalidad.sigm.modules.mercados.service.MercadoMunicipalService;
import pe.gob.municipalidad.sigm.modules.mercados.service.MercadosExportService;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MercadoMunicipalAdministrationServiceTest {

    @Autowired
    private MercadoMunicipalService mercadoService;

    @Autowired
    private MercadosExportService exportService;

    @Autowired
    private MercadoMunicipalRepository mercadoRepository;

    @Autowired
    private OficinaRepository oficinaRepository;

    @Autowired
    private MunicipalidadRepository municipalidadRepository;

    @Autowired
    private SectorMercadoRepository sectorRepository;

    @Autowired
    private PabellonMercadoRepository pabellonRepository;

    @Autowired
    private PuestoComercialRepository puestoRepository;

    @Autowired
    private MercadoCambioEstadoRepository cambioEstadoRepository;

    @Autowired
    private MercadoDocumentoRepository documentoRepository;

    @Autowired
    private DocumentoRepository coreDocumentoRepository;

    private Oficina oficinaPrueba;
    private Oficina oficinaInactiva;

    @BeforeEach
    void setUp() {
        Municipalidad muni = municipalidadRepository.findAll().stream().findFirst().orElseGet(() ->
                municipalidadRepository.save(Municipalidad.builder()
                        .nombre("Municipalidad Distrital de Quinua")
                        .nombreCorto("MD-QUINUA")
                        .ubigeo("050108")
                        .departamento("Ayacucho")
                        .provincia("Huamanga")
                        .distrito("Quinua")
                        .build()));

        oficinaPrueba = oficinaRepository.findAll().stream()
                .filter(o -> "ACTIVO".equalsIgnoreCase(o.getEstado()))
                .findFirst()
                .orElseGet(() -> oficinaRepository.save(Oficina.builder()
                        .municipalidad(muni)
                        .codigo("OFI-MERC-" + System.currentTimeMillis() % 10000)
                        .nombre("Oficina de Abastos y Comercialización")
                        .estado("ACTIVO")
                        .build()));

        oficinaInactiva = oficinaRepository.save(Oficina.builder()
                .municipalidad(muni)
                .codigo("OFI-INACT-" + System.currentTimeMillis() % 10000)
                .nombre("Oficina Desactivada")
                .estado("INACTIVO")
                .build());
    }

    @Test
    @DisplayName("01. Crear mercado válido con datos completos")
    void test01_crearMercadoValido() {
        MercadoRequest request = MercadoRequest.builder()
                .codigo("MER-TEST-01")
                .nombre("Mercado Modelo de Quinua")
                .descripcion("Mercado central de abastos")
                .direccion("Jr. Sucre 123, Quinua")
                .referenciaUbicacion("Frente a la plaza central")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .latitud(new BigDecimal("-13.042500"))
                .longitud(new BigDecimal("-74.140800"))
                .capacidadReferencial(85)
                .estado(EstadoMercado.ACTIVO)
                .oficinaAdministradoraId(oficinaPrueba.getId())
                .build();

        MercadoResponse response = mercadoService.crearMercado(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getCodigo()).isEqualTo("MER-TEST-01");
        assertThat(response.getNombre()).isEqualTo("Mercado Modelo de Quinua");
        assertThat(response.getEstado()).isEqualTo(EstadoMercado.ACTIVO);
        assertThat(response.getCapacidadReferencial()).isEqualTo(85);
        assertThat(response.getOficinaAdministradoraId()).isEqualTo(oficinaPrueba.getId());
    }

    @Test
    @DisplayName("02. Rechazar creación con código duplicado")
    void test02_rechazarCodigoDuplicado() {
        MercadoRequest req1 = MercadoRequest.builder()
                .codigo("MER-DUP-01")
                .nombre("Mercado San Isidro")
                .direccion("Av. Centenario 450")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .capacidadReferencial(50)
                .build();
        mercadoService.crearMercado(req1);

        MercadoRequest req2 = MercadoRequest.builder()
                .codigo("MER-DUP-01")
                .nombre("Mercado San Isidro Sucursal")
                .direccion("Av. Centenario 455")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .capacidadReferencial(30)
                .build();

        assertThatThrownBy(() -> mercadoService.crearMercado(req2))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("MER-DUP-01");
    }

    @Test
    @DisplayName("03. Rechazar nombre vacío o en blanco")
    void test03_rechazarNombreVacio() {
        MercadoRequest request = MercadoRequest.builder()
                .codigo("MER-INV-01")
                .nombre("   ")
                .direccion("Jr. Comercio 100")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .build();

        assertThatThrownBy(() -> mercadoService.crearMercado(request))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    @DisplayName("04. Validar dirección obligatoria")
    void test04_validarDireccionObligatoria() {
        MercadoRequest request = MercadoRequest.builder()
                .codigo("MER-NODIR-01")
                .nombre("Mercado Sin Direccion")
                .direccion("")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .build();

        assertThatThrownBy(() -> mercadoService.crearMercado(request))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    @DisplayName("05. Validar coordenadas dentro del rango geográfico válido")
    void test05_validarCoordenadasFueraDeRango() {
        MercadoRequest reqLatInvalida = MercadoRequest.builder()
                .codigo("MER-COORD-01")
                .nombre("Mercado Coordenada Inv")
                .direccion("Jr. Sol 10")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .latitud(new BigDecimal("95.000000"))
                .longitud(new BigDecimal("-74.140800"))
                .build();

        assertThatThrownBy(() -> mercadoService.crearMercado(reqLatInvalida))
                .isInstanceOf(InvalidRequestException.class);

        MercadoRequest reqLngInvalida = MercadoRequest.builder()
                .codigo("MER-COORD-02")
                .nombre("Mercado Coordenada Inv 2")
                .direccion("Jr. Sol 11")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .latitud(new BigDecimal("-13.042500"))
                .longitud(new BigDecimal("-195.000000"))
                .build();

        assertThatThrownBy(() -> mercadoService.crearMercado(reqLngInvalida))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    @DisplayName("06. Validar capacidad referencial no negativa")
    void test06_validarCapacidadNoNegativa() {
        MercadoRequest request = MercadoRequest.builder()
                .codigo("MER-CAP-NEG")
                .nombre("Mercado Cap Negativa")
                .direccion("Jr. Sol 12")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .capacidadReferencial(-5)
                .build();

        assertThatThrownBy(() -> mercadoService.crearMercado(request))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    @DisplayName("07. Asociar oficina administradora existente y activa")
    void test07_asociarOficinaExistenteYActiva() {
        MercadoRequest request = MercadoRequest.builder()
                .codigo("MER-OFI-OK")
                .nombre("Mercado Con Oficina Activa")
                .direccion("Jr. Mercado 20")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .oficinaAdministradoraId(oficinaPrueba.getId())
                .build();

        MercadoResponse response = mercadoService.crearMercado(request);
        assertThat(response.getOficinaAdministradoraId()).isEqualTo(oficinaPrueba.getId());
        assertThat(response.getOficinaAdministradoraNombre()).isEqualTo(oficinaPrueba.getNombre());
    }

    @Test
    @DisplayName("08. Rechazar asociación de oficina inexistente o inactiva")
    void test08_rechazarOficinaInexistenteOInactiva() {
        MercadoRequest reqInexistente = MercadoRequest.builder()
                .codigo("MER-OFI-INEX")
                .nombre("Mercado Con Oficina Inexistente")
                .direccion("Jr. Mercado 21")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .oficinaAdministradoraId(999999L)
                .build();

        assertThatThrownBy(() -> mercadoService.crearMercado(reqInexistente))
                .isInstanceOf(ResourceNotFoundException.class);

        MercadoRequest reqInactiva = MercadoRequest.builder()
                .codigo("MER-OFI-INACT")
                .nombre("Mercado Con Oficina Inactiva")
                .direccion("Jr. Mercado 22")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .oficinaAdministradoraId(oficinaInactiva.getId())
                .build();

        assertThatThrownBy(() -> mercadoService.crearMercado(reqInactiva))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    @DisplayName("09. Listar mercados paginados desde PostgreSQL")
    void test09_listarMercadosPaginados() {
        mercadoService.crearMercado(MercadoRequest.builder()
                .codigo("MER-PAG-01")
                .nombre("Mercado Paginado 1")
                .direccion("Direccion 1")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .build());

        mercadoService.crearMercado(MercadoRequest.builder()
                .codigo("MER-PAG-02")
                .nombre("Mercado Paginado 2")
                .direccion("Direccion 2")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .build());

        MercadoFilter filter = MercadoFilter.builder()
                .page(0)
                .size(10)
                .sortBy("codigo")
                .sortDir("asc")
                .build();

        Page<MercadoResponse> pagina = mercadoService.listarMercados(filter);
        assertThat(pagina).isNotNull();
        assertThat(pagina.getContent()).isNotEmpty();
    }

    @Test
    @DisplayName("10. Buscar mercados por código")
    void test10_buscarPorCodigo() {
        mercadoService.crearMercado(MercadoRequest.builder()
                .codigo("MER-BUSQ-COD-99")
                .nombre("Mercado Búsqueda Código")
                .direccion("Jr. Busqueda 10")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .build());

        MercadoFilter filter = MercadoFilter.builder()
                .termino("COD-99")
                .page(0)
                .size(10)
                .build();

        Page<MercadoResponse> resultados = mercadoService.listarMercados(filter);
        assertThat(resultados.getContent()).anyMatch(m -> m.getCodigo().equals("MER-BUSQ-COD-99"));
    }

    @Test
    @DisplayName("11. Buscar mercados por nombre")
    void test11_buscarPorNombre() {
        mercadoService.crearMercado(MercadoRequest.builder()
                .codigo("MER-NOM-45")
                .nombre("Mercado Artesanal Quinua Andina")
                .direccion("Jr. Artesanias 55")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .build());

        MercadoFilter filter = MercadoFilter.builder()
                .termino("Quinua Andina")
                .page(0)
                .size(10)
                .build();

        Page<MercadoResponse> resultados = mercadoService.listarMercados(filter);
        assertThat(resultados.getContent()).anyMatch(m -> m.getNombre().contains("Quinua Andina"));
    }

    @Test
    @DisplayName("12. Filtrar mercados por estado operativo")
    void test12_filtrarPorEstado() {
        mercadoService.crearMercado(MercadoRequest.builder()
                .codigo("MER-MANT-01")
                .nombre("Mercado en Mantenimiento")
                .direccion("Jr. Mantenimiento 1")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .estado(EstadoMercado.EN_MANTENIMIENTO)
                .build());

        MercadoFilter filter = MercadoFilter.builder()
                .estado(EstadoMercado.EN_MANTENIMIENTO)
                .page(0)
                .size(10)
                .build();

        Page<MercadoResponse> resultados = mercadoService.listarMercados(filter);
        assertThat(resultados.getContent()).isNotEmpty();
        assertThat(resultados.getContent()).allMatch(m -> m.getEstado() == EstadoMercado.EN_MANTENIMIENTO);
    }

    @Test
    @DisplayName("13. Filtrar mercados por oficina administradora")
    void test13_filtrarPorOficina() {
        mercadoService.crearMercado(MercadoRequest.builder()
                .codigo("MER-OFI-FILT")
                .nombre("Mercado Filtrado Oficina")
                .direccion("Jr. Oficina 1")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .oficinaAdministradoraId(oficinaPrueba.getId())
                .build());

        MercadoFilter filter = MercadoFilter.builder()
                .oficinaId(oficinaPrueba.getId())
                .page(0)
                .size(10)
                .build();

        Page<MercadoResponse> resultados = mercadoService.listarMercados(filter);
        assertThat(resultados.getContent()).isNotEmpty();
        assertThat(resultados.getContent()).allMatch(m -> oficinaPrueba.getId().equals(m.getOficinaAdministradoraId()));
    }

    @Test
    @DisplayName("14. Ordenar mercados con whitelist seguro sin SQL arbitrario")
    void test14_ordenarMercadosWhitelistSeguro() {
        MercadoFilter filterInseguro = MercadoFilter.builder()
                .sortBy("DROP TABLE mercados; --")
                .sortDir("desc")
                .page(0)
                .size(5)
                .build();

        Page<MercadoResponse> pagina = mercadoService.listarMercados(filterInseguro);
        assertThat(pagina).isNotNull();
    }

    @Test
    @DisplayName("15. Paginación consistente de mercados")
    void test15_paginacionConsistente() {
        MercadoFilter filter = MercadoFilter.builder()
                .page(0)
                .size(2)
                .build();

        Page<MercadoResponse> pag1 = mercadoService.listarMercados(filter);
        assertThat(pag1.getSize()).isEqualTo(2);
    }

    @Test
    @DisplayName("16. Consultar detalle completo con resumen de puestos y sectores")
    void test16_consultarDetalleCompleto() {
        MercadoResponse creado = mercadoService.crearMercado(MercadoRequest.builder()
                .codigo("MER-DET-01")
                .nombre("Mercado para Detalle")
                .direccion("Av. Detalle 100")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .capacidadReferencial(120)
                .build());

        MercadoDetalleResponse detalle = mercadoService.obtenerDetalle(creado.getId());
        assertThat(detalle).isNotNull();
        assertThat(detalle.getMercado()).isNotNull();
        assertThat(detalle.getMercado().getId()).isEqualTo(creado.getId());
        assertThat(detalle.getResumenPuestos()).isNotNull();
        assertThat(detalle.getSectores()).isNotNull();
        assertThat(detalle.getPabellones()).isNotNull();
        assertThat(detalle.getPuestos()).isNotNull();
    }

    @Test
    @DisplayName("17. Editar mercado y verificar persistencia")
    void test17_editarMercado() {
        MercadoResponse creado = mercadoService.crearMercado(MercadoRequest.builder()
                .codigo("MER-EDIT-01")
                .nombre("Mercado Original")
                .direccion("Jr. Antiguo 123")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .build());

        MercadoActualizacionRequest updateReq = MercadoActualizacionRequest.builder()
                .nombre("Mercado Editado y Mejorado")
                .direccion("Jr. Renovado 456")
                .referenciaUbicacion("Frente al nuevo polideportivo")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .capacidadReferencial(90)
                .estado(EstadoMercado.ACTIVO)
                .version(creado.getVersion())
                .build();

        MercadoResponse actualizado = mercadoService.actualizarMercado(creado.getId(), updateReq);
        assertThat(actualizado.getNombre()).isEqualTo("Mercado Editado y Mejorado");
        assertThat(actualizado.getDireccion()).isEqualTo("Jr. Renovado 456");
        assertThat(actualizado.getCapacidadReferencial()).isEqualTo(90);
    }

    @Test
    @DisplayName("18. Controlar concurrencia optimista con versión mismatch")
    void test18_controlConcurrenciaOptimista() {
        MercadoResponse creado = mercadoService.crearMercado(MercadoRequest.builder()
                .codigo("MER-CONC-01")
                .nombre("Mercado Concurrente")
                .direccion("Jr. Héroes 10")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .build());

        MercadoActualizacionRequest update1 = MercadoActualizacionRequest.builder()
                .nombre("Mercado Modificado Primero")
                .direccion("Jr. Héroes 10")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .version(creado.getVersion())
                .build();
        mercadoService.actualizarMercado(creado.getId(), update1);

        MercadoActualizacionRequest update2 = MercadoActualizacionRequest.builder()
                .nombre("Mercado Modificado Segundo Con Version Antigua")
                .direccion("Jr. Héroes 10")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .version(creado.getVersion())
                .build();

        assertThatThrownBy(() -> mercadoService.actualizarMercado(creado.getId(), update2))
                .isInstanceOf(OptimisticLockingFailureException.class)
                .hasMessageContaining("simultáneo");
    }

    @Test
    @DisplayName("19. Cambiar estado operativo a EN_MANTENIMIENTO y reactivar")
    void test19_cambiarEstadoOperativo() {
        MercadoResponse creado = mercadoService.crearMercado(MercadoRequest.builder()
                .codigo("MER-EST-01")
                .nombre("Mercado Transición Estado")
                .direccion("Av. Ayacucho 50")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .estado(EstadoMercado.ACTIVO)
                .build());

        CambioEstadoMercadoRequest cambioReq = CambioEstadoMercadoRequest.builder()
                .nuevoEstado(EstadoMercado.EN_MANTENIMIENTO)
                .motivo("Mantenimiento general del techo y piso por temporada de lluvias")
                .build();

        MercadoResponse resultado = mercadoService.cambiarEstado(creado.getId(), cambioReq);
        assertThat(resultado.getEstado()).isEqualTo(EstadoMercado.EN_MANTENIMIENTO);

        CambioEstadoMercadoRequest reactivacionReq = CambioEstadoMercadoRequest.builder()
                .nuevoEstado(EstadoMercado.ACTIVO)
                .motivo("Culminación satisfactoria de las obras de mantenimiento")
                .build();

        MercadoResponse reactivado = mercadoService.cambiarEstado(creado.getId(), reactivacionReq);
        assertThat(reactivado.getEstado()).isEqualTo(EstadoMercado.ACTIVO);
    }

    @Test
    @DisplayName("20. Rechazar cambio al mismo estado actual")
    void test20_rechazarCambioMismoEstado() {
        MercadoResponse creado = mercadoService.crearMercado(MercadoRequest.builder()
                .codigo("MER-MISMO-EST")
                .nombre("Mercado Mismo Estado")
                .direccion("Av. Ayacucho 55")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .estado(EstadoMercado.ACTIVO)
                .build());

        CambioEstadoMercadoRequest req = CambioEstadoMercadoRequest.builder()
                .nuevoEstado(EstadoMercado.ACTIVO)
                .motivo("Intento redundante")
                .build();

        assertThatThrownBy(() -> mercadoService.cambiarEstado(creado.getId(), req))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("ya se encuentra en el estado");
    }

    @Test
    @DisplayName("21. Conservar historial inmutable de cambios de estado")
    void test21_conservarHistorialInmutable() {
        MercadoResponse creado = mercadoService.crearMercado(MercadoRequest.builder()
                .codigo("MER-HIST-01")
                .nombre("Mercado Con Historial")
                .direccion("Jr. Memoria 12")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .build());

        mercadoService.cambiarEstado(creado.getId(), CambioEstadoMercadoRequest.builder()
                .nuevoEstado(EstadoMercado.EN_MANTENIMIENTO)
                .motivo("Refacción de sanitarios")
                .build());

        mercadoService.cambiarEstado(creado.getId(), CambioEstadoMercadoRequest.builder()
                .nuevoEstado(EstadoMercado.ACTIVO)
                .motivo("Apertura post sanitización")
                .build());

        List<MercadoHistorialItemResponse> historial = mercadoService.obtenerHistorial(creado.getId());
        assertThat(historial).hasSizeGreaterThanOrEqualTo(2);
        assertThat(historial.get(0).getMotivo()).contains("Apertura post sanitización");
    }

    @Test
    @DisplayName("22. Contar puestos y calcular resumen operativo real")
    void test22_contarPuestosYResumenOperativo() {
        MercadoResponse mercado = mercadoService.crearMercado(MercadoRequest.builder()
                .codigo("MER-PUESTOS-RES")
                .nombre("Mercado Conteo Puestos")
                .direccion("Jr. Comercio 30")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .build());

        MercadoMunicipal entidadMercado = mercadoRepository.findById(mercado.getId()).orElseThrow();

        SectorMercado sector = sectorRepository.save(SectorMercado.builder()
                .mercado(entidadMercado)
                .codigo("SEC-T1")
                .nombre("Sector Carnes")
                .estado("ACTIVO")
                .build());

        PabellonMercado pabellon = pabellonRepository.save(PabellonMercado.builder()
                .mercado(entidadMercado)
                .sector(sector)
                .codigo("PAB-T1")
                .nombre("Pabellón A")
                .estado("ACTIVO")
                .build());

        puestoRepository.save(PuestoComercial.builder()
                .mercado(entidadMercado)
                .pabellon(pabellon)
                .codigo("PST-01")
                .numero("PST-01")
                .estado(EstadoPuesto.DISPONIBLE)
                .build());

        puestoRepository.save(PuestoComercial.builder()
                .mercado(entidadMercado)
                .pabellon(pabellon)
                .codigo("PST-02")
                .numero("PST-02")
                .estado(EstadoPuesto.OCUPADO)
                .build());

        puestoRepository.save(PuestoComercial.builder()
                .mercado(entidadMercado)
                .pabellon(pabellon)
                .codigo("PST-03")
                .numero("PST-03")
                .estado(EstadoPuesto.RESERVADO)
                .build());

        ResumenPuestosMercadoResponse resumen = mercadoService.obtenerResumenPuestos(mercado.getId());
        assertThat(resumen.getTotal()).isEqualTo(3);
        assertThat(resumen.getDisponibles()).isEqualTo(1);
        assertThat(resumen.getOcupados()).isEqualTo(1);
        assertThat(resumen.getReservados()).isEqualTo(1);
        assertThat(resumen.getInhabilitados()).isEqualTo(0);
    }

    @Test
    @DisplayName("23. Exportar Excel con protección contra inyección de fórmulas CSV/Excel")
    void test23_exportarExcelConProteccionFormulaInjection() {
        mercadoService.crearMercado(MercadoRequest.builder()
                .codigo("=cmd|'/C calc'!A0")
                .nombre("+2+5")
                .direccion("-SUM(1,2)")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .estado(EstadoMercado.ACTIVO)
                .capacidadReferencial(10)
                .build());

        byte[] excelBytes = exportService.exportarExcel(MercadoFilter.builder().build());

        assertThat(excelBytes).isNotNull();
        assertThat(excelBytes.length).isGreaterThan(100);
        assertThat(excelBytes[0]).isEqualTo((byte) 0x50);
        assertThat(excelBytes[1]).isEqualTo((byte) 0x4B);
    }

    @Test
    @DisplayName("24. Exportar PDF en formato institucional apaisado")
    void test24_exportarPdfApaisado() {
        mercadoService.crearMercado(MercadoRequest.builder()
                .codigo("MER-PDF-01")
                .nombre("Mercado Municipal Central de Quinua")
                .direccion("Plaza de Armas s/n")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .estado(EstadoMercado.ACTIVO)
                .capacidadReferencial(50)
                .build());

        byte[] pdfBytes = exportService.exportarPdf(MercadoFilter.builder().build());

        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(500);
        String header = new String(pdfBytes, 0, 5, StandardCharsets.US_ASCII);
        assertThat(header).isEqualTo("%PDF-");
    }

    @Test
    @DisplayName("29. Registrar documento técnico asociado al mercado con auditoría")
    void test29_asociarDocumentoMercadoConAuditoria() {
        MercadoResponse mercado = mercadoService.crearMercado(MercadoRequest.builder()
                .codigo("MER-DOC-01")
                .nombre("Mercado con Documento")
                .direccion("Av. Documental 12")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .build());

        Documento coreDoc = coreDocumentoRepository.save(Documento.builder()
                .identificadorPublico(UUID.randomUUID())
                .nombreOriginal("plano_general.pdf")
                .nombreInterno("doc_" + System.currentTimeMillis() + ".pdf")
                .extension(".pdf")
                .mimeType("application/pdf")
                .tamanoBytes(1024L)
                .hashSha256("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
                .categoria(CategoriaDocumento.RESOLUCION)
                .clasificacionAcceso(ClasificacionAcceso.PUBLICO)
                .build());

        MercadoDocumentoRequest docReq = MercadoDocumentoRequest.builder()
                .documentoId(coreDoc.getId())
                .tipoDocumento(TipoDocumentoMercado.PLANO)
                .descripcion("Plano General de Distribución Eléctrica y Sanitaria")
                .build();

        MercadoDocumentoResponse docRes = mercadoService.asociarDocumento(mercado.getId(), docReq);

        assertThat(docRes).isNotNull();
        assertThat(docRes.getId()).isNotNull();
        assertThat(docRes.getTipoDocumento()).isEqualTo(TipoDocumentoMercado.PLANO);
        assertThat(docRes.getNombreOriginal()).isEqualTo("plano_general.pdf");

        List<MercadoDocumentoResponse> listaDocs = mercadoService.listarDocumentos(mercado.getId());
        assertThat(listaDocs).hasSize(1);
    }
}
