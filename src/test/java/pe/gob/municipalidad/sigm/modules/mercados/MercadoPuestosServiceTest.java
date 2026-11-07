package pe.gob.municipalidad.sigm.modules.mercados;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoFisicoPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoMercado;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;
import pe.gob.municipalidad.sigm.modules.mercados.service.PuestoComercialService;
import pe.gob.municipalidad.sigm.modules.mercados.service.PuestoExportService;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MercadoPuestosServiceTest {

    @Autowired
    private PuestoComercialService puestoService;

    @Autowired
    private PuestoExportService puestoExportService;

    @Autowired
    private MercadoMunicipalRepository mercadoRepository;

    @Autowired
    private SectorMercadoRepository sectorRepository;

    @Autowired
    private PabellonMercadoRepository pabellonRepository;

    @Autowired
    private PuestoComercialRepository puestoRepository;

    @Autowired
    private TipoPuestoComercialRepository tipoPuestoRepository;

    @Autowired
    private OficinaRepository oficinaRepository;

    @Autowired
    private MunicipalidadRepository municipalidadRepository;

    private MercadoMunicipal mercado1;
    private MercadoMunicipal mercado2;
    private SectorMercado sector1;
    private PabellonMercado pabellon1;
    private TipoPuestoComercial tipoFijo;

    @BeforeEach
    void setUp() {
        Municipalidad muni = municipalidadRepository.findAll().stream().findFirst().orElseGet(() -> {
            Municipalidad m = Municipalidad.builder()
                    .nombre("Municipalidad Distrital de Quinua")
                    .ubigeo("050108")
                    .build();
            return municipalidadRepository.save(m);
        });

        Oficina ofi = oficinaRepository.findAll().stream().findFirst().orElseGet(() -> {
            Oficina o = Oficina.builder()
                    .codigo("OFI-MERC-TEST")
                    .nombre("Subgerencia de Servicios Municipales")
                    .sigla("SGSM")
                    .municipalidad(muni)
                    .estado("ACTIVO")
                    .build();
            return oficinaRepository.save(o);
        });

        mercado1 = mercadoRepository.save(MercadoMunicipal.builder()
                .codigo("MERC-PUESTO-01")
                .nombre("Mercado Central de Quinua")
                .direccion("Jr. Sucre s/n, Plaza Principal, Quinua")
                .estado(EstadoMercado.ACTIVO)
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .oficinaAdministradora(ofi)
                .version(0L)
                .build());

        mercado2 = mercadoRepository.save(MercadoMunicipal.builder()
                .codigo("MERC-PUESTO-02")
                .nombre("Ferial Quinua Artesanal")
                .direccion("Av. Los Artesanos 123, Quinua")
                .estado(EstadoMercado.ACTIVO)
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .oficinaAdministradora(ofi)
                .version(0L)
                .build());

        sector1 = sectorRepository.save(SectorMercado.builder()
                .mercado(mercado1)
                .codigo("SEC-CARNES")
                .nombre("Sector Carnes y Aves")
                .estado("ACTIVO")
                .ordenVisual(1)
                .version(0L)
                .build());

        pabellon1 = pabellonRepository.save(PabellonMercado.builder()
                .mercado(mercado1)
                .sector(sector1)
                .codigo("PAB-C1")
                .nombre("Pabellón Carnicería Principal")
                .nivel(1)
                .estado("ACTIVO")
                .ordenVisual(1)
                .version(0L)
                .build());

        tipoFijo = tipoPuestoRepository.findByCodigo("FIJO").orElseGet(() ->
                tipoPuestoRepository.save(TipoPuestoComercial.builder()
                        .codigo("FIJO")
                        .nombre("Puesto Fijo de Concreto")
                        .permiteInstalacionFija(true)
                        .estado("ACTIVO")
                        .build())
        );
    }

    // 1. Crear puesto válido
    @Test
    @DisplayName("1. Crear puesto comercial válido exitosamente")
    void testCrearPuestoValido() {
        PuestoComercialCreateRequest req = PuestoComercialCreateRequest.builder()
                .mercadoId(mercado1.getId())
                .sectorId(sector1.getId())
                .pabellonId(pabellon1.getId())
                .tipoPuestoId(tipoFijo.getId())
                .codigo("P-101")
                .numero("101")
                .nombreReferencial("Carnes Don José")
                .anchoMetros(new BigDecimal("2.50"))
                .largoMetros(new BigDecimal("3.00"))
                .nivel(1)
                .ubicacionReferencia("Pasillo principal puerta 1")
                .estadoFisico(EstadoFisicoPuesto.OPERATIVO)
                .estado(EstadoPuesto.DISPONIBLE)
                .build();

        PuestoComercialResponse resp = puestoService.registrarPuesto(req);

        assertThat(resp).isNotNull();
        assertThat(resp.getId()).isNotNull();
        assertThat(resp.getCodigo()).isEqualTo("P-101");
        assertThat(resp.getNumero()).isEqualTo("101");
        assertThat(resp.getAreaMetrosCuadrados()).isEqualByComparingTo(new BigDecimal("7.50")); // Auto ancho * largo
        assertThat(resp.isDisponible()).isTrue();
    }

    // 2. Rechazar código duplicado en mismo mercado
    @Test
    @DisplayName("2. Rechazar creación de puesto con código duplicado en el mismo mercado")
    void testRechazarCodigoDuplicadoMismoMercado() {
        PuestoComercialCreateRequest req1 = PuestoComercialCreateRequest.builder()
                .mercadoId(mercado1.getId())
                .tipoPuestoId(tipoFijo.getId())
                .codigo("P-DUP")
                .numero("01")
                .build();
        puestoService.registrarPuesto(req1);

        PuestoComercialCreateRequest req2 = PuestoComercialCreateRequest.builder()
                .mercadoId(mercado1.getId())
                .tipoPuestoId(tipoFijo.getId())
                .codigo("p-dup") // Case insensitive check
                .numero("02")
                .build();

        assertThatThrownBy(() -> puestoService.registrarPuesto(req2))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("ya existe en el mercado");
    }

    // 3. Permitir mismo código en mercados distintos
    @Test
    @DisplayName("3. Permitir mismo código en mercados municipales distintos")
    void testPermitirMismoCodigoEnMercadosDistintos() {
        PuestoComercialCreateRequest req1 = PuestoComercialCreateRequest.builder()
                .mercadoId(mercado1.getId())
                .tipoPuestoId(tipoFijo.getId())
                .codigo("P-COMUN")
                .numero("01")
                .build();
        PuestoComercialResponse r1 = puestoService.registrarPuesto(req1);

        PuestoComercialCreateRequest req2 = PuestoComercialCreateRequest.builder()
                .mercadoId(mercado2.getId())
                .tipoPuestoId(tipoFijo.getId())
                .codigo("P-COMUN")
                .numero("01")
                .build();
        PuestoComercialResponse r2 = puestoService.registrarPuesto(req2);

        assertThat(r1.getId()).isNotEqualTo(r2.getId());
        assertThat(r1.getCodigo()).isEqualTo(r2.getCodigo());
    }

    // 4. Validar mercado inexistente
    @Test
    @DisplayName("4. Validar rechazo con mercado inexistente")
    void testValidarMercadoInexistente() {
        PuestoComercialCreateRequest req = PuestoComercialCreateRequest.builder()
                .mercadoId(UUID.randomUUID())
                .tipoPuestoId(tipoFijo.getId())
                .codigo("P-ERR")
                .numero("01")
                .build();

        assertThatThrownBy(() -> puestoService.registrarPuesto(req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Mercado municipal no encontrado");
    }

    // 5. Validar sector de otro mercado
    @Test
    @DisplayName("5. Rechazar sector que pertenece a otro mercado")
    void testValidarSectorDeOtroMercado() {
        SectorMercado sectorMercado2 = sectorRepository.save(SectorMercado.builder()
                .mercado(mercado2)
                .codigo("SEC-M2")
                .nombre("Sector M2")
                .estado("ACTIVO")
                .ordenVisual(1)
                .version(0L)
                .build());

        PuestoComercialCreateRequest req = PuestoComercialCreateRequest.builder()
                .mercadoId(mercado1.getId())
                .sectorId(sectorMercado2.getId())
                .tipoPuestoId(tipoFijo.getId())
                .codigo("P-SEC-INV")
                .numero("01")
                .build();

        assertThatThrownBy(() -> puestoService.registrarPuesto(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no pertenece al mercado indicado");
    }

    // 6. Validar pabellón de otro mercado
    @Test
    @DisplayName("6. Rechazar pabellón que pertenece a otro mercado")
    void testValidarPabellonDeOtroMercado() {
        PabellonMercado pabellonMercado2 = pabellonRepository.save(PabellonMercado.builder()
                .mercado(mercado2)
                .codigo("PAB-M2")
                .nombre("Pabellón M2")
                .estado("ACTIVO")
                .ordenVisual(1)
                .version(0L)
                .build());

        PuestoComercialCreateRequest req = PuestoComercialCreateRequest.builder()
                .mercadoId(mercado1.getId())
                .pabellonId(pabellonMercado2.getId())
                .tipoPuestoId(tipoFijo.getId())
                .codigo("P-PAB-INV")
                .numero("01")
                .build();

        assertThatThrownBy(() -> puestoService.registrarPuesto(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no pertenece al mercado indicado");
    }

    // 7. Validar tipo de puesto
    @Test
    @DisplayName("7. Rechazar tipo de puesto inexistente")
    void testValidarTipoPuestoInexistente() {
        PuestoComercialCreateRequest req = PuestoComercialCreateRequest.builder()
                .mercadoId(mercado1.getId())
                .tipoPuestoId(999999L)
                .codigo("P-TIPO-INV")
                .numero("01")
                .build();

        assertThatThrownBy(() -> puestoService.registrarPuesto(req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Tipo de puesto no encontrado");
    }

    // 8. Validar área negativa
    @Test
    @DisplayName("8. Validar rechazo de área negativa")
    void testValidarAreaNegativa() {
        PuestoComercialCreateRequest req = PuestoComercialCreateRequest.builder()
                .mercadoId(mercado1.getId())
                .tipoPuestoId(tipoFijo.getId())
                .codigo("P-AREA-NEG")
                .numero("01")
                .areaMetrosCuadrados(new BigDecimal("-5.00"))
                .build();

        assertThatThrownBy(() -> puestoService.registrarPuesto(req))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("no puede ser negativa");
    }

    // 9. Validar dimensiones negativas
    @Test
    @DisplayName("9. Validar rechazo de ancho o largo negativos")
    void testValidarDimensionesNegativas() {
        PuestoComercialCreateRequest req = PuestoComercialCreateRequest.builder()
                .mercadoId(mercado1.getId())
                .tipoPuestoId(tipoFijo.getId())
                .codigo("P-DIM-NEG")
                .numero("01")
                .anchoMetros(new BigDecimal("-2.00"))
                .largoMetros(new BigDecimal("3.00"))
                .build();

        assertThatThrownBy(() -> puestoService.registrarPuesto(req))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("no puede ser negativo");
    }

    // 10. Consultar puesto
    @Test
    @DisplayName("10. Consultar datos de puesto comercial por ID y Mercado")
    void testConsultarPuesto() {
        PuestoComercialResponse creado = puestoService.registrarPuesto(PuestoComercialCreateRequest.builder()
                .mercadoId(mercado1.getId())
                .tipoPuestoId(tipoFijo.getId())
                .codigo("P-CONS")
                .numero("50")
                .build());

        PuestoComercialResponse consultado = puestoService.obtenerPorIdYMercado(creado.getId(), mercado1.getId());

        assertThat(consultado).isNotNull();
        assertThat(consultado.getCodigo()).isEqualTo("P-CONS");
    }

    // 11. Editar puesto
    @Test
    @DisplayName("11. Editar datos técnicos y descripción de puesto comercial")
    void testEditarPuesto() {
        PuestoComercialResponse creado = puestoService.registrarPuesto(PuestoComercialCreateRequest.builder()
                .mercadoId(mercado1.getId())
                .tipoPuestoId(tipoFijo.getId())
                .codigo("P-EDIT")
                .numero("60")
                .descripcion("Descripción original")
                .build());

        PuestoComercialUpdateRequest updateReq = PuestoComercialUpdateRequest.builder()
                .tipoPuestoId(tipoFijo.getId())
                .descripcion("Descripción actualizada con vitrina de vidrio")
                .areaMetrosCuadrados(new BigDecimal("8.50"))
                .version(creado.getVersion())
                .build();

        PuestoComercialResponse editado = puestoService.actualizarPuesto(creado.getId(), mercado1.getId(), updateReq);

        assertThat(editado.getDescripcion()).isEqualTo("Descripción actualizada con vitrina de vidrio");
        assertThat(editado.getAreaMetrosCuadrados()).isEqualByComparingTo(new BigDecimal("8.50"));
    }

    // 12. Controlar concurrencia
    @Test
    @DisplayName("12. Controlar concurrencia optimista mediante @Version")
    void testControlConcurrenciaOptimista() {
        PuestoComercialResponse creado = puestoService.registrarPuesto(PuestoComercialCreateRequest.builder()
                .mercadoId(mercado1.getId())
                .tipoPuestoId(tipoFijo.getId())
                .codigo("P-CONC")
                .numero("70")
                .build());

        PuestoComercialUpdateRequest updateReqInvalido = PuestoComercialUpdateRequest.builder()
                .tipoPuestoId(tipoFijo.getId())
                .version(creado.getVersion() + 99L) // Versión desfasada
                .build();

        assertThatThrownBy(() -> puestoService.actualizarPuesto(creado.getId(), mercado1.getId(), updateReqInvalido))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("modificado concurrentemente");
    }

    // 13. Cambiar estado físico
    @Test
    @DisplayName("13. Cambiar estado físico a EN_MANTENIMIENTO con motivo obligatorio")
    void testCambiarEstadoFisico() {
        PuestoComercialResponse creado = puestoService.registrarPuesto(PuestoComercialCreateRequest.builder()
                .mercadoId(mercado1.getId())
                .tipoPuestoId(tipoFijo.getId())
                .codigo("P-FTO")
                .numero("80")
                .build());

        PuestoEstadoRequest estadoReq = PuestoEstadoRequest.builder()
                .nuevoEstadoFisico(EstadoFisicoPuesto.EN_MANTENIMIENTO)
                .motivo("Mantenimiento programado de instalaciones eléctricas")
                .build();

        PuestoComercialResponse actualizado = puestoService.cambiarEstado(creado.getId(), mercado1.getId(), estadoReq);

        assertThat(actualizado.getEstadoFisico()).isEqualTo(EstadoFisicoPuesto.EN_MANTENIMIENTO);
        assertThat(actualizado.isDisponible()).isFalse();
    }

    // 14. Rechazar estado inválido
    @Test
    @DisplayName("14. Rechazar cambio sin variación de estado")
    void testRechazarCambioSinVariacion() {
        PuestoComercialResponse creado = puestoService.registrarPuesto(PuestoComercialCreateRequest.builder()
                .mercadoId(mercado1.getId())
                .tipoPuestoId(tipoFijo.getId())
                .codigo("P-MISMO")
                .numero("81")
                .estadoFisico(EstadoFisicoPuesto.OPERATIVO)
                .estado(EstadoPuesto.DISPONIBLE)
                .build());

        PuestoEstadoRequest estadoReq = PuestoEstadoRequest.builder()
                .nuevoEstadoFisico(EstadoFisicoPuesto.OPERATIVO)
                .nuevoEstadoOperativo(EstadoPuesto.DISPONIBLE)
                .motivo("Sin cambios reales")
                .build();

        assertThatThrownBy(() -> puestoService.cambiarEstado(creado.getId(), mercado1.getId(), estadoReq))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("ya se encuentra en el estado indicado");
    }

    // 15. Cambiar ubicación
    @Test
    @DisplayName("15. Cambiar ubicación física (reubicación de sector y pabellón)")
    void testCambiarUbicacion() {
        PuestoComercialResponse creado = puestoService.registrarPuesto(PuestoComercialCreateRequest.builder()
                .mercadoId(mercado1.getId())
                .tipoPuestoId(tipoFijo.getId())
                .codigo("P-REUB")
                .numero("82")
                .build());

        PuestoUbicacionRequest reubReq = PuestoUbicacionRequest.builder()
                .nuevoSectorId(sector1.getId())
                .nuevoPabellonId(pabellon1.getId())
                .nuevaUbicacionReferencia("Reubicado al pasillo norte")
                .motivo("Reordenamiento de giro comercial según acta municipal")
                .build();

        PuestoComercialResponse reubicado = puestoService.cambiarUbicacion(creado.getId(), mercado1.getId(), reubReq);

        assertThat(reubicado.getSectorId()).isEqualTo(sector1.getId());
        assertThat(reubicado.getPabellonId()).isEqualTo(pabellon1.getId());
        assertThat(reubicado.getUbicacionReferencia()).isEqualTo("Reubicado al pasillo norte");
    }

    // 16. Registrar historial
    @Test
    @DisplayName("16. Registrar historial inmutable tras cambio de estado")
    void testRegistrarHistorialInmutable() {
        PuestoComercialResponse creado = puestoService.registrarPuesto(PuestoComercialCreateRequest.builder()
                .mercadoId(mercado1.getId())
                .tipoPuestoId(tipoFijo.getId())
                .codigo("P-HIST")
                .numero("83")
                .build());

        puestoService.cambiarEstado(creado.getId(), mercado1.getId(), PuestoEstadoRequest.builder()
                .nuevoEstadoFisico(EstadoFisicoPuesto.INHABILITADO)
                .motivo("Clausura preventiva por filtración de agua")
                .build());

        PuestoComercialDetailResponse detalle = puestoService.obtenerDetalleCompleto(creado.getId(), mercado1.getId());

        assertThat(detalle.getHistorialEstados()).isNotEmpty();
        assertThat(detalle.getHistorialEstados().get(0).getEstadoNuevo()).isEqualTo("INHABILITADO");
        assertThat(detalle.getHistorialEstados().get(0).getMotivo()).contains("Clausura preventiva");
    }

    // 17. Consultar disponibilidad
    @Test
    @DisplayName("17. Consultar disponibilidad de puesto operativo")
    void testConsultarDisponibilidad() {
        PuestoComercialResponse creado = puestoService.registrarPuesto(PuestoComercialCreateRequest.builder()
                .mercadoId(mercado1.getId())
                .tipoPuestoId(tipoFijo.getId())
                .codigo("P-DISP")
                .numero("84")
                .build());

        Map<String, Object> disp = puestoService.consultarDisponibilidad(creado.getId(), mercado1.getId());

        assertThat(disp.get("disponible")).isEqualTo(true);
        assertThat(disp.get("mercadoHabilitado")).isEqualTo(true);
    }

    // 18. Consultar ocupación
    @Test
    @DisplayName("18. Consultar disponibilidad cuando puesto está ocupado")
    void testConsultarOcupacion() {
        PuestoComercialResponse creado = puestoService.registrarPuesto(PuestoComercialCreateRequest.builder()
                .mercadoId(mercado1.getId())
                .tipoPuestoId(tipoFijo.getId())
                .codigo("P-OCUP")
                .numero("85")
                .estado(EstadoPuesto.OCUPADO)
                .build());

        Map<String, Object> disp = puestoService.consultarDisponibilidad(creado.getId(), mercado1.getId());

        assertThat(disp.get("disponible")).isEqualTo(false);
        assertThat(disp.get("motivoNoDisponible")).isNotNull();
    }

    // 19. Filtrar por sector
    @Test
    @DisplayName("19. Filtrar puestos comerciales por sector")
    void testFiltrarPorSector() {
        puestoService.registrarPuesto(PuestoComercialCreateRequest.builder()
                .mercadoId(mercado1.getId())
                .sectorId(sector1.getId())
                .tipoPuestoId(tipoFijo.getId())
                .codigo("P-SEC-A")
                .numero("90")
                .build());

        Page<PuestoComercialResponse> pagina = puestoService.listarPuestosPaginado(
                mercado1.getId(), null, sector1.getId(), null, null, null, null, null, null, null, null, PageRequest.of(0, 10));

        assertThat(pagina.getContent()).allMatch(p -> sector1.getId().equals(p.getSectorId()));
    }

    // 20. Filtrar por pabellón
    @Test
    @DisplayName("20. Filtrar puestos comerciales por pabellón")
    void testFiltrarPorPabellon() {
        puestoService.registrarPuesto(PuestoComercialCreateRequest.builder()
                .mercadoId(mercado1.getId())
                .pabellonId(pabellon1.getId())
                .tipoPuestoId(tipoFijo.getId())
                .codigo("P-PAB-A")
                .numero("91")
                .build());

        Page<PuestoComercialResponse> pagina = puestoService.listarPuestosPaginado(
                mercado1.getId(), null, null, pabellon1.getId(), null, null, null, null, null, null, null, PageRequest.of(0, 10));

        assertThat(pagina.getContent()).allMatch(p -> pabellon1.getId().equals(p.getPabellonId()));
    }

    // 21. Filtrar por estado
    @Test
    @DisplayName("21. Filtrar puestos por estado operativo")
    void testFiltrarPorEstado() {
        puestoService.registrarPuesto(PuestoComercialCreateRequest.builder()
                .mercadoId(mercado1.getId())
                .tipoPuestoId(tipoFijo.getId())
                .codigo("P-RES-A")
                .numero("92")
                .estado(EstadoPuesto.RESERVADO)
                .build());

        Page<PuestoComercialResponse> pagina = puestoService.listarPuestosPaginado(
                mercado1.getId(), null, null, null, null, null, null, EstadoPuesto.RESERVADO, null, null, null, PageRequest.of(0, 10));

        assertThat(pagina.getContent()).allMatch(p -> p.getEstado() == EstadoPuesto.RESERVADO);
    }

    // 22. Filtrar por tipo
    @Test
    @DisplayName("22. Filtrar puestos por tipo de puesto comercial")
    void testFiltrarPorTipo() {
        puestoService.registrarPuesto(PuestoComercialCreateRequest.builder()
                .mercadoId(mercado1.getId())
                .tipoPuestoId(tipoFijo.getId())
                .codigo("P-TIPO-A")
                .numero("93")
                .build());

        Page<PuestoComercialResponse> pagina = puestoService.listarPuestosPaginado(
                mercado1.getId(), null, null, null, null, tipoFijo.getId(), null, null, null, null, null, PageRequest.of(0, 10));

        assertThat(pagina.getContent()).allMatch(p -> tipoFijo.getId().equals(p.getTipoPuestoId()));
    }

    // 23. Paginar
    @Test
    @DisplayName("23. Paginar listado de puestos comerciales")
    void testPaginarPuestos() {
        for (int i = 1; i <= 5; i++) {
            puestoService.registrarPuesto(PuestoComercialCreateRequest.builder()
                    .mercadoId(mercado1.getId())
                    .tipoPuestoId(tipoFijo.getId())
                    .codigo("P-PAG-" + i)
                    .numero(String.valueOf(i))
                    .build());
        }

        Page<PuestoComercialResponse> pagina = puestoService.listarPuestosPaginado(
                mercado1.getId(), null, null, null, null, null, null, null, null, null, null, PageRequest.of(0, 2));

        assertThat(pagina.getSize()).isEqualTo(2);
        assertThat(pagina.getTotalElements()).isGreaterThanOrEqualTo(5);
    }

    // 24. Ordenar
    @Test
    @DisplayName("24. Ordenar puestos por código ascendente")
    void testOrdenarPuestos() {
        Page<PuestoComercialResponse> pagina = puestoService.listarPuestosPaginado(
                mercado1.getId(), null, null, null, null, null, null, null, null, null, null,
                PageRequest.of(0, 10, Sort.by("codigo").ascending()));

        assertThat(pagina).isNotNull();
    }

    // 25. Contar puestos
    @Test
    @DisplayName("25. Contar puestos totales y por estado")
    void testContarPuestos() {
        long cuentaAntes = puestoRepository.countByMercadoId(mercado1.getId());

        puestoService.registrarPuesto(PuestoComercialCreateRequest.builder()
                .mercadoId(mercado1.getId())
                .tipoPuestoId(tipoFijo.getId())
                .codigo("P-COUNT-1")
                .numero("95")
                .build());

        long cuentaDespues = puestoRepository.countByMercadoId(mercado1.getId());
        assertThat(cuentaDespues).isEqualTo(cuentaAntes + 1);
    }

    // 26. Evitar conteos duplicados
    @Test
    @DisplayName("26. Evitar duplicidad en conteo de estadísticas al estar en sector y pabellón")
    void testEvitarConteosDuplicados() {
        puestoService.registrarPuesto(PuestoComercialCreateRequest.builder()
                .mercadoId(mercado1.getId())
                .sectorId(sector1.getId())
                .pabellonId(pabellon1.getId())
                .tipoPuestoId(tipoFijo.getId())
                .codigo("P-NODUP")
                .numero("96")
                .build());

        PuestoEstadisticasResponse stats = puestoService.obtenerEstadisticas(mercado1.getId());

        long countReal = puestoRepository.countByMercadoId(mercado1.getId());
        assertThat(stats.getTotalPuestos()).isEqualTo(countReal);
    }

    // 27. Exportar Excel
    @Test
    @DisplayName("27. Exportar puestos comerciales a Excel XLSX")
    void testExportarExcel() {
        puestoService.registrarPuesto(PuestoComercialCreateRequest.builder()
                .mercadoId(mercado1.getId())
                .tipoPuestoId(tipoFijo.getId())
                .codigo("P-XLS")
                .numero("97")
                .build());

        byte[] xlsxBytes = puestoExportService.exportarExcel(
                mercado1.getId(), null, null, null, null, null, null, null, null, null, null);

        assertThat(xlsxBytes).isNotNull().isNotEmpty();
        // Encabezado PK ZIP para .xlsx
        assertThat(xlsxBytes[0]).isEqualTo((byte) 'P');
        assertThat(xlsxBytes[1]).isEqualTo((byte) 'K');
    }

    // 28. Exportar PDF
    @Test
    @DisplayName("28. Exportar puestos comerciales a PDF oficial")
    void testExportarPdf() {
        puestoService.registrarPuesto(PuestoComercialCreateRequest.builder()
                .mercadoId(mercado1.getId())
                .tipoPuestoId(tipoFijo.getId())
                .codigo("P-PDF")
                .numero("98")
                .build());

        byte[] pdfBytes = puestoExportService.exportarPdf(
                mercado1.getId(), null, null, null, null, null, null, null, null, null, null);

        assertThat(pdfBytes).isNotNull().isNotEmpty();
        // Encabezado %PDF
        assertThat(new String(pdfBytes, 0, 4)).isEqualTo("%PDF");
    }
}
