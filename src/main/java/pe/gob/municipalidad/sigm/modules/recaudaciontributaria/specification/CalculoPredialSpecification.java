package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.CalculoPredialFilterDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.CalculoImpuestoPredial;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class CalculoPredialSpecification {

    public static Specification<CalculoImpuestoPredial> conFiltro(CalculoPredialFilterDto filtro) {
        return (root, query, cb) -> {
            if (filtro == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            if (filtro.getQuery() != null && !filtro.getQuery().trim().isEmpty()) {
                String pattern = "%" + filtro.getQuery().trim().toUpperCase() + "%";
                Predicate porCodigo = cb.like(cb.upper(root.get("codigoCalculo")), pattern);
                Predicate porContribuyente = cb.like(cb.upper(root.get("contribuyente").get("nombreRazonSocial")), pattern);
                Predicate porDoc = cb.like(root.get("contribuyente").get("numeroDocumento"), pattern);
                Predicate porCodContrib = cb.like(cb.upper(root.get("contribuyente").get("codigoContribuyente")), pattern);
                predicates.add(cb.or(porCodigo, porContribuyente, porDoc, porCodContrib));
            }

            if (filtro.getEjercicioFiscal() != null) {
                predicates.add(cb.equal(root.get("periodo").get("anio"), filtro.getEjercicioFiscal()));
            }

            if (filtro.getPeriodoId() != null) {
                predicates.add(cb.equal(root.get("periodo").get("id"), filtro.getPeriodoId()));
            }

            if (filtro.getContribuyenteId() != null) {
                predicates.add(cb.equal(root.get("contribuyente").get("id"), filtro.getContribuyenteId()));
            }

            if (filtro.getEstado() != null) {
                predicates.add(cb.equal(root.get("estado"), filtro.getEstado()));
            }

            if (filtro.getTipoCalculo() != null) {
                predicates.add(cb.equal(root.get("tipoCalculo"), filtro.getTipoCalculo()));
            }

            if (filtro.getFechaDesde() != null) {
                OffsetDateTime desde = filtro.getFechaDesde().atStartOfDay().atOffset(ZoneOffset.ofHours(-5));
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaCalculo"), desde));
            }

            if (filtro.getFechaHasta() != null) {
                OffsetDateTime hasta = filtro.getFechaHasta().atTime(LocalTime.MAX).atOffset(ZoneOffset.ofHours(-5));
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaCalculo"), hasta));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
