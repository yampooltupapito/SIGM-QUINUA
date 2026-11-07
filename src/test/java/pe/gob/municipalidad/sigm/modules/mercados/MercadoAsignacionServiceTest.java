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
import pe.gob.municipalidad.sigm.common.exception.ConflictException;
import pe.gob.municipalidad.sigm.common.exception.InvalidRequestException;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.*;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;
import pe.gob.municipalidad.sigm.modules.mercados.service.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MercadoAsignacionServiceTest {

    @Autowired
    private AsignacionPuestoService asignacionService;

    @Autowired
    private EvaluacionAsignacionService evaluacionService;

    @Autowired
    private OcupacionPuestoService ocupacionService;

    @Autowired
    private DisponibilidadPuestoService disponibilidadService;

    @Autowired
    private HistorialOcupacionService historialService;

    @Autowired
    private ConstanciaAsignacionService constanciaService;

    @Autowired
    private AsignacionExportService exportService;

    @Autowired
    private AsignacionPuestoRepository asignacionRepository;

    @Autowired
    private PuestoComercialRepository puestoRepository;

    @Autowired
    private MercadoMunicipalRepository mercadoRepository;

    @Autowired
    private ComercianteRepository comercianteRepository;

    @Autowired
    private PersonaRepository personaRepository;

    private MercadoMunicipal mercadoActivo;
    private MercadoMunicipal mercadoInactivo;
    private PuestoComercial puestoLibre;
    private PuestoComercial puestoMantenimiento;
    private Comerciante comercianteActivo;
    private Comerciante comercianteSuspendido;

    @BeforeEach
    void setUp() {
        long timestamp = System.currentTimeMillis();

        // 1. Mercados de prueba
        mercadoActivo = mercadoRepository.save(MercadoMunicipal.builder()
                .codigo("MER-TEST-" + (timestamp % 10000))
                .nombre("Mercado Central de Quinua Test")
                .direccion("Plaza de Armas S/N, Quinua")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .estado(EstadoMercado.ACTIVO)
                .build());

        mercadoInactivo = mercadoRepository.save(MercadoMunicipal.builder()
                .codigo("MER-INAC-" + (timestamp % 10000))
                .nombre("Mercado Clausurado Quinua")
                .direccion("Av. Centenario S/N, Quinua")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .estado(EstadoMercado.INACTIVO)
                .build());

        // 2. Puestos de prueba
        puestoLibre = puestoRepository.save(PuestoComercial.builder()
                .mercado(mercadoActivo)
                .codigo("PUE-DISP-" + (timestamp % 10000))
                .numero("A-101")
                .areaMetrosCuadrados(new BigDecimal("12.50"))
                .estado(EstadoPuesto.DISPONIBLE)
                .build());

        puestoMantenimiento = puestoRepository.save(PuestoComercial.builder()
                .mercado(mercadoActivo)
                .codigo("PUE-MANT-" + (timestamp % 10000))
                .numero("A-102")
                .areaMetrosCuadrados(new BigDecimal("10.00"))
                .estado(EstadoPuesto.EN_MANTENIMIENTO)
                .build());

        // 3. Comerciantes de prueba
        String dni1 = "4012" + String.format("%04d", (int)(timestamp % 10000));
        Persona p1 = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento(dni1)
                .nombres("Anastacio")
                .apellidoPaterno("Condori")
                .apellidoMaterno("Yucra")
                .correo("anastacio@quinua.gob.pe")
                .direccion("Jr. Sucre 45, Quinua")
                .estado("ACTIVO")
                .build());

        comercianteActivo = comercianteRepository.save(Comerciante.builder()
                .persona(p1)
                .tipoComerciante(TipoComerciante.PERSONA_NATURAL)
                .codigoComerciante("COM-ASIG-01-" + (timestamp % 10000))
                .estado(EstadoComerciante.ACTIVO)
                .fechaRegistro(LocalDate.now())
                .build());

        String dni2 = "4099" + String.format("%04d", (int)(timestamp % 10000));
        Persona p2 = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento(dni2)
                .nombres("Lorenzo")
                .apellidoPaterno("Palomino")
                .apellidoMaterno("Soto")
                .correo("lorenzo@quinua.gob.pe")
                .direccion("Barrio San Pedro, Quinua")
                .estado("ACTIVO")
                .build());

        comercianteSuspendido = comercianteRepository.save(Comerciante.builder()
                .persona(p2)
                .tipoComerciante(TipoComerciante.PERSONA_NATURAL)
                .codigoComerciante("COM-ASIG-02-" + (timestamp % 10000))
                .estado(EstadoComerciante.SUSPENDIDO)
                .fechaRegistro(LocalDate.now())
                .build());
    }

    @Test
    @DisplayName("01. Crear borrador de solicitud de asignación")
    void test01_crearBorrador_solicitudAsignacion() {
        AsignacionCreateRequest request = AsignacionCreateRequest.builder()
                .puestoId(puestoLibre.getId())
                .comercianteId(comercianteActivo.getId())
                .fechaInicio(LocalDate.now().plusDays(1))
                .fechaFin(LocalDate.now().plusMonths(6))
                .tipoAsignacion(TipoAsignacion.REGULAR)
                .motivo("Venta de productos agrícolas tradicionales")
                .presentarInmediatamente(false)
                .build();

        AsignacionDetailResponse response = asignacionService.registrarSolicitud(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getCodigoAsignacion()).startsWith("ASI-");
        assertThat(response.getEstado()).isEqualTo(EstadoAsignacion.BORRADOR);
        assertThat(response.getPuestoCodigo()).isEqualTo(puestoLibre.getCodigo());
        assertThat(response.getTitularNombre()).contains("Anastacio Condori");
    }

    @Test
    @DisplayName("02. Presentar solicitud de asignación pasando a SOLICITADA")
    void test02_presentarSolicitud_pasaASolicitada() {
        AsignacionCreateRequest request = AsignacionCreateRequest.builder()
                .puestoId(puestoLibre.getId())
                .comercianteId(comercianteActivo.getId())
                .fechaInicio(LocalDate.now().plusDays(1))
                .presentarInmediatamente(false)
                .build();

        AsignacionDetailResponse borrador = asignacionService.registrarSolicitud(request);
        AsignacionDetailResponse presentada = asignacionService.presentarSolicitud(borrador.getId());

        assertThat(presentada.getEstado()).isEqualTo(EstadoAsignacion.SOLICITADA);
    }

    @Test
    @DisplayName("03. Validar mercado inactivo rechaza solicitud de asignación")
    void test03_validarMercadoInactivo_rechazaSolicitud() {
        PuestoComercial puestoMercadoInactivo = puestoRepository.save(PuestoComercial.builder()
                .mercado(mercadoInactivo)
                .codigo("PUE-INAC-01")
                .numero("B-01")
                .estado(EstadoPuesto.DISPONIBLE)
                .build());

        AsignacionCreateRequest request = AsignacionCreateRequest.builder()
                .puestoId(puestoMercadoInactivo.getId())
                .comercianteId(comercianteActivo.getId())
                .fechaInicio(LocalDate.now())
                .build();

        assertThatThrownBy(() -> asignacionService.registrarSolicitud(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("No se pueden registrar asignaciones en un mercado que no esté activo");
    }

    @Test
    @DisplayName("04. Validar comerciante no activo rechaza solicitud de asignación")
    void test04_validarComercianteInactivo_rechazaSolicitud() {
        AsignacionCreateRequest request = AsignacionCreateRequest.builder()
                .puestoId(puestoLibre.getId())
                .comercianteId(comercianteSuspendido.getId())
                .fechaInicio(LocalDate.now())
                .build();

        assertThatThrownBy(() -> asignacionService.registrarSolicitud(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no se encuentra en estado ACTIVO");
    }

    @Test
    @DisplayName("05. Consultar disponibilidad de puesto comercial libre")
    void test05_consultarDisponibilidad_puestoLibre() {
        DisponibilidadPuestoResponse disp = disponibilidadService.consultarDisponibilidad(puestoLibre.getId());

        assertThat(disp).isNotNull();
        assertThat(disp.isDisponible()).isTrue();
        assertThat(disp.getPuestoEstado()).isEqualTo("DISPONIBLE");
    }

    @Test
    @DisplayName("06. Rechazar puesto en mantenimiento o no disponible físicamente")
    void test06_rechazarPuestoEnMantenimiento() {
        DisponibilidadPuestoResponse disp = disponibilidadService.consultarDisponibilidad(puestoMantenimiento.getId());

        assertThat(disp.isDisponible()).isFalse();
        assertThat(disp.getMotivoNoDisponible()).contains("MANTENIMIENTO");
    }

    @Test
    @DisplayName("07. Evaluar solicitud y pasar a EN_EVALUACION")
    void test07_evaluarSolicitud_pasaAEnEvaluacion() {
        AsignacionCreateRequest request = AsignacionCreateRequest.builder()
                .puestoId(puestoLibre.getId())
                .comercianteId(comercianteActivo.getId())
                .fechaInicio(LocalDate.now())
                .presentarInmediatamente(true)
                .build();

        AsignacionDetailResponse asig = asignacionService.registrarSolicitud(request);

        AsignacionEvaluacionRequest evalReq = AsignacionEvaluacionRequest.builder()
                .resultado("CONFORME")
                .motivo("Expediente técnico cumple con los requisitos del TUPA municipal")
                .build();

        AsignacionDetailResponse evaluada = evaluacionService.evaluarSolicitud(asig.getId(), evalReq);

        assertThat(evaluada.getEstado()).isEqualTo(EstadoAsignacion.EN_EVALUACION);
        assertThat(evaluada.getEvaluaciones()).isNotEmpty();
        assertThat(evaluada.getEvaluaciones().get(0).getResultado()).isEqualTo("CONFORME");
    }

    @Test
    @DisplayName("08. Formular observación administrativa pasando a OBSERVADA")
    void test08_formularObservacion_pasaAObservada() {
        AsignacionCreateRequest request = AsignacionCreateRequest.builder()
                .puestoId(puestoLibre.getId())
                .comercianteId(comercianteActivo.getId())
                .fechaInicio(LocalDate.now())
                .presentarInmediatamente(true)
                .build();

        AsignacionDetailResponse asig = asignacionService.registrarSolicitud(request);

        AsignacionObservacionRequest obsReq = AsignacionObservacionRequest.builder()
                .detalleObservacion("Falta adjuntar carné sanitario expedido por el Centro de Salud")
                .build();

        AsignacionDetailResponse observada = evaluacionService.formularObservacion(asig.getId(), obsReq);

        assertThat(observada.getEstado()).isEqualTo(EstadoAsignacion.OBSERVADA);
    }

    @Test
    @DisplayName("09. Subsanar observación administrativa retornando a EN_EVALUACION")
    void test09_subsanarObservacion_pasaAEnEvaluacion() {
        AsignacionCreateRequest request = AsignacionCreateRequest.builder()
                .puestoId(puestoLibre.getId())
                .comercianteId(comercianteActivo.getId())
                .fechaInicio(LocalDate.now())
                .presentarInmediatamente(true)
                .build();

        AsignacionDetailResponse asig = asignacionService.registrarSolicitud(request);
        evaluacionService.formularObservacion(asig.getId(), AsignacionObservacionRequest.builder()
                .detalleObservacion("Falta certificado")
                .build());

        AsignacionDetailResponse subsanada = evaluacionService.subsanarObservacion(asig.getId(), "Se adjunta certificado solicitado", null);

        assertThat(subsanada.getEstado()).isEqualTo(EstadoAsignacion.EN_EVALUACION);
    }

    @Test
    @DisplayName("10. Aprobar solicitud formalmente pasando a APROBADA")
    void test10_aprobarSolicitud_pasaAAprobada() {
        AsignacionCreateRequest request = AsignacionCreateRequest.builder()
                .puestoId(puestoLibre.getId())
                .comercianteId(comercianteActivo.getId())
                .fechaInicio(LocalDate.now())
                .presentarInmediatamente(true)
                .build();

        AsignacionDetailResponse asig = asignacionService.registrarSolicitud(request);
        evaluacionService.evaluarSolicitud(asig.getId(), AsignacionEvaluacionRequest.builder()
                .resultado("CONFORME")
                .motivo("Evaluación favorable")
                .build());

        AsignacionAprobacionRequest aprobReq = AsignacionAprobacionRequest.builder()
                .sustentoAprobacion("Resolución de Gerencia Municipal N° 120-2026-MDQ")
                .fechaInicioVigencia(LocalDate.now())
                .fechaFinVigencia(LocalDate.now().plusMonths(12))
                .activarInmediatamente(false)
                .build();

        AsignacionDetailResponse aprobada = evaluacionService.aprobarSolicitud(asig.getId(), aprobReq);

        assertThat(aprobada.getEstado()).isEqualTo(EstadoAsignacion.APROBADA);
        assertThat(aprobada.getMotivo()).contains("Resolución de Gerencia");
    }

    @Test
    @DisplayName("11. Rechazar solicitud administrativamente pasando a RECHAZADA")
    void test11_rechazarSolicitud_pasaARechazada() {
        AsignacionCreateRequest request = AsignacionCreateRequest.builder()
                .puestoId(puestoLibre.getId())
                .comercianteId(comercianteActivo.getId())
                .fechaInicio(LocalDate.now())
                .presentarInmediatamente(true)
                .build();

        AsignacionDetailResponse asig = asignacionService.registrarSolicitud(request);

        AsignacionRechazoRequest rechReq = AsignacionRechazoRequest.builder()
                .motivoRechazo("Incompatibilidad de giro comercial con el reglamento de zonificación")
                .build();

        AsignacionDetailResponse rechazada = evaluacionService.rechazarSolicitud(asig.getId(), rechReq);

        assertThat(rechazada.getEstado()).isEqualTo(EstadoAsignacion.RECHAZADA);
        assertThat(rechazada.getMotivoRechazo()).contains("Incompatibilidad");
    }

    @Test
    @DisplayName("12. Activar ocupación efectiva pasando a VIGENTE y puesto a OCUPADO")
    void test12_activarOcupacion_pasaAVigenteYPuestoAOccupado() {
        AsignacionCreateRequest request = AsignacionCreateRequest.builder()
                .puestoId(puestoLibre.getId())
                .comercianteId(comercianteActivo.getId())
                .fechaInicio(LocalDate.now())
                .presentarInmediatamente(true)
                .build();

        AsignacionDetailResponse asig = asignacionService.registrarSolicitud(request);
        evaluacionService.evaluarSolicitud(asig.getId(), AsignacionEvaluacionRequest.builder().resultado("CONFORME").motivo("OK").build());
        evaluacionService.aprobarSolicitud(asig.getId(), AsignacionAprobacionRequest.builder().sustentoAprobacion("Res 01").build());

        AsignacionDetailResponse activa = ocupacionService.activarOcupacion(asig.getId());

        assertThat(activa.getEstado()).isEqualTo(EstadoAsignacion.VIGENTE);

        // Verificar que el puesto cambió a OCUPADO
        PuestoComercial puestoActual = puestoRepository.findById(puestoLibre.getId()).orElseThrow();
        assertThat(puestoActual.getEstado()).isEqualTo(EstadoPuesto.OCUPADO);
    }

    @Test
    @DisplayName("13. Rechazar doble asignación simultánea e incompatible en el mismo puesto")
    void test13_rechazarDobleAsignacionSimultanea() {
        // Primera asignación vigente
        AsignacionCreateRequest r1 = AsignacionCreateRequest.builder()
                .puestoId(puestoLibre.getId())
                .comercianteId(comercianteActivo.getId())
                .fechaInicio(LocalDate.now())
                .presentarInmediatamente(true)
                .build();
        AsignacionDetailResponse a1 = asignacionService.registrarSolicitud(r1);
        evaluacionService.evaluarSolicitud(a1.getId(), AsignacionEvaluacionRequest.builder().resultado("OK").motivo("OK").build());
        evaluacionService.aprobarSolicitud(a1.getId(), AsignacionAprobacionRequest.builder().sustentoAprobacion("Res").build());
        ocupacionService.activarOcupacion(a1.getId());

        // Segunda solicitud para el mismo puesto
        String dni3 = "4088" + (System.currentTimeMillis() % 10000);
        Persona p3 = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento(dni3)
                .nombres("Simeón")
                .apellidoPaterno("Huamán")
                .apellidoMaterno("Prado")
                .estado("ACTIVO")
                .build());
        Comerciante otroComerciante = comercianteRepository.save(Comerciante.builder()
                .persona(p3)
                .tipoComerciante(TipoComerciante.PERSONA_NATURAL)
                .codigoComerciante("COM-OTRO-" + (System.currentTimeMillis() % 10000))
                .estado(EstadoComerciante.ACTIVO)
                .fechaRegistro(LocalDate.now())
                .build());

        AsignacionCreateRequest r2 = AsignacionCreateRequest.builder()
                .puestoId(puestoLibre.getId())
                .comercianteId(otroComerciante.getId())
                .fechaInicio(LocalDate.now())
                .presentarInmediatamente(true)
                .build();
        AsignacionDetailResponse a2 = asignacionService.registrarSolicitud(r2);
        evaluacionService.evaluarSolicitud(a2.getId(), AsignacionEvaluacionRequest.builder().resultado("OK").motivo("OK").build());
        evaluacionService.aprobarSolicitud(a2.getId(), AsignacionAprobacionRequest.builder().sustentoAprobacion("Res2").build());

        // Al intentar activar la segunda sobre el mismo puesto, debe lanzar ConflictException
        assertThatThrownBy(() -> ocupacionService.activarOcupacion(a2.getId()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("ya cuenta con otra asignación VIGENTE activa");
    }

    @Test
    @DisplayName("14. Suspender y reanudar asignación comercial")
    void test14_suspenderYReanudarAsignacion() {
        AsignacionCreateRequest r1 = AsignacionCreateRequest.builder()
                .puestoId(puestoLibre.getId())
                .comercianteId(comercianteActivo.getId())
                .fechaInicio(LocalDate.now())
                .presentarInmediatamente(true)
                .build();
        AsignacionDetailResponse a1 = asignacionService.registrarSolicitud(r1);
        evaluacionService.evaluarSolicitud(a1.getId(), AsignacionEvaluacionRequest.builder().resultado("OK").motivo("OK").build());
        evaluacionService.aprobarSolicitud(a1.getId(), AsignacionAprobacionRequest.builder().sustentoAprobacion("Res").build());
        ocupacionService.activarOcupacion(a1.getId());

        // Suspensión
        AsignacionSuspensionRequest suspReq = AsignacionSuspensionRequest.builder()
                .motivoSuspension("Mora temporal de arbitrios")
                .mantieneReservaPuesto(true)
                .build();
        AsignacionDetailResponse suspendida = ocupacionService.suspenderAsignacion(a1.getId(), suspReq);
        assertThat(suspendida.getEstado()).isEqualTo(EstadoAsignacion.SUSPENDIDA);

        // Reanudación
        AsignacionDetailResponse reanudada = ocupacionService.reanudarAsignacion(a1.getId(), "Pago regularizado");
        assertThat(reanudada.getEstado()).isEqualTo(EstadoAsignacion.VIGENTE);
    }

    @Test
    @DisplayName("15. Finalizar asignación comercial liberando el puesto comercial")
    void test15_finalizarAsignacion_liberaPuesto() {
        AsignacionCreateRequest r1 = AsignacionCreateRequest.builder()
                .puestoId(puestoLibre.getId())
                .comercianteId(comercianteActivo.getId())
                .fechaInicio(LocalDate.now())
                .presentarInmediatamente(true)
                .build();
        AsignacionDetailResponse a1 = asignacionService.registrarSolicitud(r1);
        evaluacionService.evaluarSolicitud(a1.getId(), AsignacionEvaluacionRequest.builder().resultado("OK").motivo("OK").build());
        evaluacionService.aprobarSolicitud(a1.getId(), AsignacionAprobacionRequest.builder().sustentoAprobacion("Res").build());
        ocupacionService.activarOcupacion(a1.getId());

        // Finalización
        AsignacionFinalizacionRequest finReq = AsignacionFinalizacionRequest.builder()
                .motivoFinalizacion("Vencimiento regular del periodo")
                .registrarDevolucionInmediata(false)
                .build();
        AsignacionDetailResponse fin = ocupacionService.finalizarAsignacion(a1.getId(), finReq);
        assertThat(fin.getEstado()).isEqualTo(EstadoAsignacion.FINALIZADA);

        PuestoComercial puesto = puestoRepository.findById(puestoLibre.getId()).orElseThrow();
        assertThat(puesto.getEstado()).isEqualTo(EstadoPuesto.DISPONIBLE);
    }

    @Test
    @DisplayName("16. Registrar devolución física en mal estado cambia puesto a MANTENIMIENTO")
    void test16_registrarDevolucionFisica_maloPasaAMantenimiento() {
        AsignacionCreateRequest r1 = AsignacionCreateRequest.builder()
                .puestoId(puestoLibre.getId())
                .comercianteId(comercianteActivo.getId())
                .fechaInicio(LocalDate.now())
                .presentarInmediatamente(true)
                .build();
        AsignacionDetailResponse a1 = asignacionService.registrarSolicitud(r1);
        evaluacionService.evaluarSolicitud(a1.getId(), AsignacionEvaluacionRequest.builder().resultado("OK").motivo("OK").build());
        evaluacionService.aprobarSolicitud(a1.getId(), AsignacionAprobacionRequest.builder().sustentoAprobacion("Res").build());
        ocupacionService.activarOcupacion(a1.getId());
        ocupacionService.finalizarAsignacion(a1.getId(), AsignacionFinalizacionRequest.builder().motivoFinalizacion("Cierre").build());

        // Registrar devolución en mal estado
        DevolucionPuestoRequest devReq = DevolucionPuestoRequest.builder()
                .estadoFisicoPuesto("REQUIERE_MANTENIMIENTO")
                .observaciones("Falla en cableado y persiana metálica trabada")
                .build();
        DevolucionPuestoResponse devResp = ocupacionService.registrarDevolucion(a1.getId(), devReq);

        assertThat(devResp).isNotNull();
        assertThat(devResp.getEstadoFisicoPuesto()).isEqualTo("REQUIERE_MANTENIMIENTO");

        PuestoComercial puesto = puestoRepository.findById(puestoLibre.getId()).orElseThrow();
        assertThat(puesto.getEstado()).isEqualTo(EstadoPuesto.EN_MANTENIMIENTO);
    }

    @Test
    @DisplayName("17. Trazabilidad inmutable de movimientos en HistorialOcupacion")
    void test17_trazabilidadHistorialOcupacion() {
        AsignacionCreateRequest r1 = AsignacionCreateRequest.builder()
                .puestoId(puestoLibre.getId())
                .comercianteId(comercianteActivo.getId())
                .fechaInicio(LocalDate.now())
                .presentarInmediatamente(true)
                .build();
        AsignacionDetailResponse a1 = asignacionService.registrarSolicitud(r1);

        List<HistorialOcupacionResponse> hist = historialService.obtenerHistorialPorAsignacion(a1.getId());
        assertThat(hist).isNotEmpty();
        assertThat(hist.get(0).getPuestoCodigo()).isEqualTo(puestoLibre.getCodigo());
        assertThat(hist.get(0).getTipoMovimiento()).isEqualTo("SOLICITUD");
    }

    @Test
    @DisplayName("18. Validar rechazo de fecha fin anterior a fecha inicio")
    void test18_validarFechas_fechaFinInvalida() {
        AsignacionCreateRequest request = AsignacionCreateRequest.builder()
                .puestoId(puestoLibre.getId())
                .comercianteId(comercianteActivo.getId())
                .fechaInicio(LocalDate.now().plusMonths(2))
                .fechaFin(LocalDate.now()) // Anterior a fecha inicio
                .build();

        assertThatThrownBy(() -> asignacionService.registrarSolicitud(request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("no puede ser anterior");
    }

    @Test
    @DisplayName("19. Generar constancia oficial en PDF con código QR")
    void test19_generarConstanciaPdf_conCodigoQR() {
        AsignacionCreateRequest r1 = AsignacionCreateRequest.builder()
                .puestoId(puestoLibre.getId())
                .comercianteId(comercianteActivo.getId())
                .fechaInicio(LocalDate.now())
                .presentarInmediatamente(true)
                .build();
        AsignacionDetailResponse a1 = asignacionService.registrarSolicitud(r1);
        evaluacionService.evaluarSolicitud(a1.getId(), AsignacionEvaluacionRequest.builder().resultado("OK").motivo("OK").build());
        evaluacionService.aprobarSolicitud(a1.getId(), AsignacionAprobacionRequest.builder().sustentoAprobacion("Res").build());

        byte[] pdfBytes = constanciaService.generarConstanciaPdf(a1.getId());

        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(100);
        // Validar encabezado PDF "%PDF"
        String header = new String(pdfBytes, 0, 4);
        assertThat(header).isEqualTo("%PDF");
    }

    @Test
    @DisplayName("20. Estadísticas de asignaciones y ocupación en base de datos")
    void test20_estadisticas_calculaTotalesCorrectos() {
        AsignacionEstadisticasResponse stats = asignacionService.obtenerEstadisticas();

        assertThat(stats).isNotNull();
        assertThat(stats.getPuestosTotales()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("21. Exportar Excel sanitizando inyección de fórmulas")
    void test21_exportarExcel_sanitizaInyeccionFormulas() {
        AsignacionCreateRequest r1 = AsignacionCreateRequest.builder()
                .puestoId(puestoLibre.getId())
                .comercianteId(comercianteActivo.getId())
                .fechaInicio(LocalDate.now())
                .motivo("=cmd|'/C calc'!A0") // Posible payload de inyección
                .presentarInmediatamente(true)
                .build();
        asignacionService.registrarSolicitud(r1);

        byte[] csvBytes = exportService.exportarExcel(new AsignacionFilter());
        String csvContent = new String(csvBytes);

        assertThat(csvContent).contains("Código Asignación");
        // Debe haber sido sanitizado con apóstrofe inicial
        assertThat(csvContent).doesNotContain("\"=cmd");
        assertThat(csvContent).contains("\"'=cmd");
    }

    @Test
    @DisplayName("22. Exportar PDF del padrón de asignaciones")
    void test22_exportarPdf_generaBytesValidos() {
        byte[] pdfBytes = exportService.exportarPdf(new AsignacionFilter());

        assertThat(pdfBytes).isNotNull();
        assertThat(new String(pdfBytes, 0, 4)).isEqualTo("%PDF");
    }

    @Test
    @DisplayName("23. Anular asignación y verificar liberación de puesto")
    void test23_anularAsignacion_liberaPuesto() {
        AsignacionCreateRequest r1 = AsignacionCreateRequest.builder()
                .puestoId(puestoLibre.getId())
                .comercianteId(comercianteActivo.getId())
                .fechaInicio(LocalDate.now())
                .presentarInmediatamente(true)
                .build();
        AsignacionDetailResponse a1 = asignacionService.registrarSolicitud(r1);
        evaluacionService.evaluarSolicitud(a1.getId(), AsignacionEvaluacionRequest.builder().resultado("OK").motivo("OK").build());
        evaluacionService.aprobarSolicitud(a1.getId(), AsignacionAprobacionRequest.builder().sustentoAprobacion("Res").build());
        ocupacionService.activarOcupacion(a1.getId());

        AsignacionDetailResponse anulada = asignacionService.anularAsignacion(a1.getId(), "Nulidad de oficio del procedimiento");

        assertThat(anulada.getEstado()).isEqualTo(EstadoAsignacion.ANULADA);
        PuestoComercial puesto = puestoRepository.findById(puestoLibre.getId()).orElseThrow();
        assertThat(puesto.getEstado()).isEqualTo(EstadoPuesto.DISPONIBLE);
    }

    @Test
    @DisplayName("24. Paginación y filtrado de asignaciones")
    void test24_paginacionYFiltrado() {
        AsignacionCreateRequest r1 = AsignacionCreateRequest.builder()
                .puestoId(puestoLibre.getId())
                .comercianteId(comercianteActivo.getId())
                .fechaInicio(LocalDate.now())
                .presentarInmediatamente(false)
                .build();
        asignacionService.registrarSolicitud(r1);

        AsignacionFilter filter = AsignacionFilter.builder()
                .estado(EstadoAsignacion.BORRADOR)
                .build();

        Page<AsignacionResponse> page = asignacionService.listarConFiltros(filter, PageRequest.of(0, 10));

        assertThat(page).isNotNull();
        assertThat(page.getContent()).isNotEmpty();
    }
}
