package pe.gob.municipalidad.sigm.modules.serenazgo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.CargoOperativoSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.CargoOperativoSerenazgoRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.PersonalSerenazgoService;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.UnidadesMovilesService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UnidadesMovilesServiceTest {

    @Autowired
    private UnidadesMovilesService unidadesService;

    @Autowired
    private PersonalSerenazgoService personalService;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private CargoOperativoSerenazgoRepository cargoRepository;

    private PersonalSerenazgoResponse crearPersonalTest(String prefijo) {
        long ts = System.currentTimeMillis();
        Persona persona = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento(prefijo + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Sereno")
                .apellidoPaterno("Conductor")
                .apellidoMaterno("Quinua")
                .estado("ACTIVO")
                .build());

        CargoOperativoSerenazgo cargo = cargoRepository.findByCodigo("SERENO_CHOFER")
                .orElseGet(() -> cargoRepository.findAll().get(0));

        return personalService.registrar(PersonalSerenazgoCreateRequest.builder()
                .personaId(persona.getId())
                .cargoOperativoId(cargo.getId())
                .fechaIngreso(LocalDate.now())
                .tipoDocumentoLaboral("CAS-2026")
                .build(), "admin");
    }

    @Test
    @DisplayName("01: Registro exitoso de unidad móvil con generación correlativa UNI-XXXXXX")
    void test01_registro_unidad_exitoso() {
        String placa = "EGA-" + (int)(Math.random() * 900 + 100);
        UnidadMovilCreateRequest req = UnidadMovilCreateRequest.builder()
                .tipoUnidad(TipoUnidadMovil.CAMIONETA)
                .placa(placa)
                .marca("Toyota")
                .modelo("Hilux 4x4")
                .anio(2025)
                .color("Blanco Institucional")
                .kilometrajeActual(new BigDecimal("1500.50"))
                .codigoPatrimonial("PAT-7464-001")
                .observaciones("Equipada con sirena y circulina")
                .build();

        UnidadMovilResponse resp = unidadesService.registrar(req, "admin");

        assertThat(resp).isNotNull();
        assertThat(resp.getId()).isNotNull();
        assertThat(resp.getCodigoInterno()).startsWith("UNI-");
        assertThat(resp.getPlaca()).isEqualTo(placa);
        assertThat(resp.getEstadoOperativo()).isEqualTo(EstadoUnidadMovil.DISPONIBLE);
        assertThat(resp.getDisponibleParaAsignacion()).isTrue();
    }

    @Test
    @DisplayName("02: Rechazo de placa duplicada con normalización de caracteres")
    void test02_placa_duplicada_rechazada() {
        String placa = "EGB-999";
        UnidadMovilCreateRequest req1 = UnidadMovilCreateRequest.builder()
                .tipoUnidad(TipoUnidadMovil.MOTOCICLETA)
                .placa("  egb-999  ")
                .marca("Honda")
                .modelo("XR190")
                .build();
        unidadesService.registrar(req1, "admin");

        UnidadMovilCreateRequest req2 = UnidadMovilCreateRequest.builder()
                .tipoUnidad(TipoUnidadMovil.MOTOCICLETA)
                .placa("EGB-999")
                .marca("Yamaha")
                .build();

        assertThatThrownBy(() -> unidadesService.registrar(req2, "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe una unidad móvil registrada con la placa: EGB-999");
    }

    @Test
    @DisplayName("03: Transición transaccional de estado con auditoría en historial")
    void test03_cambio_estado_transaccional() {
        UnidadMovilResponse unidad = unidadesService.registrar(UnidadMovilCreateRequest.builder()
                .tipoUnidad(TipoUnidadMovil.BICICLETA)
                .marca("Trek")
                .modelo("Marlin 7")
                .build(), "admin");

        CambioEstadoUnidadRequest cambioReq = CambioEstadoUnidadRequest.builder()
                .nuevoEstado(EstadoUnidadMovil.EN_MANTENIMIENTO)
                .motivo("Mantenimiento preventivo de frenos y cadena")
                .build();

        UnidadMovilResponse actualizada = unidadesService.cambiarEstado(unidad.getId(), cambioReq, "admin");
        assertThat(actualizada.getEstadoOperativo()).isEqualTo(EstadoUnidadMovil.EN_MANTENIMIENTO);
        assertThat(actualizada.getDisponibleParaAsignacion()).isFalse();

        UnidadMovilDetailResponse detalle = unidadesService.obtenerDetalle(unidad.getId());
        assertThat(detalle.getHistorialEstados()).isNotEmpty();
        assertThat(detalle.getHistorialEstados().get(0).getEstadoNuevo()).isEqualTo(EstadoUnidadMovil.EN_MANTENIMIENTO);
        assertThat(detalle.getHistorialEstados().get(0).getMotivo()).contains("preventivo de frenos");
    }

    @Test
    @DisplayName("04: Asignación de unidad disponible a efectivo de serenazgo activo")
    void test04_asignacion_unidad_personal_exito() {
        UnidadMovilResponse unidad = unidadesService.registrar(UnidadMovilCreateRequest.builder()
                .tipoUnidad(TipoUnidadMovil.CAMIONETA)
                .placa("EGC-" + (int)(Math.random() * 900 + 100))
                .kilometrajeActual(new BigDecimal("5000.00"))
                .build(), "admin");

        PersonalSerenazgoResponse sereno = crearPersonalTest("72");

        AsignarUnidadRequest asigReq = AsignarUnidadRequest.builder()
                .personalId(sereno.getId())
                .motivoServicio("Patrullaje Integrado Cuadrante 2")
                .kilometrajeSalida(new BigDecimal("5000.00"))
                .fechaInicio(OffsetDateTime.now())
                .estadoInicialUnidad("BUENO")
                .build();

        AsignacionUnidadResponse asigResp = unidadesService.asignar(unidad.getId(), asigReq, "admin");

        assertThat(asigResp).isNotNull();
        assertThat(asigResp.getEstadoAsignacion()).isEqualTo(EstadoAsignacionRecurso.ACTIVA);

        UnidadMovilDetailResponse detalle = unidadesService.obtenerDetalle(unidad.getId());
        assertThat(detalle.getEstadoOperativo()).isEqualTo(EstadoUnidadMovil.ASIGNADA);
        assertThat(detalle.getResponsableActualNombre()).contains("Sereno");
    }

    @Test
    @DisplayName("05: Impedir asignación simultánea si la unidad ya está asignada o en mantenimiento")
    void test05_asignacion_simultanea_rechazada() {
        UnidadMovilResponse unidad = unidadesService.registrar(UnidadMovilCreateRequest.builder()
                .tipoUnidad(TipoUnidadMovil.MOTOCICLETA)
                .placa("EGD-" + (int)(Math.random() * 900 + 100))
                .build(), "admin");

        PersonalSerenazgoResponse sereno1 = crearPersonalTest("73");
        PersonalSerenazgoResponse sereno2 = crearPersonalTest("74");

        unidadesService.asignar(unidad.getId(), AsignarUnidadRequest.builder()
                .personalId(sereno1.getId())
                .motivoServicio("Ronda preventiva 1")
                .build(), "admin");

        assertThatThrownBy(() -> unidadesService.asignar(unidad.getId(), AsignarUnidadRequest.builder()
                .personalId(sereno2.getId())
                .motivoServicio("Ronda preventiva 2")
                .build(), "admin"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no se encuentra disponible para asignación");
    }

    @Test
    @DisplayName("06: Devolución de unidad con registro de kilometraje y reincorporación a DISPONIBLE")
    void test06_devolucion_unidad_actualiza_kilometraje() {
        UnidadMovilResponse unidad = unidadesService.registrar(UnidadMovilCreateRequest.builder()
                .tipoUnidad(TipoUnidadMovil.CAMIONETA)
                .placa("EGE-" + (int)(Math.random() * 900 + 100))
                .kilometrajeActual(new BigDecimal("10000.00"))
                .build(), "admin");

        PersonalSerenazgoResponse sereno = crearPersonalTest("75");

        unidadesService.asignar(unidad.getId(), AsignarUnidadRequest.builder()
                .personalId(sereno.getId())
                .motivoServicio("Guardia nocturna")
                .kilometrajeSalida(new BigDecimal("10000.00"))
                .build(), "admin");

        DevolverUnidadRequest devReq = DevolverUnidadRequest.builder()
                .kilometrajeRetorno(new BigDecimal("10075.50"))
                .estadoRetornoUnidad("CONFORME")
                .observacionesDevolucion("Sin novedades mecánicas")
                .build();

        AsignacionUnidadResponse devResp = unidadesService.devolver(unidad.getId(), devReq, "admin");
        assertThat(devResp.getEstadoAsignacion()).isEqualTo(EstadoAsignacionRecurso.DEVUELTA);
        assertThat(devResp.getKilometrajeRetorno()).isEqualTo(new BigDecimal("10075.50"));

        UnidadMovilDetailResponse detalle = unidadesService.obtenerDetalle(unidad.getId());
        assertThat(detalle.getEstadoOperativo()).isEqualTo(EstadoUnidadMovil.DISPONIBLE);
        assertThat(detalle.getKilometrajeActual()).isEqualTo(new BigDecimal("10075.50"));
        assertThat(detalle.getResponsableActualNombre()).isNull();
    }

    @Test
    @DisplayName("07: Programar mantenimiento preventivo para unidad móvil")
    void test07_mantenimiento_preventivo() {
        UnidadMovilResponse unidad = unidadesService.registrar(UnidadMovilCreateRequest.builder()
                .tipoUnidad(TipoUnidadMovil.CAMIONETA)
                .build(), "admin");

        RegistroMantenimientoRequest req = RegistroMantenimientoRequest.builder()
                .tipoRecurso(TipoRecursoSeguridad.UNIDAD_MOVIL)
                .tipoMantenimiento(TipoMantenimientoRecurso.PREVENTIVO)
                .fechaProgramada(LocalDate.now().plusDays(5))
                .descripcion("Revisión de frenos e inyección")
                .tallerProveedor("Taller Autorizado Ayacucho")
                .costo(new BigDecimal("450.00"))
                .build();

        MantenimientoResponse mnt = unidadesService.registrarMantenimiento(unidad.getId(), req, "admin");
        assertThat(mnt).isNotNull();
        assertThat(mnt.getCodigo()).startsWith("MNT-");
        assertThat(mnt.getEstado()).isEqualTo(EstadoMantenimientoRecurso.PROGRAMADO);
    }

    @Test
    @DisplayName("08: Registrar incidencia técnica crítica que pasa unidad a FUERA_DE_SERVICIO")
    void test08_incidencia_critica_fuera_de_servicio() {
        UnidadMovilResponse unidad = unidadesService.registrar(UnidadMovilCreateRequest.builder()
                .tipoUnidad(TipoUnidadMovil.MOTOCICLETA)
                .build(), "admin");

        RegistroIncidenciaTecnicaRequest req = RegistroIncidenciaTecnicaRequest.builder()
                .tipoRecurso(TipoRecursoSeguridad.UNIDAD_MOVIL)
                .tipoIncidencia(TipoIncidenciaTecnica.AVERIA)
                .nivelGravedad(NivelGravedadIncidencia.CRITICA)
                .descripcion("Falla en caja de cambios y pérdida de compresión")
                .build();

        IncidenciaTecnicaResponse inc = unidadesService.registrarIncidencia(unidad.getId(), req, "admin");
        assertThat(inc.getCodigo()).startsWith("INC-");
        assertThat(inc.getEstado()).isEqualTo(EstadoIncidenciaTecnica.REGISTRADA);

        UnidadMovilDetailResponse detalle = unidadesService.obtenerDetalle(unidad.getId());
        assertThat(detalle.getEstadoOperativo()).isEqualTo(EstadoUnidadMovil.FUERA_DE_SERVICIO);
    }

    @Test
    @DisplayName("09: Generación binaria válida de Ficha Técnica PDF institucional")
    void test09_generar_ficha_pdf() {
        UnidadMovilResponse unidad = unidadesService.registrar(UnidadMovilCreateRequest.builder()
                .tipoUnidad(TipoUnidadMovil.CAMIONETA)
                .placa("PDF-123")
                .marca("Nissan")
                .modelo("Frontier")
                .build(), "admin");

        byte[] pdf = unidadesService.generarFichaPdf(unidad.getId());
        assertThat(pdf).isNotNull();
        assertThat(pdf.length).isGreaterThan(100);
        // Validar cabecera PDF (%PDF-)
        assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");
    }
}
