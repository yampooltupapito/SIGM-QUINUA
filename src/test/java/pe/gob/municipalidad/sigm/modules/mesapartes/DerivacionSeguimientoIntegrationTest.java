package pe.gob.municipalidad.sigm.modules.mesapartes;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.model.*;
import pe.gob.municipalidad.sigm.common.repository.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.DerivacionDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import pe.gob.municipalidad.sigm.common.constants.SecurityConstants;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
@DisplayName("Pruebas de Integración - Derivación, Asignación y Seguimiento Interno (Prompt 15)")
class DerivacionSeguimientoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PermisoRepository permisoRepository;

    @Autowired
    private ModuloSistemaRepository moduloSistemaRepository;

    @Autowired
    private OficinaRepository oficinaRepository;

    @Autowired
    private MunicipalidadRepository municipalidadRepository;

    @Autowired
    private PersonaOficinaRepository personaOficinaRepository;

    @Autowired
    private TipoActorRepository tipoActorRepository;

    @Autowired
    private TipoTramiteRepository tipoTramiteRepository;

    @Autowired
    private ExpedienteRepository expedienteRepository;

    @Autowired
    private DerivacionExpedienteRepository derivacionRepository;

    @Autowired
    private AsignacionExpedienteRepository asignacionRepository;

    @Autowired
    private ActuacionExpedienteRepository actuacionRepository;

    private Usuario operador;
    private Usuario jefeDestino;
    private Usuario trabajadorDestino;
    private Usuario ciudadano;

    private Cookie operadorCookie;
    private Cookie jefeDestinoCookie;
    private Cookie trabajadorDestinoCookie;

    private Oficina oficinaMesaPartes;
    private Oficina oficinaDesarrolloUrbano;
    private TipoTramite tipoTramite;
    private TipoActor tipoActorServidor;

    @BeforeEach
    void setUp() {
        Municipalidad muni = municipalidadRepository.findAll().stream().findFirst().orElseGet(() ->
                municipalidadRepository.save(Municipalidad.builder()
                        .nombre("Municipalidad Distrital de Quinua")
                        .nombreCorto("MD Quinua")
                        .ubigeo("050108")
                        .departamento("Ayacucho")
                        .provincia("Huamanga")
                        .distrito("Quinua")
                        .direccion("Plaza de Armas s/n, Quinua, Huamanga, Ayacucho")
                        .telefono("066-812345")
                        .correoInstitucional("mesadepartes@muniquinua.gob.pe")
                        .estado("ACTIVO")
                        .build())
        );

        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 5);

        oficinaMesaPartes = oficinaRepository.save(Oficina.builder()
                .codigo("OFI-MPV-" + uniqueSuffix)
                .nombre("Mesa de Partes General")
                .sigla("MPV")
                .nivelJerarquico(1)
                .estado("ACTIVO")
                .municipalidad(muni)
                .build());

        oficinaDesarrolloUrbano = oficinaRepository.save(Oficina.builder()
                .codigo("OFI-DESURB-" + uniqueSuffix)
                .nombre("Subgerencia de Desarrollo Urbano y Obras")
                .sigla("SDUO")
                .nivelJerarquico(2)
                .estado("ACTIVO")
                .municipalidad(muni)
                .build());

        tipoActorServidor = tipoActorRepository.findAll().stream().findFirst().orElseGet(() ->
                tipoActorRepository.save(TipoActor.builder()
                        .codigo("SERVIDOR_PUBLICO")
                        .nombre("Servidor Público")
                        .descripcion("Trabajador municipal")
                        .build())
        );

        Rol rolOperador = rolRepository.findByCodigo("ROLE_OPERADOR").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_OPERADOR").nombre("Operador Mesa de Partes").descripcion("Operador").estado("ACTIVO").build())
        );

        Rol rolJefe = rolRepository.findByCodigo("ROLE_JEFE_OFICINA").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_JEFE_OFICINA").nombre("Jefe de Oficina").descripcion("Jefe").estado("ACTIVO").build())
        );

        Rol rolCiudadano = rolRepository.findByCodigo("ROLE_CIUDADANO").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_CIUDADANO").nombre("Ciudadano").descripcion("Ciudadano").estado("ACTIVO").build())
        );

        asegurarPermisos(rolOperador, List.of(
                "MESAPARTES_DERIVACION_CREAR", "MESAPARTES_DERIVACION_VER", "MESAPARTES_EXPEDIENTE_VER_TODOS"
        ));

        asegurarPermisos(rolJefe, List.of(
                "MESAPARTES_DERIVACION_VER", "MESAPARTES_DERIVACION_RECIBIR", "MESAPARTES_DERIVACION_RECHAZAR",
                "MESAPARTES_DERIVACION_DEVOLVER", "MESAPARTES_ASIGNACION_CREAR", "MESAPARTES_ASIGNACION_MODIFICAR",
                "MESAPARTES_ACTUACION_CREAR", "MESAPARTES_SEGUIMIENTO_VER", "MESAPARTES_ESTADISTICAS_OFICINA",
                "MESAPARTES_EXPEDIENTE_VER_OFICINA"
        ));

        String numDocSuffix = String.format("%06d", new Random().nextInt(999999));

        Persona perOp = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("71" + numDocSuffix)
                .nombres("Operador").apellidoPaterno("Remitente").correo("op." + uniqueSuffix + "@quinua.gob.pe").build());

        Persona perJefe = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("72" + numDocSuffix)
                .nombres("Jefe").apellidoPaterno("Destino").correo("jefe." + uniqueSuffix + "@quinua.gob.pe").build());

        Persona perTrab = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("73" + numDocSuffix)
                .nombres("Especialista").apellidoPaterno("Tecnico").correo("esp." + uniqueSuffix + "@quinua.gob.pe").build());

        Persona perCiu = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI").numeroDocumento("74" + numDocSuffix)
                .nombres("Ciudadano").apellidoPaterno("Solicitante").correo("ciu." + uniqueSuffix + "@gmail.com").build());

        operador = usuarioRepository.save(Usuario.builder()
                .username("op_" + uniqueSuffix)
                .passwordHash(passwordEncoder.encode("Password2026!"))
                .persona(perOp).roles(Set.of(rolOperador)).enabled(true).build());

        jefeDestino = usuarioRepository.save(Usuario.builder()
                .username("jefe_" + uniqueSuffix)
                .passwordHash(passwordEncoder.encode("Password2026!"))
                .persona(perJefe).roles(Set.of(rolJefe)).enabled(true).build());

        trabajadorDestino = usuarioRepository.save(Usuario.builder()
                .username("trab_" + uniqueSuffix)
                .passwordHash(passwordEncoder.encode("Password2026!"))
                .persona(perTrab).roles(Set.of(rolOperador)).enabled(true).build());

        ciudadano = usuarioRepository.save(Usuario.builder()
                .username("ciu_" + uniqueSuffix)
                .passwordHash(passwordEncoder.encode("Password2026!"))
                .persona(perCiu).roles(Set.of(rolCiudadano)).enabled(true).build());

        // Asignaciones de persona a oficina
        personaOficinaRepository.save(PersonaOficina.builder()
                .persona(perOp).oficina(oficinaMesaPartes).tipoActor(tipoActorServidor)
                .fechaInicio(LocalDate.now()).principal(true).estado("ACTIVO").build());

        personaOficinaRepository.save(PersonaOficina.builder()
                .persona(perJefe).oficina(oficinaDesarrolloUrbano).tipoActor(tipoActorServidor)
                .fechaInicio(LocalDate.now()).principal(true).estado("ACTIVO").build());

        personaOficinaRepository.save(PersonaOficina.builder()
                .persona(perTrab).oficina(oficinaDesarrolloUrbano).tipoActor(tipoActorServidor)
                .fechaInicio(LocalDate.now()).principal(true).estado("ACTIVO").build());

        // Cookies JWT
        operadorCookie = buildJwtCookie(operador, "ROLE_OPERADOR", List.of(
                "MESAPARTES_DERIVACION_CREAR", "MESAPARTES_DERIVACION_VER", "MESAPARTES_DERIVACION_RECIBIR",
                "MESAPARTES_DERIVACION_RECHAZAR", "MESAPARTES_DERIVACION_DEVOLVER", "MESAPARTES_ASIGNACION_CREAR",
                "MESAPARTES_ASIGNACION_MODIFICAR", "MESAPARTES_ACTUACION_CREAR", "MESAPARTES_SEGUIMIENTO_VER",
                "MESAPARTES_ESTADISTICAS_OFICINA"
        ));

        jefeDestinoCookie = buildJwtCookie(jefeDestino, "ROLE_JEFE_OFICINA", List.of(
                "MESAPARTES_DERIVACION_CREAR", "MESAPARTES_DERIVACION_VER", "MESAPARTES_DERIVACION_RECIBIR",
                "MESAPARTES_DERIVACION_RECHAZAR", "MESAPARTES_DERIVACION_DEVOLVER", "MESAPARTES_ASIGNACION_CREAR",
                "MESAPARTES_ASIGNACION_MODIFICAR", "MESAPARTES_ACTUACION_CREAR", "MESAPARTES_SEGUIMIENTO_VER",
                "MESAPARTES_ESTADISTICAS_OFICINA"
        ));

        trabajadorDestinoCookie = buildJwtCookie(trabajadorDestino, "ROLE_OPERADOR", List.of(
                "MESAPARTES_ACTUACION_CREAR", "MESAPARTES_SEGUIMIENTO_VER"
        ));

        tipoTramite = tipoTramiteRepository.save(TipoTramite.builder()
                .codigo("TRAM-URB-" + uniqueSuffix)
                .nombre("Licencia de Edificación")
                .descripcion("Trámite para obras de construcción")
                .oficinaResponsable(oficinaDesarrolloUrbano)
                .plazoDias(30)
                .unidadPlazo(UnidadPlazo.DIAS_HABILES)
                .estado("ACTIVO")
                .build());
    }

    private Expediente crearExpedientePrueba(EstadoExpediente estado, Oficina oficina) {
        String num = "MPV-2026-" + String.format("%05d", new Random().nextInt(99999));
        return expedienteRepository.save(Expediente.builder()
                .uuid(UUID.randomUUID())
                .numeroExpediente(num)
                .anio(2026)
                .asunto("Expediente de Construcción " + num)
                .descripcion("Construcción de vivienda multifamiliar de 3 pisos")
                .tipoTramite(tipoTramite)
                .solicitante(ciudadano.getPersona())
                .oficinaOrigen(oficinaMesaPartes)
                .oficinaResponsable(oficina)
                .estadoActual(estado)
                .canalIngreso(CanalIngreso.VIRTUAL)
                .prioridad(PrioridadExpediente.NORMAL)
                .confidencialidad(ConfidencialidadExpediente.INTERNO)
                .usuarioRegistro(operador)
                .fechaPresentacion(OffsetDateTime.now())
                .fechaRecepcion(estado != EstadoExpediente.PRESENTADO ? OffsetDateTime.now() : null)
                .build());
    }

    private void asegurarPermisos(Rol rol, List<String> codigosPermisos) {
        ModuloSistema mod = moduloSistemaRepository.findByCodigo("TRAMITE_DOCUMENTARIO")
                .orElseGet(() -> moduloSistemaRepository.save(ModuloSistema.builder()
                        .codigo("TRAMITE_DOCUMENTARIO").nombre("Trámite Documentario").estado("ACTIVO").build()));

        for (String cod : codigosPermisos) {
            Permiso p = permisoRepository.findByCodigo(cod).orElseGet(() ->
                    permisoRepository.save(Permiso.builder()
                            .modulo(mod).codigo(cod).nombre(cod).accion("VIEW").descripcion(cod).build()));
            if (!rol.getPermisos().contains(p)) {
                rol.getPermisos().add(p);
            }
        }
        rolRepository.save(rol);
    }

    @Test
    @DisplayName("1. Operador deriva expediente a otra oficina con éxito")
    void testOperadorPuedeDerivarExpedienteAOficina() throws Exception {
        Expediente exp = crearExpedientePrueba(EstadoExpediente.RECIBIDO, oficinaMesaPartes);

        DerivarExpedienteRequest req = DerivarExpedienteRequest.builder()
                .oficinaDestinoId(oficinaDesarrolloUrbano.getId())
                .motivo("Para evaluación técnica e inspección de campo")
                .indicaciones("Verificar alineamiento y retiros municipales")
                .build();

        mockMvc.perform(post("/api/mesa-partes/expedientes/{id}/derivar", exp.getId())
                        .cookie(operadorCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.data.oficinaDestinoId").value(oficinaDesarrolloUrbano.getId()));

        Expediente actualizado = expedienteRepository.findById(exp.getId()).orElseThrow();
        assertThat(actualizado.getEstadoActual()).isEqualTo(EstadoExpediente.DERIVADO);
    }

    @Test
    @DisplayName("2. Rechazo de derivación si la oficina de destino es la misma que la remitente")
    void testRechazaDerivacionAMismaOficina() throws Exception {
        Expediente exp = crearExpedientePrueba(EstadoExpediente.RECIBIDO, oficinaMesaPartes);

        DerivarExpedienteRequest req = DerivarExpedienteRequest.builder()
                .oficinaDestinoId(oficinaMesaPartes.getId())
                .motivo("Misma oficina")
                .build();

        mockMvc.perform(post("/api/mesa-partes/expedientes/{id}/derivar", exp.getId())
                        .cookie(operadorCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("3. Rechazo de derivación duplicada cuando ya existe una pendiente")
    void testRechazaDerivacionDuplicadaPendiente() throws Exception {
        Expediente exp = crearExpedientePrueba(EstadoExpediente.RECIBIDO, oficinaMesaPartes);

        derivacionRepository.save(DerivacionExpediente.builder()
                .expediente(exp)
                .oficinaOrigen(oficinaMesaPartes)
                .oficinaDestino(oficinaDesarrolloUrbano)
                .usuarioDerivador(operador)
                .motivo("Derivación previa")
                .estado(EstadoDerivacion.PENDIENTE)
                .fechaDerivacion(OffsetDateTime.now())
                .build());

        DerivarExpedienteRequest req = DerivarExpedienteRequest.builder()
                .oficinaDestinoId(oficinaDesarrolloUrbano.getId())
                .motivo("Intento duplicado")
                .build();

        mockMvc.perform(post("/api/mesa-partes/expedientes/{id}/derivar", exp.getId())
                        .cookie(operadorCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("4. Oficina destinataria acepta formalmente la derivación")
    void testOficinaReceptoraAceptaRecepcion() throws Exception {
        Expediente exp = crearExpedientePrueba(EstadoExpediente.DERIVADO, oficinaMesaPartes);

        DerivacionExpediente derivacion = derivacionRepository.save(DerivacionExpediente.builder()
                .expediente(exp)
                .oficinaOrigen(oficinaMesaPartes)
                .oficinaDestino(oficinaDesarrolloUrbano)
                .usuarioDerivador(operador)
                .motivo("Remisión para informe")
                .estado(EstadoDerivacion.PENDIENTE)
                .fechaDerivacion(OffsetDateTime.now())
                .build());

        RecepcionDerivacionRequest req = RecepcionDerivacionRequest.builder()
                .observaciones("Expediente recibido en despacho con todos sus recaudos")
                .build();

        mockMvc.perform(post("/api/mesa-partes/derivaciones/{id}/recibir", derivacion.getId())
                        .cookie(jefeDestinoCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.estado").value("RECIBIDA"));

        Expediente actualizado = expedienteRepository.findById(exp.getId()).orElseThrow();
        assertThat(actualizado.getEstadoActual()).isEqualTo(EstadoExpediente.EN_ATENCION);
        assertThat(actualizado.getOficinaResponsable().getId()).isEqualTo(oficinaDesarrolloUrbano.getId());
    }

    @Test
    @DisplayName("5. Oficina destinataria devuelve derivación con motivo obligatorio")
    void testOficinaReceptoraDevuelveDerivacion() throws Exception {
        Expediente exp = crearExpedientePrueba(EstadoExpediente.DERIVADO, oficinaMesaPartes);

        DerivacionExpediente derivacion = derivacionRepository.save(DerivacionExpediente.builder()
                .expediente(exp)
                .oficinaOrigen(oficinaMesaPartes)
                .oficinaDestino(oficinaDesarrolloUrbano)
                .usuarioDerivador(operador)
                .motivo("Remisión previa")
                .estado(EstadoDerivacion.PENDIENTE)
                .fechaDerivacion(OffsetDateTime.now())
                .build());

        RechazoDevolucionRequest req = RechazoDevolucionRequest.builder()
                .motivo("Falta recibo de pago de derechos de trámite")
                .build();

        mockMvc.perform(post("/api/mesa-partes/derivaciones/{id}/devolver", derivacion.getId())
                        .cookie(jefeDestinoCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.estado").value("DEVUELTA"));

        Expediente actualizado = expedienteRepository.findById(exp.getId()).orElseThrow();
        assertThat(actualizado.getEstadoActual()).isEqualTo(EstadoExpediente.RECIBIDO);
        assertThat(actualizado.getOficinaResponsable().getId()).isEqualTo(oficinaMesaPartes.getId());
    }

    @Test
    @DisplayName("6. Jefe de oficina asigna trabajador responsable del expediente")
    void testJefeDeOficinaAsignaTrabajadorResponsable() throws Exception {
        Expediente exp = crearExpedientePrueba(EstadoExpediente.EN_ATENCION, oficinaDesarrolloUrbano);

        AsignarExpedienteRequest req = AsignarExpedienteRequest.builder()
                .usuarioAsignadoId(trabajadorDestino.getId())
                .motivo("Asignación de carga técnica")
                .indicaciones("Elaborar informe técnico dentro de los próximos 5 días")
                .build();

        mockMvc.perform(post("/api/mesa-partes/expedientes/{id}/asignar", exp.getId())
                        .cookie(jefeDestinoCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.estado").value("ACTIVO"))
                .andExpect(jsonPath("$.data.usuarioAsignadoId").value(trabajadorDestino.getId()));

        Expediente actualizado = expedienteRepository.findById(exp.getId()).orElseThrow();
        assertThat(actualizado.getResponsableActual().getId()).isEqualTo(trabajadorDestino.getId());
    }

    @Test
    @DisplayName("7. Reasignación de trabajador responsable registra trazabilidad")
    void testReasignacionDeTrabajadorResponsable() throws Exception {
        Expediente exp = crearExpedientePrueba(EstadoExpediente.EN_ATENCION, oficinaDesarrolloUrbano);
        exp.setResponsableActual(jefeDestino);
        expedienteRepository.save(exp);

        ReasignarExpedienteRequest req = ReasignarExpedienteRequest.builder()
                .nuevoUsuarioId(trabajadorDestino.getId())
                .motivo("Reasignación por redistribución de carga técnica")
                .indicaciones("Continuar con la evaluación iniciada")
                .build();

        mockMvc.perform(post("/api/mesa-partes/expedientes/{id}/reasignar", exp.getId())
                        .cookie(jefeDestinoCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.usuarioAnteriorId").value(jefeDestino.getId()))
                .andExpect(jsonPath("$.data.usuarioAsignadoId").value(trabajadorDestino.getId()));

        Expediente actualizado = expedienteRepository.findById(exp.getId()).orElseThrow();
        assertThat(actualizado.getResponsableActual().getId()).isEqualTo(trabajadorDestino.getId());
    }

    @Test
    @DisplayName("8. Registrar actuación interna en el expediente")
    void testRegistrarActuacionInterna() throws Exception {
        Expediente exp = crearExpedientePrueba(EstadoExpediente.EN_ATENCION, oficinaDesarrolloUrbano);

        RegistrarActuacionRequest req = RegistrarActuacionRequest.builder()
                .tipo(TipoActuacionExpediente.INFORME)
                .descripcion("Informe Técnico N° 012-2026-SDUO: Evaluación favorable de planos de arquitectura.")
                .visibilidad(VisibilidadObservacion.INTERNA)
                .build();

        mockMvc.perform(post("/api/mesa-partes/expedientes/{id}/actuaciones", exp.getId())
                        .cookie(jefeDestinoCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tipo").value("INFORME"))
                .andExpect(jsonPath("$.data.visibilidad").value("INTERNA"));

        List<ActuacionExpediente> list = actuacionRepository.findByExpedienteIdOrderByFechaActuacionDesc(exp.getId());
        assertThat(list).isNotEmpty();
        assertThat(list.get(0).getTipo()).isEqualTo(TipoActuacionExpediente.INFORME);
    }

    @Test
    @DisplayName("9. Consultar seguimiento integral 360° y mapa de recorrido")
    void testConsultarSeguimientoIntegralYRecorrido() throws Exception {
        Expediente exp = crearExpedientePrueba(EstadoExpediente.EN_ATENCION, oficinaDesarrolloUrbano);

        mockMvc.perform(get("/api/mesa-partes/expedientes/{id}/seguimiento", exp.getId())
                        .cookie(jefeDestinoCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.expediente.id").value(exp.getId()))
                .andExpect(jsonPath("$.data.plazo").exists())
                .andExpect(jsonPath("$.data.recorrido").isArray());
    }

    @Test
    @DisplayName("10. Trabajador consulta mis expedientes asignados")
    void testConsultarMisExpedientesAsignados() throws Exception {
        Expediente exp = crearExpedientePrueba(EstadoExpediente.EN_ATENCION, oficinaDesarrolloUrbano);
        exp.setResponsableActual(trabajadorDestino);
        expedienteRepository.save(exp);

        mockMvc.perform(get("/api/mesa-partes/mis-expedientes")
                        .cookie(trabajadorDestinoCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("11. Consultar estadísticas de derivación y panel de oficina")
    void testConsultarEstadisticasYPanelOficina() throws Exception {
        mockMvc.perform(get("/api/mesa-partes/derivaciones/estadisticas")
                        .param("oficinaId", oficinaDesarrolloUrbano.getId().toString())
                        .cookie(jefeDestinoCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalExpedientesOficina").isNumber());

        mockMvc.perform(get("/api/mesa-partes/panel-oficina")
                        .param("oficinaId", oficinaDesarrolloUrbano.getId().toString())
                        .cookie(jefeDestinoCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.oficina.id").value(oficinaDesarrolloUrbano.getId()))
                .andExpect(jsonPath("$.data.cargaTrabajadores").isArray());
    }

    @Test
    @DisplayName("12. Consultar catálogo de oficinas activas y trabajadores por oficina")
    void testConsultarOficinasActivasYTrabajadores() throws Exception {
        mockMvc.perform(get("/api/mesa-partes/oficinas-activas")
                        .cookie(operadorCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        mockMvc.perform(get("/api/mesa-partes/oficinas/{id}/trabajadores", oficinaDesarrolloUrbano.getId())
                        .cookie(jefeDestinoCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("13. Rechazo de acceso a usuario no autenticado")
    void testRechazaUsuarioNoAutenticado() throws Exception {
        mockMvc.perform(get("/api/mesa-partes/derivaciones"))
                .andExpect(status().isUnauthorized());
    }

    private Cookie buildJwtCookie(Usuario usuario, String role, List<String> permissions) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(role));
        for (String p : permissions) {
            authorities.add(new SimpleGrantedAuthority(p));
        }

        CustomUserDetails userDetails = CustomUserDetails.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .personaId(usuario.getPersona() != null ? usuario.getPersona().getId() : null)
                .nombreCompleto(usuario.getPersona() != null ? usuario.getPersona().getNombreCompleto() : usuario.getUsername())
                .roles(Set.of(role))
                .permisos(new HashSet<>(permissions))
                .authorities(authorities)
                .enabled(true)
                .accountNonLocked(true)
                .build();

        String token = jwtTokenProvider.generateAccessToken(userDetails);
        Cookie cookie = new Cookie(SecurityConstants.TOKEN_COOKIE_NAME, token);
        cookie.setPath("/");
        return cookie;
    }
}
