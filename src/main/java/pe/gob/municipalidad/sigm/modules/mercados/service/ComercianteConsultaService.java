package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.PersonaJuridica;
import pe.gob.municipalidad.sigm.common.repository.PersonaJuridicaRepresentanteRepository;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoComerciante;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoComerciante;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.Expediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteRepository;

import jakarta.persistence.criteria.Join;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio encargado de consultas avanzadas, detalle con pestañas, estadísticas y enmascaramiento de datos personales.
 */
@Slf4j
@Service
public class ComercianteConsultaService {

    private final ComercianteRepository comercianteRepository;
    private final PersonaJuridicaRepresentanteRepository representanteRepository;
    private final ComercianteCambioEstadoRepository cambioEstadoRepository;
    private final ComercianteDocumentoRepository documentoRepository;
    private final AsignacionPuestoRepository asignacionRepository;
    private final AutorizacionComercialRepository autorizacionRepository;
    private final ExpedienteRepository expedienteRepository;
    private final ActividadComercialRepository actividadRepository;

    public ComercianteConsultaService(
            ComercianteRepository comercianteRepository,
            PersonaJuridicaRepresentanteRepository representanteRepository,
            ComercianteCambioEstadoRepository cambioEstadoRepository,
            ComercianteDocumentoRepository documentoRepository,
            AsignacionPuestoRepository asignacionRepository,
            AutorizacionComercialRepository autorizacionRepository,
            ExpedienteRepository expedienteRepository,
            ActividadComercialRepository actividadRepository) {
        this.comercianteRepository = comercianteRepository;
        this.representanteRepository = representanteRepository;
        this.cambioEstadoRepository = cambioEstadoRepository;
        this.documentoRepository = documentoRepository;
        this.asignacionRepository = asignacionRepository;
        this.autorizacionRepository = autorizacionRepository;
        this.expedienteRepository = expedienteRepository;
        this.actividadRepository = actividadRepository;
    }

    @Transactional(readOnly = true)
    public ComercianteResponse obtenerPorId(UUID id) {
        Comerciante comerciante = comercianteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comerciante no encontrado con ID: " + id));
        return toResponse(comerciante, tienePermisoDatosSensibles());
    }

