package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ConciliacionCajaMunicipal;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoConciliacionCaja;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ConciliacionSpecification {

    public static Specification<ConciliacionCajaMunicipal> conFiltros(String busqueda,
                                                                      UUID sesionCajaId,
                                                                      EstadoConciliacionCaja estado,
                                                                      OffsetDateTime fechaDesde,
                                                                      OffsetDateTime fechaHasta) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (busqueda != null && !busqueda.trim().isEmpty()) {
                String term = "%" + busqueda.trim().toLowerCase() + "%";
                Predicate porCodigo = cb.like(cb.lower(root.get("codigoConciliacion")), term);
                Predicate porSesion = cb.like(cb.lower(root.join("sesionCaja").get("codigoSesion")), term);
                predicates.add(cb.or(porCodigo, porSesion));
            }

            if (sesionCajaId != null) {
                predicates.add(cb.equal(root.get("sesionCaja").get("id"), sesionCajaId));
            }

            if (estado != null) {
                predicates.add(cb.equal(root.get("estado"), estado));
            }

            if (fechaDesde != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaConciliacion"), fechaDesde));
            }

            if (fechaHasta != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaConciliacion"), fechaHasta));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
