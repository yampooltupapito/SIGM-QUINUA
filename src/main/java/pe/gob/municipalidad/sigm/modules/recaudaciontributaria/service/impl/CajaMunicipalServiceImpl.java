package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.cajapago.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.CajaMunicipalService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.specification.CajaSpecification;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.specification.ConciliacionSpecification;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.specification.SesionCajaSpecification;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.validator.PagoValidator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CajaMunicipalServiceImpl implements CajaMunicipalService {

    private final CajaMunicipalRepository cajaRepository;
    private final SesionCajaMunicipalRepository sesionRepository;
    private final ConciliacionCajaMunicipalRepository conciliacionRepository;
    private final PagoTributarioRepository pagoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PagoValidator pagoValidator;

    @Override
    @Transactional(readOnly = true)
    public Page<CajaResponse> listarCajas(String busqueda, EstadoCajaMunicipal estado, Pageable pageable) {
        return cajaRepository.findAll(CajaSpecification.conFiltros(busqueda, estado), pageable)
                .map(this::mapearCajaAResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CajaResponse> listarCajasActivas() {
        return cajaRepository.findByEstado(EstadoCajaMunicipal.ACTIVA).stream()
                .map(this::mapearCajaAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CajaResponse obtenerCajaPorId(UUID id) {
        CajaMunicipal caja = cajaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Caja Municipal no encontrada: " + id));
        return mapearCajaAResponse(caja);
    }

    @Override
    @Transactional
    public CajaResponse crearCaja(CrearCajaRequest request, String usuario) {
        if (cajaRepository.existsByCodigo(request.getCodigo().trim())) {
            throw new BusinessException("Ya existe una caja municipal registrada con el código: " + request.getCodigo());
        }

        Usuario cajero = null;
        if (request.getCajeroAsignadoId() != null) {
            cajero = usuarioRepository.findById(request.getCajeroAsignadoId())
                    .orElseThrow(() -> new BusinessException("El cajero asignado no existe"));
        }

        CajaMunicipal caja = CajaMunicipal.builder()
                .codigo(request.getCodigo().trim().toUpperCase())
                .nombre(request.getNombre().trim())
                .oficinaId(request.getOficinaId())
                .ubicacion(request.getUbicacion())
                .cajeroAsignado(cajero)
                .estado(EstadoCajaMunicipal.ACTIVA)
                .observaciones(request.getObservaciones())
                .build();

        caja.setCreatedBy(usuario != null ? usuario : "SISTEMA");
        CajaMunicipal guardada = cajaRepository.save(caja);
        log.info("Caja Municipal registrada: {} por {}", guardada.getCodigo(), usuario);
        return mapearCajaAResponse(guardada);
    }

    @Override
    @Transactional
    public CajaResponse actualizarEstadoCaja(UUID id, EstadoCajaMunicipal nuevoEstado, String usuario) {
        CajaMunicipal caja = cajaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Caja Municipal no encontrada: " + id));

        if (nuevoEstado != EstadoCajaMunicipal.ACTIVA) {
            Optional<SesionCajaMunicipal> sesionActiva = sesionRepository.findSesionActivaPorCaja(id);
            if (sesionActiva.isPresent()) {
                throw new BusinessException("No se puede inactivar la caja " + caja.getCodigo() + " porque tiene una sesión de turno abierta.");
            }
        }

        caja.setEstado(nuevoEstado);
        caja.setUpdatedBy(usuario != null ? usuario : "SISTEMA");
        return mapearCajaAResponse(cajaRepository.save(caja));
    }

    @Override
    @Transactional
    public SesionCajaResponse abrirSesion(AbrirSesionCajaRequest request, String usuario) {
        Usuario cajero = usuarioRepository.findByUsername(usuario)
                .orElseThrow(() -> new BusinessException("Usuario cajero no identificado en el sistema: " + usuario));

        pagoValidator.validarAperturaSesion(request.getCajaId(), cajero.getId());

        CajaMunicipal caja = cajaRepository.findById(request.getCajaId())
                .orElseThrow(() -> new ResourceNotFoundException("Caja no encontrada"));

        Long seq = sesionRepository.obtenerSiguienteValorSecuencia();
        int anio = LocalDate.now().getYear();
        String codigoSesion = String.format("SES-%d-%05d", anio, seq);

        BigDecimal montoApertura = request.getMontoApertura() != null ?
                request.getMontoApertura().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        SesionCajaMunicipal sesion = SesionCajaMunicipal.builder()
                .codigoSesion(codigoSesion)
                .caja(caja)
                .cajero(cajero)
                .fechaApertura(OffsetDateTime.now())
                .montoApertura(montoApertura)
                .montoCierreCalculado(montoApertura)
                .diferencia(BigDecimal.ZERO)
                .totalRecaudadoEfectivo(BigDecimal.ZERO)
                .totalRecaudadoOtrosMedios(BigDecimal.ZERO)
                .totalRecaudadoGeneral(BigDecimal.ZERO)
                .totalOperaciones(0)
                .estado(EstadoSesionCaja.ABIERTA)
                .observaciones(request.getObservaciones())
                .build();

        sesion.setCreatedBy(usuario);
        SesionCajaMunicipal guardada = sesionRepository.save(sesion);
        log.info("Sesión de caja aperturada: {} para caja {} por cajero {}", codigoSesion, caja.getCodigo(), usuario);
        return mapearSesionAResponse(guardada);
    }

    @Override
    @Transactional
    public SesionCajaResponse cerrarSesion(UUID sesionId, CerrarSesionCajaRequest request, String usuario) {
        SesionCajaMunicipal sesion = sesionRepository.findById(sesionId)
                .orElseThrow(() -> new ResourceNotFoundException("Sesión de caja no encontrada: " + sesionId));

        pagoValidator.validarCierreSesion(sesion);

        // Recalcular recaudación real acumulada
        BigDecimal totalEfectivo = pagoRepository.calcularEfectivoRecaudadoEnSesion(sesionId);
        BigDecimal totalOtros = pagoRepository.calcularOtrosMediosRecaudadoEnSesion(sesionId);
        int operaciones = pagoRepository.countOperacionesEnSesion(sesionId);

        sesion.setTotalRecaudadoEfectivo(totalEfectivo);
        sesion.setTotalRecaudadoOtrosMedios(totalOtros);
        sesion.setTotalRecaudadoGeneral(totalEfectivo.add(totalOtros));
        sesion.setTotalOperaciones(operaciones);

        // El monto calculado esperado al cierre físico es: Fondo de Apertura + Efectivo Recaudado
        BigDecimal esperadoEfectivo = sesion.getMontoApertura().add(totalEfectivo);
        sesion.setMontoCierreCalculado(esperadoEfectivo);

        BigDecimal declarado = request.getMontoCierreDeclarado().setScale(2, RoundingMode.HALF_UP);
        sesion.setMontoCierreDeclarado(declarado);

        BigDecimal diferencia = declarado.subtract(esperadoEfectivo);
        sesion.setDiferencia(diferencia);
        sesion.setFechaCierre(OffsetDateTime.now());

        if (diferencia.compareTo(BigDecimal.ZERO) == 0) {
            sesion.setEstado(EstadoSesionCaja.CERRADA);
        } else {
            sesion.setEstado(EstadoSesionCaja.OBSERVADA);
            sesion.setJustificacionDiferencia(request.getJustificacionDiferencia());
        }

        if (request.getObservaciones() != null) {
            sesion.setObservaciones(sesion.getObservaciones() != null ?
                    sesion.getObservaciones() + " | Cierre: " + request.getObservaciones() : request.getObservaciones());
        }

        sesion.setUpdatedBy(usuario);
        SesionCajaMunicipal guardada = sesionRepository.save(sesion);

        // Generar acta de conciliación inicial automáticamente
        Long seqConc = conciliacionRepository.obtenerSiguienteValorSecuencia();
        int anio = LocalDate.now().getYear();
        String codigoConc = String.format("CONC-%d-%05d", anio, seqConc);

        Usuario responsable = usuarioRepository.findByUsername(usuario).orElse(null);

        ConciliacionCajaMunicipal conciliacion = ConciliacionCajaMunicipal.builder()
                .codigoConciliacion(codigoConc)
                .sesionCaja(guardada)
                .fechaConciliacion(OffsetDateTime.now())
                .importeEsperado(esperadoEfectivo)
                .importeVerificado(declarado)
                .diferencia(diferencia)
                .estado(diferencia.compareTo(BigDecimal.ZERO) == 0 ? EstadoConciliacionCaja.CONCILIADA : EstadoConciliacionCaja.CON_DIFERENCIAS)
                .responsable(responsable)
                .observaciones("Cierre automático de turno de caja. Diferencia: S/ " + diferencia)
                .desgloseDenominacionesJson(request.getDesgloseDenominacionesJson())
                .build();

        conciliacion.setCreatedBy(usuario);
        conciliacionRepository.save(conciliacion);

        log.info("Sesión de caja cerrada: {}. Esperado: S/ {}, Declarado: S/ {}, Diferencia: S/ {}",
                guardada.getCodigoSesion(), esperadoEfectivo, declarado, diferencia);

        return mapearSesionAResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public SesionCajaResponse obtenerSesionPorId(UUID sesionId) {
        SesionCajaMunicipal sesion = sesionRepository.findById(sesionId)
                .orElseThrow(() -> new ResourceNotFoundException("Sesión de caja no encontrada: " + sesionId));
        return mapearSesionAResponse(sesion);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SesionCajaResponse> obtenerSesionActivaPorCaja(UUID cajaId) {
        return sesionRepository.findSesionActivaPorCaja(cajaId)
                .map(this::mapearSesionAResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SesionCajaResponse> obtenerSesionActivaPorCajero(Long cajeroId) {
        return sesionRepository.findSesionActivaPorCajero(cajeroId)
                .map(this::mapearSesionAResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SesionCajaResponse> listarSesiones(String busqueda, UUID cajaId, Long cajeroId, EstadoSesionCaja estado, OffsetDateTime fechaDesde, OffsetDateTime fechaHasta, Pageable pageable) {
        return sesionRepository.findAll(SesionCajaSpecification.conFiltros(busqueda, cajaId, cajeroId, estado, fechaDesde, fechaHasta), pageable)
                .map(this::mapearSesionAResponse);
    }

    @Override
    @Transactional
    public ConciliacionCajaResponse conciliarSesion(ConciliarSesionRequest request, String usuario) {
        SesionCajaMunicipal sesion = sesionRepository.findById(request.getSesionCajaId())
                .orElseThrow(() -> new ResourceNotFoundException("Sesión de caja no encontrada: " + request.getSesionCajaId()));

        Usuario responsable = usuarioRepository.findByUsername(usuario)
                .orElseThrow(() -> new BusinessException("Usuario no reconocido"));

        BigDecimal esperado = sesion.getMontoCierreCalculado();
        BigDecimal verificado = request.getImporteVerificado().setScale(2, RoundingMode.HALF_UP);
        BigDecimal diferencia = verificado.subtract(esperado);

        Long seq = conciliacionRepository.obtenerSiguienteValorSecuencia();
        int anio = LocalDate.now().getYear();
        String codigo = String.format("CONC-%d-%05d", anio, seq);

        ConciliacionCajaMunicipal conc = ConciliacionCajaMunicipal.builder()
                .codigoConciliacion(codigo)
                .sesionCaja(sesion)
                .fechaConciliacion(OffsetDateTime.now())
                .importeEsperado(esperado)
                .importeVerificado(verificado)
                .diferencia(diferencia)
                .estado(diferencia.compareTo(BigDecimal.ZERO) == 0 ? EstadoConciliacionCaja.CONCILIADA : EstadoConciliacionCaja.CON_DIFERENCIAS)
                .responsable(responsable)
                .observaciones(request.getObservaciones())
                .desgloseDenominacionesJson(request.getDesgloseDenominacionesJson())
                .build();

        conc.setCreatedBy(usuario);
        ConciliacionCajaMunicipal guardada = conciliacionRepository.save(conc);
        log.info("Conciliación ejecutada: {} para sesión {}", codigo, sesion.getCodigoSesion());
        return mapearConciliacionAResponse(guardada);
    }

    @Override
    @Transactional
    public ConciliacionCajaResponse aprobarConciliacion(UUID conciliacionId, String observaciones, String usuario) {
        ConciliacionCajaMunicipal conc = conciliacionRepository.findById(conciliacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Conciliación no encontrada: " + conciliacionId));

        conc.setEstado(EstadoConciliacionCaja.CONCILIADA);
        if (observaciones != null) {
            conc.setObservaciones(conc.getObservaciones() != null ?
                    conc.getObservaciones() + " | Aprobación: " + observaciones : observaciones);
        }
        conc.setUpdatedBy(usuario);
        log.info("Conciliación {} aprobada formalmente por {}", conc.getCodigoConciliacion(), usuario);
        return mapearConciliacionAResponse(conciliacionRepository.save(conc));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConciliacionCajaResponse> listarConciliaciones(String busqueda, UUID sesionCajaId, EstadoConciliacionCaja estado, OffsetDateTime fechaDesde, OffsetDateTime fechaHasta, Pageable pageable) {
        return conciliacionRepository.findAll(ConciliacionSpecification.conFiltros(busqueda, sesionCajaId, estado, fechaDesde, fechaHasta), pageable)
                .map(this::mapearConciliacionAResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ConciliacionCajaResponse obtenerConciliacionPorId(UUID id) {
        ConciliacionCajaMunicipal conc = conciliacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conciliación no encontrada: " + id));
        return mapearConciliacionAResponse(conc);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardCajaResponse obtenerMetricasDashboard() {
        OffsetDateTime inicioHoy = LocalDate.now().atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
        OffsetDateTime finHoy = LocalDate.now().atTime(23, 59, 59).atOffset(OffsetDateTime.now().getOffset());

        return DashboardCajaResponse.builder()
                .cajasActivas(cajaRepository.countCajasActivas())
                .sesionesAbiertas(sesionRepository.countSesionesAbiertas())
                .pagosConfirmados(pagoRepository.countPagosConfirmados())
                .pagosPendientes(pagoRepository.countPagosPendientes())
                .recaudacionPeriodo(pagoRepository.calcularRecaudacionPeriodo(inicioHoy, finHoy))
                .efectivoRecaudado(pagoRepository.calcularRecaudacionPorMedio(MedioPagoTributario.EFECTIVO_CAJA, inicioHoy, finHoy))
                .operacionesNoConciliadas(conciliacionRepository.countPendientes())
                .diferenciasArqueo(sesionRepository.calcularTotalDiferencias())
                .build();
    }

    private CajaResponse mapearCajaAResponse(CajaMunicipal caja) {
        Optional<SesionCajaMunicipal> sesionActiva = sesionRepository.findSesionActivaPorCaja(caja.getId());

        return CajaResponse.builder()
                .id(caja.getId())
                .codigo(caja.getCodigo())
                .nombre(caja.getNombre())
                .oficinaId(caja.getOficinaId())
                .ubicacion(caja.getUbicacion())
                .cajeroAsignadoId(caja.getCajeroAsignado() != null ? caja.getCajeroAsignado().getId() : null)
                .cajeroAsignadoNombre(caja.getCajeroAsignado() != null ?
                        caja.getCajeroAsignado().getNombreCompleto() : "Sin asignar")
                .estado(caja.getEstado())
                .estadoDescripcion(caja.getEstado().getDescripcion())
                .estadoBadgeClass(caja.getEstado().getBadgeClass())
                .tieneSesionAbierta(sesionActiva.isPresent())
                .sesionActivaId(sesionActiva.map(SesionCajaMunicipal::getId).orElse(null))
                .sesionActivaCodigo(sesionActiva.map(SesionCajaMunicipal::getCodigoSesion).orElse(null))
                .observaciones(caja.getObservaciones())
                .build();
    }

    private SesionCajaResponse mapearSesionAResponse(SesionCajaMunicipal sesion) {
        return SesionCajaResponse.builder()
                .id(sesion.getId())
                .codigoSesion(sesion.getCodigoSesion())
                .cajaId(sesion.getCaja().getId())
                .cajaCodigo(sesion.getCaja().getCodigo())
                .cajaNombre(sesion.getCaja().getNombre())
                .cajeroId(sesion.getCajero().getId())
                .cajeroNombre(sesion.getCajero().getNombreCompleto())
                .fechaApertura(sesion.getFechaApertura())
                .fechaCierre(sesion.getFechaCierre())
                .montoApertura(sesion.getMontoApertura())
                .montoCierreDeclarado(sesion.getMontoCierreDeclarado())
                .montoCierreCalculado(sesion.getMontoCierreCalculado())
                .diferencia(sesion.getDiferencia())
                .totalRecaudadoEfectivo(sesion.getTotalRecaudadoEfectivo())
                .totalRecaudadoOtrosMedios(sesion.getTotalRecaudadoOtrosMedios())
                .totalRecaudadoGeneral(sesion.getTotalRecaudadoGeneral())
                .totalOperaciones(sesion.getTotalOperaciones())
                .estado(sesion.getEstado())
                .estadoDescripcion(sesion.getEstado().getDescripcion())
                .estadoBadgeClass(sesion.getEstado().getBadgeClass())
                .observaciones(sesion.getObservaciones())
                .justificacionDiferencia(sesion.getJustificacionDiferencia())
                .supervisorNombre(sesion.getSupervisor() != null ?
                        sesion.getSupervisor().getNombreCompleto() : null)
                .build();
    }

    private ConciliacionCajaResponse mapearConciliacionAResponse(ConciliacionCajaMunicipal c) {
        return ConciliacionCajaResponse.builder()
                .id(c.getId())
                .codigoConciliacion(c.getCodigoConciliacion())
                .sesionCajaId(c.getSesionCaja().getId())
                .sesionCajaCodigo(c.getSesionCaja().getCodigoSesion())
                .cajaNombre(c.getSesionCaja().getCaja().getNombre())
                .cajeroNombre(c.getSesionCaja().getCajero().getNombreCompleto())
                .fechaConciliacion(c.getFechaConciliacion())
                .importeEsperado(c.getImporteEsperado())
                .importeVerificado(c.getImporteVerificado())
                .diferencia(c.getDiferencia())
                .estado(c.getEstado())
                .estadoDescripcion(c.getEstado().getDescripcion())
                .estadoBadgeClass(c.getEstado().getBadgeClass())
                .responsableNombre(c.getResponsable() != null ?
                        c.getResponsable().getNombreCompleto() : "Sistema")
                .observaciones(c.getObservaciones())
                .desgloseDenominacionesJson(c.getDesgloseDenominacionesJson())
                .build();
    }
}
