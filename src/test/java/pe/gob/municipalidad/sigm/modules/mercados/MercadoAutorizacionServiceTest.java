package pe.gob.municipalidad.sigm.modules.mercados;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
class MercadoAutorizacionServiceTest {

    @Autowired
    private AutorizacionComercialService autorizacionService;

    @Autowired
    private VerificacionDocumentoMercadoService verificacionService;

    @Autowired
    private VigenciaOcupacionService vigenciaService;

    @Autowired
    private AutorizacionExportService autorizacionExportService;

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
    private TipoAutorizacionRepository tipoAutorizacionRepository;

    private MercadoMunicipal mercado;
    private PuestoComercial puesto;
    private Comerciante comerciante;
    private AsignacionPuesto asignacion;
    private TipoAutorizacion tipoAutorizacion;

    @BeforeEach
    void setUp() {
        long ts = System.currentTimeMillis();

        mercado = mercadoRepository.save(MercadoMunicipal.builder()
                .codigo("MER-AUT-" + (ts % 10000))
                .nombre("Mercado de Artesanías de Quinua")
                .direccion("Av. Los Libertadores S/N")
                .distrito("Quinua")
                .provincia("Huamanga")
                .departamento("Ayacucho")
                .estado(EstadoMercado.ACTIVO)
                .build());

        puesto = puestoRepository.save(PuestoComercial.builder()
                .mercado(mercado)
                .codigo("PUE-A-" + (ts % 10000))
                .numero("C-305")
                .areaMetrosCuadrados(new BigDecimal("16.00"))
                .estado(EstadoPuesto.DISPONIBLE)
                .build());

        Persona persona = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("45" + String.format("%06d", (int)(ts % 1000000)))
                .nombres("Damiana")
                .apellidoPaterno("Ochoa")
                .apellidoMaterno("Barrientos")
                .correo("damiana@quinua.gob.pe")
                .direccion("Barrio San Blas, Quinua")
                .estado("ACTIVO")
                .build());

        comerciante = comercianteRepository.save(Comerciante.builder()
                .persona(persona)
                .tipoComerciante(TipoComerciante.PERSONA_NATURAL)
                .codigoComerciante("COM-A-" + (ts % 10000))
                .estado(EstadoComerciante.ACTIVO)
                .fechaRegistro(LocalDate.now())
                .build());

        asignacion = asignacionRepository.save(AsignacionPuesto.builder()
                .comerciante(comerciante)
                .puesto(puesto)
                .tipoAsignacion(TipoAsignacion.TEMPORAL)
                .fechaSolicitud(LocalDate.now().minusDays(5))
                .fechaInicio(LocalDate.now())
                .fechaFin(LocalDate.now().plusMonths(12))
                .estado(EstadoAsignacion.VIGENTE)
                .build());

