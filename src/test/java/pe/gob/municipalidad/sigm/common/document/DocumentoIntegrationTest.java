package pe.gob.municipalidad.sigm.common.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.constants.SecurityConstants;
import pe.gob.municipalidad.sigm.common.document.dto.DocumentDtos.EliminarDocumentoRequest;
import pe.gob.municipalidad.sigm.common.document.dto.DocumentDtos.GenerarPdfRequest;
import pe.gob.municipalidad.sigm.common.document.entity.*;
import pe.gob.municipalidad.sigm.common.document.repository.DocumentoAccesoRepository;
import pe.gob.municipalidad.sigm.common.document.repository.DocumentoRepository;
import pe.gob.municipalidad.sigm.common.document.repository.DocumentoVersionRepository;
import pe.gob.municipalidad.sigm.common.document.service.DocumentoService;
import pe.gob.municipalidad.sigm.common.document.storage.DocumentStorageService;
import pe.gob.municipalidad.sigm.common.model.*;
import pe.gob.municipalidad.sigm.common.repository.*;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DocumentoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private OficinaRepository oficinaRepository;

    @Autowired
    private MunicipalidadRepository municipalidadRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private DocumentoRepository documentoRepository;

    @Autowired
    private DocumentoVersionRepository versionRepository;

    @Autowired
    private DocumentoAccesoRepository accesoRepository;

    @Autowired
    private DocumentStorageService storageService;

    @Autowired
    private DocumentoService documentoService;

    private Usuario adminUser;
    private Usuario regularUser;
    private Cookie adminCookie;
    private Cookie regularCookie;
    private Oficina oficinaCentral;

    @BeforeEach
    void setUp() {
        Municipalidad muni = municipalidadRepository.findAll().stream().findFirst().orElseGet(() ->
                municipalidadRepository.save(Municipalidad.builder()
                        .nombre("Municipalidad Distrital de Quinua")
                        .nombreCorto("MDQ")
                        .ubigeo("050108")
                        .departamento("Ayacucho")
                        .provincia("Huamanga")
                        .distrito("Quinua")
                        .build()));

        oficinaCentral = oficinaRepository.findByCodigo("SEC-GEN").orElseGet(() ->
                oficinaRepository.save(Oficina.builder()
                        .municipalidad(muni)
                        .codigo("SEC-GEN")
                        .nombre("Secretaría General")
                        .sigla("SG")
                        .nivelJerarquico(2)
                        .estado("ACTIVO")
                        .build()));

        Rol rolAdmin = rolRepository.findByCodigo("ROLE_ADMIN").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_ADMIN").nombre("ADMINISTRADOR").estado("ACTIVO").build()));

        Rol rolCiudadano = rolRepository.findByCodigo("ROLE_CIUDADANO").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_CIUDADANO").nombre("CIUDADANO").estado("ACTIVO").build()));

        // Usuario Admin
        String adminDoc = "7" + UUID.randomUUID().toString().replaceAll("\\D", "").substring(0, 7);
        Persona pAdmin = personaRepository.save(Persona.builder()
                .numeroDocumento(adminDoc)
                .tipoDocumento("DNI")
                .nombres("Carlos")
                .apellidoPaterno("Alvarez")
                .apellidoMaterno("Quispe")
                .correo("admin.doc@muniquinua.gob.pe")
                .estado("ACTIVO")
                .build());

        String adminUsername = "admin_doc_" + UUID.randomUUID().toString().substring(0, 8);
        adminUser = usuarioRepository.save(Usuario.builder()
                .persona(pAdmin)
                .username(adminUsername)
                .passwordHash(passwordEncoder.encode("Password123*"))
                .roles(Set.of(rolAdmin))
                .enabled(true)
                .accountLocked(false)
                .build());

        // Usuario Regular
        String regDoc = "8" + UUID.randomUUID().toString().replaceAll("\\D", "").substring(0, 7);
        Persona pReg = personaRepository.save(Persona.builder()
                .numeroDocumento(regDoc)
                .tipoDocumento("DNI")
                .nombres("Rosa")
                .apellidoPaterno("Mendoza")
                .apellidoMaterno("Flores")
                .correo("rosa.doc@gmail.com")
                .estado("ACTIVO")
                .build());

        String regUsername = "user_doc_" + UUID.randomUUID().toString().substring(0, 8);
        regularUser = usuarioRepository.save(Usuario.builder()
                .persona(pReg)
                .username(regUsername)
                .passwordHash(passwordEncoder.encode("Password123*"))
                .roles(Set.of(rolCiudadano))
                .enabled(true)
                .accountLocked(false)
                .build());

        // Tokens JWT
        CustomUserDetails adminDetails = CustomUserDetails.builder()
                .id(adminUser.getId())
                .username(adminUser.getUsername())
                .password(adminUser.getPasswordHash())
                .enabled(true)
                .accountNonLocked(true)
                .authorities(List.of(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"),
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("DOCUMENTO_CREAR"),
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("DOCUMENTO_VER"),
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("DOCUMENTO_EDITAR"),
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("DOCUMENTO_ELIMINAR"),
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("DOCUMENTO_ADMIN")
                ))
                .build();
        adminCookie = new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, jwtTokenProvider.generateAccessToken(adminDetails));
        adminCookie.setPath("/");

        CustomUserDetails regDetails = CustomUserDetails.builder()
                .id(regularUser.getId())
                .username(regularUser.getUsername())
                .password(regularUser.getPasswordHash())
                .enabled(true)
                .accountNonLocked(true)
                .authorities(List.of(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_CIUDADANO")
                ))
                .build();
        regularCookie = new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, jwtTokenProvider.generateAccessToken(regDetails));
        regularCookie.setPath("/");
    }

    @Test
    @DisplayName("Debe subir un documento válido mediante multipart/form-data y registrar la versión 1")
    void debeSubirDocumentoExitosamente() throws Exception {
        byte[] pdfContent = "%PDF-1.7 Contenido oficial institucional de Quinua".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("archivo", "oficio_001.pdf", "application/pdf", pdfContent);

        mockMvc.perform(multipart("/api/documentos")
                        .file(file)
                        .param("categoria", "OFICIO")
                        .param("clasificacionAcceso", "INTERNO")
                        .param("descripcion", "Oficio remitido a alcaldía")
                        .param("oficinaId", String.valueOf(oficinaCentral.getId()))
                        .cookie(adminCookie)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.identificadorPublico", notNullValue()))
                .andExpect(jsonPath("$.data.nombreOriginal", is("oficio_001.pdf")))
                .andExpect(jsonPath("$.data.extension", is("pdf")))
                .andExpect(jsonPath("$.data.versionActual", is(1)))
                .andExpect(jsonPath("$.data.hashSha256", notNullValue()))
                .andExpect(jsonPath("$.data.bloqueoEliminacion", is(false)));

        assertThat(documentoRepository.findAll()).isNotEmpty();
    }

    @Test
    @DisplayName("Debe rechazar la subida de un archivo vacío")
    void debeRechazarArchivoVacio() throws Exception {
        MockMultipartFile file = new MockMultipartFile("archivo", "vacio.pdf", "application/pdf", new byte[0]);

        mockMvc.perform(multipart("/api/documentos")
                        .file(file)
                        .cookie(adminCookie)
                        .with(csrf()))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("Debe rechazar archivos con extensiones peligrosas (.exe)")
    void debeRechazarArchivoPeligroso() throws Exception {
        MockMultipartFile file = new MockMultipartFile("archivo", "virus.exe", "application/octet-stream", "dummy content".getBytes());

        mockMvc.perform(multipart("/api/documentos")
                        .file(file)
                        .cookie(adminCookie)
                        .with(csrf()))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("Debe obtener metadatos de un documento por ID y por UUID")
    void debeObtenerMetadatosPorIdYUuid() throws Exception {
        Documento doc = crearDocumentoPrueba("informe_tecnico.pdf", CategoriaDocumento.INFORME, ClasificacionAcceso.INTERNO, adminUser);

        mockMvc.perform(get("/api/documentos/" + doc.getId())
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", is(doc.getId().intValue())))
                .andExpect(jsonPath("$.data.nombreOriginal", is("informe_tecnico.pdf")));

        mockMvc.perform(get("/api/documentos/uuid/" + doc.getIdentificadorPublico())
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.identificadorPublico", is(doc.getIdentificadorPublico().toString())));
    }

    @Test
    @DisplayName("Debe descargar un documento autorizado con cabeceras de seguridad")
    void debeDescargarDocumentoConCabecerasSeguras() throws Exception {
        Documento doc = crearDocumentoPrueba("resolucion_alcaldia.pdf", CategoriaDocumento.RESOLUCION, ClasificacionAcceso.INTERNO, adminUser);

        mockMvc.perform(get("/api/documentos/" + doc.getId() + "/descargar")
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment; filename=\"resolucion_alcaldia.pdf\"")));
    }

    @Test
    @DisplayName("Debe permitir la vista previa inline de un documento PDF")
    void debePermitirVistaPreviaPdf() throws Exception {
        Documento doc = crearDocumentoPrueba("memorando_interno.pdf", CategoriaDocumento.MEMORANDO, ClasificacionAcceso.INTERNO, adminUser);

        mockMvc.perform(get("/api/documentos/" + doc.getId() + "/vista-previa")
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("inline; filename=\"memorando_interno.pdf\"")));
    }

    @Test
    @DisplayName("Debe verificar la integridad criptográfica SHA-256 exitosa")
    void debeVerificarIntegridadCriptograficaExitosa() throws Exception {
        Documento doc = crearDocumentoPrueba("contrato_obras.pdf", CategoriaDocumento.CONTRATO, ClasificacionAcceso.INTERNO, adminUser);

        mockMvc.perform(get("/api/documentos/" + doc.getId() + "/integridad")
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.resultado", is("INTEGRO")))
                .andExpect(jsonPath("$.data.hashEsperado", is(doc.getHashSha256())))
                .andExpect(jsonPath("$.data.hashCalculado", is(doc.getHashSha256())));
    }

    @Test
    @DisplayName("Debe crear una nueva versión de un documento e incrementar el contador")
    void debeCrearNuevaVersion() throws Exception {
        Documento doc = crearDocumentoPrueba("solicitud_licencia.pdf", CategoriaDocumento.SOLICITUD, ClasificacionAcceso.INTERNO, adminUser);

        byte[] newPdf = "%PDF-1.7 Contenido revisado y corregido v2".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("archivo", "solicitud_licencia_v2.pdf", "application/pdf", newPdf);

        mockMvc.perform(multipart("/api/documentos/" + doc.getId() + "/versiones")
                        .file(file)
                        .param("motivoCambio", "Actualización por subsanación de requisitos")
                        .cookie(adminCookie)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.versionActual", is(2)))
                .andExpect(jsonPath("$.data.nombreOriginal", is("solicitud_licencia_v2.pdf")));

        mockMvc.perform(get("/api/documentos/" + doc.getId() + "/versiones")
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    @DisplayName("Debe generar y guardar un PDF oficial con OpenPDF y código QR")
    void debeGenerarPdfOficial() throws Exception {
        GenerarPdfRequest req = GenerarPdfRequest.builder()
                .tipoPlantilla("CONSTANCIA")
                .codigoDocumental("CONST-2026-TEST")
                .titulo("Constancia de No Adeudo")
                .destinatario("Pedro Castillo Quispe")
                .asunto("Trámite regular de rentas")
                .cuerpo("Se hace constar que a la fecha el administrado no mantiene deudas tributarias pendientes con el distrito.")
                .build();

        mockMvc.perform(post("/api/documentos/generar-pdf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .cookie(adminCookie)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.categoria", is("CONSTANCIA")))
                .andExpect(jsonPath("$.data.bloqueoEliminacion", is(true)));
    }

    @Test
    @DisplayName("Debe impedir la eliminación si el documento tiene bloqueo de conservación activo")
    void debeRechazarEliminacionConBloqueo() throws Exception {
        Documento doc = crearDocumentoPrueba("patrimonio_bloqueado.pdf", CategoriaDocumento.INFORME, ClasificacionAcceso.INTERNO, adminUser);
        doc.setBloqueoEliminacion(true);
        documentoRepository.save(doc);

        EliminarDocumentoRequest req = EliminarDocumentoRequest.builder()
                .motivo("Intento de eliminación indebida")
                .build();

        mockMvc.perform(delete("/api/documentos/" + doc.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .cookie(adminCookie)
                        .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message", containsString("bloqueo institucional de conservación")));
    }

    @Test
    @DisplayName("Debe archivar y realizar eliminación lógica cuando el documento está desbloqueado")
    void debePermitirArchivarYEliminar() throws Exception {
        Documento doc = crearDocumentoPrueba("anexo_temporal.pdf", CategoriaDocumento.ANEXO, ClasificacionAcceso.INTERNO, adminUser);

        // 1. Archivar
        EliminarDocumentoRequest reqArchivar = EliminarDocumentoRequest.builder()
                .motivo("Trámite concluido, archivo regular")
                .build();

        mockMvc.perform(post("/api/documentos/" + doc.getId() + "/archivar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqArchivar))
                        .cookie(adminCookie)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estado", is("ARCHIVADO")));

        // 2. Eliminación lógica
        EliminarDocumentoRequest reqEliminar = EliminarDocumentoRequest.builder()
                .motivo("Depuración de expediente caduco")
                .build();

        mockMvc.perform(delete("/api/documentos/" + doc.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqEliminar))
                        .cookie(adminCookie)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estado", is("ELIMINADO_LOGICO")));
    }

    @Test
    @DisplayName("Debe rechazar con 403 Forbidden (Anti-IDOR) a un usuario que intenta ver un documento confidencial ajeno")
    void debeRechazarAccesoIdorAConfidencial() throws Exception {
        Documento docConfidencial = crearDocumentoPrueba("secreto_sumario.pdf", CategoriaDocumento.INFORME, ClasificacionAcceso.CONFIDENCIAL, adminUser);

        mockMvc.perform(get("/api/documentos/" + docConfidencial.getId())
                        .cookie(regularCookie))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Debe renderizar la vista web administrativa /dashboard/documentos")
    void debeRenderizarVistaWebDocumentos() throws Exception {
        crearDocumentoPrueba("doc_vista_web.pdf", CategoriaDocumento.OFICIO, ClasificacionAcceso.INTERNO, adminUser);

        mockMvc.perform(get("/dashboard/documentos")
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Gestión Documental y Archivos Seguros")))
                .andExpect(content().string(containsString("doc_vista_web.pdf")));
    }

    private Documento crearDocumentoPrueba(String nombreOriginal, CategoriaDocumento categoria, ClasificacionAcceso clasificacion, Usuario propietario) {
        byte[] bytes = ("%PDF-1.7 Contenido de prueba para " + nombreOriginal).getBytes(StandardCharsets.UTF_8);
        String sha256 = pe.gob.municipalidad.sigm.common.document.validation.Sha256Calculator.calculate(bytes);
        String internalName = storageService.store(new ByteArrayInputStream(bytes), "pdf");

        Documento doc = Documento.builder()
                .identificadorPublico(UUID.randomUUID())
                .nombreOriginal(nombreOriginal)
                .nombreInterno(internalName)
                .extension("pdf")
                .mimeType("application/pdf")
                .tamanoBytes((long) bytes.length)
                .hashSha256(sha256)
                .categoria(categoria)
                .clasificacionAcceso(clasificacion)
                .descripcion("Descripción de prueba")
                .estado(EstadoDocumento.ACTIVO)
                .versionActual(1)
                .bloqueoEliminacion(false)
                .usuarioPropietario(propietario)
                .oficinaResponsable(oficinaCentral)
                .moduloOrigen("SISTEMA")
                .build();
        doc = documentoRepository.save(doc);

        DocumentoVersion version1 = DocumentoVersion.builder()
                .documento(doc)
                .numeroVersion(1)
                .nombreInterno(internalName)
                .tamanoBytes((long) bytes.length)
                .mimeType("application/pdf")
                .hashSha256(sha256)
                .motivoCambio("Versión inicial de prueba")
                .usuarioResponsable(propietario)
                .build();
        versionRepository.save(version1);

        return doc;
    }
}
