package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.CuentaCorrienteTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCuentaCorriente;

import java.util.ArrayList;
import java.util.List;

public class CuentaCorrienteSpecification {

    public static Specification<CuentaCorrienteTributaria> conFiltros(String query, EstadoCuentaCorriente estado, Boolean conDeuda) {
        return (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query != null && !query.trim().isEmpty()) {
                String term = "%" + query.trim().toLowerCase() + "%";
                Predicate porCuenta = cb.like(cb.lower(root.get("codigoCuenta")), term);
                Predicate porContribuyente = cb.like(cb.lower(root.get("contribuyente").get("nombreRazonSocial")), term);
                Predicate porDocumento = cb.like(cb.lower(root.get("contribuyente").get("numeroDocumento")), term);
                Predicate porCodigoContrib = cb.like(cb.lower(root.get("contribuyente").get("codigoContribuyente")), term);

                predicates.add(cb.or(porCuenta, porContribuyente, porDocumento, porCodigoContrib));
            }

            if (estado != null) {
                predicates.add(cb.equal(root.get("estado"), estado));
            }

            if (conDeuda != null && conDeuda) {
                predicates.add(cb.greaterThan(root.get("saldoActual"), java.math.BigDecimal.ZERO));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
