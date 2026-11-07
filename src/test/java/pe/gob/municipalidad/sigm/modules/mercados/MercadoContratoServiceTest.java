package pe.gob.municipalidad.sigm.modules.mercados;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.*;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;
import pe.gob.municipalidad.sigm.modules.mercados.service.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MercadoContratoServiceTest {

    @Autowired
    private ContratoOcupacionService contratoService;

    @Autowired
    private AsignacionPuestoService asignacionService;

    @Autowired
    private VigenciaOcupacionService vigenciaService;

    @Autowired
    private ContratoExportService contratoExportService;

    @Autowired
    private MercadoMunicipalRepository mercadoRepository;

    @Autowired
    private PuestoComercialRepository puestoRepository;

    @Autowired
    private ComercianteRepository comercianteRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private AsignacionPuestoRepository asignacionRepository;

    @Autowired
    private ContratoOcupacionRepository contratoRepository;

    @Autowired
    private TipoContratoRepository tipoContratoRepository;

    private MercadoMunicipal mercado;
    private PuestoComercial puesto;
    private Comerciante comerciante;
    private AsignacionPuesto asignacion;
    private TipoContrato tipoContrato;

    @BeforeEach
    void setUp() {
        long ts = System.currentTimeMillis();

        mercado = mercadoRepository.save(MercadoMunicipal.builder()
                .codigo("MER-CONT-" + (ts % 10000))
                .nombre("Mercado Central de Quinua Test")
                .direccion("Plaza Principal S/N")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .estado(EstadoMercado.ACTIVO)
                .build());

        puesto = puestoRepository.save(PuestoComercial.builder()
                .mercado(mercado)
                .codigo("PUE-C-" + (ts % 10000))
                .numero("B-201")
                .areaMetrosCuadrados(new BigDecimal("14.00"))
                .estado(EstadoPuesto.DISPONIBLE)
                .build());

        Persona persona = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("42" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Cipriano")
                .apellidoPaterno("Quispe")
                .apellidoMaterno("Huaman")
                .correo("cipriano@quinua.gob.pe")
                .direccion("Comunidad de Postoq, Quinua")
                .estado("ACTIVO")
                .build());

        comerciante = comercianteRepository.save(Comerciante.builder()
                .persona(persona)
                .tipoComerciante(TipoComerciante.PERSONA_NATURAL)
                .codigoComerciante("COM-C-" + (ts % 10000))
                .estado(EstadoComerciante.ACTIVO)
                .fechaRegistro(LocalDate.now())
                .build());

        // Asignación válida
        asignacion = asignacionRepository.save(AsignacionPuesto.builder()
                .comerciante(comerciante)
                .puesto(puesto)
                .tipoAsignacion(TipoAsignacion.TEMPORAL)
                .fechaSolicitud(LocalDate.now().minusDays(10))
                .fechaInicio(LocalDate.now())
                .fechaFin(LocalDate.now().plusMonths(12))
                .estado(EstadoAsignacion.VIGENTE)
                .build());

        tipoContrato = tipoContratoRepository.findByCodigo("USO_ESPACIO_COMERCIAL")
                .orElseGet(() -> tipoContratoRepository.save(new TipoContrato(
                        null, "USO_ESPACIO_COMERCIAL", "Uso de Espacio Comercial",
                        "Instrumento contractual para uso de puestos", "ORD-MUN-2026", 12, "ACTIVO"
                )));
    }

    @Test
    @DisplayName("1. Crear borrador de contrato con asignación válida")
    void testCrearBorradorContrato() {
        ContratoCreateRequest req = ContratoCreateRequest.builder()
                .asignacionId(asignacion.getId())
                .tipoContratoId(tipoContrato.getId())
                .fechaInicio(LocalDate.now())
                .fechaFin(LocalDate.now().plusMonths(12))
                .observaciones("Contrato de prueba para puesto B-201")
                .build();

        ContratoResponse resp = contratoService.crearBorrador(req, "operador_mercados");

        assertThat(resp).isNotNull();
        assertThat(resp.getId()).isNotNull();
        assertThat(resp.getCodigoContrato()).startsWith("CONT-");
        assertThat(resp.getEstado()).isEqualTo(EstadoContrato.BORRADOR);
        assertThat(resp.getComercianteNombre()).contains("Cipriano");
        assertThat(resp.getPuestoNumero()).isEqualTo("B-201");
    }

    @Test
    @DisplayName("2. Rechazar contrato con fechas inconsistentes (fecha fin anterior a fecha inicio)")
    void testRechazarFechasInconsistentes() {
        ContratoCreateRequest req = ContratoCreateRequest.builder()
                .asignacionId(asignacion.getId())
                .tipoContratoId(tipoContrato.getId())
                .fechaInicio(LocalDate.now())
                .fechaFin(LocalDate.now().minusDays(5)) // Invalido
                .build();

        assertThatThrownBy(() -> contratoService.crearBorrador(req, "operador"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("fecha de fin no puede ser anterior");
    }

    @Test
    @DisplayName("3. Rechazar contrato con asignación inexistente")
    void testRechazarAsignacionInexistente() {
        ContratoCreateRequest req = ContratoCreateRequest.builder()
                .asignacionId(UUID.randomUUID())
                .fechaInicio(LocalDate.now())
                .build();

        assertThatThrownBy(() -> contratoService.crearBorrador(req, "operador"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("4. Ciclo de vida completo: Borrador -> En Revisión -> Observado -> Aprobado -> Formalizado -> Terminado")
    void testCicloDeVidaContrato() {
        // 1. Crear
        ContratoCreateRequest req = ContratoCreateRequest.builder()
                .asignacionId(asignacion.getId())
                .tipoContratoId(tipoContrato.getId())
                .fechaInicio(LocalDate.now())
                .fechaFin(LocalDate.now().plusMonths(6))
                .build();
        ContratoResponse c1 = contratoService.crearBorrador(req, "operador_muni");

        // 2. Enviar a revisión
        ContratoResponse c2 = contratoService.enviarARevision(c1.getId(), "operador_muni");
        assertThat(c2.getEstado()).isEqualTo(EstadoContrato.EN_REVISION);

        // 3. Observar
        ContratoEstadoRequest obsReq = ContratoEstadoRequest.builder()
                .motivo("Falta especificar anexo de pago y dimensiones")
                .documentoReferencia("MEMO-ADM-01")
                .build();
        ContratoResponse c3 = contratoService.observar(c1.getId(), obsReq, "asesor_legal");
        assertThat(c3.getEstado()).isEqualTo(EstadoContrato.OBSERVADO);

        // 4. Subsanar y enviar a revisión nuevamente
        ContratoUpdateRequest updReq = ContratoUpdateRequest.builder()
                .tipoContratoId(tipoContrato.getId())
                .fechaInicio(LocalDate.now())
                .fechaFin(LocalDate.now().plusMonths(6))
                .observaciones("Subsanado con plano anexo")
                .build();
        contratoService.actualizarBorrador(c1.getId(), updReq, "operador_muni");
        contratoService.enviarARevision(c1.getId(), "operador_muni");

        // 5. Aprobar
        ContratoEstadoRequest aprReq = ContratoEstadoRequest.builder()
                .motivo("Conformidad técnica y jurídica")
                .documentoReferencia("INF-LEG-2026-05")
                .build();
        ContratoResponse c4 = contratoService.aprobar(c1.getId(), aprReq, "gerente_desarrollo");
        assertThat(c4.getEstado()).isEqualTo(EstadoContrato.APROBADO);

        // 6. Formalizar (pasa a VIGENTE, genera token seguro y hash SHA-256)
        ContratoEstadoRequest formReq = ContratoEstadoRequest.builder()
                .motivo("Suscripción de actas y firmas presenciales")
                .documentoReferencia("ACTA-SUSC-2026")
                .build();
        ContratoResponse c5 = contratoService.formalizar(c1.getId(), formReq, "alcaldia");
        assertThat(c5.getEstado()).isEqualTo(EstadoContrato.VIGENTE);
        assertThat(c5.getCodigoSeguro()).isNotNull();
        assertThat(c5.getHashDocumento()).isNotNull();

        // 7. Terminar
        ContratoEstadoRequest termReq = ContratoEstadoRequest.builder()
                .motivo("Cumplimiento del plazo o mutuo disenso")
                .fechaEfectiva(LocalDate.now())
                .build();
        ContratoResponse c6 = contratoService.terminar(c1.getId(), termReq, "alcaldia");
        assertThat(c6.getEstado()).isEqualTo(EstadoContrato.TERMINADO);

        // 8. Verificar historial inmutable
        ContratoDetailResponse detalle = contratoService.obtenerDetalle(c1.getId());
        assertThat(detalle.getHistorial()).isNotEmpty();
        assertThat(detalle.getHistorial().size()).isGreaterThanOrEqualTo(6);
    }

    @Test
    @DisplayName("5. Detectar solapamiento de contratos vigentes en el mismo puesto")
    void testDetectarSolapamientoContratosVigentes() {
        // Primer contrato formalizado (VIGENTE)
        ContratoCreateRequest req1 = ContratoCreateRequest.builder()
                .asignacionId(asignacion.getId())
                .tipoContratoId(tipoContrato.getId())
                .fechaInicio(LocalDate.now())
                .fechaFin(LocalDate.now().plusMonths(6))
                .build();
        ContratoResponse c1 = contratoService.crearBorrador(req1, "operador");
        contratoService.enviarARevision(c1.getId(), "operador");
        contratoService.aprobar(c1.getId(), ContratoEstadoRequest.builder().motivo("Ok").build(), "autoridad");
        contratoService.formalizar(c1.getId(), ContratoEstadoRequest.builder().motivo("Firmado").build(), "autoridad");

        // Intentar registrar un segundo contrato para las mismas fechas en el mismo puesto
        ContratoCreateRequest req2 = ContratoCreateRequest.builder()
                .asignacionId(asignacion.getId())
                .tipoContratoId(tipoContrato.getId())
                .fechaInicio(LocalDate.now().plusMonths(1))
                .fechaFin(LocalDate.now().plusMonths(4))
                .build();

        // Debe fallar al intentar crear el borrador porque existe contrato vigente superpuesto
        assertThatThrownBy(() -> contratoService.crearBorrador(req2, "operador"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Existe un contrato vigente superpuesto");
    }

    @Test
    @DisplayName("6. Generación de PDF de Contrato con QR y Hash de Integridad")
    void testGenerarPdfContrato() {
        ContratoCreateRequest req = ContratoCreateRequest.builder()
                .asignacionId(asignacion.getId())
                .tipoContratoId(tipoContrato.getId())
                .fechaInicio(LocalDate.now())
                .fechaFin(LocalDate.now().plusMonths(12))
                .build();
        ContratoResponse c = contratoService.crearBorrador(req, "operador");
        contratoService.enviarARevision(c.getId(), "operador");
        contratoService.aprobar(c.getId(), ContratoEstadoRequest.builder().motivo("Ok").build(), "autoridad");
        contratoService.formalizar(c.getId(), ContratoEstadoRequest.builder().motivo("Firmado").build(), "autoridad");

        byte[] pdfBytes = contratoService.generarPdf(c.getId());

        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(500);
        // Verificar firma mágica de archivo PDF (%PDF-)
        assertThat(new String(pdfBytes, 0, 5)).isEqualTo("%PDF-");
    }

    @Test
    @DisplayName("7. Exportación de listado de contratos a CSV y PDF")
    void testExportarContratos() {
        ContratoCreateRequest req = ContratoCreateRequest.builder()
                .asignacionId(asignacion.getId())
                .tipoContratoId(tipoContrato.getId())
                .fechaInicio(LocalDate.now())
                .fechaFin(LocalDate.now().plusMonths(12))
                .build();
        contratoService.crearBorrador(req, "operador");

        byte[] csv = contratoExportService.exportarExcel(null, mercado.getId(), null, null, null, null);
        assertThat(csv).isNotNull();
        assertThat(new String(csv)).contains("CONT-");

        byte[] pdf = contratoExportService.exportarPdf(null, mercado.getId(), null, null, null, null);
        assertThat(pdf).isNotNull();
        assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");
    }
}
