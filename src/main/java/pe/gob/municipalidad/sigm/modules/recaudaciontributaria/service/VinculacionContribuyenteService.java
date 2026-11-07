package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.document.repository.DocumentoRepository;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.portal.AprobarRechazarVinculacionRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.portal.SolicitarVinculacionRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.portal.VinculacionResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ContribuyenteTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.SolicitudVinculacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.VinculacionUsuarioContribuyente;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoVinculacionContribuyente;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoVinculacionContribuyente;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ContribuyenteTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.SolicitudVinculacionTributariaRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.VinculacionUsuarioContribuyenteRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Gestión del ciclo de vida de vinculaciones entre usuarios y contribuyentes.
 * Previene asociación automática indiscriminada y valida titularidad y acreditación.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VinculacionContribuyenteService {

    private final VinculacionUsuarioContribuyenteRepository vinculacionRepository;
    private final SolicitudVinculacionTributariaRepository solicitudRepository;
    private final ContribuyenteTributarioRepository contribuyenteRepository;
    private final DocumentoRepository documentoRepository;

    /**
     * Registra una solicitud de vinculación tributaria iniciada por el ciudadano.
     * Valida titularidad o acreditación fehaciente.
     */
    @Transactional
    public VinculacionResponse solicitarVinculacion(SolicitarVinculacionRequest request, Usuario usuario) {
        if (usuario == null) {
            throw new BusinessException("Usuario no autenticado.");
        }

        // 1. Buscar contribuyente por código o documento
        ContribuyenteTributario contribuyente = null;
        if (request.getCodigoContribuyente() != null && !request.getCodigoContribuyente().trim().isEmpty()) {
            contribuyente = contribuyenteRepository.findByCodigoContribuyente(request.getCodigoContribuyente().trim())
                    .orElse(null);
        }

        if (contribuyente == null) {
            contribuyente = contribuyenteRepository.findByNumeroDocumento(request.getNumeroDocumento().trim())
                    .orElseThrow(() -> new BusinessException("No existe un contribuyente tributario registrado con los datos proporcionados. Acérquese a la Subgerencia de Rentas."));
        }

        // 2. Verificar si ya existe vinculación registrada
        Optional<VinculacionUsuarioContribuyente> existente = vinculacionRepository
                .findByUsuarioIdAndContribuyenteId(usuario.getId(), contribuyente.getId());

        if (existente.isPresent()) {
            VinculacionUsuarioContribuyente v = existente.get();
            if (v.getEstado() == EstadoVinculacionContribuyente.APROBADA && v.isVigente()) {
                throw new BusinessException("Ya cuenta con una vinculación aprobada y vigente para este contribuyente.");
            }
            if (v.getEstado() == EstadoVinculacionContribuyente.PENDIENTE || v.getEstado() == EstadoVinculacionContribuyente.EN_VERIFICACION) {
                throw new BusinessException("Ya existe una solicitud de vinculación en proceso de evaluación.");
            }
        }

        // 3. Documento de sustento
        Documento docSustento = null;
        if (request.getDocumentoSustentoId() != null) {
            docSustento = documentoRepository.findById(request.getDocumentoSustentoId()).orElse(null);
        }

        // 4. Regla estricta: si es titular, cotejar documento de la persona asociada a la cuenta
        EstadoVinculacionContribuyente estadoInicial = EstadoVinculacionContribuyente.PENDIENTE;
        OffsetDateTime fechaVerif = null;

        Persona personaUsuario = usuario.getPersona();
        if (request.getTipoVinculacion() == TipoVinculacionContribuyente.TITULAR_VERIFICADO) {
            if (personaUsuario == null || !request.getNumeroDocumento().trim().equalsIgnoreCase(personaUsuario.getNumeroDocumento())) {
                throw new BusinessException("Para solicitar vinculación como Titular, el documento de identidad debe coincidir exactamente con el titular de la cuenta ciudadana.");
            }
            // Si el contribuyente tributario tiene la misma persona natural registrada en el padrón, auto-aprobar titularidad
            if (contribuyente.getPersona() != null && contribuyente.getPersona().getId().equals(personaUsuario.getId())) {
                estadoInicial = EstadoVinculacionContribuyente.APROBADA;
                fechaVerif = OffsetDateTime.now();
                log.info("Auto-aprobación de vinculación TITULAR_VERIFICADO para usuario '{}' y contribuyente '{}'",
                        usuario.getUsername(), contribuyente.getCodigoContribuyente());
            }
        } else {
            // Representación o apoderado siempre requiere documento de sustento y validación administrativa
            if (docSustento == null && (request.getSustento() == null || request.getSustento().trim().length() < 10)) {
                throw new BusinessException("La solicitud de representación requiere adjuntar poder, partida o sustento legal detallado.");
            }
        }

        final ContribuyenteTributario contribuyenteFinal = contribuyente;
        VinculacionUsuarioContribuyente vinculacion = existente.orElseGet(() -> VinculacionUsuarioContribuyente.builder()
                .usuario(usuario)
                .contribuyente(contribuyenteFinal)
                .build());

        vinculacion.setTipoVinculacion(request.getTipoVinculacion());
        vinculacion.setEstado(estadoInicial);
        vinculacion.setFechaSolicitud(OffsetDateTime.now());
        vinculacion.setFechaVerificacion(fechaVerif);
        vinculacion.setVerificadoPor(estadoInicial == EstadoVinculacionContribuyente.APROBADA ? usuario : null);
        vinculacion.setDocumentoSustento(docSustento);
        vinculacion.setFechaVigenciaHasta(request.getFechaVigenciaHasta());
        vinculacion.setMotivoObservacion(null);
        vinculacion.setMotivoRechazoRevocacion(null);
        vinculacion.setActivo(true);
        vinculacion.setCreatedBy(usuario.getUsername());

        VinculacionUsuarioContribuyente guardada = vinculacionRepository.save(vinculacion);

        // Registrar trámite en solicitudes
        String codigoSol = "SOL-VINC-" + System.currentTimeMillis();
        SolicitudVinculacionTributaria sol = SolicitudVinculacionTributaria.builder()
                .vinculacion(guardada)
                .codigoSolicitud(codigoSol)
                .tipoSolicitud(request.getTipoVinculacion().name())
                .sustento(request.getSustento() != null ? request.getSustento().trim() : "Solicitud ciudadana")
                .estado(estadoInicial == EstadoVinculacionContribuyente.APROBADA ? "APROBADA" : "REGISTRADA")
                .build();
        sol.setCreatedBy(usuario.getUsername());
        solicitudRepository.save(sol);

        return convertirAResponse(guardada);
    }

    /**
     * Aprobación administrativa por funcionario de Rentas.
     */
    @Transactional
    public VinculacionResponse aprobarVinculacion(UUID vinculacionId, AprobarRechazarVinculacionRequest request, Usuario funcionario) {
        VinculacionUsuarioContribuyente vinc = vinculacionRepository.findById(vinculacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Vinculación no encontrada."));

        if (vinc.getEstado() == EstadoVinculacionContribuyente.APROBADA && vinc.isVigente()) {
            throw new BusinessException("La vinculación ya se encuentra aprobada.");
        }

        vinc.setEstado(EstadoVinculacionContribuyente.APROBADA);
        vinc.setFechaVerificacion(OffsetDateTime.now());
        vinc.setVerificadoPor(funcionario);
        vinc.setFechaVigenciaHasta(request.getFechaVigenciaHasta() != null ? request.getFechaVigenciaHasta() : vinc.getFechaVigenciaHasta());
        vinc.setActivo(true);
        vinc.setMotivoObservacion(null);
        vinc.setMotivoRechazoRevocacion(null);
        vinc.setUpdatedBy(funcionario != null ? funcionario.getUsername() : "SISTEMA");

        VinculacionUsuarioContribuyente actualizada = vinculacionRepository.save(vinc);
        log.info("Vinculación '{}' aprobada por funcionario '{}'", vinculacionId, funcionario != null ? funcionario.getUsername() : "SISTEMA");

        return convertirAResponse(actualizada);
    }

    /**
     * Rechazo motivado de la solicitud de vinculación.
     */
    @Transactional
    public VinculacionResponse rechazarVinculacion(UUID vinculacionId, AprobarRechazarVinculacionRequest request, Usuario funcionario) {
        VinculacionUsuarioContribuyente vinc = vinculacionRepository.findById(vinculacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Vinculación no encontrada."));

        vinc.setEstado(EstadoVinculacionContribuyente.RECHAZADA);
        vinc.setFechaVerificacion(OffsetDateTime.now());
        vinc.setVerificadoPor(funcionario);
        vinc.setMotivoRechazoRevocacion(request.getMotivo());
        vinc.setActivo(false);
        vinc.setUpdatedBy(funcionario != null ? funcionario.getUsername() : "SISTEMA");

        VinculacionUsuarioContribuyente actualizada = vinculacionRepository.save(vinc);
        log.info("Vinculación '{}' rechazada por funcionario '{}'. Motivo: {}",
                vinculacionId, funcionario != null ? funcionario.getUsername() : "SISTEMA", request.getMotivo());

        return convertirAResponse(actualizada);
    }

    /**
     * Revocación administrativa de una vinculación existente.
     */
    @Transactional
    public VinculacionResponse revocarVinculacion(UUID vinculacionId, AprobarRechazarVinculacionRequest request, Usuario funcionario) {
        VinculacionUsuarioContribuyente vinc = vinculacionRepository.findById(vinculacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Vinculación no encontrada."));

        vinc.setEstado(EstadoVinculacionContribuyente.REVOCADA);
        vinc.setFechaVerificacion(OffsetDateTime.now());
        vinc.setVerificadoPor(funcionario);
        vinc.setMotivoRechazoRevocacion(request.getMotivo());
        vinc.setActivo(false);
        vinc.setUpdatedBy(funcionario != null ? funcionario.getUsername() : "SISTEMA");

        VinculacionUsuarioContribuyente actualizada = vinculacionRepository.save(vinc);
        log.info("Vinculación '{}' revocada por funcionario '{}'", vinculacionId, funcionario != null ? funcionario.getUsername() : "SISTEMA");

        return convertirAResponse(actualizada);
    }

    /**
     * Lista las vinculaciones registradas para el usuario autenticado.
     */
    @Transactional(readOnly = true)
    public List<VinculacionResponse> listarVinculacionesPorUsuario(Long usuarioId) {
        return vinculacionRepository.findByUsuarioIdAndActivoTrue(usuarioId).stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    /**
     * Búsqueda paginada para la bandeja administrativa de revisión.
     */
    @Transactional(readOnly = true)
    public Page<VinculacionResponse> listarBandejaAdministrativa(EstadoVinculacionContribuyente estado, String q, Pageable pageable) {
        Page<VinculacionUsuarioContribuyente> pageResult;
        if (q != null && !q.isBlank()) {
            pageResult = vinculacionRepository.buscarVinculacionesAdmin(estado, q.trim(), pageable);
        } else if (estado != null) {
            pageResult = vinculacionRepository.findByEstado(estado, pageable);
        } else {
            pageResult = vinculacionRepository.findAll(pageable);
        }
        return pageResult.map(this::convertirAResponse);
    }

    public VinculacionResponse convertirAResponse(VinculacionUsuarioContribuyente v) {
        return VinculacionResponse.builder()
                .id(v.getId())
                .usuarioId(v.getUsuario().getId())
                .username(v.getUsuario().getUsername())
                .usuarioNombreCompleto(v.getUsuario().getNombreCompleto())
                .contribuyenteId(v.getContribuyente().getId())
                .contribuyenteCodigo(v.getContribuyente().getCodigoContribuyente())
                .contribuyenteNombre(v.getContribuyente().getNombreRazonSocial())
                .contribuyenteDocumento(v.getContribuyente().getTipoDocumento() + ": " + v.getContribuyente().getNumeroDocumento())
                .tipoVinculacion(v.getTipoVinculacion())
                .estado(v.getEstado())
                .fechaSolicitud(v.getFechaSolicitud())
                .fechaVerificacion(v.getFechaVerificacion())
                .verificadoPorUsername(v.getVerificadoPor() != null ? v.getVerificadoPor().getUsername() : null)
                .documentoSustentoId(v.getDocumentoSustento() != null ? v.getDocumentoSustento().getId() : null)
                .documentoSustentoNombre(v.getDocumentoSustento() != null ? v.getDocumentoSustento().getNombreOriginal() : null)
                .fechaVigenciaHasta(v.getFechaVigenciaHasta())
                .motivoObservacion(v.getMotivoObservacion())
                .motivoRechazoRevocacion(v.getMotivoRechazoRevocacion())
                .activo(Boolean.TRUE.equals(v.getActivo()))
                .vigente(v.isVigente())
                .build();
    }
}

