package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvSolicitudDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoFormularioVersion;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FormularioDinamicoServiceImpl implements FormularioDinamicoService {

    private final TipoTramiteRepository tipoTramiteRepository;
    private final FormularioTramiteRepository formularioRepository;
    private final FormularioVersionRepository versionRepository;
    private final FormularioCampoRepository campoRepository;
    private final RespuestaFormularioRepository respuestaRepository;
    private final RespuestaValorRepository respuestaValorRepository;
    private final RequisitoDocumentalRepository requisitoRepository;

    public FormularioDinamicoServiceImpl(TipoTramiteRepository tipoTramiteRepository,
                                        FormularioTramiteRepository formularioRepository,
                                        FormularioVersionRepository versionRepository,
                                        FormularioCampoRepository campoRepository,
                                        RespuestaFormularioRepository respuestaRepository,
                                        RespuestaValorRepository respuestaValorRepository,
                                        RequisitoDocumentalRepository requisitoRepository) {
        this.tipoTramiteRepository = tipoTramiteRepository;
        this.formularioRepository = formularioRepository;
        this.versionRepository = versionRepository;
        this.campoRepository = campoRepository;
        this.respuestaRepository = respuestaRepository;
        this.respuestaValorRepository = respuestaValorRepository;
        this.requisitoRepository = requisitoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public EsquemaTramiteResponse obtenerEsquemaTramite(Long tipoTramiteId) {
        TipoTramite tramite = tipoTramiteRepository.findById(tipoTramiteId)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de trámite no encontrado con ID: " + tipoTramiteId));

        List<RequisitoDocumental> requisitos = requisitoRepository.findByTipoTramiteIdAndEstadoOrderByOrdenAsc(tipoTramiteId, "ACTIVO");
        List<RequisitoDocumentalDto> reqDtos = requisitos.stream()
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

        FormularioVersionDto formDto = null;
        Optional<FormularioTramite> formOpt = formularioRepository.findByTipoTramiteIdAndActivoTrue(tipoTramiteId);
        if (formOpt.isPresent()) {
            FormularioTramite form = formOpt.get();
            Optional<FormularioVersion> versionOpt = versionRepository.findFirstByFormularioIdAndEstadoOrderByVersionDesc(form.getId(), EstadoFormularioVersion.PUBLICADO);
            if (versionOpt.isPresent()) {
                FormularioVersion version = versionOpt.get();
                List<FormularioCampo> campos = campoRepository.findByVersionIdWithOpciones(version.getId());

                List<FormularioCampoDto> campoDtos = campos.stream().map(c -> FormularioCampoDto.builder()
                        .id(c.getId())
                        .nombreCampo(c.getNombreCampo())
                        .etiqueta(c.getEtiqueta())
                        .tipoCampo(c.getTipoCampo().name())
                        .obligatorio(c.getObligatorio())
                        .longitudMaxima(c.getLongitudMaxima())
                        .valorDefecto(c.getValorDefecto())
                        .placeholder(c.getPlaceholder())
                        .mensajeAyuda(c.getMensajeAyuda())
                        .reglaValidacion(c.getReglaValidacion())
                        .orden(c.getOrden())
                        .opciones(c.getOpciones().stream().map(o -> FormularioOpcionDto.builder()
                                .valor(o.getValor())
                                .etiqueta(o.getEtiqueta())
                                .build()).collect(Collectors.toList()))
                        .build()).collect(Collectors.toList());

                formDto = FormularioVersionDto.builder()
                        .versionId(version.getId())
                        .version(version.getVersion())
                        .codigoFormulario(form.getCodigo())
                        .nombreFormulario(form.getNombre())
                        .campos(campoDtos)
                        .build();
            }
        }

        return EsquemaTramiteResponse.builder()
                .tipoTramiteId(tramite.getId())
                .codigo(tramite.getCodigo())
                .nombre(tramite.getNombre())
                .descripcion(tramite.getDescripcion())
                .oficinaResponsableNombre(tramite.getOficinaResponsable() != null ? tramite.getOficinaResponsable().getNombre() : null)
                .plazoDias(tramite.getPlazoDias())
                .unidadPlazo(tramite.getUnidadPlazo().name())
                .permitePresentacionVirtual(tramite.getPermitePresentacionVirtual())
                .requisitos(reqDtos)
                .formulario(formDto)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public FormularioVersion obtenerVersionActivaPorTramite(Long tipoTramiteId) {
        return formularioRepository.findByTipoTramiteIdAndActivoTrue(tipoTramiteId)
                .flatMap(form -> versionRepository.findFirstByFormularioIdAndEstadoOrderByVersionDesc(form.getId(), EstadoFormularioVersion.PUBLICADO))
                .orElse(null);
    }

    @Override
    public void validarRespuestas(FormularioVersion version, Map<String, String> valores, boolean esPresentacionFinal) {
        if (version == null) {
            return;
        }

        List<FormularioCampo> campos = campoRepository.findByVersionIdWithOpciones(version.getId());
        Map<String, String> mapaValores = valores != null ? valores : Collections.emptyMap();

        for (FormularioCampo campo : campos) {
            String val = mapaValores.get(campo.getNombreCampo());
            boolean vacio = val == null || val.trim().isEmpty();

            if (esPresentacionFinal && Boolean.TRUE.equals(campo.getObligatorio()) && vacio) {
                throw new BusinessException("El campo obligatorio '" + campo.getEtiqueta() + "' debe ser completado.");
            }

            if (!vacio) {
                final String valTrimmed = val.trim();
                if (campo.getLongitudMaxima() != null && valTrimmed.length() > campo.getLongitudMaxima()) {
                    throw new BusinessException("El campo '" + campo.getEtiqueta() + "' excede la longitud máxima permitida de "
                            + campo.getLongitudMaxima() + " caracteres.");
                }

                switch (campo.getTipoCampo()) {
                    case NUMERO:
                        try {
                            Double.parseDouble(valTrimmed);
                        } catch (NumberFormatException e) {
                            throw new BusinessException("El campo '" + campo.getEtiqueta() + "' debe contener un valor numérico válido.");
                        }
                        break;
                    case FECHA:
                        try {
                            LocalDate.parse(valTrimmed);
                        } catch (DateTimeParseException e) {
                            throw new BusinessException("El campo '" + campo.getEtiqueta() + "' debe tener formato de fecha válido (AAAA-MM-DD).");
                        }
                        break;
                    case SELECCION:
                        boolean opcionValida = campo.getOpciones().stream()
                                .anyMatch(o -> o.getValor().equalsIgnoreCase(valTrimmed));
                        if (!opcionValida) {
                            throw new BusinessException("La opción seleccionada para '" + campo.getEtiqueta() + "' no es válida.");
                        }
                        break;
                    case CHECKBOX:
                        if (!"true".equalsIgnoreCase(valTrimmed) && !"false".equalsIgnoreCase(valTrimmed) && !"1".equals(valTrimmed) && !"0".equals(valTrimmed)) {
                            throw new BusinessException("El campo de verificación '" + campo.getEtiqueta() + "' debe ser verdadero o falso.");
                        }
                        break;
                    default:
                        break;
                }

                if (campo.getReglaValidacion() != null && !campo.getReglaValidacion().isBlank()) {
                    try {
                        if (!Pattern.matches(campo.getReglaValidacion(), valTrimmed)) {
                            throw new BusinessException("El valor ingresado en '" + campo.getEtiqueta() + "' no cumple con el formato requerido.");
                        }
                    } catch (Exception ex) {
                        log.warn("Regla de validación no aplicable o inválida para campo {}: {}", campo.getNombreCampo(), ex.getMessage());
                    }
                }
            }
        }
    }

    @Override
    @Transactional
    public void guardarRespuestas(Expediente expediente, Map<String, String> valores, boolean esPresentacionFinal) {
        if (valores == null || valores.isEmpty()) {
            return;
        }

        FormularioVersion version = obtenerVersionActivaPorTramite(expediente.getTipoTramite().getId());
        if (version == null) {
            return;
        }

        validarRespuestas(version, valores, esPresentacionFinal);

        RespuestaFormulario respuesta = respuestaRepository.findByExpedienteId(expediente.getId())
                .orElseGet(() -> RespuestaFormulario.builder()
                        .expediente(expediente)
                        .version(version)
                        .build());

        respuesta = respuestaRepository.save(respuesta);

        List<FormularioCampo> campos = campoRepository.findByVersionIdWithOpciones(version.getId());
        List<RespuestaValor> existentes = respuestaValorRepository.findByRespuestaId(respuesta.getId());
        Map<Long, RespuestaValor> mapaExistentes = existentes.stream()
                .collect(Collectors.toMap(r -> r.getCampo().getId(), r -> r));

        for (FormularioCampo campo : campos) {
            String val = valores.get(campo.getNombreCampo());
            if (val != null) {
                RespuestaValor rv = mapaExistentes.get(campo.getId());
                if (rv == null) {
                    rv = RespuestaValor.builder()
                            .respuesta(respuesta)
                            .campo(campo)
                            .valorTexto(val.trim())
                            .build();
                } else {
                    rv.setValorTexto(val.trim());
                }
                respuestaValorRepository.save(rv);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, String> obtenerMapaRespuestas(Long expedienteId) {
        Map<String, String> resultado = new LinkedHashMap<>();
        Optional<RespuestaFormulario> opt = respuestaRepository.findByExpedienteIdWithValores(expedienteId);
        if (opt.isPresent() && opt.get().getValores() != null) {
            for (RespuestaValor v : opt.get().getValores()) {
                if (v.getCampo() != null) {
                    resultado.put(v.getCampo().getEtiqueta(), v.getValorTexto() != null ? v.getValorTexto() : "");
                }
            }
        }
        return resultado;
    }
}
