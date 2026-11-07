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
import pe.gob.municipalidad.sigm.common.document.entity.CategoriaDocumento;
import pe.gob.municipalidad.sigm.common.document.entity.ClasificacionAcceso;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.document.entity.EstadoDocumento;
import pe.gob.municipalidad.sigm.common.document.repository.DocumentoRepository;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.DuplicateResourceException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.PersonaJuridica;
import pe.gob.municipalidad.sigm.common.repository.PersonaJuridicaRepository;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoComerciante;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoComerciante;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoRepresentacion;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;
import pe.gob.municipalidad.sigm.modules.mercados.service.*;
import pe.gob.municipalidad.sigm.modules.mercados.service.verification.IdentidadVerificationService;
import pe.gob.municipalidad.sigm.modules.mercados.service.verification.VerificationResult;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MercadoComerciantesServiceTest {

    @Autowired
    private ComercianteService comercianteService;

    @Autowired
    private ComercianteActividadService actividadService;

    @Autowired
    private ComercianteRepresentanteService representanteService;

    @Autowired
    private ComercianteExportService exportService;

    @Autowired
    private IdentidadVerificationService identidadVerificationService;

    @Autowired
    private ComercianteRepository comercianteRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private PersonaJuridicaRepository personaJuridicaRepository;

    @Autowired
    private ActividadComercialRepository actividadRepository;

    @Autowired
    private DocumentoRepository documentoRepository;

    private ActividadComercial actividadArtesania;
    private ActividadComercial actividadGastronomia;

    @BeforeEach
    void setUp() {
        actividadArtesania = actividadRepository.findByCodigo("ART-01")
                .orElseGet(() -> actividadRepository.save(ActividadComercial.builder()
                        .codigo("ART-01")
                        .nombre("Artesanía y Cerámica Tradicional")
                        .categoria("CULTURA")
                        .descripcion("Venta de retablos y cerámica típica de Quinua")
                        .estado("ACTIVO")
                        .build()));

        actividadGastronomia = actividadRepository.findByCodigo("GAS-01")
                .orElseGet(() -> actividadRepository.save(ActividadComercial.builder()
                        .codigo("GAS-01")
                        .nombre("Gastronomía Tradicional y Platos Típicos")
                        .categoria("ALIMENTACION")
                        .descripcion("Comidas típicas ayacuchanas")
                        .estado("ACTIVO")
                        .build()));
    }

    @Test
    @DisplayName("01. Registrar persona natural con datos válidos")
    void test01_registrarPersonaNatural() {
        String dni = "7489" + (System.currentTimeMillis() % 10000);
        if (dni.length() < 8) dni = String.format("%08d", Long.parseLong(dni));

        ComercianteNaturalCreateRequest request = ComercianteNaturalCreateRequest.builder()
                .numeroDocumento(dni)
                .nombres("Cipriano")
                .apellidoPaterno("Quispe")
                .apellidoMaterno("Huamán")
                .telefono("966123456")
                .correo("cipriano@quinua.gob.pe")
                .direccion("Barrio Alfareros, Quinua")
                .actividadIds(Set.of(actividadArtesania.getId()))
                .build();

        ComercianteResponse response = comercianteService.registrarComercianteNatural(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getTipoComerciante()).isEqualTo(TipoComerciante.PERSONA_NATURAL);
        assertThat(response.getTitularNombre()).contains("Cipriano Quispe");
        assertThat(response.getEstado()).isEqualTo(EstadoComerciante.ACTIVO);
        assertThat(response.getCodigoComerciante()).startsWith("COM-");
    }

    @Test
    @DisplayName("02. Registrar persona jurídica con RUC oficial válido")
    void test02_registrarPersonaJuridica() {
        // RUC oficial válido con algoritmo Módulo 11
        String ruc = "20600123450";

        ComercianteJuridicoCreateRequest request = ComercianteJuridicoCreateRequest.builder()
                .ruc(ruc)
                .razonSocial("Artesanías y Cerámicas Quinua S.A.C.")
                .nombreComercial("Artesanías Quinua")
                .domicilioFiscal("Jr. Bellido 120, Quinua")
                .telefono("066312456")
                .correo("contacto@artesaniasquinua.pe")
                .actividadIds(Set.of(actividadArtesania.getId()))
                .build();

        ComercianteResponse response = comercianteService.registrarComercianteJuridico(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getTipoComerciante()).isEqualTo(TipoComerciante.PERSONA_JURIDICA);
        assertThat(response.getTitularNombre()).contains("Artesanías Quinua");
        assertThat(response.getEstado()).isEqualTo(EstadoComerciante.ACTIVO);
    }

    @Test
    @DisplayName("03. Validar DNI válido de 8 dígitos numéricos")
    void test03_validarDniValido() {
        assertThat(identidadVerificationService.validarFormatoDni("45892314")).isTrue();
        assertThat(identidadVerificationService.validarFormatoDni("00123456")).isTrue();

        VerificationResult res = identidadVerificationService.verificarDni("45892314");
        assertThat(res.isFormatoValido()).isTrue();
    }

    @Test
    @DisplayName("04. Rechazar DNI inválido por longitud o letras")
    void test04_rechazarDniInvalido() {
        assertThat(identidadVerificationService.validarFormatoDni("1234567")).isFalse(); // 7 dígitos
        assertThat(identidadVerificationService.validarFormatoDni("123456789")).isFalse(); // 9 dígitos
        assertThat(identidadVerificationService.validarFormatoDni("4589AB14")).isFalse(); // letras

        ComercianteNaturalCreateRequest req = ComercianteNaturalCreateRequest.builder()
                .numeroDocumento("4589AB14")
                .nombres("Invalido")
                .apellidoPaterno("Test")
                .build();

        assertThatThrownBy(() -> comercianteService.registrarComercianteNatural(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no es válido");
    }

    @Test
    @DisplayName("05. Validar RUC matemáticamente correcto conforme a Módulo 11")
    void test05_validarRucValido() {
        // 20600123450 y 20123456786 son matemáticamente válidos
        assertThat(identidadVerificationService.validarFormatoRuc("20600123450")).isTrue();
        assertThat(identidadVerificationService.validarFormatoRuc("20123456786")).isTrue();
        assertThat(identidadVerificationService.validarFormatoRuc("10458923146")).isTrue();
    }

    @Test
    @DisplayName("06. Rechazar RUC inválido por prefijo o dígito verificador")
    void test06_rechazarRucInvalido() {
        assertThat(identidadVerificationService.validarFormatoRuc("30600123450")).isFalse(); // prefijo inválido (no existe 30)
        assertThat(identidadVerificationService.validarFormatoRuc("20600123459")).isFalse(); // dígito verificador erróneo (debe ser 0)
        assertThat(identidadVerificationService.validarFormatoRuc("12345")).isFalse();

        ComercianteJuridicoCreateRequest req = ComercianteJuridicoCreateRequest.builder()
                .ruc("20600123459")
                .razonSocial("Empresa Invalida S.A.")
                .build();

        assertThatThrownBy(() -> comercianteService.registrarComercianteJuridico(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no es válido según las reglas matemáticas");
    }

    @Test
    @DisplayName("07. Reutilizar Persona existente en el núcleo común sin duplicarla")
    void test07_reutilizarPersonaExistente() {
        String dni = "7123" + (System.currentTimeMillis() % 10000);
        if (dni.length() < 8) dni = String.format("%08d", Long.parseLong(dni));

        Persona existente = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento(dni)
                .nombres("Teófilo")
                .apellidoPaterno("Patrocinio")
                .estado("ACTIVO")
                .build());

        ComercianteNaturalCreateRequest request = ComercianteNaturalCreateRequest.builder()
                .numeroDocumento(dni)
                .nombres("Teófilo Modificado")
                .apellidoPaterno("Patrocinio")
                .telefono("988776655")
                .build();

        ComercianteResponse response = comercianteService.registrarComercianteNatural(request);

        assertThat(response.getTitularNombre()).contains("Teófilo");
        // Verificar que no se creó otra persona
        assertThat(personaRepository.countByNumeroDocumento(dni)).isEqualTo(1);
    }

    @Test
    @DisplayName("08. Rechazar registro duplicado para la misma persona o RUC")
    void test08_rechazarComercianteDuplicado() {
        String dni = "7234" + (System.currentTimeMillis() % 10000);
        if (dni.length() < 8) dni = String.format("%08d", Long.parseLong(dni));

        ComercianteNaturalCreateRequest req = ComercianteNaturalCreateRequest.builder()
                .numeroDocumento(dni)
                .nombres("Dionisio")
                .apellidoPaterno("Ochoa")
                .build();

        comercianteService.registrarComercianteNatural(req);

        // Intento de reempadronar a la misma persona
        assertThatThrownBy(() -> comercianteService.registrarComercianteNatural(req))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("ya se encuentra registrada como comerciante");
    }

    @Test
    @DisplayName("09. Registrar representante legal de persona jurídica")
    void test09_registrarRepresentante() {
        String ruc = "20123456786";
        ComercianteJuridicoCreateRequest reqPj = ComercianteJuridicoCreateRequest.builder()
                .ruc(ruc)
                .razonSocial("Cooperativa de Servicios Quinua R.L.")
                .build();

        ComercianteResponse comerciantePj = comercianteService.registrarComercianteJuridico(reqPj);

        RepresentanteRequest reqRep = RepresentanteRequest.builder()
                .dni("44332211")
                .nombres("Lorenzo")
                .apellidoPaterno("Bautista")
                .tipoRepresentacion(TipoRepresentacion.GERENTE_GENERAL)
                .fechaInicio(LocalDate.now())
                .build();

        RepresentanteResponse repResp = representanteService.agregarRepresentante(comerciantePj.getId(), reqRep);

        assertThat(repResp).isNotNull();
        assertThat(repResp.getNombreCompleto()).contains("Lorenzo Bautista");
        assertThat(repResp.isVigente()).isTrue();
    }

    @Test
    @DisplayName("10. Validar representante: rechazar fecha fin anterior a inicio")
    void test10_validarRepresentanteFechaFinAnteriorAInicio() {
        String ruc = "20123456786";
        PersonaJuridica pj = personaJuridicaRepository.findByRuc(ruc)
                .orElseGet(() -> personaJuridicaRepository.save(PersonaJuridica.builder().ruc(ruc).razonSocial("Test PJ").build()));

        Comerciante c = comercianteRepository.findByPersonaJuridicaId(pj.getId())
                .orElseGet(() -> comercianteRepository.save(Comerciante.builder()
                        .tipoComerciante(TipoComerciante.PERSONA_JURIDICA)
                        .personaJuridica(pj)
                        .codigoComerciante("COM-TEST-PJ")
                        .build()));

        RepresentanteRequest reqRep = RepresentanteRequest.builder()
                .dni("44332212")
                .nombres("Lorenzo")
                .apellidoPaterno("Bautista")
                .tipoRepresentacion(TipoRepresentacion.GERENTE_GENERAL)
                .fechaInicio(LocalDate.now())
                .fechaFin(LocalDate.now().minusDays(5)) // Fecha fin anterior!
                .build();

        assertThatThrownBy(() -> representanteService.agregarRepresentante(c.getId(), reqRep))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no puede ser anterior");
    }

    @Test
    @DisplayName("11. Registrar y consultar actividades comerciales")
    void test11_registrarActividadComercial() {
        String dni = "7345" + (System.currentTimeMillis() % 10000);
        if (dni.length() < 8) dni = String.format("%08d", Long.parseLong(dni));

        ComercianteResponse c = comercianteService.registrarComercianteNatural(ComercianteNaturalCreateRequest.builder()
                .numeroDocumento(dni)
                .nombres("Valerio")
                .apellidoPaterno("Huaccachi")
                .build());

        ActividadComercianteResponse actResp = actividadService.asociarActividad(c.getId(), ActividadComercianteRequest.builder()
                .actividadId(actividadGastronomia.getId())
                .principal(true)
                .build());

        assertThat(actResp).isNotNull();
        assertThat(actResp.getNombre()).contains("Gastronomía");

        List<ActividadComercianteResponse> lista = actividadService.listarActividadesPorComerciante(c.getId());
        assertThat(lista).hasSize(1);
    }

    @Test
    @DisplayName("12. Asociar varias actividades a un mismo comerciante")
    void test12_asociarVariasActividades() {
        String dni = "7456" + (System.currentTimeMillis() % 10000);
        if (dni.length() < 8) dni = String.format("%08d", Long.parseLong(dni));

        ComercianteResponse c = comercianteService.registrarComercianteNatural(ComercianteNaturalCreateRequest.builder()
                .numeroDocumento(dni)
                .nombres("Fortunato")
                .apellidoPaterno("Allcca")
                .actividadIds(Set.of(actividadArtesania.getId(), actividadGastronomia.getId()))
                .build());

        ComercianteDetailResponse detalle = comercianteService.obtenerDetalleCompleto(c.getId());
        assertThat(detalle.getActividades()).hasSize(2);
    }

    @Test
    @DisplayName("13. Consultar comerciante por ID y código")
    void test13_consultarComerciante() {
        String dni = "7567" + (System.currentTimeMillis() % 10000);
        if (dni.length() < 8) dni = String.format("%08d", Long.parseLong(dni));

        ComercianteResponse c = comercianteService.registrarComercianteNatural(ComercianteNaturalCreateRequest.builder()
                .numeroDocumento(dni)
                .nombres("Hermenegildo")
                .apellidoPaterno("Pariona")
                .build());

        ComercianteResponse porId = comercianteService.obtenerPorId(c.getId());
        ComercianteResponse porCod = comercianteService.obtenerPorCodigo(c.getCodigoComerciante());

        assertThat(porId.getId()).isEqualTo(c.getId());
        assertThat(porCod.getCodigoComerciante()).isEqualTo(c.getCodigoComerciante());
    }

    @Test
    @DisplayName("14. Editar datos de contacto y observaciones autorizadas")
    void test14_editarContacto() {
        String dni = "7678" + (System.currentTimeMillis() % 10000);
        if (dni.length() < 8) dni = String.format("%08d", Long.parseLong(dni));

        ComercianteResponse c = comercianteService.registrarComercianteNatural(ComercianteNaturalCreateRequest.builder()
                .numeroDocumento(dni)
                .nombres("Eulogio")
                .apellidoPaterno("Cárdenas")
                .telefono("900111222")
                .build());

        ComercianteUpdateRequest updateReq = ComercianteUpdateRequest.builder()
                .telefono("999888777")
                .correo("nuevo@correo.com")
                .direccion("Nueva dirección central")
                .observaciones("Actualizado por trámite de empadronamiento")
                .version(c.getVersion())
                .build();

        ComercianteResponse editado = comercianteService.actualizarComerciante(c.getId(), updateReq);

        Comerciante ent = comercianteRepository.findById(c.getId()).orElseThrow();
        assertThat(ent.getTelefonoContacto()).isEqualTo("999888777");
        assertThat(ent.getCorreoContacto()).isEqualTo("nuevo@correo.com");
        assertThat(ent.getDireccionContacto()).isEqualTo("Nueva dirección central");
        assertThat(editado.getTelefono()).contains("8777");
    }

    @Test
    @DisplayName("15. Proteger cambio de código con colisión duplicada")
    void test15_protegerCambioDeIdentidad() {
        String dni1 = "7789" + (System.currentTimeMillis() % 10000);
        if (dni1.length() < 8) dni1 = String.format("%08d", Long.parseLong(dni1));
        String dni2 = "7890" + (System.currentTimeMillis() % 10000);
        if (dni2.length() < 8) dni2 = String.format("%08d", Long.parseLong(dni2));

        ComercianteResponse c1 = comercianteService.registrarComercianteNatural(ComercianteNaturalCreateRequest.builder()
                .numeroDocumento(dni1)
                .nombres("Persona 1")
                .apellidoPaterno("Test")
                .codigoComerciante("COM-COD-01")
                .build());

        ComercianteResponse c2 = comercianteService.registrarComercianteNatural(ComercianteNaturalCreateRequest.builder()
                .numeroDocumento(dni2)
                .nombres("Persona 2")
                .apellidoPaterno("Test")
                .codigoComerciante("COM-COD-02")
                .build());

        // Intentar cambiar c2 al código de c1
        ComercianteUpdateRequest updateReq = ComercianteUpdateRequest.builder()
                .codigoComerciante("COM-COD-01")
                .version(c2.getVersion())
                .build();

        assertThatThrownBy(() -> comercianteService.actualizarComerciante(c2.getId(), updateReq))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("ya está siendo utilizado");
    }

    @Test
    @DisplayName("16. Cambiar estado físico con motivo y trazabilidad inmutable")
    void test16_cambiarEstado() {
        String dni = "7901" + (System.currentTimeMillis() % 10000);
        if (dni.length() < 8) dni = String.format("%08d", Long.parseLong(dni));

        ComercianteResponse c = comercianteService.registrarComercianteNatural(ComercianteNaturalCreateRequest.builder()
                .numeroDocumento(dni)
                .nombres("Saturnino")
                .apellidoPaterno("Ramos")
                .build());

        ComercianteEstadoRequest reqEstado = ComercianteEstadoRequest.builder()
                .nuevoEstado(EstadoComerciante.SUSPENDIDO)
                .motivo("Incumplimiento temporal de pago de merced conductiva")
                .observaciones("Notificado formalmente con Carta N° 045-2026")
                .build();

        ComercianteResponse actualizado = comercianteService.cambiarEstado(c.getId(), reqEstado);

        assertThat(actualizado.getEstado()).isEqualTo(EstadoComerciante.SUSPENDIDO);

        ComercianteDetailResponse detalle = comercianteService.obtenerDetalleCompleto(c.getId());
        assertThat(detalle.getHistorialEstados()).hasSize(1);
        assertThat(detalle.getHistorialEstados().get(0).getEstadoNuevo()).isEqualTo(EstadoComerciante.SUSPENDIDO);
        assertThat(detalle.getHistorialEstados().get(0).getMotivo()).contains("Incumplimiento");
    }

    @Test
    @DisplayName("17. Rechazar transición hacia el mismo estado")
    void test17_rechazarTransicionMismoEstado() {
        String dni = "8012" + (System.currentTimeMillis() % 10000);
        if (dni.length() < 8) dni = String.format("%08d", Long.parseLong(dni));

        ComercianteResponse c = comercianteService.registrarComercianteNatural(ComercianteNaturalCreateRequest.builder()
                .numeroDocumento(dni)
                .nombres("Anastasio")
                .apellidoPaterno("Vargas")
                .build());

        ComercianteEstadoRequest reqEstado = ComercianteEstadoRequest.builder()
                .nuevoEstado(EstadoComerciante.ACTIVO) // Ya está activo
                .motivo("Mismo estado de prueba")
                .build();

        assertThatThrownBy(() -> comercianteService.cambiarEstado(c.getId(), reqEstado))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ya se encuentra en estado");
    }

    @Test
    @DisplayName("18. Asociar y consultar documentos en repositorio central")
    void test18_consultarDocumentos() {
        String dni = "8123" + (System.currentTimeMillis() % 10000);
        if (dni.length() < 8) dni = String.format("%08d", Long.parseLong(dni));

        ComercianteResponse c = comercianteService.registrarComercianteNatural(ComercianteNaturalCreateRequest.builder()
                .numeroDocumento(dni)
                .nombres("Apolinario")
                .apellidoPaterno("Sullca")
                .build());

        Documento doc = documentoRepository.save(Documento.builder()
                .identificadorPublico(UUID.randomUUID())
                .nombreOriginal("Copia de DNI y Ficha de Empadronamiento.pdf")
                .nombreInterno(UUID.randomUUID().toString())
                .mimeType("application/pdf")
                .extension("pdf")
                .tamanoBytes(1024L)
                .hashSha256("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
                .categoria(CategoriaDocumento.DOCUMENTO_IDENTIDAD)
                .clasificacionAcceso(ClasificacionAcceso.INTERNO)
                .estado(EstadoDocumento.ACTIVO)
                .fechaCreacion(OffsetDateTime.now())
                .fechaActualizacion(OffsetDateTime.now())
                .build());

        ComercianteDocumentoResponse docResp = comercianteService.asociarDocumento(
                c.getId(), doc.getId(), "DNI", "Documento verificado");

        assertThat(docResp).isNotNull();
        assertThat(docResp.getTitulo()).isEqualTo("Copia de DNI y Ficha de Empadronamiento.pdf");

        ComercianteDetailResponse detalle = comercianteService.obtenerDetalleCompleto(c.getId());
        assertThat(detalle.getDocumentos()).hasSize(1);
    }

    @Test
    @DisplayName("19. Consultar puestos asociados (sin puestos muestra lista vacía)")
    void test19_consultarPuestos() {
        String dni = "8234" + (System.currentTimeMillis() % 10000);
        if (dni.length() < 8) dni = String.format("%08d", Long.parseLong(dni));

        ComercianteResponse c = comercianteService.registrarComercianteNatural(ComercianteNaturalCreateRequest.builder()
                .numeroDocumento(dni)
                .nombres("Casimiro")
                .apellidoPaterno("Rojas")
                .build());

        ComercianteDetailResponse detalle = comercianteService.obtenerDetalleCompleto(c.getId());
        assertThat(detalle.getPuestosAsociados()).isNotNull();
        assertThat(detalle.getPuestosAsociados()).isEmpty();
    }

    @Test
    @DisplayName("20. Consultar autorizaciones comerciales existentes")
    void test20_consultarAutorizaciones() {
        String dni = "8345" + (System.currentTimeMillis() % 10000);
        if (dni.length() < 8) dni = String.format("%08d", Long.parseLong(dni));

        ComercianteResponse c = comercianteService.registrarComercianteNatural(ComercianteNaturalCreateRequest.builder()
                .numeroDocumento(dni)
                .nombres("Benito")
                .apellidoPaterno("Juárez")
                .build());

        ComercianteDetailResponse detalle = comercianteService.obtenerDetalleCompleto(c.getId());
        assertThat(detalle.getAutorizaciones()).isNotNull();
    }

    @Test
    @DisplayName("21. Buscar por código de comerciante")
    void test21_buscarPorCodigo() {
        String dni = "8456" + (System.currentTimeMillis() % 10000);
        if (dni.length() < 8) dni = String.format("%08d", Long.parseLong(dni));

        ComercianteResponse c = comercianteService.registrarComercianteNatural(ComercianteNaturalCreateRequest.builder()
                .numeroDocumento(dni)
                .nombres("Facundo")
                .apellidoPaterno("Cabral")
                .codigoComerciante("COM-SEARCH-01")
                .build());

        ComercianteFilter filter = ComercianteFilter.builder().termino("SEARCH-01").build();
        Page<ComercianteResponse> pagina = comercianteService.listarComerciantes(filter, PageRequest.of(0, 10));

        assertThat(pagina.getContent()).anyMatch(item -> item.getCodigoComerciante().equals("COM-SEARCH-01"));
    }

    @Test
    @DisplayName("22. Buscar por nombre o apellido")
    void test22_buscarPorNombre() {
        String dni = "8567" + (System.currentTimeMillis() % 10000);
        if (dni.length() < 8) dni = String.format("%08d", Long.parseLong(dni));

        comercianteService.registrarComercianteNatural(ComercianteNaturalCreateRequest.builder()
                .numeroDocumento(dni)
                .nombres("Wenceslao")
                .apellidoPaterno("Albarracín")
                .build());

        ComercianteFilter filter = ComercianteFilter.builder().termino("Wenceslao").build();
        Page<ComercianteResponse> pagina = comercianteService.listarComerciantes(filter, PageRequest.of(0, 10));

        assertThat(pagina.getContent()).anyMatch(item -> item.getTitularNombre().contains("Wenceslao"));
    }

    @Test
    @DisplayName("23. Filtrar por tipo de comerciante (Natural vs Jurídica)")
    void test23_filtrarPorTipo() {
        ComercianteFilter filterNat = ComercianteFilter.builder().tipoComerciante(TipoComerciante.PERSONA_NATURAL).build();
        Page<ComercianteResponse> paginaNat = comercianteService.listarComerciantes(filterNat, PageRequest.of(0, 10));

        assertThat(paginaNat.getContent()).allMatch(c -> c.getTipoComerciante() == TipoComerciante.PERSONA_NATURAL);
    }

    @Test
    @DisplayName("24. Filtrar por estado")
    void test24_filtrarPorEstado() {
        ComercianteFilter filterAct = ComercianteFilter.builder().estado(EstadoComerciante.ACTIVO).build();
        Page<ComercianteResponse> paginaAct = comercianteService.listarComerciantes(filterAct, PageRequest.of(0, 10));

        assertThat(paginaAct.getContent()).allMatch(c -> c.getEstado() == EstadoComerciante.ACTIVO);
    }

    @Test
    @DisplayName("25. Paginación eficiente en PostgreSQL")
    void test25_paginarComerciantes() {
        ComercianteFilter filter = ComercianteFilter.builder().build();
        Page<ComercianteResponse> p1 = comercianteService.listarComerciantes(filter, PageRequest.of(0, 2));

        assertThat(p1.getSize()).isEqualTo(2);
    }

    @Test
    @DisplayName("26. Contar comerciantes y calcular estadísticas agregadas")
    void test26_contarComerciantes() {
        ComercianteEstadisticasResponse stats = comercianteService.obtenerEstadisticas();

        assertThat(stats.getTotalComerciantes()).isGreaterThanOrEqualTo(0);
        assertThat(stats.getPersonasNaturales() + stats.getPersonasJuridicas()).isEqualTo(stats.getTotalComerciantes());
    }

    @Test
    @DisplayName("27. Exportar padrón a Microsoft Excel protegido")
    void test27_exportarExcel() {
        ComercianteFilter filter = ComercianteFilter.builder().build();
        byte[] bytes = exportService.exportarExcel(filter);

        assertThat(bytes).isNotNull();
        assertThat(bytes.length).isGreaterThan(100);
    }

    @Test
    @DisplayName("28. Exportar padrón a documento PDF institucional")
    void test28_exportarPdf() {
        ComercianteFilter filter = ComercianteFilter.builder().build();
        byte[] bytes = exportService.exportarPdf(filter);

        assertThat(bytes).isNotNull();
        assertThat(bytes.length).isGreaterThan(100);
    }
}
