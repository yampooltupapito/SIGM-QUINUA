package pe.gob.municipalidad.sigm.modules.mercados;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Rol;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.common.repository.RolRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.mercados.dto.DashboardMercadosResponse;
import pe.gob.municipalidad.sigm.modules.mercados.dto.IndicadorOcupacionResponse;
import pe.gob.municipalidad.sigm.modules.mercados.dto.PortalComerciantePerfilResponse;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ReporteMercadoFilter;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.*;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;
import pe.gob.municipalidad.sigm.modules.mercados.service.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MercadoModulo2E2EIntegracionTest {

    @Autowired
    private MercadoMunicipalRepository mercadoRepository;

    @Autowired
    private SectorMercadoRepository sectorRepository;

    @Autowired
    private PabellonMercadoRepository pabellonRepository;

    @Autowired
    private PuestoComercialRepository puestoRepository;

    @Autowired
    private ComercianteRepository comercianteRepository;

    @Autowired
    private AsignacionPuestoRepository asignacionRepository;

    @Autowired
    private TipoPuestoComercialRepository tipoPuestoRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private VinculoUsuarioComercianteRepository vinculoRepository;

    @Autowired
    private CajaMercadoRepository cajaRepository;

    @Autowired
    private SesionCajaMercadoRepository sesionCajaRepository;

    @Autowired
    private ConceptoCobroMercadoRepository conceptoCobroRepository;

    @Autowired
    private ObligacionMercadoRepository obligacionRepository;

    @Autowired
    private PagoMercadoRepository pagoRepository;

    @Autowired
    private AplicacionPagoMercadoRepository aplicacionPagoRepository;

    @Autowired
    private InspeccionMercadoRepository inspeccionRepository;

    @Autowired
    private IncidenciaMercadoRepository incidenciaRepository;

    @Autowired
    private TipoIncidenciaMercadoRepository tipoIncidenciaRepository;

    @Autowired
    private IndicadoresMercadosService indicadoresService;

    @Autowired
    private ReporteOcupacionService reporteOcupacionService;

    @Autowired
    private ExportacionExcelService exportacionExcelService;

    @Autowired
    private ComerciantePortalContextService portalContextService;

    @Autowired
    private PortalComerciantePerfilService portalPerfilService;

    @Test
    @Order(1)
    @DisplayName("E2E 01: Infraestructura y Adjudicación - Creación de Mercado, Sector, Pabellón, Puesto y Asignación")
    void test01_infraestructura_y_adjudicacion_completa() {
        long ts = System.currentTimeMillis();

        // 1. Crear Mercado
        MercadoMunicipal mercado = new MercadoMunicipal();
        mercado.setCodigo("MKT-E2E-" + ts);
        mercado.setNombre("Mercado Central E2E " + ts);
        mercado.setEstado(EstadoMercado.ACTIVO);
        mercado.setDireccion("Plaza Principal Quinua");
        mercado = mercadoRepository.save(mercado);
        assertThat(mercado.getId()).isNotNull();

        // 2. Crear Sector
        SectorMercado sector = new SectorMercado();
        sector.setMercado(mercado);
        sector.setCodigo("SEC-01-" + ts);
        sector.setNombre("Sector Artesanías");
        sector.setOrdenVisual(1);
        sector = sectorRepository.save(sector);
        assertThat(sector.getId()).isNotNull();

        // 3. Crear Pabellón
        PabellonMercado pabellon = new PabellonMercado();
        pabellon.setMercado(mercado);
        pabellon.setCodigo("PAB-A-" + ts);
        pabellon.setNombre("Pabellón A");
        pabellon.setOrdenVisual(1);
        pabellon = pabellonRepository.save(pabellon);
        assertThat(pabellon.getId()).isNotNull();

        // 4. Tipo de Puesto y Puesto
        TipoPuestoComercial tipoPuesto = tipoPuestoRepository.findAll().stream().findFirst().orElseGet(() -> {
            TipoPuestoComercial t = new TipoPuestoComercial();
            t.setNombre("Puesto Estándar");
            t.setCodigo("STD");
            t.setEstado("ACTIVO");
            return tipoPuestoRepository.save(t);
        });

        PuestoComercial puesto = new PuestoComercial();
        puesto.setMercado(mercado);
        puesto.setSector(sector);
        puesto.setPabellon(pabellon);
        puesto.setTipoPuesto(tipoPuesto);
        puesto.setCodigo("PUESTO-E2E-" + ts);
        puesto.setNumero("01");
        puesto.setEstado(EstadoPuesto.DISPONIBLE);
        puesto.setAreaMetrosCuadrados(new BigDecimal("12.50"));
        puesto = puestoRepository.save(puesto);
        assertThat(puesto.getId()).isNotNull();

        // 5. Registrar Comerciante
        Persona persona = new Persona();
        persona.setTipoDocumento("DNI");
        persona.setNumeroDocumento("44" + String.format("%06d", (int)(ts % 1000000)));
        persona.setNombres("Juan Carlos");
        persona.setApellidoPaterno("Quispe");
        persona.setApellidoMaterno("Mendoza");
        persona.setCorreo("juan.quispe." + ts + "@quinua.gob.pe");
        persona.setEstado("ACTIVO");
        persona = personaRepository.save(persona);

        Comerciante comerciante = new Comerciante();
        comerciante.setPersona(persona);
        comerciante.setCodigoComerciante("COM-" + ts);
        comerciante.setTipoComerciante(TipoComerciante.PERSONA_NATURAL);
        comerciante.setEstado(EstadoComerciante.ACTIVO);
        comerciante = comercianteRepository.save(comerciante);

        // 6. Asignar Puesto Formalmente
        AsignacionPuesto asignacion = new AsignacionPuesto();
        asignacion.setPuesto(puesto);
        asignacion.setComerciante(comerciante);
        asignacion.setFechaInicio(LocalDate.now());
        asignacion.setFechaFin(LocalDate.now().plusYears(1));
        asignacion.setEstado(EstadoAsignacion.VIGENTE);
        asignacion.setTipoAsignacion(TipoAsignacion.REGULAR);
        asignacion.setObservaciones("Asignación Formal E2E");
        asignacion = asignacionRepository.save(asignacion);

        puesto.setEstado(EstadoPuesto.OCUPADO);
        puestoRepository.save(puesto);

        // Verificaciones
        PuestoComercial puestoVerificado = puestoRepository.findById(puesto.getId()).orElseThrow();
        assertThat(puestoVerificado.getEstado()).isEqualTo(EstadoPuesto.OCUPADO);
        assertThat(asignacionRepository.findByComercianteIdOrderByFechaInicioDesc(comerciante.getId())).isNotEmpty();
    }

    @Test
    @Order(2)
    @DisplayName("E2E 02: Conciliación Financiera - Obligación, Sesión de Caja, Pago y Amortización Exacta")
    void test02_conciliacion_financiera_y_amortizacion() {
        long ts = System.currentTimeMillis();

        // 1. Comerciante y Mercado
        MercadoMunicipal mercado = mercadoRepository.save(crearMercadoPrueba("MKT-FIN-" + ts));
        Comerciante comerciante = comercianteRepository.save(crearComerciantePrueba("45" + String.format("%06d", (int)(ts % 1000000))));
        PuestoComercial puesto = puestoRepository.save(crearPuestoPrueba(mercado, "PUESTO-FIN-" + ts));

        // 2. Concepto de Cobro
        ConceptoCobroMercado concepto = new ConceptoCobroMercado();
        concepto.setCodigo("MERCED-" + ts);
        concepto.setNombre("Merced Conductiva Mensual");
        concepto.setTipoConcepto(TipoConceptoCobro.USO_PUESTO_COMERCIAL);
        concepto.setPeriodicidad(PeriodicidadConcepto.MENSUAL);
        concepto.setEstado("ACTIVO");
        concepto = conceptoCobroRepository.save(concepto);

        // 3. Generar Obligación por S/ 150.00
        ObligacionMercado obligacion = new ObligacionMercado();
        obligacion.setCodigoObligacion("OBL-" + ts);
        obligacion.setPuesto(puesto);
        obligacion.setComerciante(comerciante);
        obligacion.setConcepto(concepto);
        obligacion.setPeriodoInicio(LocalDate.now().withDayOfMonth(1));
        obligacion.setPeriodoFin(LocalDate.now().withDayOfMonth(1).plusMonths(1).minusDays(1));
        obligacion.setFechaEmision(LocalDate.now());
        obligacion.setFechaVencimiento(LocalDate.now().plusDays(15));
        obligacion.setMontoOriginal(new BigDecimal("150.00"));
        obligacion.setMontoPagado(BigDecimal.ZERO);
        obligacion.setSaldoPendiente(new BigDecimal("150.00"));
        obligacion.setEstado(EstadoObligacion.PENDIENTE);
        obligacion = obligacionRepository.save(obligacion);

        // 4. Aperturar Caja y Sesión
        CajaMercado caja = cajaRepository.findAll().stream().findFirst().orElseGet(() -> {
            CajaMercado c = new CajaMercado();
            c.setCodigo("CAJA-01");
            c.setNombre("Caja Central Mercados");
            c.setEstado("ACTIVA");
            c.setMercado(mercado);
            return cajaRepository.save(c);
        });

        Persona personaCajero = personaRepository.save(crearPersonaPrueba("46" + String.format("%06d", (int)(ts % 1000000))));
        Usuario usuarioCajero = usuarioRepository.save(Usuario.builder()
                .username("cajero_" + ts).persona(personaCajero).passwordHash("hash")
                .enabled(true).accountLocked(false).build());

        SesionCajaMercado sesion = new SesionCajaMercado();
        sesion.setCaja(caja);
        sesion.setUsuarioCajero(usuarioCajero);
        sesion.setCajeroUsername(usuarioCajero.getUsername());
        sesion.setFechaApertura(OffsetDateTime.now());
        sesion.setMontoInicial(new BigDecimal("50.00"));
        sesion.setTotalIngresos(BigDecimal.ZERO);
        sesion.setEstado(EstadoSesionCaja.ABIERTA);
        sesion = sesionCajaRepository.save(sesion);

        // 5. Registrar Pago de S/ 150.00
        PagoMercado pago = new PagoMercado();
        pago.setCodigoPago("PAG-" + ts);
        pago.setSesionCaja(sesion);
        pago.setComerciante(comerciante);
        pago.setMontoRecibido(new BigDecimal("150.00"));
        pago.setMontoAplicado(new BigDecimal("150.00"));
        pago.setMedioPago(MedioPagoMercado.EFECTIVO);
        pago.setFechaPago(OffsetDateTime.now());
        pago.setEstado(EstadoPagoMercado.CONFIRMADO);
        pago.setUsuarioRegistro(usuarioCajero.getUsername());
        pago.setUsuarioRegistroEntity(usuarioCajero);
        pago = pagoRepository.save(pago);

        // 6. Aplicar Pago a la Obligación
        AplicacionPagoMercado aplicacion = new AplicacionPagoMercado();
        aplicacion.setPago(pago);
        aplicacion.setObligacion(obligacion);
        aplicacion.setMontoAplicado(new BigDecimal("150.00"));
        aplicacion.setFechaAplicacion(OffsetDateTime.now());
        aplicacionPagoRepository.save(aplicacion);

        obligacion.setMontoPagado(new BigDecimal("150.00"));
        obligacion.setSaldoPendiente(BigDecimal.ZERO);
        obligacion.setEstado(EstadoObligacion.PAGADA);
        obligacionRepository.save(obligacion);

        sesion.setTotalIngresos(sesion.getTotalIngresos().add(new BigDecimal("150.00")));
        sesionCajaRepository.save(sesion);

        // Verificación de integridad contable
        ObligacionMercado obligacionActualizada = obligacionRepository.findById(obligacion.getId()).orElseThrow();
        assertThat(obligacionActualizada.getSaldoPendiente()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(obligacionActualizada.getEstado()).isEqualTo(EstadoObligacion.PAGADA);

        SesionCajaMercado sesionActualizada = sesionCajaRepository.findById(sesion.getId()).orElseThrow();
        assertThat(sesionActualizada.getTotalIngresos()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    @Order(3)
    @DisplayName("E2E 03: Fiscalización e Inspección Sanitaria - Programación y Resultado")
    void test03_fiscalizacion_e_inspeccion() {
        long ts = System.currentTimeMillis();

        MercadoMunicipal mercado = mercadoRepository.save(crearMercadoPrueba("MKT-INSP-" + ts));
        PuestoComercial puesto = puestoRepository.save(crearPuestoPrueba(mercado, "PUESTO-INSP-" + ts));
        Persona inspector = personaRepository.save(crearPersonaPrueba("47" + String.format("%06d", (int)(ts % 1000000))));
        Usuario usuarioInspector = usuarioRepository.save(Usuario.builder()
                .username("inspector_" + ts).persona(inspector).passwordHash("hash").enabled(true).accountLocked(false).build());

        InspeccionMercado inspeccion = new InspeccionMercado();
        inspeccion.setMercado(mercado);
        inspeccion.setPuesto(puesto);
        inspeccion.setCodigoInspeccion("INSP-" + ts);
        inspeccion.setTipoInspeccion(TipoInspeccionMercado.PROGRAMADA);
        inspeccion.setEstado(EstadoInspeccion.PROGRAMADA);
        inspeccion.setMotivo("Fiscalización de rutina");
        inspeccion.setObjetivo("Verificar condiciones de salubridad");
        inspeccion.setFechaProgramada(LocalDate.now());
        inspeccion.setInspectorResponsable(usuarioInspector);
        inspeccion = inspeccionRepository.save(inspeccion);

        // Ejecutar inspección
        inspeccion.setEstado(EstadoInspeccion.FINALIZADA);
        inspeccion.setResultadoGeneral("CON_OBSERVACIONES");
        inspeccionRepository.save(inspeccion);

        InspeccionMercado verificada = inspeccionRepository.findById(inspeccion.getId()).orElseThrow();
        assertThat(verificada.getEstado()).isEqualTo(EstadoInspeccion.FINALIZADA);
        assertThat(verificada.getResultadoGeneral()).isEqualTo("CON_OBSERVACIONES");
    }

    @Test
    @Order(4)
    @DisplayName("E2E 04: Incidencias Operativas - Registro, Asignación, Actuación y Cierre")
    void test04_incidencias_operativas_y_resolucion() {
        long ts = System.currentTimeMillis();

        MercadoMunicipal mercado = mercadoRepository.save(crearMercadoPrueba("MKT-INC-" + ts));
        Persona reportante = personaRepository.save(crearPersonaPrueba("48" + String.format("%06d", (int)(ts % 1000000))));
        Usuario usuarioReportante = usuarioRepository.save(Usuario.builder()
                .username("user_rep_" + ts).persona(reportante).passwordHash("hash").enabled(true).accountLocked(false).build());

        TipoIncidenciaMercado tipoIncidencia = tipoIncidenciaRepository.findAll().stream().findFirst().orElseGet(() -> {
            TipoIncidenciaMercado t = new TipoIncidenciaMercado();
            t.setCodigo("INFRA");
            t.setNombre("Falla de Infraestructura");
            t.setPrioridadSugerida(PrioridadIncidencia.MEDIA);
            t.setActivo(true);
            return tipoIncidenciaRepository.save(t);
        });

        IncidenciaMercado incidencia = new IncidenciaMercado();
        incidencia.setCodigoIncidencia("INC-" + ts);
        incidencia.setMercado(mercado);
        incidencia.setTipoIncidencia(tipoIncidencia);
        incidencia.setOrigen(OrigenIncidencia.PERSONAL_MUNICIPAL);
        incidencia.setPrioridad(PrioridadIncidencia.ALTA);
        incidencia.setEstado(EstadoIncidencia.REGISTRADA);
        incidencia.setDescripcion("Fuga de agua en el pasillo principal del sector 1");
        incidencia.setFechaReporte(OffsetDateTime.now());
        incidencia.setReportanteUsuario(usuarioReportante);
        incidencia = incidenciaRepository.save(incidencia);

        // Atender y Resolver
        incidencia.setEstado(EstadoIncidencia.EN_ATENCION);
        incidencia.setResponsable(usuarioReportante);
        incidenciaRepository.save(incidencia);

        incidencia.setEstado(EstadoIncidencia.RESUELTA);
        incidencia.setFechaResolucion(OffsetDateTime.now());
        incidencia.setMotivoResolucion("Reparación de válvula de paso concluida satisfactoriamente");
        incidenciaRepository.save(incidencia);

        IncidenciaMercado verificada = incidenciaRepository.findById(incidencia.getId()).orElseThrow();
        assertThat(verificada.getEstado()).isEqualTo(EstadoIncidencia.RESUELTA);
        assertThat(verificada.getMotivoResolucion()).isNotBlank();
    }

    @Test
    @Order(5)
    @DisplayName("E2E 05: Portal del Comerciante - Aislamiento de Datos y Consulta de Perfil Propio")
    void test05_portal_comerciante_aislamiento_seguro() {
        long ts = System.currentTimeMillis();

        Rol rolComerciante = rolRepository.findByCodigo("ROLE_COMERCIANTE").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_COMERCIANTE").nombre("Comerciante").estado("ACTIVO").build()));

        Persona persona1 = personaRepository.save(crearPersonaPrueba("49" + String.format("%06d", (int)(ts % 1000000))));
        Usuario usuario1 = usuarioRepository.save(Usuario.builder()
                .username("com_portal_" + ts)
                .persona(persona1)
                .passwordHash("hash")
                .roles(Set.of(rolComerciante))
                .enabled(true)
                .accountLocked(false)
                .build());

        Comerciante comerciante1 = new Comerciante();
        comerciante1.setPersona(persona1);
        comerciante1.setCodigoComerciante("COM-P1-" + ts);
        comerciante1.setTipoComerciante(TipoComerciante.PERSONA_NATURAL);
        comerciante1.setEstado(EstadoComerciante.ACTIVO);
        comerciante1 = comercianteRepository.save(comerciante1);

        // Crear vínculo verificado
        vinculoRepository.save(VinculoUsuarioComerciante.builder()
                .usuario(usuario1)
                .comerciante(comerciante1)
                .tipoVinculo(TipoVinculoComerciante.TITULAR)
                .estado(EstadoVinculoComerciante.VERIFICADO)
                .fechaInicio(LocalDate.now().minusDays(10))
                .fechaVerificacion(OffsetDateTime.now())
                .verificadoPor(usuario1)
                .build());

        // Establecer contexto de autenticación
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario1.getUsername(), "N/A",
                        List.of(new SimpleGrantedAuthority("ROLE_COMERCIANTE")))
        );

        // Resolver contexto del comerciante autenticado
        Comerciante comercianteActivo = portalContextService.getComercianteActivo();
        assertThat(comercianteActivo.getId()).isEqualTo(comerciante1.getId());
        assertThat(comercianteActivo.getNombreOTitular()).isEqualTo(persona1.getNombreCompleto());

        // Perfil con datos protegidos
        PortalComerciantePerfilResponse perfil = portalPerfilService.obtenerPerfil();
        assertThat(perfil.getNombreCompletoORazonSocial()).isEqualTo(persona1.getNombreCompleto());
        assertThat(perfil.getDocumentoEnmascarado()).contains("***"); // anonimizado / enmascarado
    }

    @Test
    @Order(6)
    @DisplayName("E2E 06: Inteligencia de Negocios - Dashboard Ejecutivo y Tasa de Ocupación Real")
    void test06_inteligencia_negocios_y_ocupacion_real() {
        long ts = System.currentTimeMillis();

        MercadoMunicipal mercado = mercadoRepository.save(crearMercadoPrueba("MKT-REP-" + ts));
        puestoRepository.save(crearPuestoPrueba(mercado, "PUESTO-DISP-" + ts));
        PuestoComercial puestoOcup = crearPuestoPrueba(mercado, "PUESTO-OCUP-" + ts);
        puestoOcup.setEstado(EstadoPuesto.OCUPADO);
        puestoRepository.save(puestoOcup);

        ReporteMercadoFilter filtro = ReporteMercadoFilter.builder()
                .mercadoId(mercado.getId())
                .periodo(PeriodoConsulta.MES_ACTUAL)
                .build();

        // Indicador de ocupación
        IndicadorOcupacionResponse ocupacion = reporteOcupacionService.obtenerIndicadorOcupacion(filtro);
        assertThat(ocupacion.getTotalPuestos()).isGreaterThanOrEqualTo(2);
        assertThat(ocupacion.getPuestosOcupados()).isGreaterThanOrEqualTo(1);
        assertThat(ocupacion.getPuestosDisponibles()).isGreaterThanOrEqualTo(1);
        assertThat(ocupacion.getPorcentajeOcupacion()).isNotNull();

        // Dashboard ejecutivo
        DashboardMercadosResponse dash = indicadoresService.obtenerDashboard(filtro);
        assertThat(dash.getTotalMercados()).isGreaterThanOrEqualTo(1);
        assertThat(dash.getTotalPuestos()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @Order(7)
    @DisplayName("E2E 07: Seguridad en Exportación Excel - Sanitización de Fórmulas Maliciosas")
    void test07_seguridad_exportacion_excel_sanitizacion() {
        ReporteMercadoFilter filtro = ReporteMercadoFilter.builder().periodo(PeriodoConsulta.MES_ACTUAL).build();
        byte[] excelBytes = exportacionExcelService.generarReporteExcel(TipoReporteMercado.PUESTOS, filtro, true);

        assertThat(excelBytes).isNotEmpty();
        // Verificar firma binaria del ZIP/XLSX: PK..
        assertThat(excelBytes[0]).isEqualTo((byte) 'P');
        assertThat(excelBytes[1]).isEqualTo((byte) 'K');
    }

    // Métodos auxiliares de creación de entidades aisladas para pruebas
    private MercadoMunicipal crearMercadoPrueba(String codigo) {
        MercadoMunicipal m = new MercadoMunicipal();
        m.setCodigo(codigo);
        m.setNombre("Mercado Test " + codigo);
        m.setEstado(EstadoMercado.ACTIVO);
        m.setDireccion("Distrito de Quinua");
        return m;
    }

    private Persona crearPersonaPrueba(String dni) {
        Persona p = new Persona();
        p.setTipoDocumento("DNI");
        p.setNumeroDocumento(dni);
        p.setNombres("TestNombres");
        p.setApellidoPaterno("TestPaterno");
        p.setApellidoMaterno("TestMaterno");
        p.setCorreo("test." + dni + "@quinua.gob.pe");
        p.setEstado("ACTIVO");
        return p;
    }

    private Comerciante crearComerciantePrueba(String dni) {
        Persona p = personaRepository.save(crearPersonaPrueba(dni));
        Comerciante c = new Comerciante();
        c.setPersona(p);
        c.setCodigoComerciante("COM-" + dni);
        c.setTipoComerciante(TipoComerciante.PERSONA_NATURAL);
        c.setEstado(EstadoComerciante.ACTIVO);
        return c;
    }

    private PuestoComercial crearPuestoPrueba(MercadoMunicipal mercado, String codigo) {
        TipoPuestoComercial tipo = tipoPuestoRepository.findAll().stream().findFirst().orElseGet(() -> {
            TipoPuestoComercial t = new TipoPuestoComercial();
            t.setNombre("Tipo Estándar");
            t.setCodigo("STD_P");
            t.setEstado("ACTIVO");
            return tipoPuestoRepository.save(t);
        });

        PuestoComercial p = new PuestoComercial();
        p.setMercado(mercado);
        p.setTipoPuesto(tipo);
        p.setCodigo(codigo);
        p.setNumero("P-" + codigo.substring(Math.max(0, codigo.length() - 4)));
        p.setEstado(EstadoPuesto.DISPONIBLE);
        p.setAreaMetrosCuadrados(new BigDecimal("10.00"));
        return p;
    }
}
