package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ContribuyenteTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoContribuyente;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoContribuyente;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Filtros dinámicos para la consulta avanzada del Padrón de Contribuyentes.
 */
public class ContribuyenteSpecification {

    public static Specification<ContribuyenteTributario> conFiltros(
            String query,
            TipoContribuyente tipoContribuyente,
            EstadoContribuyente estado,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            Long oficinaId) {

        return (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query != null && !query.trim().isEmpty()) {
                String search = "%" + query.trim().toLowerCase() + "%";
                Predicate porNombre = cb.like(cb.lower(root.get("nombreRazonSocial")), search);
                Predicate porDoc = cb.like(cb.lower(root.get("numeroDocumento")), search);
                Predicate porCod = cb.like(cb.lower(root.get("codigoContribuyente")), search);
                Predicate porComercial = cb.like(cb.lower(cb.coalesce(root.get("nombreComercial"), "")), search);
                predicates.add(cb.or(porNombre, porDoc, porCod, porComercial));
            }

            if (tipoContribuyente != null) {
                predicates.add(cb.equal(root.get("tipoContribuyente"), tipoContribuyente));
            }

            if (estado != null) {
                predicates.add(cb.equal(root.get("estado"), estado));
            }

            if (fechaDesde != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaRegistro"), fechaDesde));
            }

            if (fechaHasta != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaRegistro"), fechaHasta));
            }

            if (oficinaId != null) {
                predicates.add(cb.equal(root.get("oficinaRegistro").get("id"), oficinaId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
