package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.administracion.service.AuditoriaAdministrativaService;
import pe.gob.municipalidad.sigm.modules.mercados.dto.TarifaMercadoRequest;
import pe.gob.municipalidad.sigm.modules.mercados.dto.TarifaMercadoResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.ConceptoCobroMercado;
import pe.gob.municipalidad.sigm.modules.mercados.entity.MercadoMunicipal;
import pe.gob.municipalidad.sigm.modules.mercados.entity.TarifaMercado;
import pe.gob.municipalidad.sigm.modules.mercados.entity.TipoPuestoComercial;
import pe.gob.municipalidad.sigm.modules.mercados.repository.ConceptoCobroMercadoRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.MercadoMunicipalRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.TarifaMercadoRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.TipoPuestoComercialRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
public class TarifaMercadoService {

    private final TarifaMercadoRepository tarifaRepository;
    private final ConceptoCobroMercadoRepository conceptoRepository;
    private final MercadoMunicipalRepository mercadoRepository;
    private final TipoPuestoComercialRepository tipoPuestoRepository;
    private final AuditoriaAdministrativaService auditoriaService;

    public TarifaMercadoService(TarifaMercadoRepository tarifaRepository,
                                ConceptoCobroMercadoRepository conceptoRepository,
                                MercadoMunicipalRepository mercadoRepository,
                                TipoPuestoComercialRepository tipoPuestoRepository,
                                AuditoriaAdministrativaService auditoriaService) {
        this.tarifaRepository = tarifaRepository;
        this.conceptoRepository = conceptoRepository;
        this.mercadoRepository = mercadoRepository;
        this.tipoPuestoRepository = tipoPuestoRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public TarifaMercadoResponse crear(TarifaMercadoRequest request, String username) {
        if (request.getMonto() == null || request.getMonto().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("El monto de la tarifa debe ser mayor o igual a 0.00 y no puede ser negativo.");
        }

        if (request.getFechaFinVigencia() != null && request.getFechaFinVigencia().isBefore(request.getFechaInicioVigencia())) {
            throw new BusinessException("La fecha de fin de vigencia no puede ser anterior a la fecha de inicio.");
        }

        ConceptoCobroMercado concepto = conceptoRepository.findById(request.getConceptoId())
                .orElseThrow(() -> new ResourceNotFoundException("Concepto de cobro no encontrado: " + request.getConceptoId()));

        MercadoMunicipal mercado = null;
        if (request.getMercadoId() != null) {
            mercado = mercadoRepository.findById(request.getMercadoId()).orElse(null);
        }

        TipoPuestoComercial tipoPuesto = null;
        if (request.getTipoPuestoId() != null) {
            tipoPuesto = tipoPuestoRepository.findById(request.getTipoPuestoId()).orElse(null);
        }

        TarifaMercado tarifa = TarifaMercado.builder()
                .concepto(concepto)
                .mercado(mercado)
                .tipoPuesto(tipoPuesto)
                .monto(request.getMonto())
                .moneda(request.getMoneda() != null ? request.getMoneda() : "PEN")
                .fechaInicioVigencia(request.getFechaInicioVigencia())
                .fechaFinVigencia(request.getFechaFinVigencia())
                .estado(request.getEstado() != null ? request.getEstado() : "ACTIVA")
                .documentoSustentoId(request.getDocumentoSustentoId())
                .fuenteNormativa(request.getFuenteNormativa())
                .versionTarifa(1)
                .build();

        TarifaMercado guardada = tarifaRepository.save(tarifa);

        auditoriaService.registrar("CREAR_TARIFA_MERCADO", "mercados_tarifas", guardada.getId(),
                "EXITO", "Tarifa registrada para concepto " + concepto.getCodigo() + " con monto: S/ " + guardada.getMonto());

        return toResponse(guardada);
    }

    @Transactional
    public TarifaMercadoResponse actualizarConVersionamiento(UUID id, TarifaMercadoRequest request, String username) {
        TarifaMercado anterior = tarifaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarifa no encontrada: " + id));

        if (request.getMonto() == null || request.getMonto().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("El monto de la tarifa debe ser mayor o igual a 0.00.");
        }

        if (request.getFechaFinVigencia() != null && request.getFechaFinVigencia().isBefore(request.getFechaInicioVigencia())) {
            throw new BusinessException("La fecha de fin de vigencia no puede ser anterior a la fecha de inicio.");
        }

        // Marcar la versión previa como HISTORICA
        anterior.setEstado("HISTORICA");
        anterior.setFechaFinVigencia(request.getFechaInicioVigencia().minusDays(1));
        tarifaRepository.save(anterior);

        // Crear nueva versión
        TarifaMercado nueva = TarifaMercado.builder()
                .concepto(anterior.getConcepto())
                .mercado(anterior.getMercado())
                .tipoPuesto(anterior.getTipoPuesto())
                .monto(request.getMonto())
                .moneda(request.getMoneda() != null ? request.getMoneda() : "PEN")
                .fechaInicioVigencia(request.getFechaInicioVigencia())
                .fechaFinVigencia(request.getFechaFinVigencia())
                .estado("ACTIVA")
                .documentoSustentoId(request.getDocumentoSustentoId())
                .fuenteNormativa(request.getFuenteNormativa())
                .versionTarifa(anterior.getVersionTarifa() + 1)
                .build();

        TarifaMercado guardada = tarifaRepository.save(nueva);

        auditoriaService.registrar("ACTUALIZAR_TARIFA_MERCADO", "mercados_tarifas", guardada.getId(),
                "EXITO", "Nueva versión " + guardada.getVersionTarifa() + " de tarifa creada para concepto " + anterior.getConcepto().getCodigo());

        return toResponse(guardada);
    }

    @Transactional(readOnly = true)
    public TarifaMercadoResponse obtenerPorId(UUID id) {
        return tarifaRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Tarifa no encontrada: " + id));
    }

    @Transactional(readOnly = true)
    public TarifaMercadoResponse obtenerTarifaAplicable(UUID conceptoId, UUID mercadoId, Long tipoPuestoId, LocalDate fecha) {
        LocalDate fechaConsulta = fecha != null ? fecha : LocalDate.now();
        return tarifaRepository.buscarTarifaAplicable(conceptoId, mercadoId, tipoPuestoId, fechaConsulta)
                .map(this::toResponse)
                .orElseThrow(() -> new BusinessException("No se encontró una tarifa activa aplicable para los parámetros indicados en fecha " + fechaConsulta));
    }

    @Transactional(readOnly = true)
    public Page<TarifaMercadoResponse> buscarPaginado(UUID conceptoId, UUID mercadoId, String estado, Pageable pageable) {
        return tarifaRepository.buscarPaginado(conceptoId, mercadoId, estado, pageable)
                .map(this::toResponse);
    }

    public TarifaMercadoResponse toResponse(TarifaMercado t) {
        return TarifaMercadoResponse.builder()
                .id(t.getId())
                .conceptoId(t.getConcepto().getId())
                .conceptoCodigo(t.getConcepto().getCodigo())
                .conceptoNombre(t.getConcepto().getNombre())
                .mercadoId(t.getMercado() != null ? t.getMercado().getId() : null)
                .mercadoNombre(t.getMercado() != null ? t.getMercado().getNombre() : "TODOS")
                .tipoPuestoId(t.getTipoPuesto() != null ? t.getTipoPuesto().getId() : null)
                .tipoPuestoNombre(t.getTipoPuesto() != null ? t.getTipoPuesto().getNombre() : "TODOS")
                .monto(t.getMonto())
                .moneda(t.getMoneda())
                .fechaInicioVigencia(t.getFechaInicioVigencia())
                .fechaFinVigencia(t.getFechaFinVigencia())
                .estado(t.getEstado())
                .fuenteNormativa(t.getFuenteNormativa())
                .versionTarifa(t.getVersionTarifa())
                .build();
    }
}