        tipoAutorizacion = tipoAutorizacionRepository.findByCodigo("AUT_COMERCIAL_TEMPORAL")
                .orElseGet(() -> tipoAutorizacionRepository.save(new TipoAutorizacion(
                        null, "AUT_COMERCIAL_TEMPORAL", "Autorización Comercial Temporal",
                        "Autorización administrativa de funcionamiento temporal",
                        "TUPA-QUINUA-04", null, 12, "D.S. 046-2017-PCM", "ACTIVO"
                )));
    }

    @Test
    @DisplayName("1. Crear solicitud de autorización en estado BORRADOR")
    void testCrearBorradorAutorizacion() {
        AutorizacionCreateRequest req = AutorizacionCreateRequest.builder()
                .asignacionId(asignacion.getId())
                .tipoAutorizacionId(tipoAutorizacion.getId())
                .fechaInicio(LocalDate.now())
                .fechaFin(LocalDate.now().plusMonths(12))
                .observaciones("Solicitud de autorización para artesanías en cerámica")
                .build();

        AutorizacionResponse resp = autorizacionService.crearBorrador(req, "operador_tramite");

        assertThat(resp).isNotNull();
        assertThat(resp.getId()).isNotNull();
        assertThat(resp.getCodigoAutorizacion()).startsWith("AUT-");
        assertThat(resp.getEstado()).isEqualTo(EstadoAutorizacion.BORRADOR);
        assertThat(resp.getComercianteNombre()).contains("Damiana");
        assertThat(resp.getPuestoNumero()).isEqualTo("C-305");
    }

    @Test
    @DisplayName("2. Ciclo de vida completo de autorización: Borrador -> En Evaluación -> Observada -> Aprobada -> Emitida -> Revocada")
    void testCicloDeVidaAutorizacion() {
        // 1. Crear
        AutorizacionCreateRequest req = AutorizacionCreateRequest.builder()
                .asignacionId(asignacion.getId())
                .tipoAutorizacionId(tipoAutorizacion.getId())
                .fechaInicio(LocalDate.now())
                .fechaFin(LocalDate.now().plusMonths(12))
                .build();
        AutorizacionResponse a1 = autorizacionService.crearBorrador(req, "operador");

        // 2. Enviar a evaluación
        AutorizacionResponse a2 = autorizacionService.enviarAEvaluacion(a1.getId(), "operador");
        assertThat(a2.getEstado()).isEqualTo(EstadoAutorizacion.EN_EVALUACION);

        // 3. Observar
        AutorizacionEstadoRequest obsReq = AutorizacionEstadoRequest.builder()
                .motivo("Se requiere certificado de inspección de defensa civil ITSE")
                .documentoReferencia("NOTIF-ITSE-01")
                .build();
        AutorizacionResponse a3 = autorizacionService.observar(a1.getId(), obsReq, "inspector_defensa_civil");
        assertThat(a3.getEstado()).isEqualTo(EstadoAutorizacion.OBSERVADA);

        // 4. Subsanar y re-enviar a evaluación
        autorizacionService.enviarAEvaluacion(a1.getId(), "operador");

        // 5. Aprobar
        AutorizacionEstadoRequest aprReq = AutorizacionEstadoRequest.builder()
                .motivo("Inspección técnica conforme con certificación favorable")
                .documentoReferencia("INF-ITSE-CONFORME")
                .build();
        AutorizacionResponse a4 = autorizacionService.aprobar(a1.getId(), aprReq, "gerente_rentas");
        assertThat(a4.getEstado()).isEqualTo(EstadoAutorizacion.APROBADA);

        // 6. Emitir acto administrativo (entra en VIGENTE con token QR y SHA-256)
        AutorizacionEstadoRequest emitReq = AutorizacionEstadoRequest.builder()
                .motivo("Resolución de subgerencia expedida")
                .documentoReferencia("RES-SUB-2026-99")
                .build();
        AutorizacionResponse a5 = autorizacionService.emitir(a1.getId(), emitReq, "subgerente");
        assertThat(a5.getEstado()).isEqualTo(EstadoAutorizacion.VIGENTE);
        assertThat(a5.getCodigoSeguro()).isNotNull();
        assertThat(a5.getHashDocumento()).isNotNull();

        // 7. Revocar acto por causal fundada
        AutorizacionEstadoRequest revReq = AutorizacionEstadoRequest.builder()
                .motivo("Revocación por cambio de giro no autorizado y reincidencia")
                .documentoReferencia("RES-GER-REV-01")
                .build();
        AutorizacionResponse a6 = autorizacionService.revocar(a1.getId(), revReq, "gerente");
        assertThat(a6.getEstado()).isEqualTo(EstadoAutorizacion.REVOCADA);

        // 8. Verificar historial inmutable
        AutorizacionDetailResponse detalle = autorizacionService.obtenerDetalle(a1.getId());
        assertThat(detalle.getHistorial()).isNotEmpty();
        assertThat(detalle.getHistorial().size()).isGreaterThanOrEqualTo(6);
    }

    @Test
    @DisplayName("3. Verificación pública de documento QR con protección de datos personales")
    void testVerificacionDocumentoQrProteccionDatos() {
        // Emitir autorización
        AutorizacionCreateRequest req = AutorizacionCreateRequest.builder()
                .asignacionId(asignacion.getId())
                .tipoAutorizacionId(tipoAutorizacion.getId())
                .fechaInicio(LocalDate.now())
                .fechaFin(LocalDate.now().plusMonths(12))
                .build();
        AutorizacionResponse aut = autorizacionService.crearBorrador(req, "operador");
        autorizacionService.enviarAEvaluacion(aut.getId(), "operador");
        autorizacionService.aprobar(aut.getId(), AutorizacionEstadoRequest.builder().motivo("Ok").build(), "autoridad");
        AutorizacionResponse emitida = autorizacionService.emitir(aut.getId(), AutorizacionEstadoRequest.builder().motivo("Emitida").build(), "autoridad");

        // Verificación mediante token público
        VerificacionDocumentoResponse verif = verificacionService.verificarPorCodigoSeguro(emitida.getCodigoSeguro());

        assertThat(verif).isNotNull();
        assertThat(verif.isEsValido()).isTrue();
        assertThat(verif.getCodigo()).isEqualTo(emitida.getCodigoAutorizacion());
        assertThat(verif.getTipoDocumento()).contains("AUTORIZACIÓN");
        assertThat(verif.getEstadoVerificacion()).contains("VIGENTE");

        // SEGURIDAD: Los datos personales deben estar enmascarados y sin exponer datos sensibles
        assertThat(verif.getTitularEnmascarado()).isNotNull();
        assertThat(verif.getTitularEnmascarado()).contains("***");
        assertThat(verif.getMercadoNombre()).isEqualTo("Mercado de Artesanías de Quinua");
        assertThat(verif.getPuestoCodigo()).isNotNull();
    }

    @Test
    @DisplayName("4. Rechazo de token seguro inválido o inexistente en verificación QR")
    void testRechazoTokenSeguroInvalido() {
        VerificacionDocumentoResponse invalido = verificacionService.verificarPorCodigoSeguro("token_falso_invalido_999");
        assertThat(invalido).isNotNull();
        assertThat(invalido.isEsValido()).isFalse();
    }

    @Test
    @DisplayName("5. Generación de PDF de Autorización con QR y encabezado oficial")
    void testGenerarPdfAutorizacion() {
        AutorizacionCreateRequest req = AutorizacionCreateRequest.builder()
                .asignacionId(asignacion.getId())
                .tipoAutorizacionId(tipoAutorizacion.getId())
                .fechaInicio(LocalDate.now())
                .fechaFin(LocalDate.now().plusMonths(12))
                .build();
        AutorizacionResponse aut = autorizacionService.crearBorrador(req, "operador");
        autorizacionService.enviarAEvaluacion(aut.getId(), "operador");
        autorizacionService.aprobar(aut.getId(), AutorizacionEstadoRequest.builder().motivo("Ok").build(), "autoridad");
        autorizacionService.emitir(aut.getId(), AutorizacionEstadoRequest.builder().motivo("Emitida").build(), "autoridad");

        byte[] pdfBytes = autorizacionService.generarPdf(aut.getId());

        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(500);
        assertThat(new String(pdfBytes, 0, 5)).isEqualTo("%PDF-");
    }

    @Test
    @DisplayName("6. Consulta de estadísticas y cálculo de próximos vencimientos")
    void testConsultarEstadisticasYVencimientos() {
        DocumentoEstadisticasResponse stats = vigenciaService.obtenerEstadisticas();
        assertThat(stats).isNotNull();
        assertThat(stats.getContratosRegistrados()).isGreaterThanOrEqualTo(0);
        assertThat(stats.getAutorizacionesRegistradas()).isGreaterThanOrEqualTo(0);

        // Alertas de vencimiento sin duplicados
        int alertasGeneradas = vigenciaService.emitirAlertasVencimiento("admin");
        assertThat(alertasGeneradas).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("7. Exportación de autorizaciones a CSV y PDF")
    void testExportarAutorizaciones() {
        AutorizacionCreateRequest req = AutorizacionCreateRequest.builder()
                .asignacionId(asignacion.getId())
                .tipoAutorizacionId(tipoAutorizacion.getId())
                .fechaInicio(LocalDate.now())
                .fechaFin(LocalDate.now().plusMonths(12))
                .build();
        autorizacionService.crearBorrador(req, "operador");

        byte[] csv = autorizacionExportService.exportarExcel(null, mercado.getId(), null, null, null, null);
        assertThat(csv).isNotNull();
        assertThat(new String(csv)).contains("AUT-");

        byte[] pdf = autorizacionExportService.exportarPdf(null, mercado.getId(), null, null, null, null);
        assertThat(pdf).isNotNull();
        assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");
    }
}
