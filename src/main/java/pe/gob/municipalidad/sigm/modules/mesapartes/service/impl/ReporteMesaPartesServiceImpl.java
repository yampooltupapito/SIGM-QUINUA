package pe.gob.municipalidad.sigm.modules.mesapartes.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.PersonaOficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.ReporteDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.Expediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.ReporteExportacion;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ReporteExportacionRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.ReporteMesaPartesService;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class ReporteMesaPartesServiceImpl implements ReporteMesaPartesService {

    private final ExpedienteRepository expedienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final PersonaOficinaRepository personaOficinaRepository;
    private final ReporteExportacionRepository exportacionRepository;

    public ReporteMesaPartesServiceImpl(ExpedienteRepository expedienteRepository,
                                       UsuarioRepository usuarioRepository,
                                       PersonaOficinaRepository personaOficinaRepository,
                                       ReporteExportacionRepository exportacionRepository) {
        this.expedienteRepository = expedienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.personaOficinaRepository = personaOficinaRepository;
        this.exportacionRepository = exportacionRepository;
    }

    @Override
    public ResumenEstadisticoDto obtenerResumen(FiltroReporteRequest filtro, String username) {
        Long oficinaId = resolverOficinaEfectiva(filtro != null ? filtro.getOficinaId() : null, username);
        OffsetDateTime[] fechas = ReporteFilterHelper.resolverRangoFechas(filtro);

        List<Object[]> conteos = expedienteRepository.countGroupByEstado(oficinaId, fechas[0], fechas[1]);

        long presentados = 0;
        long recibidos = 0;
        long enRevision = 0;
        long observados = 0;
        long enAtencion = 0;
        long resueltos = 0;
        long cerrados = 0;
        long archivados = 0;

        for (Object[] fila : conteos) {
            EstadoExpediente estado = (EstadoExpediente) fila[0];
            Long count = ((Number) fila[1]).longValue();
            if (estado == null) continue;

            if (estado == EstadoExpediente.PRESENTADO || estado == EstadoExpediente.BORRADOR) {
                presentados += count;
            } else if (estado == EstadoExpediente.RECIBIDO) {
                recibidos += count;
            } else if (estado == EstadoExpediente.EN_REVISION) {
                enRevision += count;
            } else if (estado == EstadoExpediente.OBSERVADO || estado == EstadoExpediente.EN_SUBSANACION) {
                observados += count;
            } else if (estado == EstadoExpediente.EN_ATENCION || estado == EstadoExpediente.DERIVADO) {
                enAtencion += count;
            } else if (estado == EstadoExpediente.RESUELTO) {
                resueltos += count;
            } else if (estado == EstadoExpediente.NOTIFICADO) {
                cerrados += count;
            } else if (estado == EstadoExpediente.ARCHIVADO || estado == EstadoExpediente.ANULADO) {
                archivados += count;
            }
        }

        long pendientes = presentados + recibidos + enRevision + observados + enAtencion;

        return ResumenEstadisticoDto.builder()
                .totalPresentados(presentados)
                .totalRecibidos(recibidos)
                .totalEnRevision(enRevision)
                .totalObservados(observados)
                .totalEnAtencion(enAtencion)
                .totalResueltos(resueltos)
                .totalCerrados(cerrados)
                .totalArchivados(archivados)
                .totalPendientes(pendientes)
                .build();
    }

    @Override
    public Page<ExpedienteReporteItemDto> listarExpedientes(FiltroReporteRequest filtro, Pageable pageable, String username) {
        Long oficinaId = resolverOficinaEfectiva(filtro != null ? filtro.getOficinaId() : null, username);
        OffsetDateTime[] fechas = ReporteFilterHelper.resolverRangoFechas(filtro);

        String termino = filtro != null && filtro.getTermino() != null && !filtro.getTermino().isBlank()
                ? "%" + filtro.getTermino().trim().toLowerCase() + "%"
                : null;

        Long tipoTramiteId = filtro != null ? filtro.getTipoTramiteId() : null;
        EstadoExpediente estado = filtro != null ? filtro.getEstado() : null;
        var prioridad = filtro != null ? filtro.getPrioridad() : null;
        var canal = filtro != null ? filtro.getCanalIngreso() : null;

        Page<Expediente> pagina = expedienteRepository.buscarBandejaConFiltros(
                estado,
                oficinaId,
                tipoTramiteId,
                prioridad,
                canal,
                fechas[0],
                fechas[1],
                termino,
                pageable
        );

        OffsetDateTime ahora = OffsetDateTime.now();

        return pagina.map(exp -> {
            OffsetDateTime fInicio = exp.getFechaPresentacion() != null ? exp.getFechaPresentacion() : exp.getCreatedAt();
            OffsetDateTime fFin = exp.getFechaCierre() != null ? exp.getFechaCierre() : ahora;
            long dias = fInicio != null ? Math.max(0, Duration.between(fInicio, fFin).toDays()) : 0;

            String solicitanteNombre = "";
            String solicitanteDoc = "";
            if (exp.getSolicitante() != null) {
                solicitanteNombre = (exp.getSolicitante().getNombres() != null ? exp.getSolicitante().getNombres() : "")
                        + " " + (exp.getSolicitante().getApellidoPaterno() != null ? exp.getSolicitante().getApellidoPaterno() : "");
                solicitanteDoc = exp.getSolicitante().getNumeroDocumento() != null ? exp.getSolicitante().getNumeroDocumento() : "";
            }

            return ExpedienteReporteItemDto.builder()
                    .id(exp.getId())
                    .numeroExpediente(exp.getNumeroExpediente())
                    .tipoTramite(exp.getTipoTramite() != null ? exp.getTipoTramite().getNombre() : "Trámite General")
                    .solicitante(solicitanteNombre.trim())
                    .solicitanteDoc(solicitanteDoc)
                    .asunto(exp.getAsunto())
                    .fechaPresentacion(exp.getFechaPresentacion() != null ? exp.getFechaPresentacion() : exp.getCreatedAt())
                    .oficina(exp.getOficinaResponsable() != null ? exp.getOficinaResponsable().getNombre() : "Sin asignar")
                    .estado(exp.getEstadoActual() != null ? exp.getEstadoActual().name() : "BORRADOR")
                    .prioridad(exp.getPrioridad() != null ? exp.getPrioridad().name() : "NORMAL")
                    .diasTranscurridos(dias)
                    .fechaCierre(exp.getFechaCierre())
                    .canalIngreso(exp.getCanalIngreso() != null ? exp.getCanalIngreso().name() : "VIRTUAL")
                    .build();
        });
    }

    @Override
    public Page<ExportacionHistorialDto> obtenerHistorialExportaciones(Pageable pageable, String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new AccessDeniedException("Usuario no encontrado: " + username));

        boolean esGlobal = usuario.getRoles().stream()
                .anyMatch(r -> "ROLE_ADMIN".equals(r.getCodigo()) || "ROLE_SUPERADMIN".equals(r.getCodigo()));

        Page<ReporteExportacion> page;
        if (esGlobal) {
            page = exportacionRepository.findAllWithUsuario(pageable);
        } else {
            page = exportacionRepository.findByUsuarioIdOrderByFechaExportacionDesc(usuario.getId(), pageable);
        }

        return page.map(e -> ExportacionHistorialDto.builder()
                .id(e.getId())
                .uuid(e.getUuid().toString())
                .usuarioNombre(e.getUsuario() != null && e.getUsuario().getPersona() != null
                        ? e.getUsuario().getPersona().getNombres() + " " + e.getUsuario().getPersona().getApellidoPaterno()
                        : (e.getUsuario() != null ? e.getUsuario().getUsername() : "Sistema"))
                .tipoReporte(e.getTipoReporte())
                .formato(e.getFormato())
                .cantidadRegistros(e.getCantidadRegistros())
                .estado(e.getEstado())
                .tiempoGeneracionMs(e.getTiempoGeneracionMs())
                .fechaExportacion(e.getFechaExportacion())
                .build());
    }

    @Override
    public Long resolverOficinaEfectiva(Long oficinaSolicitada, String username) {
        if (username == null) {
            return oficinaSolicitada;
        }
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new AccessDeniedException("Usuario no encontrado: " + username));
        return ReporteFilterHelper.resolverOficinaPermitida(usuario, oficinaSolicitada, personaOficinaRepository);
    }
}
