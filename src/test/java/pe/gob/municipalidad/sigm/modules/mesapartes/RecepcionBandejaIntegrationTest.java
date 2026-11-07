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
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvAdminDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.*;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

import java.time.OffsetDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
@DisplayName("Pruebas de Integración - Recepción de Expedientes, Bandeja Institucional y Observaciones (Prompt 14)")
class RecepcionBandejaIntegrationTest {

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
    private TipoTramiteRepository tipoTramiteRepository;

    @Autowired
    private RequisitoDocumentalRepository requisitoRepository;

    @Autowired
    private ExpedienteRepository expedienteRepository;

    @Autowired
    private ObservacionExpedienteRepository observacionRepository;

    @Autowired
    private SubsanacionExpedienteRepository subsanacionRepository;

    @Autowired
    private RevisionRequisitoRepository revisionRequisitoRepository;

    @Autowired
    private CambioPrioridadExpedienteRepository cambioPrioridadRepository;

    private Usuario operador;
    private Usuario ciudadano;
    private Cookie operadorCookie;
    private Cookie ciudadanoCookie;

    private Oficina oficinaRentas;
    private TipoTramite tipoTramite;
    private RequisitoDocumental reqDni;

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

        oficinaRentas = oficinaRepository.findAll().stream().findFirst().orElseGet(() ->
                oficinaRepository.save(Oficina.builder()
                        .codigo("OFI-RENTAS-" + UUID.randomUUID().toString().substring(0, 5))
                        .nombre("Subgerencia de Rentas y Fiscalización")
                        .sigla("SRF")
                        .nivelJerarquico(2)
                        .estado("ACTIVO")
                        .municipalidad(muni)
                        .build())
        );

