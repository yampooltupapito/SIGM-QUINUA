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

import java.math.BigDecimal;
import java.time.LocalDate;
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
public class AtencionIncidenciaServiceTest {

    private static final ZoneId ZONA_PERU = ZoneId.of("America/Lima");

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
    private PersonalSerenazgo sereno1;
    private PersonalSerenazgo sereno2;
    private UnidadMovilSerenazgo patrullero1;
    private EquipamientoSerenazgo radioVhf;
    private Usuario usuarioSupervisor;

    @BeforeEach
    void setUp() {
        long ts = System.currentTimeMillis();

        // 1. Tipo de Incidencia
        tipoRiesgo = tipoIncidenciaRepository.findByCodigo("INC-ATN-01")
                .orElseGet(() -> tipoIncidenciaRepository.save(TipoIncidenciaSeguridad.builder()
                        .codigo("INC-ATN-01")
                        .nombre("Alteración del orden público en plaza")
                        .descripcion("Ruidos molestos y disturbios en vía pública")
                        .categoria(CategoriaIncidenciaSeguridad.ORDEN_PUBLICO)
                        .prioridadSugerida(PrioridadIncidenciaSeguridad.MEDIA)
                        .activo(true)
                        .build()));

        // 2. Sector y Zona
        sectorCentral = sectorRepository.findByCodigo("SEC-ATN-01")
                .orElseGet(() -> sectorRepository.save(SectorPatrullaje.builder()
                        .codigo("SEC-ATN-01")
                        .nombre("Sector 01 - Centro Histórico Quinua")
                        .descripcion("Área central del distrito de Quinua")
                        .estado(EstadoSectorPatrullaje.ACTIVO)
                        .colorHex("#3B82F6")
                        .build()));

        zonaPlaza = zonaRepository.findBySectorIdAndCodigo(sectorCentral.getId(), "ZON-ATN-01")
                .orElseGet(() -> zonaRepository.save(ZonaPatrullaje.builder()
                        .sector(sectorCentral)
                        .codigo("ZON-ATN-01")
                        .nombre("Zona Plaza de Armas")
                        .descripcion("Perímetro de la plaza central")
                        .estado(EstadoSectorPatrullaje.ACTIVO)
                        .build()));

        // 3. Usuario Supervisor
        Persona pSup = personaRepository.save(Persona.builder()
                .nombres("Supervisor")
                .apellidoPaterno("Seguridad")
                .apellidoMaterno("Quinua")
                .numeroDocumento("95" + String.format("%06d", (int)(ts % 1000000)))
                .tipoDocumento("DNI")
                .correo("supervisor_atn_" + ts + "@quinua.gob.pe")
                .estado("ACTIVO")
                .build());

        usuarioSupervisor = usuarioRepository.save(Usuario.builder()
                .username("sup_atn_" + ts)
                .passwordHash("$2a$10$xyz")
                .persona(pSup)
                .enabled(true)
                .accountLocked(false)
                .build());

        // 4. Efectivos de Serenazgo
        Persona pSereno1 = personaRepository.save(Persona.builder()
                .nombres("Carlos")
                .apellidoPaterno("Mendoza")
                .apellidoMaterno("Quispe")
                .numeroDocumento("96" + String.format("%06d", (int)(ts % 1000000)))
                .tipoDocumento("DNI")
                .correo("sereno1_" + ts + "@quinua.gob.pe")
                .estado("ACTIVO")
                .build());

        sereno1 = personalRepository.save(PersonalSerenazgo.builder()
                .persona(pSereno1)
                .codigoInterno("SER-ATN-001-" + (ts % 1000))
                .estado(EstadoPersonalSerenazgo.ACTIVO)
                .fechaIngreso(LocalDate.now().minusMonths(6))
                .build());

        Persona pSereno2 = personaRepository.save(Persona.builder()
                .nombres("Raúl")
                .apellidoPaterno("Pérez")
                .apellidoMaterno("Huamán")
                .numeroDocumento("97" + String.format("%06d", (int)(ts % 1000000)))
                .tipoDocumento("DNI")
                .correo("sereno2_" + ts + "@quinua.gob.pe")
                .estado("ACTIVO")
                .build());

        sereno2 = personalRepository.save(PersonalSerenazgo.builder()
                .persona(pSereno2)
                .codigoInterno("SER-ATN-002-" + (ts % 1000))
                .estado(EstadoPersonalSerenazgo.ACTIVO)
                .fechaIngreso(LocalDate.now().minusMonths(4))
                .build());

        // 5. Patrullero
        patrullero1 = unidadMovilRepository.save(UnidadMovilSerenazgo.builder()
                .codigoInterno("MOV-ATN-" + (ts % 1000))
                .placa("EGA-" + String.format("%03d", (int)(ts % 1000)))
                .marca("Toyota")
                .modelo("Hilux 4x4")
                .tipoUnidad(TipoUnidadMovil.CAMIONETA)
                .estadoOperativo(EstadoUnidadMovil.DISPONIBLE)
                .kilometrajeActual(new BigDecimal("12500.5"))
                .build());

        // 6. Equipamiento
        radioVhf = equipamientoRepository.save(EquipamientoSerenazgo.builder()
                .tipoEquipo(TipoEquipamientoSerenazgo.RADIO_COMUNICACION)
                .descripcion("Radio Portátil Motorola VHF")
                .marca("Motorola")
                .modelo("DGP5050")
                .codigoInterno("EQ-ATN-" + (ts % 1000))
                .codigoPatrimonial("PAT-ATN-" + (ts % 1000))
                .estado(EstadoEquipamientoSerenazgo.DISPONIBLE)
                .stockTotal(1)
                .stockDisponible(1)
                .esControladoPorCantidad(false)
                .build());

        // 7. Incidencia registrada
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
    }

