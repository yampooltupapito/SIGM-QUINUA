package pe.gob.municipalidad.sigm.modules.recaudaciontributaria;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pe.gob.municipalidad.sigm.common.constants.SecurityConstants;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.documentopago.AnularDocumentoRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.documentopago.DocumentoPagoResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.documentopago.EmitirDocumentoRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.documentopago.VerificacionPublicaDocumentoResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ConceptoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ObligacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.PeriodoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoConceptoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoPeriodoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.PeriodicidadConcepto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoConceptoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoDeterminacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ContribuyenteTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.PagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoContribuyente;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoDocumentoPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.MedioPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoContribuyente;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ConceptoTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ContribuyenteTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ObligacionTributariaRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.PagoTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.PeriodoTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.DocumentoPagoTributarioService;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DocumentoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DocumentoPagoTributarioService documentoService;

    @Autowired
    private PagoTributarioRepository pagoRepository;

    @Autowired
    private ContribuyenteTributarioRepository contribuyenteRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PeriodoTributarioRepository periodoRepository;

    @Autowired
    private ConceptoTributarioRepository conceptoRepository;

    @Autowired
    private ObligacionTributariaRepository obligacionRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Cookie authCookie;
    private CustomUserDetails userDetails;
    private Usuario testUser;
    private ContribuyenteTributario testContribuyente;
    private PagoTributario testPagoConfirmado;

    @BeforeEach
    void setUp() {
        long suffix = System.currentTimeMillis() % 100000000;
        Persona persona = Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento(String.format("%08d", suffix))
                .nombres("Cajero")
                .apellidoPaterno("Oficial")
                .apellidoMaterno("Quinua")
                .build();
        persona = personaRepository.save(persona);

        testUser = Usuario.builder()
                .username("cajero_doc_" + suffix)
                .passwordHash("$2a$10$abcdefghijklmnopqrstuvwxyz123456")
                .persona(persona)
                .enabled(true)
                .build();
        testUser = usuarioRepository.save(testUser);

        userDetails = CustomUserDetails.builder()
                .id(testUser.getId())
                .username(testUser.getUsername())
                .password("dummy")
                .enabled(true)
                .accountNonLocked(true)
                .roles(Set.of("ROLE_ADMIN", "ROLE_CAJERO", "ROLE_TRIBUTACION"))
                .permisos(Set.of(
                        "TRIBUTARIA_DOCUMENTOS_VER",
                        "TRIBUTARIA_DOCUMENTOS_EMITIR",
                        "TRIBUTARIA_DOCUMENTOS_DESCARGAR",
                        "TRIBUTARIA_DOCUMENTOS_REIMPRIMIR",
                        "TRIBUTARIA_DOCUMENTOS_ANULAR",
                        "TRIBUTARIA_DOCUMENTOS_SUSTITUIR",
                        "TRIBUTARIA_PAGOS_VER"
                ))
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("ROLE_CAJERO"),
                        new SimpleGrantedAuthority("TRIBUTARIA_DOCUMENTOS_VER"),
                        new SimpleGrantedAuthority("TRIBUTARIA_DOCUMENTOS_EMITIR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_DOCUMENTOS_DESCARGAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_DOCUMENTOS_REIMPRIMIR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_DOCUMENTOS_ANULAR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_DOCUMENTOS_SUSTITUIR"),
                        new SimpleGrantedAuthority("TRIBUTARIA_PAGOS_VER")
                ))
                .build();

        String token = jwtTokenProvider.generateAccessToken(userDetails);
        authCookie = new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, token);
        authCookie.setPath("/");

        testContribuyente = ContribuyenteTributario.builder()
                .codigoContribuyente("CT-DOC-" + suffix)
                .tipoContribuyente(TipoContribuyente.PERSONA_NATURAL)
                .tipoDocumento("DNI")
                .numeroDocumento(String.format("%08d", (suffix + 8888) % 100000000))
                .nombreRazonSocial("Contribuyente Pruebas Emision")
                .estado(EstadoContribuyente.ACTIVO)
                .build();
        testContribuyente = contribuyenteRepository.save(testContribuyente);

        testPagoConfirmado = PagoTributario.builder()
                .codigoPago("PAG-DOC-" + suffix)
                .contribuyente(testContribuyente)
                .importe(new BigDecimal("200.00"))
                .moneda("PEN")
                .medioPago(MedioPagoTributario.EFECTIVO_CAJA)
                .estado(EstadoPagoTributario.CONFIRMADO)
                .fechaHoraPago(OffsetDateTime.now())
                .fechaConfirmacion(OffsetDateTime.now())
                .build();
        testPagoConfirmado = pagoRepository.save(testPagoConfirmado);
    }

    @Test
    @DisplayName("Flujo Completo: Emisión de recibo, descarga PDF, QR, reimpresión, verificación pública y anulación")
    void testFlujoCompletoEmisionYVerificacion() {
        // 1. Emisión de documento
        EmitirDocumentoRequest emitirReq = EmitirDocumentoRequest.builder()
                .pagoId(testPagoConfirmado.getId())
                .tipoDocumentoCodigo("RECIBO_PAGO")
                .idempotencyKey("IDEMP-TEST-" + System.currentTimeMillis())
                .build();

        DocumentoPagoResponse doc = documentoService.emitirDocumento(
                emitirReq, userDetails, "127.0.0.1", "JUnit5 Integration Test");

        assertNotNull(doc);
        assertNotNull(doc.getId());
        assertTrue(doc.getCodigoDocumento().startsWith("REC01-"));
        assertTrue(doc.getCodigoVerificacion().startsWith("VER-"));
        assertNotNull(doc.getHashDocumento());
        assertEquals(64, doc.getHashDocumento().length());
        assertEquals(EstadoDocumentoPagoTributario.EMITIDO, doc.getEstado());
        assertEquals(0, doc.getNumeroReimpresiones());

        // 2. Descarga de contenido PDF
        byte[] pdfBytes = documentoService.obtenerContenidoPdf(
                doc.getId(), userDetails, "127.0.0.1", "JUnit5");
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 500);

        // 3. Obtención de código QR en PNG
        byte[] qrBytes = documentoService.obtenerQrPng(doc.getId());
        assertNotNull(qrBytes);
        assertTrue(qrBytes.length > 100);

        // 4. Registro de reimpresión autorizada
        DocumentoPagoResponse docReimp = documentoService.registrarReimpresion(
                doc.getId(), userDetails, "127.0.0.1", "JUnit5");
        assertEquals(1, docReimp.getNumeroReimpresiones());
        assertEquals(doc.getCodigoDocumento(), docReimp.getCodigoDocumento()); // No altera correlativo

        // 5. Verificación pública ciudadana (con datos personales enmascarados)
        VerificacionPublicaDocumentoResponse verif = documentoService.verificarPublicamente(
                doc.getCodigoVerificacion(), "127.0.0.1", "Browser");
        assertTrue(verif.isExiste());
        assertTrue(verif.isValido());
        assertEquals(doc.getCodigoDocumento(), verif.getCodigoDocumento());
        assertTrue(verif.getContribuyenteEnmascarado().contains("*")); // Protege privacidad
        assertFalse(verif.getContribuyenteEnmascarado().contains("Pruebas Emision")); // No expone completo

        // 6. Anulación formal de documento
        AnularDocumentoRequest anularReq = AnularDocumentoRequest.builder()
                .motivo("Error en imputación de ventanilla solicitado por tesorería")
                .build();
        DocumentoPagoResponse docAnulado = documentoService.anularDocumento(
                doc.getId(), anularReq, userDetails, "127.0.0.1", "JUnit5");
        assertEquals(EstadoDocumentoPagoTributario.ANULADO, docAnulado.getEstado());
        assertEquals(anularReq.getMotivo(), docAnulado.getMotivoAnulacion());

        // 7. Re-verificación pública tras anulación (debe indicar inválido)
        VerificacionPublicaDocumentoResponse verifAnulado = documentoService.verificarPublicamente(
                doc.getCodigoVerificacion(), "127.0.0.1", "Browser");
        assertTrue(verifAnulado.isExiste());
        assertFalse(verifAnulado.isValido());
        assertEquals(EstadoDocumentoPagoTributario.ANULADO, verifAnulado.getEstado());
    }

    @Test
    @DisplayName("MVC: Consulta de bandeja y detalle de documentos tributarios")
    void testMvcBandejaYDetalle() throws Exception {
        // Emitir un documento para consultar
        EmitirDocumentoRequest emitirReq = EmitirDocumentoRequest.builder()
                .pagoId(testPagoConfirmado.getId())
                .tipoDocumentoCodigo("RECIBO_PAGO")
                .build();
        DocumentoPagoResponse doc = documentoService.emitirDocumento(
                emitirReq, userDetails, "127.0.0.1", "JUnit5");

        // 1. GET /dashboard/tributaria/documentos
        mockMvc.perform(get("/dashboard/tributaria/documentos")
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tributaria/documentos/list"))
                .andExpect(model().attributeExists("documentos"))
                .andExpect(model().attributeExists("metricas"));

        // 2. GET /dashboard/tributaria/documentos/{id}
        mockMvc.perform(get("/dashboard/tributaria/documentos/" + doc.getId())
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tributaria/documentos/detail"))
                .andExpect(model().attributeExists("doc"))
                .andExpect(model().attributeExists("historial"));

        // 3. GET /dashboard/tributaria/documentos/{id}/pdf
        mockMvc.perform(get("/dashboard/tributaria/documentos/" + doc.getId() + "/pdf")
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));

        // 4. GET /verificar/documento-tributario/{codigo} (PÚBLICO, sin cookie de sesión)
        mockMvc.perform(get("/verificar/documento-tributario/" + doc.getCodigoVerificacion()))
                .andExpect(status().isOk())
                .andExpect(view().name("portal/verificacion-tributaria"))
                .andExpect(model().attributeExists("resultado"));
    }
}
