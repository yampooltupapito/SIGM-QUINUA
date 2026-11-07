package pe.gob.municipalidad.sigm.modules.serenazgo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.AsignacionPersonalItemRequest;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.CambioEstadoProgramacionRequest;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.CancelarServicioRequest;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.ProgramacionCreateRequest;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.ProgramacionDetailResponse;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.ProgramacionResponse;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.ReprogramarServicioRequest;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.ReservaEquipamientoItemRequest;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.ReservaUnidadItemRequest;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.EquipamientoSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.PersonalSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.SectorPatrullaje;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.TurnoSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.UnidadMovilSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoAsignacionPersonal;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoEquipamientoSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoPersonalSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoProgramacionServicio;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoReservaRecurso;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoSectorPatrullaje;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoTurnoSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoUnidadMovil;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.FuncionOperativaPersonal;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoEquipamientoSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoServicioSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoTurnoSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoUnidadMovil;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.EquipamientoSerenazgoRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.PersonalSerenazgoRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.SectorPatrullajeRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.TurnoSerenazgoRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.UnidadMovilSerenazgoRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.ProgramacionServicioService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
public class ProgramacionServicioServiceTest {

    @Autowired
    private ProgramacionServicioService programacionService;

    @Autowired
    private TurnoSerenazgoRepository turnoRepository;

    @Autowired
    private SectorPatrullajeRepository sectorRepository;

    @Autowired
    private PersonalSerenazgoRepository personalRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private UnidadMovilSerenazgoRepository unidadRepository;

    @Autowired
    private EquipamientoSerenazgoRepository equipamientoRepository;

    private TurnoSerenazgo turnoManana;
    private TurnoSerenazgo turnoTarde;
    private TurnoSerenazgo turnoSolapado;
    private SectorPatrullaje sectorCentro;
    private PersonalSerenazgo sereno1;
    private PersonalSerenazgo sereno2;
    private UnidadMovilSerenazgo camioneta1;
    private EquipamientoSerenazgo radioMotorola;

    @BeforeEach
    void setUp() {
        // Turno Mañana: 08:00 a 16:00
        turnoManana = turnoRepository.save(TurnoSerenazgo.builder()
                .codigo("TUR-MAN-" + (int)(Math.random() * 10000))
                .nombre("Turno Mañana")
                .horaInicio(LocalTime.of(8, 0))
                .horaFin(LocalTime.of(16, 0))
                .cruzaMedianoche(false)
                .duracionReferencialMinutos(480)
                .tipoTurno(TipoTurnoSerenazgo.DIURNO)
                .estado(EstadoTurnoSerenazgo.ACTIVO)
                .colorHex("#10B981")
                .build());

        // Turno Tarde / Noche consecutivo: 16:00 a 23:59 (no solapa con mañana)
        turnoTarde = turnoRepository.save(TurnoSerenazgo.builder()
                .codigo("TUR-TAR-" + (int)(Math.random() * 10000))
                .nombre("Turno Tarde")
                .horaInicio(LocalTime.of(16, 0))
                .horaFin(LocalTime.of(23, 59))
                .cruzaMedianoche(false)
                .duracionReferencialMinutos(479)
                .tipoTurno(TipoTurnoSerenazgo.NOCTURNO)
                .estado(EstadoTurnoSerenazgo.ACTIVO)
                .colorHex("#F59E0B")
                .build());

        // Turno Solapado: 12:00 a 20:00 (se solapa con mañana 08:00-16:00)
        turnoSolapado = turnoRepository.save(TurnoSerenazgo.builder()
                .codigo("TUR-SOL-" + (int)(Math.random() * 10000))
                .nombre("Turno Mixto Solapado")
                .horaInicio(LocalTime.of(12, 0))
                .horaFin(LocalTime.of(20, 0))
                .cruzaMedianoche(false)
                .duracionReferencialMinutos(480)
                .tipoTurno(TipoTurnoSerenazgo.MIXTO)
                .estado(EstadoTurnoSerenazgo.ACTIVO)
                .colorHex("#6366F1")
                .build());

        // Sector Centro
        sectorCentro = sectorRepository.save(SectorPatrullaje.builder()
                .codigo("SEC-TST-" + (int)(Math.random() * 10000))
                .nombre("Sector Centro Quinua Test")
                .estado(EstadoSectorPatrullaje.ACTIVO)
                .colorHex("#3B82F6")
                .build());

        // Persona y Sereno 1
        Persona p1 = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("70" + String.format("%06d", (int)(Math.random() * 1000000)))
                .nombres("Juan Carlos")
                .apellidoPaterno("Quispe")
                .apellidoMaterno("Flores")
                .estado("ACTIVO")
                .build());

