package pe.gob.municipalidad.sigm.modules.mercados;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.administracion.service.AuditoriaAdministrativaService;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.*;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;
import pe.gob.municipalidad.sigm.modules.mercados.service.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MercadoInspeccionServiceTest {

    @Mock private InspeccionMercadoRepository inspeccionRepository;
    @Mock private InspeccionParticipanteRepository participanteRepository;
    @Mock private MercadoMunicipalRepository mercadoRepository;
    @Mock private SectorMercadoRepository sectorRepository;
    @Mock private PabellonMercadoRepository pabellonRepository;
    @Mock private PuestoComercialRepository puestoRepository;
    @Mock private ComercianteRepository comercianteRepository;
    @Mock private PlantillaInspeccionRepository plantillaRepository;
    @Mock private ItemVerificacionPlantillaRepository itemRepository;
    @Mock private RespuestaVerificacionInspeccionRepository respuestaRepository;
    @Mock private HallazgoInspeccionRepository hallazgoRepository;
    @Mock private EvidenciaInspeccionMercadoRepository evidenciaRepository;
    @Mock private MedidaCorrectivaMercadoRepository medidaRepository;
    @Mock private SubsanacionMedidaMercadoRepository subsanacionRepository;
    @Mock private OficinaRepository oficinaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private AuditoriaAdministrativaService auditoriaService;

    private InspeccionMercadoService inspeccionService;
    private VerificacionYHallazgoService verificacionService;
    private MedidaCorrectivaService medidaService;

    private MercadoMunicipal mercado1;
    private MercadoMunicipal mercado2;
    private SectorMercado sector1;
    private PuestoComercial puesto1;
    private Usuario inspectorUser;

    @BeforeEach
    void setUp() {
        inspeccionService = new InspeccionMercadoService(
                inspeccionRepository, participanteRepository, mercadoRepository,
                sectorRepository, pabellonRepository, puestoRepository,
                comercianteRepository, plantillaRepository, oficinaRepository,
                usuarioRepository, auditoriaService);

        verificacionService = new VerificacionYHallazgoService(
                inspeccionRepository, itemRepository, respuestaRepository,
                hallazgoRepository, evidenciaRepository, usuarioRepository,
                auditoriaService);

        medidaService = new MedidaCorrectivaService(
                medidaRepository, hallazgoRepository, subsanacionRepository,
                comercianteRepository, usuarioRepository, auditoriaService);

        mercado1 = MercadoMunicipal.builder()
                .id(UUID.randomUUID())
                .codigo("MER-01")
                .nombre("Mercado Central de Quinua")
                .direccion("Plaza de Armas S/N")
                .estado(EstadoMercado.ACTIVO)
                .build();

        mercado2 = MercadoMunicipal.builder()
                .id(UUID.randomUUID())
                .codigo("MER-02")
                .nombre("Mercado Artesanal")
                .direccion("Av. Centenario 123")
                .estado(EstadoMercado.ACTIVO)
                .build();

        sector1 = SectorMercado.builder()
                .id(UUID.randomUUID())
                .codigo("SEC-A")
                .nombre("Sector Carnes")
                .mercado(mercado1)
                .build();

        puesto1 = PuestoComercial.builder()
                .id(UUID.randomUUID())
                .codigo("PUE-01")
                .numero("01")
                .mercado(mercado1)
                .build();

        inspectorUser = Usuario.builder()
                .id(10L)
                .username("inspector.mercados")
                .persona(Persona.builder().nombres("Carlos").apellidoPaterno("Mendoza").build())
                .build();
    }

    @Test
    @DisplayName("01. Crear inspección programada válida con correlativo institucional")
    void test01_crearInspeccion_valida() {
        InspeccionCreateRequest request = InspeccionCreateRequest.builder()
                .tipoInspeccion(TipoInspeccionMercado.PROGRAMADA)
                .mercadoId(mercado1.getId())
                .sectorId(sector1.getId())
                .puestoId(puesto1.getId())
                .fechaProgramada(LocalDate.now().plusDays(3))
                .horaProgramada(LocalTime.of(9, 30))
                .motivo("Inspección periódica de control sanitario")
                .objetivo("Constatar condiciones de salubridad y carné sanitario")
                .inspectorResponsableId(inspectorUser.getId())
                .build();

        when(mercadoRepository.findById(mercado1.getId())).thenReturn(Optional.of(mercado1));
        when(sectorRepository.findById(sector1.getId())).thenReturn(Optional.of(sector1));
        when(puestoRepository.findById(puesto1.getId())).thenReturn(Optional.of(puesto1));
        when(usuarioRepository.findById(inspectorUser.getId())).thenReturn(Optional.of(inspectorUser));
        when(inspeccionRepository.obtenerSiguienteCorrelativo()).thenReturn(1L);

        when(inspeccionRepository.save(any(InspeccionMercado.class))).thenAnswer(invocation -> {
            InspeccionMercado i = invocation.getArgument(0);
            i.setId(UUID.randomUUID());
            return i;
        });

        InspeccionResponse response = inspeccionService.crear(request, "admin");

        assertThat(response).isNotNull();
        assertThat(response.getCodigoInspeccion()).startsWith("INS-MER-");
        assertThat(response.getEstado()).isEqualTo(EstadoInspeccion.ASIGNADA);
        assertThat(response.getMercadoNombre()).isEqualTo("Mercado Central de Quinua");
        verify(auditoriaService).registrar(eq("CREAR_INSPECCION_MERCADO"), any(), any(), eq("EXITO"), any());
    }

    @Test
    @DisplayName("02. Rechazar inspección cuando el sector no pertenece al mercado indicado")
    void test02_rechazarSectorIncompatibleConMercado() {
        // Sector pertenece a mercado 1, pero se indica mercado 2
        InspeccionCreateRequest request = InspeccionCreateRequest.builder()
                .tipoInspeccion(TipoInspeccionMercado.PROGRAMADA)
                .mercadoId(mercado2.getId())
                .sectorId(sector1.getId())
                .fechaProgramada(LocalDate.now().plusDays(2))
                .motivo("Prueba de validación")
                .objetivo("Revisión")
                .build();

        when(mercadoRepository.findById(mercado2.getId())).thenReturn(Optional.of(mercado2));
        when(sectorRepository.findById(sector1.getId())).thenReturn(Optional.of(sector1));

        assertThatThrownBy(() -> inspeccionService.crear(request, "admin"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no pertenece al mercado");
    }

    @Test
    @DisplayName("03. Iniciar inspección in situ transiciona estado a EN_PROCESO y fecha real")
    void test03_iniciarInspeccion_valida() {
        UUID id = UUID.randomUUID();
        InspeccionMercado inspeccion = InspeccionMercado.builder()
                .id(id)
                .codigoInspeccion("INS-MER-2026-000001")
                .tipoInspeccion(TipoInspeccionMercado.PROGRAMADA)
                .mercado(mercado1)
                .fechaProgramada(LocalDate.now())
                .estado(EstadoInspeccion.ASIGNADA)
                .motivo("Supervisión ordinaria")
                .objetivo("Revisión de puestos")
                .build();

        when(inspeccionRepository.findById(id)).thenReturn(Optional.of(inspeccion));
        when(inspeccionRepository.save(any(InspeccionMercado.class))).thenAnswer(i -> i.getArgument(0));

        InspeccionResponse response = inspeccionService.iniciarInspeccion(id, null, "inspector");

        assertThat(response.getEstado()).isEqualTo(EstadoInspeccion.EN_PROCESO);
        assertThat(inspeccion.getFechaInicioReal()).isNotNull();
        verify(auditoriaService).registrar(eq("INICIAR_INSPECCION_MERCADO"), any(), any(), eq("EXITO"), any());
    }

    @Test
    @DisplayName("04. Rechazar inicio de inspección cancelada o finalizada")
    void test04_rechazarInicioInspeccionCancelada() {
        UUID id = UUID.randomUUID();
        InspeccionMercado cancelada = InspeccionMercado.builder()
                .id(id)
                .codigoInspeccion("INS-MER-2026-000002")
                .estado(EstadoInspeccion.CANCELADA)
                .build();

        when(inspeccionRepository.findById(id)).thenReturn(Optional.of(cancelada));

        assertThatThrownBy(() -> inspeccionService.iniciarInspeccion(id, null, "inspector"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("No se puede iniciar");
    }

    @Test
    @DisplayName("05. Registrar respuestas a lista de verificación")
    void test05_registrarVerificaciones() {
        UUID id = UUID.randomUUID();
        InspeccionMercado inspeccion = InspeccionMercado.builder()
                .id(id)
                .codigoInspeccion("INS-MER-2026-000001")
                .estado(EstadoInspeccion.EN_PROCESO)
                .build();

        ItemVerificacionPlantilla item1 = ItemVerificacionPlantilla.builder()
                .id(UUID.randomUUID())
                .codigo("ITEM-01")
                .categoria("SALUBRIDAD")
                .descripcion("Uso de mandil")
                .build();

        when(inspeccionRepository.findById(id)).thenReturn(Optional.of(inspeccion));
        when(usuarioRepository.findById(inspectorUser.getId())).thenReturn(Optional.of(inspectorUser));
        when(itemRepository.findById(item1.getId())).thenReturn(Optional.of(item1));
        when(respuestaRepository.findByInspeccionIdAndItemVerificacionId(id, item1.getId())).thenReturn(Optional.empty());

        when(respuestaRepository.save(any(RespuestaVerificacionInspeccion.class))).thenAnswer(i -> {
            RespuestaVerificacionInspeccion r = i.getArgument(0);
            r.setId(UUID.randomUUID());
            r.setFechaRegistro(OffsetDateTime.now());
            return r;
        });

        VerificacionItemRequest reqItem = VerificacionItemRequest.builder()
                .itemVerificacionId(item1.getId())
                .resultado(ResultadoVerificacion.CONFORME)
                .observaciones("Indumentaria completa")
                .build();

        List<RespuestaVerificacionDto> dtos = verificacionService.registrarVerificaciones(id, List.of(reqItem), "inspector", inspectorUser.getId());

        assertThat(dtos).hasSize(1);
        assertThat(dtos.get(0).getResultado()).isEqualTo(ResultadoVerificacion.CONFORME);
    }

    @Test
    @DisplayName("06. Registrar hallazgo técnico con gravedad técnica")
    void test06_registrarHallazgoTecnico() {
        UUID id = UUID.randomUUID();
        InspeccionMercado inspeccion = InspeccionMercado.builder()
                .id(id)
                .codigoInspeccion("INS-MER-2026-000001")
                .estado(EstadoInspeccion.EN_PROCESO)
                .hallazgos(new ArrayList<>())
                .build();

        when(inspeccionRepository.findById(id)).thenReturn(Optional.of(inspeccion));
        when(usuarioRepository.findById(inspectorUser.getId())).thenReturn(Optional.of(inspectorUser));

        when(hallazgoRepository.save(any(HallazgoInspeccionMercado.class))).thenAnswer(i -> {
            HallazgoInspeccionMercado h = i.getArgument(0);
            h.setId(UUID.randomUUID());
            h.setFechaRegistro(OffsetDateTime.now());
            return h;
        });

        HallazgoCreateRequest req = HallazgoCreateRequest.builder()
                .categoria("SALUBRIDAD")
                .descripcion("Carné sanitario vencido de la ayudante del puesto")
                .gravedadTecnica(GravedadHallazgo.MODERADA)
                .build();

        HallazgoResponse response = verificacionService.registrarHallazgo(id, req, "inspector", inspectorUser.getId());

        assertThat(response).isNotNull();
        assertThat(response.getCodigo()).isEqualTo("HAL-001");
        assertThat(response.getGravedadTecnica()).isEqualTo(GravedadHallazgo.MODERADA);
        assertThat(response.getEstado()).isEqualTo("ABIERTO");
    }

    @Test
    @DisplayName("07. Registrar medida correctiva para un hallazgo y validar plazo")
    void test07_registrarMedidaCorrectiva() {
        UUID hallazgoId = UUID.randomUUID();
        HallazgoInspeccionMercado hallazgo = HallazgoInspeccionMercado.builder()
                .id(hallazgoId)
                .codigo("HAL-001")
                .estado("ABIERTO")
                .inspeccion(InspeccionMercado.builder().id(UUID.randomUUID()).codigoInspeccion("INS-001").build())
                .build();

        when(hallazgoRepository.findById(hallazgoId)).thenReturn(Optional.of(hallazgo));
        when(medidaRepository.save(any(MedidaCorrectivaMercado.class))).thenAnswer(i -> {
            MedidaCorrectivaMercado m = i.getArgument(0);
            m.setId(UUID.randomUUID());
            m.setCreatedAt(OffsetDateTime.now());
            return m;
        });

        MedidaCorrectivaRequest req = MedidaCorrectivaRequest.builder()
                .descripcion("Tramitar y presentar carné sanitario actualizado")
                .tipoResponsable("COMERCIANTE_TITULAR")
                .fechaAsignacion(LocalDate.now())
                .fechaLimite(LocalDate.now().plusDays(5))
                .observaciones("Plazo improrrogable")
                .build();

        MedidaCorrectivaResponse response = medidaService.crearMedida(hallazgoId, req, "inspector");

        assertThat(response).isNotNull();
        assertThat(response.getEstado()).isEqualTo(EstadoMedidaCorrectiva.PENDIENTE);
        assertThat(hallazgo.getEstado()).isEqualTo("CON_MEDIDA_CORRECTIVA");
    }

    @Test
    @DisplayName("08. Rechazar medida correctiva con fecha límite anterior a fecha de asignación")
    void test08_rechazarFechaLimiteInvalida() {
        UUID hallazgoId = UUID.randomUUID();
        HallazgoInspeccionMercado hallazgo = HallazgoInspeccionMercado.builder()
                .id(hallazgoId)
                .codigo("HAL-001")
                .build();

        when(hallazgoRepository.findById(hallazgoId)).thenReturn(Optional.of(hallazgo));

        MedidaCorrectivaRequest req = MedidaCorrectivaRequest.builder()
                .descripcion("Corregir cables eléctricos")
                .fechaAsignacion(LocalDate.now())
                .fechaLimite(LocalDate.now().minusDays(1)) // Invalido
                .build();

        assertThatThrownBy(() -> medidaService.crearMedida(hallazgoId, req, "inspector"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("fecha límite no puede ser anterior");
    }

    @Test
    @DisplayName("09. Presentar subsanación y verificarla con aprobación conforme")
    void test09_presentarYVerificarSubsanacionConforme() {
        UUID medidaId = UUID.randomUUID();
        UUID subsanacionId = UUID.randomUUID();

        HallazgoInspeccionMercado hallazgo = HallazgoInspeccionMercado.builder()
                .id(UUID.randomUUID())
                .codigo("HAL-001")
                .estado("CON_MEDIDA_CORRECTIVA")
                .medidasCorrectivas(new ArrayList<>())
                .inspeccion(InspeccionMercado.builder().id(UUID.randomUUID()).codigoInspeccion("INS-001").build())
                .build();

        MedidaCorrectivaMercado medida = MedidaCorrectivaMercado.builder()
                .id(medidaId)
                .hallazgo(hallazgo)
                .descripcion("Reemplazo de extintor")
                .fechaAsignacion(LocalDate.now())
                .fechaLimite(LocalDate.now().plusDays(10))
                .estado(EstadoMedidaCorrectiva.PENDIENTE)
                .subsanaciones(new ArrayList<>())
                .build();

        hallazgo.getMedidasCorrectivas().add(medida);

        when(medidaRepository.findById(medidaId)).thenReturn(Optional.of(medida));
        when(usuarioRepository.findById(inspectorUser.getId())).thenReturn(Optional.of(inspectorUser));
        when(medidaRepository.save(any(MedidaCorrectivaMercado.class))).thenAnswer(i -> i.getArgument(0));

        // 1. Presentar subsanación
        SubsanacionRequest subReq = SubsanacionRequest.builder()
                .descripcionSubsanacion("Se compró extintor nuevo PQS 6kg con tarjeta de inspección")
                .build();

        when(subsanacionRepository.save(any(SubsanacionMedidaMercado.class))).thenAnswer(i -> {
            SubsanacionMedidaMercado s = i.getArgument(0);
            s.setId(subsanacionId);
            return s;
        });

        MedidaCorrectivaResponse presResp = medidaService.presentarSubsanacion(medidaId, subReq, "comerciante", inspectorUser.getId());
        assertThat(presResp.getEstado()).isEqualTo(EstadoMedidaCorrectiva.PRESENTADA_PARA_VERIFICACION);

        // 2. Verificar subsanación por inspector
        SubsanacionMedidaMercado subsanacionGuardada = SubsanacionMedidaMercado.builder()
                .id(subsanacionId)
                .medidaCorrectiva(medida)
                .estadoVerificacion("PENDIENTE_REVISION")
                .build();

        when(subsanacionRepository.findById(subsanacionId)).thenReturn(Optional.of(subsanacionGuardada));

        VerificacionSubsanacionRequest verifReq = VerificacionSubsanacionRequest.builder()
                .aprobada(true)
                .observaciones("Extintor instalado y verificado físicamente")
                .build();

        MedidaCorrectivaResponse verifResp = medidaService.verificarSubsanacion(subsanacionId, verifReq, "inspector", inspectorUser.getId());

        assertThat(verifResp.getEstado()).isEqualTo(EstadoMedidaCorrectiva.SUBSANADA);
        assertThat(hallazgo.getEstado()).isEqualTo("SUBSANADO");
    }
}
