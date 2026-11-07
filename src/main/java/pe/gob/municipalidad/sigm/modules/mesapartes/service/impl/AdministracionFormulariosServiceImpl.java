package pe.gob.municipalidad.sigm.modules.mesapartes.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.ConfiguracionMpvDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvSolicitudDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoFormularioVersion;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoCambioConfiguracion;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoCampoFormulario;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoEntidadConfiguracion;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.AdministracionFormulariosService;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.ConfiguracionHistorialService;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AdministracionFormulariosServiceImpl implements AdministracionFormulariosService {

    private static final Logger log = LoggerFactory.getLogger(AdministracionFormulariosServiceImpl.class);

    private static final Pattern PATRON_XSS = Pattern.compile(
            "(<script|javascript:|onerror=|onload=|<iframe|<object|<embed|<svg|<link|onclick=)",
            Pattern.CASE_INSENSITIVE
    );

    private final FormularioTramiteRepository formularioRepository;
    private final FormularioVersionRepository versionRepository;
    private final FormularioCampoRepository campoRepository;
    private final TipoTramiteRepository tipoTramiteRepository;
    private final RequisitoDocumentalRepository requisitoRepository;
    private final ConfiguracionHistorialService historialService;

    public AdministracionFormulariosServiceImpl(
            FormularioTramiteRepository formularioRepository,
            FormularioVersionRepository versionRepository,
            FormularioCampoRepository campoRepository,
            TipoTramiteRepository tipoTramiteRepository,
            RequisitoDocumentalRepository requisitoRepository,
            ConfiguracionHistorialService historialService
    ) {
        this.formularioRepository = formularioRepository;
        this.versionRepository = versionRepository;
        this.campoRepository = campoRepository;
        this.tipoTramiteRepository = tipoTramiteRepository;
        this.requisitoRepository = requisitoRepository;
        this.historialService = historialService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FormularioAdminResponse> listarFormularios() {
        return formularioRepository.findAll()
                .stream()
                .map(this::mapearAdminResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FormularioAdminResponse obtenerPorId(Long id) {
        FormularioTramite f = formularioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Formulario no encontrado con ID: " + id));
        return mapearAdminResponse(f);
    }

    @Override
    @Transactional(readOnly = true)
    public FormularioAdminResponse obtenerPorTramiteId(Long tipoTramiteId) {
        FormularioTramite f = formularioRepository.findByTipoTramiteIdAndActivoTrue(tipoTramiteId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró formulario activo para el trámite ID: " + tipoTramiteId));
        return mapearAdminResponse(f);
    }

    @Override
    @Transactional
    public FormularioAdminResponse guardarFormulario(GuardarFormularioRequest request, String username, String ipOrigen) {
        TipoTramite tramite = tipoTramiteRepository.findById(request.getTipoTramiteId())
                .orElseThrow(() -> new ResourceNotFoundException("Trámite no encontrado con ID: " + request.getTipoTramiteId()));

        // Sanitización y Anti-XSS en cabecera
        validarTextoSeguro(request.getNombre(), "nombre del formulario");
        validarTextoSeguro(request.getDescripcion(), "descripción del formulario");

        // Buscar formulario existente o crear nuevo
        FormularioTramite formulario = formularioRepository.findByTipoTramiteIdAndActivoTrue(tramite.getId())
                .orElse(null);

        if (formulario == null) {
            formulario = FormularioTramite.builder()
                    .tipoTramite(tramite)
                    .codigo(request.getCodigo().trim().toUpperCase())
                    .nombre(request.getNombre().trim())
                    .descripcion(request.getDescripcion())
                    .activo(true)
                    .createdBy(username)
                    .build();
            formulario = formularioRepository.save(formulario);
        } else {
            formulario.setNombre(request.getNombre().trim());
            formulario.setDescripcion(request.getDescripcion());
            formulario.setUpdatedBy(username);
            formulario = formularioRepository.save(formulario);
        }

        // Determinar si creamos nueva versión o editamos borrador
        Optional<FormularioVersion> ultimaOpt = versionRepository.findFirstByFormularioIdOrderByVersionDesc(formulario.getId());
        FormularioVersion versionTrabajo;
        int numVersion = 1;

        if (ultimaOpt.isPresent()) {
            FormularioVersion ultima = ultimaOpt.get();
            if (ultima.getEstado() == EstadoFormularioVersion.PUBLICADO) {
                // Generar nueva versión en borrador inmutable
                numVersion = ultima.getVersion() + 1;
                versionTrabajo = FormularioVersion.builder()
                        .formulario(formulario)
                        .version(numVersion)
                        .estado(EstadoFormularioVersion.BORRADOR)
                        .createdBy(username)
                        .build();
                versionTrabajo = versionRepository.save(versionTrabajo);
            } else {
                versionTrabajo = ultima;
                numVersion = ultima.getVersion();
                // Limpiar campos de la versión borrador existente para reemplazarlos con la nueva configuración
                versionTrabajo.getCampos().clear();
            }
        } else {
            versionTrabajo = FormularioVersion.builder()
                    .formulario(formulario)
                    .version(1)
                    .estado(EstadoFormularioVersion.BORRADOR)
                    .createdBy(username)
                    .build();
            versionTrabajo = versionRepository.save(versionTrabajo);
        }

        // Agregar campos configurados
        if (request.getCampos() != null && !request.getCampos().isEmpty()) {
            Set<String> nombresUnicos = new HashSet<>();
            int idxOrden = 1;

            for (GuardarCampoRequest c : request.getCampos()) {
                validarTextoSeguro(c.getEtiqueta(), "etiqueta del campo");
                validarTextoSeguro(c.getPlaceholder(), "placeholder del campo");
                validarTextoSeguro(c.getTextoAyuda(), "texto de ayuda del campo");
                validarTextoSeguro(c.getMensajeValidacion(), "mensaje de validación");

                String clave = c.getNombreCampo().trim().toLowerCase();
                if (!nombresUnicos.add(clave)) {
                    throw new IllegalArgumentException("El nombre de campo '" + clave + "' está duplicado en el formulario.");
                }

                FormularioCampo campo = FormularioCampo.builder()
                        .version(versionTrabajo)
                        .nombreCampo(clave)
                        .etiqueta(c.getEtiqueta().trim())
                        .tipoCampo(c.getTipoCampo() != null ? c.getTipoCampo() : TipoCampoFormulario.TEXTO)
                        .obligatorio(Boolean.TRUE.equals(c.getObligatorio()))
                        .orden(c.getOrden() != null ? c.getOrden() : idxOrden++)
                        .placeholder(c.getPlaceholder())
                        .mensajeAyuda(c.getTextoAyuda())
                        .longitudMaxima(c.getLongitudMaxima())
                        .reglaValidacion(c.getExpresionRegular())
                        .build();

                if (c.getOpciones() != null && !c.getOpciones().isEmpty()) {
                    int opcOrden = 1;
                    for (GuardarOpcionRequest o : c.getOpciones()) {
                        validarTextoSeguro(o.getEtiqueta(), "etiqueta de opción");
                        validarTextoSeguro(o.getValor(), "valor de opción");

                        FormularioCampoOpcion opcion = FormularioCampoOpcion.builder()
                                .campo(campo)
                                .valor(o.getValor().trim())
                                .etiqueta(o.getEtiqueta().trim())
                                .orden(o.getOrden() != null ? o.getOrden() : opcOrden++)
                                .build();
                        campo.getOpciones().add(opcion);
                    }
                }

                versionTrabajo.getCampos().add(campo);
            }

            versionRepository.save(versionTrabajo);
        }

        historialService.registrarCambio(
                TipoEntidadConfiguracion.FORMULARIO,
                formulario.getId(),
                formulario.getCodigo(),
                TipoCambioConfiguracion.MODIFICACION,
                numVersion,
                numVersion,
                "Configuración visual de campos en formulario (v" + numVersion + ")",
                null,
                null,
                username,
                ipOrigen
        );

        return mapearAdminResponse(formulario);
    }

    @Override
    @Transactional
    public FormularioAdminResponse publicarVersion(Long formularioId, String username, String ipOrigen) {
        FormularioTramite formulario = formularioRepository.findById(formularioId)
                .orElseThrow(() -> new ResourceNotFoundException("Formulario no encontrado con ID: " + formularioId));

        FormularioVersion version = versionRepository.findFirstByFormularioIdOrderByVersionDesc(formularioId)
                .orElseThrow(() -> new ResourceNotFoundException("No existen versiones para este formulario."));

        if (version.getCampos().isEmpty()) {
            throw new IllegalStateException("No se puede publicar un formulario sin campos configurados.");
        }

        int versionAnt = version.getVersion();
        version.setEstado(EstadoFormularioVersion.PUBLICADO);
        version.setFechaPublicacion(OffsetDateTime.now());
        versionRepository.save(version);

        formulario.setActivo(true);
        formulario.setUpdatedBy(username);
        formularioRepository.save(formulario);

        historialService.registrarCambio(
                TipoEntidadConfiguracion.FORMULARIO,
                formulario.getId(),
                formulario.getCodigo(),
                TipoCambioConfiguracion.PUBLICACION,
                versionAnt,
                versionAnt,
                "Publicación oficial de versión " + versionAnt + " de formulario",
                null,
                null,
                username,
                ipOrigen
        );

        return mapearAdminResponse(formulario);
    }

    @Override
    @Transactional(readOnly = true)
    public EsquemaTramiteResponse generarVistaPrevia(Long formularioId) {
        FormularioTramite formulario = formularioRepository.findById(formularioId)
                .orElseThrow(() -> new ResourceNotFoundException("Formulario no encontrado con ID: " + formularioId));

        FormularioVersion version = versionRepository.findFirstByFormularioIdOrderByVersionDesc(formularioId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontraron campos para previsualizar."));

        TipoTramite tramite = formulario.getTipoTramite();

        List<RequisitoDocumentalDto> reqDtos = requisitoRepository.findByTipoTramiteIdOrderByOrdenAsc(tramite.getId())
                .stream()
                .map(r -> RequisitoDocumentalDto.builder()
                        .id(r.getId())
                        .codigo(r.getCodigo())
                        .nombre(r.getNombre())
                        .descripcion(r.getDescripcion())
                        .obligatorio(r.getObligatorio())
                        .formatosPermitidos(r.getFormatosPermitidos())
                        .tamanoMaximoMb(r.getTamanoMaximoMb())
                        .orden(r.getOrden())
                        .build())
                .collect(Collectors.toList());

        List<FormularioCampoDto> campoDtos = version.getCampos().stream()
                .map(c -> FormularioCampoDto.builder()
                        .id(c.getId())
                        .nombreCampo(c.getNombreCampo())
                        .etiqueta(c.getEtiqueta())
                        .tipoCampo(c.getTipoCampo().name())
                        .obligatorio(c.getObligatorio())
                        .longitudMaxima(c.getLongitudMaxima())
                        .placeholder(c.getPlaceholder())
                        .mensajeAyuda(c.getMensajeAyuda())
                        .reglaValidacion(c.getReglaValidacion())
                        .orden(c.getOrden())
                        .opciones(c.getOpciones().stream()
                                .map(o -> FormularioOpcionDto.builder()
                                        .valor(o.getValor())
                                        .etiqueta(o.getEtiqueta())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());

        FormularioVersionDto fDto = FormularioVersionDto.builder()
                .versionId(version.getId())
                .version(version.getVersion())
                .codigoFormulario(formulario.getCodigo())
                .nombreFormulario("[PREVISUALIZACIÓN ADMINISTRATIVA] " + formulario.getNombre())
                .campos(campoDtos)
                .build();

        return EsquemaTramiteResponse.builder()
                .tipoTramiteId(tramite.getId())
                .codigo(tramite.getCodigo())
                .nombre("[PREVISUALIZACIÓN] " + tramite.getNombre())
                .descripcion(tramite.getDescripcion())
                .oficinaResponsableNombre(tramite.getOficinaResponsable() != null ? tramite.getOficinaResponsable().getNombre() : "Oficina Institucional")
                .plazoDias(tramite.getPlazoDias())
                .unidadPlazo(tramite.getUnidadPlazo().name())
                .permitePresentacionVirtual(tramite.getPermitePresentacionVirtual())
                .requisitos(reqDtos)
                .formulario(fDto)
                .build();
    }

    private void validarTextoSeguro(String texto, String nombreCampo) {
        if (texto != null && PATRON_XSS.matcher(texto).find()) {
            throw new IllegalArgumentException("El contenido de " + nombreCampo + " contiene expresiones no permitidas por razones de seguridad.");
        }
    }

    private FormularioAdminResponse mapearAdminResponse(FormularioTramite f) {
        Optional<FormularioVersion> vOpt = versionRepository.findFirstByFormularioIdOrderByVersionDesc(f.getId());

        List<CampoConfigResponse> camposList = Collections.emptyList();
        Integer verNum = 1;
        EstadoFormularioVersion estadoV = EstadoFormularioVersion.BORRADOR;
        int totalCampos = 0;

        if (vOpt.isPresent()) {
            FormularioVersion v = vOpt.get();
            verNum = v.getVersion();
            estadoV = v.getEstado();
            totalCampos = v.getCampos().size();
            camposList = v.getCampos().stream().map(c -> CampoConfigResponse.builder()
                    .id(c.getId())
                    .nombreCampo(c.getNombreCampo())
                    .etiqueta(c.getEtiqueta())
                    .tipoCampo(c.getTipoCampo())
                    .obligatorio(c.getObligatorio())
                    .orden(c.getOrden())
                    .placeholder(c.getPlaceholder())
                    .textoAyuda(c.getMensajeAyuda())
                    .longitudMaxima(c.getLongitudMaxima())
                    .expresionRegular(c.getReglaValidacion())
                    .opciones(c.getOpciones().stream().map(o -> OpcionCampoResponse.builder()
                            .id(o.getId())
                            .valor(o.getValor())
                            .etiqueta(o.getEtiqueta())
                            .orden(o.getOrden())
                            .build()).collect(Collectors.toList()))
                    .build()).collect(Collectors.toList());
        }

        return FormularioAdminResponse.builder()
                .id(f.getId())
                .tipoTramiteId(f.getTipoTramite().getId())
                .tipoTramiteNombre(f.getTipoTramite().getNombre())
                .codigo(f.getCodigo())
                .nombre(f.getNombre())
                .descripcion(f.getDescripcion())
                .activo(f.getActivo())
                .versionActual(verNum)
                .estadoVersion(estadoV)
                .totalCampos(totalCampos)
                .updatedAt(f.getUpdatedAt())
                .campos(camposList)
                .build();
    }
}
