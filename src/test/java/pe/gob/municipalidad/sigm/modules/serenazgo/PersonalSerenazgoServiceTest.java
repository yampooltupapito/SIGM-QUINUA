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
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoPersonalSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.CargoOperativoSerenazgoRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.PersonalSerenazgoService;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PersonalSerenazgoServiceTest {

    @Autowired
    private PersonalSerenazgoService personalService;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private CargoOperativoSerenazgoRepository cargoRepository;

    private Persona crearPersonaTest(String prefijo) {
        long ts = System.currentTimeMillis();
        return personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento(prefijo + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Juan")
                .apellidoPaterno("Pérez")
                .apellidoMaterno("Quispe")
                .estado("ACTIVO")
                .build());
    }

    @Test
    @DisplayName("01: Registro exitoso de personal con código correlativo e historiales iniciales")
    void test01_registro_exitoso() {
        Persona persona = crearPersonaTest("71");
        CargoOperativoSerenazgo cargo = cargoRepository.findByCodigo("SERENO_PATRULLERO").orElseThrow();

        PersonalSerenazgoCreateRequest req = PersonalSerenazgoCreateRequest.builder()
                .personaId(persona.getId())
                .cargoOperativoId(cargo.getId())
                .tipoDocumentoLaboral("Contrato CAS")
                .fechaIngreso(LocalDate.now())
                .observaciones("Incorporación regular")
                .build();

        PersonalSerenazgoResponse res = personalService.registrar(req, "admin");

        assertThat(res).isNotNull();
        assertThat(res.getId()).isNotNull();
        assertThat(res.getCodigoInterno()).startsWith("SER-");
        assertThat(res.getEstado()).isEqualTo("ACTIVO");

        // Validar historiales iniciales
        PersonalSerenazgoDetailResponse detalle = personalService.obtenerDetalle(res.getId());
        assertThat(detalle.getHistorialCargos()).hasSize(1);
        assertThat(detalle.getHistorialEstados()).hasSize(1);
        assertThat(detalle.isDisponible()).isTrue();
    }

    @Test
    @DisplayName("02: Rechazo de duplicidad al registrar una persona con ficha activa")
    void test02_rechazo_duplicidad_persona_activa() {
        Persona persona = crearPersonaTest("72");
        CargoOperativoSerenazgo cargo = cargoRepository.findByCodigo("SERENO_PATRULLERO").orElseThrow();

        PersonalSerenazgoCreateRequest req = PersonalSerenazgoCreateRequest.builder()
                .personaId(persona.getId())
                .cargoOperativoId(cargo.getId())
                .fechaIngreso(LocalDate.now())
                .build();

        personalService.registrar(req, "admin");

        assertThatThrownBy(() -> personalService.registrar(req, "admin"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ya cuenta con una ficha activa");
    }

    @Test
    @DisplayName("03: Rechazo al vincular persona inexistente")
    void test03_rechazo_persona_inexistente() {
        CargoOperativoSerenazgo cargo = cargoRepository.findByCodigo("SERENO_PATRULLERO").orElseThrow();

        PersonalSerenazgoCreateRequest req = PersonalSerenazgoCreateRequest.builder()
                .personaId(999999L)
                .cargoOperativoId(cargo.getId())
                .fechaIngreso(LocalDate.now())
                .build();

        assertThatThrownBy(() -> personalService.registrar(req, "admin"))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("04: Actualización de datos laborales del personal")
    void test04_actualizacion_datos() {
        Persona persona = crearPersonaTest("73");
        CargoOperativoSerenazgo cargo = cargoRepository.findByCodigo("SERENO_PATRULLERO").orElseThrow();

        PersonalSerenazgoResponse creado = personalService.registrar(PersonalSerenazgoCreateRequest.builder()
                .personaId(persona.getId())
                .cargoOperativoId(cargo.getId())
                .tipoDocumentoLaboral("Prueba")
                .fechaIngreso(LocalDate.now())
                .build(), "admin");

        PersonalSerenazgoUpdateRequest updateReq = PersonalSerenazgoUpdateRequest.builder()
                .tipoDocumentoLaboral("D.L. 728 Indeterminado")
                .observaciones("Actualización por concurso interno")
                .build();

        PersonalSerenazgoResponse actualizado = personalService.actualizar(creado.getId(), updateReq, "admin");
        assertThat(actualizado).isNotNull();

        PersonalSerenazgoDetailResponse detalle = personalService.obtenerDetalle(creado.getId());
        assertThat(detalle.getTipoDocumentoLaboral()).isEqualTo("D.L. 728 Indeterminado");
        assertThat(detalle.getObservaciones()).isEqualTo("Actualización por concurso interno");
    }

    @Test
    @DisplayName("05: Cambio de estado con trazabilidad y registro en historial")
    void test05_cambio_estado_con_historial() {
        Persona persona = crearPersonaTest("74");
        CargoOperativoSerenazgo cargo = cargoRepository.findByCodigo("SERENO_PATRULLERO").orElseThrow();

        PersonalSerenazgoResponse creado = personalService.registrar(PersonalSerenazgoCreateRequest.builder()
                .personaId(persona.getId())
                .cargoOperativoId(cargo.getId())
                .fechaIngreso(LocalDate.now())
                .build(), "admin");

        CambioEstadoPersonalRequest cambioEstado = CambioEstadoPersonalRequest.builder()
                .nuevoEstado(EstadoPersonalSerenazgo.SUSPENDIDO)
                .fechaEfectiva(LocalDate.now())
                .motivo("Licencia médica temporal de 15 días")
                .build();

        PersonalSerenazgoResponse res = personalService.cambiarEstado(creado.getId(), cambioEstado, "admin");
        assertThat(res.getEstado()).isEqualTo("SUSPENDIDO");
        assertThat(res.isDisponible()).isFalse();

        PersonalSerenazgoDetailResponse detalle = personalService.obtenerDetalle(creado.getId());
        assertThat(detalle.getHistorialEstados()).hasSize(2);
        assertThat(detalle.isDisponible()).isFalse();
    }

    @Test
    @DisplayName("06: Rechazo al cambiar al mismo estado actual")
    void test06_rechazo_mismo_estado() {
        Persona persona = crearPersonaTest("75");
        CargoOperativoSerenazgo cargo = cargoRepository.findByCodigo("SERENO_PATRULLERO").orElseThrow();

        PersonalSerenazgoResponse creado = personalService.registrar(PersonalSerenazgoCreateRequest.builder()
                .personaId(persona.getId())
                .cargoOperativoId(cargo.getId())
                .fechaIngreso(LocalDate.now())
                .build(), "admin");

        CambioEstadoPersonalRequest cambioEstado = CambioEstadoPersonalRequest.builder()
                .nuevoEstado(EstadoPersonalSerenazgo.ACTIVO)
                .fechaEfectiva(LocalDate.now())
                .motivo("Mismo estado redundante")
                .build();

        assertThatThrownBy(() -> personalService.cambiarEstado(creado.getId(), cambioEstado, "admin"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("07: Cambio de cargo operativo con cierre de vigencia anterior")
    void test07_cambio_cargo_operativo() {
        Persona persona = crearPersonaTest("76");
        CargoOperativoSerenazgo cargoInicial = cargoRepository.findByCodigo("SERENO_PATRULLERO").orElseThrow();
        CargoOperativoSerenazgo cargoNuevo = cargoRepository.findByCodigo("SUPERVISOR_TURNO").orElseThrow();

        PersonalSerenazgoResponse creado = personalService.registrar(PersonalSerenazgoCreateRequest.builder()
                .personaId(persona.getId())
                .cargoOperativoId(cargoInicial.getId())
                .fechaIngreso(LocalDate.now())
                .build(), "admin");

        CambioCargoPersonalRequest cambioCargo = CambioCargoPersonalRequest.builder()
                .nuevoCargoId(cargoNuevo.getId())
                .fechaInicio(LocalDate.now())
                .motivo("Promoción por mérito y rendimiento operativo")
                .build();

        PersonalSerenazgoResponse res = personalService.cambiarCargo(creado.getId(), cambioCargo, "admin");
        assertThat(res.getNombreCargoOperativo()).isEqualTo(cargoNuevo.getNombre());

        PersonalSerenazgoDetailResponse detalle = personalService.obtenerDetalle(creado.getId());
        assertThat(detalle.getHistorialCargos()).hasSize(2);
        assertThat(detalle.getHistorialCargos().get(0).getCargoNuevo()).isEqualTo(cargoNuevo.getNombre());
    }

    @Test
    @DisplayName("08: Listado y filtrado con paginación")
    void test08_listado_y_filtros() {
        Persona persona = crearPersonaTest("77");
        CargoOperativoSerenazgo cargo = cargoRepository.findByCodigo("CHOFER_PATRULLERO").orElseThrow();

        personalService.registrar(PersonalSerenazgoCreateRequest.builder()
                .personaId(persona.getId())
                .cargoOperativoId(cargo.getId())
                .fechaIngreso(LocalDate.now())
                .build(), "admin");

        PersonalSerenazgoFilter filtro = PersonalSerenazgoFilter.builder()
                .cargoOperativoId(cargo.getId())
                .estado(EstadoPersonalSerenazgo.ACTIVO)
                .build();

        Page<PersonalSerenazgoResponse> pagina = personalService.listar(filtro, PageRequest.of(0, 10));
        assertThat(pagina).isNotEmpty();
        assertThat(pagina.getContent().stream().allMatch(p -> p.getCargoOperativoId().equals(cargo.getId()))).isTrue();
    }

    @Test
    @DisplayName("09: Generación exitosa de Ficha Técnica en PDF institucional")
    void test09_generar_pdf() {
        Persona persona = crearPersonaTest("78");
        CargoOperativoSerenazgo cargo = cargoRepository.findByCodigo("SERENO_PATRULLERO").orElseThrow();

        PersonalSerenazgoResponse creado = personalService.registrar(PersonalSerenazgoCreateRequest.builder()
                .personaId(persona.getId())
                .cargoOperativoId(cargo.getId())
                .fechaIngreso(LocalDate.now())
                .build(), "admin");

        byte[] pdfBytes = personalService.generarFichaPdf(creado.getId());
        assertThat(pdfBytes).isNotEmpty();
        // Verificar que empiece con la cabecera PDF '%PDF'
        assertThat(new String(pdfBytes, 0, 4)).isEqualTo("%PDF");
    }

    @Test
    @DisplayName("10: Estadísticas operativas agregadas")
    void test10_estadisticas_personal() {
        EstadisticasPersonalResponse stats = personalService.obtenerEstadisticas();
        assertThat(stats).isNotNull();
        assertThat(stats.getTotalRegistrados()).isGreaterThanOrEqualTo(0);
        assertThat(stats.getDistribucionPorCargo()).isNotNull();
    }
}
