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
import pe.gob.municipalidad.sigm.modules.serenazgo.service.IncidenciaSeguridadCiudadanaService;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IncidenciaSeguridadServiceTest {

    @Autowired
    private IncidenciaSeguridadCiudadanaService incidenciaService;

    @Autowired
    private TipoIncidenciaSeguridadRepository tipoIncidenciaRepository;

    @Autowired
    private SectorPatrullajeRepository sectorRepository;

    @Autowired
    private ZonaPatrullajeRepository zonaRepository;

    @Autowired
    private PersonalSerenazgoRepository personalRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private OficinaRepository oficinaRepository;

    private TipoIncidenciaSeguridad tipoRiesgo;
    private SectorPatrullaje sectorCentral;
    private ZonaPatrullaje zonaPlaza;
    private Usuario usuarioOperador;
    private PersonalSerenazgo serenoTurno;

    @BeforeEach
    void setUp() {
        // 1. Tipo de Incidencia
        tipoRiesgo = tipoIncidenciaRepository.findByCodigo("INC-ORDEN-01")
                .orElseGet(() -> tipoIncidenciaRepository.save(TipoIncidenciaSeguridad.builder()
                        .codigo("INC-ORDEN-01")
                        .nombre("Alteración del orden público")
                        .descripcion("Ruidos molestos, disturbios menores o discusiones en vía pública")
                        .categoria(CategoriaIncidenciaSeguridad.ORDEN_PUBLICO)
                        .prioridadSugerida(PrioridadIncidenciaSeguridad.MEDIA)
                        .activo(true)
                        .build()));

        // 2. Sector de Patrullaje
        sectorCentral = sectorRepository.findByCodigo("SEC-01")
                .orElseGet(() -> sectorRepository.save(SectorPatrullaje.builder()
                        .codigo("SEC-01")
                        .nombre("Sector 01 - Centro Histórico Quinua")
                        .descripcion("Área central del distrito de Quinua")
                        .estado(EstadoSectorPatrullaje.ACTIVO)
                        .colorHex("#3B82F6")
                        .build()));

        // 3. Zona de Patrullaje
        zonaPlaza = zonaRepository.findBySectorIdAndCodigo(sectorCentral.getId(), "ZON-01-01")
                .orElseGet(() -> zonaRepository.save(ZonaPatrullaje.builder()
                        .sector(sectorCentral)
                        .codigo("ZON-01-01")
                        .nombre("Zona Plaza de Armas y Alrededores")
                        .descripcion("Perímetro de la plaza central")
                        .estado(EstadoSectorPatrullaje.ACTIVO)
                        .build()));

        // 4. Usuario Operador
        if (usuarioOperador == null) {
            long ts = System.currentTimeMillis();
            Persona p = personaRepository.save(Persona.builder()
                    .nombres("Operador")
                    .apellidoPaterno("Serenazgo")
                    .apellidoMaterno("Quinua")
                    .numeroDocumento("95" + String.format("%06d", (int)(ts % 1000000)))
                    .tipoDocumento("DNI")
                    .correo("operador_" + ts + "@quinua.gob.pe")
                    .estado("ACTIVO")
                    .build());

            usuarioOperador = usuarioRepository.save(Usuario.builder()
                    .username("operador_inc_" + ts)
                    .passwordHash("$2a$10$xyz")
                    .persona(p)
                    .enabled(true)
                    .accountLocked(false)
                    .build());
        }

        // 5. Personal de Serenazgo
        if (serenoTurno == null) {
            long ts2 = System.currentTimeMillis() + 10;
            Persona pSereno = personaRepository.save(Persona.builder()
                    .nombres("Carlos")
                    .apellidoPaterno("Quispe")
                    .apellidoMaterno("Huamán")
                    .numeroDocumento("96" + String.format("%06d", (int)(ts2 % 1000000)))
                    .tipoDocumento("DNI")
                    .correo("sereno_" + ts2 + "@quinua.gob.pe")
                    .estado("ACTIVO")
                    .build());

            serenoTurno = personalRepository.save(PersonalSerenazgo.builder()
                    .persona(pSereno)
                    .codigoInterno("SER-" + String.format("%03d", (int)(ts2 % 1000)))
                    .estado(EstadoPersonalSerenazgo.ACTIVO)
                    .fechaIngreso(LocalDate.now().minusMonths(6))
                    .build());
        }
    }

    @Test
    @Order(1)
    @DisplayName("Debe registrar una nueva incidencia con correlativo institucional e historial inicial")
    void testRegistrarIncidenciaExitosamente() {
        IncidenciaCreateRequest req = IncidenciaCreateRequest.builder()
                .titulo("Griterío y música a alto volumen en la vía pública")
                .tipoIncidenciaId(tipoRiesgo.getId())
                .prioridad(PrioridadIncidenciaSeguridad.MEDIA)
                .descripcion("Vecinos reportan reunión social no autorizada con parlantes de alta potencia.")
                .fechaHoraOcurrencia(OffsetDateTime.now().minusHours(1))
                .origenHecho(OrigenHechoIncidencia.HECHO_REPORTADO)
                .canalReporte(CanalReporteIncidencia.LLAMADA_OPERADOR)
                .sectorId(sectorCentral.getId())
                .zonaId(zonaPlaza.getId())
                .ubicacionReferencia("Jr. Ayacucho cuadra 3, cerca a la panadería municipal")
                .latitud(new BigDecimal("-13.0456100"))
                .longitud(new BigDecimal("-74.1378200"))
                .reportanteAnonimo(false)
                .reportanteNombre("María Fernández Gómez")
                .reportanteTelefono("966987654")
                .reportanteDireccion("Jr. Ayacucho 340")
                .nivelConfidencialidad(NivelConfidencialidad.ORDINARIO)
                .personalReportaId(serenoTurno.getId())
                .build();

        IncidenciaResponse res = incidenciaService.registrarIncidencia(req, usuarioOperador.getUsername());

        assertThat(res).isNotNull();
        assertThat(res.getId()).isNotNull();
        assertThat(res.getCodigo()).startsWith("INC-");
        assertThat(res.getEstado()).isEqualTo(EstadoIncidenciaSeguridad.REGISTRADA);
        assertThat(res.getSectorNombre()).isEqualTo(sectorCentral.getNombre());
        assertThat(res.getZonaNombre()).isEqualTo(zonaPlaza.getNombre());

        // Validar Ficha de Detalle
        IncidenciaDetailResponse detalle = incidenciaService.obtenerDetallePorId(res.getId(), true);
        assertThat(detalle.getHistorial()).isNotEmpty();
        assertThat(detalle.getHistorial().get(0).getTipoEvento()).isEqualTo(TipoEventoHistorialIncidencia.REGISTRO);
    }

    @Test
    @Order(2)
    @DisplayName("Debe enmascarar datos del reportante anónimo o restringido para usuarios ordinarios")
    void testProteccionDatosPersonalesReportante() {
        IncidenciaCreateRequest req = IncidenciaCreateRequest.builder()
                .titulo("Sospecha de consumo de sustancias en parque")
                .tipoIncidenciaId(tipoRiesgo.getId())
                .prioridad(PrioridadIncidenciaSeguridad.BAJA)
                .descripcion("Dos personas sospechosas merodeando en inmediaciones del parque.")
                .ubicacionReferencia("Parque de la Memoria")
                .sectorId(sectorCentral.getId())
                .reportanteAnonimo(true)
                .reportanteNombre("Confidencial")
                .reportanteTelefono("966000000")
                .nivelConfidencialidad(NivelConfidencialidad.RESTRINGIDO)
                .build();

        IncidenciaResponse res = incidenciaService.registrarIncidencia(req, usuarioOperador.getUsername());

        // Consulta ordinaria (sin privilegios de inspección de datos sensibles)
        IncidenciaDetailResponse detalleAnonimo = incidenciaService.obtenerDetallePorId(res.getId(), false);
        assertThat(detalleAnonimo.getReportanteNombre()).isEqualTo("[RESERVADO / CONFIDENCIAL]");
        assertThat(detalleAnonimo.getReportanteTelefono()).isEqualTo("[OCULTO]");
    }

    @Test
    @Order(3)
    @DisplayName("Debe actualizar información general de la incidencia conservando bitácora de auditoría")
    void testActualizarIncidencia() {
        IncidenciaCreateRequest req = IncidenciaCreateRequest.builder()
                .titulo("Reporte inicial de cables sueltos")
                .tipoIncidenciaId(tipoRiesgo.getId())
                .prioridad(PrioridadIncidenciaSeguridad.BAJA)
                .descripcion("Cables de telefonía colgando cerca al poste.")
                .ubicacionReferencia("Jr. Sucre")
                .sectorId(sectorCentral.getId())
                .build();

        IncidenciaResponse res = incidenciaService.registrarIncidencia(req, usuarioOperador.getUsername());

        IncidenciaUpdateRequest updateReq = IncidenciaUpdateRequest.builder()
                .titulo("Cables de telefonía colgando con riesgo peatonal")
                .tipoIncidenciaId(tipoRiesgo.getId())
                .descripcion("Cables de telefonía caídos sobre la acera peatonal con riesgo de tropiezo.")
                .ubicacionReferencia("Jr. Sucre cuadra 2, frente a la botica")
                .sectorId(sectorCentral.getId())
                .motivoModificacion("Se precisa la dirección exacta y la condición de riesgo tras llamada vecinal adicional")
                .build();

        IncidenciaResponse actualizada = incidenciaService.actualizarIncidencia(res.getId(), updateReq, usuarioOperador.getUsername());
        assertThat(actualizada.getTitulo()).contains("riesgo peatonal");

        IncidenciaDetailResponse detalle = incidenciaService.obtenerDetallePorId(res.getId(), true);
        assertThat(detalle.getHistorial()).hasSizeGreaterThanOrEqualTo(2);
        assertThat(detalle.getHistorial().get(1).getTipoEvento()).isEqualTo(TipoEventoHistorialIncidencia.ACTUALIZACION);
    }

    @Test
    @Order(4)
    @DisplayName("Debe realizar transiciones de ciclo de vida de la incidencia registrando trazabilidad")
    void testTransicionEstadosIncidencia() {
        IncidenciaCreateRequest req = IncidenciaCreateRequest.builder()
                .titulo("Animal callejero agresivo en vía pública")
                .tipoIncidenciaId(tipoRiesgo.getId())
                .prioridad(PrioridadIncidenciaSeguridad.MEDIA)
                .descripcion("Perro de raza mediana agresivo con transeúntes.")
                .ubicacionReferencia("Av. Independencia")
                .sectorId(sectorCentral.getId())
                .build();

        IncidenciaResponse res = incidenciaService.registrarIncidencia(req, usuarioOperador.getUsername());

        // 1. REGISTRADA -> EN_EVALUACION
        CambioEstadoIncidenciaRequest estReq1 = CambioEstadoIncidenciaRequest.builder()
                .nuevoEstado(EstadoIncidenciaSeguridad.EN_EVALUACION)
                .motivo("Se envía patrulla para verificación preliminar en el lugar")
                .build();
        IncidenciaResponse paso1 = incidenciaService.cambiarEstado(res.getId(), estReq1, usuarioOperador.getUsername());
        assertThat(paso1.getEstado()).isEqualTo(EstadoIncidenciaSeguridad.EN_EVALUACION);

        // 2. EN_EVALUACION -> RESUELTA
        CambioEstadoIncidenciaRequest estReq2 = CambioEstadoIncidenciaRequest.builder()
                .nuevoEstado(EstadoIncidenciaSeguridad.RESUELTA)
                .motivo("Personal ubicó al propietario del animal quien procedió a ingresarlo a su domicilio")
                .build();
        IncidenciaResponse paso2 = incidenciaService.cambiarEstado(res.getId(), estReq2, usuarioOperador.getUsername());
        assertThat(paso2.getEstado()).isEqualTo(EstadoIncidenciaSeguridad.RESUELTA);
    }

    @Test
    @Order(5)
    @DisplayName("Debe modificar la prioridad administrativa con justificación obligatoria")
    void testCambiarPrioridad() {
        IncidenciaCreateRequest req = IncidenciaCreateRequest.builder()
                .titulo("Conato de pleito entre transeúntes")
                .tipoIncidenciaId(tipoRiesgo.getId())
                .prioridad(PrioridadIncidenciaSeguridad.BAJA)
                .descripcion("Discusión verbal en vía pública.")
                .ubicacionReferencia("Jr. Sucre")
                .sectorId(sectorCentral.getId())
                .build();

        IncidenciaResponse res = incidenciaService.registrarIncidencia(req, usuarioOperador.getUsername());

        CambioPrioridadIncidenciaRequest prioReq = CambioPrioridadIncidenciaRequest.builder()
                .nuevaPrioridad(PrioridadIncidenciaSeguridad.ALTA)
                .justificacion("Se reporta incremento de involucrados en la disputa y necesidad de apoyo preventivo urgente")
                .build();

        IncidenciaResponse priorizada = incidenciaService.cambiarPrioridad(res.getId(), prioReq, usuarioOperador.getUsername());
        assertThat(priorizada.getPrioridad()).isEqualTo(PrioridadIncidenciaSeguridad.ALTA);

        IncidenciaDetailResponse detalle = incidenciaService.obtenerDetallePorId(res.getId(), true);
        assertThat(detalle.getHistorial().stream().anyMatch(h -> h.getTipoEvento() == TipoEventoHistorialIncidencia.CAMBIO_PRIORIDAD)).isTrue();
    }

    @Test
    @Order(6)
    @DisplayName("Debe cerrar administrativamente una incidencia registrando conclusiones y motivo formal")
    void testCierreAdministrativo() {
        IncidenciaCreateRequest req = IncidenciaCreateRequest.builder()
                .titulo("Alarma vecinal activada por error")
                .tipoIncidenciaId(tipoRiesgo.getId())
                .prioridad(PrioridadIncidenciaSeguridad.BAJA)
                .descripcion("Alarma comunitaria sonando continuamente.")
                .ubicacionReferencia("Esquina mercado")
                .sectorId(sectorCentral.getId())
                .build();

        IncidenciaResponse res = incidenciaService.registrarIncidencia(req, usuarioOperador.getUsername());

        CierreIncidenciaRequest cierreReq = CierreIncidenciaRequest.builder()
                .motivoCierre("Verificación física completada. El comité vecinal procedió al reinicio de la sirena.")
                .resultadoAtencion("Sin daños a personas ni propiedad pública. Situación normalizada.")
                .build();

        IncidenciaResponse cerrada = incidenciaService.cerrarIncidencia(res.getId(), cierreReq, usuarioOperador.getUsername());
        assertThat(cerrada.getEstado()).isEqualTo(EstadoIncidenciaSeguridad.CERRADA);

        IncidenciaDetailResponse detalle = incidenciaService.obtenerDetallePorId(res.getId(), true);
        assertThat(detalle.getMotivoCierre()).isNotBlank();
        assertThat(detalle.getFechaCierre()).isNotNull();

        // No se debe permitir volver a cambiar estado de una incidencia ya cerrada
        CambioEstadoIncidenciaRequest estReq = CambioEstadoIncidenciaRequest.builder()
                .nuevoEstado(EstadoIncidenciaSeguridad.EN_EVALUACION)
                .motivo("Intento de reapertura indebida")
                .build();
        assertThatThrownBy(() -> incidenciaService.cambiarEstado(res.getId(), estReq, usuarioOperador.getUsername()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @Order(7)
    @DisplayName("Debe descartar formalmente un registro duplicado o improcedente")
    void testDescartarIncidencia() {
        IncidenciaCreateRequest req = IncidenciaCreateRequest.builder()
                .titulo("Llamada reiterativa de broma")
                .tipoIncidenciaId(tipoRiesgo.getId())
                .prioridad(PrioridadIncidenciaSeguridad.BAJA)
                .descripcion("Comunicación con sonidos ininteligibles.")
                .ubicacionReferencia("Desconocida")
                .sectorId(sectorCentral.getId())
                .build();

        IncidenciaResponse res = incidenciaService.registrarIncidencia(req, usuarioOperador.getUsername());

        DescarteIncidenciaRequest descarteReq = DescarteIncidenciaRequest.builder()
                .motivoDescarte("Llamada calificada como improcedente y falsa alarma tras corroboración del operador.")
                .build();

        IncidenciaResponse descartada = incidenciaService.descartarIncidencia(res.getId(), descarteReq, usuarioOperador.getUsername());
        assertThat(descartada.getEstado()).isEqualTo(EstadoIncidenciaSeguridad.DESCARTADA);
    }

    @Test
    @Order(8)
    @DisplayName("Debe detectar posibles duplicados en ventana de 24 horas para el mismo tipo y sector")
    void testDeteccionPosiblesDuplicados() {
        OffsetDateTime ahora = OffsetDateTime.now();

        IncidenciaCreateRequest req1 = IncidenciaCreateRequest.builder()
                .titulo("Foco de alumbrado público parpadeando en la plaza")
                .tipoIncidenciaId(tipoRiesgo.getId())
                .prioridad(PrioridadIncidenciaSeguridad.BAJA)
                .descripcion("Lámpara del poste 12 en mal estado.")
                .fechaHoraOcurrencia(ahora)
                .ubicacionReferencia("Plaza Central")
                .sectorId(sectorCentral.getId())
                .build();

        incidenciaService.registrarIncidencia(req1, usuarioOperador.getUsername());

        // Búsqueda de duplicados con los mismos parámetros
        List<PosibleDuplicadoResponse> duplicados = incidenciaService.buscarPosiblesDuplicados(
                tipoRiesgo.getId(),
                sectorCentral.getId(),
                ahora.plusMinutes(15),
                null
        );

        assertThat(duplicados).isNotEmpty();
        assertThat(duplicados.get(0).getTitulo()).contains("Foco de alumbrado");
    }

    @Test
    @Order(9)
    @DisplayName("Debe permitir registrar notas de seguimiento administrativo y archivos adjuntos")
    void testAgregarComentariosYAdjuntos() {
        IncidenciaCreateRequest req = IncidenciaCreateRequest.builder()
                .titulo("Incidencia con evidencia fotográfica")
                .tipoIncidenciaId(tipoRiesgo.getId())
                .prioridad(PrioridadIncidenciaSeguridad.MEDIA)
                .descripcion("Se adjunta reporte gráfico preliminar.")
                .ubicacionReferencia("Jr. Bolívar")
                .sectorId(sectorCentral.getId())
                .build();

        IncidenciaResponse res = incidenciaService.registrarIncidencia(req, usuarioOperador.getUsername());

        // Comentario
        AgregarComentarioRequest comReq = AgregarComentarioRequest.builder()
                .comentario("Se coordinó telefónicamente con la cuadrilla de guardia para inspección visual.")
                .esInterno(true)
                .build();
        IncidenciaComentarioResponse comRes = incidenciaService.agregarComentario(res.getId(), comReq, usuarioOperador.getUsername());
        assertThat(comRes.getId()).isNotNull();

        // Archivo Adjunto
        byte[] contenidoDummy = "CONTENIDO_SIMULADO_EVIDENCIA".getBytes(StandardCharsets.UTF_8);
        IncidenciaDocumentoResponse docRes = incidenciaService.adjuntarDocumento(
                res.getId(),
                "foto_constatacion.jpg",
                "image/jpeg",
                contenidoDummy,
                "Fotografía del estado del poste",
                true,
                usuarioOperador.getUsername()
        );

        assertThat(docRes.getId()).isNotNull();
        assertThat(docRes.getHashSha256()).isNotBlank();
        assertThat(docRes.getEsFotografia()).isTrue();
    }

    @Test
    @Order(10)
    @DisplayName("Debe exportar la ficha técnica en PDF y la planilla en Excel sin errores")
    void testExportacionPdfYExcel() throws Exception {
        IncidenciaCreateRequest req = IncidenciaCreateRequest.builder()
                .titulo("Incidencia para prueba de exportación documental")
                .tipoIncidenciaId(tipoRiesgo.getId())
                .prioridad(PrioridadIncidenciaSeguridad.ALTA)
                .descripcion("Verificación de generación de documentos PDF y XLSX.")
                .ubicacionReferencia("Plaza Mayor")
                .sectorId(sectorCentral.getId())
                .build();

        IncidenciaResponse res = incidenciaService.registrarIncidencia(req, usuarioOperador.getUsername());

        // Exportar PDF
        byte[] pdf = incidenciaService.exportarFichaPdf(res.getId());
        assertThat(pdf).isNotNull();
        assertThat(pdf.length).isGreaterThan(100);

        // Exportar Excel
        byte[] excel = incidenciaService.exportarIncidenciasExcel(LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
        assertThat(excel).isNotNull();
        assertThat(excel.length).isGreaterThan(100);
    }
}
