package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.administracion.service.AuditoriaAdministrativaService;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoCuota;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoObligacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoMovimientoCuentaCorriente;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ObligacionMercadoService {

    private final ObligacionMercadoRepository obligacionRepository;
    private final CuotaObligacionMercadoRepository cuotaRepository;
    private final ComercianteRepository comercianteRepository;
    private final PuestoComercialRepository puestoRepository;
    private final AsignacionPuestoRepository asignacionRepository;
    private final ConceptoCobroMercadoRepository conceptoRepository;
    private final TarifaMercadoRepository tarifaRepository;
    private final MovimientoCuentaCorrienteMercadoRepository movimientoCcRepository;
    private final AplicacionPagoMercadoRepository aplicacionRepository;
    private final AuditoriaAdministrativaService auditoriaService;

    public ObligacionMercadoService(ObligacionMercadoRepository obligacionRepository,
                                   CuotaObligacionMercadoRepository cuotaRepository,
                                   ComercianteRepository comercianteRepository,
                                   PuestoComercialRepository puestoRepository,
                                   AsignacionPuestoRepository asignacionRepository,
                                   ConceptoCobroMercadoRepository conceptoRepository,
                                   TarifaMercadoRepository tarifaRepository,
                                   MovimientoCuentaCorrienteMercadoRepository movimientoCcRepository,
                                   AplicacionPagoMercadoRepository aplicacionRepository,
                                   AuditoriaAdministrativaService auditoriaService) {
        this.obligacionRepository = obligacionRepository;
        this.cuotaRepository = cuotaRepository;
        this.comercianteRepository = comercianteRepository;
        this.puestoRepository = puestoRepository;
        this.asignacionRepository = asignacionRepository;
        this.conceptoRepository = conceptoRepository;
        this.tarifaRepository = tarifaRepository;
        this.movimientoCcRepository = movimientoCcRepository;
        this.aplicacionRepository = aplicacionRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public ObligacionResponse crear(ObligacionCreateRequest request, String username) {
        if (request.getComercianteId() == null) {
            throw new BusinessException("El comerciante es obligatorio para emitir la obligación comercial.");
        }

        Comerciante comerciante = comercianteRepository.findById(request.getComercianteId())
                .orElseThrow(() -> new ResourceNotFoundException("Comerciante no encontrado: " + request.getComercianteId()));

        ConceptoCobroMercado concepto = conceptoRepository.findById(request.getConceptoId())
                .orElseThrow(() -> new ResourceNotFoundException("Concepto de cobro no encontrado: " + request.getConceptoId()));

        if (!"ACTIVO".equalsIgnoreCase(concepto.getEstado())) {
            throw new BusinessException("El concepto de cobro no está activo.");
        }

        AsignacionPuesto asignacion = null;
        PuestoComercial puesto = null;

        if (request.getAsignacionId() != null) {
            asignacion = asignacionRepository.findByIdCompleto(request.getAsignacionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada: " + request.getAsignacionId()));
            puesto = asignacion.getPuesto();
            comerciante = asignacion.getComerciante();
        } else if (request.getPuestoId() != null) {
            puesto = puestoRepository.findById(request.getPuestoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Puesto no encontrado: " + request.getPuestoId()));
        }

        if (concepto.isRequiereAsignacion() && asignacion == null && puesto == null) {
            throw new BusinessException("El concepto '" + concepto.getNombre() + "' requiere estar asociado a un puesto o asignación comercial.");
        }

        // Control de duplicados en el mismo período
        if (request.getPeriodoInicio() != null && request.getPeriodoFin() != null) {
            if (request.getPeriodoFin().isBefore(request.getPeriodoInicio())) {
                throw new BusinessException("La fecha de fin de período no puede ser anterior a la de inicio.");
            }

            boolean existeDuplicado = obligacionRepository.existsDuplicada(
                    comerciante.getId(),
                    puesto != null ? puesto.getId() : null,
                    concepto.getId(),
                    request.getPeriodoInicio(),
                    request.getPeriodoFin()
            );
            if (existeDuplicado) {
                throw new BusinessException("Ya existe una obligación comercial registrada para este comerciante, puesto y período.");
            }
        }

        // Determinar tarifa aplicable si no fue especificado el monto o se pasó tarifaId
        TarifaMercado tarifa = null;
        BigDecimal montoOriginal = request.getMontoOriginal();

        if (request.getTarifaId() != null) {
            tarifa = tarifaRepository.findById(request.getTarifaId()).orElse(null);
        } else if (montoOriginal == null) {
            UUID mercadoId = puesto != null && puesto.getMercado() != null ? puesto.getMercado().getId() : null;
            Long tipoPuestoId = puesto != null && puesto.getTipoPuesto() != null ? puesto.getTipoPuesto().getId() : null;
            tarifa = tarifaRepository.buscarTarifaAplicable(concepto.getId(), mercadoId, tipoPuestoId, LocalDate.now()).orElse(null);
            if (tarifa != null) {
                montoOriginal = tarifa.getMonto();
            }
        }

        if (montoOriginal == null || montoOriginal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El monto de la obligación económica debe ser mayor a 0.00.");
        }

        // Generar correlativo oficial
        Long seq = obligacionRepository.obtenerSiguienteCorrelativo();
        int anio = Year.now().getValue();
        String codigoObligacion = String.format("OBL-%d-%06d", anio, seq);

        ObligacionMercado obligacion = ObligacionMercado.builder()
                .codigoObligacion(codigoObligacion)
                .comerciante(comerciante)
                .asignacion(asignacion)
                .puesto(puesto)
                .concepto(concepto)
                .tarifa(tarifa)
                .periodoInicio(request.getPeriodoInicio())
                .periodoFin(request.getPeriodoFin())
                .fechaEmision(request.getFechaEmision() != null ? request.getFechaEmision() : LocalDate.now())
                .fechaVencimiento(request.getFechaVencimiento() != null ? request.getFechaVencimiento() : LocalDate.now().plusDays(30))
                .montoOriginal(montoOriginal)
                .montoPagado(BigDecimal.ZERO)
                .saldoPendiente(montoOriginal)
                .moneda(request.getMoneda() != null ? request.getMoneda() : "PEN")
                .estado(EstadoObligacion.PENDIENTE)
                .expedienteId(request.getExpedienteId())
                .documentoSustentoId(request.getDocumentoSustentoId())
                .observaciones(request.getObservaciones())
                .build();

        // Generar cuotas programadas
        int nCuotas = request.getNumeroCuotas() != null && request.getNumeroCuotas() > 1 ? request.getNumeroCuotas() : 1;
        List<CuotaObligacionMercado> cuotas = new ArrayList<>();

        BigDecimal cuotaBase = montoOriginal.divide(BigDecimal.valueOf(nCuotas), 2, RoundingMode.DOWN);
        BigDecimal sumaCuotas = BigDecimal.ZERO;

        for (int i = 1; i <= nCuotas; i++) {
            BigDecimal montoCuota = (i == nCuotas) ? montoOriginal.subtract(sumaCuotas) : cuotaBase;
            sumaCuotas = sumaCuotas.add(montoCuota);

            LocalDate vencimientoCuota = obligacion.getFechaVencimiento();
            if (nCuotas > 1 && obligacion.getPeriodoInicio() != null) {
                vencimientoCuota = obligacion.getPeriodoInicio().plusMonths(i - 1).plusDays(25);
            }

            CuotaObligacionMercado cuota = CuotaObligacionMercado.builder()
                    .obligacion(obligacion)
                    .numeroCuota(i)
                    .fechaVencimiento(vencimientoCuota)
                    .montoProgramado(montoCuota)
                    .montoPagado(BigDecimal.ZERO)
                    .saldo(montoCuota)
                    .estado(EstadoCuota.PENDIENTE)
                    .build();
            cuotas.add(cuota);
        }
        obligacion.setCuotas(cuotas);

        ObligacionMercado guardada = obligacionRepository.save(obligacion);

        // Registrar movimiento de CARGO en la cuenta corriente del comerciante
        BigDecimal deudaActual = obligacionRepository.sumarDeudaTotalComerciante(comerciante.getId());
        movimientoCcRepository.save(MovimientoCuentaCorrienteMercado.builder()
                .comerciante(comerciante)
                .puesto(puesto)
                .tipoMovimiento(TipoMovimientoCuentaCorriente.CARGO)
                .concepto("Emisión de " + guardada.getCodigoObligacion() + ": " + concepto.getNombre())
                .monto(guardada.getMontoOriginal())
                .saldoResultante(deudaActual)
                .referenciaId(guardada.getId())
                .tipoReferencia("OBLIGACION")
                .fechaMovimiento(OffsetDateTime.now())
                .usuario(username != null ? username : "sistema")
                .build());

        auditoriaService.registrar("EMITIR_OBLIGACION_MERCADO", "mercados_obligaciones", guardada.getId(),
                "EXITO", "Obligación emitida: " + guardada.getCodigoObligacion() + " por S/ " + guardada.getMontoOriginal());

        return toResponse(guardada);
    }

    @Transactional
    public void anular(UUID id, String motivo, String username) {
        ObligacionMercado obligacion = obligacionRepository.findByIdConRelaciones(id)
                .orElseThrow(() -> new ResourceNotFoundException("Obligación no encontrada: " + id));

        if (obligacion.getMontoPagado().compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException("No se puede anular una obligación que ya tiene pagos parciales o totales registrados. Debe reversar los pagos primero.");
        }

        if (obligacion.getEstado() == EstadoObligacion.ANULADA) {
            throw new BusinessException("La obligación ya se encuentra anulada.");
        }

        obligacion.setEstado(EstadoObligacion.ANULADA);
        obligacion.setSaldoPendiente(BigDecimal.ZERO);
        obligacion.setObservaciones((obligacion.getObservaciones() != null ? obligacion.getObservaciones() + " | " : "") + "ANULADA: " + motivo);

        obligacionRepository.save(obligacion);

        // Movimiento compensatorio de anulación en cuenta corriente
        BigDecimal deudaRestante = obligacionRepository.sumarDeudaTotalComerciante(obligacion.getComerciante().getId());
        movimientoCcRepository.save(MovimientoCuentaCorrienteMercado.builder()
                .comerciante(obligacion.getComerciante())
                .puesto(obligacion.getPuesto())
                .tipoMovimiento(TipoMovimientoCuentaCorriente.REVERSO)
                .concepto("Anulación de obligación " + obligacion.getCodigoObligacion() + ": " + motivo)
                .monto(obligacion.getMontoOriginal())
                .saldoResultante(deudaRestante)
                .referenciaId(obligacion.getId())
                .tipoReferencia("OBLIGACION_ANULADA")
                .fechaMovimiento(OffsetDateTime.now())
                .usuario(username != null ? username : "sistema")
                .build());

        auditoriaService.registrar("ANULAR_OBLIGACION_MERCADO", "mercados_obligaciones", obligacion.getId(),
                "EXITO", "Obligación anulada: " + obligacion.getCodigoObligacion() + ". Motivo: " + motivo);
    }

    @Transactional(readOnly = true)
    public ObligacionDetailResponse obtenerDetalle(UUID id) {
        ObligacionMercado o = obligacionRepository.findByIdConRelaciones(id)
                .orElseThrow(() -> new ResourceNotFoundException("Obligación no encontrada: " + id));

        ObligacionDetailResponse detail = new ObligacionDetailResponse();
        copyPropertiesToResponse(o, detail);

        List<CuotaObligacionMercado> cuotas = cuotaRepository.findByObligacionIdOrderByNumeroCuotaAsc(o.getId());
        detail.setCuotas(cuotas.stream().map(c -> CuotaResponse.builder()
                .id(c.getId())
                .numeroCuota(c.getNumeroCuota())
                .fechaVencimiento(c.getFechaVencimiento())
                .montoProgramado(c.getMontoProgramado())
                .montoPagado(c.getMontoPagado())
                .saldo(c.getSaldo())
                .estado(c.getEstado())
                .estadoDescripcion(c.getEstado() != null ? c.getEstado().getDescripcion() : null)
                .build()).collect(Collectors.toList()));

        List<AplicacionPagoMercado> aplicaciones = aplicacionRepository.findByObligacionId(o.getId());
        detail.setAplicaciones(aplicaciones.stream().map(a -> AplicacionResponse.builder()
                .id(a.getId())
                .pagoId(a.getPago().getId())
                .codigoPago(a.getPago().getCodigoPago())
                .obligacionId(o.getId())
                .codigoObligacion(o.getCodigoObligacion())
                .conceptoNombre(o.getConcepto().getNombre())
                .cuotaId(a.getCuota() != null ? a.getCuota().getId() : null)
                .numeroCuota(a.getCuota() != null ? a.getCuota().getNumeroCuota() : null)
                .montoAplicado(a.getMontoAplicado())
                .fechaAplicacion(a.getFechaAplicacion())
                .estado(a.getEstado())
                .build()).collect(Collectors.toList()));

        return detail;
    }

    @Transactional(readOnly = true)
    public Page<ObligacionResponse> buscarPaginado(UUID mercadoId, UUID comercianteId, UUID puestoId, UUID conceptoId,
                                                  String estado, LocalDate fechaDesde, LocalDate fechaHasta, String q, Pageable pageable) {
        Pageable unsortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        return obligacionRepository.buscarPaginado(mercadoId, comercianteId, puestoId, conceptoId, estado, fechaDesde, fechaHasta, q, unsortedPageable)
                .map(this::toResponse);
    }

    @Transactional
    public int actualizarObligacionesVencidas() {
        List<ObligacionMercado> vencidas = obligacionRepository.buscarVencidas(LocalDate.now());
        for (ObligacionMercado o : vencidas) {
            o.setEstado(EstadoObligacion.VENCIDA);
            obligacionRepository.save(o);
        }
        return vencidas.size();
    }

    public ObligacionResponse toResponse(ObligacionMercado o) {
        ObligacionResponse resp = new ObligacionResponse();
        copyPropertiesToResponse(o, resp);
        return resp;
    }

    private void copyPropertiesToResponse(ObligacionMercado o, ObligacionResponse resp) {
        resp.setId(o.getId());
        resp.setCodigoObligacion(o.getCodigoObligacion());
        if (o.getComerciante() != null) {
            resp.setComercianteId(o.getComerciante().getId());
            if (o.getComerciante().getPersona() != null) {
                resp.setComercianteNombre(o.getComerciante().getPersona().getNombreCompleto());
                resp.setComercianteDocumento(o.getComerciante().getPersona().getNumeroDocumento());
            } else if (o.getComerciante().getPersonaJuridica() != null) {
                resp.setComercianteNombre(o.getComerciante().getPersonaJuridica().getRazonSocial());
                resp.setComercianteDocumento(o.getComerciante().getPersonaJuridica().getRuc());
            } else {
                resp.setComercianteNombre(o.getComerciante().getCodigoComerciante() != null ? o.getComerciante().getCodigoComerciante() : "-");
                resp.setComercianteDocumento("-");
            }
        } else {
            resp.setComercianteNombre("-");
            resp.setComercianteDocumento("-");
        }
        resp.setPuestoId(o.getPuesto() != null ? o.getPuesto().getId() : null);
        resp.setPuestoNumero(o.getPuesto() != null ? o.getPuesto().getNumero() : "-");
        resp.setMercadoNombre(o.getPuesto() != null && o.getPuesto().getMercado() != null ? o.getPuesto().getMercado().getNombre() : "-");
        resp.setAsignacionId(o.getAsignacion() != null ? o.getAsignacion().getId() : null);
        resp.setConceptoId(o.getConcepto().getId());
        resp.setConceptoCodigo(o.getConcepto().getCodigo());
        resp.setConceptoNombre(o.getConcepto().getNombre());
        resp.setPeriodoInicio(o.getPeriodoInicio());
        resp.setPeriodoFin(o.getPeriodoFin());
        resp.setFechaEmision(o.getFechaEmision());
        resp.setFechaVencimiento(o.getFechaVencimiento());
        resp.setMontoOriginal(o.getMontoOriginal());
        resp.setMontoPagado(o.getMontoPagado());
        resp.setSaldoPendiente(o.getSaldoPendiente());
        resp.setMoneda(o.getMoneda());
        resp.setEstado(o.getEstado());
        resp.setEstadoDescripcion(o.getEstado() != null ? o.getEstado().getDescripcion() : null);
        resp.setObservaciones(o.getObservaciones());
    }
}
