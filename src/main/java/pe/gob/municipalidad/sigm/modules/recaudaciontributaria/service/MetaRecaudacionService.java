package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.reportes.ReporteTributarioDtos.CrearMetaRecaudacionRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.reportes.ReporteTributarioDtos.MetaRecaudacionItemDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ConceptoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.MetaRecaudacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoMetaRecaudacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ConceptoTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.MetaRecaudacionTributariaRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.PagoTributarioRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio para la configuración, versionado y cálculo de cumplimiento de metas de recaudación tributaria.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetaRecaudacionService {

    private final MetaRecaudacionTributariaRepository metaRepository;
    private final ConceptoTributarioRepository conceptoRepository;
    private final PagoTributarioRepository pagoRepository;

    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");

    @Transactional
    public MetaRecaudacionItemDto crearMeta(CrearMetaRecaudacionRequest request, Usuario autor) {
        if (request.getEjercicio() == null || request.getEjercicio() < 2000) {
            throw new BusinessException("El ejercicio fiscal es obligatorio y debe ser válido.");
        }
        if (request.getImporteMeta() == null || request.getImporteMeta().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El importe de la meta debe ser estrictamente positivo.");
        }

        ConceptoTributario concepto = null;
        if (request.getConceptoId() != null) {
            concepto = conceptoRepository.findById(request.getConceptoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Concepto tributario no encontrado."));
        }

        MetaRecaudacionTributaria meta = MetaRecaudacionTributaria.builder()
                .ejercicio(request.getEjercicio())
                .periodo(request.getPeriodo() != null ? request.getPeriodo() : 0)
                .concepto(concepto)
                .importeMeta(request.getImporteMeta())
                .estado(EstadoMetaRecaudacion.BORRADOR)
                .aprobado(false)
                .observacion(request.getObservacion())
                .build();
        meta.setCreatedBy(autor != null ? autor.getUsername() : "SISTEMA");

        MetaRecaudacionTributaria guardada = metaRepository.save(meta);
        log.info("Meta de recaudación creada para el ejercicio {} con importe {}", meta.getEjercicio(), meta.getImporteMeta());
        return convertirADto(guardada);
    }

    @Transactional
    public MetaRecaudacionItemDto aprobarMeta(UUID metaId, Usuario aprobador) {
        MetaRecaudacionTributaria meta = metaRepository.findById(metaId)
                .orElseThrow(() -> new ResourceNotFoundException("Meta de recaudación no encontrada."));

        meta.setAprobado(true);
        meta.setAprobadoPor(aprobador);
        meta.setFechaAprobacion(OffsetDateTime.now(ZONA_LIMA));
        meta.setEstado(EstadoMetaRecaudacion.VIGENTE);
        meta.setUpdatedBy(aprobador != null ? aprobador.getUsername() : "SISTEMA");

        MetaRecaudacionTributaria guardada = metaRepository.save(meta);
        log.info("Meta de recaudación '{}' aprobada por usuario '{}'", metaId, aprobador != null ? aprobador.getUsername() : "SISTEMA");
        return convertirADto(guardada);
    }

    @Transactional(readOnly = true)
    public Page<MetaRecaudacionItemDto> listarMetas(Integer ejercicio, EstadoMetaRecaudacion estado, Pageable pageable) {
        return metaRepository.buscarMetas(ejercicio, estado, pageable).map(this::convertirADto);
    }

    public MetaRecaudacionItemDto convertirADto(MetaRecaudacionTributaria m) {
        String periodoNombre = "Anual Global";
        OffsetDateTime desde;
        OffsetDateTime hasta;

        if (m.getPeriodo() != null && m.getPeriodo() >= 1 && m.getPeriodo() <= 12) {
            periodoNombre = Month.of(m.getPeriodo()).getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es")).toUpperCase();
            LocalDate pDia = LocalDate.of(m.getEjercicio(), m.getPeriodo(), 1);
            LocalDate uDia = pDia.plusMonths(1).minusDays(1);
            desde = pDia.atStartOfDay(ZONA_LIMA).toOffsetDateTime();
            hasta = uDia.atTime(23, 59, 59).atZone(ZONA_LIMA).toOffsetDateTime();
        } else {
            desde = LocalDate.of(m.getEjercicio(), 1, 1).atStartOfDay(ZONA_LIMA).toOffsetDateTime();
            hasta = LocalDate.of(m.getEjercicio(), 12, 31).atTime(23, 59, 59).atZone(ZONA_LIMA).toOffsetDateTime();
        }

        BigDecimal recaudado = pagoRepository.calcularRecaudacionPeriodo(desde, hasta);
        if (recaudado == null) recaudado = BigDecimal.ZERO;

        BigDecimal cumplimiento = BigDecimal.ZERO;
        if (m.getImporteMeta() != null && m.getImporteMeta().compareTo(BigDecimal.ZERO) > 0) {
            cumplimiento = recaudado.multiply(BigDecimal.valueOf(100)).divide(m.getImporteMeta(), 2, RoundingMode.HALF_UP);
        }

        return MetaRecaudacionItemDto.builder()
                .id(m.getId())
                .ejercicio(m.getEjercicio())
                .periodo(m.getPeriodo())
                .periodoNombre(periodoNombre)
                .conceptoNombre(m.getConcepto() != null ? m.getConcepto().getNombre() : "TODOS LOS CONCEPTOS")
                .importeMeta(m.getImporteMeta())
                .recaudadoReal(recaudado)
                .porcentajeCumplimiento(cumplimiento)
                .estado(m.getEstado())
                .observacion(m.getObservacion())
                .build();
    }
}

