package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.CajaMunicipal;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCajaMunicipal;

import java.util.ArrayList;
import java.util.List;

public class CajaSpecification {

    public static Specification<CajaMunicipal> conFiltros(String busqueda, EstadoCajaMunicipal estado) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (busqueda != null && !busqueda.trim().isEmpty()) {
                String term = "%" + busqueda.trim().toLowerCase() + "%";
                Predicate porCodigo = cb.like(cb.lower(root.get("codigo")), term);
                Predicate porNombre = cb.like(cb.lower(root.get("nombre")), term);
                Predicate porUbicacion = cb.like(cb.lower(root.get("ubicacion")), term);
                predicates.add(cb.or(porCodigo, porNombre, porUbicacion));
            }

            if (estado != null) {
                predicates.add(cb.equal(root.get("estado"), estado));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
