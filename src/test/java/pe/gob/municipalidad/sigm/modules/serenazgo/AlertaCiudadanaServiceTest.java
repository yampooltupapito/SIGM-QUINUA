package pe.gob.municipalidad.sigm.modules.serenazgo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.AlertaCiudadanaSeguridadService;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AlertaCiudadanaServiceTest {

    @Autowired
    private AlertaCiudadanaSeguridadService alertaService;

    @Autowired
    private AlertaCiudadanaSeguridadRepository alertaRepository;

    @Autowired
    private TipoIncidenciaSeguridadRepository tipoIncidenciaRepository;

    @Autowired
    private SectorPatrullajeRepository sectorRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private OficinaRepository oficinaRepository;

    private TipoIncidenciaSeguridad tipoRuidos;
    private TipoIncidenciaSeguridad tipoSeguridad;
    private SectorPatrullaje sector1;
    private Usuario usuarioCiudadano;
    private Usuario usuarioAdmin;

    @BeforeEach
    void setUp() {
        tipoRuidos = tipoIncidenciaRepository.findByCodigo("SEG-RUI-01").orElseGet(() -> {
            TipoIncidenciaSeguridad t = new TipoIncidenciaSeguridad();
            t.setCodigo("SEG-RUI-01");
            t.setNombre("Ruidos Molestos en Vía Pública");
            t.setCategoria(CategoriaIncidenciaSeguridad.ORDEN_PUBLICO);
            t.setPrioridadSugerida(PrioridadIncidenciaSeguridad.BAJA);
            t.setActivo(true);
            return tipoIncidenciaRepository.save(t);
        });

        tipoSeguridad = tipoIncidenciaRepository.findByCodigo("SEG-ROB-01").orElseGet(() -> {
            TipoIncidenciaSeguridad t = new TipoIncidenciaSeguridad();
            t.setCodigo("SEG-ROB-01");
            t.setNombre("Presunto Hurto / Tentativa");
            t.setCategoria(CategoriaIncidenciaSeguridad.ORDEN_PUBLICO);
            t.setPrioridadSugerida(PrioridadIncidenciaSeguridad.ALTA);
            t.setActivo(true);
            return tipoIncidenciaRepository.save(t);
        });

        sector1 = sectorRepository.findByCodigo("SEC-01").orElseGet(() -> {
            SectorPatrullaje s = new SectorPatrullaje();
            s.setCodigo("SEC-01");
            s.setNombre("Casco Urbano Quinua");
            s.setEstado(EstadoSectorPatrullaje.ACTIVO);
            return sectorRepository.save(s);
        });

        usuarioCiudadano = usuarioRepository.findByUsername("ciudadano.test").orElseGet(() -> {
            long ts = System.currentTimeMillis();
            Persona p = new Persona();
            p.setTipoDocumento("DNI");
            p.setNumeroDocumento(String.format("%08d", Math.abs((int)(ts % 100000000))));
            p.setNombres("Carlos");
            p.setApellidoPaterno("Mendoza");
            p.setApellidoMaterno("Quispe");
            p.setCorreo("carlos" + ts + "@quinua.gob.pe");
            p.setEstado("ACTIVO");
            p = personaRepository.save(p);

            Usuario u = new Usuario();
            u.setUsername("ciudadano.test");
            u.setPasswordHash("$2a$10$abcdefghijklmnopqrstuv");
            u.setPersona(p);
            u.setEnabled(true);
            u.setAccountLocked(false);
            return usuarioRepository.save(u);
        });

        usuarioAdmin = usuarioRepository.findByUsername("admin").orElse(usuarioCiudadano);
    }

    @Test
    @Order(1)
    @DisplayName("1. Registro anónimo exitoso: genera código ALT-YYYY-XXXXX, PIN secreto de 6 caracteres y hash seguro")
    void testRegistroAnonimoExitoso() {
        AlertaRegistroCiudadanoRequest request = AlertaRegistroCiudadanoRequest.builder()
                .tipoReporteId(tipoRuidos.getId())
                .titulo("Música excesiva en Plaza de Quinua")
                .descripcion("Se observa música con parlantes en la vía pública impidiendo el descanso.")
                .ubicacionReferencia("Plaza de Armas frente al Obelisco")
                .sectorId(sector1.getId())
                .latitud(new BigDecimal("-13.043500"))
                .longitud(new BigDecimal("-74.137800"))
                .reportanteAnonimo(true)
                .build();

        AlertaConfirmacionResponse conf = alertaService.registrarAlertaCiudadana(
                request, null, null, "127.0.0.1"
        );

        assertThat(conf).isNotNull();
        assertThat(conf.getCodigo()).startsWith("ALT-");
        assertThat(conf.getPinConsulta()).hasSize(6);
        assertThat(conf.getEstado()).isEqualTo(EstadoAlertaCiudadana.RECIBIDA);

        AlertaCiudadanaSeguridad entidad = alertaRepository.findByCodigo(conf.getCodigo()).orElseThrow();
        assertThat(entidad.getReportanteAnonimo()).isTrue();
        assertThat(entidad.getReportanteNombre()).isNull();
        assertThat(entidad.getClaveConsultaHash()).isNotNull();
        assertThat(entidad.getClaveConsultaHash()).startsWith("$2a$"); // BCrypt hash
    }

    @Test
    @Order(2)
    @DisplayName("2. Registro identificado con archivos adjuntos: almacena hash SHA-256 y miniaturas")
    void testRegistroIdentificadoConArchivos() {
        AlertaRegistroCiudadanoRequest request = AlertaRegistroCiudadanoRequest.builder()
                .tipoReporteId(tipoSeguridad.getId())
                .titulo("Personas sospechosas rondando viviendas")
                .descripcion("Dos personas vestidas de negro observando cerrojos en la esquina.")
                .ubicacionReferencia("Jr. Sucre con Jr. Ayacucho")
                .sectorId(sector1.getId())
                .reportanteAnonimo(false)
                .reportanteNombre("María Quispe")
                .reportanteTelefonoContacto("966123456")
                .reportanteEmail("maria.quispe@correo.pe")
                .build();

        MockMultipartFile archivo1 = new MockMultipartFile(
                "archivos", "foto_evidencia.jpg", "image/jpeg", "fake-image-bytes".getBytes(StandardCharsets.UTF_8)
        );

        AlertaConfirmacionResponse conf = alertaService.registrarAlertaCiudadana(
                request, List.of(archivo1), usuarioCiudadano.getUsername(), "192.168.1.50"
        );

        assertThat(conf).isNotNull();
        AlertaDetailResponse detalle = alertaService.obtenerDetalleAdmin(conf.getId(), true);
        assertThat(detalle.getReportanteAnonimo()).isFalse();
        assertThat(detalle.getReportanteNombre()).isEqualTo("María Quispe");
        assertThat(detalle.getDocumentos()).hasSize(1);
        assertThat(detalle.getDocumentos().get(0).getNombreArchivo()).isEqualTo("foto_evidencia.jpg");
        assertThat(detalle.getDocumentos().get(0).getHashSha256()).isNotNull();
    }

    @Test
    @Order(3)
    @DisplayName("3. Consulta de seguimiento protegida: PIN correcto permite acceso, PIN incorrecto lanza AccessDeniedException")
    void testConsultaSeguimientoProtegida() {
        AlertaRegistroCiudadanoRequest request = AlertaRegistroCiudadanoRequest.builder()
                .tipoReporteId(tipoRuidos.getId())
                .titulo("Reporte para prueba de seguimiento")
                .descripcion("Descripción de prueba con longitud suficiente para validación.")
                .ubicacionReferencia("Av. Centenario 123")
                .reportanteAnonimo(true)
                .build();

        AlertaConfirmacionResponse conf = alertaService.registrarAlertaCiudadana(
                request, null, null, "127.0.0.1"
        );

        // Consulta con PIN correcto -> OK
        AlertaDetailResponse detalle = alertaService.consultarSeguimiento(
                conf.getCodigo(), conf.getPinConsulta(), null
        );
        assertThat(detalle).isNotNull();
        assertThat(detalle.getCodigo()).isEqualTo(conf.getCodigo());

        // Consulta con PIN incorrecto -> Excepción AccessDenied
        assertThatThrownBy(() -> alertaService.consultarSeguimiento(conf.getCodigo(), "WRONG6", null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("PIN de seguridad");
    }

    @Test
    @Order(4)
    @DisplayName("4. Clasificación administrativa de alerta: actualiza tipo y genera historial")
    void testClasificarAlerta() {
        AlertaRegistroCiudadanoRequest request = AlertaRegistroCiudadanoRequest.builder()
                .tipoReporteId(tipoRuidos.getId())
                .titulo("Reporte mal categorizado por ciudadano")
                .descripcion("El ciudadano reportó ruido pero se trata de una tentativa de robo.")
                .ubicacionReferencia("Jr. Comercio 45")
                .reportanteAnonimo(true)
                .build();

        AlertaConfirmacionResponse conf = alertaService.registrarAlertaCiudadana(
                request, null, null, "127.0.0.1"
        );
        AlertaCiudadanaSeguridad entidad = alertaRepository.findByCodigo(conf.getCodigo()).orElseThrow();

        AlertaClasificarRequest reqClasificar = AlertaClasificarRequest.builder()
                .nuevoTipoReporteId(tipoSeguridad.getId())
                .motivo("Reclasificación técnica tras revisión de relato del ciudadano")
                .build();

        alertaService.clasificarAlerta(entidad.getId(), reqClasificar, usuarioAdmin.getUsername());

        AlertaCiudadanaSeguridad actualizada = alertaRepository.findById(entidad.getId()).orElseThrow();
        assertThat(actualizada.getTipoReporte().getId()).isEqualTo(tipoSeguridad.getId());
        assertThat(actualizada.getEstado()).isEqualTo(EstadoAlertaCiudadana.CLASIFICADA);
    }

    @Test
    @Order(5)
    @DisplayName("5. Cambio de prioridad administrativa: actualiza prioridad y guarda auditoría")
    void testCambiarPrioridad() {
        AlertaRegistroCiudadanoRequest request = AlertaRegistroCiudadanoRequest.builder()
                .tipoReporteId(tipoSeguridad.getId())
                .titulo("Alerta de prioridad media")
                .descripcion("Descripción clara del hecho que requiere elevación a crítica.")
                .ubicacionReferencia("Sector Pampas")
                .reportanteAnonimo(true)
                .build();

        AlertaConfirmacionResponse conf = alertaService.registrarAlertaCiudadana(
                request, null, null, "127.0.0.1"
        );
        AlertaCiudadanaSeguridad entidad = alertaRepository.findByCodigo(conf.getCodigo()).orElseThrow();

        AlertaPrioridadRequest reqPrioridad = AlertaPrioridadRequest.builder()
                .nuevaPrioridad(PrioridadIncidenciaSeguridad.CRITICA)
                .justificacion("Se constata cercanía a centro educativo y concurrencia de personas")
                .build();

        alertaService.cambiarPrioridad(entidad.getId(), reqPrioridad, usuarioAdmin.getUsername());

        AlertaCiudadanaSeguridad actualizada = alertaRepository.findById(entidad.getId()).orElseThrow();
        assertThat(actualizada.getPrioridad()).isEqualTo(PrioridadIncidenciaSeguridad.CRITICA);
    }

    @Test
    @Order(6)
    @DisplayName("6. Ciclo de solicitud de información complementaria y respuesta del ciudadano")
    void testSolicitudInformacionYRespuesta() {
        AlertaRegistroCiudadanoRequest reg = AlertaRegistroCiudadanoRequest.builder()
                .tipoReporteId(tipoRuidos.getId())
                .titulo("Reporte ambiguo")
                .descripcion("Hay ruido pero no se precisó si es en predio privado o vía pública.")
                .ubicacionReferencia("Cerca al mercado")
                .reportanteAnonimo(true)
                .build();

        AlertaConfirmacionResponse conf = alertaService.registrarAlertaCiudadana(
                reg, null, null, "127.0.0.1"
        );
        AlertaCiudadanaSeguridad entidad = alertaRepository.findByCodigo(conf.getCodigo()).orElseThrow();

        // Operador solicita info
        AlertaSolicitarInfoRequest reqInfo = AlertaSolicitarInfoRequest.builder()
                .solicitud("Favor de confirmar si el sonido proviene de un local comercial o vivienda particular.")
                .build();

        alertaService.solicitarInformacion(entidad.getId(), reqInfo, usuarioAdmin.getUsername());

        AlertaCiudadanaSeguridad enEspera = alertaRepository.findById(entidad.getId()).orElseThrow();
        assertThat(enEspera.getEstado()).isEqualTo(EstadoAlertaCiudadana.REQUIERE_INFORMACION);

        // Ciudadano responde
        AlertaDetailResponse detalleEnEspera = alertaService.obtenerDetalleAdmin(entidad.getId(), true);
        AlertaSolicitudInfoResponse solPendiente = detalleEnEspera.getSolicitudesInfo().get(0);
        AlertaRespuestaCiudadanoRequest respuestaReq = AlertaRespuestaCiudadanoRequest.builder()
                .codigo(conf.getCodigo())
                .pinConsulta(conf.getPinConsulta())
                .solicitudId(solPendiente.getId())
                .respuesta("Es un local comercial tipo bar que opera sin cartel visible.")
                .build();

        alertaService.responderSolicitudInformacion(respuestaReq, null);

        AlertaCiudadanaSeguridad respondida = alertaRepository.findById(entidad.getId()).orElseThrow();
        assertThat(respondida.getEstado()).isEqualTo(EstadoAlertaCiudadana.EN_REVISION);

        AlertaDetailResponse detalleFinal = alertaService.obtenerDetalleAdmin(entidad.getId(), true);
        assertThat(detalleFinal.getSolicitudesInfo().get(0).getEstado()).isEqualTo(EstadoSolicitudInfoAlerta.RESPONDIDA);
        assertThat(detalleFinal.getSolicitudesInfo().get(0).getRespuestaCiudadano()).contains("local comercial tipo bar");
    }

    @Test
    @Order(7)
    @DisplayName("7. Vinculación con Incidencia: Creación automática de nueva Incidencia desde la Alerta")
    void testVincularCreandoNuevaIncidencia() {
        AlertaRegistroCiudadanoRequest reg = AlertaRegistroCiudadanoRequest.builder()
                .tipoReporteId(tipoSeguridad.getId())
                .titulo("Tentativa de ingreso a local comunal")
                .descripcion("Candados forzados en puerta lateral del local comunal.")
                .ubicacionReferencia("Jr. Huamanga s/n")
                .sectorId(sector1.getId())
                .reportanteAnonimo(false)
                .reportanteNombre("Vigilante Comunal")
                .build();

        AlertaConfirmacionResponse conf = alertaService.registrarAlertaCiudadana(
                reg, null, null, "127.0.0.1"
        );
        AlertaCiudadanaSeguridad entidad = alertaRepository.findByCodigo(conf.getCodigo()).orElseThrow();

        AlertaVincularIncidenciaRequest reqVincular = AlertaVincularIncidenciaRequest.builder()
                .crearNuevaIncidencia(true)
                .motivo("Se verificaron daños materiales en cerraduras; amerita apertura de incidencia formal")
                .build();

        alertaService.vincularConIncidencia(entidad.getId(), reqVincular, usuarioAdmin.getUsername());

        AlertaCiudadanaSeguridad vinculada = alertaRepository.findById(entidad.getId()).orElseThrow();
        assertThat(vinculada.getEstado()).isEqualTo(EstadoAlertaCiudadana.VINCULADA);
        assertThat(vinculada.getIncidencia()).isNotNull();
        assertThat(vinculada.getIncidencia().getCodigo()).startsWith("INC-");
    }

    @Test
    @Order(8)
    @DisplayName("8. Cierre administrativo y descarte: validación de motivos y bitácora")
    void testCierreYDescarte() {
        // Alerta A para cerrar
        AlertaRegistroCiudadanoRequest regA = AlertaRegistroCiudadanoRequest.builder()
                .tipoReporteId(tipoRuidos.getId())
                .titulo("Alerta para cierre")
                .descripcion("Hecho verificado y cesado pacíficamente.")
                .ubicacionReferencia("Calle Real 10")
                .reportanteAnonimo(true)
                .build();
        AlertaConfirmacionResponse confA = alertaService.registrarAlertaCiudadana(regA, null, null, "127.0.0.1");
        AlertaCiudadanaSeguridad alertaA = alertaRepository.findByCodigo(confA.getCodigo()).orElseThrow();

        AlertaCierreRequest reqCierre = AlertaCierreRequest.builder()
                .motivo("Personal de serenazgo intervino en ronda preventiva y la música cesó")
                .resultadoCiudadano("Situación atendida mediante exhortación preventiva de serenazgo.")
                .build();
        alertaService.cerrarAlerta(alertaA.getId(), reqCierre, usuarioAdmin.getUsername());

        AlertaCiudadanaSeguridad cerrada = alertaRepository.findById(alertaA.getId()).orElseThrow();
        assertThat(cerrada.getEstado()).isEqualTo(EstadoAlertaCiudadana.CERRADA);
        assertThat(cerrada.getResultadoCiudadano()).isNotNull();

        // Alerta B para descartar
        AlertaRegistroCiudadanoRequest regB = AlertaRegistroCiudadanoRequest.builder()
                .tipoReporteId(tipoRuidos.getId())
                .titulo("Alerta para descarte")
                .descripcion("Reporte de prueba o duplicado no subsanable.")
                .ubicacionReferencia("Lugar ficticio")
                .reportanteAnonimo(true)
                .build();
        AlertaConfirmacionResponse confB = alertaService.registrarAlertaCiudadana(regB, null, null, "127.0.0.1");
        AlertaCiudadanaSeguridad alertaB = alertaRepository.findByCodigo(confB.getCodigo()).orElseThrow();

        AlertaDescarteRequest reqDescarte = AlertaDescarteRequest.builder()
                .motivoDescarte("Información no comprobable, dirección no existente en el distrito de Quinua")
                .build();
        alertaService.descartarAlerta(alertaB.getId(), reqDescarte, usuarioAdmin.getUsername());

        AlertaCiudadanaSeguridad descartada = alertaRepository.findById(alertaB.getId()).orElseThrow();
        assertThat(descartada.getEstado()).isEqualTo(EstadoAlertaCiudadana.DESCARTADA);
        assertThat(descartada.getMotivoDescarte()).contains("dirección no existente");
    }

    @Test
    @Order(9)
    @DisplayName("9. Dashboard KPIs y listado con filtros administrativos")
    void testDashboardYFiltros() {
        DashboardAlertasResponse kpis = alertaService.obtenerDashboardAlertas();
        assertThat(kpis).isNotNull();
        assertThat(kpis.getTotalAlertas()).isGreaterThanOrEqualTo(0);

        AlertaFilter filtro = AlertaFilter.builder()
                .estado(EstadoAlertaCiudadana.RECIBIDA)
                .build();

        Page<AlertaResponse> page = alertaService.listarConFiltrosAdmin(filtro, PageRequest.of(0, 10));
        assertThat(page).isNotNull();
    }

    @Test
    @Order(10)
    @DisplayName("10. Generación de reportes PDF y Excel: devuelve bytes válidos sin errores")
    void testExportacionesPdfYExcel() {
        AlertaRegistroCiudadanoRequest reg = AlertaRegistroCiudadanoRequest.builder()
                .tipoReporteId(tipoRuidos.getId())
                .titulo("Alerta para reporte PDF")
                .descripcion("Verificación de generación de documento de ficha técnica.")
                .ubicacionReferencia("Sector Plaza")
                .reportanteAnonimo(true)
                .build();
        AlertaConfirmacionResponse conf = alertaService.registrarAlertaCiudadana(reg, null, null, "127.0.0.1");
        AlertaCiudadanaSeguridad alerta = alertaRepository.findByCodigo(conf.getCodigo()).orElseThrow();

        byte[] pdfBytes = alertaService.exportarFichaPdf(alerta.getId());
        assertThat(pdfBytes).isNotEmpty();
        assertThat(new String(pdfBytes, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");

        byte[] excelBytes = alertaService.exportarAlertasExcel(LocalDate.now().minusDays(7), LocalDate.now().plusDays(1));
        assertThat(excelBytes).isNotEmpty();
    }
}
