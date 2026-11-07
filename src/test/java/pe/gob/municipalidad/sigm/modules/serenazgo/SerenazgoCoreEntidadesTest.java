package pe.gob.municipalidad.sigm.modules.serenazgo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class SerenazgoCoreEntidadesTest {

    @Autowired
    private CargoOperativoSerenazgoRepository cargoRepository;

    @Autowired
    private PersonalSerenazgoRepository personalRepository;

    @Autowired
    private UnidadMovilSerenazgoRepository unidadRepository;

    @Autowired
    private EquipamientoSerenazgoRepository equipamientoRepository;

    @Autowired
    private SectorPatrullajeRepository sectorRepository;

    @Autowired
    private ZonaPatrullajeRepository zonaRepository;

    @Autowired
    private TurnoSerenazgoRepository turnoRepository;

    @Autowired
    private ProgramacionServicioSerenazgoRepository programacionRepository;

    @Autowired
    private TipoIncidenciaSeguridadRepository tipoIncidenciaRepository;

    @Autowired
    private IncidenciaSeguridadCiudadanaRepository incidenciaRepository;

    @Autowired
    private AlertaCiudadanaSeguridadRepository alertaRepository;

    @Autowired
    private IntervencionSerenazgoRepository intervencionRepository;

    @Autowired
    private PatrullajeSerenazgoRepository patrullajeRepository;

    @Autowired
    private SupervisionSerenazgoRepository supervisionRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    @DisplayName("01: Validar Catálogos Técnicos insertados por Flyway V37")
    void test01_catalogos_tecnicos_flyway() {
        assertThat(cargoRepository.findAll()).isNotEmpty();
        assertThat(tipoIncidenciaRepository.findAll()).isNotEmpty();
        assertThat(turnoRepository.findAll()).isNotEmpty();

        assertThat(cargoRepository.findByCodigo("SERENO_PATRULLERO")).isPresent();
        assertThat(tipoIncidenciaRepository.findByCodigo("ALTERACION_ORDEN")).isPresent();
        assertThat(turnoRepository.findByCodigo("TURNO_MANANA")).isPresent();
    }

    @Test
    @DisplayName("02: Validar Persistencia y Secuencia de Personal de Serenazgo")
    void test02_persistencia_personal_serenazgo() {
        long ts = System.currentTimeMillis();
        Persona persona = new Persona();
        persona.setTipoDocumento("DNI");
        persona.setNumeroDocumento("70" + String.format("%06d", (int)(ts % 1000000)));
        persona.setNombres("SerenoTest");
        persona.setApellidoPaterno("Paterno");
        persona.setApellidoMaterno("Materno");
        persona.setEstado("ACTIVO");
        persona = personaRepository.save(persona);

        Long sec = personalRepository.obtenerSiguienteValorSecuencia();
        assertThat(sec).isNotNull();

        CargoOperativoSerenazgo cargo = cargoRepository.findByCodigo("SERENO_PATRULLERO").orElseThrow();

        PersonalSerenazgo sereno = PersonalSerenazgo.builder()
                .codigoInterno("SER-" + sec)
                .persona(persona)
                .cargoOperativo(cargo)
                .estado(EstadoPersonalSerenazgo.ACTIVO)
                .fechaIngreso(LocalDate.now())
                .build();
        sereno = personalRepository.save(sereno);

        assertThat(sereno.getId()).isNotNull();
        assertThat(personalRepository.findByCodigoInterno("SER-" + sec)).isPresent();
    }

    @Test
    @DisplayName("03: Validar Unidades Móviles y Equipamiento de Serenazgo")
    void test03_persistencia_unidad_y_equipamiento() {
        long ts = System.currentTimeMillis();
        Long secUnidad = unidadRepository.obtenerSiguienteValorSecuencia();

        UnidadMovilSerenazgo unidad = UnidadMovilSerenazgo.builder()
                .codigoInterno("MOV-" + secUnidad)
                .tipoUnidad(TipoUnidadMovil.CAMIONETA)
                .placa("EGA-" + (ts % 1000))
                .marca("Toyota")
                .modelo("Hilux 4x4")
                .anio(2025)
                .estadoOperativo(EstadoUnidadMovil.OPERATIVA)
                .kilometrajeActual(new BigDecimal("12500.50"))
                .build();
        unidad = unidadRepository.save(unidad);
        assertThat(unidad.getId()).isNotNull();

        EquipamientoSerenazgo radio = EquipamientoSerenazgo.builder()
                .codigoInterno("EQ-RAD-" + ts)
                .tipoEquipo(TipoEquipamientoSerenazgo.RADIO_COMUNICACION)
                .descripcion("Radio Portátil Motorola VHF")
                .numeroSerie("SN-" + ts)
                .estado(EstadoEquipamientoSerenazgo.DISPONIBLE)
                .unidadMovil(unidad)
                .build();
        radio = equipamientoRepository.save(radio);
        assertThat(radio.getId()).isNotNull();
        assertThat(equipamientoRepository.findByUnidadMovilId(unidad.getId())).isNotEmpty();
    }

    @Test
    @DisplayName("04: Validar Jerarquía Territorial - Sectores, Zonas y Puntos de Referencia")
    void test04_territorio_sectores_y_zonas() {
        long ts = System.currentTimeMillis();
        Long secSector = sectorRepository.obtenerSiguienteValorSecuencia();

        SectorPatrullaje sector = SectorPatrullaje.builder()
                .codigo("SEC-" + secSector)
                .nombre("Sector Centro Histórico Quinua")
                .descripcion("Área circundante a la Plaza Mayor y talleres artesanales")
                .colorHex("#3b82f6")
                .estado(EstadoSectorPatrullaje.ACTIVO)
                .build();
        sector = sectorRepository.save(sector);
        assertThat(sector.getId()).isNotNull();

        ZonaPatrullaje zona = ZonaPatrullaje.builder()
                .sector(sector)
                .codigo("Z-01")
                .nombre("Zona Plaza Principal")
                .estado(EstadoSectorPatrullaje.ACTIVO)
                .build();
        zona = zonaRepository.save(zona);
        assertThat(zona.getId()).isNotNull();

        assertThat(zonaRepository.findBySectorId(sector.getId())).hasSize(1);
    }

    @Test
    @DisplayName("05: Validar Incidencia, Alerta Ciudadana, Intervención y Patrullaje")
    void test05_ciclo_operativo_incidencia_y_patrullaje() {
        long ts = System.currentTimeMillis();

        // 1. Usuario registro
        Persona per = new Persona();
        per.setTipoDocumento("DNI");
        per.setNumeroDocumento("71" + String.format("%06d", (int)(ts % 1000000)));
        per.setNombres("OperadorTest");
        per.setApellidoPaterno("Operador");
        per.setApellidoMaterno("Central");
        per.setEstado("ACTIVO");
        per = personaRepository.save(per);

        Usuario usuarioOperador = usuarioRepository.save(Usuario.builder()
                .username("operador_" + ts)
                .persona(per)
                .passwordHash("hash")
                .enabled(true)
                .accountLocked(false)
                .build());

        // 2. Sereno Responsable
        CargoOperativoSerenazgo cargo = cargoRepository.findByCodigo("SERENO_PATRULLERO").orElseThrow();
        PersonalSerenazgo sereno = personalRepository.save(PersonalSerenazgo.builder()
                .codigoInterno("SER-OP-" + ts)
                .persona(per)
                .cargoOperativo(cargo)
                .estado(EstadoPersonalSerenazgo.ACTIVO)
                .build());

        // 3. Sector
        SectorPatrullaje sector = sectorRepository.save(SectorPatrullaje.builder()
                .codigo("SEC-OP-" + ts)
                .nombre("Sector Operativo Test")
                .estado(EstadoSectorPatrullaje.ACTIVO)
                .build());

        // 4. Tipo Incidencia
        TipoIncidenciaSeguridad tipoInc = tipoIncidenciaRepository.findByCodigo("ALTERACION_ORDEN").orElseThrow();

        // 5. Registrar Incidencia
        Long secInc = incidenciaRepository.obtenerSiguienteValorSecuencia();
        IncidenciaSeguridadCiudadana inc = IncidenciaSeguridadCiudadana.builder()
                .codigo("INC-" + secInc)
                .tipoIncidencia(tipoInc)
                .prioridad(PrioridadIncidenciaSeguridad.MEDIA)
                .descripcion("Discusión en vía pública sin agresión física")
                .ubicacionReferencia("Esq. Jirón Sucre con San Martín")
                .sector(sector)
                .latitud(new BigDecimal("-13.0583000"))
                .longitud(new BigDecimal("-74.1389000"))
                .estado(EstadoIncidenciaSeguridad.ASIGNADA)
                .canalReporte(CanalReporteIncidencia.LLAMADA_OPERADOR)
                .usuarioRegistro(usuarioOperador)
                .responsable(sereno)
                .build();
        inc = incidenciaRepository.save(inc);
        assertThat(inc.getId()).isNotNull();

        // 6. Registrar Intervención
        Long secInt = intervencionRepository.obtenerSiguienteValorSecuencia();
        IntervencionSerenazgo intervencion = IntervencionSerenazgo.builder()
                .codigo("INT-" + secInt)
                .incidencia(inc)
                .responsable(sereno)
                .descripcionActuacion("Se exhortó a las partes a mantener la calma y retirarse pacíficamente")
                .resultado(ResultadoIntervencionSerenazgo.RESUELTO_EN_LUGAR)
                .estado(EstadoIntervencionSerenazgo.FINALIZADA)
                .fechaFin(OffsetDateTime.now())
                .build();
        intervencion = intervencionRepository.save(intervencion);
        assertThat(intervencion.getId()).isNotNull();

        // 7. Registrar Patrullaje
        Long secPat = patrullajeRepository.obtenerSiguienteValorSecuencia();
        PatrullajeSerenazgo patrullaje = PatrullajeSerenazgo.builder()
                .codigo("PAT-" + secPat)
                .sector(sector)
                .responsable(sereno)
                .tipoPatrullaje(TipoPatrullajeSerenazgo.PATRULLAJE_MUNICIPAL)
                .estado(EstadoPatrullajeSerenazgo.EN_CURSO)
                .build();
        patrullaje = patrullajeRepository.save(patrullaje);
        assertThat(patrullaje.getId()).isNotNull();

        // 8. Registrar Supervisión
        Long secSup = supervisionRepository.obtenerSiguienteValorSecuencia();
        SupervisionSerenazgo supervision = SupervisionSerenazgo.builder()
                .codigo("SUP-" + secSup)
                .supervisor(sereno)
                .sector(sector)
                .patrullaje(patrullaje)
                .resultado(ResultadoSupervisionSerenazgo.CONFORME)
                .estado(EstadoSupervisionSerenazgo.REGISTRADA)
                .observaciones("Patrullaje ejecutado de acuerdo a la hoja de ruta establecida")
                .build();
        supervision = supervisionRepository.save(supervision);
        assertThat(supervision.getId()).isNotNull();
    }
}