    @Transactional(readOnly = true)
    public ComercianteResponse obtenerPorCodigo(String codigo) {
        Comerciante comerciante = comercianteRepository.findByCodigoComerciante(codigo.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Comerciante no encontrado con código: " + codigo));
        return toResponse(comerciante, tienePermisoDatosSensibles());
    }

    @Transactional(readOnly = true)
    public Page<ComercianteResponse> listarPaginado(ComercianteFilter filter, Pageable pageable) {
        boolean tienePermiso = tienePermisoDatosSensibles();
        return comercianteRepository.findAll(filter.toSpecification(), pageable)
                .map(c -> toResponse(c, tienePermiso));
    }

    @Transactional(readOnly = true)
    public ComercianteDetailResponse obtenerDetalleCompleto(UUID id) {
        Comerciante c = comercianteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comerciante no encontrado con ID: " + id));

        boolean tienePermiso = tienePermisoDatosSensibles();
        String docTitular = c.getNumeroDocumentoTitular();
        String docEnmascarado = enmascararDocumento(docTitular, tienePermiso);

        ComercianteDetailResponse.ComercianteDetailResponseBuilder builder = ComercianteDetailResponse.builder()
                .id(c.getId())
                .codigoComerciante(c.getCodigoComerciante())
                .tipoComerciante(c.getTipoComerciante())
                .tipoComercianteDescripcion(c.getTipoComerciante() != null ? c.getTipoComerciante().getDescripcion() : null)
                .tipoDocumento(c.getTipoDocumentoTitular())
                .numeroDocumento(tienePermiso ? docTitular : docEnmascarado)
                .documentoEnmascarado(docEnmascarado)
                .telefono(tienePermiso ? c.getTelefonoContacto() : enmascararTelefono(c.getTelefonoContacto()))
                .correo(tienePermiso ? c.getCorreoContacto() : enmascararCorreo(c.getCorreoContacto()))
                .direccion(c.getDireccionContacto())
                .estado(c.getEstado())
                .estadoDescripcion(c.getEstado() != null ? c.getEstado().getDescripcion() : null)
                .fechaRegistro(c.getFechaRegistro())
                .observaciones(c.getObservaciones())
                .version(c.getVersion());

        // Datos Persona Natural
        if (c.isPersonaNatural() && c.getPersona() != null) {
            Persona p = c.getPersona();
            builder.personaId(p.getId())
                    .nombres(p.getNombres())
                    .apellidoPaterno(p.getApellidoPaterno())
                    .apellidoMaterno(p.getApellidoMaterno())
                    .nombreCompleto(p.getNombreCompleto());
        }

        // Datos Persona Jurídica
        if (c.isPersonaJuridica() && c.getPersonaJuridica() != null) {
            PersonaJuridica pj = c.getPersonaJuridica();
            builder.personaJuridicaId(pj.getId())
                    .ruc(tienePermiso ? pj.getRuc() : enmascararDocumento(pj.getRuc(), false))
                    .razonSocial(pj.getRazonSocial())
                    .nombreComercial(pj.getNombreComercial())
                    .domicilioFiscal(pj.getDomicilioFiscal())
                    .estadoRegistroInterno(pj.getEstadoRegistroInterno() != null ? pj.getEstadoRegistroInterno().getDescripcion() : null);

            // Representantes
            List<RepresentanteResponse> representantes = representanteRepository.findByPersonaJuridicaId(pj.getId()).stream()
                    .map(r -> {
                        Persona repP = r.getPersona();
                        return RepresentanteResponse.builder()
                                .id(r.getId())
                                .personaId(repP != null ? repP.getId() : null)
                                .dni(tienePermiso ? (repP != null ? repP.getNumeroDocumento() : "") : enmascararDocumento(repP != null ? repP.getNumeroDocumento() : "", false))
                                .nombreCompleto(repP != null ? repP.getNombreCompleto() : "")
                                .tipoRepresentacion(r.getTipoRepresentacion())
                                .tipoRepresentacionDescripcion(r.getTipoRepresentacion() != null ? r.getTipoRepresentacion().getDescripcion() : null)
                                .fechaInicio(r.getFechaInicio())
                                .fechaFin(r.getFechaFin())
                                .vigente(r.isVigente())
                                .estado(r.getEstado())
                                .build();
                    })
                    .collect(Collectors.toList());
            builder.representantes(representantes);
        }

        // Actividades Detalle
        List<ActividadComercianteResponse> actividades = c.getActividades().stream()
                .map(a -> ActividadComercianteResponse.builder()
                        .id(a.getId())
                        .codigo(a.getCodigo())
                        .nombre(a.getNombre())
                        .categoria(a.getCategoria())
                        .descripcion(a.getDescripcion())
                        .principal(true)
                        .estado(a.getEstado())
                        .build())
                .collect(Collectors.toList());
        builder.actividades(actividades);

        // Documentos
        List<ComercianteDocumentoResponse> documentos = documentoRepository.findByComercianteIdOrderByCreatedAtDesc(c.getId()).stream()
                .map(d -> ComercianteDocumentoResponse.builder()
                        .id(d.getId())
                        .documentoId(d.getDocumento() != null ? d.getDocumento().getId() : null)
                        .documentoUuid(d.getDocumento() != null ? d.getDocumento().getIdentificadorPublico() : null)
                        .tipoDocumento(d.getTipoDocumentoComerciante())
                        .numeroDocumento(d.getDocumento() != null ? String.valueOf(d.getDocumento().getId()) : null)
                        .titulo(d.getDocumento() != null ? d.getDocumento().getNombreOriginal() : null)
                        .observaciones(d.getObservaciones())
                        .fechaSubida(d.getCreatedAt())
                        .subidoPor(d.getCreatedBy())
                        .build())
                .collect(Collectors.toList());
        builder.documentos(documentos);

        // Puestos Asociados (Asignaciones)
        List<ComerciantePuestoAsociadoResponse> puestos = asignacionRepository.findByComercianteIdOrderByFechaInicioDesc(c.getId()).stream()
                .map(asig -> {
                    PuestoComercial puesto = asig.getPuesto();
                    MercadoMunicipal mercado = puesto != null ? puesto.getMercado() : null;
                    SectorMercado sector = puesto != null ? puesto.getSector() : null;
                    PabellonMercado pabellon = puesto != null ? puesto.getPabellon() : null;
                    return ComerciantePuestoAsociadoResponse.builder()
                            .asignacionId(asig.getId())
                            .mercadoId(mercado != null ? mercado.getId() : null)
                            .mercadoNombre(mercado != null ? mercado.getNombre() : null)
                            .puestoId(puesto != null ? puesto.getId() : null)
                            .puestoCodigo(puesto != null ? puesto.getCodigo() : null)
                            .puestoNumero(puesto != null ? puesto.getNumero() : null)
                            .sectorNombre(sector != null ? sector.getNombre() : null)
                            .pabellonNombre(pabellon != null ? pabellon.getNombre() : null)
                            .tipoAsignacion(asig.getTipoAsignacion() != null ? asig.getTipoAsignacion().name() : null)
                            .estadoAsignacion(asig.getEstado() != null ? asig.getEstado().name() : null)
                            .fechaInicio(asig.getFechaInicio())
                            .fechaFin(asig.getFechaFin())
                            .build();
                })
                .collect(Collectors.toList());
        builder.puestosAsociados(puestos);

        // Autorizaciones Comerciales
        List<ComercianteAutorizacionResponse> autorizaciones = autorizacionRepository.findByComercianteId(c.getId()).stream()
                .map(aut -> ComercianteAutorizacionResponse.builder()
                        .autorizacionId(aut.getId())
                        .numeroResolucion(aut.getNumeroAutorizacion())
                        .tipoAutorizacion(aut.getTipoAutorizacion())
                        .estado(aut.getEstado() != null ? aut.getEstado().name() : null)
                        .fechaEmision(aut.getFechaInicio())
                        .fechaVigenciaFin(aut.getFechaFin())
                        .mercadoNombre(aut.getPuesto() != null && aut.getPuesto().getMercado() != null ? aut.getPuesto().getMercado().getNombre() : null)
                        .puestoCodigo(aut.getPuesto() != null ? aut.getPuesto().getCodigo() : null)
                        .documentoId(aut.getDocumento() != null ? aut.getDocumento().getId() : null)
                        .build())
                .collect(Collectors.toList());
        builder.autorizaciones(autorizaciones);

        // Expedientes Vinculados (Mesa de Partes)
        if (c.isPersonaNatural() && c.getPersona() != null) {
            try {
                List<Expediente> expList = expedienteRepository.findAll((root, query, cb) ->
                        cb.equal(root.get("solicitante").get("id"), c.getPersona().getId()));
                List<ComercianteExpedienteResponse> expedientes = expList.stream()
                        .map(e -> ComercianteExpedienteResponse.builder()
                                .id(e.getId())
                                .numeroExpediente(e.getNumeroExpediente())
                                .asunto(e.getAsunto())
                                .estado(e.getEstadoActual() != null ? e.getEstadoActual().name() : null)
                                .fechaRegistro(e.getCreatedAt() != null ? e.getCreatedAt().toLocalDateTime() : null)
                                .build())
                        .collect(Collectors.toList());
                builder.expedientes(expedientes);
            } catch (Exception e) {
                log.debug("No se pudieron cargar expedientes MPV para comerciante {}: {}", c.getId(), e.getMessage());
            }
        }

        // Historial de Estados
        List<ComercianteCambioEstadoResponse> historial = cambioEstadoRepository.findByComercianteIdOrderByFechaCambioDesc(c.getId()).stream()
                .map(ce -> ComercianteCambioEstadoResponse.builder()
                        .id(ce.getId())
                        .estadoAnterior(ce.getEstadoAnterior())
                        .estadoNuevo(ce.getEstadoNuevo())
                        .motivo(ce.getMotivo())
                        .observaciones(ce.getObservaciones())
                        .usuarioId(ce.getUsuarioId())
                        .usuarioUsername(ce.getUsuarioUsername())
                        .fechaCambio(ce.getFechaCambio())
                        .build())
                .collect(Collectors.toList());
        builder.historialEstados(historial);

        return builder.build();
    }

    @Transactional(readOnly = true)
    public ComercianteEstadisticasResponse obtenerEstadisticas() {
        long total = comercianteRepository.count();
        long naturales = comercianteRepository.countByTipoComerciante(TipoComerciante.PERSONA_NATURAL);
        long juridicas = comercianteRepository.countByTipoComerciante(TipoComerciante.PERSONA_JURIDICA);
        long activos = comercianteRepository.countByEstado(EstadoComerciante.ACTIVO);
        long pendientes = comercianteRepository.countByEstado(EstadoComerciante.PENDIENTE_VERIFICACION);
        long suspendidos = comercianteRepository.countByEstado(EstadoComerciante.SUSPENDIDO);
        long inactivos = comercianteRepository.countByEstado(EstadoComerciante.INACTIVO);

        Map<String, Long> porActividad = new HashMap<>();
        List<ActividadComercial> actividades = actividadRepository.findAll();
        for (ActividadComercial act : actividades) {
            long count = comercianteRepository.findAll((root, query, cb) -> {
                query.distinct(true);
                Join<Comerciante, ActividadComercial> joinAct = root.join("actividades");
                return cb.equal(joinAct.get("id"), act.getId());
            }).size();
            if (count > 0) {
                porActividad.put(act.getNombre(), count);
            }
        }

        return ComercianteEstadisticasResponse.builder()
                .totalComerciantes(total)
                .personasNaturales(naturales)
                .personasJuridicas(juridicas)
                .activos(activos)
                .pendientesVerificacion(pendientes)
                .suspendidos(suspendidos)
                .inactivos(inactivos)
                .porActividad(porActividad)
                .build();
    }

    public ComercianteResponse toResponse(Comerciante c, boolean tienePermisoDatosSensibles) {
        String docTitular = c.getNumeroDocumentoTitular();
        String docEnmascarado = enmascararDocumento(docTitular, tienePermisoDatosSensibles);

        List<String> actividades = c.getActividades().stream()
                .map(ActividadComercial::getNombre)
                .sorted()
                .collect(Collectors.toList());

        String actividadPrincipal = actividades.isEmpty() ? "Sin actividad declarada" : actividades.get(0);

        return ComercianteResponse.builder()
                .id(c.getId())
                .codigoComerciante(c.getCodigoComerciante())
                .tipoComerciante(c.getTipoComerciante())
                .tipoComercianteDescripcion(c.getTipoComerciante() != null ? c.getTipoComerciante().getDescripcion() : null)
                .titularNombre(c.getNombreOTitular())
                .tipoDocumento(c.getTipoDocumentoTitular())
                .documentoEnmascarado(docEnmascarado)
                .documentoCompleto(tienePermisoDatosSensibles ? docTitular : null)
                .telefono(tienePermisoDatosSensibles ? c.getTelefonoContacto() : enmascararTelefono(c.getTelefonoContacto()))
                .correo(tienePermisoDatosSensibles ? c.getCorreoContacto() : enmascararCorreo(c.getCorreoContacto()))
                .direccion(c.getDireccionContacto())
                .estado(c.getEstado())
                .estadoDescripcion(c.getEstado() != null ? c.getEstado().getDescripcion() : null)
                .fechaRegistro(c.getFechaRegistro())
                .actividadPrincipal(actividadPrincipal)
                .actividades(actividades)
                .version(c.getVersion())
                .build();
    }

    public String enmascararDocumento(String documento, boolean tienePermiso) {
        if (tienePermiso || documento == null || documento.trim().isEmpty()) {
            return documento;
        }
        String clean = documento.trim();
        int len = clean.length();
        if (len == 8) { // DNI: ****1234
            return "****" + clean.substring(4);
        } else if (len == 11) { // RUC: 20******123
            return clean.substring(0, 2) + "******" + clean.substring(8);
        } else if (len > 4) {
            return "*".repeat(len - 4) + clean.substring(len - 4);
        }
        return "****";
    }

    public String enmascararTelefono(String telefono) {
        if (telefono == null || telefono.isBlank()) {
            return telefono;
        }
        String clean = telefono.trim();
        if (clean.length() <= 4) {
            return "****";
        }
        return "*".repeat(clean.length() - 4) + clean.substring(clean.length() - 4);
    }

    public String enmascararCorreo(String correo) {
        if (correo == null || correo.isBlank() || !correo.contains("@")) {
            return correo;
        }
        String[] parts = correo.split("@", 2);
        String name = parts[0];
        String domain = parts[1];
        if (name.length() <= 2) {
            return "*@" + domain;
        }
        return name.charAt(0) + "*".repeat(name.length() - 2) + name.charAt(name.length() - 1) + "@" + domain;
    }

    public boolean tienePermisoDatosSensibles() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("MERCADOS_COMERCIANTE_DATOS_SENSIBLES")
                        || a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_SUPERADMIN"));
    }
}
