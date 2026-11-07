package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import pe.gob.municipalidad.sigm.common.document.repository.DocumentoRepository;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.*;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.SeguimientoDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.impl.*;

import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias - Seguimiento Ciudadano y Verificación Documental (Prompt 17)")
class SeguimientoVerificacionUnitTest {

    @Mock
    private CodigoSeguimientoRepository codigoSeguimientoRepository;
    @Mock
    private VerificacionDocumentalRepository verificacionDocumentalRepository;
    @Mock
    private ConsultaSeguimientoAuditoriaRepository auditoriaRepository;
    @Mock
    private ExpedienteRepository expedienteRepository;
    @Mock
    private ExpedienteHistorialRepository historialRepository;
    @Mock
    private ObservacionExpedienteRepository observacionRepository;
    @Mock
    private SubsanacionExpedienteRepository subsanacionRepository;
    @Mock
    private DecisionExpedienteRepository decisionRepository;
    @Mock
    private ExpedienteDocumentoRepository expedienteDocumentoRepository;
    @Mock
    private DocumentoRepository documentoRepository;
    @Mock
    private pe.gob.municipalidad.sigm.common.document.service.DocumentoService documentoService;
    @Mock
    private UsuarioRepository usuarioRepository;

    private CodigoSeguimientoServiceImpl codigoSeguimientoService;
    private VerificacionDocumentalServiceImpl verificacionDocumentalService;
    private ConsultaPublicaExpedienteServiceImpl consultaPublicaService;
    private SeguimientoCiudadanoServiceImpl seguimientoCiudadanoService;

    private Expediente expedientePrueba;
    private Persona personaCiudadano;
    private Usuario usuarioCiudadano;
    private TipoTramite tipoTramite;
    private CodigoSeguimiento codigoSeguimientoPrueba;

    @BeforeEach
    void setUp() {
        codigoSeguimientoService = new CodigoSeguimientoServiceImpl(codigoSeguimientoRepository, expedienteRepository);
        verificacionDocumentalService = new VerificacionDocumentalServiceImpl(verificacionDocumentalRepository, auditoriaRepository);
        consultaPublicaService = new ConsultaPublicaExpedienteServiceImpl(
                codigoSeguimientoService,
                expedienteRepository,
                historialRepository,
                auditoriaRepository
        );
        seguimientoCiudadanoService = new SeguimientoCiudadanoServiceImpl(
                expedienteRepository,
                historialRepository,
                observacionRepository,
                subsanacionRepository,
                decisionRepository,
                expedienteDocumentoRepository,
                documentoRepository,
                documentoService,
                usuarioRepository,
                codigoSeguimientoService
        );

        personaCiudadano = new Persona();
        personaCiudadano.setId(100L);
        personaCiudadano.setTipoDocumento("DNI");
        personaCiudadano.setNombres("Juan");
        personaCiudadano.setApellidoPaterno("Pérez");
        personaCiudadano.setNumeroDocumento("44556677");
        personaCiudadano.setCorreo("juan.perez@ejemplo.com");

        usuarioCiudadano = new Usuario();
        usuarioCiudadano.setId(10L);
        usuarioCiudadano.setUsername("44556677");
        usuarioCiudadano.setPersona(personaCiudadano);

        tipoTramite = new TipoTramite();
        tipoTramite.setId(1L);
        tipoTramite.setNombre("Constancia de Posesión");

        expedientePrueba = new Expediente();
        expedientePrueba.setId(1L);
        expedientePrueba.setUuid(UUID.randomUUID());
        expedientePrueba.setNumeroExpediente("MPV-2026-000001");
        expedientePrueba.setCodigoSeguimiento("SEG-ABC12345");
        expedientePrueba.setAsunto("Solicitud de Constancia de Posesión");
        expedientePrueba.setDescripcion("Petitorio para acceso a servicios básicos");
        expedientePrueba.setSolicitante(personaCiudadano);
        expedientePrueba.setTipoTramite(tipoTramite);
        expedientePrueba.setEstadoActual(EstadoExpediente.RECIBIDO);
        expedientePrueba.setFechaPresentacion(OffsetDateTime.now().minusDays(1));
        expedientePrueba.setFechaRecepcion(OffsetDateTime.now().minusHours(12));

        codigoSeguimientoPrueba = CodigoSeguimiento.builder()
                .id(1L)
                .expediente(expedientePrueba)
                .codigo("SEG-ABC12345")
                .tokenHash("hash123")
                .estado(EstadoCodigoSeguimiento.ACTIVO)
                .intentosFallidos(0)
                .fechaEmision(OffsetDateTime.now())
                .fechaExpiracion(OffsetDateTime.now().plusMonths(6))
                .build();
    }

