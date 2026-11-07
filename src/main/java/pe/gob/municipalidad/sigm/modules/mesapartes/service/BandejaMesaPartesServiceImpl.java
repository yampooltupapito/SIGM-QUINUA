package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Rol;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvAdminDtos.BandejaEstadisticasAdminResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvDtos.ExpedienteResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.CanalIngreso;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.PrioridadExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.mapper.MpvMapper;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteRepository;
import pe.gob.municipalidad.sigm.security.authorization.OfficeSecurityService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Slf4j
@Service
public class BandejaMesaPartesServiceImpl implements BandejaMesaPartesService {

    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");

    private final ExpedienteRepository expedienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final MpvMapper mpvMapper;
    private final OfficeSecurityService officeSecurityService;

    public BandejaMesaPartesServiceImpl(ExpedienteRepository expedienteRepository,
                                        UsuarioRepository usuarioRepository,
                                        MpvMapper mpvMapper,
                                        OfficeSecurityService officeSecurityService) {
        this.expedienteRepository = expedienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.mpvMapper = mpvMapper;
        this.officeSecurityService = officeSecurityService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExpedienteResponse> listarBandeja(
            EstadoExpediente estado,
            Long oficinaId,
            Long tipoTramiteId,
            PrioridadExpediente prioridad,
            CanalIngreso canalIngreso,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            String termino,
            Pageable pageable,
            String username
    ) {
        Usuario usuario = obtenerUsuario(username);

        if (esCiudadanoPuro(usuario)) {
            throw new AccessDeniedException("Acceso restringido: La bandeja institucional es de uso exclusivo para servidores públicos autorizados.");
        }

        Long oficinaFiltro = resolverOficinaFiltro(usuario, oficinaId);

        OffsetDateTime dtDesde = fechaDesde != null ? fechaDesde.atStartOfDay(ZONA_LIMA).toOffsetDateTime() : null;
        OffsetDateTime dtHasta = fechaHasta != null ? fechaHasta.atTime(LocalTime.MAX).atZone(ZONA_LIMA).toOffsetDateTime() : null;

        org.springframework.data.jpa.domain.Specification<pe.gob.municipalidad.sigm.modules.mesapartes.entity.Expediente> spec = (root, query, cb) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();

            if (estado != null) {
                predicates.add(cb.equal(root.get("estadoActual"), estado));
            }
            if (oficinaFiltro != null) {
                predicates.add(cb.equal(root.get("oficinaResponsable").get("id"), oficinaFiltro));
            }
            if (tipoTramiteId != null) {
                predicates.add(cb.equal(root.get("tipoTramite").get("id"), tipoTramiteId));
            }
            if (prioridad != null) {
                predicates.add(cb.equal(root.get("prioridad"), prioridad));
            }
            if (canalIngreso != null) {
                predicates.add(cb.equal(root.get("canalIngreso"), canalIngreso));
            }
            if (dtDesde != null) {
                predicates.add(cb.or(
                        cb.and(cb.isNotNull(root.get("fechaPresentacion")), cb.greaterThanOrEqualTo(root.get("fechaPresentacion"), dtDesde)),
                        cb.and(cb.isNull(root.get("fechaPresentacion")), cb.greaterThanOrEqualTo(root.get("createdAt"), dtDesde))
                ));
            }
            if (dtHasta != null) {
                predicates.add(cb.or(
                        cb.and(cb.isNotNull(root.get("fechaPresentacion")), cb.lessThanOrEqualTo(root.get("fechaPresentacion"), dtHasta)),
                        cb.and(cb.isNull(root.get("fechaPresentacion")), cb.lessThanOrEqualTo(root.get("createdAt"), dtHasta))
                ));
            }
            if (termino != null && !termino.trim().isEmpty()) {
                String pattern = "%" + termino.trim().toLowerCase() + "%";
                var solicitante = root.join("solicitante", jakarta.persistence.criteria.JoinType.LEFT);
                predicates.add(cb.or(
                        cb.like(cb.lower(cb.coalesce(root.get("numeroExpediente"), "")), pattern),
                        cb.like(cb.lower(root.get("asunto")), pattern),
                        cb.like(cb.lower(cb.coalesce(solicitante.get("nombres"), "")), pattern),
                        cb.like(cb.lower(cb.coalesce(solicitante.get("apellidoPaterno"), "")), pattern),
                        cb.like(cb.lower(cb.coalesce(solicitante.get("numeroDocumento"), "")), pattern)
                ));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return expedienteRepository.findAll(spec, pageable).map(mpvMapper::toExpedienteResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public BandejaEstadisticasAdminResponse obtenerEstadisticasBandeja(String username) {
        Usuario usuario = obtenerUsuario(username);

        if (esCiudadanoPuro(usuario)) {
            throw new AccessDeniedException("Acceso denegado a estadísticas institucionales.");
        }

        Long oficinaFiltro = resolverOficinaFiltro(usuario, null);

        long total = expedienteRepository.countByOficina(oficinaFiltro);
        long presentados = expedienteRepository.countByEstadoAndOficina(EstadoExpediente.PRESENTADO, oficinaFiltro);
        long recibidos = expedienteRepository.countByEstadoAndOficina(EstadoExpediente.RECIBIDO, oficinaFiltro);
        long enRevision = expedienteRepository.countByEstadoAndOficina(EstadoExpediente.EN_REVISION, oficinaFiltro);
        long observados = expedienteRepository.countByEstadoAndOficina(EstadoExpediente.OBSERVADO, oficinaFiltro);
        long pendientesSub = expedienteRepository.countByEstadoAndOficina(EstadoExpediente.EN_SUBSANACION, oficinaFiltro);
        long resueltos = expedienteRepository.countByEstadoAndOficina(EstadoExpediente.RESUELTO, oficinaFiltro);

        return BandejaEstadisticasAdminResponse.builder()
                .totalExpedientes(total)
                .presentados(presentados)
                .recibidos(recibidos)
                .enRevision(enRevision)
                .observados(observados)
                .pendientesSubsanacion(pendientesSub)
                .resueltos(resueltos)
                .build();
    }

    private Long resolverOficinaFiltro(Usuario usuario, Long oficinaSolicitada) {
        if (officeSecurityService.tieneAccesoTransversalMesaPartes(usuario)) {
            return oficinaSolicitada; // Administradores y Mesa de Partes central ven todas o filtran por cualquiera
        }
        Long oficinaAsignadaId = officeSecurityService.obtenerOficinaActivaId(usuario.getUsername());
        if (oficinaAsignadaId != null) {
            if (oficinaSolicitada != null && !oficinaSolicitada.equals(oficinaAsignadaId)) {
                throw new AccessDeniedException("No tiene autorización para consultar la bandeja de otra unidad orgánica.");
            }
            return oficinaAsignadaId;
        }
        return oficinaSolicitada;
    }

    private boolean tieneAccesoTotal(Usuario usuario) {
        if (usuario.getRoles() == null) return false;
        return usuario.getRoles().stream()
                .anyMatch(r -> {
                    String cod = r.getCodigo() != null ? r.getCodigo().toUpperCase() : "";
                    String nom = r.getNombre() != null ? r.getNombre().toUpperCase() : "";
                    return cod.contains("ADMIN") || cod.contains("OPERADOR") || cod.contains("MESA") || cod.contains("JEFE")
                            || nom.contains("ADMIN") || nom.contains("OPERADOR") || nom.contains("MESA") || nom.contains("JEFE");
                });
    }

    private boolean esCiudadanoPuro(Usuario usuario) {
        return !tieneAccesoTotal(usuario);
    }

    private Usuario obtenerUsuario(String username) {
        return usuarioRepository.findByUsernameWithRolesAndPersona(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
    }
}
