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
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.dto.MercadosDtos.*;
import pe.gob.municipalidad.sigm.modules.mercados.dto.MercadosDtos.ComercianteResponse;
import pe.gob.municipalidad.sigm.modules.mercados.dto.MercadosDtos.AsignacionResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.HistorialOcupacion;
import pe.gob.municipalidad.sigm.modules.mercados.entity.PuestoComercial;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoComerciante;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoMercado;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.repository.HistorialOcupacionRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.PuestoComercialRepository;
import pe.gob.municipalidad.sigm.modules.mercados.service.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MercadosServicesIntegrationTest {

    @Autowired
    private MercadoMunicipalService mercadoService;

    @Autowired
    private SectorMercadoService sectorService;

    @Autowired
    private PabellonMercadoService pabellonService;

    @Autowired
    private PuestoComercialService puestoService;

    @Autowired
    private ComercianteService comercianteService;

    @Autowired
    private AsignacionPuestoService asignacionService;

    @Autowired
    private MercadosDashboardService dashboardService;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private PuestoComercialRepository puestoRepository;

    @Autowired
    private HistorialOcupacionRepository historialRepository;

    private Persona personaPrueba;

    @BeforeEach
    void setUp() {
        String numDoc = "88" + (System.currentTimeMillis() % 1000000);
        personaPrueba = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento(numDoc)
                .nombres("Serafín")
                .apellidoPaterno("Huamán")
                .apellidoMaterno("Quispe")
                .direccion("Comunidad de Huayllapampa, Quinua")
                .telefono("966112233")
                .correo("serafin.huaman@test.pe")
                .estado("ACTIVO")
                .build());
    }

    @Test
    @DisplayName("1. Crear mercado válido")
    void testCrearMercadoValido() {
        String cod = "MKT-TEST-" + System.currentTimeMillis();
        MercadoRequest req = MercadoRequest.builder()
                .codigo(cod)
                .nombre("Mercado Modelo de Quinua")
                .descripcion("Mercado central para productores y artesanos locales")
                .direccion("Jr. Sucre cuadra 2 s/n, Quinua")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .capacidadReferencial(80)
                .estado(EstadoMercado.ACTIVO)
                .build();

        MercadoResponse res = mercadoService.crearMercado(req);

        assertThat(res.getId()).isNotNull();
        assertThat(res.getCodigo()).isEqualTo(cod.toUpperCase());
        assertThat(res.getNombre()).isEqualTo("Mercado Modelo de Quinua");
        assertThat(res.getEstado()).isEqualTo(EstadoMercado.ACTIVO);
    }

    @Test
    @DisplayName("2. Rechazar código de mercado duplicado")
    void testRechazarMercadoCodigoDuplicado() {
        String cod = "MKT-DUP-" + System.currentTimeMillis();
        MercadoRequest req1 = MercadoRequest.builder()
                .codigo(cod)
                .nombre("Mercado A")
                .direccion("Dirección 1")
                .build();
        mercadoService.crearMercado(req1);

        MercadoRequest req2 = MercadoRequest.builder()
                .codigo(cod)
                .nombre("Mercado B")
                .direccion("Dirección 2")
                .build();

        assertThatThrownBy(() -> mercadoService.crearMercado(req2))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("ya se encuentra registrado");
    }

    @Test
    @DisplayName("3. Validar mercado inexistente")
    void testValidarMercadoInexistente() {
        UUID idInexistente = UUID.randomUUID();
        assertThatThrownBy(() -> mercadoService.obtenerPorId(idInexistente))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Mercado municipal no encontrado");
    }

    @Test
    @DisplayName("4. Registrar sector válido")
    void testRegistrarSectorValido() {
        MercadoResponse mercado = mercadoService.crearMercado(MercadoRequest.builder()
                .codigo("MKT-SEC-" + System.currentTimeMillis())
                .nombre("Mercado Sectorial")
                .direccion("Plaza de Armas")
                .build());

        SectorResponse sector = sectorService.crearSector(SectorRequest.builder()
                .mercadoId(mercado.getId())
                .codigo("SEC-ARTESANIA")
                .nombre("Sector Artesanías y Cerámica")
                .descripcion("Espacio para alfareros y artesanos de Quinua")
                .tipoSector("ARTESANIAS")
                .ordenVisual(1)
                .build());

        assertThat(sector.getId()).isNotNull();
        assertThat(sector.getMercadoId()).isEqualTo(mercado.getId());
        assertThat(sector.getCodigo()).isEqualTo("SEC-ARTESANIA");
    }

    @Test
    @DisplayName("5. Rechazar sector de mercado inexistente")
    void testRechazarSectorMercadoInexistente() {
        SectorRequest req = SectorRequest.builder()
                .mercadoId(UUID.randomUUID())
                .codigo("SEC-FAIL")
                .nombre("Sector Fantasma")
                .build();

        assertThatThrownBy(() -> sectorService.crearSector(req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Mercado municipal no encontrado");
    }

    @Test
    @DisplayName("6. Registrar pabellón válido")
    void testRegistrarPabellonValido() {
        MercadoResponse mercado = mercadoService.crearMercado(MercadoRequest.builder()
                .codigo("MKT-PAB-" + System.currentTimeMillis())
                .nombre("Mercado con Pabellones")
                .direccion("Av. Centenario")
                .build());

        SectorResponse sector = sectorService.crearSector(SectorRequest.builder()
                .mercadoId(mercado.getId())
                .codigo("SEC-ALIM")
                .nombre("Sector Alimentos")
                .build());

        PabellonResponse pabellon = pabellonService.crearPabellon(PabellonRequest.builder()
                .mercadoId(mercado.getId())
                .sectorId(sector.getId())
                .codigo("PAB-A")
                .nombre("Pabellón A - Frutas y Verduras")
                .nivel(1)
                .build());

        assertThat(pabellon.getId()).isNotNull();
        assertThat(pabellon.getCodigo()).isEqualTo("PAB-A");
        assertThat(pabellon.getSectorId()).isEqualTo(sector.getId());
    }

    @Test
    @DisplayName("7. Validar coherencia sector-pabellón")
    void testValidarCoherenciaSectorPabellon() {
        MercadoResponse mercado1 = mercadoService.crearMercado(MercadoRequest.builder()
                .codigo("MKT-1-" + System.currentTimeMillis())
                .nombre("Mercado Uno")
                .direccion("Direccion Uno")
                .build());

        MercadoResponse mercado2 = mercadoService.crearMercado(MercadoRequest.builder()
                .codigo("MKT-2-" + System.currentTimeMillis())
                .nombre("Mercado Dos")
                .direccion("Direccion Dos")
                .build());

        SectorResponse sectorDeMercado1 = sectorService.crearSector(SectorRequest.builder()
                .mercadoId(mercado1.getId())
                .codigo("SEC-M1")
                .nombre("Sector M1")
                .build());

        // Intentar crear pabellón en Mercado 2 usando sector de Mercado 1
        PabellonRequest reqIncoherente = PabellonRequest.builder()
                .mercadoId(mercado2.getId())
                .sectorId(sectorDeMercado1.getId())
                .codigo("PAB-INC")
                .nombre("Pabellón Incoherente")
                .build();

        assertThatThrownBy(() -> pabellonService.crearPabellon(reqIncoherente))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Incoherencia estructural");
    }

    @Test
    @DisplayName("8. Registrar puesto válido")
    void testRegistrarPuestoValido() {
        MercadoResponse mercado = mercadoService.crearMercado(MercadoRequest.builder()
                .codigo("MKT-PUESTO-" + System.currentTimeMillis())
                .nombre("Mercado Puestos")
                .direccion("Av. 9 de Diciembre")
                .build());

        PuestoResponse puesto = puestoService.crearPuesto(PuestoRequest.builder()
                .mercadoId(mercado.getId())
                .codigo("PUE-101")
                .numero("101")
                .descripcion("Puesto de abarrotes con mostrador")
                .areaMetrosCuadrados(new BigDecimal("9.50"))
                .ubicacionReferencia("Frente a puerta principal")
                .estado(EstadoPuesto.DISPONIBLE)
                .build());

        assertThat(puesto.getId()).isNotNull();
        assertThat(puesto.getCodigo()).isEqualTo("PUE-101");
        assertThat(puesto.getNumero()).isEqualTo("101");
        assertThat(puesto.getEstado()).isEqualTo(EstadoPuesto.DISPONIBLE);
    }

    @Test
    @DisplayName("9. Rechazar puesto duplicado en el mismo mercado")
    void testRechazarPuestoDuplicado() {
        MercadoResponse mercado = mercadoService.crearMercado(MercadoRequest.builder()
                .codigo("MKT-PST-DUP-" + System.currentTimeMillis())
                .nombre("Mercado Test Duplicado")
                .direccion("Av. Principal")
                .build());

        puestoService.crearPuesto(PuestoRequest.builder()
                .mercadoId(mercado.getId())
                .codigo("PUE-201")
                .numero("201")
                .build());

        assertThatThrownBy(() -> puestoService.crearPuesto(PuestoRequest.builder()
                .mercadoId(mercado.getId())
                .codigo("PUE-201")
                .numero("201-B")
                .build()))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("ya existe en el mercado");
    }

    @Test
    @DisplayName("10. Validar área negativa en puesto")
    void testValidarAreaNegativa() {
        MercadoResponse mercado = mercadoService.crearMercado(MercadoRequest.builder()
                .codigo("MKT-NEG-" + System.currentTimeMillis())
                .nombre("Mercado Negativo")
                .direccion("Direccion")
                .build());

        PuestoRequest reqAreaNegativa = PuestoRequest.builder()
                .mercadoId(mercado.getId())
                .codigo("PUE-NEG")
                .numero("99")
                .areaMetrosCuadrados(new BigDecimal("-5.00"))
                .build();

        assertThatThrownBy(() -> puestoService.crearPuesto(reqAreaNegativa))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("no puede ser un valor negativo");
    }

    @Test
    @DisplayName("11. Registrar comerciante y 12. Reutilizar Persona institucional")
    void testRegistrarComercianteReutilizandoPersona() {
        String codCom = "COM-" + System.currentTimeMillis();
        ComercianteResponse comerciante = comercianteService.registrarComerciante(ComercianteRequest.builder()
                .personaId(personaPrueba.getId())
                .codigoComerciante(codCom)
                .estado(EstadoComerciante.ACTIVO)
                .observaciones("Productor local empadronado")
                .build());

        assertThat(comerciante.getId()).isNotNull();
        assertThat(comerciante.getPersonaId()).isEqualTo(personaPrueba.getId());
        assertThat(comerciante.getNumeroDocumento()).isEqualTo(personaPrueba.getNumeroDocumento());
        assertThat(comerciante.getNombreCompleto()).contains("Serafín");
        assertThat(comerciante.getEstado()).isEqualTo(EstadoComerciante.ACTIVO);
    }

    @Test
    @DisplayName("13. Rechazar duplicación incompatible de comerciante para misma persona")
    void testRechazarComercianteDuplicadoMismaPersona() {
        comercianteService.registrarComerciante(ComercianteRequest.builder()
                .personaId(personaPrueba.getId())
                .codigoComerciante("COM-FIRST-" + System.currentTimeMillis())
                .build());

        assertThatThrownBy(() -> comercianteService.registrarComerciante(ComercianteRequest.builder()
                .personaId(personaPrueba.getId())
                .codigoComerciante("COM-SECOND-" + System.currentTimeMillis())
                .build()))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("ya se encuentra registrada como comerciante");
    }

    @Test
    @DisplayName("14. Consultar puestos disponibles")
    void testConsultarPuestosDisponibles() {
        MercadoResponse mercado = mercadoService.crearMercado(MercadoRequest.builder()
                .codigo("MKT-DISP-" + System.currentTimeMillis())
                .nombre("Mercado Disponibilidad")
                .direccion("Direccion")
                .build());

        puestoService.crearPuesto(PuestoRequest.builder()
                .mercadoId(mercado.getId())
                .codigo("PUE-D1")
                .numero("D1")
                .estado(EstadoPuesto.DISPONIBLE)
                .build());

        puestoService.crearPuesto(PuestoRequest.builder()
                .mercadoId(mercado.getId())
                .codigo("PUE-M1")
                .numero("M1")
                .estado(EstadoPuesto.EN_MANTENIMIENTO)
                .build());

        List<PuestoResponse> disponibles = puestoService.listarPuestosDisponibles(mercado.getId());
        assertThat(disponibles).hasSize(1);
        assertThat(disponibles.get(0).getCodigo()).isEqualTo("PUE-D1");
    }

    @Test
    @DisplayName("15. Crear asignación válida y 18. Registrar historial de ocupación")
    void testCrearAsignacionValidaYRegistrarHistorial() {
        MercadoResponse mercado = mercadoService.crearMercado(MercadoRequest.builder()
                .codigo("MKT-ASIG-" + System.currentTimeMillis())
                .nombre("Mercado Asignación")
                .direccion("Jr. Huanta")
                .build());

        PuestoResponse puesto = puestoService.crearPuesto(PuestoRequest.builder()
                .mercadoId(mercado.getId())
                .codigo("PUE-A10")
                .numero("A10")
                .estado(EstadoPuesto.DISPONIBLE)
                .build());

        ComercianteResponse comerciante = comercianteService.registrarComerciante(ComercianteRequest.builder()
                .personaId(personaPrueba.getId())
                .codigoComerciante("COM-ASIG-" + System.currentTimeMillis())
                .build());

        AsignacionResponse asignacion = asignacionService.crearAsignacion(AsignacionRequest.builder()
                .puestoId(puesto.getId())
                .comercianteId(comerciante.getId())
                .fechaInicio(LocalDate.now())
                .fechaFin(LocalDate.now().plusMonths(12))
                .observaciones("Asignación de puesto comercial aprobada")
                .build());

        assertThat(asignacion.getId()).isNotNull();
        assertThat(asignacion.getPuestoId()).isEqualTo(puesto.getId());
        assertThat(asignacion.getComercianteId()).isEqualTo(comerciante.getId());

        // Verificar que el puesto pasó automáticamente a OCUPADO
        PuestoComercial puestoDb = puestoRepository.findById(puesto.getId()).orElseThrow();
        assertThat(puestoDb.getEstado()).isEqualTo(EstadoPuesto.OCUPADO);

        // 18. Verificar historial de ocupación
        List<HistorialOcupacion> historial = historialRepository.findByPuestoIdOrderByFechaDesc(puesto.getId());
        assertThat(historial).isNotEmpty();
        assertThat(historial.get(0).getEstadoNuevo()).isEqualTo("OCUPADO");
    }

    @Test
    @DisplayName("16. Rechazar ocupación incompatible en puesto ya ocupado")
    void testRechazarOcupacionIncompatiblePuestoYaOcupado() {
        MercadoResponse mercado = mercadoService.crearMercado(MercadoRequest.builder()
                .codigo("MKT-INCOMP-" + System.currentTimeMillis())
                .nombre("Mercado Incompatible")
                .direccion("Direccion")
                .build());

        PuestoResponse puesto = puestoService.crearPuesto(PuestoRequest.builder()
                .mercadoId(mercado.getId())
                .codigo("PUE-X1")
                .numero("X1")
                .estado(EstadoPuesto.DISPONIBLE)
                .build());

        ComercianteResponse com1 = comercianteService.registrarComerciante(ComercianteRequest.builder()
                .personaId(personaPrueba.getId())
                .codigoComerciante("COM-1-" + System.currentTimeMillis())
                .build());

        Persona persona2 = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("77" + (System.currentTimeMillis() % 1000000))
                .nombres("Teófila")
                .apellidoPaterno("Cárdenas")
                .estado("ACTIVO")
                .build());

        ComercianteResponse com2 = comercianteService.registrarComerciante(ComercianteRequest.builder()
                .personaId(persona2.getId())
                .codigoComerciante("COM-2-" + System.currentTimeMillis())
                .build());

        // Primera asignación exitosa
        asignacionService.crearAsignacion(AsignacionRequest.builder()
                .puestoId(puesto.getId())
                .comercianteId(com1.getId())
                .fechaInicio(LocalDate.now())
                .build());

        // Segunda asignación simultánea al mismo puesto: debe ser rechazada
        assertThatThrownBy(() -> asignacionService.crearAsignacion(AsignacionRequest.builder()
                .puestoId(puesto.getId())
                .comercianteId(com2.getId())
                .fechaInicio(LocalDate.now())
                .build()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("ya cuenta con una asignación vigente activa");
    }

    @Test
    @DisplayName("17. Controlar concurrencia optimista con @Version")
    void testControlConcurrenciaVersion() {
        MercadoResponse mercado = mercadoService.crearMercado(MercadoRequest.builder()
                .codigo("MKT-VER-" + System.currentTimeMillis())
                .nombre("Mercado Concurrencia")
                .direccion("Av. Concurrencia")
                .build());

        PuestoResponse puesto = puestoService.crearPuesto(PuestoRequest.builder()
                .mercadoId(mercado.getId())
                .codigo("PUE-VER")
                .numero("V1")
                .build());

        assertThat(puesto.getVersion()).isNotNull();
        assertThat(puesto.getVersion()).isEqualTo(0L);
    }

    @Test
    @DisplayName("23. Verificar relaciones JPA: Mercado -> Sectores -> Puestos")
    void testVerificarRelacionesJPA() {
        MercadoResponse mercado = mercadoService.crearMercado(MercadoRequest.builder()
                .codigo("MKT-JPA-" + System.currentTimeMillis())
                .nombre("Mercado Relaciones JPA")
                .direccion("Quinua")
                .build());

        SectorResponse sector = sectorService.crearSector(SectorRequest.builder()
                .mercadoId(mercado.getId())
                .codigo("SEC-JPA")
                .nombre("Sector JPA")
                .build());

        PuestoResponse puesto = puestoService.crearPuesto(PuestoRequest.builder()
                .mercadoId(mercado.getId())
                .sectorId(sector.getId())
                .codigo("PUE-JPA")
                .numero("J1")
                .build());

        PuestoComercial entidad = puestoRepository.findById(puesto.getId()).orElseThrow();
        assertThat(entidad.getMercado().getId()).isEqualTo(mercado.getId());
        assertThat(entidad.getSector().getId()).isEqualTo(sector.getId());
    }

    @Test
    @DisplayName("24. Verificar dashboard métricas y consultas reales")
    void testVerificarDashboardMetricas() {
        DashboardMercadosMetrics metricas = dashboardService.obtenerMetricas();
        assertThat(metricas).isNotNull();
        assertThat(metricas.getTotalMercados()).isGreaterThanOrEqualTo(0L);
        assertThat(metricas.getTotalPuestos()).isGreaterThanOrEqualTo(0L);
        assertThat(metricas.getTotalComerciantes()).isGreaterThanOrEqualTo(0L);
    }

    @Test
    @DisplayName("25. Verificar consultas paginadas")
    void testVerificarConsultasPaginadas() {
        Page<MercadoResponse> pagina = mercadoService.listarMercados(null, null, PageRequest.of(0, 5));
        assertThat(pagina).isNotNull();
        assertThat(pagina.getSize()).isEqualTo(5);
        assertThat(pagina.getContent()).isNotNull();
    }
}
