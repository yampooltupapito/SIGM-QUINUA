package pe.gob.municipalidad.sigm.modules.serenazgo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.AtencionIncidenciaSerenazgoService;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.IncidenciaSeguridadCiudadanaService;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.IntervencionSerenazgoService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IntervencionSerenazgoServiceTest {

    private static final ZoneId ZONA_PERU = ZoneId.of("America/Lima");

    @Autowired
    private IntervencionSerenazgoService intervencionService;

    @Autowired
    private AtencionIncidenciaSerenazgoService atencionService;

    @Autowired
    private IncidenciaSeguridadCiudadanaService incidenciaService;

    @Autowired
    private IncidenciaSeguridadCiudadanaRepository incidenciaRepository;

    @Autowired
    private PersonalSerenazgoRepository personalRepository;

    @Autowired
    private UnidadMovilSerenazgoRepository unidadMovilRepository;

    @Autowired
    private EquipamientoSerenazgoRepository equipamientoRepository;

    @Autowired
    private SectorPatrullajeRepository sectorRepository;

    @Autowired
    private ZonaPatrullajeRepository zonaRepository;

    @Autowired
    private TipoIncidenciaSeguridadRepository tipoIncidenciaRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private SectorPatrullaje sectorCentral;
    private ZonaPatrullaje zonaPlaza;
    private TipoIncidenciaSeguridad tipoRiesgo;
    private IncidenciaSeguridadCiudadana incidencia;
    private AtencionDetailResponse atencionResponse;
    private PersonalSerenazgo sereno1;
    private PersonalSerenazgo sereno2;
    private UnidadMovilSerenazgo patrullero1;
    private EquipamientoSerenazgo radioVhf;
    private Usuario usuarioSupervisor;

    @BeforeEach
    void setUp() {
        long ts = System.currentTimeMillis();

        // 1. Tipo Incidencia
        tipoRiesgo = tipoIncidenciaRepository.findByCodigo("INC-INT-01")
                .orElseGet(() -> tipoIncidenciaRepository.save(TipoIncidenciaSeguridad.builder()
                        .codigo("INC-INT-01")
                        .nombre("Ruidos molestos y desorden vecinal")
                        .descripcion("Situación que requiere verificación y orientación ciudadana")
                        .categoria(CategoriaIncidenciaSeguridad.ORDEN_PUBLICO)
                        .prioridadSugerida(PrioridadIncidenciaSeguridad.MEDIA)
                        .activo(true)
                        .build()));

        // 2. Sector y Zona
        sectorCentral = sectorRepository.findByCodigo("SEC-INT-01")
                .orElseGet(() -> sectorRepository.save(SectorPatrullaje.builder()
                        .codigo("SEC-INT-01")
                        .nombre("Sector Centro Histórico")
                        .descripcion("Área central de Quinua")
                        .estado(EstadoSectorPatrullaje.ACTIVO)
                        .colorHex("#3B82F6")
                        .build()));

        zonaPlaza = zonaRepository.findBySectorIdAndCodigo(sectorCentral.getId(), "ZON-INT-01")
                .orElseGet(() -> zonaRepository.save(ZonaPatrullaje.builder()
                        .sector(sectorCentral)
                        .codigo("ZON-INT-01")
                        .nombre("Plaza de Armas y alrededores")
                        .descripcion("Perímetro de la plaza central")
                        .estado(EstadoSectorPatrullaje.ACTIVO)
                        .build()));

        // 3. Usuario Supervisor
        Persona pSup = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("47" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Supervisor")
                .apellidoPaterno("Quinua")
                .apellidoMaterno("Serenazgo")
                .correo("supervisor_int_" + ts + "@quinua.gob.pe")
                .estado("ACTIVO")
                .build());

        usuarioSupervisor = usuarioRepository.save(Usuario.builder()
                .persona(pSup)
                .username("sup_int_" + ts)
                .passwordHash("hash123")
                .enabled(true)
                .accountLocked(false)
                .build());

        // 4. Personal Serenos
        Persona pSereno1 = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("48" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Juan Carlos")
                .apellidoPaterno("Huamán")
                .apellidoMaterno("Quispe")
                .correo("sereno1_int_" + ts + "@quinua.gob.pe")
                .estado("ACTIVO")
                .build());

        sereno1 = personalRepository.save(PersonalSerenazgo.builder()
                .persona(pSereno1)
                .codigoInterno("SER-INT-001-" + (ts % 1000))
                .estado(EstadoPersonalSerenazgo.ACTIVO)
                .fechaIngreso(LocalDate.now().minusMonths(6))
                .build());

        Persona pSereno2 = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("49" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Edwin")
                .apellidoPaterno("Flores")
                .apellidoMaterno("Mendoza")
                .correo("sereno2_int_" + ts + "@quinua.gob.pe")
                .estado("ACTIVO")
                .build());

        sereno2 = personalRepository.save(PersonalSerenazgo.builder()
                .persona(pSereno2)
                .codigoInterno("SER-INT-002-" + (ts % 1000))
                .estado(EstadoPersonalSerenazgo.ACTIVO)
                .fechaIngreso(LocalDate.now().minusMonths(4))
                .build());

        // 5. Móvil y Equipamiento
        patrullero1 = unidadMovilRepository.save(UnidadMovilSerenazgo.builder()
                .codigoInterno("MOV-INT-" + (ts % 1000))
                .placa("EGA-" + String.format("%03d", (int)(ts % 1000)))
                .marca("Toyota")
                .modelo("Hilux 4x4")
                .tipoUnidad(TipoUnidadMovil.CAMIONETA)
                .estadoOperativo(EstadoUnidadMovil.DISPONIBLE)
                .kilometrajeActual(new BigDecimal("12500.5"))
                .build());

        radioVhf = equipamientoRepository.save(EquipamientoSerenazgo.builder()
                .tipoEquipo(TipoEquipamientoSerenazgo.RADIO_COMUNICACION)
                .descripcion("Radio Portátil Motorola VHF")
                .marca("Motorola")
                .modelo("DEP450")
                .codigoInterno("EQ-INT-" + (ts % 1000))
                .codigoPatrimonial("PAT-INT-" + (ts % 1000))
                .estado(EstadoEquipamientoSerenazgo.DISPONIBLE)
                .stockTotal(1)
                .stockDisponible(1)
                .esControladoPorCantidad(false)
                .build());

        // 6. Incidencia Base
        IncidenciaCreateRequest reqInc = IncidenciaCreateRequest.builder()
                .titulo("Griterío y disturbios en la plaza")
                .tipoIncidenciaId(tipoRiesgo.getId())
                .prioridad(PrioridadIncidenciaSeguridad.MEDIA)
                .descripcion("Vecinos reportan reunión ruidosa con parlantes.")
                .fechaHoraOcurrencia(OffsetDateTime.now(ZONA_PERU).minusHours(1))
                .origenHecho(OrigenHechoIncidencia.HECHO_REPORTADO)
                .canalReporte(CanalReporteIncidencia.LLAMADA_OPERADOR)
                .sectorId(sectorCentral.getId())
                .zonaId(zonaPlaza.getId())
                .ubicacionReferencia("Plaza Mayor de Quinua s/n")
                .nivelConfidencialidad(NivelConfidencialidad.ORDINARIO)
                .build();

        IncidenciaResponse incRes = incidenciaService.registrarIncidencia(reqInc, usuarioSupervisor.getUsername());
        incidencia = incidenciaRepository.findById(incRes.getId()).orElseThrow();

        // 7. Crear Atención para vincular
        AtencionCrearRequest reqAtn = new AtencionCrearRequest();
        reqAtn.setIncidenciaId(incidencia.getId());
        reqAtn.setPrioridadOperativa(PrioridadIncidenciaSeguridad.ALTA);
        reqAtn.setSectorId(sectorCentral.getId());
        reqAtn.setResponsablePersonalId(sereno1.getId());
        reqAtn.setPersonalIds(List.of(sereno2.getId()));
        reqAtn.setUnidadMovilId(patrullero1.getId());
        reqAtn.setEquipamientoIds(List.of(radioVhf.getId()));
        reqAtn.setDespacharInmediatamente(false);
        reqAtn.setObservaciones("Asignación de prueba.");

        atencionResponse = atencionService.crearAtencion(reqAtn, usuarioSupervisor.getUsername());
    }

    @Test
    @Order(1)
    @DisplayName("Debe crear una intervención en estado BORRADOR con correlativo INT-YYYY-00000")
    void testCrearIntervencion_Borrador_Exito() {
        LocalDateTime ahora = LocalDateTime.now();

        IntervencionCrearRequest request = new IntervencionCrearRequest();
        request.setIncidenciaId(incidencia.getId());
        request.setAtencionId(atencionResponse.getId());
        request.setTipoIntervencion(TipoIntervencionSerenazgo.PATRULLAJE_PREVENTIVO);
        request.setFechaInicio(ahora.minusMinutes(40));
        request.setFechaFin(ahora.minusMinutes(10));
        request.setResponsableId(sereno1.getId());
        request.setSectorId(sectorCentral.getId());
        request.setZonaId(zonaPlaza.getId());
        request.setUbicacionDescripcion("Plaza Principal de Quinua");
        request.setLatitud(-13.042500);
        request.setLongitud(-74.138900);
        request.setDescripcionActuacion("Se realizó patrullaje preventivo verificando la desconcentración pacífica.");
        request.setResultado(ResultadoIntervencionSerenazgo.ORIENTACION_REALIZADA);
        request.setNivelConfidencialidad(NivelConfidencialidad.PUBLICO);
        request.setGuardarComoBorrador(true);

        IntervencionDetailResponse response = intervencionService.crearIntervencion(request, usuarioSupervisor.getUsername());

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getCodigo()).matches("^INT-\\d{4}-\\d{5}$");
        assertThat(response.getEstado()).isEqualTo(EstadoIntervencionSerenazgo.BORRADOR);
        assertThat(response.getIncidenciaCodigo()).isEqualTo(incidencia.getCodigo());
        assertThat(response.getAtencionCodigo()).isEqualTo(atencionResponse.getCodigo());
        assertThat(response.getSectorNombre()).isEqualTo("Sector Centro Histórico");
    }

    @Test
    @Order(2)
    @DisplayName("Debe crear intervención directa en estado REGISTRADA sin atención previa si se autoriza")
    void testCrearIntervencion_RegistradaDirecta_Exito() {
        LocalDateTime ahora = LocalDateTime.now();

        IntervencionCrearRequest request = new IntervencionCrearRequest();
        request.setIncidenciaId(incidencia.getId());
        request.setAtencionId(null); // Sin atención previa
        request.setTipoIntervencion(TipoIntervencionSerenazgo.ORIENTACION_CIUDADANA);
        request.setFechaInicio(ahora.minusHours(2));
        request.setFechaFin(ahora.minusMinutes(90));
        request.setResponsableId(sereno1.getId());
        request.setSectorId(sectorCentral.getId());
        request.setZonaId(zonaPlaza.getId());
        request.setUbicacionDescripcion("Frontis de la Municipalidad Distrital de Quinua");
        request.setDescripcionActuacion("Orientación a visitantes sobre rutas turísticas y normas de convivencia.");
        request.setResultado(ResultadoIntervencionSerenazgo.ORIENTACION_REALIZADA);
        request.setGuardarComoBorrador(false);
        request.setRegistroRetrospectivo(true);
        request.setMotivoRegistroRetrospectivo("Actuación de apoyo turístico realizada durante rondas de a pie.");

        IntervencionDetailResponse response = intervencionService.crearIntervencion(request, usuarioSupervisor.getUsername());

        assertThat(response).isNotNull();
        assertThat(response.getEstado()).isEqualTo(EstadoIntervencionSerenazgo.REGISTRADA);
        assertThat(response.getRegistroRetrospectivo()).isTrue();
        assertThat(response.getMotivoRegistroRetrospectivo()).contains("apoyo turístico");
    }

    @Test
    @Order(3)
    @DisplayName("Debe fallar al crear intervención si la atención no pertenece a la incidencia indicada")
    void testCrearIntervencion_AtencionInvalida_LanzaExcepcion() {
        // Crear otra incidencia distinta
        IncidenciaCreateRequest reqInc2 = IncidenciaCreateRequest.builder()
                .titulo("Segunda incidencia para prueba cruzada")
                .tipoIncidenciaId(tipoRiesgo.getId())
                .prioridad(PrioridadIncidenciaSeguridad.BAJA)
                .descripcion("Segunda incidencia para prueba de validación cruzada")
                .fechaHoraOcurrencia(OffsetDateTime.now(ZONA_PERU).minusHours(1))
                .origenHecho(OrigenHechoIncidencia.HECHO_REPORTADO)
                .canalReporte(CanalReporteIncidencia.LLAMADA_OPERADOR)
                .sectorId(sectorCentral.getId())
                .ubicacionReferencia("Jr. Sucre cuadra 2")
                .nivelConfidencialidad(NivelConfidencialidad.ORDINARIO)
                .build();

        IncidenciaResponse inc2 = incidenciaService.registrarIncidencia(reqInc2, usuarioSupervisor.getUsername());

        IntervencionCrearRequest request = new IntervencionCrearRequest();
        request.setIncidenciaId(inc2.getId()); // Incidencia 2
        request.setAtencionId(atencionResponse.getId()); // Pero atención pertenece a incidencia 1
        request.setTipoIntervencion(TipoIntervencionSerenazgo.VERIFICACION_REPORTADA);
        request.setFechaInicio(LocalDateTime.now().minusMinutes(30));
        request.setResponsableId(sereno1.getId());
        request.setDescripcionActuacion("Verificación de prueba");
        request.setResultado(ResultadoIntervencionSerenazgo.ACTUACION_REGISTRADA);

        assertThatThrownBy(() -> intervencionService.crearIntervencion(request, usuarioSupervisor.getUsername()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no corresponde a la incidencia");
    }

    @Test
    @Order(4)
    @DisplayName("Debe gestionar participantes (agregar y remover) conservando trazabilidad")
    void testGestionParticipantes_Exito() {
        LocalDateTime ahora = LocalDateTime.now();

        IntervencionCrearRequest request = new IntervencionCrearRequest();
        request.setIncidenciaId(incidencia.getId());
        request.setTipoIntervencion(TipoIntervencionSerenazgo.APOYO_MUNICIPAL);
        request.setFechaInicio(ahora.minusMinutes(30));
        request.setResponsableId(sereno1.getId());
        request.setDescripcionActuacion("Intervención para pruebas de personal participante");
        request.setResultado(ResultadoIntervencionSerenazgo.ACTUACION_REGISTRADA);
        request.setGuardarComoBorrador(true);

        IntervencionDetailResponse interv = intervencionService.crearIntervencion(request, usuarioSupervisor.getUsername());

        // 1. Agregar sereno 2 como Apoyo
        IntervencionParticipanteRequest part2 = new IntervencionParticipanteRequest();
        part2.setPersonalId(sereno2.getId());
        part2.setFuncionDesempenada("Apoyo en control perimétrico disuasivo");

        IntervencionDetailResponse r1 = intervencionService.agregarParticipante(interv.getId(), part2, usuarioSupervisor.getUsername());
        assertThat(r1.getParticipantes()).isNotEmpty();

        // 2. Remover sereno 2 (queda sereno 1 como responsable designado)
        IntervencionDetailResponse r2 = intervencionService.removerParticipante(interv.getId(), sereno2.getId(), usuarioSupervisor.getUsername());
        assertThat(r2.getParticipantes()).hasSize(1);
        assertThat(r2.getParticipantes().get(0).getPersonalId()).isEqualTo(sereno1.getId());
    }

    @Test
    @Order(5)
    @DisplayName("Debe registrar actuaciones cronológicas objetivas")
    void testRegistrarActuaciones_Exito() {
        LocalDateTime ahora = LocalDateTime.now();

        IntervencionCrearRequest request = new IntervencionCrearRequest();
        request.setIncidenciaId(incidencia.getId());
        request.setTipoIntervencion(TipoIntervencionSerenazgo.PATRULLAJE_PREVENTIVO);
        request.setFechaInicio(ahora.minusMinutes(45));
        request.setResponsableId(sereno1.getId());
        request.setDescripcionActuacion("Intervención para registro de secuencia de actuaciones");
        request.setResultado(ResultadoIntervencionSerenazgo.ACTUACION_REGISTRADA);

        IntervencionDetailResponse interv = intervencionService.crearIntervencion(request, usuarioSupervisor.getUsername());

        // Actuación 1: Verificación
        IntervencionActuacionRequest act1 = new IntervencionActuacionRequest();
        act1.setTipoActuacion(TipoActuacionIntervencion.OBSERVACION_PREVENTIVA);
        act1.setFechaHora(ahora.minusMinutes(40));
        act1.setDescripcion("Llegada al lugar. Se constata aglomeración de personas con altoparlantes.");
        act1.setPersonalResponsableId(sereno1.getId());
        act1.setObservaciones("Situación tranquila, sin incidentes de violencia física.");

        IntervencionDetailResponse respAct1 = intervencionService.registrarActuacion(interv.getId(), act1, usuarioSupervisor.getUsername());
        assertThat(respAct1.getActuaciones()).hasSize(1);

        // Actuación 2: Diálogo
        IntervencionActuacionRequest act2 = new IntervencionActuacionRequest();
        act2.setTipoActuacion(TipoActuacionIntervencion.DIALOGO_VECINAL);
        act2.setFechaHora(ahora.minusMinutes(25));
        act2.setDescripcion("Se dialoga con el responsable exhortando al cese de emisión de ruidos nocivos.");
        act2.setPersonalResponsableId(sereno1.getId());

        IntervencionDetailResponse respAct2 = intervencionService.registrarActuacion(interv.getId(), act2, usuarioSupervisor.getUsername());
        assertThat(respAct2.getActuaciones()).hasSize(2);
        assertThat(respAct2.getActuaciones().get(0).getTipoActuacion()).isEqualTo(TipoActuacionIntervencion.OBSERVACION_PREVENTIVA);
        assertThat(respAct2.getActuaciones().get(1).getTipoActuacion()).isEqualTo(TipoActuacionIntervencion.DIALOGO_VECINAL);
    }

    @Test
    @Order(6)
    @DisplayName("Debe registrar y actualizar comunicaciones institucionales con la PNP u otras dependencias")
    void testRegistrarComunicacionesInstitucionales_Exito() {
        LocalDateTime ahora = LocalDateTime.now();

        IntervencionCrearRequest request = new IntervencionCrearRequest();
        request.setIncidenciaId(incidencia.getId());
        request.setTipoIntervencion(TipoIntervencionSerenazgo.COMUNICACION_AUTORIDAD);
        request.setFechaInicio(ahora.minusMinutes(50));
        request.setResponsableId(sereno1.getId());
        request.setDescripcionActuacion("Intervención con comunicación formal a la Comisaría de Quinua.");
        request.setResultado(ResultadoIntervencionSerenazgo.COMUNICACION_INSTITUCIONAL);

        IntervencionDetailResponse interv = intervencionService.crearIntervencion(request, usuarioSupervisor.getUsername());

        // Registrar comunicación
        IntervencionComunicacionRequest comReq = new IntervencionComunicacionRequest();
        comReq.setEntidadDestinataria(EntidadComunicacion.POLICIA_NACIONAL_PERU);
        comReq.setCanal(CanalComunicacionDespacho.RADIO_TETRA_VHF);
        comReq.setMotivo("Poner en conocimiento presunto consumo de bebidas alcohólicas en vía pública");
        comReq.setFechaComunicacion(ahora.minusMinutes(35));
        comReq.setPersonalComunicadorId(sereno1.getId());
        comReq.setNumeroReferencia("OFI-2026-SCQ-044");

        IntervencionDetailResponse respCom = intervencionService.registrarComunicacion(interv.getId(), comReq, usuarioSupervisor.getUsername());
        assertThat(respCom.getComunicaciones()).hasSize(1);
        UUID comId = respCom.getComunicaciones().get(0).getId();
        assertThat(respCom.getComunicaciones().get(0).getEstadoComunicacion()).isEqualTo(EstadoComunicacionIntervencion.ENVIADA);

        // Actualizar a confirmada
        IntervencionDetailResponse respConf = intervencionService.actualizarEstadoComunicacion(
                comId,
                EstadoComunicacionIntervencion.RECEPCION_CONFIRMADA,
                "SO3 PNP Quispe - Radio Operador Comisaría",
                "Patrullero policial tomó conocimiento para patrullaje integrado",
                usuarioSupervisor.getUsername());

        assertThat(respConf.getComunicaciones().get(0).getEstadoComunicacion()).isEqualTo(EstadoComunicacionIntervencion.RECEPCION_CONFIRMADA);
        assertThat(respConf.getComunicaciones().get(0).getConfirmadoPor()).contains("SO3 PNP Quispe");
    }

    @Test
    @Order(7)
    @DisplayName("Debe asociar equipamiento utilizado a la intervención")
    void testAsociarEquipamiento_Exito() {
        LocalDateTime ahora = LocalDateTime.now();

        IntervencionCrearRequest request = new IntervencionCrearRequest();
        request.setIncidenciaId(incidencia.getId());
        request.setTipoIntervencion(TipoIntervencionSerenazgo.PATRULLAJE_PREVENTIVO);
        request.setFechaInicio(ahora.minusMinutes(30));
        request.setResponsableId(sereno1.getId());
        request.setDescripcionActuacion("Intervención con equipamiento de comunicación");
        request.setResultado(ResultadoIntervencionSerenazgo.ACTUACION_REGISTRADA);

        IntervencionDetailResponse interv = intervencionService.crearIntervencion(request, usuarioSupervisor.getUsername());

        IntervencionDetailResponse respEq = intervencionService.asociarEquipamiento(
                interv.getId(),
                radioVhf.getId(),
                1,
                "BUENO",
                "Utilizado durante toda la intervención sin novedades",
                usuarioSupervisor.getUsername());

        assertThat(respEq.getEquipamiento()).hasSize(1);
        assertThat(respEq.getEquipamiento().get(0).getEquipamientoCodigo()).contains("PAT-INT-");
        assertThat(respEq.getEquipamiento().get(0).getEquipamientoNombre()).contains("Motorola");
    }

    @Test
    @Order(8)
    @DisplayName("Debe cumplir el ciclo de vida completo: BORRADOR -> REGISTRADA -> EN_REVISION -> OBSERVADA -> SUBSANADA -> VALIDADA -> ARCHIVADA")
    void testCicloDeVida_Completo_Exito() {
        LocalDateTime ahora = LocalDateTime.now();

        // 1. Crear como BORRADOR
        IntervencionCrearRequest req = new IntervencionCrearRequest();
        req.setIncidenciaId(incidencia.getId());
        req.setTipoIntervencion(TipoIntervencionSerenazgo.PATRULLAJE_PREVENTIVO);
        req.setFechaInicio(ahora.minusHours(3));
        req.setFechaFin(ahora.minusHours(2));
        req.setResponsableId(sereno1.getId());
        req.setDescripcionActuacion("Intervención preventiva con ciclo de vida completo para pruebas");
        req.setResultado(ResultadoIntervencionSerenazgo.ORIENTACION_REALIZADA);
        req.setGuardarComoBorrador(true);

        IntervencionDetailResponse rBorrador = intervencionService.crearIntervencion(req, usuarioSupervisor.getUsername());
        assertThat(rBorrador.getEstado()).isEqualTo(EstadoIntervencionSerenazgo.BORRADOR);

        // 2. Agregar participante
        IntervencionParticipanteRequest part = new IntervencionParticipanteRequest();
        part.setPersonalId(sereno2.getId());
        part.setFuncionDesempenada("Apoyo operativo en ronda");
        intervencionService.agregarParticipante(rBorrador.getId(), part, usuarioSupervisor.getUsername());

        // 3. Enviar a Revisión
        IntervencionDetailResponse rRev = intervencionService.enviarARevision(rBorrador.getId(), usuarioSupervisor.getUsername());
        assertThat(rRev.getEstado()).isEqualTo(EstadoIntervencionSerenazgo.EN_REVISION);

        // 4. Observar
        IntervencionObservarRequest obsReq = new IntervencionObservarRequest();
        obsReq.setMotivo("Falta detallar la dirección exacta y especificar si hubo retiro voluntario");
        IntervencionDetailResponse rObs = intervencionService.observarIntervencion(rBorrador.getId(), obsReq, usuarioSupervisor.getUsername());
        assertThat(rObs.getEstado()).isEqualTo(EstadoIntervencionSerenazgo.OBSERVADA);
        assertThat(rObs.getMotivoObservacion()).contains("Falta detallar");

        // 5. Subsanar
        IntervencionDetailResponse rSub = intervencionService.subsanarIntervencion(
                rBorrador.getId(),
                "Se subsanó adjuntando la ubicación precisa y confirmando retiro voluntario sin incidentes",
                usuarioSupervisor.getUsername());
        assertThat(rSub.getEstado()).isEqualTo(EstadoIntervencionSerenazgo.EN_REVISION);

        // 6. Validar Administrativamente
        IntervencionValidarRequest valReq = new IntervencionValidarRequest();
        valReq.setObservaciones("Revisado conforme a directiva de Serenazgo municipal de Quinua");
        IntervencionDetailResponse rVal = intervencionService.validarIntervencion(rBorrador.getId(), valReq, usuarioSupervisor.getUsername());
        assertThat(rVal.getEstado()).isEqualTo(EstadoIntervencionSerenazgo.VALIDADA);
        assertThat(rVal.getFechaValidacion()).isNotNull();

        // 7. No se debe poder editar una intervención ya validada
        IntervencionEditarRequest editReq = new IntervencionEditarRequest();
        editReq.setTipoIntervencion(TipoIntervencionSerenazgo.OTRA_ACTUACION);
        editReq.setFechaInicio(ahora.minusHours(3));
        editReq.setResponsableId(sereno1.getId());
        editReq.setDescripcionActuacion("Intento de modificación en estado VALIDADA");
        editReq.setResultado(ResultadoIntervencionSerenazgo.ACTUACION_REGISTRADA);
        editReq.setNivelConfidencialidad(NivelConfidencialidad.PUBLICO);

        assertThatThrownBy(() -> intervencionService.actualizarIntervencion(rBorrador.getId(), editReq, usuarioSupervisor.getUsername()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Solo se pueden editar intervenciones");

        // 8. Archivar
        IntervencionArchivarRequest arcReq = new IntervencionArchivarRequest();
        arcReq.setMotivo("Archivo administrativo regular de actuaciones concluidas del mes");
        IntervencionDetailResponse rArc = intervencionService.archivarIntervencion(rBorrador.getId(), arcReq, usuarioSupervisor.getUsername());
        assertThat(rArc.getEstado()).isEqualTo(EstadoIntervencionSerenazgo.ARCHIVADA);
        assertThat(rArc.getMotivoArchivo()).contains("Archivo administrativo regular");
    }

    @Test
    @Order(9)
    @DisplayName("Debe listar intervenciones con filtros y paginación")
    void testListarIntervenciones_ConFiltros_Exito() {
        IntervencionCrearRequest req = new IntervencionCrearRequest();
        req.setIncidenciaId(incidencia.getId());
        req.setTipoIntervencion(TipoIntervencionSerenazgo.PATRULLAJE_PREVENTIVO);
        req.setFechaInicio(LocalDateTime.now().minusMinutes(15));
        req.setResponsableId(sereno1.getId());
        req.setSectorId(sectorCentral.getId());
        req.setDescripcionActuacion("Intervención para verificación de listado con filtros");
        req.setResultado(ResultadoIntervencionSerenazgo.ACTUACION_REGISTRADA);
        intervencionService.crearIntervencion(req, usuarioSupervisor.getUsername());

        IntervencionFilter filtro = new IntervencionFilter();
        filtro.setSectorId(sectorCentral.getId());

        Page<IntervencionResponse> resultado = intervencionService.listarIntervenciones(filtro, PageRequest.of(0, 10));

        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).isNotEmpty();
    }

    @Test
    @Order(10)
    @DisplayName("Debe exportar reporte de intervenciones a PDF y Excel con contenido válido")
    void testExportacionPdfYExcel_Exito() {
        IntervencionFilter filtro = new IntervencionFilter();

        byte[] pdfBytes = intervencionService.exportarPdf(filtro);
        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(100);

        byte[] xlsxBytes = intervencionService.exportarExcel(filtro);
        assertThat(xlsxBytes).isNotNull();
        assertThat(xlsxBytes.length).isGreaterThan(100);
    }

    @Test
    @Order(11)
    @DisplayName("Debe generar la Ficha Administrativa de Intervención en PDF con el descargo legal correspondiente")
    void testGenerarFichaPdf_Exito() {
        LocalDateTime ahora = LocalDateTime.now();

        IntervencionCrearRequest req = new IntervencionCrearRequest();
        req.setIncidenciaId(incidencia.getId());
        req.setTipoIntervencion(TipoIntervencionSerenazgo.ORIENTACION_CIUDADANA);
        req.setFechaInicio(ahora.minusMinutes(20));
        req.setResponsableId(sereno1.getId());
        req.setDescripcionActuacion("Intervención para prueba de generación de Ficha PDF individual.");
        req.setResultado(ResultadoIntervencionSerenazgo.ORIENTACION_REALIZADA);

        IntervencionDetailResponse interv = intervencionService.crearIntervencion(req, usuarioSupervisor.getUsername());

        byte[] fichaPdf = intervencionService.generarFichaPdf(interv.getId());
        assertThat(fichaPdf).isNotNull();
        assertThat(fichaPdf.length).isGreaterThan(500);
    }
}