    @Test
    @Order(1)
    @DisplayName("Debe crear una orden de atención correctamente y cambiar el estado de la incidencia a ASIGNADA")
    void testCrearAtencion_Exito() {
        AtencionCrearRequest req = new AtencionCrearRequest();
        req.setIncidenciaId(incidencia.getId());
        req.setPrioridadOperativa(PrioridadIncidenciaSeguridad.ALTA);
        req.setSectorId(sectorCentral.getId());
        req.setResponsablePersonalId(sereno1.getId());
        req.setPersonalIds(List.of(sereno2.getId()));
        req.setUnidadMovilId(patrullero1.getId());
        req.setEquipamientoIds(List.of(radioVhf.getId()));
        req.setDespacharInmediatamente(false);
        req.setObservaciones("Asignación de prueba.");

        AtencionDetailResponse atn = atencionService.crearAtencion(req, usuarioSupervisor.getUsername());

        assertThat(atn).isNotNull();
        assertThat(atn.getCodigo()).startsWith("ATN-");
        assertThat(atn.getEstado()).isEqualTo(EstadoAtencionIncidencia.ASIGNADA);
        assertThat(atn.getPrioridadOperativa()).isEqualTo(PrioridadIncidenciaSeguridad.ALTA);
        assertThat(atn.getResponsableOperativoNombre()).contains("Carlos");
        assertThat(atn.getUnidadMovilCodigo()).isEqualTo(patrullero1.getCodigoInterno());
        assertThat(atn.getPersonalAsignado()).hasSize(2);
        assertThat(atn.getEquipamiento()).hasSize(1);

        // Verificar sincronización de incidencia
        IncidenciaSeguridadCiudadana incActualizada = incidenciaRepository.findById(incidencia.getId()).orElseThrow();
        assertThat(incActualizada.getEstado()).isEqualTo(EstadoIncidenciaSeguridad.ASIGNADA);
    }

    @Test
    @Order(2)
    @DisplayName("Debe permitir registrar una atención sin vehículo en modalidad pedestre")
    void testCrearAtencion_SinVehiculo_Exito() {
        AtencionCrearRequest req = new AtencionCrearRequest();
        req.setIncidenciaId(incidencia.getId());
        req.setPrioridadOperativa(PrioridadIncidenciaSeguridad.BAJA);
        req.setResponsablePersonalId(sereno1.getId());
        req.setUnidadMovilId(null); // Sin vehículo
        req.setDespacharInmediatamente(false);

        AtencionDetailResponse atn = atencionService.crearAtencion(req, usuarioSupervisor.getUsername());

        assertThat(atn.getUnidadMovilCodigo()).isNull();
        assertThat(atn.getEstado()).isEqualTo(EstadoAtencionIncidencia.ASIGNADA);
    }

