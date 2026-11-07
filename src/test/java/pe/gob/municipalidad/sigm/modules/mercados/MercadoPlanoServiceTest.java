package pe.gob.municipalidad.sigm.modules.mercados;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.*;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;
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
class MercadoPlanoServiceTest {

    @Autowired private PlanoMercadoService planoService;
    @Autowired private VersionPlanoMercadoService versionService;
    @Autowired private EditorPlanoMercadoService editorService;
    @Autowired private PublicacionPlanoMercadoService publicacionService;
    @Autowired private ConsultaPlanoMercadoService consultaService;
    @Autowired private EstadoVisualPuestoService estadoVisualService;

    @Autowired private MercadoMunicipalRepository mercadoRepository;
    @Autowired private PuestoComercialRepository puestoRepository;
    @Autowired private AsignacionPuestoRepository asignacionRepository;
    @Autowired private ComercianteRepository comercianteRepository;
    @Autowired private PersonaRepository personaRepository;

    private MercadoMunicipal mercado1;
    private MercadoMunicipal mercado2;
    private PuestoComercial puesto1;
    private PuestoComercial puesto2;
    private PuestoComercial puestoOtroMercado;

    @BeforeEach
    void setUp() {
        long ts = System.currentTimeMillis();

        mercado1 = mercadoRepository.save(MercadoMunicipal.builder()
                .nombre("Mercado Central Quinua " + ts)
                .codigo("MCQ-" + (ts % 10000))
                .direccion("Jr. Sucre s/n")
                .estado(EstadoMercado.ACTIVO)
                .build());

        mercado2 = mercadoRepository.save(MercadoMunicipal.builder()
                .nombre("Mercado de Productores " + ts)
                .codigo("MPQ-" + (ts % 10000))
                .direccion("Av. Los Libertadores")
                .estado(EstadoMercado.ACTIVO)
                .build());

        puesto1 = puestoRepository.save(PuestoComercial.builder()
                .mercado(mercado1)
                .codigo("P-101")
                .numero("101")
                .nombreReferencial("Abarrotes")
                .areaMetrosCuadrados(new BigDecimal("12.50"))
                .anchoMetros(new BigDecimal("3.00"))
                .largoMetros(new BigDecimal("4.00"))
                .estado(EstadoPuesto.DISPONIBLE)
                .estadoFisico(EstadoFisicoPuesto.OPERATIVO)
                .build());

        puesto2 = puestoRepository.save(PuestoComercial.builder()
                .mercado(mercado1)
                .codigo("P-102")
                .numero("102")
                .nombreReferencial("Verduras")
                .areaMetrosCuadrados(new BigDecimal("10.00"))
                .anchoMetros(new BigDecimal("2.50"))
                .largoMetros(new BigDecimal("4.00"))
                .estado(EstadoPuesto.DISPONIBLE)
                .estadoFisico(EstadoFisicoPuesto.OPERATIVO)
                .build());

        puestoOtroMercado = puestoRepository.save(PuestoComercial.builder()
                .mercado(mercado2)
                .codigo("EXT-201")
                .numero("201")
                .areaMetrosCuadrados(new BigDecimal("15.00"))
                .estado(EstadoPuesto.DISPONIBLE)
                .estadoFisico(EstadoFisicoPuesto.OPERATIVO)
                .build());
    }

    @Test
    @DisplayName("Crear plano maestro inicializa versión 1 en borrador")
    void testCrearPlanoInicializaVersionBorrador() {
        PlanoMercadoCreateRequest req = PlanoMercadoCreateRequest.builder()
                .mercadoId(mercado1.getId())
                .codigo("PLN-" + System.currentTimeMillis())
                .nombre("Plano Arquitectónico General")
                .descripcion("Distribución física inicial")
                .anchoBase(new BigDecimal("1400.00"))
                .altoBase(new BigDecimal("900.00"))
                .unidadReferencia("PIXEL")
                .build();

        PlanoMercadoResponse resp = planoService.crearPlano(req, "admin", null);

        assertThat(resp).isNotNull();
        assertThat(resp.getId()).isNotNull();
        assertThat(resp.getVersionActualNumero()).isEqualTo(1);
        assertThat(resp.getVersionActualEstado()).isEqualTo("BORRADOR");
    }

