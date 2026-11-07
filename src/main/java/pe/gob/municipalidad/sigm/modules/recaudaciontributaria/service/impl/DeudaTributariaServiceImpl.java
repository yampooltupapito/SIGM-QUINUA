package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.document.repository.DocumentoRepository;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.deuda.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.DeudaTributariaService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.InteresMoratorioService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeudaTributariaServiceImpl implements DeudaTributariaService {

    private final ObligacionTributariaRepository obligacionRepository;
    private final SeguimientoDeudaTributariaRepository seguimientoRepository;
    private final GestionSeguimientoDeudaRepository gestionRepository;
    private final CompromisoSeguimientoDeudaRepository compromisoRepository;
    private final InteresMoratorioService interesService;
    private final UsuarioRepository usuarioRepository;
    private final OficinaRepository oficinaRepository;
    private final DocumentoRepository commonDocumentoRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<CarteraItemDto> listarCartera(
            String query,
            UUID contribuyenteId,
            UUID predioId,
            UUID conceptoId,
            UUID periodoId,
            EstadoObligacionTributaria estado,
            EstadoExigibilidadObligacion exigibilidad,
            Boolean soloConSaldo,
            LocalDate fechaCorte,
            TramoAntiguedadDeuda tramo,
            Pageable pageable) {

        LocalDate corte = fechaCorte != null ? fechaCorte : LocalDate.now();

        Specification<ObligacionTributaria> spec = CarteraDeudaSpecification.filtrarCartera(
                query, contribuyenteId, predioId, conceptoId, periodoId,
                estado, exigibilidad, soloConSaldo, corte, tramo);

        Page<ObligacionTributaria> pageObligaciones = obligacionRepository.findAll(spec, pageable);

        List<CarteraItemDto> dtoList = pageObligaciones.getContent().stream()
                .map(ob -> mapToCarteraItem(ob, corte))
                .toList();

        return new PageImpl<>(dtoList, pageable, pageObligaciones.getTotalElements());
    }

    @Override
    @Transactional
    public DeudaDetalleResponse obtenerFichaDeuda(UUID obligacionId, LocalDate fechaCorte) {
        ObligacionTributaria obligacion = obligacionRepository.findById(obligacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Obligación tributaria no encontrada: " + obligacionId));

        LocalDate corte = fechaCorte != null ? fechaCorte : LocalDate.now();
        SeguimientoDeudaTributaria seguimiento = obtenerOCrearSeguimiento(obligacion, corte);

        SimulacionInteresResponse simulacion = interesService.calcularInteresParaObligacion(obligacion, corte);

        return mapToDetalleResponse(obligacion, seguimiento, simulacion, corte);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardDeudasResponse obtenerDashboardDeudas(LocalDate fechaCorte) {
        LocalDate corte = fechaCorte != null ? fechaCorte : LocalDate.now();

        BigDecimal saldoTotal = obligacionRepository.sumarTotalSaldoPendiente();
        BigDecimal saldoNoVencido = obligacionRepository.sumarSaldoNoVencido(corte);
        BigDecimal saldoVencido = obligacionRepository.sumarSaldoVencido(corte);
        BigDecimal saldoExigible = obligacionRepository.sumarSaldoExigible();

        long totalPendientes = obligacionRepository.countByEstado(EstadoObligacionTributaria.VIGENTE);
        long contribuyentesConDeuda = obligacionRepository.countContribuyentesConDeuda();
        long seguimientosActivos = seguimientoRepository.countByEstadoSeguimiento(EstadoSeguimientoDeuda.EN_SEGUIMIENTO)
                + seguimientoRepository.countByEstadoSeguimiento(EstadoSeguimientoDeuda.ASIGNADO);
        long compromisosPendientes = compromisoRepository.countByEstado(EstadoCompromisoDeuda.PENDIENTE);

        // Agregaciones por concepto, antigüedad y período desde cartera activa
        List<ObligacionTributaria> activas = obligacionRepository.findAll().stream()
                .filter(o -> o.getSaldo() != null && o.getSaldo().compareTo(BigDecimal.ZERO) > 0)
                .filter(o -> o.getEstado() != EstadoObligacionTributaria.ANULADA && o.getEstado() != EstadoObligacionTributaria.BORRADOR)
                .toList();

        Map<String, BigDecimal> distConcepto = activas.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getConcepto() != null ? o.getConcepto().getNombre() : "GENERAL",
                        Collectors.reducing(BigDecimal.ZERO, ObligacionTributaria::getSaldo, BigDecimal::add)
                ));

        Map<String, BigDecimal> distTramo = new LinkedHashMap<>();
        for (TramoAntiguedadDeuda t : TramoAntiguedadDeuda.values()) {
            distTramo.put(t.getEtiqueta(), BigDecimal.ZERO);
        }
        for (ObligacionTributaria o : activas) {
            long dias = 0;
            if (o.getFechaVencimiento() != null && corte.isAfter(o.getFechaVencimiento())) {
                dias = ChronoUnit.DAYS.between(o.getFechaVencimiento(), corte);
            }
            TramoAntiguedadDeuda tramo = TramoAntiguedadDeuda.calcularTramo(dias);
            distTramo.put(tramo.getEtiqueta(), distTramo.get(tramo.getEtiqueta()).add(o.getSaldo()));
        }

        Map<String, BigDecimal> distPeriodo = activas.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getPeriodo() != null ? String.valueOf(o.getPeriodo().getAnio()) : "OTROS",
                        TreeMap::new,
                        Collectors.reducing(BigDecimal.ZERO, ObligacionTributaria::getSaldo, BigDecimal::add)
                ));

        long vencidasCount = activas.stream()
                .filter(o -> o.getFechaVencimiento() != null && corte.isAfter(o.getFechaVencimiento()))
                .count();

        return DashboardDeudasResponse.builder()
                .saldoTotalPendiente(saldoTotal != null ? saldoTotal : BigDecimal.ZERO)
                .saldoNoVencido(saldoNoVencido != null ? saldoNoVencido : BigDecimal.ZERO)
                .saldoVencido(saldoVencido != null ? saldoVencido : BigDecimal.ZERO)
                .saldoExigible(saldoExigible != null ? saldoExigible : BigDecimal.ZERO)
                .totalObligacionesPendientes(totalPendientes)
                .totalObligacionesVencidas(vencidasCount)
                .totalContribuyentesConDeuda(contribuyentesConDeuda)
                .totalSeguimientosActivos(seguimientosActivos)
                .totalCompromisosPendientes(compromisosPendientes)
                .distribucionPorConcepto(distConcepto)
                .distribucionPorTramo(distTramo)
                .distribucionPorPeriodo(distPeriodo)
                .build();
    }

    @Override
    @Transactional
    public DeudaDetalleResponse clasificarObligacion(UUID obligacionId, ClasificarDeudaRequest request, String username) {
        ObligacionTributaria obligacion = obligacionRepository.findById(obligacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Obligación tributaria no encontrada: " + obligacionId));

        SeguimientoDeudaTributaria seguimiento = obtenerOCrearSeguimiento(obligacion, LocalDate.now());
        seguimiento.setClasificacion(request.getClasificacion());
        if (request.getObservaciones() != null && !request.getObservaciones().isBlank()) {
            seguimiento.setObservaciones(request.getObservaciones().trim());
        }
        seguimiento.setFechaUltimaRevision(OffsetDateTime.now());
        seguimiento.setUpdatedBy(username);
        seguimiento = seguimientoRepository.save(seguimiento);

        // Registrar gestión automática de cambio de clasificación
        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);
        GestionSeguimientoDeuda gestion = GestionSeguimientoDeuda.builder()
                .seguimiento(seguimiento)
                .tipoGestion(TipoGestionDeuda.ACTUALIZACION_SEGUIMIENTO)
                .fechaGestion(OffsetDateTime.now())
                .responsable(usuario)
                .resultado("Clasificación actualizada a: " + request.getClasificacion().getDescripcion())
                .observaciones(request.getObservaciones())
                .build();
        gestion.setCreatedBy(username);
        gestionRepository.save(gestion);

        log.info("Obligación {} reclasificada a {} por {}", obligacion.getCodigoObligacion(), request.getClasificacion(), username);
        return obtenerFichaDeuda(obligacionId, LocalDate.now());
    }

    @Override
    @Transactional
    public DeudaDetalleResponse asignarResponsable(UUID obligacionId, AsignarResponsableRequest request, String username) {
        ObligacionTributaria obligacion = obligacionRepository.findById(obligacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Obligación tributaria no encontrada: " + obligacionId));

        Usuario responsable = usuarioRepository.findById(request.getResponsableId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario responsable no encontrado: " + request.getResponsableId()));

        Oficina oficina = null;
        if (request.getOficinaId() != null) {
            oficina = oficinaRepository.findById(request.getOficinaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Oficina no encontrada: " + request.getOficinaId()));
        }

        SeguimientoDeudaTributaria seguimiento = obtenerOCrearSeguimiento(obligacion, LocalDate.now());
        seguimiento.setResponsable(responsable);
        seguimiento.setOficina(oficina);
        seguimiento.setEstadoSeguimiento(EstadoSeguimientoDeuda.ASIGNADO);
        seguimiento.setFechaUltimaRevision(OffsetDateTime.now());
        seguimiento.setUpdatedBy(username);
        seguimiento = seguimientoRepository.save(seguimiento);

        // Registrar gestión de asignación
        Usuario autor = usuarioRepository.findByUsername(username).orElse(null);
        GestionSeguimientoDeuda gestion = GestionSeguimientoDeuda.builder()
                .seguimiento(seguimiento)
                .tipoGestion(TipoGestionDeuda.DERIVACION_INTERNA)
                .fechaGestion(OffsetDateTime.now())
                .responsable(autor)
                .resultado("Asignación de seguimiento a: " + responsable.getNombreCompleto() + (oficina != null ? " (" + oficina.getNombre() + ")" : ""))
                .observaciones(request.getObservaciones())
                .build();
        gestion.setCreatedBy(username);
        gestionRepository.save(gestion);

        log.info("Obligación {} asignada a responsable {} por {}", obligacion.getCodigoObligacion(), responsable.getUsername(), username);
        return obtenerFichaDeuda(obligacionId, LocalDate.now());
    }

    @Override
    @Transactional
    public DeudaDetalleResponse registrarGestion(UUID obligacionId, RegistrarGestionDeudaRequest request, String username) {
        ObligacionTributaria obligacion = obligacionRepository.findById(obligacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Obligación tributaria no encontrada: " + obligacionId));

        Usuario autor = usuarioRepository.findByUsername(username).orElse(null);
        Documento docSustento = null;
        if (request.getDocumentoSustentoId() != null) {
            docSustento = commonDocumentoRepository.findById(request.getDocumentoSustentoId()).orElse(null);
        }

        SeguimientoDeudaTributaria seguimiento = obtenerOCrearSeguimiento(obligacion, LocalDate.now());

        GestionSeguimientoDeuda gestion = GestionSeguimientoDeuda.builder()
                .seguimiento(seguimiento)
                .tipoGestion(request.getTipoGestion())
                .fechaGestion(OffsetDateTime.now())
                .responsable(autor)
                .resultado(request.getResultado().trim())
                .observaciones(request.getObservaciones())
                .documentoSustento(docSustento)
                .build();
        gestion.setCreatedBy(username);
        gestionRepository.save(gestion);

        seguimiento.setFechaUltimaRevision(OffsetDateTime.now());
        seguimiento.setUpdatedBy(username);
        seguimientoRepository.save(seguimiento);

        log.info("Gestión de seguimiento registrada para obligación {} por {}", obligacion.getCodigoObligacion(), username);
        return obtenerFichaDeuda(obligacionId, LocalDate.now());
    }

    @Override
    @Transactional
    public DeudaDetalleResponse registrarCompromiso(UUID obligacionId, RegistrarCompromisoDeudaRequest request, String username) {
        ObligacionTributaria obligacion = obligacionRepository.findById(obligacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Obligación tributaria no encontrada: " + obligacionId));

        if (obligacion.getSaldo() == null || obligacion.getSaldo().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("No se pueden registrar compromisos de pago para una obligación sin saldo pendiente.");
        }

        Usuario autor = usuarioRepository.findByUsername(username).orElse(null);
        SeguimientoDeudaTributaria seguimiento = obtenerOCrearSeguimiento(obligacion, LocalDate.now());

        CompromisoSeguimientoDeuda compromiso = CompromisoSeguimientoDeuda.builder()
                .seguimiento(seguimiento)
                .fechaRegistro(OffsetDateTime.now())
                .fechaComprometida(request.getFechaComprometida())
                .montoComprometido(request.getMontoComprometido() != null ? request.getMontoComprometido() : obligacion.getSaldo())
                .observaciones(request.getObservaciones())
                .estado(EstadoCompromisoDeuda.PENDIENTE)
                .responsable(autor)
                .build();
        compromiso.setCreatedBy(username);
        compromisoRepository.save(compromiso);

        seguimiento.setEstadoSeguimiento(EstadoSeguimientoDeuda.COMPROMISO_PENDIENTE);
        seguimiento.setFechaUltimaRevision(OffsetDateTime.now());
        seguimiento.setUpdatedBy(username);
        seguimientoRepository.save(seguimiento);

        log.info("Compromiso informativo de pago registrado para obligación {} por {}", obligacion.getCodigoObligacion(), username);
        return obtenerFichaDeuda(obligacionId, LocalDate.now());
    }

    private SeguimientoDeudaTributaria obtenerOCrearSeguimiento(ObligacionTributaria obligacion, LocalDate fechaCorte) {
        return seguimientoRepository.findByObligacionId(obligacion.getId())
                .orElseGet(() -> {
                    ClasificacionDeudaTributaria clasif = determinarClasificacion(obligacion, null, fechaCorte);
                    SeguimientoDeudaTributaria nuevo = SeguimientoDeudaTributaria.builder()
                            .obligacion(obligacion)
                            .contribuyente(obligacion.getContribuyente())
                            .clasificacion(clasif)
                            .estadoSeguimiento(EstadoSeguimientoDeuda.EN_SEGUIMIENTO)
                            .fechaInicio(OffsetDateTime.now())
                            .fechaUltimaRevision(OffsetDateTime.now())
                            .build();
                    nuevo.setCreatedBy("SYSTEM");
                    return seguimientoRepository.save(nuevo);
                });
    }

    private ClasificacionDeudaTributaria determinarClasificacion(
            ObligacionTributaria obligacion,
            SeguimientoDeudaTributaria seguimiento,
            LocalDate fechaCorte) {

        if (obligacion.getSaldo() == null || obligacion.getSaldo().compareTo(BigDecimal.ZERO) <= 0) {
            return ClasificacionDeudaTributaria.CANCELADA;
        }

        if (obligacion.getEstado() == EstadoObligacionTributaria.SUSPENDIDA
                || obligacion.getEstadoExigibilidad() == EstadoExigibilidadObligacion.SUSPENDIDA_LEGALMENTE) {
            return ClasificacionDeudaTributaria.SUSPENDIDA;
        }

        if (seguimiento != null && seguimiento.getClasificacion() != null) {
            ClasificacionDeudaTributaria actual = seguimiento.getClasificacion();
            if (actual == ClasificacionDeudaTributaria.OBSERVADA
                    || actual == ClasificacionDeudaTributaria.EN_REVISION
                    || actual == ClasificacionDeudaTributaria.CON_FRACCIONAMIENTO_VIGENTE) {
                return actual;
            }
        }

        LocalDate corte = fechaCorte != null ? fechaCorte : LocalDate.now();
        if (obligacion.getFechaVencimiento() == null || !corte.isAfter(obligacion.getFechaVencimiento())) {
            return ClasificacionDeudaTributaria.PENDIENTE_NO_VENCIDA;
        }

        if (obligacion.getEstadoExigibilidad() == EstadoExigibilidadObligacion.EXIGIBLE
                || obligacion.getEstadoExigibilidad() == EstadoExigibilidadObligacion.EN_COBRANZA_ORDINARIA) {
            return ClasificacionDeudaTributaria.VENCIDA_EXIGIBLE;
        }

        return ClasificacionDeudaTributaria.VENCIDA_PENDIENTE_VERIFICACION;
    }

    private CarteraItemDto mapToCarteraItem(ObligacionTributaria o, LocalDate fechaCorte) {
        long diasMorosidad = 0;
        if (o.getFechaVencimiento() != null && fechaCorte.isAfter(o.getFechaVencimiento())) {
            diasMorosidad = ChronoUnit.DAYS.between(o.getFechaVencimiento(), fechaCorte);
        }
        TramoAntiguedadDeuda tramo = TramoAntiguedadDeuda.calcularTramo(diasMorosidad);

        SeguimientoDeudaTributaria seg = seguimientoRepository.findByObligacionId(o.getId()).orElse(null);
        ClasificacionDeudaTributaria clasif = determinarClasificacion(o, seg, fechaCorte);

        return CarteraItemDto.builder()
                .obligacionId(o.getId())
                .codigoObligacion(o.getCodigoObligacion())
                .contribuyenteId(o.getContribuyente() != null ? o.getContribuyente().getId() : null)
                .contribuyenteNombre(o.getContribuyente() != null ? o.getContribuyente().getNombreRazonSocial() : "-")
                .contribuyenteNumeroDoc(o.getContribuyente() != null ? o.getContribuyente().getNumeroDocumento() : "-")
                .contribuyenteTipoDoc(o.getContribuyente() != null ? o.getContribuyente().getTipoDocumento() : "-")
                .conceptoCodigo(o.getConcepto() != null ? o.getConcepto().getCodigo() : "-")
                .conceptoNombre(o.getConcepto() != null ? o.getConcepto().getNombre() : "-")
                .periodoAnio(o.getPeriodo() != null ? String.valueOf(o.getPeriodo().getAnio()) : "-")
                .cuotaNumero(o.getCuotaNumero())
                .totalCuotas(o.getTotalCuotas())
                .predioCodigo(o.getPredio() != null ? o.getPredio().getCodigoPredio() : null)
                .predioDireccion(o.getPredio() != null ? o.getPredio().getDireccion() : null)
                .importeDeterminado(o.getImporteDeterminado())
                .importePagado(o.getImportePagado())
                .saldo(o.getSaldo())
                .moneda(o.getMoneda() != null ? o.getMoneda() : "PEN")
                .fechaVencimiento(o.getFechaVencimiento())
                .diasMorosidad(diasMorosidad)
                .tramoAntiguedad(tramo)
                .estadoObligacion(o.getEstado())
                .estadoExigibilidad(o.getEstadoExigibilidad())
                .clasificacion(clasif)
                .seguimientoId(seg != null ? seg.getId() : null)
                .responsableNombre(seg != null && seg.getResponsable() != null ? seg.getResponsable().getNombreCompleto() : "Sin Asignar")
                .oficinaNombre(seg != null && seg.getOficina() != null ? seg.getOficina().getNombre() : null)
                .totalGestiones(seg != null && seg.getGestiones() != null ? seg.getGestiones().size() : 0)
                .totalCompromisos(seg != null && seg.getCompromisos() != null ? seg.getCompromisos().size() : 0)
                .build();
    }

    private DeudaDetalleResponse mapToDetalleResponse(
            ObligacionTributaria o,
            SeguimientoDeudaTributaria s,
            SimulacionInteresResponse simulacion,
            LocalDate fechaCorte) {

        long diasMorosidad = 0;
        if (o.getFechaVencimiento() != null && fechaCorte.isAfter(o.getFechaVencimiento())) {
            diasMorosidad = ChronoUnit.DAYS.between(o.getFechaVencimiento(), fechaCorte);
        }
        TramoAntiguedadDeuda tramo = TramoAntiguedadDeuda.calcularTramo(diasMorosidad);
        ClasificacionDeudaTributaria clasif = determinarClasificacion(o, s, fechaCorte);

        List<DeudaDetalleResponse.GestionDto> gestionesDto = s != null && s.getGestiones() != null
                ? s.getGestiones().stream().map(g -> DeudaDetalleResponse.GestionDto.builder()
                        .id(g.getId())
                        .tipoGestion(g.getTipoGestion())
                        .fechaGestion(g.getFechaGestion())
                        .responsableNombre(g.getResponsable() != null ? g.getResponsable().getNombreCompleto() : "Sistema")
                        .resultado(g.getResultado())
                        .observaciones(g.getObservaciones())
                        .documentoSustentoId(g.getDocumentoSustento() != null ? g.getDocumentoSustento().getId() : null)
                        .documentoNombre(g.getDocumentoSustento() != null ? g.getDocumentoSustento().getNombreOriginal() : null)
                        .build()).toList()
                : Collections.emptyList();

        List<DeudaDetalleResponse.CompromisoDto> compromisosDto = s != null && s.getCompromisos() != null
                ? s.getCompromisos().stream().map(c -> DeudaDetalleResponse.CompromisoDto.builder()
                        .id(c.getId())
                        .fechaRegistro(c.getFechaRegistro())
                        .fechaComprometida(c.getFechaComprometida())
                        .montoComprometido(c.getMontoComprometido())
                        .observaciones(c.getObservaciones())
                        .estado(c.getEstado())
                        .responsableNombre(c.getResponsable() != null ? c.getResponsable().getNombreCompleto() : "Sistema")
                        .build()).toList()
                : Collections.emptyList();

        return DeudaDetalleResponse.builder()
                .obligacionId(o.getId())
                .codigoObligacion(o.getCodigoObligacion())
                .tipoDeterminacion(o.getTipoDeterminacion() != null ? o.getTipoDeterminacion().name() : "-")
                .importeDeterminado(o.getImporteDeterminado())
                .importePagado(o.getImportePagado())
                .importeAjustes(o.getImporteAjustes())
                .saldo(o.getSaldo())
                .moneda(o.getMoneda() != null ? o.getMoneda() : "PEN")
                .fechaDeterminacion(o.getFechaDeterminacion())
                .fechaVencimiento(o.getFechaVencimiento())
                .estadoObligacion(o.getEstado())
                .estadoFinanciero(o.getEstadoFinanciero())
                .estadoExigibilidad(o.getEstadoExigibilidad())
                .cuotaNumero(o.getCuotaNumero())
                .totalCuotas(o.getTotalCuotas())
                .contribuyenteId(o.getContribuyente() != null ? o.getContribuyente().getId() : null)
                .contribuyenteNombre(o.getContribuyente() != null ? o.getContribuyente().getNombreRazonSocial() : "-")
                .contribuyenteNumeroDoc(o.getContribuyente() != null ? o.getContribuyente().getNumeroDocumento() : "-")
                .contribuyenteTipoDoc(o.getContribuyente() != null ? o.getContribuyente().getTipoDocumento() : "-")
                .contribuyenteDomicilio(o.getContribuyente() != null ? o.getContribuyente().getDomicilioFiscal() : "-")
                .conceptoCodigo(o.getConcepto() != null ? o.getConcepto().getCodigo() : "-")
                .conceptoNombre(o.getConcepto() != null ? o.getConcepto().getNombre() : "-")
                .periodoAnio(o.getPeriodo() != null ? String.valueOf(o.getPeriodo().getAnio()) : "-")
                .predioCodigo(o.getPredio() != null ? o.getPredio().getCodigoPredio() : null)
                .predioDireccion(o.getPredio() != null ? o.getPredio().getDireccion() : null)
                .diasMorosidad(diasMorosidad)
                .tramoAntiguedad(tramo)
                .clasificacion(clasif)
                .seguimientoId(s != null ? s.getId() : null)
                .estadoSeguimiento(s != null ? s.getEstadoSeguimiento() : EstadoSeguimientoDeuda.EN_SEGUIMIENTO)
                .responsableId(s != null && s.getResponsable() != null ? s.getResponsable().getId() : null)
                .responsableNombre(s != null && s.getResponsable() != null ? s.getResponsable().getNombreCompleto() : "Sin Asignar")
                .oficinaId(s != null && s.getOficina() != null ? s.getOficina().getId() : null)
                .oficinaNombre(s != null && s.getOficina() != null ? s.getOficina().getNombre() : null)
                .fechaInicioSeguimiento(s != null ? s.getFechaInicio() : null)
                .fechaUltimaRevision(s != null ? s.getFechaUltimaRevision() : null)
                .observacionesSeguimiento(s != null ? s.getObservaciones() : null)
                .gestiones(gestionesDto)
                .compromisos(compromisosDto)
                .simulacionInteres(simulacion)
                .build();
    }
}