    @Test
    @Order(3)
    @DisplayName("Debe rechazar la asignación si la unidad móvil ya se encuentra en servicio activo")
    void testCrearAtencion_ValidarConflictoUnidad() {
        // Primera atención con patrullero1
        AtencionCrearRequest req1 = new AtencionCrearRequest();
        req1.setIncidenciaId(incidencia.getId());
        req1.setResponsablePersonalId(sereno1.getId());
        req1.setUnidadMovilId(patrullero1.getId());
        atencionService.crearAtencion(req1, usuarioSupervisor.getUsername());

        // Segunda incidencia
        IncidenciaCreateRequest reqInc2 = IncidenciaCreateRequest.builder()
                .titulo("Segunda incidencia simultánea")
                .tipoIncidenciaId(tipoRiesgo.getId())
                .prioridad(PrioridadIncidenciaSeguridad.BAJA)
                .descripcion("Verificación preventiva")
                .fechaHoraOcurrencia(OffsetDateTime.now(ZONA_PERU))
                .origenHecho(OrigenHechoIncidencia.HECHO_REPORTADO)
                .canalReporte(CanalReporteIncidencia.LLAMADA_OPERADOR)
                .ubicacionReferencia("Jr. Ayacucho 123")
                .build();
        IncidenciaResponse incRes2 = incidenciaService.registrarIncidencia(reqInc2, usuarioSupervisor.getUsername());

        // Intentar segunda atención con la misma unidad
        AtencionCrearRequest req2 = new AtencionCrearRequest();
        req2.setIncidenciaId(incRes2.getId());
        req2.setResponsablePersonalId(sereno2.getId());
        req2.setUnidadMovilId(patrullero1.getId());

        assertThatThrownBy(() -> atencionService.crearAtencion(req2, usuarioSupervisor.getUsername()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ya se encuentra en servicio");
    }

    @Test
    @Order(4)
    @DisplayName("Debe ejecutar el despacho operativo y registrar la orden de desplazamiento")
    void testDespacharAtencion_Exito() {
        AtencionCrearRequest req = new AtencionCrearRequest();
        req.setIncidenciaId(incidencia.getId());
        req.setResponsablePersonalId(sereno1.getId());
        req.setUnidadMovilId(patrullero1.getId());
        AtencionDetailResponse atnCreada = atencionService.crearAtencion(req, usuarioSupervisor.getUsername());

        AtencionDespacharRequest despReq = new AtencionDespacharRequest();
        despReq.setCanalComunicacion(CanalComunicacionDespacho.RADIO_TETRA_VHF);
        despReq.setInstrucciones("Desplazarse con precaución hacia la plaza central.");

        AtencionDetailResponse atnDespachada = atencionService.despacharAtencion(atnCreada.getId(), despReq, usuarioSupervisor.getUsername());

        assertThat(atnDespachada.getEstado()).isEqualTo(EstadoAtencionIncidencia.DESPACHADA);
        assertThat(atnDespachada.getCanalComunicacion()).isEqualTo(CanalComunicacionDespacho.RADIO_TETRA_VHF);
        assertThat(atnDespachada.getFechaHoraDespacho()).isNotNull();
        assertThat(atnDespachada.getEventos()).anyMatch(e -> e.getTipoEvento() == TipoEventoAtencion.DESPACHO_OPERATIVO);
    }

    @Test
    @Order(5)
    @DisplayName("Debe permitir a la cuadrilla aceptar la asignación y registrar la confirmación")
    void testAceptarAtencion_Exito() {
        AtencionCrearRequest req = new AtencionCrearRequest();
        req.setIncidenciaId(incidencia.getId());
        req.setResponsablePersonalId(sereno1.getId());
        req.setDespacharInmediatamente(true);
        req.setCanalComunicacion(CanalComunicacionDespacho.TELEFONIA_MOVIL);
        AtencionDetailResponse atn = atencionService.crearAtencion(req, usuarioSupervisor.getUsername());

        AtencionAceptarRechazarRequest acceptReq = new AtencionAceptarRechazarRequest();
        acceptReq.setAceptado(true);
        acceptReq.setObservaciones("Cuadrilla en marcha hacia el punto.");

        AtencionDetailResponse atnAceptada = atencionService.aceptarORechazarAtencion(atn.getId(), acceptReq, sereno1.getCodigoEmpleado());

        assertThat(atnAceptada.getEstado()).isEqualTo(EstadoAtencionIncidencia.ACEPTADA);
        assertThat(atnAceptada.getFechaHoraAceptacion()).isNotNull();
    }

    @Test
    @Order(6)
    @DisplayName("Debe registrar un rechazo justificado y regresar la orden a PENDIENTE_ASIGNACION")
    void testRechazarAtencion_Exito() {
        AtencionCrearRequest req = new AtencionCrearRequest();
        req.setIncidenciaId(incidencia.getId());
        req.setResponsablePersonalId(sereno1.getId());
        req.setDespacharInmediatamente(true);
        AtencionDetailResponse atn = atencionService.crearAtencion(req, usuarioSupervisor.getUsername());

        AtencionAceptarRechazarRequest rechazoReq = new AtencionAceptarRechazarRequest();
        rechazoReq.setAceptado(false);
        rechazoReq.setMotivoRechazo("Falla en radio y corte de acceso vial por derrumbe temporal.");

        AtencionDetailResponse atnRechazada = atencionService.aceptarORechazarAtencion(atn.getId(), rechazoReq, sereno1.getCodigoEmpleado());

        assertThat(atnRechazada.getEstado()).isEqualTo(EstadoAtencionIncidencia.PENDIENTE_ASIGNACION);
        assertThat(atnRechazada.getMotivoRechazo()).contains("Falla en radio");
        assertThat(atnRechazada.getEventos()).anyMatch(e -> e.getTipoEvento() == TipoEventoAtencion.RECHAZO_UNIDAD);
    }

    @Test
    @Order(7)
    @DisplayName("Debe registrar la llegada al sitio e inicio efectivo de la atención")
    void testIniciarAtencion_Exito() {
        AtencionCrearRequest req = new AtencionCrearRequest();
        req.setIncidenciaId(incidencia.getId());
        req.setResponsablePersonalId(sereno1.getId());
        req.setDespacharInmediatamente(true);
        AtencionDetailResponse atn = atencionService.crearAtencion(req, usuarioSupervisor.getUsername());

        // Aceptar
        AtencionAceptarRechazarRequest acceptReq = new AtencionAceptarRechazarRequest();
        acceptReq.setAceptado(true);
        atencionService.aceptarORechazarAtencion(atn.getId(), acceptReq, sereno1.getCodigoEmpleado());

        // Iniciar
        AtencionIniciarRequest initReq = new AtencionIniciarRequest();
        initReq.setDescripcionSituacionInicial("Llegada sin contratiempos. Se visualiza aglomeración menor.");

        AtencionDetailResponse atnIniciada = atencionService.iniciarAtencion(atn.getId(), initReq, sereno1.getCodigoEmpleado());

        assertThat(atnIniciada.getEstado()).isEqualTo(EstadoAtencionIncidencia.EN_ATENCION);
        assertThat(atnIniciada.getFechaHoraInicio()).isNotNull();

        // Incidencia debe sincronizarse a EN_ATENCION
        IncidenciaSeguridadCiudadana inc = incidenciaRepository.findById(incidencia.getId()).orElseThrow();
        assertThat(inc.getEstado()).isEqualTo(EstadoIncidenciaSeguridad.EN_ATENCION);
    }

    @Test
    @Order(8)
    @DisplayName("Debe registrar novedades en la bitácora operativa con trazabilidad")
    void testRegistrarNovedad_Exito() {
        AtencionCrearRequest req = new AtencionCrearRequest();
        req.setIncidenciaId(incidencia.getId());
        req.setResponsablePersonalId(sereno1.getId());
        req.setDespacharInmediatamente(true);
        AtencionDetailResponse atn = atencionService.crearAtencion(req, usuarioSupervisor.getUsername());

        AtencionNovedadRequest novReq = new AtencionNovedadRequest();
        novReq.setTipoEvento(TipoEventoAtencion.REGISTRO_NOVEDAD);
        novReq.setDescripcion("Se entrevistó al denunciante y se dialogó pacíficamente.");
        novReq.setVisibleCiudadano(true);

        AtencionDetailResponse atnNov = atencionService.registrarNovedad(atn.getId(), novReq, usuarioSupervisor.getUsername());

        assertThat(atnNov.getEventos()).anyMatch(e -> e.getDescripcion().contains("dialogó pacíficamente") && e.isVisibleCiudadano());
    }

    @Test
    @Order(9)
    @DisplayName("Debe permitir poner la atención en espera y luego reanudarla correctamente")
    void testPausarYReanudarAtencion_Exito() {
        AtencionCrearRequest req = new AtencionCrearRequest();
        req.setIncidenciaId(incidencia.getId());
        req.setResponsablePersonalId(sereno1.getId());
        req.setDespacharInmediatamente(true);
        AtencionDetailResponse atn = atencionService.crearAtencion(req, usuarioSupervisor.getUsername());

        // Aceptar e Iniciar
        AtencionAceptarRechazarRequest acc = new AtencionAceptarRechazarRequest();
        acc.setAceptado(true);
        atencionService.aceptarORechazarAtencion(atn.getId(), acc, sereno1.getCodigoEmpleado());
        atencionService.iniciarAtencion(atn.getId(), new AtencionIniciarRequest(), sereno1.getCodigoEmpleado());

        // Pausar
        AtencionPausarReanudarRequest pauseReq = new AtencionPausarReanudarRequest();
        pauseReq.setMotivo("A la espera de apoyo policial para verificación de inmueble.");
        AtencionDetailResponse atnPausada = atencionService.pausarAtencion(atn.getId(), pauseReq, usuarioSupervisor.getUsername());
        assertThat(atnPausada.getEstado()).isEqualTo(EstadoAtencionIncidencia.EN_ESPERA);

        // Reanudar
        AtencionPausarReanudarRequest resumeReq = new AtencionPausarReanudarRequest();
        resumeReq.setMotivo("Llegó efectivo PNP, se reinicia diligencia conjunta.");
        AtencionDetailResponse atnReanudada = atencionService.reanudarAtencion(atn.getId(), resumeReq, usuarioSupervisor.getUsername());
        assertThat(atnReanudada.getEstado()).isEqualTo(EstadoAtencionIncidencia.EN_ATENCION);
    }

    @Test
    @Order(10)
    @DisplayName("Debe registrar escalamiento institucional hacia organismo externo como la PNP")
    void testEscalarAEntidadExterna_Exito() {
        AtencionCrearRequest req = new AtencionCrearRequest();
        req.setIncidenciaId(incidencia.getId());
        req.setResponsablePersonalId(sereno1.getId());
        AtencionDetailResponse atn = atencionService.crearAtencion(req, usuarioSupervisor.getUsername());

        AtencionEscalarRequest escReq = new AtencionEscalarRequest();
        escReq.setEntidad(EntidadEscalamiento.POLICIA_NACIONAL_PERU);
        escReq.setCanalComunicacion(CanalComunicacionDespacho.TELEFONIA_MOVIL);
        escReq.setContactoReceptor("Comisaría Rural de Quinua - Alférez Pérez");
        escReq.setMotivo("Presunta comisión de delito patrimonial que excede competencias de serenazgo.");
        escReq.setNumeroReferencia("COM-2026-089");

        AtencionDetailResponse atnEscalada = atencionService.escalarAtencion(atn.getId(), escReq, usuarioSupervisor.getUsername());

        assertThat(atnEscalada.getEscalamientos()).hasSize(1);
        assertThat(atnEscalada.getEscalamientos().get(0).getEntidad()).isEqualTo(EntidadEscalamiento.POLICIA_NACIONAL_PERU);
        assertThat(atnEscalada.getEscalamientos().get(0).getNumeroReferencia()).isEqualTo("COM-2026-089");
    }

    @Test
    @Order(11)
    @DisplayName("Debe finalizar la atención, liberar recursos y actualizar la incidencia a RESUELTA si se solicita")
    void testFinalizarAtencion_Exito() {
        AtencionCrearRequest req = new AtencionCrearRequest();
        req.setIncidenciaId(incidencia.getId());
        req.setResponsablePersonalId(sereno1.getId());
        req.setUnidadMovilId(patrullero1.getId());
        req.setEquipamientoIds(List.of(radioVhf.getId()));
        req.setDespacharInmediatamente(true);
        AtencionDetailResponse atn = atencionService.crearAtencion(req, usuarioSupervisor.getUsername());

        // Aceptar e iniciar
        AtencionAceptarRechazarRequest acc = new AtencionAceptarRechazarRequest();
        acc.setAceptado(true);
        atencionService.aceptarORechazarAtencion(atn.getId(), acc, sereno1.getCodigoEmpleado());
        atencionService.iniciarAtencion(atn.getId(), new AtencionIniciarRequest(), sereno1.getCodigoEmpleado());

        // Finalizar
        AtencionFinalizarRequest finReq = new AtencionFinalizarRequest();
        finReq.setResultado(ResultadoAtencionIncidencia.SITUACION_CONTROLADA);
        finReq.setResumenActuaciones("Se exhortó a los ciudadanos a mantener la tranquilidad pública. Situación normalizada.");
        finReq.setResolverIncidencia(true);

        AtencionDetailResponse atnFin = atencionService.finalizarAtencion(atn.getId(), finReq, usuarioSupervisor.getUsername());

        assertThat(atnFin.getEstado()).isEqualTo(EstadoAtencionIncidencia.FINALIZADA);
        assertThat(atnFin.getResultadoAdministrativo()).isEqualTo(ResultadoAtencionIncidencia.SITUACION_CONTROLADA);
        assertThat(atnFin.getFechaHoraFinalizacion()).isNotNull();

        // Verificar que los recursos queden liberados
        assertThat(atencionService.isUnidadDisponible(patrullero1.getId())).isTrue();
        assertThat(atencionService.isPersonalDisponible(sereno1.getId())).isTrue();

        // Verificar incidencia resuelta
        IncidenciaSeguridadCiudadana inc = incidenciaRepository.findById(incidencia.getId()).orElseThrow();
        assertThat(inc.getEstado()).isEqualTo(EstadoIncidenciaSeguridad.RESUELTA);
    }

    @Test
    @Order(12)
    @DisplayName("Debe permitir cancelar una orden de atención con motivo obligatorio y liberar los recursos")
    void testCancelarAtencion_Exito() {
        AtencionCrearRequest req = new AtencionCrearRequest();
        req.setIncidenciaId(incidencia.getId());
        req.setResponsablePersonalId(sereno1.getId());
        req.setUnidadMovilId(patrullero1.getId());
        AtencionDetailResponse atn = atencionService.crearAtencion(req, usuarioSupervisor.getUsername());

        AtencionCancelarRequest cancelReq = new AtencionCancelarRequest();
        cancelReq.setMotivo("Duplicidad de orden generada por error de operadora.");

        AtencionDetailResponse atnCancelada = atencionService.cancelarAtencion(atn.getId(), cancelReq, usuarioSupervisor.getUsername());

        assertThat(atnCancelada.getEstado()).isEqualTo(EstadoAtencionIncidencia.CANCELADA);
        assertThat(atnCancelada.getMotivoCancelacion()).contains("Duplicidad de orden");

        // Recursos deben estar libres
        assertThat(atencionService.isUnidadDisponible(patrullero1.getId())).isTrue();
    }

    @Test
    @Order(13)
    @DisplayName("Debe listar atenciones con filtros y retornar las métricas del dashboard operacionales")
    void testListarAtencionesYMetricas_Exito() {
        AtencionCrearRequest req = new AtencionCrearRequest();
        req.setIncidenciaId(incidencia.getId());
        req.setResponsablePersonalId(sereno1.getId());
        atencionService.crearAtencion(req, usuarioSupervisor.getUsername());

        AtencionFilter filter = new AtencionFilter();
        Page<AtencionResponse> page = atencionService.listarAtenciones(filter, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(1);

        DashboardAtencionesResponse metricas = atencionService.obtenerMetricas();
        assertThat(metricas).isNotNull();
        assertThat(metricas.getTotalAtenciones()).isGreaterThanOrEqualTo(1);
    }
}
