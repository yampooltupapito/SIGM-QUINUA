package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.ContribuyenteService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.specification.ContribuyenteSpecification;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.validation.ContribuyenteValidator;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContribuyenteServiceImpl implements ContribuyenteService {

    private final ContribuyenteTributarioRepository contribuyenteRepository;
    private final RepresentanteContribuyenteRepository representanteRepository;
    private final DomicilioContribuyenteRepository domicilioRepository;
    private final ContactoContribuyenteRepository contactoRepository;
    private final HistorialEstadoContribuyenteRepository historialRepository;
    private final PersonaRepository personaRepository;
    private final OficinaRepository oficinaRepository;
    private final ContribuyenteValidator contribuyenteValidator;

    @Override
    @Transactional
    public ContribuyenteDetailResponse registrarContribuyente(ContribuyenteCreateRequest request, String usuario) {
        log.info("Iniciando registro de contribuyente por usuario '{}': tipo={}, doc={} {}",
                usuario, request.getTipoContribuyente(), request.getTipoDocumento(), request.getNumeroDocumento());

        EstadoVerificacionDocumento estadoVerif = contribuyenteValidator.validarCreacion(request);

        // Generar código único y estable
        Long siguienteSeq = contribuyenteRepository.obtenerSiguienteValorSecuencia();
        String codigoGenerado = String.format("CT-%06d", siguienteSeq);

        while (contribuyenteRepository.existsByCodigoContribuyente(codigoGenerado)) {
            siguienteSeq++;
            codigoGenerado = String.format("CT-%06d", siguienteSeq);
        }

        // Determinar nombre o razón social formal
        String nombreFinal;
        if (request.getTipoContribuyente() == TipoContribuyente.PERSONA_NATURAL) {
            StringBuilder sb = new StringBuilder();
            sb.append(request.getPrimerApellido().trim().toUpperCase());
            if (request.getSegundoApellido() != null && !request.getSegundoApellido().trim().isEmpty()) {
                sb.append(" ").append(request.getSegundoApellido().trim().toUpperCase());
            }
            sb.append(", ").append(request.getNombres().trim());
            nombreFinal = sb.toString();
        } else {
            nombreFinal = request.getNombreRazonSocial().trim();
        }

        // Vincular persona institucional si ya existe en el sistema
        Optional<Persona> personaOpt = personaRepository.findByTipoDocumentoAndNumeroDocumento(
                request.getTipoDocumento().trim().toUpperCase(),
                request.getNumeroDocumento().trim());

        Oficina oficina = null;
        if (request.getOficinaRegistroId() != null) {
            oficina = oficinaRepository.findById(request.getOficinaRegistroId()).orElse(null);
        }

        ContribuyenteTributario contribuyente = ContribuyenteTributario.builder()
                .codigoContribuyente(codigoGenerado)
                .persona(personaOpt.orElse(null))
                .tipoContribuyente(request.getTipoContribuyente())
                .tipoDocumento(request.getTipoDocumento().trim().toUpperCase())
                .numeroDocumento(request.getNumeroDocumento().trim())
                .nombreRazonSocial(nombreFinal)
                .nombreComercial(request.getNombreComercial() != null ? request.getNombreComercial().trim() : null)
                .nombres(request.getNombres() != null ? request.getNombres().trim() : null)
                .primerApellido(request.getPrimerApellido() != null ? request.getPrimerApellido().trim() : null)
                .segundoApellido(request.getSegundoApellido() != null ? request.getSegundoApellido().trim() : null)
                .representanteLegalNombre(request.getRepresentanteLegalNombre())
                .representanteLegalDocumento(request.getRepresentanteLegalDocumento())
                .domicilioFiscal(request.getDomicilioFiscal())
                .referenciaDomicilio(request.getReferenciaDomicilio())
                .telefono(request.getTelefono())
                .telefonoAlternativo(request.getTelefonoAlternativo())
                .correo(request.getCorreo())
                .medioContactoPreferido(request.getMedioContactoPreferido() != null ?
                        request.getMedioContactoPreferido() : MedioContactoPreferido.CORREO_ELECTRONICO)
                .estado(EstadoContribuyente.ACTIVO)
                .estadoVerificacion(estadoVerif)
                .fechaRegistro(LocalDate.now())
                .oficinaRegistro(oficina)
                .observaciones(request.getObservaciones())
                .build();

        ContribuyenteTributario guardado = contribuyenteRepository.save(contribuyente);

        // Si se especificó domicilio inicial, registrarlo en la tabla de domicilios
        if ((request.getDomicilioFiscal() != null && !request.getDomicilioFiscal().trim().isEmpty()) ||
            (request.getComunidadCentroPoblado() != null && !request.getComunidadCentroPoblado().trim().isEmpty()) ||
            (request.getNombreVia() != null && !request.getNombreVia().trim().isEmpty())) {

            DomicilioContribuyente dom = DomicilioContribuyente.builder()
                    .contribuyente(guardado)
                    .tipoDomicilio(TipoDomicilioContribuyente.FISCAL)
                    .departamento("Ayacucho")
                    .provincia("Huamanga")
                    .distrito("Quinua")
                    .ubigeo("050108")
                    .tipoVia(request.getTipoVia())
                    .nombreVia(request.getNombreVia())
                    .numeroMunicipal(request.getNumeroMunicipal())
                    .comunidadCentroPoblado(request.getComunidadCentroPoblado())
                    .referencia(request.getReferenciaDomicilio())
                    .esFiscal(true)
                    .esNotificacion(true)
                    .estado("ACTIVO")
                    .fechaRegistro(LocalDate.now())
                    .build();
            domicilioRepository.save(dom);
            guardado.getDomicilios().add(dom);
        }

        // Si se especificó contacto telefónico o correo, registrarlos
        if (request.getTelefono() != null && !request.getTelefono().trim().isEmpty()) {
            ContactoContribuyente contTel = ContactoContribuyente.builder()
                    .contribuyente(guardado)
                    .tipoContacto(TipoContactoContribuyente.TELEFONO_MOVIL)
                    .valor(request.getTelefono().trim())
                    .descripcion("Teléfono principal")
                    .esPrincipal(true)
                    .autorizadoNotificacion(true)
                    .estado("ACTIVO")
                    .build();
            contactoRepository.save(contTel);
            guardado.getContactos().add(contTel);
        }

        if (request.getCorreo() != null && !request.getCorreo().trim().isEmpty()) {
            ContactoContribuyente contEmail = ContactoContribuyente.builder()
                    .contribuyente(guardado)
                    .tipoContacto(TipoContactoContribuyente.CORREO_ELECTRONICO)
                    .valor(request.getCorreo().trim())
                    .descripcion("Correo principal de contacto")
                    .esPrincipal(false)
                    .autorizadoNotificacion(true)
                    .estado("ACTIVO")
                    .build();
            contactoRepository.save(contEmail);
            guardado.getContactos().add(contEmail);
        }

        // Registrar auditoría en el historial de estados
        HistorialEstadoContribuyente hist = HistorialEstadoContribuyente.builder()
                .contribuyente(guardado)
                .estadoAnterior(EstadoContribuyente.ACTIVO)
                .nuevoEstado(EstadoContribuyente.ACTIVO)
                .motivo("Alta y registro inicial del contribuyente en el padrón tributario municipal")
                .usuarioRegistro(usuario != null ? usuario : "SYSTEM")
                .fechaRegistro(OffsetDateTime.now())
                .build();
        historialRepository.save(hist);
        guardado.getHistorialEstados().add(hist);

        log.info("Contribuyente registrado con éxito: ID={}, Código={}", guardado.getId(), guardado.getCodigoContribuyente());
        return mapToDetailResponse(guardado);
    }

    @Override
    @Transactional
    public ContribuyenteDetailResponse actualizarContribuyente(UUID id, ContribuyenteUpdateRequest request, String usuario) {
        ContribuyenteTributario contribuyente = contribuyenteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el contribuyente con ID: " + id));

        contribuyenteValidator.validarActualizacion(id, request);

        contribuyente.setTipoDocumento(request.getTipoDocumento().trim().toUpperCase());
        contribuyente.setNumeroDocumento(request.getNumeroDocumento().trim());

        if (contribuyente.getTipoContribuyente() == TipoContribuyente.PERSONA_NATURAL) {
            contribuyente.setNombres(request.getNombres() != null ? request.getNombres().trim() : null);
            contribuyente.setPrimerApellido(request.getPrimerApellido() != null ? request.getPrimerApellido().trim() : null);
            contribuyente.setSegundoApellido(request.getSegundoApellido() != null ? request.getSegundoApellido().trim() : null);

            StringBuilder sb = new StringBuilder();
            if (request.getPrimerApellido() != null) sb.append(request.getPrimerApellido().trim().toUpperCase());
            if (request.getSegundoApellido() != null && !request.getSegundoApellido().trim().isEmpty()) {
                sb.append(" ").append(request.getSegundoApellido().trim().toUpperCase());
            }
            if (request.getNombres() != null) sb.append(", ").append(request.getNombres().trim());
            contribuyente.setNombreRazonSocial(sb.toString());
        } else {
            if (request.getNombreRazonSocial() != null && !request.getNombreRazonSocial().trim().isEmpty()) {
                contribuyente.setNombreRazonSocial(request.getNombreRazonSocial().trim());
            }
            contribuyente.setNombreComercial(request.getNombreComercial() != null ? request.getNombreComercial().trim() : null);
            contribuyente.setRepresentanteLegalNombre(request.getRepresentanteLegalNombre());
            contribuyente.setRepresentanteLegalDocumento(request.getRepresentanteLegalDocumento());
        }

        contribuyente.setDomicilioFiscal(request.getDomicilioFiscal());
        contribuyente.setReferenciaDomicilio(request.getReferenciaDomicilio());
        contribuyente.setTelefono(request.getTelefono());
        contribuyente.setTelefonoAlternativo(request.getTelefonoAlternativo());
        contribuyente.setCorreo(request.getCorreo());
        if (request.getMedioContactoPreferido() != null) {
            contribuyente.setMedioContactoPreferido(request.getMedioContactoPreferido());
        }
        contribuyente.setObservaciones(request.getObservaciones());

        ContribuyenteTributario actualizado = contribuyenteRepository.save(contribuyente);
        log.info("Contribuyente actualizado por usuario '{}': ID={}", usuario, id);
        return mapToDetailResponse(actualizado);
    }

    @Override
    @Transactional
    public ContribuyenteDetailResponse cambiarEstado(UUID id, CambioEstadoContribuyenteRequest request, String usuario) {
        ContribuyenteTributario contribuyente = contribuyenteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el contribuyente con ID: " + id));

        if (request.getNuevoEstado() == contribuyente.getEstado()) {
            throw new BusinessException("El contribuyente ya se encuentra en el estado " + request.getNuevoEstado().getDescripcion());
        }

        EstadoContribuyente estadoAnterior = contribuyente.getEstado();
        contribuyente.setEstado(request.getNuevoEstado());
        contribuyente.setMotivoCambioEstado(request.getMotivo());
        contribuyente.setFechaCambioEstado(OffsetDateTime.now());

        HistorialEstadoContribuyente hist = HistorialEstadoContribuyente.builder()
                .contribuyente(contribuyente)
                .estadoAnterior(estadoAnterior)
                .nuevoEstado(request.getNuevoEstado())
                .motivo(request.getMotivo())
                .resolucionSustento(request.getResolucionSustento())
                .usuarioRegistro(usuario != null ? usuario : "SYSTEM")
                .fechaRegistro(OffsetDateTime.now())
                .build();
        historialRepository.save(hist);

        ContribuyenteTributario guardado = contribuyenteRepository.save(contribuyente);
        log.info("Estado de contribuyente {} cambiado de {} a {} por {}", id, estadoAnterior, request.getNuevoEstado(), usuario);
        return mapToDetailResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public ContribuyenteDetailResponse obtenerDetalle(UUID id) {
        ContribuyenteTributario contribuyente = contribuyenteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el contribuyente con ID: " + id));
        return mapToDetailResponse(contribuyente);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContribuyenteSummaryDto> listarConFiltros(ContribuyenteFilterDto filtro, Pageable pageable) {
        Specification<ContribuyenteTributario> spec = ContribuyenteSpecification.conFiltros(
                filtro != null ? filtro.getQuery() : null,
                filtro != null ? filtro.getTipo() : null,
                filtro != null ? filtro.getEstado() : null,
                filtro != null ? filtro.getFechaDesde() : null,
                filtro != null ? filtro.getFechaHasta() : null,
                filtro != null ? filtro.getOficinaId() : null);

        return contribuyenteRepository.findAll(spec, pageable).map(this::mapToSummaryDto);
    }

    @Override
    @Transactional(readOnly = true)
    public DuplicateCheckResponse verificarDuplicados(String tipoDocumento, String numeroDocumento, String nombreRazonSocial, UUID excludeId) {
        return contribuyenteValidator.verificarDuplicados(tipoDocumento, numeroDocumento, nombreRazonSocial, excludeId);
    }

    @Override
    @Transactional
    public RepresentanteResponse agregarRepresentante(UUID contribuyenteId, RepresentanteContribuyenteRequest request, String usuario) {
        ContribuyenteTributario contribuyente = contribuyenteRepository.findById(contribuyenteId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el contribuyente con ID: " + contribuyenteId));

        RepresentanteContribuyente rep = RepresentanteContribuyente.builder()
                .contribuyente(contribuyente)
                .tipoDocumento(request.getTipoDocumento().trim().toUpperCase())
                .numeroDocumento(request.getNumeroDocumento().trim())
                .nombresApellidos(request.getNombresApellidos().trim())
                .tipoRepresentacion(request.getTipoRepresentacion())
                .partidaRegistral(request.getPartidaRegistral())
                .asientoRegistral(request.getAsientoRegistral())
                .fechaInicio(request.getFechaInicio())
                .fechaFin(request.getFechaFin())
                .observaciones(request.getObservaciones())
                .estado(EstadoRepresentanteLegal.ACTIVO)
                .build();

        RepresentanteContribuyente guardado = representanteRepository.save(rep);
        log.info("Representante legal agregado para contribuyente {}: ID={}", contribuyenteId, guardado.getId());
        return mapRepresentanteToDto(guardado);
    }

    @Override
    @Transactional
    public void eliminarRepresentante(UUID contribuyenteId, UUID representanteId, String usuario) {
        RepresentanteContribuyente rep = representanteRepository.findById(representanteId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el representante con ID: " + representanteId));
        if (!rep.getContribuyente().getId().equals(contribuyenteId)) {
            throw new BusinessException("El representante no pertenece al contribuyente especificado.");
        }
        rep.setEstado(EstadoRepresentanteLegal.REVOCADO);
        representanteRepository.save(rep);
        log.info("Representante legal revocado ID={} por {}", representanteId, usuario);
    }

    @Override
    @Transactional
    public DomicilioResponse agregarDomicilio(UUID contribuyenteId, DomicilioContribuyenteRequest request, String usuario) {
        ContribuyenteTributario contribuyente = contribuyenteRepository.findById(contribuyenteId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el contribuyente con ID: " + contribuyenteId));

        if (request.isEsFiscal()) {
            domicilioRepository.findByContribuyenteIdAndEsFiscalTrue(contribuyenteId).ifPresent(domFiscal -> {
                domFiscal.setEsFiscal(false);
                domicilioRepository.save(domFiscal);
            });
        }

        DomicilioContribuyente dom = DomicilioContribuyente.builder()
                .contribuyente(contribuyente)
                .tipoDomicilio(request.getTipoDomicilio())
                .departamento(request.getDepartamento() != null ? request.getDepartamento() : "Ayacucho")
                .provincia(request.getProvincia() != null ? request.getProvincia() : "Huamanga")
                .distrito(request.getDistrito() != null ? request.getDistrito() : "Quinua")
                .ubigeo(request.getUbigeo() != null ? request.getUbigeo() : "050108")
                .tipoVia(request.getTipoVia())
                .nombreVia(request.getNombreVia())
                .numeroMunicipal(request.getNumeroMunicipal())
                .interior(request.getInterior())
                .manzanaLote(request.getManzanaLote())
                .comunidadCentroPoblado(request.getComunidadCentroPoblado())
                .referencia(request.getReferencia())
                .esFiscal(request.isEsFiscal())
                .esNotificacion(request.isEsNotificacion())
                .estado("ACTIVO")
                .fechaRegistro(LocalDate.now())
                .build();

        DomicilioContribuyente guardado = domicilioRepository.save(dom);
        if (request.isEsFiscal()) {
            contribuyente.setDomicilioFiscal(guardado.getDireccionCompleta());
            contribuyenteRepository.save(contribuyente);
        }
        return mapDomicilioToDto(guardado);
    }

    @Override
    @Transactional
    public void eliminarDomicilio(UUID contribuyenteId, UUID domicilioId, String usuario) {
        DomicilioContribuyente dom = domicilioRepository.findById(domicilioId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el domicilio con ID: " + domicilioId));
        if (!dom.getContribuyente().getId().equals(contribuyenteId)) {
            throw new BusinessException("El domicilio no pertenece al contribuyente especificado.");
        }
        dom.setEstado("HISTORICO");
        dom.setFechaFinVigencia(LocalDate.now());
        domicilioRepository.save(dom);
    }

    @Override
    @Transactional
    public ContactoResponse agregarContacto(UUID contribuyenteId, ContactoContribuyenteRequest request, String usuario) {
        ContribuyenteTributario contribuyente = contribuyenteRepository.findById(contribuyenteId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el contribuyente con ID: " + contribuyenteId));

        ContactoContribuyente cont = ContactoContribuyente.builder()
                .contribuyente(contribuyente)
                .tipoContacto(request.getTipoContacto())
                .valor(request.getValor().trim())
                .descripcion(request.getDescripcion())
                .esPrincipal(request.isEsPrincipal())
                .autorizadoNotificacion(request.isAutorizadoNotificacion())
                .estado("ACTIVO")
                .build();

        ContactoContribuyente guardado = contactoRepository.save(cont);
        return mapContactoToDto(guardado);
    }

    @Override
    @Transactional
    public void eliminarContacto(UUID contribuyenteId, UUID contactoId, String usuario) {
        ContactoContribuyente cont = contactoRepository.findById(contactoId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el contacto con ID: " + contactoId));
        if (!cont.getContribuyente().getId().equals(contribuyenteId)) {
            throw new BusinessException("El contacto no pertenece al contribuyente especificado.");
        }
        cont.setEstado("INACTIVO");
        contactoRepository.save(cont);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> stats = new HashMap<>();
        long total = contribuyenteRepository.count();
        long naturales = contribuyenteRepository.countByTipoContribuyente(TipoContribuyente.PERSONA_NATURAL);
        long juridicas = contribuyenteRepository.countByTipoContribuyente(TipoContribuyente.PERSONA_JURIDICA);
        long activos = contribuyenteRepository.countByEstado(EstadoContribuyente.ACTIVO);
        long inactivos = contribuyenteRepository.countByEstado(EstadoContribuyente.INACTIVO);
        long observados = contribuyenteRepository.countByEstado(EstadoContribuyente.OBSERVADO);

        stats.put("total", total);
        stats.put("naturales", naturales);
        stats.put("juridicas", juridicas);
        stats.put("activos", activos);
        stats.put("inactivos", inactivos);
        stats.put("observados", observados);
        return stats;
    }

    private ContribuyenteSummaryDto mapToSummaryDto(ContribuyenteTributario c) {
        return ContribuyenteSummaryDto.builder()
                .id(c.getId())
                .codigoContribuyente(c.getCodigoContribuyente())
                .tipoContribuyente(c.getTipoContribuyente() != null ? c.getTipoContribuyente().name() : "")
                .tipoContribuyenteDesc(c.getTipoContribuyente() != null ? c.getTipoContribuyente().getDescripcion() : "")
                .tipoDocumento(c.getTipoDocumento())
                .numeroDocumento(c.getNumeroDocumento())
                .numeroDocumentoEnmascarado(c.getNumeroDocumentoEnmascarado())
                .nombreRazonSocial(c.getNombreRazonSocial())
                .domicilioFiscal(c.getDomicilioFiscal())
                .telefono(c.getTelefono())
                .correo(c.getCorreo())
                .estado(c.getEstado() != null ? c.getEstado().name() : "ACTIVO")
                .estadoDesc(c.getEstado() != null ? c.getEstado().getDescripcion() : "Activo")
                .badgeClass(c.getEstado() != null ? c.getEstado().getBadgeClass() : "")
                .fechaRegistro(c.getFechaRegistro() != null ? c.getFechaRegistro().toString() : "")
                .build();
    }

    private ContribuyenteDetailResponse mapToDetailResponse(ContribuyenteTributario c) {
        List<RepresentanteResponse> reps = representanteRepository.findByContribuyenteIdOrderByCreatedAtDesc(c.getId())
                .stream().map(this::mapRepresentanteToDto).toList();

        List<DomicilioResponse> doms = domicilioRepository.findByContribuyenteIdOrderByCreatedAtDesc(c.getId())
                .stream().map(this::mapDomicilioToDto).toList();

        List<ContactoResponse> conts = contactoRepository.findByContribuyenteIdOrderByCreatedAtDesc(c.getId())
                .stream().map(this::mapContactoToDto).toList();

        List<HistorialEstadoResponse> hist = historialRepository.findByContribuyenteIdOrderByFechaRegistroDesc(c.getId())
                .stream().map(this::mapHistorialToDto).toList();

        return ContribuyenteDetailResponse.builder()
                .id(c.getId())
                .codigoContribuyente(c.getCodigoContribuyente())
                .personaId(c.getPersona() != null ? c.getPersona().getId() : null)
                .tipoContribuyente(c.getTipoContribuyente())
                .tipoContribuyenteDesc(c.getTipoContribuyente() != null ? c.getTipoContribuyente().getDescripcion() : "")
                .tipoDocumento(c.getTipoDocumento())
                .numeroDocumento(c.getNumeroDocumento())
                .numeroDocumentoEnmascarado(c.getNumeroDocumentoEnmascarado())
                .nombreRazonSocial(c.getNombreRazonSocial())
                .nombreComercial(c.getNombreComercial())
                .nombres(c.getNombres())
                .primerApellido(c.getPrimerApellido())
                .segundoApellido(c.getSegundoApellido())
                .representanteLegalNombre(c.getRepresentanteLegalNombre())
                .representanteLegalDocumento(c.getRepresentanteLegalDocumento())
                .domicilioFiscal(c.getDomicilioFiscal())
                .referenciaDomicilio(c.getReferenciaDomicilio())
                .telefono(c.getTelefono())
                .telefonoEnmascarado(c.getTelefonoEnmascarado())
                .telefonoAlternativo(c.getTelefonoAlternativo())
                .correo(c.getCorreo())
                .correoEnmascarado(c.getCorreoEnmascarado())
                .medioContactoPreferido(c.getMedioContactoPreferido())
                .medioContactoPreferidoDesc(c.getMedioContactoPreferido() != null ? c.getMedioContactoPreferido().getDescripcion() : "")
                .estado(c.getEstado())
                .estadoDesc(c.getEstado() != null ? c.getEstado().getDescripcion() : "")
                .badgeClass(c.getEstado() != null ? c.getEstado().getBadgeClass() : "")
                .estadoVerificacion(c.getEstadoVerificacion())
                .estadoVerificacionDesc(c.getEstadoVerificacion() != null ? c.getEstadoVerificacion().getDescripcion() : "")
                .estadoVerificacionBadgeClass(c.getEstadoVerificacion() != null ? c.getEstadoVerificacion().getBadgeClass() : "")
                .fechaRegistro(c.getFechaRegistro())
                .oficinaRegistroNombre(c.getOficinaRegistro() != null ? c.getOficinaRegistro().getNombre() : null)
                .observaciones(c.getObservaciones())
                .version(c.getVersion())
                .representantes(reps)
                .domicilios(doms)
                .contactos(conts)
                .historialEstados(hist)
                .build();
    }

    private RepresentanteResponse mapRepresentanteToDto(RepresentanteContribuyente r) {
        return RepresentanteResponse.builder()
                .id(r.getId())
                .tipoDocumento(r.getTipoDocumento())
                .numeroDocumento(r.getNumeroDocumento())
                .numeroDocumentoEnmascarado(r.getNumeroDocumentoEnmascarado())
                .nombresApellidos(r.getNombresApellidos())
                .tipoRepresentacion(r.getTipoRepresentacion())
                .tipoRepresentacionDesc(r.getTipoRepresentacion() != null ? r.getTipoRepresentacion().getDescripcion() : "")
                .partidaRegistral(r.getPartidaRegistral())
                .asientoRegistral(r.getAsientoRegistral())
                .fechaInicio(r.getFechaInicio())
                .fechaFin(r.getFechaFin())
                .estado(r.getEstado())
                .estadoDesc(r.getEstado() != null ? r.getEstado().getDescripcion() : "")
                .badgeClass(r.getEstado() != null ? r.getEstado().getBadgeClass() : "")
                .observaciones(r.getObservaciones())
                .build();
    }

    private DomicilioResponse mapDomicilioToDto(DomicilioContribuyente d) {
        return DomicilioResponse.builder()
                .id(d.getId())
                .tipoDomicilio(d.getTipoDomicilio())
                .tipoDomicilioDesc(d.getTipoDomicilio() != null ? d.getTipoDomicilio().getDescripcion() : "")
                .departamento(d.getDepartamento())
                .provincia(d.getProvincia())
                .distrito(d.getDistrito())
                .ubigeo(d.getUbigeo())
                .tipoVia(d.getTipoVia())
                .nombreVia(d.getNombreVia())
                .numeroMunicipal(d.getNumeroMunicipal())
                .interior(d.getInterior())
                .manzanaLote(d.getManzanaLote())
                .comunidadCentroPoblado(d.getComunidadCentroPoblado())
                .referencia(d.getReferencia())
                .esFiscal(d.isEsFiscal())
                .esNotificacion(d.isEsNotificacion())
                .estado(d.getEstado())
                .fechaRegistro(d.getFechaRegistro())
                .direccionCompleta(d.getDireccionCompleta())
                .build();
    }

    private ContactoResponse mapContactoToDto(ContactoContribuyente c) {
        return ContactoResponse.builder()
                .id(c.getId())
                .tipoContacto(c.getTipoContacto())
                .tipoContactoDesc(c.getTipoContacto() != null ? c.getTipoContacto().getDescripcion() : "")
                .valor(c.getValor())
                .valorEnmascarado(c.getValorEnmascarado())
                .descripcion(c.getDescripcion())
                .esPrincipal(c.isEsPrincipal())
                .autorizadoNotificacion(c.isAutorizadoNotificacion())
                .estado(c.getEstado())
                .build();
    }

    private HistorialEstadoResponse mapHistorialToDto(HistorialEstadoContribuyente h) {
        return HistorialEstadoResponse.builder()
                .id(h.getId())
                .estadoAnterior(h.getEstadoAnterior())
                .estadoAnteriorDesc(h.getEstadoAnterior() != null ? h.getEstadoAnterior().getDescripcion() : "")
                .nuevoEstado(h.getNuevoEstado())
                .nuevoEstadoDesc(h.getNuevoEstado() != null ? h.getNuevoEstado().getDescripcion() : "")
                .nuevoEstadoBadgeClass(h.getNuevoEstado() != null ? h.getNuevoEstado().getBadgeClass() : "")
                .motivo(h.getMotivo())
                .resolucionSustento(h.getResolucionSustento())
                .usuarioRegistro(h.getUsuarioRegistro())
                .fechaRegistro(h.getFechaRegistro())
                .build();
    }
}
