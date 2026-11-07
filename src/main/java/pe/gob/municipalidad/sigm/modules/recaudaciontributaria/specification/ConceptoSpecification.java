package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.ConceptoFilterDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ConceptoTributario;

import java.util.ArrayList;
import java.util.List;

public class ConceptoSpecification {

    public static Specification<ConceptoTributario> conFiltro(ConceptoFilterDto filtro) {
        return (root, query, cb) -> {
            if (filtro == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            if (filtro.getQuery() != null && !filtro.getQuery().trim().isEmpty()) {
                String pattern = "%" + filtro.getQuery().trim().toUpperCase() + "%";
                Predicate porCodigo = cb.like(cb.upper(root.get("codigo")), pattern);
                Predicate porNombre = cb.like(cb.upper(root.get("nombre")), pattern);
                Predicate porDesc = cb.like(cb.upper(cb.coalesce(root.get("descripcion"), "")), pattern);
                predicates.add(cb.or(porCodigo, porNombre, porDesc));
            }

            if (filtro.getTipoConcepto() != null) {
                predicates.add(cb.equal(root.get("tipoConcepto"), filtro.getTipoConcepto()));
            }

            if (filtro.getNaturalezaTributaria() != null) {
                predicates.add(cb.equal(root.get("naturalezaTributaria"), filtro.getNaturalezaTributaria()));
            }

            if (filtro.getPeriodicidad() != null) {
                predicates.add(cb.equal(root.get("periodicidad"), filtro.getPeriodicidad()));
            }

            if (filtro.getEstado() != null) {
                predicates.add(cb.equal(root.get("estado"), filtro.getEstado()));
            }

            if (filtro.getAplicaAPredio() != null) {
                predicates.add(cb.equal(root.get("aplicaAPredio"), filtro.getAplicaAPredio()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
