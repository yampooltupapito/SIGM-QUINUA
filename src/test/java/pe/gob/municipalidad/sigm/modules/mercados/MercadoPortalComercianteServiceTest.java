package pe.gob.municipalidad.sigm.modules.mercados;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Rol;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.common.repository.RolRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.*;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;
import pe.gob.municipalidad.sigm.modules.mercados.service.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MercadoPortalComercianteServiceTest {

    @Autowired private ComerciantePortalContextService contextService;
    @Autowired private PortalComercianteDashboardService dashboardService;
    @Autowired private PortalComerciantePerfilService perfilService;
    @Autowired private PortalComerciantePuestoService puestoService;
    @Autowired private PortalComercianteContratoService contratoService;
    @Autowired private PortalComercianteAutorizacionService autorizacionService;
    @Autowired private PortalComercianteFinanzasService finanzasService;
    @Autowired private PortalComercianteSolicitudService solicitudService;
    @Autowired private PortalComercianteIncidenciaService incidenciaService;
    @Autowired private PortalComercianteDocumentoService documentoService;

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PersonaRepository personaRepository;
    @Autowired private RolRepository rolRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Autowired private ComercianteRepository comercianteRepository;
    @Autowired private VinculoUsuarioComercianteRepository vinculoRepository;
    @Autowired private MercadoMunicipalRepository mercadoRepository;
    @Autowired private PuestoComercialRepository puestoRepository;
    @Autowired private AsignacionPuestoRepository asignacionRepository;
    @Autowired private ContratoOcupacionRepository contratoRepository;
    @Autowired private AutorizacionComercialRepository autorizacionRepository;
    @Autowired private ObligacionMercadoRepository obligacionRepository;
    @Autowired private PagoMercadoRepository pagoRepository;
    @Autowired private ConceptoCobroMercadoRepository conceptoRepository;
    @Autowired private TipoIncidenciaMercadoRepository tipoIncidenciaRepository;

    private Usuario usuarioA;
    private Usuario usuarioB;
    private Comerciante comercianteA;
    private Comerciante comercianteB;
    private MercadoMunicipal mercado;
    private PuestoComercial puestoA;
    private PuestoComercial puestoB;
    private AsignacionPuesto asignacionA;
    private AsignacionPuesto asignacionB;
    private ContratoOcupacion contratoA;
    private ContratoOcupacion contratoB;
    private AutorizacionComercial autorizacionA;
    private ObligacionMercado obligacionA;
    private ObligacionMercado obligacionB;
    private PagoMercado pagoA;
    private TipoIncidenciaMercado tipoIncidencia;

    @BeforeEach
    void setUp() {
        long ts = System.currentTimeMillis();

        Rol rolComerciante = rolRepository.findByCodigo("ROLE_COMERCIANTE").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_COMERCIANTE").nombre("Comerciante").estado("ACTIVO").build()));

        // Persona y Usuario A
        Persona personaA = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento(String.valueOf(ts % 100000000))
                .nombres("Juan")
                .apellidoPaterno("Quispe")
                .apellidoMaterno("Morales")
                .correo("juan.quispe" + ts + "@test.gob.pe")
                .telefono("966123456")
                .direccion("Jr. Sucre 123, Quinua")
                .estado("ACTIVO")
                .build());

        usuarioA = usuarioRepository.save(Usuario.builder()
                .username("juanquispe_" + ts)
                .passwordHash(passwordEncoder.encode("Password123!"))
                .persona(personaA)
                .roles(Set.of(rolComerciante))
                .enabled(true)
                .accountLocked(false)
                .build());

        // Comerciante A
        comercianteA = comercianteRepository.save(Comerciante.builder()
                .persona(personaA)
                .usuario(usuarioA)
                .codigoComerciante("COM-A-" + (ts % 10000))
                .tipoComerciante(TipoComerciante.PERSONA_NATURAL)
                .estado(EstadoComerciante.ACTIVO)
                .build());

        // Vínculo A
        vinculoRepository.save(VinculoUsuarioComerciante.builder()
                .usuario(usuarioA)
                .comerciante(comercianteA)
                .tipoVinculo(TipoVinculoComerciante.TITULAR)
                .estado(EstadoVinculoComerciante.VERIFICADO)
                .fechaInicio(LocalDate.now().minusDays(30))
                .fechaVerificacion(OffsetDateTime.now())
                .verificadoPor(usuarioA)
                .build());

        // Persona, Usuario y Comerciante B
        Persona personaB = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento(String.valueOf((ts + 1) % 100000000))
                .nombres("María")
                .apellidoPaterno("Condori")
                .apellidoMaterno("Rojas")
                .correo("maria.condori" + ts + "@test.gob.pe")
                .telefono("966654321")
                .direccion("Av. Huamanga 456")
                .estado("ACTIVO")
                .build());

        usuarioB = usuarioRepository.save(Usuario.builder()
                .username("mariacondori_" + ts)
                .passwordHash(passwordEncoder.encode("Password123!"))
                .persona(personaB)
                .roles(Set.of(rolComerciante))
                .enabled(true)
                .accountLocked(false)
                .build());

        comercianteB = comercianteRepository.save(Comerciante.builder()
                .persona(personaB)
                .usuario(usuarioB)
                .codigoComerciante("COM-B-" + (ts % 10000))
                .tipoComerciante(TipoComerciante.PERSONA_NATURAL)
                .estado(EstadoComerciante.ACTIVO)
                .build());

        vinculoRepository.save(VinculoUsuarioComerciante.builder()
                .usuario(usuarioB)
                .comerciante(comercianteB)
                .tipoVinculo(TipoVinculoComerciante.TITULAR)
                .estado(EstadoVinculoComerciante.VERIFICADO)
                .fechaInicio(LocalDate.now().minusDays(30))
                .fechaVerificacion(OffsetDateTime.now())
                .verificadoPor(usuarioB)
                .build());

        // Mercado y Puestos
        mercado = mercadoRepository.save(MercadoMunicipal.builder()
                .nombre("Mercado Central de Quinua " + ts)
                .codigo("MC-" + (ts % 1000))
                .direccion("Plaza Principal s/n")
                .estado(EstadoMercado.ACTIVO)
                .build());

        puestoA = puestoRepository.save(PuestoComercial.builder()
                .mercado(mercado)
                .codigo("PA-" + (ts % 1000))
                .numero("A-01")
                .areaMetrosCuadrados(BigDecimal.valueOf(6.5))
                .estado(EstadoPuesto.OCUPADO)
                .build());

        puestoB = puestoRepository.save(PuestoComercial.builder()
                .mercado(mercado)
                .codigo("PB-" + (ts % 1000))
                .numero("B-01")
                .areaMetrosCuadrados(BigDecimal.valueOf(8.0))
                .estado(EstadoPuesto.OCUPADO)
                .build());

        // Asignaciones
        asignacionA = asignacionRepository.save(AsignacionPuesto.builder()
                .codigoAsignacion("ASIG-A-" + (ts % 10000))
                .puesto(puestoA)
                .comerciante(comercianteA)
                .fechaInicio(LocalDate.now().minusDays(15))
                .fechaFin(LocalDate.now().plusMonths(11))
                .estado(EstadoAsignacion.VIGENTE)
                .tipoAsignacion(TipoAsignacion.REGULAR)
                .build());

        asignacionB = asignacionRepository.save(AsignacionPuesto.builder()
                .codigoAsignacion("ASIG-B-" + (ts % 10000))
                .puesto(puestoB)
                .comerciante(comercianteB)
                .fechaInicio(LocalDate.now().minusDays(15))
                .fechaFin(LocalDate.now().plusMonths(11))
                .estado(EstadoAsignacion.VIGENTE)
                .tipoAsignacion(TipoAsignacion.REGULAR)
                .build());

        // Contratos
        contratoA = new ContratoOcupacion();
        contratoA.setCodigoContrato("CON-A-" + (ts % 10000));
        contratoA.setCodigoSeguro("SEC-A-" + UUID.randomUUID().toString().substring(0, 8));
        contratoA.setAsignacion(asignacionA);
        contratoA.setPuesto(puestoA);
        contratoA.setComerciante(comercianteA);
        contratoA.setTipoContratoCodigo("ESTANDAR");
        contratoA.setFechaElaboracion(LocalDate.now().minusDays(15));
        contratoA.setFechaInicio(LocalDate.now().minusDays(15));
        contratoA.setFechaFin(LocalDate.now().plusMonths(11));
        contratoA.setEstado(EstadoContrato.VIGENTE);
        contratoA = contratoRepository.save(contratoA);

        contratoB = new ContratoOcupacion();
        contratoB.setCodigoContrato("CON-B-" + (ts % 10000));
        contratoB.setCodigoSeguro("SEC-B-" + UUID.randomUUID().toString().substring(0, 8));
        contratoB.setAsignacion(asignacionB);
        contratoB.setPuesto(puestoB);
        contratoB.setComerciante(comercianteB);
        contratoB.setTipoContratoCodigo("ESTANDAR");
        contratoB.setFechaElaboracion(LocalDate.now().minusDays(15));
        contratoB.setFechaInicio(LocalDate.now().minusDays(15));
        contratoB.setFechaFin(LocalDate.now().plusMonths(11));
        contratoB.setEstado(EstadoContrato.VIGENTE);
        contratoB = contratoRepository.save(contratoB);

        // Autorización A
        autorizacionA = new AutorizacionComercial();
        autorizacionA.setCodigoAutorizacion("AUT-A-" + (ts % 10000));
        autorizacionA.setNumeroAutorizacion("001-" + ts);
        autorizacionA.setCodigoSeguro("SEC-AUT-A-" + UUID.randomUUID().toString().substring(0, 8));
        autorizacionA.setTipoAutorizacion("PADRON_MUNICIPAL");
        autorizacionA.setAsignacion(asignacionA);
        autorizacionA.setPuesto(puestoA);
        autorizacionA.setComerciante(comercianteA);
        autorizacionA.setFechaEmision(LocalDate.now().minusDays(10));
        autorizacionA.setFechaInicio(LocalDate.now().minusDays(10));
        autorizacionA.setFechaFin(LocalDate.now().plusMonths(11));
        autorizacionA.setEstado(EstadoAutorizacion.VIGENTE);
        autorizacionA = autorizacionRepository.save(autorizacionA);

        // Concepto y Obligaciones
        ConceptoCobroMercado concepto = conceptoRepository.findAll().stream().findFirst().orElseGet(() ->
                conceptoRepository.save(ConceptoCobroMercado.builder()
                        .codigo("TARIFA_DIARIA")
                        .nombre("Tarifa Diaria de Mercado")
                        .tipoConcepto(TipoConceptoCobro.USO_PUESTO_COMERCIAL)
                        .estado("ACTIVO")
                        .build()));

        obligacionA = obligacionRepository.save(ObligacionMercado.builder()
                .codigoObligacion("OBL-A-" + (ts % 10000))
                .comerciante(comercianteA)
                .puesto(puestoA)
                .concepto(concepto)
                .montoOriginal(BigDecimal.valueOf(50.0))
                .saldoPendiente(BigDecimal.valueOf(50.0))
                .fechaEmision(LocalDate.now().minusDays(5))
                .fechaVencimiento(LocalDate.now().plusDays(25))
                .estado(EstadoObligacion.PENDIENTE)
                .build());

        obligacionB = obligacionRepository.save(ObligacionMercado.builder()
                .codigoObligacion("OBL-B-" + (ts % 10000))
                .comerciante(comercianteB)
                .puesto(puestoB)
                .concepto(concepto)
                .montoOriginal(BigDecimal.valueOf(60.0))
                .saldoPendiente(BigDecimal.valueOf(60.0))
                .fechaEmision(LocalDate.now().minusDays(5))
                .fechaVencimiento(LocalDate.now().plusDays(25))
                .estado(EstadoObligacion.PENDIENTE)
                .build());

        // Pago A
        pagoA = pagoRepository.save(PagoMercado.builder()
                .codigoPago("PAG-A-" + (ts % 10000))
                .codigoSeguro("SEC-PAG-A-" + UUID.randomUUID().toString().substring(0, 8))
                .comerciante(comercianteA)
                .montoRecibido(BigDecimal.valueOf(100.0))
                .montoAplicado(BigDecimal.valueOf(100.0))
                .fechaPago(OffsetDateTime.now().minusDays(2))
                .medioPago(MedioPagoMercado.EFECTIVO)
                .estado(EstadoPagoMercado.CONFIRMADO)
                .usuarioRegistro("ADMIN")
                .build());

        // Tipo Incidencia
        tipoIncidencia = tipoIncidenciaRepository.findAll().stream().findFirst().orElseGet(() ->
                tipoIncidenciaRepository.save(TipoIncidenciaMercado.builder()
                        .codigo("ELECTRICO")
                        .nombre("Falla de Alumbrado")
                        .build()));

        // Autenticar por defecto como Usuario A
        autenticarComo(usuarioA);
    }

    private void autenticarComo(Usuario usuario) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        usuario.getUsername(),
                        "Password123!",
                        List.of(new SimpleGrantedAuthority("ROLE_COMERCIANTE"))
                )
        );
    }

    @Test
    @DisplayName("Consulta de perfil propio con DNI parcialmente enmascarado por privacidad")
    void testConsultaPerfilConDniEnmascarado() {
        PortalComerciantePerfilResponse perfil = perfilService.obtenerPerfil();

        assertThat(perfil).isNotNull();
        assertThat(perfil.getNombreCompletoORazonSocial()).contains("Juan Quispe");
        assertThat(perfil.getDocumentoEnmascarado()).contains("***");
        assertThat(perfil.getTelefono()).isEqualTo("966123456");
    }

    @Test
    @DisplayName("Actualización autorizada de datos de contacto del comerciante con auditoría")
    void testActualizarContactoComerciante() {
        PortalComercianteContactoUpdateRequest req = PortalComercianteContactoUpdateRequest.builder()
                .telefono("966999888")
                .correoElectronico("nuevo.correo@quinua.gob.pe")
                .direccion("Av. Centenario 456, Quinua")
                .build();

        PortalComerciantePerfilResponse actualizado = perfilService.actualizarContacto(req);

        assertThat(actualizado.getTelefono()).isEqualTo("966999888");
        assertThat(actualizado.getCorreoElectronico()).isEqualTo("nuevo.correo@quinua.gob.pe");
        assertThat(actualizado.getDireccion()).isEqualTo("Av. Centenario 456, Quinua");
    }

    @Test
    @DisplayName("Consulta de puestos propios asignados")
    void testConsultaPuestosPropios() {
        List<PortalComerciantePuestoResponse> puestos = puestoService.listarMisPuestos();

        assertThat(puestos).isNotEmpty();
        assertThat(puestos.get(0).getPuestoId()).isEqualTo(puestoA.getId());
        assertThat(puestos.get(0).getEstadoAsignacion()).isEqualTo(EstadoAsignacion.VIGENTE);
    }

    @Test
    @DisplayName("Rechazo estricto IDOR al intentar consultar detalle de un puesto ajeno")
    void testRechazoPuestoAjenoIdor() {
        // Usuario A intenta acceder al puesto B
        assertThatThrownBy(() -> puestoService.obtenerDetallePuesto(puestoB.getId()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("no pertenece al comerciante en sesión");
    }

    @Test
    @DisplayName("Consulta de contratos de ocupación formalizados propios")
    void testConsultaContratosPropios() {
        Page<PortalComercianteContratoResponse> page = contratoService.listarMisContratos(EstadoContrato.VIGENTE, PageRequest.of(0, 10));

        assertThat(page.getContent()).isNotEmpty();
        assertThat(page.getContent().get(0).getContratoId()).isEqualTo(contratoA.getId());
    }

    @Test
    @DisplayName("Rechazo estricto IDOR al intentar consultar contrato ajeno")
    void testRechazoContratoAjenoIdor() {
        assertThatThrownBy(() -> contratoService.obtenerDetalleContrato(contratoB.getId()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("no corresponde al comerciante en sesión");
    }

    @Test
    @DisplayName("Consulta de autorizaciones comerciales propias")
    void testConsultaAutorizacionesPropias() {
        Page<PortalComercianteAutorizacionResponse> page = autorizacionService.listarMisAutorizaciones(null, PageRequest.of(0, 10));

        assertThat(page.getContent()).isNotEmpty();
        assertThat(page.getContent().get(0).getAutorizacionId()).isEqualTo(autorizacionA.getId());
        assertThat(page.getContent().get(0).isVigente()).isTrue();
    }

    @Test
    @DisplayName("Rechazo estricto IDOR al consultar deuda u obligación ajena")
    void testRechazoObligacionAjenaIdor() {
        assertThatThrownBy(() -> finanzasService.obtenerDetalleObligacion(obligacionB.getId()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("no corresponde al comerciante en sesión");
    }

    @Test
    @DisplayName("Consulta de estado de cuenta corriente y generación de resumen PDF")
    void testEstadoCuentaYGeneracionPdf() {
        PortalComercianteEstadoCuentaResponse estadoCuenta = finanzasService.obtenerEstadoCuenta();

        assertThat(estadoCuenta).isNotNull();
        assertThat(estadoCuenta.getDeudaTotalExigible()).isEqualByComparingTo(BigDecimal.valueOf(50.0));
        assertThat(estadoCuenta.getTotalHistoricoPagado()).isEqualByComparingTo(BigDecimal.valueOf(100.0));

        byte[] pdf = finanzasService.generarEstadoCuentaPdf();
        assertThat(pdf).isNotNull();
        assertThat(pdf.length).isGreaterThan(100);
    }

    @Test
    @DisplayName("Descarga de constancia oficial de pago propio y rechazo de comprobante ajeno")
    void testDescargaConstanciaPagoYRechazoAjeno() {
        byte[] pdf = finanzasService.descargarConstanciaPagoPdf(pagoA.getId());
        assertThat(pdf).isNotNull();
        assertThat(pdf.length).isGreaterThan(100);

        // Cambiar a usuario B e intentar descargar el pago del usuario A
        autenticarComo(usuarioB);
        assertThatThrownBy(() -> finanzasService.descargarConstanciaPagoPdf(pagoA.getId()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("no corresponde al comerciante en sesión");
    }

    @Test
    @DisplayName("Presentación de solicitud de renovación sobre asignación propia")
    void testPresentacionSolicitudRenovacion() {
        PortalComercianteRenovacionRequest req = PortalComercianteRenovacionRequest.builder()
                .asignacionId(asignacionA.getId())
                .plazoMesesSolicitado(12)
                .justificacion("Continuidad del servicio comercial de abarrotes para la población.")
                .build();

        SolicitudGestionOcupacionResponse resp = solicitudService.presentarSolicitudRenovacion(req);

        assertThat(resp).isNotNull();
        assertThat(resp.getCodigoSolicitud()).isNotNull();
    }

    @Test
    @DisplayName("Rechazo al intentar solicitar renovación sobre una asignación ajena")
    void testRechazoRenovacionAsignacionAjena() {
        PortalComercianteRenovacionRequest req = PortalComercianteRenovacionRequest.builder()
                .asignacionId(asignacionB.getId()) // Asignación de Comerciante B
                .plazoMesesSolicitado(12)
                .justificacion("Intento de renovar puesto ajeno.")
                .build();

        assertThatThrownBy(() -> solicitudService.presentarSolicitudRenovacion(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("No puede presentar renovación sobre una asignación que no le pertenece");
    }

    @Test
    @DisplayName("Reporte de incidencia operativa y rechazo si asigna puesto ajeno")
    void testReporteIncidenciaYRechazoPuestoAjeno() {
        PortalComercianteIncidenciaRequest reqOk = PortalComercianteIncidenciaRequest.builder()
                .mercadoId(mercado.getId())
                .puestoId(puestoA.getId())
                .tipoIncidenciaId(tipoIncidencia.getId())
                .descripcion("Falla en el cableado de luz del pasadizo cercano.")
                .build();

        IncidenciaResponse resp = incidenciaService.reportarIncidencia(reqOk);
        assertThat(resp).isNotNull();
        assertThat(resp.getCodigoIncidencia()).isNotNull();

        // Intento de asociar a puesto ajeno
        PortalComercianteIncidenciaRequest reqIdor = PortalComercianteIncidenciaRequest.builder()
                .mercadoId(mercado.getId())
                .puestoId(puestoB.getId())
                .tipoIncidenciaId(tipoIncidencia.getId())
                .descripcion("Intento de asociar a puesto ajeno.")
                .build();

        assertThatThrownBy(() -> incidenciaService.reportarIncidencia(reqIdor))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("no pertenece al comerciante en sesión");
    }

    @Test
    @DisplayName("Rechazo de acceso si el usuario no tiene un vínculo verificado y vigente")
    void testRechazoUsuarioSinVinculo() {
        Persona personaSin = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento(String.valueOf(System.currentTimeMillis() % 100000000))
                .nombres("Pedro")
                .apellidoPaterno("Sin")
                .apellidoMaterno("Vinculo")
                .correo("pedro" + System.currentTimeMillis() + "@test.gob.pe")
                .estado("ACTIVO")
                .build());

        Usuario usuarioSinVinculo = usuarioRepository.save(Usuario.builder()
                .username("anonimo_sin_vinculo_" + System.currentTimeMillis())
                .passwordHash(passwordEncoder.encode("Password123!"))
                .persona(personaSin)
                .enabled(true)
                .accountLocked(false)
                .build());

        autenticarComo(usuarioSinVinculo);

        assertThatThrownBy(() -> contextService.getComercianteActivo())
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("No cuenta con un registro comercial verificado vinculado");
    }
}