    // 1. CÓDIGO SEGUIMIENTO: GENERACIÓN Y FORMATO SEGURO
    @Test
    @DisplayName("Debe generar código de seguimiento con prefijo SEG- y longitud adecuada")
    void testGenerarCodigoSeguimiento() {
        when(codigoSeguimientoRepository.findFirstByExpedienteIdOrderByFechaEmisionDesc(1L)).thenReturn(Optional.empty());
        when(codigoSeguimientoRepository.save(any(CodigoSeguimiento.class))).thenAnswer(inv -> inv.getArgument(0));

        CodigoSeguimiento result = codigoSeguimientoService.generarOObtenerCodigo(expedientePrueba);

        assertNotNull(result);
        assertNotNull(result.getCodigo());
        assertTrue(result.getCodigo().startsWith("SEG-"));
        assertEquals(12, result.getCodigo().length());
        assertEquals(EstadoCodigoSeguimiento.ACTIVO, result.getEstado());
        assertEquals(0, result.getIntentosFallidos());
        verify(codigoSeguimientoRepository, times(1)).save(any(CodigoSeguimiento.class));
    }

    // 2. CÓDIGO SEGUIMIENTO: RECHAZO SI ESTÁ REVOCADO
    @Test
    @DisplayName("Debe lanzar excepción si el código de seguimiento ha sido revocado")
    void testValidarCodigoRevocado() {
        codigoSeguimientoPrueba.setEstado(EstadoCodigoSeguimiento.REVOCADO);
        when(codigoSeguimientoRepository.findByCodigo("SEG-ABC12345"))
                .thenReturn(Optional.of(codigoSeguimientoPrueba));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                codigoSeguimientoService.validarCodigoSeguimiento("SEG-ABC12345", "127.0.0.1")
        );
        assertTrue(ex.getMessage().contains("revocado"));
    }

    // 3. CÓDIGO SEGUIMIENTO: BLOQUEO POR EXCESO DE INTENTOS FALLIDOS
    @Test
    @DisplayName("Debe bloquear código si supera el número máximo de intentos fallidos")
    void testBloqueoPorIntentosFallidos() {
        codigoSeguimientoPrueba.setEstado(EstadoCodigoSeguimiento.BLOQUEADO);
        codigoSeguimientoPrueba.setBloqueadoHasta(OffsetDateTime.now().plusMinutes(30));
        when(codigoSeguimientoRepository.findByCodigo("SEG-ABC12345"))
                .thenReturn(Optional.of(codigoSeguimientoPrueba));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                codigoSeguimientoService.validarCodigoSeguimiento("SEG-ABC12345", "127.0.0.1")
        );
        assertTrue(ex.getMessage().contains("bloqueado"));
    }

    // 4. CONSULTA PÚBLICA: RECHAZO POR RATE LIMIT (LIMITACIÓN DE CONSULTAS)
    @Test
    @DisplayName("Debe lanzar excepción de rate limit si la misma IP realiza más de 30 consultas en 5 minutos")
    void testRateLimitConsultaPublica() {
        when(auditoriaRepository.countByIpAndTipoAndFechaAfter(eq("192.168.1.50"), eq(TipoConsultaSeguimiento.SEGUIMIENTO_EXPEDIENTE), any(OffsetDateTime.class)))
                .thenReturn(35L);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                consultaPublicaService.consultarPublicamente(
                        new ConsultaPublicaRequest("SEG-ABC12345"),
                        "192.168.1.50",
                        "Mozilla/5.0"
                )
        );
        assertTrue(ex.getMessage().contains("límite de consultas"));
    }

    // 5. CONSULTA PÚBLICA: RESPUESTA MÍNIMA Y SEGURA SIN PII
    @Test
    @DisplayName("Debe devolver respuesta pública con datos mínimos y sin exponer PII del ciudadano")
    void testConsultaPublicaExitosa() {
        when(auditoriaRepository.countByIpAndTipoAndFechaAfter(eq("127.0.0.1"), eq(TipoConsultaSeguimiento.SEGUIMIENTO_EXPEDIENTE), any(OffsetDateTime.class)))
                .thenReturn(0L);
        when(codigoSeguimientoRepository.findByCodigo("SEG-ABC12345"))
                .thenReturn(Optional.of(codigoSeguimientoPrueba));
        when(codigoSeguimientoRepository.save(any(CodigoSeguimiento.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(historialRepository.findFirstByExpedienteIdOrderByFechaMovimientoDesc(1L))
                .thenReturn(Optional.empty());

        ConsultaPublicaResponse response = consultaPublicaService.consultarPublicamente(
                new ConsultaPublicaRequest("SEG-ABC12345"),
                "127.0.0.1",
                "Mozilla/5.0"
        );

        assertNotNull(response);
        assertTrue(response.isEncontrado());
        assertEquals("MPV-2026-000001", response.getNumeroExpediente());
        assertEquals("Constancia de Posesión", response.getTipoTramite());
        assertEquals(EstadoExpediente.RECIBIDO, response.getEstado());
        assertEquals(2, response.getEtapaProgreso()); // RECIBIDO -> Etapa 2
        assertEquals(6, response.getEtapas().size());
        verify(auditoriaRepository, times(1)).save(any(ConsultaSeguimientoAuditoria.class));
    }

    // 6. SEGUIMIENTO CIUDADANO: ANTI-IDOR RECHAZA ACCESO A EXPEDIENTE AJENO
    @Test
    @DisplayName("Anti-IDOR: Debe lanzar AccessDeniedException si un ciudadano intenta consultar expediente ajeno")
    void testSeguimientoCiudadanoRechazaIdor() {
        Persona otraPersona = new Persona();
        otraPersona.setId(999L);
        otraPersona.setNumeroDocumento("99999999");

        Usuario otroUsuario = new Usuario();
        otroUsuario.setId(99L);
        otroUsuario.setUsername("99999999");
        otroUsuario.setPersona(otraPersona);

        when(expedienteRepository.findById(1L)).thenReturn(Optional.of(expedientePrueba));
        when(usuarioRepository.findByUsername("99999999")).thenReturn(Optional.of(otroUsuario));

        assertThrows(AccessDeniedException.class, () ->
                seguimientoCiudadanoService.obtenerSeguimientoDetalle(1L, "99999999")
        );
    }

    // 7. SEGUIMIENTO CIUDADANO: ACCESO AUTORIZADO A EXPEDIENTE PROPIO
    @Test
    @DisplayName("Debe retornar detalle integral de seguimiento para el titular autenticado")
    void testSeguimientoCiudadanoTitularExitoso() {
        when(expedienteRepository.findById(1L)).thenReturn(Optional.of(expedientePrueba));
        when(usuarioRepository.findByUsername("44556677")).thenReturn(Optional.of(usuarioCiudadano));
        when(codigoSeguimientoRepository.findFirstByExpedienteIdOrderByFechaEmisionDesc(1L))
                .thenReturn(Optional.of(codigoSeguimientoPrueba));
        when(historialRepository.findByExpedienteIdOrderByFechaMovimientoAsc(1L)).thenReturn(Collections.emptyList());
        when(observacionRepository.findByExpedienteIdAndVisibilidadOrderByFechaRegistroDesc(eq(1L), any(VisibilidadObservacion.class)))
                .thenReturn(Collections.emptyList());
        when(subsanacionRepository.findByExpedienteIdOrderByFechaSubsanacionDesc(1L)).thenReturn(Collections.emptyList());
        when(expedienteDocumentoRepository.findByExpedienteIdWithDocumento(1L)).thenReturn(Collections.emptyList());
        when(decisionRepository.findByExpedienteIdOrderByFechaDecisionDesc(1L)).thenReturn(Collections.emptyList());

        SeguimientoCiudadanoDetalleDto detalle = seguimientoCiudadanoService.obtenerSeguimientoDetalle(1L, "44556677");

        assertNotNull(detalle);
        assertEquals("MPV-2026-000001", detalle.getNumeroExpediente());
        assertEquals("Juan Pérez", detalle.getSolicitanteNombre());
        assertEquals("DNI: 44556677", detalle.getSolicitanteDocumento());
        assertEquals("SEG-ABC12345", detalle.getCodigoSeguimiento());
        assertEquals(2, detalle.getEtapaProgreso());
    }

    // 8. VERIFICACIÓN DOCUMENTAL: DOCUMENTO ACTIVO Y VÁLIDO
    @Test
    @DisplayName("Debe verificar correctamente un documento activo con código seguro")
    void testVerificarDocumentoActivo() {
        VerificacionDocumental doc = VerificacionDocumental.builder()
                .id(1L)
                .codigoVerificacion("VER-12345678")
                .tokenQr("qr-token-123")
                .tipoDocumento("CONSTANCIA_PRESENTACION")
                .numeroDocumento("CONST-MPV-2026-000001")
                .fechaEmision(OffsetDateTime.now())
                .entidadEmisora("Municipalidad Distrital de Quinua")
                .autoridadFirmante("Mesa de Partes Virtual")
                .cargoFirmante("Sistema Institucional")
                .hashSha256("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
                .estadoDocumento(EstadoVerificacionDocumento.VIGENTE)
                .urlVerificacion("/verificar-documento?codigo=VER-12345678")
                .vecesConsultado(0)
                .build();

        when(verificacionDocumentalRepository.findByCodigoVerificacion("VER-12345678"))
                .thenReturn(Optional.of(doc));

        VerificarDocumentoResponse res = verificacionDocumentalService.verificarDocumento(
                "VER-12345678", "127.0.0.1", "Mozilla/5.0"
        );

        assertNotNull(res);
        assertTrue(res.isValido());
        assertEquals("VIGENTE", res.getEstadoDocumento());
        assertEquals("CONSTANCIA_PRESENTACION", res.getTipoDocumento());
        assertEquals("CONST-MPV-2026-000001", res.getNumeroDocumento());
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", res.getHashSha256());
        assertEquals(1, res.getVecesConsultado());
        verify(auditoriaRepository, times(1)).save(any(ConsultaSeguimientoAuditoria.class));
    }

    // 9. VERIFICACIÓN DOCUMENTAL: DOCUMENTO ANULADO
    @Test
    @DisplayName("Debe alertar que el documento se encuentra ANULADO")
    void testVerificarDocumentoAnulado() {
        VerificacionDocumental doc = VerificacionDocumental.builder()
                .id(2L)
                .codigoVerificacion("VER-87654321")
                .tokenQr("qr-token-87654321")
                .tipoDocumento("RESOLUCION")
                .numeroDocumento("RES-001-2026")
                .fechaEmision(OffsetDateTime.now())
                .entidadEmisora("Municipalidad Distrital de Quinua")
                .hashSha256("hash999")
                .estadoDocumento(EstadoVerificacionDocumento.ANULADO)
                .urlVerificacion("/verificar-documento?codigo=VER-87654321")
                .vecesConsultado(1)
                .build();

        when(verificacionDocumentalRepository.findByCodigoVerificacion("VER-87654321"))
                .thenReturn(Optional.of(doc));

        VerificarDocumentoResponse res = verificacionDocumentalService.verificarDocumento(
                "VER-87654321", "127.0.0.1", "Mozilla/5.0"
        );

        assertNotNull(res);
        assertFalse(res.isValido());
        assertEquals("ANULADO", res.getEstadoDocumento());
        assertTrue(res.getMensajeLegal().contains("ANULADO"));
    }

    // 10. VERIFICACIÓN DOCUMENTAL: CÓDIGO INEXISTENTE
    @Test
    @DisplayName("Debe manejar adecuadamente códigos inexistentes sin exponer información sensible")
    void testVerificarDocumentoInexistente() {
        when(verificacionDocumentalRepository.findByCodigoVerificacion("VER-NOEXISTE"))
                .thenReturn(Optional.empty());
        when(verificacionDocumentalRepository.findByTokenQr("VER-NOEXISTE"))
                .thenReturn(Optional.empty());

        VerificarDocumentoResponse res = verificacionDocumentalService.verificarDocumento(
                "VER-NOEXISTE", "127.0.0.1", "Mozilla/5.0"
        );

        assertNotNull(res);
        assertFalse(res.isValido());
        assertEquals("NO_VERIFICABLE", res.getEstadoDocumento());
        assertTrue(res.getMensajeLegal().contains("No se encontró"));
    }
}