    @Test
    @DisplayName("Rechaza crear plano con código duplicado")
    void testRechazaCodigoDuplicado() {
        String cod = "PLN-DUP-" + System.currentTimeMillis();

        PlanoMercadoCreateRequest req1 = PlanoMercadoCreateRequest.builder()
                .mercadoId(mercado1.getId())
                .codigo(cod)
                .nombre("Plano 1")
                .anchoBase(new BigDecimal("1200"))
                .altoBase(new BigDecimal("800"))
                .build();
        planoService.crearPlano(req1, "admin", null);

        PlanoMercadoCreateRequest req2 = PlanoMercadoCreateRequest.builder()
                .mercadoId(mercado2.getId())
                .codigo(cod)
                .nombre("Plano 2")
                .anchoBase(new BigDecimal("1200"))
                .altoBase(new BigDecimal("800"))
                .build();

        assertThatThrownBy(() -> planoService.crearPlano(req2, "admin", null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Ya existe un plano con el código");
    }

    @Test
    @DisplayName("Guardar borrador con puestos del mercado y validar cálculo de hash")
    void testGuardarBorradorConPuestos() {
        PlanoMercadoCreateRequest req = PlanoMercadoCreateRequest.builder()
                .mercadoId(mercado1.getId())
                .codigo("PLN-SAVE-" + System.currentTimeMillis())
                .nombre("Plano para Guardar")
                .anchoBase(new BigDecimal("1400"))
                .altoBase(new BigDecimal("900"))
                .build();
        PlanoMercadoResponse plano = planoService.crearPlano(req, "admin", null);

        ElementoPlanoRequest elem1 = ElementoPlanoRequest.builder()
                .tipoElemento(TipoElementoPlano.PUESTO)
                .referenciaEntidadId(puesto1.getId())
                .codigoVisual(puesto1.getCodigo())
                .etiqueta(puesto1.getNumero())
                .coordenadaX(new BigDecimal("100"))
                .coordenadaY(new BigDecimal("150"))
                .ancho(new BigDecimal("60"))
                .alto(new BigDecimal("60"))
                .build();

        ElementoPlanoRequest elem2 = ElementoPlanoRequest.builder()
                .tipoElemento(TipoElementoPlano.SECTOR)
                .codigoVisual("SEC-01")
                .etiqueta("Sector Abarrotes")
                .coordenadaX(new BigDecimal("50"))
                .coordenadaY(new BigDecimal("100"))
                .ancho(new BigDecimal("300"))
                .alto(new BigDecimal("200"))
                .build();

        GuardarPlanoRequest saveReq = GuardarPlanoRequest.builder()
                .versionId(plano.getVersionActualId())
                .descripcionCambios("Agregado puesto 1 y sector general")
                .elementos(List.of(elem1, elem2))
                .build();

        VersionPlanoResponse guardado = editorService.guardarBorrador(plano.getId(), saveReq, "admin", null);

        assertThat(guardado).isNotNull();
        assertThat(guardado.getTotalElementos()).isEqualTo(2);
        assertThat(guardado.getTotalPuestos()).isEqualTo(1);
        assertThat(guardado.getHashContenido()).isNotBlank();
    }

    @Test
    @DisplayName("Rechaza vincular un puesto comercial perteneciente a otro mercado")
    void testRechazaPuestoDeOtroMercado() {
        PlanoMercadoCreateRequest req = PlanoMercadoCreateRequest.builder()
                .mercadoId(mercado1.getId())
                .codigo("PLN-EXT-" + System.currentTimeMillis())
                .nombre("Plano Validacion Aislamiento")
                .anchoBase(new BigDecimal("1400"))
                .altoBase(new BigDecimal("900"))
                .build();
        PlanoMercadoResponse plano = planoService.crearPlano(req, "admin", null);

        ElementoPlanoRequest elemPuestoExt = ElementoPlanoRequest.builder()
                .tipoElemento(TipoElementoPlano.PUESTO)
                .referenciaEntidadId(puestoOtroMercado.getId()) // Puesto de mercado 2 en plano de mercado 1!
                .codigoVisual(puestoOtroMercado.getCodigo())
                .coordenadaX(new BigDecimal("100"))
                .coordenadaY(new BigDecimal("100"))
                .ancho(new BigDecimal("60"))
                .alto(new BigDecimal("60"))
                .build();

        GuardarPlanoRequest saveReq = GuardarPlanoRequest.builder()
                .versionId(plano.getVersionActualId())
                .elementos(List.of(elemPuestoExt))
                .build();

        assertThatThrownBy(() -> editorService.guardarBorrador(plano.getId(), saveReq, "admin", null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("pertenece a otro mercado y no puede vincularse a este plano");
    }

    @Test
    @DisplayName("Rechaza duplicar el mismo puesto comercial en la misma versión")
    void testRechazaPuestoDuplicadoEnPlano() {
        PlanoMercadoCreateRequest req = PlanoMercadoCreateRequest.builder()
                .mercadoId(mercado1.getId())
                .codigo("PLN-DUP-PUESTO-" + System.currentTimeMillis())
                .nombre("Plano Validacion Duplicados")
                .anchoBase(new BigDecimal("1400"))
                .altoBase(new BigDecimal("900"))
                .build();
        PlanoMercadoResponse plano = planoService.crearPlano(req, "admin", null);

        ElementoPlanoRequest elem1 = ElementoPlanoRequest.builder()
                .tipoElemento(TipoElementoPlano.PUESTO)
                .referenciaEntidadId(puesto1.getId())
                .codigoVisual(puesto1.getCodigo())
                .coordenadaX(new BigDecimal("100"))
                .coordenadaY(new BigDecimal("100"))
                .ancho(new BigDecimal("60"))
                .alto(new BigDecimal("60"))
                .build();

        ElementoPlanoRequest elem2 = ElementoPlanoRequest.builder()
                .tipoElemento(TipoElementoPlano.PUESTO)
                .referenciaEntidadId(puesto1.getId()) // Mismo puesto repetido!
                .codigoVisual(puesto1.getCodigo())
                .coordenadaX(new BigDecimal("200"))
                .coordenadaY(new BigDecimal("100"))
                .ancho(new BigDecimal("60"))
                .alto(new BigDecimal("60"))
                .build();

        GuardarPlanoRequest saveReq = GuardarPlanoRequest.builder()
                .versionId(plano.getVersionActualId())
                .elementos(List.of(elem1, elem2))
                .build();

        assertThatThrownBy(() -> editorService.guardarBorrador(plano.getId(), saveReq, "admin", null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("aparece duplicado en la distribución");
    }

    @Test
    @DisplayName("Publicar versión oficial archiva automáticamente la versión publicada anterior")
    void testPublicacionOficialYArchivoHistorico() {
        PlanoMercadoCreateRequest req = PlanoMercadoCreateRequest.builder()
                .mercadoId(mercado1.getId())
                .codigo("PLN-PUB-" + System.currentTimeMillis())
                .nombre("Plano Ciclo Publicación")
                .anchoBase(new BigDecimal("1400"))
                .altoBase(new BigDecimal("900"))
                .build();
        PlanoMercadoResponse plano = planoService.crearPlano(req, "admin", null);

        // Guardar elementos en versión 1
        ElementoPlanoRequest elem = ElementoPlanoRequest.builder()
                .tipoElemento(TipoElementoPlano.PUESTO)
                .referenciaEntidadId(puesto1.getId())
                .codigoVisual(puesto1.getCodigo())
                .coordenadaX(new BigDecimal("100"))
                .coordenadaY(new BigDecimal("100"))
                .ancho(new BigDecimal("60"))
                .alto(new BigDecimal("60"))
                .build();

        editorService.guardarBorrador(plano.getId(), GuardarPlanoRequest.builder()
                .versionId(plano.getVersionActualId())
                .elementos(List.of(elem))
                .build(), "admin", null);

        // Publicar versión 1
        VersionPlanoResponse pubV1 = publicacionService.publicarVersion(
                plano.getId(), plano.getVersionActualId(),
                PublicarPlanoRequest.builder().observaciones("Primera publicación oficial").build(),
                "admin", null);

        assertThat(pubV1.getEstado()).isEqualTo(EstadoVersionPlano.PUBLICADA);

        // Crear versión 2
        VersionPlanoResponse v2 = versionService.crearNuevoBorrador(plano.getId(), "Borrador de ajustes v2", "admin", null);
        editorService.guardarBorrador(plano.getId(), GuardarPlanoRequest.builder()
                .versionId(v2.getId())
                .elementos(List.of(elem))
                .build(), "admin", null);

        // Publicar versión 2
        VersionPlanoResponse pubV2 = publicacionService.publicarVersion(
                plano.getId(), v2.getId(),
                PublicarPlanoRequest.builder().observaciones("Actualización oficial").build(),
                "admin", null);

        assertThat(pubV2.getEstado()).isEqualTo(EstadoVersionPlano.PUBLICADA);

        // Verificar que versión 1 ahora está ARCHIVADA
        VersionPlanoResponse estadoV1 = versionService.obtenerPorId(pubV1.getId());
        assertThat(estadoV1.getEstado()).isEqualTo(EstadoVersionPlano.ARCHIVADA);
    }

    @Test
    @DisplayName("Restaurar versión histórica como nuevo borrador sin modificar la original")
    void testRestaurarVersionComoNuevoBorrador() {
        PlanoMercadoCreateRequest req = PlanoMercadoCreateRequest.builder()
                .mercadoId(mercado1.getId())
                .codigo("PLN-RESTORE-" + System.currentTimeMillis())
                .nombre("Plano para Restaurar")
                .anchoBase(new BigDecimal("1400"))
                .altoBase(new BigDecimal("900"))
                .build();
        PlanoMercadoResponse plano = planoService.crearPlano(req, "admin", null);

        ElementoPlanoRequest elem = ElementoPlanoRequest.builder()
                .tipoElemento(TipoElementoPlano.PUESTO)
                .referenciaEntidadId(puesto1.getId())
                .codigoVisual(puesto1.getCodigo())
                .coordenadaX(new BigDecimal("100"))
                .coordenadaY(new BigDecimal("100"))
                .ancho(new BigDecimal("60"))
                .alto(new BigDecimal("60"))
                .build();

        editorService.guardarBorrador(plano.getId(), GuardarPlanoRequest.builder()
                .versionId(plano.getVersionActualId())
                .elementos(List.of(elem))
                .build(), "admin", null);

        VersionPlanoResponse pubV1 = publicacionService.publicarVersion(
                plano.getId(), plano.getVersionActualId(), null, "admin", null);

        // Restaurar v1 como nuevo borrador (debe generar v2)
        VersionPlanoResponse v2Restaurada = versionService.restaurarComoBorrador(
                plano.getId(), pubV1.getId(), "admin", null);

        assertThat(v2Restaurada.getNumeroVersion()).isEqualTo(2);
        assertThat(v2Restaurada.getEstado()).isEqualTo(EstadoVersionPlano.BORRADOR);
        assertThat(v2Restaurada.getTotalElementos()).isEqualTo(1);

        // La versión 1 original sigue siendo PUBLICADA (inmutable)
        VersionPlanoResponse v1Consulta = versionService.obtenerPorId(pubV1.getId());
        assertThat(v1Consulta.getEstado()).isEqualTo(EstadoVersionPlano.PUBLICADA);
    }

    @Test
    @DisplayName("Sincronización en tiempo real: Puesto cambia a OCUPADO al registrar asignación vigente")
    void testEstadoVisualCambiaAOcupadoConAsignacionVigente() {
        // Inicialmente el puesto está disponible
        EstadoVisualPuesto estadoInicial = estadoVisualService.calcularEstadoVisual(puesto1);
        assertThat(estadoInicial).isEqualTo(EstadoVisualPuesto.DISPONIBLE);

        // Crear comerciante y asignación VIGENTE
        String dniUnico = String.valueOf((int) (10000000 + (Math.random() * 89999999)));
        Persona persona = personaRepository.save(Persona.builder()
                .numeroDocumento(dniUnico)
                .nombres("Juan")
                .apellidoPaterno("Pérez")
                .apellidoMaterno("Quispe")
                .tipoDocumento("DNI")
                .build());

        Comerciante comerciante = comercianteRepository.save(Comerciante.builder()
                .codigoComerciante("COM-TEST-" + System.currentTimeMillis())
                .persona(persona)
                .tipoComerciante(TipoComerciante.PERSONA_NATURAL)
                .estado(EstadoComerciante.ACTIVO)
                .build());

        asignacionRepository.save(AsignacionPuesto.builder()
                .codigoAsignacion("ASIG-TEST-" + System.currentTimeMillis())
                .puesto(puesto1)
                .comerciante(comerciante)
                .fechaInicio(LocalDate.now().minusDays(10))
                .fechaFin(LocalDate.now().plusYears(1))
                .estado(EstadoAsignacion.VIGENTE)
                .build());

        // Al recalcular directamente desde PostgreSQL, el estado visual debe ser OCUPADO
        EstadoVisualPuesto estadoNuevo = estadoVisualService.calcularEstadoVisual(puesto1);
        assertThat(estadoNuevo).isEqualTo(EstadoVisualPuesto.OCUPADO);
    }
}
