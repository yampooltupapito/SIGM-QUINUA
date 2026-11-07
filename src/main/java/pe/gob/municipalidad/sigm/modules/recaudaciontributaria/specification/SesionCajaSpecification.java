package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.SesionCajaMunicipal;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoSesionCaja;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SesionCajaSpecification {

    public static Specification<SesionCajaMunicipal> conFiltros(String busqueda,
                                                               UUID cajaId,
                                                               Long cajeroId,
                                                               EstadoSesionCaja estado,
                                                               OffsetDateTime fechaDesde,
                                                               OffsetDateTime fechaHasta) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (busqueda != null && !busqueda.trim().isEmpty()) {
                String term = "%" + busqueda.trim().toLowerCase() + "%";
                Predicate porCodigo = cb.like(cb.lower(root.get("codigoSesion")), term);
                Predicate porCaja = cb.like(cb.lower(root.join("caja").get("nombre")), term);
                predicates.add(cb.or(porCodigo, porCaja));
            }

            if (cajaId != null) {
                predicates.add(cb.equal(root.get("caja").get("id"), cajaId));
            }

            if (cajeroId != null) {
                predicates.add(cb.equal(root.get("cajero").get("id"), cajeroId));
            }

            if (estado != null) {
                predicates.add(cb.equal(root.get("estado"), estado));
            }

            if (fechaDesde != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaApertura"), fechaDesde));
            }

            if (fechaHasta != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaApertura"), fechaHasta));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
