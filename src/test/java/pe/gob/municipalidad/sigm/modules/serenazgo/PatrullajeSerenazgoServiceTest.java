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
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.PatrullajeSerenazgoService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PatrullajeSerenazgoServiceTest {

    @Autowired
    private PatrullajeSerenazgoService patrullajeService;

    @Autowired
    private SectorPatrullajeRepository sectorRepository;

    @Autowired
    private ZonaPatrullajeRepository zonaRepository;

    @Autowired
    private RutaPatrullajeRepository rutaRepository;

    @Autowired
    private PuntoControlRutaRepository puntoControlRepository;

    @Autowired
    private PersonalSerenazgoRepository personalRepository;

    @Autowired
    private UnidadMovilSerenazgoRepository unidadMovilRepository;

    @Autowired
    private EquipamientoSerenazgoRepository equipamientoRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private IncidenciaSeguridadCiudadanaRepository incidenciaRepository;

    @Autowired
    private TipoIncidenciaSeguridadRepository tipoIncidenciaRepository;

    private SectorPatrullaje sectorUrbano;
    private ZonaPatrullaje zonaPlaza;
    private RutaPatrullaje rutaPlaza;
    private PuntoControlRuta punto1;
    private PuntoControlRuta punto2;
    private PersonalSerenazgo serenoLider;
    private PersonalSerenazgo serenoApoyo;
    private UnidadMovilSerenazgo camioneta;
    private EquipamientoSerenazgo radioTetra;

    @BeforeEach
    void setUp() {
        long ts = System.currentTimeMillis();

        // 1. Sector y Zona
        sectorUrbano = sectorRepository.findByCodigo("SEC-PAT-01")
                .orElseGet(() -> sectorRepository.save(SectorPatrullaje.builder()
                        .codigo("SEC-PAT-01")
                        .nombre("Sector Casco Urbano Quinua")
                        .descripcion("Área cívica y comercial")
                        .estado(EstadoSectorPatrullaje.ACTIVO)
                        .colorHex("#3B82F6")
                        .build()));

        zonaPlaza = zonaRepository.findBySectorIdAndCodigo(sectorUrbano.getId(), "ZON-PAT-01")
                .orElseGet(() -> zonaRepository.save(ZonaPatrullaje.builder()
                        .sector(sectorUrbano)
                        .codigo("ZON-PAT-01")
                        .nombre("Plaza Mayor y Calles Históricas")
                        .estado(EstadoSectorPatrullaje.ACTIVO)
                        .build()));

        // 2. Ruta y Puntos de Control
        rutaPlaza = rutaRepository.findByCodigo("RUT-PAT-01")
                .orElseGet(() -> {
                    RutaPatrullaje r = rutaRepository.save(RutaPatrullaje.builder()
                            .codigo("RUT-PAT-01")
                            .nombre("Ronda Preventiva Casco Cívico")
                            .sector(sectorUrbano)
                            .zona(zonaPlaza)
                            .distanciaEstimadaKm(BigDecimal.valueOf(2.5))
                            .tiempoEstimadoMin(40)
                            .estado("ACTIVA")
                            .versionRuta(1)
                            .build());

                    punto1 = puntoControlRepository.save(PuntoControlRuta.builder()
                            .ruta(r)
                            .orden(1)
                            .codigo("PC-01")
                            .nombre("Caseta Plaza Mayor")
                            .referenciaUbicacion("Frente al Palacio Municipal")
                            .latitud(BigDecimal.valueOf(-13.0425))
                            .longitud(BigDecimal.valueOf(-74.1388))
                            .tiempoEstimadoDesdeInicioMin(0)
                            .activo(true)
                            .build());

                    punto2 = puntoControlRepository.save(PuntoControlRuta.builder()
                            .ruta(r)
                            .orden(2)
                            .codigo("PC-02")
                            .nombre("Centro de Salud Quinua")
                            .referenciaUbicacion("Jr. Arequipa s/n")
                            .latitud(BigDecimal.valueOf(-13.0435))
                            .longitud(BigDecimal.valueOf(-74.1378))
                            .tiempoEstimadoDesdeInicioMin(15)
                            .activo(true)
                            .build());

                    r.getPuntosControl().add(punto1);
                    r.getPuntosControl().add(punto2);
                    return r;
                });

        if (punto1 == null && !rutaPlaza.getPuntosControl().isEmpty()) {
            punto1 = rutaPlaza.getPuntosControl().get(0);
            if (rutaPlaza.getPuntosControl().size() > 1) {
                punto2 = rutaPlaza.getPuntosControl().get(1);
            }
        }

        // 3. Personal de Serenazgo
        Persona p1 = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("41" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Juan Carlos")
                .apellidoPaterno("Quispe")
                .apellidoMaterno("Huaman")
                .estado("ACTIVO")
                .build());

        serenoLider = personalRepository.save(PersonalSerenazgo.builder()
                .persona(p1)
                .codigoInterno("SER-LID-" + ts)
                .estado(EstadoPersonalSerenazgo.ACTIVO)
                .build());

        Persona p2 = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("42" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Pedro")
                .apellidoPaterno("Alanya")
                .apellidoMaterno("Flores")
                .estado("ACTIVO")
                .build());

        serenoApoyo = personalRepository.save(PersonalSerenazgo.builder()
                .persona(p2)
                .codigoInterno("SER-APO-" + ts)
                .estado(EstadoPersonalSerenazgo.ACTIVO)
                .build());

        // 4. Unidad Móvil
        camioneta = unidadMovilRepository.save(UnidadMovilSerenazgo.builder()
                .codigoInterno("UN-PAT-" + ts)
                .placa("EGA-" + String.format("%03d", (int)(ts % 1000)))
                .tipoUnidad(TipoUnidadMovil.CAMIONETA)
                .estadoOperativo(EstadoUnidadMovil.DISPONIBLE)
                .kilometrajeActual(BigDecimal.valueOf(15420.0))
                .build());

        // 5. Equipamiento
        radioTetra = equipamientoRepository.save(EquipamientoSerenazgo.builder()
                .codigoInterno("EQ-RAD-" + ts)
                .codigoPatrimonial("CP-RAD-" + ts)
                .tipoEquipo(TipoEquipamientoSerenazgo.RADIO_COMUNICACION)
                .descripcion("Radio Portátil TETRA")
                .estado(EstadoEquipamientoSerenazgo.DISPONIBLE)
                .build());
    }

    @Test
    @Order(1)
    @DisplayName("CP-01: Planificar y crear patrullaje preventivo exitosamente")
    void testCrearPatrullajeExitoso() {
        LocalDateTime inicio = LocalDateTime.now().plusHours(1);
        LocalDateTime fin = inicio.plusHours(4);

        PatrullajeCrearRequest req = PatrullajeCrearRequest.builder()
                .tipoPatrullaje(TipoPatrullajeSerenazgo.PATRULLAJE_MOTORIZADO)
                .sectorId(sectorUrbano.getId())
                .zonaId(zonaPlaza.getId())
                .rutaId(rutaPlaza.getId())
                .fechaHoraInicioProgramada(inicio)
                .fechaHoraFinProgramada(fin)
                .responsableId(serenoLider.getId())
                .unidadMovilId(camioneta.getId())
                .participantesIds(List.of(serenoApoyo.getId()))
                .equipamientoIds(List.of(radioTetra.getId()))
                .objetivoOperativo("Patrullaje preventivo disuasivo en perímetro cívico y posta médica de Quinua.")
                .descripcion("Servicio preventivo ordinario")
                .observaciones("Coordinación previa con central")
                .build();

        PatrullajeDetailResponse resp = patrullajeService.crearPatrullaje(req, "supervisor_quinua");

        assertThat(resp).isNotNull();
        assertThat(resp.getId()).isNotNull();
        assertThat(resp.getCodigo()).startsWith("PAT-");
        assertThat(resp.getEstado()).isEqualTo(EstadoPatrullajeSerenazgo.ASIGNADO);
        assertThat(resp.getResponsableNombre()).contains("Juan Carlos");
        assertThat(resp.getUnidadPlaca()).isEqualTo(camioneta.getPlaca());
        assertThat(resp.getParticipantes()).hasSize(1);
        assertThat(resp.getEquipamiento()).hasSize(1);
        assertThat(resp.getHistorial()).isNotEmpty();
    }

    @Test
    @Order(2)
    @DisplayName("CP-02: Validar error cuando fecha de fin es anterior a fecha de inicio")
    void testCrearPatrullajeFechasInvalidas() {
        LocalDateTime inicio = LocalDateTime.now().plusHours(2);
        LocalDateTime fin = inicio.minusHours(1);

        PatrullajeCrearRequest req = PatrullajeCrearRequest.builder()
                .tipoPatrullaje(TipoPatrullajeSerenazgo.PATRULLAJE_A_PIE)
                .sectorId(sectorUrbano.getId())
                .fechaHoraInicioProgramada(inicio)
                .fechaHoraFinProgramada(fin)
                .responsableId(serenoLider.getId())
                .objetivoOperativo("Ronda a pie")
                .build();

        assertThatThrownBy(() -> patrullajeService.crearPatrullaje(req, "test_user"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no puede ser anterior");
    }

    @Test
    @Order(3)
    @DisplayName("CP-03: Validar que patrullaje extraordinario exige fundamentación y motivo")
    void testCrearPatrullajeExtraordinarioSinMotivo() {
        LocalDateTime inicio = LocalDateTime.now().plusHours(1);
        LocalDateTime fin = inicio.plusHours(3);

        PatrullajeCrearRequest req = PatrullajeCrearRequest.builder()
                .tipoPatrullaje(TipoPatrullajeSerenazgo.PATRULLAJE_A_PIE)
                .sectorId(sectorUrbano.getId())
                .fechaHoraInicioProgramada(inicio)
                .fechaHoraFinProgramada(fin)
                .responsableId(serenoLider.getId())
                .esExtraordinario(true)
                .motivoExtraordinario("   ")
                .objetivoOperativo("Patrullaje especial")
                .build();

        assertThatThrownBy(() -> patrullajeService.crearPatrullaje(req, "test_user"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fundamentar el motivo");
    }

    @Test
    @Order(4)
    @DisplayName("CP-04: Iniciar patrullaje formalmente y verificar transición de estado")
    void testIniciarPatrullajeExitoso() {
        PatrullajeDetailResponse nuevo = crearPatrullajeBase();

        PatrullajeIniciarRequest iniReq = PatrullajeIniciarRequest.builder()
                .kmInicio(BigDecimal.valueOf(15420.0))
                .observaciones("Salida conforme de base central")
                .build();

        PatrullajeDetailResponse iniciado = patrullajeService.iniciarPatrullaje(nuevo.getId(), iniReq, "sereno_operativo");

        assertThat(iniciado.getEstado()).isEqualTo(EstadoPatrullajeSerenazgo.EN_CURSO);
        assertThat(iniciado.getFechaHoraInicioReal()).isNotNull();
        assertThat(iniciado.getKmInicio()).isEqualByComparingTo("15420.0");
    }

    @Test
    @Order(5)
    @DisplayName("CP-05: Validar que no se puede iniciar un patrullaje ya finalizado o cancelado")
    void testIniciarPatrullajeEstadoInvalido() {
        PatrullajeDetailResponse nuevo = crearPatrullajeBase();
        patrullajeService.cancelarPatrullaje(nuevo.getId(), new PatrullajeCancelarRequest("Suspensión por lluvia"), "admin");

        assertThatThrownBy(() -> patrullajeService.iniciarPatrullaje(nuevo.getId(), new PatrullajeIniciarRequest(), "sereno"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Solo se puede iniciar un patrullaje en estado PROGRAMADO o ASIGNADO");
    }

    @Test
    @Order(6)
    @DisplayName("CP-06: Registrar paso efectivo por punto de control en ruta")
    void testRegistrarPasoPuntoExitoso() {
        PatrullajeDetailResponse nuevo = crearPatrullajeBase();
        patrullajeService.iniciarPatrullaje(nuevo.getId(), new PatrullajeIniciarRequest(), "sereno");

        PatrullajeRegistrarVisitaRequest visReq = PatrullajeRegistrarVisitaRequest.builder()
                .puntoControlId(punto1.getId())
                .estadoVisita(EstadoVisitaPunto.VISITADO)
                .metodoRegistro(MetodoRegistroVisita.MANUAL)
                .observaciones("Paso por caseta sin incidentes")
                .build();

        PatrullajeDetailResponse resp = patrullajeService.registrarPasoPunto(nuevo.getId(), visReq, "sereno");

        assertThat(resp.getPuntosVisitados()).isGreaterThanOrEqualTo(1);
        assertThat(resp.getVisitasPuntos()).anyMatch(v -> v.getPuntoControlId().equals(punto1.getId()) && v.getEstadoVisita() == EstadoVisitaPunto.VISITADO);
    }

    @Test
    @Order(7)
    @DisplayName("CP-07: Validar que omitir un punto de control requiere obligatoriamente motivo justificado")
    void testRegistrarPuntoOmitidoSinMotivo() {
        PatrullajeDetailResponse nuevo = crearPatrullajeBase();
        patrullajeService.iniciarPatrullaje(nuevo.getId(), new PatrullajeIniciarRequest(), "sereno");

        PatrullajeRegistrarVisitaRequest visReq = PatrullajeRegistrarVisitaRequest.builder()
                .puntoControlId(punto1.getId())
                .estadoVisita(EstadoVisitaPunto.OMITIDO)
                .motivoOmision("   ")
                .build();

        assertThatThrownBy(() -> patrullajeService.registrarPasoPunto(nuevo.getId(), visReq, "sereno"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("obligatorio detallar el motivo");
    }

    @Test
    @Order(8)
    @DisplayName("CP-08: Registrar novedad operativa durante el patrullaje")
    void testRegistrarNovedadExitoso() {
        PatrullajeDetailResponse nuevo = crearPatrullajeBase();
        patrullajeService.iniciarPatrullaje(nuevo.getId(), new PatrullajeIniciarRequest(), "sereno");

        PatrullajeRegistrarNovedadRequest novReq = PatrullajeRegistrarNovedadRequest.builder()
                .tipoNovedad(TipoNovedadPatrullaje.OBSERVACION_PREVENTIVA)
                .descripcion("Grupo de jóvenes consumiendo bebidas en vía pública, se procedió a exhortación pacífica.")
                .observaciones("Se retiraron sin alteración del orden")
                .build();

        PatrullajeDetailResponse resp = patrullajeService.registrarNovedad(nuevo.getId(), novReq, "sereno");

        assertThat(resp.getNovedades()).hasSize(1);
        assertThat(resp.getNovedades().get(0).getTipoNovedad()).isEqualTo(TipoNovedadPatrullaje.OBSERVACION_PREVENTIVA);
    }

    @Test
    @Order(9)
    @DisplayName("CP-09: Pausar y reanudar patrullaje con trazabilidad en historial")
    void testPausarYReanudarPatrullaje() {
        PatrullajeDetailResponse nuevo = crearPatrullajeBase();
        patrullajeService.iniciarPatrullaje(nuevo.getId(), new PatrullajeIniciarRequest(), "sereno");

        // Pausar
        PatrullajeDetailResponse enPausa = patrullajeService.pausarPatrullaje(nuevo.getId(),
                new PatrullajePausarRequest("Reabastecimiento de combustible en base municipal"), "central_operador");
        assertThat(enPausa.getEstado()).isEqualTo(EstadoPatrullajeSerenazgo.EN_PAUSA);
        assertThat(enPausa.getMotivoPausa()).contains("combustible");

        // Reanudar
        PatrullajeDetailResponse reanudado = patrullajeService.reanudarPatrullaje(nuevo.getId(),
                new PatrullajeReanudarRequest("Tanque lleno, reingreso a cuadrante"), "central_operador");
        assertThat(reanudado.getEstado()).isEqualTo(EstadoPatrullajeSerenazgo.EN_CURSO);
    }

    @Test
    @Order(10)
    @DisplayName("CP-10: Finalizar patrullaje y verificar resultado administrativo")
    void testFinalizarPatrullajeExitoso() {
        PatrullajeDetailResponse nuevo = crearPatrullajeBase();
        patrullajeService.iniciarPatrullaje(nuevo.getId(), new PatrullajeIniciarRequest(null, BigDecimal.valueOf(15420.0), "Ok"), "sereno");

        PatrullajeFinalizarRequest finReq = PatrullajeFinalizarRequest.builder()
                .kmFin(BigDecimal.valueOf(15435.5))
                .resultadoAdministrativo("Ronda concluida con total normalidad y sin alteraciones graves al orden público.")
                .observaciones("Relevo conforme con guardia entrante")
                .build();

        PatrullajeDetailResponse finalizado = patrullajeService.finalizarPatrullaje(nuevo.getId(), finReq, "supervisor");

        assertThat(finalizado.getEstado()).isEqualTo(EstadoPatrullajeSerenazgo.FINALIZADO);
        assertThat(finalizado.getFechaHoraFinReal()).isNotNull();
        assertThat(finalizado.getDistanciaRecorridaEstimadaKm()).isEqualByComparingTo("15.5");
        assertThat(finalizado.getResultadoAdministrativo()).contains("total normalidad");
    }

    @Test
    @Order(11)
    @DisplayName("CP-11: Listar patrullajes con filtros paginados y verificar KPIs")
    void testListarPatrullajesConFiltros() {
        crearPatrullajeBase();

        PatrullajeFilter filtro = PatrullajeFilter.builder()
                .sectorId(sectorUrbano.getId())
                .build();

        Page<PatrullajeResponse> page = patrullajeService.listarPatrullajes(filtro, PageRequest.of(0, 10));

        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(1);

        DashboardPatrullajesResponse kpis = patrullajeService.obtenerDashboardKpis();
        assertThat(kpis.getTotal()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @Order(12)
    @DisplayName("CP-12: Exportar listado a Excel y generar ficha en PDF")
    void testExportacionesPdfYExcel() {
        PatrullajeDetailResponse nuevo = crearPatrullajeBase();

        byte[] excel = patrullajeService.exportarExcel(new PatrullajeFilter());
        assertThat(excel).isNotNull().isNotEmpty();

        byte[] pdf = patrullajeService.generarFichaPdf(nuevo.getId());
        assertThat(pdf).isNotNull().isNotEmpty();
    }

    private PatrullajeDetailResponse crearPatrullajeBase() {
        LocalDateTime inicio = LocalDateTime.now().plusMinutes(10);
        LocalDateTime fin = inicio.plusHours(3);

        PatrullajeCrearRequest req = PatrullajeCrearRequest.builder()
                .tipoPatrullaje(TipoPatrullajeSerenazgo.PATRULLAJE_MOTORIZADO)
                .sectorId(sectorUrbano.getId())
                .zonaId(zonaPlaza.getId())
                .rutaId(rutaPlaza.getId())
                .fechaHoraInicioProgramada(inicio)
                .fechaHoraFinProgramada(fin)
                .responsableId(serenoLider.getId())
                .unidadMovilId(camioneta.getId())
                .objetivoOperativo("Patrullaje preventivo de prueba")
                .build();

        return patrullajeService.crearPatrullaje(req, "test_admin");
    }
}