        sereno1 = personalRepository.save(PersonalSerenazgo.builder()
                .codigoInterno("SER-TST-001-" + (int)(Math.random() * 10000))
                .persona(p1)
                .estado(EstadoPersonalSerenazgo.ACTIVO)
                .fechaIngreso(LocalDate.now().minusMonths(6))
                .build());

        // Persona y Sereno 2
        Persona p2 = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("71" + String.format("%06d", (int)(Math.random() * 1000000)))
                .nombres("Pedro")
                .apellidoPaterno("Huaman")
                .apellidoMaterno("Rojas")
                .estado("ACTIVO")
                .build());

        sereno2 = personalRepository.save(PersonalSerenazgo.builder()
                .codigoInterno("SER-TST-002-" + (int)(Math.random() * 10000))
                .persona(p2)
                .estado(EstadoPersonalSerenazgo.ACTIVO)
                .fechaIngreso(LocalDate.now().minusMonths(3))
                .build());

        // Unidad móvil
        camioneta1 = unidadRepository.save(UnidadMovilSerenazgo.builder()
                .codigoInterno("MOV-TST-" + (int)(Math.random() * 10000))
                .tipoUnidad(TipoUnidadMovil.CAMIONETA)
                .placa("EGB-" + (int)(Math.random() * 900 + 100))
                .marca("Toyota")
                .modelo("Hilux 4x4")
                .estadoOperativo(EstadoUnidadMovil.OPERATIVA)
                .kilometrajeActual(new BigDecimal("25000"))
                .build());

        // Equipamiento
        radioMotorola = equipamientoRepository.save(EquipamientoSerenazgo.builder()
                .codigoInterno("RAD-TST-" + (int)(Math.random() * 10000))
                .tipoEquipo(TipoEquipamientoSerenazgo.RADIO_COMUNICACION)
                .descripcion("Radio Portátil Motorola VHF")
                .marca("Motorola")
                .modelo("DEP450")
                .estado(EstadoEquipamientoSerenazgo.DISPONIBLE)
                .esControladoPorCantidad(true)
                .stockTotal(5)
                .stockDisponible(5)
                .build());
    }

    @Test
    @Order(1)
    @DisplayName("01: Debe crear exitosamente una programación de servicio con personal, unidad y equipamiento")
    void test01_crearProgramacionExitosa() {
        LocalDate fecha = LocalDate.now().plusDays(2);

        ProgramacionCreateRequest request = ProgramacionCreateRequest.builder()
                .turnoId(turnoManana.getId())
                .sectorId(sectorCentro.getId())
                .tipoServicio(TipoServicioSerenazgo.PATRULLAJE_PREVENTIVO)
                .fechaServicio(fecha)
                .descripcion("Patrullaje preventivo en el sector Centro")
                .observaciones("Servicio regular coordinado")
                .personalAsignado(List.of(
                        AsignacionPersonalItemRequest.builder()
                                .personalId(sereno1.getId())
                                .funcionOperativa(FuncionOperativaPersonal.CONDUCTOR)
                                .esResponsable(true)
                                .observaciones("Chofer designado")
                                .build(),
                        AsignacionPersonalItemRequest.builder()
                                .personalId(sereno2.getId())
                                .funcionOperativa(FuncionOperativaPersonal.PATRULLERO)
                                .esResponsable(false)
                                .build()
                ))
                .unidadesReservadas(List.of(
                        ReservaUnidadItemRequest.builder()
                                .unidadId(camioneta1.getId())
                                .observaciones("Unidad operativa")
                                .build()
                ))
                .equipamientoReservado(List.of(
                        ReservaEquipamientoItemRequest.builder()
                                .equipamientoId(radioMotorola.getId())
                                .cantidad(2)
                                .observaciones("Radios para comunicación de campo")
                                .build()
                ))
                .build();

        ProgramacionResponse response = programacionService.crearProgramacion(request, "supervisor_test");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getCodigo()).startsWith("PRG-");
        assertThat(response.getEstado()).isIn(EstadoProgramacionServicio.BORRADOR, EstadoProgramacionServicio.PROGRAMADA);
        assertThat(response.getTotalPersonalAsignado()).isEqualTo(2);
        assertThat(response.getTotalUnidadesReservadas()).isEqualTo(1);
        assertThat(response.getTotalEquipamientoReservado()).isEqualTo(1);

        ProgramacionDetailResponse detalle = programacionService.obtenerDetallePorId(response.getId());
        assertThat(detalle.getPersonalAsignado()).hasSize(2);
        assertThat(detalle.getUnidadesReservadas()).hasSize(1);
        assertThat(detalle.getEquipamientoReservado()).hasSize(1);
        assertThat(detalle.getHistorial()).isNotEmpty();
    }

    @Test
    @Order(2)
    @DisplayName("02: Debe rechazar asignación por conflicto horario de personal (regla de intervalos semiabiertos)")
    void test02_conflictoHorarioPersonal() {
        LocalDate fecha = LocalDate.now().plusDays(3);

        // Primer servicio: 08:00 a 16:00
        ProgramacionCreateRequest req1 = ProgramacionCreateRequest.builder()
                .turnoId(turnoManana.getId())
                .sectorId(sectorCentro.getId())
                .tipoServicio(TipoServicioSerenazgo.PATRULLAJE_PREVENTIVO)
                .fechaServicio(fecha)
                .personalAsignado(List.of(
                        AsignacionPersonalItemRequest.builder()
                                .personalId(sereno1.getId())
                                .funcionOperativa(FuncionOperativaPersonal.PATRULLERO)
                                .esResponsable(true)
                                .build()
                ))
                .build();

        programacionService.crearProgramacion(req1, "supervisor_test");

        // Segundo servicio: 12:00 a 20:00 (solapa con 08:00-16:00 para sereno1)
        ProgramacionCreateRequest req2 = ProgramacionCreateRequest.builder()
                .turnoId(turnoSolapado.getId())
                .sectorId(sectorCentro.getId())
                .tipoServicio(TipoServicioSerenazgo.VIGILANCIA_MUNICIPAL)
                .fechaServicio(fecha)
                .personalAsignado(List.of(
                        AsignacionPersonalItemRequest.builder()
                                .personalId(sereno1.getId())
                                .funcionOperativa(FuncionOperativaPersonal.PATRULLERO)
                                .esResponsable(true)
                                .build()
                ))
                .build();

        assertThatThrownBy(() -> programacionService.crearProgramacion(req2, "supervisor_test"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Conflicto horario");
    }

    @Test
    @Order(3)
    @DisplayName("03: Servicios consecutivos en el límite exacto no deben generar conflicto (intervalos semiabiertos)")
    void test03_serviciosConsecutivosSinConflicto() {
        LocalDate fecha = LocalDate.now().plusDays(4);

        // Servicio 1: 08:00 a 16:00
        ProgramacionCreateRequest req1 = ProgramacionCreateRequest.builder()
                .turnoId(turnoManana.getId())
                .sectorId(sectorCentro.getId())
                .tipoServicio(TipoServicioSerenazgo.PATRULLAJE_PREVENTIVO)
                .fechaServicio(fecha)
                .personalAsignado(List.of(
                        AsignacionPersonalItemRequest.builder()
                                .personalId(sereno1.getId())
                                .funcionOperativa(FuncionOperativaPersonal.PATRULLERO)
                                .esResponsable(true)
                                .build()
                ))
                .build();

        ProgramacionResponse res1 = programacionService.crearProgramacion(req1, "supervisor_test");
        assertThat(res1).isNotNull();

        // Servicio 2: 16:00 a 23:59 (inicia exactamente cuando termina el anterior)
        ProgramacionCreateRequest req2 = ProgramacionCreateRequest.builder()
                .turnoId(turnoTarde.getId())
                .sectorId(sectorCentro.getId())
                .tipoServicio(TipoServicioSerenazgo.PATRULLAJE_PREVENTIVO)
                .fechaServicio(fecha)
                .personalAsignado(List.of(
                        AsignacionPersonalItemRequest.builder()
                                .personalId(sereno1.getId())
                                .funcionOperativa(FuncionOperativaPersonal.PATRULLERO)
                                .esResponsable(true)
                                .build()
                ))
                .build();

        ProgramacionResponse res2 = programacionService.crearProgramacion(req2, "supervisor_test");
        assertThat(res2).isNotNull();
    }

    @Test
    @Order(4)
    @DisplayName("04: Debe rechazar reserva de unidad móvil por conflicto de horario en servicio simultáneo")
    void test04_conflictoHorarioUnidadMovil() {
        LocalDate fecha = LocalDate.now().plusDays(5);

        // Servicio 1 reserva camioneta1 de 08:00 a 16:00
        ProgramacionCreateRequest req1 = ProgramacionCreateRequest.builder()
                .turnoId(turnoManana.getId())
                .sectorId(sectorCentro.getId())
                .tipoServicio(TipoServicioSerenazgo.PATRULLAJE_PREVENTIVO)
                .fechaServicio(fecha)
                .unidadesReservadas(List.of(
                        ReservaUnidadItemRequest.builder()
                                .unidadId(camioneta1.getId())
                                .build()
                ))
                .build();

        programacionService.crearProgramacion(req1, "supervisor_test");

        // Servicio 2 intenta reservar la misma camioneta1 de 12:00 a 20:00 (solapado)
        ProgramacionCreateRequest req2 = ProgramacionCreateRequest.builder()
                .turnoId(turnoSolapado.getId())
                .sectorId(sectorCentro.getId())
                .tipoServicio(TipoServicioSerenazgo.VIGILANCIA_MUNICIPAL)
                .fechaServicio(fecha)
                .unidadesReservadas(List.of(
                        ReservaUnidadItemRequest.builder()
                                .unidadId(camioneta1.getId())
                                .build()
                ))
                .build();

        assertThatThrownBy(() -> programacionService.crearProgramacion(req2, "supervisor_test"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Conflicto de unidad");
    }

    @Test
    @Order(5)
    @DisplayName("05: Debe confirmar, iniciar y finalizar el ciclo de vida de una programación")
    void test05_confirmarYCambiarEstado() {
        LocalDate fecha = LocalDate.now().plusDays(6);

        ProgramacionCreateRequest req = ProgramacionCreateRequest.builder()
                .turnoId(turnoManana.getId())
                .sectorId(sectorCentro.getId())
                .tipoServicio(TipoServicioSerenazgo.PATRULLAJE_PREVENTIVO)
                .fechaServicio(fecha)
                .personalAsignado(List.of(
                        AsignacionPersonalItemRequest.builder()
                                .personalId(sereno1.getId())
                                .funcionOperativa(FuncionOperativaPersonal.PATRULLERO)
                                .esResponsable(true)
                                .build()
                ))
                .build();

        ProgramacionResponse creada = programacionService.crearProgramacion(req, "supervisor_test");
        UUID id = creada.getId();

        // 1. Confirmar
        ProgramacionResponse confirmada = programacionService.confirmarProgramacion(id, "Aprobado por el jefe de serenazgo", "jefe_serenazgo");
        assertThat(confirmada.getEstado()).isEqualTo(EstadoProgramacionServicio.CONFIRMADA);

        // 2. Iniciar (pasar a EN_CURSO)
        ProgramacionResponse enCurso = programacionService.cambiarEstado(id, CambioEstadoProgramacionRequest.builder()
                .nuevoEstado(EstadoProgramacionServicio.EN_CURSO)
                .motivo("Inicio regular de patrullaje")
                .build(), "operador_cc");
        assertThat(enCurso.getEstado()).isEqualTo(EstadoProgramacionServicio.EN_CURSO);

        // 3. Finalizar
        ProgramacionResponse finalizada = programacionService.finalizarServicio(id, "Patrullaje concluido sin novedades", "operador_cc");
        assertThat(finalizada.getEstado()).isEqualTo(EstadoProgramacionServicio.FINALIZADA);

        // Verificar historial
        ProgramacionDetailResponse detalle = programacionService.obtenerDetallePorId(id);
        assertThat(detalle.getHistorial()).hasSizeGreaterThanOrEqualTo(4); // CREACION, CONFIRMACION, EN_CURSO, FINALIZACION
    }

    @Test
    @Order(6)
    @DisplayName("06: Debe reprogramar un servicio cambiando fecha y turno")
    void test06_reprogramarServicio() {
        LocalDate fechaOriginal = LocalDate.now().plusDays(7);
        LocalDate fechaNueva = LocalDate.now().plusDays(8);

        ProgramacionCreateRequest req = ProgramacionCreateRequest.builder()
                .turnoId(turnoManana.getId())
                .sectorId(sectorCentro.getId())
                .tipoServicio(TipoServicioSerenazgo.PATRULLAJE_PREVENTIVO)
                .fechaServicio(fechaOriginal)
                .personalAsignado(List.of(
                        AsignacionPersonalItemRequest.builder()
                                .personalId(sereno1.getId())
                                .funcionOperativa(FuncionOperativaPersonal.PATRULLERO)
                                .esResponsable(true)
                                .build()
                ))
                .build();

        ProgramacionResponse creada = programacionService.crearProgramacion(req, "supervisor_test");
        UUID id = creada.getId();

        ReprogramarServicioRequest repReq = ReprogramarServicioRequest.builder()
                .nuevoTurnoId(turnoTarde.getId())
                .nuevaFechaServicio(fechaNueva)
                .motivo("Reorganización operativa autorizada")
                .build();

        ProgramacionResponse reprogramada = programacionService.reprogramarServicio(id, repReq, "jefe_serenazgo");

        assertThat(reprogramada.getFechaServicio()).isEqualTo(fechaNueva);
        assertThat(reprogramada.getTurnoNombre()).isEqualTo(turnoTarde.getNombre());

        ProgramacionDetailResponse detalle = programacionService.obtenerDetallePorId(id);
        boolean tieneEventoReprogramacion = detalle.getHistorial().stream()
                .anyMatch(h -> h.getTipoEvento().name().contains("REPROGRAMACION"));
        assertThat(tieneEventoReprogramacion).isTrue();
    }

    @Test
    @Order(7)
    @DisplayName("07: Debe cancelar servicio y liberar personal y recursos asociados")
    void test07_cancelarServicioYLiberarRecursos() {
        LocalDate fecha = LocalDate.now().plusDays(9);

        ProgramacionCreateRequest req = ProgramacionCreateRequest.builder()
                .turnoId(turnoManana.getId())
                .sectorId(sectorCentro.getId())
                .tipoServicio(TipoServicioSerenazgo.PATRULLAJE_PREVENTIVO)
                .fechaServicio(fecha)
                .personalAsignado(List.of(
                        AsignacionPersonalItemRequest.builder()
                                .personalId(sereno1.getId())
                                .funcionOperativa(FuncionOperativaPersonal.PATRULLERO)
                                .esResponsable(true)
                                .build()
                ))
                .unidadesReservadas(List.of(
                        ReservaUnidadItemRequest.builder()
                                .unidadId(camioneta1.getId())
                                .build()
                ))
                .build();

        ProgramacionResponse creada = programacionService.crearProgramacion(req, "supervisor_test");
        UUID id = creada.getId();

        // Cancelar servicio
        CancelarServicioRequest cancelReq = CancelarServicioRequest.builder()
                .motivo("Mantenimiento general del sector e inclemencias climatológicas")
                .build();

        ProgramacionResponse cancelada = programacionService.cancelarServicio(id, cancelReq, "jefe_serenazgo");

        assertThat(cancelada.getEstado()).isEqualTo(EstadoProgramacionServicio.CANCELADA);

        ProgramacionDetailResponse detalle = programacionService.obtenerDetallePorId(id);
        assertThat(detalle.getPersonalAsignado().get(0).getEstadoAsignacion()).isEqualTo(EstadoAsignacionPersonal.CANCELADO);
        assertThat(detalle.getUnidadesReservadas().get(0).getEstadoReserva()).isEqualTo(EstadoReservaRecurso.CANCELADA);
    }

    @Test
    @Order(8)
    @DisplayName("08: Debe generar reporte de despacho diario en formato PDF")
    void test08_exportarDespachoDiarioPdf() {
        LocalDate fecha = LocalDate.now().plusDays(10);

        ProgramacionCreateRequest req = ProgramacionCreateRequest.builder()
                .turnoId(turnoManana.getId())
                .sectorId(sectorCentro.getId())
                .tipoServicio(TipoServicioSerenazgo.PATRULLAJE_PREVENTIVO)
                .fechaServicio(fecha)
                .personalAsignado(List.of(
                        AsignacionPersonalItemRequest.builder()
                                .personalId(sereno1.getId())
                                .funcionOperativa(FuncionOperativaPersonal.PATRULLERO)
                                .esResponsable(true)
                                .build()
                ))
                .build();

        programacionService.crearProgramacion(req, "supervisor_test");

        byte[] pdfBytes = programacionService.exportarDespachoDiarioPdf(fecha);

        assertThat(pdfBytes).isNotEmpty();
        // Encabezado estándar de archivo PDF (%PDF)
        assertThat(new String(pdfBytes, 0, 4)).isEqualTo("%PDF");
    }

    @Test
    @Order(9)
    @DisplayName("09: Debe exportar programación de servicios en formato Excel XLSX")
    void test09_exportarProgramacionExcel() {
        LocalDate desde = LocalDate.now().minusDays(1);
        LocalDate hasta = LocalDate.now().plusDays(15);

        byte[] xlsxBytes = programacionService.exportarProgramacionExcel(desde, hasta);

        assertThat(xlsxBytes).isNotEmpty();
        // Firma ZIP de formato XLSX (PK..)
        assertThat(new String(xlsxBytes, 0, 2)).isEqualTo("PK");
    }
}
