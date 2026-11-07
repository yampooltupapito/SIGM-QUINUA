package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ConfiguracionArbitrioMunicipal;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoConfiguracionArbitrio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoServicioArbitrio;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ConfiguracionArbitrioSpecification {

    public static Specification<ConfiguracionArbitrioMunicipal> conFiltro(
            String query, Integer anio, UUID servicioId, TipoServicioArbitrio tipoServicio, EstadoConfiguracionArbitrio estado) {
        return (root, q, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query != null && !query.trim().isEmpty()) {
                String pattern = "%" + query.trim().toUpperCase() + "%";
                Predicate porCod = cb.like(cb.upper(root.get("codigoConfiguracion")), pattern);
                Predicate porServNom = cb.like(cb.upper(root.get("servicio").get("nombre")), pattern);
                Predicate porNorma = cb.like(cb.upper(root.get("norma").get("numeroNorma")), pattern);
                predicates.add(cb.or(porCod, porServNom, porNorma));
            }

            if (anio != null) {
                predicates.add(cb.equal(root.get("periodo").get("anio"), anio));
            }

            if (servicioId != null) {
                predicates.add(cb.equal(root.get("servicio").get("id"), servicioId));
            }

            if (tipoServicio != null) {
                predicates.add(cb.equal(root.get("servicio").get("tipoServicio"), tipoServicio));
            }

            if (estado != null) {
                predicates.add(cb.equal(root.get("estado"), estado));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