        // Crear o buscar rol OPERADOR con permisos de recepción y observación
        Rol rolOperador = rolRepository.findByCodigo("ROLE_OPERADOR").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_OPERADOR").nombre("Operador Mesa de Partes").descripcion("Operador Mesa de Partes").estado("ACTIVO").build())
        );

        Rol rolCiudadano = rolRepository.findByCodigo("ROLE_CIUDADANO").orElseGet(() ->
                rolRepository.save(Rol.builder().codigo("ROLE_CIUDADANO").nombre("Ciudadano").descripcion("Ciudadano").estado("ACTIVO").build())
        );

        // Asignar permisos específicos si no están presentes
        asegurarPermisos(rolOperador, List.of(
                "MESAPARTES_BANDEJA",
                "MESAPARTES_EXPEDIENTE_VER_TODOS",
                "MESAPARTES_EXPEDIENTE_RECIBIR",
                "MESAPARTES_EXPEDIENTE_OBSERVAR",
                "MESAPARTES_EXPEDIENTE_DERIVAR",
                "MESAPARTES_EXPEDIENTE_PRIORIDAD"
        ));

        asegurarPermisos(rolCiudadano, List.of(
                "MESAPARTES_EXPEDIENTE_VER_PROPIO",
                "MESAPARTES_EXPEDIENTE_SUBSANAR"
        ));

        // Personas
        String suffix = UUID.randomUUID().toString().substring(0, 6);
        Persona perOp = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("70" + suffix.substring(0, 6))
                .nombres("Operador")
                .apellidoPaterno("Mesa")
                .apellidoMaterno("Partes")
                .correo("operador." + suffix + "@quinua.gob.pe")
                .build());

        Persona perCiu = personaRepository.save(Persona.builder()
                .tipoDocumento("DNI")
                .numeroDocumento("45" + suffix.substring(0, 6))
                .nombres("Juan")
                .apellidoPaterno("Quispe")
                .apellidoMaterno("Huamán")
                .correo("juan." + suffix + "@gmail.com")
                .build());

        // Usuarios
        operador = usuarioRepository.save(Usuario.builder()
                .username("op_mpv_" + suffix)
                .passwordHash(passwordEncoder.encode("Operador2026!"))
                .persona(perOp)
                .roles(Set.of(rolOperador))
                .enabled(true)
                .build());

        ciudadano = usuarioRepository.save(Usuario.builder()
                .username("ciu_mpv_" + suffix)
                .passwordHash(passwordEncoder.encode("Ciudadano2026!"))
                .persona(perCiu)
                .roles(Set.of(rolCiudadano))
                .enabled(true)
                .build());

        // Tokens JWT
        pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails opDetails =
                pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails.builder()
                        .id(operador.getId())
                        .username(operador.getUsername())
                        .personaId(operador.getPersona().getId())
                        .nombreCompleto("Operador Mesa Partes")
                        .roles(Set.of("ROLE_OPERADOR"))
                        .permisos(Set.of("MESAPARTES_BANDEJA", "MESAPARTES_EXPEDIENTE_VER_TODOS", "MESAPARTES_EXPEDIENTE_RECIBIR", "MESAPARTES_EXPEDIENTE_OBSERVAR", "MESAPARTES_EXPEDIENTE_PRIORIDAD"))
                        .authorities(List.of(
                                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_OPERADOR"),
                                new org.springframework.security.core.authority.SimpleGrantedAuthority("MESAPARTES_BANDEJA"),
                                new org.springframework.security.core.authority.SimpleGrantedAuthority("MESAPARTES_EXPEDIENTE_VER_TODOS"),
                                new org.springframework.security.core.authority.SimpleGrantedAuthority("MESAPARTES_EXPEDIENTE_RECIBIR"),
                                new org.springframework.security.core.authority.SimpleGrantedAuthority("MESAPARTES_EXPEDIENTE_OBSERVAR"),
                                new org.springframework.security.core.authority.SimpleGrantedAuthority("MESAPARTES_EXPEDIENTE_PRIORIDAD")
                        ))
                        .enabled(true)
                        .accountNonLocked(true)
                        .build();
        String opToken = jwtTokenProvider.generateAccessToken(opDetails);
        operadorCookie = new Cookie(pe.gob.municipalidad.sigm.common.constants.SecurityConstants.TOKEN_COOKIE_NAME, opToken);

        pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails ciuDetails =
                pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails.builder()
                        .id(ciudadano.getId())
                        .username(ciudadano.getUsername())
                        .personaId(ciudadano.getPersona().getId())
                        .nombreCompleto("Juan Quispe Huamán")
                        .roles(Set.of("ROLE_CIUDADANO"))
                        .permisos(Set.of("MESAPARTES_EXPEDIENTE_VER_PROPIO", "MESAPARTES_EXPEDIENTE_SUBSANAR"))
                        .authorities(List.of(
                                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_CIUDADANO"),
                                new org.springframework.security.core.authority.SimpleGrantedAuthority("MESAPARTES_EXPEDIENTE_VER_PROPIO"),
                                new org.springframework.security.core.authority.SimpleGrantedAuthority("MESAPARTES_EXPEDIENTE_SUBSANAR")
                        ))
                        .enabled(true)
                        .accountNonLocked(true)
                        .build();
        String ciuToken = jwtTokenProvider.generateAccessToken(ciuDetails);
        ciudadanoCookie = new Cookie(pe.gob.municipalidad.sigm.common.constants.SecurityConstants.TOKEN_COOKIE_NAME, ciuToken);

        // Trámite y Requisito
        tipoTramite = tipoTramiteRepository.save(TipoTramite.builder()
                .codigo("TT-LIC-" + suffix)
                .nombre("Licencia de Funcionamiento Comercial")
                .descripcion("Trámite para apertura de establecimientos")
                .oficinaResponsable(oficinaRentas)
                .permitePresentacionVirtual(true)
                .requiereDocumentos(true)
                .plazoDias(15)
                .unidadPlazo(UnidadPlazo.DIAS_HABILES)
                .estado("ACTIVO")
                .build());

        reqDni = requisitoRepository.save(RequisitoDocumental.builder()
                .tipoTramite(tipoTramite)
                .codigo("REQ-DNI-" + suffix)
                .nombre("Copia simple de DNI")
                .descripcion("Documento de identidad legible")
                .obligatorio(true)
                .formatosPermitidos("PDF,JPG,PNG")
                .tamanoMaximoMb(5)
                .orden(1)
                .estado("ACTIVO")
                .build());
    }

    private void asegurarPermisos(Rol rol, List<String> codigos) {
        ModuloSistema mod = moduloSistemaRepository.findByCodigo("MESAPARTES").orElseGet(() ->
                moduloSistemaRepository.save(ModuloSistema.builder()
                        .codigo("MESAPARTES")
                        .nombre("Mesa de Partes")
                        .orden(1)
                        .build())
        );
        for (String cod : codigos) {
            Permiso p = permisoRepository.findByCodigo(cod).orElseGet(() ->
                    permisoRepository.save(Permiso.builder()
                            .codigo(cod)
                            .nombre(cod)
                            .accion("VIEW")
                            .modulo(mod)
                            .estado("ACTIVO")
                            .build())
            );
            if (rol.getPermisos() == null) {
                rol.setPermisos(new HashSet<>());
            }
            rol.getPermisos().add(p);
        }
        rolRepository.save(rol);
    }

    @Test
    @DisplayName("1. Operador autorizado puede acceder a la bandeja institucional /api/mesa-partes/bandeja")
    void test1_accesoBandejaInstitucional_operador_200OK() throws Exception {
        mockMvc.perform(get("/api/mesa-partes/bandeja")
                        .cookie(operadorCookie)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("2. Ciudadano es rechazado con 403 Forbidden al intentar consultar la bandeja institucional")
    void test2_accesoBandejaInstitucional_ciudadano_403Forbidden() throws Exception {
        mockMvc.perform(get("/api/mesa-partes/bandeja")
                        .cookie(ciudadanoCookie)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("3. Operador puede consultar estadísticas reales de la bandeja institucional")
    void test3_estadisticasBandeja_operador_200OK() throws Exception {
        mockMvc.perform(get("/api/mesa-partes/bandeja/estadisticas")
                        .cookie(operadorCookie)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalExpedientes").isNumber())
                .andExpect(jsonPath("$.data.presentados").isNumber())
                .andExpect(jsonPath("$.data.recibidos").isNumber());
    }

    @Test
    @DisplayName("4. Flujo de Recepción: Recibir formalmente expediente en estado PRESENTADO")
    void test4_recepcionExpediente_flujoCompleto() throws Exception {
        // Crear expediente presentado
        Expediente exp = expedienteRepository.save(Expediente.builder()
                .numeroExpediente("MPV-2026-990001")
                .anio(2026)
                .asunto("Solicitud de Inspección Técnica")
                .tipoTramite(tipoTramite)
                .solicitante(ciudadano.getPersona())
                .oficinaResponsable(oficinaRentas)
                .estadoActual(EstadoExpediente.PRESENTADO)
                .fechaPresentacion(OffsetDateTime.now().minusHours(1))
                .canalIngreso(CanalIngreso.VIRTUAL)
                .prioridad(PrioridadExpediente.NORMAL)
                .build());

        // Operador recibe el expediente
        mockMvc.perform(post("/api/mesa-partes/expedientes/{id}/recibir", exp.getId())
                        .cookie(operadorCookie)
                        .param("observaciones", "Revisión conforme de recaudos iniciales")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.estadoActual").value("RECIBIDO"));

        // Verificar en BD que se actualizó la fecha de recepción
        Expediente enBd = expedienteRepository.findById(exp.getId()).orElseThrow();
        assertThat(enBd.getEstadoActual()).isEqualTo(EstadoExpediente.RECIBIDO);
        assertThat(enBd.getFechaRecepcion()).isNotNull();

        // Intento de segunda recepción duplicada debe fallar con 422 (violación de regla de negocio)
        mockMvc.perform(post("/api/mesa-partes/expedientes/{id}/recibir", exp.getId())
                        .cookie(operadorCookie)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("5. Verificación de requisitos: listar y calificar requisito como CONFORME")
    void test5_verificacionRequisitos_operador() throws Exception {
        Expediente exp = expedienteRepository.save(Expediente.builder()
                .numeroExpediente("MPV-2026-990002")
                .anio(2026)
                .asunto("Solicitud con Requisitos")
                .tipoTramite(tipoTramite)
                .solicitante(ciudadano.getPersona())
                .oficinaResponsable(oficinaRentas)
                .estadoActual(EstadoExpediente.RECIBIDO)
                .fechaPresentacion(OffsetDateTime.now().minusHours(2))
                .fechaRecepcion(OffsetDateTime.now().minusHours(1))
                .build());

        // Listar o inicializar requisitos
        mockMvc.perform(get("/api/mesa-partes/expedientes/{id}/requisitos", exp.getId())
                        .cookie(operadorCookie)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].codigoRequisito").value(reqDni.getCodigo()))
                .andExpect(jsonPath("$.data[0].estadoRevision").value("PENDIENTE_REVISION"));

        // Calificar como CONFORME
        VerificarRequisitoRequest verReq = VerificarRequisitoRequest.builder()
                .estadoRevision(EstadoRevisionRequisito.CONFORME)
                .observaciones("DNI vigente y legible")
                .build();

        mockMvc.perform(post("/api/mesa-partes/expedientes/{id}/requisitos/{reqId}/verificar", exp.getId(), reqDni.getId())
                        .cookie(operadorCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verReq))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estadoRevision").value("CONFORME"))
                .andExpect(jsonPath("$.data.observaciones").value("DNI vigente y legible"));
    }

    @Test
    @DisplayName("6. Observaciones: Registrar observación interna vs ciudadana con requerimiento de subsanación y respuesta")
    void test6_observacionInternaVsCiudadana_y_subsanacion() throws Exception {
        Expediente exp = expedienteRepository.save(Expediente.builder()
                .numeroExpediente("MPV-2026-990003")
                .anio(2026)
                .asunto("Expediente a Observar")
                .tipoTramite(tipoTramite)
                .solicitante(ciudadano.getPersona())
                .oficinaResponsable(oficinaRentas)
                .estadoActual(EstadoExpediente.RECIBIDO)
                .fechaPresentacion(OffsetDateTime.now().minusHours(2))
                .fechaRecepcion(OffsetDateTime.now().minusHours(1))
                .build());

        // 1. Operador crea observación INTERNA
        RegistrarObservacionAdminRequest obsInterna = RegistrarObservacionAdminRequest.builder()
                .tipoObservacion(TipoObservacionExpediente.TECNICA)
                .visibilidad(VisibilidadObservacion.INTERNA)
                .descripcion("Nota técnica interna para el fiscalizador: verificar croquis catastral.")
                .build();

        mockMvc.perform(post("/api/mesa-partes/expedientes/{id}/observaciones", exp.getId())
                        .cookie(operadorCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(obsInterna)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.visibilidad").value("INTERNA"));

        // Ciudadano consulta sus observaciones: NO debe ver la observación interna
        mockMvc.perform(get("/api/mesa-partes/expedientes/{id}/observaciones", exp.getId())
                        .cookie(ciudadanoCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());

        // 2. Operador formula observación CIUDADANA con requerimiento de subsanación
        RegistrarObservacionAdminRequest obsCiudadana = RegistrarObservacionAdminRequest.builder()
                .tipoObservacion(TipoObservacionExpediente.DOCUMENTAL)
                .visibilidad(VisibilidadObservacion.CIUDADANO)
                .solicitarSubsanacion(true)
                .diasPlazoSubsanacion(10)
                .requisitoId(reqDni.getId())
                .descripcion("El documento de identidad adjunto se encuentra borroso y no legible.")
                .build();

        mockMvc.perform(post("/api/mesa-partes/expedientes/{id}/observaciones", exp.getId())
                        .cookie(operadorCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(obsCiudadana)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.visibilidad").value("CIUDADANO"));

        // Verificar que el expediente cambió a OBSERVADO
        Expediente expObservado = expedienteRepository.findById(exp.getId()).orElseThrow();
        assertThat(expObservado.getEstadoActual()).isEqualTo(EstadoExpediente.OBSERVADO);

        // Ciudadano consulta: ahora SÍ ve la observación ciudadana
        mockMvc.perform(get("/api/mesa-partes/expedientes/{id}/observaciones", exp.getId())
                        .cookie(ciudadanoCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].descripcion").value("El documento de identidad adjunto se encuentra borroso y no legible."));

        // 3. Ciudadano responde y presenta subsanación
        SubsanarExpedienteRequest subReq = SubsanarExpedienteRequest.builder()
                .respuestaCiudadano("Se remite copia a color completamente nítida de mi DNI.")
                .build();

        mockMvc.perform(post("/api/mesa-partes/expedientes/{id}/subsanar", exp.getId())
                        .cookie(ciudadanoCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(subReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estadoActual").value("EN_SUBSANACION"));

        // Verificar que en BD pasó a EN_SUBSANACION
        Expediente expSubsanado = expedienteRepository.findById(exp.getId()).orElseThrow();
        assertThat(expSubsanado.getEstadoActual()).isEqualTo(EstadoExpediente.EN_SUBSANACION);
    }

    @Test
    @DisplayName("7. Modificación de prioridad y descarga de constancia de recepción PDF")
    void test7_cambioPrioridad_y_constanciaRecepcionPdf() throws Exception {
        Expediente exp = expedienteRepository.save(Expediente.builder()
                .numeroExpediente("MPV-2026-990004")
                .anio(2026)
                .asunto("Expediente Prioritario")
                .tipoTramite(tipoTramite)
                .solicitante(ciudadano.getPersona())
                .oficinaResponsable(oficinaRentas)
                .estadoActual(EstadoExpediente.RECIBIDO)
                .fechaPresentacion(OffsetDateTime.now().minusHours(3))
                .fechaRecepcion(OffsetDateTime.now().minusHours(2))
                .prioridad(PrioridadExpediente.NORMAL)
                .build());

        // Operador actualiza prioridad a URGENTE
        ActualizarPrioridadRequest prioReq = ActualizarPrioridadRequest.builder()
                .prioridad(PrioridadExpediente.URGENTE)
                .motivo("Requerimiento con orden judicial perentoria")
                .build();

        mockMvc.perform(post("/api/mesa-partes/expedientes/{id}/prioridad", exp.getId())
                        .cookie(operadorCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(prioReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.prioridad").value("URGENTE"));

        // Descarga de Constancia Oficial de Recepción en PDF
        MvcResult res = mockMvc.perform(get("/api/mesa-partes/expedientes/{id}/constancia-recepcion", exp.getId())
                        .cookie(operadorCookie))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andReturn();

        byte[] pdfBytes = res.getResponse().getContentAsByteArray();
        assertThat(pdfBytes).isNotEmpty();
        assertThat(new String(pdfBytes, 0, 5)).isEqualTo("%PDF-");
    }
}
