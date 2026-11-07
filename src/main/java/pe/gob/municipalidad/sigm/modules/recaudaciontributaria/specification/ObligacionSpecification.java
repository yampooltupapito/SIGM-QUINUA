package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.ObligacionFilterDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ObligacionTributaria;

import java.util.ArrayList;
import java.util.List;

public class ObligacionSpecification {

    public static Specification<ObligacionTributaria> conFiltros(ObligacionFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.conjunction();
            }

            if (filter.getQuery() != null && !filter.getQuery().trim().isEmpty()) {
                String term = "%" + filter.getQuery().trim().toLowerCase() + "%";
                Predicate porCodigo = cb.like(cb.lower(root.get("codigoObligacion")), term);
                Predicate porContribuyente = cb.like(cb.lower(root.get("contribuyente").get("razonSocialNombre")), term);
                Predicate porDocumento = cb.like(cb.lower(root.get("contribuyente").get("numeroDocumento")), term);
                Predicate porPredio = root.get("predio") != null ?
                        cb.like(cb.lower(root.get("predio").get("codigoPredio")), term) : cb.disjunction();

                predicates.add(cb.or(porCodigo, porContribuyente, porDocumento, porPredio));
            }

            if (filter.getCodigo() != null && !filter.getCodigo().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("codigoObligacion")), "%" + filter.getCodigo().trim().toLowerCase() + "%"));
            }

            if (filter.getContribuyenteId() != null) {
                predicates.add(cb.equal(root.get("contribuyente").get("id"), filter.getContribuyenteId()));
            }

            if (filter.getPredioId() != null) {
                predicates.add(cb.equal(root.get("predio").get("id"), filter.getPredioId()));
            }

            if (filter.getConceptoId() != null) {
                predicates.add(cb.equal(root.get("concepto").get("id"), filter.getConceptoId()));
            }

            if (filter.getPeriodoId() != null) {
                predicates.add(cb.equal(root.get("periodo").get("id"), filter.getPeriodoId()));
            }

            if (filter.getAnioPeriodo() != null) {
                predicates.add(cb.equal(root.get("periodo").get("anio"), filter.getAnioPeriodo()));
            }

            if (filter.getTipoDeterminacion() != null) {
                predicates.add(cb.equal(root.get("tipoDeterminacion"), filter.getTipoDeterminacion()));
            }

            if (filter.getEstado() != null) {
                predicates.add(cb.equal(root.get("estado"), filter.getEstado()));
            }

            if (filter.getEstadoFinanciero() != null) {
                predicates.add(cb.equal(root.get("estadoFinanciero"), filter.getEstadoFinanciero()));
            }

            if (filter.getEstadoExigibilidad() != null) {
                predicates.add(cb.equal(root.get("estadoExigibilidad"), filter.getEstadoExigibilidad()));
            }

            if (filter.getFechaDesde() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaDeterminacion"), filter.getFechaDesde()));
            }

            if (filter.getFechaHasta() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaDeterminacion"), filter.getFechaHasta()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
