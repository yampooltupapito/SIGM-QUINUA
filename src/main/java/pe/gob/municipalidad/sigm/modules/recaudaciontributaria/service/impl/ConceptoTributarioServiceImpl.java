package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.ConceptoTributarioService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.specification.ConceptoSpecification;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.validation.ConceptoValidator;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.SectorPatrullaje;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.SectorPatrullajeRepository;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConceptoTributarioServiceImpl implements ConceptoTributarioService {

    private final ConceptoTributarioRepository conceptoRepository;
    private final PeriodoTributarioRepository periodoRepository;
    private final NormaTributariaConfiguradaRepository normaRepository;
    private final ConfiguracionConceptoTributarioRepository configuracionRepository;
    private final ParametroTributarioRepository parametroRepository;
    private final ReglaTributariaConfiguradaRepository reglaRepository;
    private final CalendarioTributarioRepository calendarioRepository;
    private final HistorialConfiguracionTributariaRepository historialRepository;
    private final SectorPatrullajeRepository sectorRepository;
    private final ConceptoValidator conceptoValidator;

    @Override
    @Transactional
    public ConceptoDetailResponse registrarConcepto(ConceptoCreateRequest request, String usuario) {
        log.info("Registrando nuevo concepto tributario: codigo={}, nombre={}", request.getCodigo(), request.getNombre());
        conceptoValidator.validarCreacionConcepto(request);

        NormaTributariaConfigurada norma = null;
        if (request.getNormaLegalId() != null) {
            norma = normaRepository.findById(request.getNormaLegalId()).orElse(null);
        }

        ConceptoTributario concepto = ConceptoTributario.builder()
                .codigo(request.getCodigo().trim().toUpperCase())
                .nombre(request.getNombre().trim())
                .descripcion(request.getDescripcion())
                .tipoConcepto(request.getTipoConcepto())
                .naturalezaTributaria(request.getNaturalezaTributaria() != null ? request.getNaturalezaTributaria() : NaturalezaTributaria.IMPUESTO)
                .periodicidad(request.getPeriodicidad() != null ? request.getPeriodicidad() : PeriodicidadConcepto.ANUAL)
                .aplicaAPredio(request.getAplicaAPredio() != null ? request.getAplicaAPredio() : true)
                .cuentaPresupuestal(request.getCuentaPresupuestal())
                .estado(EstadoConceptoTributario.ACTIVO)
                .fechaInicioVigencia(request.getFechaInicioVigencia())
                .fechaFinVigencia(request.getFechaFinVigencia())
                .normaLegal(norma)
                .oficinaResponsableId(request.getOficinaResponsableId())
                .build();

        ConceptoTributario guardado = conceptoRepository.save(concepto);
        log.info("Concepto tributario creado con ID: {}", guardado.getId());
        return mapToDetailResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public ConceptoDetailResponse obtenerConceptoPorId(UUID id) {
        ConceptoTributario concepto = conceptoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el concepto tributario con ID: " + id));
        return mapToDetailResponse(concepto);
    }

    @Override
    @Transactional(readOnly = true)
    public ConceptoDetailResponse obtenerConceptoPorCodigo(String codigo) {
        ConceptoTributario concepto = conceptoRepository.findByCodigoIgnoreCase(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el concepto tributario con código: " + codigo));
        return mapToDetailResponse(concepto);
    }

    @Override
    @Transactional
    public ConceptoDetailResponse actualizarConcepto(UUID id, ConceptoUpdateRequest request, String usuario) {
        log.info("Actualizando concepto tributario ID: {}", id);
        ConceptoTributario concepto = conceptoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el concepto tributario con ID: " + id));

        conceptoValidator.validarActualizacionConcepto(id, request);

        NormaTributariaConfigurada norma = null;
        if (request.getNormaLegalId() != null) {
            norma = normaRepository.findById(request.getNormaLegalId()).orElse(null);
        }

        concepto.setNombre(request.getNombre().trim());
        concepto.setDescripcion(request.getDescripcion());
        concepto.setTipoConcepto(request.getTipoConcepto());
        if (request.getNaturalezaTributaria() != null) {
            concepto.setNaturalezaTributaria(request.getNaturalezaTributaria());
        }
        if (request.getPeriodicidad() != null) {
            concepto.setPeriodicidad(request.getPeriodicidad());
        }
        if (request.getAplicaAPredio() != null) {
            concepto.setAplicaAPredio(request.getAplicaAPredio());
        }
        concepto.setCuentaPresupuestal(request.getCuentaPresupuestal());
        concepto.setFechaInicioVigencia(request.getFechaInicioVigencia());
        concepto.setFechaFinVigencia(request.getFechaFinVigencia());
        concepto.setNormaLegal(norma);
        if (request.getOficinaResponsableId() != null) {
            concepto.setOficinaResponsableId(request.getOficinaResponsableId());
        }
        if (request.getEstado() != null) {
            concepto.setEstado(request.getEstado());
        }

        ConceptoTributario actualizado = conceptoRepository.save(concepto);
        return mapToDetailResponse(actualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConceptoSummaryDto> listarConceptos(ConceptoFilterDto filtro, Pageable pageable) {
        return conceptoRepository.findAll(ConceptoSpecification.conFiltro(filtro), pageable)
                .map(this::mapToSummaryDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConceptoSummaryDto> listarTodosConceptos(ConceptoFilterDto filtro) {
        return conceptoRepository.findAll(ConceptoSpecification.conFiltro(filtro))
                .stream()
                .map(this::mapToSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ConfiguracionDetailResponse crearConfiguracion(ConfiguracionCreateRequest request, String usuario) {
        log.info("Creando configuración para concepto ID: {}, periodo ID: {}", request.getConceptoId(), request.getPeriodoId());
        conceptoValidator.validarCreacionConfiguracion(request);

        ConceptoTributario concepto = conceptoRepository.findById(request.getConceptoId())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el concepto tributario con ID: " + request.getConceptoId()));

        PeriodoTributario periodo = periodoRepository.findById(request.getPeriodoId())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el período tributario con ID: " + request.getPeriodoId()));

        NormaTributariaConfigurada norma = null;
        if (request.getNormaId() != null) {
            norma = normaRepository.findById(request.getNormaId()).orElse(null);
        }

        SectorPatrullaje sector = null;
        if (request.getSectorId() != null) {
            sector = sectorRepository.findById(request.getSectorId()).orElse(null);
        }

        // Determinar siguiente número de versión para el concepto y período
        int siguienteVersion = configuracionRepository.findFirstByConceptoIdAndPeriodoIdOrderByNumeroVersionDesc(concepto.getId(), periodo.getId())
                .map(c -> c.getNumeroVersion() + 1)
                .orElse(1);

        ConfiguracionConceptoTributario conf = ConfiguracionConceptoTributario.builder()
                .concepto(concepto)
                .periodo(periodo)
                .norma(norma)
                .numeroVersion(siguienteVersion)
                .estado(EstadoConfiguracionConcepto.BORRADOR)
                .descripcionMotivo(request.getDescripcionMotivo())
                .fechaInicioVigencia(request.getFechaInicioVigencia())
                .fechaFinVigencia(request.getFechaFinVigencia())
                .sector(sector)
                .zona(request.getZona())
                .oficinaResponsableId(request.getOficinaResponsableId())
                .usuarioCreador(usuario != null ? usuario : "SISTEMA")
                .build();

        ConfiguracionConceptoTributario guardada = configuracionRepository.save(conf);

        // Registrar auditoría inicial
        HistorialConfiguracionTributaria hist = HistorialConfiguracionTributaria.builder()
                .configuracion(guardada)
                .accion(AccionHistorialConfiguracion.CREACION)
                .estadoAnterior(null)
                .nuevoEstado(EstadoConfiguracionConcepto.BORRADOR)
                .motivo("Creación inicial de borrador de configuración tributaria versión " + siguienteVersion)
                .usuarioRegistro(usuario != null ? usuario : "SISTEMA")
                .fechaRegistro(OffsetDateTime.now())
                .build();
        historialRepository.save(hist);

        log.info("Configuración creada exitosamente con ID: {}, versión: {}", guardada.getId(), siguienteVersion);
        return mapConfiguracionToDetail(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public ConfiguracionDetailResponse obtenerConfiguracionPorId(UUID configuracionId) {
        ConfiguracionConceptoTributario conf = configuracionRepository.findById(configuracionId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la configuración con ID: " + configuracionId));
        return mapConfiguracionToDetail(conf);
    }

    @Override
    @Transactional
    public ConfiguracionDetailResponse agregarParametro(UUID configuracionId, ParametroCreateRequest request, String usuario) {
        ConfiguracionConceptoTributario conf = configuracionRepository.findById(configuracionId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la configuración con ID: " + configuracionId));

        if (!conf.esEditable()) {
            throw new BusinessException("No se pueden agregar o modificar parámetros en una configuración en estado " + conf.getEstado() + ". Debe generar una nueva versión en borrador.");
        }

        conceptoValidator.validarParametro(request);

        if (parametroRepository.existsByConfiguracionIdAndCodigo(configuracionId, request.getCodigo().trim())) {
            throw new BusinessException("Ya existe un parámetro con el código '" + request.getCodigo().trim() + "' en esta versión.");
        }

        ParametroTributario param = ParametroTributario.builder()
                .configuracion(conf)
                .codigo(request.getCodigo().trim().toUpperCase())
                .nombre(request.getNombre().trim())
                .descripcion(request.getDescripcion())
                .tipoDato(request.getTipoDato())
                .valorDecimal(request.getValorDecimal())
                .valorTexto(request.getValorTexto())
                .valorBooleano(request.getValorBooleano())
                .valorFecha(request.getValorFecha())
                .unidad(request.getUnidad())
                .origenNormativo(request.getOrigenNormativo())
                .esObligatorio(request.getEsObligatorio() != null ? request.getEsObligatorio() : true)
                .ordenVisual(request.getOrdenVisual() != null ? request.getOrdenVisual() : conf.getParametros().size() + 1)
                .estado("ACTIVO")
                .build();

        parametroRepository.save(param);
        conf.getParametros().add(param);

        HistorialConfiguracionTributaria hist = HistorialConfiguracionTributaria.builder()
                .configuracion(conf)
                .accion(AccionHistorialConfiguracion.MODIFICACION_BORRADOR)
                .estadoAnterior(conf.getEstado())
                .nuevoEstado(conf.getEstado())
                .motivo("Se agregó el parámetro: " + param.getCodigo() + " (" + param.getNombre() + ")")
                .usuarioRegistro(usuario != null ? usuario : "SISTEMA")
                .fechaRegistro(OffsetDateTime.now())
                .build();
        historialRepository.save(hist);

        return mapConfiguracionToDetail(conf);
    }

    @Override
    @Transactional
    public ConfiguracionDetailResponse eliminarParametro(UUID configuracionId, UUID parametroId, String usuario) {
        ConfiguracionConceptoTributario conf = configuracionRepository.findById(configuracionId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la configuración con ID: " + configuracionId));

        if (!conf.esEditable()) {
            throw new BusinessException("No se pueden eliminar parámetros de una configuración en estado " + conf.getEstado() + ".");
        }

        ParametroTributario param = parametroRepository.findById(parametroId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el parámetro con ID: " + parametroId));

        String cod = param.getCodigo();
        parametroRepository.delete(param);
        conf.getParametros().removeIf(p -> p.getId().equals(parametroId));

        HistorialConfiguracionTributaria hist = HistorialConfiguracionTributaria.builder()
                .configuracion(conf)
                .accion(AccionHistorialConfiguracion.MODIFICACION_BORRADOR)
                .estadoAnterior(conf.getEstado())
                .nuevoEstado(conf.getEstado())
                .motivo("Se eliminó el parámetro con código: " + cod)
                .usuarioRegistro(usuario != null ? usuario : "SISTEMA")
                .fechaRegistro(OffsetDateTime.now())
                .build();
        historialRepository.save(hist);

        return mapConfiguracionToDetail(conf);
    }

    @Override
    @Transactional
    public ConfiguracionDetailResponse enviarARevision(UUID configuracionId, String motivo, String usuario) {
        ConfiguracionConceptoTributario conf = configuracionRepository.findById(configuracionId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la configuración con ID: " + configuracionId));

        conceptoValidator.validarParaRevision(conf);

        EstadoConfiguracionConcepto anterior = conf.getEstado();
        conf.setEstado(EstadoConfiguracionConcepto.EN_REVISION);

        HistorialConfiguracionTributaria hist = HistorialConfiguracionTributaria.builder()
                .configuracion(conf)
                .accion(AccionHistorialConfiguracion.ENVIO_REVISION)
                .estadoAnterior(anterior)
                .nuevoEstado(EstadoConfiguracionConcepto.EN_REVISION)
                .motivo(motivo != null && !motivo.isBlank() ? motivo : "Envío de versión para revisión técnica y legal")
                .usuarioRegistro(usuario != null ? usuario : "SISTEMA")
                .fechaRegistro(OffsetDateTime.now())
                .build();
        historialRepository.save(hist);

        ConfiguracionConceptoTributario guardada = configuracionRepository.save(conf);
        return mapConfiguracionToDetail(guardada);
    }

    @Override
    @Transactional
    public ConfiguracionDetailResponse aprobarConfiguracion(UUID configuracionId, String resolucion, String motivo, String usuario) {
        ConfiguracionConceptoTributario conf = configuracionRepository.findById(configuracionId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la configuración con ID: " + configuracionId));

        conceptoValidator.validarParaAprobacion(conf, resolucion);

        EstadoConfiguracionConcepto anterior = conf.getEstado();
        conf.setEstado(EstadoConfiguracionConcepto.APROBADO);
        conf.setResolucionAprobacion(resolucion.trim());
        conf.setUsuarioAprobador(usuario != null ? usuario : "SISTEMA");
        conf.setFechaAprobacion(OffsetDateTime.now());

        HistorialConfiguracionTributaria hist = HistorialConfiguracionTributaria.builder()
                .configuracion(conf)
                .accion(AccionHistorialConfiguracion.APROBACION)
                .estadoAnterior(anterior)
                .nuevoEstado(EstadoConfiguracionConcepto.APROBADO)
                .resolucionSustento(resolucion.trim())
                .motivo(motivo != null && !motivo.isBlank() ? motivo : "Aprobación formal según sustento legal: " + resolucion.trim())
                .usuarioRegistro(usuario != null ? usuario : "SISTEMA")
                .fechaRegistro(OffsetDateTime.now())
                .build();
        historialRepository.save(hist);

        ConfiguracionConceptoTributario guardada = configuracionRepository.save(conf);
        return mapConfiguracionToDetail(guardada);
    }

    @Override
    @Transactional
    public ConfiguracionDetailResponse activarConfiguracion(UUID configuracionId, String motivo, String usuario) {
        ConfiguracionConceptoTributario conf = configuracionRepository.findById(configuracionId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la configuración con ID: " + configuracionId));

        conceptoValidator.validarParaActivacion(conf);

        // Desactivar versiones activas anteriores en el mismo ámbito si existieran
        List<ConfiguracionConceptoTributario> activasAnteriores = configuracionRepository.findByConceptoIdAndPeriodoIdAndEstado(
                conf.getConcepto().getId(), conf.getPeriodo().getId(), EstadoConfiguracionConcepto.ACTIVO);

        for (ConfiguracionConceptoTributario prev : activasAnteriores) {
            if (!prev.getId().equals(conf.getId())) {
                prev.setEstado(EstadoConfiguracionConcepto.INACTIVO);
                prev.setMotivoDesactivacion("Cese por activación de nueva versión " + conf.getNumeroVersion());
                configuracionRepository.save(prev);

                HistorialConfiguracionTributaria histPrev = HistorialConfiguracionTributaria.builder()
                        .configuracion(prev)
                        .accion(AccionHistorialConfiguracion.DESACTIVACION)
                        .estadoAnterior(EstadoConfiguracionConcepto.ACTIVO)
                        .nuevoEstado(EstadoConfiguracionConcepto.INACTIVO)
                        .motivo("Desactivada por reemplazo de versión activa v" + conf.getNumeroVersion())
                        .usuarioRegistro(usuario != null ? usuario : "SISTEMA")
                        .fechaRegistro(OffsetDateTime.now())
                        .build();
                historialRepository.save(histPrev);
            }
        }

        EstadoConfiguracionConcepto anterior = conf.getEstado();
        conf.setEstado(EstadoConfiguracionConcepto.ACTIVO);
        conf.setUsuarioActivador(usuario != null ? usuario : "SISTEMA");
        conf.setFechaActivacion(OffsetDateTime.now());

        HistorialConfiguracionTributaria hist = HistorialConfiguracionTributaria.builder()
                .configuracion(conf)
                .accion(AccionHistorialConfiguracion.ACTIVACION)
                .estadoAnterior(anterior)
                .nuevoEstado(EstadoConfiguracionConcepto.ACTIVO)
                .motivo(motivo != null && !motivo.isBlank() ? motivo : "Activación y puesta en vigencia para determinación tributaria")
                .usuarioRegistro(usuario != null ? usuario : "SISTEMA")
                .fechaRegistro(OffsetDateTime.now())
                .build();
        historialRepository.save(hist);

        ConfiguracionConceptoTributario guardada = configuracionRepository.save(conf);
        return mapConfiguracionToDetail(guardada);
    }

    @Override
    @Transactional
    public ConfiguracionDetailResponse desactivarConfiguracion(UUID configuracionId, String motivo, String usuario) {
        ConfiguracionConceptoTributario conf = configuracionRepository.findById(configuracionId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la configuración con ID: " + configuracionId));

        if (motivo == null || motivo.trim().isEmpty()) {
            throw new BusinessException("Debe ingresar un motivo justificado para desactivar la configuración.");
        }

        EstadoConfiguracionConcepto anterior = conf.getEstado();
        conf.setEstado(EstadoConfiguracionConcepto.INACTIVO);
        conf.setMotivoDesactivacion(motivo.trim());

        HistorialConfiguracionTributaria hist = HistorialConfiguracionTributaria.builder()
                .configuracion(conf)
                .accion(AccionHistorialConfiguracion.DESACTIVACION)
                .estadoAnterior(anterior)
                .nuevoEstado(EstadoConfiguracionConcepto.INACTIVO)
                .motivo(motivo.trim())
                .usuarioRegistro(usuario != null ? usuario : "SISTEMA")
                .fechaRegistro(OffsetDateTime.now())
                .build();
        historialRepository.save(hist);

        ConfiguracionConceptoTributario guardada = configuracionRepository.save(conf);
        return mapConfiguracionToDetail(guardada);
    }

    @Override
    @Transactional
    public ConfiguracionDetailResponse crearNuevaVersion(UUID configuracionId, String motivo, String usuario) {
        ConfiguracionConceptoTributario base = configuracionRepository.findById(configuracionId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la configuración con ID: " + configuracionId));

        conceptoValidator.validarParaNuevaVersion(base);

        int siguienteVersion = configuracionRepository.findFirstByConceptoIdAndPeriodoIdOrderByNumeroVersionDesc(base.getConcepto().getId(), base.getPeriodo().getId())
                .map(c -> c.getNumeroVersion() + 1)
                .orElse(base.getNumeroVersion() + 1);

        ConfiguracionConceptoTributario nueva = ConfiguracionConceptoTributario.builder()
                .concepto(base.getConcepto())
                .periodo(base.getPeriodo())
                .norma(base.getNorma())
                .numeroVersion(siguienteVersion)
                .versionAnteriorId(base.getId())
                .estado(EstadoConfiguracionConcepto.BORRADOR)
                .descripcionMotivo(motivo != null && !motivo.isBlank() ? motivo : "Nueva versión generada a partir de v" + base.getNumeroVersion())
                .fechaInicioVigencia(base.getFechaInicioVigencia())
                .fechaFinVigencia(base.getFechaFinVigencia())
                .sector(base.getSector())
                .zona(base.getZona())
                .oficinaResponsableId(base.getOficinaResponsableId())
                .usuarioCreador(usuario != null ? usuario : "SISTEMA")
                .build();

        ConfiguracionConceptoTributario guardada = configuracionRepository.save(nueva);

        // Copiar fielmente parámetros base a la nueva versión para edición
        for (ParametroTributario pb : base.getParametros()) {
            ParametroTributario clon = ParametroTributario.builder()
                    .configuracion(guardada)
                    .codigo(pb.getCodigo())
                    .nombre(pb.getNombre())
                    .descripcion(pb.getDescripcion())
                    .tipoDato(pb.getTipoDato())
                    .valorDecimal(pb.getValorDecimal())
                    .valorTexto(pb.getValorTexto())
                    .valorBooleano(pb.getValorBooleano())
                    .valorFecha(pb.getValorFecha())
                    .unidad(pb.getUnidad())
                    .origenNormativo(pb.getOrigenNormativo())
                    .esObligatorio(pb.getEsObligatorio())
                    .ordenVisual(pb.getOrdenVisual())
                    .estado("ACTIVO")
                    .build();
            parametroRepository.save(clon);
            guardada.getParametros().add(clon);
        }

        // Copiar reglas base
        for (ReglaTributariaConfigurada rb : base.getReglas()) {
            ReglaTributariaConfigurada rclon = ReglaTributariaConfigurada.builder()
                    .configuracion(guardada)
                    .codigo(rb.getCodigo())
                    .nombre(rb.getNombre())
                    .tipoRegla(rb.getTipoRegla())
                    .condicionAplicacion(rb.getCondicionAplicacion())
                    .baseLegalArticulo(rb.getBaseLegalArticulo())
                    .porcentajeBeneficio(rb.getPorcentajeBeneficio())
                    .montoFijoBeneficio(rb.getMontoFijoBeneficio())
                    .ordenAplicacion(rb.getOrdenAplicacion())
                    .activo(rb.getActivo())
                    .build();
            reglaRepository.save(rclon);
            guardada.getReglas().add(rclon);
        }

        HistorialConfiguracionTributaria hist = HistorialConfiguracionTributaria.builder()
                .configuracion(guardada)
                .accion(AccionHistorialConfiguracion.NUEVA_VERSION)
                .estadoAnterior(null)
                .nuevoEstado(EstadoConfiguracionConcepto.BORRADOR)
                .motivo("Generación de versión " + siguienteVersion + " basada en versión " + base.getNumeroVersion() + ": " + (motivo != null ? motivo : ""))
                .usuarioRegistro(usuario != null ? usuario : "SISTEMA")
                .fechaRegistro(OffsetDateTime.now())
                .build();
        historialRepository.save(hist);

        log.info("Nueva versión {} creada para concepto {} con ID {}", siguienteVersion, base.getConcepto().getCodigo(), guardada.getId());
        return mapConfiguracionToDetail(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public ComparacionVersionesResponse compararVersiones(UUID versionBaseId, UUID versionComparadaId) {
        ConfiguracionConceptoTributario base = configuracionRepository.findById(versionBaseId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la versión base con ID: " + versionBaseId));

        ConfiguracionConceptoTributario comp = configuracionRepository.findById(versionComparadaId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la versión comparada con ID: " + versionComparadaId));

        Map<String, ParametroTributario> mapaBase = base.getParametros().stream()
                .collect(Collectors.toMap(ParametroTributario::getCodigo, p -> p, (a, b) -> a));

        Map<String, ParametroTributario> mapaComp = comp.getParametros().stream()
                .collect(Collectors.toMap(ParametroTributario::getCodigo, p -> p, (a, b) -> a));

        Set<String> todosCodigos = new TreeSet<>(mapaBase.keySet());
        todosCodigos.addAll(mapaComp.keySet());

        List<DiferenciaParametroDto> diferencias = new ArrayList<>();
        int mod = 0, agre = 0, elim = 0, sin = 0;

        for (String cod : todosCodigos) {
            ParametroTributario pBase = mapaBase.get(cod);
            ParametroTributario pComp = mapaComp.get(cod);

            if (pBase != null && pComp != null) {
                String v1 = pBase.getValorFormateado();
                String v2 = pComp.getValorFormateado();
                boolean iguales = Objects.equals(v1, v2);

                diferencias.add(DiferenciaParametroDto.builder()
                        .codigo(cod)
                        .nombre(pComp.getNombre())
                        .tipoDiferencia(iguales ? "SIN_CAMBIO" : "MODIFICADO")
                        .valorVersionAnterior(v1)
                        .valorVersionNueva(v2)
                        .unidad(pComp.getUnidad().getNombre())
                        .origenNormativo(pComp.getOrigenNormativo())
                        .build());

                if (iguales) sin++; else mod++;
            } else if (pBase == null) {
                diferencias.add(DiferenciaParametroDto.builder()
                        .codigo(cod)
                        .nombre(pComp.getNombre())
                        .tipoDiferencia("AGREGADO")
                        .valorVersionAnterior("-")
                        .valorVersionNueva(pComp.getValorFormateado())
                        .unidad(pComp.getUnidad().getNombre())
                        .origenNormativo(pComp.getOrigenNormativo())
                        .build());
                agre++;
            } else {
                diferencias.add(DiferenciaParametroDto.builder()
                        .codigo(cod)
                        .nombre(pBase.getNombre())
                        .tipoDiferencia("ELIMINADO")
                        .valorVersionAnterior(pBase.getValorFormateado())
                        .valorVersionNueva("-")
                        .unidad(pBase.getUnidad().getNombre())
                        .origenNormativo(pBase.getOrigenNormativo())
                        .build());
                elim++;
            }
        }

        return ComparacionVersionesResponse.builder()
                .conceptoId(base.getConcepto().getId())
                .conceptoCodigo(base.getConcepto().getCodigo())
                .conceptoNombre(base.getConcepto().getNombre())
                .versionBaseId(base.getId())
                .numeroVersionBase(base.getNumeroVersion())
                .estadoVersionBase(base.getEstado().getNombre())
                .normaVersionBase(base.getNorma() != null ? base.getNorma().getNumeroNorma() : "-")
                .fechaVigenciaBase(base.getFechaInicioVigencia() != null ? base.getFechaInicioVigencia().toString() : "-")
                .versionComparadaId(comp.getId())
                .numeroVersionComparada(comp.getNumeroVersion())
                .estadoVersionComparada(comp.getEstado().getNombre())
                .normaVersionComparada(comp.getNorma() != null ? comp.getNorma().getNumeroNorma() : "-")
                .fechaVigenciaComparada(comp.getFechaInicioVigencia() != null ? comp.getFechaInicioVigencia().toString() : "-")
                .parametros(diferencias)
                .totalModificados(mod)
                .totalAgregados(agre)
                .totalEliminados(elim)
                .totalSinCambio(sin)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConfiguracionSummaryDto> listarConfiguracionesPorConcepto(UUID conceptoId) {
        return configuracionRepository.findByConceptoIdOrderByNumeroVersionDesc(conceptoId)
                .stream()
                .map(this::mapConfiguracionToSummary)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NormaDetailResponse registrarNorma(NormaCreateRequest request, String usuario) {
        log.info("Registrando nueva norma tributaria: {} N° {}", request.getTipoNorma(), request.getNumeroNorma());
        NormaTributariaConfigurada mod = null;
        if (request.getNormaModificaId() != null) {
            mod = normaRepository.findById(request.getNormaModificaId()).orElse(null);
        }

        NormaTributariaConfigurada norma = NormaTributariaConfigurada.builder()
                .tipoNorma(request.getTipoNorma())
                .numeroNorma(request.getNumeroNorma().trim())
                .entidadEmisora(request.getEntidadEmisora() != null ? request.getEntidadEmisora().trim() : "Municipalidad Distrital de Quinua")
                .descripcion(request.getDescripcion().trim())
                .fechaPublicacion(request.getFechaPublicacion())
                .fechaInicioVigencia(request.getFechaInicioVigencia())
                .fechaFinVigencia(request.getFechaFinVigencia())
                .enlaceOficial(request.getEnlaceOficial())
                .alcanceTerritorial(request.getAlcanceTerritorial() != null ? request.getAlcanceTerritorial() : "DISTRITAL_QUINUA")
                .archivoHash(request.getArchivoHash())
                .normaModifica(mod)
                .estadoValidacion(EstadoValidacionNorma.REGISTRADA)
                .observaciones(request.getObservaciones())
                .build();

        NormaTributariaConfigurada guardada = normaRepository.save(norma);
        return mapNormaToDetail(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NormaSummaryDto> listarNormas() {
        return normaRepository.findAllByOrderByFechaInicioVigenciaDesc()
                .stream()
                .map(n -> NormaSummaryDto.builder()
                        .id(n.getId())
                        .tipoNorma(n.getTipoNorma() != null ? n.getTipoNorma().getDescripcion() : "")
                        .numeroNorma(n.getNumeroNorma())
                        .entidadEmisora(n.getEntidadEmisora())
                        .descripcion(n.getDescripcion())
                        .fechaPublicacion(n.getFechaPublicacion() != null ? n.getFechaPublicacion().toString() : "-")
                        .fechaInicioVigencia(n.getFechaInicioVigencia() != null ? n.getFechaInicioVigencia().toString() : "-")
                        .estadoValidacion(n.getEstadoValidacion() != null ? n.getEstadoValidacion().getDescripcion() : "")
                        .badgeClass(n.getEstadoValidacion() != null ? n.getEstadoValidacion().getBadgeClass() : "")
                        .validador(n.getUsuarioValidador() != null ? n.getUsuarioValidador().getUsername() : "-")
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PeriodoSummaryDto> listarPeriodos() {
        return periodoRepository.findAllByOrderByAnioDesc()
                .stream()
                .map(p -> PeriodoSummaryDto.builder()
                        .id(p.getId())
                        .anio(p.getAnio())
                        .fechaInicio(p.getFechaInicio() != null ? p.getFechaInicio().toString() : "")
                        .fechaFin(p.getFechaFin() != null ? p.getFechaFin().toString() : "")
                        .uitVigente(p.getUitVigente())
                        .estado(p.getEstado() != null ? p.getEstado().name() : "")
                        .badgeClass(p.getEstado() != null ? p.getEstado().getBadgeClass() : "")
                        .resolucionAprobacion(p.getResolucionAprobacion())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CalendarioCuotaResponse agregarCuotaCalendario(UUID conceptoId, UUID periodoId, CalendarioCuotaRequest request, String usuario) {
        ConceptoTributario concepto = conceptoRepository.findById(conceptoId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el concepto tributario con ID: " + conceptoId));

        PeriodoTributario periodo = periodoRepository.findById(periodoId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el período tributario con ID: " + periodoId));

        if (calendarioRepository.existsByConceptoIdAndPeriodoIdAndNumeroCuota(conceptoId, periodoId, request.getNumeroCuota())) {
            throw new BusinessException("Ya existe la cuota N° " + request.getNumeroCuota() + " programada para este concepto y período.");
        }

        NormaTributariaConfigurada norma = null;
        if (request.getNormaId() != null) {
            norma = normaRepository.findById(request.getNormaId()).orElse(null);
        }

        CalendarioTributario cal = CalendarioTributario.builder()
                .concepto(concepto)
                .periodo(periodo)
                .norma(norma)
                .numeroCuota(request.getNumeroCuota())
                .descripcionCuota(request.getDescripcionCuota().trim())
                .fechaVencimiento(request.getFechaVencimiento())
                .permiteDescuento(request.getPermiteDescuento() != null ? request.getPermiteDescuento() : false)
                .porcentajeDescuento(request.getPorcentajeDescuento())
                .estado(EstadoCalendarioVencimiento.PROGRAMADO)
                .build();

        CalendarioTributario guardado = calendarioRepository.save(cal);
        return mapCalendarioToResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalendarioCuotaResponse> listarCalendarioPorConceptoYPeriodo(UUID conceptoId, UUID periodoId) {
        return calendarioRepository.findByConceptoIdAndPeriodoIdOrderByNumeroCuotaAsc(conceptoId, periodoId)
                .stream()
                .map(this::mapCalendarioToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalConceptos", conceptoRepository.count());
        stats.put("conceptosActivos", conceptoRepository.countByEstado(EstadoConceptoTributario.ACTIVO) + conceptoRepository.countByEstado(EstadoConceptoTributario.VIGENTE));
        stats.put("configuracionesBorrador", configuracionRepository.countByEstado(EstadoConfiguracionConcepto.BORRADOR));
        stats.put("configuracionesEnRevision", configuracionRepository.countByEstado(EstadoConfiguracionConcepto.EN_REVISION));
        stats.put("configuracionesAprobadas", configuracionRepository.countByEstado(EstadoConfiguracionConcepto.APROBADO));
        stats.put("configuracionesActivas", configuracionRepository.countByEstado(EstadoConfiguracionConcepto.ACTIVO));
        stats.put("normasPendientes", normaRepository.countByEstadoValidacion(EstadoValidacionNorma.REGISTRADA));
        return stats;
    }

    private ConceptoSummaryDto mapToSummaryDto(ConceptoTributario c) {
        return ConceptoSummaryDto.builder()
                .id(c.getId())
                .codigo(c.getCodigo())
                .nombre(c.getNombre())
                .descripcion(c.getDescripcion())
                .tipoConcepto(c.getTipoConcepto() != null ? c.getTipoConcepto().getDescripcion() : "")
                .periodicidad(c.getPeriodicidad() != null ? c.getPeriodicidad().getDescripcion() : "")
                .aplicaAPredio(Boolean.TRUE.equals(c.getAplicaAPredio()))
                .cuentaPresupuestal(c.getCuentaPresupuestal())
                .estado(c.getEstado() != null ? c.getEstado().getDescripcion() : "Vigente")
                .badgeClass(c.getEstado() != null ? c.getEstado().getBadgeClass() : "")
                .fechaInicioVigencia(c.getFechaInicioVigencia() != null ? c.getFechaInicioVigencia().toString() : "")
                .build();
    }

    private ConceptoDetailResponse mapToDetailResponse(ConceptoTributario c) {
        List<ConfiguracionSummaryDto> configs = listarConfiguracionesPorConcepto(c.getId());

        // Buscar versión activa si existe
        ConfiguracionDetailResponse confActiva = configuracionRepository.findByConceptoIdOrderByNumeroVersionDesc(c.getId())
                .stream()
                .filter(cfg -> cfg.getEstado() == EstadoConfiguracionConcepto.ACTIVO)
                .findFirst()
                .map(this::mapConfiguracionToDetail)
                .orElse(null);

        return ConceptoDetailResponse.builder()
                .id(c.getId())
                .codigo(c.getCodigo())
                .nombre(c.getNombre())
                .descripcion(c.getDescripcion())
                .tipoConcepto(c.getTipoConcepto())
                .tipoConceptoDesc(c.getTipoConcepto() != null ? c.getTipoConcepto().getDescripcion() : "")
                .naturalezaTributaria(c.getNaturalezaTributaria())
                .naturalezaTributariaDesc(c.getNaturalezaTributaria() != null ? c.getNaturalezaTributaria().getDescripcion() : "")
                .periodicidad(c.getPeriodicidad())
                .periodicidadDesc(c.getPeriodicidad() != null ? c.getPeriodicidad().getDescripcion() : "")
                .aplicaAPredio(c.getAplicaAPredio())
                .cuentaPresupuestal(c.getCuentaPresupuestal())
                .estado(c.getEstado())
                .estadoDesc(c.getEstado() != null ? c.getEstado().getDescripcion() : "")
                .badgeClass(c.getEstado() != null ? c.getEstado().getBadgeClass() : "")
                .fechaInicioVigencia(c.getFechaInicioVigencia())
                .fechaFinVigencia(c.getFechaFinVigencia())
                .normaLegalId(c.getNormaLegal() != null ? c.getNormaLegal().getId() : null)
                .normaLegalNumero(c.getNormaLegal() != null ? c.getNormaLegal().getNumeroNorma() : null)
                .normaLegalDescripcion(c.getNormaLegal() != null ? c.getNormaLegal().getDescripcion() : null)
                .oficinaResponsableId(c.getOficinaResponsableId())
                .version(c.getVersion())
                .configuracionActiva(confActiva)
                .configuraciones(configs)
                .build();
    }

    private ConfiguracionSummaryDto mapConfiguracionToSummary(ConfiguracionConceptoTributario c) {
        return ConfiguracionSummaryDto.builder()
                .id(c.getId())
                .conceptoId(c.getConcepto().getId())
                .conceptoCodigo(c.getConcepto().getCodigo())
                .conceptoNombre(c.getConcepto().getNombre())
                .periodoId(c.getPeriodo().getId())
                .anioPeriodo(c.getPeriodo().getAnio())
                .numeroVersion(c.getNumeroVersion())
                .estado(c.getEstado())
                .estadoDesc(c.getEstado() != null ? c.getEstado().getDescripcion() : "")
                .badgeClass(c.getEstado() != null ? c.getEstado().getBadgeClass() : "")
                .normaNumero(c.getNorma() != null ? c.getNorma().getNumeroNorma() : "-")
                .fechaInicioVigencia(c.getFechaInicioVigencia())
                .fechaFinVigencia(c.getFechaFinVigencia())
                .cantidadParametros(c.getParametros().size())
                .cantidadReglas(c.getReglas().size())
                .usuarioCreador(c.getUsuarioCreador())
                .build();
    }

    private ConfiguracionDetailResponse mapConfiguracionToDetail(ConfiguracionConceptoTributario c) {
        List<ParametroResponse> params = c.getParametros().stream()
                .map(p -> ParametroResponse.builder()
                        .id(p.getId())
                        .configuracionId(c.getId())
                        .codigo(p.getCodigo())
                        .nombre(p.getNombre())
                        .descripcion(p.getDescripcion())
                        .tipoDato(p.getTipoDato())
                        .tipoDatoDesc(p.getTipoDato() != null ? p.getTipoDato().getDescripcion() : "")
                        .valorDecimal(p.getValorDecimal())
                        .valorTexto(p.getValorTexto())
                        .valorBooleano(p.getValorBooleano())
                        .valorFecha(p.getValorFecha())
                        .unidad(p.getUnidad())
                        .unidadDesc(p.getUnidad() != null ? p.getUnidad().getDescripcion() : "")
                        .valorFormateado(p.getValorFormateado())
                        .origenNormativo(p.getOrigenNormativo())
                        .esObligatorio(p.getEsObligatorio())
                        .ordenVisual(p.getOrdenVisual())
                        .estado(p.getEstado())
                        .build())
                .collect(Collectors.toList());

        List<ReglaResponse> reglas = c.getReglas().stream()
                .map(r -> ReglaResponse.builder()
                        .id(r.getId())
                        .configuracionId(c.getId())
                        .codigo(r.getCodigo())
                        .nombre(r.getNombre())
                        .tipoRegla(r.getTipoRegla())
                        .tipoReglaDesc(r.getTipoRegla() != null ? r.getTipoRegla().getDescripcion() : "")
                        .condicionAplicacion(r.getCondicionAplicacion())
                        .baseLegalArticulo(r.getBaseLegalArticulo())
                        .porcentajeBeneficio(r.getPorcentajeBeneficio())
                        .montoFijoBeneficio(r.getMontoFijoBeneficio())
                        .ordenAplicacion(r.getOrdenAplicacion())
                        .activo(r.getActivo())
                        .build())
                .collect(Collectors.toList());

        List<HistorialConfiguracionResponse> historial = c.getHistorial().stream()
                .map(h -> HistorialConfiguracionResponse.builder()
                        .id(h.getId())
                        .configuracionId(c.getId())
                        .accion(h.getAccion())
                        .accionDesc(h.getAccion() != null ? h.getAccion().getDescripcion() : "")
                        .estadoAnterior(h.getEstadoAnterior())
                        .estadoAnteriorDesc(h.getEstadoAnterior() != null ? h.getEstadoAnterior().getDescripcion() : "-")
                        .nuevoEstado(h.getNuevoEstado())
                        .nuevoEstadoDesc(h.getNuevoEstado() != null ? h.getNuevoEstado().getDescripcion() : "")
                        .nuevoEstadoBadgeClass(h.getNuevoEstado() != null ? h.getNuevoEstado().getBadgeClass() : "")
                        .motivo(h.getMotivo())
                        .resolucionSustento(h.getResolucionSustento())
                        .usuarioRegistro(h.getUsuarioRegistro())
                        .fechaRegistro(h.getFechaRegistro())
                        .detallesCambios(h.getDetallesCambios())
                        .build())
                .collect(Collectors.toList());

        return ConfiguracionDetailResponse.builder()
                .id(c.getId())
                .conceptoId(c.getConcepto().getId())
                .conceptoCodigo(c.getConcepto().getCodigo())
                .conceptoNombre(c.getConcepto().getNombre())
                .periodoId(c.getPeriodo().getId())
                .anioPeriodo(c.getPeriodo().getAnio())
                .normaId(c.getNorma() != null ? c.getNorma().getId() : null)
                .normaNumero(c.getNorma() != null ? c.getNorma().getNumeroNorma() : null)
                .normaDescripcion(c.getNorma() != null ? c.getNorma().getDescripcion() : null)
                .numeroVersion(c.getNumeroVersion())
                .versionAnteriorId(c.getVersionAnteriorId())
                .estado(c.getEstado())
                .estadoDesc(c.getEstado() != null ? c.getEstado().getDescripcion() : "")
                .badgeClass(c.getEstado() != null ? c.getEstado().getBadgeClass() : "")
                .descripcionMotivo(c.getDescripcionMotivo())
                .fechaInicioVigencia(c.getFechaInicioVigencia())
                .fechaFinVigencia(c.getFechaFinVigencia())
                .sectorId(c.getSector() != null ? c.getSector().getId() : null)
                .sectorNombre(c.getSector() != null ? c.getSector().getNombre() : null)
                .zona(c.getZona())
                .oficinaResponsableId(c.getOficinaResponsableId())
                .usuarioCreador(c.getUsuarioCreador())
                .usuarioAprobador(c.getUsuarioAprobador())
                .fechaAprobacion(c.getFechaAprobacion())
                .resolucionAprobacion(c.getResolucionAprobacion())
                .usuarioActivador(c.getUsuarioActivador())
                .fechaActivacion(c.getFechaActivacion())
                .motivoDesactivacion(c.getMotivoDesactivacion())
                .version(c.getVersion())
                .parametros(params)
                .reglas(reglas)
                .historial(historial)
                .build();
    }

    private CalendarioCuotaResponse mapCalendarioToResponse(CalendarioTributario c) {
        return CalendarioCuotaResponse.builder()
                .id(c.getId())
                .conceptoId(c.getConcepto().getId())
                .conceptoNombre(c.getConcepto().getNombre())
                .periodoId(c.getPeriodo().getId())
                .anioPeriodo(c.getPeriodo().getAnio())
                .numeroCuota(c.getNumeroCuota())
                .descripcionCuota(c.getDescripcionCuota())
                .fechaVencimiento(c.getFechaVencimiento())
                .permiteDescuento(c.getPermiteDescuento())
                .porcentajeDescuento(c.getPorcentajeDescuento())
                .estado(c.getEstado())
                .estadoDesc(c.getEstado() != null ? c.getEstado().getDescripcion() : "")
                .badgeClass(c.getEstado() != null ? c.getEstado().getBadgeClass() : "")
                .resolucionProrroga(c.getResolucionProrroga())
                .build();
    }

    private NormaDetailResponse mapNormaToDetail(NormaTributariaConfigurada n) {
        return NormaDetailResponse.builder()
                .id(n.getId())
                .tipoNorma(n.getTipoNorma())
                .tipoNormaDesc(n.getTipoNorma() != null ? n.getTipoNorma().getDescripcion() : "")
                .numeroNorma(n.getNumeroNorma())
                .entidadEmisora(n.getEntidadEmisora())
                .descripcion(n.getDescripcion())
                .fechaPublicacion(n.getFechaPublicacion())
                .fechaInicioVigencia(n.getFechaInicioVigencia())
                .fechaFinVigencia(n.getFechaFinVigencia())
                .enlaceOficial(n.getEnlaceOficial())
                .alcanceTerritorial(n.getAlcanceTerritorial())
                .archivoHash(n.getArchivoHash())
                .estadoValidacion(n.getEstadoValidacion())
                .estadoValidacionDesc(n.getEstadoValidacion() != null ? n.getEstadoValidacion().getDescripcion() : "")
                .badgeClass(n.getEstadoValidacion() != null ? n.getEstadoValidacion().getBadgeClass() : "")
                .normaModificaId(n.getNormaModifica() != null ? n.getNormaModifica().getId() : null)
                .normaModificaNumero(n.getNormaModifica() != null ? n.getNormaModifica().getNumeroNorma() : null)
                .validador(n.getUsuarioValidador() != null ? n.getUsuarioValidador().getUsername() : null)
                .observaciones(n.getObservaciones())
                .build();
    }
}
