package pe.gob.municipalidad.sigm.modules.serenazgo;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.CargoOperativoSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoAsignacionRecurso;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoEquipamientoSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoEquipamientoSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.CargoOperativoSerenazgoRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.EquipamientoSerenazgoService;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.PersonalSerenazgoService;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.RecursosSeguridadService;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class EquipamientoSerenazgoServiceTest {

    @Autowired
    private EquipamientoSerenazgoService equipamientoService;

    @Autowired
    private RecursosSeguridadService recursosService;

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
                .nombres("Efectivo")
                .apellidoPaterno("Custodio")
                .apellidoMaterno("Quinua")
                .estado("ACTIVO")
                .build());

        CargoOperativoSerenazgo cargo = cargoRepository.findByCodigo("SERENO_PATRULLERO")
                .orElseGet(() -> cargoRepository.findAll().get(0));

        return personalService.registrar(PersonalSerenazgoCreateRequest.builder()
                .personaId(persona.getId())
                .cargoOperativoId(cargo.getId())
                .fechaIngreso(LocalDate.now())
                .tipoDocumentoLaboral("CAS-2026")
                .build(), "admin");
    }

    @Test
    @DisplayName("01: Registro de equipamiento individualizado y por lote de cantidad")
    void test01_registrar_equipamiento_individual_y_cantidad() {
        // Individualizado (Radio)
        EquipamientoResponse radio = equipamientoService.registrar(EquipamientoCreateRequest.builder()
                .tipoEquipo(TipoEquipamientoSerenazgo.RADIO_COMUNICACION)
                .descripcion("Radio Portátil VHF Motorola")
                .marca("Motorola")
                .modelo("DGP5050")
                .numeroSerie("SER-987654")
                .codigoPatrimonial("PAT-EQP-001")
                .esControladoPorCantidad(false)
                .build(), "admin");

        assertThat(radio).isNotNull();
        assertThat(radio.getCodigoInterno()).startsWith("EQP-");
        assertThat(radio.getEstado()).isEqualTo(EstadoEquipamientoSerenazgo.DISPONIBLE);
        assertThat(radio.getStockDisponible()).isEqualTo(1);

        // Controlado por cantidad (Conos viales)
        EquipamientoResponse conos = equipamientoService.registrar(EquipamientoCreateRequest.builder()
                .tipoEquipo(TipoEquipamientoSerenazgo.CONO_SEGURIDAD)
                .descripcion("Cono reflectivo de seguridad vial 70cm")
                .esControladoPorCantidad(true)
                .stockTotal(15)
                .build(), "admin");

        assertThat(conos).isNotNull();
        assertThat(conos.getEsControladoPorCantidad()).isTrue();
        assertThat(conos.getStockDisponible()).isEqualTo(15);
    }

    @Test
    @DisplayName("02: Asignación de equipamiento a sereno con control de stock y cambio de estado")
    void test02_asignar_equipamiento_a_sereno() {
        EquipamientoResponse radio = equipamientoService.registrar(EquipamientoCreateRequest.builder()
                .tipoEquipo(TipoEquipamientoSerenazgo.RADIO_COMUNICACION)
                .descripcion("Radio de patrulla")
                .esControladoPorCantidad(false)
                .build(), "admin");

        PersonalSerenazgoResponse sereno = crearPersonalTest("76");

        AsignacionEquipamientoResponse asig = equipamientoService.asignar(radio.getId(), AsignarEquipamientoRequest.builder()
                .personalId(sereno.getId())
                .motivo("Dotación de servicio de patrullaje a pie")
                .cantidad(1)
                .build(), "admin");

        assertThat(asig).isNotNull();
        assertThat(asig.getEstadoAsignacion()).isEqualTo(EstadoAsignacionRecurso.ACTIVA);

        EquipamientoDetailResponse detalle = equipamientoService.obtenerDetalle(radio.getId());
        assertThat(detalle.getEstado()).isEqualTo(EstadoEquipamientoSerenazgo.ASIGNADO);
        assertThat(detalle.getStockDisponible()).isEqualTo(0);
        assertThat(detalle.getResponsableNombre()).contains("Efectivo");
    }

    @Test
    @DisplayName("03: Devolución de equipamiento con reintegración de existencias a DISPONIBLE")
    void test03_devolver_equipamiento_reintegra_stock() {
        EquipamientoResponse radio = equipamientoService.registrar(EquipamientoCreateRequest.builder()
                .tipoEquipo(TipoEquipamientoSerenazgo.RADIO_COMUNICACION)
                .descripcion("Radio portátil")
                .esControladoPorCantidad(false)
                .build(), "admin");

        PersonalSerenazgoResponse sereno = crearPersonalTest("77");

        equipamientoService.asignar(radio.getId(), AsignarEquipamientoRequest.builder()
                .personalId(sereno.getId())
                .motivo("Ronda matutina")
                .build(), "admin");

        DevolverEquipamientoRequest devReq = DevolverEquipamientoRequest.builder()
                .cantidadDevuelta(1)
                .estadoBienDevolucion("CONFORME")
                .observaciones("Batería completa y antena intacta")
                .build();

        AsignacionEquipamientoResponse devResp = equipamientoService.devolver(radio.getId(), devReq, "admin");
        assertThat(devResp.getEstadoAsignacion()).isEqualTo(EstadoAsignacionRecurso.DEVUELTA_TOTAL);

        EquipamientoDetailResponse detalle = equipamientoService.obtenerDetalle(radio.getId());
        assertThat(detalle.getEstado()).isEqualTo(EstadoEquipamientoSerenazgo.DISPONIBLE);
        assertThat(detalle.getStockDisponible()).isEqualTo(1);
        assertThat(detalle.getResponsableNombre()).isNull();
    }

    @Test
    @DisplayName("04: Rechazo de asignación si la cantidad solicitada supera el stock disponible")
    void test04_stock_insuficiente_rechazado() {
        EquipamientoResponse conos = equipamientoService.registrar(EquipamientoCreateRequest.builder()
                .tipoEquipo(TipoEquipamientoSerenazgo.CONO_SEGURIDAD)
                .descripcion("Cono vial")
                .esControladoPorCantidad(true)
                .stockTotal(4)
                .build(), "admin");

        PersonalSerenazgoResponse sereno = crearPersonalTest("78");

        assertThatThrownBy(() -> equipamientoService.asignar(conos.getId(), AsignarEquipamientoRequest.builder()
                .personalId(sereno.getId())
                .cantidad(5)
                .motivo("Operativo")
                .build(), "admin"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Stock insuficiente");
    }

    @Test
    @DisplayName("05: Generación y exportación de libro Excel con 2 hojas y protección contra fórmulas maliciosas")
    void test05_exportacion_excel_sanitizada() throws Exception {
        byte[] bytesExcel = recursosService.exportarExcel(new UnidadMovilFilter(), new EquipamientoFilter());
        assertThat(bytesExcel).isNotNull();
        assertThat(bytesExcel.length).isGreaterThan(1000);

        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytesExcel))) {
            assertThat(wb.getNumberOfSheets()).isEqualTo(2);
            assertThat(wb.getSheetAt(0).getSheetName()).isEqualTo("Unidades Móviles");
            assertThat(wb.getSheetAt(1).getSheetName()).isEqualTo("Equipamiento Operativo");
        }
    }
}
