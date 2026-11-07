package pe.gob.municipalidad.sigm.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.model.*;
import pe.gob.municipalidad.sigm.common.repository.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.RequisitoDocumental;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.TipoTramite;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoCatalogoTramite;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.UnidadPlazo;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.RequisitoDocumentalRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.TipoTramiteRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Semilla de datos institucionales de prueba exclusivamente activa bajo el perfil 'dev'.
 * Cumple con la pauta de arquitectura: se provisionan usuarios demostrativos institucionales
 * con contraseñas seguras y separación de funciones para evaluación del sistema.
 */
@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataSeeder implements ApplicationRunner {

    private final MunicipalidadRepository municipalidadRepository;
    private final OficinaRepository oficinaRepository;
    private final PersonaRepository personaRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final TipoActorRepository tipoActorRepository;
    private final PersonaOficinaRepository personaOficinaRepository;
    private final TipoTramiteRepository tipoTramiteRepository;
    private final RequisitoDocumentalRepository requisitoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Inicializando datos de prueba para entorno de desarrollo (perfil: dev)...");

        // 1. Municipalidad Distrital de Quinua
        Municipalidad muni = municipalidadRepository.findAll().stream().findFirst().orElseGet(() ->
                municipalidadRepository.save(Municipalidad.builder()
                        .nombre("Municipalidad Distrital de Quinua")
                        .nombreCorto("MD Quinua")
                        .ubigeo("050108")
                        .departamento("Ayacucho")
                        .provincia("Huamanga")
                        .distrito("Quinua")
                        .direccion("Plaza de Armas s/n, Quinua, Ayacucho")
                        .telefono("066-812345")
                        .correoInstitucional("mesadepartes@muniquinua.gob.pe")
                        .estado("ACTIVO")
                        .build())
        );

        // 2. Oficinas municipales
        Oficina ofiMpv = oficinaRepository.findByCodigo("OFI-MPV").orElseGet(() ->
                oficinaRepository.save(Oficina.builder()
                        .codigo("OFI-MPV")
                        .nombre("Mesa de Partes Virtual y Trámite Documentario")
                        .sigla("MPV")
                        .nivelJerarquico(1)
                        .estado("ACTIVO")
                        .municipalidad(muni)
                        .build())
        );

        Oficina ofiObras = oficinaRepository.findByCodigo("OFI-OBRAS").orElseGet(() ->
                oficinaRepository.save(Oficina.builder()
                        .codigo("OFI-OBRAS")
                        .nombre("Subgerencia de Desarrollo Urbano y Obras Públicas")
                        .sigla("SGDU")
                        .nivelJerarquico(2)
                        .estado("ACTIVO")
                        .municipalidad(muni)
                        .build())
        );

        Oficina ofiSecGen = oficinaRepository.findByCodigo("OFI-SG").orElseGet(() ->
                oficinaRepository.save(Oficina.builder()
                        .codigo("OFI-SG")
                        .nombre("Secretaría General y Archivo")
                        .sigla("SG")
                        .nivelJerarquico(1)
                        .estado("ACTIVO")
                        .municipalidad(muni)
                        .build())
        );

        // 3. Tipo de actor institucional
        TipoActor tipoServidor = tipoActorRepository.findAll().stream().findFirst().orElseGet(() ->
                tipoActorRepository.save(TipoActor.builder()
                        .codigo("SERVIDOR_PUBLICO")
                        .nombre("Servidor Municipal")
                        .descripcion("Servidor de planta o CAS")
                        .build())
        );

        // 4. Roles institucionales
        Rol rolSuperAdmin = obtenerRol("ROLE_SUPERADMIN", "Superadministrador General");
        Rol rolAdmin = obtenerRol("ROLE_ADMIN", "Administrador General del Sistema");
        Rol rolMesaPartes = obtenerRol("ROLE_MESA_PARTES", "Operador de Mesa de Partes");
        Rol rolJefe = obtenerRol("ROLE_JEFE_OFICINA", "Jefe de Unidad Orgánica");
        Rol rolOperador = obtenerRol("ROLE_OPERADOR", "Operador Administrativo");
        Rol rolCiudadano = obtenerRol("ROLE_CIUDADANO", "Ciudadano / Administrado");

        // Asignar todos los permisos existentes a roles de administración
        List<Permiso> todosPermisos = permisoRepository.findAll();
        if (!todosPermisos.isEmpty()) {
            if (rolSuperAdmin.getPermisos() == null || rolSuperAdmin.getPermisos().isEmpty()) {
                rolSuperAdmin.setPermisos(new HashSet<>(todosPermisos));
                rolRepository.save(rolSuperAdmin);
            }
            if (rolAdmin.getPermisos() == null || rolAdmin.getPermisos().isEmpty()) {
                rolAdmin.setPermisos(new HashSet<>(todosPermisos));
                rolRepository.save(rolAdmin);
            }
        }

        // 5. Creación de usuarios institucionales de prueba
        // A) Superadministrador / Administrador
        crearUsuarioSiNoExiste(
                "admin",
                "Admin2026!",
                "45000001",
                "Alonso",
                "Gutiérrez",
                "Quinua",
                "admin@muniquinua.gob.pe",
                Set.of(rolSuperAdmin, rolAdmin),
                ofiSecGen,
                tipoServidor
        );

        // B) Operador de Mesa de Partes
        crearUsuarioSiNoExiste(
                "operador.mpv",
                "Operador2026!",
                "70112233",
                "María",
                "Fernández",
                "Quispe",
                "mesapartes@muniquinua.gob.pe",
                Set.of(rolMesaPartes, rolOperador),
                ofiMpv,
                tipoServidor
        );

        // C) Jefe de Oficina de Obras Públicas
        crearUsuarioSiNoExiste(
                "jefe.obras",
                "Jefe2026!",
                "71223344",
                "Carlos",
                "Mendoza",
                "Huamán",
                "desarrollourbano@muniquinua.gob.pe",
                Set.of(rolJefe),
                ofiObras,
                tipoServidor
        );

        // D) Especialista Técnico
        crearUsuarioSiNoExiste(
                "especialista",
                "Tecnico2026!",
                "72334455",
                "Javier",
                "Rojas",
                "Ayala",
                "especialista.obras@muniquinua.gob.pe",
                Set.of(rolOperador),
                ofiObras,
                tipoServidor
        );

        // E) Ciudadano / Administrado
        crearUsuarioSiNoExiste(
                "ciudadano",
                "Ciudadano2026!",
                "44556677",
                "Juan",
                "Pérez",
                "Gómez",
                "vecino.quinua@gmail.com",
                Set.of(rolCiudadano),
                null,
                null
        );

        // 6. Trámites TUPA iniciales publicados
        if (tipoTramiteRepository.count() == 0) {
            TipoTramite tramiteEdificacion = tipoTramiteRepository.save(TipoTramite.builder()
                    .codigo("TR-LIC-001")
                    .nombre("Licencia de Edificación y Conformidad de Obra")
                    .descripcion("Procedimiento para solicitar autorización de obras de construcción unifamiliar o comercial.")
                    .codigoTupa("TUPA-URB-01")
                    .baseNormativa("Ley N° 29090 y Ordenanza Municipal N° 004-2025-MDQ")
                    .plazoDias(30)
                    .unidadPlazo(UnidadPlazo.DIAS_HABILES)
                    .estado("ACTIVO")
                    .estadoCatalogo(EstadoCatalogoTramite.PUBLICADO)
                    .versionActual(1)
                    .permitePresentacionVirtual(true)
                    .permitePresencial(true)
                    .requiereTasa(true)
                    .montoTasa(new BigDecimal("85.50"))
                    .cuentaTasa("00-066-8921 BN")
                    .oficinaResponsable(ofiObras)
                    .build());

            requisitoRepository.save(RequisitoDocumental.builder()
                    .tipoTramite(tramiteEdificacion)
                    .codigo("REQ-MEMORIA")
                    .nombre("Memoria Descriptiva y Planos de Arquitectura")
                    .descripcion("Documentación técnica firmada por arquitecto o ingeniero colegiado.")
                    .obligatorio(true)
                    .formatosPermitidos("application/pdf,image/jpeg")
                    .tamanoMaximoMb(15)
                    .orden(1)
                    .estado("ACTIVO")
                    .build());

            requisitoRepository.save(RequisitoDocumental.builder()
                    .tipoTramite(tramiteEdificacion)
                    .codigo("REQ-PAGO")
                    .nombre("Comprobante de Pago de Tasa Administrativa")
                    .descripcion("Voucher de pago Banco de la Nación o caja municipal.")
                    .obligatorio(true)
                    .formatosPermitidos("application/pdf,image/jpeg,image/png")
                    .tamanoMaximoMb(5)
                    .orden(2)
                    .estado("ACTIVO")
                    .build());

            tipoTramiteRepository.save(TipoTramite.builder()
                    .codigo("TR-POS-001")
                    .nombre("Constancia de Posesión de Predio")
                    .descripcion("Certificación municipal de posesión continua y pacífica de predios en Quinua.")
                    .codigoTupa("TUPA-URB-02")
                    .baseNormativa("Ley Orgánica de Municipalidades N° 27972")
                    .plazoDias(15)
                    .unidadPlazo(UnidadPlazo.DIAS_HABILES)
                    .estado("ACTIVO")
                    .estadoCatalogo(EstadoCatalogoTramite.PUBLICADO)
                    .versionActual(1)
                    .permitePresentacionVirtual(true)
                    .permitePresencial(true)
                    .requiereTasa(false)
                    .oficinaResponsable(ofiObras)
                    .build());

            log.info("Trámites TUPA iniciales publicados correctamente.");
        }

        log.info("Semilla de datos para desarrollo inicializada con éxito.");
    }

    private Rol obtenerRol(String codigo, String nombre) {
        return rolRepository.findByCodigo(codigo).orElseGet(() ->
                rolRepository.save(Rol.builder()
                        .codigo(codigo)
                        .nombre(nombre)
                        .descripcion(nombre)
                        .estado("ACTIVO")
                        .build())
        );
    }

    private void crearUsuarioSiNoExiste(
            String username,
            String rawPassword,
            String dni,
            String nombres,
            String apePaterno,
            String apeMaterno,
            String correo,
            Set<Rol> roles,
            Oficina oficina,
            TipoActor tipoActor
    ) {
        var existenteOpt = usuarioRepository.findByUsername(username);
        if (existenteOpt.isPresent()) {
            Usuario existente = existenteOpt.get();
            if (!Boolean.TRUE.equals(existente.getEnabled())
                    || Boolean.TRUE.equals(existente.getAccountLocked())
                    || (existente.getFailedAttempts() != null && existente.getFailedAttempts() > 0)) {
                existente.setEnabled(true);
                existente.setAccountLocked(false);
                existente.setFailedAttempts(0);
                usuarioRepository.save(existente);
            }
            return;
        }

        Persona persona = personaRepository.findByTipoDocumentoAndNumeroDocumento("DNI", dni).orElseGet(() ->
                personaRepository.save(Persona.builder()
                        .tipoDocumento("DNI")
                        .numeroDocumento(dni)
                        .nombres(nombres)
                        .apellidoPaterno(apePaterno)
                        .apellidoMaterno(apeMaterno)
                        .correo(correo)
                        .direccion("Plaza de Armas de Quinua s/n, Ayacucho")
                        .telefono("966123456")
                        .estado("ACTIVO")
                        .build())
        );

        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .persona(persona)
                .roles(roles)
                .enabled(true)
                .accountLocked(false)
                .failedAttempts(0)
                .build());

        if (oficina != null && tipoActor != null) {
            personaOficinaRepository.save(PersonaOficina.builder()
                    .persona(persona)
                    .oficina(oficina)
                    .tipoActor(tipoActor)
                    .fechaInicio(LocalDate.now())
                    .principal(true)
                    .estado("ACTIVO")
                    .build());
        }

        log.info("Usuario creado: '{}' ({})", username, persona.getNombres() + " " + persona.getApellidoPaterno());
    }
}
